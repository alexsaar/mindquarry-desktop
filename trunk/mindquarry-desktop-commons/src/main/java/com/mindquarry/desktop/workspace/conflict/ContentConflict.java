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

import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.Status;

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

    private void resolveConflict() throws ClientException {
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
            if (!conflictFile.delete()) {
                log.error("deleting failed.");
                // TODO: callback for error handling
                System.exit(-1);
            }
            
            if (!remoteFile.renameTo(conflictFile)) {
                log.error("renaming failed.");
                // TODO: callback for error handling
                System.exit(-1);
            }
            break;
            
        case USE_LOCAL:
            // copy local changes to main file
            parent = new File(status.getPath()).getParentFile();
            File localFile = new File(parent, status.getConflictWorking());
            
            conflictFile = new File(status.getPath());
            if (!conflictFile.delete()) {
                log.error("deleting failed.");
                // TODO: callback for error handling
                System.exit(-1);
            }
            
            if (!localFile.renameTo(conflictFile)) {
                log.error("renaming failed.");
                // TODO: callback for error handling
                System.exit(-1);
            }
            break;
            
        case MERGE:
            // TODO: GUI callback ??
            log.error("content conflict merge not implemented yet");
            return;
        }
        
        client.resolved(status.getPath(), false);
    }

    @Override
    public void beforeCommit() throws ClientException {
        resolveConflict();
    }

    @Override
    public void beforeRemoteStatus() throws ClientException {
        resolveConflict();
    }

    public String toString() {
        return "Content Conflict: " + status.getPath() + (action == Action.UNKNOWN ? "" : " " + action.name());
    }
    
    public void doUseLocal() {
        this.action = Action.USE_LOCAL;
    }

    public void doUseRemote() {
        this.action = Action.USE_REMOTE;
    }

    public void doMerge() {
        this.action = Action.MERGE;
    }
}