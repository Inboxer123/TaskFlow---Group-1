package todo.ui;

import todo.exception.InvalidTaskException;
import todo.exception.TaskNotFoundException;
import todo.model.PersonalTask;
import todo.model.Priority;
import todo.model.Task;
import todo.model.SchoolTask;
import todo.model.WorkTask;
import todo.service.TaskService;
import todo.util.InputValidator;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class TodoApp extends JFrame {

    private static final Color C_BG = new Color(13, 17, 30);
    private static final Color C_CARD = new Color(22, 27, 45);
    private static final Color C_INPUT = new Color(30, 36, 58);
    private static final Color C_BORDER = new Color(44, 54, 82);
    private static final Color C_BLUE = new Color(96, 165, 250);
    private static final Color C_GREEN = new Color(74, 222, 128);
    private static final Color C_RED = new Color(248, 113, 113);
    private static final Color C_GOLD = new Color(251, 191, 36);
    private static final Color C_TEXT = new Color(226, 232, 240);
    private static final Color C_MUTED = new Color(100, 116, 139);
    private static final Color C_SEL = new Color(37, 99, 235);

    private static final Font F_TITLE = new Font("Segoe UI", Font.BOLD, 26);
    private static final Font F_LABEL = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font F_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font F_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    private static final int CAT_PERSONAL = 0;
    private static final int CAT_SCHOOL = 1;
    private static final int CAT_WORK = 2;

    // ASSOCIATION — TodoApp depends on TaskService but does not own it
    private final TaskService taskService;

    private JTextField tfTitle;
    private JTextArea taDesc;
    private JComboBox<String> cbCategory;
    private JComboBox<Priority> cbPriority;
    private JTextField tfDeadline;
    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> cbFilter;
    private JLabel lblStats;
    private JProgressBar progressBar;
    private JLabel lblProgress;

    private final java.util.ArrayList<Integer> rowTaskIds = new java.util.ArrayList<>();

    public TodoApp(TaskService taskService) {
        this.taskService = taskService;
        setupWindow();
        buildUI();

        boolean loaded = false;
        try {
            loaded = taskService.loadFromFile();
        } catch (Exception ex) {
            // Corrupt or incompatible save file — ignore and start fresh
        }
        if (!loaded) {
            seedSampleData();
        }

        refreshTable();
    }

    private void setupWindow() {
        setTitle("TaskFlow - Java OOP To-Do Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setMinimumSize(new Dimension(900, 580));
        setLocationRelativeTo(null);
        getContentPane().setBackground(C_BG);
        setLayout(new BorderLayout());
    }

    private void buildUI() {
        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel p = colorPanel(new BorderLayout(), C_CARD);
        p.setBorder(new MatteBorder(0, 0, 1, 0, C_BORDER));
        p.setPreferredSize(new Dimension(0, 60));

        JLabel title = makeLabel("  TaskFlow", F_TITLE, C_BLUE);
        JLabel sub = makeLabel("  Your tasks, your flow.", F_SMALL, C_MUTED);

        JPanel left = colorPanel(new GridLayout(2, 1), C_CARD);
        left.add(title);
        left.add(sub);

        lblStats = makeLabel("", F_LABEL, C_MUTED);
        lblStats.setBorder(new EmptyBorder(0, 0, 0, 18));

        p.add(left, BorderLayout.WEST);
        p.add(lblStats, BorderLayout.EAST);
        return p;
    }

    private JPanel buildCenter() {
        JPanel p = colorPanel(new BorderLayout(12, 0), C_BG);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));
        p.add(buildForm(), BorderLayout.WEST);
        p.add(buildTable(), BorderLayout.CENTER);
        return p;
    }

    private JPanel buildForm() {
        JPanel p = colorPanel(null, C_CARD);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(C_BORDER, 1, true),
                new EmptyBorder(16, 16, 16, 16)));
        p.setPreferredSize(new Dimension(255, 0));

        p.add(sectionLabel("Add New Task"));
        p.add(vgap(10));

        p.add(fieldLabel("Category"));
        cbCategory = makeCombo(new String[] { "Personal", "School", "Work" });
        p.add(cbCategory);
        p.add(vgap(8));

        p.add(fieldLabel("Title *"));
        tfTitle = makeTextField("Enter task title...");
        p.add(tfTitle);
        p.add(vgap(8));

        p.add(fieldLabel("Description"));
        taDesc = new JTextArea(3, 1);
        styleTextArea(taDesc);
        JScrollPane sp = new JScrollPane(taDesc);
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        sp.setBorder(new LineBorder(C_BORDER, 1, true));
        p.add(sp);
        p.add(vgap(8));

        p.add(fieldLabel("Priority"));
        cbPriority = new JComboBox<>(Priority.values());
        styleCombo(cbPriority);
        p.add(cbPriority);
        p.add(vgap(8));

        p.add(fieldLabel("Deadline *"));
        tfDeadline = makeTextField("YYYY-MM-DD");
        p.add(tfDeadline);
        p.add(vgap(2));
        JLabel hint = makeLabel("Format: YYYY-MM-DD", F_SMALL, C_MUTED);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(hint);
        p.add(vgap(14));

        JButton btnAdd = makeButton("Add Task", C_BLUE, C_BG);
        JButton btnClear = makeButton("Clear", C_INPUT, C_MUTED);
        btnAdd.addActionListener(e -> onAddTask());
        btnClear.addActionListener(e -> clearForm());
        p.add(btnAdd);
        p.add(vgap(6));
        p.add(btnClear);
        p.add(Box.createVerticalGlue());
        return p;
    }

    private JPanel buildTable() {
        JPanel p = colorPanel(new BorderLayout(0, 10), C_BG);

        JPanel toolbar = colorPanel(new FlowLayout(FlowLayout.LEFT, 8, 0), C_BG);
        toolbar.add(makeLabel("Filter:", F_LABEL, C_MUTED));

        cbFilter = makeCombo(new String[] { "All", "Pending", "Completed", "High", "Medium", "Low" });
        cbFilter.setPreferredSize(new Dimension(130, 30));
        cbFilter.addActionListener(e -> refreshTable());
        toolbar.add(cbFilter);
        toolbar.add(Box.createHorizontalStrut(14));

        JButton btnToggle = makeButton("Toggle Done", C_GREEN, C_BG);
        JButton btnDelete = makeButton("Delete", C_RED, C_BG);
        JButton btnSort = makeButton("Sort Priority", C_GOLD, C_BG);
        btnToggle.addActionListener(e -> onToggle());
        btnDelete.addActionListener(e -> onDelete());
        btnSort.addActionListener(e -> onSort());
        toolbar.add(btnToggle);
        toolbar.add(btnDelete);
        toolbar.add(btnSort);
        p.add(toolbar, BorderLayout.NORTH);

        String[] cols = { "#", "Category", "Title", "Priority", "Status", "Created", "Deadline" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setFont(F_BODY);
        table.setRowHeight(32);
        table.setBackground(C_CARD);
        table.setForeground(C_TEXT);
        table.setGridColor(C_BORDER);
        table.setSelectionBackground(C_SEL);
        table.setSelectionForeground(Color.WHITE);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader th = table.getTableHeader();
        th.setFont(F_LABEL);
        th.setBackground(C_INPUT);
        th.setForeground(C_MUTED);
        th.setPreferredSize(new Dimension(0, 34));
        ((DefaultTableCellRenderer) th.getDefaultRenderer())
                .setHorizontalAlignment(SwingConstants.LEFT);

        int[] widths = { 36, 90, 260, 80, 80, 130, 120 };
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        table.getColumnModel().getColumn(4).setCellRenderer(new StatusCellRenderer());
        table.getColumnModel().getColumn(6).setCellRenderer(new DeadlineCellRenderer());

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(C_CARD);
        scroll.setBorder(new LineBorder(C_BORDER, 1, true));
        p.add(scroll, BorderLayout.CENTER);

        JPanel prog = colorPanel(new BorderLayout(8, 0), C_BG);
        lblProgress = makeLabel("Progress: 0 / 0", F_SMALL, C_MUTED);
        progressBar = new JProgressBar(0, 100);
        progressBar.setForeground(C_GREEN);
        progressBar.setBackground(C_INPUT);
        progressBar.setBorderPainted(false);
        progressBar.setPreferredSize(new Dimension(0, 8));
        prog.add(lblProgress, BorderLayout.WEST);
        prog.add(progressBar, BorderLayout.CENTER);
        p.add(prog, BorderLayout.SOUTH);

        return p;
    }

    private JPanel buildFooter() {
        JPanel p = colorPanel(new FlowLayout(FlowLayout.CENTER), C_CARD);
        p.setBorder(new MatteBorder(1, 0, 0, 0, C_BORDER));
        p.setPreferredSize(new Dimension(0, 30));
        p.add(makeLabel(
                "by allen, biancs, cassy, cath, ches, jang, rub, jhade, & paulo",
                F_SMALL, C_MUTED));
        return p;
    }

    private void onAddTask() {
        try {
            String title = tfTitle.getText().trim();
            String desc = taDesc.getText().trim();
            Priority priority = (Priority) cbPriority.getSelectedItem();
            String deadline = tfDeadline.getText().trim();
            int catIdx = cbCategory.getSelectedIndex();

            InputValidator.requireNonBlank(title, "Title");
            InputValidator.requireMaxLength(title, 80, "Title");

            InputValidator.requireNonBlank(deadline, "Deadline");
            InputValidator.requireDateFormat(deadline, "Deadline");

            // POLYMORPHISM — declared type is Task; runtime type varies by category
            Task task;
            if (catIdx == CAT_SCHOOL) {
                task = new SchoolTask(title, desc, priority, deadline, "");
            } else if (catIdx == CAT_WORK) {
                task = new WorkTask(title, desc, priority, deadline, "");
            } else {
                task = new PersonalTask(title, desc, priority, deadline);
            }

            taskService.addTask(task);
            clearForm();
            refreshTable();
            saveData();
            showInfo("Task \"" + title + "\" added successfully!");

        } catch (InvalidTaskException ex) {
            showError("Validation Error", ex.getMessage());
        }
    }

    private void onToggle() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showError("No Selection", "Please select a task first.");
            return;
        }
        try {
            taskService.toggleComplete(rowTaskIds.get(row));
            refreshTable();
            saveData();
        } catch (TaskNotFoundException ex) {
            showError("Not Found", ex.getMessage());
        }
    }

    private void onDelete() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showError("No Selection", "Please select a task first.");
            return;
        }

        int id = rowTaskIds.get(row);
        String title = (String) tableModel.getValueAt(row, 2);

        int choice = JOptionPane.showConfirmDialog(this,
                "Delete task: \"" + title + "\"?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                taskService.removeTask(id);
                refreshTable();
                saveData();
            } catch (TaskNotFoundException ex) {
                showError("Not Found", ex.getMessage());
            }
        }
    }

    private void onSort() {
        cbFilter.setSelectedIndex(0);
        repopulate(taskService.getSortedByPriority());
    }

    private void refreshTable() {
        String filter = (String) cbFilter.getSelectedItem();
        List<Task> list;
        switch (filter) {
            case "Pending":
                list = taskService.getPendingTasks();
                break;
            case "Completed":
                list = taskService.getCompletedTasks();
                break;
            case "High":
                list = taskService.getByPriority(Priority.HIGH);
                break;
            case "Medium":
                list = taskService.getByPriority(Priority.MEDIUM);
                break;
            case "Low":
                list = taskService.getByPriority(Priority.LOW);
                break;
            default:
                list = taskService.getAllTasks();
        }
        repopulate(list);
    }

    private void repopulate(List<Task> list) {
        tableModel.setRowCount(0);
        rowTaskIds.clear();

        if (list.isEmpty()) {
            tableModel.addRow(new Object[] { "", "", "No tasks found.", "", "", "", "" });
            updateStats();
            return;
        }

        int rowNum = 1;
        for (Task t : list) {
            String deadline = (t.getDeadline() != null && !t.getDeadline().isEmpty())
                    ? t.getDeadline()
                    : "-";

            tableModel.addRow(new Object[] {
                    rowNum++,
                    t.getCategory(),
                    t.getTitle(),
                    t.getPriority().toString(),
                    t.isCompleted() ? "Done" : "Pending",
                    t.getCreatedAt(),
                    deadline
            });
            rowTaskIds.add(t.getId());
        }
        updateStats();
    }

    private void updateStats() {
        int total = taskService.getTotalCount();
        int done = taskService.getCompletedCount();
        int pending = total - done;
        int pct = (total == 0) ? 0 : done * 100 / total;

        lblStats.setText(total + " tasks  |  " + pending + " pending  |  " + done + " done   ");
        lblProgress.setText("  Progress: " + done + " / " + total + "  ");
        progressBar.setValue(pct);
    }

    private void seedSampleData() {
        try {
            taskService.addTask(new SchoolTask(
                    "Submit OOP Project", "Final Java project submission",
                    Priority.HIGH, "2025-12-15", "CS101"));
            taskService.addTask(new PersonalTask(
                    "Buy groceries", "Milk, eggs, bread, coffee",
                    Priority.MEDIUM, "2025-10-10"));
            taskService.addTask(new SchoolTask(
                    "Review lab report", "Check partner's lab write-up",
                    Priority.MEDIUM, "2025-12-10", "Biology"));
            taskService.addTask(new PersonalTask(
                    "Morning jog", "30-minute run at the park",
                    Priority.LOW, "2025-10-20"));
            taskService.addTask(new SchoolTask(
                    "Prepare presentation slides", "OOP design patterns presentation",
                    Priority.HIGH, "2025-12-20", "CS101"));
            taskService.toggleComplete(2);
        } catch (InvalidTaskException | TaskNotFoundException ignored) {
        }
    }

    private void saveData() {
        try {
            taskService.saveToFile();
        } catch (Exception ex) {
            System.err.println("Warning: could not save tasks – " + ex.getMessage());
        }
    }

    private void clearForm() {
        tfTitle.setText("");
        taDesc.setText("");
        tfDeadline.setText("");
        cbCategory.setSelectedIndex(0);
        cbPriority.setSelectedIndex(0);
    }

    private void showError(String title, String msg) {
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private static JPanel colorPanel(LayoutManager lm, Color bg) {
        JPanel p = (lm == null) ? new JPanel() : new JPanel(lm);
        p.setBackground(bg);
        return p;
    }

    private static JLabel makeLabel(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    private static JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l.setForeground(C_BLUE);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private static JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(F_LABEL);
        l.setForeground(C_MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private static JTextField makeTextField(String tooltip) {
        JTextField f = new JTextField();
        f.setFont(F_BODY);
        f.setBackground(C_INPUT);
        f.setForeground(C_TEXT);
        f.setCaretColor(C_BLUE);
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(C_BORDER, 1, true),
                new EmptyBorder(5, 8, 5, 8)));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        f.setToolTipText(tooltip);
        return f;
    }

    private static void styleTextArea(JTextArea ta) {
        ta.setFont(F_BODY);
        ta.setBackground(C_INPUT);
        ta.setForeground(C_TEXT);
        ta.setCaretColor(C_BLUE);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBorder(new EmptyBorder(6, 8, 6, 8));
    }

    private static JComboBox<String> makeCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        styleCombo(cb);
        return cb;
    }

    private static <T> void styleCombo(JComboBox<T> cb) {
        cb.setFont(F_BODY);
        cb.setBackground(C_INPUT);
        cb.setForeground(C_TEXT);
        cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        cb.setBorder(new LineBorder(C_BORDER, 1, true));
    }

    private static JButton makeButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setFont(F_LABEL);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setBorder(new EmptyBorder(7, 14, 7, 14));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        return b;
    }

    private static Component vgap(int h) {
        return Box.createVerticalStrut(h);
    }

    // INHERITANCE — extends DefaultTableCellRenderer to override rendering behavior
    private static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            String status = (value != null) ? value.toString() : "";
            setForeground(status.equals("Done") ? C_GREEN : C_GOLD);
            setBackground(isSelected ? C_SEL : C_CARD);
            return this;
        }
    }

    // INHERITANCE — same pattern; method overriding customizes cell appearance
    private static class DeadlineCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            String text = (value != null) ? value.toString() : "-";
            setForeground(text.equals("-") ? C_MUTED : C_BLUE);
            setBackground(isSelected ? C_SEL : C_CARD);
            return this;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            TaskService service = new TaskService();
            TodoApp app = new TodoApp(service);
            app.setVisible(true);
        });
    }
}