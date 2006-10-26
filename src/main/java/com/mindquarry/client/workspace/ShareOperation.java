/**
 * Copyright (C) 2005 MindQuarry GmbH, All Rights Reserved
 */
package com.mindquarry.client.workspace;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.springframework.beans.factory.BeanFactory;
import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.SVNClientInterface;
import org.tigris.subversion.javahl.Status;

import com.mindquarry.client.MindClient;
import com.mindquarry.client.util.RegUtil;

/**
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class ShareOperation implements IRunnableWithProgress {
    private final MindClient client;

    private final SVNClientInterface svnClient;

    public ShareOperation(MindClient client) {
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
        monitor.beginTask("Sharing workspaces ...", IProgressMonitor.UNKNOWN);

        // init SVN types
        svnClient.username(client.getOptions()
                .getProperty(MindClient.LOGIN_KEY));
        svnClient.password(client.getOptions().getProperty(
                MindClient.PASSWORD_KEY));

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
            checkStatus(wsDir);
            // TODO fix invalid thread access for this dialog

            // retrieve commit message
            // InputDialog dlg = new InputDialog(MindClient.getShell(),
            // "Changes Description",
            // "Please provide a short description of the changes you have made
            // to workspace "
            // + wsDir.getName() + ".",
            // "Description of your changes.", null);
            // if (dlg.open() == Window.OK) {

            // commit changes
            try {
                svnClient.commit(new String[] { wsDir.getAbsolutePath() },
                        "shared changes", true);
            } catch (ClientException e) {
                e.printStackTrace();
                continue;
            }
            // }
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
