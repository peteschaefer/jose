package de.jose.util.concurrent;

import java.util.ArrayList;
import java.util.concurrent.Future;

/**
 *
 * @param <R>
 */
public class BatchThreadPool<R extends Runnable> extends QueueThreadPool<R>
{
    private int batchSize;
    private BatchJob batch;

    public BatchThreadPool(int poolSize, int queueCapacity, int batchSize) {
        super(poolSize, queueCapacity);
        this.batchSize = batchSize;
    }

    public BatchThreadPool(int queueCapacity, int batchSize) {
        super(queueCapacity);
        this.batchSize = batchSize;
    }


    public Future submit(Runnable task) {
        if (batch == null) batch = new BatchJob();
        batch.add(task);
        if (batch.size() >= batchSize) flush();
        return null;
    }


    @Override
    public void finish() {
        this.flush();
        super.finish();
    }

    @Override
    public void abort() {
        batch = null;
        super.abort();
    }

    @Override
    public void reset() {
        batch = null;
        super.reset();
    }

    private void flush()
    {
        if (batch!=null) {
            super.submit(batch);
            batch=null;
        }
    }

    private static class BatchJob extends ArrayList<Runnable> implements Runnable
    {
        @Override
        public void run() {
            for (Runnable r : this) r.run();
        }
    }
}
