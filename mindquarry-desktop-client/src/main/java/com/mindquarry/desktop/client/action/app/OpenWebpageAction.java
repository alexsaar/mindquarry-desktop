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

import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;

import com.mindquarry.desktop.client.I18N;
import com.mindquarry.desktop.client.MindClient;
import com.mindquarry.desktop.client.action.ActionBase;
import com.mindquarry.desktop.preferences.profile.Profile;

/**
 * Add summary documentation here.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class OpenWebpageAction extends ActionBase {
    public static final String ID = OpenWebpageAction.class.getSimpleName();

	private static final Image IMAGE = new Image(
			Display.getCurrent(),
			OpenWebpageAction.class
					.getResourceAsStream("/org/tango-project/tango-icon-theme/" + ICON_SIZE + "/apps/internet-web-browser.png")); //$NON-NLS-1$

	public OpenWebpageAction(MindClient client) {
		super(client);

		setId(ID);
		setActionDefinitionId(ID);

		setText(I18N.getString("Go to webpage"));//$NON-NLS-1$
		setToolTipText(I18N.getString("Open the web interface of the current profiles server."));//$NON-NLS-1$
		//setAccelerator(SWT.CTRL | SWT.SHIFT | 'O');
		setImageDescriptor(ImageDescriptor.createFromImage(IMAGE));
	}

	public void run() {
		PreferenceStore store = client.getPreferenceStore();
		if (Profile.getSelectedProfile(store) != null) {
			Program.launch(Profile.getSelectedProfile(store).getServerURL());
		}
	}
	
	public String getGroup() {
        return ActionBase.MANAGEMENT_ACTION_GROUP;
    }
	
	public boolean isToolbarAction() {
        return false;
    }
}
