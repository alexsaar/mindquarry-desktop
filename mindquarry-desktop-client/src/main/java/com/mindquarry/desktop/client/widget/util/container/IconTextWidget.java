package com.mindquarry.desktop.client.widget.util.container;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class IconTextWidget extends Composite {

    protected Image icon;
    protected String message;

    public IconTextWidget(Composite parent, Image icon, String message) {
        super(parent, SWT.NONE);
        this.icon = icon;
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
        internalComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));

        Label label = new Label(internalComp, SWT.CENTER);
        label.setImage(icon);
        label.setBackground(label.getParent().getBackground());
        label.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
        
        label = new Label(internalComp, SWT.CENTER|SWT.WRAP);
        label.setText(message);
        label.setBackground(label.getParent().getBackground());
        label.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
    }

}