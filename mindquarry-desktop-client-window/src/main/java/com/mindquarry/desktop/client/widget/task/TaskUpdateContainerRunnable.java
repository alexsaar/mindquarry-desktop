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
 * Container that can show three different things, depending on state:
 * 
 * (a) Show a list of tasks (b) Show a progress bar with a message while
 * updating (c) Show a message with an icon, e.g. that there are no tasks or
 * that there was an error.
 * 
 * @author <a href="mailto:saar@mindquarry.com">Alexander Saar</a>
 * @author <a href="mailto:christian.richardt@mindquarry.com">Christian Richardt</a>
 */
public class TaskUpdateContainerRunnable extends
        UpdateContainerRunnable<TableViewer> {
    private MindClient client;

    public TaskUpdateContainerRunnable(MindClient client,
            ContainerWidget<TableViewer> containerWidget, boolean refreshing,
            boolean empty, String icon, String message) {
        super(containerWidget, refreshing, empty, icon, message); //$NON-NLS-1$
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
        // containerWidget.getViewer().activateCustomTooltips();
        containerWidget.getViewer().getTable().setLayoutData(
                new GridData(GridData.FILL_BOTH));
        containerWidget.getViewer().getTable().setHeaderVisible(false);
        containerWidget.getViewer().getTable().setLinesVisible(false);
        // containerWidget.getViewer().getTable().setToolTipText("");
        containerWidget.getViewer().getTable().setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, true, true));
        containerWidget.getShell().addListener(SWT.Resize, new Listener() {
            public void handleEvent(Event event) {
                if (containerWidget.getViewer() != null) {
                    doResize();
                }
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
        
        containerWidget.layout(true);
        doResize();
    }

    private void doResize() {
        containerWidget
                .getViewer()
                .getTable()
                .getColumn(0)
                .setWidth(
                        containerWidget.getViewer().getTable().getClientArea().width - 4);
    }

    @Override
    protected void destroyContainerContent() {
        if (containerWidget.getViewer() != null) {
            containerWidget.getViewer().getTable().dispose();
            containerWidget.setViewer(null);
        }
    }
}
