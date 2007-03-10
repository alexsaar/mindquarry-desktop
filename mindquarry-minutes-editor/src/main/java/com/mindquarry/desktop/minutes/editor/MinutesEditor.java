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
package com.mindquarry.desktop.minutes.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import com.mindquarry.desktop.minutes.editor.action.ActionBase;
import com.mindquarry.desktop.minutes.editor.action.AddMemberAction;
import com.mindquarry.desktop.minutes.editor.action.EditPreferencesAction;
import com.mindquarry.desktop.minutes.editor.action.NewConversationAction;
import com.mindquarry.desktop.minutes.editor.action.OpenConversationAction;
import com.mindquarry.desktop.minutes.editor.splash.SplashScreen;
import com.mindquarry.desktop.minutes.editor.widget.ConversationWidget;
import com.mindquarry.desktop.minutes.editor.widget.PeopleWidget;

/**
 * Main class for the minutes editor.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class MinutesEditor extends ApplicationWindow {
    public static final String EDITOR_TITLE = "Mindquarry Minutes Editor"; //$NON-NLS-1$

    public static final String EDITOR_IMG_KEY = "editor-image"; //$NON-NLS-1$

    public static final String SMILE_IMG_KEY = "smile"; //$NON-NLS-1$

    public static final String CONV_TITLE_FONT_KEY = "conversation-title"; //$NON-NLS-1$

    public static final String CONV_TOPIC_TITLE_FONT_KEY = "conversation-topic-title"; //$NON-NLS-1$

    private static List<ActionBase> actions;

    static {
        // create actions
        actions = new ArrayList<ActionBase>();
        actions.add(new NewConversationAction());
        actions.add(new OpenConversationAction());
        actions.add(new AddMemberAction());
        actions.add(new EditPreferencesAction());
    }

    public static ActionBase getAction(String id) {
        for (ActionBase action : actions) {
            if (action.getId().equals(id)) {
                return action;
            }
        }
        return null;
    }

    public MinutesEditor() {
        super(null);
    }

    /**
     * The application entry point for the minutes editor.
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // show splash
        new SplashScreen().show();
        
        // run editor
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
        manager.add(getAction(NewConversationAction.class.getName()));
        manager.add(getAction(OpenConversationAction.class.getName()));
        manager.add(new Separator());
        manager.add(getAction(AddMemberAction.class.getName()));
        manager.add(new Separator());
        manager.add(getAction(EditPreferencesAction.class.getName()));
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
        initRegistries();

        SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);
        sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));

        new PeopleWidget(sashForm, SWT.BORDER);
        new ConversationWidget(sashForm, SWT.BORDER);

        sashForm.setWeights(new int[] { 1, 3 });

        // init window shell
        Window.setDefaultImage(JFaceResources.getImage(EDITOR_IMG_KEY));
        getShell().setImage(JFaceResources.getImage(EDITOR_IMG_KEY));
        getShell().setText(EDITOR_TITLE);
        getShell().setSize(600, 400);

        setStatus("Ready.");
        return parent;
    }

    /**
     * Initialzes the image registry for the minutes editor.
     */
    private void initRegistries() {
        ImageRegistry reg = JFaceResources.getImageRegistry();

        Image img = new Image(
                Display.getCurrent(),
                MinutesEditor.class
                        .getResourceAsStream("/com/mindquarry/icons/16x16/logo/mindquarry-icon.png")); //$NON-NLS-1$
        reg.put(EDITOR_IMG_KEY, img);
        img = new Image(
                Display.getCurrent(),
                MinutesEditor.class
                        .getResourceAsStream("/org/tango-project/tango-icon-theme/22x22/emotes/face-smile.png")); //$NON-NLS-1$
        reg.put(SMILE_IMG_KEY, img);

        FontRegistry fReg = JFaceResources.getFontRegistry();
        fReg.put(CONV_TOPIC_TITLE_FONT_KEY, new FontData[] { new FontData(
                "Arial", //$NON-NLS-1$
                10, SWT.ITALIC) });
        fReg.put(CONV_TITLE_FONT_KEY, new FontData[] { new FontData("Arial", //$NON-NLS-1$
                10, SWT.BOLD) });
    }
}
