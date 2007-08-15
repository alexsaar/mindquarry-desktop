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
package com.mindquarry.desktop.client.action.task;

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.mindquarry.desktop.client.Messages;
import com.mindquarry.desktop.client.MindClient;
import com.mindquarry.desktop.client.action.ActionBase;
import com.mindquarry.desktop.client.dialog.task.TaskSettingsDialog;
import com.mindquarry.desktop.client.dialog.team.TeamSelectionDialog;
import com.mindquarry.desktop.client.widget.task.TaskContainerWidget;
import com.mindquarry.desktop.model.task.Task;
import com.mindquarry.desktop.model.team.Team;
import com.mindquarry.desktop.model.team.TeamList;
import com.mindquarry.desktop.preferences.profile.Profile;
import com.mindquarry.desktop.util.HttpUtilities;

/**
 * Add summary documentation here.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class CreateTaskAction extends ActionBase {
	public static final String ID = "add-task";
	
	private TaskContainerWidget taskContainer;

	private static final Image IMAGE = new Image(
			Display.getCurrent(),
			CreateTaskAction.class
					.getResourceAsStream("/com/mindquarry/icons/" + ICON_SIZE + "/actions/task-add.png")); //$NON-NLS-1$

	public CreateTaskAction(MindClient client) {
		super(client);

		setId(ID);
		setActionDefinitionId(ID);

		setText(TEXT);
		setToolTipText(TOOLTIP);
		setAccelerator(SWT.CTRL + +SWT.SHIFT + 'A');
		setImageDescriptor(ImageDescriptor.createFromImage(IMAGE));
	}

	public void run() {
		Profile profile = Profile.getSelectedProfile(client
				.getPreferenceStore());

		// retrieve list of teams
		TeamList teamList;
		try {
			teamList = new TeamList(profile.getServerURL() + "/teams", //$NON-NLS-1$
					profile.getLogin(), profile.getPassword());
		} catch (Exception e) {
			client.showMessage(Messages.getString(
					"com.mindquarry.desktop.client", //$NON-NLS-1$
					"error"), //$NON-NLS-1$
					Messages.getString(CreateTaskAction.class, "0")); //$NON-NLS-1$
			log.error("Error while updating team list.", e); //$NON-NLS-1$
			return;
		}
		if (teamList.getTeams().size() == 0) {
			client.showMessage(Messages.getString(CreateTaskAction.class, "1"), //$NON-NLS-1$
					Messages.getString(CreateTaskAction.class, "2")); //$NON-NLS-1$
		}
		// create initial task
		Task task = new Task();
		task.setStatus("new"); //$NON-NLS-1$
		task.setPriority("low"); //$NON-NLS-1$
		task.setTitle(Messages.getString(getClass(), "3")); //$NON-NLS-1$
		task.setSummary(Messages.getString(getClass(), "4")); //$NON-NLS-1$
		task.setDate(null); // no due date by default

		TaskSettingsDialog dlg = new TaskSettingsDialog(new Shell(SWT.ON_TOP),
				task, true);
		if (dlg.open() == Window.OK) {
			try {
				boolean published = false;
				List teams = teamList.getTeams();
				if (teams.size() == 1) {
					// don't show dialog if user is in only one team anyway
					Team theOnlyTeam = (Team) teams.get(0);
					published = publishTask(task, theOnlyTeam.getId());
				} else {
					TeamSelectionDialog tsDlg = new TeamSelectionDialog(
							new Shell(SWT.ON_TOP), teams);
					if (tsDlg.open() == Window.OK) {
						published = publishTask(task, tsDlg.getSelectedTeam());
					}
				}
				if(published) {
					taskContainer.addTask(task);
				}
			} catch (Exception e) {
				client.showMessage(Messages.getString(getClass(), "5"), //$NON-NLS-1$
						Messages.getString(getClass(), "6")); //$NON-NLS-1$
				log.error(Messages.getString(getClass(), "6"), e); //$NON-NLS-1$
			}
		}
	}

	private boolean publishTask(Task task, String teamID) {
		boolean published = true;
		Profile profile = Profile.getSelectedProfile(client
				.getPreferenceStore());
		try {
			String taskID = HttpUtilities.putAsXML(profile.getLogin(), profile
					.getPassword(), profile.getServerURL() + "/tasks/" //$NON-NLS-1$
					+ teamID + "/new", //$NON-NLS-1$
					task.getContentAsXML().asXML().getBytes("utf-8"));//$NON-NLS-1$
			task.setId(taskID);
		} catch (Exception e) {
			published = false;
			client.showMessage(Messages.getString(getClass(), "5"), //$NON-NLS-1$
					Messages.getString(getClass(), "6")); //$NON-NLS-1$
			log.error(Messages.getString(getClass(), "6"), e); //$NON-NLS-1$
		}
		return published;
	}
	
	public void setTaskContainer(TaskContainerWidget taskContainer) {
		this.taskContainer = taskContainer;
	}
}
