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
package com.mindquarry.desktop.client.widget.task;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.mindquarry.desktop.client.Messages;
import com.mindquarry.desktop.model.task.Task;

/**
 * Label provider for the Task table.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class TaskTableLabelProvider extends ColumnLabelProvider {
	private static final String ICON_SIZE = "32x32";//$NON-NLS-1$

	// status images
	private static Image newTask = new Image(
			Display.getCurrent(),
			TaskTableLabelProvider.class
					.getResourceAsStream("/com/mindquarry/icons/" + ICON_SIZE + "/status/task-new.png")); //$NON-NLS-1$

	private static Image runningTask = new Image(
			Display.getCurrent(),
			TaskTableLabelProvider.class
					.getResourceAsStream("/com/mindquarry/icons/" + ICON_SIZE + "/status/task-running.png")); //$NON-NLS-1$

	private static Image pausedTask = new Image(
			Display.getCurrent(),
			TaskTableLabelProvider.class
					.getResourceAsStream("/com/mindquarry/icons/" + ICON_SIZE + "/status/task-paused.png")); //$NON-NLS-1$

	private static Image doneTask = new Image(
			Display.getCurrent(),
			TaskTableLabelProvider.class
					.getResourceAsStream("/com/mindquarry/icons/" + ICON_SIZE + "/status/task-done.png")); //$NON-NLS-1$

	public Image getImage(Object element) {
		Task task = (Task) element;
		if (task.getStatus() == null) {
			return null;
		}
		if (task.getStatus().equals(Task.STATUS_NEW)) {
			return createOverlayIcon(task, newTask, Task.STATUS_NEW);
		} else if (task.getStatus().equals(Task.STATUS_RUNNING)) {
			return createOverlayIcon(task, runningTask, Task.STATUS_RUNNING);
		} else if (task.getStatus().equals(Task.STATUS_PAUSED)) {
			return createOverlayIcon(task, pausedTask, Task.STATUS_PAUSED);
		} else if (task.getStatus().equals(Task.STATUS_DONE)) {
			return createOverlayIcon(task, doneTask, Task.STATUS_DONE);
		}
		return null;
	}

	private Image createOverlayIcon(Task task, Image image, String status) {
		Image overlay = image;
		if (task.getPriority() != null) {
			overlay = new Image(Display.getCurrent(),
					TaskTableLabelProvider.class
							.getResourceAsStream("/com/mindquarry/icons/"
									+ ICON_SIZE + "/status/task-" + status
									+ "-" + task.getPriority() + ".png"));
		}
		return overlay;
	}

	public String getText(Object element) {
		Task task = (Task) element;
		String text = task.getTitle();
		return text;
	}

	public String getToolTipText(Object element) {
		Task task = (Task) element;

		final int maxLength = 100;
		String text = ""; //$NON-NLS-1$
		String title = task.getTitle();
		if (title != null) {
			title = title.length() > maxLength ? title.substring(0, maxLength)
					+ "..." : title; //$NON-NLS-1$
		} else {
			title = "-"; //$NON-NLS-1$
		}
		text += Messages.getString("Title") //$NON-NLS-1$
				+ ": " + title; //$NON-NLS-1$ 
		text += "\n" + Messages.getString("Status") //$NON-NLS-1$//$NON-NLS-2$
				+ ": " + task.getStatus(); //$NON-NLS-1$ 
		String summary = task.getSummary();
		if (summary != null) {
			summary = summary.length() > maxLength ? summary.substring(0,
					maxLength)
					+ "..." : summary; //$NON-NLS-1$
		} else {
			summary = "-"; //$NON-NLS-1$
		}
		text += "\n" + Messages.getString("Summary") //$NON-NLS-1$ //$NON-NLS-2$
				+ ": " + summary; //$NON-NLS-1$ 
		return text;
	}

	public int getToolTipTimeDisplayed(Object object) {
		return 2000;
	}

	public int getToolTipDisplayDelayTime(Object object) {
		return 10;
	}
}
