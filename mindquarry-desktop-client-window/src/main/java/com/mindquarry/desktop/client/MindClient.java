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
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import com.mindquarry.desktop.client.action.ImageAndTextToolbarManager;
import com.mindquarry.desktop.client.action.app.AboutAction;
import com.mindquarry.desktop.client.action.app.CloseAction;
import com.mindquarry.desktop.client.action.app.OpenWebpageAction;
import com.mindquarry.desktop.client.action.app.PreferencesAction;
import com.mindquarry.desktop.client.action.app.ShowMainWindowAction;
import com.mindquarry.desktop.client.action.task.SynchronizeTasksAction;
import com.mindquarry.desktop.client.action.workspace.UpdateWorkspacesAction;
import com.mindquarry.desktop.client.widget.app.CategoryWidget;
import com.mindquarry.desktop.client.widget.team.TeamlistWidget;
import com.mindquarry.desktop.client.widget.util.IconActionThread;
import com.mindquarry.desktop.event.EventBus;
import com.mindquarry.desktop.event.EventListener;
import com.mindquarry.desktop.model.team.Team;
import com.mindquarry.desktop.preferences.PreferenceUtilities;
import com.mindquarry.desktop.preferences.dialog.FilteredPreferenceDialog;
import com.mindquarry.desktop.preferences.pages.GeneralSettingsPage;
import com.mindquarry.desktop.preferences.pages.ServerProfilesPage;
import com.mindquarry.desktop.preferences.profile.Profile;
import com.mindquarry.desktop.splash.SplashScreen;
import com.mindquarry.desktop.util.AutostartUtilities;
import com.mindquarry.desktop.util.NotAuthorizedException;
import com.mindquarry.desktop.workspace.exception.CancelException;

/**
 * Main class for the Mindquarry Desktop Client.
 * 
 * @author <a href="saar(at)mindquarry(dot)com">Alexander Saar</a>
 */
public class MindClient extends ApplicationWindow implements EventListener {
    // #########################################################################
    // ### CONSTANTS & STATIC MEMBERS
    // #########################################################################
    private static final Log log = LogFactory.getLog(MindClient.class);
    
    public static final String ID = MindClient.class.getSimpleName();
    public static final String APPLICATION_NAME = "Mindquarry Desktop Client";
    public static final String CONTEXT_FILE = "/com/mindquarry/desktop/client/client-context.xml";
    public static final String ACTIONS_CONTEXT_FILE = "/com/mindquarry/desktop/client/client-actions-context.xml";

    public static final String PREF_FILE = PreferenceUtilities.SETTINGS_FOLDER
            + "/mindclient.settings"; //$NON-NLS-1$

    private static final String LOCK_FILE = PreferenceUtilities.SETTINGS_FOLDER
            + "/mindclient.lock"; //$NON-NLS-1$

    public static final String CLIENT_IMG_KEY = "client-icon";
    public static final String CLIENT_TRAY_IMG_KEY = "client-tray-icon";

    public static final String TASK_TITLE_FONT_KEY = "task-title";
    public static final String TEAM_NAME_FONT_KEY = "team-name";

    public static final List<String> INITIAL_TOOLBAR_GROUPS = new ArrayList<String>();
    public static final List<String> DEFAULT_TOOLBAR_GROUPS = new ArrayList<String>();

    // The Windows autostart feature works by setting a value (the path
    // to the desktop client JAR) in the registry. For this it needs to
    // know its own installation path. It looks through the classpath
    // searching for one of the following JARs:
    public static final Set<String> JAR_NAMES = new HashSet<String>();
    static {
        JAR_NAMES.add("mindquarry-desktop-client.jar");
        JAR_NAMES.add("mindquarry-desktop-client-windows.jar");
        JAR_NAMES.add("mindquarry-desktop-client-win32.jar");
        // for the JNLP distribution:
        JAR_NAMES.add("MindClient.jar");
    }

    static {
        DEFAULT_TOOLBAR_GROUPS.add(ActionBase.MANAGEMENT_ACTION_GROUP);
        DEFAULT_TOOLBAR_GROUPS.add(ActionBase.STOP_ACTION_GROUP);

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

    private FileLock fileLock;

    private EventBus eventBus;

    // #########################################################################
    // ### CONSTRUCTORS & MAIN
    // #########################################################################
    public MindClient() throws IOException {
        super(null);
        createLock();
        // initialize preferences
        prefFile = new File(PREF_FILE);
        store = new PreferenceStore(prefFile.getAbsolutePath());
    }

    private void createLock() throws IOException {
        File f = new File(LOCK_FILE);

        if (!f.exists()) {
            f.createNewFile();
        }
        // this is the recommended way to implement locking, it doesn't leave
        // a lock file, not even if the app crashes:
        FileChannel fileChannel = (new FileOutputStream(f)).getChannel();
        fileLock = fileChannel.tryLock();
        if (fileLock == null) {
            MessageDialog dlg = new MessageDialog(getShell(),
                    Messages.getString("Mindquarry Client already running"), null, //$NON-NLS-1$
                    Messages.getString("The Mindquarry Desktop Client seems to be running " + //$NON-NLS-1$
                    		"already. Only one instance of the Desktop Client can be " + //$NON-NLS-1$
                    		"running at a time. If you are sure that the Desktop Client " + //$NON-NLS-1$
                    		"isn't running, select 'Start anyway'."), MessageDialog.ERROR, //$NON-NLS-1$
                    new String[]{Messages.getString("Exit"), //$NON-NLS-1$
                    Messages.getString("Start anyway")}, 0); //$NON-NLS-1$
            int result = dlg.open();
            if (result == 0) {
                System.exit(1);
            } else {
                log.warn("Starting despite lock file"); //$NON-NLS-1$
            }
        } else {
            log.info("Aquired lock: " + fileLock);
        }
        // Note: no need to delete the lock, the JVM will care about that
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

        client.initMacIfAvailable();
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
                    Messages.getString("Could not save Desktop Client settings")
                            + ": " + e.toString());
        }
        AutostartUtilities.setAutostart(store
                .getBoolean(GeneralSettingsPage.AUTOSTART),
                JAR_NAMES); //$NON-NLS-1$
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
     * Disable all features in the client that are dependent on a correctly
     * working connection.
     */
    private void displayNotConnected() {
        String message = Messages.getString("Not connected.\n" //$NON-NLS-1$
                + "Please click the 'Refresh' button to connect."); //$NON-NLS-1$

        teamList.clear();
        categoryWidget.getWorkspaceBrowser().showErrorMessage(message);
        categoryWidget.getTaskContainer().updateContainer(false, message, false); //$NON-NLS-1$
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
            displayNotConnected();
            return false;
        }
        return false;
    }
    
    /**
     * Displays an error message and prompts the user to check their server
     * settings in the preferences dialog, or to cancel.
     * 
     * @param exception Contains the unknown host.
     * @return True if and only if preferences dialog was shown to user which
     *         means that the server details were potentially updated.
     */
    public Boolean handleUnknownHostException(UnknownHostException exception) {
        // create custom error message with the option to open the preferences dialog
        MessageDialog messageDialog = new MessageDialog(
                getShell(),
                Messages.getString("Error"), //$NON-NLS-1$
                null,
                (Messages.getString("Unknown server: ") + "\"" //$NON-NLS-1$ //$NON-NLS-2$
                        + exception.getLocalizedMessage()
                        + "\".\n\n" //$NON-NLS-1$
                        + Messages.getString("Please check your Mindquarry server URL in the preferences dialog.")), //$NON-NLS-1$
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
            displayNotConnected();
            return false;
        }
        return false;
    }

    // #########################################################################
    // ### PROTECTED METHODS
    // #########################################################################
    protected ToolBarManager createToolBarManager(int style) {
        ToolBarManager manager = new ImageAndTextToolbarManager(style);

        // create toolbar groups
        manager.add(new GroupMarker(ActionBase.WORKSPACE_ACTION_GROUP));
        manager.add(new GroupMarker(ActionBase.TASK_ACTION_GROUP));
        manager.add(new GroupMarker(ActionBase.STOP_ACTION_GROUP));
        manager.add(new GroupMarker(ActionBase.MANAGEMENT_ACTION_GROUP));
        manager.appendToGroup(ActionBase.MANAGEMENT_ACTION_GROUP,
                new Separator());

        for (ActionBase action : actions) {
            if ((action.isToolbarAction())
                    && (INITIAL_TOOLBAR_GROUPS.contains(action.getGroup()))) {
                manager.appendToGroup(action.getGroup(), action);
                if (action.isEnabledByDefault()) {
                    action.setEnabled(true);
                } else {
                    action.setEnabled(false);
                }
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

        ((UpdateWorkspacesAction) getAction(UpdateWorkspacesAction.class
                .getName())).setTeamList(teamList);
        ((SynchronizeTasksAction) getAction(SynchronizeTasksAction.class
                .getName())).setTeamList(teamList);
        
        // initialize window shell
        Window.setDefaultImage(JFaceResources.getImage(CLIENT_IMG_KEY));
        getShell().addListener(SWT.Show, new Listener() {
            private boolean first = true;

            public void handleEvent(Event event) {
                if (first) {
                    first = false;
                    // TODO: send start event
                    //eventBus.sendEvent(new ProfileActivatedEvent(this, new Profile("test", "user", "pw", "http://server", "folder")));
                    refreshOnStartup();
                }
                getShell().setActive();
            }
            
        });
        getShell().addShellListener(new IconifyingShellListener());
        getShell().setImage(JFaceResources.getImage(CLIENT_IMG_KEY));
        getShell().setText(APPLICATION_NAME);
        getShell().setSize(800, 600);

        createTrayIconAndMenu(Display.getDefault());
        
        return parent;
    }

    private void initMacIfAvailable() {
//        if (SVNFileUtil.isOSX) {
//            CarbonUIEnhancer mac = new CarbonUIEnhancer(this);
//        }
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
        FilteredPreferenceDialog dlg = new FilteredPreferenceDialog(new Shell(), PreferenceUtilities.getDefaultPreferenceManager());
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
                    Messages.getString("Could not load Desktop Client settings")
                            + ": " + e.toString());
        }
    }

    private void initRegistries() {
        ImageRegistry reg = JFaceResources.getImageRegistry();

        Image img;
        if (SVNFileUtil.isOSX) {
            img = new Image(
                    Display.getCurrent(),
                    MindClient.class
                            .getResourceAsStream("/com/mindquarry/icons/128x128/logo/mindquarry-icon.png")); //$NON-NLS-1$
        } else {
            img = new Image(
                    Display.getCurrent(),
                    MindClient.class
                            .getResourceAsStream("/com/mindquarry/icons/32x32/logo/mindquarry-icon.png")); //$NON-NLS-1$
        }
        reg.put(CLIENT_IMG_KEY, img);
        
        Image trayImg = new Image(
                Display.getCurrent(),
                MindClient.class
                        .getResourceAsStream("/com/mindquarry/icons/16x16/logo/mindquarry-icon.png")); //$NON-NLS-1$
        reg.put(CLIENT_TRAY_IMG_KEY, trayImg);

        FontRegistry fReg = JFaceResources.getFontRegistry();
        fReg.put(TASK_TITLE_FONT_KEY, getSystemFont());
        fReg.put(TEAM_NAME_FONT_KEY, getSmallSystemFont());
    }

	private FontData[] getSmallSystemFont() {
		if (SVNFileUtil.isOSX) {
			// see http://developer.apple.com/documentation/UserExperience/Conceptual/OSXHIGuidelines/XHIGText/chapter_13_section_2.html
			return new FontData[] { new FontData("Lucida Grande", //$NON-NLS-1$
	                11, SWT.NONE) };
		}
		return new FontData[] { new FontData("Arial", //$NON-NLS-1$
                10, SWT.NONE) };
	}

	private FontData[] getSystemFont() {
		if (SVNFileUtil.isOSX) {
			//see http://developer.apple.com/documentation/UserExperience/Conceptual/OSXHIGuidelines/XHIGText/chapter_13_section_2.html
			return new FontData[] { new FontData("Lucida Grande", //$NON-NLS-1$
	                13, SWT.NONE) };
		}
		return new FontData[] { new FontData("Arial", //$NON-NLS-1$
                12, SWT.NONE) };
	}

    private void createTrayIconAndMenu(Display display) {
        // create tray item
        Tray tray = display.getSystemTray();
        Shell shell = new Shell(display);

        trayItem = new TrayItem(tray, SWT.NONE);
        trayItem.setImage(JFaceResources.getImage(CLIENT_TRAY_IMG_KEY));

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
                        getShell().forceActive();
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

        ActionContributionItem aboutAction =
          new ActionContributionItem(new AboutAction(this));
        aboutAction.fill(trayMenu, trayMenu.getItemCount());

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
                        try {
                            displayNotConnected();
                            teamList.refresh();                            
                        } catch (CancelException e) {
                            // TODO: better exception handling
                            log.warn("Refreshing after profile change cancelled.", e);
                        }
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
        try {
            displayNotConnected();
            teamList.refresh();
        } catch (CancelException e) {
            // TODO: better exception handling
            log.error("Refresh on startup cancelled.", e);
        }
    }

    private void refreshAll() throws CancelException {
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
        }

        public void shellDeactivated(ShellEvent e) {
        }

        public void shellDeiconified(ShellEvent e) {
        }

        public void shellIconified(ShellEvent e) {
            // on mac hiding the window after iconifying leads to an awkward behaviour
            if (!SVNFileUtil.isOSX) {
                hideMainWindow();
            }
        }
    }

    public void setActions(List<ActionBase> actions) {
        this.actions = actions;
    }
    
    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
        this.eventBus.registerEventListener(this);
    }
    
    public EventBus getEventBus() {
        return eventBus;
    }
    
    public boolean close() {
        getShell().setVisible(false);
        return false;
    }

    public TrayItem getTrayItem() {
        return this.trayItem;
    }

    public void onEvent(com.mindquarry.desktop.event.Event event) {
        // TODO Auto-generated method stub
        
    }
}
