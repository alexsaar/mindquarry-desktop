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
package com.mindquarry.minutes.editor.widget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Add summary documentation here.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class ConversationWidget extends EditorWidget {
    /**
     * {@inheritDoc}
     */
    public ConversationWidget(Composite parent, int style) {
        super(parent, style);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.mindquarry.minutes.editor.widget.EditorWidget#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createContents(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText("Conversation widget area");
    }
}
