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
package com.mindquarry.client;

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.GetTrayItemLocationHackMacOSX;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.mindquarry.client.ballon.MindClientBallonWidget;
import com.mindquarry.client.util.os.OperatingSystem;
import com.mindquarry.client.util.widgets.IconActionThread;
import com.mindquarry.client.workspace.WorkspaceSynchronizeListener;
import com.mindquarry.desktop.DesktopConstants;
import com.mindquarry.desktop.preferences.PreferenceUtilities;
import com.mindquarry.desktop.preferences.dialog.FilteredPreferenceDialog;
import com.mindquarry.desktop.preferences.profile.Profile;

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

    public static final OperatingSystem OS = getOperatingSystem();

    private static Shell shell;

    private final Image icon;

    private final BeanFactory factory;

    private File prefFile;

    private static TrayItem item;

    private static IconActionThread iconAction;

    private PreferenceStore store;

    public MindClient() throws IOException {
        factory = new ClassPathXmlApplicationContext(
                new String[] { "applicationContext.xml" }); //$NON-NLS-1$

        icon = new Image(Display.getCurrent(), getClass().getResourceAsStream(
                DesktopConstants.MINDQUARRY_ICON));

        // init settings
        prefFile = new File(PREF_FILE);

        // load preferences
        PreferenceUtilities.checkPreferenceFile(prefFile);
        store = new PreferenceStore(prefFile.getAbsolutePath());
        store.load();
    }

    public static void main(String[] args) throws IOException {
        // init display & shell
        Display display = new Display();
        Display.setAppName(APPLICATION_NAME);
        display.setWarnings(true);

        shell = new Shell(display, SWT.NONE);
        shell.setText(APPLICATION_NAME);

        MindClient mindclient = new MindClient();
        mindclient.checkArguments(args);
        mindclient.createTrayIconAndMenu(display);

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
            showPreferenceDialog();
        } else if ((args.length == 3) && (!prefFile.exists())) {
            addNewProfile(args[0], args[1], args[2]);
            showPreferenceDialog();
        } else if (!prefFile.exists()) {
            addNewProfile(Messages.getString("MindClient.8"), "http://server", //$NON-NLS-1$ //$NON-NLS-2$
                    "your login name"); //$NON-NLS-1$
            showPreferenceDialog();
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
                display, this, item);

        final Menu menu = new Menu(shell, SWT.POP_UP);
        if (MindClient.getOperatingSystem() == OperatingSystem.MAC_OS_X) {
            // Mac does not use the right mouse for tray icons

            // left-click => menu
            item.addSelectionListener(new SelectionListener() {
                // single-click
                public void widgetSelected(SelectionEvent e) {
                    menu.setLocation(GetTrayItemLocationHackMacOSX
                            .getAlignedLocation(item));
                    menu.setVisible(true);
                }

                // double-click
                public void widgetDefaultSelected(SelectionEvent e) {
                }
            });
            // extra item in menu => balloon window
            MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
            menuItem.setText(Messages.getString("MindClient.9")); //$NON-NLS-1$
            menuItem.addListener(SWT.Selection, new Listener() {
                public void handleEvent(Event event) {
                    ballonWindow.toggleBalloon();
                }
            });
        } else {
            // Windows/Gnome

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
        }
        // go to webpage
        MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
        menuItem.setText(Messages.getString("MindClient.10")); //$NON-NLS-1$
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
        menuItem.setText(Messages.getString("MindClient.11")); //$NON-NLS-1$
        menuItem.addListener(SWT.Selection, WorkspaceSynchronizeListener
                .getInstance(this, menuItem, null));

        // add separator
        menuItem = new MenuItem(menu, SWT.SEPARATOR);

        // options dialog
        menuItem = new MenuItem(menu, SWT.PUSH);
        menuItem.setText(Messages.getString("MindClient.0")); //$NON-NLS-1$
        menuItem.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                try {
                    showPreferenceDialog();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        // add separator
        menuItem = new MenuItem(menu, SWT.SEPARATOR);

        // close application
        menuItem = new MenuItem(menu, SWT.PUSH);
        menuItem.setText(Messages.getString("MindClient.1")); //$NON-NLS-1$
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

    private void loadOptions() {
        try {
            store.load();
        } catch (Exception e) {
            showErrorMessage(Messages.getString("MindClient.5")); //$NON-NLS-1$
        }
    }

    private void showPreferenceDialog() throws IOException {
        // request preference values from user
        PreferenceManager mgr = PreferenceUtilities
                .getDefaultPreferenceManager();

        // Create the preferences dialog
        FilteredPreferenceDialog dlg = new FilteredPreferenceDialog(getShell(),
                mgr);
        dlg.setPreferenceStore(store);
        dlg.setHelpAvailable(true);
        dlg.open();
        saveOptions();
    }

    public void saveOptions() {
        try {
            store.save();
        } catch (Exception e) {
            showErrorMessage(Messages.getString("MindClient.7")); //$NON-NLS-1$
        }
    }

    @Override
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

    public BeanFactory getFactory() {
        return factory;
    }

    private static OperatingSystem getOperatingSystem() {
        String os = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
        if (os.startsWith("windows")) { //$NON-NLS-1$
            return OperatingSystem.WINDOWS;
        } else if (os.startsWith("mac")) { //$NON-NLS-1$
            return OperatingSystem.MAC_OS_X;
        } else {
            return OperatingSystem.OTHER;
        }
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
                tip.setText(Messages.getString("MindClient.12")); //$NON-NLS-1$
                item.setToolTip(tip);
                tip.setAutoHide(true);
                tip.setVisible(true);
            }
        });
    }
}
