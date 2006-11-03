/**
 * Copyright (C) 2005 MindQuarry GmbH, All Rights Reserved
 */
package com.mindquarry.client.task;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class TaskDoneListener implements Listener {
    private final TaskManager tman;

    public TaskDoneListener(final TaskManager tman) {
        this.tman = tman;
    }

    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event) {
        // finish task
        ISelection selection = tman.getTaskTableViewer().getSelection();
        if (selection instanceof StructuredSelection) {
            StructuredSelection structsel = (StructuredSelection) selection;
            Object element = structsel.getFirstElement();

            if (element instanceof Task) {
                tman.setDone((Task) element);
            }
        }
    }
}
