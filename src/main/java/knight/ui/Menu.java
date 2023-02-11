package knight.ui;

import knight.model.Engine;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

import static knight.ui.Model.Mode.RUN;
import static knight.ui.Model.Mode.VIEW;

public class Menu extends JMenuBar {

    private static final int[] THREAD_COUNT = {1, 2, 4, 8, 12, 16};

    private final Model model;
    private final Listener listener;

    private final Map<Integer, JRadioButtonMenuItem> threadMenuItemMap = new HashMap<>();

    private JMenuItem startStopItem;
    private JMenuItem showItem;

    public Menu(Model model, Listener listener) {
        this.model = model;
        this.listener = listener;
        model.addModeListener(this::menuState);
        createMenu();
    }

    private void menuState(Model.Mode mode) {
        startStopItem.setEnabled(mode != VIEW);
        showItem.setEnabled(mode == VIEW);
    }

    private void createMenu() {
        // Menu "Knight"
        JMenu knightMenu = new JMenu("Knight");
        knightMenu.add(knightItem("Reset", listener::onReset));
        startStopItem = knightItem("Start/Stop", () -> {
            if ( model.getMode() == RUN) {
                listener.onStop();
            } else {
                listener.onStart();
            }
        });
        knightMenu.add(startStopItem);
        showItem = knightItem("Show", listener::onShow);
        showItem.setEnabled(false);
        knightMenu.add(showItem);
        knightMenu.addSeparator();
        knightMenu.add(knightItem("Quit", listener::onQuit));
        add(knightMenu);
        // Menu "Thread"
        add(threadMenu());
    }

    private JMenuItem knightItem(String title, Runnable action) {
        JMenuItem menuItem = new JMenuItem(title);
        menuItem.addActionListener(a -> action.run());
        return menuItem;
    }

    private JMenu threadMenu() {
        JMenu threadMenu = new JMenu("Thread");
        for (int threadCount : THREAD_COUNT) {
            JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(Integer.toString(threadCount));
            menuItem.setSelected(threadCount == Engine.DEFAULT_THREAD_COUNT);
            menuItem.addActionListener(a -> {
                threadMenuItemMap.get(model.getThreadCount()).setSelected(false);
                model.setThreadCount(threadCount);
            });
            threadMenuItemMap.put(threadCount, menuItem);
            threadMenu.add(menuItem);
        }
        return threadMenu;
    }
}
