/*
 * Copyright 2025 Cosinus Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.cosinus.streamer.ui.action;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.model.ExecuteStreamerModel;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.action.SwingAction;
import org.cosinus.swing.dialog.DialogHandler;
import org.cosinus.swing.exec.ProcessExecutor;
import org.cosinus.swing.file.FileCompatibleApplications;
import org.cosinus.swing.file.FileSystem;
import org.cosinus.swing.image.icon.IconInitializer;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.cosinus.swing.boot.SwingApplicationFrame.applicationFrame;

@Component
public class ExecuteStreamerWithAction implements SwingAction {

    public static final String EXECUTE_STREAMER_WITH_ACTION_ID = "execute-streamer-with";

    private static final String OPEN_WITH_DIALOG = "executeWithDialog.json";

    private final StreamerViewHandler streamerViewHandler;

    private final FileSystem fileSystem;

    private final ProcessExecutor processExecutor;

    private final DialogHandler dialogHandler;

    private final IconInitializer iconInitializer;

    public ExecuteStreamerWithAction(final StreamerViewHandler streamerViewHandler,
                                     final FileSystem fileSystem,
                                     final ProcessExecutor processExecutor,
                                     final DialogHandler dialogHandler,
                                     final IconInitializer iconInitializer) {
        this.streamerViewHandler = streamerViewHandler;
        this.fileSystem = fileSystem;
        this.processExecutor = processExecutor;
        this.dialogHandler = dialogHandler;
        this.iconInitializer = iconInitializer;
    }

    @Override
    public void run() {
        StreamerView<?> streamerView = streamerViewHandler.getCurrentView();
        ofNullable(streamerView.getCurrentStreamerOrParent())
            .filter(not(Streamer::isParent))
            .map(Streamer::getPath)
            .map(Path::toFile)
            .filter(File::exists)
            .ifPresent(this::openFile);
    }

    public void openFile(final File file) {
        FileCompatibleApplications compatibleApplicationsMap =
            fileSystem.findCompatibleApplicationsToExecuteFile(file);
        if (compatibleApplicationsMap.isEmpty()) {
            dialogHandler.showInfo("No compatible applications found.");
            return;
        }

        compatibleApplicationsMap
            .values()
            .forEach(iconInitializer::updateIcon);

        ExecuteStreamerModel executeStreamerModel = new ExecuteStreamerModel(
            compatibleApplicationsMap.values(),
            compatibleApplicationsMap.getDefaultApplication());

        dialogHandler
            .showDialog(() -> dialogHandler.createDialog(applicationFrame, OPEN_WITH_DIALOG,
                executeStreamerModel))
            .response()
            .map(ExecuteStreamerModel::getSelectedApplication)
            .ifPresent(application -> {
                if (executeStreamerModel.isSetAsDefault()) {
                    fileSystem.setDefaultApplicationToExecuteFile(application.getId(), file);
                }
                processExecutor.execute(application.isRunInTerminal(), false,
                    application.getCommandToExecuteFile(file));
            });
    }

    @Override
    public String getId() {
        return EXECUTE_STREAMER_WITH_ACTION_ID;
    }
}
