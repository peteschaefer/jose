package de.jose.util;

import de.jose.pgn.PositionFilter;

import java.util.concurrent.*;

/**
 * A thread pool with more flexible shutdown phase.
 * finish() and abort() wait for tasks to finish, without having to enter the shutdown() phase.
 *
 */
public class JoThreadPool<R extends Runnable> extends ThreadPoolExecutor
{
    public JoThreadPool(int queueCapacity) {
        this(Runtime.getRuntime().availableProcessors()-1, queueCapacity);
    }

    public JoThreadPool(int poolSize, int queueCapacity) {
        super(poolSize, poolSize, 0L, TimeUnit.MILLISECONDS,
                (BlockingQueue<Runnable>) new ArrayBlockingQueue<R>(queueCapacity));
    }

    private Thread closingThread = null;
    private int queueWatermark = 0;

    @Override
    public Future<?> submit(Runnable task) {
        Future<?> result = super.submit(task);
        if (getQueue().size() > queueWatermark) queueWatermark = getQueue().size();
        return result;
    }

    public void reset()
    {
        getQueue().clear();
        queueWatermark = 0;
    }

    public int getQueueWatermark() {
        return queueWatermark;
    }

    /**
     * drop waiting tasks, waiting for executing tasks to finish
     */
    public void abort() {
        getQueue().clear();
        finish();
    }

    /**
     * wait for all tasks to finish
     * (note: this is similar to ThreadPoolExecutor.awaitTermination() but without having to enter the shutdown() phase.
     * After all tasks have finished, new tasks may be submitted, again (which is not possible after ThreadPoolExecutor.shutdown());
     *
     */
    public void finish() {
        while(getActiveCount() > 0) {
            closingThread = Thread.currentThread();
            try {
                synchronized(this) {
                    wait();
                }
            } catch (InterruptedException e) {
                continue;
            }
        }
        closingThread = null;
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if (closingThread != null) closingThread.interrupt();
    }
}
