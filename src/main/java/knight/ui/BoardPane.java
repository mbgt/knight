package knight.ui;

import knight.model.Board;
import knight.model.Dim;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static knight.ui.Model.Mode.SET;

class BoardPane extends JPanel {
    private static final String TEMPLATE = """
            <html><body><div style="font-size:24">$num</div></body></html>
            """;

    private final Model model;

    private final ImageIcon knightIcon;
    private final ImageIcon crossIcon;

    private Map<Integer, JLabel> moveFieldMap = new HashMap<>();

    public BoardPane(Model model) {
        this.model = model;
        this.knightIcon = loadIcon("/knight.png");
        this.crossIcon = loadIcon("/cross.png");
        model.addSizeListener(this::resize);
        model.addBoardListener(this::draw);
        SwingUtilities.invokeLater(() -> {
            resize(model.getBoardSize());
            draw(model.getBoard());
        });
    }

    private void draw(Board board) {
        Iterator<JLabel> fieldIerator = Arrays.asList(getComponents()).stream() //
                .map(JLabel.class::cast).iterator();
        moveFieldMap.clear();
        int[][] moves = board.getMoves();
        for (int row = 0; row < model.getBoardSize().dim().y(); row++) {
            for (int col = 0; col < model.getBoardSize().dim().x(); col++) {
                JLabel field = fieldIerator.next();
                int move = moves[col][row];
                Dim position = new Dim(col, row);
                if (model.getMode() == SET && position.equals(model.getStartPosition())) {
                    setIcon(field, knightIcon);
                    field.setText("");
                } else if (move == -1) {
                    setIcon(field, crossIcon);
                    field.setText("");
                } else if (move == 0) {
                    field.setIcon(null);
                    field.setText("");
                } else {
                    field.setIcon(null);
                    field.setText(TEMPLATE.replace("$num", String.format("%2d", move)));
                    moveFieldMap.put(move, field);
                }
            }
        }
        repaint();
    }

    private void resize(Model.BoardSize boardSize) {
        removeAll();
        moveFieldMap.clear();
        setLayout(new GridLayout(boardSize.dim().y(), boardSize.dim().x()));
        for (int row = 0; row < boardSize.dim().y(); row++) {
            for (int col = 0; col < boardSize.dim().x(); col++) {
                JLabel label = new JLabel();
                label.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
                label.addMouseListener(onClickField(label));
                label.setHorizontalAlignment(JLabel.CENTER);
                add(label);
            }
        }
        model.setBoard(new Board(boardSize.dim()));
        validate();
    }

    private MouseListener onClickField(JLabel label) {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (model.getMode() == SET) {
                    int fieldIndex = Arrays.asList(getComponents()).indexOf(e.getComponent());
                    Dim position = new Dim(fieldIndex % model.getBoardSize().dim().x(),
                            fieldIndex / model.getBoardSize().dim().x());
                    Board board = model.getBoard();
                    if (e.isControlDown()) {
                        model.getBoard().toggleBlack(position.x(), position.y());
                    } else {
                        model.setStartPosition(position);
                    }
                    model.setBoard(board);
                }
            }
        };
    }

    void animate() {
        moveFieldMap.values().forEach(l -> l.setForeground(l.getBackground()));
        setIcon(moveFieldMap.get(1), knightIcon);
        new SwingWorker<Void, Integer>() {
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

            @Override
            protected Void doInBackground() throws Exception {
                AtomicInteger delay = new AtomicInteger(1);
                moveFieldMap.entrySet().stream().sorted(Comparator.comparing(Entry::getKey)).map(Entry::getKey)
                        .forEach(key -> scheduler.schedule(() -> publish(key),
                                800 * delay.getAndIncrement(), TimeUnit.MILLISECONDS));
                scheduler.shutdown();
                scheduler.awaitTermination(100, TimeUnit.SECONDS);
                return null;
            }

            @Override
            protected void process(List<Integer> steps) {
                steps.forEach(step -> {
                    JLabel field = moveFieldMap.get(step);
                    field.setIcon(null);
                    field.setForeground(Color.black);
                    // Springer auf nächstes Feld schieben
                    if (moveFieldMap.containsKey(step + 1)) {
                        setIcon(moveFieldMap.get(step + 1), knightIcon);
                    }
                    field.invalidate();
                });
            }
        }.execute();
    }

    private ImageIcon loadIcon(String fileName) {
        try {
            return new ImageIcon(getClass().getResourceAsStream(fileName).readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setIcon(JLabel label, ImageIcon icon) {
        label.setIcon(new ImageIcon(icon.getImage().getScaledInstance(
                label.getWidth() - 4, label.getHeight() - 4, Image.SCALE_SMOOTH)));
    }
}
