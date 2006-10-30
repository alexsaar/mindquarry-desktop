/**
 * Copyright (C) 2005 MindQuarry GmbH, All Rights Reserved
 */
package com.mindquarry.client.tray;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.TableViewer;
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
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import com.mindquarry.client.MindClient;
import com.mindquarry.client.ballon.BalloonWindow;
import com.mindquarry.client.task.TaskDoneListener;
import com.mindquarry.client.task.TaskManager;
import com.mindquarry.client.task.TaskSelectionChangedListener;
import com.mindquarry.client.task.TaskTableCellModifier;
import com.mindquarry.client.task.TaskTableContentProvider;
import com.mindquarry.client.task.TaskTableLabelProvider;
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

    private Text wikiTextArea = null;

    private Table taskTable = null;

    private TableViewer taskTableViewer = null;

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
            balloon.setLocation(curPos);
            balloon.setAnchor(anchor);
            balloon.open();
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
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;

        GridData gd1 = new GridData();
        gd1.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
        gd1.grabExcessHorizontalSpace = true;

        GridData gd2 = new GridData();
        gd2.grabExcessVerticalSpace = true;
        gd2.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
        gd2.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
        gd2.horizontalSpan = 2;
        gd2.grabExcessHorizontalSpace = true;

        GridData gd3 = new GridData();
        gd3.horizontalAlignment = org.eclipse.swt.layout.GridData.END;
        gd3.verticalAlignment = org.eclipse.swt.layout.GridData.END;
        gd3.grabExcessHorizontalSpace = true;

        GridData gd4 = new GridData();
        gd4.grabExcessVerticalSpace = false;
        gd4.verticalAlignment = org.eclipse.swt.layout.GridData.END;
        gd4.horizontalAlignment = org.eclipse.swt.layout.GridData.END;

        Group workspacesGroup = new Group(container, SWT.SHADOW_NONE);
        workspacesGroup.setBackground(container.getBackground());
        workspacesGroup.setText("Workspaces");
        workspacesGroup.setLayout(layout);
        workspacesGroup.setLayoutData(gd1);

        Link label = new Link(workspacesGroup, SWT.NONE);
        label.setBackground(workspacesGroup.getBackground());
        label
                .setText("Share and synchronize your local documents with your team by using the buttons below.");
        label.setLayoutData(gd2);

        Button shareButton = new Button(workspacesGroup, SWT.NONE);
        shareButton.setImage(new Image(Display.getCurrent(), getClass()
                .getResourceAsStream("/icons/24x24/actions/up.png")));
        shareButton.setLayoutData(gd3);
        shareButton.setText("Share");
        shareButton
                .setToolTipText("Use this button to share your local workspace changes with the other teams members.");
        shareButton.addListener(SWT.Selection, new WorkspaceShareListener(
                client, shareButton));

        Button syncButton = new Button(workspacesGroup, SWT.PUSH);
        syncButton.setText("Synchronize");
        syncButton
                .setToolTipText("Use this button to synchronize your local workspaces.");
        syncButton.setLayoutData(gd4);
        syncButton.setImage(new Image(Display.getCurrent(), getClass()
                .getResourceAsStream("/icons/24x24/actions/down.png")));
        syncButton.addListener(SWT.Selection, new WorkspaceSynchronizeListener(
                client, syncButton));
    }

    /**
     * This method initializes tasksGroup
     */
    private void createTasksGroup() {
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;

        GridData gd1 = new GridData();
        gd1.grabExcessHorizontalSpace = true;
        gd1.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;

        GridData gd2 = new GridData();
        gd2.grabExcessHorizontalSpace = true;
        gd2.grabExcessVerticalSpace = true;
        gd2.heightHint = 100;
        gd2.horizontalSpan = 2;
        gd2.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;

        GridData gd3 = new GridData();
        gd3.grabExcessHorizontalSpace = true;
        gd3.horizontalAlignment = org.eclipse.swt.layout.GridData.END;

        Group tasksGroup = new Group(container, SWT.NONE);
        tasksGroup.setBackground(container.getBackground());
        tasksGroup.setLayoutData(gd1);
        tasksGroup.setLayout(layout);
        tasksGroup.setText("Tasks");

        Menu menu = new Menu(tasksGroup);
        MenuItem item = new MenuItem(menu, SWT.PUSH);
        item.setText("Done");
        item = new MenuItem(menu, SWT.PUSH);
        item.setText("Reject");

        TaskManager tman = new TaskManager(client);
        tman.refresh();

        taskTable = new Table(tasksGroup, SWT.BORDER);
        taskTable.setMenu(menu);
        taskTable.setHeaderVisible(false);
        taskTable.setLayoutData(gd2);
        taskTable.setLinesVisible(false);
        taskTableViewer = new TableViewer(taskTable);

        TableColumn activityColumn = new TableColumn(taskTable, SWT.NONE);
        activityColumn.setResizable(false);
        activityColumn.setWidth(100);
        activityColumn.setText("Task");

        Button doneButton = new Button(tasksGroup, SWT.NONE);
        doneButton.setEnabled(false);
        doneButton.setText("Done");
        doneButton.setToolTipText("Use this button to finish a running task.");
        doneButton.setLayoutData(gd3);
        doneButton.setImage(new Image(Display.getCurrent(), getClass()
                .getResourceAsStream("/icons/24x24/emblems/done.png")));
        doneButton.addListener(SWT.Selection, new TaskDoneListener(client,
                doneButton, taskTableViewer, tman));

        // create task list
        CellEditor[] editors = new CellEditor[taskTable.getColumnCount()];
        editors[0] = new CheckboxCellEditor(taskTable.getParent());

        taskTableViewer.setCellEditors(editors);
        taskTableViewer
                .setColumnProperties(new String[] { TaskManager.TITLE_COLUMN });
        taskTableViewer.getTable().getColumn(0).setWidth(300);

        taskTableViewer.setLabelProvider(new TaskTableLabelProvider());
        taskTableViewer.setContentProvider(new TaskTableContentProvider());
        taskTableViewer.setCellModifier(new TaskTableCellModifier(
                taskTableViewer));
        taskTableViewer
                .addSelectionChangedListener(new TaskSelectionChangedListener(
                        tman, taskTableViewer, doneButton));
        taskTableViewer.setInput(tman);
    }

    /**
     * This method initializes wikiGroup
     */
    private void createWikiGroup() {
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;

        GridData gd1 = new GridData();
        gd1.grabExcessHorizontalSpace = true;
        gd1.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
        gd1.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
        gd1.grabExcessVerticalSpace = true;

        GridData gd2 = new GridData();
        gd2.grabExcessHorizontalSpace = true;
        gd2.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
        gd2.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
        gd2.horizontalSpan = 2;
        gd2.heightHint = 130;
        gd2.grabExcessVerticalSpace = true;

        GridData gd3 = new GridData();
        gd3.horizontalAlignment = org.eclipse.swt.layout.GridData.END;
        gd3.grabExcessHorizontalSpace = true;

        GridData gd4 = new GridData();
        gd4.horizontalAlignment = org.eclipse.swt.layout.GridData.END;

        Group wikiGroup = new Group(container, SWT.NONE);
        wikiGroup.setBackground(container.getBackground());
        wikiGroup.setLayoutData(gd1);
        wikiGroup.setLayout(layout);
        wikiGroup.setText("Wiki");

        wikiTextArea = new Text(wikiGroup, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL
                | SWT.BORDER);
        wikiTextArea.setLayoutData(gd2);
        wikiTextArea.setEnabled(false);

        Button clearButton = new Button(wikiGroup, SWT.NONE);
        clearButton.setText("Clear");
        clearButton
                .setToolTipText("Use this button to clear the text in the Wiki textbox.");
        clearButton.setImage(new Image(Display.getCurrent(), getClass()
                .getResourceAsStream("/icons/24x24/actions/edit-clear.png")));
        clearButton.setEnabled(false);
        clearButton.setLayoutData(gd3);

        Button postButton = new Button(wikiGroup, SWT.NONE);
        postButton.setText("Post");
        postButton
                .setToolTipText("Use this button for posting the content of the Wiki textbox to your personal Wiki page.");
        postButton.setImage(new Image(Display.getCurrent(), getClass()
                .getResourceAsStream("/icons/24x24/actions/document-new.png")));
        postButton.setEnabled(false);
        postButton.setLayoutData(gd4);
    }
}
