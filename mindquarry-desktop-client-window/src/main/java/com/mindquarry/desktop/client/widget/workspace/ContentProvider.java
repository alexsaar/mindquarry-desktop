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
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.mindquarry.desktop.util.FileHelper;
import com.mindquarry.desktop.workspace.conflict.Change;

/**
 * Provides the contents for a tree view showing all file changes and conflicts,
 * as detected by {@link SVNSynchronizer#getChangesAndConflicts()}.
 * 
 * @author <a href="saar(at)mindquarry(dot)com">Alexander Saar</a>
 * @author <a href="christian(dot)richardt(at)mindquarry(dot)com">Christian
 *         Richardt</a>
 */
public class ContentProvider implements ITreeContentProvider {

    @SuppressWarnings("unused")
    private final WorkspaceBrowserWidget workspaceBrowser;

    private ChangeTree changeTree;

    /**
     * Constructor which constructs a tree of changes from
     * {@link WorkspaceBrowserWidget#getChangeSets()}.
     * 
     * @param widget
     *            WorkspaceBrowserWidget, which contains the change sets.
     */
    public ContentProvider(WorkspaceBrowserWidget widget) {
        this.workspaceBrowser = widget;
        
        // create the tree of changes from the list of all changes/conflicts 
        changeTree = new ChangeTree(widget.getChangeSets().getChanges());
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

    static class ChangeTree {
        private TreeNode root;

        public ChangeTree(List<Change> changes) {
            this.root = null;
            for (Change change : changes) {
                addToTree(change);
            }
        }

        public TreeNode getRoot() {
            return root;
        }

        private void addToTree(Change change) {
            if (root == null) {
                root = TreeNode.createTree(change);
            } else {
                root.insertChange(change);
            }
        }

        public File[] getChildren(File parent) {
            TreeNode parentNode = root.findFile(parent);
            if (parentNode == null) {
                return new File[] {};
            }

            List<TreeNode> children = parentNode.getChildren();
            List<File> childrenFiles = new ArrayList<File>();
            for (TreeNode child : children) {
                childrenFiles.add(child.getFile());
            }
            return childrenFiles.toArray(new File[] {});
        }

        public boolean hasChildren(File parent) {
            TreeNode parentNode = root.findFile(parent);
            if (parentNode == null) {
                return false;
            }

            return parentNode.getChildren().size() != 0;
        }

        // Inner classes -------------------------------------------------------

        public static abstract class TreeNode {
            protected List<TreeNode> children;

            protected File file;

            protected TreeNode(File file) {
                this.children = new ArrayList<TreeNode>();
                this.file = file;
            }

            protected TreeNode(File file, TreeNode child) {
                this(file);
                addChild(child);
            }

            protected TreeNode(File file, List<TreeNode> children) {
                this(file);
                addChildren(children);
            }

            public void addChild(TreeNode child) {
                if (child != null)
                    this.children.add(child);
            }

            public void addChildren(List<TreeNode> children) {
                this.children.addAll(children);
            }

            static ChangeTree.TreeNode createTree(Change change) {
                File file = change.getFile();
                ChangeTree.TreeNode node = new ChangeTree.ChangeTreeNode(change);
                file = file.getParentFile();
                while (file != null) {
                    node = new ChangeTree.EmptyTreeNode(file, node);
                    file = file.getParentFile();
                }
                return node;
            }

            public boolean insertChange(Change change) {
                // check whether this will be the correct subtree to insert in
                if (!FileHelper.isParent(file, change.getFile())) {
                    System.err
                            .println("Could not insert change in this subtree!");
                    return false;
                }

                // check whether the change would be a child of this subtree
                if (file.equals(change.getFile().getParentFile())) {
                    // check whether this child already exists
                    for (TreeNode child : children) {
                        if (child.getFile().equals(change.getFile())) {
                            if (child instanceof EmptyTreeNode) {
                                // replace existing EmptyTreeNode with
                                // ChangeTreeNode
                                ChangeTreeNode newChild = new ChangeTreeNode(
                                        change, child.getChildren());
                                children.remove(child);
                                children.add(newChild);
                                return true;
                            } else { // multiple conflicts at same file =>
                                // die
                                throw new RuntimeException(
                                        "Unexpected multiple conflicts at "
                                                + change.getFile());
                            }
                        }
                    }

                    // add new child node
                    addChild(new ChangeTreeNode(change));
                    return true;
                }

                // recurse in the tree
                for (TreeNode child : children) {
                    if (FileHelper.isParent(child.getFile(), change.getFile())) {
                        return child.insertChange(change);
                    }
                }

                // need to insert some children first
                File newChild = change.getFile().getParentFile();
                TreeNode node = new ChangeTreeNode(change);

                while (!file.equals(newChild)) {
                    node = new EmptyTreeNode(newChild, node);
                    newChild = newChild.getParentFile();
                }
                addChild(node);
                return true;
            }

            public TreeNode findFile(File targetFile) {
                // check whether we found the file
                if (targetFile.equals(file))
                    return this;

                // check whether is the right subtree to search in
                if (!FileHelper.isParent(file, targetFile)) {
                    return null;
                }

                // recurse in the tree
                for (TreeNode child : children) {
                    TreeNode node = child.findFile(targetFile);
                    if (node != null)
                        return node;
                }

                // not found
                return null;
            }

            public String toString() {
                return printThis() + "\n" + printChildren(1);
            }

            protected abstract String printThis();

            protected String printChildren(int indentation) {
                StringBuilder sb = new StringBuilder();
                for (TreeNode child : children) {
                    for (int i = 0; i < indentation; i++)
                        sb.append("  ");
                    sb.append("+ ");
                    sb.append(child.printThis());
                    sb.append("\n");
                    sb.append(child.printChildren(indentation + 1));
                }
                return sb.toString();
            }

            public File getFile() {
                return file;
            }

            public List<TreeNode> getChildren() {
                return children;
            }
        }

        public static class EmptyTreeNode extends TreeNode {
            final private File file;

            public EmptyTreeNode(File file) {
                super(file);
                this.file = file;
            }

            public EmptyTreeNode(File file, TreeNode child) {
                super(file, child);
                this.file = file;
            }

            public EmptyTreeNode(File file, List<TreeNode> children) {
                super(file, children);
                this.file = file;
            }

            @Override
            protected String printThis() {
                String res = file.getName();
                if (res == null || res.length() == 0)
                    res = file.toString();
                return "<" + res + ">";
            }
        }

        public static class ChangeTreeNode extends TreeNode {
            final private Change change;

            public ChangeTreeNode(Change change) {
                super(change.getFile());
                this.change = change;
            }

            public ChangeTreeNode(Change change, TreeNode child) {
                super(change.getFile(), child);
                this.change = change;
            }

            public ChangeTreeNode(Change change, List<TreeNode> children) {
                super(change.getFile(), children);
                this.change = change;
            }

            public Change getChange() {
                return change;
            }

            @Override
            protected String printThis() {
                return "<" + change + ">";
            }
        }
    }

}
