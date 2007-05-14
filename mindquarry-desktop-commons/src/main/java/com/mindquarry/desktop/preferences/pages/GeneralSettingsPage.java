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

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/**
 * This class creates a preference page for shortcuts.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class GeneralSettingsPage extends FieldEditorPreferencePage {
    public static final String NAME = "general"; //$NON-NLS-1$

    public static final String NOTIFY_DELAY = "com.mindquarry.desktop.notify.delay"; //$NON-NLS-1$

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

    protected void createFieldEditors() {
        IPreferenceStore store = getPreferenceStore();
        store.setDefault(NOTIFY_DELAY, 2);

        IntegerFieldEditor notifyDisplayDelay = new IntegerFieldEditor(
                NOTIFY_DELAY, "Notification Message Delay (in seconds):",
                getFieldEditorParent());
        addField(notifyDisplayDelay);
    }
}
