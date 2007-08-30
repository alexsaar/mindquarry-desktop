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
package com.mindquarry.desktop.client.widget.app;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.mindquarry.desktop.client.Messages;
import com.mindquarry.desktop.client.MindClient;
import com.mindquarry.desktop.client.action.task.CreateTaskAction;
import com.mindquarry.desktop.client.action.task.SynchronizeTasksAction;
import com.mindquarry.desktop.client.action.workspace.SynchronizeWorkspacesAction;
import com.mindquarry.desktop.client.action.workspace.UpdateWorkspacesAction;
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

    // add spaces to make the tab a bit wider for better usability
    // (TODO: find a cleaner way):
    private static final String SPACE_HOLDER = "      ";
    private static final String TAB_TASKS_TEXT = Messages.getString("Tasks")
            + SPACE_HOLDER;
    private static final String TAB_FILES_TEXT = Messages.getString("File Changes")
            + SPACE_HOLDER;

    private static Image tasksIcon = new Image(
            Display.getCurrent(),
            CategoryWidget.class
                    .getResourceAsStream("/com/mindquarry/icons/" + ICON_SIZE + "/apps/mindquarry-tasks.png")); //$NON-NLS-1$

    private static Image docsIcon = new Image(
            Display.getCurrent(),
            CategoryWidget.class
                    .getResourceAsStream("/com/mindquarry/icons/" + ICON_SIZE + "/apps/mindquarry-documents.png")); //$NON-NLS-1$

    private TaskContainerWidget taskContainer;

    private WorkspaceBrowserWidget workspaceBrowser;

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
                if (tabFolder.getSelection().getText().equals(TAB_TASKS_TEXT)) {
                    client.setTasksActive();
                } else if (tabFolder.getSelection().getText().equals(
                        TAB_FILES_TEXT)) {
                    client.setFilesActive();
                }
            }
        });
        createWorkspaceCategory(tabFolder);
        createTasksCategory(tabFolder);
    }

    private void createTasksCategory(final CTabFolder tabFolder) {
        CTabItem tabItem = new CTabItem(tabFolder, SWT.NULL);
        tabItem.setText(TAB_TASKS_TEXT);
        tabItem.setImage(tasksIcon);
        tabItem.setFont(JFaceResources.getFont(MindClient.TEAM_NAME_FONT_KEY));

        Composite taskComposite = new Composite(tabFolder, SWT.NONE);
        taskComposite.setLayout(new GridLayout(7, false));
        taskComposite.setBackground(getShell().getDisplay().getSystemColor(
                SWT.COLOR_WHITE));
        tabItem.setControl(taskComposite);

        Label label = new Label(taskComposite, SWT.LEFT);
        label.setBackground(Display.getCurrent()
                .getSystemColor(SWT.COLOR_WHITE));
        label.setText(Messages.getString("Priority"));
        label.setFont(JFaceResources.getFont(MindClient.TEAM_NAME_FONT_KEY));

        CCombo priority = new CCombo(taskComposite, SWT.BORDER | SWT.READ_ONLY
                | SWT.FLAT);
        priority.setBackground(Display.getCurrent().getSystemColor(
                SWT.COLOR_WHITE));
        priority.add("All"); //$NON-NLS-1$
        priority.add("Low"); //$NON-NLS-1$
        priority.add("Medium"); //$NON-NLS-1$
        priority.add("Important"); //$NON-NLS-1$
        priority.add("Critical"); //$NON-NLS-1$
        priority.select(0);
        priority.setFont(JFaceResources.getFont(MindClient.TEAM_NAME_FONT_KEY));

        label = new Label(taskComposite, SWT.LEFT);
        label.setBackground(Display.getCurrent()
                .getSystemColor(SWT.COLOR_WHITE));
        label.setFont(JFaceResources.getFont(MindClient.TEAM_NAME_FONT_KEY));
        label.setText(Messages.getString("Status"));

        CCombo status = new CCombo(taskComposite, SWT.BORDER | SWT.READ_ONLY
                | SWT.FLAT);
        status.setBackground(Display.getCurrent().getSystemColor(
                SWT.COLOR_WHITE));
        status.add("All"); //$NON-NLS-1$
        status.add("New"); //$NON-NLS-1$
        status.add("Running"); //$NON-NLS-1$
        status.add("Paused"); //$NON-NLS-1$
        status.add("Done"); //$NON-NLS-1$
        status.select(0);
        status.setFont(JFaceResources.getFont(MindClient.TEAM_NAME_FONT_KEY));

        label = new Label(taskComposite, SWT.LEFT);
        label.setBackground(Display.getCurrent()
                .getSystemColor(SWT.COLOR_WHITE));
        label.setText(Messages.getString("Search"));
        label.setFont(JFaceResources.getFont(MindClient.TEAM_NAME_FONT_KEY));

        Text search = new Text(taskComposite, SWT.SINGLE | SWT.LEFT
                | SWT.BORDER);
        search.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        search.setFont(JFaceResources.getFont(MindClient.TEAM_NAME_FONT_KEY));

        Button reset = new Button(taskComposite, SWT.CENTER | SWT.PUSH);
        reset.setText(Messages.getString("Reset"));
        reset.setFont(JFaceResources.getFont(MindClient.TEAM_NAME_FONT_KEY));

        taskContainer = new TaskContainerWidget(taskComposite, client);
        FacetSelectionListener listener = new FacetSelectionListener(
                taskContainer, status, priority, search);
        priority.addSelectionListener(listener);
        status.addSelectionListener(listener);
        search.addModifyListener(listener);
        reset.addSelectionListener(listener);

        ((SynchronizeTasksAction) client.getAction(SynchronizeTasksAction.class
                .getName())).setTaskContainer(taskContainer);
        ((CreateTaskAction) client.getAction(CreateTaskAction.class.getName()))
                .setTaskContainer(taskContainer);
    }

    private void createWorkspaceCategory(final CTabFolder tabFolder) {
        CTabItem tabItem = new CTabItem(tabFolder, SWT.NULL);
        tabItem.setText(TAB_FILES_TEXT);
        tabItem.setImage(docsIcon);
        tabItem.setFont(JFaceResources.getFont(MindClient.TEAM_NAME_FONT_KEY));
        tabFolder.setSelection(tabItem);

        workspaceBrowser = new WorkspaceBrowserWidget(tabFolder, client);
        tabItem.setControl(workspaceBrowser);

        ((SynchronizeWorkspacesAction) client
                .getAction(SynchronizeWorkspacesAction.class.getName()))
                .setWorkspaceWidget(workspaceBrowser);
        ((UpdateWorkspacesAction) client.getAction(UpdateWorkspacesAction.class
                .getName())).setWorkspaceWidget(workspaceBrowser);
    }

    class FacetSelectionListener extends SelectionAdapter implements
            ModifyListener {
        private TaskContainerWidget taskContainer;

        private CCombo status;
        private CCombo priority;

        private Text search;

        public FacetSelectionListener(TaskContainerWidget taskContainer,
                CCombo status, CCombo priority, Text search) {
            this.taskContainer = taskContainer;
            this.status = status;
            this.priority = priority;
            this.search = search;
        }

        public void widgetSelected(SelectionEvent e) {
            if (e.widget instanceof Button) {
                status.select(0);
                priority.select(0);
                search.setText("");
            }
            applyFacets();
        }

        public void modifyText(ModifyEvent e) {
            applyFacets();
        }

        private void applyFacets() {
            String statusString = status.getItem(status.getSelectionIndex())
                    .toLowerCase();
            String priorityString = priority.getItem(
                    priority.getSelectionIndex()).toLowerCase();
            String searchString = search.getText().toLowerCase();
            taskContainer.applyFacets(statusString, priorityString,
                    searchString);
        }
    }

    public TaskContainerWidget getTaskContainer() {
        return taskContainer;
    }

    public WorkspaceBrowserWidget getWorkspaceBrowser() {
        return workspaceBrowser;
    }
}
