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
import java.util.Set;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.tigris.subversion.javahl.StatusKind;
import org.tmatesoft.svn.core.internal.wc.SVNAdminDirectoryLocator;

/**
 * @author <a href="saar(at)mindquarry(dot)com">Alexander Saar</a>
 */
public class ContentProvider implements ITreeContentProvider {
    private final WorkspaceBrowserWidget workspaceBrowser;

    public ContentProvider(WorkspaceBrowserWidget widget) {
        this.workspaceBrowser = widget;
    }

    public Object[] getChildren(Object parentElement) {
        File workspaceRoot = (File) parentElement;
        File[] children = workspaceRoot.listFiles(new FilenameFilter() {
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
                if (!workspaceBrowser.localChanges.containsKey(f)
                        && !workspaceBrowser.remoteChanges.containsKey(f)) {
                    return false;
                }
                if (name.equals(".svn")
						|| name.equals(SVNAdminDirectoryLocator.SHALLOW_DIR_REF_FILENAME)) {
                    return false;
                }
                return true;
            }

            /**
             * Return true if <tt>file</tt> contains at leats one file
             * with a local or remote modification.
             */
            private boolean containsChange(File dir) {
                String potentialSuperDir = dir.getAbsolutePath() + File.separator;
                for (File remoteFile : workspaceBrowser.remoteChanges.keySet()) {
                    if (remoteFile.getAbsolutePath().startsWith(potentialSuperDir)) {
                        return true;
                    }
                }
                for (File localFile : workspaceBrowser.localChanges.keySet()) {
                    if (localFile.getAbsolutePath().startsWith(potentialSuperDir)) {
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
            for (File remoteFile : workspaceBrowser.remoteChanges.keySet()) {
                int remoteStatus = -1;
                if (workspaceBrowser.remoteChanges.containsKey(remoteFile)) {
                    remoteStatus = workspaceBrowser.remoteChanges.get(remoteFile).getRepositoryTextStatus(); 
                }
                if (remoteStatus == StatusKind.added &&
                        remoteFile.getParentFile().equals(workspaceRoot)) {
                    allFiles.add(remoteFile);
                }
            }
        }
        // don't skip the files that were deleted locally:
        if (workspaceBrowser.localChanges != null) {
            for (File localFile : workspaceBrowser.localChanges.keySet()) {
                int localStatus = -1;
                if (workspaceBrowser.localChanges.containsKey(localFile)) {
                    localStatus = workspaceBrowser.localChanges.get(localFile).getTextStatus();
                }
                if ((localStatus == StatusKind.deleted || localStatus == StatusKind.missing)
                        && localFile.getParentFile().equals(workspaceRoot)) {
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
