/**
 * Copyright (C) 2006 Mindquarry GmbH, All Rights Reserved
 */
package com.mindquarry.client.tray.awt;

import java.awt.TrayIcon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;

import com.mindquarry.client.MindClient;
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

    private final Tray tray;

    private final Shell shell;

    public TrayIconMouseListener(Display display, MindClient mindclient,
            Tray tray, Shell shell) {
        this.display = display;
        this.mindclient = mindclient;
        this.tray = tray;
        this.shell = shell;
    }

    /**
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked(MouseEvent e) {
        if ((e.getButton() == MouseEvent.BUTTON1)
                && (e.getSource() instanceof TrayIcon)) {
            shell.getDisplay().asyncExec(new Runnable() {
                /**
                 * @see java.lang.Runnable#run()
                 */
                public void run() {
                    Event event = new Event();
                    event.widget = new TrayItem(tray, SWT.NONE);

                    new TrayIconSelectionListener(display, mindclient)
                            .widgetSelected(new SelectionEvent(event));
                }
            });
        }
    }

    /**
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    public void mouseEntered(MouseEvent e) {
    }

    /**
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    public void mouseExited(MouseEvent e) {
    }

    /**
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    public void mousePressed(MouseEvent e) {
    }

    /**
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent e) {
    }
}
