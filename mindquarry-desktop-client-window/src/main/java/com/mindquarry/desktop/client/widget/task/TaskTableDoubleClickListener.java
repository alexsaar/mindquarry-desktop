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
package com.mindquarry.desktop.client.widget.task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import com.mindquarry.desktop.client.Messages;
import com.mindquarry.desktop.client.MindClient;
import com.mindquarry.desktop.client.dialog.task.TaskSettingsDialog;
import com.mindquarry.desktop.model.task.Task;
import com.mindquarry.desktop.model.task.TaskList;
import com.mindquarry.desktop.preferences.profile.Profile;
import com.mindquarry.desktop.util.HttpUtilities;
import com.sun.org.apache.bcel.internal.generic.GETSTATIC;

/**
 * Add summary documentation here.
 * 
 * @author <a href="mailto:saar@mindquarry.com">Alexander Saar</a>
 * 
 */
public class TaskTableDoubleClickListener implements IDoubleClickListener {
	private static Log log = LogFactory
			.getLog(TaskTableDoubleClickListener.class);

	private final MindClient client;

	private final Table table;
	private final TableViewer tableViewer;

	private final TaskList tasks;

	public TaskTableDoubleClickListener(MindClient client, Table table,
			TableViewer tableViewer, TaskList tasks) {
		this.client = client;

		this.table = table;
		this.tableViewer = tableViewer;

		this.tasks = tasks;
	}

	public void doubleClick(DoubleClickEvent event) {
		Profile prof = Profile.getSelectedProfile(client.getPreferenceStore());

		ISelection selection = event.getSelection();

		if (selection instanceof StructuredSelection) {
			StructuredSelection structsel = (StructuredSelection) selection;
			Object element = structsel.getFirstElement();

			if (element instanceof Task) {
				Task task = (Task) element;

				try {
					// use a clone of the task so cancel works:
					TaskSettingsDialog dlg = new TaskSettingsDialog(new Shell(
							SWT.ON_TOP), task.clone(), false);

					if (dlg.open() == Window.OK) {
						int taskPos = tasks.getTasks().indexOf(task);
						if (taskPos != -1) {
							// can be -1 if set to "done" while the dialog
							// was open
							tasks.getTasks().set(taskPos, dlg.getChangedTask());
						}
						task = dlg.getChangedTask();
						HttpUtilities.putAsXML(prof.getLogin(), prof
								.getPassword(), task.getId(), task
								.getContentAsXML().asXML().getBytes("utf-8"));
					}
				} catch (Exception e) {
					MessageDialog
							.openError(
									new Shell(SWT.ON_TOP),
									Messages.getString(
											TaskContainerWidget.class, "6"), Messages.getString(TaskContainerWidget.class, "7")//$NON-NLS-1$
											+ ": " + e.toString());
					log.error("Could not update task with id " //$NON-NLS-1$
							+ task.getId(), e);
				}
			}
			if (table != null) {
				// avoid crash if last bug got closed but the task dialog
				// was still open and then OK was pressed:
				// (TODO: task is submitted but not visible until manual
				// refresh)
				tableViewer.refresh();
			}
		}
	}
}
