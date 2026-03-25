/*
 * Copyright 2025 Cosinus Software
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

import org.cosinus.stream.StreamSupplier;
import org.cosinus.streamer.api.Streamable;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.expand.BinaryExpanderHandler;
import org.cosinus.streamer.ui.action.execute.load.image.LoadImageActionModel;
import org.cosinus.streamer.ui.action.execute.load.image.LoadImageWorker;
import org.cosinus.streamer.ui.action.execute.save.SaveActionModel;
import org.cosinus.streamer.ui.action.execute.save.SaveWorkerExecutor;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.streamer.ui.view.image.ImageStreamerView;
import org.cosinus.streamer.ui.view.table.ViewItem;
import org.cosinus.swing.action.execute.ActionExecutor;
import org.cosinus.swing.dialog.DialogHandler;
import org.cosinus.swing.image.LoadThumbnailsWorker;
import org.cosinus.swing.preference.Preferences;
import org.cosinus.swing.translate.Translator;
import org.cosinus.swing.worker.Worker;
import org.cosinus.swing.worker.WorkerExecutor;
import org.cosinus.swing.worker.WorkerListener;
import org.cosinus.swing.worker.WorkerModel;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.cosinus.streamer.ui.action.LoadStreamerAction.LOAD_STREAMER_ACTION_ID;
import static org.cosinus.streamer.ui.preference.StreamerPreferences.PREVIEW;
import static org.cosinus.streamer.ui.view.table.icon.IconTable.PREVIEW_CELL_SIZE;
import static org.cosinus.streamer.ui.view.table.icon.IconView.ICON_VIEW_NAME;
import static org.cosinus.swing.boot.SwingApplicationFrame.applicationFrame;

/**
 * Implementation of {@link ActionExecutor} based on {@link LoadWorker}
 */
@Component
public class LoadActionExecutor extends WorkerExecutor<LoadActionModel<?>, Worker<?, ?, ?>> {

    private final StreamerViewHandler streamerViewHandler;

    private final BinaryExpanderHandler binaryExpanderHandler;

    private final DialogHandler dialogHandler;

    private final Translator translator;

    private final SaveWorkerExecutor saveWorkerExecutor;

    private final Preferences preferences;

    protected LoadActionExecutor(final StreamerViewHandler streamerViewHandler,
                                 final BinaryExpanderHandler binaryExpanderHandler,
                                 final DialogHandler dialogHandler,
                                 final Translator translator,
                                 final SaveWorkerExecutor saveWorkerExecutor,
                                 final Preferences preferences) {
        this.streamerViewHandler = streamerViewHandler;
        this.binaryExpanderHandler = binaryExpanderHandler;
        this.dialogHandler = dialogHandler;
        this.translator = translator;
        this.saveWorkerExecutor = saveWorkerExecutor;
        this.preferences = preferences;
    }

    @Override
    public void execute(LoadActionModel<?> actionModel) {
        if (actionModel instanceof LoadImageActionModel loadImageActionModel) {
            super.execute(loadImageActionModel);
            return;
        }

        actionModel.setStreamerToLoad(actionModel.isExpanding() ?
            binaryExpanderHandler.expandStreamer(actionModel.getInitialStreamerToLoad()) :
            (Streamer) actionModel.getInitialStreamerToLoad());

        Streamer streamerToLoad = actionModel.getStreamerToLoad();
        StreamerView currentStreamerView = streamerViewHandler.getCurrentView();
        if (isCurrentStreamerViewDirty() && !streamerToLoad.isDirty() && !isCurrentStreamerSaving() && shouldSave()) {
            saveWorkerExecutor.execute(new SaveActionModel(currentStreamerView));
            return;
        }

        StreamerView streamerViewToLoadTo = streamerViewHandler.createStreamerView(
            actionModel.getLocationToLoadTo(),
            ofNullable(actionModel.getStreamerViewNameToLoadIn())
                .orElseGet(() -> getDefaultViewName(streamerToLoad)));
        streamerViewHandler.setView(actionModel.getLocationToLoadTo(), streamerViewToLoadTo);
        streamerViewToLoadTo.setParentStreamer(streamerToLoad);

        actionModel.setStreamerViewToLoadTo(streamerViewToLoadTo);

        super.execute(Optional.of(streamerViewToLoadTo)
            .filter(ImageStreamerView.class::isInstance)
            .map(ImageStreamerView.class::cast)
            .<LoadActionModel>map(imageStreamerView ->
                new LoadImageActionModel(streamerToLoad.binaryStreamer(), imageStreamerView))
            .orElse(actionModel));
    }

    private String getDefaultViewName(Streamer<?> streamerToLoad) {
        if (!streamerToLoad.isParent()) {
            //TODO: to remember the last view used for this streamer type
            return streamerViewHandler.getAvailableViewNames(streamerToLoad)
                .stream()
                .findFirst()
                .orElse(null);
        }
        return null;
    }

    @Override
    protected Worker<?, ?, ?> createWorker(final LoadActionModel<?> loadActionModel) {
        loadActionModel
            .getStreamerViewToLoadTo()
            .getLoadWorkerModel()
            .setContentIdentifier(loadActionModel.getItemToSelectAfterLoad());

        Worker worker = loadActionModel instanceof LoadImageActionModel loadImageActionModel ?
            new LoadImageWorker(loadImageActionModel) :
            new LoadWorker<>(loadActionModel);

        return worker
            .registerListener(loadActionModel.getStreamerViewToLoadTo().getLoadWorkerListener())
            .registerListener(createWorkerFinishListener(loadActionModel))
            .registerListener(loadActionModel.getStreamerViewToLoadTo().getLoadingIndicator());
    }

    protected WorkerListener createWorkerFinishListener(final LoadActionModel<?> actionModel) {
        return new WorkerListener() {
            @Override
            public void workerFinished(WorkerModel workerModel) {
                boolean showPreview = preferences.booleanPreference(PREVIEW);
                final StreamSupplier<File> streamSupplier = () -> actionModel.getStreamerViewToLoadTo()
                    .getAllViewItems()
                    .stream()
                    .map(ViewItem::getStreamable)
                    .map(Streamable::getRealPath)
                    .filter(Objects::nonNull)
                    .map(Path::toFile);
                if (showPreview && ICON_VIEW_NAME.equals(actionModel.getStreamerViewToLoadTo().getName())) {
                    new LoadThumbnailsWorker(PREVIEW_CELL_SIZE,
                        files -> actionModel.getStreamerViewToLoadTo().refresh(),
                        streamSupplier,
                        true)
                        .execute();
                    new LoadThumbnailsWorker(PREVIEW_CELL_SIZE,
                        files -> actionModel.getStreamerViewToLoadTo().refresh(),
                        streamSupplier,
                        false)
                        .execute();
                }
            }
        };
    }

    @Override
    public String getHandledAction() {
        return LOAD_STREAMER_ACTION_ID;
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
