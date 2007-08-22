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
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.mindquarry.desktop.client.MindClient;
import com.mindquarry.desktop.client.widget.WidgetBase;
import com.mindquarry.desktop.model.team.Team;
import com.mindquarry.desktop.preferences.profile.Profile;

/**
 * @author <a href="saar(at)mindquarry(dot)com">Alexander Saar</a>
 */
public class WorkspaceBrowserWidget extends WidgetBase {
    private static Log log = LogFactory.getLog(WorkspaceBrowserWidget.class);

    private TreeViewer viewer;

    public WorkspaceBrowserWidget(Composite parent, MindClient client) {
        super(parent, SWT.NONE, client);

        // initialize layout
        setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 2,
                1));
        setLayout(new GridLayout(1, true));
        ((GridLayout) getLayout()).horizontalSpacing = 0;
        ((GridLayout) getLayout()).verticalSpacing = 0;
        ((GridLayout) getLayout()).marginHeight = 0;
        ((GridLayout) getLayout()).marginWidth = 0;
    }

    // #########################################################################
    // ### WIDGET METHODS
    // #########################################################################
    protected void createContents(Composite parent) {
        viewer = new TreeViewer(parent, SWT.BORDER | SWT.H_SCROLL
                | SWT.V_SCROLL | SWT.FULL_SELECTION);
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
        viewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
        viewer.getTree().setHeaderVisible(true);
        viewer.getTree().setLinesVisible(true);
        viewer.getTree().setFont(
                JFaceResources.getFont(MindClient.TEAM_NAME_FONT_KEY));

        TreeViewerColumn col = new TreeViewerColumn(viewer, SWT.LEFT);
        col.getColumn().setText("Name");
        col.getColumn().setWidth(500);
        col.setLabelProvider(new ColumnLabelProvider() {
            public Image getImage(Object element) {
                File file = (File) element;
                if (file.isDirectory()) {
                    return new Image(
                            Display.getCurrent(),
                            getClass()
                                    .getResourceAsStream(
                                            "/org/tango-project/tango-icon-theme/32x32/places/folder.png")); //$NON-NLS-1$
                } else if (file.isFile()) {
                    return new Image(
                            Display.getCurrent(),
                            getClass()
                                    .getResourceAsStream(
                                            "/org/tango-project/tango-icon-theme/32x32/mimetypes/text-x-generic.png")); //$NON-NLS-1$
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
            public String getText(Object element) {
                return "";
            }
        });
        col = new TreeViewerColumn(viewer, SWT.CENTER);
        col.getColumn().setResizable(false);
        col.getColumn().setWidth(32);
        col.setLabelProvider(new ColumnLabelProvider() {
            public Image getImage(Object element) {
                File file = (File) element;
                if (file.isDirectory()) {
                    return new Image(
                            Display.getCurrent(),
                            getClass()
                                    .getResourceAsStream(
                                            "/com/mindquarry/icons/32x32/actions/synchronize-down.png")); //$NON-NLS-1$
                } else if (file.isFile()) {
                    return new Image(
                            Display.getCurrent(),
                            getClass()
                                    .getResourceAsStream(
                                            "/com/mindquarry/icons/32x32/actions/synchronize-up.png")); //$NON-NLS-1$
                }
                return null;
            }

            public String getText(Object element) {
                return "";
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
        viewer.expandAll();
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
            File[] children = workspaceRoot.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    if (name.equals(".svn") || name.equals(".svnref")) {
                        return false;
                    }
                    return true;
                }
            });
            return children;
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
}
