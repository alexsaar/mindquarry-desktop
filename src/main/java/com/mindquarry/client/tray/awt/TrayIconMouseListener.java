/**
 * Copyright (C) 2006 Mindquarry GmbH, All Rights Reserved
 */
package com.mindquarry.client.tray.awt;

import java.awt.Button;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Menu;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

import com.mindquarry.client.Messages;
import com.mindquarry.client.MindClient;
import com.mindquarry.client.dialog.OptionsDialog;
import com.mindquarry.client.tray.TrayIconSelectionListener;

/**
 * Mouse listener to be sued by AWT tray icon.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class TrayIconMouseListener implements MouseListener {
    private final Display display;

    private final MindClient mindclient;

    private final Shell shell;
    
    private final TrayIcon ti;
    
    private final Frame frame;

	private final PopupMenu menu;

    public TrayIconMouseListener(Display display, MindClient mindclient, Shell shell, TrayIcon ti) {
        this.display = display;
        this.mindclient = mindclient;
        this.shell = shell;
        this.ti = ti;
        
        this.menu = createPopupMenu();
        this.frame = createDummyFrame(this.menu);
    }

    /**
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked(MouseEvent e) {
    }
    
    

    /**
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    public void mouseEntered(MouseEvent e) {
        // not used
    }

    /**
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    public void mouseExited(MouseEvent e) {
        // not used
    }

    /**
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    public void mousePressed(MouseEvent e) {
        System.out.println("Pressed " + e);
        if (((e.getButton() == MouseEvent.BUTTON2)||((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK))
                && (e.getSource() instanceof TrayIcon)) {
            frame.setVisible(true);
            menu.show(frame, e.getXOnScreen(), e.getYOnScreen());
            frame.setVisible(false);
        } else if ((e.getButton() == MouseEvent.BUTTON1)
                && (e.getSource() instanceof TrayIcon)) {
            shell.getDisplay().asyncExec(new Runnable() {
                /**
                 * @see java.lang.Runnable#run()
                 */
                public void run() {
                    Event event = new Event();
                    //event.widget = new TrayItem(tray, SWT.NONE);
                    event.widget = shell;
                    
                    new TrayIconSelectionListener(display, mindclient)
                            .widgetSelected(new SelectionEvent(event));
                }
            });
        }
    }

	private Frame createDummyFrame(PopupMenu menu) {
		Frame frame = new Frame();
		frame.add(menu);
		frame.setUndecorated(true);
		return frame;
	}

	private PopupMenu createPopupMenu() {
		PopupMenu menu = new PopupMenu();
		java.awt.MenuItem mi = new java.awt.MenuItem(Messages
		        .getString("MindClient.0")); //$NON-NLS-1$
		mi.addActionListener(new ActionListener() {
		    /**
		     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		     */
		    public void actionPerformed(ActionEvent e) {
		    	shell.getDisplay().asyncExec(new Runnable() {
		            /**
		             * @see java.lang.Runnable#run()
		             */
		            public void run() {
		            	System.out.println("Show options");
		                OptionsDialog dlg = new OptionsDialog(shell,
		                        mindclient.getIcon(), mindclient.getOptions());
		                if (dlg.open() == Window.OK) {
		                    mindclient.saveOptions();
		                }
		            }
		        });
		    }
		});
		menu.add(mi);
		mi = new java.awt.MenuItem("-"); //$NON-NLS-1$
		menu.add(mi);
		mi = new java.awt.MenuItem(Messages.getString("MindClient.1")); //$NON-NLS-1$
		mi.addActionListener(new ActionListener() {
		    /**
		     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		     */
		    public void actionPerformed(ActionEvent e) {
		        System.exit(0);
		    }
		});
		menu.add(mi);
		return menu;
	}

    /**
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent e) {

        System.out.println("Released " + e);
        // not used
    }
}
