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
package com.mindquarry.desktop.client.action.app;

import org.eclipse.jface.action.Action;

import com.mindquarry.desktop.client.I18N;
import com.mindquarry.desktop.client.MindClient;

/**
 * Shows the main window (used in tray icon).
 * 
 * @author <a href="mailto:alexander(dot)klimetschek(at)mindquarry(dot)com">
 *         Alexander Klimetschek</a>
 *
 */
public class ShowMainWindowAction extends Action {
    public static final String ID = ShowMainWindowAction.class.getSimpleName();
    
    private MindClient client;

	public ShowMainWindowAction(MindClient client) {
		super();
		
		this.client = client;

		setId(ID);
		setActionDefinitionId(ID);

		setText(I18N.getString("Show Window"));//$NON-NLS-1$
		setToolTipText(I18N.getString("Displays the Mindquarry Desktop Client main window"));//$NON-NLS-1$
	}

	public void run() {
        client.getShell().open();
	    client.getShell().forceActive();
	}
	
	public boolean isToolbarAction() {
        return false;
    }
}
