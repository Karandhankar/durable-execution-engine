package examples.onboarding;

import engine.DurableContext;

import java.util.concurrent.CompletableFuture;

public class EmployeeOnboardingWorkflow {

    public static void run(DurableContext ctx) throws Exception {


        String employeeId = ctx.step("create_record", String.class, () -> {
            System.out.println("Creating employee record...");
            Thread.sleep(2000);
            return "EMP001";
        });

        System.out.println("Employee created with ID: " + employeeId);

        // STEP 2 — Parallel task 1
        CompletableFuture<Void> laptopFuture = CompletableFuture.runAsync(() -> {
            try {
                ctx.step("provision_laptop", String.class, () -> {
                    System.out.println("Provisioning laptop...");
                    Thread.sleep(2000);
                    return "Laptop Ready";
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });




        CompletableFuture<Void> accessFuture = CompletableFuture.runAsync(() -> {
            try {
                ctx.step("grant_access", String.class, () -> {
                    System.out.println("Granting system access...");
                    Thread.sleep(2000);
                    return "Access Granted";
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // Wait for parallel tasks to complete
        CompletableFuture.allOf(laptopFuture, accessFuture).join();

        // STEP 4 — Final sequential step
        ctx.step("send_email", String.class, () -> {
            System.out.println("Sending welcome email...");
            Thread.sleep(1000);
            return "Email Sent";
        });

        System.out.println("Workflow Completed Successfully!");
    }
}
