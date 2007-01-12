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
import org.springframework.beans.factory.BeanFactory;
import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.SVNClientInterface;
import org.tigris.subversion.javahl.Status;

import com.mindquarry.client.MindClient;
import com.mindquarry.client.util.os.HomeUtil;
import com.mindquarry.client.util.os.OperatingSystem;

/**
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class ShareOperation implements IRunnableWithProgress {
    private final MindClient client;

    private final SVNClientInterface svnClient;

    public ShareOperation(final MindClient client) {
        this.client = client;

        // get SVN client interface component
        BeanFactory factory = client.getFactory();
        svnClient = (SVNClientInterface) factory
                .getBean(SVNClientInterface.class.getName());
    }

    /**
     * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void run(IProgressMonitor monitor) throws InvocationTargetException,
            InterruptedException {
        monitor.beginTask(Messages.getString("ShareOperation.0"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$

        // init SVN types
        svnClient.username(client.getOptions()
                .getProperty(MindClient.LOGIN_KEY));
        svnClient.password(client.getOptions().getProperty(
                MindClient.PASSWORD_KEY));

        // get directory for workspaces
        File teamspacesDir;
        if (client.getOS() == OperatingSystem.WINDOWS) {
            teamspacesDir = new File(HomeUtil.getTeamspaceFolderWindows());
        } else {
            teamspacesDir = new File(HomeUtil.getTeamspaceFolder());
        }
        // loop existing workspace directories
        for (final File tsDir : teamspacesDir.listFiles()) {
            if (monitor.isCanceled()) {
                return;
            }
            checkStatus(tsDir);

            // retrieve (asynchronously) commit message
            final InputDialog dlg = new InputDialog(MindClient.getShell(),
                    Messages.getString("ShareOperation.1"), //$NON-NLS-1$
                    Messages.getString("ShareOperation.2") //$NON-NLS-1$
                            + tsDir.getName() + ".", //$NON-NLS-1$
                    Messages.getString("ShareOperation.3"), null); //$NON-NLS-1$
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

    private void checkStatus(File item) {
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
                    checkStatus(child);
                }
            } catch (ClientException e) {
                e.printStackTrace();
            }
        }
    }
}
