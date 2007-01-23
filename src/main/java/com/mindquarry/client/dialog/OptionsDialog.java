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
package com.mindquarry.client.dialog;

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
import org.eclipse.swt.widgets.Text;

import com.mindquarry.client.options.Profile;
import com.mindquarry.client.options.ProfileList;

/**
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class OptionsDialog extends TitleAreaDialog {
    private Image optionsImage;

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

        // Create the logo image
        this.optionsImage = new Image(null, getClass().getResourceAsStream(
                "/images/options.png")); //$NON-NLS-1$

        this.icon = icon;
        this.profiles = profiles;
        this.oldProfiles = profiles;
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

        // Set the logo
        if (this.optionsImage != null) {
            setTitleImage(this.optionsImage);
        }
        return contents;
    }

    /**
     * Closes the dialog box Override so we can dispose the image we created
     */
    @Override
    public boolean close() {
        if (this.optionsImage != null) {
            this.optionsImage.dispose();
        }
        // reset changes on cancel
        this.profiles = this.oldProfiles;
        return super.close();
    }

    /**
     * Creates the main dialog area
     * 
     * @param parent the parent composite
     * @return Control
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        this.validator = new FieldValidator();

        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(1, true));

        createProfileManagementGroup(composite);
        createProfileSettingsGroup(composite);
        return composite;
    }

    /**
     * @param composite
     */
    private void createProfileSettingsGroup(Composite composite) {
        // create widgets for profile settings
        Group settingsGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
        settingsGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        settingsGroup.setText("Profile Settings");
        settingsGroup.setLayout(new GridLayout(1, true));

        this.loginLabel = new CLabel(settingsGroup, SWT.LEFT);
        this.loginLabel.setText(Messages.getString("OptionsDialog.3")); //$NON-NLS-1$
        this.loginLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        this.loginText = new Text(settingsGroup, SWT.SINGLE | SWT.BORDER);
        this.loginText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        this.loginText.addModifyListener(this.validator);
        this.loginText.addFocusListener(new TextFocusListener(this.loginText));

        this.pwdLabel = new CLabel(settingsGroup, SWT.LEFT);
        this.pwdLabel.setText(Messages.getString("OptionsDialog.4")); //$NON-NLS-1$
        this.pwdLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        this.pwdText = new Text(settingsGroup, SWT.PASSWORD | SWT.BORDER);
        this.pwdText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        this.pwdText.addModifyListener(this.validator);
        this.pwdText.addFocusListener(new TextFocusListener(this.pwdText));

        this.quarryEndpointLabel = new CLabel(settingsGroup, SWT.LEFT);
        this.quarryEndpointLabel.setText(Messages.getString("OptionsDialog.5")); //$NON-NLS-1$
        this.quarryEndpointLabel.setLayoutData(new GridData(
                GridData.FILL_HORIZONTAL));

        this.endpointText = new Text(settingsGroup, SWT.SINGLE
                | SWT.BORDER);
        this.endpointText.setLayoutData(new GridData(
                GridData.FILL_HORIZONTAL));
        this.endpointText.addModifyListener(this.validator);
        this.endpointText.addFocusListener(new TextFocusListener(
                this.endpointText));

        this.locationLabel = new CLabel(settingsGroup, SWT.LEFT);
        this.locationLabel.setText("Workspace Location:");
        this.locationLabel
                .setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite locationArea = new Composite(settingsGroup, SWT.NONE);
        locationArea.setLayout(new GridLayout(2, false));
        ((GridLayout) locationArea.getLayout()).marginBottom = 0;
        ((GridLayout) locationArea.getLayout()).marginTop = 0;
        ((GridLayout) locationArea.getLayout()).marginLeft = 0;
        ((GridLayout) locationArea.getLayout()).marginRight = 0;
        ((GridLayout) locationArea.getLayout()).marginHeight = 0;
        ((GridLayout) locationArea.getLayout()).marginWidth = 0;

        this.locationText = new Text(locationArea, SWT.BORDER);
        this.locationText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        this.locationText.addModifyListener(this.validator);
        this.locationText.addFocusListener(new TextFocusListener(this.pwdText));

        Button selectLocationButton = new Button(locationArea, SWT.PUSH);
        selectLocationButton.setText("Browse...");
        selectLocationButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                DirectoryDialog fd = new DirectoryDialog(getShell(), SWT.OPEN);
                fd.setText("Select location for workspaces");

                locationText.setText(fd.open());
            }
        });
    }

    /**
     * @param composite
     */
    private void createProfileManagementGroup(Composite composite) {
        // create widgets for profile selection
        Group profileGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
        profileGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        profileGroup.setText("Profiles");
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
        addProfileButton.setText("Add Profile...");
        addProfileButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                InputDialog dlg = new InputDialog(getShell(),
                        "Create new profile", "Enter your profile name",
                        "My Quarry Profile", new IInputValidator() {
                            /**
                             * @see org.eclipse.jface.dialogs.IInputValidator#isValid(java.lang.String)
                             */
                            public String isValid(String text) {
                                if (text.length() < 1) {
                                    return "Profile name must contain at least one character.";
                                }
                                return null;
                            }
                        });
                if (dlg.open() == Window.OK) {
                    Profile profile = new Profile();
                    profile.setName(dlg.getValue());

                    profileList.add(profile.getName());
                    profileList
                            .setSelection(new String[] { profile.getName() });
                    resetFields();
                }
            }
        });
        final Button delProfileButton = new Button(buttonArea, SWT.PUSH);
        delProfileButton.setLayoutData(new GridData(GridData.FILL_BOTH));
        delProfileButton.setText("Remove Profile");
        delProfileButton.setEnabled(false);
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
                resetFields();
            }
        });
        profileList.addListener(SWT.Selection, new Listener() {
            /**
             * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
             */
            public void handleEvent(Event event) {
                delProfileButton.setEnabled(true);
                Profile profile = profiles.getProfileByName(profileList
                        .getSelection()[0]);
                loginText.setText(profile.getLogin());
                pwdText.setText(profile.getPassword());
                endpointText.setText(profile.getEndpoint());
                locationText.setText(profile.getLocation());
            }
        });
    }
    
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
        
        this.validator.init();
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
            this.text.setSelection(0, this.text.getText().length());
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
            if(profileList.getSelection().length == 0) {
                setErrorMessage("Please select a profile!");
                getButton(IDialogConstants.OK_ID).setEnabled(false);
                return;
            } else {
                updateProfile();
            }
            // otherwise check settings of selected profile
            if (loginText.getText().equals("")) { //$NON-NLS-1$
                setErrorMessage(Messages.getString("OptionsDialog.6")); //$NON-NLS-1$
                getButton(IDialogConstants.OK_ID).setEnabled(false);
                return;
            } else if (pwdText.getText().equals("")) { //$NON-NLS-1$
                setErrorMessage(Messages.getString("OptionsDialog.7")); //$NON-NLS-1$
                getButton(IDialogConstants.OK_ID).setEnabled(false);
                return;
            } else if (endpointText.getText().equals("")) { //$NON-NLS-1$
                setErrorMessage(Messages.getString("OptionsDialog.8")); //$NON-NLS-1$
                getButton(IDialogConstants.OK_ID).setEnabled(false);
                return;
            } else if (locationText.getText().equals("")) { //$NON-NLS-1$
                setErrorMessage("Workspace location must not be empty.");
                getButton(IDialogConstants.OK_ID).setEnabled(false);
                return;
            } else {
                try {
                    new URL(endpointText.getText());
                } catch (MalformedURLException e) {
                    setErrorMessage(Messages.getString("OptionsDialog.9")); //$NON-NLS-1$
                    getButton(IDialogConstants.OK_ID).setEnabled(false);
                    return;
                }
                File location = new File(locationText.getText());
                if ((!location.exists()) || (!location.isDirectory())) {
                    setErrorMessage("Workspace location does not exist.");
                    getButton(IDialogConstants.OK_ID).setEnabled(false);
                    return;
                }
            }
            setErrorMessage(null);
            getButton(IDialogConstants.OK_ID).setEnabled(true);
        }
        
        public void init() {
            modifyText(null);
        }

        private void updateProfile() {
            // set values of current profile
            Profile profile = profiles.getProfileByName(profileList.getSelection()[0]);
            profile.setLogin(loginText.getText());
            profile.setPassword(pwdText.getText());
            profile.setEndpoint(endpointText.getText());
            profile.setLocation(locationText.getText());
        }
    }
}
