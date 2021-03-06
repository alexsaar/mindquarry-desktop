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
package com.mindquarry.desktop.client.dialog.task;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.mindquarry.desktop.client.I18N;
import com.mindquarry.desktop.client.dialog.DialogBase;
import com.mindquarry.desktop.client.widget.util.ImageCombo;
import com.mindquarry.desktop.model.task.Task;

/**
 * Dialog adding new tasks to a team.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class TaskSettingsDialog extends DialogBase {
    private Text title = null;
    private Text summary = null;

    private ImageCombo status = null;
    private ImageCombo priority = null;

    private Button dueDateCheckbox;
    private DateTime calendar;

    private Task task;
    private boolean isNew = false;

    public TaskSettingsDialog(Shell shell, Task task, boolean isNew) {
        super(shell);
        setShellStyle(SWT.CLOSE | SWT.MIN | SWT.MAX | SWT.RESIZE | SWT.APPLICATION_MODAL);
        setBlockOnOpen(true);

        this.task = task;
        this.isNew = isNew;
    }

    public Task getChangedTask() {
        return task;
    }

    /**
     * @see org.eclipse.jface.dialogs.TitleAreaDialog#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite parent) {
        Control contents = super.createContents(parent);

        setTitle(I18N.getString("Edit the contents of a task")); //$NON-NLS-1$
        setMessage(
                I18N
                        .getString("Please enter the tasks data and press OK for adding/changing the task."), //$NON-NLS-1$
                IMessageProvider.INFORMATION);
        getShell().setText(I18N.getString("Edit Task") //$NON-NLS-1$
                + ": " //$NON-NLS-1$
                + task.getTitle());

        getShell().setSize(400, 650);
        getShell().redraw();
        return contents;
    }

    /**
     * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
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
        label.setText(I18N.getString("Title") //$NON-NLS-1$
                + ":"); //$NON-NLS-1$

        title = new Text(composite, SWT.BORDER | SWT.SINGLE);
        title.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        label = new Label(composite, SWT.LEFT);
        label.setText(I18N.getString("Status") //$NON-NLS-1$
                + ":"); //$NON-NLS-1$

        status = new ImageCombo(composite, SWT.BORDER | SWT.READ_ONLY
                | SWT.FLAT);
        status.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        status.setBackground(Display.getCurrent().getSystemColor(
                SWT.COLOR_WHITE));
        status.add("New", new Image(null, getClass().getResourceAsStream( //$NON-NLS-1$
                "/com/mindquarry/icons/16x16/status/task-new.png"))); //$NON-NLS-1$
        status.add("Running", new Image(null, getClass().getResourceAsStream( //$NON-NLS-1$
                "/com/mindquarry/icons/16x16/status/task-running.png"))); //$NON-NLS-1$
        status.add("Paused", new Image(null, getClass().getResourceAsStream( //$NON-NLS-1$
                "/com/mindquarry/icons/16x16/status/task-paused.png"))); //$NON-NLS-1$
        status.add("Done", new Image(null, getClass().getResourceAsStream( //$NON-NLS-1$
                "/com/mindquarry/icons/16x16/status/task-done.png"))); //$NON-NLS-1$
        status.select(0);

        label = new Label(composite, SWT.LEFT);
        label.setText(I18N.getString("Priority") //$NON-NLS-1$
                + ":"); //$NON-NLS-1$

        priority = new ImageCombo(composite, SWT.BORDER | SWT.READ_ONLY
                | SWT.FLAT);
        priority.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        priority.setBackground(Display.getCurrent().getSystemColor(
                SWT.COLOR_WHITE));
        priority.add("Low", null); //$NON-NLS-1$
        priority.add("Medium", null); //$NON-NLS-1$
        priority.add("Important", null); //$NON-NLS-1$
        priority.add("Critical", null); //$NON-NLS-1$
        priority.select(0);

        label = new Label(composite, SWT.LEFT);
        label.setText(I18N.getString("Summary") //$NON-NLS-1$
                + ":"); //$NON-NLS-1$

        summary = new Text(composite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL
                | SWT.WRAP);
        summary.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent fe) {
                // select the default text so user can just type new text
                // without manually deleting the default text:
                String defaultText = I18N
                        .getString("Edit the contents of a task");//$NON-NLS-1$
                if (summary.getText().equals(defaultText)) {
                    summary.selectAll();
                }
            }
        });
        summary.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                // don't print tab in text field but move to next
                // widget:
                if (e.keyCode == SWT.TAB) {
                    e.doit = false;
                    dueDateCheckbox.setFocus();
                } else if (e.keyCode == SWT.CR && e.stateMask == SWT.CTRL) {
                    // support Ctrl+Return => OK
                    getButton(IDialogConstants.OK_ID).notifyListeners(
                            SWT.Selection, new Event());
                }
            }

            public void keyReleased(KeyEvent e) {
            }
        });
        GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL
                | GridData.VERTICAL_ALIGN_FILL);
        gridData.heightHint = 100;
        gridData.widthHint = 360;
        summary.setLayoutData(gridData);
        ((GridData) summary.getLayoutData()).verticalSpan = 3;
        ((GridData) summary.getLayoutData()).grabExcessVerticalSpace = true;

        dueDateCheckbox = new Button(composite, SWT.CHECK);
        dueDateCheckbox.setText(I18N.getString("Due Date") //$NON-NLS-1$
                + ":"); //$NON-NLS-1$
        dueDateCheckbox.addSelectionListener(new DueDateCheckboxListener());

        calendar = new DateTime(composite, SWT.BORDER | SWT.CALENDAR);
        calendar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    /**
     * Creates the buttons for the button bar
     * 
     * @param parent
     *            the parent composite
     */
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        
        // check if buttons should be enabled
        isValid();
    }

    protected String getHelpURL() {
        // TODO fix help URL
        return "http://www.mindquarry.com/";
    }

    private void initTask() {
        if ((task.getTitle() != null)
                && (!task.getTitle().equals(Task.NO_TITLE_PLACEHOLDER))) {
            title.setText(task.getTitle());
            if (isNew) {
                title.selectAll();
            } else {
                // move cursor to end of text
                title.setSelection(title.getText().length());
            }
        }
        if (task.getSummary() != null) {
            summary.setText(task.getSummary());
        }
        /*
         * if (task.getDescription() != null) {
         * description.setText(task.getDescription()); }
         */
        if (task.getStatus() != null) {
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
        if (task.getPriority() != null) {
            if (task.getPriority().equals(Task.PRIORITY_LOW)) {
                priority.select(0);
            } else if (task.getPriority().equals(Task.PRIORITY_MEDIUM)) {
                priority.select(1);
            } else if (task.getPriority().equals(Task.PRIORITY_IMPORTANT)) {
                priority.select(2);
            } else if (task.getPriority().equals(Task.PRIORITY_CRITICAL)) {
                priority.select(3);
            }
        }
        if (task.getDate() != null && !task.getDate().trim().equals("")) { //$NON-NLS-1$
            // iso 8601 date format:
            String[] dateParts = task.getDate().split("-"); //$NON-NLS-1$
            if (dateParts.length == 3) {
                calendar.setDay(Integer.valueOf(dateParts[2]).intValue());
                calendar.setMonth(Integer.valueOf(dateParts[1]).intValue() - 1);
                calendar.setYear(Integer.valueOf(dateParts[0]).intValue());
            }
            dueDateCheckbox.setSelection(true);
            calendar.setEnabled(true);
        } else {
            dueDateCheckbox.setSelection(false);
            calendar.setEnabled(false);
        }
    }

    private void registerListeners() {
        title.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                if (isValid()) {
                    task.setTitle(title.getText());
                }
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
        /*
         * description.addModifyListener(new ModifyListener() { public void
         * modifyText(ModifyEvent e) {
         * task.setDescription(description.getText()); } });
         */
        priority.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                task.setPriority(priority.getText().toLowerCase());
            }
        });
        calendar.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                setDateFromUserInput();
            }
        });
    }

    private void setDateFromUserInput() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.YEAR, calendar.getYear());
        cal.set(Calendar.MONTH, calendar.getMonth());
        cal.set(Calendar.DAY_OF_MONTH, calendar.getDay());
        task.setDate(sdf.format(cal.getTime()));
    }

    class DueDateCheckboxListener implements SelectionListener {
        public void widgetDefaultSelected(SelectionEvent arg0) {
            // do nothing
        }

        public void widgetSelected(SelectionEvent arg0) {
            if (dueDateCheckbox.getSelection()) {
                calendar.setEnabled(true);
                setDateFromUserInput();
            } else {
                calendar.setEnabled(false);
                task.setDate(null);
            }
        }
    }

    private boolean isValid() {
        if (title.getText().equals("")) {
            setInvalid(I18N.getString("Title must not be empty."));
            return false;
        }
        setValid();
        return true;
    }
}
