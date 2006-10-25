/**
 * Copyright (C) 2005 MindQuarry GmbH, All Rights Reserved
 */
package com.mindquarry.client.workspace;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.util.SVNURLUtil;
import org.tmatesoft.svn.core.wc.SVNCommitClient;
import org.tmatesoft.svn.core.wc.SVNWCClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import com.mindquarry.client.MindClient;
import com.mindquarry.client.util.RegUtil;

/**
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class ShareOperation implements IRunnableWithProgress {
    private final MindClient client;

    public ShareOperation(MindClient client) {
        this.client = client;
    }

    /**
     * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void run(IProgressMonitor monitor) throws InvocationTargetException,
            InterruptedException {
        monitor.beginTask("Sharing workspaces ...", IProgressMonitor.UNKNOWN);

        // init SVN types
        ISVNAuthenticationManager authManager = SVNWCUtil
                .createDefaultAuthenticationManager(client.getOptions()
                        .getProperty(MindClient.LOGIN_KEY), client.getOptions()
                        .getProperty(MindClient.PASSWORD_KEY));
        SVNCommitClient svnClient = new SVNCommitClient(authManager, SVNWCUtil
                .createDefaultOptions(true));
        SVNWCClient wcClient = new SVNWCClient(authManager, SVNWCUtil
                .createDefaultOptions(true));

        // get directory for workspaces
        File workspacesDir = new File(RegUtil.getMyDocumentsFolder());
        if (!workspacesDir.exists()) {
            // TODO check if this is right
            return;
        }
        // loop existing workspace directories
        for (File wsDir : workspacesDir.listFiles()) {
            if (monitor.isCanceled()) {
                return;
            }
            checkStatus(wsDir, wcClient);
            // retrieve commit message

            // TODO fix invalid thread access
            // InputDialog dlg = new InputDialog(MindClient.getShell(),
            // "Changes Description",
            // "Please provide a short description of the changes you have made
            // to workspace "
            // + wsDir.getName() + ".",
            // "Description of your changes.", null);
            // if (dlg.open() == Window.OK) {
            try {
                svnClient.doCommit(new File[] { wsDir }, false,
                        "sharing changes", true, true);
            } catch (SVNException e) {
                e.printStackTrace();
                continue;
            }
            // }
        }
        monitor.done();
    }

    private void checkStatus(File wsDir, SVNWCClient wcClient) {
        for (File item : wsDir.listFiles()) {
            if ((!SVNWCUtil.isVersionedDirectory(item))
                    && (!item.getName().equals(".svn")) //$NON-NLS-1$ 
                    && (item.isDirectory())) {
                try {
                    wcClient.doAdd(item, true, false, false, true);
                } catch (SVNException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
