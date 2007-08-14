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
package com.mindquarry.desktop.client.action.task;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.mindquarry.desktop.client.MindClient;
import com.mindquarry.desktop.client.action.ActionBase;

/**
 * Add summary documentation here.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class FinishTaskAction extends ActionBase {
	public static final String ID = "finish-task";

	private static final Image IMAGE = new Image(
			Display.getCurrent(),
			FinishTaskAction.class
					.getResourceAsStream("/com/mindquarry/icons/" + ICON_SIZE + "/status/task-done.png")); //$NON-NLS-1$

	public FinishTaskAction(MindClient client) {
		super(client);

		setId(ID);
		setActionDefinitionId(ID);

		setText(TEXT);
		setToolTipText(TOOLTIP);
		setAccelerator(SWT.CTRL + +SWT.SHIFT + 'F');
		setImageDescriptor(ImageDescriptor.createFromImage(IMAGE));
	}

	public void run() {
		
	}
}
