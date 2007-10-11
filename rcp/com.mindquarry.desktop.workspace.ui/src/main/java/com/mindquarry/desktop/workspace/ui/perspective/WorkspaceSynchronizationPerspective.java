package com.mindquarry.desktop.workspace.ui.perspective;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import com.mindquarry.desktop.team.ui.view.TeamlistView;
import com.mindquarry.desktop.workspace.ui.view.WorkspaceChangesView;

public class WorkspaceSynchronizationPerspective implements IPerspectiveFactory {
    public static final String WORKSPACE_FOLDER_ID = "workspace";

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

        // add workspace changes view
        IFolderLayout folder = layout.createFolder(WORKSPACE_FOLDER_ID,
                IPageLayout.TOP, 0.5f, editorArea);
        folder.addPlaceholder(WorkspaceChangesView.ID + ":*");
        folder.addView(WorkspaceChangesView.ID);
    }
}
