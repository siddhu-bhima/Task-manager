import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;

public class TaskManagerWithFeatures extends JFrame {
    private JTextField taskField, timeField;
    private JButton addButton, deleteSelectedButton, clearButton;
    private JTable taskTable;
    private TaskTableModel tableModel;
    private JLabel statusLabel;

    // Task class with text, completion status, and scheduled time
    static class Task {
        String text;
        boolean completed;
        String scheduledTime;

        Task(String text, String scheduledTime) {
            this.text = text;
            this.completed = false;
            this.scheduledTime = scheduledTime;
        }
    }

    // Custom table model
    class TaskTableModel extends AbstractTableModel {
        private ArrayList<Task> tasks = new ArrayList<>();
        private String[] columnNames = {"Completed", "Task", "Scheduled Time"};

        @Override
        public int getRowCount() { return tasks.size(); }
        @Override
        public int getColumnCount() { return columnNames.length; }
        @Override
        public String getColumnName(int col) { return columnNames[col]; }
        @Override
        public Class<?> getColumnClass(int col) { return col == 0 ? Boolean.class : String.class; }
        @Override
        public boolean isCellEditable(int row, int col) { return col == 0; }

        @Override
        public Object getValueAt(int row, int col) {
            Task task = tasks.get(row);
            switch (col) {
                case 0: return task.completed;
                case 1: return task.text;
                case 2: return task.scheduledTime;
                default: return "";
            }
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            if (col == 0) {
                tasks.get(row).completed = (Boolean) value;
                fireTableRowsUpdated(row, row);
            }
        }

        public void addTask(Task task) {
            tasks.add(task);
            fireTableRowsInserted(tasks.size() - 1, tasks.size() - 1);
        }

        public void removeTasks(int[] rows) {
            Arrays.sort(rows);
            for (int i = rows.length - 1; i >= 0; i--) {
                if (rows[i] >= 0 && rows[i] < tasks.size()) {
                    tasks.remove(rows[i]);
                }
            }
            fireTableDataChanged();
        }

        public void removeTask(int row) {
            if (row >= 0 && row < tasks.size()) {
                tasks.remove(row);
                fireTableRowsDeleted(row, row);
            }
        }

        public void clearTasks() {
            int size = tasks.size();
            tasks.clear();
            fireTableRowsDeleted(0, size - 1);
        }

        public int getTaskCount() { return tasks.size(); }

        public Task getTask(int row) { return tasks.get(row); }
    }

    public TaskManagerWithFeatures() {
        // Initialize components
        taskField = new JTextField(20);
        timeField = new JTextField(10);
        addButton = new JButton("Add Task");
        deleteSelectedButton = new JButton("Delete Selected");
        clearButton = new JButton("Clear All");
        tableModel = new TaskTableModel();
        taskTable = new JTable(tableModel);
        statusLabel = new JLabel("Tasks: 0");

        // Frame setup with colorful background
        setTitle("Task Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(173, 216, 230)); // Light blue frame background

        // Input panel
        JPanel inputPanel = new JPanel();
        inputPanel.setBackground(new Color(144, 238, 144)); // Light green
        inputPanel.add(new JLabel("Task:"));
        inputPanel.add(taskField);
        inputPanel.add(new JLabel("Time (e.g., 6:30 AM):"));
        inputPanel.add(timeField);
        addButton.setBackground(new Color(135, 206, 250)); // Sky blue
        addButton.setForeground(Color.BLACK);
        inputPanel.add(addButton);
        add(inputPanel, BorderLayout.NORTH);

        // Table setup
        taskTable.setRowHeight(25);
        taskTable.getColumnModel().getColumn(0).setMaxWidth(80);
        taskTable.getColumnModel().getColumn(2).setMaxWidth(120);
        taskTable.setDefaultRenderer(String.class, new TaskRenderer());
        JScrollPane scrollPane = new JScrollPane(taskTable);
        scrollPane.setBackground(new Color(240, 230, 140)); // Khaki
        add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(255, 182, 193)); // Light pink
        deleteSelectedButton.setBackground(new Color(255, 99, 71)); // Tomato red
        deleteSelectedButton.setForeground(Color.WHITE);
        buttonPanel.add(deleteSelectedButton);
        clearButton.setBackground(new Color(255, 215, 0)); // Gold
        clearButton.setForeground(Color.BLACK);
        buttonPanel.add(clearButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Status bar
        JPanel statusPanel = new JPanel();
        statusPanel.setBackground(new Color(221, 160, 221)); // Plum
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.PAGE_END);

        // Event Listeners
        addButton.addActionListener(e -> addTask());
        deleteSelectedButton.addActionListener(e -> deleteSelectedTasks());
        clearButton.addActionListener(e -> clearTasks());

        // Add right-click context menu for specific task deletion
        JPopupMenu popup = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete This Task");
        deleteItem.addActionListener(e -> deleteSpecificTask());
        popup.add(deleteItem);
        taskTable.setComponentPopupMenu(popup);

        // Enable multiple selection for delete selected
        taskTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        updateStatus();
    }

    // Custom renderer for strikethrough and colors
    class TaskRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
                                                       boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (row < tableModel.getRowCount()) {
                Task task = tableModel.getTask(row);
                if (task.completed) {
                    c.setForeground(new Color(34, 139, 34));
                    setText("<html><strike>" + value + "</strike></html>");
                } else {
                    c.setForeground(new Color(178, 34, 34));
                    setText(value.toString());
                }
            }
            return c;
        }
    }

    private void addTask() {
        String taskText = taskField.getText().trim();
        String timeText = timeField.getText().trim().toUpperCase();
        if (taskText.isEmpty() || timeText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Task and time cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(timeText.contains("M") ? "h:mm a" : "HH:mm");
            sdf.setLenient(false);
            sdf.parse(timeText);
            tableModel.addTask(new Task(taskText, timeText));
            taskField.setText("");
            timeField.setText("");
            updateStatus();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Enter a valid time (e.g., '6:30 AM' or '14:00')!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedTasks() {
        int[] selectedRows = taskTable.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "Please select at least one task to delete!", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ArrayList<String> uncompletedTasks = new ArrayList<>();
        for (int row : selectedRows) {
            Task task = tableModel.getTask(row);
            if (!task.completed) {
                uncompletedTasks.add(task.text + " (" + task.scheduledTime + ")");
            }
        }

        int confirm;
        if (!uncompletedTasks.isEmpty()) {
            String message = "The following tasks are not completed:\n" + 
                            String.join("\n", uncompletedTasks) + 
                            "\n\nAre you sure you want to delete them?";
            confirm = JOptionPane.showConfirmDialog(this, message, 
                "Uncompleted Tasks", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        } else {
            confirm = JOptionPane.showConfirmDialog(this, 
                "Delete " + selectedRows.length + " selected task(s)?", 
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        }

        if (confirm == JOptionPane.YES_OPTION) {
            tableModel.removeTasks(selectedRows);
            updateStatus();
            taskTable.clearSelection();
        }
    }

    private void deleteSpecificTask() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a task to delete!", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Task task = tableModel.getTask(selectedRow);
        int confirm;
        if (!task.completed) {
            String message = "Task '" + task.text + " (" + task.scheduledTime + ")' is not completed.\n" +
                            "Are you sure you want to delete it?";
            confirm = JOptionPane.showConfirmDialog(this, message, 
                "Uncompleted Task", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        } else {
            confirm = JOptionPane.showConfirmDialog(this, 
                "Delete task '" + task.text + "'?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        }

        if (confirm == JOptionPane.YES_OPTION) {
            tableModel.removeTask(selectedRow);
            updateStatus();
            taskTable.clearSelection();
        }
    }

    private void clearTasks() {
        if (tableModel.getTaskCount() > 0 && 
            JOptionPane.showConfirmDialog(this, "Clear all tasks?", "Confirm", 
            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            tableModel.clearTasks();
            updateStatus();
        }
    }

    private void updateStatus() {
        statusLabel.setText("Tasks: " + tableModel.getTaskCount());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TaskManagerWithFeatures().setVisible(true));
    }
}
