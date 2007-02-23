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
package com.mindquarry.minutes.editor;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import com.mindquarry.minutes.editor.action.AddMemberAction;
import com.mindquarry.minutes.editor.action.EditPreferencesAction;
import com.mindquarry.minutes.editor.action.NewConversationAction;
import com.mindquarry.minutes.editor.action.OpenConversationAction;
import com.mindquarry.minutes.editor.widget.ConversationWidget;
import com.mindquarry.minutes.editor.widget.PeopleWidget;

/**
 * Main class for the minutes editor.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class MinutesEditor extends ApplicationWindow {
    public static final String EDITOR_TITLE = "Mindquarry Minutes Editor"; //$NON-NLS-1$

    public static final String EDITOR_IMG_KEY = "editor-image"; //$NON-NLS-1$

    private Action newConversationAction;

    private Action openConversationAction;

    private Action addMemberAction;

    private Action editPreferencesAction;

    public MinutesEditor() {
        super(null);

        // create actions
        newConversationAction = new NewConversationAction();
        openConversationAction = new OpenConversationAction();
        addMemberAction = new AddMemberAction();
        editPreferencesAction = new EditPreferencesAction();
    }

    /**
     * The application entry point for the minutes editor.
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        MinutesEditor editor = new MinutesEditor();
        editor.addToolBar(SWT.FLAT | SWT.WRAP);
        editor.addStatusLine();
        editor.setBlockOnOpen(true);
        editor.open();
    }

    /**
     * @see org.eclipse.jface.window.ApplicationWindow#createToolBarManager(int)
     */
    @Override
    protected ToolBarManager createToolBarManager(int style) {
        ToolBarManager manager = super.createToolBarManager(style);
        manager.add(newConversationAction);
        manager.add(openConversationAction);
        manager.add(new Separator());
        manager.add(addMemberAction);
        manager.add(new Separator());
        manager.add(editPreferencesAction);
        return manager;
    }

    /**
     * Creates the main window's contents
     * 
     * @param parent the main window
     * @return Control
     */
    @Override
    protected Control createContents(Composite parent) {
        initImageRegistry();

        SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);
        sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));

        new PeopleWidget(sashForm, SWT.BORDER);
        new ConversationWidget(sashForm, SWT.BORDER);

        sashForm.setWeights(new int[] { 1, 3 });

        // init window shell
        getShell().setImage(JFaceResources.getImage(EDITOR_IMG_KEY));
        getShell().setText(EDITOR_TITLE);
        getShell().setSize(600, 400);

        setStatus("Ready.");
        return parent;
    }

    /**
     * Initialzes the image registry for the minutes editor.
     */
    private void initImageRegistry() {
        ImageRegistry reg = JFaceResources.getImageRegistry();

        Image img = new Image(
                Display.getCurrent(),
                MinutesEditor.class
                        .getResourceAsStream("/com/mindquarry/icons/16x16/logo/mindquarry-icon.png")); //$NON-NLS-1$
        reg.put(EDITOR_IMG_KEY, img);
    }
}
