package com.zeotap.durable.engine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DurableContext {
    private static final Logger logger = LoggerFactory.getLogger(DurableContext.class);
    private final String workflowId;
    private final PersistenceLayer persistence;
    private final ObjectMapper objectMapper;

    public DurableContext(String workflowId, PersistenceLayer persistence) {
        this.workflowId = workflowId;
        this.persistence = persistence;
        this.objectMapper = new ObjectMapper();
    }

    public <T> T step(String stepId, Class<T> returnType, Callable<T> fn) {
        String stepKey = stepId; // In a real scenario, we might want to sequence this if IDs are reused.

        if (persistence.isStepCompleted(workflowId, stepKey)) {
            logger.info("Step {} already completed. Loading result...", stepKey);
            String outputJson = persistence.getStepOutput(workflowId, stepKey);
            try {
                if (outputJson == null || outputJson.equals("null")) {
                    return null;
                }
                return objectMapper.readValue(outputJson, returnType);
            } catch (JsonProcessingException e) {
                logger.error("Failed to deserialize step output", e);
                throw new RuntimeException(e);
            }
        }

        logger.info("Executing step {}...", stepKey);
        T result;
        try {
            result = fn.call();
        } catch (Exception e) {
            logger.error("Step {} failed", stepKey, e);
            persistence.saveStep(workflowId, stepKey, "FAILED", null);
            throw new RuntimeException(e);
        }

        try {
            String outputJson = objectMapper.writeValueAsString(result);
            persistence.saveStep(workflowId, stepKey, "COMPLETED", outputJson);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize step output", e);
            throw new RuntimeException(e);
        }

        return result;
    }

    // Checkpoint for void steps
    public void step(String stepId, Runnable fn) {
        step(stepId, Void.class, () -> {
            fn.run();
            return null;
        });
    }

    // --- Automatic ID Generation ---

    private String generateStepId() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        // Index 0: getStackTrace
        // Index 1: generateStepId
        // Index 2: step (the public wrapper)
        // Index 3: The actual caller in the workflow
        if (stackTrace.length < 4) {
            throw new RuntimeException("Cannot generate step ID: stack trace too shallow");
        }
        StackTraceElement caller = stackTrace[3];
        // Create an ID based on ClassName:MethodName:LineNumber
        // We use only the simple class name to keep it shorter, but full name is safer.
        // Using full class name to avoid collisions across packages.
        return caller.getClassName() + ":" + caller.getMethodName() + ":" + caller.getLineNumber();
    }

    public <T> T step(Class<T> returnType, Callable<T> fn) {
        return step(generateStepId(), returnType, fn);
    }

    public void step(Runnable fn) {
        step(generateStepId(), fn);
    }
}
