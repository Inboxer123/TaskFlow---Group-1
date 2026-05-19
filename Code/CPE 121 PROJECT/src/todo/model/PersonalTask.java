package todo.model;

public class PersonalTask extends Task {

    private static final long serialVersionUID = 1L;

    // ENCAPSULATION — private field accessible only through getters/setters
    private String deadline;

    public PersonalTask(String title, String description,
            Priority priority, String deadline) {
        super(title, description, priority); // INHERITANCE — calls parent constructor
        this.deadline = deadline;
    }

    @Override
    public String getDeadline() { // POLYMORPHISM — overrides getDeadline() from Task
        return deadline;
    }

    public void setDeadline(String d) {
        this.deadline = d;
    }

    @Override
    public String getCategory() { // POLYMORPHISM — overrides abstract getCategory() from Task
        return "Personal";
    }
}