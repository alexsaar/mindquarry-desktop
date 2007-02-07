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
package com.mindquarry.client.ballon;

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
import org.eclipse.swt.widgets.GetTrayItemLocationHackMacOSX;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TrayItem;

import com.mindquarry.client.MindClient;
import com.mindquarry.client.options.Profile;
import com.mindquarry.client.task.TaskDoneListener;
import com.mindquarry.client.task.TaskManager;
import com.mindquarry.client.task.TaskRefreshListener;
import com.mindquarry.client.util.os.OperatingSystem;
import com.mindquarry.client.workspace.WorkspaceSynchronizeListener;

/**
 * Specialized implementation of the ballon widget that contains all widgets for
 * the MindClient.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class MindClientBallonWidget extends BalloonWindow implements
        SelectionListener, Listener {
    private static final Point BALLOON_SIZE = new Point(356, 387);

    private final Display display;

    private final MindClient client;

    private Composite container = null;

    private TaskManager tman;

    private CCombo profileSelector;

    private TrayItem itemForPosition;

    public MindClientBallonWidget(Display display, final MindClient client,
            TrayItem itemForPosition) {
        super(display, SWT.TITLE | SWT.CLOSE | SWT.TOOL | SWT.ON_TOP);

        this.display = display;
        this.client = client;
        this.itemForPosition = itemForPosition;
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
        for (Profile profile : client.getProfileList().getProfiles()) {
            profileSelector.add(profile.getName());

            selected++;
            if ((client.getProfileList().selectedProfile() != null)
                    && (profile.getName().equals(client.getProfileList()
                            .selectedProfile().getName()))) {
                profileSelector.select(selected);
            }
        }
        // select first profile, if no selected profile is specified
        if ((profileSelector.getSelectionIndex() == -1)
                && (client.getProfileList().getProfiles().length > 0)) {
            client.getProfileList().select(
                    client.getProfileList().getProfiles()[0]);
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
        // on mac the everything will be below the tray icon
        if (MindClient.OS == OperatingSystem.MAC_OS_X) {
            curPos = GetTrayItemLocationHackMacOSX
                    .getAlignedLocation(this.itemForPosition);
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
        group.setText(Messages.getString("MindClientBallonWidget.9")); //$NON-NLS-1$
        group.setLayout(new GridLayout(1, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

        Label label = new Label(group, SWT.LEFT);
        label.setBackground(group.getBackground());
        label.setText(Messages.getString("MindClientBallonWidget.10") + ":"); //$NON-NLS-1$//$NON-NLS-2$

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
                        client.getProfileList().select(items[i]);
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
        group.setText(Messages.getString("MindClientBallonWidget.0")); //$NON-NLS-1$
        group.setLayout(new GridLayout(2, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

        Link label = new Link(group, SWT.NONE);
        label.setBackground(group.getBackground());
        label.setText(Messages.getString("MindClientBallonWidget.1")); //$NON-NLS-1$
        label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 2));

        Button syncButton = new Button(group, SWT.PUSH);
        syncButton.setText(Messages.getString("MindClientBallonWidget.4")); //$NON-NLS-1$
        syncButton.setToolTipText(Messages
                .getString("MindClientBallonWidget.5")); //$NON-NLS-1$
        syncButton.setLayoutData(new GridData(SWT.END, SWT.END, true, false));
        syncButton
                .setImage(new Image(
                        Display.getCurrent(),
                        getClass()
                                .getResourceAsStream(
                                        "/com/mindquarry/icons/22x22/actions/synchronize-vertical.png"))); //$NON-NLS-1$
        syncButton.addListener(SWT.Selection, WorkspaceSynchronizeListener
                .getInstance(client, syncButton));
    }

    /**
     * This method initializes tasksGroup
     */
    private void createTasksGroup() {
        final Group group = new Group(container, SWT.NONE);
        group.setBackground(container.getBackground());
        group.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
        group.setLayout(new GridLayout(2, false));
        group.setText(Messages.getString("MindClientBallonWidget.6")); //$NON-NLS-1$

        Composite taskContainer = new Composite(group, SWT.NONE);
        taskContainer.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true,
                true, 2, 0));
        ((GridData) taskContainer.getLayoutData()).heightHint = 150;
        taskContainer.setBackground(taskContainer.getDisplay().getSystemColor(
                SWT.COLOR_WHITE));
        taskContainer.setLayout(new GridLayout(1, true));
        ((GridLayout) taskContainer.getLayout()).horizontalSpacing = 0;
        ((GridLayout) taskContainer.getLayout()).verticalSpacing = 0;
        ((GridLayout) taskContainer.getLayout()).marginHeight = 0;
        ((GridLayout) taskContainer.getLayout()).marginWidth = 0;

        Button refreshButton = new Button(group, SWT.NONE);
        refreshButton.setText(Messages.getString("MindClientBallonWidget.11")); //$NON-NLS-1$
        refreshButton.setToolTipText(Messages
                .getString("MindClientBallonWidget.12")); //$NON-NLS-1$
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
        doneButton.setText(Messages.getString("MindClientBallonWidget.7")); //$NON-NLS-1$
        doneButton.setToolTipText(Messages
                .getString("MindClientBallonWidget.8")); //$NON-NLS-1$
        doneButton.setLayoutData(new GridData(SWT.END, SWT.NONE, false, false));
        doneButton.setImage(new Image(Display.getCurrent(), getClass()
                .getResourceAsStream(
                        "/com/mindquarry/icons/22x22/status/task-done.png"))); //$NON-NLS-1$

        tman = new TaskManager(client, taskContainer, refreshButton, doneButton);
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
    // group.setText(Messages.getString("MindClientBallonWidget.9"));
    // //$NON-NLS-1$
    //
    // final Text wikiTextArea = new Text(group, SWT.MULTI | SWT.WRAP
    // | SWT.V_SCROLL | SWT.BORDER);
    // wikiTextArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,
    // 2, 2));
    // ((GridData) wikiTextArea.getLayoutData()).heightHint = 130;
    // wikiTextArea.setEnabled(false);
    //
    // Button clearButton = new Button(group, SWT.NONE);
    // clearButton.setText(Messages.getString("MindClientBallonWidget.10"));
    // //$NON-NLS-1$
    // clearButton.setToolTipText(Messages
    // .getString("MindClientBallonWidget.11")); //$NON-NLS-1$
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
    // postButton.setText(Messages.getString("MindClientBallonWidget.12"));
    // //$NON-NLS-1$
    // postButton.setToolTipText(Messages
    // .getString("MindClientBallonWidget.13")); //$NON-NLS-1$
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
}
