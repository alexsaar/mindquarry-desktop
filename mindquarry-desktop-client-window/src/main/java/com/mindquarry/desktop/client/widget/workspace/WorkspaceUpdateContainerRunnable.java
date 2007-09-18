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
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.tigris.subversion.javahl.NodeKind;
import org.tigris.subversion.javahl.Status;
import org.tmatesoft.svn.core.internal.wc.SVNFileUtil;

import com.mindquarry.desktop.client.Messages;
import com.mindquarry.desktop.client.MindClient;
import com.mindquarry.desktop.client.action.workspace.OpenFileAction;
import com.mindquarry.desktop.client.action.workspace.OpenSelectedFileEvent;
import com.mindquarry.desktop.client.widget.util.container.ContainerWidget;
import com.mindquarry.desktop.client.widget.util.container.UpdateContainerRunnable;
import com.mindquarry.desktop.event.EventBus;
import com.mindquarry.desktop.workspace.conflict.Change;

/**
 * Class that creates the workspace widget.
 * 
 * @author <a href="mailto:saar@mindquarry.com">Alexander Saar</a>
 */
public class WorkspaceUpdateContainerRunnable extends
        UpdateContainerRunnable<TreeViewer> {
    private static Log log = LogFactory
            .getLog(WorkspaceUpdateContainerRunnable.class);

    private static final Image FOLDER_IMAGE = new Image(
            Display.getCurrent(),
            WorkspaceBrowserWidget.class
                    .getResourceAsStream("/org/tango-project/tango-icon-theme/32x32/places/folder.png")); //$NON-NLS-1$

    private static final Image FILE_IMAGE = new Image(
            Display.getCurrent(),
            WorkspaceBrowserWidget.class
                    .getResourceAsStream("/org/tango-project/tango-icon-theme/32x32/mimetypes/text-x-generic.png")); //$NON-NLS-1$

    private static final Image UNKNOWN_FILE_IMAGE = new Image(
            Display.getCurrent(),
            WorkspaceBrowserWidget.class
                    .getResourceAsStream("/org/tango-project/tango-icon-theme/32x32/mimetypes/text-x-generic-template.png")); //$NON-NLS-1$

    private MindClient client;
    private MenuItem menuItem;
    
    public WorkspaceUpdateContainerRunnable(MindClient client,
            ContainerWidget<TreeViewer> containerWidget, boolean refreshing,
            boolean empty, String icon, String message) {
        super(containerWidget, refreshing, empty, icon, message);
        this.client = client;
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

        addContextMenu();
        
        containerWidget.getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                // make sure the "open" button is only enables when it makes sense:
                boolean enableButton = enableOpenButton();
                client.enableAction(enableButton, OpenFileAction.class.getName());
            }
        });
        containerWidget.getViewer().addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent arg0) {
                EventBus.send(new OpenSelectedFileEvent(this));
            }
        });
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
        
        // simulate tooltips:
        Listener treeListener = new HoverForToolTipListener(containerWidget.getViewer());
        containerWidget.getViewer().getTree().addListener (SWT.Dispose, treeListener);
        containerWidget.getViewer().getTree().addListener (SWT.KeyDown, treeListener);
        containerWidget.getViewer().getTree().addListener (SWT.MouseMove, treeListener);
        containerWidget.getViewer().getTree().addListener (SWT.MouseHover, treeListener);
        
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
        col.getColumn().setWidth(400);
        col.setLabelProvider(new ColumnLabelProvider() {
            public Image getImage(Object element) {
                File file = (File) element;
                WorkspaceBrowserWidget widget = (WorkspaceBrowserWidget) containerWidget;
                if (widget.changes != null && widget.changes.getFiles().contains(file)) {
                    Status status = widget.changes.getStatus(file);
                    // first check for a NodeKind set as local property
                    if (status.getNodeKind() == NodeKind.dir) {
                        return FOLDER_IMAGE;
                    } else if (status.getNodeKind() == NodeKind.file) {
                        return FILE_IMAGE;
                    // otherwise look for the remote variant (ie. newly added file or folder remotely)
                    } else if (status.getReposKind() == NodeKind.dir) {
                        return FOLDER_IMAGE;
                    } else if (status.getReposKind() == NodeKind.file) {
                        return FILE_IMAGE;
                    }
                }
                // fallback: simply lookup the local file
                if (file.isDirectory()) {
                    return FOLDER_IMAGE;
                } else if (file.isFile()) {
                    return FILE_IMAGE;
                } else {
                    return UNKNOWN_FILE_IMAGE;
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
                // lookup the status via the File -> Status maps
                WorkspaceBrowserWidget widget = (WorkspaceBrowserWidget) containerWidget;

                ModificationDescription descr = ModificationDescription.getDescription(null, null);
                if (widget.changes != null
                        && widget.changes.getFiles().contains(file)) {
                    descr = ModificationDescription
                            .getDescription(widget.changes.getChange(file));
                }
                
                return descr.getImage();
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
        
    /**
     * Return true if the user selected an item which can be opened.
     */
    private boolean enableOpenButton() {
        ISelection iSelection = containerWidget.getViewer().getSelection();
        if (iSelection instanceof StructuredSelection) {
            StructuredSelection structsel = (StructuredSelection) iSelection;
            Object element = structsel.getFirstElement();
            if (element instanceof File) {
                File file = (File) element;
                boolean enableButton = false;
                if (containerWidget.getViewer().getSelection().isEmpty()) {
                    enableButton = false;
                } else {
                    if (file.exists() && (SVNFileUtil.isWindows || SVNFileUtil.isOSX)) {
                        // on windows and Mac we can open both files and
                        // directories with Program.launch()
                        enableButton = true;
                    } else if (file.exists() && file.isFile()) {
                        // TODO: we cannot open directories on Unix yet,
                        // it depends on the desktop (gnome-open, kfmclient)
                        enableButton = true;
                    } else {
                        // a remotely added file, we cannot view that yet:
                        enableButton = false;
                    }
                }
                return enableButton;
            }
        }
        return false;
    }

    private void addContextMenu() {
        final Menu popupmenu = new Menu (client.getShell(), SWT.POP_UP);
        popupmenu.addMenuListener(new MenuListener() {
            public void menuHidden(MenuEvent e) {
            }
            public void menuShown(MenuEvent e) {
                if (enableOpenButton()) {
                    if (menuItem != null) {
                        menuItem.dispose();
                    }
                    menuItem = new MenuItem(popupmenu, SWT.NONE);
                    menuItem.setText(Messages.getString("Open local file"));
                    menuItem.addListener(SWT.Selection, new Listener() {
                        public void handleEvent(Event e) {
                            EventBus.send(new OpenSelectedFileEvent(this));
                        }
                    });
                } else {
                    if (menuItem != null) {
                        menuItem.dispose();
                    }
                }
            }
        });
        containerWidget.getViewer().getControl().setMenu(popupmenu);
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
    
    class HoverForToolTipListener implements Listener {

        private Shell tip;
        private Label label;
        private TreeViewer treeViewer;
        
        HoverForToolTipListener(TreeViewer treeViewer) {
            this.treeViewer = treeViewer;
        }
        
        /**
         * TreeView doesn't directly support tooltips, so we need to simulate
         * them. Also see https://bugs.eclipse.org/bugs/attachment.cgi?id=53988
         */
        public void handleEvent (Event event) {
            if (!(containerWidget instanceof WorkspaceBrowserWidget)) {
                return;
            }
            switch (event.type) {
                case SWT.Dispose:
                case SWT.KeyDown:
                case SWT.MouseMove: {
                    if (tip == null) break;
                    tip.dispose ();
                    tip = null;
                    label = null;
                    break;
                }
                case SWT.MouseHover: {
                    Point coords = new Point(event.x, event.y);
                    TreeItem item = treeViewer.getTree().getItem(coords);
                    if (item != null) {
                        int columns = treeViewer.getTree().getColumnCount();

                        for (int i = 0; i < columns || i == 0; i++) {
                            if (item.getBounds(i).contains(coords)) {
                                if (tip != null && !tip.isDisposed ()) { 
                                    tip.dispose();
                                }
                                
                                WorkspaceBrowserWidget browserWidget = (WorkspaceBrowserWidget) containerWidget;
                                File file = (File)item.getData();
                                Change change = null;
                                if (browserWidget.changes.getFiles().contains(file)) {
                                    change = browserWidget.changes.getChange(file);
                                }
                                
                                ModificationDescription modDescription = 
                                    ModificationDescription.getDescription(change);
                                
                                String tooltip = modDescription.getDescription();
                                if (tooltip != null && !tooltip.equals("")) {
                                    tip = new Shell(treeViewer.getTree().getShell(), SWT.ON_TOP | SWT.NO_FOCUS | SWT.TOOL);
                                    tip.setBackground(treeViewer.getTree().getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
                                    FillLayout layout = new FillLayout();
                                    layout.marginWidth = 2;
                                    tip.setLayout(layout);
                                    label = new Label(tip, SWT.NONE);
                                    label.setForeground(treeViewer.getTree().getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
                                    label.setBackground(treeViewer.getTree().getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
                                    label.setData("_TABLEITEM", item);
                                    label.setText(tooltip);
                                    //TODO: do we need these?
                                    //label.addListener (SWT.MouseExit, labelListener);
                                    //label.addListener (SWT.MouseDown, labelListener);
                                    Point size = tip.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                                    Rectangle rect = item.getBounds(i);
                                    Point pt = treeViewer.getTree().toDisplay(rect.x, rect.y);
                                    tip.setBounds(pt.x, pt.y, size.x, size.y);
                                    tip.setVisible(true);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
