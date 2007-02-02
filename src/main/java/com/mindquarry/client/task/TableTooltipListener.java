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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.mindquarry.client.MindClient;

/**
 * Table listener for displaying a fake tooltip on table rows.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class TableTooltipListener implements Listener {
    private Table table;

    private Shell tip;

    private Label label;

    private Listener labelListener;

    public TableTooltipListener(Table table, Listener labelListener) {
        this.table = table;
        this.labelListener = labelListener;
    }

    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event) {
        switch (event.type) {
        case SWT.Dispose:
        case SWT.KeyDown:
        case SWT.MouseMove: {
            if (tip == null)
                break;
            tip.dispose();
            tip = null;
            label = null;
            break;
        }
        case SWT.MouseHover: {
            TableItem item = table.getItem(new Point(event.x, event.y));
            if (item != null) {
                if (tip != null && !tip.isDisposed()) {
                    tip.dispose();
                }
                tip = new Shell(MindClient.getShell(), SWT.ON_TOP | SWT.TOOL);
                tip.setLayout(new FillLayout());

                label = new Label(tip, SWT.WRAP);
                label.setForeground(MindClient.getShell().getDisplay()
                        .getSystemColor(SWT.COLOR_INFO_FOREGROUND));
                label.setBackground(MindClient.getShell().getDisplay()
                        .getSystemColor(SWT.COLOR_INFO_BACKGROUND));
                label.setData("_TABLEITEM", item); //$NON-NLS-1$
                label.setText(createTooltipText(item));
                label.addListener(SWT.MouseExit, labelListener);
                label.addListener(SWT.MouseDown, labelListener);

                Point size = tip.computeSize(table.getSize().x, SWT.DEFAULT);
                Rectangle rect = item.getBounds(0);
                Point pt = table.toDisplay(rect.x, rect.y);

                tip.setLocation(pt.x, pt.y);
                tip.setSize(size.x, size.y);
                tip.setVisible(true);
            }
        }
        }
    }

    private String createTooltipText(TableItem item) {
        String text = ""; //$NON-NLS-1$
        if ((item.getText() != null) && (!item.getText().equals(""))) { //$NON-NLS-1$
            text += "Title" + ": " + item.getText(); //$NON-NLS-2$
        }
        if ((item.getText(1) != null) && (!item.getText(1).equals(""))) { //$NON-NLS-1$
            text += "\n" + "Status" + ": " + item.getText(1); //$NON-NLS-1$ //$NON-NLS-3$
        }
        if ((item.getText(2) != null) && (!item.getText(2).equals(""))) { //$NON-NLS-1$
            text += "\n" + "Summary" + ": " + item.getText(2); //$NON-NLS-1$//$NON-NLS-3$
        }
        return text;
    }
}
