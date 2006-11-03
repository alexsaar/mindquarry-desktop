/**
 * Copyright (C) 2005 MindQuarry GmbH, All Rights Reserved
 */
package com.mindquarry.client.tray;

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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;

import com.mindquarry.client.MindClient;
import com.mindquarry.client.ballon.BalloonWindow;
import com.mindquarry.client.task.TaskDoneListener;
import com.mindquarry.client.task.TaskManager;
import com.mindquarry.client.workspace.WorkspaceShareListener;
import com.mindquarry.client.workspace.WorkspaceSynchronizeListener;

/**
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class TrayIconSelectionListener implements SelectionListener {
    private static final Point BALLOON_SIZE = new Point(356, 557);

    private final Display display;

    private final MindClient client;

    private BalloonWindow balloon;

    private Composite container = null;

    private TaskManager tman;

    public TrayIconSelectionListener(Display display, final MindClient client) {
        this.display = display;
        this.client = client;
    }

    /**
     * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
     */
    public void widgetDefaultSelected(SelectionEvent e) {
    }

    /**
     * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
     */
    public void widgetSelected(SelectionEvent e) {
        if (container == null || container.isDisposed()) {
            createContainer();
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
            // TODO set location based on event data
            balloon.setLocation(curPos);
            balloon.setAnchor(anchor);
            balloon.open();
            
            // run task update in separate thread, so that GUI can continue
            // processing
            new Thread(new Runnable() {
                public void run() {
                    tman.refresh();
                }
            }).start();
        } else {
            balloon.close();
        }
    }
    

    /**
     * This method initializes sShell
     */
    public void createContainer() {
        balloon = new BalloonWindow(Display.getCurrent(), SWT.TITLE | SWT.CLOSE
                | SWT.TOOL | SWT.ON_TOP);
        balloon.setText(MindClient.APPLICATION_NAME);
        balloon.setImage(client.getIcon());

        container = balloon.getContents();
        container.setLayout(new GridLayout());

        createWorkspacesGroup();
        createTasksGroup();
        createWikiGroup();

        container.pack();
        container.setSize(BALLOON_SIZE);
    }
    

    /**
     * This method initializes workspacesGroup
     */
    private void createWorkspacesGroup() {
        Group workspacesGroup = new Group(container, SWT.SHADOW_NONE);
        workspacesGroup.setBackground(container.getBackground());
        workspacesGroup.setText("Workspaces");
        workspacesGroup.setLayout(new GridLayout(2, false));
        workspacesGroup.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true,
                false));

        Link label = new Link(workspacesGroup, SWT.NONE);
        label.setBackground(workspacesGroup.getBackground());
        label
                .setText("Share and synchronize your local documents with your team by using the buttons below.");
        label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 2));

        Button shareButton = new Button(workspacesGroup, SWT.NONE);
        shareButton.setImage(new Image(Display.getCurrent(), getClass()
                .getResourceAsStream("/icons/24x24/actions/up.png"))); //$NON-NLS-1$
        shareButton.setLayoutData(new GridData(SWT.END, SWT.END, true, false));
        shareButton.setText("Share");
        shareButton
                .setToolTipText("Use this button to share your local workspace changes with the other teams members.");
        shareButton.addListener(SWT.Selection, new WorkspaceShareListener(
                client, shareButton));

        Button syncButton = new Button(workspacesGroup, SWT.PUSH);
        syncButton.setText("Synchronize");
        syncButton
                .setToolTipText("Use this button to synchronize your local workspaces.");
        syncButton.setLayoutData(new GridData(SWT.END, SWT.END, false, false));
        syncButton.setImage(new Image(Display.getCurrent(), getClass()
                .getResourceAsStream("/icons/24x24/actions/down.png"))); //$NON-NLS-1$
        syncButton.addListener(SWT.Selection, new WorkspaceSynchronizeListener(
                client, syncButton));
    }

    /**
     * This method initializes tasksGroup
     */
    private void createTasksGroup() {
        final Group tasksGroup = new Group(container, SWT.NONE);
        tasksGroup.setBackground(container.getBackground());
        tasksGroup.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
        tasksGroup.setLayout(new GridLayout(1, false));
        tasksGroup.setText("Tasks");

        Composite taskContainer = new Composite(tasksGroup, SWT.NONE);
        taskContainer.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true,
                true, 0, 0));
        ((GridData) taskContainer.getLayoutData()).heightHint = 150;
        taskContainer.setBackground(taskContainer.getDisplay().getSystemColor(
                SWT.COLOR_WHITE));
        taskContainer.setLayout(new GridLayout(1, true));
        ((GridLayout) taskContainer.getLayout()).horizontalSpacing = 0;
        ((GridLayout) taskContainer.getLayout()).verticalSpacing = 0;
        ((GridLayout) taskContainer.getLayout()).marginHeight = 0;
        ((GridLayout) taskContainer.getLayout()).marginWidth = 0;

        Button doneButton = new Button(tasksGroup, SWT.NONE);
        doneButton.setEnabled(false);
        doneButton.setText("Done");
        doneButton.setToolTipText("Use this button to finish a running task.");
        doneButton.setLayoutData(new GridData(SWT.END, SWT.NONE, true, false));
        doneButton.setImage(new Image(Display.getCurrent(), getClass()
                .getResourceAsStream("/icons/24x24/emblems/done.png"))); //$NON-NLS-1$
                
        tman = new TaskManager(client, taskContainer, doneButton);
        doneButton.addListener(SWT.Selection, new TaskDoneListener(tman));
    }

    /**
     * This method initializes wikiGroup
     */
    private void createWikiGroup() {
        Group wikiGroup = new Group(container, SWT.NONE);
        wikiGroup.setBackground(container.getBackground());
        wikiGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        wikiGroup.setLayout(new GridLayout(2, false));
        wikiGroup.setText("Wiki");

        Text wikiTextArea = new Text(wikiGroup, SWT.MULTI | SWT.WRAP
                | SWT.V_SCROLL | SWT.BORDER);
        wikiTextArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,
                2, 2));
        ((GridData) wikiTextArea.getLayoutData()).heightHint = 130;
        wikiTextArea.setEnabled(false);

        Button clearButton = new Button(wikiGroup, SWT.NONE);
        clearButton.setText("Clear");
        clearButton
                .setToolTipText("Use this button to clear the text in the Wiki textbox.");
        clearButton.setImage(new Image(Display.getCurrent(), getClass()
                .getResourceAsStream("/icons/24x24/actions/edit-clear.png"))); //$NON-NLS-1$
        clearButton.setEnabled(false);
        clearButton.setLayoutData(new GridData(SWT.END, SWT.NONE, true, false));

        Button postButton = new Button(wikiGroup, SWT.NONE);
        postButton.setText("Post");
        postButton
                .setToolTipText("Use this button for posting the content of the Wiki textbox to your personal Wiki page.");
        postButton.setImage(new Image(Display.getCurrent(), getClass()
                .getResourceAsStream("/icons/24x24/actions/document-new.png"))); //$NON-NLS-1$
        postButton.setEnabled(false);
        postButton.setLayoutData(new GridData(SWT.END, SWT.NONE, false, false));
    }
}
