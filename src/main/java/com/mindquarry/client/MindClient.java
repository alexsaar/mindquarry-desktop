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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
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

    public static final String MINDCLIENT_ICON = "/icons/16x16/mindquarry.png"; //$NON-NLS-1$

    private final OperatingSystem OS;

    private static Shell shell;

    private final Image icon;

    private final BeanFactory factory;

    private Properties options;

    private File optionsFile;

    public MindClient() {
        factory = new ClassPathXmlApplicationContext(
                new String[] { "applicationContext.xml" }); //$NON-NLS-1$

        icon = new Image(Display.getCurrent(), getClass().getResourceAsStream(
                MINDCLIENT_ICON));

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
        display.setWarnings(true);
        Display.setAppName("Mindquarry");
        shell = new Shell(display, SWT.NONE);
        shell.setText("Mindquarry");

        // check CLI arguments
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
        final Tray tray = display.getSystemTray();
        final TrayIconSelectionListener trayListener = new TrayIconSelectionListener(display, mindclient);
        
        final TrayItem item = new TrayItem (tray, SWT.NONE);
		item.setToolTipText("SWT TrayItem");
		item.setImage(mindclient.icon);
		item.addListener (SWT.Show, new Listener () {
			public void handleEvent (Event event) {
				System.out.println("show");
			}
		});
		item.addListener (SWT.Hide, new Listener () {
			public void handleEvent (Event event) {
				System.out.println("hide");
			}
		});
		item.addListener (SWT.Selection, new Listener () {
			public void handleEvent (final Event event) {
				//System.out.println("event: " + Thread.currentThread().getName() + " " + event);
				
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
                OptionsDialog dlg = new OptionsDialog(shell,
                        mindclient.icon, mindclient.options);
                if (dlg.open() == Window.OK) {
                    mindclient.saveOptions();
                }
            }
        });
        
        item.addListener (SWT.DefaultSelection, new Listener () {
			public void handleEvent (Event event) {
				System.out.println("default selection");
				menu.setVisible(true);
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

        //createSWTTrayIcon(mindclient, display, tray);
        while (!display.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }

    private static void createSWTTrayIcon(final MindClient mindclient,
            final Display display, final Tray tray) {
        if (tray != null) {
            TrayItem ti = new TrayItem(tray, SWT.NONE);
            ti.setImage(mindclient.icon);
            
            {
            	TrayItem item = ti;
            	
	            item.addListener (SWT.Show, new Listener () {
	    			public void handleEvent (Event event) {
	    				System.out.println("show");
	    			}
	    		});
	    		item.addListener (SWT.Hide, new Listener () {
	    			public void handleEvent (Event event) {
	    				System.out.println("hide");
	    			}
	    		});
	    		item.addListener (SWT.Selection, new Listener () {
	    			public void handleEvent (Event event) {
	    				System.out.println("selection");
	    			}
	    		});
	    		item.addListener (SWT.DefaultSelection, new Listener () {
	    			public void handleEvent (Event event) {
	    				System.out.println("default selection");
	    			}
	    		});
	    		final Menu menu = new Menu (shell, SWT.POP_UP);
	    		for (int i = 0; i < 8; i++) {
	    			MenuItem mi = new MenuItem (menu, SWT.PUSH);
	    			mi.setText ("Item" + i);
	    			mi.addListener (SWT.Selection, new Listener () {
	    				public void handleEvent (Event event) {
	    					System.out.println("selection " + event.widget);
	    				}
	    			});
	    			if (i == 0) menu.setDefaultItem(mi);
	    		}
	    		item.addListener (SWT.MenuDetect, new Listener () {
	    			public void handleEvent (Event event) {
	    				menu.setVisible (true);
	    			}
	    		});
	        }
            
            if (false) {
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
	            menuItem.setText(Messages.getString("MindClient.0")); //$NON-NLS-1$
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
	            menuItem.setText(Messages.getString("MindClient.1")); //$NON-NLS-1$
	            menuItem.addListener(SWT.Selection, new Listener() {
	                /**
	                 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	                 */
	                public void handleEvent(Event event) {
	                    System.exit(1);
	                }
	            });
            }
        } else {
            // there must be a tray
            System.exit(-1);
        }
    }

    private void loadOptions() {
        try {
            if (!optionsFile.exists()) {
                // init options with dummy values
                options.put(LOGIN_KEY, Messages.getString("MindClient.2")); //$NON-NLS-1$
                options.put(PASSWORD_KEY, "password"); //$NON-NLS-1$
                options.put(ENDPOINT_KEY, Messages.getString("MindClient.3")); //$NON-NLS-1$

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
            MessageDialog.openError(shell, Messages.getString("MindClient.4"), //$NON-NLS-1$
                    Messages.getString("MindClient.5")); //$NON-NLS-1$
        }
    }

    public void saveOptions() {
        FileOutputStream fos;
        try {
            if (!optionsFile.exists()) {
                optionsFile.getParentFile().mkdirs();
                optionsFile.createNewFile();
            }
            fos = new FileOutputStream(optionsFile);
            options.storeToXML(fos, Messages.getString("MindClient.6")); //$NON-NLS-1$
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
