package com.mindquarry.desktop.workspace.ui.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;

public class MessagePopupAction extends Action {
    public static final String ID = "com.mindquarry.desktop.workspace.ui.openMessage";
    
    private final IWorkbenchWindow window;

    public MessagePopupAction(String text, IWorkbenchWindow window) {
        super(text);
        this.window = window;
        
        // the ID is used to refer to the action in a menu or toolbar
        setId(ID);

        // associate the action with a pre-defined command to allow key bindings
        setActionDefinitionId(ID);
    }

    @Override
    public void run() {
        MessageDialog.openInformation(window.getShell(), "Open",
                "Open Message Dialog!");
    }
}
