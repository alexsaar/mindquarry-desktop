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

import com.mindquarry.desktop.I18N;
import com.mindquarry.desktop.workspace.exception.CancelException;

/**
 * Represents the following conflicts replaced/modified, modified/replaced and
 * replaced/replaced.
 * 
 * @author <a href="mailto:victor.saar@mindquarry.com">Victor Saar</a>
 * @author <a href="mailto:klimetschek@mindquarry.com">Alexander Klimetschek</a>
 */
public class ReplaceConflict extends RenamingConflict {
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
    private List<Status> localChildren;
    private List<Status> remoteChildren;
    
	public ReplaceConflict(Status status, List<Status> localChildren, List<Status> remoteChildren) {
		super(status);
		this.localChildren = localChildren;
		this.remoteChildren = remoteChildren;
	}

	public void beforeUpdate() throws ClientException, IOException {
		File file = new File(status.getPath());
		
		switch (action) {
		case UNKNOWN:
			// client did not set a conflict resolution
			log.error("AddConflict with no action set: " + status.getPath());
			break;
			
		case RENAME:
			log.info("renaming " +file.getAbsolutePath()+ " to " + newName);
			
            File source      = new File(status.getPath());
            File destination = new File(source.getParent(), newName);
            
            if (source.isDirectory()) {
                FileUtils.copyDirectory(source, destination);

                removeDotSVNDirectories(destination.getPath());
            } else {
                FileUtils.copyFile(source, destination);
            }

            client.add(destination.getPath(), true, true);
            client.remove(new String[] { file.getPath() }, null, true);
			
			break;
			
		case REPLACE:
			log.info("replacing with new file/folder from server: " + status.getPath());
			
            client.revert(file.getPath(), true);
            
            if (status.getRepositoryTextStatus() == StatusKind.replaced) {
                FileUtils.forceDelete(file);
            }
            
			break;
		}
	}

    public void afterUpdate() {
		// nothing to do here
	}

	public void accept(ConflictHandler handler) throws CancelException {
		handler.handle(this);
	}

    /**
     * Resolve the conflict by renaming the local file or dir before updating.
     * 
     * Make sure you have called isRenamePossible(newName) and it returned true
     * before calling doRename(newName).
     * 
     * @param newName the new name of the file to rename to. Must be just a
     *                filename but not a full path name.
     */
	public void doRename(String newName) {
		this.action = Action.RENAME;
		this.newName = newName;
	}
	
    /**
     * Resolve the conflict by replacing the local content with the one from the
     * server. Note that the local file or directory content will be lost!
     */
	public void doReplace() {
		this.action = Action.REPLACE;
	}
	
	public String toString() {
		return "Replaced/Modified/Replaced Conflict: " + status.getPath() + (action == Action.UNKNOWN ? "" : " " + action.name());
	}

    public List<Status> getLocalChildren() {
        return localChildren;
    }

    public List<Status> getRemoteChildren() {
        return remoteChildren;
    }
    
    @Override
    public String getLongDescription() {
        return I18N.get("This item has been replaced locally or remotely. " +
                "You will need to resolve the conflict.");
    }
}
