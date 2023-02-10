package knight.model;

/**
 * x, y Wertepaar für 2-dimensionale Grösse, Position, Vektor oder Fläche
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
}
