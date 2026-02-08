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

import org.cosinus.streamer.api.Streamable;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.expand.BinaryExpanderHandler;
import org.cosinus.streamer.ui.action.execute.load.image.LoadImageActionModel;
import org.cosinus.streamer.ui.action.execute.load.image.LoadImageExecutor;
import org.cosinus.streamer.ui.action.execute.save.SaveActionModel;
import org.cosinus.streamer.ui.action.execute.save.SaveWorkerExecutor;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.streamer.ui.view.image.ImageStreamerView;
import org.cosinus.streamer.ui.view.table.ViewItem;
import org.cosinus.swing.action.execute.ActionExecutor;
import org.cosinus.swing.dialog.DialogHandler;
import org.cosinus.swing.file.mimetype.MimeTypeResolver;
import org.cosinus.swing.image.LoadThumbnailsWorker;
import org.cosinus.swing.preference.Preferences;
import org.cosinus.swing.progress.ProgressListener;
import org.cosinus.swing.progress.ProgressModel;
import org.cosinus.swing.translate.Translator;
import org.cosinus.swing.worker.WorkerExecutor;
import org.cosinus.swing.worker.WorkerListener;
import org.cosinus.swing.worker.WorkerModel;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.cosinus.streamer.ui.action.LoadStreamerAction.LOAD_STREAMER_ACTION_ID;
import static org.cosinus.streamer.ui.preference.StreamerPreferences.PREVIEW;
import static org.cosinus.streamer.ui.view.image.ImageStreamerView.IMAGE_VIEWER;
import static org.cosinus.streamer.ui.view.table.icon.IconTable.PREVIEW_CELL_SIZE;
import static org.cosinus.streamer.ui.view.table.icon.IconView.ICON_VIEW_NAME;
import static org.cosinus.streamer.ui.view.text.TextStreamerView.TEXT_EDITOR;
import static org.cosinus.swing.boot.SwingApplicationFrame.applicationFrame;

/**
 * Implementation of {@link ActionExecutor} based on {@link LoadWorker}
 */
@Component
public class LoadActionExecutor<T> extends WorkerExecutor<LoadActionModel<T>, LoadWorkerModel<T>, T, ProgressModel> {

    private final StreamerViewHandler streamerViewHandler;

    private final BinaryExpanderHandler binaryExpanderHandler;

    private final DialogHandler dialogHandler;

    private final Translator translator;

    private final SaveWorkerExecutor saveWorkerExecutor;

    private final LoadImageExecutor loadImageExecutor;

    private final MimeTypeResolver mimeTypeResolver;

    private final Preferences preferences;

    protected LoadActionExecutor(final StreamerViewHandler streamerViewHandler,
                                 final BinaryExpanderHandler binaryExpanderHandler,
                                 final DialogHandler dialogHandler,
                                 final Translator translator,
                                 final SaveWorkerExecutor saveWorkerExecutor,
                                 final LoadImageExecutor loadImageExecutor,
                                 final MimeTypeResolver mimeTypeResolver,
                                 final Preferences preferences) {
        this.streamerViewHandler = streamerViewHandler;
        this.binaryExpanderHandler = binaryExpanderHandler;
        this.dialogHandler = dialogHandler;
        this.translator = translator;
        this.saveWorkerExecutor = saveWorkerExecutor;
        this.loadImageExecutor = loadImageExecutor;
        this.mimeTypeResolver = mimeTypeResolver;
        this.preferences = preferences;
    }

    @Override
    public void execute(LoadActionModel<T> actionModel) {
        actionModel.setStreamerToLoad(actionModel.isExpanding() ?
            binaryExpanderHandler.expandStreamer(actionModel.getInitialStreamerToLoad()) :
            (Streamer<T>) actionModel.getInitialStreamerToLoad());

        Streamer<T> streamerToLoad = actionModel.getStreamerToLoad();
        StreamerView<T, T> currentStreamerView = (StreamerView<T, T>) streamerViewHandler.getCurrentView();
        if (isCurrentStreamerViewDirty() && !streamerToLoad.isDirty() && !isCurrentStreamerSaving() && shouldSave()) {
            saveWorkerExecutor.execute(new SaveActionModel<>(currentStreamerView));
            return;
        }

        StreamerView<T, T> streamerViewToLoadTo = streamerViewHandler.createStreamerView(
            actionModel.getLocationToLoadTo(),
            ofNullable(actionModel.getStreamerViewNameToLoadIn())
                .filter(viewName -> !streamerToLoad.isTextCompatible() || TEXT_EDITOR.equals(viewName))
                .orElseGet(() -> getDefaultViewName(streamerToLoad)));
        streamerViewHandler.setView(actionModel.getLocationToLoadTo(), streamerViewToLoadTo);
        streamerViewToLoadTo.setParentStreamer(streamerToLoad);

        actionModel.setStreamerViewToLoadTo(streamerViewToLoadTo);

        Optional.of(streamerViewToLoadTo)
            .filter(ImageStreamerView.class::isInstance)
            .map(ImageStreamerView.class::cast)
            .map(imageStreamerView ->
                new LoadImageActionModel(streamerToLoad.binaryStreamer(), imageStreamerView))
            .ifPresentOrElse(
                this::startLoadImageExecutor,
                () -> startLoadExecutor(actionModel));
    }

    private void startLoadImageExecutor(LoadImageActionModel loadImageActionModel) {
        cancel(loadImageActionModel.getExecutionId());
        loadImageExecutor.execute(loadImageActionModel);
    }

    private void startLoadExecutor(LoadActionModel<T> actionModel) {
        loadImageExecutor.cancel(actionModel.getExecutionId());
        super.execute(actionModel);
    }

    private String getDefaultViewName(Streamer<?> streamerToLoad) {
        if (!streamerToLoad.isParent()) {
            if (streamerToLoad.getViewId() != null) {
                return streamerToLoad.getViewId();
            }
            if (isImageCompatible(streamerToLoad.getPath())) {
                return IMAGE_VIEWER;
            }
            if (isTextCompatible(streamerToLoad.getPath())) {
                return TEXT_EDITOR;
            }
        }
        return null;
    }

    private boolean isImageCompatible(Path path) {
        return mimeTypeResolver.isImageCompatible(path);
    }

    private boolean isTextCompatible(Path path) {
        return mimeTypeResolver.isTextCompatible(path) ||
            mimeTypeResolver.hasUnknownMimeType(path);
    }

    @Override
    protected StreamerView<T, T> getWorkerListener(LoadActionModel<T> actionModel) {
        return actionModel.getStreamerViewToLoadTo();
    }

    @Override
    protected ProgressListener getProgressListener(LoadActionModel<T> actionModel) {
        return actionModel.getStreamerViewToLoadTo().getLoadingIndicator();
    }


    @Override
    protected LoadWorker<T> createWorker(LoadActionModel<T> actionModel) {
        actionModel
            .getStreamerViewToLoadTo()
            .getLoadWorkerModel()
            .setContentIdentifier(actionModel.getItemToSelectAfterLoad());

        LoadWorker loadWorker = new LoadWorker<>(
            actionModel,
            actionModel.getStreamerToLoad(),
            actionModel.getStreamerViewToLoadTo());

        loadWorker.registerListener(new WorkerListener() {
            @Override
            public void workerFinished(WorkerModel workerModel) {
                boolean showPreview = preferences.booleanPreference(PREVIEW);
                if (showPreview && ICON_VIEW_NAME.equals(actionModel.getStreamerViewToLoadTo().getName())) {
                    new LoadThumbnailsWorker(PREVIEW_CELL_SIZE,
                        files -> actionModel.getStreamerViewToLoadTo().refresh(),
                        () -> actionModel.getStreamerViewToLoadTo()
                            .getAllViewItems()
                            .stream()
                            .map(ViewItem::getStreamable)
                            .map(Streamable::getRealPath)
                            .filter(Objects::nonNull)
                            .map(Path::toFile))
                        .execute();
                }
            }
        });
        return loadWorker;
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
