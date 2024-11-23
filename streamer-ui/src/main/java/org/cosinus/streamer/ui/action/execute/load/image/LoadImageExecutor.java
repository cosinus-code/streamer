package org.cosinus.streamer.ui.action.execute.load.image;

import org.cosinus.streamer.api.worker.Worker;
import org.cosinus.streamer.api.worker.WorkerListener;
import org.cosinus.streamer.api.worker.WorkerListenerHandler;
import org.cosinus.streamer.ui.action.execute.WorkerExecutor;
import org.cosinus.streamer.ui.action.execute.load.LoadWorkerModel;
import org.cosinus.streamer.ui.action.progress.ProgressFormHandler;
import org.cosinus.swing.image.UpdatableImage;
import org.springframework.stereotype.Component;

import static org.cosinus.streamer.ui.action.execute.load.image.LoadImageActionModel.LOAD_IMAGE_ACTION_ID;

@Component
public class LoadImageExecutor
    extends WorkerExecutor<LoadImageActionModel, LoadWorkerModel<byte[], UpdatableImage>, UpdatableImage> {

    protected LoadImageExecutor(final ProgressFormHandler progressFormHandler,
                                final WorkerListenerHandler workerListenerHandler) {
        super(progressFormHandler, workerListenerHandler);
    }

    @Override
    public String getHandledAction() {
        return LOAD_IMAGE_ACTION_ID;
    }

    @Override
    protected WorkerListener<LoadWorkerModel<byte[], UpdatableImage>, UpdatableImage>
    createWorkerListener(LoadImageActionModel actionModel) {
        return actionModel.getImageStreamerView();
    }

    @Override
    protected Worker<LoadWorkerModel<byte[], UpdatableImage>, UpdatableImage>
    createSwingWorker(LoadImageActionModel actionModel) {
        return new LoadImageWorker(actionModel);
    }

}
