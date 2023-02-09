package knight.ui;

import knight.model.Board;
import knight.model.Dim;
import knight.model.Engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class Model {
    static final int[][] SAMPLE = {{21, 14, 9, 4, 19}, {8, 1, 20, 15, 10}, {13, 22, 3, 18, 5}, {2, 7, 24, 11, 16}, {23, 12, 17, 6, 25}};

    enum Mode {SET, RUN, VIEW}

    enum BoardSize {
        SIZE_5x5(new Dim(5, 5)), SIZE_6x5(new Dim(6, 5)), SIZE_6x6(new Dim(6, 6));
        private Dim size;

        BoardSize(Dim size) {
            this.size = size;
        }

        public Dim dim() {
            return size;
        }

        public String toString() {
            return size.toString();
        }
    }

    private Model.Mode mode = Mode.SET;

    private Model.BoardSize boardSize = Model.BoardSize.SIZE_5x5;
    private List<Board> boards = Collections.emptyList();
    private Board board = new Board(BoardSize.SIZE_5x5.size);
    private Dim startPosition = new Dim(0,0);

    private int threadCount = Engine.DEFAULT_THREAD_COUNT;

    private List<Consumer<Mode>> modeListener = new ArrayList<>();
    private List<Consumer<Model.BoardSize>> sizeListener = new ArrayList<>();
    private List<Consumer<Board>> boardListener = new ArrayList<>();

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        if (mode == Mode.SET) {
            setBoard(new Board(getBoardSize().dim()));
        }
        modeListener.forEach(listener -> listener.accept(mode));
    }

    public BoardSize getBoardSize() {
        return boardSize;
    }

    public void setBoardSize(BoardSize boardSize) {
        this.boardSize = boardSize;
        sizeListener.forEach(listener -> listener.accept(boardSize));
    }


    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
        boardListener.forEach(listener ->  listener.accept(board));
    }


    public Dim getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(Dim startPosition) {
        this.startPosition = startPosition;
    }


    public List<Board> getBoards() {
        return boards;
    }

    public void setBoards(List<Board> boards) {
        this.boards = boards;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public void addModeListener(Consumer<Mode> listener) {
        modeListener.add(listener);
    }

    public void addSizeListener(Consumer<BoardSize> listener) {
        sizeListener.add(listener);
    }

    public void addBoardListener(Consumer<Board> listener) {
        boardListener.add(listener);
    }
}
