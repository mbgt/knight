package knight.ui;

import knight.model.Board;
import knight.ui.Model.Mode;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.text.NumberFormat;
import java.util.stream.IntStream;

public class StatusPane extends JPanel {
    private final Model model;

    private JTextField leftField;
    private JTextField rightField;

    private long statusUpdateTicks = System.currentTimeMillis();
    private long startTicks;

    StatusPane(Model model) {
        this.model = model;
        model.addModeListener(this::mode);
        model.addBoardListener(this::board);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(BorderFactory.createEtchedBorder());
        createFields();
    }

    private void board(Board board) {
        switch (model.getMode()) {
            case RUN -> {
                if (System.currentTimeMillis() - statusUpdateTicks > 100) {
                    statusUpdateTicks = System.currentTimeMillis();
                    setStatus(model.getMoves(), model.getSolutions());
                    setInfo(String.format("Zeit (s): %8.3f", 0.001f * (System.currentTimeMillis() - startTicks)));
                }
            }
            case VIEW -> {
                int index = IntStream.range(0, model.getBoards().size())
                        .filter(i -> model.getBoards().get(i) == board).findFirst().orElse(0);
                rightField.setText(String.format("%d / %d", index + 1, model.getBoards().size()));
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
                startTicks = System.currentTimeMillis();
                setStatus(0, 0);
                setInfo(String.format("Zeit (s): %8.3f", 0.0f));
            }
            case VIEW -> {
                if (model.getErrors() == 0) {
                    setStatus(model.getMoves(), model.getSolutions());
                } else {
                    rightField.setText("Errors (see log): " + model.getErrors());
                }
                setInfo(String.format("Zeit (s): %8.3f", 0.001f * (System.currentTimeMillis() - startTicks)));
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
        Font font = new Font("Verdana", Font.BOLD, 14);
        Border emptyBorder = BorderFactory.createEmptyBorder(3, 3, 3, 3);
        leftField = new JTextField();
        leftField.setBorder(emptyBorder);
        leftField.setEditable(false);
        leftField.setFont(font);
        add(leftField);

        rightField = new JTextField();
        rightField.setBorder(emptyBorder);
        rightField.setEditable(false);
        rightField.setHorizontalAlignment(JTextField.RIGHT);
        rightField.setFont(font);
        add(rightField);
    }
}
