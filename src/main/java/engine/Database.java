package engine;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private static final String URL = "jdbc:sqlite:durable.db";

    static {
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {

            // Enable WAL mode for concurrency
            stmt.execute("PRAGMA journal_mode=WAL;");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS steps (
                    workflow_id TEXT,
                    step_key TEXT,
                    status TEXT,
                    output TEXT,
                    PRIMARY KEY (workflow_id, step_key)
                )
            """);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
    public static int getLastSequence(String workflowId) {

        String sql = "SELECT step_key FROM steps WHERE workflow_id = ? ORDER BY step_key DESC LIMIT 1";

        try (Connection conn = getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, workflowId);
            java.sql.ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String stepKey = rs.getString("step_key");

                // step_key format: stepName_1
                String[] parts = stepKey.split("_");
                return Integer.parseInt(parts[parts.length - 1]);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }


}
