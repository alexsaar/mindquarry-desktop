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

import java.io.InputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;

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
import com.mindquarry.desktop.util.HttpUtilities;

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

    private final Display display;

    private final MindClient client;

    private Composite container = null;

    private TaskManager tman;

    private CCombo profileSelector;

    public MindClientBallonWidget(Display display, final MindClient client) {
        super(display, SWT.TITLE | SWT.CLOSE | SWT.TOOL | SWT.ON_TOP);
        if(System.getProperty("os.name").equals("Linux")) {
        	BALLOON_SIZE = new Point(356, 437);
        } else {
        	BALLOON_SIZE = new Point(356, 397);
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
        profileSelector.removeAll();

        // add profiles and select selected profile in combo box
        int selected = -1;
        for (Profile profile : Profile
                .loadProfiles(client.getPreferenceStore())) {
            profileSelector.add(profile.getName());

            selected++;
            if ((Profile.getSelectedProfile(client.getPreferenceStore()) != null)
                    && (profile.getName().equals(Profile.getSelectedProfile(
                            client.getPreferenceStore()).getName()))) {
                profileSelector.select(selected);
            }
        }
        // select first profile, if no selected profile is specified
        List<Profile> profiles = Profile.loadProfiles(client
                .getPreferenceStore());
        if ((profileSelector.getSelectionIndex() == -1)
                && (profiles.size() > 0)) {
            profileSelector.select(0);
        }
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

        createProfileGroup();
        createWorkspacesGroup();
        createTasksGroup();
        // createWikiGroup();

        container.pack();
        container.setSize(BALLOON_SIZE);
    }

    /**
     * Initializes the profile group for switching between profiles.
     */
    private void createProfileGroup() {
        Group group = new Group(container, SWT.SHADOW_NONE);
        group.setBackground(container.getBackground());
        group.setText("Profiles"); //$NON-NLS-1$
        group.setLayout(new GridLayout(1, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

        Label label = new Label(group, SWT.LEFT);
        label.setBackground(group.getBackground());
        label.setText("Select Profile" + ":");  //$NON-NLS-1$//$NON-NLS-2$

        profileSelector = new CCombo(group, SWT.BORDER | SWT.READ_ONLY);
        profileSelector.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
        profileSelector.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        profileSelector.addListener(SWT.Selection, new Listener() {
            /**
             * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
             */
            public void handleEvent(Event event) {
                // find the selected item and set the according profile as
                // selected
                String[] items = profileSelector.getItems();
                for (int i = 0; i < items.length; i++) {
                    if (items[i].equals(profileSelector.getItem(profileSelector
                            .getSelectionIndex()))) {
                        Profile.selectProfile(client.getPreferenceStore(),
                                items[i]);
                        tman.asyncRefresh();
                    }
                }
            }
        });
    }

    /**
     * This method initializes workspacesGroup
     */
    private void createWorkspacesGroup() {
        Group group = new Group(container, SWT.SHADOW_NONE);
        group.setBackground(container.getBackground());
        group.setText("Workspaces"); //$NON-NLS-1$
        group.setLayout(new GridLayout(2, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

        Link label = new Link(group, SWT.NONE);
        label.setBackground(group.getBackground());
        label.setText("Synchronize your local documents with your team by pressing the 'Synchronize' button below."); //$NON-NLS-1$
        label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 2));

        SynchronizeWidget synArea = new SynchronizeWidget(group);
        synArea.setVisible(false);

        Button syncButton = new Button(group, SWT.PUSH);
        syncButton.setText("Synchronize"); //$NON-NLS-1$
        syncButton.setToolTipText("Use this button to synchronize your local workspaces."); //$NON-NLS-1$
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
        group.setText("Tasks"); //$NON-NLS-1$

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
        createTaskButton.setText("Create Task..."); //$NON-NLS-1$
        createTaskButton.setToolTipText("Creates a new task"); //$NON-NLS-1$
        createTaskButton.setLayoutData(new GridData(SWT.LEFT, SWT.NONE, true,
                false));
        createTaskButton.setImage(new Image(Display.getCurrent(), getClass()
                .getResourceAsStream(
                        "/com/mindquarry/icons/22x22/actions/task-new.png"))); //$NON-NLS-1$
        createTaskButton.addListener(SWT.Selection, new CreateTaskListener());
        Button refreshButton = new Button(group, SWT.NONE);
        refreshButton.setText("Refresh");
        refreshButton.setToolTipText("Refresh list of tasks.");
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
        doneButton.setText("Done"); //$NON-NLS-1$
        doneButton.setToolTipText("Use this button to finish a running task.");
        doneButton.setLayoutData(new GridData(SWT.END, SWT.NONE, false, false));
        doneButton.setImage(new Image(Display.getCurrent(), getClass()
                .getResourceAsStream(
                        "/com/mindquarry/icons/22x22/status/task-done.png"))); //$NON-NLS-1$

        tman = TaskManager.getInstance(client, taskContainer, refreshButton,
                doneButton);
        refreshButton.addListener(SWT.Selection, new TaskRefreshListener(tman));
        doneButton.addListener(SWT.Selection, new TaskDoneListener(tman));
    }

    /**
     * This method initializes wikiGroup
     */
    // private void createWikiGroup() {
    // Group group = new Group(container, SWT.NONE);
    // group.setBackground(container.getBackground());
    // group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    // group.setLayout(new GridLayout(2, false));
    // group.setText("Wiki");
    //
    // final Text wikiTextArea = new Text(group, SWT.MULTI | SWT.WRAP
    // | SWT.V_SCROLL | SWT.BORDER);
    // wikiTextArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,
    // 2, 2));
    // ((GridData) wikiTextArea.getLayoutData()).heightHint = 130;
    // wikiTextArea.setEnabled(false);
    //
    // Button clearButton = new Button(group, SWT.NONE);
    // clearButton.setText("Clear");
    // clearButton.setToolTipText("Use this button to clear the text in the Wiki
    // textbox."));
    // clearButton
    // .setImage(new Image(
    // Display.getCurrent(),
    // getClass()
    // .getResourceAsStream(
    // "/org/tango-project/tango-icon-theme/22x22/actions/edit-clear.png")));
    // //$NON-NLS-1$
    // clearButton.setEnabled(false);
    // clearButton.setLayoutData(new GridData(SWT.END, SWT.NONE, true, false));
    //
    // Button postButton = new Button(group, SWT.NONE);
    // postButton.setText("Post");
    // postButton.setToolTipText("Use this button for posting the content of the Wiki textbox to your personal Wiki page.");
    // postButton
    // .setImage(new Image(
    // Display.getCurrent(),
    // getClass()
    // .getResourceAsStream(
    // "/org/tango-project/tango-icon-theme/22x22/actions/document-new.png")));
    // //$NON-NLS-1$
    // postButton.setEnabled(false);
    // postButton.setLayoutData(new GridData(SWT.END, SWT.NONE, false, false));
    // }
    public void handleEvent(Event event) {
        this.toggleBalloon();
    }

    class CreateTaskListener implements Listener {
        public void handleEvent(Event event) {
            Profile profile = Profile.getSelectedProfile(client
                    .getPreferenceStore());

            // get teamspace list
            InputStream content;
            try {
                content = HttpUtilities.getContentAsXML(profile.getLogin(),
                        profile.getPassword(), profile.getServerURL()
                                + "/teams"); //$NON-NLS-1$
            } catch (Exception e) {
                MindClient.showErrorMessage(e.getLocalizedMessage());
                return;
            }
            TeamList teamList = new TeamList(content, profile.getLogin(),
                    profile.getPassword());
            if (teamList.getTeams().size() == 0) {
                MindClient
                        .showErrorMessage("You are not a member of a team. Thus you can not create new tasks."); //$NON-NLS-1$
            }
            Calendar cal = new GregorianCalendar();
            String date = (cal.get(Calendar.MONTH) + 1) + "/" //$NON-NLS-1$
                    + cal.get(Calendar.DAY_OF_MONTH) + "/" //$NON-NLS-1$
                    + cal.get(Calendar.YEAR);

            // create initial task
            Task task = new Task();
            task.setStatus("new"); //$NON-NLS-1$
            task.setPriority("low"); //$NON-NLS-1$
            task.setTitle("new task"); //$NON-NLS-1$
            task.setSummary("summary of the task"); //$NON-NLS-1$
            task.setDescription("description of the task"); //$NON-NLS-1$
            task.setDate(date);

            TaskDialog dlg = new TaskDialog(MindClient.getShell(), task);
            if (dlg.open() == Window.OK) {
                try {

                    TeamSelectionDialog tsDlg = new TeamSelectionDialog(
                            MindClient.getShell(), teamList.getTeams());

                    if (tsDlg.open() == Window.OK) {
                        HttpUtilities.putAsXML(profile.getLogin(), profile
                                .getPassword(), profile.getServerURL()
                                + "/tasks/" //$NON-NLS-1$
                                + tsDlg.getSelectedTeam() + "/new", //$NON-NLS-1$
                                task.getContentAsXML().asXML().getBytes());
                    }
                } catch (Exception e) {
                    MindClient.showErrorMessage("Could not create the task."); //$NON-NLS-1$
                    e.printStackTrace();
                }
            }
        }
    }
}
