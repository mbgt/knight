package knight.ui;

import knight.model.Board;
import knight.model.Dim;
import knight.model.Engine;

import javax.swing.*;
import java.awt.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static knight.ui.Model.Mode.*;

public class Main {

    private JFrame frame;
    private BoardPane boardPane;
    private StatusPane statusPane;
    private ControlPane controlPane;

    private Model model = new Model();

    private Engine engine;

    private Optional<Model.Mode> nextMode = Optional.empty();

    public Main() {
        createFrame();
    }

    private void createFrame() {
        frame = new JFrame("Knights");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(640, 640));
        frame.setMinimumSize(new Dimension(360, 360));
        frame.setJMenuBar(new Menu(model, menuActions()));
        frame.setContentPane(createContent());
        frame.pack();
        frame.setVisible(true);
    }


    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout());
        boardPane = new BoardPane(model);
        content.add(boardPane, BorderLayout.CENTER);
        statusPane = new StatusPane(model);
        content.add(statusPane, BorderLayout.SOUTH);
        controlPane = new ControlPane(model, events());
        content.add(controlPane, BorderLayout.NORTH);
        return content;
    }

    private Menu.MenuActions menuActions() {
        return new Menu.MenuActions() {
            @Override
            public void onReset() {
                if (model.getMode() == RUN) {
                    stop(SET);
                } else {
                    model.setMode(SET);
                }
            }

            @Override
            public void onStartStop() {
                if (model.getMode() == RUN) {
                    stop(VIEW);
                } else if (model.getMode() == SET) {
                    start();
                }
            }

            @Override
            public void onShow() {
            }

            @Override
            public void onQuit() {
                System.exit(0);
            }
        };
    }
    private ControlPane.Events events() {
        return new ControlPane.Events() {
            @Override
            public void onStart() {
                start();
            }

            @Override
            public void onStop() {
                stop(SET);
            }

            @Override
            public void onShow() {
                boardPane.animate();
            }
        };
    }

    private void animate(int[][] board) {
        boardPane.setVisible(false);
        //  boardPane.draw(board);
        boardPane.animate();
        boardPane.setVisible(true);
    }

    private void run(Engine engine, int x, int y) {
        List<Board> boards = engine.solve(x, y) //
                .collect(toList());
        boards.stream().findFirst().
                map(Board::getMoves).ifPresent(this::animate);
    }

    private void start() {
        model.setMode(RUN);
        statusPane.setStatus(0, 0);
        statusPane.setInfo("Zeit (s): 0");
        new SwingWorker<List<Board>, Board>() {

            private int solutions = 0;
            private LocalDateTime startTime = LocalDateTime.now();

            @Override
            protected List<Board> doInBackground() throws Exception {
                engine = new Engine(model.getBoard(), model.getThreadCount());
                List<Board> boards = engine.solve(model.getStartPosition().x(), model.getStartPosition().y()) //
                        .peek(this::solution)
                        .collect(toList());
                model.setBoards(boards);
                return boards;
            }

            @Override
            protected void process(List<Board> boards) {
                model.setBoard(boards.get(0));
                statusPane.setStatus(engine.moves(), engine.solutions());
                statusPane.setInfo(String.format("Zeit (s): %d",
                        Duration.between(startTime, LocalDateTime.now()).getSeconds()));
            }

            @Override
            protected void done() {
                synchronized (nextMode) {
                    if (nextMode.isEmpty() || nextMode.get() == VIEW) {
                        model.setMode(VIEW);
                        statusPane.setStatus(engine.moves(), engine.solutions());
                        statusPane.setInfo(String.format("Zeit (s): %d",
                                Duration.between(startTime, LocalDateTime.now()).getSeconds()));
                    } else {
                        model.setMode(SET);
                    }
                    engine = null;
                    nextMode = Optional.empty();
                }
            }

            private void solution(Board board) {
                if (solutions++ % 100 == 0) {
                    publish(board);
                }
            }
        }.execute();
    }

    private void stop(Model.Mode mode) {
        synchronized (nextMode) {
            if (engine != null) {
                nextMode = Optional.of(mode);
                engine.stop();
            }
        }
    }

    private void start(String args[]) {
        int size_x = 5, size_y = 5, x = 0, y = 0;
        if (args.length > 0) {
            if (args[0].matches("\\dx\\d")) {
                size_x = Integer.parseInt(String.valueOf(args[0].charAt(0)));
                size_y = Integer.parseInt(String.valueOf(args[0].charAt(2)));
            } else {
                throw new IllegalArgumentException("Invalid argument: " + args[0]);
            }
            if (args.length == 3) {
                x = Integer.parseInt(args[1]);
                y = Integer.parseInt(args[2]);
            }
            Engine engine = new Engine(new Dim(size_x, size_y));
            run(engine, x, y);
        } else {
            //  animate(MODEL);
        }
    }

    public static void main(String args[]) {
        Main main = new Main();
        main.start(args);
    }
}
