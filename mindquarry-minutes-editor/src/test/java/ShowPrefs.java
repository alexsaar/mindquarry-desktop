import java.io.File;
import java.io.IOException;

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

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
        mgr.addToRoot(new PreferenceNode("profiles", new ServerProfilesPage()));
        mgr.addToRoot(new PreferenceNode("shortcuts", new ShortcutsPage()));

        // Set the preference store
        File prefFile = new File("minutes-editor.properties");
        if(!prefFile.exists()) {
            prefFile.createNewFile();
        }
        PreferenceStore ps = new PreferenceStore(prefFile.getAbsolutePath());
        ps.load();

        // Create the preferences dialog
        PreferenceDialog dlg = new PreferenceDialog(new Shell(), mgr);
        dlg.setPreferenceStore(ps);
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
