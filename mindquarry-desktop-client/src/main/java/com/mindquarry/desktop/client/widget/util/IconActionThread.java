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
package com.mindquarry.desktop.client.widget.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TrayItem;

import com.mindquarry.desktop.client.I18N;
import com.mindquarry.desktop.client.MindClient;

/**
 * Thread implementation that replaces the tray icon for faking an animated tray
 * icon.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class IconActionThread extends Thread {
    public static final String ICON_BASE_PATH = "/com/mindquarry/icons/16x16/logo/mindquarry-icon";

    private Log log;
    private Shell shell;
    private TrayItem item;

    private int count = 10;
    private boolean running, ascending = false;

    private List<String> actions = new ArrayList<String>();
    private Map<Integer, Image> icons = new HashMap<Integer, Image>();

    public IconActionThread(TrayItem item, Shell shell) {
        log = LogFactory.getLog(IconActionThread.class);
        this.shell = shell;

        this.item = item;
        this.item.setToolTipText(MindClient.APPLICATION_NAME);

        // initialize icons
        icons.put(10, new Image(Display.getCurrent(), getClass()
                .getResourceAsStream(ICON_BASE_PATH + ".png")));
        for (int i = 1; i <= 9; i++) {
            icons.put(i, new Image(Display.getCurrent(), getClass()
                    .getResourceAsStream(ICON_BASE_PATH + "-" + i + ".png")));
        }
    }

    @Override
    public void run() {
        while (true) {
            if (running) {
                // increase or decrease counter depending on current modus
                if (ascending) {
                    count++;
                } else {
                    count--;
                }
                // set new icon
                final Image icon = icons.get(count);
                shell.getDisplay().syncExec(new Runnable() {
                    public void run() {
                        // check if still running, otherwise reset icon
                        if (running) {
                            item.setImage(icon);
                        } else {
                            item.setImage(icons.get(10));
                        }
                    }
                });
                // check next modus and switch, if necessary
                if (!ascending && (count == 1)) {
                    ascending = true;
                }
                if (ascending && (count == 10)) {
                    ascending = false;
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                log.error("thread error", e); //$NON-NLS-1$
            }
        }
    }

    public void startAction(String description) {
        actions.add(description);
        updateToolTip();
        running = true;
    }

    public void stopAction(String description) {
        actions.remove(description);
        if (actions.size() == 0) {
            running = false;
            reset();
        }
        updateToolTip();
    }

    private void updateToolTip() {
        String tooltip = ""; //$NON-NLS-1$
        if (actions.size() > 0) {
            tooltip += I18N.getString("Running actions:") + "\n"; //$NON-NLS-1$ //$NON-NLS-2$

            Iterator<String> aIt = actions.iterator();
            while (aIt.hasNext()) {
                String action = aIt.next();
                tooltip += "- " + action; //$NON-NLS-1$
                if (aIt.hasNext()) {
                    tooltip += "\n"; //$NON-NLS-1$
                }
            }
        } else {
            tooltip += I18N.getString("Currently no action is running."); //$NON-NLS-1$
        }
        final String util = tooltip;
        shell.getDisplay().syncExec(new Runnable() {
            public void run() {
                item.setToolTipText(util);
            }
        });
    }

    private void reset() {
        shell.getDisplay().syncExec(new Runnable() {
            public void run() {
                item.setImage(icons.get(10));
            }
        });
        ascending = false;
        count = 10;
    }
}
