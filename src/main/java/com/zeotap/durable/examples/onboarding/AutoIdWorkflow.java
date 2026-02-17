package com.zeotap.durable.examples.onboarding;

import com.zeotap.durable.engine.DurableContext;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoIdWorkflow implements Consumer<DurableContext> {
    private static final Logger logger = LoggerFactory.getLogger(AutoIdWorkflow.class);

    @Override
    public void accept(DurableContext ctx) {
        logger.info("Starting AutoIdWorkflow...");

        // Step 1: Automatic ID
        String greeting = ctx.step(String.class, () -> {
            logger.info("Generating greeting...");
            return "Hello from AutoID";
        });
        logger.info("Step 1 Result: " + greeting);

        // Crash simulation
        if ("true".equals(System.getProperty("SIMULATE_CRASH"))) {
            logger.warn("SIMULATING CRASH IN AUTO ID WORKFLOW...");
            System.exit(1);
        }

        // Step 2: Automatic ID
        ctx.step(() -> {
            logger.info("Executing void step with auto ID...");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        logger.info("AutoIdWorkflow completed.");
    }
}
