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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceStore;
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
import com.mindquarry.desktop.preferences.profile.Profile;
import com.mindquarry.desktop.util.HttpUtilities;

/**
 * Add summary documentation here.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class CreateTaskAction extends ActionBase {
    public static final String ID = CreateTaskAction.class.getSimpleName();

	private TaskContainerWidget taskContainer;

	private static final Image IMAGE = new Image(
			Display.getCurrent(),
			CreateTaskAction.class
					.getResourceAsStream("/com/mindquarry/icons/" + ICON_SIZE + "/actions/task-add.png")); //$NON-NLS-1$

	public CreateTaskAction(MindClient client) {
		super(client);

		setId(ID);
		setActionDefinitionId(ID);

		setText(Messages.getString("New Task"));
		setToolTipText(Messages.getString("Creates a new task."));
		setAccelerator(SWT.CTRL + +SWT.SHIFT + 'A');
		setImageDescriptor(ImageDescriptor.createFromImage(IMAGE));
	}

	public void run() {
		PreferenceStore store = client.getPreferenceStore();
		Profile profile = Profile.getSelectedProfile(store);

		// create initial task
		Task task = new Task();
		task.setStatus("new"); //$NON-NLS-1$
		task.setPriority("low"); //$NON-NLS-1$
		task.setTitle(Messages.getString("new task")); //$NON-NLS-1$
		task.setSummary(Messages.getString("summary of the task")); //$NON-NLS-1$
		task.setDate(null); // no due date by default

		List teams = client.getSelectedTeams();
		if (teams.size() == 0) {
			MessageDialog.openError(new Shell(SWT.None), Messages
					.getString("Error"), Messages //$NON-NLS-1$
					.getString("Cannot not add a new task because of one of the following issues:\n\n" +
                            "(1) You are not a member of any team.\n" +
                            "(2) You currently have no teams selected.\n" +
                            "(3) Your Mindquarry server settings are not correct.")); //$NON-NLS-1$
			return;
		}

		TaskSettingsDialog dlg = new TaskSettingsDialog(client.getShell(),
				task, true);
		if (dlg.open() == Window.OK) {
			boolean published = false;
			String teamID = null;
			if (teams.size() == 1) {
				// don't show dialog if user is in only one team anyway
				teamID = ((Team) teams.get(0)).getId();
				published = publishTask(task, teamID);
			} else {
				// display dialog for team selection
				TeamSelectionDialog tsDlg = new TeamSelectionDialog(new Shell(
						SWT.ON_TOP), teams);
				if (tsDlg.open() == Window.OK) {
					teamID = tsDlg.getSelectedTeam();
					published = publishTask(task, teamID);
				}
			}
			if (published) {
				taskContainer.addTask(task);
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
			MessageDialog.openError(new Shell(SWT.ON_TOP), 
			        Messages.getString("Network error"),
			        Messages.getString("Could not create the task."));
			log.error(Messages.getString("Could not create the task."), e); //$NON-NLS-1$
		}
		return published;
	}

	public void setTaskContainer(TaskContainerWidget taskContainer) {
		this.taskContainer = taskContainer;
	}
	
	public String getGroup() {
        return ActionBase.TASK_ACTION_GROUP;
    }
	
	public boolean isToolbarAction() {
        return true;
    }
}
