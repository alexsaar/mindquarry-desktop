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
package com.mindquarry.minutes.editor.action;

import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.mindquarry.desktop.preferences.PreferenceUtilities;
import com.mindquarry.desktop.preferences.dialog.FilteredPreferenceDialog;

/**
 * Add summary documentation here.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class EditPreferencesAction extends ActionBase {
    private static final String TEXT = "Edit options";

    private static final String DESCRIPTION = "Configure the minutes editor by editing its preferences.";

    private static final Image IMAGE = new Image(
            Display.getCurrent(),
            EditPreferencesAction.class
                    .getResourceAsStream("/org/tango-project/tango-icon-theme/22x22/categories/preferences-system.png")); //$NON-NLS-1$

    public EditPreferencesAction() {
        setText(TEXT);
        setImageDescriptor(ImageDescriptor.createFromImage(IMAGE));
    }

    /**
     * @see org.eclipse.jface.action.Action#getText()
     */
    @Override
    public String getText() {
        return TEXT;
    }

    /**
     * @see org.eclipse.jface.action.Action#getDescription()
     */
    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    /**
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        PreferenceManager mgr = PreferenceUtilities
                .getDefaultPreferenceManager();
        FilteredPreferenceDialog dlg = new FilteredPreferenceDialog(null, mgr);
        dlg.setPreferenceStore(new PreferenceStore("minutes-editor.properties")); //$NON-NLS-1$
        dlg.open();
    }
}
