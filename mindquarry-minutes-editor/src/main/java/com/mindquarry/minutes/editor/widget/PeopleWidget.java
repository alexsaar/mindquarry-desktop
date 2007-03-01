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
package com.mindquarry.minutes.editor.widget;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * Add summary documentation here.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class PeopleWidget extends EditorWidget {
    private static final String PARTICIPANT_COL = "Conversation Participant";

    /**
     * {@inheritDoc}
     */
    public PeopleWidget(Composite parent, int style) {
        super(parent, style);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.mindquarry.minutes.editor.widget.EditorWidget#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected void createContents(Composite parent) {
        Table table = new Table(parent, SWT.MULTI);

        TableColumn titleColumn = new TableColumn(table, SWT.NONE);
        titleColumn.setResizable(false);

        TableViewer viewer = new TableViewer(table);
        viewer.setColumnProperties(new String[] { PARTICIPANT_COL });
        viewer.setLabelProvider(new LabelProvider() {

        });
        viewer.setContentProvider(new IStructuredContentProvider() {
            /**
             * @see org.eclipse.jface.viewers.IContentProvider#dispose()
             */
            public void dispose() {
                // nothing to do so far
            }

            /**
             * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
             *      java.lang.Object, java.lang.Object)
             */
            public void inputChanged(Viewer viewer, Object oldInput,
                    Object newInput) {
                // nothing to do so far
            }

            /**
             * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
             */
            public Object[] getElements(Object inputElement) {
                if (inputElement instanceof List) {
                    List list = (List) inputElement;
                    return list.toArray();
                }
                return null;
            }
        });
    }
}
