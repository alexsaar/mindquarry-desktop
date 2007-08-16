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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.tigris.subversion.javahl.Status;

import junit.framework.TestCase;

/**
 * Setup shell for testing the conflict dialogs.
 *
 * @author <a hef="mailto:saar@mindquarry.com">Alexander Saar</a>
 */
public class AbstractConflictDialogTest extends TestCase {
	private static final String APPLICATION_NAME = "test";
	
	protected Shell shell;
	
    protected Status fakeStatus = new Status("/home/dnaber/tools", "url", 0, 4711, 4710, 1000, "dnaber",
            0, 0, 0, 0, false, false, "conflictOld", "conflictNew", "conflictWorking",
            "urlCopiedFrom", 1, false, "lockToken", "lockOwner", "lockComment", 0, null,
            0, 44, 0, "reposLastCmtAuthor");
	
	public void setUp() {
		Display.setAppName(APPLICATION_NAME);
		
		Display display = Display.getDefault();
		display.setWarnings(true);

		shell = new Shell(display, SWT.ON_TOP);
		shell.setText(APPLICATION_NAME);
	}
}
