/*
 * Copyright 2020 Cosinus Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cosinus.streamer.ui.action.execute.load;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.expand.BinaryExpanderHandler;
import org.cosinus.streamer.ui.action.execute.WorkerExecutor;
import org.cosinus.streamer.api.worker.WorkerListenerHandler;
import org.cosinus.streamer.ui.action.execute.save.SaveActionModel;
import org.cosinus.streamer.ui.action.execute.save.SaveWorkerExecutor;
import org.cosinus.streamer.ui.action.progress.ProgressFormHandler;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.action.execute.ActionExecutor;
import org.cosinus.swing.dialog.DialogHandler;
import org.cosinus.swing.translate.Translator;
import org.springframework.stereotype.Component;

import static java.util.Optional.ofNullable;
import static org.cosinus.streamer.ui.view.text.TextStreamerView.TEXT_EDITOR;
import static org.cosinus.swing.boot.SwingApplicationFrame.applicationFrame;

/**
 * Implementation of {@link ActionExecutor} based on {@link LoadWorker}
 */
@Component
public class LoadActionExecutor<T> extends WorkerExecutor<LoadActionModel<T>, LoadWorkerModel<T>, T> {

    private final StreamerViewHandler streamerViewHandler;

    private final BinaryExpanderHandler binaryExpanderHandler;

    private final DialogHandler dialogHandler;

    private final Translator translator;

    private final SaveWorkerExecutor saveWorkerExecutor;

    protected LoadActionExecutor(final ProgressFormHandler progressFormHandler,
                                 final WorkerListenerHandler workerListenerHandler,
                                 final StreamerViewHandler streamerViewHandler,
                                 final BinaryExpanderHandler binaryExpanderHandler,
                                 final DialogHandler dialogHandler,
                                 final Translator translator,
                                 final SaveWorkerExecutor saveWorkerExecutor) {
        super(progressFormHandler, workerListenerHandler);
        this.streamerViewHandler = streamerViewHandler;
        this.binaryExpanderHandler = binaryExpanderHandler;
        this.dialogHandler = dialogHandler;
        this.translator = translator;
        this.saveWorkerExecutor = saveWorkerExecutor;
    }

    @Override
    public void execute(LoadActionModel<T> actionModel) {
        actionModel.setStreamerToLoad(actionModel.isExpanding() ?
            binaryExpanderHandler.expandStreamer(actionModel.getInitialStreamerToLoad()) :
            (Streamer<T>) actionModel.getInitialStreamerToLoad());

        Streamer<T> streamerToLoad = actionModel.getStreamerToLoad();
        StreamerView<?> currentStreamerView = streamerViewHandler.getCurrentView();
        if (isCurrentStreamerViewDirty() && !streamerToLoad.isDirty() && !isCurrentStreamerSaving() && shouldSave()) {
            saveWorkerExecutor.execute(new SaveActionModel<>(currentStreamerView));
            return;
        }

        StreamerView<T> streamerViewToLoadTo = streamerViewHandler.getStreamerView(
            actionModel.getLocationToLoadTo(),
            ofNullable(actionModel.getStreamerViewNameToLoadIn())
                .filter(viewName -> !streamerToLoad.isTextCompatible() || TEXT_EDITOR.equals(viewName))
                .orElseGet(() -> !streamerToLoad.isParent() && streamerToLoad.isTextCompatible() ? TEXT_EDITOR : null));
        streamerViewToLoadTo.setParentStreamer(streamerToLoad);

        actionModel.setStreamerViewToLoadTo(streamerViewToLoadTo);

        super.execute(actionModel);
    }

    @Override
    protected StreamerView<T> createWorkerListener(LoadActionModel<T> actionModel) {
        return actionModel.getStreamerViewToLoadTo();
    }


    @Override
    protected LoadWorker<T> createSwingWorker(LoadActionModel<T> actionModel) {
        return new LoadWorker<>(
            actionModel,
            actionModel.getStreamerToLoad(),
            actionModel.getStreamerViewToLoadTo(),
            actionModel.getItemToSelectAfterLoad());
    }

    @Override
    public String getHandledAction() {
        return LoadActionModel.class.getName();
    }

    private boolean isCurrentStreamerViewDirty() {
        return ofNullable(streamerViewHandler.getCurrentView())
            .map(StreamerView::isDirty)
            .orElse(false);
    }

    private boolean shouldSave() {
        return dialogHandler.confirm(applicationFrame, translator.translate("do-you-want-to-save"));
    }

    private boolean isCurrentStreamerSaving() {
        return ofNullable(streamerViewHandler.getCurrentView())
            .map(StreamerView::getParentStreamer)
            .map(Streamer::getId)
            .map(saveWorkerExecutor::isWorkerRunning)
            .orElse(false);
    }
}
