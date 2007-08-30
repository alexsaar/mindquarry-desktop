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
package com.mindquarry.desktop.client.widget.team;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.mindquarry.desktop.client.Messages;
import com.mindquarry.desktop.client.MindClient;
import com.mindquarry.desktop.client.action.team.RefreshTeamlistAction;
import com.mindquarry.desktop.client.widget.WidgetBase;
import com.mindquarry.desktop.model.team.Team;
import com.mindquarry.desktop.model.team.TeamList;
import com.mindquarry.desktop.preferences.profile.Profile;
import com.mindquarry.desktop.util.NotAuthorizedException;
import com.mindquarry.desktop.workspace.exception.CancelException;

/**
 * Simple table listing the teams with checkboxes.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class TeamlistWidget extends WidgetBase {
    private static Log log = LogFactory.getLog(TeamlistWidget.class);

    private TableViewer viewer;
    
    private Table table;

    /**
     * {@inheritDoc}
     */
    public TeamlistWidget(Composite parent, int style, MindClient client) {
        super(parent, style, client);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.mindquarry.desktop.minutes.editor.widget.EditorWidget#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected void createContents(Composite parent) {
        setLayout(new GridLayout(2, false));
        ((GridLayout) getLayout()).marginBottom = 0;
        ((GridLayout) getLayout()).marginTop = 12;
        ((GridLayout) getLayout()).marginLeft = 2;
        ((GridLayout) getLayout()).marginRight = 0;

        ((GridLayout) getLayout()).marginWidth = 0;
        ((GridLayout) getLayout()).marginHeight = 0;

        ((GridLayout) getLayout()).verticalSpacing = 2;
        ((GridLayout) getLayout()).horizontalSpacing = 0;

        Label label = new Label(parent, SWT.LEFT);
        label.setText("Teams:");
        label.setFont(JFaceResources.getFont(MindClient.TEAM_NAME_FONT_KEY));

        // create team list table
        table = new Table(parent, SWT.SINGLE | SWT.CHECK
                | SWT.FULL_SELECTION | SWT.BORDER);
        table.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true,
                true, 2, 1));
        table.setFont(JFaceResources.getFont(MindClient.TEAM_NAME_FONT_KEY));

        Menu menu = new Menu(table);
        table.setMenu(menu);

        RefreshTeamlistAction action = (RefreshTeamlistAction)client
                .getAction(RefreshTeamlistAction.class.getName());
        action.setTeamList(this);
        
        ActionContributionItem refreshTeamsAction = new ActionContributionItem(
                action);
        refreshTeamsAction.fill(menu, menu.getItemCount());

        viewer = new TableViewer(table);
        viewer.setContentProvider(new TeamlistContentProvider());
        viewer.setLabelProvider(new TeamlistLabelProvider());

        // create selection buttons
        Button button = new Button(parent, SWT.PUSH | SWT.FLAT | SWT.CENTER);
        button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        button.setText(Messages.getString("Select All"));
        button.setFont(JFaceResources.getFont(MindClient.TEAM_NAME_FONT_KEY));
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                selectAll();
            }
        });
        button = new Button(parent, SWT.PUSH | SWT.FLAT | SWT.CENTER);
        button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        button.setText(Messages.getString("Deselect All"));
        button.setFont(JFaceResources.getFont(MindClient.TEAM_NAME_FONT_KEY));
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                deselectAll();
            }
        });
    }

    public List<Team> getSelectedTeams() {
        return getTeams(true);
    }

    public List<Team> getTeams() {
        return getTeams(false);
    }

    private List<Team> getTeams(boolean selectedOnly) {
        List<Team> teams = new ArrayList<Team>();
        for (TableItem item : viewer.getTable().getItems()) {
            if (selectedOnly && !item.getChecked()) {
                continue;
            }
            Team team = (Team) item.getData();
            teams.add(team);
        }
        return teams;
    }

    public void refresh() throws CancelException {
        client.startAction(Messages.getString("Updating list of teams")); //$NON-NLS-1$
        try {
            viewer.setInput(queryTeams());
        } finally {
            client.stopAction(Messages.getString("Updating list of teams")); //$NON-NLS-1$
        }
    }

    public void selectAll() {
        setChecked(true);
    }

    public void deselectAll() {
        setChecked(false);
    }

    public void clear() {
        viewer.setInput(null);
    }

    private void setChecked(boolean checked) {
        TableItem[] tis = viewer.getTable().getItems();
        for (TableItem item : tis) {
            item.setChecked(checked);
        }
    }

    private TeamList queryTeams() throws CancelException {
        Profile selected = Profile.getSelectedProfile(client
                .getPreferenceStore());
        if (selected == null) {
            MessageDialog
                    .openError(
                            getShell(),
                            Messages.getString("Error"),
                            Messages
                                    .getString("Currently there is no profile selected. Please select the profile of the server you want to work with or create a new profile."));
            return null;
        }
        // retrieve list of teams
        TeamList teamList;
        try {
            teamList = new TeamList(selected.getServerURL() + "/teams", //$NON-NLS-1$
                    selected.getLogin(), selected.getPassword());
            return teamList;
        } catch (NotAuthorizedException e) {
            log.error("Error while updating team list at " //$NON-NLS-1$
                    + selected.getServerURL(), e);
            
            Boolean retry = client.handleNotAuthorizedException(e);
            if(retry) {
                return queryTeams();
            }
            
            throw new CancelException("Updating team list cancelled due to wrong credentials.", e);
        } catch (UnknownHostException uhe) {
            log.error("Error while updating team list at " //$NON-NLS-1$
                    + selected.getServerURL(), uhe);
            MessageDialog.openError(getShell(), Messages.getString("Error"),
                    Messages.getString("Unknown server: ") + uhe.getLocalizedMessage()); //$NON-NLS-1$
            return null;
        } catch (Exception e) {
            // FIXME: could be: wrong server name, no network, server temporarily not reachable - better text
            log.error("Error while updating team list at " //$NON-NLS-1$
                    + selected.getServerURL(), e);
            MessageDialog.openError(getShell(), Messages.getString("Error"),
                    Messages.getString("Could not update team list: ") + e.getLocalizedMessage()); //$NON-NLS-1$
            return null;
        }
    }
}
