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
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.Status;
import org.tigris.subversion.javahl.StatusKind;

import com.mindquarry.desktop.util.FileHelper;
import com.mindquarry.desktop.workspace.exception.CancelException;

/**
 * Local add conflicts with remote add of object with the same name.
 * 
 * @author <a href="mailto:saar@mindquarry.com">Alexander Saar</a>
 * @author <a href="mailto:victor.saar@mindquarry.com">Victor Saar</a>
 * @author <a href="mailto:klimetschek@mindquarry.com">Alexander Klimetschek</a>
 */
public class AddConflict extends RenamingConflict {
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
    
	public AddConflict(Status status, List<Status> localAdded, List<Status> remoteAdded) {
		super(status);
		this.localAdded = localAdded;
		this.remoteAdded = remoteAdded;
		
		this.folder = new File(status.getPath()).getParentFile();
	}

	public void beforeUpdate() throws ClientException, IOException {
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
			
			File destination = new File(file.getParentFile(), newName);
			FileHelper.renameTo(file, destination);
			// NOTE: this could fail if the file with newName already exists
			// although we do check this previously in isRenamePossible() it
			// can happen when there are multiple add conflicts and the user
			// chooses twice the same newName for different add conflicts -
			// this is very seldom and can simply be given as error messages
			
            client.add(destination.getPath(), true, true);
            
			break;
			
		case REPLACE:
			log.info("replacing with new file/folder from server: " + status.getPath());
			
            if (status.getTextStatus() != StatusKind.unversioned) {
                // the file is added, but in order to delete it without breaking
                // the svn status we have to revert it to 'unversioned'
                client.revert(status.getPath(), true);
            }

            FileUtils.forceDelete(file);
            
			break;
		}
	}

	public void afterUpdate() {
		// nothing to do here
	}

	public void accept(ConflictHandler handler) throws CancelException {
		handler.handle(this);
	}
	
	public void doRename(String newName) {
	    // TODO: double check here? what if it fails? throw exception? return false?
//	    if (!isRenamePossible(newName)) {
//	        
//	    }
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
