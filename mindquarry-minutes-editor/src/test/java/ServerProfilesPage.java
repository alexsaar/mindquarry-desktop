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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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

/**
 * This class creates a preference page for Mindquarry Server profiles.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class ServerProfilesPage extends PreferencePage {
    public static final String PROFILE_KEY_BASE = "com.mindquarry.server.profile."; //$NON-NLS-1$

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
        super("Server Profiles");
        profiles = new ArrayList<Profile>();

        // inital preference page
        setDescription("Manage different Mindquarry installations by using Mindquarry Server profiles.");
        Image img = new Image(null, getClass().getResourceAsStream(
                "/com/mindquarry/icons/16x16/logo/mindquarry-icon.png")); //$NON-NLS-1$
        ImageDescriptor imgDesc = ImageDescriptor.createFromImage(img);
        setImageDescriptor(imgDesc);
    }

    /**
     * Creates the controls for this page
     */
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, true));

        createProfileManagementGroup(composite);
        createProfileSettingsGroup(composite);
        loadStoredProfiles();
        return composite;
    }

    private void createProfileManagementGroup(Composite composite) {
        Group profileGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
        profileGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        profileGroup.setText("Server Profiles");
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
        addButton.setText("Add Profile...");
        addButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                InputDialog dlg = new InputDialog(getShell(),
                        "Create new server profile",
                        "Please enter your profile name",
                        "My Mindquarry Server Profile",
                        new AddProfileInputValidator());

                // open dialog and check results
                if (dlg.open() == Window.OK) {
                    Profile profile = new Profile(dlg.getValue(), "", //$NON-NLS-1$
                            "", //$NON-NLS-1$
                            "", //$NON-NLS-1$
                            ""); //$NON-NLS-1$
                    profileList.add(profile.getName());
                    profiles.add(profile);
                    resetFields();
                }
            }
        });
        delButton = new Button(buttonArea, SWT.PUSH);
        delButton.setLayoutData(new GridData(GridData.FILL_BOTH));
        delButton.setText("Delete Profile");
        delButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                Profile profile = findByName(profileList.getSelection()[0]);
                profiles.remove(profile);
                profileList.remove(profileList.getSelectionIndices());

                delButton.setEnabled(false);
                resetFields();
            }
        });
        profileList.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                delButton.setEnabled(true);

                Profile profile = findByName(profileList.getSelection()[0]);
                login.setText(profile.getLogin());
                pwd.setText(profile.getPassword());
                url.setText(profile.getServerURL());
                folder.setText(profile.getWorkspaceFolder());
            }
        });
    }

    private void createProfileSettingsGroup(Composite composite) {
        Group settingsGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
        settingsGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        settingsGroup.setText("Profile Settings");
        settingsGroup.setLayout(new GridLayout(1, true));

        // init login section
        CLabel loginLabel = new CLabel(settingsGroup, SWT.LEFT);
        loginLabel.setText("Your Login ID:");
        loginLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        login = new Text(settingsGroup, SWT.SINGLE | SWT.BORDER);
        login.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        login.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                String[] selection = profileList.getSelection();
                if (selection.length > 0) {
                    Profile profile = findByName(selection[0]);
                    profile.setLogin(login.getText());
                }
            }
        });
        // init password section
        CLabel pwdLabel = new CLabel(settingsGroup, SWT.LEFT);
        pwdLabel.setText("Your Password:");
        pwdLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        pwd = new Text(settingsGroup, SWT.PASSWORD | SWT.BORDER);
        pwd.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        pwd.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                String[] selection = profileList.getSelection();
                if (selection.length > 0) {
                    Profile profile = findByName(selection[0]);
                    profile.setPassword(pwd.getText());
                }
            }
        });
        // init server URL section
        CLabel quarryEndpointLabel = new CLabel(settingsGroup, SWT.LEFT);
        quarryEndpointLabel.setText("URL of the Mindquarry Server:");
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
                }
            }
        });
        // init workspace folder section
        CLabel locationLabel = new CLabel(settingsGroup, SWT.LEFT);
        locationLabel.setText("Folder for Workspaces:");
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
                }
            }
        });
        Button selectWSLocationButton = new Button(locationArea, SWT.PUSH);
        selectWSLocationButton.setText("Browse...");
        selectWSLocationButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                DirectoryDialog fd = new DirectoryDialog(getShell(), SWT.OPEN);
                fd.setText("Select folder for workspaces.");

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

    private void loadStoredProfiles() {
        PreferenceStore store = (PreferenceStore) getPreferenceStore();
        HashMap<Integer, Profile> storedProfiles = new HashMap<Integer, Profile>();

        // load stored profiles
        String[] prefs = store.preferenceNames();
        for (String pref : prefs) {
            if (pref.startsWith(PROFILE_KEY_BASE)) {
                // analyze preference
                int nbr = Integer.valueOf(pref.substring(PROFILE_KEY_BASE
                        .length(), PROFILE_KEY_BASE.length() + 1));
                String prefName = pref.substring(PROFILE_KEY_BASE.length() + 2,
                        pref.length());

                // init profile
                Profile profile;
                if (storedProfiles.containsKey(nbr)) {
                    profile = storedProfiles.get(nbr);
                } else {
                    profile = new Profile("",  //$NON-NLS-1$
                            "",  //$NON-NLS-1$
                            "",  //$NON-NLS-1$
                            "",  //$NON-NLS-1$
                            ""); //$NON-NLS-1$
                    storedProfiles.put(nbr, profile);
                }
                // set profile values
                if (prefName.equals("name")) { //$NON-NLS-1$
                    profile.setName(store.getString(pref));
                    System.out.println(store.getString(pref));
                } else if (prefName.equals("login")) { //$NON-NLS-1$
                    profile.setLogin(store.getString(pref));
                } else if (prefName.equals("password")) { //$NON-NLS-1$
                    profile.setPassword(store.getString(pref));
                } else if (prefName.equals("url")) { //$NON-NLS-1$
                    profile.setServerURL(store.getString(pref));
                } else if (prefName.equals("workspaces")) { //$NON-NLS-1$
                    profile.setWorkspaceFolder(store.getString(pref));
                }
            }
        }
        // set profile list
        Iterator<Integer> keyIter = storedProfiles.keySet().iterator();
        while (keyIter.hasNext()) {
            Profile profile = storedProfiles.get(keyIter.next());
            profileList.add(profile.getName());
            profiles.add(profile);
        }
    }

    @Override
    public boolean performOk() {
        IPreferenceStore store = getPreferenceStore();

        // set properties from profiles
        int pos = 0;
        for (Profile profile : profiles) {
            store.putValue(PROFILE_KEY_BASE + pos + ".name", profile.getName()); //$NON-NLS-1$
            store.putValue(PROFILE_KEY_BASE + pos + ".login", //$NON-NLS-1$
                    profile.getLogin());
            store.putValue(PROFILE_KEY_BASE + pos + ".password", //$NON-NLS-1$
                    profile.getPassword());
            store.putValue(PROFILE_KEY_BASE + pos + ".url", //$NON-NLS-1$
                    profile.getServerURL());
            store.putValue(PROFILE_KEY_BASE + pos + ".workspaces", //$NON-NLS-1$
                    profile.getWorkspaceFolder());
            pos++;
        }
        return true;
    }

    class AddProfileInputValidator implements IInputValidator {
        public String isValid(String text) {
            // check if a name was provided for the profile
            if (text.length() < 1) {
                return "Profile name must contain at least one character.";
            }
            // check if the name does already exist
            for (String profile : profileList.getItems()) {
                if (text.equals(profile)) {
                    return "A profile with the same name already exists. Each profile must have a unique name.";
                }
            }
            return null;
        }
    }

    class Profile {
        private static final long serialVersionUID = -3145142861005182807L;

        private String name;

        private String login;

        private String password;

        private String serverURL;

        private String workspaceFolder;

        /**
         * Default constructor
         */
        public Profile() {
        }

        /**
         * Copy constructor.
         */
        public Profile(Profile old) {
            name = new String(old.getName());
            login = new String(old.getLogin());
            password = new String(old.getPassword());
            serverURL = new String(old.getServerURL());
            workspaceFolder = new String(old.getWorkspaceFolder());
        }

        /**
         * Constructor that initializes all fields.
         */
        public Profile(String name, String login, String password,
                String serverURL, String workspaceFolder) {
            super();
            this.name = name;
            this.login = login;
            this.password = password;
            this.serverURL = serverURL;
            this.workspaceFolder = workspaceFolder;
        }

        /**
         * Getter for name.
         * 
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * Setter for name.
         * 
         * @param name the name to set
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Getter for serverURL.
         * 
         * @return the serverURL
         */
        public String getServerURL() {
            return serverURL;
        }

        /**
         * Setter for serverURL.
         * 
         * @param url the serverURL to set
         */
        public void setServerURL(String serverURL) {
            this.serverURL = serverURL;
        }

        /**
         * Getter for workspaceFolder.
         * 
         * @return the workspaceFolder
         */
        public String getWorkspaceFolder() {
            return workspaceFolder;
        }

        /**
         * Setter for location.
         * 
         * @param folder the location to set
         */
        public void setWorkspaceFolder(String workspaceFolder) {
            this.workspaceFolder = workspaceFolder;
        }

        /**
         * Getter for login.
         * 
         * @return the login
         */
        public String getLogin() {
            return login;
        }

        /**
         * Setter for login.
         * 
         * @param login the login to set
         */
        public void setLogin(String login) {
            this.login = login;
        }

        /**
         * Getter for password.
         * 
         * @return the password
         */
        public String getPassword() {
            return password;
        }

        /**
         * Setter for password.
         * 
         * @param password the password to set
         */
        public void setPassword(String password) {
            this.password = password;
        }
    }
}
