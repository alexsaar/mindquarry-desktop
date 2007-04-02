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
import java.io.IOException;
import java.util.ArrayList;

import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.Info;
import org.tigris.subversion.javahl.LogMessage;
import org.tigris.subversion.javahl.ChangePath;
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

    public LogMessage[] getRemoteChanges() {
        // Note: not used right now
	try {
	    Info info = client.info(localPath);
//	    long revision = info.getRevision() + 1;
	    Revision start = Revision.BASE;
	    Revision head = Revision.HEAD;
	    return client.logMessages(localPath, start, head, false, true, 0);
	} catch (ClientException e) {
	    return new LogMessage[0];
	}
    }

    public String[] getRemoteChangePaths() {
        LogMessage[] messages;
        int trimLength;
        Info info;
        try {
            info = client.info(localPath);
            trimLength = info.getUrl().length() - info.getRepository().length();
            long revision = info.getRevision() + 1;
            Revision start = Revision.getInstance(revision);
    	    Revision head = Revision.HEAD;
    	    messages = client.logMessages(localPath, start, head, false, true, 0);
        }
        catch (ClientException e) {
            return new String[0];
        }
        ArrayList<String> changePaths = new ArrayList<String>();
        for (LogMessage message : messages) {
            long revision = message.getRevisionNumber();
            ChangePath[] changes = message.getChangedPaths();
            for (ChangePath change : changes) {
                String absPath = change.getPath();
                String path;
                if (absPath.length() > trimLength)
                    path = absPath.substring(trimLength + 1);
                else
                    continue;
                
                if (changePaths.contains(path))
                    continue;
                
                long itemRev = 0;
                try {
                    String itemLocalPath = localPath + "/" + path;
//                    System.out.println("item local path " + itemLocalPath);
                    Info itemInfo = client.info(itemLocalPath);
                    if (itemInfo != null)
                        itemRev = itemInfo.getLastChangedRevision();
                }
                catch (ClientException e) { }
                
                if (itemRev > revision - 1) {
//                    System.out.println("skipping newer: " + path);
                    continue;
                }
                
//                System.out.println("rch adding " + path + " irev " + itemRev + " msg rev " + revision);
                changePaths.add(path);
            }
        }

        return changePaths.toArray(new String[0]);
    }

    /**
     * Commits all specified files to the repository. Will call
     * resolveConflict() for each conflicted file and getCommitMessage() once to
     * get the commit message before commiting.
     */
    public void commit(String[] paths) throws Exception {
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
			    + status.getConflictNew()).renameTo(new File(status.getPath()));
		    break;
		case CONFLICT_OVERRIDE_FROM_WC:
		    // copy local changes to main file
		    new File(basePath + "/" //$NON-NLS-1$
			    + status.getConflictWorking()).renameTo(new File(status.getPath()));
		    break;
		}
		client.resolved(status.getPath(), false);
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

}
