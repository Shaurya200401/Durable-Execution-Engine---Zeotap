package com.zeotap.durable.examples.onboarding;

import com.zeotap.durable.engine.DurableContext;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnboardingWorkflow implements Consumer<DurableContext> {
    private static final Logger logger = LoggerFactory.getLogger(OnboardingWorkflow.class);

    @Override
    public void accept(DurableContext ctx) {
        // Step 1: Create Record (Sequential)
        String userId = ctx.step("create_record", String.class, () -> {
            logger.info("Creating employee record...");
            // Simulate DB work
            Thread.sleep(1000);
            return "emp_12345";
        });
        logger.info("User created with ID: " + userId);

        // Simulate crash if flag is set
        if ("true".equals(System.getProperty("SIMULATE_CRASH")) && userId != null) {
            logger.warn("SIMULATING CRASH AFTER STEP 1...");
            System.exit(1);
        }

        // Step 2 & 3: Provision Laptop & Access (Parallel)
        CompletableFuture<String> laptopFuture = CompletableFuture
                .supplyAsync(() -> ctx.step("provision_laptop", String.class, () -> {
                    logger.info("Provisioning laptop for " + userId + "...");
                    Thread.sleep(2000);
                    return "MacBook Pro M3";
                }));

        CompletableFuture<String> accessFuture = CompletableFuture
                .supplyAsync(() -> ctx.step("provision_access", String.class, () -> {
                    logger.info("Granting access for " + userId + "...");
                    Thread.sleep(1500);
                    return "Granted: Jira, Slack, GitHub";
                }));

        // Wait for both to complete
        String laptop = laptopFuture.join();
        String access = accessFuture.join();
        logger.info("Provisioning complete. Laptop: " + laptop + ", Access: " + access);

        // Step 4: Send Welcome Email (Sequential)
        ctx.step("send_email", Void.class, () -> {
            logger.info("Sending welcome email to " + userId + "...");
            Thread.sleep(500);
            return null;
        });

        logger.info("Onboarding workflow completed for " + userId);
    }
}
