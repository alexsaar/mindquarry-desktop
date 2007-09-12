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
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;

/**
 * @author <a href="mailto:lars(dot)trieloff(at)mindquarry(dot)com">Lars
 *         Trieloff</a>
 */
public class UpdateWidget extends Composite {
	private String message;
    private Composite internalComp;
    private Label mainLabel;
    private Label updateLabel;
	
    public UpdateWidget(Composite parent, String message) {
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

        internalComp = new Composite(this, SWT.NONE);
        internalComp.setBackground(internalComp.getParent().getBackground());
        internalComp.setLayout(new GridLayout(1, true));
        internalComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
                true));

        ProgressBar bar = new ProgressBar(internalComp, SWT.HORIZONTAL
                | SWT.INDETERMINATE);
        bar.setSize(200, 16);
        bar.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

        mainLabel = new Label(internalComp, SWT.CENTER|SWT.WRAP);
        mainLabel.setText(message);
        mainLabel.setBackground(mainLabel.getParent().getBackground());
        mainLabel.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

        // Another label to show more detailed progress information (e.g.
        // which file is currently being downloaded):
        updateLabel = new Label(internalComp, SWT.CENTER|SWT.WRAP);
        // TODO: how to make the label resize properly without using "\n\n"
        // and manual internalComp.layout()?
        updateLabel.setText("\n\n");
	}

    /**
     * Set the progress message.
     */
    protected void setMessage(final String message) {
        getDisplay().syncExec(new Runnable() {
            public void run() {
                if (!mainLabel.isDisposed()) {
                    mainLabel.setText(message);
                    mainLabel.setBackground(mainLabel.getParent().getBackground());
                    mainLabel.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, true));
                    internalComp.layout();
                }
            }
        });
    }

    /**
     * Set an additional message showing more detailed progress information.
     */
    protected void setUpdateMessage(final String message) {
        getDisplay().syncExec(new Runnable() {
            public void run() {
                updateLabel.setText(message);
                updateLabel.setBackground(updateLabel.getParent().getBackground());
                updateLabel.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, true));
                internalComp.layout();
            }
        });
    }
    
}
