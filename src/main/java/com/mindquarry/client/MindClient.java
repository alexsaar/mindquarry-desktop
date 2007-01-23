/*
 * Copyright (C) 2006-2007 MindQuarry GmbH, All Rights Reserved
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

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.mindquarry.client.dialog.OptionsDialog;
import com.mindquarry.client.options.Profile;
import com.mindquarry.client.options.ProfileList;
import com.mindquarry.client.tray.TrayIconSelectionListener;
import com.mindquarry.client.util.os.HomeUtil;
import com.mindquarry.client.util.os.OperatingSystem;

/**
 * @author <a href="mailto:lars(dot)trieloff(at)mindquarry(dot)com">Lars
 *         Trieloff</a>
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class MindClient {
    public static final String APPLICATION_NAME = "Mindquarry Desktop Client"; //$NON-NLS-1$

    public static final String MINDCLIENT_SETTINGS = HomeUtil
            .getSettingsFolder()
            + "/mindclient.settings"; //$NON-NLS-1$

    public static final String MINDCLIENT_ICON = "/icons/16x16/mindquarry.png"; //$NON-NLS-1$

    private final OperatingSystem OS;

    private static Shell shell;

    private final Image icon;

    private final BeanFactory factory;

    private ProfileList profileList;

    private File optionsFile;

    public MindClient() {
        factory = new ClassPathXmlApplicationContext(
                new String[] { "applicationContext.xml" }); //$NON-NLS-1$

        icon = new Image(Display.getCurrent(), getClass().getResourceAsStream(
                MINDCLIENT_ICON));

        // init settings file & name
        profileList = new ProfileList();
        optionsFile = new File(MINDCLIENT_SETTINGS);

        // check underlying OS version
        OperatingSystemMXBean rtBean = ManagementFactory
                .getOperatingSystemMXBean();
        if (rtBean.getName().startsWith("Windows")) { //$NON-NLS-1$
            OS = OperatingSystem.WINDOWS;
        } else {
            OS = OperatingSystem.OTHER;
        }
    }

    public static void main(String[] args) throws IOException {
        final MindClient mindclient = new MindClient();

        // init display & shell
        final Display display = Display.getCurrent();
        display.setWarnings(true);
        Display.setAppName(APPLICATION_NAME);
        shell = new Shell(display, SWT.NONE);
        shell.setText(APPLICATION_NAME);

        // check CLI arguments
        if (args.length == 3) {
            Profile profile = new Profile();
            profile.setName(args[0]);
            profile.setEndpoint(args[1]);
            profile.setLogin(args[2]);

            mindclient.profileList.addProfile(profile);

            OptionsDialog dlg = new OptionsDialog(MindClient.getShell(),
                    mindclient.icon, mindclient.profileList);
            if (dlg.open() == Window.OK) {
                mindclient.optionsFile.getParentFile().mkdirs();
                mindclient.optionsFile.createNewFile();
            }
            mindclient.saveOptions();
        } else {
            // if something went wrong, try to load local options
            mindclient.loadOptions();
        }
        final Tray tray = display.getSystemTray();
        final TrayIconSelectionListener trayListener = new TrayIconSelectionListener(
                display, mindclient);

        final TrayItem item = new TrayItem(tray, SWT.NONE);
        item.setImage(mindclient.icon);
        item.addListener(SWT.Selection, new Listener() {
            public void handleEvent(final Event event) {
                trayListener.handleEvent(event);
            }
        });

        final Menu menu = new Menu(shell, SWT.POP_UP);
        item.addListener(SWT.MenuDetect, new Listener() {
            public void handleEvent(Event event) {
                menu.setVisible(true);
            }
        });
        MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
        menuItem.setText(Messages.getString("MindClient.0")); //$NON-NLS-1$
        menuItem.addListener(SWT.Selection, new Listener() {
            /**
             * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
             */
            public void handleEvent(Event event) {
                OptionsDialog dlg = new OptionsDialog(shell, mindclient.icon,
                        mindclient.profileList);
                if (dlg.open() == Window.OK) {
                    mindclient.saveOptions();
                }
            }
        });
        menuItem = new MenuItem(menu, SWT.SEPARATOR);

        menuItem = new MenuItem(menu, SWT.PUSH);
        menuItem.setText(Messages.getString("MindClient.1")); //$NON-NLS-1$
        menuItem.addListener(SWT.Selection, new Listener() {
            /**
             * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
             */
            public void handleEvent(Event event) {
                System.exit(1);
            }
        });
        while (!display.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }

    private void loadOptions() {
        try {
            if (!optionsFile.exists()) {
                // init options with dummy values
                Profile profile = new Profile();
                profile.setName("Your Quarry Profile");
                profile.setEndpoint(Messages.getString("MindClient.3")); //$NON-NLS-1$
                profile.setLogin(Messages.getString("MindClient.2")); //$NON-NLS-1$

                profileList.addProfile(profile);

                // request option values from user
                OptionsDialog dlg = new OptionsDialog(MindClient.getShell(),
                        icon, profileList);
                if (dlg.open() == Window.OK) {
                    optionsFile.getParentFile().mkdirs();
                    optionsFile.createNewFile();
                    saveOptions();
                }
            } else {
                FileInputStream fis = new FileInputStream(optionsFile);

                XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(
                        new FileInputStream(optionsFile)));
                profileList = (ProfileList)decoder.readObject();
                decoder.close();
            }
        } catch (Exception e) {
            MessageDialog.openError(shell, Messages.getString("MindClient.4"), //$NON-NLS-1$
                    Messages.getString("MindClient.5")); //$NON-NLS-1$
        }
    }

    public void saveOptions() {
        try {
            if (!optionsFile.exists()) {
                optionsFile.getParentFile().mkdirs();
                optionsFile.createNewFile();
            }
            XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(optionsFile)));
            encoder.writeObject(profileList);
            encoder.close();
        } catch (Exception e) {
            MessageDialog.openError(shell, Messages.getString("MindClient.4"), //$NON-NLS-1$
                    Messages.getString("MindClient.7")); //$NON-NLS-1$
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

    public ProfileList getProfileList() {
        return profileList;
    }

    public static Shell getShell() {
        return shell;
    }

    public BeanFactory getFactory() {
        return factory;
    }

    public OperatingSystem getOS() {
        return OS;
    }
}
