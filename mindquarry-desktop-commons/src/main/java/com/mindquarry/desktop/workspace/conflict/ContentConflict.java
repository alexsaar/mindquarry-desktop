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
package com.mindquarry.desktop.workspace.conflict;

import java.io.File;
import java.io.IOException;

import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.Status;

import com.mindquarry.desktop.util.FileHelper;
import com.mindquarry.desktop.workspace.exception.CancelException;

/**
 * Represents the standard svn conflict for a file content's.
 * 
 * @author <a href="mailto:alexander(dot)klimetschek(at)mindquarry(dot)com">
 *         Alexander Klimetschek</a>
 *
 */
public class ContentConflict extends Conflict {

    private Action action = Action.UNKNOWN;
    
    public enum Action {
        /**
         * Indicating no conflict solution action was chosen yet.
         */
        UNKNOWN,
        /**
         * Use the local version (mine) of the file (loosing the server's
         * modifications).
         */
        USE_LOCAL,
        /**
         * Use the server version of the file (loosing the local modifications).
         */
        USE_REMOTE,
        /**
         * Call a merge program (ie. Word in merge-mode) to let the user handle
         * the conflicts.
         */
        MERGE;
    }
    
    
    public ContentConflict(Status status) {
        super(status);
    }

    @Override
    public void accept(ConflictHandler handler) throws CancelException {
        handler.handle(this);
    }

    private void resolveConflict() throws ClientException, IOException {
        // check for conflict resolve method
        switch (action) {
        case UNKNOWN:
            // client did not set a conflict resolution
            log.error("ContentConflict with no action set: " + status.getPath());
            return;
            
        case USE_REMOTE:
            // copy latest revision from repository to main file
            File parent = new File(status.getPath()).getParentFile();
            File remoteFile = new File(parent, status.getConflictNew());
            
            File conflictFile = new File(status.getPath());
            FileHelper.delete(conflictFile);
            FileHelper.renameTo(remoteFile, conflictFile);
            
            break;
            
        case USE_LOCAL:
            // just keep the local file
            break;
            
        case MERGE:
            // all work (except resolving) is done in the GUI class
        	break;
        }
        
        client.resolved(status.getPath(), false);
    }

    @Override
    public void beforeCommit() throws ClientException, IOException {
        resolveConflict();
    }

    @Override
    public void beforeRemoteStatus() throws ClientException, IOException {
        resolveConflict();
    }

    public String toString() {
        return "Content Conflict: " + status.getPath() + (action == Action.UNKNOWN ? "" : " " + action.name());
    }
    
    /**
     * Resolves the conflict by using the local content of the file. Note that
     * the local modifications will be lost!
     */
    public void doUseLocal() {
        this.action = Action.USE_LOCAL;
    }

    /**
     * Resolves the conflict by using the remote content of the file. The remote
     * modifications are not lost, they are already part of a version on the
     * server.
     */
    public void doUseRemote() {
        this.action = Action.USE_REMOTE;
    }

    /**
     * Resolves the conflict by merging - this actually does nothing except for
     * resolving the conflict for svn. This expects the file to contain the new
     * merged content - how this happens is up to the client code or user.
     */
    public void doMerge() {
        this.action = Action.MERGE;
    }
}
