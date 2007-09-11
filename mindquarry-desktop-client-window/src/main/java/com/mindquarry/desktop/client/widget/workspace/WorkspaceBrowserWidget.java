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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.NodeKind;
import org.tigris.subversion.javahl.Status;
import org.tigris.subversion.javahl.StatusKind;

import com.mindquarry.desktop.client.Messages;
import com.mindquarry.desktop.client.MindClient;
import com.mindquarry.desktop.client.action.workspace.InteractiveConflictHandler;
import com.mindquarry.desktop.client.action.workspace.OpenFileEvent;
import com.mindquarry.desktop.client.widget.util.container.ContainerWidget;
import com.mindquarry.desktop.event.EventBus;
import com.mindquarry.desktop.event.EventListener;
import com.mindquarry.desktop.model.team.Team;
import com.mindquarry.desktop.preferences.profile.Profile;
import com.mindquarry.desktop.workspace.SVNSynchronizer;

/**
 * Widget displaying incoming and outgoing file changes.
 * 
 * @author <a href="saar(at)mindquarry(dot)com">Alexander Saar</a>
 */
public class WorkspaceBrowserWidget extends ContainerWidget<TreeViewer> implements EventListener {
    private static Log log = LogFactory.getLog(WorkspaceBrowserWidget.class);

    protected File workspaceRoot;

    // FIXME: these maps currently contain all files of all teams - split this
    protected Map<File, Status> localChanges = new HashMap<File, Status>();
    protected Map<File, Status> remoteChanges = new HashMap<File, Status>();
    // ignore files like "<filename>.r200" that are created in case of conflicts:
    protected Map<File, Integer> toIgnore = new HashMap<File, Integer>();

    private WorkspaceUpdateContainerRunnable containerRunnable;

    public WorkspaceBrowserWidget(Composite parent, MindClient client) {
        super(parent, SWT.NONE, client);
        EventBus.registerListener(this);
    }

    // #########################################################################
    // ### PUBLIC METHODS
    // #########################################################################

    /**
     * Checks if a refresh of the changes list itself is needed.
     */
    public boolean refreshNeeded(Boolean applyNewChanges) {
        PreferenceStore store = client.getPreferenceStore();
        Profile selectedProfile = Profile.getSelectedProfile(store);
        if (selectedProfile == null) {
            return false;
        }
        Map<File, Status> newLocalChanges = new HashMap<File, Status>();
        Map<File, Status> newRemoteChanges = new HashMap<File, Status>();
        getAllChanges(selectedProfile, newLocalChanges, newRemoteChanges);
        
        if (changesEqual(newLocalChanges, localChanges, false)
                && changesEqual(newRemoteChanges, remoteChanges, true)) {
            log.debug("Changes list does not need update");
            return false;
        }
        log.debug("Changes list needs update");
        if(applyNewChanges) {
            localChanges = newLocalChanges;
            remoteChanges = newRemoteChanges;
            workspaceRoot = new File(selectedProfile.getWorkspaceFolder());
        }
        return true;
    }
    
    public void onEvent(com.mindquarry.desktop.event.Event event) {
        if (event instanceof OpenFileEvent) {
            ISelection selection = viewer.getSelection();
            if (selection instanceof StructuredSelection) {
                StructuredSelection structsel = (StructuredSelection) selection;
                Object element = structsel.getFirstElement();
                if (element instanceof File) {
                    File file = (File) element;
                    log.debug("Launching " + file.getAbsolutePath());
                    // TODO: this doesn't work with directories
                    Program.launch(file.getAbsolutePath());
                } else {
                    log.warn("onEvent: not of expected type File: " + element.getClass());
                }
            }
        }
    }
    
    public boolean changesEqual(Map<File, Status> first, Map<File, Status> second, boolean remote) {
        if (first == null && second == null) {
            return true;
        }
        if (first == null || second == null) {
            return false;
        }
        if (first.size() != second.size()) {
            return false;
        }
        Set<File> firstKeys = first.keySet();
        Set<File> secondKeys = second.keySet();
        if (!firstKeys.equals(secondKeys)) {
            return false;
        }
        for (File file : firstKeys) {
            Status firstStatus = first.get(file);
            Status secondStatus = second.get(file);
            if (remote) {
                if (firstStatus.getRepositoryTextStatus() != secondStatus.getRepositoryTextStatus()) {
                    return false;
                }
            } else {
                if (firstStatus.getTextStatus() != secondStatus.getTextStatus()) {
                    return false;
                }
            }
        }
        
        return true;
    }

    /**
     * Refreshes the list of incoming and outgoing changes.
     */
    public void refresh() {
        PreferenceStore store = client.getPreferenceStore();
        Profile selectedProfile = Profile.getSelectedProfile(store);
        if (selectedProfile == null) {
            return;
        }
        log.info("Starting workspace changes refresh."); //$NON-NLS-1$
        if (refreshing) {
            log.info("Already refreshing, nothing to do."); //$NON-NLS-1$
            return;
        }
        refreshing = true;
        try {
            // update tree
            Map<File, Status> newLocalChanges = new HashMap<File, Status>();
            Map<File, Status> newRemoteChanges = new HashMap<File, Status>();
            getAllChanges(selectedProfile, newLocalChanges, newRemoteChanges);
            localChanges = newLocalChanges;
            remoteChanges = newRemoteChanges;
            workspaceRoot = new File(selectedProfile.getWorkspaceFolder());
        } finally {
            refreshing = false;
        }
    }

    public boolean isRefreshListEmpty() {
        // FIXME: as ContentProvider does some filtering, this does
        // not always reflect the status in the GUI
        return localChanges.size() == 0 && remoteChanges.size() == 0;
    }
    
    public void setMessage(String message) {
        if (containerRunnable != null) {
            containerRunnable.setMessage(message);
        }
    }
    
    public void setUpdateMessage(String message) {
        if (containerRunnable != null) {
            containerRunnable.setUpdateMessage(message);
        }
    }

    /**
     * Return if at least one team workspace of the current profile has
     * been checked out. Return false on e.g. first start-up.
     */
    public boolean hasCheckout() {
        // TODO: check if there are actually team dirs and
        //  .svn/.svnref dirs/files
        PreferenceStore store = client.getPreferenceStore();
        Profile selected = Profile.getSelectedProfile(store);
        if (selected == null) {
            return false;
        }
        File wsFolder = new File(selected.getWorkspaceFolder());
        if (!wsFolder.exists() || wsFolder.list() == null || 
                wsFolder.list().length == 0) {
            log.debug("folder does not contain a checkout : " + 
                    wsFolder.getAbsolutePath());
            return false;
        }
        return true;
    }
    
    public void showRefreshMessage(String message) {
        updateContainer(true, message, null, false, null);
    }

    public void showEmptyMessage(boolean isEmpty) {
        String emptyMessage = Messages.getString(
                "There are currently no workspace changes to synchronize,\n" +
                "i.e. there are no local changes and there are no changes on the server.\n" +
                "Last refresh: ")
                + new SimpleDateFormat().format(new Date()); //$NON-NLS-1$

        updateContainer(false, null, null, isEmpty, emptyMessage);
    }

    public void showMessage(String message, String icon) {
        containerRunnable = new WorkspaceUpdateContainerRunnable(client, this, false, true, icon, message);
            getDisplay().syncExec(containerRunnable);
    }

    // #########################################################################
    // ### PRIVATE METHODS
    // #########################################################################

    private void updateContainer(final boolean refreshing,
            final String refreshMessage, final String errMessage, boolean empty, String emptyMessage) {
        containerRunnable = new WorkspaceUpdateContainerRunnable(client, this, refreshing,
            refreshMessage, empty, emptyMessage, errMessage);
        getDisplay().syncExec(containerRunnable);
    }

    private void getAllChanges(Profile selected,
            Map<File, Status> localChanges, Map<File, Status> remoteChanges) {
        try {
            log.debug("getting all changes");
            final List<Team> selectedTeams = new ArrayList<Team>();
            Display.getDefault().syncExec(new Runnable() {
                public void run() {
                    selectedTeams.addAll(client.getSelectedTeams());
                }
            });
            for (Team team : selectedTeams) {
                String url = team.getWorkspaceURL();
                log.info("Refreshing for SVN URL: " + url);

                setMessage(Messages.getString(
                        "Refreshing workspaces changes for team \"{0}\" (team {1} of {2}) ...", //$NON-NLS-1$
                        team.getName(),
                        Integer.toString(selectedTeams.indexOf(team)+1),
                        Integer.toString(selectedTeams.size())));
                
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
                sc.cleanup();
                boolean teamHasObstruction = false;
                List<Status> tmpLocalChanges = sc.getLocalChanges();
                for (Status status : tmpLocalChanges) {
                    if (status.getTextStatus() == StatusKind.obstructed) {
                        localChanges.put(new File(status.getPath()), status);
                        teamHasObstruction = true;
                    }
                }
                toIgnore = new HashMap<File, Integer>();
                // we need to stop here in case of obstruction,
                // as getRemoteAndLocalChanges() would throw a
                // ClientException:
                if (!teamHasObstruction) {
                    List<Status> allChanges = sc.getRemoteAndLocalChanges();
                    for (Status status : allChanges) {
                        File f = new File(status.getPath());
                        // we don't know why yet, but files may sometimes be
                        // set to "normal". We ignore these files, i.e. treat
                        // them as non-modified:
                        if ((status.getTextStatus() == StatusKind.none ||
                                status.getTextStatus() == StatusKind.normal) &&
                                (status.getRepositoryTextStatus() == StatusKind.none || 
                                status.getRepositoryTextStatus() == StatusKind.normal)) {
                            continue;
                        }
                        if (status.getTextStatus() == StatusKind.external && (status.getRepositoryTextStatus() == StatusKind.none || status.getRepositoryTextStatus() == StatusKind.external )) {
                        	continue;
                        }
                        localChanges.put(new File(status.getPath()), status);
                        remoteChanges.put(new File(status.getPath()), status);
                        // If we add a directory with contents, SVN will only
                        // report the directory as unversioned, not the files
                        // inside the directory, so we add the files (= all
                        // files
                        // below the directory) here:
                        if (status.getTextStatus() == StatusKind.unversioned
                                && f.isDirectory()) {
                            SetStatusFileFilter fileFilter = new SetStatusFileFilter();
                            SetStatusDirFilter dirFilter = new SetStatusDirFilter();
                            FileUtils.iterateFiles(f, fileFilter, dirFilter);
                            for (Status tmpStatus : fileFilter.getSubFiles()) {
                                localChanges.put(new File(tmpStatus.getPath()), status);
                            }
                        }
                        // if there's a local conflict, ignore (i.e. don't display) files
                        // like <file>.r<rev> or <file>.mine, because these will not be
                        // uploaded to the server:
                        if (status.getTextStatus() == StatusKind.conflicted) {
                            // status.getConflictNew() etc return relative path names,
                            // but we need absolute path names in the map:
                            File dir = new File(status.getPath()).getParentFile();
                            toIgnore.put(new File(dir, status.getConflictNew()), status.getTextStatus());
                            toIgnore.put(new File(dir, status.getConflictOld()), status.getTextStatus());
                            toIgnore.put(new File(dir, status.getConflictWorking()), status.getTextStatus());
                        }
                    }
                } else {
                    log.info("obstructed status, not calling getRemoteAndLocalChanges()");
                }
                log.debug("local changes: " + localChanges);
                log.debug("remote changes: " + remoteChanges);
            }
            workspaceRoot = new File(selected.getWorkspaceFolder());
        } catch (ClientException e) {
            // TODO: handle exception
            // may happen on very first checkout (before checkout, actually)
            // may happen on network timeout
            // may happen on wrong credentials
            // may happen on wrong server url
            
            // list of seen apr errors:
            // 175002: connection refused
            
            log.error(e.toString() + " (apr error " + e.getAprError() + ")", e);
        }
    }

    // #########################################################################
    // ### INNER CLASSES
    // #########################################################################

    /**
     * Use the filter not as a filter, but to iterate over all files recursively.
     */
    class SetStatusFileFilter extends FileFileFilter {
        List<Status> subFiles = new ArrayList<Status>();

        public boolean accept(File file) {
            // make up a Status mostly with fake values, as only
            // path and StatusKind will be used:
            Status status = new Status(file.getAbsolutePath(), null,
                    NodeKind.file, 0, 0, 0, null, StatusKind.unversioned, 0, 0,
                    0, false, false, null, null, null, null, 0, false, null,
                    null, null, 0, null, 0, 0, 0, null);
            subFiles.add(status);
            return true;
        }

        List<Status> getSubFiles() {
            return subFiles;
        }
    }

    class SetStatusDirFilter extends DirectoryFileFilter {
        List<Status> subFiles = new ArrayList<Status>();

        public boolean accept(File file) {
            Status status = new Status(file.getAbsolutePath(), null,
                    NodeKind.dir, 0, 0, 0, null, StatusKind.unversioned, 0, 0,
                    0, false, false, null, null, null, null, 0, false, null,
                    null, null, 0, null, 0, 0, 0, null);
            subFiles.add(status);
            return true;
        }

        List<Status> getSubFiles() {
            return subFiles;
        }
    }
}
