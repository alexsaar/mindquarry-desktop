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
package com.mindquarry.desktop.workspace.deprecated;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tigris.subversion.javahl.ChangePath;
import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.Info;
import org.tigris.subversion.javahl.LogMessage;
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
    private Log log = LogFactory.getLog(this.getClass());
    
    public static final int CONFLICT_RESET_FROM_SERVER = 0;

    public static final int CONFLICT_OVERRIDE_FROM_WC = 1;
    
    public static final int CONFLICT_RENAME_AND_RETRY = 2;

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
            cleanup();
            client.status(localPath, false, false, true);
        } catch (ClientException e) {
            // no working copy, checkout
            initialCheckout();
            return;
        }
        client.update(localPath, Revision.HEAD, true);
    }
    
    public void updateSelectedFiles(String[] files) {
        cleanup();
        for (String file : files) {
            try {
                client.update(file, Revision.HEAD, true);
            }
            catch (ClientException e) { }
        }
    }
    
    public void addSelectedFiles(String[] files) {
        cleanup();
        for (String file : files) {
            try {
                client.add(file, true, true);
            }
            catch (ClientException e) { }
        }
    }

    public void prepareFiles() {
        try {
            Status[] stati = client.status(localPath, true, false, true);
            for (Status stat : stati) {
                if (stat.getTextStatus() == StatusKind.missing || stat.getTextStatus() == StatusKind.deleted) {
                    // first remove files that the user deleted manually (otherwise
                    // the update following later would re-fetch them):
                    //System.err.println("remove and commit " + stat.getPath() + " -> stat.getTextStatus() " + stat.getTextStatus());
                    client.remove(new String[] { stat.getPath() }, null, true);
                    client.commit(new String[] { stat.getPath() }, null, true);
                }
            }
        }   
        catch (ClientException e) {
            log.error(e.toString(), e);
        }
    }

    /**
     * Performs a new checkout from repositoryURL
     */
    protected void initialCheckout() throws ClientException {
        // TODO: check if localPath already exists and if we need to move it
        // first?
        client.checkout(repositoryURL, localPath, Revision.HEAD, true);
    }

    /**
     * Returns all changed or newly created files in the working copy at
     * localPath. This includes files that have not been scheduled for addition
     * to the repository.
     */
    public Status[] getLocalChanges() throws ClientException {
        cleanup();
        prepareFiles();
        
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

/*                // skip directories
                if (stat.getNodeKind() == NodeKind.dir) {
                    continue;
                }*/

                // skip ignored files
                if (stat.getTextStatus() == StatusKind.ignored
                        || stat.getTextStatus() == StatusKind.external) {
                    continue;
                }

                // skip conflict related files (.mine, .rxyz)
                boolean isConflictRelatedFile = false;
                for (String conflict : conflicts) {
                    if (stat.getPath().startsWith(conflict + ".")) { //$NON-NLS-1$
                        isConflictRelatedFile = true;
                        break;
                    }
                }
                if (isConflictRelatedFile) {
                    continue;
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

    public ChangePath[] getRemoteChanges() {
        LogMessage[] messages;
        
        // get local revision, path and log messages from here to HEAD
        int trimLength;
        Info info;
        try {
            info = client.info(localPath);
            trimLength = info.getUrl().length() - info.getRepository().length();
            long revision = info.getRevision() + 1;
            Revision start = Revision.getInstance(revision);
            Revision head = Revision.HEAD;
            messages = client.logMessages(localPath, start, head, false, true,
                    0);
        } catch (ClientException e) {
            return new ChangePath[0];
        }
        
        ArrayList<ChangePath> changePaths = new ArrayList<ChangePath>();
        for (LogMessage message : messages) {
            long revision = message.getRevisionNumber();
            ChangePath[] changes = message.getChangedPaths();
            for (ChangePath change : changes) {
                String absPath = change.getPath();
                
                String path = absPath;
                // truncate path to be relative to wc root
                if (absPath.length() > trimLength) {
                    path = absPath.substring(trimLength + 1);
                }
                
                // get the items local revision
                String itemLocalPath = localPath + "/" + path; //$NON-NLS-1$
                long itemRev = -1;
                try {
                    Info itemInfo = client.info(itemLocalPath);
/*                    // skip directories
                    if (itemInfo.getNodeKind() != NodeKind.file) {
                        continue;
                    }*/
                    if (itemInfo != null) {
                        itemRev = itemInfo.getLastChangedRevision();
                    }
                } catch (ClientException e) { }

                // couldn't get local rev, file is probably missing
                // so: skip if deleted on server
                if (itemRev == -1 && change.getAction() == 'D') {
                    continue;
                }

                // if our copy of this item is newer that the log message, skip it
                // and wait for possibly newer log messages
                if (itemRev > revision) {
                    continue;
                }
                
                // check if we already added this change to our list
                ChangePath existing = null;
                for (ChangePath oldChange : changePaths) {
                    if (oldChange.getPath().equals(absPath)) {
                        existing = oldChange;
                        break;
                    }
                }
                if (existing != null) {
                    // remove it if it's been deleted
                    if (change.getAction() == 'D') {
                        changePaths.remove(existing);
                    }
                    continue;
                }
                
                // add the change path to the result array
                changePaths.add(change);
            }
        }
        return changePaths.toArray(new ChangePath[0]);
    }

    /**
     * Commits all specified files to the repository. Will call
     * resolveConflict() for each conflicted file and getCommitMessage() once to
     * get the commit message before commiting.
     *
     * @return true if commit succeeded, false if getCommitMessage() returned null
     */
    public boolean commit(String[] paths) throws Exception {
        cleanup();
        for (String path : paths) {
            Status status = client.singleStatus(path, false);
            String basePath = new File(path).getParentFile().getAbsolutePath();

            if (status.getTextStatus() == StatusKind.unversioned) {
                client.add(path, true, true);
            } else if (status.getTextStatus() == StatusKind.conflicted) {
                // check for conflict resolve method
                switch (resolveConflict(status)) {
                case CONFLICT_RESET_FROM_SERVER:
                    // copy latest revision from repository to main file
                    new File(basePath + "/" //$NON-NLS-1$
                            + status.getConflictNew()).renameTo(new File(status
                            .getPath()));
                    break;
                case CONFLICT_OVERRIDE_FROM_WC:
                    // copy local changes to main file
                    new File(basePath + "/" //$NON-NLS-1$
                            + status.getConflictWorking()).renameTo(new File(
                            status.getPath()));
                    break;
                }
                client.resolved(status.getPath(), false);
            }
        }
        String message = getCommitMessage();

        // if getCommitMessage returns null, we don't commit
        if (message == null) {
            return false;
        }
        client.commit(paths, message, true);
        return true;
    }

    /**
     * Cancels the current SVN operation
     */
    protected void cancelOperation() throws ClientException {
        client.cancelOperation();
    }
    
    protected void cleanup() {
        try {
            client.cleanup(localPath);
        }
        catch (ClientException e) { }
    }
    
    public String getWorkingCopyRelativePath() throws ClientException {
        String relPath = null;
        try {
            Info info = client.info(localPath);
            relPath = info.getUrl().substring(info.getRepository().length());
        }
        catch (ClientException e) {
            relPath = "/"; //$NON-NLS-1$
        }
        return relPath;
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

}
