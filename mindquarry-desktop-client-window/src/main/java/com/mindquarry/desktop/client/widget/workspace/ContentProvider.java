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
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.tigris.subversion.javahl.StatusKind;

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
                if (name.equals(".svn") || name.equals(".svnref")) {
                    return false;
                }
                return true;
            }

            private boolean containsChange(File f) {
                for (File remoteFile : workspaceBrowser.remoteChanges.keySet()) {
                    if (remoteFile.getAbsolutePath().startsWith(
                            f.getAbsolutePath() + "/")) {
                        return true;
                    }
                }
                return false;
            }
        });
        // files may be added remotely:
        List<File> allFiles = new ArrayList<File>();
        allFiles.addAll(Arrays.asList(children));
        if (workspaceBrowser.remoteChanges != null) {
            for (File remoteFile : workspaceBrowser.remoteChanges.keySet()) {
                if (workspaceBrowser.remoteChanges.containsKey(remoteFile)
                        && workspaceBrowser.remoteChanges.get(remoteFile) == StatusKind.added
                        && remoteFile.getParentFile().equals(workspaceRoot)) {
                    allFiles.add(remoteFile);
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
