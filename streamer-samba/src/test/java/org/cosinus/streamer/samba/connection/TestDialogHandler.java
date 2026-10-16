/*
 * Copyright 2026 Cosinus Software
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

package org.cosinus.streamer.samba.connection;

import org.cosinus.streamer.samba.credentials.SambaCredentials;
import org.cosinus.swing.dialog.DialogHandler;
import org.cosinus.swing.ui.UIModel;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.cosinus.streamer.samba.credentials.SambaCredentialsModel.SAMBA_DOMAIN;
import static org.cosinus.streamer.samba.credentials.SambaCredentialsModel.SAMBA_PASSWORD;
import static org.cosinus.streamer.samba.credentials.SambaCredentialsModel.SAMBA_USERNAME;

/**
 * Dialog handler answering the samba credentials dialog with the credentials set by the test,
 * instead of showing the dialog.
 */
public class TestDialogHandler extends DialogHandler {

    private final List<String> askedUsernames = new ArrayList<>();

    private SambaCredentials enteredCredentials;

    public TestDialogHandler() {
        super(null, null, null);
    }

    /**
     * Start a new test: answer with other credentials and forget the previously asked usernames.
     *
     * @param enteredCredentials the credentials to answer with, or null to answer as cancelled
     */
    public void reset(SambaCredentials enteredCredentials) {
        this.enteredCredentials = enteredCredentials;
        askedUsernames.clear();
    }

    /**
     * @return the usernames the credentials were asked with, one for each time they were asked
     */
    public List<String> getAskedUsernames() {
        return askedUsernames;
    }

    @Override
    public <M extends UIModel> Optional<M> lockAndShowDialog(Frame parent,
                                                             String descriptorName,
                                                             Supplier<Optional<M>> modelSupplier) {
        return modelSupplier.get()
            .flatMap(model -> {
                askedUsernames.add((String) model.getValue(SAMBA_USERNAME));
                if (enteredCredentials == null) {
                    return Optional.empty();
                }
                model.putValue(SAMBA_DOMAIN, enteredCredentials.domain());
                model.putValue(SAMBA_USERNAME, enteredCredentials.username());
                model.putValue(SAMBA_PASSWORD, enteredCredentials.password());
                return Optional.of(model);
            });
    }
}
