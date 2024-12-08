package org.cosinus.streamer.api.worker;

import error.ActionException;
import org.cosinus.streamer.api.stream.consumer.StreamConsumer;
import org.cosinus.streamer.api.stream.pipeline.PipelineStrategy;
import org.cosinus.streamer.api.stream.pipeline.StreamPipeline;
import org.cosinus.swing.action.execute.ActionModel;

import java.io.IOException;
import java.io.UncheckedIOException;

import static java.util.Optional.ofNullable;

public abstract class PipelineWorker<M extends WorkerModel<V>, T, V>
    extends Worker<M, V>
    implements StreamPipeline<T>, StreamConsumer<T> {

    private final StreamConsumer<T> streamConsumer;

    protected PipelineWorker(ActionModel actionModel, M workerModel) {
        super(actionModel, workerModel);
        this.streamConsumer = streamConsumer();
    }

    @Override
    protected void doWork() {
        try {
            openPipeline();
        } catch (IOException | UncheckedIOException ex) {
            throw new ActionException(ex, "act-load-error");
        }
    }

    @Override
    public final StreamConsumer<T> openPipelineOutputStream(PipelineStrategy pipelineStrategy) {
        return this;
    }

    protected abstract StreamConsumer<T> streamConsumer();

    @Override
    public void accept(T item) {
        checkWorkerStatus();
//        try {
//            java.lang.Thread.sleep(10);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
        ofNullable(streamConsumer)
            .ifPresent(consumer -> consumer.accept(item));
        publish(transform(item));
    }

    protected abstract V transform(T item);

    @Override
    public void close() throws IOException {
        if (streamConsumer != null) {
            streamConsumer.close();
        }
    }
}
