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
package com.mindquarry.desktop.minutes.editor.splash;

import org.eclipse.swt.widgets.ProgressBar;

/**
 * Add summary documentation here.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class SplashRunnable implements Runnable {
    private ProgressBar bar;

    public SplashRunnable(ProgressBar bar) {
        this.bar = bar;
        this.bar.setMaximum(5);
    }

    public void run() {
        for (int i = 0; i < 5; i++) {
            bar.setSelection(i + 1);
            try {
                Thread.sleep(500);
            } catch (Throwable e) {
            }
        }
    }
}
