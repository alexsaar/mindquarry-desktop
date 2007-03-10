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
package com.mindquarry.desktop.client.workspace.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;

/**
 * Add summary documentation here.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class SynchronizeWidget extends Composite {
    private Label synLabel;

    private ProgressBar progressBar;

    public SynchronizeWidget(Composite parent) {
        super(parent, SWT.NONE);
        setBackground(getParent().getBackground());
        setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        setLayout(new GridLayout(1, true));
        ((GridLayout) getLayout()).marginTop = 0;
        ((GridLayout) getLayout()).marginBottom = 0;
        ((GridLayout) getLayout()).marginLeft = 0;
        ((GridLayout) getLayout()).marginRight = 0;

        progressBar = new ProgressBar(this, SWT.NONE);
        progressBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        synLabel = new Label(this, SWT.LEFT);
        synLabel.setBackground(getBackground());
        synLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        synLabel.setText(""); //$NON-NLS-1$
    }

    public void setProgressSteps(final int value) {
        progressBar.setMaximum(value);
    }

    public void updateProgress() {
        int step = ((ProgressBar) progressBar).getSelection();
        progressBar.setSelection(++step);
    }

    public void resetProgress() {
        progressBar.setSelection(0);
    }

    public void setMessage(String message) {
        synLabel.setText(message);
    }
}
