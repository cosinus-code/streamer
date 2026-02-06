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

import org.cosinus.swing.action.ActionContext;
import org.cosinus.swing.action.ActionInContext;
import org.cosinus.swing.dialog.DialogHandler;
import org.cosinus.swing.translate.Translator;
import org.springframework.stereotype.Component;

import static org.cosinus.swing.image.icon.IconProvider.ICON_HELP;

@Component
public class ShowHelpAction implements ActionInContext {

    public static final String SHOW_HELP_ACTION_NAME = "menu-help-index";

    private final DialogHandler dialogHandler;

    private final Translator translator;

    public ShowHelpAction(final DialogHandler dialogHandler,
                          final Translator translator) {
        this.dialogHandler = dialogHandler;
        this.translator = translator;
    }

    @Override
    public void run(ActionContext context) {
        dialogHandler.showInfo(translator.translate("not-implemented"));
    }

    @Override
    public String getIconName() {
        return ICON_HELP;
    }

    @Override
    public String getId() {
        return SHOW_HELP_ACTION_NAME;
    }
}
