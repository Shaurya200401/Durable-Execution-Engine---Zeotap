# Durable Execution Engine

A lightweight, fault-tolerant execution engine for defining and running durable workflows. This engine ensures that workflows can resume from the last successful step in case of failures (e.g., system crash, restart), making it ideal for robust process automation.

## Project Structure

The project is organized as follows:

- **`src/main/java/com/zeotap/durable/engine`**: Contains the core library logic (DurableContext, WorkflowRunner, Step, PersistenceLayer).
- **`src/main/java/com/zeotap/durable/examples/onboarding`**: Sample workflow implementations (Employee Onboarding, User Registration).
- **`src/main/java/com/zeotap/durable/app`**: The CLI entry point (`Main.java`) for running workflows.
- **`src/main/java/com/zeotap/durable/util`**: Utility classes (e.g., `CheckDb` for inspecting the database).
- **`/docs`**: Documentation and design notes (includes `Prompts.txt`).
- **`durable.db`**: SQLite database file (created automatically) for persisting execution state.

## Initial Setup

### Prerequisites

- Java 17 or higher
- Maven 3.x

### Build

To build the project and run tests:

```bash
mvn clean package
```

### Run

To run the workflow engine CLI:

```bash
java -jar target/durable-engine-1.0-SNAPSHOT.jar
```

Or using Maven:

```bash
mvn exec:java -Dexec.mainClass="com.zeotap.durable.app.Main"
```

## Features

### 1. Durability & Resumption
The engine tracks the execution state of each step in a local SQLite database (`durable.db`).
- **State Persistence**: Before and after every step, the engine records the status (`PENDING`, `COMPLETED`).
- **Resumption**: When the application restarts with the same `WorkflowID`, it skips already completed steps and resumes execution from the first pending step.

### 2. Thread Safety
The engine is designed to be thread-safe for parallel execution scenarios (like the parallel tasks in the Onboarding workflow):
- **ConcurrentHashMaps**: Used for in-memory state tracking where necessary.
- **Synchronized Database Access**: The `PersistenceLayer` ensures that concurrent writes to the SQLite database does not cause race conditions. Connection pooling or synchronized methods are used to serialize critical database operations.
- **Atomic Operations**: Step status updates are atomic to ensure consistency.

### 3. Sequence Tracking & Loops
- **Step IDs**: Each step is identified by a unique key (e.g., `step-1`, `step-2`).
- **Loop Handling**: For workflows with loops, the engine generates unique instance IDs for each iteration (e.g., `step-loop-A-iter-1`, `step-loop-A-iter-2`). This ensures that the same logical step can be executed multiple times within a single workflow instance without ambiguity.
- **Automatic ID Generation**: A helper utility enables defining workflows without manually specifying IDs, automatically assigning them based on execution order.

## Simulation

You can simulate a crash to test durability:

1. Run the application with the `--crash` flag or terminate it manually (Ctrl+C) during execution.
   ```bash
   java -jar target/durable-engine-1.0-SNAPSHOT.jar --crash
   ```
2. Restart the application with the *same* Workflow ID.
3. Observe that it skips the previously completed steps and finishes the rest.

## Design Decisions

- **SQLite**: Chosen for simplicity and zero-configuration persistence. No external database server or Docker container is required.
- **Java Functional Interfaces**: `Consumer<DurableContext>` is used to define workflows code-as-configuration, allowing full flexibility of Java code relative to declarative YAML-based engines.

## Deliverables

- **Engine**: Core logic in `engine` package.
- **Examples**: `OnboardingWorkflow` demonstrating sequential and parallel steps.
- **App**: CLI for interactive execution.
- **Documentation**: This README and `/docs` folder.
