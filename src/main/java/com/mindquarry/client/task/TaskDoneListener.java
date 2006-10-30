/**
 * Copyright (C) 2005 MindQuarry GmbH, All Rights Reserved
 */
package com.mindquarry.client.task;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.mindquarry.client.MindClient;

/**
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class TaskDoneListener implements Listener {
    private final TableViewer taskTableViewer;
    
    private final TaskManager tman;

    private final MindClient client;

    private final Button button;

    public TaskDoneListener(final MindClient client, final Button button,
            final TableViewer taskTableViewer, final TaskManager tman) {
        this.taskTableViewer = taskTableViewer;
        this.tman = tman;
        this.client = client;
        this.button = button;
    }

    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event) {
        button.setEnabled(false);

        // finish task
        ISelection selection = taskTableViewer.getSelection();
        if (selection instanceof StructuredSelection) {
            StructuredSelection structsel = (StructuredSelection) selection;
            Object element = structsel.getFirstElement();

            if (element instanceof Task) {
                tman.setDone((Task) element);
                taskTableViewer.refresh();
            }
        }
        button.setEnabled(true);
    }
}
