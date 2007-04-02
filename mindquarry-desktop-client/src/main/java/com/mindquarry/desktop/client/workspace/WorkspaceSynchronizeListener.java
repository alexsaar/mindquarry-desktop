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
package com.mindquarry.desktop.client.workspace;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Widget;

import com.mindquarry.desktop.client.MindClient;
import com.mindquarry.desktop.client.workspace.widgets.SynchronizeWidget;
import com.mindquarry.desktop.preferences.profile.Profile;

/**
 * Listener that is responsible for receiving and handling workspace
 * synchronization events. After receiving such an event it does some
 * postprocessing and triggers synchronization of workspaces.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class WorkspaceSynchronizeListener implements Listener {
    private static WorkspaceSynchronizeListener instance;
    
    private Log log;

    private final MindClient client;

    private List<Widget> triggerWidgets;

    private List<SynchronizeWidget> synAreas;

    private WorkspaceSynchronizeListener(final MindClient client) {
        this.client = client;
        triggerWidgets = new ArrayList<Widget>();
        synAreas = new ArrayList<SynchronizeWidget>();
        log = LogFactory.getLog(WorkspaceSynchronizeListener.class);
    }

    public static WorkspaceSynchronizeListener getInstance(
            final MindClient client, Widget triggerWidget,
            SynchronizeWidget synArea) {
        if (instance == null) {
            instance = new WorkspaceSynchronizeListener(client);
        }
        instance.addTriggerWidget(triggerWidget);
        instance.addSynArea(synArea);
        return instance;
    }

    private void addTriggerWidget(Widget widget) {
        if (widget == null) {
            return;
        }
        if (!triggerWidgets.contains(widget)) {
            triggerWidgets.add(widget);
        }
    }

    private void addSynArea(SynchronizeWidget synArea) {
        if (synArea == null) {
            return;
        }
        if (!synAreas.contains(synArea)) {
            synAreas.add(synArea);
        }
    }

    private void setEnabled(Widget widget, boolean enable) {
        if (widget instanceof Control) {
            ((Control) widget).setEnabled(enable);
        } else if (widget instanceof MenuItem) {
            ((MenuItem) widget).setEnabled(enable);
        }
    }

    private void enableTriggerWidgets(final boolean enable,
            final List<Widget> widgets) {
        MindClient.getShell().getDisplay().syncExec(new Runnable() {
            public void run() {
                for (Widget widget : widgets) {
                    setEnabled(widget, enable);
                }
            }
        });
    }

    private void enableSynAreas(final boolean enabled,
            final List<SynchronizeWidget> synAreas) {
        MindClient.getShell().getDisplay().syncExec(new Runnable() {
            public void run() {
                for (SynchronizeWidget synArea : synAreas) {
                    synArea.setVisible(enabled);
                }
            }
        });
    }

    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event) {
        Profile selectedProfile = Profile.getSelectedProfile(client
                .getPreferenceStore());

        if (selectedProfile == null) {
            MindClient.showErrorMessage("No profile selected. Please select a profile and try again.");
            return;
        }
        new Thread(new Runnable() {
            public void run() {
                enableTriggerWidgets(false, triggerWidgets);
                enableSynAreas(true, synAreas);

                try {
                    ListWorkspacesOperation listOp = new ListWorkspacesOperation(
                            client, synAreas);
                    listOp.run();

                    // need to sync workspaces first (for merging, up-to-date
                    // working copies and so on)
                    SvnOperation op = new UpdateOperation(client, synAreas,
                            listOp.getWorkspaces());
                    op.run();
                    // share workspace changes
                    op = new PublishOperation(client, synAreas, listOp
                            .getWorkspaces());
                    op.run();
                } catch (Exception e) {
                    MindClient.showErrorMessage("Error during workspaces synchronization.");
                    log.error("Error during workspaces synchronization.", e); //$NON-NLS-1$
                }
                enableTriggerWidgets(true, triggerWidgets);
                enableSynAreas(false, synAreas);
            }
        }).start();
    }
}
