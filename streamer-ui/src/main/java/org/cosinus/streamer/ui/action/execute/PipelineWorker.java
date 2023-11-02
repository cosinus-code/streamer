package org.cosinus.streamer.ui.action.execute;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.streamer.api.stream.pipeline.StreamPipeline;
import org.cosinus.streamer.ui.error.AbortActionException;
import org.cosinus.streamer.ui.error.ActionException;

import java.io.IOException;
import java.io.UncheckedIOException;

public abstract class PipelineWorker<M extends WorkerModel<T>, T>
    extends Worker<M, T>
    implements StreamPipeline<T> {

    private static final Logger LOG = LogManager.getLogger(PipelineWorker.class);

    protected PipelineWorker(String id, M workerModel) {
        super(id, workerModel);
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
}
