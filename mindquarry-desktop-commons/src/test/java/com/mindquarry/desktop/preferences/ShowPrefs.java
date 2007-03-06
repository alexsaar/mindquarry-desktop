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

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.mindquarry.desktop.preferences.dialog.FilteredPreferenceDialog;
import com.mindquarry.desktop.preferences.pages.ShortcutsPage;

/**
 * This class demonstrates JFace preferences
 */
public class ShowPrefs {
    /**
     * Runs the application
     */
    public void run() throws IOException {
        Display display = new Display();

        // create pref manager and add nodes
        PreferenceManager mgr = PreferenceUtilities
                .getDefaultPreferenceManager();
        mgr.addTo("general", new PreferenceNode("shortcuts",
                new ShortcutsPage()));

        // Set the preference store
        File prefFile = new File("./preference-test.properties"); //$NON-NLS-1$
        PreferenceUtilities.checkPreferenceFile(prefFile);
        PreferenceStore store = new PreferenceStore(prefFile.getAbsolutePath());
        store.load();

        // Create the preferences dialog
        // PreferenceDialog dlg = new PreferenceDialog(new Shell(), mgr);
        FilteredPreferenceDialog dlg = new FilteredPreferenceDialog(
                new Shell(), mgr);
        dlg.setPreferenceStore(store);
        dlg.setHelpAvailable(true);
        dlg.open();

        store.save();
        display.dispose();
    }

    /**
     * The application entry point
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        new ShowPrefs().run();
    }
}
