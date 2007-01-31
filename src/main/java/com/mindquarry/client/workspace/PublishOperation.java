/*
 * Copyright (C) 2006-2007 MindQuarry GmbH, All Rights Reserved
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
package com.mindquarry.client.workspace;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.Status;

import com.mindquarry.client.MindClient;

/**
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class PublishOperation extends SvnOperation implements
        IRunnableWithProgress {
    public PublishOperation(final MindClient client) {
        super(client);
    }

    /**
     * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void run(IProgressMonitor monitor) throws InvocationTargetException,
            InterruptedException {
        monitor
                .beginTask(
                        Messages.getString("PublishOperation.0"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$

        // init SVN client API types
        svnClient
                .username(client.getProfileList().selectedProfile().getLogin());
        svnClient.password(client.getProfileList().selectedProfile()
                .getPassword());

        // get directory for workspaces
        File teamspacesDir = new File(client.getProfileList().selectedProfile()
                .getLocation());

        // loop existing workspace directories
        for (final File tsDir : teamspacesDir.listFiles()) {
            if (monitor.isCanceled()) {
                return;
            }
            // check if folder is really a workspace folder
            if (!isFolderVersionControled(tsDir)) {
                continue;
            }
            checkVersionStatus(tsDir);

            // retrieve (asynchronously) commit message
            final InputDialog dlg = new InputDialog(MindClient.getShell(),
                    Messages.getString("PublishOperation.1"), //$NON-NLS-1$
                    Messages.getString("PublishOperation.2") //$NON-NLS-1$
                            + tsDir.getName() + ".", //$NON-NLS-1$
                    Messages.getString("PublishOperation.3"), null); //$NON-NLS-1$
            MindClient.getShell().getDisplay().syncExec(new Runnable() {
                /**
                 * @see java.lang.Runnable#run()
                 */
                public void run() {
                    dlg.open();
                }
            });
            // commit changes
            try {
                svnClient.commit(new String[] { tsDir.getAbsolutePath() }, dlg
                        .getValue(), true);
            } catch (ClientException e) {
                e.printStackTrace();
                continue;
            }
        }
        monitor.done();
    }

    private void checkVersionStatus(File item) {
        for (File child : item.listFiles()) {
            if (child.getName().equals(".svn")) { //$NON-NLS-1$
                continue;
            }
            try {
                // retrieve local status
                Status status = svnClient.singleStatus(child.getAbsolutePath(),
                        false);

                // check if the item is managed by SVN, if it is a directory
                // check also child for finding not managed items in the
                // subdirectories
                if (!status.isManaged()) {
                    svnClient.add(child.getAbsolutePath(), true);
                } else if (child.isDirectory()) {
                    checkVersionStatus(child);
                }
            } catch (ClientException e) {
                e.printStackTrace();
            }
        }
    }
}
