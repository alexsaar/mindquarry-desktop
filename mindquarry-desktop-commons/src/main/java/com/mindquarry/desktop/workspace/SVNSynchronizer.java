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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.CommitMessage;
import org.tigris.subversion.javahl.NodeKind;
import org.tigris.subversion.javahl.Notify2;
import org.tigris.subversion.javahl.NotifyAction;
import org.tigris.subversion.javahl.NotifyInformation;
import org.tigris.subversion.javahl.PropertyData;
import org.tigris.subversion.javahl.Revision;
import org.tigris.subversion.javahl.Status;
import org.tigris.subversion.javahl.StatusKind;
import org.tigris.subversion.javahl.Status.Kind;
import org.tmatesoft.svn.core.javahl.SVNClientImpl;

import com.mindquarry.desktop.util.FileHelper;
import com.mindquarry.desktop.util.MimeTypeUtilities;
import com.mindquarry.desktop.util.RelativePath;
import com.mindquarry.desktop.workspace.conflict.Change;
import com.mindquarry.desktop.workspace.conflict.Conflict;
import com.mindquarry.desktop.workspace.conflict.ConflictHandler;
import com.mindquarry.desktop.workspace.conflict.LocalAddition;
import com.mindquarry.desktop.workspace.conflict.LocalDeletion;
import com.mindquarry.desktop.workspace.conflict.LocalModification;
import com.mindquarry.desktop.workspace.conflict.LocalReplace;
import com.mindquarry.desktop.workspace.conflict.RemoteAddition;
import com.mindquarry.desktop.workspace.conflict.RemoteDeletion;
import com.mindquarry.desktop.workspace.conflict.RemoteModification;
import com.mindquarry.desktop.workspace.conflict.RemoteReplace;
import com.mindquarry.desktop.workspace.exception.CancelException;
import com.mindquarry.desktop.workspace.exception.SynchronizeException;

/**
 * Helper class that implements desktop synchronization using the SVN kit. It
 * provides callback hooks for handling conflicts that need user interaction.
 * 
 * Callback handler for conflict resolving must implement the
 * {@link ConflictHandler} interface. A commit message handler must be set by
 * calling {@link setCommitMessageHandler()}.
 * 
 * @author <a href="mailto:saar@mindquarry.com">Alexander Saar</a>
 * @author <a href="mailto:victor.saar@mindquarry.com">Victor Saar</a>
 * @author <a href="mailto:alexander.klimetschek@mindquarry.com">Alexander
 *         Klimetschek</a>
 */
public class SVNSynchronizer {
    private static final Log log = LogFactory.getLog(SVNSynchronizer.class);

    protected String repositoryURL;

    protected File localPathFile;

    protected String username;

    protected String password;

    protected SVNClientImpl client;

    protected ConflictHandler handler;

    /**
     * Constructor for SVNSynchronizer that has all mandatory fields as
     * parameter.
     * 
     * @param repositoryURL
     *            URL of the central SVN repository
     * @param localPath
     *            local working copy path (typically the root of the wc)
     * @param username
     *            subversion username
     * @param password
     *            subversion password
     * @param handler
     *            callback handler to resolve conflicts in the GUI
     */
    public SVNSynchronizer(String repositoryURL, String localPath,
            String username, String password, ConflictHandler handler) {
        log.debug("Creating SVNSynchronizer for " + repositoryURL
                + ", local path: " + localPath);
        this.repositoryURL = repositoryURL;
        this.username = username;
        this.password = password;
        this.handler = handler;

        this.localPathFile = new File(localPath);

        if (handler == null) {
            throw new NullPointerException(
                    "Constructor parameter ConflictHandler handler cannot be null");
        }

        // create SVN client, set authentication info
        client = SVNClientImpl.newInstance();
        if (username != null) {
            client.username(username);
            if (password != null) {
                client.password(password);
            }
        }
    }

    /**
     * Sets an optional notify listener to get notifications directly from the
     * svn client upon update and commit.
     */
    public void setNotifyListener(Notify2 notifyListener) {
        // register for svn notifications on update and commit
        client.notification2(notifyListener);
    }

    public void setCommitMessageHandler(CommitMessage commitMsgHandler) {
        client.commitMessageHandler(commitMsgHandler);
    }

    /**
     * Like synchronize(), but does a checkout if <tt>localPath</tt> isn't a
     * checkout. Also, creates the path if it doesn't exist.
     * 
     * @throws SynchronizeException
     */
    public void synchronizeOrCheckout() throws SynchronizeException {
        log.debug("synchronizeOrCheckout on "
                        + localPathFile.getAbsolutePath());

        // if directory doesn't exist, create it:
        if (!localPathFile.exists()) {
            boolean createdDir = localPathFile.mkdirs();
            if (!createdDir) {
                throw new RuntimeException("Could not create directory: "
                        + localPathFile.getAbsolutePath());
            }
        }
        if (localPathFile.isFile()) {
            throw new IllegalArgumentException("File where directory "
                    + "was expected: " + localPathFile.getAbsolutePath());
        }

        boolean isCheckedOut = isCheckedOut(localPathFile);
        if (isCheckedOut) {
            // already check out, sync it
            synchronize();
        } else {
            // check if the directories are empty,
            // otherwise we'd try to check out into a directory
            // that contains local files already which causes
            // confusion.
            Iterator iter = FileUtils.iterateFiles(localPathFile,
                    TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
            if (iter.hasNext()) {
                throw new SynchronizeException(
                        "Cannot initially checkout into '"
                                + localPathFile.getAbsolutePath()
                                + "' because it seems not empty.");
            } else {
                try {
                    log.debug("checkout " + repositoryURL + " to "
                            + localPathFile.getAbsolutePath());
                    client.checkout(repositoryURL, localPathFile
                            .getAbsolutePath(), Revision.HEAD, true);
                } catch (ClientException e) {
                    throw new RuntimeException("Checkout of " + repositoryURL
                            + " to " + localPathFile.getAbsolutePath()
                            + " failed", e);
                }
            }
        }
    }

    /**
     * A cleanup is good for removing any old working copy locks (throws only
     * exception if the path does not exist or is not part of a working copy -
     * that has to be checked outside this method)
     * 
     * @throws ClientException
     */
    public void cleanup() throws ClientException {
        log.debug("cleaning up " + localPathFile.getAbsolutePath());
        client.cleanup(localPathFile.getAbsolutePath());
    }

    /**
     * Retrieves local changes for the wc root as a list that is sorted with the
     * top-most folder or file first.
     */
    public List<Status> getLocalChanges() throws ClientException {
        return getLocalChanges(localPathFile);
    }

    /**
     * Important method that returns a list of all changes and conflicts that
     * are to take place when synchronising.
     * 
     * @throws IOException
     */
    public List<Change> getChangesAndConflicts() throws ClientException,
            IOException {
        deleteMissingAndAddUnversioned(localPathFile);

        List<Status> remoteAndLocalChanges = getRemoteAndLocalChanges();
        List<Status> remoteAndLocalChanges2 = new ArrayList<Status>(
                remoteAndLocalChanges);
        log.debug("Analyzing changes and conflicts ...");

        for (Status s : remoteAndLocalChanges) {
            log.debug("analyzing "
                    + SVNSynchronizer.textStatusDesc(s.getTextStatus())
                    + " "
                    + nodeKindDesc(s.getNodeKind())
                    + " <->"
                    + " "
                    + SVNSynchronizer.textStatusDesc(s
                            .getRepositoryTextStatus()) + " "
                    + nodeKindDesc(s.getReposKind()) + " '" + wcPath(s) + "'");
        }
        List<Change> changes = new ArrayList<Change>();

        // LOCAL status can be everything except:
        // none/normal won't be displayed in local changes
        // unversioned/missing set to added/deleted (handled anyway)
        // merged only happens on update
        // ignored can be ignored ;-)
        // incomplete (on dir) missing files are set to deleted

        // LOCAL status can be any one of those:
        // simple ones:
        // modified
        // added
        // deleted
        // replaced (only possible with svn client)
        // hard ones:
        // conflicted
        // obstructed (eg. deleted file, created dir with same name)
        // external (only possible with svn client)

        // REMOTE status can be only the following:
        // none
        // normal
        // modified
        // added
        // deleted
        // replaced (delete and re-add in one step)

        // content conflicts
        changes.addAll(ConflictHelper.findLocalConflicted(remoteAndLocalChanges));
        changes.addAll(ConflictHelper.findIncomingConflicted(remoteAndLocalChanges));

        // replace conflicts
        changes
                .addAll(ConflictHelper.findLocalContainerReplacedConflicts(remoteAndLocalChanges));
        changes
                .addAll(ConflictHelper.findRemoteContainerReplacedConflicts(remoteAndLocalChanges));
        changes.addAll(ConflictHelper.findReplacedModifiedConflicts(remoteAndLocalChanges));

        // add conflicts
        changes.addAll(ConflictHelper.findAddConflicts(remoteAndLocalChanges));

        // delete/modified conflicts
        changes.addAll(ConflictHelper.findLocalContainerDeleteConflicts(client, remoteAndLocalChanges));
        changes.addAll(ConflictHelper.findRemoteContainerDeleteConflicts(remoteAndLocalChanges));
        changes.addAll(ConflictHelper.findFileDeleteModifiedConflicts(remoteAndLocalChanges));
        changes.addAll(ConflictHelper.findFileModifiedDeleteConflicts(remoteAndLocalChanges));

        // property conflicts
        // get up-to-date remote and local changes to get property conflicts of
        // previously removed stati
        changes.addAll(ConflictHelper.findPropertyConflicts(client, remoteAndLocalChanges2));

        // categorize normal changes
        Iterator<Status> iter = remoteAndLocalChanges.iterator();
        while (iter.hasNext()) {
            Status status = iter.next();

            // ----- local -----

            // local addition of files/dirs
            if (status.getTextStatus() == StatusKind.added) {
                iter.remove();
                changes.add(new LocalAddition(new File(status.getPath()),
                        status));
                continue;
            }

            // local deletion of files/dirs
            if (status.getTextStatus() == StatusKind.deleted) {
                iter.remove();
                changes.add(new LocalDeletion(status));
                continue;
            }

            // local modification of files/dirs
            if (status.getTextStatus() == StatusKind.modified) {
                iter.remove();
                changes.add(new LocalModification(status));
                continue;
            }

            // local replace of files/dirs
            if (status.getTextStatus() == StatusKind.replaced) {
                iter.remove();
                changes
                        .add(new LocalReplace(new File(status.getPath()),
                                status));
                continue;
            }
            // ----- remote -----

            // remote addition of files/dirs
            if (status.getRepositoryTextStatus() == StatusKind.added) {
                iter.remove();
                changes.add(new RemoteAddition(new File(status.getPath()),
                        status));
                continue;
            }
            // remote deletion of files/dirs
            if (status.getRepositoryTextStatus() == StatusKind.deleted) {
                iter.remove();
                changes.add(new RemoteDeletion(status));
                continue;
            }
            // remote modification of files/dirs
            if (status.getRepositoryTextStatus() == StatusKind.modified) {
                iter.remove();
                changes.add(new RemoteModification(status));
                continue;
            }
            // remote replace of files/dirs
            if (status.getRepositoryTextStatus() == StatusKind.replaced) {
                iter.remove();
                changes.add(new RemoteReplace(status));
                continue;
            }
        }
        // add normal changes
        log.debug("Detected the following changes:");
        for (Status status : remoteAndLocalChanges) {
            Change change = new Change(status);
            log.debug(change);
            changes.add(change);
        }
        return changes;
    }

    // #########################################################################
    // ### PROTECTED METHODS
    // #########################################################################

    /**
     * Central method: will do a full synchronization, including update and
     * commit. During that the ConflictHandler will be asked. Will fail if
     * there's no checkout yet, see synchronizeOrCheckout(). If the users
     * cancels (i.e. a CancelException is thrown inside a ConflictHandler), the
     * method will end silently.
     * 
     * @throws SynchronizeException
     *             thrown if an unexpected IO, network or SVN error occurs
     */
    protected void synchronize() throws SynchronizeException {
        try {
            log.debug("synchronizing...");
            cleanup();

            // local checks only: conflicted and obstructed
            List<Conflict> localConflicts = analyzeConflictedAndObstructed();
            handleConflictsBeforeRemoteStatus(localConflicts);

            deleteMissingAndAddUnversioned(localPathFile);

            List<Conflict> conflicts = analyzeChangesAndAskUser();

            handleConflictsBeforeUpdate(conflicts);
            log.info("updating " + localPathFile.getAbsolutePath()
                            + " to HEAD");
            client.update(localPathFile.getAbsolutePath(), Revision.HEAD, true);
            handleConflictsAfterUpdate(conflicts);

            localConflicts = analyzeConflicted();
            handleConflictsBeforeCommit(localConflicts);

            // we use the CommitMessage interface as callback
            log.info("committing " + localPathFile.getAbsolutePath());
            client.commit(new String[] { localPathFile.getAbsolutePath() },
                    null, true);

        } catch (CancelException e) {
            log.info("Cancelled");
            throw new SynchronizeException("synchronize() cancelled: "
                    + e.toString(), e);
        } catch (Exception e) {
            // TODO think about exception handling
            log.error(e);
            if (e.getCause() != null) {
                e.getCause().printStackTrace();
            }
            throw new SynchronizeException("synchronize() failed: "
                    + e.toString(), e);
        }
    }

    // #########################################################################
    // ### PRIVATE/INTERNAL METHODS
    // #########################################################################

    private boolean isCheckedOut(File file) {
        try {
            // throws exception if no .svnref or .svn exists
            client.info(file.getAbsolutePath());
        } catch (ClientException e) {
            // probably not a checkout directory:
            log.info("Not a checked out dir, got exception on "
                    + file.getAbsolutePath() + ": " + e);
            return false;
        }
        log.debug("Is a checked out dir:" + file.getAbsolutePath());
        return true;
    }

    private void presentConflictToUser(Conflict conflict)
            throws CancelException {
        conflict.setSVNClient(client);

        log.info("-----------------------------------------------------------");
        log.info("## Found conflict: " + conflict.toString());

        // resolve it, ask the user
        conflict.accept(handler);
    }
    
    /**
     * Retrieves local changes for a wc path as a list that is sorted with the
     * top-most folder or file first.
     */
    private List<Status> getLocalChanges(File file) throws ClientException {
        log.info("## local changes for '" + file.getAbsolutePath() + "':");

        // we need a modifiable list - Arrays.asList is fixed
        List<Status> statusList = new ArrayList<Status>();
        statusList.addAll(Arrays.asList(client.status(file.getAbsolutePath(),
                true, false, false)));

        // sort the list from top-level folder to bottom which is important
        // for handling multiple conflicts on the parent folder first
        Collections.sort(statusList, new StatusComparator());
        for (Status s : statusList) {
            log.info(textStatusDesc(s.getTextStatus()) + " " + s.getPath());
        }
        return statusList;
    }
    
    /**
     * Returns a list with all local and remote changes combined. It's not
     * easily possible to get only the remote changes, that's why we use this
     * combined list throughout the code. The status inside this list will be
     * different from the one returned by getLocalChanges() since it might
     * contain the remote change of the same path. The list will be sorted from
     * top to down.
     */
    private List<Status> getRemoteAndLocalChanges() throws ClientException {
        log.info("## remote changes:");

        // we need a modifiable list - Arrays.asList is fixed
        List<Status> statusList = new ArrayList<Status>();
        statusList.addAll(Arrays.asList(client.status(localPathFile
                .getAbsolutePath(), true, true, false)));

        // sort the list from top-level folder to bottom which is important
        // for handling multiple conflicts on the parent folder first
        Collections.sort(statusList, new StatusComparator());
        for (Status s : statusList) {
            log.info(SVNSynchronizer
                    .textStatusDesc(s.getRepositoryTextStatus())
                    + " " + s.getPath());
        }
        return statusList;
    }

    /**
     * Merge a new value with an existing or non-existing property value.
     */
    private String mergeIgnoreProperty(PropertyData property, String newValue) {
        List<String> mergedValues = new ArrayList<String>();

        // Note: property might be null, as well as property.getValue()
        if (property == null) {
            return newValue;
        }
        String propVal = property.getValue();
        if (propVal == null) {
            return newValue;
        } else {
            mergedValues.addAll(Arrays.asList(propVal.split("\\n|\\r\\n")));
        }
        if (!mergedValues.contains(newValue)) {
            mergedValues.add(newValue);
        }
        StringBuffer buffer = new StringBuffer();
        for (String value : mergedValues) {
            buffer.append(value + "\n");
        }
        return buffer.toString();
    }

    /**
     * This removes the need for calling svn del and svn add manually. It also
     * automatically adds hidden files (such as Thumbs.db on Windows or
     * .something) to the ignore list.
     */
    private void deleteMissingAndAddUnversioned(File base)
            throws ClientException, IOException {
        for (Status s : getLocalChanges(base)) {
            log.debug("deleting/adding/ignoring "
                    + SVNSynchronizer.textStatusDesc(s.getTextStatus())
                    + " "
                    + nodeKindDesc(s.getNodeKind())
                    + " <->"
                    + " "
                    + SVNSynchronizer.textStatusDesc(s
                            .getRepositoryTextStatus()) + " "
                    + nodeKindDesc(s.getReposKind()) + " '" + wcPath(s) + "'");

            if (s.getTextStatus() == StatusKind.missing) {
                // Note: a missing element could either be an already versioned
                // element or something that was just added. The added variant
                // cannot be diagnosed without asking the server for status
                // information.
                Status remoteStatus = client.singleStatus(s.getPath(), true);
                long remoteRev = -1;
                if (s.getNodeKind() == NodeKind.dir) {
                    // getReposLastCmtRevisionNumber() does not properly work
                    // with files:
                    remoteRev = remoteStatus.getReposLastCmtRevisionNumber();
                } else {
                    // getLastChangedRevisionNumber() does not properly work
                    // with directories:
                    remoteRev = remoteStatus.getLastChangedRevisionNumber();
                }
                
                if (remoteRev < 0) {
                    log.debug("missing item that was locally added: "
                                + s.getPath());

                    // locally added -> undo add
                    client.revert(s.getPath(), true);
                } else {
                    log.debug("missing item that is already versioned (delete now): "
                                + s.getPath()
                                + ", nodeKind: "
                                + s.getNodeKind());

                    // already versioned -> delete
                    
                    if (s.getNodeKind() == NodeKind.dir) {
                        // we must remove each single file or folder inside dir
                        // (and dir itself) because simply deleting the top dir
                        // will let all subfiles and folders in the 'missing'
                        // state - and upon update they would be re-added, which
                        // is not what we want. We want the missing directory
                        // to be turned into a deleted one without any of the
                        // real dir or files inside be left over because this
                        // would confuse the user
                        
                        log.debug("deleting all subfiles/folders of directory '" + s.getPath() + "':");
                        // this is a status() method with a new parameter
                        // 'showMissing' that will include all subdirs and
                        // subfiles that are below the missing dir
                        Status[] localStati = client.status(s.getPath(), true, false, true, false, false, true);
                        for (Status status : localStati) {
                            client.remove(new String[] { status.getPath() }, null, true);
                        }
                        
                        // Normally, client.remove doesn't delete the directory,
                        // but leaves the empty directory structure behind.
                        // However, since we call this function when refreshing
                        // the workspace changes, empty directories that are
                        // left behind will confuse the user, so we need to make
                        // sure it's really gone. We can reconstruct it later
                        // using our shallow working copy.
                        File dir = new File(s.getPath());
                        if (dir.exists()) {
                            FileUtils.deleteDirectory(dir);
                        }
                    } else {
                        
                        // if the first parameter would be an URL, it would do a
                        // commit (and use the second parameter as commit message) -
                        // but we use a local filesystem path here and thus we only
                        // schedule for a deletion
                        client.remove(new String[] { s.getPath() }, null, true);
                    }
                }

            } else if (s.getTextStatus() == StatusKind.unversioned) {
                // set standard to-be-ignored files
                File file = new File(s.getPath());
                if (file.isHidden()) {
                    log.debug("unversioned item is hidden (ignore now): "
                            + s.getPath());

                    // update the svn:ignore property by appending a new line
                    // with the filename to be ignored (on the parent folder!)
                    PropertyData ignoreProp = client.propertyGet(file
                            .getParent(), PropertyData.IGNORE);

                    if (ignoreProp == null) { // create ignore property
                        client.propertyCreate(file.getParent(),
                                PropertyData.IGNORE, file.getName(), false);
                    } else { // merge ignore property
                        ignoreProp.setValue(mergeIgnoreProperty(ignoreProp,
                                file.getName()), false);
                    }
                } else {
                    log.debug("unversioned item (add now): " + s.getPath());

                    // TODO: check for new files that have the same name when
                    // looking at it case-insensitive (on unix systems) to avoid
                    // problems when checking out on Windows (eg. 'report' is
                    // the same as 'REPORT' under windows, but not on unix).
                    // for this to work we simply check if there is a file with
                    // the same case-insensitive name in this folder, exclude it
                    // from the add and give a warning message to the user
                    // TODO: check for special filename chars (eg. ";" ":" "*")
                    // that are not cross-platform

                    // otherwise we turn all unversioned into added
                    // Do not recurse, we do that ourselve below; we need to
                    // look at each file individually because we want to ignore
                    // some - setting the recurse flag here would add all files
                    // and folders inside the directory
                    client.add(s.getPath(), false);

                    // For directories, we recurse into it: the reason is that
                    // we need to re-retrieve the stati for that directory after
                    // it has been added, because all the unversioned children
                    // are not part of the initial stati list (when the dir is
                    // unversioned). Note: we cannot use isNodeKind() == 'dir'
                    // because svn sees it as 'none' at this point
                    if (new File(s.getPath()).isDirectory()) {
                        deleteMissingAndAddUnversioned(new File(s.getPath()));
                    } else {
                        // For files, we guess the MIME type and set it as a
                        // property. This allows the server to provide more
                        // specific options, such as showing images inline
                        // and displaying more suitable icons for files.
                        // Also, if the svn:mime-type property is
                        // set, then the Subversion Apache module will use its
                        // value to populate the Content-type: HTTP header when
                        // responding to GET requests.
                        String mimeType = MimeTypeUtilities.guessMimetype(s
                                .getPath());
                        log.debug("Setting mime type for " + s.getPath() + ": "
                                + mimeType);
                        client.propertyCreate(s.getPath(), "svn:mime-type",
                                mimeType, false);
                        if (mimeType.startsWith("text/")) {
                            // Causes the file to contain the EOL markers that
                            // are native to the operating system on which
                            // Subversion was run. Subversion will actually
                            // store the file in the repository using normalized
                            // LF EOL markers.
                            client.propertyCreate(s.getPath(), "svn:eol-style",
                                    "native", false);
                        }
                    }
                }
            }
        }
    }

    /**
     * Looks for local conflicted files and obstructed files. This only does a
     * local status call, because obstructed files can break a remote status.
     */
    private List<Conflict> analyzeConflictedAndObstructed()
            throws ClientException {
        List<Conflict> conflicts = new ArrayList<Conflict>();

        List<Status> localChanges = getLocalChanges();
        for (Status s : localChanges) {
            log.debug("locally analyzing " + textStatusDesc(s.getTextStatus())
                    + " " + nodeKindDesc(s.getNodeKind()) + " '" + wcPath(s)
                    + "'");
        }
        conflicts.addAll(ConflictHelper.findLocalObstructed(localChanges));
        conflicts.addAll(ConflictHelper.findLocalConflicted(localChanges));
        return conflicts;
    }

    /**
     * Looks for local conflicted files. This only does a local status call as
     * it happens after the update.
     */
    private List<Conflict> analyzeConflicted() throws ClientException {
        List<Conflict> conflicts = new ArrayList<Conflict>();

        List<Status> localChanges = getLocalChanges();
        for (Status s : localChanges) {
            log.debug("locally analyzing " + textStatusDesc(s.getTextStatus())
                    + " " + nodeKindDesc(s.getNodeKind()) + " '" + wcPath(s)
                    + "'");
        }
        conflicts.addAll(ConflictHelper.findLocalConflicted(localChanges));
        return conflicts;
    }

    /**
     * Handles conflicts that need to be resolved before calling remote status.
     * 
     * @throws IOException
     * @throws CancelException
     */
    private void handleConflictsBeforeRemoteStatus(List<Conflict> localConflicts)
            throws ClientException, IOException, CancelException {
        for (Conflict conflict : localConflicts) {
            presentConflictToUser(conflict);
            log.info(">> Before Remote Status: " + conflict.toString());
            conflict.beforeRemoteStatus();
        }
    }

    /**
     * Handles conflicts that need to be resolved before committing.
     * 
     * @throws IOException
     * @throws CancelException
     */
    private void handleConflictsBeforeCommit(List<Conflict> localConflicts)
            throws ClientException, IOException, CancelException {
        for (Conflict conflict : localConflicts) {
            presentConflictToUser(conflict);
            log.info(">> Before Commit: " + conflict.toString());
            conflict.beforeCommit();
        }
    }

    /**
     * Important method that looks out for any structure conflicts before an
     * update and creates {@link Conflict} objects for those. Upon each conflict
     * found, the user is asked to resolve it.
     * 
     * If the user cancels during the conflict resolving, a CancelException is
     * thrown.
     */
    private List<Conflict> analyzeChangesAndAskUser() throws ClientException {
        List<Status> remoteAndLocalChanges = getRemoteAndLocalChanges();

        List<Conflict> conflicts = new ArrayList<Conflict>();

        for (Status s : remoteAndLocalChanges) {
            log.debug("analyzing "
                    + SVNSynchronizer.textStatusDesc(s.getTextStatus())
                    + " "
                    + nodeKindDesc(s.getNodeKind())
                    + " <->"
                    + " "
                    + SVNSynchronizer.textStatusDesc(s
                            .getRepositoryTextStatus()) + " "
                    + nodeKindDesc(s.getReposKind()) + " '" + wcPath(s) + "'");
        }

        // LOCAL status can be everything except:
        // none/normal won't be displayed in local changes
        // unversioned/missing set to added/deleted (handled anyway)
        // merged only happens on update
        // ignored can be ignored ;-)
        // incomplete (on dir) missing files are set to deleted

        // LOCAL status can be any one of those:
        // simple ones:
        // modified
        // added
        // deleted
        // replaced (only possible with svn client)
        // hard ones:
        // conflicted
        // obstructed (eg. deleted file, created dir with same name)
        // external (only possible with svn client)

        // REMOTE status can be only the following:
        // none
        // normal
        // modified
        // added
        // deleted
        // replaced (delete and re-add in one step)

        // replace conflicts
        conflicts.addAll(ConflictHelper.findLocalContainerReplacedConflicts(remoteAndLocalChanges));
        conflicts.addAll(ConflictHelper.findRemoteContainerReplacedConflicts(remoteAndLocalChanges));
        conflicts.addAll(ConflictHelper.findReplacedModifiedConflicts(remoteAndLocalChanges));

        // add conflicts
        conflicts.addAll(ConflictHelper.findAddConflicts(remoteAndLocalChanges));

        // delete/modified conflicts
        conflicts.addAll(ConflictHelper.findLocalContainerDeleteConflicts(client, remoteAndLocalChanges));
        conflicts.addAll(ConflictHelper.findRemoteContainerDeleteConflicts(remoteAndLocalChanges));
        conflicts.addAll(ConflictHelper.findFileDeleteModifiedConflicts(remoteAndLocalChanges));
        conflicts.addAll(ConflictHelper.findFileModifiedDeleteConflicts(remoteAndLocalChanges));

        // property conflicts
        // get up-to-date remote and local changes to get property conflicts of
        // previously removed stati
        conflicts.addAll(ConflictHelper.findPropertyConflicts(client, getRemoteAndLocalChanges()));
        return conflicts;
    }

    /**
     * Calls {@link Conflict.handleBeforeUpdate} on all conflicts in the list.
     * 
     * @throws IOException
     * @throws CancelException
     */
    private void handleConflictsBeforeUpdate(List<Conflict> conflicts)
            throws ClientException, IOException, CancelException {
        for (Conflict conflict : conflicts) {
            presentConflictToUser(conflict);
            log.info(">> Before Update: " + conflict.toString());
            conflict.beforeUpdate();
        }
    }

    /**
     * Calls {@link Conflict.handleAfterUpdate} on all conflicts in the list.
     * 
     * @throws IOException
     */
    private void handleConflictsAfterUpdate(List<Conflict> conflicts)
            throws ClientException, IOException {
        for (Conflict conflict : conflicts) {
            log.info(">> After Update: " + conflict.toString());
            conflict.afterUpdate();
        }
    }

    /**
     * Returns the relative path in the working copy of the status object (for
     * shorter strings in log output).
     */
    private String wcPath(Status status) {
        return RelativePath.getRelativePath(localPathFile, new File(status
                .getPath()));
    }
    
    // #########################################################################
    // ### INNER CLASSES
    // #########################################################################
    
    private class StatusComparator implements Comparator<Status> {
        public int compare(Status left, Status right) {
            return left.getPath().compareTo(right.getPath());
        }
    }

    // #########################################################################
    // ### STATIC METHODS
    // #########################################################################

    /**
     * Gets all children and grand-children and so on for the path.
     */
    public static List<Status> getChildren(String path,
            List<Status> remoteAndLocalChanges) {
        List<Status> result = new ArrayList<Status>();
        // FIXME: not the fastest way (iterate over all + isParent for each)
        for (Status s : remoteAndLocalChanges) {
            if (FileHelper.isParent(path, s.getPath())) {
                result.add(s);
            }
        }
        return result;
    }

    /**
     * Helper method that stringifies a notify object from the notify callback
     * of svnkit.
     */
    public static String notifyToString(NotifyInformation info) {
        if (info.getAction() == -11) {
            // see org.tigris.subversion.javahl.JavaHLObjectFactory:
            // "undocumented thing"
            return "commit completed";
        } else if (info.getAction() < 0
                || info.getAction() >= NotifyAction.actionNames.length) {
            return info.getAction() + " " + info.getPath() + " "
                    + info.getErrMsg();
        } else {
            return NotifyAction.actionNames[info.getAction()] + " "
                    + info.getPath();
        }
    }

    /**
     * Returns the given nodekind int value as a human-readable string (eg.
     * "file" or "dir").
     */
    public static String nodeKindDesc(int nodeKind) {
        if (nodeKind == NodeKind.file) {
            return "file";
        } else if (nodeKind == NodeKind.dir) {
            return "dir";
        } else if (nodeKind == NodeKind.none) {
            return "none";
        } else {
            return "unknown";
        }
    }

    /**
     * Returns a human-readable string for the text status - fixes the missing
     * 'obstructed' case in Kind.getDescription(int).
     */
    public static String textStatusDesc(int kind) {
        switch (kind) {
        case StatusKind.obstructed:
            return "obstructed";

        default:
            return Kind.getDescription(kind);
        }
    }
}
