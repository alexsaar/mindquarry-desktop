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
import java.io.FilenameFilter;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Widget;
import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.Status;

import com.mindquarry.client.MindClient;

/**
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class PublishOperation extends SvnOperation {
    public PublishOperation(final MindClient client,
            List<Widget> progressBars) {
        super(client, progressBars);
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        // get directory for workspaces
        File teamspacesDir = new File(client.getProfileList().selectedProfile()
                .getLocation());

        // list directories
        File[] directories = teamspacesDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                File file = new File(dir, name);
                // check if folder is really a workspace folder
                return file.isDirectory() && isFolderVersionControled(file);
            }
        });
        // init SVN client API types
        svnClient
                .username(client.getProfileList().selectedProfile().getLogin());
        svnClient.password(client.getProfileList().selectedProfile()
                .getPassword());

        // loop existing workspace directories
        for (final File tsDir : directories) {
            Status[] stati = checkStatus(tsDir);
            if (stati != null && stati.length > 0) {
                // build message with list of changes
                String msg = Messages.getString("PublishOperation.2") //$NON-NLS-1$
                        + tsDir.getName() + ":\n\n" //$NON-NLS-1$
                        + getStatiDescription(stati, tsDir.getPath());

                // retrieve (asynchronously) commit message
                final InputDialog dlg = new InputDialog(MindClient.getShell(),
                        Messages.getString("PublishOperation.1"), //$NON-NLS-1$
                        msg, Messages.getString("PublishOperation.3"), null); //$NON-NLS-1$
                MindClient.getShell().getDisplay().syncExec(new Runnable() {
                    /**
                     * @see java.lang.Runnable#run()
                     */
                    public void run() {
                        dlg.setBlockOnOpen(true);
                        if (dlg.open() == Dialog.OK) {
                            // commit changes
                            try {
                                svnClient.commit(new String[] { tsDir
                                        .getAbsolutePath() }, dlg.getValue(),
                                        true);
                            } catch (ClientException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                updateProgress();
            }
        }
    }
}
