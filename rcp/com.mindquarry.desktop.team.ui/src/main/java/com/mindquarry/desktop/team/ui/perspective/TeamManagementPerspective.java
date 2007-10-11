package com.mindquarry.desktop.team.ui.perspective;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import com.mindquarry.desktop.team.ui.view.TeamlistView;

public class TeamManagementPerspective implements IPerspectiveFactory {
    /**
     * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
     */
    public void createInitialLayout(IPageLayout layout) {
        // disable editor area
        layout.setEditorAreaVisible(false);

        // add team list view
        String editorArea = layout.getEditorArea();
        layout.addStandaloneView(TeamlistView.ID, false, IPageLayout.LEFT,
                0.25f, editorArea);
        layout.getViewLayout(TeamlistView.ID).setCloseable(false);
    }
}
