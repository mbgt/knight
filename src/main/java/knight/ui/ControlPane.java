package knight.ui;

import knight.model.Board;
import knight.ui.Model.BoardSize;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AdjustmentEvent;

import static knight.ui.Model.Mode.*;

class ControlPane extends JPanel {
    private final Model model;
    private final ActionListener actionListener;

    private JScrollBar solutionScrollbar;


    ControlPane(Model model, ActionListener actionListener) {
        super(new CardLayout(2, 2));
        this.model = model;
        this.actionListener = actionListener;
        add(SET.name(), settingsCard());
        add(RUN.name(), runningCard());
        add(VIEW.name(), navigationCard());
        model.addModeListener(this::mode);
    }

    private Component runningCard() {
        JPanel running = new JPanel();
        running.setLayout(new BoxLayout(running, BoxLayout.X_AXIS));
        running.add(new Box.Filler(new Dimension(120, 24),
                new Dimension(320, 24), new Dimension(1920, 24)));
        // Stop Button
        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener(action -> actionListener.onStop());
        stopButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
        running.add(stopButton);
        return running;
    }

    private JPanel settingsCard() {
        JPanel settings = new JPanel();
        settings.setLayout(new BoxLayout(settings, BoxLayout.X_AXIS));
        // Brettgrösse
        JComboBox<BoardSize> sizeCombo = new JComboBox<>(BoardSize.values());
        sizeCombo.setEditable(false);
        sizeCombo.setAlignmentX(LEFT_ALIGNMENT);
        sizeCombo.addItemListener(event -> model.setBoardSize((BoardSize) event.getItem()));
        settings.add(sizeCombo);
        // Start Button
        JButton startButton = new JButton("Start");
        startButton.addActionListener(action -> actionListener.onStart());
        startButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
        settings.add(startButton);
        return settings;
    }

    private JPanel navigationCard() {
        JPanel navigation = new JPanel();
        navigation.setLayout(new BoxLayout(navigation, BoxLayout.X_AXIS));
        JButton showButton = new JButton("Show");
        showButton.addActionListener(action -> actionListener.onShow());
        navigation.add(showButton);
        solutionScrollbar = new JScrollBar(Adjustable.HORIZONTAL);
        solutionScrollbar.setPreferredSize(new Dimension(320, 25));
        solutionScrollbar.addAdjustmentListener(this::adjustBoard);
        navigation.add(solutionScrollbar);
        // Reset Button
        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(action -> model.setMode(SET));
        resetButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
        navigation.add(resetButton);
        return navigation;
    }

    private void adjustBoard(AdjustmentEvent adjustment) {
        Board board = model.getBoards().get(adjustment.getValue());
        model.setBoard(board);
    }

    private void mode(Model.Mode mode) {
        CardLayout layout = (CardLayout) getLayout();
        layout.show(this, mode.name());
        if (mode == VIEW && model.getBoards().size() > 0) {
            solutionScrollbar.setEnabled(true);
            solutionScrollbar.setModel(new DefaultBoundedRangeModel(0, 1, 0, model.getBoards().size()));
        } else {
            solutionScrollbar.setEnabled(false);
            solutionScrollbar.setModel(new DefaultBoundedRangeModel(0, 0, 0, 0));
        }
    }
}
