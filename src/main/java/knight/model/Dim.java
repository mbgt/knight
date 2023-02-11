package knight.model;

/**
 * x, y Wertepaar für 2-dimensionale Grösse, Position des Springers, Zugvektor oder Spielfeld
 *
 * @author matthias.baumgartner@gmx.net
 */
public record Dim(int x, int y) {
    public String toString() {
        return String.format("%dx%d", x, y);
    }
    public int area() {
        return x*y;
    }

    public int linear(Dim size) {
        return y*size.x + x;
    }
}
