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
package com.mindquarry.desktop.client.ballon;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;

import com.mindquarry.desktop.client.Messages;
import com.mindquarry.desktop.client.MindClient;
import com.mindquarry.desktop.client.task.TaskDoneListener;
import com.mindquarry.desktop.client.task.TaskManager;
import com.mindquarry.desktop.client.task.TaskRefreshListener;
import com.mindquarry.desktop.client.task.dialog.TaskDialog;
import com.mindquarry.desktop.client.team.dialog.TeamSelectionDialog;
import com.mindquarry.desktop.client.workspace.WorkspaceSynchronizeListener;
import com.mindquarry.desktop.client.workspace.widgets.SynchronizeWidget;
import com.mindquarry.desktop.model.task.Task;
import com.mindquarry.desktop.model.team.TeamList;
import com.mindquarry.desktop.preferences.profile.Profile;

/**
 * Specialized implementation of the ballon widget that contains all widgets for
 * the MindClient.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class MindClientBallonWidget extends BalloonWindow implements
        SelectionListener, Listener {
    private static Point BALLOON_SIZE;

    private Log log;

    private final Display display;

    private final MindClient client;

    private Composite container = null;

    private TaskManager tman;

    public MindClientBallonWidget(Display display, final MindClient client) {
        super(display, SWT.TITLE | SWT.CLOSE | SWT.TOOL | SWT.ON_TOP);
        log = LogFactory.getLog(MindClientBallonWidget.class);

        if (System.getProperty("os.name").equals("Linux")) { //$NON-NLS-1$ //$NON-NLS-2$
            BALLOON_SIZE = new Point(356, 340);
        } else {
            BALLOON_SIZE = new Point(356, 330);
        }

        this.display = display;
        this.client = client;
        createContainer();
    }

    /**
     * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
     */
    public void widgetDefaultSelected(SelectionEvent e) {
        // nothing to do here
    }

    /**
     * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
     */
    public void widgetSelected(SelectionEvent e) {
        toggleBalloon();
    }

    public void toggleBalloon() {
        MindClient.getShell().getDisplay().syncExec(new Runnable() {
            public void run() {
                if (isVisible()) {
                    hide();
                } else {
                    initBalloonPosition();
                    show();

                    if (!tman.isInitialized()) {
                        tman.asyncRefresh();
                    }
                    updateProfileSelector();
                }
            }
        });
    }

    private void updateProfileSelector() {
        client.updateProfileSelector();
    }

    public void asyncRefreshTasks() {
        tman.asyncRefresh();
    }

    private void initBalloonPosition() {
        Rectangle diSize = display.getBounds();
        Point curPos = display.getCursorLocation();

        Point position = new Point(0, 0);
        int anchor = 0;
        if (diSize.height / 2 > curPos.y) {
            position.y = curPos.y;
            anchor |= SWT.TOP;
        } else {
            position.y = curPos.y;
            anchor |= SWT.BOTTOM;
        }
        if (diSize.width / 2 > curPos.x) {
            anchor |= SWT.LEFT;
        } else {
            anchor |= SWT.RIGHT;
        }
        setLocation(curPos);
        setAnchor(anchor);
    }

    /**
     * This method initializes sShell
     */
    public void createContainer() {
        setText(MindClient.APPLICATION_NAME);
        setImage(client.getIcon());

        container = getContents();
        container.setLayout(new GridLayout());

        createWorkspacesGroup();
        createTasksGroup();
        // createWikiGroup();

        container.pack();
        container.setSize(BALLOON_SIZE);
    }

    /**
     * This method initializes workspacesGroup
     */
    private void createWorkspacesGroup() {
        Group group = new Group(container, SWT.SHADOW_NONE);
        group.setBackground(container.getBackground());
        group.setText(Messages.getString(MindClientBallonWidget.class, "0")); //$NON-NLS-1$
        group.setLayout(new GridLayout(2, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

        Link label = new Link(group, SWT.NONE);
        label.setBackground(group.getBackground());
        label.setText(Messages.getString(MindClientBallonWidget.class, "1")); //$NON-NLS-1$
        label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 2));

        SynchronizeWidget synArea = new SynchronizeWidget(group);
        synArea.setVisible(false);

        Button syncButton = new Button(group, SWT.PUSH);
        syncButton.setText(Messages
                .getString(MindClientBallonWidget.class, "2")); //$NON-NLS-1$
        syncButton.setToolTipText(Messages.getString(
                MindClientBallonWidget.class, "3")); //$NON-NLS-1$
        syncButton
                .setImage(new Image(
                        Display.getCurrent(),
                        getClass()
                                .getResourceAsStream(
                                        "/com/mindquarry/icons/22x22/actions/synchronize-vertical.png"))); //$NON-NLS-1$
        syncButton.addListener(SWT.Selection, WorkspaceSynchronizeListener
                .getInstance(client, syncButton, synArea));
    }

    /**
     * This method initializes tasksGroup
     */
    private void createTasksGroup() {
        final Group group = new Group(container, SWT.NONE);
        group.setBackground(container.getBackground());
        group.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
        group.setLayout(new GridLayout(3, false));
        group.setText(Messages.getString(MindClientBallonWidget.class, "4")); //$NON-NLS-1$

        Composite taskContainer = new Composite(group, SWT.NONE);
        taskContainer.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true,
                true, 3, 0));
        ((GridData) taskContainer.getLayoutData()).heightHint = 150;
        taskContainer.setBackground(taskContainer.getDisplay().getSystemColor(
                SWT.COLOR_WHITE));
        taskContainer.setLayout(new GridLayout(1, true));
        ((GridLayout) taskContainer.getLayout()).horizontalSpacing = 0;
        ((GridLayout) taskContainer.getLayout()).verticalSpacing = 0;
        ((GridLayout) taskContainer.getLayout()).marginHeight = 0;
        ((GridLayout) taskContainer.getLayout()).marginWidth = 0;

        Button createTaskButton = new Button(group, SWT.NONE);
        createTaskButton.setText(Messages.getString(
                MindClientBallonWidget.class, "5") //$NON-NLS-1$
                + "..."); //$NON-NLS-1$
        createTaskButton.setToolTipText(Messages.getString(
                MindClientBallonWidget.class, "6")); //$NON-NLS-1$
        createTaskButton.setLayoutData(new GridData(SWT.LEFT, SWT.NONE, true,
                false));
        createTaskButton.setImage(new Image(Display.getCurrent(), getClass()
                .getResourceAsStream(
                        "/com/mindquarry/icons/22x22/actions/task-new.png"))); //$NON-NLS-1$
        createTaskButton.addListener(SWT.Selection, new CreateTaskListener());
        Button refreshButton = new Button(group, SWT.NONE);
        refreshButton.setText(Messages.getString(MindClientBallonWidget.class,
                "7")); //$NON-NLS-1$
        refreshButton.setToolTipText(Messages.getString(
                MindClientBallonWidget.class, "8")); //$NON-NLS-1$
        refreshButton
                .setLayoutData(new GridData(SWT.END, SWT.NONE, true, false));
        refreshButton
                .setImage(new Image(
                        Display.getCurrent(),
                        getClass()
                                .getResourceAsStream(
                                        "/org/tango-project/tango-icon-theme/22x22/actions/view-refresh.png"))); //$NON-NLS-1$

        Button doneButton = new Button(group, SWT.NONE);
        doneButton.setEnabled(false);
        doneButton.setText(Messages
                .getString(MindClientBallonWidget.class, "9")); //$NON-NLS-1$
        doneButton.setToolTipText(Messages.getString(
                MindClientBallonWidget.class, "10")); //$NON-NLS-1$
        doneButton.setLayoutData(new GridData(SWT.END, SWT.NONE, false, false));
        doneButton.setImage(new Image(Display.getCurrent(), getClass()
                .getResourceAsStream(
                        "/com/mindquarry/icons/22x22/status/task-done.png"))); //$NON-NLS-1$

        tman = TaskManager.getInstance(client, taskContainer, refreshButton,
                createTaskButton, doneButton);
        refreshButton.addListener(SWT.Selection, new TaskRefreshListener(tman));
        doneButton.addListener(SWT.Selection, new TaskDoneListener(tman));
    }

    public void handleEvent(Event event) {
        this.toggleBalloon();
    }

    private void log(String message, Exception e) {
        log.error(message, e);
    }

    class CreateTaskListener implements Listener {
        public void handleEvent(Event event) {
            Profile profile = Profile.getSelectedProfile(client
                    .getPreferenceStore());

            // get teamspace list
            TeamList teamList;
            try {
                teamList = new TeamList(profile.getServerURL() + "/teams", //$NON-NLS-1$
                        profile.getLogin(), profile.getPassword());
            } catch (Exception e) {
                client.showMessage(Messages.getString(
                        "com.mindquarry.desktop.client", //$NON-NLS-1$
                        "error"), //$NON-NLS-1$
                        Messages.getString(MindClientBallonWidget.class, "11")); //$NON-NLS-1$
                log("Error while updating team list.", e); //$NON-NLS-1$
                return;
            }
            if (teamList.getTeams().size() == 0) {
                client.showMessage(Messages.getString(
                        MindClientBallonWidget.class, "12"), //$NON-NLS-1$
                        Messages.getString(MindClientBallonWidget.class, "13")); //$NON-NLS-1$
            }
            Calendar cal = new GregorianCalendar(); // current date
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
            String date = sdf.format(cal.getTime());

            // create initial task
            Task task = new Task();
            task.setStatus("new"); //$NON-NLS-1$
            task.setPriority("low"); //$NON-NLS-1$
            task.setTitle(Messages
                    .getString(MindClientBallonWidget.class, "14")); //$NON-NLS-1$
            task.setSummary(Messages.getString(MindClientBallonWidget.class,
                    "15")); //$NON-NLS-1$
            task.setDate(date);

            TaskDialog dlg = new TaskDialog(MindClient.getShell(), task);
            if (dlg.open() == Window.OK) {
                try {
                    TeamSelectionDialog tsDlg = new TeamSelectionDialog(
                            MindClient.getShell(), teamList.getTeams());
                    if (tsDlg.open() == Window.OK) {
                        tman.add(task, tsDlg.getSelectedTeam());
                    }
                } catch (Exception e) {
                    client.showMessage(Messages.getString(
                            MindClientBallonWidget.class, "16"), //$NON-NLS-1$
                            Messages.getString(MindClientBallonWidget.class,
                                    "17")); //$NON-NLS-1$
                    log.error(Messages.getString(MindClientBallonWidget.class,
                            "17"), e); //$NON-NLS-1$
                }
            }
        }
    }
}
