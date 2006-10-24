/**
 * Copyright (C) 2005 MindQuarry GmbH, All Rights Reserved
 */
package com.mindquarry.client.task;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;

/**
 * @author <a href="mailto:lars(dot)trieloff(at)mindquarry(dot)com">Lars
 *         Trieloff</a>
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class TaskSelectionChangedListener implements ISelectionChangedListener {
    private final TaskManager tman;

    private final TableViewer taskTableViewer;

    public TaskSelectionChangedListener(TaskManager tman,
            TableViewer taskTableViewer) {
        this.tman = tman;
        this.taskTableViewer = taskTableViewer;
    }

    /**
     * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
     */
    public void selectionChanged(SelectionChangedEvent event) {
        ISelection selection = event.getSelection();

        if (selection instanceof StructuredSelection) {
            StructuredSelection structsel = (StructuredSelection) selection;
            Object element = structsel.getFirstElement();

            if (element instanceof Task) {
                tman.startTask((Task) element);
                taskTableViewer.refresh();
            }
        }
    }
}
