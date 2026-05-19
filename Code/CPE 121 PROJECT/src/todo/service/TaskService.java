package todo.service;

import todo.exception.InvalidTaskException;
import todo.exception.TaskNotFoundException;
import todo.model.Priority;
import todo.model.Task;
import todo.util.InputValidator;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TaskService {

    // COMPOSITION — TaskService owns and manages the task list
    private final List<Task> tasks;

    public TaskService() {
        this.tasks = new ArrayList<>();
    }

    public void addTask(Task task) throws InvalidTaskException {
        if (task == null) {
            throw new InvalidTaskException("Task cannot be null.");
        }

        InputValidator.requireNonBlank(task.getTitle(), "Title");
        InputValidator.requireMaxLength(task.getTitle(), 80, "Title");

        InputValidator.requireNonBlank(task.getDeadline(), "Deadline");
        InputValidator.requireDateFormat(task.getDeadline(), "Deadline");

        tasks.add(task);
    }

    public List<Task> getAllTasks() {
        return Collections.unmodifiableList(tasks);
    }

    public Task findById(int id) throws TaskNotFoundException {
        return tasks.stream()
                .filter(t -> t.getId() == id)
                .findFirst()
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    public List<Task> getPendingTasks() {
        return tasks.stream()
                .filter(t -> !t.isCompleted())
                .collect(Collectors.toList());
    }

    public List<Task> getCompletedTasks() {
        return tasks.stream()
                .filter(Task::isCompleted)
                .collect(Collectors.toList());
    }

    public List<Task> getByPriority(Priority priority) {
        return tasks.stream()
                .filter(t -> t.getPriority() == priority)
                .collect(Collectors.toList());
    }

    public List<Task> getSortedByPriority() {
        return tasks.stream()
                .sorted(Comparator.comparingInt(
                        (Task t) -> t.getPriority().ordinal()).reversed())
                .collect(Collectors.toList());
    }

    public void toggleComplete(int id) throws TaskNotFoundException {
        findById(id).toggleComplete();
    }

    public void removeTask(int id) throws TaskNotFoundException {
        tasks.remove(findById(id));
    }

    public int getTotalCount() {
        return tasks.size();
    }

    public int getCompletedCount() {
        return (int) tasks.stream().filter(Task::isCompleted).count();
    }

    private static final String SAVE_FILE = System.getProperty("user.home") + File.separator + "taskflow_save.dat";

    public void saveToFile() throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            oos.writeObject(new ArrayList<>(tasks));
        }
    }

    @SuppressWarnings("unchecked")
    public boolean loadFromFile() throws IOException, ClassNotFoundException {
        File f = new File(SAVE_FILE);
        if (!f.exists())
            return false;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            List<Task> loaded = (List<Task>) ois.readObject();
            tasks.clear();
            tasks.addAll(loaded);

            loaded.stream()
                    .mapToInt(Task::getId)
                    .max()
                    .ifPresent(Task::syncIdCounter);
        }
        return true;
    }
}