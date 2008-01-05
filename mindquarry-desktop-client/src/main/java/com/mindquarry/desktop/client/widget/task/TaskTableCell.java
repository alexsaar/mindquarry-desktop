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
package com.mindquarry.desktop.client.widget.task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.mindquarry.desktop.client.I18N;
import com.mindquarry.desktop.client.MindClient;
import com.mindquarry.desktop.client.dialog.task.TaskSettingsDialog;
import com.mindquarry.desktop.client.widget.WidgetBase;
import com.mindquarry.desktop.model.task.Task;
import com.mindquarry.desktop.preferences.profile.Profile;
import com.mindquarry.desktop.util.HttpUtilities;

/**
 * Add summary documentation here.
 * 
 * @author <a href="mailto:saar@mindquarry.com">Alexander Saar</a>
 */
public class TaskTableCell extends WidgetBase {
    private static final Log log = LogFactory.getLog(TaskTableCell.class);

    private Label title;
    private Label description;
    
    private Task task;

    public TaskTableCell(Composite parent, int style, final MindClient client,
            final Task task) {
        super(parent, style, client);
        setBackground(getParent().getBackground());

        title.setText(task.getTitle());
        description.setText(task.getDescription() != null ? task
                .getDescription() : I18N
                .getString("No description available for this task"));
        
        this.task = task;
    }

    @Override
    protected void createContents(Composite parent) {
        parent.setLayout(new GridLayout(1, false));
        ((GridLayout) getLayout()).verticalSpacing = 0;
        ((GridLayout) getLayout()).marginHeight = 2;

        title = new Label(parent, SWT.LEFT);
        title.setBackground(getParent().getBackground());
        title.setFont(JFaceResources.getFont(MindClient.TASK_TITLE_FONT_KEY));
        
        description = new Label(parent, SWT.LEFT);
        description.setBackground(getParent().getBackground());
        description.setForeground(Display.getDefault().getSystemColor(
                SWT.COLOR_DARK_GRAY));
        description.setFont(JFaceResources
                .getFont(MindClient.TASK_DESC_FONT_KEY));
        
        MouseClickAdapter ddcAdapter = new MouseClickAdapter();
        title.addMouseListener(ddcAdapter);
        description.addMouseListener(ddcAdapter);
        addMouseListener(ddcAdapter);
    }

    @Override
    public void setBackground(Color color) {
        title.setBackground(color);
        description.setBackground(color);
        super.setBackground(color);
    }
    
    private class MouseClickAdapter extends MouseAdapter {
        @Override
        public void mouseDoubleClick(MouseEvent event) {
            Profile prof = Profile.getSelectedProfile(client
                    .getPreferenceStore());

            try {
                // use a clone of the task so cancel works:
                TaskSettingsDialog dlg = new TaskSettingsDialog(client
                        .getShell(), task.clone(), false);

                if (dlg.open() == Window.OK) {
                    Task newTask = dlg.getChangedTask();
                    HttpUtilities.putAsXML(prof.getLogin(), prof
                            .getPassword(), newTask.getId(), newTask
                            .getContentAsXML().asXML().getBytes("utf-8"));
                }
            } catch (Exception e) {
                MessageDialog.openError(new Shell(SWT.ON_TOP), I18N
                        .getString("Network error"),//$NON-NLS-1$
                        I18N.getString("Could not update the task")//$NON-NLS-1$
                                + ": " + e.toString());
                log.error("Could not update task with id " //$NON-NLS-1$
                        + task.getId(), e);
            }
        }

        @Override
        public void mouseDown(MouseEvent e) {
            setBackground(Display.getDefault().getSystemColor(SWT.COLOR_LIST_SELECTION));
        }
    }
}
