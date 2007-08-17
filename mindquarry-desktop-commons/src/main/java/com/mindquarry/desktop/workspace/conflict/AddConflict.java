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
		UNKNOWN, RENAME, REPLACE;
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
		File file = new File(localStatus.getPath());
		
		switch (action) {
		case UNKNOWN:
			// client did not set a conflict resolution
			log.error("AddConflict with no action set: " + localStatus.getPath());
			break;
			
		case RENAME:
			log.info("renaming to " + newName);
			
			client.revert(localStatus.getPath(), true);
			client.cleanup(new File(localStatus.getPath()).getParentFile().getPath());
			
			if (!file.renameTo(new File(file.getParentFile(), newName))) {
				log.error("rename to " + newName + " failed.");
				// TODO: callback for error handling
				System.exit(-1);
			}
			// FIXME: if status.getTextStatus() == StatusKind.added we have to
			// revert before the rename (only when user uses svn client with svn add)
			break;
			
		case REPLACE:
			log.info("replacing with new file/folder from server: " + localStatus.getPath());
			
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

	public void doRename(String newName) {
		this.action = Action.RENAME;
		this.newName = newName;
	}
	
//	public void doReplace() {
//		this.action = Action.REPLACE;
//	}
	
	public String toString() {
		return "Add/Add Conflict: " + localStatus.getPath() + (action == Action.UNKNOWN ? "" : " " + action.name());
	}

    public Action getAction() {
        return action;
    }

    public List<Status> getLocalAdded() {
        return localAdded;
    }

    public List<Status> getRemoteAdded() {
        return remoteAdded;
    }
}
