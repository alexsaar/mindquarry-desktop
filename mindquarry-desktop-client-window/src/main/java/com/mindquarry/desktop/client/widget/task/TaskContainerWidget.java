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

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.mindquarry.desktop.client.Messages;
import com.mindquarry.desktop.client.MindClient;
import com.mindquarry.desktop.client.dialog.task.TaskSettingsDialog;
import com.mindquarry.desktop.client.widget.WidgetBase;
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
	private TableViewer tableViewer;

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
		setLayoutData(new GridData(GridData.FILL_BOTH));
		((GridData) getLayoutData()).heightHint = 150;

		setLayout(new GridLayout(1, true));
		((GridLayout) getLayout()).horizontalSpacing = 0;
		((GridLayout) getLayout()).verticalSpacing = 0;
		((GridLayout) getLayout()).marginHeight = 0;
		((GridLayout) getLayout()).marginWidth = 0;
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
		Thread updateThread = new Thread(new Runnable() {
			public void run() {
				client.startAction(Messages.getString(
						TaskContainerWidget.class, "1"));
				refresh();
				client.stopAction(Messages.getString(TaskContainerWidget.class,
						"1"));
			}
		}, "task-update");
		updateThread.setDaemon(true);
		updateThread.start();
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

		try {
			log.info("Retrieving list of tasks."); //$NON-NLS-1$
			tasks = new TaskList(profile.getServerURL() + "/tasks", //$NON-NLS-1$
					profile.getLogin(), profile.getPassword());
		} catch (Exception e) {
			log.error("Could not update list of tasks for "
					+ profile.getServerURL(), e); //$NON-NLS-1$

			String errMessage = Messages.getString(TaskContainerWidget.class,
					"5");
			//$NON-NLS-1$
			errMessage += " " + e.getLocalizedMessage(); //$NON-NLS-1$

			updateTaskWidgetContents(false, errMessage, false);
			refreshing = false;
			client.showMessage(Messages.getString(
					"com.mindquarry.desktop.client", //$NON-NLS-1$
					"error"), //$NON-NLS-1$
					errMessage);
			return;
		}
		// add task to internal list of tasks, if not yet exist
		PreferenceStore store = client.getPreferenceStore();

		// loop and add tasks
		Iterator tIt = tasks.getTasks().iterator();
		while (tIt.hasNext()) {
			Task task = (Task) tIt.next();

			boolean listTask = true;
			if (!store.getBoolean(TaskPage.LIST_FINISHED_TASKS)) {
				if ((task.getStatus() != null)
						&& (task.getStatus().equals(Task.STATUS_DONE))) {
					listTask = false;
				}
			}
			if (!listTask) {
				log.info("Removing task with id '" + task.getId() + "'."); //$NON-NLS-1$//$NON-NLS-2$
				tIt.remove();
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
					tableViewer.setInput(tasks);

					// set background color for every second table item
					TableItem[] items = tableViewer.getTable().getItems();
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
					refreshWidget = new TaskUpdateWidget(self, Messages
							.getString(TaskContainerWidget.class, "2") //$NON-NLS-1$
							+ " ..."); //$NON-NLS-1$
				} else if (errMessage == null && !empty) {
					destroyContent();
					table = new Table(self, SWT.FULL_SELECTION | SWT.SINGLE);
					table.setLayoutData(new GridData(GridData.FILL_BOTH));
					table.setHeaderVisible(false);
					table.setLinesVisible(false);
					table.setToolTipText(""); //$NON-NLS-1$
					table.setFont(JFaceResources.getFont(MindClient.TASK_TITLE_FONT_KEY));

					table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
							true));

					// create table viewer
					tableViewer = new TableViewer(table);
					tableViewer.activateCustomTooltips();

					// create columns
					TableColumn col = new TableColumn(table, SWT.NONE);
					col.setResizable(false);
					col.setWidth(200);
					col.setText(Messages.getString(
							TaskContainerWidget.class, "3"));//$NON-NLS-1$
					
					TableViewerColumn vCol = new TableViewerColumn(tableViewer,
							col);
					vCol.setLabelProvider(new TaskTableLabelProvider());
					tableViewer.setColumnPart(vCol, 0);

					// create task list
					CellEditor[] editors = new CellEditor[table
							.getColumnCount()];
					editors[0] = new CheckboxCellEditor(table.getParent());

					tableViewer.setCellEditors(editors);
					tableViewer.getTable().getColumn(0).setWidth(getSize().x);
					getShell().addListener(SWT.Resize, new Listener() {
						public void handleEvent(Event event) {
							tableViewer.getTable().getColumn(0).setWidth(
									getSize().x);
						}
					});

					tableViewer
							.setContentProvider(new TaskTableContentProvider());
					tableViewer
							.addSelectionChangedListener(new TaskSelectionChangedListener(
									tableViewer, null));
					tableViewer
							.addDoubleClickListener(new TaskTableDoubleClickListener(
									client, table, tableViewer, tasks));
				} else if (errMessage == null && empty) {
					destroyContent();
					noTasksWidget = new NoTasksWidget(self, Messages.getString(
							TaskContainerWidget.class, "4")); //$NON-NLS-1$
				} else {
					destroyContent();
					errorWidget = new TaskErrorWidget(self, errMessage);
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
}