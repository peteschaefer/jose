package de.jose.util.concurrent;

import java.util.concurrent.*;

/**
 * A thread pool with more flexible shutdown phase.
 * finish() and abort() wait for tasks to finish, without having to enter the shutdown() phase.
 * There is no mechanism for aborting jobs, assuming that jobs are cheap.
 *
 *  extendsd ThreadPoolExecutor
 *  exposes the undelying Queue, so that it can be monitored, or cleared on shutdown
 *
 */
public class QueueThreadPool<R extends Runnable> extends ThreadPoolExecutor
{
    public QueueThreadPool(int queueCapacity) {
        //  assuming that are tasks are memory-bound, we don't want to use hyper-threading
        //  use physical processor count (and let 1 free for the gui)
        this(Math.max(2,Runtime.getRuntime().availableProcessors()/2-1), queueCapacity);
    }

    public QueueThreadPool(int poolSize, int queueCapacity) {
        super(poolSize, poolSize, 0L, TimeUnit.MILLISECONDS,
                (BlockingQueue<Runnable>) new ArrayBlockingQueue<R>(queueCapacity));
    }

    private Thread closingThread = null;
    private int queueWatermark = 0;
    //  number of submitted jobs
    public long jobCount = 0;
    //  numer of completed jobs
    public long completedCount = 0;

    @Override
    public Future<?> submit(Runnable task) {
        Future<?> result = super.submit(task);
        jobCount++;
        if (getQueue().size() > queueWatermark) queueWatermark = getQueue().size();
        return result;
    }

    public void reset()
    {
        getQueue().clear();
        queueWatermark = 0;
        jobCount = 0;
        completedCount = 0;
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
        //while(getActiveCount() > 0 && getQueue().size() > 0) {
        while(completedCount < jobCount) {
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
        completedCount++;
        super.afterExecute(r, t);
        if (closingThread != null && completedCount >= jobCount) closingThread.interrupt();
    }
}
