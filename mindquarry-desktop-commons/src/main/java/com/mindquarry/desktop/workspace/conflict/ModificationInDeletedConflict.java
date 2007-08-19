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

import org.tigris.subversion.javahl.Status;

import com.mindquarry.desktop.workspace.exception.CancelException;

/**
 * Local add conflicts with remotely deleted parent folder.
 * 
 * @author <a href="mailto:saar@mindquarry.com">Alexander Saar</a>
 * @author <a href="mailto:victor.saar@mindquarry.com">Victor Saar</a>
 * @author <a href="mailto:klimetschek@mindquarry.com">Alexander Klimetschek</a>
 */
public class ModificationInDeletedConflict extends Conflict {
	private Action action = Action.UNKNOWN;
	
	public enum Action {
		UNKNOWN, READD, DELETE, MOVE;
	}
	
	public ModificationInDeletedConflict(Status localStatus) {
		super(localStatus);
	}

	public void beforeUpdate() {
		File file = new File(status.getPath());
		
		switch (action) {
		case UNKNOWN:
			// client did not set a conflict resolution
			System.err.println("AddInDeletedConflict with no action set: " + status.getPath());
			break;
			
		case READD:
			log.info("readding to " + status.getPath());
			// nothing to do here, got this for free
			break;
			
		case DELETE:
			log.info("deleting file/folder: " + status.getPath());
			
			if (!file.delete()) {
				log.error("deleting failed.");
				// TODO: callback for error handling
				System.exit(-1);
			}
			break;
		case MOVE:
		    log.error("AddInDeletedConflict: action MOVE not implemented yet");
		    break;
		}
	}

	public void afterUpdate() {
		// nothing to do here
	}

	public void accept(ConflictHandler handler) throws CancelException {
		handler.handle(this);
	}

	public void doReAdd() {
		this.action = Action.READD;
	}
	
	public void doDelete() {
		this.action = Action.DELETE;
	}
	
//	public void doMove() {
//		this.action = Action.MOVE;
//	}
	
	public String toString() {
		return "Add/InDeleted Conflict: " + status.getPath() + (action == Action.UNKNOWN ? "" : " " + action.name());
	}
}
