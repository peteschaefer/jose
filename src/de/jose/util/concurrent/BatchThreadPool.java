package de.jose.util.concurrent;

import java.util.ArrayList;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * A thread pool that collects small tasks into batches.
 * - less overhead for thread pool management
 * -> better throughput
 * (higher latency to do batching, but that's not our concern)
 *
 * @param <R>
 */
public class BatchThreadPool<R extends Runnable> extends QueueThreadPool<R>
{
    private int batchSize;
    private BatchJob batch=null;
    private Consumer<ArrayList<R>> onBatchFinished;

    public BatchThreadPool(int poolSize, int queueCapacity, int batchSize) {
        super(poolSize, queueCapacity);
        this.batchSize = batchSize;
    }

    public BatchThreadPool(int queueCapacity, int batchSize) {
        super(queueCapacity);
        this.batchSize = batchSize;
    }


    public Future submit(Runnable task) {
        if (batch == null) batch = new BatchJob(batchSize);
        batch.add((R)task);
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

    public void setOnBatchFinished(Consumer<ArrayList<R>> onBatchFinished) {
        this.onBatchFinished = onBatchFinished;
    }

    private class BatchJob extends ArrayList<R> implements Runnable
    {
        public BatchJob(int initialCapacity) {
            super(initialCapacity);
        }

        @Override
        public void run() {
            for (Runnable r : this) r.run();
            if (onBatchFinished!=null)
                onBatchFinished.accept(this);
        }
    }
}
