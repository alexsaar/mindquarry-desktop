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

import java.io.IOException;

import org.eclipse.swt.widgets.Display;
import org.junit.Test;

import junit.framework.TestCase;

/**
 * Test cases for the {@link NotificationWidget}.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class NotificationWidgetTest extends TestCase {
    @Test
    public void testNotificationWidget() throws IOException {
        final Display display = new Display();

        final NotificationWidget widget = new NotificationWidget(display);
        Thread worker = new Thread(new Runnable() {
            public void run() {
                widget
                        .show(
                                "the notification title", //$NON-NLS-1$
                                "this is a notification that contains a bit more text...", //$NON-NLS-1$
                                2000);
                widget.show("the notification title", //$NON-NLS-1$
                        "this is a second notification...", //$NON-NLS-1$
                        2000);
                widget.dispose();
            }
        });
        worker.start();
        while (!widget.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}
