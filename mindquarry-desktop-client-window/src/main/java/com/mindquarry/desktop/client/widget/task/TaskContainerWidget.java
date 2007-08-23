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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.mindquarry.desktop.client.Messages;
import com.mindquarry.desktop.client.MindClient;
import com.mindquarry.desktop.client.widget.WidgetBase;
import com.mindquarry.desktop.model.task.Task;
import com.mindquarry.desktop.model.task.TaskList;
import com.mindquarry.desktop.model.team.Team;
import com.mindquarry.desktop.preferences.profile.Profile;
import com.mindquarry.desktop.util.NotAuthorizedException;

/**
 * @author <a href="mailto:lars(dot)trieloff(at)mindquarry(dot)com">Lars
 *         Trieloff</a>
 */
public class TaskContainerWidget extends WidgetBase {
    private static Log log = LogFactory.getLog(TaskContainerWidget.class);

    private boolean refreshing = false;
    private boolean initialized = false;

    private TaskList tasks;

    private TableViewer viewer;

    private Composite noTasksWidget;
    private Composite refreshWidget;
    private Composite errorWidget;

    public TaskContainerWidget(Composite parent, MindClient client) {
        super(parent, SWT.BORDER, client);
    }

    // #########################################################################
    // ### WIDGET METHODS
    // #########################################################################
    protected void createContents(Composite parent) {
        setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 6,
                1));

        setLayout(new GridLayout(1, true));
        ((GridLayout) getLayout()).horizontalSpacing = 0;
        ((GridLayout) getLayout()).verticalSpacing = 0;
        ((GridLayout) getLayout()).marginHeight = 0;
        ((GridLayout) getLayout()).marginWidth = 0;
    }

    // #########################################################################
    // ### PUBLIC METHODS
    // #########################################################################
    public void addTask(Task task) {
        if (viewer != null) {
            TaskList content = (TaskList) viewer.getInput();
            content.getTasks().add(task);
            viewer.setInput(content);
            viewer.refresh();
        }
    }

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
                client.startAction(Messages.getString("Synchronizing tasks"));
                refresh();
                client.stopAction(Messages.getString("Synchronizing tasks"));
            }
        }, "task-update");
        updateThread.setDaemon(true);
        updateThread.start();
    }

    private String statusFacet = "all";
    private String priorityFacet = "all";
    private String searchFacet = "";

    public void applyFacets(String status, String priority, String search) {
        statusFacet = status;
        priorityFacet = priority;
        searchFacet = search;
        applyFacets();
    }

    private void applyFacets() {
        if ((viewer != null) && (tasks != null)) {
            TaskList content = new TaskList();
            content.getTasks().addAll(tasks.getTasks());

            if ((statusFacet.equals("all")) && (priorityFacet.equals("all"))
                    && searchFacet.equals("")) {
                viewer.setInput(tasks);
                viewer.refresh();
            } else {
                Iterator<Task> it = content.getTasks().iterator();
                while (it.hasNext()) {
                    Task task = it.next();
                    boolean hide = false;

                    // check status facet
                    if ((!statusFacet.equals("all"))
                            && (!task.getStatus().equals(statusFacet))) {
                        hide = true;
                    }
                    // check priority facet
                    if ((task.getPriority() == null)
                            && (!priorityFacet.equals("all"))) {
                        hide = true;
                    } else if ((!priorityFacet.equals("all"))
                            && (!task.getPriority().equals(priorityFacet))) {
                        hide = true;
                    }
                    if (hide) {
                        it.remove();
                    }
                }
            }
            // check live search constraint
            Iterator<Task> it = content.getTasks().iterator();
            while (it.hasNext()) {
                Task task = it.next();
                if ((!searchFacet.equals(""))
                        && (!task.getTitle().toLowerCase()
                                .contains(searchFacet))) {
                    it.remove();
                }
            }
            // update table
            viewer.setInput(content);
            viewer.refresh();
            markColumns();
        }
    }

    // #########################################################################
    // ### PRIVATE METHODS
    // #########################################################################
    private void refresh() {
        log.info("Starting task list refresh."); //$NON-NLS-1$
        refreshing = true;

        PreferenceStore store = client.getPreferenceStore();
        Profile profile = Profile.getSelectedProfile(store);

        // check profile
        if (profile == null) {
            log.debug("No profile selected."); //$NON-NLS-1$
            refreshing = false;
            return;
        }
        updateContainer(true, null, false);

        log.info("Retrieving list of tasks."); //$NON-NLS-1$

        // cleanup current task list
        if (tasks == null) {
            tasks = new TaskList();
        }
        tasks.getTasks().clear();

        // retrieve tasks for all selected teams
        final List items = new ArrayList();
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                items.addAll(client.getSelectedTeams());
            }
        });
        for (Object item : items) {
            Team team = (Team) item;
            try {
                tasks.getTasks().addAll(
                        new TaskList(profile.getServerURL() + "/tasks/" //$NON-NLS-1$
                                + team.getId() + "/", profile.getLogin(),
                                profile.getPassword()).getTasks());
            } catch (final NotAuthorizedException e) {
                log.error("Could not update list of tasks for "
                        + profile.getServerURL(), e); //$NON-NLS-1$

                getDisplay().syncExec(new Runnable() {
                    public void run() {
                        MessageDialog.openError(getShell(), Messages
                                .getString("Error"), //$NON-NLS-1$
                                e.getLocalizedMessage());
                    }
                });
                updateContainer(false, e.getLocalizedMessage(), false);
                refreshing = false;
                return;
            } catch (Exception e) {
                log.error("Could not update list of tasks for "
                        + profile.getServerURL(), e); //$NON-NLS-1$

                String errMessage = Messages
                        .getString("List of tasks could not be updated"); //$NON-NLS-1$
                errMessage += " " + e.getLocalizedMessage(); //$NON-NLS-1$

                MessageDialog.openError(getShell(),
                        Messages.getString("Error"), errMessage);

                updateContainer(false, errMessage, false);
                refreshing = false;
                return;
            }
        }
        if (tasks.getTasks().isEmpty()) {
            updateContainer(false, null, true);
        } else {
            updateContainer(false, null, false);

            // update task table
            log.info("Updating list of tasks."); //$NON-NLS-1$
            getDisplay().syncExec(new Runnable() {
                public void run() {
                    // viewer.setInput(tasks);
                    applyFacets();
                }
            });
        }
        refreshing = false;
        initialized = true;
    }

    private void markColumns() {
        // set background color for every second table item
        TableItem[] items = viewer.getTable().getItems();
        for (int i = 0; i < items.length; i++) {
            if (i % 2 == 1) {
                items[i].setBackground(new Color(Display.getCurrent(), 233,
                        233, 251));
            }
        }
    }

    private void updateContainer(final boolean refreshing,
            final String errMessage, final boolean empty) {
        final Composite self = this;
        getDisplay().syncExec(new Runnable() {
            public void run() {
                if (refreshing) {
                    destroyContent();
                    refreshWidget = new TaskUpdateWidget(self, Messages
                            .getString("Updating task list") //$NON-NLS-1$
                            + " ..."); //$NON-NLS-1$
                } else if (errMessage == null && !empty) {
                    destroyContent();

                    // create table viewer
                    viewer = new TableViewer(self, SWT.FULL_SELECTION);
                    viewer.activateCustomTooltips();
                    viewer.getTable().setLayoutData(
                            new GridData(GridData.FILL_BOTH));
                    viewer.getTable().setHeaderVisible(false);
                    viewer.getTable().setLinesVisible(false);
                    viewer.getTable().setToolTipText(""); //$NON-NLS-1$
                    viewer.getTable().setFont(
                            JFaceResources
                                    .getFont(MindClient.TASK_TITLE_FONT_KEY));
                    viewer.getTable().setLayoutData(
                            new GridData(SWT.FILL, SWT.FILL, true, true));
                    getShell().addListener(SWT.Resize, new Listener() {
                        public void handleEvent(Event event) {
                            viewer.getTable().getColumn(0).setWidth(
                                    viewer.getTable().getSize().x);
                        }
                    });
                    viewer.setContentProvider(new TaskTableContentProvider());
                    viewer.addSelectionChangedListener(new SelectionChanged(
                            viewer, null));
                    viewer.addDoubleClickListener(new DoubleClickListener(
                            client, viewer, tasks));

                    // create columns
                    TableViewerColumn col = new TableViewerColumn(viewer,
                            SWT.NONE);
                    col.setLabelProvider(new TaskTableLabelProvider());
                    col.getColumn().setResizable(false);
                    col.getColumn().setWidth(200);
                    col.getColumn().setText(Messages.getString("Description"));//$NON-NLS-1$
                    viewer.getTable().getColumn(0).setWidth(getSize().x);
                } else if (errMessage == null && empty) {
                    destroyContent();
                    noTasksWidget = new NoTasksWidget(self, Messages
                            .getString("Currently no tasks are active.")); //$NON-NLS-1$
                } else {
                    destroyContent();
                    errorWidget = new TaskErrorWidget(self, errMessage);
                }
                layout(true);
            }

            private void destroyContent() {
                if (viewer != null) {
                    viewer.getTable().dispose();
                    viewer = null;
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
