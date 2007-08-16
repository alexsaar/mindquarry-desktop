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
 * Local add conflicts with remote add of object with the same name.
 * 
 * @author <a href="mailto:saar@mindquarry.com">Alexander Saar</a>
 * 
 */
public class AddConflict extends Conflict {
	private Action action = Action.UNKNOWN;
	
	public enum Action {
		UNKNOWN, RENAME, REPLACE;
	}
	
	private String newName;
	
	public AddConflict(Status localStatus) {
		super(localStatus);
	}

	public void handleBeforeUpdate() {
		File file = new File(localStatus.getPath());
		
		switch (action) {
		case UNKNOWN:
			// client did not set a conflict resolution
			System.err.println("AddConflict with no action set: " + localStatus.getPath());
			break;
			
		case RENAME:
			System.out.println("renaming to " + newName);
			
			if (!file.renameTo(new File(file.getParentFile(), newName))) {
				System.err.println("rename to " + newName + " failed.");
				// TODO: callback for error handling
				System.exit(-1);
			}
			// FIXME: if status.getTextStatus() == StatusKind.added we have to
			// revert before the rename (only when user uses svn client with svn add)
			break;
			
		case REPLACE:
			System.out.println("replacing with new file/folder from server: " + localStatus.getPath());
			
			if (!file.delete()) {
				System.err.println("deleting failed.");
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
		handler.visit(this);
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
}
