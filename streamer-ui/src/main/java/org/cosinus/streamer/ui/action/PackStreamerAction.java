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

package org.cosinus.streamer.ui.action;

import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.expand.BinaryExpanderHandler;
import org.cosinus.streamer.pack.archive.ArchiveExpander;
import org.cosinus.streamer.ui.action.execute.load.LoadActionExecutor;
import org.cosinus.streamer.ui.action.execute.pack.PackActionModel;
import org.cosinus.streamer.ui.action.execute.pack.PackWorkerExecutor;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.action.execute.ActionExecutors;
import org.cosinus.swing.dialog.DialogHandler;
import org.cosinus.swing.preference.Preferences;
import org.cosinus.swing.translate.Translator;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.cosinus.swing.window.Dialog;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.awt.event.KeyEvent.VK_F5;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.cosinus.streamer.ui.action.execute.pack.PackActionModel.pack;
import static org.cosinus.swing.boot.SwingApplicationFrame.applicationFrame;
import static org.cosinus.swing.util.FileUtils.setExtension;

/**
 * Copy streamers action
 */
@Component
public class PackStreamerAction extends AbstractCopyAction<PackActionModel> {

    public static final String PACK_STREAMER_ACTION_ID = "act-pack";

    private static final String PACK_CONFIRMATION_UI = "packConfirmationDialog.json";

    private final ApplicationUIHandler uiHandler;

    private final BinaryExpanderHandler binaryExpanderHandler;

    private final PackWorkerExecutor packWorkerExecutor;

    private final BinaryExpanderHandler expanderHandler;

    public PackStreamerAction(final Preferences preferences,
                              final Translator translator,
                              final DialogHandler dialogHandler,
                              final ActionExecutors actionExecutors,
                              final LoadActionExecutor loadActionExecutor,
                              final StreamerViewHandler streamerViewHandler,
                              final ApplicationUIHandler uiHandler,
                              final BinaryExpanderHandler binaryExpanderHandler,
                              final PackWorkerExecutor packWorkerExecutor,
                              final BinaryExpanderHandler expanderHandler) {
        super(preferences, translator, dialogHandler, actionExecutors,
            loadActionExecutor, streamerViewHandler);
        this.uiHandler = uiHandler;
        this.binaryExpanderHandler = binaryExpanderHandler;
        this.packWorkerExecutor = packWorkerExecutor;
        this.expanderHandler = expanderHandler;
    }

    @Override
    public void run() {
        if (binaryExpanderHandler.getBinaryExpandersMap().isEmpty()) {
            dialogHandler.showInfo(translator.translate("act_pack_no_pack_system"));
            return;
        }

        super.run();
    }

    @Override
    protected void executeStreamerCopy(PackActionModel packAction) {
        ofNullable(packAction.getPackType())
            .map(binaryExpanderHandler.getBinaryExpandersMap()::get)
            .ifPresent(expander -> {
                List<?> streamersToCopy = packAction.getStreamersToCopy();
                Optional<String> streamerName = streamersToCopy.size() == 1 ?
                    streamersToCopy
                        .stream()
                        .filter(item -> Streamer.class.isAssignableFrom(item.getClass()))
                        .map(Streamer.class::cast)
                        .map(Streamer::getName)
                        .findFirst() :
                    empty();

                String name = streamerName
                    .orElseGet(() -> packAction.getSource().getName());
                String packName = setExtension(name, packAction.getPackType());
                Path packStreamerPath = packAction.getTargetPath().resolve(packName);
                Streamer destination = packAction.getDestination().create(packStreamerPath, false);
                ParentStreamer expandedDestination = (ParentStreamer) expander.expand(destination.binaryStreamer());

                packWorkerExecutor.execute(packAction.to(expandedDestination));
            });
    }

    @Override
    protected Dialog<PackActionModel> copyConfirmationDialog(PackActionModel copyAction) {
        return dialogHandler.createDialog(applicationFrame, PACK_CONFIRMATION_UI, copyAction);
    }

    @Override
    public PackActionModel createActionModel() {
        final StreamerView<?, ?> sourceStreamerView = streamerViewHandler.getCurrentView();
        final StreamerView<?, ?> destinationStreamerView = streamerViewHandler.getOppositeView();

        return pack(sourceStreamerView.getSelectedItems())
            .from(sourceStreamerView)
            .to(destinationStreamerView)
            .packTypes(expanderHandler.getBinaryExpandersMap()
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() instanceof ArchiveExpander)
                .map(Map.Entry::getKey)
                .toArray(String[]::new));
    }

    @Override
    public String getId() {
        return PACK_STREAMER_ACTION_ID;
    }

    @Override
    public Optional<KeyStroke> getKeyStroke() {
        return Optional.of(uiHandler.getAltDownKeyStroke(VK_F5));
    }
}
