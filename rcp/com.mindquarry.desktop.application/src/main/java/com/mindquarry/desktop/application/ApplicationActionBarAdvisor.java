package com.mindquarry.desktop.application;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

/**
 * An action bar advisor is responsible for creating, adding, and disposing of
 * the actions added to a workbench window. Each window will be populated with
 * new actions.
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {
    // Actions - important to allocate these only in makeActions, and then use
    // them in the fill methods. This ensures that the actions aren't recreated
    // when fillActionBars is called with FILL_PROXY.
    private IWorkbenchAction propertiesAction;
    private IWorkbenchAction preferencesAction;
    private IWorkbenchAction exitAction;

    private IWorkbenchAction introAction;
    private IWorkbenchAction helpContentsAction;
    private IWorkbenchAction helpSearchAction;
    private IWorkbenchAction aboutAction;

    public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
        super(configurer);
    }

    /**
     * @see org.eclipse.ui.application.ActionBarAdvisor#makeActions(org.eclipse.ui.IWorkbenchWindow)
     */
    @Override
    protected void makeActions(final IWorkbenchWindow window) {
        // Creates the actions and registers them.

        // Registering is needed to ensure that key bindings work.
        // The corresponding commands key bindings are defined in the plugin.xml

        // Registering also provides automatic disposal of the actions when the
        // window is closed.

        // actions for the file menu
        propertiesAction = ActionFactory.PROPERTIES.create(window);
        register(propertiesAction);

        preferencesAction = ActionFactory.PREFERENCES.create(window);
        register(preferencesAction);

        exitAction = ActionFactory.QUIT.create(window);
        register(exitAction);

        // actions for the help menu
        introAction = ActionFactory.INTRO.create(window);
        register(introAction);

        helpContentsAction = ActionFactory.HELP_CONTENTS.create(window);
        register(helpContentsAction);

        helpSearchAction = ActionFactory.HELP_SEARCH.create(window);
        register(helpSearchAction);

        aboutAction = ActionFactory.ABOUT.create(window);
        register(aboutAction);
    }

    /**
     * @see org.eclipse.ui.application.ActionBarAdvisor#fillMenuBar(org.eclipse.jface.action.IMenuManager)
     */
    @Override
    protected void fillMenuBar(IMenuManager menuBar) {
        // create file menu
        MenuManager fileMenu = new MenuManager("&File",
                IWorkbenchActionConstants.M_FILE);
        menuBar.add(fileMenu);

        // Add a group marker indicating where action set menus will appear.
        menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));

        // create window menu
        MenuManager windowMenu = new MenuManager("&Window",
                IWorkbenchActionConstants.M_WINDOW);
        menuBar.add(windowMenu);

        // create help menu
        MenuManager helpMenu = new MenuManager("&Help",
                IWorkbenchActionConstants.M_HELP);
        menuBar.add(helpMenu);

        // fill file menu
        fileMenu.add(propertiesAction);
        fileMenu.add(preferencesAction);
        fileMenu.add(new Separator());
        fileMenu.add(exitAction);

        // fill help menu
        helpMenu.add(introAction);
        helpMenu.add(new Separator());
        helpMenu.add(helpContentsAction);
        helpMenu.add(helpSearchAction);
        helpMenu.add(new Separator());
        helpMenu.add(aboutAction);
    }

    /**
     * @see org.eclipse.ui.application.ActionBarAdvisor#fillCoolBar(org.eclipse.jface.action.ICoolBarManager)
     */
    @Override
    protected void fillCoolBar(ICoolBarManager coolBar) {
        IToolBarManager toolbar = new ToolBarManager(SWT.FLAT | SWT.RIGHT);
        toolbar.add(helpContentsAction);
        toolbar.add(helpSearchAction);
        coolBar.add(new ToolBarContributionItem(toolbar, "help"));
    }
}
