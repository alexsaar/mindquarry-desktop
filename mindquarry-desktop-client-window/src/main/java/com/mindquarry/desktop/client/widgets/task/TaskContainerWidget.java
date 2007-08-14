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
package com.mindquarry.desktop.client.widgets.task;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.mindquarry.desktop.client.Messages;
import com.mindquarry.desktop.client.MindClient;
import com.mindquarry.desktop.client.dialog.task.TaskSettingsDialog;
import com.mindquarry.desktop.client.widgets.WidgetBase;
import com.mindquarry.desktop.model.task.Task;
import com.mindquarry.desktop.model.task.TaskList;
import com.mindquarry.desktop.preferences.pages.TaskPage;
import com.mindquarry.desktop.preferences.profile.Profile;
import com.mindquarry.desktop.util.HttpUtilities;

/**
 * @author <a href="mailto:lars(dot)trieloff(at)mindquarry(dot)com">Lars
 *         Trieloff</a>
 */
public class TaskContainerWidget extends WidgetBase {
	private static Log log = LogFactory.getLog(TaskContainerWidget.class);

	private boolean refreshing = false;
	private boolean initialized = false;

	private TaskList tasks;

	private Table table;
	private TableViewer taskTableViewer;

	private Composite noTasksWidget;
	private Composite refreshWidget;
	private Composite errorWidget;

	public TaskContainerWidget(Composite parent, MindClient client) {
		super(parent, SWT.NONE, client);
	}

	// #########################################################################
	// ### WIDGET METHODS
	// #########################################################################
	protected void createContents(Composite parent) {

	}

	// #########################################################################
	// ### PUBLIC METHODS
	// #########################################################################

	/**
	 * Runs task update in a separate thread, so that GUI can continue
	 * processing. Thus this method returns immediately. While updating tasks
	 * the Task Manager will show an update widget instead of the task table.
	 */
	public void asyncRefresh() {
		log.info("Starting async task list refresh."); //$NON-NLS-1$
		if (refreshing) {
			log.info("Already refreshing, nothing to do."); //$NON-NLS-1$
			return;
		}
		refreshing = true;
		new Thread(new Runnable() {
			public void run() {
				refresh();
			}
		}).start();
	}

	// #########################################################################
	// ### PRIVATE METHODS
	// #########################################################################
	private void refresh() {
		log.info("Starting task list refresh."); //$NON-NLS-1$
		refreshing = true;
		
		Profile profile = Profile.getSelectedProfile(client
				.getPreferenceStore());

		// check profile
		if (profile == null) {
			log.debug("No profile selected."); //$NON-NLS-1$
			refreshing = false;
			return;
		}
		updateTaskWidgetContents(true, null, false);

		TaskList taskList = null;
		try {
			log.info("Retrieving list of tasks."); //$NON-NLS-1$
			taskList = new TaskList(profile.getServerURL() + "/tasks", //$NON-NLS-1$
					profile.getLogin(), profile.getPassword());
		} catch (Exception e) {
			log.error("Could not update list of tasks for "
					+ profile.getServerURL(), e); //$NON-NLS-1$
			String errMessage = "message"; //$NON-NLS-1$
			
			// TODO fix message
//			String errMessage = Messages.getString(TaskManager.class, "5"); //$NON-NLS-1$
			errMessage += " " + e.getLocalizedMessage(); //$NON-NLS-1$

			updateTaskWidgetContents(false, errMessage, false);
			refreshing = false;
			client.showMessage(Messages.getString(
					"com.mindquarry.desktop.client", //$NON-NLS-1$
					"error"), //$NON-NLS-1$
					errMessage);
			return;
		}
		// loop and add tasks
		Iterator tIt = taskList.getTasks().iterator();
		while (tIt.hasNext()) {
			Task task = (Task) tIt.next();
			// add task to internal list of tasks, if not yet exist
			PreferenceStore store = client.getPreferenceStore();

			boolean listTask = true;
			if (!store.getBoolean(TaskPage.LIST_FINISHED_TASKS)) {
				if ((task.getStatus() != null)
						&& (task.getStatus().equals(Task.STATUS_DONE))) {
					listTask = false;
				}
			}
			if (listTask && (!tasks.getTasks().contains(task))) {
				log.info("Adding task with id '" + task.getId() + "'."); //$NON-NLS-1$//$NON-NLS-2$
				tasks.getTasks().add(task);
			}
		}
		if (tasks.getTasks().isEmpty()) {
			updateTaskWidgetContents(false, null, true);
		} else {
			updateTaskWidgetContents(false, null, false);

			// update task table
			log.info("Updating list of tasks."); //$NON-NLS-1$
			getDisplay().syncExec(new Runnable() {
				public void run() {
					taskTableViewer.setInput(tasks);

					// set background color for every second table item
					TableItem[] items = taskTableViewer.getTable().getItems();
					for (int i = 0; i < items.length; i++) {
						if (i % 2 == 1) {
							items[i].setBackground(new Color(Display
									.getCurrent(), 233, 233, 251));
						}
					}
				}
			});
		}
		refreshing = false;
		initialized = true;
	}

	private void updateTaskWidgetContents(final boolean refreshing,
			final String errMessage, final boolean empty) {
		final Composite self = this;
		getDisplay().syncExec(new Runnable() {
			public void run() {
				if (refreshing) {
					destroyContent();
					refreshWidget = new TaskUpdateWidget(self, "message" //$NON-NLS-1$
							+ " ...", client); //$NON-NLS-1$
					
					// TODO fix message
//					refreshWidget = new TaskUpdateWidget(self, Messages
//							.getString(TaskManager.class, "2") //$NON-NLS-1$
//							+ " ...", client); //$NON-NLS-1$
				} else if (errMessage == null && !empty) {
					destroyContent();
					table = new Table(self, SWT.BORDER);
					table.setHeaderVisible(false);
					table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
							true));
					((GridData) table.getLayoutData()).heightHint = ((GridData) table
							.getParent().getLayoutData()).heightHint;
					table.setLinesVisible(false);
					table.setToolTipText(""); //$NON-NLS-1$

					// create table viewer
					taskTableViewer = new TableViewer(table);
					taskTableViewer.activateCustomTooltips();

					// create columns
					TableColumn titleCol = new TableColumn(table, SWT.NONE);
					titleCol.setResizable(false);
					titleCol.setWidth(100);
					titleCol.setText("message"); //$NON-NLS-1$
					
					// TODO fix message
//					titleCol
//							.setText(Messages.getString(TaskManager.class, "3")); //$NON-NLS-1$

					TableViewerColumn vCol = new TableViewerColumn(
							taskTableViewer, titleCol);
					vCol.setLabelProvider(new TaskTableLabelProvider());
					taskTableViewer.setColumnPart(vCol, 0);

					// create task list
					CellEditor[] editors = new CellEditor[table
							.getColumnCount()];
					editors[0] = new CheckboxCellEditor(table.getParent());

					taskTableViewer.setCellEditors(editors);
					taskTableViewer.getTable().getColumn(0).setWidth(300);
					taskTableViewer
							.setContentProvider(new TaskTableContentProvider());
					taskTableViewer
							.addSelectionChangedListener(new TaskSelectionChangedListener(
									taskTableViewer, null));
					taskTableViewer
							.addDoubleClickListener(new TaskTableDoubleClickListener());
				} else if (errMessage == null && empty) {
					destroyContent();
					noTasksWidget = new NoTasksWidget(self, "message", client); //$NON-NLS-1$
					
					// TODO fix message
//					noTasksWidget = new NoTasksWidget(self, Messages.getString(
//							TaskManager.class, "4"), client); //$NON-NLS-1$
				} else {
					destroyContent();
					errorWidget = new TaskErrorWidget(self, errMessage, client);
				}
				layout(true);
			}

			private void destroyContent() {
				if (table != null) {
					table.dispose();
					table = null;
				}
				if (refreshWidget != null) {
					refreshWidget.dispose();
					refreshWidget = null;
				}
				if (errorWidget != null) {
					errorWidget.dispose();
					errorWidget = null;
				}
				if (noTasksWidget != null) {
					noTasksWidget.dispose();
					noTasksWidget = null;
				}
			}
		});
	}

	class TaskTableDoubleClickListener implements IDoubleClickListener {
		public void doubleClick(DoubleClickEvent event) {
			Profile prof = Profile.getSelectedProfile(client
					.getPreferenceStore());

			ISelection selection = event.getSelection();

			if (selection instanceof StructuredSelection) {
				StructuredSelection structsel = (StructuredSelection) selection;
				Object element = structsel.getFirstElement();

				if (element instanceof Task) {
					Task task = (Task) element;

					try {
						// use a clone of the task so cancel works:
						TaskSettingsDialog dlg = new TaskSettingsDialog(
								new Shell(Display.getDefault()), task.clone(),
								false);

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
									.getContentAsXML().asXML()
									.getBytes("utf-8"));
						}
					} catch (Exception e) {
						// TODO fix message
						
//						client.showMessage(Messages.getString(
//								TaskManager.class, "6"), //$NON-NLS-1$
//								Messages.getString(TaskManager.class, "7") //$NON-NLS-1$
//										+ ": " //$NON-NLS-1$
//										+ e.toString());
						log.error("Could not update task with id " //$NON-NLS-1$
								+ task.getId(), e);
					}
				}
				if (table != null) {
					// avoid crash if last bug got closed but the task dialog
					// was still open and then OK was pressed:
					// (TODO: task is submitted but not visible until manual
					// refresh)
					taskTableViewer.refresh();
				}
			}
		}
	}
}
