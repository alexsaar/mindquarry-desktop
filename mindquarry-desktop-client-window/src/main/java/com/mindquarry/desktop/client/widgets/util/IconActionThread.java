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
package com.mindquarry.desktop.client.widgets.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TrayItem;

import com.mindquarry.desktop.client.Messages;
import com.mindquarry.desktop.client.MindClient;

/**
 * Thread implementation that replaces the tray icon for faking an animated tray
 * icon.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class IconActionThread extends Thread {
	private Log log;

	private TrayItem item;

	private boolean running = false;

	private int count = 10;

	private boolean ascending = false;

	private List actions = new ArrayList();

	private Shell shell;

	public IconActionThread(TrayItem item, Shell shell) {
		this.item = item;
		this.item.setToolTipText(MindClient.APPLICATION_NAME);

		this.shell = shell;

		log = LogFactory.getLog(IconActionThread.class);
	}

	/**
	 * @see java.lang.Thread#run()
	 */
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
				final Image icon = getImage(count);
				shell.getDisplay().syncExec(new Runnable() {
					public void run() {
						item.setImage(icon);
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

	private Image getImage(int count) {
		if (count == 10) {
			return new Image(
					Display.getCurrent(),
					getClass()
							.getResourceAsStream(
									"/com/mindquarry/icons/16x16/logo/mindquarry-icon.png")); //$NON-NLS-1$
		}
		return new Image(Display.getCurrent(), getClass().getResourceAsStream(
				"/com/mindquarry/icons/16x16/logo/mindquarry-icon-" + count //$NON-NLS-1$
						+ ".png")); //$NON-NLS-1$
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
			tooltip += "Running actions:" + "\n"; //$NON-NLS-1$ //$NON-NLS-2$

			Iterator aIt = actions.iterator();
			while (aIt.hasNext()) {
				String action = (String) aIt.next();
				tooltip += "- " + action; //$NON-NLS-1$
			}
		} else {
			tooltip += Messages.getString(IconActionThread.class, "0"); //$NON-NLS-1$
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
				item.setImage(getImage(10));
			}
		});
		ascending = false;
		count = 10;
	}
}
