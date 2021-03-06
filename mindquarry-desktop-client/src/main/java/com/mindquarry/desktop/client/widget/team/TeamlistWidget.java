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

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentException;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceStore;
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
import org.eclipse.swt.widgets.TableItem;

import com.mindquarry.desktop.client.I18N;
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
        viewer = new TableViewer(parent, SWT.SINGLE | SWT.CHECK
                | SWT.FULL_SELECTION | SWT.BORDER);
        viewer.setContentProvider(new TeamlistContentProvider());
        viewer.setLabelProvider(new TeamlistLabelProvider());
        viewer.getTable().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (e.detail == SWT.CHECK) {
                    setSelectedTeams();
                }
            }
        });
        viewer.getTable().setLayoutData(
                new GridData(GridData.FILL, GridData.FILL, true, true, 2, 1));
        viewer.getTable().setFont(
                JFaceResources.getFont(MindClient.TEAM_NAME_FONT_KEY));

        Menu menu = new Menu(viewer.getTable());
        viewer.getTable().setMenu(menu);

        RefreshTeamlistAction action = (RefreshTeamlistAction) client
                .getAction(RefreshTeamlistAction.class.getName());
        action.setTeamList(this);

        ActionContributionItem refreshTeamsAction = new ActionContributionItem(
                action);
        refreshTeamsAction.fill(menu, menu.getItemCount());

        // create selection buttons
        Button button = new Button(parent, SWT.PUSH | SWT.FLAT | SWT.CENTER);
        button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        button.setText(I18N.getString("Select All"));
        button.setFont(JFaceResources.getFont(MindClient.TEAM_NAME_FONT_KEY));
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                selectAll();
            }
        });
        button = new Button(parent, SWT.PUSH | SWT.FLAT | SWT.CENTER);
        button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        button.setText(I18N.getString("Deselect All"));
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
        client.startAction(I18N.getString("Updating list of teams")); //$NON-NLS-1$
        // TODO: show progress bar in the widget itself
        try {
            Profile profile = Profile.getSelectedProfile(client
                    .getPreferenceStore());
            if (profile.getType() == Profile.Type.MindquarryServer) {
            	viewer.setInput(queryTeams());
            } else {
            	viewer.setInput(profile.getTeamsFromSVNRepos());
            }
        } finally {
            client.stopAction(I18N.getString("Updating list of teams")); //$NON-NLS-1$
        }
    }

    public void selectAll() {
        setChecked(true);
    }

    public void loadSelection() {
        Profile profile = Profile.getSelectedProfile(client
                .getPreferenceStore());

        TableItem[] tis = viewer.getTable().getItems();
        for (TableItem item : tis) {
            if (profile.getSelectedTeams().contains(
                    ((Team) item.getData()).getId())) {
                item.setChecked(true);
            }
        }
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
        setSelectedTeams();
    }

    private void setSelectedTeams() {
        PreferenceStore store = client.getPreferenceStore();
        List<Profile> profiles = Profile.loadProfiles(store);
        Profile selected = Profile.getSelectedProfile(store);

        for (Profile profile : profiles) {
            if (profile.getName().equals(selected.getName())) {
                profile.clearSelectedTeams();

                // set selected teams
                TableItem[] tis = viewer.getTable().getItems();
                for (TableItem item : tis) {
                    if (item.getChecked()) {
                        profile.selectTeam(((Team) item.getData()).getId());
                    }
                }
                break;
            }
        }
        Profile.storeProfiles(store, profiles);
    }

    private TeamList queryTeams() throws CancelException {
        Profile selected = Profile.getSelectedProfile(client
                .getPreferenceStore());
        if (selected == null) {
            MessageDialog
                    .openError(
                            getShell(),
                            I18N.getString("Error"),
                            I18N
                                    .getString("Currently there is no profile selected. Please select the profile of the server you want to work with or create a new profile."));
            return null;
        }
        // retrieve list of teams
        TeamList teamList;
        String teamUrl = selected.getServerURL() + "/teams";
        try {
            teamList = new TeamList(teamUrl, //$NON-NLS-1$
                    selected.getLogin(), selected.getPassword());
            return teamList;
        } catch (NotAuthorizedException e) {
            log.error("Error while updating team list at " //$NON-NLS-1$
                    + selected.getServerURL(), e);

            if (client.handleNotAuthorizedException(e)) {
                return queryTeams();
            }

            throw new CancelException(
                    "Updating team list cancelled due to wrong credentials.", e);
        } catch (UnknownHostException uhe) {
            log.error("Error while updating team list at " //$NON-NLS-1$
                    + selected.getServerURL(), uhe);

            if (client.handleUnknownHostException(uhe)) {
                return queryTeams();
            }

            throw new CancelException(
                    "Updating team list cancelled due to unknown server.", uhe);
        } catch (MalformedURLException murle) {
            log.error("Error while updating team list at " //$NON-NLS-1$
                    + selected.getServerURL(), murle);

            if (client.handleMalformedURLException(murle)) {
                return queryTeams();
            }

            throw new CancelException(
                    "Updating team list cancelled due to unknown server.",
                    murle);
        } catch (Exception e) {
            // FIXME: could be: wrong server name, no network, server
            // temporarily not reachable - better text
            log.error("Error while updating team list at " //$NON-NLS-1$
                    + selected.getServerURL(), e);
            String msg = I18N.get(
                    "Could not update team list from {0}: ", //$NON-NLS-1$
                    selected.getServerURL())
                    + e.getLocalizedMessage();
            if (e.getCause() != null
                    && e.getCause().getClass() == DocumentException.class) {
                // this happens when HTML is returned instead of XML
                msg = I18N
                        .get(
                                "The specified URL \"{0}\" does belong to a "
                                        + "running Mindquarry server. No team information found at {1}",
                                selected.getServerURL(), teamUrl);
            }
            MessageDialog.openError(getShell(), I18N.getString("Error"), //$NON-NLS-1$
                    msg);
            return null;
        }
    }
}
