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
package com.mindquarry.desktop.preferences.pages;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 * A base class with helper methods to mark the error controls with a red
 * border in addition to the error message displayed at the top of the dialog.
 * 
 * For this you will have to create a composite with either
 * <code>createErrorBorderComposite(Composite parent)</code> or with
 * <code>createErrorBorderComposite(Composite parent, int border)</code>
 * and put your control as child of it. Then you will have to register the
 * control so that a call to
 * <code>setInvalid(String message, Control control)</code> can find the error
 * composite later.
 * 
 * <p>Example code for creation:</p>
 * <pre>
 * Composite errorComp = createErrorBorderComposite(proxyGroup);
 * url = new Text(errorComp, SWT.SINGLE | SWT.BORDER);
 * url.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 * registerErrorBorderComposite(errorComp, url);
 * </pre>
 * 
 * <p>Example code for setting an error message:</p>
 * <pre>
 * setInvalid("Proxy URL is not a valid URL", url);
 * </pre>
 * 
 * @author <a href="mailto:alexander(dot)klimetschek(at)mindquarry(dot)com">
 *         Alexander Klimetschek</a>
 *
 */
public abstract class ErrorDisplayingPreferencePage extends PreferencePage {

    Map<Control, Composite> errorBorderComposites = new HashMap<Control, Composite>();
    Map<Composite, Color> currentErrorComposites = new HashMap<Composite, Color>();

    public ErrorDisplayingPreferencePage() {
        super();
    }

    public ErrorDisplayingPreferencePage(String title) {
        super(title);
    }

    public ErrorDisplayingPreferencePage(String title, ImageDescriptor image) {
        super(title, image);
    }

    public Composite createErrorBorderComposite(Composite parent) {
        return createErrorBorderComposite(parent, 0);
    }
    
    public Composite createErrorBorderComposite(Composite parent, int border) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL|
                GridData.FILL_VERTICAL));
        GridLayout layout = new GridLayout(1, true);
        layout.marginHeight = border;
        layout.marginWidth = border;
        composite.setLayout(layout);
        return composite;
    }

    public void registerErrorBorderComposite(Composite composite, Control control) {
        errorBorderComposites.put(control, composite);
    }

    protected void setInvalid(String message, Control... controls) {
        // restore the old color on all previous error composites
        for (Composite composite : currentErrorComposites.keySet()) {
            composite.setBackground(currentErrorComposites.get(composite));
        }
        currentErrorComposites.clear();
        // set the red error color on all new composites for each control
        for (Control control: controls) {
            if (errorBorderComposites.containsKey(control)) {
                Composite composite = errorBorderComposites.get(control);
                currentErrorComposites.put(composite, composite.getBackground());
                composite.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
            }
        }
        setErrorMessage(message);
        setValid(false);
    }

    protected void setValid() {
        // restore the old color on all previous error composites
        for (Composite composite : currentErrorComposites.keySet()) {
            composite.setBackground(currentErrorComposites.get(composite));
        }
        currentErrorComposites.clear();
        setErrorMessage(null);
        setMessage(null, INFORMATION);
        setValid(true);
    }

}