package knight.ui;

import knight.model.Board;
import knight.model.Dim;
import knight.model.Engine;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static knight.ui.Model.Mode.*;

public class Main {

    private BoardPane boardPane;

    private final Model model = new Model();

    private Engine engine;

    private Optional<Model.Mode> nextMode = Optional.empty();

    public Main() {
        createFrame();
    }

    private void createFrame() {
        JFrame frame = new JFrame("Knights");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(640, 640));
        frame.setMinimumSize(new Dimension(360, 360));
        frame.setJMenuBar(new Menu(model, listener()));
        frame.setContentPane(createContent());
        frame.pack();
        frame.setVisible(true);
    }


    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout());
        boardPane = new BoardPane(model);
        content.add(boardPane, BorderLayout.CENTER);
        StatusPane statusPane = new StatusPane(model);
        content.add(statusPane, BorderLayout.SOUTH);
        ControlPane controlPane = new ControlPane(model, listener());
        content.add(controlPane, BorderLayout.NORTH);
        return content;
    }

    private Listener listener() {
        return new Listener() {
            @Override
            public void onReset() {
                if (model.getMode() == RUN) {
                    stop(SET);
                } else {
                    model.setMode(SET);
                }
            }

            @Override
            public void onStart() {

                start();
            }

            @Override
            public void onStop() {
                stop(VIEW);
            }

            @Override
            public void onShow() {
                boardPane.animate();
            }

            @Override
            public void onQuit() {
                System.exit(0);
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
                .toList();
        boards.stream().findFirst().
                map(Board::getMoves).ifPresent(this::animate);
    }

    private void start() {
        model.setMode(RUN);
        new SwingWorker<List<Board>, Board>() {
            @Override
            protected List<Board> doInBackground() {
                engine = new Engine(model.getBoard(), model.getThreadCount());
                List<Board> boards = engine.solve(model.getStartPosition().x(), model.getStartPosition().y()) //
                        .peek(this::publish)
                        .limit(1000000)
                        .collect(toList());
                model.setBoards(boards);
                return boards;
            }

            @Override
            protected void process(List<Board> boards) {
                model.setMoves(engine.moves());
                model.setSolutions(engine.solutions());
                model.setBoard(boards.get(0));
            }

            @Override
            protected void done() {
                synchronized (nextMode) {
                    if (nextMode.isEmpty() || nextMode.get() == VIEW) {
                        model.setMode(VIEW);
                    } else {
                        model.setMode(SET);
                    }
                    engine = null;
                    nextMode = Optional.empty();
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

    private void start(String[] args) {
        int size_x, size_y, x = 0, y = 0;
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
        }  //  animate(MODEL);

    }

    public static void main(String[] args) {
        Main main = new Main();
        main.start(args);
    }
}
