package knight.ui;

import knight.model.Board;
import knight.model.Dim;
import knight.model.Engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class Model {
    enum Mode {SET, RUN, VIEW}

    enum BoardSize {
        SIZE_5x5(new Dim(5, 5)),
        SIZE_6x4(new Dim(6, 4)),
        SIZE_6x5(new Dim(6, 5)),
        SIZE_6x6(new Dim(6, 6)),
        SIZE_7x5(new Dim(7, 5)),
        SIZE_7x6(new Dim(7, 6));
        private final Dim size;

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
    private long moves;
    private int solutions;

    private int threadCount = Engine.DEFAULT_THREAD_COUNT;

    private final List<Consumer<Mode>> modeListener = new ArrayList<>();
    private final List<Consumer<Model.BoardSize>> sizeListener = new ArrayList<>();
    private final List<Consumer<Board>> boardListener = new ArrayList<>();

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        if (mode == Mode.SET) {
            moves = 0;
            solutions = 0;
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


    public long getMoves() {
        return moves;
    }

    public void setMoves(long moves) {
        this.moves = moves;
    }

    public int getSolutions() {
        return solutions;
    }

    public void setSolutions(int solutions) {
        this.solutions = solutions;
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
