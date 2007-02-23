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
package com.mindquarry.client.util.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Add summary documentation here.
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
        shell.setLayout(new GridLayout(2, false));
        shell.setLocation(display.getBounds().width - WIDTH, display
                .getBounds().height);
        shell.setLocation(400, 400);
        shell.setSize(WIDTH, HEIGHT);

        img = new Image(null, getClass().getResourceAsStream(
                "/com/mindquarry/icons/16x16/logo/mindquarry-icon.png")); //$NON-NLS-1$

        Label icon = new Label(shell, SWT.CENTER);
        icon.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        icon.setImage(img);

        msgLabel = new Label(shell, SWT.WRAP);
        msgLabel.setSize(WIDTH - 16, HEIGHT);
        msgLabel.setText(""); //$NON-NLS-1$

        shell.open();
    }

    public void show(final String message, final long duration) {
        display.syncExec(new Runnable() {
            public void run() {
                msgLabel.setText(message);
                msgLabel.redraw();
            }
        });

        final int height = shell.getSize().y;
        new Thread(new Runnable() {
            public void run() {
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
        }).start();
    }

    private void updatePosition(final boolean up) {
        display.syncExec(new Runnable() {
            public void run() {
                int x = shell.getLocation().x;
                int y = shell.getLocation().y;
                if (up) {
                    shell.setLocation(x, --y);
                } else {
                    shell.setLocation(x, ++y);
                }
            }
        });
    }

    private void redrawAndSleep() {
        display.syncExec(new Runnable() {
            public void run() {
                shell.redraw(shell.getLocation().x, shell.getLocation().y,
                        shell.getSize().x, shell.getSize().y, true);
                shell.layout(true, true);
            }
        });
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

    public static void main(String[] args) {
        Display display = new Display();

        NotificationWidget shell = new NotificationWidget(display);
        shell.show("this is a test with much more text", 2000); //$NON-NLS-1$

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}
