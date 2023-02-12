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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static knight.ui.Model.Mode.RUN;
import static knight.ui.Model.Mode.SET;

class BoardPane extends JPanel {
    private static final String TEMPLATE = """
            <html><body><div style="font-size:24">$num</div></body></html>
            """;

    private final Model model;

    private final ImageIcon knightIcon;
    private final ImageIcon crossIcon;

    private final Map<Integer, JLabel> moveFieldMap = new HashMap<>();
    private final AtomicInteger playing = new AtomicInteger(0);
    private long lastUpdatedTicks = new Date().getTime();

    public BoardPane(Model model) {
        this.model = model;
        this.knightIcon = loadIcon("/knight.png");
        this.crossIcon = loadIcon("/cross.png");
        model.addSizeListener(this::resize);
        model.addBoardListener(board -> {
            if (model.getMode() != RUN || new Date().getTime() - lastUpdatedTicks > 500) {
                lastUpdatedTicks = new Date().getTime();
                playing.set(0);
                draw(board);
            }
        });
        model.addModeListener(mode -> playing.set(0)); // trigger animation executors shutdown
        resize(model.getBoardSize());
    }

    private void draw(Board board) {
        Iterator<JLabel> fieldIerator = Arrays.stream(getComponents()) //
                .map(JLabel.class::cast).iterator();
        moveFieldMap.clear();
        int[][] moves = board.getMoves();
        for (int row = 0; row < model.getBoardSize().dim().y(); row++) {
            for (int col = 0; col < model.getBoardSize().dim().x(); col++) {
                JLabel field = fieldIerator.next();
                int move = moves[col][row];
                if (move == -1) {
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
        if (model.getMode() == SET) {
            JLabel knightField = (JLabel) getComponent(model.getStartPosition().linear(model.getBoardSize().dim()));
            setIcon(knightField, knightIcon);
        }
    }

    private void resize(Model.BoardSize boardSize) {
        removeAll();
        moveFieldMap.clear();
        setLayout(new GridLayout(boardSize.dim().y(), boardSize.dim().x()));

        for (int row = 0; row < boardSize.dim().y(); row++) {
            for (int col = 0; col < boardSize.dim().x(); col++) {
                JLabel label = new JLabel();
                label.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
                label.addMouseListener(onClickField());
                label.setHorizontalAlignment(JLabel.CENTER);
                add(label);
            }
        }
        validate();
        model.setBoard(new Board(boardSize.dim()));
    }

    private MouseListener onClickField() {
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
        setIcon(moveFieldMap.get(1), knightIcon);
        new SwingWorker<Void, Integer>() {


            private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            private final LinkedList<Integer> orderedMoves = moveFieldMap.keySet().stream()
                    .sorted().collect(LinkedList::new, LinkedList::add, LinkedList::addAll);
            private long lastMove = new Date().getTime();
            private final CountDownLatch waitForCompletion = new CountDownLatch(1);

            @Override
            protected Void doInBackground() throws InterruptedException {
                playing.incrementAndGet();
                scheduler.scheduleAtFixedRate(() -> publish(0), 100, 100, TimeUnit.MILLISECONDS);
                waitForCompletion.await();
                return null;
            }

            @Override
            protected void process(List<Integer> steps) {
                if (orderedMoves.isEmpty() || playing.get() == 0) {
                    waitForCompletion.countDown();
                    scheduler.shutdown();
                } else if (new Date().getTime() - lastMove > 800) {
                    lastMove = new Date().getTime();
                    JLabel field = moveFieldMap.get(orderedMoves.remove());
                    field.setIcon(null);
                    field.setForeground(Color.black);
                    // Springer auf nächstes Feld schieben
                    if (orderedMoves.size() > 0) {
                        setIcon(moveFieldMap.get(orderedMoves.peek()), knightIcon);
                    }
                }
            }

            @Override
            protected void done() {
                if (playing.get() > 0) {
                    playing.decrementAndGet();
                }
            }
        }.execute();
    }

    private ImageIcon loadIcon(String name) {
        try (var fileStream = getClass().getResourceAsStream(name)) {
            if (fileStream != null) {
                return new ImageIcon(fileStream.readAllBytes());
            } else {
                throw new RuntimeException("Missing icon: " + name);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setIcon(JLabel label, ImageIcon icon) {
        int width = label.getWidth() > 0 ? label.getWidth() : 600 / model.getBoardSize().dim().x();
        int height = label.getHeight() > 0 ? label.getHeight() : 600 / model.getBoardSize().dim().y();
        label.setIcon(new ImageIcon(icon.getImage().getScaledInstance(
                width - 4, height - 4, Image.SCALE_DEFAULT)));
    }
}
