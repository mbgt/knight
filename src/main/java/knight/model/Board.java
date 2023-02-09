package knight.model;

import java.util.Arrays;


/**
 * Spielfeld mit variabeler Grösse size {@link Dim} und aktueller Position x,y des Springers für Springerproblem
 *
 * @author matthias.baumgartner@gmx.net
 */
public class Board {
    /**
     * Mögliche Züge des Springers
     */
    static final Dim[] MOVES = {new Dim(1, 2), new Dim(2, 1), new Dim(2, -1), new Dim(1, -2),
            new Dim(-1, -2), new Dim(-2, -1), new Dim(-2, 1), new Dim(-1, 2)};

    static final Board SENTINEL = new Board(new Dim(0, 0));

    final int[][] board;
    final Dim size;
    final int ccyThreshold;
    int blacks;
    int x, y;

    Board(Board copy) {
        this.size = copy.size;
        this.board = new int[this.size.x()][];
        for (int i = 0; i < this.size.x(); i++) {
            this.board[i] = Arrays.copyOf(copy.board[i], copy.size.y());
        }
        this.x = copy.x;
        this.y = copy.y;
        this.blacks = copy.blacks;
        this.ccyThreshold = copy.ccyThreshold;
    }

    public Board(Dim size) {
        this(size, new Dim[]{});

    }

    public Board(Dim size, Dim... blacks) {
        this.board = new int[size.x()][size.y()];
        this.size = size;
        this.blacks = blacks.length;
        for (Dim move : blacks) {
            this.board[move.x()][move.y()] = -1;
        }
        this.ccyThreshold = (int) (0.1 * size.x() * size.y() + 1 + this.blacks);
    }

    public int[][] getMoves() {
        return board;
    }

    public void toggleBlack(int x, int y) {
        if (board[x][y] != -1) {
            board[x][y] = -1;
            blacks++;
        } else {
            board[x][y] = 0;
            blacks--;
        }
    }

    /**
     * Springt vom aktuellen Feld auf Feld mit Vektor (x,y)
     */
    void move(int x, int y, int step) {
        this.x += x;
        this.y += y;
        this.board[this.x][this.y] = step;
    }

    /**
     * Markiert aktuelles Feld und macht Sprungziel um Vektor (x,y) rückgängig
     */
    void undo(int x, int y) {
        this.board[this.x][this.y] = 0;
        this.x -= x;
        this.y -= y;
    }

    /**
     * Prüft ob Sprung by @{@link Dim} Felder zulässig ist
     */
    boolean check(Dim move) {
        if (this.x + move.x() >= 0 && this.x + move.x() < this.size.x() && this.y + move.y() >= 0 && this.y + move.y() < this.size.y()) {
            return board[this.x + move.x()][this.y + move.y()] == 0;
        }
        return false;
    }
}
