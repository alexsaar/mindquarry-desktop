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
package com.mindquarry.desktop.client.widget.workspace;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TreeItem;
import org.tigris.subversion.javahl.StatusKind;

import com.mindquarry.desktop.client.Messages;
import com.mindquarry.desktop.client.MindClient;
import com.mindquarry.desktop.client.widget.util.container.ContainerWidget;
import com.mindquarry.desktop.client.widget.util.container.UpdateContainerRunnable;

/**
 * Add summary documentation here.
 * 
 * @author <a href="mailto:saar@mindquarry.com">Alexander Saar</a>
 */
public class WorkspaceUpdateContainerRunnable extends
        UpdateContainerRunnable<TreeViewer> {
    private static Log log = LogFactory
            .getLog(WorkspaceUpdateContainerRunnable.class);

    private static final Image folderImage = new Image(
            Display.getCurrent(),
            WorkspaceBrowserWidget.class
                    .getResourceAsStream("/org/tango-project/tango-icon-theme/32x32/places/folder.png")); //$NON-NLS-1$

    private static final Image fileImage = new Image(
            Display.getCurrent(),
            WorkspaceBrowserWidget.class
                    .getResourceAsStream("/org/tango-project/tango-icon-theme/32x32/mimetypes/text-x-generic.png")); //$NON-NLS-1$

    private static final Image unknownFileImage = new Image(
            Display.getCurrent(),
            WorkspaceBrowserWidget.class
                    .getResourceAsStream("/org/tango-project/tango-icon-theme/32x32/mimetypes/text-x-generic-template.png")); //$NON-NLS-1$

    private static final Image downloadImage = new Image(
            Display.getCurrent(),
            WorkspaceBrowserWidget.class
                    .getResourceAsStream("/com/mindquarry/icons/32x32/actions/synchronize-down.png")); //$NON-NLS-1$

    private static final Image uploadImage = new Image(
            Display.getCurrent(),
            WorkspaceBrowserWidget.class
                    .getResourceAsStream("/com/mindquarry/icons/32x32/actions/synchronize-up.png")); //$NON-NLS-1$

    private static final Image deleteImage = new Image(
            Display.getCurrent(),
            WorkspaceBrowserWidget.class
                    .getResourceAsStream("/org/tango-project/tango-icon-theme/32x32/status/user-trash-full.png")); //$NON-NLS-1$

    private Image conflictImage = new Image(
            Display.getCurrent(),
            getClass()
                    .getResourceAsStream(
                            "/org/tango-project/tango-icon-theme/32x32/status/dialog-warning.png")); //$NON-NLS-1$
    
    private static final String UPDATE_MESSAGE = Messages.getString("Synchronizing workspaces ..."); //$NON-NLS-1$
    private static final String EMPTY_MESSAGE = Messages
        .getString("There are currently no workspace changes to synchronize."); //$NON-NLS-1$

    public WorkspaceUpdateContainerRunnable(MindClient client,
            ContainerWidget<TreeViewer> containerWidget, boolean empty,
            String errMessage, boolean refreshing) {
        super(containerWidget, empty, errMessage, UPDATE_MESSAGE,
                EMPTY_MESSAGE, refreshing);
    }

    /**
     * @see com.mindquarry.desktop.client.widget.util.container.UpdateContainerRunnable#createContainerContent()
     */
    @Override
    protected void createContainerContent() {
        // create workspace/changes browser
        containerWidget
                .setViewer(new TreeViewer(containerWidget, SWT.BORDER
                        | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION));
        containerWidget.getViewer().setContentProvider(
                new ContentProvider((WorkspaceBrowserWidget) containerWidget));
        containerWidget.getViewer().setSorter(new ViewerSorter() {
            public int category(Object element) {
                File file = (File) element;
                // sort directories first, rest is sorted naturally
                if (file.isDirectory()) {
                    return 1;
                } else {
                    return 2;
                }
            }
        });
        containerWidget.getViewer().getTree().setLayoutData(
                new GridData(GridData.FILL_BOTH));
        containerWidget.getViewer().getTree().setHeaderVisible(true);
        containerWidget.getViewer().getTree().setLinesVisible(true);
        containerWidget.getViewer().getTree().setFont(
                JFaceResources.getFont(MindClient.TEAM_NAME_FONT_KEY));
        containerWidget.getViewer().getTree().addListener(SWT.Selection,
                new Listener() {
                    public void handleEvent(Event event) {
                        if (event.detail == SWT.CHECK) {
                            TreeItem item = (TreeItem) event.item;
                            if (item.getChecked()) {
                                // check all parents
                                TreeItem parent = item.getParentItem();
                                while (parent != null) {
                                    parent.setChecked(true);
                                    parent = parent.getParentItem();
                                }
                            } else {
                                // uncheck all children
                                TreeItem[] children = item.getItems();
                                uncheckChildren(children);
                            }
                        }
                    }

                    private void uncheckChildren(TreeItem[] children) {
                        for (TreeItem child : children) {
                            child.setChecked(false);
                            uncheckChildren(child.getItems());
                        }
                    }
                });
        TreeViewerColumn col = new TreeViewerColumn(
                containerWidget.getViewer(), SWT.LEFT);
        col.getColumn().setText("Name");
        col.getColumn().setWidth(540);
        col.setLabelProvider(new ColumnLabelProvider() {
            public Image getImage(Object element) {
                File file = (File) element;
                if (file.isDirectory()) {
                    return folderImage;
                } else if (file.isFile()) {
                    return fileImage;
                } else {
                    return unknownFileImage;
                }
            }

            public String getText(Object element) {
                return ((File) element).getName();
            }
        });
        col = new TreeViewerColumn(containerWidget.getViewer(), SWT.CENTER);
        col.getColumn().setResizable(false);
        col.getColumn().setWidth(32);
        col.setLabelProvider(new ColumnLabelProvider() {
            public Image getImage(Object element) {
                File file = (File) element;
                int localStatus = -1;
                int remoteStatus = -1;
                if (((WorkspaceBrowserWidget) containerWidget).localChanges != null
                        && ((WorkspaceBrowserWidget) containerWidget).localChanges
                                .containsKey(file)) {
                    localStatus = ((WorkspaceBrowserWidget) containerWidget).localChanges
                            .get(file).intValue();
                }
                if (((WorkspaceBrowserWidget) containerWidget).remoteChanges != null
                        && ((WorkspaceBrowserWidget) containerWidget).remoteChanges
                                .containsKey(file)) {
                    remoteStatus = ((WorkspaceBrowserWidget) containerWidget).remoteChanges
                            .get(file).intValue();
                }
                if (localStatus == StatusKind.obstructed) {
                    // FIXME: add question mark icon
                } else if (localStatus == StatusKind.added
                        || localStatus == StatusKind.unversioned) {
                    // TODO: show upload icon with "+" sign
                    return uploadImage;
                } else if (localStatus == StatusKind.modified) {
                    return uploadImage;
                } else if (remoteStatus == StatusKind.modified) {
                    return downloadImage;
                } else if (remoteStatus == StatusKind.added) {
                    return downloadImage;
                } else if (localStatus == StatusKind.deleted || localStatus == StatusKind.missing) {
                    return deleteImage;
                } else if (remoteStatus == StatusKind.deleted) {
                    return deleteImage;
                } else if (localStatus == StatusKind.conflicted) {
                    return conflictImage;
                } else if (localStatus != -1 || remoteStatus != -1) {
                    log.warn("No icon set for local/remote status "
                            + localStatus + "/" + remoteStatus + " on file "
                            + file.getAbsolutePath());
                }
                // TODO: which other cases do we need to display?
                return null;
            }

            public String getText(Object element) {
                return "";
            }
        });
        containerWidget.getViewer().setInput(
                ((WorkspaceBrowserWidget) containerWidget).workspaceRoot);
        containerWidget.getViewer().expandAll();
        checkAllItem(containerWidget.getViewer().getTree().getItems());
        containerWidget.layout(true);
    }
    
    private void checkAllItem(TreeItem[] items) {
        for(TreeItem item : items) {
            item.setChecked(true);
            checkAllItem(item.getItems());
        }
    }

    @Override
    protected void destroyContainerContent() {
        if (containerWidget.getViewer() != null) {
            containerWidget.getViewer().getTree().dispose();
            containerWidget.setViewer(null);
        }
    }
}
