package gui.performance;

import datalink.CRUDPerformance;
import gui.EmployeeSelectedListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import model.Employee;
import model.Performance;

/**
 *
 * @author Saleh
 */
public class PerformanceSubmit implements EmployeeSelectedListener {

    private JPanel mainPanel;
    private JButton btnSubmit;
    private PerformanceInput performanceInput;
    private StringBuilder stringBuilder;
    private Employee employee;

    public PerformanceSubmit() {

        mainPanel = new JPanel();
        btnSubmit = new JButton("Submit");
        btnSubmit.setEnabled(false);
        btnSubmit.addActionListener(new SubmitPerformance());
        mainPanel.add(btnSubmit);
        stringBuilder = new StringBuilder(145);

    }

    public void setPerformanceInput(PerformanceInput performanceInput) {
        this.performanceInput = performanceInput;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private String prepareInputsFailMessage(List<String> failMessages) {

        this.stringBuilder.setLength(0);

        failMessages.stream().forEach(msg -> {
            this.stringBuilder.append(msg).append("\n");
        });

        return this.stringBuilder.toString();
    }

    private LocalDateTime getDateTimeCombined() {

        LocalDate date = performanceInput.getDate();
        String time = performanceInput.getTime();

        // Formate time from "01:15 pm" to "13:15"
        DateTimeFormatter parseStringTime = DateTimeFormatter.ofPattern("hh:mm a", Locale.UK);
        LocalTime timeParsedFromString_12_to_24 = LocalTime.parse(time, parseStringTime);

        LocalDateTime dateTime = LocalDateTime.of(date, timeParsedFromString_12_to_24);
        return dateTime;
    }

    private void checkFilledInputs() {

        List<String> messages = new ArrayList<>();
        List<Boolean> booleans = new ArrayList<>();

        if (performanceInput.getBoolTfTimeFilled()) {
            booleans.add(true);
        } else {
            booleans.add(false);
            messages.add("Time: input either incorrect or empty");
        }
        if (performanceInput.getBoolDateFilled()) {
            booleans.add(true);
        } else {
            booleans.add(false);
            messages.add("Date: empty input");
        }
        if (performanceInput.getBoolComboState()) {
            booleans.add(true);
        } else {
            booleans.add(false);
            messages.add("State: not selected");
        }
        if (performanceInput.getBoolComboType()) {
            booleans.add(true);
        } else {
            booleans.add(false);
            messages.add("Type: not selected");
        }
        if (performanceInput.getBoolTfAmountFilled()) {
            booleans.add(true);
        } else {
            booleans.add(false);
            messages.add("Amount: input either incorrect or empty");
        }
        if (!performanceInput.getTitle().isBlank() && performanceInput.getTitle().length() >= 10) {
            booleans.add(true);
        } else {
            booleans.add(false);
            messages.add("Title: empty input");
        }

        // Check if List of boolean values are all true or one value at least is false
        // Important note: If booleans.size() is zero;
        // the result of allMatch() is always true.
        // So you have to check the size first, or depending on context needs.
        boolean areAllInputsFilled = booleans.stream().allMatch(Boolean::booleanValue);

        if (areAllInputsFilled) {
            // employee_id, date_time, type_id, state, amount, title, description
            Performance performance = new Performance();
            performance.setEmployeeId(employee.getId());
            performance.setDateTime(getDateTimeCombined());
            performance.setTypeId(performanceInput.getPerformanceType().getId());
            performance.setState(performanceInput.getStateOfPerformance());
            performance.setAmount(performanceInput.getAmount());
            performance.setTitle(performanceInput.getTitle());
            performance.setDescription(performanceInput.getDescription());

            CRUDPerformance.create(performance);
        } else {
            JOptionPane.showConfirmDialog(null,
                    prepareInputsFailMessage(messages), "Incorrect inputs or empty fields",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void employeeSelected(Employee employee) {
        btnSubmit.setEnabled(true);
        this.employee = employee;
    }

    @Override
    public void employeeDeselected() {
        btnSubmit.setEnabled(false);
        this.employee = null;
    }

    class SubmitPerformance implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {

            checkFilledInputs();
        }
    }

}
