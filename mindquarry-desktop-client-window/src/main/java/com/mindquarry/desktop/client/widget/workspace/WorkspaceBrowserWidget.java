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

import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;

import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicMatch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

import com.mindquarry.desktop.client.MindClient;
import com.mindquarry.desktop.client.widget.WidgetBase;
import com.mindquarry.desktop.client.widgets.WorkspaceBrowserWidget;

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
		viewer.setInput(new File("."));
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
			File file = (File) parentElement;
			if ((file.isDirectory()) && (file.listFiles().length > 0)) {
				return file.listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						if (name.equals(".svn")) {
							return false;
						}
						return true;
					}
				});
			}
			return null;
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
								"/org/tango-project/tango-icon-theme/16x16/places/folder.png")); //$NON-NLS-1$

		private Image fileImg = new Image(
				Display.getCurrent(),
				getClass()
						.getResourceAsStream(
								"/org/tango-project/tango-icon-theme/16x16/mimetypes/text-x-generic.png")); //$NON-NLS-1$

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
			}
			return result;
		}

		private FileTypeMap mimeMap = MimetypesFileTypeMap
				.getDefaultFileTypeMap();

		public String getColumnText(Object element, int columnIndex) {
			File file = (File) element;

			String result = null;
			switch (columnIndex) {
			case 0:
				result = file.getName();
				break;
			case 1:
				if (file.isFile()) {
					result = mimeMap.getContentType(file);
					if (result.equals("application/octet-stream")) {
						try {
							MagicMatch match = Magic.getMagicMatch(file, true,
									true);
							result = match.getMimeType();
						} catch (Exception e) {
//							log.warn("Could not handle mime type.", e);
						}
					}
				}
				break;
			}
			return result;
		}
	}
}
