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
package com.mindquarry.desktop.client.workspace.dialog;

import java.io.File;
import java.io.FilenameFilter;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Dialog for resolving working copy conflicts.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class SyncSelectionDialog extends TitleAreaDialog {
	public SyncSelectionDialog(Shell shell) {
		super(shell);
		setBlockOnOpen(true);
	}

	/**
	 * @see org.eclipse.jface.window.Window#setShellStyle(int)
	 */
	protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(newShellStyle | SWT.RESIZE | SWT.MAX);
	}

	/**
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);

		setTitle("Workspace Synchronization");
		setMessage("Select files for synchronization.",
				IMessageProvider.INFORMATION);
		getShell().setText("Workspace Synchronization");

		return contents;
	}

	/**
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		// build the separator line
		Label titleBarSeparator = new Label(composite, SWT.HORIZONTAL
				| SWT.SEPARATOR);
		titleBarSeparator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Tree tree = new Tree(composite, SWT.CHECK | SWT.BORDER | SWT.H_SCROLL
				| SWT.V_SCROLL);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

		TreeColumn col1 = new TreeColumn(tree, SWT.LEFT);
		col1.setText("Name");
		col1.setWidth(200);
		TreeColumn col2 = new TreeColumn(tree, SWT.LEFT);
		col2.setText("Synchronize as Link");
		col2.setWidth(200);

		TreeViewer viewer = new TreeViewer(tree);
		viewer.setLabelProvider(new TreeLabelProvider());
		viewer.setContentProvider(new TreeContentProvider());
		viewer.setInput(new File("."));
		checkItems(tree.getItems());
		
		composite = new Composite(composite, SWT.NONE);
		composite.setLayout(new GridLayout(2, true));
		
		Button selectAll = new Button(composite, SWT.PUSH);
		selectAll.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		selectAll.setText("Select all");
		Button deselectAll = new Button(composite, SWT.PUSH);
		deselectAll.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		deselectAll.setText("Deselect all");		
		return composite;
	}
	
	private void checkItems(TreeItem[] items) {
		for (TreeItem item : items) {
			item.setChecked(true);
			checkItems(item.getItems());
		}
	}

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

		public String getColumnText(Object element, int columnIndex) {
			File file = (File) element;

			String result = "";
			switch (columnIndex) {
			case 0:
				result = file.getName();
				break;
			case 1:
				result = String.valueOf(file.isDirectory());
				break;
			}
			return result;
		}
	}
}
