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
import java.util.List;

import org.tigris.subversion.javahl.SVNClient;
import org.tigris.subversion.javahl.Status;

import com.mindquarry.desktop.workspace.exception.CancelException;

/**
 * Local delete (of folder or file) conflicts with remote modification of
 * file or contents of the folder.
 * 
 * @author <a href="mailto:saar@mindquarry.com">Alexander Saar</a>
 * @author <a href="mailto:victor.saar@mindquarry.com">Victor Saar</a>
 * @author <a href="mailto:klimetschek@mindquarry.com">Alexander Klimetschek</a>
 */
public class DeleteWithModificationConflict extends Conflict {
	private Action action = Action.UNKNOWN;
	
	public enum Action {
		UNKNOWN, DELETE, KEEPMODIFIED, REVERTDELETE;
	}
	
	private List<Status> remoteMods;
	
	public DeleteWithModificationConflict(Status localStatus, List<Status> remoteMods) {
		super(localStatus);
		this.remoteMods = remoteMods;
	}

	public void handleBeforeUpdate() {
        // NOTE: here we could implement a fast-path avoiding the download
        // of the new files by simply deleting the folder on the server
        // before we run the update
	    // if (action == Action.DELETE) {
        // nothing to do here, we have to wait for the update and delete
        // the folder again (svn will recreate it due to the remote mods)
	    // }

	    if (action == Action.REVERTDELETE) {
	        // TODO: revert delete before update
	        
	        // status probably missing (not svn deleted yet) because it's a conflict
	    }
	}

	public void handleAfterUpdate() {
        switch (action) {
        case UNKNOWN:
            // client did not set a conflict resolution
            log.error("DeleteWithModificationConflict with no action set: " + localStatus.getPath());
            break;
            
        case DELETE:
            log.info("now deleting again " + localStatus.getPath());
            
            File file = new File(localStatus.getPath());
            if (!file.delete()) {
                log.error("deleting failed.");
                // TODO: callback for error handling
                System.exit(-1);
            }
            
            break;
            
        case KEEPMODIFIED:
            log.info("keeping added/modified from remote: " + localStatus.getPath());
            
            break;
            
        case REVERTDELETE:
            log.info("reverting delete: " + localStatus.getPath());
            
            // TODO: revert deleted status
            
            break;
        }
	}

	public void accept(ConflictHandler handler) throws CancelException {
		handler.handle(this);
	}

	public void doDelete() {
		this.action = Action.DELETE;
	}
	
    public void doKeepModified() {
        this.action = Action.KEEPMODIFIED;
    }
    
    public void doRevertDelete() {
        this.action = Action.REVERTDELETE;
    }
    
	public String toString() {
		return "Delete/Modification Conflict: " + localStatus.getPath() + (action == Action.UNKNOWN ? "" : " " + action.name());
	}

    public List<Status> getRemoteMods() {
        return remoteMods;
    }
}
