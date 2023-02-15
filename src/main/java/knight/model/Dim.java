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

    public Dim mirrorHoizontal(int width) {
        return new Dim(width-x()-1, y());
    }

    public Dim mirrorVertical(int height) {
        return new Dim(x(), height-y()-1);
    }

    public Dim mirrorBoth(Dim size) {
        return new Dim(size.x()-x()-1, size.y()-y()-1);
    }
}
