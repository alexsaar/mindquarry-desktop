/**
 * Copyright (C) 2005 MindQuarry GmbH, All Rights Reserved
 */
package com.mindquarry.client.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.mindquarry.client.MindClient;
import com.mindquarry.client.util.widgets.UpdateComposite;

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

    private final TaskManager myself = this;

    private Composite refreshWidget;

    private Table taskTable = null;

    private TableViewer taskTableViewer = null;

    public TaskManager(final MindClient client, final Composite taskContainer,
            final Button doneButton) {
        this.client = client;
        this.taskContainer = taskContainer;
        this.doneButton = doneButton;
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

    public void refresh() {
        setRefreshing(true);
        tasks.add(new Task("1", "test", "test", "test"));
        tasks.add(new Task("1", "test2", "test2", "test2"));
        tasks.add(new Task("1", "test3", "test3", "test3"));

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // String content = null;
        // try {
        // content = HttpUtil.getContentAsXML(client.getOptions().getProperty(
        // MindClient.LOGIN_KEY), client.getOptions().getProperty(
        // MindClient.PASSWORD_KEY), client.getOptions().getProperty(
        // MindClient.ENDPOINT_KEY)
        // + "/tasks"); //$NON-NLS-1$
        // } catch (Exception e) {
        // MessageDialogUtil
        // .displaySyncErrorMsg("Could not retrieve list of tasks due to
        // unexpected connection errors.");
        // }
        // // check if some contant was received
        // if (content == null) {
        // return;
        // }
        // SAXReader reader = new SAXReader();
        // Document doc;
        // try {
        // doc = reader.read(new StringReader(content));
        // } catch (DocumentException e) {
        // e.printStackTrace();
        // return;
        // }
        // // create a transformer
        // TaskListTransformer t = new TaskListTransformer(tasks);
        //
        // // perform the transform
        // t.execute(doc);

        // update task table
        setRefreshing(false);
        taskContainer.getDisplay().syncExec(new Runnable() {
            /**
             * @see java.lang.Runnable#run()
             */
            public void run() {
                taskTableViewer.setInput(myself);
            }
        });
    }

    private void setRefreshing(final boolean refreshing) {
        taskContainer.getDisplay().syncExec(new Runnable() {
            /**
             * @see java.lang.Runnable#run()
             */
            public void run() {
                if (refreshing) {
                    if (taskTable != null) {
                        taskTable.dispose();
                        taskTable = null;
                    }
                    refreshWidget = new UpdateComposite(taskContainer,
                            "Updating task list...");
                } else {
                    if (refreshWidget != null) {
                        refreshWidget.dispose();
                        refreshWidget = null;
                    }
                    taskTable = new Table(taskContainer, SWT.BORDER);
                    taskTable.setHeaderVisible(false);
                    taskTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
                            true, true));
                    ((GridData) taskTable.getLayoutData()).heightHint = 150;
                    taskTable.setLinesVisible(false);
                    taskTableViewer = new TableViewer(taskTable);

                    TableColumn activityColumn = new TableColumn(taskTable,
                            SWT.NONE);
                    activityColumn.setResizable(false);
                    activityColumn.setWidth(100);
                    activityColumn.setText("Task");

                    // create task list
                    CellEditor[] editors = new CellEditor[taskTable
                            .getColumnCount()];
                    editors[0] = new CheckboxCellEditor(taskTable.getParent());

                    taskTableViewer.setCellEditors(editors);
                    taskTableViewer
                            .setColumnProperties(new String[] { TaskManager.TITLE_COLUMN });
                    taskTableViewer.getTable().getColumn(0).setWidth(300);

                    taskTableViewer
                            .setLabelProvider(new TaskTableLabelProvider());
                    taskTableViewer
                            .setContentProvider(new TaskTableContentProvider());
                    taskTableViewer.setCellModifier(new TaskTableCellModifier(
                            taskTableViewer));
                    taskTableViewer
                            .addSelectionChangedListener(new TaskSelectionChangedListener(
                                    myself, taskTableViewer, doneButton));
                }
                taskContainer.layout(true);
            }
        });
    }

    public TableViewer getTaskTableViewer() {
        return taskTableViewer;
    }
}
