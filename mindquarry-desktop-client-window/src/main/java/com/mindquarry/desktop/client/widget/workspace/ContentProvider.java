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

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;


/**
 * Provides the contents for a tree view showing all file changes and conflicts,
 * as detected by {@link SVNSynchronizer#getChangesAndConflicts()}.
 * 
 * @author <a href="saar(at)mindquarry(dot)com">Alexander Saar</a>
 * @author <a href="christian(dot)richardt(at)mindquarry(dot)com">Christian
 *         Richardt</a>
 */
public class ContentProvider implements ITreeContentProvider {

    private ChangeTree changeTree;

    /**
     * Constructor which constructs a tree of changes from
     * {@link WorkspaceBrowserWidget#getChangeSets()}.
     * 
     * @param widget
     *            WorkspaceBrowserWidget, which contains the change sets.
     */
    public ContentProvider(WorkspaceBrowserWidget widget) {
        changeTree = widget.getChangeTree();
    }

    /**
     * Lists all children of the provided file within the tree of changes. See
     * {@link ITreeContentProvider#getChildren(Object)}.
     * 
     * @param parentElement
     *            The parent as a File.
     */
    public Object[] getChildren(Object parentElement) {
        File rootDir = (File) parentElement;
        return changeTree.getChildren(rootDir);
    }

    /**
     * Returns the parent of the provided file within the tree of changes. See
     * {@link ITreeContentProvider#getParent(Object)}.
     * 
     * @param child
     *            The child as a File.
     */
    public Object getParent(Object child) {
        File file = (File) child;
        return file.getParent();
    }

    /**
     * Returns whether a particular file has children in the tree of changes.
     * See {@link ITreeContentProvider#hasChildren(Object)}.
     * 
     * @param parentElement
     *            The child as a File.
     */
    public boolean hasChildren(Object parentElement) {
        File file = (File) parentElement;
        return changeTree.hasChildren(file);
    }

    /**
     * Lists all children of the provided file within the tree of changes. See
     * {@link IStructuredContentProvider#getElements(Object)}.
     * 
     * @param inputElement
     *            The parent as a File.
     */
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
