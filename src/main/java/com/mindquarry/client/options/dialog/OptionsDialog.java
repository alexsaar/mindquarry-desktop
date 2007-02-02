/*
 * Copyright (C) 2006-2007 MindQuarry GmbH, All Rights Reserved
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
package com.mindquarry.client.options.dialog;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import com.mindquarry.client.options.Profile;
import com.mindquarry.client.options.ProfileList;

/**
 * Dialog widget for editing MindClient options.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class OptionsDialog extends TitleAreaDialog {
    private Text login = null;

    private Text pwd = null;

    private Text location = null;

    private Text endpoint = null;

    private Button delProfileButton;

    private FieldValidator validator = null;

    private final Image icon;

    private ProfileList profiles;

    private Profile toBeStored;

    private List pList;

    /**
     * Default constructor.
     * 
     * @param shell the shell
     */
    public OptionsDialog(Shell shell, Image icon, ProfileList profiles) {
        super(shell);
        setBlockOnOpen(true);

        this.icon = icon;
        this.profiles = profiles;
    }

    /**
     * Creates the dialog's contents
     * 
     * @param parent the parent composite
     * @return Control
     */
    @Override
    protected Control createContents(Composite parent) {
        Control contents = super.createContents(parent);

        setTitle(Messages.getString("OptionsDialog.0")); //$NON-NLS-1$
        setMessage(Messages.getString("OptionsDialog.1"), //$NON-NLS-1$
                IMessageProvider.INFORMATION);

        getShell().setText(Messages.getString("OptionsDialog.0")); //$NON-NLS-1$
        getShell().setImage(icon);

        return contents;
    }

    /**
     * Creates the main dialog area
     * 
     * @param parent the parent composite
     * @return Control
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        validator = new FieldValidator();

        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(1, true));

        TabFolder tabFolder = new TabFolder(composite, SWT.NONE);
        tabFolder.setLayout(new GridLayout(1, true));
        tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

        // create profile management tab
        composite = new Composite(tabFolder, SWT.NONE);
        composite.setLayout(new GridLayout(1, true));

        TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
        tabItem.setText(Messages.getString("OptionsDialog.10")); //$NON-NLS-1$
        tabItem.setControl(composite);

        createProfileManagementGroup(composite);
        createProfileSettingsGroup(composite);
        return composite;
    }

    private void createProfileManagementGroup(Composite composite) {
        // create widgets for profile selection
        Group profileGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
        profileGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        profileGroup.setText(Messages.getString("OptionsDialog.11")); //$NON-NLS-1$
        profileGroup.setLayout(new GridLayout(2, false));

        // create profile list
        pList = new List(profileGroup, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
        pList.setLayoutData(new GridData(GridData.FILL_BOTH));
        updateProfileList();

        // create buttons for profile management
        Composite buttonArea = new Composite(profileGroup, SWT.NONE);
        buttonArea.setLayout(new GridLayout(1, true));

        Button addProfileButton = new Button(buttonArea, SWT.PUSH);
        addProfileButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        addProfileButton.setText(Messages.getString("OptionsDialog.12")); //$NON-NLS-1$
        addProfileButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                InputDialog dlg = new InputDialog(getShell(), Messages
                        .getString("OptionsDialog.13"), //$NON-NLS-1$
                        Messages.getString("OptionsDialog.14"), //$NON-NLS-1$
                        Messages.getString("OptionsDialog.15"), //$NON-NLS-1$
                        new IInputValidator() {
                            /**
                             * @see org.eclipse.jface.dialogs.IInputValidator#isValid(java.lang.String)
                             */
                            public String isValid(String text) {
                                // check if a name was provided for the profile
                                if (text.length() < 1) {
                                    return Messages
                                            .getString("OptionsDialog.16"); //$NON-NLS-1$
                                }
                                // check if the name does already exist
                                for (Profile profile : profiles.getProfiles()) {
                                    if (profile.getName().equals(text)) {
                                        return Messages
                                                .getString("OptionsDialog.17"); //$NON-NLS-1$
                                    }
                                }
                                return null;
                            }
                        });
                if (dlg.open() == Window.OK) {
                    storeValues();
                    
                    Profile profile = new Profile();
                    profile.setName(dlg.getValue());
                    profiles.addProfile(profile);
                    toBeStored = profile;

                    pList.add(profile.getName());
                    pList.setSelection(new String[] { profile.getName() });
                    resetFields();
                }
            }
        });
        delProfileButton = new Button(buttonArea, SWT.PUSH);
        delProfileButton.setLayoutData(new GridData(GridData.FILL_BOTH));
        delProfileButton.setText(Messages.getString("OptionsDialog.18")); //$NON-NLS-1$
        delProfileButton.addListener(SWT.Selection, new Listener() {
            /**
             * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
             */
            public void handleEvent(Event event) {
                int[] selection = pList.getSelectionIndices();
                String name = pList.getItem(selection[0]);
                pList.remove(selection);
                profiles.deleteProfile(name);
                toBeStored = null;

                delProfileButton.setEnabled(false);
                resetFields();
            }
        });
        pList.addListener(SWT.Selection, new Listener() {
            /**
             * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
             */
            public void handleEvent(Event event) {
                storeValues();
                delProfileButton.setEnabled(true);

                Profile profile = profiles.getProfileByName(pList
                        .getSelection()[0]);
                toBeStored = profile;
                login.setText(profile.getLogin());
                pwd.setText(profile.getPassword());
                endpoint.setText(profile.getEndpoint());
                location.setText(profile.getLocation());
            }
        });
    }

    /**
     * Create widgets for profile settings.
     */
    private void createProfileSettingsGroup(Composite composite) {
        Group settingsGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
        settingsGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        settingsGroup.setText(Messages.getString("OptionsDialog.20")); //$NON-NLS-1$
        settingsGroup.setLayout(new GridLayout(1, true));

        CLabel loginLabel = new CLabel(settingsGroup, SWT.LEFT);
        loginLabel.setText(Messages.getString("OptionsDialog.3")); //$NON-NLS-1$
        loginLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        login = new Text(settingsGroup, SWT.SINGLE | SWT.BORDER);
        login.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        login.addModifyListener(validator);
        login.addFocusListener(new TextFocusListener(login));

        CLabel pwdLabel = new CLabel(settingsGroup, SWT.LEFT);
        pwdLabel.setText(Messages.getString("OptionsDialog.4")); //$NON-NLS-1$
        pwdLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        pwd = new Text(settingsGroup, SWT.PASSWORD | SWT.BORDER);
        pwd.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        pwd.addModifyListener(validator);
        pwd.addFocusListener(new TextFocusListener(pwd));

        CLabel quarryEndpointLabel = new CLabel(settingsGroup, SWT.LEFT);
        quarryEndpointLabel.setText(Messages.getString("OptionsDialog.5")); //$NON-NLS-1$
        quarryEndpointLabel
                .setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        endpoint = new Text(settingsGroup, SWT.SINGLE | SWT.BORDER);
        endpoint.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        endpoint.addModifyListener(validator);
        endpoint.addFocusListener(new TextFocusListener(endpoint));

        CLabel locationLabel = new CLabel(settingsGroup, SWT.LEFT);
        locationLabel.setText(Messages.getString("OptionsDialog.21")); //$NON-NLS-1$
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

        location = new Text(locationArea, SWT.BORDER);
        location.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        location.addModifyListener(validator);
        location.addFocusListener(new TextFocusListener(pwd));

        Button selectLocationButton = new Button(locationArea, SWT.PUSH);
        selectLocationButton.setText(Messages.getString("OptionsDialog.22")); //$NON-NLS-1$
        selectLocationButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                DirectoryDialog fd = new DirectoryDialog(getShell(), SWT.OPEN);
                fd.setText(Messages.getString("OptionsDialog.23")); //$NON-NLS-1$

                location.setText(fd.open());
                validator.modifyText(null);
            }
        });
    }

    private void resetFields() {
        login.setText(""); //$NON-NLS-1$
        pwd.setText(""); //$NON-NLS-1$
        endpoint.setText(""); //$NON-NLS-1$
        location.setText(""); //$NON-NLS-1$
    }

    private void updateProfileList() {
        for (Profile profile : profiles.getProfiles()) {
            pList.add(profile.getName());
        }
    }

    /**
     * Creates the buttons for the button bar
     * 
     * @param parent the parent composite
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
                true);
        getButton(IDialogConstants.OK_ID).setText("Done");

        // init dialog validator
        validator.init();
    }

    private void storeValues() {
        if (toBeStored != null) {
            toBeStored.setLogin(login.getText());
            toBeStored.setPassword(pwd.getText());
            toBeStored.setEndpoint(endpoint.getText());
            toBeStored.setLocation(location.getText());
        }
    }

    @Override
    public boolean close() {
        storeValues();
        return super.close();
    }

    /**
     * Field validator for the OptionsDialog.
     * 
     * @author <a
     *         href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
     *         Saar</a>
     */
    class FieldValidator implements ModifyListener {
        public void modifyText(ModifyEvent event) {
            // check settings of selected profile
            if (login.getText().equals("")) { //$NON-NLS-1$
                setErrorMessage(Messages.getString("OptionsDialog.6")); //$NON-NLS-1$
                return;
            }
            if (pwd.getText().equals("")) { //$NON-NLS-1$
                setErrorMessage(Messages.getString("OptionsDialog.7")); //$NON-NLS-1$
                return;
            }
            if (endpoint.getText().equals("")) { //$NON-NLS-1$
                setErrorMessage(Messages.getString("OptionsDialog.8")); //$NON-NLS-1$
                return;
            }
            try {
                new URL(endpoint.getText());
            } catch (MalformedURLException e) {
                setErrorMessage(Messages.getString("OptionsDialog.9")); //$NON-NLS-1$
                return;
            }
            if (location.getText().equals("")) { //$NON-NLS-1$
                setErrorMessage(Messages.getString("OptionsDialog.28")); //$NON-NLS-1$
                return;
            }
            File file = new File(location.getText());
            if ((!file.exists()) || (!file.isDirectory())) {
                setErrorMessage(Messages.getString("OptionsDialog.29")); //$NON-NLS-1$
                return;
            }
            // if we get this far, all input is valid
            setErrorMessage(null);
        }

        public void init() {
            if (pList.getItemCount() > 0) {
                pList.select(0);
                Profile profile = profiles.getProfileByName(pList
                        .getSelection()[0]);
                toBeStored = profile;
                login.setText(profile.getLogin());
                pwd.setText(profile.getPassword());
                endpoint.setText(profile.getEndpoint());
                location.setText(profile.getLocation());
                modifyText(null);
            }
        }
    }
}
