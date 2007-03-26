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
package com.mindquarry.desktop.workspace;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.Notify2;
import org.tigris.subversion.javahl.NotifyInformation;
import org.tigris.subversion.javahl.Revision;
import org.tigris.subversion.javahl.Status;
import org.tigris.subversion.javahl.StatusKind;
import org.tmatesoft.svn.core.javahl.SVNClientImpl;

/**
 * Add summary documentation here.
 * 
 * @author <a href="mailto:jonas(at)metaquark(dot)de">Jonas Witt</a>
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public abstract class SVNHelper implements Notify2 {
    public static final int CONFLICT_RESET_FROM_SERVER = 0;

    public static final int CONFLICT_OVERRIDE_FROM_WC = 1;

    protected String repositoryURL;

    protected String localPath;

    protected String username;

    protected String password;

    protected SVNClientImpl client;

    public SVNHelper(String repositoryURL, String localPath, String username,
            String password) {
        this.repositoryURL = repositoryURL;
        this.localPath = localPath;
        this.username = username;
        this.password = password;

        // create SVN client, set authentification info
        client = SVNClientImpl.newInstance();
        if (username != null) {
            client.username(username);
            if (password != null) {
                client.password(password);
            }
        }

        // register for svn notifications on update and commit
        client.notification2(this);
    }

    /**
     * Updates the working copy at localPath or performs a new checkout from
     * repositoryURL if no working copy is present
     */
    public void update() throws ClientException {
        // check whether localPath is a working copy or not
        try {
            client.status(localPath, false, false, true);
        } catch (ClientException e) {
            // no working copy, checkout
            initialCheckout();
            return;
        }
        client.update(localPath, Revision.HEAD, true);
    }

    /**
     * Performs a new checkout from repositoryURL
     */
    protected void initialCheckout() throws ClientException {
        // TODO: check if localPath already exists and if we need to move it
        // first
        client.checkout(repositoryURL, localPath, Revision.HEAD, true);
    }

    /**
     * Returns all changed or newly created files in the working copy at
     * localPath. This includes files that have not been scheduled for addition
     * to the repository.
     */
    public Status[] getLocalChanges() throws ClientException {
        try {
            Status[] stati = client.status(localPath, true, false, true);

            // add all conflicted file paths to an array
            ArrayList<String> conflicts = new ArrayList<String>();
            for (Status stat : stati) {
                if (stat.getTextStatus() == StatusKind.conflicted) {
                    conflicts.add(stat.getPath());
                }
            }

            // collect changes
            ArrayList<Status> changes = new ArrayList<Status>();
            for (Status stat : stati) {

                // skip ignored files
                if (stat.getTextStatus() == StatusKind.ignored
                        || stat.getTextStatus() == StatusKind.external) {
                    continue;
                }

                // skip conflict related files (.mine, .rxyz)
                boolean isConflictRelatedFile = false;
                for (String conflict : conflicts) {
                    if (stat.getPath().startsWith(conflict + ".")) {
                        isConflictRelatedFile = true;
                        break;
                    }
                }
                if (isConflictRelatedFile) {
                    continue;
                }

                if (stat.getTextStatus() == StatusKind.missing) {
                    client.remove(new String[] { stat.getPath() }, null, true);
                }

                // add changed or modified files to the results array
                if (stat.getTextStatus() > StatusKind.normal) {
                    changes.add(stat);
                }
            }
            return changes.toArray(new Status[0]);
        } catch (ClientException ce) {
            return new Status[0];
        }
    }

    /**
     * Commits all specified files to the repository. Will call
     * resolveConflict() for each conflicted file and getCommitMessage() once to
     * get the commit message before commiting.
     */
    public void commit(String[] paths) throws ClientException {
        for (String path : paths) {
            try {
                final Status status = client.singleStatus(path, false);

                if (status.getTextStatus() == StatusKind.unversioned) {
                    client.add(path, true, true);
                } else if (status.getTextStatus() == StatusKind.conflicted) {
                    switch (resolveConflict(status)) {
                    case CONFLICT_RESET_FROM_SERVER:
                        // copy latest revision to main file
                        try {
                            copyFile(status.getConflictNew(), status.getPath());
                        } catch (IOException e) {
                            System.err
                                    .println("Could not resolve conflict on file "
                                            + status.getPath());
                        }
                        break;
                    case CONFLICT_OVERRIDE_FROM_WC:
                        try {
                            copyFile(status.getPath() + ".mine", status
                                    .getPath());
                        } catch (IOException e) {
                            System.err
                                    .println("Could not resolve conflict on file "
                                            + status.getPath());
                        }
                        break;
                    }
                    client.resolved(status.getPath(), false);
                }
            } catch (ClientException e) {
                System.err.println("client exception: " + path);
            }
        }
        String message = getCommitMessage();

        // if getCommitMessage returns null, we don't commit
        if (message != null) {
            client.commit(paths, message, true);
        }
    }

    /**
     * Cancels the current SVN operation
     */
    protected void cancelOperation() {
        try {
            client.cancelOperation();
        } catch (ClientException e) {
            e.printStackTrace();
        }
    }

    /**
     * Is called for every single file when updating the repository or commiting
     * changes to notify about progress.
     */
    public abstract void onNotify(NotifyInformation info);

    /**
     * Is called for every conflicted file when commiting changes. Return either
     * CONFLICT_RESET_FROM_SERVER to discard local changes or
     * CONFLICT_OVERRIDE_FROM_WC to check in the local copy regardless.
     */
    protected abstract int resolveConflict(Status status);

    /**
     * Is called once before commiting to get the commit message. Return null to
     * cancel commit.
     */
    protected abstract String getCommitMessage();

    /**
     * Copies a file from source to dest.
     */
    private void copyFile(String source, String dest) throws IOException {
        FileUtils.copyFile(new File(source), new File(dest));
    }
}
