package knight.model;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * @author matthias.baumgartner@gmx.net
 */
public class Engine {
    public static final int DEFAULT_THREAD_COUNT = 4;

    private final AtomicInteger solutions = new AtomicInteger(0);
    private final AtomicLong moveCounter = new AtomicLong(0);
    private final BlockingQueue<Board> buffer = new ArrayBlockingQueue<>(256); // solution  buffer
    private final AtomicInteger errors = new AtomicInteger(0);

    private final Board board;
    private final int threadCount;
    private final int threshold;

    private Semaphore concurrent;
    private ExecutorService executor;
    private boolean stop;

    public Engine(Dim size) {
        this(size, DEFAULT_THREAD_COUNT, new Dim[]{});
    }

    public Engine(Dim size, int threadCount) {
        this(size, threadCount, new Dim[]{});
    }

    public Engine(Dim size, int threadCount, Dim... blacks) {
        this.board = new Board(size, blacks);
        this.threadCount = threadCount;
        this.threshold = threshold(board);
    }

    public Engine(Board board, int threadCount) {
        this.board = board;
        this.threadCount = threadCount;
        this.threshold = threshold(board);
    }

    public static void main(String[] args) {
        Engine engine = new Engine(new Dim(5, 5), DEFAULT_THREAD_COUNT);
        Formatter formatter = new Formatter();
        long result = engine.solve(1, 1).map(formatter::format).peek(System.out::println).count();
        System.out.printf("Lösungen: %d, Züge: %d%n", result, engine.moveCounter.get());
    }

    /**
     *
     */
    public Stream<Board> solve(int x, int y) {
        executor = Executors.newFixedThreadPool(threadCount);
        concurrent = new Semaphore(threadCount);
        Thread mainThread = new Thread(() -> {
            board.move(new Dim(x, y));  // Startposition
            solve(board);
            executor.shutdown();
            try {
                executor.awaitTermination(1, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                errors.incrementAndGet();
                throw new RuntimeException(e);
            } finally {
                solution(Board.SENTINEL);
            }
        });
        mainThread.start();
        try {
            UnaryOperator<Board> take = b -> {
                try {
                    return buffer.take();
                } catch (InterruptedException e) {
                    errors.incrementAndGet();
                    throw new RuntimeException(e);
                }
            };
            return Stream.iterate(buffer.take(), b -> b != Board.SENTINEL, take).filter(b -> b != Board.SENTINEL);
        } catch (InterruptedException e) {
            errors.incrementAndGet();
            throw new RuntimeException(e);
        }
    }


    /**
     * Springt um x,y und sucht neue Sprungposition
     */
    private void solve(Board board) {
        if (stop) return;
        for (Dim move : Board.MOVES) {
            if (board.check(move)) {
                board.move(move);
                step();
                if (board.isSolved()) {
                    solution(new Board(board));
                } else {
                    if (board.step == threshold) {
                        lock();
                        final Board subBoard = new Board(board);
                        executor.submit(() -> {
                            solve(subBoard);
                            unlock();
                        });
                    } else {
                        solve(board);
                    }
                }
                board.undo(move);
            }
        }
    }

    public int solutions() {
        return solutions.get();
    }

    public long moves() {
        return moveCounter.get();
    }

    public int errors() {
        return errors.get();
    }

    public void stop() {
        this.stop = true;
    }

    private int threshold(Board board) {
        return (int) (0.1 * board.size.area() + 1 + board.blacks);
    }

    private void solution(Board board) {
        try {
            if (board != Board.SENTINEL) {
                board = new Board(board);
                solutions.incrementAndGet();
            }
            buffer.put(board);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void step() {
        moveCounter.incrementAndGet();
    }

    private void lock() {
        try {
            concurrent.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void unlock() {
        concurrent.release();
    }
}