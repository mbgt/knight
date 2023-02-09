package knight.model;

public interface Blacks {
    Dim[] BLACK_TRIANGLE = {new Dim(0, 0), new Dim(0, 1), new Dim(1, 0),
            new Dim(5, 4), new Dim(5, 5), new Dim(4, 5),
            new Dim(5, 0), new Dim(5, 1), new Dim(4, 0),
            new Dim(0, 5), new Dim(1, 5), new Dim(0, 4)};
    Dim[] BLACK_CORNER = {new Dim(0, 0), new Dim(5, 0), new Dim(5, 5), new Dim(0, 5)};
    Dim[] BLACK_CORENER_2 = {new Dim(0, 0), new Dim(1, 1), new Dim(5, 0), new Dim(4, 1),
            new Dim(5, 5), new Dim(4, 4), new Dim(0, 5), new Dim(1, 4)};
    Dim[] BLACK_CORNER_GUARD = {new Dim(1, 1), new Dim(4, 1), new Dim(4, 4), new Dim(1, 4)};
}
