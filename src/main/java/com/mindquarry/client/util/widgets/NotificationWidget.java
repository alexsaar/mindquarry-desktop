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

        shell = new Shell(SWT.ON_TOP);
        shell.setLayout(new GridLayout(2, false));
        shell.setLocation(400,400);

        createContents();

        shell.pack();
        shell.open();
    }
    
    public static void main(String[] args) {
        Display display = new Display();

        NotificationWidget shell = new NotificationWidget(display);
        shell.show("this is a test with much more text", 2000); //$NON-NLS-1$

//        while (!shell.isDisposed()) {
//            if (!display.readAndDispatch())
//                display.sleep();
//        }
        display.dispose();
    }

    private void createContents() {
        img = new Image(null, getClass().getResourceAsStream(
                "/com/mindquarry/icons/16x16/logo/mindquarry-icon.png")); //$NON-NLS-1$

        Label icon = new Label(shell, SWT.CENTER);
        icon.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        icon.setImage(img);

        msgLabel = new Label(shell, SWT.WRAP);
        msgLabel.setText(""); //$NON-NLS-1$
    }

    public void show(String message, final long duration) {
        msgLabel.setText(message);
        msgLabel.redraw();
        shell.pack();
        shell.setSize(WIDTH, HEIGHT);
        shell.layout();

        int height = shell.getSize().y;
        int x = shell.getLocation().x;
        int y = shell.getLocation().y;

        for (int i = 0; i <= height; i++) {
            shell.setLocation(x, y--);
            redrawAndSleep();
        }
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
        }
        for (int i = 0; i <= height; i++) {
            shell.setLocation(x, y++);
            redrawAndSleep();
        }
    }

    private void redrawAndSleep() {
        display.syncExec(new Runnable() {
            public void run() {
                shell.redraw(shell.getLocation().x, shell.getLocation().y,
                        shell.getSize().x, shell.getSize().y, true);
                shell.layout(true, true);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public boolean isDisposed() {
        if (shell.isDisposed()) {
            img.dispose();
        }
        return shell.isDisposed();
    }
}
