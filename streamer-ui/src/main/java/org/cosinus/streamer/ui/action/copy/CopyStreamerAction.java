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

package org.cosinus.streamer.ui.action.copy;

import org.cosinus.streamer.api.ContainerStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.action.LoadStreamerAction;
import org.cosinus.streamer.ui.action.context.StreamerActionContext;
import org.cosinus.streamer.ui.action.execute.copy.CopyActionModel;
import org.cosinus.streamer.ui.action.progress.ProgressListenerHandler;
import org.cosinus.swing.action.execute.ActionExecutors;
import org.cosinus.swing.dialog.DialogHandler;
import org.cosinus.swing.preference.Preferences;
import org.cosinus.swing.translate.Translator;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.Optional;

import static java.awt.event.KeyEvent.VK_F5;
import static org.cosinus.streamer.ui.action.execute.copy.CopyActionModel.copy;

/**
 * Copy streamers action
 */
@Component
public class CopyStreamerAction<A> extends AbstractCopyAction<A> {

    public static final String COPY_STREAMER_ACTION_ID = "copy-streamer";

    public CopyStreamerAction(Preferences preferences,
                              Translator translator,
                              DialogHandler dialogHandler,
                              ActionExecutors actionExecutors,
                              ProgressListenerHandler progressListenerHandler,
                              LoadStreamerAction loadStreamerAction) {
        super(preferences,
              translator,
              dialogHandler,
              actionExecutors,
              progressListenerHandler,
              loadStreamerAction);
    }

    @Override
    protected <S extends Streamer, T extends Streamer>
    void execute(CopyActionModel<S, T> copyAction, StreamerActionContext actionContext) {
        //TODO: to avoid cast
        T destination = (T) copyAction.getDestination().getParent().container(copyAction.getTargetPath());
        if (destination == null) {
            dialogHandler.showInfo(translator.translate("act_copy_destination_not_found"));
            return;
        }
        super.execute(copyAction.to(destination), actionContext);
    }

    @Override
    protected <S extends ContainerStreamer, T extends ContainerStreamer>
    CopyActionModel<S, T> copySpecifications(StreamerActionContext actionContext) {
        return copy(actionContext.getCurrentView().getSelectedContent())
            .from(actionContext.getCurrentView().getLoadedStreamer())
            .to(actionContext.getOppositeView().getLoadedStreamer());
    }

    @Override
    public String getId() {
        return COPY_STREAMER_ACTION_ID;
    }

    @Override
    public Optional<KeyStroke> getKeyStroke() {
        return Optional.of(KeyStroke.getKeyStroke(VK_F5, 0));
    }
}
