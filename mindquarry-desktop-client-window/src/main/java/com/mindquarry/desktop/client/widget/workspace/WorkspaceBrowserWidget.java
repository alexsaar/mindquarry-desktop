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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.tigris.subversion.javahl.ChangePath;
import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.Status;

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
		col2.setText("Mime Type");
		col2.setWidth(200);

		TreeViewer viewer = new TreeViewer(tree);
		viewer.setLabelProvider(new TreeLabelProvider());
		viewer.setContentProvider(new TreeContentProvider());

		PreferenceStore store = client.getPreferenceStore();
		Profile selected = Profile.getSelectedProfile(store);
		String localPath = selected.getWorkspaceFolder();

		String username = selected.getLogin();
		String password = selected.getPassword();

		List stati = new ArrayList();
		for (Object item : client.getSelectedTeams()) {
			Team team = (Team) item;

			for (File folder : new File(localPath).listFiles()) {
//				JavaSVNHelper helper = new JavaSVNHelper(
//						team.getWorkspaceURL(), folder.getAbsolutePath(),
//						username, password);
//				for (ChangePath path : helper.getRemoteChanges()) {
//					stati.add(path);
//				}
//				try {
//					for (Status status : helper.getLocalChanges()) {
//						stati.add(status);
//					}
//				} catch (ClientException e) {
//					e.printStackTrace();
//				}
			}
		}
		viewer.setInput(stati);
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
			List stati = (List) parentElement;
			return (stati.toArray(new Object[0]));
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
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
								"/org/tango-project/tango-icon-theme/16x16/places/folder.png")); //$NON-NLS-1$

		private Image fileImg = new Image(
				Display.getCurrent(),
				getClass()
						.getResourceAsStream(
								"/org/tango-project/tango-icon-theme/16x16/mimetypes/text-x-generic.png")); //$NON-NLS-1$

		public Image getColumnImage(Object element, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return folderImg;
			}
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			String result = null;
			if (element instanceof ChangePath) {
				ChangePath path = (ChangePath) element;
				switch (columnIndex) {
				case 0:
					result = path.getPath();
					result = result.replace("/trunk/", "");
					result = result.replace("/tags/", "");
					result = result.replace("/branches/", "");
					break;
				case 1:
					result = "" + path.getAction();
					break;
				}
			} else if (element instanceof Status) {
				Status status = (Status) element;
				switch (columnIndex) {
				case 0:
					result = status.getPath();
					break;
				case 1:
					result = status.getTextStatusDescription();
					break;
				}
			}

			// switch (columnIndex) {
			// case 0:
			// result = status.getPath();
			// break;
			// case 1:
			// result = status.getTextStatusDescription();
			// // if (file.isFile()) {
			// // result = client.getMimeType(file);
			// // if (result.equals("application/octet-stream")) {
			// // try {
			// // MagicMatch match = Magic.getMagicMatch(file, true,
			// // true);
			// // result = match.getMimeType();
			// // } catch (Exception e) {
			// // // log.warn("Could not handle mime type.", e);
			// // }
			// // }
			// // }
			// break;
			// }
			return result;
		}
	}
}
