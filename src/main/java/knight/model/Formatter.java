package knight.model;

import java.util.Arrays;

import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;

public class Formatter {
    String format(Board board) {
        String formatString = "%02d";
        return Arrays.stream(board.getMoves()) //
                .map(row -> Arrays.stream(row).boxed().map("%02d"::formatted).collect(joining(" ")))
                .collect(joining(lineSeparator())) + lineSeparator() + lineSeparator();
    }
}
