package todo.model;

public class WorkTask extends Task {

    private static final long serialVersionUID = 1L;

    // ENCAPSULATION — private fields accessible only through getters/setters
    private String deadline;
    private String team;

    public WorkTask(String title, String description,
            Priority priority, String deadline, String team) {
        super(title, description, priority); // INHERITANCE — calls parent constructor
        this.deadline = deadline;
        this.team = team;
    }

    @Override
    public String getDeadline() { // POLYMORPHISM — overrides getDeadline() from Task
        return deadline;
    }

    public String getTeam() {
        return team;
    }

    public void setDeadline(String d) {
        this.deadline = d;
    }

    public void setTeam(String t) {
        this.team = t;
    }

    @Override
    public String getCategory() { // POLYMORPHISM — overrides abstract getCategory() from Task
        return "Work";
    }
}