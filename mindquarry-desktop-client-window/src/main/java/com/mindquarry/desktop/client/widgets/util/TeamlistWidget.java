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
package com.mindquarry.desktop.client.widgets.util;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.mindquarry.desktop.client.MindClient;
import com.mindquarry.desktop.client.widget.WidgetBase;

/**
 * Add summary documentation here.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class TeamlistWidget extends WidgetBase {
	/**
	 * {@inheritDoc}
	 */
	public TeamlistWidget(Composite parent, int style, MindClient client) {
		super(parent, style, client);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.mindquarry.desktop.minutes.editor.widget.EditorWidget#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected void createContents(Composite parent) {
		setLayout(new GridLayout(1, false));
		((GridLayout) getLayout()).marginBottom = 2;
		((GridLayout) getLayout()).marginTop = 6;
		((GridLayout) getLayout()).marginLeft = 2;
		((GridLayout) getLayout()).marginRight = 0;

		((GridLayout) getLayout()).marginWidth = 0;
		((GridLayout) getLayout()).marginHeight = 0;

		((GridLayout) getLayout()).verticalSpacing = 2;
		((GridLayout) getLayout()).horizontalSpacing = 0;

		Label label = new Label(parent, SWT.LEFT);
		label.setText("Teams:");

		Table table = new Table(parent, SWT.SINGLE | SWT.CHECK
				| SWT.FULL_SELECTION | SWT.BORDER);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));

		TableColumn titleColumn = new TableColumn(table, SWT.NONE);
		titleColumn.setResizable(false);

		TableViewer viewer = new TableViewer(table);
	}
}
