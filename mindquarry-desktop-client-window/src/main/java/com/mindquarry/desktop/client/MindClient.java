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

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
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

import com.mindquarry.desktop.client.action.ActionBase;
import com.mindquarry.desktop.client.action.app.CloseAction;
import com.mindquarry.desktop.client.action.app.OpenWebpageAction;
import com.mindquarry.desktop.client.action.app.PreferencesAction;
import com.mindquarry.desktop.client.action.task.CreateTaskAction;
import com.mindquarry.desktop.client.action.task.FinishTaskAction;
import com.mindquarry.desktop.client.action.task.SynchronizeTasksAction;
import com.mindquarry.desktop.client.action.workspace.SynchronizeWorkspacesAction;
import com.mindquarry.desktop.client.widget.util.CategoryWidget;
import com.mindquarry.desktop.client.widget.util.IconActionThread;
import com.mindquarry.desktop.client.widget.util.TeamlistWidget;
import com.mindquarry.desktop.preferences.PreferenceUtilities;
import com.mindquarry.desktop.preferences.dialog.FilteredPreferenceDialog;
import com.mindquarry.desktop.preferences.pages.GeneralSettingsPage;
import com.mindquarry.desktop.preferences.pages.ServerProfilesPage;
import com.mindquarry.desktop.preferences.pages.TaskPage;
import com.mindquarry.desktop.preferences.profile.Profile;
import com.mindquarry.desktop.splash.SplashScreen;
import com.mindquarry.desktop.util.AutostartUtilities;
import com.mindquarry.desktop.widget.NotificationWidget;

/**
 * Main class for the Mindquarry Desktop Client.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class MindClient extends ApplicationWindow {
	// #########################################################################
	// ### CONSTANTS
	// #########################################################################
	public static final String APPLICATION_NAME = "Mindquarry Desktop Client";

	public static final String PREF_FILE = PreferenceUtilities.SETTINGS_FOLDER
			+ "/mindclient.settings"; //$NON-NLS-1$

	public static final String CLIENT_IMG_KEY = "client-icon";

	public static final String TASK_TITLE_FONT_KEY = "task-title";
	public static final String TEAM_NAME_FONT_KEY = "team-name";

	public static final String WORKSPACE_ACTION_GROUP = "workspace-actions";
	public static final String TASK_ACTION_GROUP = "task-actions";
	public static final String MANAGEMENT_ACTION_GROUP = "management-actions";

	// #########################################################################
	// ### MEMBERS
	// #########################################################################
	private File prefFile;
	private PreferenceStore store;

	private Menu trayMenu;
	private Menu profilesMenu;
	private List profilesInMenu = new ArrayList();

	private static IconActionThread iconAction;

	private TrayItem trayItem;
	private List<ActionBase> actions;

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
		SplashScreen splash = SplashScreen.newInstance(6);
		splash.show();

		// run editor
		MindClient client = new MindClient();
		splash.step();

		client.initActions();
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

	public synchronized void showMessage(final String title,
			final String message) {
		NotificationWidget widget = new NotificationWidget(getShell()
				.getDisplay());
		int delay = getPreferenceStore().getInt(
				GeneralSettingsPage.NOTIFY_DELAY);
		if (delay == 0) {
			// at first start the store returns 0 which doesn't make sense, so
			// use a default:
			delay = GeneralSettingsPage.DEFAULT_NOTIFY_DELAY;
		}
		widget.show(title, message, delay * 1000);
		widget.dispose();
	}

	public void saveOptions() {
		try {
			store.save();
		} catch (Exception e) {
			showMessage(Messages.getString("com.mindquarry.desktop.client", //$NON-NLS-1$
					"error"), //$NON-NLS-1$
					Messages.getString(MindClient.class, "7") //$NON-NLS-1$
							+ ": "//$NON-NLS-1$
							+ e.toString());
		}
		AutostartUtilities.setAutostart(store
				.getBoolean(GeneralSettingsPage.AUTOSTART),
				"mindquarry-desktop-client.jar"); //$NON-NLS-1$
	}

	public void updateProfileSelector() {
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
		Iterator pIt = Profile.loadProfiles(getPreferenceStore()).iterator();
		boolean hasSelection = false;
		profilesInMenu = new ArrayList();
		int i = 0;
		while (pIt.hasNext()) {
			Profile profile = (Profile) pIt.next();
			final MenuItem menuItem = new MenuItem(profilesMenu, SWT.RADIO, i);
			i++;
			menuItem.setText(profile.getName());
			menuItem.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					Profile.selectProfile(getPreferenceStore(), menuItem
							.getText());
					saveOptions();
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
		if (!hasSelection && profilesInMenu.size() > 0) {
			MenuItem mi = (MenuItem) profilesInMenu.get(0);
			mi.setSelection(true);
		}
	}

	public void showPreferenceDialog(boolean showProfiles) {
		showPreferenceDialog(showProfiles, true);
	}

	public void startAction(String description) {
		iconAction.startAction(description);
	}

	public void stopAction(String description) {
		iconAction.stopAction(description);
	}

	public void setTasksActive() {
		getToolBarManager().remove(
				getAction(SynchronizeWorkspacesAction.class.getName()).getId());

		getToolBarManager().appendToGroup(TASK_ACTION_GROUP,
				getAction(SynchronizeTasksAction.class.getName()));
		getToolBarManager().appendToGroup(TASK_ACTION_GROUP,
				getAction(CreateTaskAction.class.getName()));
		getToolBarManager().appendToGroup(TASK_ACTION_GROUP,
				getAction(FinishTaskAction.class.getName()));
		getToolBarManager().update(true);
	}

	public void setFilesActive() {
		getToolBarManager().appendToGroup(WORKSPACE_ACTION_GROUP,
				getAction(SynchronizeWorkspacesAction.class.getName()));

		getToolBarManager().remove(
				getAction(SynchronizeTasksAction.class.getName()).getId());
		getToolBarManager().remove(
				getAction(CreateTaskAction.class.getName()).getId());
		getToolBarManager().remove(
				getAction(FinishTaskAction.class.getName()).getId());
		getToolBarManager().update(true);
	}

	// #########################################################################
	// ### PROTECTED METHODS
	// #########################################################################
	protected ToolBarManager createToolBarManager(int style) {
		ToolBarManager manager = super.createToolBarManager(style);

		// create toolbar groups
		manager.add(new GroupMarker(WORKSPACE_ACTION_GROUP));
		manager.add(new GroupMarker(TASK_ACTION_GROUP));
		manager.add(new GroupMarker(MANAGEMENT_ACTION_GROUP));

		// fill workspace group
		// manager.appendToGroup(WORKSPACE_ACTION_GROUP,
		// getAction(SynchronizeWorkspacesAction.class.getName()));

		// fill tasks group
		manager.appendToGroup(TASK_ACTION_GROUP,
				getAction(SynchronizeTasksAction.class.getName()));
		manager.appendToGroup(TASK_ACTION_GROUP,
				getAction(CreateTaskAction.class.getName()));
		manager.appendToGroup(TASK_ACTION_GROUP,
				getAction(FinishTaskAction.class.getName()));

		// fill management group
		manager.appendToGroup(MANAGEMENT_ACTION_GROUP, new Separator());
		manager.appendToGroup(MANAGEMENT_ACTION_GROUP,
				getAction(PreferencesAction.class.getName()));

		return manager;
	}

	protected Control createContents(Composite parent) {
		initRegistries();

		SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));

		new TeamlistWidget(sashForm, SWT.NONE, this);
		new CategoryWidget(sashForm, SWT.NONE, this);

		sashForm.setWeights(new int[] { 1, 3 });

		// init window shell
		Window.setDefaultImage(JFaceResources.getImage(CLIENT_IMG_KEY));
		getShell().addShellListener(new IconifyingShellListener());
		getShell().setImage(JFaceResources.getImage(CLIENT_IMG_KEY));
		getShell().setText(APPLICATION_NAME);
		getShell().setSize(800, 600);

		setStatus("Ready.");

		createTrayIconAndMenu(Display.getDefault());
		getAction(SynchronizeTasksAction.class.getName()).run();
		return parent;
	}

	// #########################################################################
	// ### PRIVATE METHODS
	// #########################################################################
	private void initActions() {
		actions = new ArrayList<ActionBase>();

		// create workspace actions
		actions.add(new SynchronizeWorkspacesAction(this));

		// create task actions
		actions.add(new SynchronizeTasksAction(this));
		actions.add(new CreateTaskAction(this));
		actions.add(new FinishTaskAction(this));

		// create management actions
		actions.add(new PreferencesAction(this));
		actions.add(new OpenWebpageAction(this));
		actions.add(new CloseAction(this));
	}

	public ActionBase getAction(String id) {
		for (ActionBase action : actions) {
			if (action.getId().equals(id)) {
				return action;
			}
		}
		return null;
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
			addNewProfile(
					Messages.getString(MindClient.class, "0"), "http://your.mindquarry.server", //$NON-NLS-1$//$NON-NLS-2$
					"LoginID"); //$NON-NLS-1$
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
				+ "/Mindquarry Workspaces");
		if (!wsFolder.exists()) {
			wsFolder.mkdirs();
		}
		URL url;
		try {
			url = new URL(endpoint);
			profile.setWorkspaceFolder(wsFolder.getAbsolutePath() + "/"
					+ url.getHost());
		} catch (MalformedURLException e) {
			showMessage("Error", e.getLocalizedMessage());
			profile.setWorkspaceFolder(wsFolder.getAbsolutePath());
		}
		return Profile.addProfile(store, profile);
	}

	private void showPreferenceDialog(boolean showProfiles, boolean loadProfiles) {
		if (loadProfiles) {
			loadOptions();
		}

		// request preference values from user
		PreferenceManager mgr = PreferenceUtilities
				.getDefaultPreferenceManager();
		mgr.addToRoot(new PreferenceNode(TaskPage.NAME, new TaskPage()));

		// Create the preferences dialog
		FilteredPreferenceDialog dlg = new FilteredPreferenceDialog(new Shell(
				SWT.ON_TOP), mgr);
		dlg.setPreferenceStore(store);
		if (showProfiles) {
			dlg.setSelectedNode(ServerProfilesPage.NAME);
		}
		dlg.open();
		saveOptions();
		updateProfileSelector();
	}

	private void loadOptions() {
		try {
			PreferenceUtilities.checkPreferenceFile(prefFile);
			store.load();
		} catch (Exception e) {
			showMessage(Messages.getString("com.mindquarry.desktop.client", //$NON-NLS-1$
					"error"), //$NON-NLS-1$
					Messages.getString(MindClient.class, "6") //$NON-NLS-1$
							+ ": "//$NON-NLS-1$
							+ e.toString());
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
				12, SWT.ITALIC) });
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
		// right-click / context menu => menu
		trayItem.addListener(SWT.MenuDetect, new Listener() {
			public void handleEvent(Event event) {
				trayMenu.setVisible(true);
			}
		});
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
		// profiles sub menu
		MenuItem menuItem = new MenuItem(trayMenu, SWT.CASCADE);
		menuItem.setText(Messages.getString(MindClient.class, "1")); //$NON-NLS-1$

		profilesMenu = new Menu(shell, SWT.DROP_DOWN);
		menuItem.setMenu(profilesMenu);

		// list all profiles in the menu
		updateProfileSelector();

		// open web page action
		menuItem = new MenuItem(trayMenu, SWT.SEPARATOR);
		ActionContributionItem webpageAction = new ActionContributionItem(
				getAction(OpenWebpageAction.class.getName()));
		webpageAction.fill(trayMenu, trayMenu.getItemCount());

		// synchronize action
		ActionContributionItem syncAction = new ActionContributionItem(
				getAction(SynchronizeWorkspacesAction.class.getName()));
		syncAction.fill(trayMenu, trayMenu.getItemCount());

		// options dialog
		menuItem = new MenuItem(trayMenu, SWT.SEPARATOR);
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

	private void hideMainWindow() {
		getShell().setVisible(false);
	}

	// #########################################################################
	// ### INNER CLASSES
	// #########################################################################

	class IconifyingShellListener implements ShellListener {
		public void shellClosed(ShellEvent e) {
		}

		public void shellActivated(ShellEvent e) {
		}

		public void shellDeactivated(ShellEvent e) {
		}

		public void shellDeiconified(ShellEvent e) {
		}

		public void shellIconified(ShellEvent e) {
			hideMainWindow();
		}
	}
}
