/*
 * Copyright (C) 2006-2007 Mindquarry GmbH, All Rights Reserved
 * 
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 */
package com.mindquarry.desktop.client.action.workspace;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.mindquarry.desktop.client.Messages;
import com.mindquarry.desktop.client.MindClient;
import com.mindquarry.desktop.client.action.ActionBase;

/**
 * Open the selected file.
 * 
 * @author dnaber
 */
public class OpenFileAction extends ActionBase {
    public static final String ID = OpenFileAction.class
            .getSimpleName();

    private static final Image IMAGE = new Image(
            Display.getCurrent(),
            OpenFileAction.class
                    .getResourceAsStream("/org/tango-project/tango-icon-theme/" + ICON_SIZE + "/actions/document-open.png")); //$NON-NLS-1$ //$NON-NLS-2$

    public OpenFileAction(MindClient client) {
        super(client);

        setId(ID);
        setActionDefinitionId(ID);

        setText(Messages.getString("Open local file")); //$NON-NLS-1$
        setToolTipText(Messages.getString("Open selected file locally")); //$NON-NLS-1$
        //setAccelerator(SWT.CTRL + +SWT.SHIFT + 'U');
        setImageDescriptor(ImageDescriptor.createFromImage(IMAGE));
    }

    public void run() {
        client.getEventBus().sendEvent(new OpenFileEvent());
    }
    
    public String getGroup() {
        return ActionBase.WORKSPACE_OPEN_GROUP;
    }

    public boolean isToolbarAction() {
        return true;
    }

    public boolean isEnabledByDefault() {
        return false;
    }

}
