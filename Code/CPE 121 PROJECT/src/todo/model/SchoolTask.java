package todo.model;

public class SchoolTask extends Task {

    private static final long serialVersionUID = 1L;

    // ENCAPSULATION — private fields accessible only through getters/setters
    private String deadline;
    private String subject;

    public SchoolTask(String title, String description,
            Priority priority, String deadline, String subject) {
        super(title, description, priority); // INHERITANCE — calls parent constructor
        this.deadline = deadline;
        this.subject = subject;
    }

    @Override
    public String getDeadline() { // POLYMORPHISM — overrides getDeadline() from Task
        return deadline;
    }

    public String getSubject() {
        return subject;
    }

    public void setDeadline(String d) {
        this.deadline = d;
    }

    public void setSubject(String s) {
        this.subject = s;
    }

    @Override
    public String getCategory() { // POLYMORPHISM — overrides abstract getCategory() from Task
        return "School";
    }
}