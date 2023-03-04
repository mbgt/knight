package knight.model;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * Multithread Backtracking Algorithmus für Springerproblem
 *
 * @author matthias.baumgartner@gmx.net
 */
public class Engine {
    public static final int DEFAULT_THREAD_COUNT = 4;

    private final AtomicInteger solutionCount = new AtomicInteger(0);
    private final AtomicLong moveCount = new AtomicLong(0);
    private final BlockingQueue<Board> solutionBuffer = new ArrayBlockingQueue<>(256);
    private final AtomicInteger errors = new AtomicInteger(0);

    private final Board board;  // Spielfeld
    private final int threadCount;  // Grösse des Threadpools
    private final int threadThreshold;  // Zugtiefe ab welcher parallelisiert wird

    private Semaphore concurrent;  // Semaphore limitiert gleichzeitige Threads
    private ExecutorService executor;   // Threadpool
    private boolean stop;   // Anforderung zum Stop der Lösungssuche

    public Engine(Dim size) {
        this(size, DEFAULT_THREAD_COUNT, new Dim[]{});
    }

    public Engine(Dim size, int threadCount) {
        this(size, threadCount, new Dim[]{});
    }

    public Engine(Dim size, int threadCount, Dim... blacks) {
        this.board = new Board(size, blacks);
        this.threadCount = threadCount;
        this.threadThreshold = threshold(board);
    }

    public Engine(Board board, int threadCount) {
        this.board = board;
        this.threadCount = threadCount;
        this.threadThreshold = threshold(board);
    }

    public static void main(String[] args) {
        Engine engine = new Engine(new Dim(5, 5), DEFAULT_THREAD_COUNT);
        Formatter formatter = new Formatter();
        long result = engine.solve(1, 1).map(formatter::format).peek(System.out::println).count();
        System.out.printf("Lösungen: %d, Züge: %d%n", result, engine.moveCount.get());
    }

    /**
     *  Hauptroutine für Multithread Backtracking
     *
     */
    public Stream<Board> solve(int x, int y) {
        executor = Executors.newFixedThreadPool(threadCount);
        concurrent = new Semaphore(threadCount);
        Thread mainThread = new Thread(() -> {
            board.move(new Dim(x, y));  // Startposition
            solve(board);
            count(board.getCount());
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.MINUTES)) {
                    errors.incrementAndGet();
                    throw new RuntimeException("Timeout while waiting for worker threads termination");
                }
            } catch (InterruptedException e) {
                errors.incrementAndGet();
                throw new RuntimeException("Main solution thread interrupted: ", e);
            } finally {
                solution(Board.SENTINEL);
            }
        });
        mainThread.start();
        // Solutions aus Buffer lesen und und als Rückgabewert streamen
        try {
            UnaryOperator<Board> take = b -> {
                try {
                    return solutionBuffer.take();
                } catch (InterruptedException e) {
                    errors.incrementAndGet();
                    throw new RuntimeException("Solution stream interrupted ", e);
                }
            };
            return Stream.iterate(solutionBuffer.take(), b -> b != Board.SENTINEL, take).filter(b -> b != Board.SENTINEL);
        } catch (InterruptedException e) {
            errors.incrementAndGet();
            throw new RuntimeException("Solution stream  interrupted ", e);
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
                // step();
                if (board.isSolved()) {
                    solution(new Board(board));
                } else {
                    if (board.step == threadThreshold) {
                        lock();
                        final Board subBoard = new Board(board);
                        executor.submit(() -> {
                            solve(subBoard);
                            count(subBoard.getCount());
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
        return solutionCount.get();
    }

    public long moves() {
        return moveCount.getOpaque();
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
                solutionCount.incrementAndGet();
            }
            if (!solutionBuffer.offer(board, 1, TimeUnit.SECONDS)) {
                stop();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while offering solution ",e);
        }
    }

    private void count(long moves) {
        moveCount.getAndAdd(moves);
    }

    private void lock() {
        try {
            concurrent.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted  while acquiring worker thread ", e);
        }
    }

    private void unlock() {
        concurrent.release();
    }
}