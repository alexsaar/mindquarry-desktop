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
package com.mindquarry.desktop.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/**
 * This class creates a preference page for shortcuts.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class GeneralSettingsPage extends FieldEditorPreferencePage {
    /**
     * ShortcutsPage default constructor
     */
    public GeneralSettingsPage() {
        super("Common Settings", FieldEditorPreferencePage.GRID);
        setDescription("Manage common settings of the application.");
        setImageDescriptor(ImageDescriptor
                .createFromImage(new Image(
                        null,
                        getClass()
                                .getResourceAsStream(
                                        "/com/mindquarry/icons/16x16/logo/mindquarry-icon.png"))));
    }

    @Override
    protected void createFieldEditors() {
        BooleanFieldEditor encryptPasswords = new BooleanFieldEditor(
                "com.mindquarry.desktop.encrypt", //$NON-NLS-1$
                "&" + //$NON-NLS-1$
                        "Encrypt sentitive data", getFieldEditorParent());
        addField(encryptPasswords);
    }
}
