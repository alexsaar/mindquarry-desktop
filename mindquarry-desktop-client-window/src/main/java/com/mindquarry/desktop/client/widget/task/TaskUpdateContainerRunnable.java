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

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.mindquarry.desktop.client.Messages;
import com.mindquarry.desktop.client.MindClient;
import com.mindquarry.desktop.client.widget.util.container.ContainerWidget;
import com.mindquarry.desktop.client.widget.util.container.UpdateContainerRunnable;

/**
 * Add summary documentation here.
 * 
 * @author <a href="mailto:saar@mindquarry.com">Alexander Saar</a>
 */
public class TaskUpdateContainerRunnable extends
        UpdateContainerRunnable<TableViewer> {
    private static final String UPDATE_MESSAGE = Messages.getString("Updating task list"); //$NON-NLS-1$
    private static final String EMPTY_MESSAGE = Messages
        .getString("Currently no tasks are active."); //$NON-NLS-1$
    private MindClient client;

    public TaskUpdateContainerRunnable(MindClient client,
            ContainerWidget<TableViewer> containerWidget, boolean empty,
            String errMessage, boolean refreshing) {
        super(containerWidget, empty, errMessage, 
                UPDATE_MESSAGE, EMPTY_MESSAGE, refreshing);
        this.client = client;
    }

    /**
     * @see com.mindquarry.desktop.client.widget.util.container.UpdateContainerRunnable#createContainerContent()
     */
    @Override
    protected void createContainerContent() {
        // create table viewer
        containerWidget.setViewer(new TableViewer(containerWidget,
                SWT.FULL_SELECTION));
        containerWidget.getViewer().activateCustomTooltips();
        containerWidget.getViewer().getTable().setLayoutData(
                new GridData(GridData.FILL_BOTH));
        containerWidget.getViewer().getTable().setHeaderVisible(false);
        containerWidget.getViewer().getTable().setLinesVisible(false);
        containerWidget.getViewer().getTable().setToolTipText(""); //$NON-NLS-1$
        containerWidget.getViewer().getTable().setFont(
                JFaceResources.getFont(MindClient.TASK_TITLE_FONT_KEY));
        containerWidget.getViewer().getTable().setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, true, true));
        containerWidget.getShell().addListener(SWT.Resize, new Listener() {
            public void handleEvent(Event event) {
                containerWidget.getViewer().getTable().getColumn(0).setWidth(
                        containerWidget.getViewer().getTable().getSize().x);
            }
        });
        containerWidget.getViewer().setContentProvider(new ContentProvider());
        containerWidget.getViewer().addDoubleClickListener(
                new DoubleClickListener(client, containerWidget.getViewer()));

        // create columns
        TableViewerColumn col = new TableViewerColumn(containerWidget
                .getViewer(), SWT.NONE);
        col.setLabelProvider(new TaskLabelProvider());
        col.getColumn().setResizable(false);
        col.getColumn().setWidth(200);
        col.getColumn().setText(Messages.getString("Description"));//$NON-NLS-1$
        containerWidget.getViewer().getTable().getColumn(0).setWidth(
                containerWidget.getSize().x - 4);
    }

    @Override
    protected void destroyContainerContent() {
        if (containerWidget.getViewer() != null) {
            containerWidget.getViewer().getTable().dispose();
            containerWidget.setViewer(null);
        }
    }
}
