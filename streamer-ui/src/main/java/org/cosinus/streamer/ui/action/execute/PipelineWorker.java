package org.cosinus.streamer.ui.action.execute;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.streamer.api.stream.consumer.StreamConsumer;
import org.cosinus.streamer.api.stream.pipeline.PipelineStrategy;
import org.cosinus.streamer.api.stream.pipeline.StreamPipeline;
import org.cosinus.streamer.api.worker.WorkerModel;
import org.cosinus.streamer.ui.error.AbortActionException;
import org.cosinus.streamer.ui.error.ActionException;

import java.io.IOException;
import java.io.UncheckedIOException;

import static java.util.Optional.ofNullable;

public abstract class PipelineWorker<M extends WorkerModel<T>, T>
    extends Worker<M, T>
    implements StreamPipeline<T>, StreamConsumer<T> {

    private static final Logger LOG = LogManager.getLogger(PipelineWorker.class);

    private final StreamConsumer<T> streamConsumer;

    protected PipelineWorker(String id, M workerModel) {
        super(id, workerModel);
        this.streamConsumer = streamConsumer();
    }

    @Override
    protected void doWork() {
        try {
            openPipeline();
        } catch (AbortActionException ex) {
            LOG.trace("Action aborted: " + getId());
        } catch (IOException | UncheckedIOException ex) {
            throw new ActionException(ex, "act-load-error");
        }
    }

    @Override
    final public StreamConsumer<T> openPipelineOutputStream(PipelineStrategy pipelineStrategy) {
        return this;
    }

    protected abstract StreamConsumer<T> streamConsumer();

    @Override
    public void accept(T item) {
//        try
//        {
//            java.lang.Thread.sleep(10);
//        }
//        catch (InterruptedException e)
//        {
//            throw new RuntimeException(e);
//        }
        ofNullable(streamConsumer)
            .ifPresent(consumer -> consumer.accept(item));
        publish(item);
    }

    @Override
    public void close() throws IOException {
        if (streamConsumer != null) {
            streamConsumer.close();
        }
    }
}
