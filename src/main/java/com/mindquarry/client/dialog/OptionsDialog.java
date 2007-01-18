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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
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

import com.mindquarry.client.MindClient;

/**
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class OptionsDialog extends TitleAreaDialog {
    private Image optionsImage;

    private CLabel profileLabel = null;

    private CCombo profileCombo = null;

    private CLabel loginLabel = null;

    private Text loginText = null;

    private CLabel pwdLabel = null;

    private Text pwdText = null;

    private CLabel locationLabel = null;

    private Text locationText = null;

    private CLabel quarryEndpointLabel = null;

    private Text quarryEndpointText = null;

    private FieldValidator validator = null;

    private final Image icon;

    private final Properties options;

    /**
     * Default constructor.
     * 
     * @param shell the shell
     */
    public OptionsDialog(Shell shell, Image icon, Properties options) {
        super(shell);
        setBlockOnOpen(true);

        // Create the logo image
        this.optionsImage = new Image(null, getClass().getResourceAsStream(
                "/images/options.png")); //$NON-NLS-1$

        this.icon = icon;
        this.options = options;
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

        // create widgets for profile selection
        Group profileGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
        profileGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        profileGroup.setText("Profiles");
        profileGroup.setLayout(new GridLayout(2, false));

        List profileList = new List(profileGroup, SWT.SINGLE | SWT.BORDER
                | SWT.V_SCROLL);
        profileList.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        profileList.add("www.mindquarry.org");
        profileList.add("www.mindquarry.org");
        profileList.add("www.mindquarry.org");
        profileList.add("www.mindquarry.org");
        profileList.add("www.mindquarry.org");
        profileList.add("www.mindquarry.org");
        profileList.add("www.mindquarry.org");
        profileList.add("www.mindquarry.org");
        profileList.add("www.mindquarry.org");
        profileList.add("www.mindquarry.org");
        profileList.add("www.mindquarry.org");
        profileList.add("www.mindquarry.org");
        profileList.add("www.mindquarry.org");
        profileList.add("www.mindquarry.org");
        profileList.add("www.mindquarry.org");

        Composite buttonArea = new Composite(profileGroup, SWT.NONE);
        buttonArea.setLayout(new GridLayout(1, true));

        Button addProfileButton = new Button(buttonArea, SWT.PUSH);
        addProfileButton.setLayoutData(new GridData(GridData.FILL_BOTH
                | GridData.GRAB_VERTICAL));
        addProfileButton.setText("Add Profile...");

        Button delProfileButton = new Button(buttonArea, SWT.PUSH);
        delProfileButton.setLayoutData(new GridData(GridData.FILL_BOTH));
        delProfileButton.setText("Remove Profile");

        Composite buttonAreaSpacer = new Composite(buttonArea, SWT.NONE);
        buttonAreaSpacer.setLayoutData(new GridData(GridData.GRAB_VERTICAL));

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
        this.loginText.setText(this.options.getProperty(MindClient.LOGIN_KEY));
        this.loginText.addModifyListener(this.validator);
        this.loginText.addFocusListener(new TextFocusListener(this.loginText));

        this.pwdLabel = new CLabel(settingsGroup, SWT.LEFT);
        this.pwdLabel.setText(Messages.getString("OptionsDialog.4")); //$NON-NLS-1$
        this.pwdLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        this.pwdText = new Text(settingsGroup, SWT.PASSWORD | SWT.BORDER);
        this.pwdText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        this.pwdText.setText(this.options.getProperty(MindClient.PASSWORD_KEY));
        this.pwdText.addModifyListener(this.validator);
        this.pwdText.addFocusListener(new TextFocusListener(this.pwdText));

        this.quarryEndpointLabel = new CLabel(settingsGroup, SWT.LEFT);
        this.quarryEndpointLabel.setText(Messages.getString("OptionsDialog.5")); //$NON-NLS-1$
        this.quarryEndpointLabel.setLayoutData(new GridData(
                GridData.FILL_HORIZONTAL));

        this.quarryEndpointText = new Text(settingsGroup, SWT.SINGLE
                | SWT.BORDER);
        this.quarryEndpointText.setLayoutData(new GridData(
                GridData.FILL_HORIZONTAL));
        this.quarryEndpointText.setText(this.options
                .getProperty(MindClient.ENDPOINT_KEY));
        this.quarryEndpointText.addModifyListener(this.validator);
        this.quarryEndpointText.addFocusListener(new TextFocusListener(
                this.quarryEndpointText));

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
        this.locationText.setText(this.options
                .getProperty(MindClient.LOCATION_KEY));
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
        return composite;
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

    @Override
    protected void okPressed() {
        this.options.put(MindClient.LOGIN_KEY, this.loginText.getText());
        this.options.put(MindClient.PASSWORD_KEY, this.pwdText.getText());
        this.options.put(MindClient.ENDPOINT_KEY, this.quarryEndpointText
                .getText());

        super.okPressed();
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
            if (loginText.getText().equals("")) { //$NON-NLS-1$
                setErrorMessage(Messages.getString("OptionsDialog.6")); //$NON-NLS-1$
                getButton(IDialogConstants.OK_ID).setEnabled(false);
                return;
            } else if (pwdText.getText().equals("")) { //$NON-NLS-1$
                setErrorMessage(Messages.getString("OptionsDialog.7")); //$NON-NLS-1$
                getButton(IDialogConstants.OK_ID).setEnabled(false);
                return;
            } else if (quarryEndpointText.getText().equals("")) { //$NON-NLS-1$
                setErrorMessage(Messages.getString("OptionsDialog.8")); //$NON-NLS-1$
                getButton(IDialogConstants.OK_ID).setEnabled(false);
                return;
            } else {
                try {
                    new URL(quarryEndpointText.getText());
                } catch (MalformedURLException e) {
                    setErrorMessage(Messages.getString("OptionsDialog.9")); //$NON-NLS-1$
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
    }
}
