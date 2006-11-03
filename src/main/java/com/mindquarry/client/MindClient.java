/**
 * Copyright (C) 2005 MindQuarry GmbH, All Rights Reserved
 */
package com.mindquarry.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

    public static final String PASSWORD_KEY = "password"; //$NON-NLS-1$

    public static final String LOGIN_KEY = "login"; //$NON-NLS-1$
    
    public static final String ENDPOINT_KEY = "endpoint"; //$NON-NLS-1$

    public static final String MINDCLIENT_SETTINGS = HomeUtil
            .getSettingsFolder()
            + "/mindclient.settings"; //$NON-NLS-1$

    private final OperatingSystem OS;

    private static Shell shell;

    private final Image icon;

    private final BeanFactory factory;

    private Properties options;

    private File optionsFile;

    private Log log;

    public MindClient() {
        log = LogFactory.getLog(MindClient.class);

        factory = new ClassPathXmlApplicationContext(
                new String[] { "applicationContext.xml" }); //$NON-NLS-1$

        icon = new Image(Display.getCurrent(), getClass().getResourceAsStream(
                "/icons/16x16/mindquarry.png")); //$NON-NLS-1$

        // init settings file & name
        options = new Properties();
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
        shell = new Shell(SWT.NONE);

        // check arguments
        if (mindclient.optionsFile.exists()) {
            mindclient.loadOptions();
        } else if (args.length == 1) {
            mindclient.options.put(ENDPOINT_KEY, args[0]);
            mindclient.options.put(LOGIN_KEY, ""); //$NON-NLS-1$
            mindclient.options.put(PASSWORD_KEY, ""); //$NON-NLS-1$

            OptionsDialog dlg = new OptionsDialog(MindClient.getShell(),
                    mindclient.icon, mindclient.options);
            if (dlg.open() == Window.OK) {
                mindclient.optionsFile.getParentFile().mkdirs();
                mindclient.optionsFile.createNewFile();
            }
            mindclient.saveOptions();
        } else if (args.length == 2) {
            mindclient.options.put(ENDPOINT_KEY, args[0]);
            mindclient.options.put(LOGIN_KEY, args[1]);
            mindclient.options.put(PASSWORD_KEY, ""); //$NON-NLS-1$

            OptionsDialog dlg = new OptionsDialog(MindClient.getShell(),
                    mindclient.icon, mindclient.options);
            if (dlg.open() == Window.OK) {
                mindclient.optionsFile.getParentFile().mkdirs();
                mindclient.optionsFile.createNewFile();
            }
            mindclient.saveOptions();
        } else if (args.length == 3) {
            mindclient.options.put(ENDPOINT_KEY, args[0]);
            mindclient.options.put(LOGIN_KEY, args[1]);
            mindclient.options.put(PASSWORD_KEY, args[2]);
            mindclient.saveOptions();
        } else {
            mindclient.loadOptions();
        }
        
        // check Java version
        RuntimeMXBean rtBean = ManagementFactory.getRuntimeMXBean();
        if (rtBean.getVmVersion().startsWith("1.5")) { //$NON-NLS-1$
            mindclient.log.info("Using Java 5");
        } else {
            mindclient.log.info("Using Java Version " + rtBean.getVmVersion());
        }
        
        // init tray icon
        Tray tray = display.getSystemTray();
        if (tray != null) {
            TrayItem ti = new TrayItem(tray, SWT.NONE);
            ti.setImage(mindclient.icon);
            ti.addSelectionListener(new TrayIconSelectionListener(display,
                    mindclient));
            ti.setToolTipText(APPLICATION_NAME);

            final Menu menu = new Menu(shell, SWT.POP_UP);
            ti.addListener(SWT.MenuDetect, new Listener() {
                public void handleEvent(Event event) {
                    menu.setVisible(true);
                }
            });
            MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
            menuItem.setText("Goto quarry...");
            menuItem.addListener(SWT.Selection, new Listener() {
                /**
                 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
                 */
                public void handleEvent(Event event) {
                    try {
                        // TODO add implementation for lunix & unix based
                        // systems here
                        Runtime.getRuntime().exec(
                                "start " //$NON-NLS-1$
                                        + mindclient.getOptions().getProperty(
                                                MindClient.ENDPOINT_KEY));
                    } catch (IOException e) {
                        MessageDialog.openError(MindClient.getShell(),
                                "Runtime Error",
                                "Could not open quarry installation.");
                    }
                }
            });
            menuItem = new MenuItem(menu, SWT.PUSH);
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
        while (!tray.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }

    private void loadOptions() {
        try {
            if (!optionsFile.exists()) {
                // init options with dummy values
                options.put(LOGIN_KEY,
                        "Your login for your quarry installation");
                options.put(PASSWORD_KEY, "password");
                options.put(ENDPOINT_KEY,
                        "The location of your quarry installation.");

                // request option values from user
                OptionsDialog dlg = new OptionsDialog(MindClient.getShell(),
                        icon, options);
                if (dlg.open() == Window.OK) {
                    optionsFile.getParentFile().mkdirs();
                    optionsFile.createNewFile();
                    saveOptions();
                }
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

    private void saveOptions() {
        FileOutputStream fos;
        try {
            if (!optionsFile.exists()) {
                optionsFile.getParentFile().mkdirs();
                optionsFile.createNewFile();
            }
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

    public BeanFactory getFactory() {
        return factory;
    }

    public OperatingSystem getOS() {
        return OS;
    }
}
