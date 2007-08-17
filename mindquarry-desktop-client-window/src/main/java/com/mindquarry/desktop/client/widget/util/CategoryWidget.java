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
package com.mindquarry.desktop.client.widget.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import com.mindquarry.desktop.client.MindClient;
import com.mindquarry.desktop.client.action.task.CreateTaskAction;
import com.mindquarry.desktop.client.action.task.SynchronizeTasksAction;
import com.mindquarry.desktop.client.widget.WidgetBase;
import com.mindquarry.desktop.client.widget.task.TaskContainerWidget;
import com.mindquarry.desktop.client.widget.workspace.WorkspaceBrowserWidget;

/**
 * Add summary documentation here.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class CategoryWidget extends WidgetBase {
	private static final String ICON_SIZE = "22x22";//$NON-NLS-1$

	private static Image tasksIcon = new Image(
			Display.getCurrent(),
			CategoryWidget.class
					.getResourceAsStream("/com/mindquarry/icons/" + ICON_SIZE + "/apps/mindquarry-tasks.png")); //$NON-NLS-1$

	private static Image docsIcon = new Image(
			Display.getCurrent(),
			CategoryWidget.class
					.getResourceAsStream("/com/mindquarry/icons/" + ICON_SIZE + "/apps/mindquarry-documents.png")); //$NON-NLS-1$

	public CategoryWidget(Composite parent, int style, MindClient client) {
		super(parent, style, client);
	}

	protected void createContents(Composite parent) {
		final CTabFolder tabFolder = new CTabFolder(parent, SWT.TOP | SWT.FLAT
				| SWT.BORDER);
		tabFolder.setSimple(false);
		tabFolder.setUnselectedImageVisible(false);
		tabFolder.setUnselectedCloseVisible(false);
		tabFolder.setMinimizeVisible(false);
		tabFolder.setMaximizeVisible(false);

		tabFolder.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// nothing to do here
			}

			public void widgetSelected(SelectionEvent e) {
				if (tabFolder.getSelection().getText().equals("Tasks")) {
					client.setTasksActive();
				} else if (tabFolder.getSelection().getText().equals("Files")) {
					client.setFilesActive();
				}
			}
		});
		CTabItem tabItem = new CTabItem(tabFolder, SWT.NULL);
		tabItem.setText("Tasks");
		tabItem.setImage(tasksIcon);
		tabFolder.setSelection(tabItem);

		// create tasks tab
		Composite taskComposite = new Composite(tabFolder, SWT.NONE);
		taskComposite.setLayout(new GridLayout(4, false));
		taskComposite.setBackground(getShell().getDisplay().getSystemColor(
				SWT.COLOR_WHITE));
		tabItem.setControl(taskComposite);

		Label label = new Label(taskComposite, SWT.LEFT);
		label.setBackground(Display.getCurrent()
				.getSystemColor(SWT.COLOR_WHITE));
		label.setText("Priority");

		CCombo priority = new CCombo(taskComposite, SWT.BORDER | SWT.READ_ONLY
				| SWT.FLAT);
		priority.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		priority.setBackground(Display.getCurrent().getSystemColor(
				SWT.COLOR_WHITE));
		priority.add("All"); //$NON-NLS-1$
		priority.add("Low"); //$NON-NLS-1$
		priority.add("Medium"); //$NON-NLS-1$
		priority.add("Important"); //$NON-NLS-1$
		priority.add("Critical"); //$NON-NLS-1$
		priority.select(0);

		label = new Label(taskComposite, SWT.LEFT);
		label.setBackground(Display.getCurrent()
				.getSystemColor(SWT.COLOR_WHITE));
		label.setText("Status");

		CCombo status = new CCombo(taskComposite, SWT.BORDER | SWT.READ_ONLY
				| SWT.FLAT);
		status.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		status.setBackground(Display.getCurrent().getSystemColor(
				SWT.COLOR_WHITE));
		status.add("All"); //$NON-NLS-1$
		status.add("New"); //$NON-NLS-1$
		status.add("Running"); //$NON-NLS-1$
		status.add("Paused"); //$NON-NLS-1$
		status.add("Done"); //$NON-NLS-1$
		status.select(0);

		final TaskContainerWidget taskContainer = new TaskContainerWidget(
				taskComposite, client);

		FacetSelectionListener listener = new FacetSelectionListener(
				taskContainer, status, priority);
		priority.addSelectionListener(listener);
		status.addSelectionListener(listener);

		// set action fields
		((SynchronizeTasksAction) client.getAction(SynchronizeTasksAction.class
				.getName())).setTaskContainer(taskContainer);
		((CreateTaskAction) client.getAction(CreateTaskAction.class.getName()))
				.setTaskContainer(taskContainer);

		tabItem = new CTabItem(tabFolder, SWT.NULL);
		tabItem.setText("Files");
		tabItem.setImage(docsIcon);

		WorkspaceBrowserWidget workspaceBrowser = new WorkspaceBrowserWidget(
				tabFolder, client);
		tabItem.setControl(workspaceBrowser);
	}

	class FacetSelectionListener extends SelectionAdapter {
		private TaskContainerWidget taskContainer;

		private CCombo status;
		private CCombo priority;

		public FacetSelectionListener(TaskContainerWidget taskContainer,
				CCombo status, CCombo priority) {
			this.taskContainer = taskContainer;
			this.status = status;
			this.priority = priority;
		}

		public void widgetSelected(SelectionEvent e) {
			String statusString = status.getItem(status.getSelectionIndex())
					.toLowerCase();
			String priorityString = priority.getItem(
					priority.getSelectionIndex()).toLowerCase();
			taskContainer.applyFacets(statusString, priorityString);
		}
	}
}
