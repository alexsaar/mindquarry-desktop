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
package com.mindquarry.desktop.minutes.editor.widget;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * Abstract base class for all minutes editor widgets. Provides base
 * functionality that is common across all widgets like calling
 * {@link EditorWidget#createContents(Composite)} method for initializing the
 * widgets content.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public abstract class EditorWidget extends Composite {
    /**
     * {@inheritDoc}
     * 
     * In addition to normal initialization the constructor calls the virtual
     * createContents() method that must be overriden by subclasses and which is
     * used for initializing the widgets content.
     */
    public EditorWidget(Composite parent, int style) {
        super(parent, style);

        // init layout and content
        setLayout(new FillLayout());
        createContents(this);
    }

    /**
     * This method should be overriden by subclasses for initializing the
     * widgets content.
     * 
     * @param parent the parent widget to be used
     */
    protected abstract void createContents(Composite parent);
}
