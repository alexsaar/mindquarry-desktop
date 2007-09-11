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
import com.mindquarry.desktop.client.dialog.conflict.ContentConflictDialog;
import com.mindquarry.desktop.client.dialog.conflict.DeleteWithModificationConflictDialog;
import com.mindquarry.desktop.client.dialog.conflict.ObstructedConflictDialog;
import com.mindquarry.desktop.client.dialog.conflict.PropertyConflictDialog;
import com.mindquarry.desktop.client.dialog.conflict.ReplaceConflictDialog;
import com.mindquarry.desktop.workspace.conflict.AddConflict;
import com.mindquarry.desktop.workspace.conflict.ConflictHandler;
import com.mindquarry.desktop.workspace.conflict.ContentConflict;
import com.mindquarry.desktop.workspace.conflict.DeleteWithModificationConflict;
import com.mindquarry.desktop.workspace.conflict.ObstructedConflict;
import com.mindquarry.desktop.workspace.conflict.PropertyConflict;
import com.mindquarry.desktop.workspace.conflict.ReplaceConflict;

/**
 * Show GUI dialogs to let the user resolve conflicts.
 * 
 * @author dnaber
 * @author <a href="mailto:victor(dot)saar(at)mindquarry(dot)com">Victor Saar</a>
 */
public class InteractiveConflictHandler implements ConflictHandler {

    private Shell shell;
    
    private boolean cancelled = false;
    
    public InteractiveConflictHandler(Shell shell) {
        this.shell = shell;
    }
    
    public void handle(final AddConflict conflict) throws SynchronizeCancelException {
        shell.getDisplay().syncExec(new Runnable() {
            public void run() {
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
                    cancelled = true;
                }
            }
        });
        if (cancelled) {
            throw new SynchronizeCancelException();
        }
    }

    public void handle(final DeleteWithModificationConflict conflict)
            throws SynchronizeCancelException {
        shell.getDisplay().syncExec(new Runnable() {
            public void run() {
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
                    cancelled = true;
                }
            }
        });
        if (cancelled) {
            throw new SynchronizeCancelException();
        }
    }

    public void handle(final ReplaceConflict conflict) throws SynchronizeCancelException {
        shell.getDisplay().syncExec(new Runnable() {
            public void run() {
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
                    cancelled = true;
                }
            }
        });
        if (cancelled) {
            throw new SynchronizeCancelException();
        }
    }

    public void handle(final ContentConflict conflict) throws SynchronizeCancelException {
        shell.getDisplay().syncExec(new Runnable() {
            public void run() {
                ContentConflictDialog dlg = 
                    new ContentConflictDialog(conflict, shell);
                int resolution = dlg.open();
                if (resolution == IDialogConstants.OK_ID) {
                    if (dlg.getResolveMethod() == ContentConflict.Action.USE_LOCAL) {
                        conflict.doUseLocal();
                    } else if (dlg.getResolveMethod() == ContentConflict.Action.USE_REMOTE) {
                        conflict.doUseRemote();
                    } else if (dlg.getResolveMethod() == ContentConflict.Action.MERGE) {
                        conflict.doMerge();
                    } else {
                        throw new IllegalArgumentException("Unexpected dialog resolution: " + 
                                dlg.getResolveMethod());
                    }
                } else {
                    cancelled = true;
                }
            }
        });
        if (cancelled) {
            throw new SynchronizeCancelException();
        }
    }

    public void handle(final ObstructedConflict conflict)
            throws SynchronizeCancelException {
        shell.getDisplay().syncExec(new Runnable() {
            public void run() {
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
                    cancelled = true;
                }
            }
        });
        if (cancelled) {
            throw new SynchronizeCancelException();
        }
    }

    public void handle(final PropertyConflict conflict) throws SynchronizeCancelException {
        shell.getDisplay().syncExec(new Runnable() {
            public void run() {
                PropertyConflictDialog dlg = 
                    new PropertyConflictDialog(conflict, shell);
                int resolution = dlg.open();
                if (resolution == IDialogConstants.OK_ID) {
                    if (dlg.getResolveMethod() == PropertyConflict.Action.USE_LOCAL_VALUE) {
                        conflict.doUseLocalValue();
                    } else if (dlg.getResolveMethod() == PropertyConflict.Action.USE_REMOTE_VALUE) {
                        conflict.doUseRemoteValue();
                    } else if (dlg.getResolveMethod() == PropertyConflict.Action.USE_NEW_VALUE) {
                        conflict.doUseNewValue(dlg.getNewValue());
                    } else {
                        throw new IllegalArgumentException("Unexpected dialog resolution: " + 
                                dlg.getResolveMethod());
                    }
                } else {
                    cancelled = true;
                }
            }
        });
        if (cancelled) {
            throw new SynchronizeCancelException();
        }
    }

}
