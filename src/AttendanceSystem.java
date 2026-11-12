
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class AttendanceSystem extends Frame
        implements ActionListener {

    private TextField nameField;
    private TextField rollField;
    private TextField searchDateField;

    private CheckboxGroup attendanceGroup;
    private Checkbox present;
    private Checkbox absent;

    private Button submitButton;
    private Button viewButton;
    private Button clearButton;

    private TextArea outputArea;
    private Label messageLabel;

    public AttendanceSystem() {

        setTitle("Student Attendance Management System");
        setSize(750, 600);
        setLayout(new BorderLayout(10, 10));

        // Heading
        Label heading = new Label(
                "STUDENT ATTENDANCE MANAGEMENT SYSTEM",
                Label.CENTER);

        heading.setFont(
                new Font("Arial", Font.BOLD, 20));

        heading.setForeground(new Color(0, 70, 130));

        add(heading, BorderLayout.NORTH);

        // Input panel
        Panel inputPanel =
                new Panel(new GridLayout(5, 2, 10, 10));

        inputPanel.setBackground(
                new Color(235, 245, 255));

        inputPanel.add(new Label("Student Name:"));
        nameField = new TextField(20);
        inputPanel.add(nameField);

        inputPanel.add(new Label("Roll Number:"));
        rollField = new TextField(20);
        inputPanel.add(rollField);

        inputPanel.add(new Label("Today's Date:"));
        inputPanel.add(new Label(
                LocalDate.now().toString()));

        inputPanel.add(new Label(
                "Search Date (YYYY-MM-DD):"));

        searchDateField = new TextField(
                LocalDate.now().toString(), 20);

        inputPanel.add(searchDateField);

        inputPanel.add(new Label("Attendance Status:"));

        Panel statusPanel =
                new Panel(new FlowLayout(FlowLayout.LEFT));

        attendanceGroup = new CheckboxGroup();

        present = new Checkbox(
                "Present", attendanceGroup, true);

        absent = new Checkbox(
                "Absent", attendanceGroup, false);

        statusPanel.add(present);
        statusPanel.add(absent);

        inputPanel.add(statusPanel);

        // Buttons
        Panel buttonPanel =
                new Panel(new FlowLayout());

        submitButton = new Button("Mark Attendance");
        viewButton = new Button("View Records");
        clearButton = new Button("Clear");

        buttonPanel.add(submitButton);
        buttonPanel.add(viewButton);
        buttonPanel.add(clearButton);

        submitButton.addActionListener(this);
        viewButton.addActionListener(this);
        clearButton.addActionListener(this);

        // Output area
        outputArea = new TextArea(
                "", 15, 80, TextArea.SCROLLBARS_BOTH);

        outputArea.setEditable(false);

        outputArea.setFont(
                new Font("Monospaced", Font.PLAIN, 12));

        messageLabel = new Label("Ready");
        messageLabel.setForeground(Color.BLUE);

        // Center panel
        Panel centerPanel =
                new Panel(new BorderLayout(8, 8));

        centerPanel.add(
                inputPanel, BorderLayout.NORTH);

        centerPanel.add(
                buttonPanel, BorderLayout.CENTER);

        // Output panel
        Panel outputPanel =
                new Panel(new BorderLayout(5, 5));

        outputPanel.add(
                new Label("Attendance Records"),
                BorderLayout.NORTH);

        outputPanel.add(
                outputArea, BorderLayout.CENTER);

        outputPanel.add(
                messageLabel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);
        add(outputPanel, BorderLayout.SOUTH);

        // Close application
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        setLocationRelativeTo(null);
        setVisible(true);

        // Display today's saved attendance on startup
        viewRecords();
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == submitButton) {
            saveAttendance();

        } else if (e.getSource() == viewButton) {
            viewRecords();

        } else if (e.getSource() == clearButton) {
            nameField.setText("");
            rollField.setText("");

            searchDateField.setText(
                    LocalDate.now().toString());

            attendanceGroup.setSelectedCheckbox(present);

            outputArea.setText("");
            messageLabel.setText("Fields cleared");
        }
    }

    // Save attendance in MySQL for today
    private void saveAttendance() {

        String name = nameField.getText().trim();
        String roll = rollField.getText().trim();

        if (name.isEmpty() || roll.isEmpty()) {
            messageLabel.setText(
                    "Enter student name and roll number.");
            return;
        }

        Checkbox selected =
                attendanceGroup.getSelectedCheckbox();

        String status = selected.getLabel().toUpperCase();
        LocalDate today = LocalDate.now();

        String sql =
                "INSERT INTO attendance " +
                "(student_name, roll_no, attendance_date, status) " +
                "VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "student_name = VALUES(student_name), " +
                "status = VALUES(status)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, roll);
            ps.setDate(3, Date.valueOf(today));
            ps.setString(4, status);

            ps.executeUpdate();

            messageLabel.setText(
                    "Attendance saved for " + today);

            // Refresh today's records
            searchDateField.setText(today.toString());
            viewRecords();

        } catch (SQLException ex) {
            messageLabel.setText(
                    "Database error: " + ex.getMessage());

            ex.printStackTrace();
        }
    }

    // View records for the entered date
    private void viewRecords() {

        String dateText =
                searchDateField.getText().trim();

        LocalDate selectedDate;

        try {
            selectedDate = LocalDate.parse(dateText);

            // Reject dates that are not written as YYYY-MM-DD
            if (!selectedDate.toString().equals(dateText)) {
                throw new DateTimeParseException(
                        "Use YYYY-MM-DD format",
                        dateText, 0);
            }

        } catch (DateTimeParseException ex) {
            messageLabel.setText(
                    "Enter date as YYYY-MM-DD.");
            return;
        }

        String sql =
                "SELECT id, student_name, roll_no, " +
                "attendance_date, status " +
                "FROM attendance " +
                "WHERE attendance_date = ? " +
                "ORDER BY roll_no";

        StringBuilder records = new StringBuilder();

        records.append(String.format(
                "%-5s %-20s %-12s %-14s %-10s%n",
                "ID", "NAME", "ROLL NO", "DATE", "STATUS"));

        records.append(
                "--------------------------------------------------------\n");

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(selectedDate));

            try (ResultSet rs = ps.executeQuery()) {

                boolean found = false;

                while (rs.next()) {
                    found = true;

                    records.append(String.format(
                            "%-5d %-20s %-12s %-14s %-10s%n",
                            rs.getInt("id"),
                            rs.getString("student_name"),
                            rs.getString("roll_no"),
                            rs.getDate("attendance_date"),
                            rs.getString("status")));
                }

                if (!found) {
                    records.append(
                            "No records found for ")
                            .append(selectedDate)
                            .append("\n");
                }
            }

            outputArea.setText(records.toString());

            messageLabel.setText(
                    "Records for " + selectedDate);

        } catch (SQLException ex) {
            outputArea.setText(
                    "Unable to retrieve records.\n" +
                    ex.getMessage());

            messageLabel.setText("Database error");
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(
                () -> new AttendanceSystem());
    }
}
