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
package com.mindquarry.client.teamspace.dialog;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog adding new tasks to a team.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class TeamSelectionDialog extends TitleAreaDialog {
    private CCombo teamWidget = null;

    private List<String> teams;

    public TeamSelectionDialog(Shell shell, List<String> teams) {
        super(shell);
        setBlockOnOpen(true);
        this.teams = teams;
    }

    @Override
    protected Control createContents(Composite parent) {
        Control contents = super.createContents(parent);

        setTitle("Select a Team");
        setMessage("Please select on of your teams.",
                IMessageProvider.INFORMATION);

        getShell().setText("Select a Team");
        return contents;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(1, true));

        teamWidget = new CCombo(composite, SWT.BORDER | SWT.READ_ONLY
                | SWT.FLAT);
        teamWidget.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        teamWidget.setBackground(Display.getCurrent().getSystemColor(
                SWT.COLOR_WHITE));

        for (String team : teams) {
            teamWidget.add(team);
        }
        teamWidget.select(0);
        return composite;
    }

    /**
     * Creates the buttons for the button bar
     * 
     * @param parent the parent composite
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
                true);
    }

    public String getSelectedTeam() {
        return teamWidget.getItem(teamWidget.getSelectionIndex());
    }
}
