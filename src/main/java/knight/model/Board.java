package knight.model;

import java.util.Arrays;


/**
 * Spielfeld mit variabeler Grösse {@link Dim} und aktueller Position des Springers
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

    final int[][] board;    // 0 = frei, -1, gesperrt, 1-n Zug des Springers
    final Dim size;
    int blacks;     // Anzahl
    int x, y;   // aktuelle Position
    int step;   // Spielzug

    Board(Board copy) {
        this.size = copy.size;
        this.board = new int[this.size.x()][];
        for (int i = 0; i < this.size.x(); i++) {
            this.board[i] = Arrays.copyOf(copy.board[i], copy.size.y());
        }
        this.blacks = copy.blacks;
        this.step = copy.step;
        this.x = copy.x;
        this.y = copy.y;
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
    }


    /**
     * Toggle zwischen gesperrtem und freiem Spielfeld
     */
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
     * Zug vom aktuellen Feld auf Feld mit Vektor (x,y)
     */
    void move(Dim move) {
        this.x += move.x();
        this.y += move.y();
        this.board[this.x][this.y] = ++step;
    }

    /**
     * Markiert aktuelles Feld und macht Sprungziel um Vektor (x,y) rückgängig
     */
    void undo(Dim move) {
        this.board[this.x][this.y] = 0;
        this.x -= move.x();
        this.y -= move.y();
        this.step--;
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

    public int[][] getMoves() {
        return board;
    }
    boolean isSolved() {
        return step + blacks == size.area();
    }
}
