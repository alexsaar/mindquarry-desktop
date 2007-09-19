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

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.mindquarry.desktop.Messages;
import com.mindquarry.desktop.preferences.profile.Profile;
import com.mindquarry.desktop.util.HttpUtilities;

/**
 * This class creates a preference page for Mindquarry Server profiles.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class ServerProfilesPage extends PreferencePage {
    public static final String NAME = "profiles"; //$NON-NLS-1$

    private Text login;

    private Text pwd;

    private Text folder;

    private Text url;

    private Button delButton;

    private List profileList;

    private java.util.List<Profile> profiles;

    /**
     * ProfilesPage default constructor
     */
    public ServerProfilesPage() {
        super(Messages.getString("Server Profiles")); //$NON-NLS-1$
        profiles = new ArrayList<Profile>();

        // initialize preference page
        setDescription(Messages
                .getString("Manage different Mindquarry installations by using Mindquarry server profiles.")); //$NON-NLS-1$
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

    private boolean performValidation() {
        // check if selected profile is valid
        if ((profileList.getItemCount() > 0)
                && (profileList.getSelection().length > 0)) {
            String profileName = profileList.getItem(profileList
                    .getSelectionIndex());

            // validate currently selected profile
            Profile profile = findByName(profileName);

            String message = checkProfileValidity(profile);
            if (message != null) {
                setInvalid(message);
                return false;
            }
        }
        // selected profile seems to be valid, check other profiles as well
        String[] selection = profileList.getSelection();
        if (selection.length > 0) {
            for (Profile profile : profiles) {
                if (!profile.getName().equals(selection[0])) {
                    String message = checkProfileValidity(profile);
                    if (message != null) {
                        setInvalid("There is a problem in profile '"
                                + profile.getName()
                                + "'. "
                                + "Please check these problems before you proceed.");
                        return false;
                    }
                }
            }
        }
        // everything seems to be valid
        setValid();
        return true;
    }

    private String checkProfileValidity(Profile profile) {
        // check login ID
        if (profile.getLogin() == null) {
            return "Login ID must be set.";
        } else if (profile.getLogin().equals("")) {
            return "Login ID must not be empty.";
        }
        // check password
        if (profile.getPassword() == null) {
            return "Password must be set.";
        } else if (profile.getPassword().equals("")) {
            return "Password can not be empty.";
        }
        // check server endpoint
        if (profile.getServerURL() == null) {
            return "Server URL must be set.";
        } else {
            try {
                new URL(profile.getServerURL());
            } catch (MalformedURLException e) {
                return "Server URL is not a valid URL.";
            }
        }
        // check workspace folder
        if (profile.getWorkspaceFolder() == null) {
            return "Workspace folder must be set.";
        } else {
            File file = new File(profile.getWorkspaceFolder());
            if (!file.exists()) {
                return "Workspace folder does not exist.";
            }
        }
        return null;
    }

    private void setInvalid(String message) {
        setErrorMessage(message);
        setValid(false);
    }

    private void setValid() {
        setErrorMessage(null);
        setMessage(null, INFORMATION);
        setValid(true);
    }

    private void createProfileManagementGroup(Composite composite) {
        Group profileGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
        profileGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        profileGroup.setText(Messages.getString("Server Profiles")); //$NON-NLS-1$
        profileGroup.setLayout(new GridLayout(2, false));

        // create profile list
        profileList = new List(profileGroup, SWT.SINGLE | SWT.BORDER
                | SWT.V_SCROLL);
        profileList.setLayoutData(new GridData(GridData.FILL_BOTH));

        // create buttons for profile management
        Composite buttonArea = new Composite(profileGroup, SWT.NONE);
        buttonArea.setLayout(new GridLayout(1, true));

        Button addButton = new Button(buttonArea, SWT.PUSH);
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        addButton.setText(Messages.getString("Add Profile") //$NON-NLS-1$
                + " ..."); //$NON-NLS-1$
        addButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                InputDialog dlg = new InputDialog(getShell(), Messages
                        .getString("Create new server profile"), //$NON-NLS-1$
                        Messages.getString("Please enter the profile name:"), //$NON-NLS-1$
                        Messages.getString("My Mindquarry Server Profile"), //$NON-NLS-1$
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
        renameButton.setText(Messages.getString("Rename Profile") //$NON-NLS-1$
                + " ..."); //$NON-NLS-1$
        renameButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                renameProfile();
            }
        });

        delButton = new Button(buttonArea, SWT.PUSH);
        delButton.setEnabled(false);
        delButton.setLayoutData(new GridData(GridData.FILL_BOTH));
        delButton.setText(Messages.getString("Delete Profile")); //$NON-NLS-1$
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
            InputDialog dlg = new InputDialog(getShell(), Messages
                    .getString("Rename server profile"), //$NON-NLS-1$
                    Messages.getString("Please enter the new profile name:"), //$NON-NLS-1$
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

            delButton.setEnabled(true);
        }
    }

    private void createProfileSettingsGroup(Composite composite) {
        Group settingsGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
        settingsGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        settingsGroup.setText(Messages.getString("Profile Settings")); //$NON-NLS-1$
        settingsGroup.setLayout(new GridLayout(1, true));

        // init login section
        CLabel loginLabel = new CLabel(settingsGroup, SWT.LEFT);
        loginLabel.setText(Messages.getString("Your Login ID") //$NON-NLS-1$
                + ":"); //$NON-NLS-1$
        loginLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        login = new Text(settingsGroup, SWT.SINGLE | SWT.BORDER);
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
        login.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                login.selectAll();
            }

            public void focusLost(FocusEvent e) {
                // nothing to do here
            }
        });
        // init password section
        CLabel pwdLabel = new CLabel(settingsGroup, SWT.LEFT);
        pwdLabel.setText(Messages.getString("Your Password") //$NON-NLS-1$
                + ":"); //$NON-NLS-1$
        pwdLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        pwd = new Text(settingsGroup, SWT.PASSWORD | SWT.BORDER);
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
        pwd.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                pwd.selectAll();
            }

            public void focusLost(FocusEvent e) {
                // nothing to do here
            }
        });
        // init server URL section
        CLabel quarryEndpointLabel = new CLabel(settingsGroup, SWT.LEFT);
        quarryEndpointLabel.setText(Messages
                .getString("URL of the Mindquarry Server") //$NON-NLS-1$
                + ":"); //$NON-NLS-1$
        quarryEndpointLabel
                .setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        url = new Text(settingsGroup, SWT.SINGLE | SWT.BORDER);
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
        url.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                url.selectAll();
            }

            public void focusLost(FocusEvent e) {
                // nothing to do here
            }
        });
        // init workspace folder section
        CLabel locationLabel = new CLabel(settingsGroup, SWT.LEFT);
        locationLabel.setText(Messages.getString("Folder for Workspaces") //$NON-NLS-1$
                + ":"); //$NON-NLS-1$
        locationLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite locationArea = new Composite(settingsGroup, SWT.NONE);
        locationArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        locationArea.setLayout(new GridLayout(2, false));
        ((GridLayout) locationArea.getLayout()).marginBottom = 0;
        ((GridLayout) locationArea.getLayout()).marginTop = 0;
        ((GridLayout) locationArea.getLayout()).marginLeft = 0;
        ((GridLayout) locationArea.getLayout()).marginRight = 0;
        ((GridLayout) locationArea.getLayout()).marginHeight = 0;
        ((GridLayout) locationArea.getLayout()).marginWidth = 0;

        folder = new Text(locationArea, SWT.BORDER);
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
        folder.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                folder.selectAll();
            }

            public void focusLost(FocusEvent e) {
                // nothing to do here
            }
        });
        Button selectWSLocationButton = new Button(locationArea, SWT.PUSH);
        selectWSLocationButton.setText(Messages.getString("Browse")); //$NON-NLS-1$
        selectWSLocationButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                DirectoryDialog fd = new DirectoryDialog(getShell(), SWT.OPEN);
                fd.setText(Messages.getString("Select folder for workspaces.")); //$NON-NLS-1$

                String path = fd.open();
                if (path != null) {
                    folder.setText(path);
                }
            }
        });

        // init verify server button
        Button verifyServerButton = new Button(settingsGroup, SWT.LEFT
                | SWT.PUSH);
        verifyServerButton
                .setText(Messages.getString("Verify server settings"));
        verifyServerButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                try {
                    HttpUtilities.CheckResult result = HttpUtilities
                            .checkServerExistence(login.getText(), pwd.getText(),
                                    url.getText());
                    
                    if(HttpUtilities.CheckResult.AUTH_REFUSED == result) {
                        setErrorMessage("Your login ID or password is incorrect.");
                    } else if(HttpUtilities.CheckResult.NOT_AVAILABLE == result) {
                        setErrorMessage("Server could not be found.");
                    } else {
                        setMessage("Your server settings are correct.", INFORMATION);
                    }
                } catch(MalformedURLException murle) {
                    setErrorMessage("Server URL is not a valid URL.");
                }
            }
        });
    }

    //    
    // private void verifyServerSettings() {
    // HttpClient client = HttpUtilities.createHttpClient(login, pwd, url);
    // GetMethod get = createAndExecuteGetMethod(url, client);
    //
    // String result = null;
    // if (get.getStatusCode() == 200) {
    // result = get.getResponseBodyAsString();
    // } else if (get.getStatusCode() == 401) {
    // }
    // }

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

        String[] selection = profileList.getSelection();
        if (selection.length > 0) {
            Profile.selectProfile(store, selection[0]);
        }
        return true;
    }

    class AddProfileInputValidator implements IInputValidator {
        public String isValid(String text) {
            // check if a name was provided for the profile
            if (text.trim().length() < 1) {
                return Messages
                        .getString("Profile name must contain at least one character."); //$NON-NLS-1$
            }
            // check if the name does already exist
            for (String profile : profileList.getItems()) {
                if (text.equals(profile)) {
                    return Messages
                            .getString("A profile with the same name already exists. Each profile must have a unique name."); //$NON-NLS-1$
                }
            }
            return null;
        }
    }
}
