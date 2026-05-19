package todo.model;

public enum Priority {
    LOW("Low", "\uD83D\uDFE2"),
    MEDIUM("Medium", "\uD83D\uDFE1"),
    HIGH("High", "\uD83D\uDD34");

    private final String label;
    private final String icon;

    Priority(String label, String icon) {
        this.label = label;
        this.icon  = icon;
    }

    public String getLabel() { return label; }
    public String getIcon()  { return icon;  }

    @Override
    public String toString() { return icon + " " + label; }
}