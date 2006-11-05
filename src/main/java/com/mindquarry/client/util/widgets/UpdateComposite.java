/**
 * Copyright (C) 2005 MindQuarry GmbH, All Rights Reserved
 */
package com.mindquarry.client.util.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;

/**
 * @author <a href="mailto:lars(dot)trieloff(at)mindquarry(dot)com">Lars
 *         Trieloff</a>
 */
public class UpdateComposite extends Composite {
    public UpdateComposite(Composite parent, String text) {
        super(parent, SWT.BORDER);

        setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, true, 2, 2));
        setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
        setLayout(new GridLayout(1, true));
        ((GridData) getLayoutData()).heightHint = ((GridData) getParent()
                .getLayoutData()).heightHint;

        Composite internalComp = new Composite(this, SWT.NONE);
        internalComp.setBackground(internalComp.getParent().getBackground());
        internalComp.setLayout(new GridLayout(1, true));
        internalComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
                true));

        ProgressBar bar = new ProgressBar(internalComp, SWT.HORIZONTAL
                | SWT.INDETERMINATE);
        bar.setSize(200, 16);
        bar.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

        Label label = new Label(internalComp, SWT.CENTER);
        label.setText(text);
        label.setBackground(label.getParent().getBackground());
        label.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
    }
}
