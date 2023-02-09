package knight.ui;

import knight.ui.Model.Mode;

import javax.swing.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class StatusPane extends JPanel {
    private final Model model;

    private JTextField leftField;
    private JTextField rightField;

    StatusPane(Model model) {
        this.model = model;
        model.addModeListener(this::mode);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        createFields();
    }

    void setInfo(String info) {
        leftField.setText(info);
    }

    void setStatus(long moves, int solutions) {
        rightField.setText(String.format("Züge: %12s Lösungen: %6s",
                        NumberFormat.getIntegerInstance().format(moves),
                NumberFormat.getIntegerInstance().format(solutions)));
    }

    private void createFields() {
        leftField = new JTextField();
        leftField.setEditable(false);
        leftField.setBorder(BorderFactory.createEmptyBorder());
        add(leftField);

        rightField = new JTextField();
        rightField.setEditable(false);
        rightField.setHorizontalAlignment(JTextField.RIGHT);
        rightField.setBorder(BorderFactory.createEmptyBorder());
        add(rightField);
    }

    private void mode(Mode mode) {
        if (mode == Mode.SET) {
            leftField.setText("");
            rightField.setText("");
        }
    }
}
