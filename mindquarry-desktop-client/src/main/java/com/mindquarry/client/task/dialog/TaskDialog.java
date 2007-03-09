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
package com.mindquarry.client.task.dialog;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.mindquarry.client.task.Task;

/**
 * Dialog adding new tasks to a team.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class TaskDialog extends TitleAreaDialog {
    private Text title = null;

    private Text summary = null;

    private Text description = null;

    private CCombo status = null;

    private CCombo priority = null;

    private Task task;

    public TaskDialog(Shell shell, Task task) {
        super(shell);
        setBlockOnOpen(true);
        this.task = task;
    }

    @Override
    protected Control createContents(Composite parent) {
        Control contents = super.createContents(parent);

        setTitle("Edit the contents of a task");
        setMessage(
                "Please enter the tasks data and press OK for adding/changing the task.",
                IMessageProvider.INFORMATION);

        getShell().setText("Edit Task Data");

        return contents;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(1, true));

        createTaskDataSection(composite);
        initTask();
        return composite;
    }

    private void createTaskDataSection(Composite composite) {
        Label label = new Label(composite, SWT.LEFT);
        label.setText("Title:");

        title = new Text(composite, SWT.BORDER | SWT.SINGLE);
        title.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        label = new Label(composite, SWT.LEFT);
        label.setText("Status:");

        status = new CCombo(composite, SWT.BORDER | SWT.READ_ONLY | SWT.FLAT);
        status.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        status.setBackground(Display.getCurrent().getSystemColor(
                SWT.COLOR_WHITE));
        status.add("New");
        status.add("Running");
        status.add("Paused");
        status.add("Done");
        status.select(0);

        label = new Label(composite, SWT.LEFT);
        label.setText("Priority:");

        priority = new CCombo(composite, SWT.BORDER | SWT.READ_ONLY | SWT.FLAT);
        priority.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        priority.setBackground(Display.getCurrent().getSystemColor(
                SWT.COLOR_WHITE));
        priority.add("Low");
        priority.add("Medium");
        priority.add("Important");
        priority.add("Critical");
        priority.select(0);

        label = new Label(composite, SWT.LEFT);
        label.setText("Summary:");

        summary = new Text(composite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL
                | SWT.WRAP);
        summary.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
                | GridData.VERTICAL_ALIGN_FILL));
        ((GridData) summary.getLayoutData()).verticalSpan = 3;
        ((GridData) summary.getLayoutData()).grabExcessVerticalSpace = true;

        label = new Label(composite, SWT.LEFT);
        label.setText("Description:");

        description = new Text(composite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL
                | SWT.WRAP);
        description.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
                | GridData.VERTICAL_ALIGN_FILL));
        ((GridData) description.getLayoutData()).verticalSpan = 3;
        ((GridData) description.getLayoutData()).grabExcessVerticalSpace = true;

        label = new Label(composite, SWT.LEFT);
        label.setText("Due Date:");

        DateTime calendar = new DateTime(composite, SWT.BORDER | SWT.CALENDAR);
        calendar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    /**
     * Creates the buttons for the button bar
     * 
     * @param parent the parent composite
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
                true);
        createButton(parent, IDialogConstants.CANCEL_ID,
                IDialogConstants.CANCEL_LABEL, true);
    }

    /**
     * Setter for task.
     * 
     * @param task the task to set
     */
    private void initTask() {
        title.setText(task.getTitle());
        summary.setText(task.getSummary());

        if (task.getStatus().equals(Task.STATUS_NEW)) {
            status.select(0);
        } else if (task.getStatus().equals(Task.STATUS_RUNNING)) {
            status.select(1);
        } else if (task.getStatus().equals(Task.STATUS_PAUSED)) {
            status.select(2);
        } else if (task.getStatus().equals(Task.STATUS_DONE)) {
            status.select(3);
        }
    }
}
