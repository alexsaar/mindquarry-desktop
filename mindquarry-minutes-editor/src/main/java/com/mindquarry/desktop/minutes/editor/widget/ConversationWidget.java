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

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;

import com.mindquarry.desktop.minutes.editor.MinutesEditor;
import com.mindquarry.desktop.minutes.editor.action.AddMemberAction;

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
     * @see com.mindquarry.desktop.minutes.editor.widget.EditorWidget#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createContents(Composite parent) {
        Composite comp = new Composite(parent, SWT.NULL);
        comp.setLayout(new GridLayout(2, false));

        // create topic label
        Label label = new Label(comp, SWT.LEFT);
        label.setText("Topic" + ":"); //$NON-NLS-2$
        label.setFont(JFaceResources
                .getFont(MinutesEditor.CONV_TITLE_FONT_KEY));

        label = new Label(comp, SWT.LEFT);
        label.setText("something that has to be discussed");
        label.setFont(JFaceResources
                .getFont(MinutesEditor.CONV_TOPIC_TITLE_FONT_KEY));

        MenuManager mm = new MenuManager("Conversation Menu");
        mm.add(MinutesEditor.getAction(AddMemberAction.class.getName()));

        Menu menu = mm.createContextMenu(this);
        label.setMenu(menu);
        
        setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
    }
}
