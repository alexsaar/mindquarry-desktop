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

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.mindquarry.desktop.client.MindClient;
import com.mindquarry.desktop.client.widget.WidgetBase;

/**
 * @author <a href="saar(at)mindquarry(dot)com">Alexander Saar</a>
 */
public abstract class ContainerWidget<V extends Viewer> extends WidgetBase {
    public static final Color HIGHLIGHT_COLOR = new Color(Display.getCurrent(),
            233, 233, 251);

    protected Composite refreshWidget;
    protected Composite messageWidget;

    protected V viewer;

    protected boolean refreshing = false;

    public ContainerWidget(Composite parent, int style, MindClient client) {
        super(parent, style, client);
    }

    // #########################################################################
    // ### WIDGET METHODS
    // #########################################################################
    protected void createContents(Composite parent) {
        // initialize layout
        setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 1,
                1));
        setLayout(new GridLayout(1, true));
        ((GridLayout) getLayout()).horizontalSpacing = 0;
        ((GridLayout) getLayout()).verticalSpacing = 0;
        ((GridLayout) getLayout()).marginHeight = 0;
        ((GridLayout) getLayout()).marginWidth = 0;
    }

    // #########################################################################
    // ### PUBLIC AND PROTECTED METHODS
    // #########################################################################

    protected abstract void refresh();

    public V getViewer() {
        return viewer;
    }

    public void setViewer(V viewer) {
        this.viewer = viewer;
    }

    public void showErrorMessage(String message) {
        showMessage(message, "networkerror");
    }

    public void showEmptyMessage(String message) {
        showMessage(message, "info");
    }

    public abstract void showRefreshMessage(String message);

    public abstract void showEmptyMessage(boolean isEmpty);

    public abstract void showMessage(String message, String icon);
}
