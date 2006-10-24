/**
 * Copyright (C) 2005 MindQuarry GmbH, All Rights Reserved
 */
package com.mindquarry.client.task;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * @author <a href="mailto:lars(dot)trieloff(at)mindquarry(dot)com">Lars
 *         Trieloff</a>
 */
public class TaskTableContentProvider implements IStructuredContentProvider,
        TaskListChangeListener {
    /**
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose() {
    }

    /**
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
     *      java.lang.Object, java.lang.Object)
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if ((oldInput != null) && (oldInput instanceof TaskManager)) {
            ((TaskManager) oldInput).removeChangeListener(this);
        }
        if ((newInput != null) && (newInput instanceof TaskManager)) {
            ((TaskManager) newInput).addChangeListener(this);
        }
    }

    /**
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof TaskManager) {
            TaskManager manager = (TaskManager) inputElement;
            return manager.getTasks();
        }
        return new Object[] {};
    }
}
