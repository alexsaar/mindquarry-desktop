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

import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.Status;
import org.tigris.subversion.javahl.StatusKind;

import com.mindquarry.desktop.workspace.exception.CancelException;

/**
 * Local add conflicts with remote add of object with the same name.
 * 
 * @author <a href="mailto:saar@mindquarry.com">Alexander Saar</a>
 * @author <a href="mailto:victor.saar@mindquarry.com">Victor Saar</a>
 * @author <a href="mailto:klimetschek@mindquarry.com">Alexander Klimetschek</a>
 */
public class AddConflict extends Conflict {
	private Action action = Action.UNKNOWN;
	
	public enum Action {
        /**
         * Indicating no conflict solution action was chosen yet.
         */
		UNKNOWN,
		/**
		 * Rename locally to a different file/folder name to avoid the conflict.
		 */
		RENAME,
		/**
		 * Replace the local file with the remotely added one, overwriting the local data.
		 */
		REPLACE;
	}
	
	private String newName;
    private List<Status> localAdded;
    private List<Status> remoteAdded;
	
	public AddConflict(Status localStatus, List<Status> localAdded, List<Status> remoteAdded) {
		super(localStatus);
		this.localAdded = localAdded;
		this.remoteAdded = remoteAdded;
	}

	public void handleBeforeUpdate() throws ClientException {
		File file = new File(status.getPath());
		
		switch (action) {
		case UNKNOWN:
			// client did not set a conflict resolution
			log.error("AddConflict with no action set: " + status.getPath());
			break;
			
		case RENAME:
			log.info("renaming to " + newName);

			if (status.getTextStatus() != StatusKind.unversioned) {
    			// the file is added, but in order to rename it without breaking
    			// the svn status we have to revert it to 'unversioned'
    			client.revert(status.getPath(), true);
			}
			
			if (!file.renameTo(new File(file.getParentFile(), newName))) {
				log.error("rename to " + newName + " failed.");
				// TODO: callback for error handling
				System.exit(-1);
			}
			break;
			
		case REPLACE:
			log.info("replacing with new file/folder from server: " + status.getPath());
			
            if (status.getTextStatus() != StatusKind.unversioned) {
                // the file is added, but in order to delete it without breaking
                // the svn status we have to revert it to 'unversioned'
                client.revert(status.getPath(), true);
            }
            
			if (!file.delete()) {
				log.error("deleting failed.");
				// TODO: callback for error handling
				System.exit(-1);
			}
			break;
		}
	}

	public void handleAfterUpdate() {
		// nothing to do here
	}

	public void accept(ConflictHandler handler) throws CancelException {
		handler.handle(this);
	}
	
	public boolean isRenamePossible(String newName) {
	    // TODO: implement this and check for local files/folders with the same
	    // name but also for remotely added stuff that will come along with the
	    // next update making the new name a conflict as well
	    return true;
	}

	public void doRename(String newName) {
		this.action = Action.RENAME;
		this.newName = newName;
	}
	
	public void doReplace() {
		this.action = Action.REPLACE;
	}
	
	public String toString() {
		return "Add/Add Conflict: " + status.getPath() + (action == Action.UNKNOWN ? "" : " " + action.name());
	}

    public List<Status> getLocalAdded() {
        return localAdded;
    }

    public List<Status> getRemoteAdded() {
        return remoteAdded;
    }
}