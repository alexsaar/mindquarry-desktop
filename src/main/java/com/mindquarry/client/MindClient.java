/**
 * Copyright (C) 2005 MindQuarry GmbH, All Rights Reserved
 */
package com.mindquarry.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

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
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;

import com.mindquarry.client.dialog.OptionsDialog;
import com.mindquarry.client.tray.TrayIconSelectionListener;

/**
 * @author <a href="mailto:lars(dot)trieloff(at)mindquarry(dot)com">Lars
 *         Trieloff</a>
 */
public class MindClient {
    public static final String ENDPOINT_KEY = "endpoint"; //$NON-NLS-1$

    public static final String PASSWORD_KEY = "password"; //$NON-NLS-1$

    public static final String LOGIN_KEY = "login"; //$NON-NLS-1$

    public static final String MINDCLIENT_SETTINGS = "mindclient.settings"; //$NON-NLS-1$

    private static Shell shell;

    private Image icon = new Image(Display.getCurrent(), getClass()
            .getResourceAsStream("/icons/16x16/mindquarry.png")); //$NON-NLS-1$

    private Properties options;

    private File optionsFile;

    public static void main(String[] args) {
        final MindClient mindclient = new MindClient();
        mindclient.loadOptions();

        // init tray icon
        final Display display = Display.getCurrent();
        shell = new Shell(SWT.NONE);
        Tray tray = display.getSystemTray();

        if (tray != null) {
            TrayItem ti = new TrayItem(tray, SWT.NONE);
            ti.setImage(mindclient.icon);
            ti.addSelectionListener(new TrayIconSelectionListener(display,
                    mindclient));

            final Menu menu = new Menu(shell, SWT.POP_UP);
            ti.addListener(SWT.MenuDetect, new Listener() {
                public void handleEvent(Event event) {
                    menu.setVisible(true);
                }
            });
            MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
            menuItem.setText("Options...");
            menuItem.addListener(SWT.Selection, new Listener() {
                /**
                 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
                 */
                public void handleEvent(Event event) {
                    OptionsDialog dlg = new OptionsDialog(shell,
                            mindclient.icon, mindclient.options);
                    if (dlg.open() == Window.OK) {
                        mindclient.saveOptions();
                    }
                }
            });
            menuItem = new MenuItem(menu, SWT.SEPARATOR);

            menuItem = new MenuItem(menu, SWT.PUSH);
            menuItem.setText("Close");
            menuItem.addListener(SWT.Selection, new Listener() {
                /**
                 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
                 */
                public void handleEvent(Event event) {
                    System.exit(1);
                }
            });
        } else {
            // there must be a tray
            System.exit(1);
        }
        // init javaSVN for http & https
        DAVRepositoryFactory.setup();
        // init javaSVN for SVN (over svn and svn+ssh)
        SVNRepositoryFactoryImpl.setup();
        
        while (!tray.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }

    private void loadOptions() {
        options = new Properties();

        optionsFile = new File(MINDCLIENT_SETTINGS);
        try {
            if (!optionsFile.exists()) {
                optionsFile.createNewFile();
                initOptions(options);
                saveOptions();
            } else {
                FileInputStream fis = new FileInputStream(optionsFile);
                options.loadFromXML(fis);
                fis.close();
            }
        } catch (Exception e) {
            MessageDialog.openError(shell, "Error",
                    "Could not load MindClient settings.");
        }
    }

    private void initOptions(Properties props) {
        props.put(LOGIN_KEY, "Your login for your quarry installation.");
        props.put(PASSWORD_KEY, "password");
        props.put(ENDPOINT_KEY, "The location of your quarry installation.");
    }

    private void saveOptions() {
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(optionsFile);
            options.storeToXML(fos, "MindCLient Settings");
        } catch (Exception e) {
            MessageDialog.openError(shell, "Error",
                    "Could not save MindClient settings.");
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

    public Properties getOptions() {
        return options;
    }

    public static Shell getShell() {
        return shell;
    }
}
