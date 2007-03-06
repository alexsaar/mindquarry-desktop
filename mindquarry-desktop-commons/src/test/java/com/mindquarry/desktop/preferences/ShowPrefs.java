package com.mindquarry.desktop.preferences;
import java.io.File;
import java.io.IOException;

import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.mindquarry.desktop.preferences.FilteredPreferenceDialog;
import com.mindquarry.desktop.preferences.GeneralSettingsPage;
import com.mindquarry.desktop.preferences.ServerProfilesPage;
import com.mindquarry.desktop.preferences.ShortcutsPage;

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
        PreferenceManager mgr = new PreferenceManager();
        GeneralSettingsPage general = new GeneralSettingsPage();
        mgr.addToRoot(new PreferenceNode("general", general));
        mgr.addTo("general", new PreferenceNode("profiles", new ServerProfilesPage()));
        mgr.addTo("general", new PreferenceNode("shortcuts", new ShortcutsPage()));
        
        // Set the preference store
        File prefFile = new File("minutes-editor.properties");
        if(!prefFile.exists()) {
            prefFile.createNewFile();
        }
        PreferenceStore ps = new PreferenceStore(prefFile.getAbsolutePath());
        ps.load();

        // Create the preferences dialog
        //PreferenceDialog dlg = new PreferenceDialog(new Shell(), mgr);
        FilteredPreferenceDialog dlg = new FilteredPreferenceDialog(new Shell(), mgr);
        dlg.setPreferenceStore(ps);
        dlg.setHelpAvailable(true);
        dlg.open();

        ps.save();
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
