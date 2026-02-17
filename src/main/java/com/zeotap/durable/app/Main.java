package com.zeotap.durable.app;

import com.zeotap.durable.engine.WorkflowRunner;
import com.zeotap.durable.examples.onboarding.OnboardingWorkflow;
import com.zeotap.durable.examples.onboarding.AutoIdWorkflow;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--crash")) {
            System.setProperty("SIMULATE_CRASH", "true");
        }

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter Workflow ID (or press enter for default 'wf-001'):");
        String inputId = scanner.nextLine().trim();
        String workflowId = inputId.isEmpty() ? "wf-001" : inputId;

        System.out.println("Starting workflow: " + workflowId);

        WorkflowRunner runner = new WorkflowRunner();

        try {
            if (workflowId.toLowerCase().startsWith("auto")) {
                runner.run(workflowId, new AutoIdWorkflow());
            } else {
                runner.run(workflowId, new OnboardingWorkflow());
            }
            System.out.println("Workflow finished successfully.");
        } catch (Exception e) {
            System.err.println("Workflow failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
