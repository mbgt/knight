package knight.ui;

import knight.model.Engine;
import knight.ui.Model.BoardSize;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

import static java.awt.Event.CTRL_MASK;

public class Menu extends JMenuBar {
    interface MenuActions {
        void onReset();

        void onStartStop();

        void onShow();

        void onQuit();
    }

    private static final int[] THREAD_COUNT = {1, 2, 4, 8, 12, 16};

    private Model model;
    private MenuActions actions;

    private Map<Integer, JRadioButtonMenuItem> threadMenuItemMap = new HashMap<>();
    private Map<BoardSize, JRadioButtonMenuItem> sizeMenuItemMap = new HashMap<>();

    public Menu(Model model, MenuActions actions) {
        this.model = model;
        this.actions = actions;
        createMenu();
    }

    private void createMenu() {
        // Menu "Knight"
        JMenu knightMenu = new JMenu("Knight");
        knightMenu.add(knightItem("Reset", actions::onReset, 'r'));
        knightMenu.add(knightItem("Start/Stop", actions::onStartStop, 't'));
        knightMenu.add(knightItem("Show", actions::onShow, 'h'));
        knightMenu.addSeparator();
        knightMenu.add(knightItem("Quit", actions::onQuit, 'q'));
        add(knightMenu);
        // Menu "Thread"
        add(threadMenu());
    }

    private JMenuItem knightItem(String title, Runnable action, char key) {
        JMenuItem menuItem = new JMenuItem(title);
        menuItem.addActionListener(a -> action.run());
        menuItem.setAccelerator(KeyStroke.getKeyStroke(Character.valueOf(key), CTRL_MASK));
        return menuItem;
    }

    private JMenu boardMenu() {
        JMenu boardMenu = new JMenu("Matrix");
        for (BoardSize boardSize : BoardSize.values()) {
            JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(boardSize.toString());
            menuItem.setSelected(boardSize == model.getBoardSize());
            menuItem.addActionListener(a -> {
                sizeMenuItemMap.get(model.getBoardSize()).setSelected(false);
                model.setBoardSize(boardSize);
            });
            sizeMenuItemMap.put(boardSize, menuItem);
            boardMenu.add(menuItem);
        }
        return boardMenu;
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
