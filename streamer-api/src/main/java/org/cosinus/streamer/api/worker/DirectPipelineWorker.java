package org.cosinus.streamer.api.worker;

import org.cosinus.swing.action.execute.ActionModel;

public abstract class DirectPipelineWorker<M extends WorkerModel<T>, T> extends PipelineWorker<M, T, T> {

    protected DirectPipelineWorker(ActionModel actionModel, M workerModel) {
        super(actionModel, workerModel);
    }

    @Override
    protected T transform(T item) {
        return item;
    }
}
