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
package com.mindquarry.desktop.client.task.dialog;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.mindquarry.desktop.client.task.Task;
import com.mindquarry.desktop.client.util.widgets.ImageCombo;

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

    private ImageCombo status = null;

    private ImageCombo priority = null;

    private DateTime calendar;

    private Task task;

    private Task backupTask;

    public TaskDialog(Shell shell, Task task) {
        super(shell);
        setBlockOnOpen(true);

        this.task = task;
        backupTask = task;
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
        registerListeners();
        return composite;
    }

    private void createTaskDataSection(Composite composite) {
        Label label = new Label(composite, SWT.LEFT);
        label.setText("Title:");

        title = new Text(composite, SWT.BORDER | SWT.SINGLE);
        title.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        label = new Label(composite, SWT.LEFT);
        label.setText("Status:");

        status = new ImageCombo(composite, SWT.BORDER | SWT.READ_ONLY
                | SWT.FLAT);
        status.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        status.setBackground(Display.getCurrent().getSystemColor(
                SWT.COLOR_WHITE));
        status.add("New", new Image(null, getClass().getResourceAsStream(
                "/com/mindquarry/icons/16x16/status/task-new.png"))); //$NON-NLS-1$
        status.add("Running", new Image(null, getClass().getResourceAsStream(
                "/com/mindquarry/icons/16x16/status/task-running.png"))); //$NON-NLS-1$
        status.add("Paused", new Image(null, getClass().getResourceAsStream(
                "/com/mindquarry/icons/16x16/status/task-paused.png"))); //$NON-NLS-1$
        status.add("Done", new Image(null, getClass().getResourceAsStream(
                "/com/mindquarry/icons/16x16/status/task-done.png"))); //$NON-NLS-1$
        status.select(0);

        label = new Label(composite, SWT.LEFT);
        label.setText("Priority:");

        priority = new ImageCombo(composite, SWT.BORDER | SWT.READ_ONLY
                | SWT.FLAT);
        priority.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        priority.setBackground(Display.getCurrent().getSystemColor(
                SWT.COLOR_WHITE));
        priority.add("Low", null);
        priority.add("Medium", null);
        priority.add("Important", null);
        priority.add("Critical", null);
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

        calendar = new DateTime(composite, SWT.BORDER | SWT.CALENDAR);
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

    private void initTask() {
        title.setText(task.getTitle());
        summary.setText(task.getSummary());
        description.setText(task.getDescription());

        if (task.getStatus().equals(Task.STATUS_NEW)) {
            status.select(0);
        } else if (task.getStatus().equals(Task.STATUS_RUNNING)) {
            status.select(1);
        } else if (task.getStatus().equals(Task.STATUS_PAUSED)) {
            status.select(2);
        } else if (task.getStatus().equals(Task.STATUS_DONE)) {
            status.select(3);
        }
        if (task.getPriority().equals(Task.PRIORITY_LOW)) {
            priority.select(0);
        } else if (task.getPriority().equals(Task.PRIORITY_MEDIUM)) {
            priority.select(1);
        } else if (task.getPriority().equals(Task.PRIORITY_IMPORTANT)) {
            priority.select(2);
        } else if (task.getPriority().equals(Task.PRIORITY_CRITICAL)) {
            priority.select(3);
        }
        String[] dateParts = task.getDate().split("/"); //$NON-NLS-1$
        calendar.setDay(Integer.valueOf(dateParts[1]));
        calendar.setMonth(Integer.valueOf(dateParts[0]) - 1);
        calendar.setYear(Integer.valueOf(dateParts[2]));
    }

    private void registerListeners() {
        title.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                task.setTitle(title.getText());
            }
        });
        status.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                task.setStatus(status.getText().toLowerCase());
            }
        });
        summary.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                task.setSummary(summary.getText());
            }
        });
        description.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                task.setDescription(description.getText());
            }
        });
        priority.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                task.setPriority(priority.getText().toLowerCase());
            }
        });
        calendar.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                task.setDate((calendar.getMonth() + 1) + "/" //$NON-NLS-1$
                        + calendar.getDay() + "/" //$NON-NLS-1$
                        + calendar.getYear());
            }
        });
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
     */
    @Override
    protected void cancelPressed() {
        super.cancelPressed();
        task = backupTask;
    }
}
