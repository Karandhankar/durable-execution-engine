import engine.DurableContext;
import examples.onboarding.EmployeeOnboardingWorkflow;

import java.util.Scanner;

public class App {

    public static void main(String[] args) throws Exception {

        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter workflow ID:");
        String workflowId = scanner.nextLine();

        DurableContext ctx = new DurableContext(workflowId);

        EmployeeOnboardingWorkflow.run(ctx);
    }
}
