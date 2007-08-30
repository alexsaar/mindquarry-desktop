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
package com.mindquarry.desktop.client.action.team;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.mindquarry.desktop.client.Messages;
import com.mindquarry.desktop.client.MindClient;
import com.mindquarry.desktop.client.action.ActionBase;
import com.mindquarry.desktop.client.widget.team.TeamlistWidget;
import com.mindquarry.desktop.workspace.exception.CancelException;

/**
 * Add summary documentation here.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class RefreshTeamlistAction extends ActionBase {
    public static final String ID = RefreshTeamlistAction.class.getSimpleName();

	private TeamlistWidget teamList;

	private static final Image IMAGE = new Image(
			Display.getCurrent(),
			RefreshTeamlistAction.class
					.getResourceAsStream("/org/tango-project/tango-icon-theme/" + ICON_SIZE + "/actions/view-refresh.png")); //$NON-NLS-1$

	public RefreshTeamlistAction(MindClient client) {
		super(client);

		setId(ID);
		setActionDefinitionId(ID);

		setText(Messages.getString("Refresh list of teams"));
		setToolTipText(Messages.getString("Refresh the list of teams."));
		setAccelerator(SWT.CTRL + +SWT.SHIFT + 'S');
		setImageDescriptor(ImageDescriptor.createFromImage(IMAGE));
	}

	public void run() {
		try {
            teamList.refresh();
        } catch (CancelException e) {
            // TODO: better exception handling
            log.warn("Refreshing team list cancelled.", e);
        }
	}

	public void setTeamList(TeamlistWidget teamList) {
		this.teamList = teamList;
	}

    public String getGroup() {
        return ActionBase.TEAM_ACTION_GROUP;
    }
    
    public boolean isToolbarAction() {
        return false;
    }
}
