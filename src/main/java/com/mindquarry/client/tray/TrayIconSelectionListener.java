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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import com.mindquarry.client.MindClient;
import com.mindquarry.client.ballon.BalloonWindow;
import com.mindquarry.client.task.Task;
import com.mindquarry.client.task.TaskManager;
import com.mindquarry.client.task.TaskSelectionChangedListener;
import com.mindquarry.client.task.TaskTableCellModifier;
import com.mindquarry.client.task.TaskTableContentProvider;
import com.mindquarry.client.task.TaskTableLabelProvider;
import com.mindquarry.client.workspace.WorkspaceSynchronizeListener;

/**
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class TrayIconSelectionListener implements SelectionListener {
    private static final Point BALLOON_SIZE = new Point(356, 557);

    public static final String TITLE_COLUMN = "title";
    
    private final Display display;
    
    private final MindClient client;

    private BalloonWindow balloon;

    private Composite container = null;

    private Group workspacesGroup = null;

    private Group tasksGroup = null;

    private Group wikiGroup = null;

    private Button syncButton = null;

    private Button shareButton = null;

    private Text wikiTextArea = null;

    private Button postButton = null;

    private Button clearButton = null;

    private Table taskTable = null;

    private TableViewer taskTableViewer = null;

    private Button moreButton = null;

    private Button button = null;

    public TrayIconSelectionListener(Display display, MindClient client) {
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
        balloon.setText("Mindquarry Client");
        balloon.setImage(client.getIcon());

        container = balloon.getContents();
        container.setLayout(new GridLayout());

        createWorkspacesGroup();
        createTasksGroup();
        createWikiGroup();
        createTableViewer();

        container.pack();
        container.setSize(BALLOON_SIZE);
    }

    private void createTableViewer() {
        final TaskManager tman = new TaskManager();
        tman.addTask(new Task("Write User-centric-design memo"));
        tman.addTask(new Task("Transform website SVG mockup to XHTML+CSS"));
        tman.addTask(new Task("Write XSLT Stylesheets for Lenya navigation"));

        CellEditor[] editors = new CellEditor[taskTable.getColumnCount()];
        editors[0] = new CheckboxCellEditor(taskTable.getParent());

        taskTableViewer.setCellEditors(editors);
        taskTableViewer.setColumnProperties(new String[] { TITLE_COLUMN });

        taskTableViewer.setLabelProvider(new TaskTableLabelProvider());
        taskTableViewer.setContentProvider(new TaskTableContentProvider());
        taskTableViewer.setCellModifier(new TaskTableCellModifier(
                taskTableViewer));
        taskTableViewer
                .addSelectionChangedListener(new TaskSelectionChangedListener(
                        tman, taskTableViewer));
        taskTableViewer.setInput(tman);
    }

    /**
     * This method initializes workspacesGroup
     */
    private void createWorkspacesGroup() {
        GridData gridData22 = new GridData();
        gridData22.grabExcessVerticalSpace = true;
        gridData22.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
        gridData22.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
        gridData22.horizontalSpan = 2;
        gridData22.grabExcessHorizontalSpace = true;
        GridData gridData12 = new GridData();
        gridData12.grabExcessHorizontalSpace = true;
        gridData12.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
        gridData12.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
        gridData12.horizontalSpan = 2;
        gridData12.grabExcessVerticalSpace = true;
        GridData gridData21 = new GridData();
        gridData21.grabExcessVerticalSpace = false;
        gridData21.verticalAlignment = org.eclipse.swt.layout.GridData.END;
        gridData21.horizontalAlignment = org.eclipse.swt.layout.GridData.END;
        GridData gridData11 = new GridData();
        gridData11.horizontalAlignment = org.eclipse.swt.layout.GridData.END;
        gridData11.verticalAlignment = org.eclipse.swt.layout.GridData.END;
        gridData11.grabExcessHorizontalSpace = true;
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        GridData gridData = new GridData();
        gridData.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;

        workspacesGroup = new Group(container, SWT.SHADOW_NONE);
        workspacesGroup.setBackground(container.getBackground());
        workspacesGroup.setText("Workspaces");
        workspacesGroup.setLayout(gridLayout);
        workspacesGroup.setLayoutData(gridData);

        shareButton = new Button(workspacesGroup, SWT.NONE);
        shareButton.setImage(new Image(Display.getCurrent(), getClass()
                .getResourceAsStream("/icons/24x24/actions/up.png")));
        shareButton.setLayoutData(gridData11);
        shareButton.setText("Share");

        syncButton = new Button(workspacesGroup, SWT.PUSH);
        syncButton.setText("Synchronize");
        syncButton.setLayoutData(gridData21);
        syncButton.setImage(new Image(Display.getCurrent(), getClass()
                .getResourceAsStream("/icons/24x24/actions/down.png")));
        syncButton.addListener(SWT.Selection,
                new WorkspaceSynchronizeListener(client));
    }

    /**
     * This method initializes tasksGroup
     */
    private void createTasksGroup() {
        GridData gridData8 = new GridData();
        gridData8.horizontalAlignment = org.eclipse.swt.layout.GridData.END;
        GridData gridData4 = new GridData();
        gridData4.grabExcessHorizontalSpace = true;
        gridData4.horizontalAlignment = org.eclipse.swt.layout.GridData.END;
        GridLayout gridLayout2 = new GridLayout();
        gridLayout2.numColumns = 2;
        GridData gridData7 = new GridData();
        gridData7.grabExcessHorizontalSpace = true;
        gridData7.grabExcessVerticalSpace = true;
        gridData7.heightHint = 100;
        gridData7.horizontalSpan = 2;
        gridData7.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
        GridData gridData1 = new GridData();
        gridData1.grabExcessHorizontalSpace = true;
        gridData1.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;

        tasksGroup = new Group(container, SWT.NONE);
        tasksGroup.setBackground(container.getBackground());
        tasksGroup.setLayoutData(gridData1);
        tasksGroup.setLayout(gridLayout2);
        tasksGroup.setText("Tasks");

        taskTable = new Table(tasksGroup, SWT.BORDER);
        taskTable.setHeaderVisible(false);
        taskTable.setLayoutData(gridData7);
        taskTable.setLinesVisible(false);
        taskTableViewer = new TableViewer(taskTable);

        TableColumn activityColumn = new TableColumn(taskTable, SWT.NONE);
        activityColumn.setResizable(false);
        activityColumn.setWidth(100);
        activityColumn.setText("Task");

        moreButton = new Button(tasksGroup, SWT.NONE);
        moreButton.setEnabled(false);
        moreButton.setText("Other");
        moreButton.setLayoutData(gridData4);
        moreButton
                .setImage(new Image(Display.getCurrent(), getClass()
                        .getResourceAsStream(
                                "/icons/24x24/actions/system-search.png")));

        button = new Button(tasksGroup, SWT.NONE);
        button.setEnabled(false);
        button.setText("Done");
        button.setLayoutData(gridData8);
        button.setImage(new Image(Display.getCurrent(), getClass()
                .getResourceAsStream("/icons/24x24/emblems/done.png")));
    }

    /**
     * This method initializes wikiGroup
     */
    private void createWikiGroup() {
        GridData gridData6 = new GridData();
        gridData6.horizontalAlignment = org.eclipse.swt.layout.GridData.END;
        gridData6.grabExcessHorizontalSpace = true;
        GridData gridData5 = new GridData();
        gridData5.horizontalAlignment = org.eclipse.swt.layout.GridData.END;
        GridLayout gridLayout1 = new GridLayout();
        gridLayout1.numColumns = 2;
        GridData gridData3 = new GridData();
        gridData3.grabExcessHorizontalSpace = true;
        gridData3.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
        gridData3.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
        gridData3.horizontalSpan = 2;
        gridData3.heightHint = 130;
        gridData3.grabExcessVerticalSpace = true;
        GridData gridData2 = new GridData();
        gridData2.grabExcessHorizontalSpace = true;
        gridData2.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
        gridData2.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
        gridData2.grabExcessVerticalSpace = true;

        wikiGroup = new Group(container, SWT.NONE);
        wikiGroup.setBackground(container.getBackground());
        wikiGroup.setLayoutData(gridData2);
        wikiGroup.setLayout(gridLayout1);
        wikiGroup.setText("Wiki");

        wikiTextArea = new Text(wikiGroup, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL
                | SWT.BORDER);
        wikiTextArea.setLayoutData(gridData3);

        clearButton = new Button(wikiGroup, SWT.NONE);
        clearButton.setText("Clear");
        clearButton.setImage(new Image(Display.getCurrent(), getClass()
                .getResourceAsStream("/icons/24x24/actions/edit-clear.png")));
        clearButton.setEnabled(false);
        clearButton.setLayoutData(gridData6);

        postButton = new Button(wikiGroup, SWT.NONE);
        postButton.setText("Post");
        postButton.setImage(new Image(Display.getCurrent(), getClass()
                .getResourceAsStream("/icons/24x24/actions/document-new.png")));
        postButton.setEnabled(false);
        postButton.setLayoutData(gridData5);
    }
}
