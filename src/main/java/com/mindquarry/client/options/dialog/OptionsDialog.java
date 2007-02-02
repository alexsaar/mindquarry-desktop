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
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
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
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class OptionsDialog extends TitleAreaDialog {
    private CLabel loginLabel = null;

    private Text loginText = null;

    private CLabel pwdLabel = null;

    private Text pwdText = null;

    private CLabel locationLabel = null;

    private Text locationText = null;

    private CLabel quarryEndpointLabel = null;

    private Text endpointText = null;

    private FieldValidator validator = null;

    private final Image icon;

    private ProfileList oldProfiles;

    private ProfileList profiles;

    private List profileList;

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
        oldProfiles = profiles;
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
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        // save the current profile upon ok click
        saveCurrentProfile();
        super.okPressed();
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
     */
    @Override
    protected void cancelPressed() {
        // reset changes on cancel
        profiles = oldProfiles;
        super.cancelPressed();
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
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
        tabItem.setText(Messages.getString("OptionsDialog.10")); //$NON-NLS-1$
        tabItem.setControl(composite);

        createProfileManagementGroup(composite);
        createProfileSettingsGroup(composite);

        // create task management tab
        // composite = new Composite(tabFolder, SWT.NONE);
        // composite.setLayout(new GridLayout(1, true));
        // composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        //
        // tabItem = new TabItem(tabFolder, SWT.NONE);
        // tabItem.setText("Task Management");
        // tabItem.setControl(composite);
        //
        // createTaskManagementGroup(composite);
        
//        delProfileButton.setEnabled(true);
//        saveProfileButton.setEnabled(true);
        
        if (profileList.getItemCount() > 0) {
            profileList.select(0);
        }

        return composite;
    }
    
    private void createProfileManagementGroup(Composite composite) {
        // create widgets for profile selection
        Group profileGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
        profileGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        profileGroup.setText(Messages.getString("OptionsDialog.11")); //$NON-NLS-1$
        profileGroup.setLayout(new GridLayout(2, false));

        // create profile list
        profileList = new List(profileGroup, SWT.SINGLE | SWT.BORDER
                | SWT.V_SCROLL);
        profileList.setLayoutData(new GridData(GridData.FILL_BOTH));
        updateProfileList();

        // create buttons for profile management
        Composite buttonArea = new Composite(profileGroup, SWT.NONE);
        buttonArea.setLayout(new GridLayout(1, true));

        Button addProfileButton = new Button(buttonArea, SWT.PUSH);
        addProfileButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        addProfileButton.setText(Messages.getString("OptionsDialog.12")); //$NON-NLS-1$
        addProfileButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                InputDialog dlg = new InputDialog(getShell(),
                        Messages.getString("OptionsDialog.13"), Messages.getString("OptionsDialog.14"), //$NON-NLS-1$ //$NON-NLS-2$
                        Messages.getString("OptionsDialog.15"), new IInputValidator() { //$NON-NLS-1$
                            /**
                             * @see org.eclipse.jface.dialogs.IInputValidator#isValid(java.lang.String)
                             */
                            public String isValid(String text) {
                                // check if a name was provided for the profile
                                if (text.length() < 1) {
                                    return Messages.getString("OptionsDialog.16"); //$NON-NLS-1$
                                }
                                // check if the name does already exist
                                for (Profile profile : profiles.getProfiles()) {
                                    if (profile.getName().equals(text)) {
                                        return Messages.getString("OptionsDialog.17"); //$NON-NLS-1$
                                    }
                                }
                                return null;
                            }
                        });
                if (dlg.open() == Window.OK) {
                    Profile profile = new Profile();
                    profile.setName(dlg.getValue());
                    profiles.addProfile(profile);

                    profileList.add(profile.getName());
                    profileList
                            .setSelection(new String[] { profile.getName() });
                    resetFields();
                }
            }
        });
        final Button delProfileButton = new Button(buttonArea, SWT.PUSH);
        delProfileButton.setLayoutData(new GridData(GridData.FILL_BOTH));
        delProfileButton.setText(Messages.getString("OptionsDialog.18")); //$NON-NLS-1$
        delProfileButton.setEnabled(false);

        final Button saveProfileButton = new Button(buttonArea, SWT.PUSH);
        saveProfileButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        saveProfileButton.setText(Messages.getString("OptionsDialog.19")); //$NON-NLS-1$
        saveProfileButton.setEnabled(false);
        saveProfileButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                saveCurrentProfile();
            }
        });
        delProfileButton.addListener(SWT.Selection, new Listener() {
            /**
             * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
             */
            public void handleEvent(Event event) {
                int[] selection = profileList.getSelectionIndices();
                String name = profileList.getItem(selection[0]);
                profileList.remove(selection);

                profiles.deleteProfile(name);

                delProfileButton.setEnabled(false);
                saveProfileButton.setEnabled(false);
                resetFields();
            }
        });
        profileList.addListener(SWT.Selection, new Listener() {
            /**
             * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
             */
            public void handleEvent(Event event) {
                delProfileButton.setEnabled(true);
                saveProfileButton.setEnabled(true);

                if (profileList.getSelection().length > 0) {
                    Profile profile = profiles.getProfileByName(profileList
                            .getSelection()[0]);
                    loginText.setText(profile.getLogin());
                    pwdText.setText(profile.getPassword());
                    endpointText.setText(profile.getEndpoint());
                    locationText.setText(profile.getLocation());
                }
            }
        });
    }

    private void createProfileSettingsGroup(Composite composite) {
        // create widgets for profile settings
        Group settingsGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
        settingsGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        settingsGroup.setText(Messages.getString("OptionsDialog.20")); //$NON-NLS-1$
        settingsGroup.setLayout(new GridLayout(1, true));

        loginLabel = new CLabel(settingsGroup, SWT.LEFT);
        loginLabel.setText(Messages.getString("OptionsDialog.3")); //$NON-NLS-1$
        loginLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        loginText = new Text(settingsGroup, SWT.SINGLE | SWT.BORDER);
        loginText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        loginText.addModifyListener(validator);
        loginText.addFocusListener(new TextFocusListener(loginText));

        pwdLabel = new CLabel(settingsGroup, SWT.LEFT);
        pwdLabel.setText(Messages.getString("OptionsDialog.4")); //$NON-NLS-1$
        pwdLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        pwdText = new Text(settingsGroup, SWT.PASSWORD | SWT.BORDER);
        pwdText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        pwdText.addModifyListener(validator);
        pwdText.addFocusListener(new TextFocusListener(pwdText));

        quarryEndpointLabel = new CLabel(settingsGroup, SWT.LEFT);
        quarryEndpointLabel.setText(Messages.getString("OptionsDialog.5")); //$NON-NLS-1$
        quarryEndpointLabel
                .setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        endpointText = new Text(settingsGroup, SWT.SINGLE | SWT.BORDER);
        endpointText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        endpointText.addModifyListener(validator);
        endpointText.addFocusListener(new TextFocusListener(endpointText));

        locationLabel = new CLabel(settingsGroup, SWT.LEFT);
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

        locationText = new Text(locationArea, SWT.BORDER);
        locationText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        locationText.addModifyListener(validator);
        locationText.addFocusListener(new TextFocusListener(pwdText));

        Button selectLocationButton = new Button(locationArea, SWT.PUSH);
        selectLocationButton.setText(Messages.getString("OptionsDialog.22")); //$NON-NLS-1$
        selectLocationButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                DirectoryDialog fd = new DirectoryDialog(getShell(), SWT.OPEN);
                fd.setText(Messages.getString("OptionsDialog.23")); //$NON-NLS-1$

                locationText.setText(fd.open());
                validator.modifyText(null);
            }
        });
    }

//    private void createTaskManagementGroup(Composite composite) {
//        // create widgets for profile selection
//        Group tasksGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
//        tasksGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
//        tasksGroup.setText(Messages.getString("OptionsDialog.24")); //$NON-NLS-1$
//        tasksGroup.setLayout(new GridLayout(2, false));
//
//        Composite area = new Composite(tasksGroup, SWT.NONE);
//        area.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//        area.setLayout(new GridLayout(2, false));
//        ((GridLayout) area.getLayout()).marginBottom = 0;
//        ((GridLayout) area.getLayout()).marginTop = 0;
//        ((GridLayout) area.getLayout()).marginLeft = 0;
//        ((GridLayout) area.getLayout()).marginRight = 0;
//        ((GridLayout) area.getLayout()).marginHeight = 0;
//        ((GridLayout) area.getLayout()).marginWidth = 0;
//
//        Button finishedTasks = new Button(area, SWT.CHECK);
//        Label finishedLabel = new Label(area, SWT.LEFT);
//        finishedLabel.setText(Messages.getString("OptionsDialog.25")); //$NON-NLS-1$
//
//        Button updateTasks = new Button(area, SWT.CHECK);
//        Label updateLabel = new Label(area, SWT.LEFT);
//        updateLabel.setText(Messages.getString("OptionsDialog.26")); //$NON-NLS-1$
//    }

    private void resetFields() {
        loginText.setText(""); //$NON-NLS-1$
        pwdText.setText(""); //$NON-NLS-1$
        endpointText.setText(""); //$NON-NLS-1$
        locationText.setText(""); //$NON-NLS-1$
    }

    /**
     * @param profileList
     */
    private void updateProfileList() {
        for (Profile profile : profiles.getProfiles()) {
            profileList.add(profile.getName());
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
        createButton(parent, IDialogConstants.CANCEL_ID,
                IDialogConstants.CANCEL_LABEL, false);

        validator.init();
    }
    
    private void saveCurrentProfile() {
        Profile profile = profiles.getProfileByName(profileList
                .getSelection()[0]);
        profile.setLogin(loginText.getText());
        profile.setPassword(pwdText.getText());
        profile.setEndpoint(endpointText.getText());
        profile.setLocation(locationText.getText());
        
        // select the profile if none is selected yet
        if (profiles.selectedProfile() == null) {
            profiles.select(profile);
        }
    }

    private class TextFocusListener implements FocusListener {
        private Text text;

        public TextFocusListener(Text text) {
            this.text = text;
        }

        /**
         * @see org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.swt.events.FocusEvent)
         */
        public void focusGained(FocusEvent e) {
            text.setSelection(0, text.getText().length());
        }

        /**
         * @see org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt.events.FocusEvent)
         */
        public void focusLost(FocusEvent e) {
            // nothing to do here
        }
    }

    /**
     * Field validator for the OptionsDialog.
     * 
     * @author <a
     *         href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
     *         Saar</a>
     */
    class FieldValidator implements ModifyListener {
        /**
         * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
         */
        public void modifyText(ModifyEvent event) {
            // check if a profile is selected
            if (profileList.getSelection().length == 0) {
                setErrorMessage(Messages.getString("OptionsDialog.27")); //$NON-NLS-1$
                getButton(IDialogConstants.OK_ID).setEnabled(false);
                return;
            }
            // otherwise check settings of selected profile
            if (loginText.getText().equals("")) { //$NON-NLS-1$
                setErrorMessage(Messages.getString("OptionsDialog.6")); //$NON-NLS-1$
                getButton(IDialogConstants.OK_ID).setEnabled(false);
                return;
            }
            
            if (pwdText.getText().equals("")) { //$NON-NLS-1$
                setErrorMessage(Messages.getString("OptionsDialog.7")); //$NON-NLS-1$
                getButton(IDialogConstants.OK_ID).setEnabled(false);
                return;
            }
            
            if (endpointText.getText().equals("")) { //$NON-NLS-1$
                setErrorMessage(Messages.getString("OptionsDialog.8")); //$NON-NLS-1$
                getButton(IDialogConstants.OK_ID).setEnabled(false);
                return;
            }
            
            try {
                new URL(endpointText.getText());
            } catch (MalformedURLException e) {
                setErrorMessage(Messages.getString("OptionsDialog.9")); //$NON-NLS-1$
                getButton(IDialogConstants.OK_ID).setEnabled(false);
                return;
            }

            if (locationText.getText().equals("")) { //$NON-NLS-1$
                setErrorMessage(Messages.getString("OptionsDialog.28")); //$NON-NLS-1$
                getButton(IDialogConstants.OK_ID).setEnabled(false);
                return;
            }
            
            File location = new File(locationText.getText());
            if ((!location.exists()) || (!location.isDirectory())) {
                setErrorMessage(Messages.getString("OptionsDialog.29")); //$NON-NLS-1$
                getButton(IDialogConstants.OK_ID).setEnabled(false);
                return;
            }
            
            // if we get this far, all input is valid
            setErrorMessage(null);
            getButton(IDialogConstants.OK_ID).setEnabled(true);
        }

        public void init() {
            modifyText(null);
        }
    }
}
