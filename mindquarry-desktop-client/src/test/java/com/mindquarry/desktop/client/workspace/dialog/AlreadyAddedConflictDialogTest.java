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
package com.mindquarry.desktop.client.workspace.dialog;

import com.mindquarry.desktop.workspace.deprecated.SVNHelper;



/**
 * Test the conflict dialog.
 *
 */
public class AlreadyAddedConflictDialogTest extends AbstractConflictDialogTest {
	
	public void testDialog() {
	    AlreadyAddedConflictDialog dlg = new AlreadyAddedConflictDialog(shell, fakeStatus, "my_file.txt");
		int res = dlg.open();
		if (res == 0) {
	        if (dlg.getResolveMethod() == SVNHelper.CONFLICT_OVERRIDE_FROM_WC) {
	            System.out.println("override changes on server with local workspace");
	        } else if (dlg.getResolveMethod() == SVNHelper.CONFLICT_RENAME_AND_RETRY) {
	            System.out.println("new name: " + dlg.getNewName());
	        } else {
	            fail("unexpected resolve method " + res);
	        }
		} else {
		    System.out.println("dialog cancelled");
		}
	}
	
}
