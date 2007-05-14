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
package com.mindquarry.desktop.widget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * A widget for displaying notification messages.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class NotificationWidget {
    private static final int WIDTH = 160;

    private static final int HEIGHT = 60;

    private final Display display;

    private Shell shell;

    private Label msgLabel;

    private Image img;

    public NotificationWidget(Display display) {
        this.display = display;

        display.syncExec(new Runnable() {
            public void run() {
                createContents();
            }
        });
    }

    private void createContents() {
        shell = new Shell(SWT.ON_TOP);
//        shell.setLocation(display.getBounds().width - WIDTH, display
//                .getBounds().height);
        shell.setLocation(400, 400);
        shell.setSize(WIDTH, HEIGHT);

        img = new Image(null, getClass().getResourceAsStream(
                "/com/mindquarry/icons/16x16/logo/mindquarry-icon.png")); //$NON-NLS-1$

        Label icon = new Label(shell, SWT.CENTER);
        icon.setSize(20, 20);
        icon.setImage(img);

        msgLabel = new Label(shell, SWT.WRAP);
        msgLabel.setLocation(22, 2);
        msgLabel.setSize(WIDTH - 22, HEIGHT -2);
        
        shell.open();
    }

    public void show(final String message, final long duration) {
        msgLabel.setText(message);
        msgLabel.redraw();

        int height = shell.getSize().y;
        for (int i = 0; i <= height; i++) {
            updatePosition(true);
            redrawAndSleep();
        }
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
        }
        for (int i = 0; i <= height; i++) {
            updatePosition(false);
            redrawAndSleep();
        }
    }

    private void updatePosition(final boolean up) {
        int x = shell.getLocation().x;
        int y = shell.getLocation().y;
        if (up) {
            shell.setLocation(x, --y);
        } else {
            shell.setLocation(x, ++y);
        }
    }

    private void redrawAndSleep() {
        shell.redraw(shell.getLocation().x, shell.getLocation().y, shell
                .getSize().x, shell.getSize().y, true);
        shell.layout(true, true);

        try {
            Thread.sleep(6);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean isDisposed() {
        if (shell.isDisposed()) {
            img.dispose();
        }
        return shell.isDisposed();
    }
}
