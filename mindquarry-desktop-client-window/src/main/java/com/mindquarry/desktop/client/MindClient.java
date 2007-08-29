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
package com.mindquarry.desktop.client;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.tmatesoft.svn.core.internal.wc.SVNFileUtil;

import com.mindquarry.desktop.client.action.ActionBase;
import com.mindquarry.desktop.client.action.app.CloseAction;
import com.mindquarry.desktop.client.action.app.OpenWebpageAction;
import com.mindquarry.desktop.client.action.app.PreferencesAction;
import com.mindquarry.desktop.client.action.app.ShowMainWindowAction;
import com.mindquarry.desktop.client.action.task.SynchronizeTasksAction;
import com.mindquarry.desktop.client.action.workspace.UpdateWorkspacesAction;
import com.mindquarry.desktop.client.widget.app.CategoryWidget;
import com.mindquarry.desktop.client.widget.team.TeamlistWidget;
import com.mindquarry.desktop.client.widget.util.IconActionThread;
import com.mindquarry.desktop.model.team.Team;
import com.mindquarry.desktop.preferences.PreferenceUtilities;
import com.mindquarry.desktop.preferences.dialog.FilteredPreferenceDialog;
import com.mindquarry.desktop.preferences.pages.GeneralSettingsPage;
import com.mindquarry.desktop.preferences.pages.ServerProfilesPage;
import com.mindquarry.desktop.preferences.profile.Profile;
import com.mindquarry.desktop.splash.SplashScreen;
import com.mindquarry.desktop.util.AutostartUtilities;
import com.mindquarry.desktop.util.NotAuthorizedException;

/**
 * Main class for the Mindquarry Desktop Client.
 * 
 * @author <a href="saar(at)mindquarry(dot)com">Alexander Saar</a>
 */
public class MindClient extends ApplicationWindow {
    // #########################################################################
    // ### CONSTANTS & STATIC MEMBERS
    // #########################################################################
    public static final String ID = MindClient.class.getSimpleName();
    public static final String APPLICATION_NAME = "Mindquarry Desktop Client";
    public static final String CONTEXT_FILE = "/com/mindquarry/desktop/client/client-context.xml";
    public static final String ACTIONS_CONTEXT_FILE = "/com/mindquarry/desktop/client/client-actions-context.xml";

    public static final String PREF_FILE = PreferenceUtilities.SETTINGS_FOLDER
            + "/mindclient.settings"; //$NON-NLS-1$

    public static final String CLIENT_IMG_KEY = "client-icon";

    public static final String TASK_TITLE_FONT_KEY = "task-title";
    public static final String TEAM_NAME_FONT_KEY = "team-name";

    public static final List<String> INITIAL_TOOLBAR_GROUPS = new ArrayList<String>();
    public static final List<String> DEFAULT_TOOLBAR_GROUPS = new ArrayList<String>();

    static {
        DEFAULT_TOOLBAR_GROUPS.add(ActionBase.MANAGEMENT_ACTION_GROUP);

        INITIAL_TOOLBAR_GROUPS.add(ActionBase.WORKSPACE_ACTION_GROUP);
        INITIAL_TOOLBAR_GROUPS.addAll(DEFAULT_TOOLBAR_GROUPS);
    }

    private static BeanFactory factory;

    // #########################################################################
    // ### MEMBERS
    // #########################################################################
    private File prefFile;
    private PreferenceStore store;

    private Menu trayMenu;
    private Menu profilesMenu;
    private List<MenuItem> profilesInMenu = new ArrayList<MenuItem>();

    private static IconActionThread iconAction;

    private TrayItem trayItem;
    private List<ActionBase> actions = new ArrayList<ActionBase>();

    private TeamlistWidget teamList;

    private FileTypeMap mimeMap = MimetypesFileTypeMap.getDefaultFileTypeMap();
    private CategoryWidget categoryWidget;

    // #########################################################################
    // ### CONSTRUCTORS & MAIN
    // #########################################################################
    public MindClient() {
        super(null);

        // initialize preferences
        prefFile = new File(PREF_FILE);
        store = new PreferenceStore(prefFile.getAbsolutePath());
    }

    /**
     * The application entry point for the desktop client.
     * 
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        // show splash
        SplashScreen splash = SplashScreen.newInstance(5);
        splash.show();

        // initialize application context
        factory = new ClassPathXmlApplicationContext(new String[] {
                CONTEXT_FILE, ACTIONS_CONTEXT_FILE });

        // run editor
        MindClient client = (MindClient) factory.getBean(MindClient.ID);
        splash.step();

        client.addToolBar(SWT.FLAT | SWT.WRAP);
        splash.step();

        client.addStatusLine();
        splash.step();

        client.setBlockOnOpen(true);
        splash.step();

        client.checkArguments(args);
        splash.step();

        client.open();
    }

    // #########################################################################
    // ### PUBLIC METHODS
    // #########################################################################
    public PreferenceStore getPreferenceStore() {
        return store;
    }

    public void saveOptions() {
        try {
            store.save();
        } catch (Exception e) {
            MessageDialog.openError(getShell(), Messages.getString("Error"),
                    Messages.getString("Could not save MindClient settings")
                            + ": " + e.toString());
        }
        AutostartUtilities.setAutostart(store
                .getBoolean(GeneralSettingsPage.AUTOSTART),
                "mindquarry-desktop-client.jar"); //$NON-NLS-1$
    }

    public void showPreferenceDialog(boolean showProfiles) {
        showPreferenceDialog(showProfiles, true);
    }

    public void startAction(final String description) {
        if (iconAction == null) {
            // happens at startup
            return;
        }
        iconAction.startAction(description);
    }

    public void stopAction(String description) {
        if (iconAction == null) {
            // happens at startup
            return;
        }
        iconAction.stopAction(description);
    }

    public void setTasksActive() {
        activateActionGroup(ActionBase.TASK_ACTION_GROUP);
    }

    public void setFilesActive() {
        activateActionGroup(ActionBase.WORKSPACE_ACTION_GROUP);
    }

    public ActionBase getAction(String id) {
        for (ActionBase action : actions) {
            if (action.getId().equals(id)) {
                return action;
            }
        }
        return null;
    }

    public List<Team> getSelectedTeams() {
        return teamList.getSelectedTeams();
    }

    public List<Team> getTeams() {
        return teamList.getTeams();
    }

    public String getMimeType(File file) {
        return mimeMap.getContentType(file);
    }

    public CategoryWidget getCategoryWidget() {
        return categoryWidget;
    }
    
    /**
     * Displays an error message and prompts the user to check their credentials
     * in the preferences dialog, or to cancel.
     * 
     * @param exception Contains the error message to be displayed.
     * @return True if and only if preferences dialog was shown to user which
     *         means that the credentials were potentially updated.
     */
    public Boolean handleNotAuthorizedException(NotAuthorizedException exception) {
        // create custom error message with the option to open the preferences dialog
        MessageDialog messageDialog = new MessageDialog(
                getShell(),
                Messages.getString("Error"), //$NON-NLS-1$
                null,
                (exception.getLocalizedMessage()
                        + "\n\n" //$NON-NLS-1$
                        + Messages.getString("Please check your username and password settings in the preferences dialog.")), //$NON-NLS-1$
                MessageDialog.ERROR,
                new String[] {
                        Messages.getString("Go to preferences"), //$NON-NLS-1$
                        Messages.getString("Cancel") //$NON-NLS-1$
                },
                0);
        
        int buttonClicked = messageDialog.open();
        switch(buttonClicked) {
        case 0: // go to preferences
            showPreferenceDialog(true);
            return true;
            
        case 1: // cancel
            // TODO: need to disable all features in the client that are
            // dependent on a correctly working connection
            return false;
        }
        return false;
    }

    // #########################################################################
    // ### PROTECTED METHODS
    // #########################################################################
    protected ToolBarManager createToolBarManager(int style) {
        ToolBarManager manager = super.createToolBarManager(style);

        // create toolbar groups
        manager.add(new GroupMarker(ActionBase.WORKSPACE_ACTION_GROUP));
        manager.add(new GroupMarker(ActionBase.TASK_ACTION_GROUP));
        manager.add(new GroupMarker(ActionBase.MANAGEMENT_ACTION_GROUP));
        manager.appendToGroup(ActionBase.MANAGEMENT_ACTION_GROUP,
                new Separator());

        for (ActionBase action : actions) {
            if ((action.isToolbarAction())
                    && (INITIAL_TOOLBAR_GROUPS.contains(action.getGroup()))) {
                manager.appendToGroup(action.getGroup(), action);
            }
        }
        return manager;
    }

    protected Control createContents(Composite parent) {
        initRegistries();

        SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);
        sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));

        teamList = new TeamlistWidget(sashForm, SWT.NONE, this);
        categoryWidget = new CategoryWidget(sashForm, SWT.NONE, this);
        sashForm.setWeights(new int[] { 1, 3 });

        // initialize window shell
        Window.setDefaultImage(JFaceResources.getImage(CLIENT_IMG_KEY));
        getShell().addShellListener(new IconifyingShellListener());
        getShell().setImage(JFaceResources.getImage(CLIENT_IMG_KEY));
        getShell().setText(APPLICATION_NAME);
        getShell().setSize(800, 600);

        createTrayIconAndMenu(Display.getDefault());
        return parent;
    }

    public void enableActions(boolean enabled, String group) {
        for (ActionBase action : actions) {
            if (action.getGroup().equals(group)) {
                action.setEnabled(enabled);
            }
        }
    }

    // #########################################################################
    // ### PRIVATE METHODS
    // #########################################################################
    private void activateActionGroup(String group) {
        for (ActionBase action : actions) {
            if ((action.isToolbarAction())
                    && (!DEFAULT_TOOLBAR_GROUPS.contains(action.getGroup()))) {
                getToolBarManager().remove(action.getId());
            }
            if ((action.isToolbarAction()) && (action.getGroup().equals(group))) {
                getToolBarManager().appendToGroup(group, action);
            }
        }
        getToolBarManager().update(true);
    }
    
    private void checkArguments(String[] args) {
        if (args.length == 3 && prefFile.exists()) {
            loadOptions();
            if (!profileNameExists(args[0])) {
                addNewProfile(args[0], args[1], args[2]);
                showPreferenceDialog(true, false);
            } else {
                // don't show dialog -- probably started via Windows desktop
                // link
            }
        } else if (args.length == 3 && !prefFile.exists()) {
            addNewProfile(args[0], args[1], args[2]);
            showPreferenceDialog(true);
        } else if (!prefFile.exists()) {
            addNewProfile(Messages.getString("Your Mindquarry Server Profile"), //$NON-NLS-1$
                    Messages.getString("http://your.mindquarry.server"), //$NON-NLS-1$
                    Messages.getString("LoginID")); //$NON-NLS-1$
            showPreferenceDialog(true);
        } else {
            loadOptions();
        }
    }

    private boolean profileNameExists(String name) {
        String[] keys = store.preferenceNames();
        for (int i = 0; i < keys.length; i++) {
            if (keys[i].endsWith("." //$NON-NLS-1$
                    + Profile.PREF_NAME)
                    && store.getString(keys[i]).equals(name)) {
                return true;
            }
        }
        return false;
    }

    private boolean addNewProfile(String name, String endpoint, String login) {
        // add new profile entry
        Profile profile = new Profile();
        profile.setName(name);
        profile.setServerURL(endpoint);
        profile.setLogin(login);
        profile.setPassword(""); //$NON-NLS-1$

        File wsFolder = new File(System.getProperty("user.home") //$NON-NLS-1$ 
                + "/Mindquarry Workspaces"); //$NON-NLS-1$
        if (!wsFolder.exists()) {
            wsFolder.mkdirs();
        }
        URL url;
        try {
            url = new URL(endpoint);
            profile.setWorkspaceFolder(wsFolder.getAbsolutePath() + "/"
                    + url.getHost());
        } catch (MalformedURLException e) {
            MessageDialog.openError(getShell(), "Error", e
                    .getLocalizedMessage());
            profile.setWorkspaceFolder(wsFolder.getAbsolutePath());
        }
        return Profile.addProfile(store, profile);
    }

    private void showPreferenceDialog(boolean showProfiles, boolean loadProfiles) {
        if (loadProfiles) {
            loadOptions();
        }
        // Create the preferences dialog
        FilteredPreferenceDialog dlg = new FilteredPreferenceDialog(new Shell(
                SWT.ON_TOP), PreferenceUtilities.getDefaultPreferenceManager());
        dlg.setPreferenceStore(store);
        if (showProfiles) {
            dlg.setSelectedNode(ServerProfilesPage.NAME);
        }
        dlg.open();
        saveOptions();
    }

    private void loadOptions() {
        try {
            PreferenceUtilities.checkPreferenceFile(prefFile);
            store.load();
        } catch (Exception e) {
            MessageDialog.openError(getShell(), Messages.getString("Error"),
                    Messages.getString("Could not load MindClient settings")
                            + ": " + e.toString());
        }
    }

    private void initRegistries() {
        ImageRegistry reg = JFaceResources.getImageRegistry();

        Image img = new Image(
                Display.getCurrent(),
                MindClient.class
                        .getResourceAsStream("/com/mindquarry/icons/16x16/logo/mindquarry-icon.png")); //$NON-NLS-1$
        reg.put(CLIENT_IMG_KEY, img);

        FontRegistry fReg = JFaceResources.getFontRegistry();
        fReg.put(TASK_TITLE_FONT_KEY, new FontData[] { new FontData("Arial", //$NON-NLS-1$
                12, SWT.NONE) });
        fReg.put(TEAM_NAME_FONT_KEY, new FontData[] { new FontData("Arial", //$NON-NLS-1$
                10, SWT.NONE) });
    }

    private void createTrayIconAndMenu(Display display) {
        // create tray item
        Tray tray = display.getSystemTray();
        Shell shell = new Shell(display);

        trayItem = new TrayItem(tray, SWT.NONE);
        trayItem.setImage(JFaceResources.getImage(CLIENT_IMG_KEY));

        trayMenu = new Menu(shell, SWT.POP_UP);
        
        if (SVNFileUtil.isOSX) {
            // no right click on tray icons in OSX, do the menu on left click
            trayItem.addSelectionListener(new SelectionListener() {
                public void widgetDefaultSelected(SelectionEvent e) {
                    // double click: nothing to do here
                }

                public void widgetSelected(SelectionEvent e) {
                    // single left click => show menu
                    trayMenu.setVisible(true);
                }
            });
            
        } else {
            // left-click
            trayItem.addSelectionListener(new SelectionListener() {
                public void widgetDefaultSelected(SelectionEvent e) {
                    // double click: nothing to do here
                }

                public void widgetSelected(SelectionEvent e) {
                    // single left click => show window
                    if (getShell().isVisible()) {
                        getShell().setVisible(false);
                    } else {
                        getShell().open();
                    }
                }
            });
            
            // right-click / context menu => show menu
            trayItem.addListener(SWT.MenuDetect, new Listener() {
                public void handleEvent(Event event) {
                    trayMenu.setVisible(true);
                }
            });
        }
        
        ActionContributionItem showMainWindowAction =
            new ActionContributionItem(new ShowMainWindowAction(this));
        showMainWindowAction.fill(trayMenu, trayMenu.getItemCount());
        
        MenuItem menuItem = new MenuItem(trayMenu, SWT.SEPARATOR);
        
        // profiles sub menu
        menuItem = new MenuItem(trayMenu, SWT.CASCADE);
        menuItem.setText(Messages.getString("Server Profiles")); //$NON-NLS-1$

        profilesMenu = new Menu(shell, SWT.DROP_DOWN);
        profilesMenu.addListener(SWT.Show, new Listener() {
            public void handleEvent(Event event) {
                updateProfileSelector();
            }
        });
        updateProfileSelector();

        menuItem.setMenu(profilesMenu);

        // open web page action
        menuItem = new MenuItem(trayMenu, SWT.SEPARATOR);
        ActionContributionItem webpageAction = new ActionContributionItem(
                getAction(OpenWebpageAction.class.getName()));
        webpageAction.fill(trayMenu, trayMenu.getItemCount());

        // options dialog
        ActionContributionItem preferencesAction = new ActionContributionItem(
                getAction(PreferencesAction.class.getName()));
        preferencesAction.fill(trayMenu, trayMenu.getItemCount());

        // exit action
        menuItem = new MenuItem(trayMenu, SWT.SEPARATOR);
        ActionContributionItem closeAction = new ActionContributionItem(
                getAction(CloseAction.class.getName()));
        closeAction.fill(trayMenu, trayMenu.getItemCount());

        iconAction = new IconActionThread(trayItem, getShell());
        iconAction.setDaemon(true);
        iconAction.start();
    }

    private void updateProfileSelector() {
        if ((trayMenu == null) || (profilesMenu == null)) {
            return; // happens at very first start
        }
        MenuItem[] menuItems = profilesMenu.getItems();

        // remove the existing profiles first
        for (int i = 0; i < menuItems.length; i++) {
            if ((menuItems[i].getStyle() & SWT.RADIO) != 0) {
                menuItems[i].dispose();
            }
        }
        Iterator<Profile> pIt = Profile.loadProfiles(getPreferenceStore())
                .iterator();
        profilesInMenu = new ArrayList<MenuItem>();

        int i = 0;
        boolean hasSelection = false;
        MenuItem firstMenuItem = null;
        while (pIt.hasNext()) {
            Profile profile = pIt.next();
            final MenuItem menuItem = new MenuItem(profilesMenu, SWT.RADIO, i);
            if (i == 0) {
                firstMenuItem = menuItem;
            }
            i++;
            menuItem.setText(profile.getName());
            menuItem.addListener(SWT.Selection, new Listener() {
                public void handleEvent(Event event) {
                    if (((MenuItem) event.widget).getSelection()) {
                        Profile.selectProfile(getPreferenceStore(), menuItem
                                .getText());
                        saveOptions();
                        teamList.refresh();
                    }
                }
            });
            // activate selected profile in menu
            if ((Profile.getSelectedProfile(getPreferenceStore()) != null)
                    && (profile.getName().equals(Profile.getSelectedProfile(
                            getPreferenceStore()).getName()))) {
                menuItem.setSelection(true);
                hasSelection = true;
            }
            profilesInMenu.add(menuItem);
        }
        if (!hasSelection && firstMenuItem != null) {
            Profile
                    .selectProfile(getPreferenceStore(), firstMenuItem
                            .getText());
        }
    }

    private void hideMainWindow() {
        getShell().setVisible(false);
    }

    private void refreshOnStartup() {
        teamList.refresh();
        getAction(UpdateWorkspacesAction.class.getName()).run();
        getAction(SynchronizeTasksAction.class.getName()).run();
    }

    // #########################################################################
    // ### INNER CLASSES
    // #########################################################################
    class IconifyingShellListener implements ShellListener {
        boolean first = true;

        public void shellClosed(ShellEvent e) {
        }

        public void shellActivated(ShellEvent e) {
            if (first) {
                first = false;
                refreshOnStartup();
            }
        }

        public void shellDeactivated(ShellEvent e) {
        }

        public void shellDeiconified(ShellEvent e) {
        }

        public void shellIconified(ShellEvent e) {
            hideMainWindow();
        }
    }

    public void setActions(List<ActionBase> actions) {
        this.actions = actions;
    }
    
    public boolean close() {
        getShell().setVisible(false);
        return false;
    }

    public TrayItem getTrayItem() {
        return this.trayItem;
    }
}
