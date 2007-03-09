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
package com.mindquarry.client.task;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * @author <a href="mailto:lars(dot)trieloff(at)mindquarry(dot)com">Lars
 *         Trieloff</a>
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class TaskTableLabelProvider implements ITableLabelProvider {
    private Image newTask = new Image(Display.getCurrent(), getClass()
            .getResourceAsStream(
                    "/com/mindquarry/icons/16x16/status/task-new.png")); //$NON-NLS-1$

    private Image runningTask = new Image(Display.getCurrent(), getClass()
            .getResourceAsStream(
                    "/com/mindquarry/icons/16x16/status/task-running.png")); //$NON-NLS-1$

    private Image pausedTask = new Image(Display.getCurrent(), getClass()
            .getResourceAsStream(
                    "/com/mindquarry/icons/16x16/status/task-paused.png")); //$NON-NLS-1$

    private Image doneTask = new Image(Display.getCurrent(), getClass()
            .getResourceAsStream(
                    "/com/mindquarry/icons/16x16/status/task-done.png")); //$NON-NLS-1$

    /**
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
     *      int)
     */
    public Image getColumnImage(Object element, int columnIndex) {
        Task task = (Task) element;
        if (task.getStatus().equals(Task.STATUS_NEW)) {
            return newTask;
        } else if (task.getStatus().equals(Task.STATUS_RUNNING)) {
            return runningTask;
        } else if (task.getStatus().equals(Task.STATUS_PAUSED)) {
            return pausedTask;
        } else if (task.getStatus().equals(Task.STATUS_DONE)) {
            return doneTask;
        }
        return null;
    }

    /**
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
     *      int)
     */
    public String getColumnText(Object element, int columnIndex) {
        Task task = (Task) element;

        String text = ""; //$NON-NLS-1$
        if (columnIndex == 0) {
            text = task.getTitle();
        } else if (columnIndex == 1) {
            text = task.getStatus();
        } else if (columnIndex == 2) {
            text = task.getSummary();
        }
        return text;
    }

    /**
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    public void addListener(ILabelProviderListener listener) {
    }

    /**
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
     */
    public void dispose() {
    }

    /**
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object,
     *      java.lang.String)
     */
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    /**
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    public void removeListener(ILabelProviderListener listener) {
    }
}
