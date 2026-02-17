package engine;

import com.google.gson.Gson;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public class DurableContext {

    private final String workflowId;
    private final AtomicInteger logicalClock = new AtomicInteger(0);
    private final Gson gson = new Gson();

    public DurableContext(String workflowId) {
        this.workflowId = workflowId;
    }

    /**
     * Durable Step Primitive
     * Generic + Memoized + Thread Safe
     */
    public synchronized <T> T step(String stepId, Class<T> type, Callable<T> fn) throws Exception {

        int sequence = logicalClock.incrementAndGet();
        String stepKey = stepId + "_" + sequence;

        try (Connection conn = Database.getConnection()) {

            conn.setAutoCommit(false);

            PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT status, output FROM steps WHERE workflow_id = ? AND step_key = ?"
            );

            checkStmt.setString(1, workflowId);
            checkStmt.setString(2, stepKey);

            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                String status = rs.getString("status");

                if ("COMPLETED".equals(status)) {
                    System.out.println("Skipping already completed step: " + stepKey);
                    String outputJson = rs.getString("output");

                    conn.commit();
                    return gson.fromJson(outputJson, type);
                }
            }

            // Execute step
            T result = fn.call();

            PreparedStatement insertStmt = conn.prepareStatement(
                    "INSERT OR REPLACE INTO steps (workflow_id, step_key, status, output) VALUES (?, ?, ?, ?)"
            );

            insertStmt.setString(1, workflowId);
            insertStmt.setString(2, stepKey);
            insertStmt.setString(3, "COMPLETED");
            insertStmt.setString(4, gson.toJson(result));

            insertStmt.executeUpdate();

            conn.commit();

            return result;
        }
    }
}
