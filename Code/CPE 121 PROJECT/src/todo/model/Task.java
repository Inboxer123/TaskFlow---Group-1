package todo.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// ABSTRACTION — abstract class with abstract method getCategory()
// INHERITANCE — subclasses extend this class
public abstract class Task implements Displayable, Serializable {

    private static final long serialVersionUID = 1L;

    private static int idCounter = 1;

    // ENCAPSULATION — private fields accessible only through getters/setters
    private final int           id;
    private String              title;
    private String              description;
    private boolean             completed;
    private Priority            priority;
    private final LocalDateTime createdAt;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("MMM dd, yyyy");

    protected Task(String title, String description, Priority priority) {
        this.id          = idCounter++;
        this.title       = title;
        this.description = description;
        this.priority    = priority;
        this.completed   = false;
        this.createdAt   = LocalDateTime.now();
    }

    // ABSTRACTION — forces subclasses to declare their own category
    public abstract String getCategory();

    public String getDeadline() { return null; }

    public int      getId()          { return id;          }
    public String   getTitle()       { return title;       }
    public String   getDescription() { return description; }
    public boolean  isCompleted()    { return completed;   }
    public Priority getPriority()    { return priority;    }
    public String   getCreatedAt()   { return createdAt.format(FMT); }

    public void setTitle(String title)             { this.title       = title;       }
    public void setDescription(String description) { this.description = description; }
    public void setPriority(Priority priority)     { this.priority    = priority;    }
    public void setCompleted(boolean completed)    { this.completed   = completed;   }

    public void toggleComplete() { this.completed = !this.completed; }

    public static void syncIdCounter(int minId) {
        if (minId >= idCounter) {
            idCounter = minId + 1;
        }
    }

    @Override
    public String toDisplayString() {
        return String.format("[%s] #%d | %-22s | %-8s | %s",
                completed ? "V" : " ", id, title, priority.getLabel(), getCategory());
    }

    @Override
    public String toString() { return toDisplayString(); }
}