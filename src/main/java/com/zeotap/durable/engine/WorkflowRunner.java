package com.zeotap.durable.engine;

import java.util.function.Consumer;

public class WorkflowRunner {
    private final PersistenceLayer persistence;

    public WorkflowRunner() {
        this.persistence = new PersistenceLayer();
    }

    public void run(String workflowId, Consumer<DurableContext> workflow) {
        DurableContext context = new DurableContext(workflowId, persistence);
        workflow.accept(context);
    }
}
