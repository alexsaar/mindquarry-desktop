/*
 * Copyright (C) 2006-2007 MindQuarry GmbH, All Rights Reserved
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
package com.mindquarry.client.task;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.DocumentSource;
import org.dom4j.io.SAXReader;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.mindquarry.client.MindClient;
import com.mindquarry.client.options.Profile;
import com.mindquarry.client.task.widgets.TaskErrorComposite;
import com.mindquarry.client.task.widgets.TaskUpdateComposite;
import com.mindquarry.client.task.xml.TaskListTransformer;
import com.mindquarry.client.task.xml.TaskTransformer;
import com.mindquarry.client.util.network.HttpUtil;
import com.mindquarry.client.util.widgets.MessageDialogUtil;

/**
 * @author <a href="mailto:lars(dot)trieloff(at)mindquarry(dot)com">Lars
 *         Trieloff</a>
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class TaskManager {
    public static final String TITLE_COLUMN = "title"; //$NON-NLS-1$

    private List<Task> tasks = new ArrayList<Task>();

    private List<TaskListChangeListener> listeners = new Vector<TaskListChangeListener>();

    private final MindClient client;

    private final Composite taskContainer;

    private final Button doneButton;

    private final Button refreshButton;

    private final TaskManager myself = this;

    private Composite refreshWidget;

    private Composite errorWidget;

    private Table table = null;

    private TableViewer taskTableViewer = null;

    private boolean refreshing = false;

    private boolean initialized = false;

    private static final StreamSource taskDoneXSL = new StreamSource(
            TaskManager.class.getResourceAsStream("/xslt/taskDone.xsl")); //$NON-NLS-1$

    public TaskManager(final MindClient client, final Composite taskContainer,
            Button refreshButton, final Button doneButton) {
        this.client = client;
        this.taskContainer = taskContainer;
        this.doneButton = doneButton;
        this.refreshButton = refreshButton;
    }

    public void startTask(Task t) {
        for (Task task : tasks) {
            task.setActive(false);
        }
        t.setActive(true);
    }

    public void stopTask(Task t) {
        t.setActive(false);
    }

    public void setDone(Task task) {
        tasks.remove(task);
        taskTableViewer.refresh();

        // set task content to status "done"
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        Source xmlSource = new DocumentSource(task.getContent());

        TransformerFactory transFact = TransformerFactory.newInstance();
        try {
            Transformer trans = transFact.newTransformer(taskDoneXSL);
            trans.transform(xmlSource, new StreamResult(result));

            HttpUtil.putAsXML(client.getProfileList().selectedProfile()
                    .getLogin(), client.getProfileList().selectedProfile()
                    .getPassword(), task.getId(), result.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // must disable doneButton explicitly, because removing tasks does
        // not fire a selection changed event
        doneButton.setEnabled(false);
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
        switchRefreshButtonStatus(false);

        // check profile
        Profile profile = client.getProfileList().selectedProfile();
        if (profile == null) {
            refreshing = false;
            switchRefreshButtonStatus(true);
            return;
        }
        setRefreshing(true, false);

        InputStream content = null;
        try {
            content = HttpUtil.getContentAsXML(profile.getLogin(), profile
                    .getPassword(), profile.getEndpoint() + "/tasks"); //$NON-NLS-1$
        } catch (Exception e) {
            MessageDialogUtil.displaySyncErrorMsg(Messages
                    .getString("TaskManager.0")); //$NON-NLS-1$
        }
        // check if some contant was received
        if (content == null) {
            setRefreshing(false, true);
            switchRefreshButtonStatus(true);
            refreshing = false;
            return;
        }
        SAXReader reader = new SAXReader();
        Document doc;
        try {
            doc = reader.read(content);
        } catch (DocumentException e) {
            e.printStackTrace();

            switchRefreshButtonStatus(true);
            refreshing = false;
            return;
        }
        // create and execute transformer for tasklist description
        TaskListTransformer tlTransformer = new TaskListTransformer();
        tlTransformer.execute(doc);

        // get task descriptions
        for (String taskURI : tlTransformer.getTaskURIs()) {
            content = null;
            try {
                content = HttpUtil.getContentAsXML(client.getProfileList()
                        .selectedProfile().getLogin(), client.getProfileList()
                        .selectedProfile().getPassword(), client
                        .getProfileList().selectedProfile().getEndpoint()
                        + "/tasks/" + taskURI); //$NON-NLS-1$
            } catch (Exception e) {
                MessageDialogUtil.displaySyncErrorMsg(Messages
                        .getString("TaskManager.1")); //$NON-NLS-1$
            }
            // check if some contant was received
            if (content == null) {
                switchRefreshButtonStatus(true);
                setRefreshing(false, true);
                refreshing = false;
                return;
            }
            try {
                doc = reader.read(content);
            } catch (DocumentException e) {
                e.printStackTrace();
            }
            TaskTransformer tTransformer = new TaskTransformer();
            tTransformer.execute(doc);

            // get initialized teask and set content
            Task newTask = tTransformer.getTask();
            newTask.setContent(doc);

            // add task to internal list of tasks, if not yet exist
            if ((!tasks.contains(newTask))
                    && (!newTask.getStatus().equals("done"))) { //$NON-NLS-1$
                tasks.add(newTask);
            }
        }
        // update task table
        setRefreshing(false, false);
        taskContainer.getDisplay().syncExec(new Runnable() {
            public void run() {
                taskTableViewer.setInput(myself);
            }
        });
        switchRefreshButtonStatus(true);
        refreshing = false;
        initialized = true;
    }

    /**
     * 
     */
    private void switchRefreshButtonStatus(final boolean enabled) {
        taskContainer.getDisplay().syncExec(new Runnable() {
            public void run() {
                refreshButton.setEnabled(enabled);
            }
        });
    }

    private void setRefreshing(final boolean refreshing, final boolean error) {
        taskContainer.getDisplay().syncExec(new Runnable() {
            /**
             * @see java.lang.Runnable#run()
             */
            public void run() {
                if (refreshing && !error) {
                    destroyContent();
                    refreshWidget = new TaskUpdateComposite(taskContainer,
                            Messages.getString("TaskManager.2") + //$NON-NLS-1$
                                    client.getProfileList().selectedProfile()
                                            .getName() + "..."); //$NON-NLS-1$
                } else if (!refreshing && !error) {
                    destroyContent();
                    table = new Table(taskContainer, SWT.BORDER);
                    table.setHeaderVisible(false);
                    table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                            true));
                    ((GridData) table.getLayoutData()).heightHint = ((GridData) table
                            .getParent().getLayoutData()).heightHint;
                    table.setLinesVisible(false);
                    table.setToolTipText(""); //$NON-NLS-1$

                    // add a "fake" tooltip
                    Listener labelListener = new TableItemTooltipListener(table);
                    Listener tableListener = new TableTooltipListener(table,
                            labelListener);
                    table.addListener(SWT.Dispose, tableListener);
                    table.addListener(SWT.KeyDown, tableListener);
                    table.addListener(SWT.MouseMove, tableListener);
                    table.addListener(SWT.MouseHover, tableListener);

                    // create table viewer
                    taskTableViewer = new TableViewer(table);
                    TableColumn titleColumn = new TableColumn(table, SWT.NONE);
                    titleColumn.setResizable(false);
                    titleColumn.setWidth(100);
                    titleColumn.setText(Messages.getString("TaskManager.3")); //$NON-NLS-1$

                    // create dummy columns for holding additional tooltip content
                    new TableColumn(table, SWT.NONE);
                    new TableColumn(table, SWT.NONE);

                    // create task list
                    CellEditor[] editors = new CellEditor[table
                            .getColumnCount()];
                    editors[0] = new CheckboxCellEditor(table.getParent());

                    taskTableViewer.setCellEditors(editors);
                    taskTableViewer
                            .setColumnProperties(new String[] { TaskManager.TITLE_COLUMN });
                    taskTableViewer.getTable().getColumn(0).setWidth(300);

                    taskTableViewer
                            .setLabelProvider(new TaskTableLabelProvider());
                    taskTableViewer
                            .setContentProvider(new TaskTableContentProvider());
                    taskTableViewer
                            .addSelectionChangedListener(new TaskSelectionChangedListener(
                                    myself, taskTableViewer, doneButton));
                } else {
                    destroyContent();
                    errorWidget = new TaskErrorComposite(taskContainer,
                            "List of tasks could not be updated.");
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
            }
        });
    }

    public TableViewer getTaskTableViewer() {
        return taskTableViewer;
    }

    /**
     * Getter for initialized.
     * 
     * @return the initialized
     */
    public boolean isInitialized() {
        return initialized;
    }
}
