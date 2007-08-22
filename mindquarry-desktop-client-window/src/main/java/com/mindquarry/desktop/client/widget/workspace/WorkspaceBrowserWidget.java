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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import com.mindquarry.desktop.client.MindClient;
import com.mindquarry.desktop.client.widget.WidgetBase;
import com.mindquarry.desktop.model.team.Team;
import com.mindquarry.desktop.preferences.profile.Profile;

/**
 * @author <a href="mailto:lars(dot)trieloff(at)mindquarry(dot)com">Lars
 *         Trieloff</a>
 */
public class WorkspaceBrowserWidget extends WidgetBase {
    private static Log log = LogFactory.getLog(WorkspaceBrowserWidget.class);

    private TreeViewer viewer;

    public WorkspaceBrowserWidget(Composite parent, MindClient client) {
        super(parent, SWT.NONE, client);
    }

    // #########################################################################
    // ### WIDGET METHODS
    // #########################################################################
    protected void createContents(Composite parent) {
        Tree tree = new Tree(parent, SWT.CHECK | SWT.BORDER | SWT.H_SCROLL
                | SWT.V_SCROLL);
        tree.setLayoutData(new GridData(GridData.FILL_BOTH));
        tree.setHeaderVisible(true);
        tree.setLinesVisible(true);

        TreeColumn col1 = new TreeColumn(tree, SWT.LEFT);
        col1.setText("Name");
        col1.setWidth(200);
        TreeColumn col2 = new TreeColumn(tree, SWT.LEFT);
        col2.setText("Status");
        col2.setWidth(32);

        viewer = new TreeViewer(tree);
        viewer.setLabelProvider(new TreeLabelProvider());
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

        refresh();
    }

    private void refresh() {
        PreferenceStore store = client.getPreferenceStore();
        Profile selected = Profile.getSelectedProfile(store);

        // check profile
        if (selected == null) {
            log.debug("No profile selected."); //$NON-NLS-1$
            return;
        }
        viewer.setInput(new File(selected.getWorkspaceFolder()));
    }

    // #########################################################################
    // ### PUBLIC METHODS
    // #########################################################################

    // #########################################################################
    // ### PRIVATE METHODS
    // #########################################################################

    // #########################################################################
    // ### NESTED CLASSES
    // #########################################################################
    private final class TreeContentProvider implements ITreeContentProvider {
        public Object[] getChildren(Object parentElement) {
            final List<Team> selectedTeams = new ArrayList();
            Display.getDefault().syncExec(new Runnable() {
                public void run() {
                    selectedTeams.addAll(client.getSelectedTeams());
                }
            });

            File workspaceRoot = (File) parentElement;
            return workspaceRoot.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    if (name.equals(".svn") || name.equals(".svnref")) {
                        return false;
                    } else {
                        for (Team team : selectedTeams) {
                            if (team.getName().equals(name)) {
                                return true;
                            }
                        }
                    }
                    return true;
                }
            });
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

    private class TreeLabelProvider extends LabelProvider implements
            ITableLabelProvider {
        private Image folderImg = new Image(
                Display.getCurrent(),
                getClass()
                        .getResourceAsStream(
                                "/org/tango-project/tango-icon-theme/32x32/places/folder.png")); //$NON-NLS-1$

        private Image fileImg = new Image(
                Display.getCurrent(),
                getClass()
                        .getResourceAsStream(
                                "/org/tango-project/tango-icon-theme/32x32/mimetypes/text-x-generic.png")); //$NON-NLS-1$

        public Image getColumnImage(Object element, int columnIndex) {
            File file = (File) element;

            Image result = null;
            switch (columnIndex) {
            case 0:
                if (file.isDirectory()) {
                    result = folderImg;
                } else if (file.isFile()) {
                    result = fileImg;
                }
                break;
            // TOOD: show images
            /*
             * case 1: if (file.isDirectory()) { result = folderImg; } else if
             * (file.isFile()) { result = fileImg; } break;
             */
            }
            return result;
        }

        public String getColumnText(Object element, int columnIndex) {
            File file = (File) element;

            String result = null;
            switch (columnIndex) {
            case 0:
                result = file.getName();
                break;
            case 1:
                // FIXME:
                result = "-";
                break;
            }
            return result;
        }
    }
}
