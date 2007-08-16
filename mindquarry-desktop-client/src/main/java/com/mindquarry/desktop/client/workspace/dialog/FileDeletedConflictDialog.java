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
package com.mindquarry.desktop.client.workspace.dialog;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.tigris.subversion.javahl.Status;

import com.mindquarry.desktop.client.Messages;
import com.mindquarry.desktop.workspace.SVNHelper;

/**
 * Dialog for resolving working copy conflicts.
 * 
 * @author dnaber
 */
public class FileDeletedConflictDialog extends AbstractConflictDialog {

    public FileDeletedConflictDialog(Shell shell, Status remoteStatus) {
        super(shell, remoteStatus, SVNHelper.CONFLICT_OVERRIDE_FROM_WC);
    }

    protected void createLowerDialogArea(Composite composite) {
        makeRadioButton(composite, Messages.getString(FileDeletedConflictDialog.class, "1"),  //$NON-NLS-1$
                SVNHelper.CONFLICT_OVERRIDE_FROM_WC);
        makeRadioButton(composite, Messages.getString(FileDeletedConflictDialog.class, "2"),  //$NON-NLS-1$
                SVNHelper.CONFLICT_RENAME_AND_RETRY);
    }

    @Override
    protected String getMessage() {
        return Messages.getString(FileDeletedConflictDialog.class, "0");
    }
    
}
