/**
 * 
 */
package com.mindquarry.client;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Link;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;

/**
 * @author <a href="mailto:lars@trieloff.net">Lars Trieloff</a>
 *
 */
public class ClientShell {
	private Shell sShell = null;  //  @jve:decl-index=0:visual-constraint="10,10"
	private Group workspacesGroup = null;
	private Group tasksGroup = null;
	private Group wikiGroup = null;
	private Button syncButton = null;
	private Button shareButton = null;
	private Text wikiTextArea = null;
	private Button postButton = null;
	private Button clearButton = null;
	private Table taskTable = null;
	private Link shareLink = null;
	private Link syncLink = null;
	private TableViewer taskTableViewer = null;
	

	 public static void main(String[] args) {
	      ClientShell test = new ClientShell();
	      Display display = new Display();
	      test.createSShell();
	      test.sShell.open();
	      while (!test.sShell.isDisposed()) {
	         if (!display.readAndDispatch())
	            display.sleep();
	      }
	      System.out.println("finishing...");
	      display.dispose();
	   }
	
	/**
	 * This method initializes sShell
	 */
	private void createSShell() {
		sShell = new Shell();
		sShell.setText("Mindquarry Client");
		sShell.setLayout(new GridLayout());
		createWorkspacesGroup();
		createTasksGroup();
		createWikiGroup();
		
		
		TaskManager tman = new TaskManager();
		tman.addTask(new Task("Boo Bar"));
		tman.addTask(new Task("Foo Bar"));
		tman.addTask(new Task("Uppa"));
		//tman.addTask(new Task("Huppa"));
		
		CellEditor[] editors = new CellEditor[taskTable.getColumnCount()];
		editors[0] = new CheckboxCellEditor(taskTable);
		editors[1] = new TextCellEditor(taskTable);
		editors[2] = new CheckboxCellEditor(taskTable);
		
		taskTableViewer.setCellEditors(editors);
		
		taskTableViewer.setContentProvider(new TaskTableContentProvider());
		taskTableViewer.setLabelProvider(new TaskTableLabelProvider());
		taskTableViewer.setInput(tman);
		
		sShell.pack();
		
		sShell.setSize(new Point(300, 500));
	}
	
	/**
	 * This method initializes workspacesGroup	
	 *
	 */
	private void createWorkspacesGroup() {
		GridData gridData22 = new GridData();
		gridData22.grabExcessVerticalSpace = true;
		gridData22.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData22.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData22.grabExcessHorizontalSpace = true;
		GridData gridData12 = new GridData();
		gridData12.grabExcessHorizontalSpace = true;
		gridData12.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData12.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData12.grabExcessVerticalSpace = true;
		GridData gridData21 = new GridData();
		gridData21.grabExcessVerticalSpace = true;
		gridData21.verticalAlignment = org.eclipse.swt.layout.GridData.END;
		gridData21.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		GridData gridData11 = new GridData();
		gridData11.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData11.verticalAlignment = org.eclipse.swt.layout.GridData.END;
		gridData11.grabExcessHorizontalSpace = true;
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		GridData gridData = new GridData();
		gridData.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		workspacesGroup = new Group(sShell, SWT.SHADOW_NONE);
		workspacesGroup.setText("Workspaces");
		workspacesGroup.setLayout(gridLayout);
		workspacesGroup.setLayoutData(gridData);
		shareLink = new Link(workspacesGroup, SWT.NONE);
		shareLink.setText("Share your local work on following workspaces with your team: <a>Mindquarry</a> and <a>Goshaky</a>");
		shareLink.setLayoutData(gridData22);
		shareButton = new Button(workspacesGroup, SWT.NONE);
		shareButton.setText("Share");
		shareButton.setLayoutData(gridData11);
		shareButton.setImage(new Image(Display.getCurrent(), getClass().getResourceAsStream("/icons/24x24/actions/up.png")));
		syncLink = new Link(workspacesGroup, SWT.NONE);
		syncLink.setText("Synchronize your team's work to your local workspaces: <a>cyclr.com</a> and <a>Damagecontrol</a>.");
		syncLink.setLayoutData(gridData12);
		syncButton = new Button(workspacesGroup, SWT.PUSH);
		syncButton.setText("Synchronize");
		syncButton.setImage(new Image(Display.getCurrent(), getClass().getResourceAsStream("/icons/24x24/actions/down.png")));
		syncButton.setLayoutData(gridData21);
	}
	/**
	 * This method initializes tasksGroup	
	 *
	 */
	private void createTasksGroup() {
		GridData gridData7 = new GridData();
		gridData7.grabExcessHorizontalSpace = true;
		gridData7.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		GridData gridData1 = new GridData();
		gridData1.grabExcessHorizontalSpace = true;
		gridData1.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		tasksGroup = new Group(sShell, SWT.BORDER);
		tasksGroup.setLayout(new GridLayout());
		tasksGroup.setLayoutData(gridData1);
		tasksGroup.setText("Tasks");
		taskTable = new Table(tasksGroup, SWT.NONE);
		taskTable.setHeaderVisible(true);
		taskTable.setLayoutData(gridData7);
		taskTable.setLinesVisible(true);
		taskTableViewer = new TableViewer(taskTable);
		TableColumn activityColumn = new TableColumn(taskTable, SWT.NONE);
		activityColumn.setWidth(60);
		activityColumn.setText("Active");
		TableColumn taskColumn = new TableColumn(taskTable, SWT.NONE);
		taskColumn.setWidth(150);
		taskColumn.setText("Task");
		TableColumn statusColumn = new TableColumn(taskTable, SWT.LEFT);
		statusColumn.setWidth(40);
		statusColumn.setResizable(true);
		statusColumn.setText("Done");
	}
	/**
	 * This method initializes wikiGroup	
	 *
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
		gridData3.grabExcessVerticalSpace = true;
		GridData gridData2 = new GridData();
		gridData2.grabExcessHorizontalSpace = true;
		gridData2.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData2.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData2.grabExcessVerticalSpace = true;
		wikiGroup = new Group(sShell, SWT.NONE);
		wikiGroup.setLayoutData(gridData2);
		wikiGroup.setLayout(gridLayout1);
		wikiGroup.setText("Wiki");
		wikiTextArea = new Text(wikiGroup, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);
		wikiTextArea.setLayoutData(gridData3);
		clearButton = new Button(wikiGroup, SWT.NONE);
		clearButton.setText("Clear");
		clearButton.setLayoutData(gridData6);
		postButton = new Button(wikiGroup, SWT.NONE);
		postButton.setText("Post");
		postButton.setLayoutData(gridData5);
	}

}
