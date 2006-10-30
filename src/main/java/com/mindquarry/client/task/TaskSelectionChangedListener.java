/**
 * Copyright (C) 2005 MindQuarry GmbH, All Rights Reserved
 */
package com.mindquarry.client.task;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Button;

/**
 * @author <a href="mailto:lars(dot)trieloff(at)mindquarry(dot)com">Lars
 *         Trieloff</a>
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class TaskSelectionChangedListener implements ISelectionChangedListener {
    private final TaskManager tman;

    private final TableViewer taskTableViewer;

    private final Button button;

    public TaskSelectionChangedListener(final TaskManager tman,
            final TableViewer taskTableViewer, final Button button) {
        this.button = button;
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
                Task task = (Task) element;
                if (task.isActive()) {
                    tman.stopTask((Task) element);
                    button.setEnabled(false);
                } else {
                    tman.startTask((Task) element);
                    button.setEnabled(true);
                }
                taskTableViewer.refresh();
            }
        }
    }
}
