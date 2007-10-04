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
package com.mindquarry.desktop.workspace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tigris.subversion.javahl.ChangePath;
import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.LogMessage;
import org.tigris.subversion.javahl.NodeKind;
import org.tigris.subversion.javahl.PropertyData;
import org.tigris.subversion.javahl.Revision;
import org.tigris.subversion.javahl.Status;
import org.tigris.subversion.javahl.StatusKind;
import org.tmatesoft.svn.core.javahl.SVNClientImpl;

import com.mindquarry.desktop.util.FileHelper;
import com.mindquarry.desktop.workspace.conflict.AddConflict;
import com.mindquarry.desktop.workspace.conflict.Conflict;
import com.mindquarry.desktop.workspace.conflict.ContentConflict;
import com.mindquarry.desktop.workspace.conflict.DeleteWithModificationConflict;
import com.mindquarry.desktop.workspace.conflict.ObstructedConflict;
import com.mindquarry.desktop.workspace.conflict.PropertyConflict;
import com.mindquarry.desktop.workspace.conflict.ReplaceConflict;
import com.mindquarry.desktop.workspace.exception.CancelException;

/**
 * Helper class for conflict analysis.
 */
public class ConflictHelper {
    private static final Log log = LogFactory.getLog(ConflictHelper.class);

    
    /**
     * Finds all local files that are obstructed (ie. file changed into a folder
     * or vice-versa).
     */
    public static List<Conflict> findLocalObstructed(List<Status> localChanges) {
        List<Conflict> conflicts = new ArrayList<Conflict>();
        Iterator<Status> iter = localChanges.iterator();

        while (iter.hasNext()) {
            Status status = iter.next();

            // local OBSTRUCTED
            if (status.getTextStatus() == StatusKind.obstructed) {
                iter.remove();

                conflicts.add(new ObstructedConflict(status));
            }
        }
        return conflicts;
    }

    /**
     * Finds all local files that are marked as (content-) conflicted.
     */
    public static List<Conflict> findLocalConflicted(List<Status> localChanges) {
        List<Conflict> conflicts = new ArrayList<Conflict>();
        Iterator<Status> iter = localChanges.iterator();

        while (iter.hasNext()) {
            Status status = iter.next();

            // local CONFLICTED, remote MODIFIED
            if (status.getTextStatus() == StatusKind.conflicted) {
                iter.remove();

                conflicts.add(new ContentConflict(status));
            }
        }
        return conflicts;
    }

    /**
     * Finds all files that will be marked as (content-) conflicted after
     * update.
     */
    public static List<Conflict> findIncomingConflicted(
            List<Status> remoteAndLocalChanges) {
        List<Conflict> conflicts = new ArrayList<Conflict>();
        Iterator<Status> iter = remoteAndLocalChanges.iterator();

        while (iter.hasNext()) {
            Status status = iter.next();

            // local MODIFIED, remote MODIFIED
            if (status.getTextStatus() == StatusKind.modified
                    && status.getRepositoryTextStatus() == StatusKind.modified) {
                iter.remove();

                conflicts.add(new ContentConflict(status));
            }
        }
        return conflicts;
    }

    /**
     * Finds all Add/Add conflicts, including file/file, file/dir, dir/file and
     * dir/dir conflicts.
     */
    public static List<Conflict> findAddConflicts(List<Status> remoteAndLocalChanges) {
        List<Conflict> conflicts = new ArrayList<Conflict>();

        Iterator<Status> iter = remoteAndLocalChanges.iterator();
        while (iter.hasNext()) {
            Status status = iter.next();

            // local ADD / UNVERSIONED / IGNORED /EXTERNAL
            // (as we added everything, unversioned shouldn't happen)
            // and remote ADD
            if ((status.getTextStatus() == StatusKind.added
                    || status.getTextStatus() == StatusKind.unversioned
                    || status.getTextStatus() == StatusKind.ignored || status
                    .getTextStatus() == StatusKind.external)
                    && status.getRepositoryTextStatus() == StatusKind.added) {

                Status conflictParent = status;
                // we remove all files/stati connected to this conflict
                iter.remove();

                List<Status> localAdded = new ArrayList<Status>();
                List<Status> remoteAdded = new ArrayList<Status>();

                // find all children (locally and remotely)
                while (iter.hasNext()) {
                    status = iter.next();
                    if (FileHelper.isParent(conflictParent.getPath(), status
                            .getPath())) {
                        if (status.getTextStatus() == StatusKind.added
                                || status.getTextStatus() == StatusKind.unversioned
                                || status.getTextStatus() == StatusKind.ignored
                                || status.getTextStatus() == StatusKind.external) {
                            localAdded.add(status);
                        } else if (status.getRepositoryTextStatus() == StatusKind.added) {
                            remoteAdded.add(status);
                        }
                        iter.remove();
                    } else {
                        // no more children found, this conflict is done
                        // reset global iterator for next conflict search
                        iter = remoteAndLocalChanges.iterator();
                        break;
                    }
                }
                conflicts.add(new AddConflict(conflictParent, localAdded,
                        remoteAdded));
            }
        }
        return conflicts;
    }

    /**
     * Finds all conflicts where a local folder delete conflicts with remotely
     * added or modified files in that directory.
     * 
     * @throws ClientException
     *             if getting the log fails.
     */
    public static List<Conflict> findLocalContainerDeleteConflicts(
            SVNClientImpl client, List<Status> remoteAndLocalChanges) throws ClientException {
        List<Conflict> conflicts = new ArrayList<Conflict>();

        Iterator<Status> iter = remoteAndLocalChanges.iterator();

        // remember any deleted dirs we have already handled to
        // avoid an endless recursion of the main while loop
        Set<Status> handledDeletedDirs = new HashSet<Status>();

        while (iter.hasNext()) {
            Status status = iter.next();

            // DELETED DIRECTORIES (locally)
            if (status.getNodeKind() == NodeKind.dir
                    && status.getTextStatus() == StatusKind.deleted
                    && !handledDeletedDirs.contains(status)) {

                // conflict if there is a child that is added or removed
                // remotely

                Status conflictParent = status;
                handledDeletedDirs.add(conflictParent);

                List<Status> remoteModList = new ArrayList<Status>();

                // find all children (using status)
                while (iter.hasNext()) {
                    status = iter.next();
                    if (FileHelper.isParent(conflictParent.getPath(), status
                            .getPath())) {
                        // Note: if something is locally deleted or missing, the
                        // remote status
                        // will always be 'added' - to detect things that were
                        // actually added
                        // remotely we need to have a local status of 'none'
                        if ((status.getTextStatus() == StatusKind.none && status
                                .getRepositoryTextStatus() == StatusKind.added)
                                || status.getRepositoryTextStatus() == StatusKind.replaced
                                || status.getRepositoryTextStatus() == StatusKind.modified) {
                            remoteModList.add(status);
                        }
                        iter.remove();
                    } else {
                        // no more children found, this conflict is done
                        break;
                    }
                }

                // find all children (extended, using log)
                Map<String, String> modifiedFiles = new HashMap<String, String>();
                log.debug("logMessages '" + conflictParent.getPath() + "'...");
                // FIXME: this may return "M" for files which are not below
                // conflictParent and thus display a conflict although
                // there isn't any. (sorry, don't know how exactly to reproduce 
                // this, try: modify a file on the remote side in the toplevel
                // directory, then move a non-toplevel directory locally
                // and display changes):
                LogMessage[] messages = client.logMessages(conflictParent
                        .getPath(), Revision.BASE, Revision.HEAD, false, true);
                for (LogMessage message : messages) {
                    for (ChangePath changePath : message.getChangedPaths()) {
                        log.debug("SVN Log R" + message.getRevisionNumber()
                                + "> " + changePath.getAction() + " "
                                + changePath.getPath());

                        // keep a history of the status
                        String history = changePath.getAction() + "";
                        if (modifiedFiles.containsKey(changePath.getPath())) {
                            history = modifiedFiles.get(changePath.getPath())
                                    + history;
                        }
                        modifiedFiles.put(changePath.getPath(), history);
                    }
                }

                // // Some tests for making sure that only files that were
                // modified
                // // are found
                // String[] tests = new String[] {
                // "M", "MM", "MMM", "MMMM", // modified
                // "A", "AM", "AMM", "ADA", "AMDAM", "AR", // added -> ignore
                // "D", "MD", "MMD", "DAD", "MDAMD", "RD", // deleted -> ignore
                // "R", "MRM", "MDAM", "DA", "DADA", // replace -> ignore
                // "AMD", // none -> ignore
                // };
                // for(String test : tests) {
                // modifiedFiles.put(test, test);
                // }

                // find files that were modified remotely (and not deleted,
                // added, replaced and various combinations thereof)
                for (String path : modifiedFiles.keySet()) {
                    if (modifiedFiles.get(path).matches("M+")) {
                        log.debug("found remote modification: " + path);
                        remoteModList.add(new Status(path, null,
                                NodeKind.unknown, -1, -1, -1, "",
                                StatusKind.deleted, StatusKind.none,
                                StatusKind.modified, StatusKind.none, false,
                                false, null, null, null, null, -1, false, null,
                                null, null, -1, null, -1, -1, NodeKind.unknown,
                                null));
                    }
                }

                if (remoteModList.size() > 0) {
                    // also remove the deleted folder status object
                    remoteAndLocalChanges.remove(conflictParent);

                    conflicts.add(new DeleteWithModificationConflict(true,
                            conflictParent, remoteModList));
                }

                // reset global iterator for next conflict search
                iter = remoteAndLocalChanges.iterator();
            }
        }

        return conflicts;
    }

    /**
     * Finds all conflicts where a remote folder delete conflicts with locally
     * added or modified files in that directory.
     */
    public static  List<Conflict> findRemoteContainerDeleteConflicts(
            List<Status> remoteAndLocalChanges) {
        List<Conflict> conflicts = new ArrayList<Conflict>();

        Iterator<Status> iter = remoteAndLocalChanges.iterator();

        // remember any deleted dirs we have already handled to
        // avoid an endless recursion of the main while loop
        Set<Status> handledDeletedDirs = new HashSet<Status>();

        while (iter.hasNext()) {
            Status status = iter.next();

            // DELETED DIRECTORIES (remotely)
            if (status.getNodeKind() == NodeKind.dir
                    && status.getRepositoryTextStatus() == StatusKind.deleted
                    && !handledDeletedDirs.contains(status)) {

                // conflict if there is a child that is added or removed locally

                Status conflictParent = status;
                handledDeletedDirs.add(conflictParent);

                List<Status> localModList = new ArrayList<Status>();

                // find all children
                while (iter.hasNext()) {
                    status = iter.next();
                    if (FileHelper.isParent(conflictParent.getPath(), status
                            .getPath())) {
                        if (status.getTextStatus() == StatusKind.added
                                || status.getTextStatus() == StatusKind.replaced
                                || status.getTextStatus() == StatusKind.modified) {
                            localModList.add(status);
                        }
                        iter.remove();
                    } else {
                        // no more children found, this conflict is done
                        break;
                    }
                }
                if (localModList.size() > 0) {
                    // also remove the deleted folder status object
                    remoteAndLocalChanges.remove(conflictParent);

                    conflicts.add(new DeleteWithModificationConflict(false,
                            conflictParent, localModList));
                }
                // reset global iterator for next conflict search
                iter = remoteAndLocalChanges.iterator();
            }
        }
        return conflicts;
    }

    /**
     * Finds all conflicts where a locally deleted file conflicts with a remote
     * file modification.
     */
    public static List<Conflict> findFileDeleteModifiedConflicts(
            List<Status> remoteAndLocalChanges) {
        List<Conflict> conflicts = new ArrayList<Conflict>();

        Iterator<Status> iter = remoteAndLocalChanges.iterator();
        while (iter.hasNext()) {
            Status status = iter.next();

            // local DELETE
            if (status.getNodeKind() == NodeKind.file
                    && status.getTextStatus() == StatusKind.deleted) {

                // if remote MOD
                if (status.getRepositoryTextStatus() == StatusKind.modified) {
                    iter.remove();

                    conflicts.add(new DeleteWithModificationConflict(true,
                            status, null));
                }
            }
        }
        return conflicts;
    }

    /**
     * Finds all conflicts where a locally modified file conflicts with a remote
     * file deletion.
     */
    public static List<Conflict> findFileModifiedDeleteConflicts(
            List<Status> remoteAndLocalChanges) {
        List<Conflict> conflicts = new ArrayList<Conflict>();

        Iterator<Status> iter = remoteAndLocalChanges.iterator();
        while (iter.hasNext()) {
            Status status = iter.next();

            // remote DELETE
            if (status.getNodeKind() == NodeKind.file
                    && status.getRepositoryTextStatus() == StatusKind.deleted) {

                // if local MOD
                if (status.getTextStatus() == StatusKind.modified) {
                    iter.remove();

                    conflicts.add(new DeleteWithModificationConflict(false,
                            status, null));
                }
            }
        }
        return conflicts;
    }

    /**
     * Finds all conflicts where a locally replaced folder conflicts with a
     * remote modification of (in) that folder.
     */
    public static List<Conflict> findLocalContainerReplacedConflicts(
            List<Status> remoteAndLocalChanges) {
        List<Conflict> conflicts = new ArrayList<Conflict>();

        Iterator<Status> iter = remoteAndLocalChanges.iterator();

        // remember any replaced dirs we have already handled to
        // avoid an endless recursion of the main while loop
        Set<Status> handledReplacedDirs = new HashSet<Status>();
        while (iter.hasNext()) {
            Status status = iter.next();

            // REPLACED DIRECTORIES (locally)
            if (status.getNodeKind() == NodeKind.dir
                    && status.getTextStatus() == StatusKind.replaced
                    && !handledReplacedDirs.contains(status)) {

                // conflict if there is a modification inside the directory
                // remotely

                Status conflictParent = status;
                handledReplacedDirs.add(conflictParent);

                List<Status> localChildren = new ArrayList<Status>();
                List<Status> remoteChildren = new ArrayList<Status>();

                // find all children
                while (iter.hasNext()) {
                    status = iter.next();
                    if (FileHelper.isParent(conflictParent.getPath(), status
                            .getPath())) {
                        if (status.getRepositoryTextStatus() == StatusKind.added
                                || status.getRepositoryTextStatus() == StatusKind.replaced
                                || status.getRepositoryTextStatus() == StatusKind.modified
                                || status.getRepositoryTextStatus() == StatusKind.deleted) {
                            remoteChildren.add(status);
                        } else {
                            localChildren.add(status);
                        }
                        // TODO: some might have to be added to both local and
                        // remote
                        iter.remove();
                    } else {
                        // no more children found, this conflict is done
                        break;
                    }
                }
                if (remoteChildren.size() > 0) {
                    // also remove the deleted folder status object
                    remoteAndLocalChanges.remove(conflictParent);

                    conflicts.add(new ReplaceConflict(conflictParent,
                            localChildren, remoteChildren));
                }
                // reset global iterator for next conflict search
                iter = remoteAndLocalChanges.iterator();
            }
        }
        return conflicts;
    }

    /**
     * Finds all conflicts where a remotely replaced folder conflicts with a
     * local modification of (in) that folder.
     */
    public static List<Conflict> findRemoteContainerReplacedConflicts(
            List<Status> remoteAndLocalChanges) {
        List<Conflict> conflicts = new ArrayList<Conflict>();

        Iterator<Status> iter = remoteAndLocalChanges.iterator();

        // remember any replaced dirs we have already handled to
        // avoid an endless recursion of the main while loop
        Set<Status> handledReplacedDirs = new HashSet<Status>();

        while (iter.hasNext()) {
            Status status = iter.next();

            // REPLACED DIRECTORIES (remotely)
            if (status.getReposKind() == NodeKind.dir
                    && status.getRepositoryTextStatus() == StatusKind.replaced
                    && !handledReplacedDirs.contains(status)) {

                // conflict if there is a modification inside the directory
                // locally

                Status conflictParent = status;
                handledReplacedDirs.add(conflictParent);

                List<Status> localChildren = new ArrayList<Status>();
                List<Status> remoteChildren = new ArrayList<Status>();

                // find all children
                while (iter.hasNext()) {
                    status = iter.next();
                    if (FileHelper.isParent(conflictParent.getPath(), status
                            .getPath())) {
                        if (status.getTextStatus() != StatusKind.none
                                && status.getTextStatus() != StatusKind.normal) {
                            localChildren.add(status);
                        } else {
                            remoteChildren.add(status);
                        }
                        // TODO: some might have to be added to both local and
                        // remote
                        iter.remove();
                    } else {
                        // no more children found, this conflict is done
                        break;
                    }
                }
                if (localChildren.size() > 0) {
                    // also remove the deleted folder status object
                    remoteAndLocalChanges.remove(conflictParent);

                    conflicts.add(new ReplaceConflict(conflictParent,
                            localChildren, remoteChildren));
                }
                // reset global iterator for next conflict search
                iter = remoteAndLocalChanges.iterator();
            }
        }
        return conflicts;
    }

    /**
     * Finds all Replaced/Modified, Modified/Replaced and Replaced/Replaced
     * conflicts.
     */
    public static List<Conflict> findReplacedModifiedConflicts(
            List<Status> remoteAndLocalChanges) {
        List<Conflict> conflicts = new ArrayList<Conflict>();

        Iterator<Status> iter = remoteAndLocalChanges.iterator();

        while (iter.hasNext()) {
            Status status = iter.next();

            // local REPLACE remote MOD, local MOD remote REPLACE
            // or locale REPLACE remote REPLACE
            if ((status.getTextStatus() == StatusKind.replaced && status
                    .getRepositoryTextStatus() == StatusKind.modified)
                    || (status.getTextStatus() == StatusKind.modified && status
                            .getRepositoryTextStatus() == StatusKind.replaced)
                    || (status.getTextStatus() == StatusKind.replaced && status
                            .getRepositoryTextStatus() == StatusKind.replaced)) {

                Status conflictParent = status;
                // we remove all files/stati connected to this conflict
                iter.remove();

                List<Status> localChildren = new ArrayList<Status>();
                List<Status> remoteChildren = new ArrayList<Status>();

                // find all children (locally and remotely)
                while (iter.hasNext()) {
                    status = iter.next();
                    if (FileHelper.isParent(conflictParent.getPath(), status
                            .getPath())) {
                        if (status.getTextStatus() != StatusKind.normal) {
                            localChildren.add(status);
                        }
                        if (status.getRepositoryTextStatus() != StatusKind.normal) {
                            remoteChildren.add(status);
                        }
                        iter.remove();
                    } else {
                        // no more children found, this conflict is done
                        // reset global iterator for next conflict search
                        iter = remoteAndLocalChanges.iterator();
                        break;
                    }
                }
                conflicts.add(new ReplaceConflict(conflictParent,
                        localChildren, remoteChildren));
            }
        }
        return conflicts;
    }

    /**
     * @throws CancelException
     * @throws ClientException
     * 
     */
    public static List<Conflict> findPropertyConflicts(SVNClientImpl client,
            List<Status> remoteAndLocalChanges) throws ClientException {
        List<Conflict> conflicts = new ArrayList<Conflict>();

        Iterator<Status> iter = remoteAndLocalChanges.iterator();
        while (iter.hasNext()) {
            Status status = iter.next();

            // LOCAL property status can be any one of those:
            // none
            // normal
            // modified
            // conflicted

            // REMOTE property status can be only the following:
            // none
            // normal
            // modified
            if (status.getRepositoryPropStatus() == StatusKind.modified
                    && status.getPropStatus() == StatusKind.modified) {
                PropertyData[] remoteProps = client.properties(status.getUrl());
                PropertyData[] localProps = client.properties(status.getPath());

                for (PropertyData localProp : localProps) {
                    for (PropertyData remoteProp : remoteProps) {
                        if (localProp.getName().equals(remoteProp.getName())
                                && !localProp.getValue().equals(
                                        remoteProp.getValue())) {
                            log.info("found conflicting property "
                                    + localProp.getName());

                            // TODO add further mergeable properties (e.g.
                            // mq:tags for Tagging)
                            if (localProp.getName().equals(PropertyData.IGNORE)
                                    || localProp.getName().equals(
                                            PropertyData.EXTERNALS)) {
                                conflicts.add(new PropertyConflict(status,
                                        localProp, remoteProp, true));
                            } else {
                                conflicts.add(new PropertyConflict(status,
                                        localProp, remoteProp, false));
                            }
                            break;
                        }
                    }
                }
            }
        }
        return conflicts;
    }
}
