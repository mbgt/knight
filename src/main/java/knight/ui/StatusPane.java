package knight.ui;

import knight.model.Board;
import knight.ui.Model.Mode;

import javax.swing.*;
import java.text.NumberFormat;
import java.util.Date;
import java.util.stream.IntStream;

public class StatusPane extends JPanel {
    private final Model model;

    private JTextField leftField;
    private JTextField rightField;

    private long statusUpdateTicks = new Date().getTime();
    private long startTicks;

    StatusPane(Model model) {
        this.model = model;
        model.addModeListener(this::mode);
        model.addBoardListener(this::board);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        createFields();
    }

    private void board(Board board) {
        switch (model.getMode()) {
            case RUN -> {
                if (new Date().getTime() - statusUpdateTicks > 100) {
                    statusUpdateTicks = new Date().getTime();
                    setStatus(model.getMoves(), model.getSolutions());
                    setInfo(String.format("Zeit (s): %8.3f", 0.001f * (new Date().getTime() - startTicks)));
                }
            }
            case VIEW -> {
                int index = IntStream.range(0, model.getBoards().size())
                        .filter(i -> model.getBoards().get(i) == board).findFirst().orElse(0);
                rightField.setText(String.format("%d / %d", index + 1, model.getBoards().size()));
            }
            default -> {
            }
        }
    }

    private void mode(Mode mode) {
        switch (mode) {
            case SET -> {
                leftField.setText("");
                rightField.setText("");
            }
            case RUN -> {
                startTicks = new Date().getTime();
                setStatus(0, 0);
                setInfo(String.format("Zeit (ms): %8.3f", 0.0f));
            }
            case VIEW -> {
                setStatus(model.getMoves(), model.getSolutions());
                setInfo(String.format("Zeit (s): %8.3f", 0.001f * (new Date().getTime() - startTicks)));
            }
        }
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
}
