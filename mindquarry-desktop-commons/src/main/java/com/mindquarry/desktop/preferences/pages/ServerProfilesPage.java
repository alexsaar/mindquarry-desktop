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
package com.mindquarry.desktop.preferences.pages;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.mindquarry.desktop.I18N;
import com.mindquarry.desktop.preferences.dialog.SVNRepoEditDialog;
import com.mindquarry.desktop.preferences.profile.Profile;
import com.mindquarry.desktop.preferences.profile.Profile.SVNRepoData;
import com.mindquarry.desktop.util.HttpUtilities;

/**
 * This class creates a preference page for Mindquarry Server profiles.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class ServerProfilesPage extends ErrorDisplayingPreferencePage {
	
	private static Log log = LogFactory.getLog(ServerProfilesPage.class);
	
    private static final Image OK_IMAGE = new Image(
            Display.getCurrent(),
            ServerProfilesPage.class
                    .getResourceAsStream("/com/mindquarry/icons/16x16/actions/save.png")); //$NON-NLS-1$
    
    public static final String NAME = "profiles";
    public static final String TITLE = I18N.get("Server Profiles");

    private Text login;
    private Text pwd;
    private Text folder;
    private Text url;

    private Button delButton;
    private List profileList;
    private java.util.List<Profile> profiles;

	private Button[] typeRadios;

	private Composite mqServerSettings;

	private Composite plainSVNSettings;

	private StackLayout settingsStackLayout;

	private Composite settingsStack;

	private List svnRepoList;

    /**
     * ProfilesPage default constructor
     */
    public ServerProfilesPage() {
        super(TITLE);
        profiles = new ArrayList<Profile>();

        // initialize preference page
        setDescription(I18N
                .get("Manage different Mindquarry installations by using Mindquarry server profiles.")); //$NON-NLS-1$
        Image img = new Image(
                null,
                getClass()
                        .getResourceAsStream(
                                "/org/tango-project/tango-icon-theme/16x16/places/network-server.png")); //$NON-NLS-1$
        ImageDescriptor imgDesc = ImageDescriptor.createFromImage(img);
        setImageDescriptor(imgDesc);
        noDefaultAndApplyButton();
    }

    /**
     * Creates the controls for this page
     */
    @Override
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, true));

        createProfileManagementGroup(composite);
        createProfileSettingsGroup(composite);

        // load list of stored profiles
        PreferenceStore store = (PreferenceStore) getPreferenceStore();
        profiles = Profile.loadProfiles(store);
        for (Profile profile : profiles) {
            profileList.add(profile.getName());
        }
        if (profileList.getItemCount() > 0) {
            Profile selectedProfile = Profile
                    .getSelectedProfile((PreferenceStore) getPreferenceStore());
            if (selectedProfile != null) { // might be null on very first start
                String selectedProfileName = selectedProfile.getName();
                for (String item : profileList.getItems()) {
                    if (item.equals(selectedProfileName)) {
                        profileList.select(profileList
                                .indexOf(selectedProfileName));
                    }
                }
            } else {
                profileList.select(0);
            }
            activateProfileSelection();
        }
        performValidation();
        return composite;
    }

    private void performValidation() {
        // check if selected profile is valid
        if ((profileList.getItemCount() > 0)
                && (profileList.getSelection().length > 0)) {
            String profileName = profileList.getItem(profileList
                    .getSelectionIndex());

            // validate currently selected profile
            Profile profile = findByName(profileName);

            if (!checkProfileValidity(profile, true)) {
                return;
            }
        }
        // selected profile seems to be valid, check other profiles as well
        String[] selection = profileList.getSelection();
        if (selection.length > 0) {
            for (Profile profile : profiles) {
                if (!profile.getName().equals(selection[0])) {
                    if (!checkProfileValidity(profile, false)) {
                        setInvalid(I18N.get(
                                "There is a problem in profile '{0}'. Please check before you proceed.",
                                profile.getName()),
                                profileList);
                        return;
                    }
                }
            }
        }
        // everything seems to be valid
        setValid();
    }

    private boolean checkProfileValidity(Profile profile, boolean currentlyDisplayed) {
    	if (profile.getType() == Profile.Type.MindquarryServer) {
	        // check server endpoint
	        if (profile.getServerURL() == null) {
	            if (!currentlyDisplayed) return false;
	            setInvalid(I18N.get("Server URL must be set."), url);
	            return false;
	        } else {
	            try {
	                new URL(profile.getServerURL());
	            } catch (MalformedURLException e) {
	                if (!currentlyDisplayed) return false;
	                setInvalid(I18N.get("Server URL is not a valid URL ({0})", e.getLocalizedMessage()), url);
	                return false;
	            }
	        }
	        // check login ID
	        if (profile.getLogin() == null) {
	            if (!currentlyDisplayed) return false;
	            setInvalid(I18N.get("Login ID must be set."), login);
	            return false;
	        } else if (profile.getLogin().equals("")) {
	            if (!currentlyDisplayed) return false;
	            setInvalid(I18N.get("Login ID must not be empty."), login);
	            return false;
	        }
	        // check password
	        if (profile.getPassword() == null) {
	            if (!currentlyDisplayed) return false;
	            setInvalid(I18N.get("Password must be set."), pwd);
	            return false;
	        } else if (profile.getPassword().equals("")) {
	            if (!currentlyDisplayed) return false;
	            setInvalid(I18N.get("Password can not be empty."), pwd);
	            return false;
	        }
	        // check workspace folder
	        if (profile.getWorkspaceFolder() == null) {
	            if (!currentlyDisplayed) return false;
	            setInvalid(I18N.get("Workspace folder must be set."), folder);
	            return false;
	        } else {
	            File file = new File(profile.getWorkspaceFolder());
	            if (!file.exists()) {
	                if (!currentlyDisplayed) return false;
	                setInvalid(I18N.get("Workspace folder does not exist."), folder);
	                return false;
	            } else if (!file.isDirectory()) {
	                if (!currentlyDisplayed) return false;
	                setInvalid(I18N.get("Workspace folder is a file, not a directory."), folder);
	                return false;
	            }
	        }
    	}
        return true;
    }

    private void createProfileManagementGroup(Composite composite) {
        Group profileGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
        profileGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        profileGroup.setText(I18N.get("Server Profiles")); //$NON-NLS-1$
        profileGroup.setLayout(new GridLayout(2, false));

        // create profile list
        Composite errorComp = createErrorBorderComposite(profileGroup, 3);
        profileList = new List(errorComp, SWT.SINGLE | SWT.BORDER
                | SWT.V_SCROLL);
        profileList.setLayoutData(new GridData(GridData.FILL_BOTH));
        registerErrorBorderComposite(errorComp, profileList);

        // create buttons for profile management
        Composite buttonArea = new Composite(profileGroup, SWT.NONE);
        buttonArea.setLayout(new GridLayout(1, true));

        Button addButton = new Button(buttonArea, SWT.PUSH);
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        addButton.setText(I18N.get("Add Profile") //$NON-NLS-1$
                + " ..."); //$NON-NLS-1$
        addButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                InputDialog dlg = new InputDialog(getShell(), I18N
                        .get("Create new server profile"), //$NON-NLS-1$
                        I18N.get("Please enter the profile name:"), //$NON-NLS-1$
                        I18N.get("My Mindquarry Server Profile"), //$NON-NLS-1$
                        new AddProfileInputValidator());

                // open dialog and check results
                if (dlg.open() == Window.OK) {
                    Profile profile = new Profile(dlg.getValue(), "", //$NON-NLS-1$
                            "", //$NON-NLS-1$
                            "", //$NON-NLS-1$
                            ""); //$NON-NLS-1$
                    profileList.add(profile.getName());
                    profileList.select(profileList.getItemCount() - 1);
                    profiles.add(profile);
                    activateProfileSelection();
                    login.setFocus();
                }
            }
        });

        Button renameButton = new Button(buttonArea, SWT.PUSH);
        renameButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        renameButton.setText(I18N.get("Rename Profile ...")); //$NON-NLS-1$
        renameButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                renameProfile();
            }
        });

        delButton = new Button(buttonArea, SWT.PUSH);
        delButton.setEnabled(false);
        delButton.setLayoutData(new GridData(GridData.FILL_BOTH));
        delButton.setText(I18N.get("Delete Profile")); //$NON-NLS-1$
        delButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                Profile profile = findByName(profileList.getSelection()[0]);
                profiles.remove(profile);
                profileList.remove(profileList.getSelectionIndices());

                delButton.setEnabled(false);
                resetFields();
            }
        });
        profileList.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent event) {
                renameProfile();
            }

            public void widgetSelected(SelectionEvent event) {
                // single click, do nothing
            }
        });
        profileList.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                activateProfileSelection();
            }
        });
    }

    private void renameProfile() {
        String[] selection = profileList.getSelection();
        if (selection.length > 0) {
            Profile profile = findByName(selection[0]);
            InputDialog dlg = new InputDialog(getShell(), I18N
                    .get("Rename server profile"), //$NON-NLS-1$
                    I18N.get("Please enter the new profile name:"), //$NON-NLS-1$
                    selection[0], new AddProfileInputValidator());
            if (dlg.open() == Window.OK) {
                profile.setName(dlg.getValue());
                profileList.setItem(profileList.getSelectionIndex(), dlg
                        .getValue());
            }
        }
    }

    private void activateProfileSelection() {
        String[] selection = profileList.getSelection();
        if (selection.length > 0) {
            Profile profile = findByName(selection[0]);
            login.setText(profile.getLogin());
            pwd.setText(profile.getPassword());
            url.setText(profile.getServerURL());
            folder.setText(profile.getWorkspaceFolder());
            if (profile.getType() == Profile.Type.MindquarryServer) {
            	typeRadios[0].setSelection(true);
            	typeRadios[1].setSelection(false);
		        settingsStackLayout.topControl = mqServerSettings;
		        settingsStack.layout();
            } else {
            	typeRadios[0].setSelection(false);
            	typeRadios[1].setSelection(true);            	
		        settingsStackLayout.topControl = plainSVNSettings;
		        settingsStack.layout();
            }
            svnRepoList.removeAll();
            for (Profile.SVNRepoData svnRepo : profile.getSvnRepos()) {
            	svnRepoList.add(svnRepo.id + " - " + svnRepo.svnURL);
            }

            delButton.setEnabled(true);
        }
    }

    private void createProfileSettingsGroup(Composite composite) {
        Group settingsGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
        settingsGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        settingsGroup.setLayout(new GridLayout(1, true));
        settingsGroup.setText(I18N.get("Profile Settings")); //$NON-NLS-1$
        
        typeRadios = new Button[2];

        typeRadios[0] = new Button(settingsGroup, SWT.RADIO);
        typeRadios[0].setSelection(true);
        typeRadios[0].setText(I18N.get("Mindquarry Server"));
        typeRadios[0].setBounds(10, 5, 75, 30);
        
        typeRadios[0].addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
		        settingsStackLayout.topControl = mqServerSettings;
		        settingsStack.layout();
		        
                String[] selection = profileList.getSelection();
                if (selection.length > 0) {
                    Profile profile = findByName(selection[0]);
                    profile.setType(Profile.Type.MindquarryServer);
                }
                performValidation();
			}
        	
        });


        typeRadios[1] = new Button(settingsGroup, SWT.RADIO);
        typeRadios[1].setText(I18N.get("Plain SVN"));
        typeRadios[1].setBounds(10, 30, 75, 30);
        
        typeRadios[1].addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
		        settingsStackLayout.topControl = plainSVNSettings;
		        settingsStack.layout();
		        
                String[] selection = profileList.getSelection();
                if (selection.length > 0) {
                    Profile profile = findByName(selection[0]);
                    profile.setType(Profile.Type.SVN);
                }
                performValidation();
			}
        	
        });
        
        settingsStack = new Composite(settingsGroup, SWT.NORMAL);
        settingsStackLayout = new StackLayout();
        settingsStack.setLayout(settingsStackLayout);
        
        createMindquarryServerSettings(settingsStack);
        createPlainSVNSettings(settingsStack);
        settingsStackLayout.topControl = mqServerSettings;
    }
    
    private void createPlainSVNSettings(Composite parent) {
        plainSVNSettings = new Composite(parent, SWT.NORMAL);
        plainSVNSettings.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        plainSVNSettings.setLayout(new GridLayout(2, false));

        svnRepoList = new List(plainSVNSettings, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
        svnRepoList.setLayoutData(new GridData(GridData.FILL_BOTH));
        svnRepoList.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent event) {
            	// double click
                editSVN();
            	log.debug("svnrepolist default selected: " + event.item);
            }

            public void widgetSelected(SelectionEvent event) {
                // single click, do nothing
            	log.debug("svnrepolist selected: " + event.item);
            }
        });

        Composite buttonArea = new Composite(plainSVNSettings, SWT.NONE);
        buttonArea.setLayout(new GridLayout(1, true));

        Button addSVNButton = new Button(buttonArea, SWT.PUSH);
        addSVNButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        addSVNButton.setText(I18N.get("Add SVN Checkout ..."));
        addSVNButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
            	Profile.SVNRepoData newSVN = new Profile.SVNRepoData("New SVN");
            	
                String[] selection = profileList.getSelection();
                if (selection.length > 0) {
                    Profile profile = findByName(selection[0]);
                    profile.getSvnRepos().add(newSVN);
                }
                // Note: maintain same order/index in profile svnrepolist and in list widget
                svnRepoList.add(newSVN.id);
                // select new element immediately for use in editSVN below
                svnRepoList.select(svnRepoList.getItemCount()-1);
                
            	editSVN();
            }
        });

        Button renameSVNButton = new Button(buttonArea, SWT.PUSH);
        renameSVNButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        renameSVNButton.setText(I18N.get("Edit SVN Checkout ..."));
        renameSVNButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                editSVN();
            }
        });

        final Button delSVNButton = new Button(buttonArea, SWT.PUSH);
        delSVNButton.setLayoutData(new GridData(GridData.FILL_BOTH));
        delSVNButton.setText(I18N.get("Delete SVN Checkout"));
        delSVNButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
        		String[] selection = profileList.getSelection();
        		if (selection.length > 0) {
        		    Profile profile = findByName(selection[0]);
        		    
        	        int selected = svnRepoList.getSelectionIndex();
        	        java.util.List<SVNRepoData> repos = profile.getSvnRepos();
        	        if (selected < repos.size()) {
        	        	repos.remove(selected);
        	        	svnRepoList.remove(selected);
                        delSVNButton.setEnabled(false);
        	        }
        		}
            }
        });
    }
    
    private void editSVN() {
		String[] selection = profileList.getSelection();
		if (selection.length > 0) {
		    Profile profile = findByName(selection[0]);
		    
	        int selected = svnRepoList.getSelectionIndex();
	        java.util.List<SVNRepoData> repos = profile.getSvnRepos();
	        if (selected < repos.size()) {
	        	SVNRepoEditDialog dialog = new SVNRepoEditDialog(getShell(), repos.get(selected).clone());
	        	if (dialog.open() == Dialog.OK) {
	        		SVNRepoData data = dialog.getData();
	        		repos.set(selected, data);
	        		svnRepoList.setItem(selected, data.id + " - " + data.svnURL);
	        	}
	        }
		}
    }

    private void createMindquarryServerSettings(Composite parent) {
        mqServerSettings = new Composite(parent, SWT.NORMAL);
        mqServerSettings.setLayoutData(new GridData(GridData.FILL_BOTH));
        mqServerSettings.setLayout(new GridLayout(1, true));
        
        // initialize server URL section
        CLabel quarryEndpointLabel = new CLabel(mqServerSettings, SWT.LEFT);
        quarryEndpointLabel.setText(I18N
                .get("URL of the Mindquarry Server:")); //$NON-NLS-1$
        quarryEndpointLabel
                .setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite errorComp = createErrorBorderComposite(mqServerSettings, 1);
        url = new Text(errorComp, SWT.SINGLE | SWT.BORDER);
        registerErrorBorderComposite(errorComp, url);
        url.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        url.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                String[] selection = profileList.getSelection();
                if (selection.length > 0) {
                    Profile profile = findByName(selection[0]);
                    profile.setServerURL(url.getText());
                    performValidation();
                }
            }
        });
        url.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                url.selectAll();
            }
        });
        // initialize login section
        CLabel loginLabel = new CLabel(mqServerSettings, SWT.LEFT);
        loginLabel.setText(I18N.get("Your Login ID:")); //$NON-NLS-1$
        loginLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        errorComp = createErrorBorderComposite(mqServerSettings, 1);
        login = new Text(errorComp, SWT.SINGLE | SWT.BORDER);
        registerErrorBorderComposite(errorComp, login);
        login.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        login.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                String[] selection = profileList.getSelection();
                if (selection.length > 0) {
                    Profile profile = findByName(selection[0]);
                    profile.setLogin(login.getText());
                    performValidation();
                }
            }
        });
        login.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                login.selectAll();
            }
        });
        // initialize password section
        CLabel pwdLabel = new CLabel(mqServerSettings, SWT.LEFT);
        pwdLabel.setText(I18N.get("Your Password:")); //$NON-NLS-1$
        pwdLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        errorComp = createErrorBorderComposite(mqServerSettings, 1);
        pwd = new Text(errorComp, SWT.PASSWORD | SWT.BORDER);
        registerErrorBorderComposite(errorComp, pwd);
        pwd.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        pwd.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                String[] selection = profileList.getSelection();
                if (selection.length > 0) {
                    Profile profile = findByName(selection[0]);
                    profile.setPassword(pwd.getText());
                    performValidation();
                }
            }
        });
        pwd.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                pwd.selectAll();
            }
        });
        // init verify server button
        Composite verifyArea = new Composite(mqServerSettings, SWT.NONE);
        verifyArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout layout = new GridLayout(2, false);
        layout.marginBottom = 0;
        layout.marginTop = 0;
        layout.marginLeft = 0;
        layout.marginRight = 0;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        verifyArea.setLayout(layout);
        Button verifyServerButton = new Button(verifyArea, SWT.LEFT | SWT.PUSH);
        verifyServerButton.setText(I18N.get("Verify server settings"));
        
        final CLabel verifiedLabel = new CLabel(verifyArea, SWT.WRAP);
        verifiedLabel.setLayoutData(new GridData(300, 20));
        
        verifyServerButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                try {
                    HttpUtilities.CheckResult result = HttpUtilities
                            .checkServerExistence(login.getText(), pwd.getText(),
                                    url.getText());
                    
                    if (HttpUtilities.CheckResult.AUTH_REFUSED == result) {
                        String msg = I18N.get("Login ID or password is incorrect.");
                        setInvalid(msg, login, pwd);
                        verifiedLabel.setText(msg);
                        verifiedLabel.setImage(JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_ERROR));
                    } else if(HttpUtilities.CheckResult.NOT_AVAILABLE == result) {
                        String msg = I18N.get("Server could not be found.");
                        setInvalid(msg, url);
                        verifiedLabel.setText(msg);
                        verifiedLabel.setImage(JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_ERROR));
                    } else {
                        setValid();
                        String msg = I18N.get("Server settings are correct.");
                        setMessage(msg, INFORMATION);
                        verifiedLabel.setText(msg);
                        verifiedLabel.setImage(OK_IMAGE);
                    }
                } catch (MalformedURLException murle) {
                    String msg = I18N.get("Server URL is not a valid URL ({0})", murle.getLocalizedMessage());
                    setInvalid(msg, url);
                    verifiedLabel.setText(msg);
                    verifiedLabel.setImage(JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_ERROR));
                }
                verifiedLabel.getParent().layout();
            }
        });
        
        // initialize workspace folder section
        CLabel locationLabel = new CLabel(mqServerSettings, SWT.LEFT);
        locationLabel.setText(I18N.get("Folder for Workspaces:")); //$NON-NLS-1$
        locationLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite locationArea = new Composite(mqServerSettings, SWT.NONE);
        locationArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        layout = new GridLayout(2, false);
        layout.marginBottom = 0;
        layout.marginTop = 0;
        layout.marginLeft = 0;
        layout.marginRight = 0;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        locationArea.setLayout(layout);

        errorComp = createErrorBorderComposite(locationArea, 1);
        folder = new Text(errorComp, SWT.BORDER);
        registerErrorBorderComposite(errorComp, folder);
        folder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        folder.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                String[] selection = profileList.getSelection();
                if (selection.length > 0) {
                    Profile profile = findByName(selection[0]);
                    profile.setWorkspaceFolder(folder.getText());
                    performValidation();
                }
            }
        });
        folder.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                folder.selectAll();
            }
        });
        Button selectWSLocationButton = new Button(locationArea, SWT.PUSH);
        selectWSLocationButton.setText(I18N.get("Browse")); //$NON-NLS-1$
        selectWSLocationButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                DirectoryDialog fd = new DirectoryDialog(getShell(), SWT.OPEN);
                fd.setText(I18N.get("Select folder for workspaces.")); //$NON-NLS-1$

                String path = fd.open();
                if (path != null) {
                    folder.setText(path);
                }
            }
        });
    }

    private Profile findByName(String name) {
        Profile result = null;
        for (Profile profile : profiles) {
            if (profile.getName().equals(name)) {
                result = profile;
                break;
            }
        }
        return result;
    }

    private void resetFields() {
        login.setText(""); //$NON-NLS-1$
        pwd.setText(""); //$NON-NLS-1$
        url.setText(""); //$NON-NLS-1$
        folder.setText(""); //$NON-NLS-1$
    }

    @Override
    public boolean performOk() {
        PreferenceStore store = (PreferenceStore) getPreferenceStore();
        Profile.storeProfiles(store, profiles);

        if ((profileList != null) &&(profileList.getSelection().length > 0)) {
            String[] selection = profileList.getSelection();
            Profile.selectProfile(store, selection[0]);
        }
        return true;
    }

    /**
	 * Always valid, overwriting the standard behaviour that depends on
	 * validation errors. But we have errors (like non-selected profile is
	 * missing a url) that should not keep the user from storing the data. There
	 * will be an error upon profile usage (eg. server connection) anyway.
	 */
	@Override
	public boolean isValid() {
		return true;
	}

	class AddProfileInputValidator implements IInputValidator {
        public String isValid(String text) {
            // check if a name was provided for the profile
            if (text.trim().length() < 1) {
                return I18N
                        .get("Profile name must contain at least one character."); //$NON-NLS-1$
            }
            // check if the name does already exist
            for (String profile : profileList.getItems()) {
                if (text.equals(profile)) {
                    return I18N
                            .get("A profile with the same name already exists. Each profile must have a unique name."); //$NON-NLS-1$
                }
            }
            return null;
        }
    }
}
