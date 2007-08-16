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
 * Local delete (of folder or file) conflicts with remote modification of
 * file or contents of the folder.
 * 
 * @author <a href="mailto:alexander(dot)klimetschek(at)mindquarry(dot)com">
 *         Alexander Klimetschek</a>
 *
 */
public class DeleteWithModificationConflict extends Conflict {
	private Action action = Action.UNKNOWN;
	
	public enum Action {
		UNKNOWN, DELETE, KEEPADDED;
	}
	
	// TODO: add list of remote changes here
	public DeleteWithModificationConflict(Status localStatus) {
		super(localStatus);
	}

	public void handleBeforeUpdate() {
		File file = new File(localStatus.getPath());
		
		switch (action) {
		case UNKNOWN:
			// client did not set a conflict resolution
			System.err.println("AddConflict with no action set: " + localStatus.getPath());
			break;
			
		case DELETE:
			System.out.println("deleting " + localStatus.getPath());
			
			// delete the added/remote one
			
			break;
			
		case KEEPADDED:
			System.out.println("keeping added from remote: " + localStatus.getPath());
			
			break;
		}
	}

	public void handleAfterUpdate() {
		// nothing to do here
	}

	public void accept(ConflictHandler handler) throws CancelException {
		handler.visit(this);
	}

	public void doDelete() {
		this.action = Action.DELETE;
	}
	
    public void doKeepAdded() {
        this.action = Action.KEEPADDED;
    }
    
	public String toString() {
		return "Delete/Modification Conflict: " + localStatus.getPath() + (action == Action.UNKNOWN ? "" : " " + action.name());
	}
}
