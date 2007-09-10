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
package com.mindquarry.desktop.client.widget.util.container;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * @author <a href="mailto:lars(dot)trieloff(at)mindquarry(dot)com">Lars
 *         Trieloff</a>
 */
public class NoContentWidget extends Composite {
	private String message;

	public NoContentWidget(Composite parent, String message) {
		super(parent, SWT.NONE);
		this.message = message;

		// init layout and content
		setLayout(new FillLayout());
		createContents(this);
	}

	protected void createContents(Composite parent) {
		setLayoutData(new GridData(GridData.FILL_BOTH));
		setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		setLayout(new GridLayout(1, true));
		((GridData) getLayoutData()).heightHint = ((GridData) parent
				.getLayoutData()).heightHint;

		Composite internalComp = new Composite(parent, SWT.NONE);
		internalComp.setBackground(internalComp.getParent().getBackground());
		internalComp.setLayout(new GridLayout(1, true));
		internalComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				true));

        Image icon = new Image(null, getClass().getResourceAsStream(
            "/org/tango-project/tango-icon-theme/22x22/status/dialog-information.png")); //$NON-NLS-1$

        Label label = new Label(internalComp, SWT.CENTER);
        label.setImage(icon);
        label.setBackground(label.getParent().getBackground());
        label.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

		label = new Label(internalComp, SWT.CENTER);
		label.setText(message);
		label.setBackground(label.getParent().getBackground());
		label.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
	}
}
