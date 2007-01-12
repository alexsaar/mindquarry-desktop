/*
 * Copyright (C) 2005-2006 MindQuarry GmbH, All Rights Reserved
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
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.mindquarry.client.MindClient;

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
        optionsImage = new Image(null, getClass().getResourceAsStream(
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
        if (optionsImage != null) {
            setTitleImage(optionsImage);
        }
        return contents;
    }

    /**
     * Closes the dialog box Override so we can dispose the image we created
     */
    @Override
    public boolean close() {
        if (optionsImage != null) {
            optionsImage.dispose();
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
        validator = new FieldValidator();

        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        gridLayout.makeColumnsEqualWidth = true;

        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(gridLayout);

        loginLabel = new CLabel(composite, SWT.LEFT);
        loginLabel.setText(Messages.getString("OptionsDialog.3")); //$NON-NLS-1$
        loginLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        loginText = new Text(composite, SWT.SINGLE | SWT.BORDER);
        loginText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        loginText.setText(options.getProperty(MindClient.LOGIN_KEY));
        loginText.addModifyListener(validator);
        loginText.addFocusListener(new TextFocusListener(loginText));

        pwdLabel = new CLabel(composite, SWT.LEFT);
        pwdLabel.setText(Messages.getString("OptionsDialog.4")); //$NON-NLS-1$
        pwdLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        pwdText = new Text(composite, SWT.PASSWORD | SWT.BORDER);
        pwdText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        pwdText.setText(options.getProperty(MindClient.PASSWORD_KEY));
        pwdText.addModifyListener(validator);
        pwdText.addFocusListener(new TextFocusListener(pwdText));

        quarryEndpointLabel = new CLabel(composite, SWT.LEFT);
        quarryEndpointLabel.setText(Messages.getString("OptionsDialog.5")); //$NON-NLS-1$
        quarryEndpointLabel
                .setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        quarryEndpointText = new Text(composite, SWT.SINGLE | SWT.BORDER);
        quarryEndpointText
                .setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        quarryEndpointText
                .setText(options.getProperty(MindClient.ENDPOINT_KEY));
        quarryEndpointText.addModifyListener(validator);
        quarryEndpointText.addFocusListener(new TextFocusListener(
                quarryEndpointText));

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

        validator.init();
    }

    @Override
    protected void okPressed() {
        options.put(MindClient.LOGIN_KEY, loginText.getText());
        options.put(MindClient.PASSWORD_KEY, pwdText.getText());
        options.put(MindClient.ENDPOINT_KEY, quarryEndpointText.getText());

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
