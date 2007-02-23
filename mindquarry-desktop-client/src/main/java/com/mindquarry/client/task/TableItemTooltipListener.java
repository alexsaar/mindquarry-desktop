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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * Add summary documentation here.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class TableItemTooltipListener implements Listener {
    private Table table;

    public TableItemTooltipListener(Table table) {
        this.table = table;
    }

    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event) {
        Label label = (Label) event.widget;
        Shell shell = label.getShell();

        switch (event.type) {
        case SWT.MouseDown:
            Event e = new Event();
            e.item = (TableItem) label.getData("_TABLEITEM"); //$NON-NLS-1$

            // Assuming table is single select, set the
            // selection as if the mouse down event went
            // through to the table
            table.setSelection(new TableItem[] { (TableItem) e.item });
            table.notifyListeners(SWT.Selection, e);
            // fall through
        case SWT.MouseExit:
            shell.dispose();
            break;
        }
    }
}
