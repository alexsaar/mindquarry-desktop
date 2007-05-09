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
package com.mindquarry.desktop.client;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;

import com.mindquarry.desktop.DesktopConstants;
import com.mindquarry.desktop.client.ballon.MindClientBallonWidget;
import com.mindquarry.desktop.client.util.widgets.IconActionThread;
import com.mindquarry.desktop.client.workspace.WorkspaceSynchronizeListener;
import com.mindquarry.desktop.preferences.PreferenceUtilities;
import com.mindquarry.desktop.preferences.dialog.FilteredPreferenceDialog;
import com.mindquarry.desktop.preferences.pages.ServerProfilesPage;
import com.mindquarry.desktop.preferences.pages.TaskPage;
import com.mindquarry.desktop.preferences.profile.Profile;
import com.mindquarry.desktop.splash.SplashScreen;

/**
 * Main class for the MindClient. Responsible for managing persistence of
 * options and creation of initial GUI types.
 * 
 * @author <a href="mailto:lars(dot)trieloff(at)mindquarry(dot)com">Lars
 *         Trieloff</a>
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class MindClient {
    public static final String APPLICATION_NAME = "Mindquarry Desktop Client"; //$NON-NLS-1$

    public static final String PREF_FILE = PreferenceUtilities.SETTINGS_FOLDER
            + "/mindclient.settings"; //$NON-NLS-1$
    
    private Log log;

    private static Shell shell;

    private final Image icon;

    private File prefFile;

    private static TrayItem item;

    private static IconActionThread iconAction;

    private PreferenceStore store;

    public MindClient() throws IOException {
        log = LogFactory.getLog(MindClient.class);
        icon = new Image(Display.getCurrent(), getClass().getResourceAsStream(
                DesktopConstants.MINDQUARRY_ICON));
        Window.setDefaultImage(icon);

        // init settings
        prefFile = new File(PREF_FILE);

        // load preferences
        store = new PreferenceStore(prefFile.getAbsolutePath());
    }

    public static void main(String[] args) throws IOException {
        // init display & shell
        Display display = new Display();
        Display.setAppName(APPLICATION_NAME);
        display.setWarnings(true);

        shell = new Shell(display, SWT.ON_TOP);
        shell.setText(APPLICATION_NAME);
        
        // init splash screen
        SplashScreen splash = SplashScreen.newInstance(3);
        splash.show();

        // init client
        MindClient mindclient = new MindClient();
        splash.step();
        
        mindclient.checkArguments(args);
        splash.step();
        
        mindclient.createTrayIconAndMenu(display);
        splash.step();

        while (!display.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }

    private void checkArguments(String[] args) throws IOException {
        if ((args.length == 3) && (prefFile.exists())) {
            loadOptions();
            addNewProfile(args[0], args[1], args[2]);
            showPreferenceDialog(true);
        } else if ((args.length == 3) && (!prefFile.exists())) {
            addNewProfile(args[0], args[1], args[2]);
            showPreferenceDialog(true);
        } else if (!prefFile.exists()) {
            addNewProfile("Your Mindquarry Server Profile", "http://server", //$NON-NLS-2$
                    "your login name"); //$NON-NLS-1$
            showPreferenceDialog(true);
        } else {
            loadOptions();
        }
    }

    private boolean addNewProfile(String name, String endpoint, String login) {
        // add new profile entry
        Profile profile = new Profile();
        profile.setName(name);
        profile.setServerURL(endpoint);
        profile.setLogin(login);
        profile.setPassword(""); //$NON-NLS-1$
        profile.setWorkspaceFolder(""); //$NON-NLS-1$
        return Profile.addProfile(store, profile);
    }

    private void createTrayIconAndMenu(Display display) {
        Tray tray = display.getSystemTray();

        item = new TrayItem(tray, SWT.NONE);
        item.setImage(icon);

        final MindClientBallonWidget ballonWindow = new MindClientBallonWidget(
                display, this);

        final Menu menu = new Menu(shell, SWT.POP_UP);
        // right-click / context menu => menu
        item.addListener(SWT.MenuDetect, new Listener() {
            public void handleEvent(Event event) {
                menu.setVisible(true);
            }
        });

        // left-click => balloon window
        item.addListener(SWT.Selection, new Listener() {
            public void handleEvent(final Event event) {
                ballonWindow.handleEvent(event);
            }
        });
        // go to webpage
        MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
        menuItem.setText("Go to webpage");
        menuItem.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                if (Profile.getSelectedProfile(store) != null) {
                    Program.launch(Profile.getSelectedProfile(store)
                            .getServerURL());
                }
            }
        });
        // synchronize
        menuItem = new MenuItem(menu, SWT.PUSH);
        menuItem.setText("Synchronize");
        menuItem.addListener(SWT.Selection, WorkspaceSynchronizeListener
                .getInstance(this, menuItem, null));

        // add separator
        menuItem = new MenuItem(menu, SWT.SEPARATOR);

        // options dialog
        menuItem = new MenuItem(menu, SWT.PUSH);
        menuItem.setText("Options...");
        menuItem.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                try {
                    showPreferenceDialog(false);
                } catch (IOException e) {
                    log.error("Error in preferences dialog", e);
                }
            }
        });
        // add separator
        menuItem = new MenuItem(menu, SWT.SEPARATOR);

        // close application
        menuItem = new MenuItem(menu, SWT.PUSH);
        menuItem.setText("Close");
        menuItem.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                saveOptions();
                System.exit(1);
            }
        });
        iconAction = new IconActionThread(item);
        iconAction.setDaemon(true);
        iconAction.start();
    }

    private void showPreferenceDialog(boolean showProfiles) throws IOException {
        loadOptions();

        // request preference values from user
        PreferenceManager mgr = PreferenceUtilities
                .getDefaultPreferenceManager();
        mgr.addToRoot(new PreferenceNode(TaskPage.NAME, new TaskPage()));

        // Create the preferences dialog
        FilteredPreferenceDialog dlg = new FilteredPreferenceDialog(getShell(),
                mgr);
        dlg.setPreferenceStore(store);
        if (showProfiles) {
            dlg.setSelectedNode(ServerProfilesPage.NAME);
        }
        dlg.open();
        saveOptions();
    }

    private void loadOptions() {
        try {
            PreferenceUtilities.checkPreferenceFile(prefFile);
            store.load();
        } catch (Exception e) {
            showErrorMessage("Could not load MindClient settings.");
        }
    }

    public void saveOptions() {
        try {
            store.save();
        } catch (Exception e) {
            showErrorMessage("Could not save MindClient settings.");
        }
    }

    protected void finalize() throws Throwable {
        if (icon != null) {
            icon.dispose();
        }
    }

    public final Image getIcon() {
        return icon;
    }

    public PreferenceStore getPreferenceStore() {
        return store;
    }

    public static Shell getShell() {
        return shell;
    }

    public static IconActionThread getIconActionHandler() {
        return MindClient.iconAction;
    }

    public static void showErrorMessage(final String message) {
        shell.getDisplay().asyncExec(new Runnable() {
            public void run() {
                final ToolTip tip = new ToolTip(shell, SWT.BALLOON
                        | SWT.ICON_ERROR);
                tip.setMessage(message);
                tip.setText("An error occured");
                item.setToolTip(tip);
                tip.setAutoHide(true);
                tip.setVisible(true);
            }
        });
    }
}
