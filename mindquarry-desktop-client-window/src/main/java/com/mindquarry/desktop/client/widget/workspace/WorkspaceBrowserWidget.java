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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TreeItem;
import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.Status;
import org.tigris.subversion.javahl.StatusKind;

import com.mindquarry.desktop.client.Messages;
import com.mindquarry.desktop.client.MindClient;
import com.mindquarry.desktop.client.action.workspace.InteractiveConflictHandler;
import com.mindquarry.desktop.client.widget.util.container.ContainerWidget;
import com.mindquarry.desktop.client.widget.util.container.ErrorWidget;
import com.mindquarry.desktop.client.widget.util.container.NoContentWidget;
import com.mindquarry.desktop.client.widget.util.container.UpdateWidget;
import com.mindquarry.desktop.model.team.Team;
import com.mindquarry.desktop.preferences.profile.Profile;
import com.mindquarry.desktop.workspace.SVNSynchronizer;

/**
 * @author <a href="saar(at)mindquarry(dot)com">Alexander Saar</a>
 */
public class WorkspaceBrowserWidget extends ContainerWidget {
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

    private File workspaceRoot;

    Map<File, Integer> localChanges = new HashMap<File, Integer>();
    Map<File, Integer> remoteChanges = new HashMap<File, Integer>();

    public WorkspaceBrowserWidget(Composite parent, MindClient client) {
        super(parent, SWT.NONE, client);
    }

    // #########################################################################
    // ### PUBLIC METHODS
    // #########################################################################

    /**
     * Runs task update in a separate thread, so that GUI can continue
     * processing. Thus this method returns immediately. While updating tasks
     * the Task Manager will show an update widget instead of the task table.
     */
    public void asyncRefresh() {
        log.info("Starting async workspace changes refresh."); //$NON-NLS-1$
        if (refreshing) {
            log.info("Already refreshing, nothing to do."); //$NON-NLS-1$
            return;
        }
        refreshing = true;
        Thread updateThread = new Thread(new Runnable() {
            public void run() {
                client.startAction(Messages
                        .getString("Refreshing workspace changes"));
                refresh();
                client.stopAction(Messages
                        .getString("Refreshing workspace changes"));
            }
        }, "workspace-changes-update");
        updateThread.setDaemon(true);
        updateThread.start();
    }

    // #########################################################################
    // ### PRIVATE METHODS
    // #########################################################################

    /**
     * Checks if a refresh of the changes list itself is needed.
     */
    public boolean refreshNeeded() {
        PreferenceStore store = client.getPreferenceStore();
        Profile selectedProfile = Profile.getSelectedProfile(store);
        if (selectedProfile == null) {
            log.debug("No profile selected."); //$NON-NLS-1$
            return false;
        }
        Map<File, Integer> newLocalChanges = new HashMap<File, Integer>();
        Map<File, Integer> newRemoteChanges = new HashMap<File, Integer>();
        getAllChanges(selectedProfile, newLocalChanges, newRemoteChanges);
        if (newLocalChanges.equals(localChanges)
                && newRemoteChanges.equals(remoteChanges)) {
            log.debug("Changes list does not need update");
            return false;
        }
        log.debug("Changes list needs update");
        return true;
    }

    public void refresh() {
        PreferenceStore store = client.getPreferenceStore();
        Profile selectedProfile = Profile.getSelectedProfile(store);
        if (selectedProfile == null) {
            log.debug("No profile selected."); //$NON-NLS-1$
            return;
        }
        updateContainer(true, null, false);
        Map<File, Integer> newLocalChanges = new HashMap<File, Integer>();
        Map<File, Integer> newRemoteChanges = new HashMap<File, Integer>();
        getAllChanges(selectedProfile, newLocalChanges, newRemoteChanges);
        localChanges = newLocalChanges;
        remoteChanges = newRemoteChanges;
        workspaceRoot = new File(selectedProfile.getWorkspaceFolder());

        boolean empty = localChanges.size() == 0 && remoteChanges.size() == 0;
        updateContainer(false, null, empty);
        refreshing = false;
    }

    private void getAllChanges(Profile selected,
            Map<File, Integer> localChanges, Map<File, Integer> remoteChanges) {
        try {
            final List<Team> selectedTeams = new ArrayList<Team>();
            Display.getDefault().syncExec(new Runnable() {
                public void run() {
                    selectedTeams.addAll(client.getSelectedTeams());
                }
            });
            for (Team team : selectedTeams) {
                String url = team.getWorkspaceURL();
                String folder = selected.getWorkspaceFolder() + "/"
                        + team.getName();
                String login = selected.getLogin();
                String password = selected.getPassword();

                // iterate over local changes first to avoid exceptions
                // later (in rare cases of "obstructed" state only):
                File teamDir = new File(folder);
                // TODO: also consider the case where the team folder exists
                // but is not a SVN checkout
                if (!teamDir.exists()) {
                    continue;
                }
                SVNSynchronizer sc = new SVNSynchronizer(url, folder, login,
                        password, new InteractiveConflictHandler(client
                                .getShell()));
                List<Status> tmpLocalChanges = sc.getLocalChanges();
                for (Status status : tmpLocalChanges) {
                    if (status.getTextStatus() == StatusKind.obstructed) {
                        localChanges.put(new File(status.getPath()),
                                StatusKind.obstructed);
                    }
                }
                // we need to stop here in case of obstruction,
                // as getRemoteAndLocalChanges() would throw a
                // ClientException:
                if (localChanges.size() == 0) {
                    List<Status> allChanges = sc.getRemoteAndLocalChanges();
                    for (Status status : allChanges) {
                        System.err.println("+ " + status.getPath());
                        localChanges.put(new File(status.getPath()),
                                new Integer(status.getTextStatus()));
                        remoteChanges.put(new File(status.getPath()),
                                new Integer(status.getRepositoryTextStatus()));
                        // TODO: get the two files (remote/local) from the
                        // conflict,
                        // e.g. conflict.txt.r172 etc -> hide them!
                    }
                } else {
                    log
                            .info("obstructed status, not calling getRemoteAndLocalChanges()");
                }
                // FIXME:
                System.err.println("local " + localChanges);
                System.err.println("remote " + remoteChanges);
            }
            workspaceRoot = new File(selected.getWorkspaceFolder());
            updateContainer(false, null, false);
            refreshing = false;
        } catch (ClientException e) {
            // TODO: handle exception
            // may happen on very first checkout (before checkout, actually)
            // log.error(e.toString(), e);
            log.error(e.toString());
        }
    }

    private void updateContainer(final boolean refreshing,
            final String errMessage, final boolean empty) {
        final WorkspaceBrowserWidget self = this;
        getDisplay().syncExec(new Runnable() {
            public void run() {
                if (refreshing) {
                    destroyContent();
                    refreshWidget = new UpdateWidget(self, Messages
                            .getString("Synchronizing workspaces...")); //$NON-NLS-1$
                } else if (errMessage == null && !empty) {
                    destroyContent();

                    // create workspace/changes browser
                    viewer = new TreeViewer(self, SWT.BORDER | SWT.H_SCROLL
                            | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.CHECK);
                    viewer.setContentProvider(new ContentProvider(self));
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
                    viewer.getTree().setLayoutData(
                            new GridData(GridData.FILL_BOTH));
                    viewer.getTree().setHeaderVisible(true);
                    viewer.getTree().setLinesVisible(true);
                    viewer.getTree().setFont(
                            JFaceResources
                                    .getFont(MindClient.TEAM_NAME_FONT_KEY));
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
                    TreeViewerColumn col = new TreeViewerColumn(viewer,
                            SWT.LEFT);
                    col.getColumn().setText("Name");
                    col.getColumn().setWidth(540);
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
                        public Image getImage(Object element) {
                            File file = (File) element;
                            int localStatus = -1;
                            int remoteStatus = -1;
                            if (localChanges != null
                                    && localChanges.containsKey(file)) {
                                localStatus = localChanges.get(file).intValue();
                            }
                            if (remoteChanges != null
                                    && remoteChanges.containsKey(file)) {
                                remoteStatus = remoteChanges.get(file)
                                        .intValue();
                            }
                            if (localStatus == StatusKind.obstructed) {
                                // FIXME: add question mark icon
                            } else if (localStatus == StatusKind.added
                                    || localStatus == StatusKind.unversioned) {
                                // TODO: show upload icon with "+" sign
                                return uploadImage;
                            } else if (remoteStatus == StatusKind.modified) {
                                return uploadImage;
                            } else if (remoteStatus == StatusKind.added) {
                                return downloadImage;
                            } else if (localStatus == StatusKind.conflicted) {
                                return conflictImage;
                            } else if (localStatus != -1 || remoteStatus != -1) {
                                log.warn("No icon set for local/remote status "
                                        + localStatus + "/" + remoteStatus
                                        + " on file " + file.getAbsolutePath());
                            }
                            // TODO: which other cases do we need to display?
                            return null;
                        }

                        public String getText(Object element) {
                            return "";
                        }
                    });
                    viewer.setInput(workspaceRoot);
                    viewer.expandAll();
                    //viewer.getTree().selectAll();
                } else if (errMessage == null && empty) {
                    destroyContent();
                    noContentWidget = new NoContentWidget(self, Messages
                            .getString("No changes to synchronize.")); //$NON-NLS-1$
                } else {
                    destroyContent();
                    errorWidget = new ErrorWidget(self, errMessage);
                }
                layout(true);
            }

            private void destroyContent() {
                if (viewer != null) {
                    viewer.getTree().dispose();
                    viewer = null;
                }
                if (refreshWidget != null) {
                    refreshWidget.dispose();
                    refreshWidget = null;
                }
                if (errorWidget != null) {
                    errorWidget.dispose();
                    errorWidget = null;
                }
                if (noContentWidget != null) {
                    noContentWidget.dispose();
                    noContentWidget = null;
                }
            }
        });
    }
}
