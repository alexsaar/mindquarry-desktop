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

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableItem;

import com.mindquarry.desktop.client.Messages;
import com.mindquarry.desktop.client.MindClient;
import com.mindquarry.desktop.client.action.ActionBase;
import com.mindquarry.desktop.client.widget.util.container.ContainerWidget;
import com.mindquarry.desktop.event.Event;
import com.mindquarry.desktop.event.EventBus;
import com.mindquarry.desktop.event.EventListener;
import com.mindquarry.desktop.model.task.NewTaskFromUrlEvent;
import com.mindquarry.desktop.model.task.Task;
import com.mindquarry.desktop.model.task.TaskList;
import com.mindquarry.desktop.model.team.Team;
import com.mindquarry.desktop.preferences.profile.Profile;
import com.mindquarry.desktop.util.ExceptionUtilities;
import com.mindquarry.desktop.util.NotAuthorizedException;

/**
 * @author <a href="mailto:saar(at)mindquarry(dot)com">Alexander Saar</a>
 */
public class TaskContainerWidget extends ContainerWidget<TableViewer> implements EventListener {
    private static final String FACET_ALL = "all";

    private static final Color HIGHLIGHT_COLOR = new Color(Display.getCurrent(),
            233, 233, 251);

    private static Log log = LogFactory.getLog(TaskContainerWidget.class);

    private TaskList tasks;

    private String statusFacet = FACET_ALL;
    private String priorityFacet = FACET_ALL;
    private String searchFacet = "";

    private TaskUpdateContainerRunnable containerRunnable;

    private int taskDownloadCount = 1;
    private int tasksInCurrentTeamCount = -1;
    private String updateMessage = null;
    
    public TaskContainerWidget(Composite parent, MindClient client) {
        super(parent, SWT.BORDER, client);
        EventBus.registerListener(this);
    }

    // #########################################################################
    // ### WIDGET METHODS
    // #########################################################################
    protected void createContents(Composite parent) {
        super.createContents(parent);
        setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 7,
                1));
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

            if ((statusFacet.equals(FACET_ALL)) && (priorityFacet.equals(FACET_ALL))
                    && searchFacet.equals("")) {
                viewer.setInput(tasks);
                viewer.refresh();
            } else {
                Iterator<Task> it = content.getTasks().iterator();
                while (it.hasNext()) {
                    Task task = it.next();
                    boolean hide = false;

                    // check status facet
                    if ((!statusFacet.equals(FACET_ALL))
                            && (!task.getStatus().equals(statusFacet))) {
                        hide = true;
                    }
                    // check priority facet
                    if ((task.getPriority() == null)
                            && (!priorityFacet.equals(FACET_ALL))) {
                        hide = true;
                    } else if ((!priorityFacet.equals(FACET_ALL))
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
    public void refresh() {
        log.info("Starting task list refresh."); //$NON-NLS-1$
        enableAction(false);
        refreshing = true;

        PreferenceStore store = client.getPreferenceStore();
        Profile profile = Profile.getSelectedProfile(store);

        // check profile
        if (profile == null) {
            log.debug("No profile selected."); //$NON-NLS-1$
            enableAction(true);
            refreshing = false;
            return;
        }
        showRefreshMessage(Messages.getString("Updating task list") + " ..."); //$NON-NLS-1$ //$NON-NLS-2$
        log.info("Retrieving list of tasks."); //$NON-NLS-1$

        // cleanup current task list
        if (tasks == null) {
            tasks = new TaskList();
        }
        tasks.getTasks().clear();

        // retrieve tasks for all selected teams
        final List<Team> teams = new ArrayList<Team>();
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                teams.addAll(client.getSelectedTeams());
            }
        });
        try {
            for (Team team : teams) {
                String url = profile.getServerURL();
                String login = profile.getLogin();
                String password = profile.getPassword();

                TaskList taskList = new TaskList(url + "/tasks/"
                        + team.getId() + "/", login, password);

                // set for use in onEvent():
                tasksInCurrentTeamCount = taskList.getSize();
                taskDownloadCount = 1;
                updateMessage = Messages.getString(
                        "Updating task list for team \"{0}\" (team {1} of {2}): ",
                        team.getName(),
                        Integer.toString(teams.indexOf(team)+1),
                        Integer.toString(teams.size()));
                tasks.getTasks().addAll(taskList.getTasks());
            }
        } catch (final NotAuthorizedException e) {
            log.error("Could not update list of tasks for " //$NON-NLS-1$
                    + profile.getServerURL(), e);
            
            getDisplay().syncExec(new Runnable() {
                public void run() {
                    Boolean retry = client.handleNotAuthorizedException(e);
                    refreshing = retry;
                }
            });

            if (refreshing) {
                refresh();
            } else {
                enableAction(true);
            }
            
            return;
        } catch (final UnknownHostException e) {
            log.error("Could not update list of tasks for " //$NON-NLS-1$
                    + profile.getServerURL(), e);
            
            getDisplay().syncExec(new Runnable() {
                public void run() {
                    Boolean retry = client.handleUnknownHostException(e);
                    refreshing = retry;
                }
            });

            if (refreshing) {
                refresh();
            } else {
                enableAction(true);
            }
            
            return;
        } catch (Exception e) {
            if (ExceptionUtilities.hasCause(e, ThreadDeath.class)) {
                // task update was cancelled by user
            } else {
                log.error("Could not update list of tasks for " //$NON-NLS-1$
                        + profile.getServerURL(), e);

                final String errMessage = Messages
                        .getString("List of tasks could not be updated") //$NON-NLS-1$
                        + " " + e.getLocalizedMessage();

                getDisplay().syncExec(new Runnable() {
                    public void run() {
                        MessageDialog.openError(getShell(),
                                Messages.getString("Error"), errMessage);
                    }
                });

                showErrorMessage(errMessage);
            }
            enableAction(true);
            refreshing = false;
            return;
        }

        showEmptyMessage(tasks.getTasks().isEmpty());
        if (!tasks.getTasks().isEmpty()) {
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
        enableAction(true);
    }

    public void showRefreshMessage(String message) {
        updateContainer(true, message, false, null, null);
    }

    public void showEmptyMessage(boolean isEmpty) {
        updateContainer(false, null, isEmpty, Messages.getString(
                "Currently no tasks are active."), null); //$NON-NLS-1$
    }

    public void showMessage(String message, String icon) {
        containerRunnable = new TaskUpdateContainerRunnable(client, this, false, true, icon, message);
            getDisplay().syncExec(containerRunnable);
    }

    public void setMessage(String message) {
        if (containerRunnable != null) {
            containerRunnable.setMessage(message);
        }
    }
    
    public void setUpdateMessage(String message) {
        if (containerRunnable != null) {
            containerRunnable.setUpdateMessage(message);
        }
    }

    private void enableAction(boolean enable) {
        client.enableActions(enable, ActionBase.TASK_ACTION_GROUP);
        client.enableActions(!enable, ActionBase.STOP_ACTION_GROUP);
    }
    
    private void markColumns() {
        // set background color for every second table item
        TableItem[] items = viewer.getTable().getItems();
        for (int i = 0; i < items.length; i++) {
            if (i % 2 == 1) {
                items[i].setBackground(HIGHLIGHT_COLOR);
            }
        }
    }

    private void updateContainer(final boolean refreshing, String refreshMessage,
            final boolean empty, String emptyMessage, final String errorMessage) {
        containerRunnable = new TaskUpdateContainerRunnable(client, this,
                refreshing, refreshMessage, empty, emptyMessage, errorMessage);
        getDisplay().syncExec(containerRunnable);
    }

    public void onEvent(Event event) {
        if (event instanceof NewTaskFromUrlEvent) {
            setMessage(updateMessage + Messages.getString("Task {0} of {1}",
                    taskDownloadCount+"",
                    tasksInCurrentTeamCount+""));
            taskDownloadCount++;
        }
    }
}
