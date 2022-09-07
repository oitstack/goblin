package io.github.oitstack.goblin.runtime.utils;



import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class Timeouts {

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool(new ThreadFactory() {

        final AtomicInteger threadCounter = new AtomicInteger(0);

        @Override
        public Thread newThread( Runnable r) {
            Thread thread = new Thread(r, "ducttape-" + threadCounter.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        }
    });

    /**
     * Execute a lambda expression with a timeout. If it completes within the time, the result will be returned.
     * If it does not complete within the time, a TimeoutException will be thrown.
     * If it throws an exception, a RuntimeException wrapping that exception will be thrown.
     *
     * @param timeout  how long to wait
     * @param timeUnit time unit for time interval
     * @param lambda   supplier lambda expression (may throw checked exceptions)
     * @param <T>      return type of the lambda
     * @return the result of the successful lambda expression call
     */
    public static <T> T getWithTimeout(final long timeout, final TimeUnit timeUnit,  final Callable<T> lambda) {

        Future<T> future = EXECUTOR_SERVICE.submit(lambda);
        return callFuture(timeout, timeUnit, future);
    }

    /**
     * Execute a lambda expression with a timeout. If it completes within the time, the result will be returned.
     * If it does not complete within the time, a TimeoutException will be thrown.
     * If it throws an exception, a RuntimeException wrapping that exception will be thrown.
     *
     * @param timeout  how long to wait
     * @param timeUnit time unit for time interval
     * @param lambda   supplier lambda expression (may throw checked exceptions)
     */
    public static void doWithTimeout(final long timeout,  final TimeUnit timeUnit,  final Runnable lambda) {


        Future<?> future = EXECUTOR_SERVICE.submit(lambda);
        callFuture(timeout, timeUnit, future);
    }

    private static <T> T callFuture(final long timeout,final TimeUnit timeUnit,  final Future<T> future) {
        try {
            return future.get(timeout, timeUnit);
        } catch (ExecutionException e) {
            // The cause of the ExecutionException is the actual exception that was thrown
            throw new RuntimeException(e.getCause());
        } catch (TimeoutException | InterruptedException e) {
            throw new io.github.oitstack.goblin.runtime.exceptions.TimeoutException(e);
        }
    }
}

