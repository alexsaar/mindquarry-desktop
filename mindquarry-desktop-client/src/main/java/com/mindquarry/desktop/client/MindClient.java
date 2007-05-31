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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;

import com.mindquarry.desktop.DesktopConstants;
import com.mindquarry.desktop.client.ballon.MindClientBallonWidget;
import com.mindquarry.desktop.client.util.widgets.IconActionThread;
import com.mindquarry.desktop.client.workspace.WorkspaceSynchronizeListener;
import com.mindquarry.desktop.preferences.PreferenceUtilities;
import com.mindquarry.desktop.preferences.dialog.FilteredPreferenceDialog;
import com.mindquarry.desktop.preferences.pages.GeneralSettingsPage;
import com.mindquarry.desktop.preferences.pages.ServerProfilesPage;
import com.mindquarry.desktop.preferences.pages.TaskPage;
import com.mindquarry.desktop.preferences.profile.Profile;
import com.mindquarry.desktop.splash.SplashScreen;
import com.mindquarry.desktop.util.AutostartUtilities;
import com.mindquarry.desktop.widget.NotificationWidget;

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

    private static Shell shell;

    private final Image icon;

    private File prefFile;

    private static TrayItem item;

    private static IconActionThread iconAction;

    private PreferenceStore store;

    private MindClientBallonWidget ballonWindow;

    private Menu menu;

    private Menu profilesMenu;

    private List profilesInMenu = new ArrayList();

    //private Log log;

    public MindClient() throws IOException {
        icon = new Image(Display.getCurrent(), getClass().getResourceAsStream(
                DesktopConstants.MINDQUARRY_ICON));
        Window.setDefaultImage(icon);

        // init settings
        prefFile = new File(PREF_FILE);

        // load preferences
        store = new PreferenceStore(prefFile.getAbsolutePath());
        //log = LogFactory.getLog(MindClient.class);
    }

    public static void main(String[] args) throws IOException {
        // init display & shell
        Display display = new Display();
        Display.setAppName(APPLICATION_NAME);
        display.setWarnings(true);

        shell = new Shell(display, SWT.NONE);
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

    private void checkArguments(String[] args) {
        if (args.length == 3 && prefFile.exists()) {
            loadOptions();
            if (!profileNameExists(args[0])) {
                addNewProfile(args[0], args[1], args[2]);
                showPreferenceDialog(true, false);
            } else {
                // don't show dialog -- probably started via Windows desktop
                // link
            }
        } else if (args.length == 3 && !prefFile.exists()) {
            addNewProfile(args[0], args[1], args[2]);
            showPreferenceDialog(true);
        } else if (!prefFile.exists()) {
            addNewProfile(
                    Messages.getString(MindClient.class, "0"), "http://server", //$NON-NLS-1$//$NON-NLS-2$
                    "Login ID"); //$NON-NLS-1$
            showPreferenceDialog(true);
        } else {
            loadOptions();
        }
    }

    private boolean profileNameExists(String name) {
        String[] keys = store.preferenceNames();
        for (int i = 0; i < keys.length; i++) {
            if (keys[i].endsWith("." //$NON-NLS-1$
                    + Profile.PREF_NAME)
                    && store.getString(keys[i]).equals(name)) {
                return true;
            }
        }
        return false;
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

        ballonWindow = new MindClientBallonWidget(display, this);

        menu = new Menu(shell, SWT.POP_UP);
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

        // profiles sub menu
        MenuItem menuItem = new MenuItem(menu, SWT.CASCADE);
        menuItem.setText(Messages.getString(MindClient.class, "1")); //$NON-NLS-1$

        profilesMenu = new Menu(shell, SWT.DROP_DOWN);
        menuItem.setMenu(profilesMenu);

        // list all profiles in the menu
        updateProfileSelector();

        // add separator
        menuItem = new MenuItem(menu, SWT.SEPARATOR);

        // go to webpage
        menuItem = new MenuItem(menu, SWT.PUSH);
        menuItem.setText(Messages.getString(MindClient.class, "2")); //$NON-NLS-1$
        menuItem.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                if (Profile.getSelectedProfile(store) != null) {
                    Program.launch(Profile.getSelectedProfile(store)
                            .getServerURL());
                }
            }
        });
        menuItem
                .setImage(new Image(
                        Display.getCurrent(),
                        getClass()
                                .getResourceAsStream(
                                        "/org/tango-project/tango-icon-theme/16x16/apps/internet-web-browser.png"))); //$NON-NLS-1$
        // synchronize
        menuItem = new MenuItem(menu, SWT.PUSH);
        menuItem.setText(Messages.getString(MindClient.class, "3")); //$NON-NLS-1$
        menuItem.addListener(SWT.Selection, WorkspaceSynchronizeListener
                .getInstance(this, menuItem, null));
        menuItem
                .setImage(new Image(
                        Display.getCurrent(),
                        getClass()
                                .getResourceAsStream(
                                        "/com/mindquarry/icons/16x16/actions/synchronize-vertical.png"))); //$NON-NLS-1$

        // add separator
        menuItem = new MenuItem(menu, SWT.SEPARATOR);

        // options dialog
        menuItem = new MenuItem(menu, SWT.PUSH);
        menuItem.setText(Messages.getString(MindClient.class, "4") //$NON-NLS-1$
                + "..."); //$NON-NLS-1$
        menuItem.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                showPreferenceDialog(false);
            }
        });
        menuItem
                .setImage(new Image(
                        Display.getCurrent(),
                        getClass()
                                .getResourceAsStream(
                                        "/org/tango-project/tango-icon-theme/16x16/categories/preferences-system.png"))); //$NON-NLS-1$
        // add separator
        menuItem = new MenuItem(menu, SWT.SEPARATOR);

        // close application
        menuItem = new MenuItem(menu, SWT.PUSH);
        menuItem.setText(Messages.getString(MindClient.class, "5")); //$NON-NLS-1$
        menuItem.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                saveOptions();
                System.exit(1);
            }
        });
        menuItem
                .setImage(new Image(
                        Display.getCurrent(),
                        getClass()
                                .getResourceAsStream(
                                        "/org/tango-project/tango-icon-theme/16x16/actions/process-stop.png"))); //$NON-NLS-1$
        iconAction = new IconActionThread(item);
        iconAction.setDaemon(true);
        iconAction.start();
    }

    public void updateProfileSelector() {
        if ((menu == null) || (profilesMenu == null)) {
            return; // happens at very first start
        }
        MenuItem[] menuItems = profilesMenu.getItems();

        // remove the existing profiles first
        for (int i = 0; i < menuItems.length; i++) {
            if ((menuItems[i].getStyle() & SWT.RADIO) != 0) {
                menuItems[i].dispose();
            }
        }
        Iterator pIt = Profile.loadProfiles(getPreferenceStore()).iterator();
        boolean hasSelection = false;
        profilesInMenu = new ArrayList();
        int i = 0;
        while (pIt.hasNext()) {
            Profile profile = (Profile) pIt.next();
            final MenuItem menuItem = new MenuItem(profilesMenu, SWT.RADIO, i);
            i++;
            menuItem.setText(profile.getName());
            menuItem.addListener(SWT.Selection, new Listener() {
                public void handleEvent(Event event) {
                    Profile.selectProfile(getPreferenceStore(), menuItem
                            .getText());

                    // TODO trigger task synchronization, this needs
                    // interruption of the task synchronization
                    // TaskManager tm = TaskManager.getInstance();
                    // if (tm != null) {
                    // tm.asyncRefresh();
                    // }
                }
            });
            if ((Profile.getSelectedProfile(getPreferenceStore()) != null)
                    && (profile.getName().equals(Profile.getSelectedProfile(
                            getPreferenceStore()).getName()))) {
                menuItem.setSelection(true);
                hasSelection = true;
            }
            profilesInMenu.add(menuItem);
        }
        if (!hasSelection && profilesInMenu.size() > 0) {
            MenuItem mi = (MenuItem) profilesInMenu.get(0);
            mi.setSelection(true);
        }
    }

    private void showPreferenceDialog(boolean showProfiles) {
        showPreferenceDialog(showProfiles, true);
    }

    private void showPreferenceDialog(boolean showProfiles, boolean loadProfiles) {
        if (loadProfiles) {
            loadOptions();
        }

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
            showMessage(Messages.getString("com.mindquarry.desktop.client", //$NON-NLS-1$
                    "error"), //$NON-NLS-1$
                    Messages.getString(MindClient.class, "6") //$NON-NLS-1$
                            + ": "//$NON-NLS-1$
                            + e.toString());
        }
    }

    public void saveOptions() {
        try {
            store.save();
        } catch (Exception e) {
            showMessage(Messages.getString("com.mindquarry.desktop.client", //$NON-NLS-1$
                    "error"), //$NON-NLS-1$
                    Messages.getString(MindClient.class, "7") //$NON-NLS-1$
                            + ": "//$NON-NLS-1$
                            + e.toString());
        }
        updateProfileSelector();
        AutostartUtilities.setAutostart(store
                .getBoolean(GeneralSettingsPage.AUTOSTART),
                "mindquarry-desktop-client.jar"); //$NON-NLS-1$
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

    public synchronized void showMessage(final String title,
            final String message) {
        NotificationWidget widget = new NotificationWidget(shell.getDisplay());
        int delay = getPreferenceStore().getInt(GeneralSettingsPage.NOTIFY_DELAY);
        if (delay == 0) {
            // at first start the store returns 0 which doesn't make sense, so use a default:
            delay = GeneralSettingsPage.DEFAULT_NOTIFY_DELAY;
        }
        widget.show(title, message, getPreferenceStore().getInt(
                GeneralSettingsPage.NOTIFY_DELAY) * 1000);
        widget.dispose();
    }
}
