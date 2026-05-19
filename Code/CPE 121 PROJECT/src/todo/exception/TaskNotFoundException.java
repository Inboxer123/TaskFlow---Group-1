package todo.exception;

public class TaskNotFoundException extends Exception {

    private final int taskId;

    public TaskNotFoundException(int taskId) {
        super("Task with ID " + taskId + " was not found.");
        this.taskId = taskId;
    }

    public int getTaskId() { return taskId; }
}