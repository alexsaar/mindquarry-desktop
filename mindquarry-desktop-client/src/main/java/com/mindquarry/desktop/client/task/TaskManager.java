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
package com.mindquarry.desktop.client.task;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.dom4j.io.DocumentSource;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.mindquarry.desktop.client.MindClient;
import com.mindquarry.desktop.client.task.dialog.TaskDialog;
import com.mindquarry.desktop.client.task.widgets.NoTasksComposite;
import com.mindquarry.desktop.client.task.widgets.TaskErrorComposite;
import com.mindquarry.desktop.client.task.widgets.TaskUpdateComposite;
import com.mindquarry.desktop.model.task.Task;
import com.mindquarry.desktop.model.task.TaskList;
import com.mindquarry.desktop.preferences.pages.TaskPage;
import com.mindquarry.desktop.preferences.profile.Profile;
import com.mindquarry.desktop.util.HttpUtilities;

/**
 * Responsible class for managing tasks.
 * 
 * @author <a href="mailto:lars(dot)trieloff(at)mindquarry(dot)com">Lars
 *         Trieloff</a>
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class TaskManager {
    private static TaskManager instance;

    public static final String TITLE_COLUMN = "title"; //$NON-NLS-1$

    private List<Task> tasks = new ArrayList<Task>();

    private List<TaskListChangeListener> listeners = new Vector<TaskListChangeListener>();

    private final MindClient client;

    private final Composite taskContainer;

    private final Button doneButton;

    private final Button refreshButton;

    private final TaskManager myself = this;

    private Composite noTasksWidget;

    private Composite refreshWidget;

    private Composite errorWidget;

    private Table table = null;

    private TableViewer taskTableViewer = null;

    private boolean refreshing = false;

    private boolean initialized = false;

    private TaskManager(final MindClient client, final Composite taskContainer,
            Button refreshButton, final Button doneButton) {
        this.client = client;
        this.taskContainer = taskContainer;
        this.doneButton = doneButton;
        this.refreshButton = refreshButton;
    }

    public static TaskManager getInstance(final MindClient client,
            final Composite taskContainer, Button refreshButton,
            final Button doneButton) {
        if (instance == null) {
            instance = new TaskManager(client, taskContainer, refreshButton,
                    doneButton);
        }
        return instance;
    }

    public void setDone(Task task) {
        task.setStatus(Task.STATUS_DONE);

        PreferenceStore store = client.getPreferenceStore();
        if (!store.getBoolean(TaskPage.LIST_FINISHED_TASKS)) {
            tasks.remove(task);
        }
        Profile selectedProfile = Profile.getSelectedProfile(client
                .getPreferenceStore());

        try {
            HttpUtilities.putAsXML(selectedProfile.getLogin(), selectedProfile
                    .getPassword(), task.getId(), task.getContentAsXML()
                    .asXML().getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // must disable doneButton explicitly, because removing tasks does
        // not fire a selection changed event
        doneButton.setEnabled(false);

        // check number of active tasks, if 0 display NoTasksWidget
        if (tasks.isEmpty()) {
            updateTaskWidgetContents(false, false, true);
        }
        taskTableViewer.refresh();
    }

    public void removeChangeListener(TaskListChangeListener provider) {
        this.listeners.remove(provider);
    }

    public void addChangeListener(TaskListChangeListener provider) {
        this.listeners.remove(provider);
    }

    public Task[] getTasks() {
        return tasks.toArray(new Task[] {});
    }

    /**
     * Runs task update in a separate thread, so that GUI can continue
     * processing. Thus this method returns immediatly. While updating tasks the
     * Task Manager will show an update widget instead of the task table.
     */
    public void cancelRefresh() {
        if (!refreshing) {
            return;
        }
        refreshing = false;
    }

    /**
     * Runs task update in a separate thread, so that GUI can continue
     * processing. Thus this method returns immediatly. While updating tasks the
     * Task Manager will show an update widget instead of the task table.
     */
    public void asyncRefresh() {
        if (refreshing) {
            return;
        }
        refreshing = true;
        new Thread(new Runnable() {
            public void run() {
                refresh();
            }
        }).start();
    }

    private void refresh() {
        refreshing = true;
        setRefreshStatus(false);
        tasks.clear();

        Profile profile = Profile.getSelectedProfile(client
                .getPreferenceStore());

        // check profile
        if (profile == null) {
            refreshing = false;
            setRefreshStatus(true);
            return;
        }
        updateTaskWidgetContents(true, false, false);

        TaskList taskList = null;
        try {
            taskList = new TaskList(profile.getServerURL() + "/tasks", //$NON-NLS-1$
                    profile.getLogin(), profile.getPassword());
        } catch (Exception e) {
            updateTaskWidgetContents(false, true, false);
            setRefreshStatus(true);
            refreshing = false;
            e.printStackTrace();
            return;
        }
        // loop and add tasks
        for (Task task : taskList.getTasks()) {
            // add task to internal list of tasks, if not yet exist
            PreferenceStore store = client.getPreferenceStore();

            boolean listTask = true;
            if (!store.getBoolean(TaskPage.LIST_FINISHED_TASKS)) {
                if (task.getStatus().equals(Task.STATUS_DONE)) {
                    listTask = false;
                }
            }
            if (listTask && (!tasks.contains(task))) {
                tasks.add(task);
            }
        }
        if (tasks.isEmpty()) {
            updateTaskWidgetContents(false, false, true);
        } else {
            updateTaskWidgetContents(false, false, false);

            // update task table
            taskContainer.getDisplay().syncExec(new Runnable() {
                public void run() {
                    taskTableViewer.setInput(myself);
                }
            });
        }
        setRefreshStatus(true);
        refreshing = false;
        initialized = true;
    }

    private void setRefreshStatus(final boolean enabled) {
        taskContainer.getDisplay().syncExec(new Runnable() {
            public void run() {
                refreshButton.setEnabled(enabled);

                if (!enabled) {
                    MindClient.getIconActionHandler().startAction(
                            Messages.getString("TaskManager.0")); //$NON-NLS-1$
                } else {
                    MindClient.getIconActionHandler().stopAction(
                            Messages.getString("TaskManager.0")); //$NON-NLS-1$
                }
            }
        });
    }

    private void updateTaskWidgetContents(final boolean refreshing,
            final boolean error, final boolean empty) {
        taskContainer.getDisplay().syncExec(new Runnable() {
            public void run() {
                if (refreshing) {
                    destroyContent();
                    refreshWidget = new TaskUpdateComposite(taskContainer,
                            Messages.getString("TaskManager.2") //$NON-NLS-1$
                                    + " ..."); //$NON-NLS-1$
                } else if (!error && !empty) {
                    destroyContent();
                    table = new Table(taskContainer, SWT.BORDER);
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
                    titleCol.setText(Messages.getString("TaskManager.3")); //$NON-NLS-1$

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
                                    taskTableViewer, doneButton));
                    taskTableViewer
                            .addDoubleClickListener(new TaskTableDoubleClickListener());
                } else if (!error && empty) {
                    destroyContent();
                    noTasksWidget = new NoTasksComposite(taskContainer,
                            Messages.getString("TaskManager.1")); //$NON-NLS-1$
                } else {
                    destroyContent();
                    errorWidget = new TaskErrorComposite(taskContainer,
                            Messages.getString("TaskManager.4")); //$NON-NLS-1$
                }
                taskContainer.layout(true);
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

    public TableViewer getTaskTableViewer() {
        return taskTableViewer;
    }

    /**
     * Indicates if the task manager is initialized or not.
     */
    public boolean isInitialized() {
        return initialized;
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
                        TaskDialog dlg = new TaskDialog(MindClient.getShell(),
                                task);
                        if (dlg.open() == Window.OK) {
                            HttpUtilities.putAsXML(prof.getLogin(), prof
                                    .getPassword(), task.getId(), task
                                    .getContentAsXML().asXML().getBytes());
                        }
                    } catch (Exception e) {
                        MindClient
                                .showErrorMessage("Could not update the task. "
                                        + e.getLocalizedMessage());
                        e.printStackTrace();
                    }
                }
                taskTableViewer.refresh();
            }
        }
    }
}
