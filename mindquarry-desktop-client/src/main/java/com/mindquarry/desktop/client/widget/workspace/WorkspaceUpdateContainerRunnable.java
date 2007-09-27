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
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
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
import com.mindquarry.desktop.client.widget.util.FileIconUtil;
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
        containerWidget.setViewer(new TreeViewer(containerWidget, SWT.BORDER
                | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION));
        containerWidget.getViewer().setContentProvider(
                new ContentProvider((WorkspaceBrowserWidget) containerWidget));

        addContextMenu();

        containerWidget.getViewer().addSelectionChangedListener(
                new ISelectionChangedListener() {
                    public void selectionChanged(SelectionChangedEvent event) {
                        // make sure the "open" button is only enabled when it
                        // makes sense:
                        boolean enableButton = enableOpenButton();
                        client.enableAction(enableButton, OpenFileAction.class
                                .getName());
                    }
                });
        containerWidget.getViewer().addDoubleClickListener(
                new IDoubleClickListener() {
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
        Listener treeListener = new HoverForToolTipListener(containerWidget
                .getViewer());
        containerWidget.getViewer().getTree().addListener(SWT.Dispose,
                treeListener);
        containerWidget.getViewer().getTree().addListener(SWT.KeyDown,
                treeListener);
        containerWidget.getViewer().getTree().addListener(SWT.MouseMove,
                treeListener);
        containerWidget.getViewer().getTree().addListener(SWT.MouseHover,
                treeListener);

        containerWidget.getViewer().getTree().setLayoutData(
                new GridData(GridData.FILL_BOTH));
        containerWidget.getViewer().getTree().setHeaderVisible(false);
        containerWidget.getViewer().getTree().setLinesVisible(false);
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
        
        // first column: file/directory name
        TreeViewerColumn col = new TreeViewerColumn(
                containerWidget.getViewer(), SWT.LEFT);
        col.getColumn().setText("Name");
        col.getColumn().setResizable(false);
        col.getColumn().setWidth(500);
        col.setLabelProvider(new ColumnLabelProvider() {
            public Image getImage(Object element) {
                File file = (File) element;
                WorkspaceBrowserWidget widget = (WorkspaceBrowserWidget) containerWidget;
                if (widget.changeSets != null
                        && widget.changeSets.getFiles().contains(file)) {
                    Status status = widget.changeSets.getStatus(file);
                    return FileIconUtil.getIcon(file, status);
                }
                // fallback: simply lookup the local file
                return FileIconUtil.getIcon(file);
            }

            public String getText(Object element) {
                return ((File) element).getName();
            }
        });

        // second column: direction information (up/down/conflict)
        col = new TreeViewerColumn(containerWidget.getViewer(), SWT.CENTER);
        col.getColumn().setResizable(false);
        col.getColumn().setWidth(32);
        col.setLabelProvider(new ColumnLabelProvider() {
            public Image getImage(Object element) {
                File file = (File) element;
                // lookup the status via the File -> Status maps
                WorkspaceBrowserWidget widget = (WorkspaceBrowserWidget) containerWidget;

                ModificationDescription descr = new ModificationDescription(null);
                if (widget.changeSets != null
                        && widget.changeSets.getFiles().contains(file)) {
                    descr = new ModificationDescription(widget.changeSets.getChange(file));
                }
                return descr.getDirectionImage();
            }

            public String getText(Object element) {
                return "";
            }
        });

        // third column: status information (modified, added, ...)
        col = new TreeViewerColumn(containerWidget.getViewer(), SWT.CENTER);
        col.getColumn().setResizable(false);
        col.getColumn().setWidth(32);
        col.setLabelProvider(new ColumnLabelProvider() {
            public Image getImage(Object element) {
                File file = (File) element;
                // lookup the status via the File -> Status maps
                WorkspaceBrowserWidget widget = (WorkspaceBrowserWidget) containerWidget;
                Change change = widget.changeSets.getChange(file);
                if (change != null && change.getStatus() != null &&
                        change.getStatus().getNodeKind() == NodeKind.dir) {
                    // "modified" etc on directory is too confusing
                    return null;
                }

                ModificationDescription descr = new ModificationDescription(null);
                if (widget.changeSets != null
                        && widget.changeSets.getFiles().contains(file)) {
                    descr = new ModificationDescription(widget.changeSets.getChange(file));
                }
              
                return descr.getStatusImage();
            }

            public String getText(Object element) {
                return "";
            }
        });

        // add auto resizing of tree columns
        containerWidget.getShell().addListener(SWT.Resize, new Listener() {
            public void handleEvent(Event event) {
                if (containerWidget.getViewer() != null) {
                    adjustWidth();
                }
            }
        });
        // set input and trigger refresh
        containerWidget.getViewer().setInput(
                ((WorkspaceBrowserWidget) containerWidget).workspaceRoot);
        containerWidget.getViewer().expandAll();
        checkAllItem(containerWidget.getViewer().getTree().getItems());

        containerWidget.layout(true);

        // set background color for every second table item
        containerWidget.getViewer().getTree().addTreeListener(
                new TreeListener() {
                    public void treeCollapsed(TreeEvent e) {
                        // NOTE: this event is thrown before the actual items
                        // have the expanded value updated! That's why we pass
                        // through the item to handle it differently
                        markRows(containerWidget.getViewer().getTree()
                                .getItems(), 0, (TreeItem) e.item);
                    }

                    public void treeExpanded(TreeEvent e) {
                        markRows(containerWidget.getViewer().getTree()
                                .getItems(), 0, (TreeItem) e.item);
                    }
                });
        adjustWidth();
        markRows(containerWidget.getViewer().getTree().getItems(), 0, null);
    }
    
    /**
     * Marks the rows of a tree in alternating colors (white, highlighted,
     * white, highlighted...).
     * 
     * @param items the tree items to look at
     * @param count number of items visible from the top in a linear order, must
     *              be passed on (and returned)
     * @param expandedOrCollapsedItem the item that is about to expand or
     *              collapse (workaround for a not-yet-set item.getExpanded())
     * @return the current number of lines visited
     */
    private int markRows(TreeItem[] items, int count, TreeItem expandedOrCollapsedItem) {
        for (int i = 0; i < items.length; i++) {
            if (count % 2 == 1) {
                items[i].setBackground(ContainerWidget.HIGHLIGHT_COLOR);
            } else {
                items[i].setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
            }
            count++;
            if (expandedOrCollapsedItem == items[i]) {
                if (!items[i].getExpanded()) {
                    count = markRows(items[i].getItems(), count, expandedOrCollapsedItem);
                }
            } else {
                if (items[i].getExpanded()) {
                    count = markRows(items[i].getItems(), count, expandedOrCollapsedItem);
                }
            }
        }
        return count;
    }

    private void adjustWidth() {
        containerWidget.getViewer().getTree().getColumn(0).setWidth(
                containerWidget.getViewer().getTree().getClientArea().width
                        - getColumnSpace());
    }

    private int getColumnSpace() {
        if (SVNFileUtil.isOSX) {
            return 32+136;
        }
        return 32+36;
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
                    if (file.exists()
                            && (SVNFileUtil.isWindows || SVNFileUtil.isOSX)) {
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
        final Menu popupmenu = new Menu(client.getShell(), SWT.POP_UP);
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
        for (TreeItem item : items) {
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
        public void handleEvent(Event event) {
            if (!(containerWidget instanceof WorkspaceBrowserWidget)) {
                return;
            }
            switch (event.type) {
            case SWT.Dispose:
            case SWT.KeyDown:
            case SWT.MouseMove: {
                if (tip == null)
                    break;
                tip.dispose();
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
                                if (browserWidget.changeSets.getFiles().contains(file)) {
                                    change = browserWidget.changeSets.getChange(file);
                                }
                                
                                ModificationDescription modDescription = new
                                    ModificationDescription(change);
                                
                                String tooltip = modDescription.getLongDescription();
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
