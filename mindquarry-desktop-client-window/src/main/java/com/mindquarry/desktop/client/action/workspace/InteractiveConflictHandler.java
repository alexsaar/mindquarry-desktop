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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Shell;

import com.mindquarry.desktop.client.dialog.conflict.AddConflictDialog;
import com.mindquarry.desktop.client.dialog.conflict.DeleteWithModificationConflictDialog;
import com.mindquarry.desktop.client.dialog.conflict.ObstructedConflictDialog;
import com.mindquarry.desktop.client.dialog.conflict.ReplaceConflictDialog;
import com.mindquarry.desktop.workspace.conflict.AddConflict;
import com.mindquarry.desktop.workspace.conflict.ConflictHandler;
import com.mindquarry.desktop.workspace.conflict.ContentConflict;
import com.mindquarry.desktop.workspace.conflict.DeleteWithModificationConflict;
import com.mindquarry.desktop.workspace.conflict.ObstructedConflict;
import com.mindquarry.desktop.workspace.conflict.PropertyConflict;
import com.mindquarry.desktop.workspace.conflict.ReplaceConflict;
import com.mindquarry.desktop.workspace.exception.CancelException;

/**
 * Show GUI dialogs to let the user resolve conflicts.
 * 
 * @author dnaber
 */
public class InteractiveConflictHandler implements ConflictHandler {

    private Shell shell;
    
    public InteractiveConflictHandler(Shell shell) {
        this.shell = shell;
    }
    
    public void handle(AddConflict conflict) throws CancelException {
        AddConflictDialog dlg = new AddConflictDialog(conflict, shell);
        int resolution = dlg.open();
        if (resolution == IDialogConstants.OK_ID) {
            if (dlg.getResolveMethod() == AddConflict.Action.RENAME) {
                conflict.doRename(dlg.getNewName());
            } else if (dlg.getResolveMethod() == AddConflict.Action.REPLACE) {
                conflict.doReplace();
            } else {
                throw new IllegalArgumentException("Unexpected dialog resolution: " + 
                        dlg.getResolveMethod());
            }
        } else {
            throw new CancelException();
        }
    }

    public void handle(DeleteWithModificationConflict conflict)
            throws CancelException {
        DeleteWithModificationConflictDialog dlg = 
            new DeleteWithModificationConflictDialog(conflict, shell);
        int resolution = dlg.open();
        if (resolution == IDialogConstants.OK_ID) {
            if (dlg.getResolveMethod() == DeleteWithModificationConflict.Action.REVERTDELETE) {
                conflict.doRevertDelete();
            } else if (dlg.getResolveMethod() == DeleteWithModificationConflict.Action.DELETE) {
                conflict.doDelete();
            } else if (dlg.getResolveMethod() == DeleteWithModificationConflict.Action.ONLYKEEPMODIFIED) {
                conflict.doOnlyKeepModified();
            } else {
                throw new IllegalArgumentException("Unexpected dialog resolution: " + 
                        dlg.getResolveMethod());
            }
        } else {
            throw new CancelException();
        }
    }

    public void handle(ReplaceConflict conflict) throws CancelException {
        ReplaceConflictDialog dlg = new ReplaceConflictDialog(conflict, shell);
        int resolution = dlg.open();
        if (resolution == IDialogConstants.OK_ID) {
            if (dlg.getResolveMethod() == ReplaceConflict.Action.RENAME) {
                conflict.doRename(dlg.getNewName());
            } else if (dlg.getResolveMethod() == ReplaceConflict.Action.REPLACE) {
                conflict.doReplace();
            } else {
                throw new IllegalArgumentException("Unexpected dialog resolution: " + 
                        dlg.getResolveMethod());
            }
        } else {
            throw new CancelException();
        }
    }

    public void handle(ContentConflict conflict) throws CancelException {
        // TODO Auto-generated method stub
        System.err.println("FIXME: implement");
    }

    public void handle(ObstructedConflict conflict)
            throws CancelException {
        ObstructedConflictDialog dlg = 
            new ObstructedConflictDialog(conflict, shell);
        int resolution = dlg.open();
        if (resolution == IDialogConstants.OK_ID) {
            if (dlg.getResolveMethod() == ObstructedConflict.Action.RENAME) {
                conflict.doRename(dlg.getNewName());
            } else if (dlg.getResolveMethod() == ObstructedConflict.Action.REVERT) {
                conflict.doRevert();
            } else {
                throw new IllegalArgumentException("Unexpected dialog resolution: " + 
                        dlg.getResolveMethod());
            }
        } else {
            throw new CancelException();
        }
    }

    public void handle(PropertyConflict conflict) throws CancelException {
        // TODO Auto-generated method stub
        System.err.println("FIXME: implement");
    }

    public String getCommitMessage(String repoURL) throws CancelException {
        // TODO Auto-generated method stub
        return null;
    }

}
