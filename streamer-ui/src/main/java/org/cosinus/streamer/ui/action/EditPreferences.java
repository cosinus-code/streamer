package org.cosinus.streamer.ui.action;

import org.cosinus.swing.action.ActionContext;
import org.cosinus.swing.action.ActionInContext;
import org.cosinus.swing.boot.ApplicationInitializationHandler;
import org.cosinus.swing.dialog.DialogHandler;
import org.springframework.stereotype.Component;

import static org.cosinus.swing.boot.SwingApplicationFrame.applicationFrame;

/**
 * Start draw action
 */
@Component
public class EditPreferences implements ActionInContext<ActionContext> {

    private static final String EDIT_PREFERENCES = "menu-edit-preferences";

    private final DialogHandler dialogHandler;

    public EditPreferences(DialogHandler dialogHandler) {
        this.dialogHandler = dialogHandler;
    }

    @Override
    public void run(ActionContext context) {
        dialogHandler.showPreferencesDialog(applicationFrame);
    }

    @Override
    public String getId() {
        return EDIT_PREFERENCES;
    }
}
