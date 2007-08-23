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
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TreeItem;
import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.Status;
import org.tigris.subversion.javahl.StatusKind;

import com.mindquarry.desktop.client.MindClient;
import com.mindquarry.desktop.client.action.workspace.InteractiveConflictHandler;
import com.mindquarry.desktop.client.widget.WidgetBase;
import com.mindquarry.desktop.model.team.Team;
import com.mindquarry.desktop.preferences.profile.Profile;
import com.mindquarry.desktop.workspace.SVNSynchronizer;

/**
 * @author <a href="saar(at)mindquarry(dot)com">Alexander Saar</a>
 */
public class WorkspaceBrowserWidget extends WidgetBase {
    private static Log log = LogFactory.getLog(WorkspaceBrowserWidget.class);

    private static final Image folderImage = new Image(
            Display.getCurrent(),
            WorkspaceBrowserWidget.class
                    .getResourceAsStream("/org/tango-project/tango-icon-theme/32x32/places/folder.png")); //$NON-NLS-1$

    private static final Image fileImage = new Image(
            Display.getCurrent(),
            WorkspaceBrowserWidget.class
                    .getResourceAsStream("/org/tango-project/tango-icon-theme/32x32/mimetypes/text-x-generic.png")); //$NON-NLS-1$

    private static final Image downloadImage = new Image(
            Display.getCurrent(),
            WorkspaceBrowserWidget.class
                    .getResourceAsStream("/com/mindquarry/icons/32x32/actions/synchronize-down.png")); //$NON-NLS-1$

    private static final Image uploadImage = new Image(
            Display.getCurrent(),
            WorkspaceBrowserWidget.class
                    .getResourceAsStream("/com/mindquarry/icons/32x32/actions/synchronize-up.png")); //$NON-NLS-1$

    private Image conflictImage = new Image(
            Display.getCurrent(),
            getClass()
                    .getResourceAsStream(
                            "/org/tango-project/tango-icon-theme/32x32/status/dialog-warning.png")); //$NON-NLS-1$

    private TreeViewer viewer;

    private Map<File, Integer> localChanges = new HashMap<File, Integer>();
    private Map<File, Integer> remoteChanges = new HashMap<File, Integer>();

    public WorkspaceBrowserWidget(Composite parent, MindClient client) {
        super(parent, SWT.NONE, client);

        // initialize layout
        setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 2,
                1));
        setLayout(new GridLayout(1, true));
        ((GridLayout) getLayout()).horizontalSpacing = 0;
        ((GridLayout) getLayout()).verticalSpacing = 0;
        ((GridLayout) getLayout()).marginHeight = 0;
        ((GridLayout) getLayout()).marginWidth = 0;
    }

    // #########################################################################
    // ### WIDGET METHODS
    // #########################################################################
    protected void createContents(Composite parent) {
        viewer = new TreeViewer(parent, SWT.BORDER | SWT.H_SCROLL
                | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.CHECK);
        viewer.setContentProvider(new TreeContentProvider());
        viewer.setSorter(new ViewerSorter() {
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
        viewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
        viewer.getTree().setHeaderVisible(true);
        viewer.getTree().setLinesVisible(true);
        viewer.getTree().setFont(
                JFaceResources.getFont(MindClient.TEAM_NAME_FONT_KEY));
        viewer.getTree().addListener(SWT.Selection, new Listener() {
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

        TreeViewerColumn col = new TreeViewerColumn(viewer, SWT.LEFT);
        col.getColumn().setText("Name");
        col.getColumn().setWidth(500);
        col.setLabelProvider(new ColumnLabelProvider() {
            public Image getImage(Object element) {
                File file = (File) element;
                if (file.isDirectory()) {
                    return folderImage;
                } else if (file.isFile()) {
                    return fileImage;
                }
                return null;
            }

            public String getText(Object element) {
                return ((File) element).getName();
            }
        });
        col = new TreeViewerColumn(viewer, SWT.CENTER);
        col.getColumn().setResizable(false);
        col.getColumn().setWidth(32);
        col.setLabelProvider(new ColumnLabelProvider() {
            public String getText(Object element) {
                return "";
            }
        });
        col = new TreeViewerColumn(viewer, SWT.CENTER);
        col.getColumn().setResizable(false);
        col.getColumn().setWidth(32);
        col.setLabelProvider(new ColumnLabelProvider() {
            public Image getImage(Object element) {
                File file = (File) element;
                int localStatus = -1;
                int remoteStatus = -1;
                if (localChanges != null && localChanges.containsKey(file)) {
                    localStatus = localChanges.get(file).intValue();
                }
                if (remoteChanges != null && remoteChanges.containsKey(file)) {
                    remoteStatus = remoteChanges.get(file).intValue();
                }
                if (localStatus == StatusKind.obstructed) {
                    //FIXME: add question mark icon
                } else if (localStatus == StatusKind.added || localStatus == StatusKind.unversioned) {
                    // TODO: show upload icon with "+" sign
                    return uploadImage;
                } else if (remoteStatus == StatusKind.modified) {
                    return uploadImage;
                } else if (remoteStatus == StatusKind.added) {
                    return downloadImage;
                } else if (localStatus == StatusKind.conflicted) {
                    return conflictImage;
                } else if (localStatus != -1 || remoteStatus != -1) {
                    log.warn("No icon set for local/remote status " + localStatus + "/"
                            + remoteStatus + " on file " + file.getAbsolutePath());
                }
                // TODO: which other cases do we need to display?
                return null;
            }

            public String getText(Object element) {
                return "";
            }
        });
    }

    // #########################################################################
    // ### PUBLIC METHODS
    // #########################################################################

    public void refresh() {
        try {
            PreferenceStore store = client.getPreferenceStore();
            Profile selected = Profile.getSelectedProfile(store);
            if (selected == null) {
                log.debug("No profile selected."); //$NON-NLS-1$
                return;
            }

            List<Team> selectedTeams = client.getSelectedTeams();
            for (Team team : selectedTeams) {
                SVNSynchronizer sc = new SVNSynchronizer(team.getWorkspaceURL(),
                        selected.getWorkspaceFolder(),
                        selected.getLogin(), selected.getPassword(),
                        new InteractiveConflictHandler(client.getShell()));
                // iterate over local changes first to avoid exceptions
                // later (in rare cases of "obstructed" state only):
                List<Status> tmpLocalChanges = sc.getLocalChanges();
                for (Status status : tmpLocalChanges) {
                    if (status.getTextStatus() == StatusKind.obstructed) {
                        localChanges.put(new File(status.getPath()), StatusKind.obstructed);
                    }
                }
                // we need to stop here in case of obstruction,
                // as getRemoteAndLocalChanges() would throw a
                // ClientException:
                if (localChanges.size() == 0) {
                    List<Status> allChanges = sc.getRemoteAndLocalChanges();
                    for (Status status : allChanges) {
                        localChanges.put(new File(status.getPath()), new Integer(status.getTextStatus()));
                        remoteChanges.put(new File(status.getPath()), new Integer(status.getRepositoryTextStatus()));
                        // TODO: get the two files (remote/local) from the conflict,
                        // e.g. conflict.txt.r172 etc -> hide them!
                    }
                } else {
                    log.info("obstructed status, not calling getRemoteAndLocalChanges()");
                }
                //FIXME:
                System.err.println("local " + localChanges);
                System.err.println("remote " + remoteChanges);
            }

            //viewer.setInput(new File(selected.getWorkspaceFolder()));
            viewer.setInput(new File(selected.getWorkspaceFolder()));
            viewer.expandAll();
        } catch (ClientException e) {
            // TODO: handle exception
            // may happen on very first checkout (before checkout, actually)
            //log.error(e.toString(), e);
            log.error(e.toString());
        }
    }

    // #########################################################################
    // ### PRIVATE METHODS
    // #########################################################################

    // #########################################################################
    // ### NESTED CLASSES
    // #########################################################################
    private final class TreeContentProvider implements ITreeContentProvider {
        public Object[] getChildren(Object parentElement) {
            File workspaceRoot = (File) parentElement;
            File[] children = workspaceRoot.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    File f = new File(dir, name);
                    // show only changed files, but within their directory structure:
                    if (f.isDirectory()) {
                        // if there is at least one change below this directory,
                        // show it, otherwise don't:
                        if (containsChange(f)) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                    if (!localChanges.containsKey(f) && !remoteChanges.containsKey(f)) {
                        return false;
                    }
                    if (name.equals(".svn") || name.equals(".svnref")) {
                        return false;
                    }
                    return true;
                }

                private boolean containsChange(File f) {
                    for (File remoteFile : remoteChanges.keySet()) {
                        if (remoteFile.getAbsolutePath().startsWith(f.getAbsolutePath()+"/")) {
                            return true;
                        }
                    }
                    return false;
                }
            });
            // files may be added remotely:
            List<File> allFiles = new ArrayList<File>();
            allFiles.addAll(Arrays.asList(children));
            if (remoteChanges != null) {
                for (File remoteFile : remoteChanges.keySet()) {
                    if (remoteChanges.containsKey(remoteFile) &&
                            remoteChanges.get(remoteFile) == StatusKind.added &&
                            remoteFile.getParentFile().equals(workspaceRoot)) {
                        allFiles.add(remoteFile);
                    }
                }
            }
            return allFiles.toArray(new File[]{});
        }

        public Object getParent(Object element) {
            File file = (File) element;
            return file.getParent();
        }

        public boolean hasChildren(Object element) {
            File file = (File) element;
            if ((file.isDirectory()) && (file.listFiles().length > 0)) {
                return true;
            }
            return false;
        }

        public Object[] getElements(Object inputElement) {
            return getChildren(inputElement);
        }

        public void dispose() {
            // nothing to do here
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // nothing to do here
        }
    }

}
