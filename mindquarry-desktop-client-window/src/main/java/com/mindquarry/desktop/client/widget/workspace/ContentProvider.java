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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.tigris.subversion.javahl.NodeKind;
import org.tigris.subversion.javahl.Status;
import org.tigris.subversion.javahl.StatusKind;
import org.tmatesoft.svn.core.internal.wc.SVNAdminDirectoryLocator;
import org.tmatesoft.svn.core.internal.wc.SVNFileListUtil;

/**
 * @author <a href="saar(at)mindquarry(dot)com">Alexander Saar</a>
 */
public class ContentProvider implements ITreeContentProvider {
    private final WorkspaceBrowserWidget workspaceBrowser;

    public ContentProvider(WorkspaceBrowserWidget widget) {
        this.workspaceBrowser = widget;
    }

    public Object[] getChildren(Object parentElement) {
        File rootDir = (File) parentElement;
        // TODO: below we iterate over the local and remote changes anyway, do we 
        // really need to iterate over the file system at all?
        File[] children = SVNFileListUtil.listFiles(rootDir, new FilenameFilter() {
            public boolean accept(File dir, String name) {
                File f = new File(dir, name);
                // ignore e.g. conflict files like <file>.r<rev>
                if (workspaceBrowser.toIgnore.containsKey(f)) {
                    return false;
                }
                // show only changed files, but within their directory
                // structure:
                if (f.isDirectory()) {
                    // if there is at least one change below this directory,
                    // show it, otherwise don't:
                    if (containsChange(f)) {
                        return true;
                    } else {
                        return false;
                    }
                }
                if (!workspaceBrowser.localChanges.getFiles().contains(f)
                        && !workspaceBrowser.remoteChanges.getFiles().contains(f)) {
                    return false;
                }
                if (name.equals(".svn")
						|| name.equals(SVNAdminDirectoryLocator.SHALLOW_DIR_REF_FILENAME)) {
                    return false;
                }
                return true;
            }

            /**
             * Return true if <tt>file</tt> contains at least one file
             * with a local or remote modification (or if itself is modified).
             */
            private boolean containsChange(File dir) {
                List<File> allChangedFiles = workspaceBrowser.remoteChanges.getFiles();
                allChangedFiles.addAll(workspaceBrowser.localChanges.getFiles());
                String potentialSuperDir = dir.getAbsolutePath() + File.separator;
                for (File changedFile : allChangedFiles) {
                    String file = changedFile.getAbsolutePath();
                    if (changedFile.isDirectory()) {
                        file += File.separator;
                    }
                    if (file.startsWith(potentialSuperDir)) {
                        return true;
                    }
                }
                return false;
            }

        });
        // don't skip files that have been added remotely:
        Set<File> allFiles = new HashSet<File>();
        if (children != null) {
            allFiles.addAll(Arrays.asList(children));
        }
        if (workspaceBrowser.remoteChanges != null) {
            for (File remoteFile : workspaceBrowser.remoteChanges.getFiles()) {
                int remoteStatus = -1;
                remoteStatus = workspaceBrowser.remoteChanges.getStatus(remoteFile).
                    getRepositoryTextStatus(); 
                if (remoteStatus == StatusKind.added &&
                        remoteFile.getParentFile().equals(rootDir)) {
                    allFiles.add(remoteFile);
                }
            }
        }
        // don't skip the files that were deleted locally:
        if (workspaceBrowser.localChanges != null) {
            for (File localFile : workspaceBrowser.localChanges.getFiles()) {
                int localStatus = workspaceBrowser.localChanges.getStatus(localFile).
                    getTextStatus();
                if ((localStatus == StatusKind.deleted || localStatus == StatusKind.missing)
                        && localFile.getParentFile().equals(rootDir)) {
                    // If someone moves a directory, local status of the old name
                    // will be "deleted" locally, but remote status will be "added". 
                    // This directory has been displayed above already, so we need
                    // to filter them out:
                    if (allFiles.contains(localFile)) {
                        continue;
                    }
                    allFiles.add(localFile);
                }
            }
        }
        return allFiles.toArray(new File[] {});
    }

    public Object getParent(Object element) {
        File file = (File) element;
        return file.getParent();
    }

    public boolean hasChildren(Object element) {
        File file = (File) element;
        if (file.isDirectory() && file.listFiles().length > 0) {
            return true;
        }
        // directories added remotely:
        if (workspaceBrowser.remoteChanges != null) {
            Status s = workspaceBrowser.remoteChanges.getStatus(file);
            if (s != null && s.getReposKind() == NodeKind.dir) {
                return true;
            }
        }
        // directories deleted locally:
        if (isDeletedLocally(file)) {
            return true;
        }
        return false;
    }

    private boolean isDeletedLocally(File file) {
        if (workspaceBrowser.localChanges != null) {
            for (File localFile : workspaceBrowser.localChanges.getFiles()) {
                int localStatus = workspaceBrowser.localChanges.getStatus(localFile).
                    getTextStatus();
                if ((localStatus == StatusKind.deleted || localStatus == StatusKind.missing)
                        && localFile.getParentFile().equals(file)) {
                    return true;
                }
            }
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
