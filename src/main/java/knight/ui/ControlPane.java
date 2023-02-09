package knight.ui;

import knight.ui.Model.BoardSize;

import javax.swing.*;
import java.awt.*;

import static knight.ui.Model.Mode.*;

class ControlPane extends JPanel {

    interface Events {
        void onStart();

        void onStop();

        void onShow();
    }

    private final Model model;
    private final Events events;

    private JScrollBar solutionScrollbar;

    ControlPane(Model model, Events events) {
        super(new CardLayout(2, 2));
        this.model = model;
        this.events = events;
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
        stopButton.addActionListener(action -> events.onStop());
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
        startButton.addActionListener(action -> events.onStart());
        startButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
        settings.add(startButton);
        return settings;
    }

    private JPanel navigationCard() {
        JPanel navigation = new JPanel();
        navigation.setLayout(new BoxLayout(navigation, BoxLayout.X_AXIS));
        JButton showButton = new JButton("Show");
        showButton.addActionListener(action -> events.onShow());
        navigation.add(showButton);
        solutionScrollbar = new JScrollBar(Adjustable.HORIZONTAL);
        solutionScrollbar.setPreferredSize(new Dimension(320, 24));
        navigation.add(solutionScrollbar);
        // Reset Button
        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(action -> model.setMode(SET));
        resetButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
        navigation.add(resetButton);
        return navigation;
    }

    private void mode(Model.Mode mode) {
        CardLayout layout = (CardLayout) getLayout();
        layout.show(this, mode.name());
        if (mode == VIEW && model.getBoards().size() > 0) {
            solutionScrollbar.setModel(new DefaultBoundedRangeModel(0, 10, 0, model.getBoards().size()));
        }
    }
}
