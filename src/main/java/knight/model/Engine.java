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
    private final Board board;
    private final BlockingQueue<Board> buffer = new ArrayBlockingQueue<>(32); // solution  buffer
    private final int threadCount;

    private Semaphore concurrent;
    private ExecutorService executor;
    private boolean stop;
    private AtomicInteger errors = new AtomicInteger(0);

    public Engine(Dim size) {
        this(size, DEFAULT_THREAD_COUNT, new Dim[]{});
    }
    public Engine(Dim size, int threadCount) {
        this(size, threadCount, new Dim[]{});
    }

    public Engine(Dim size, int threadCount, Dim... blacks) {
        board = new Board(size, blacks);
        this.threadCount = threadCount;
    }

    public Engine(Board board, int threadCount) {
        this.board = board;
        this.threadCount = threadCount;
    }

    public static void main(String[] args) {
        Engine engine = new Engine(new Dim(5, 5), DEFAULT_THREAD_COUNT);
        Formatter formatter = new Formatter();
        long result = engine.solve(1, 1).map(formatter::format).peek(System.out::println).count();
        System.out.println(String.format("Lösungen: %d, Züge: %d", result, engine.moveCounter.get()));
    }

    /**
     *
     */
    public Stream<Board> solve(int x, int y) {
        executor = Executors.newFixedThreadPool(threadCount);
        concurrent = new Semaphore(threadCount);
        Thread mainThread = new Thread(() -> {
            solve(board, x, y, board.blacks+1);
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

    /**
     * Springt um x,y und sucht neue Sprungposition
     *
     * @param x
     * @param x
     */
    private void solve(Board board, int x, int y, int cnt) {
        if (stop) return;
        board.move(x, y, cnt);
        if (cnt < 6 + board.blacks) {
            System.out.println(String.format("Step: %d, x: %d, y: %d", cnt, x, y));
        }
        if (cnt == board.size.x() * board.size.y()) {
            solution(new Board(board));
        } else {
            for (Dim move : Board.MOVES) {
                if (board.check(move)) {
                    move(x, y, move);
                    if (cnt == board.ccyThreshold) {
                        lock();
                        final Board subBoard = new Board(board);
                        Future<?> solveFuture = executor.submit(() -> {
                            solve(subBoard, move.x(), move.y(), cnt + 1);
                            unlock();
                        });
                    } else {
                        solve(board, move.x(), move.y(), cnt + 1);
                    }
                }
            }
        }
        board.undo(x, y);
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

    private void move(int x, int y, Dim move) {
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