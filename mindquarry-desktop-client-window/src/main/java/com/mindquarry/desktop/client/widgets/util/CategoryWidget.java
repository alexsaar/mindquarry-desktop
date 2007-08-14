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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.mindquarry.desktop.client.MindClient;
import com.mindquarry.desktop.client.widgets.WidgetBase;
import com.mindquarry.desktop.client.widgets.task.TaskContainerWidget;

/**
 * Add summary documentation here.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class CategoryWidget extends WidgetBase {
	public CategoryWidget(Composite parent, int style, MindClient client) {
		super(parent, style, client);
	}

	protected void createContents(Composite parent) {
		TabFolder tabFolder = new TabFolder(parent, SWT.TOP);

		TabItem tabItem = new TabItem(tabFolder, SWT.NULL);
		tabItem.setText("Files");

		tabItem = new TabItem(tabFolder, SWT.NULL);
		tabItem.setText("Tasks");

		TaskContainerWidget taskContainer = new TaskContainerWidget(tabFolder,
				client);
		tabItem.setControl(taskContainer);
	}
}
