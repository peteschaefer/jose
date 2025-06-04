package de.jose.util.concurrent;

import java.util.Iterator;
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
        futures = new ConcurrentHashMap<>(queueCapacity);
    }

    private ConcurrentHashMap<Future<R>,Runnable> futures;
    private Thread closingThread = null;
    private int queueWatermark = 0;
    //  number of submitted jobs
    public long jobCount = 0;
    //  number of completed jobs
    //public long completedCount = 0;

    @Override
    public Future<R> submit(Runnable task) {
        Future<R> result = (Future<R>) super.submit(task);
        jobCount++;
        futures.put(result,task);
        if (getQueue().size() > queueWatermark) queueWatermark = getQueue().size();
        return result;
    }

    public void reset()
    {
        getQueue().clear();
        futures.clear();
        queueWatermark = 0;
        jobCount = 0;
    }

    public int getQueueWatermark() {
        return queueWatermark;
    }

    /**
     * drop waiting tasks, waiting for executing tasks to finish
     */
    public void abort() {
        System.out.println("[thread pool aborting... "+futures.size());
        getQueue().clear();
        Iterator<Future<R>> i = futures.keySet().iterator();
        while(i.hasNext()) {
            Future<R> f = i.next();
            if (!f.isDone())
                f.cancel(true);
        }
        futures.clear();
        System.out.println("...thread pool aborted]");
    }

    /**
     * wait for all tasks to finish
     * (note: this is similar to ThreadPoolExecutor.awaitTermination() but without having to enter the shutdown() phase.
     * After all tasks have finished, new tasks may be submitted, again (which is not possible after ThreadPoolExecutor.shutdown());
     *
     */
    public void finish() {
        //while(getActiveCount() > 0 && getQueue().size() > 0) {
        //  note: getActiveCount() is unreliable. May return too early, leaving jobs not done.
        while(!futures.isEmpty()) {
            closingThread = Thread.currentThread();
            try {
                Iterator<Future<R>> i =  futures.keySet().iterator();
                while(i.hasNext()) {
                    Future<R> f = i.next();
                    if (f.isDone())
                        i.remove();
                    else
                        f.get(200, TimeUnit.MILLISECONDS);
                }
            } catch (InterruptedException e) {
                continue;
            } catch (ExecutionException e) {
                //  job threw
                throw new RuntimeException(e.getCause());
            } catch (TimeoutException e) {
                System.err.println("[bored of waiting]");
                continue;
            }
        }
        closingThread = null;
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t)
    {
        super.afterExecute(r, t);
        Future<R> f = (Future<R>) r;
        if (true/*f.isDone()*/) {
            futures.remove(f);
            //System.err.println("[job "+f.isDone()+"]");
            try {
                f.get(200,TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                /*?*/
                System.err.println("[bored of waiting]");
            } catch (ExecutionException e) {
                e.getCause().printStackTrace(System.err);
            } catch (TimeoutException e) {
                System.err.println("[bored of waiting]");
            }
        }
        if (t!=null)
            t.printStackTrace(System.err);
        if (closingThread != null) closingThread.interrupt();
    }
}
