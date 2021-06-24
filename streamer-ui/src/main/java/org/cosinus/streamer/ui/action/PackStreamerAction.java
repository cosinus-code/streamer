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

package org.cosinus.streamer.ui.action;

import org.cosinus.streamer.api.DirectoryStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.pack.PackerHandler;
import org.cosinus.streamer.ui.action.context.StreamerActionContext;
import org.cosinus.streamer.ui.action.copy.AbstractCopyAction;
import org.cosinus.streamer.ui.action.execute.copy.CopyActionModel;
import org.cosinus.streamer.ui.action.progress.ProgressListenerHandler;
import org.cosinus.swing.action.execute.ActionExecutors;
import org.cosinus.swing.dialog.DialogHandler;
import org.cosinus.swing.preference.Preferences;
import org.cosinus.swing.translate.Translator;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.Optional;

import static java.awt.event.KeyEvent.VK_F5;
import static org.cosinus.streamer.ui.action.execute.copy.CopyActionModel.pack;

/**
 * Copy streamers action
 */
@Component
public class PackStreamerAction<A> extends AbstractCopyAction<A> {

    public static final String PACK_STREAMER_ACTION_ID = "pack-streamer";

    private final ApplicationUIHandler uiHandler;

    private final PackerHandler packerHandler;

    public PackStreamerAction(Preferences preferences,
                              Translator translator,
                              DialogHandler dialogHandler,
                              ActionExecutors actionExecutors,
                              ProgressListenerHandler progressListenerHandler,
                              LoadStreamerAction loadStreamerAction,
                              ApplicationUIHandler uiHandler,
                              PackerHandler packerHandler) {
        super(preferences,
              translator,
              dialogHandler,
              actionExecutors,
              progressListenerHandler,
              loadStreamerAction);
        this.uiHandler = uiHandler;
        this.packerHandler = packerHandler;
    }

    @Override
    public void run(StreamerActionContext actionContext) {
        if (packerHandler.getPackersMap().isEmpty()) {
            dialogHandler.showInfo(translator.translate("act_pack_no_pack_system"));
            return;
        }

        super.run(actionContext);
    }

    @Override
    protected <S extends Streamer, T extends Streamer>
    void execute(CopyActionModel<S, T> copyAction,
                 StreamerActionContext actionContext) {

//        Optional.ofNullable(copyAction.getPackType())
//                .map(packerHandler.getPackers()::get)
//                .ifPresent(packer -> {
//                    DataView<S> currentView = actionContext.getCurrentView();
//                    List<S> streamersToCopy = copyAction.getStreamersToCopy();
//                    String name = streamersToCopy.size() == 1 ?
//                            streamersToCopy.get(0).getName() :
//                            currentView.getCurrentStreamer().getName();
//                    String packName = setExtension(name, copyAction.getPackType());
//                    Path packStreamerPath = copyAction.getTargetPath().resolve(packName);
//
//                    Streamer destination = copyAction.getDestination().create(packStreamerPath);
//
//                    super.execute(copy(streamersToCopy)
//                                          .from(currentView.getLoadedStreamer())
//                                          .to(packer.pack(destination))
//                                            //TODO: to avoid setting target path to null
//                                          .toTargetPath((Path) null),
//                                  actionContext);
//                });
    }

    @Override
    protected <S extends DirectoryStreamer, T extends DirectoryStreamer>
    CopyActionModel<S, T> copySpecifications(StreamerActionContext actionContext) {
        return pack(actionContext.getCurrentView().getSelectedContent())
            .to(actionContext.getOppositeView().getLoadedStreamer());
    }

    @Override
    protected Object[] transferType() {
        return packerHandler.getPackersMap()
            .keySet()
            .toArray();
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
