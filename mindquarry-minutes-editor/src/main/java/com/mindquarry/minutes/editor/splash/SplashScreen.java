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
package com.mindquarry.minutes.editor.splash;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

/**
 * Splash screen for the minutes editor.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class SplashScreen {
    public void show() {
        Display display = Display.getCurrent();
        Shell splash = new Shell(SWT.ON_TOP);

        Image image = new Image(display, getClass().getResourceAsStream(
                "/com/mindquarry/splash/splash.png")); //$NON-NLS-1$
        final ProgressBar bar = new ProgressBar(splash, SWT.NONE);

        Label label = new Label(splash, SWT.NONE);
        label.setImage(image);
        initLayout(splash, bar, label);

        initPosition(display, splash);
        splash.open();

        display.syncExec(new SplashRunnable(bar));
        splash.close();
        image.dispose();
    }

    private void initLayout(Shell splash, ProgressBar bar, Label label) {
        FormLayout layout = new FormLayout();
        splash.setLayout(layout);

        FormData labelData = new FormData();
        labelData.right = new FormAttachment(100, 0);
        labelData.bottom = new FormAttachment(100, 0);
        label.setLayoutData(labelData);

        FormData progressData = new FormData();
        progressData.left = new FormAttachment(0, 5);
        progressData.right = new FormAttachment(50, -10);
        progressData.bottom = new FormAttachment(100, -5);
        bar.setLayoutData(progressData);
        splash.pack();
    }

    private void initPosition(Display display, Shell splash) {
        Rectangle splashRect = splash.getBounds();
        Rectangle displayRect = display.getBounds();
        int x = (displayRect.width - splashRect.width) / 2;
        int y = (displayRect.height - splashRect.height) / 2;
        splash.setLocation(x, y);
    }
}
