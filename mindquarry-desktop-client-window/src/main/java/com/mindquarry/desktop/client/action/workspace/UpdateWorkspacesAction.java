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
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.mindquarry.desktop.client.Messages;
import com.mindquarry.desktop.client.MindClient;
import com.mindquarry.desktop.client.action.ActionBase;

/**
 * Update list of SVN changes.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class UpdateWorkspacesAction extends ActionBase {
    public static final String ID = UpdateWorkspacesAction.class
            .getSimpleName();

    private static final Image IMAGE = new Image(
            Display.getCurrent(),
            UpdateWorkspacesAction.class
                    .getResourceAsStream("/org/tango-project/tango-icon-theme/" + ICON_SIZE + "/actions/view-refresh.png")); //$NON-NLS-1$

    public UpdateWorkspacesAction(MindClient client) {
        super(client);

        setId(ID);
        setActionDefinitionId(ID);

        setText(Messages.getString("Update changes"));
        setToolTipText(Messages.getString("Update the list of changes"));
        setAccelerator(SWT.CTRL + +SWT.SHIFT + 'U');
        setImageDescriptor(ImageDescriptor.createFromImage(IMAGE));
    }

    public void run() {
        client.getCategoryWidget().getWorkspaceBrowserWidget()
                .asyncRefresh();
    }

    public String getGroup() {
        return ActionBase.WORKSPACE_ACTION_GROUP;
    }

    public boolean isToolbarAction() {
        return false;
    }
}
