/**
 * Copyright (C) 2005 MindQuarry GmbH, All Rights Reserved
 */
package com.mindquarry.client.dialog;

import java.util.Properties;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
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
                "/images/options.png"));

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

        setTitle("MindClient Options");
        setMessage("Configure your MindClient seetings.",
                IMessageProvider.INFORMATION);

        getShell().setText("MindClient Options");
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
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        gridLayout.makeColumnsEqualWidth = true;

        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(gridLayout);

        loginLabel = new CLabel(composite, SWT.LEFT);
        loginLabel.setText("Your Login ID:");
        loginLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        loginText = new Text(composite, SWT.SINGLE | SWT.BORDER);
        loginText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        loginText.setText(options.getProperty(MindClient.LOGIN_KEY));

        pwdLabel = new CLabel(composite, SWT.LEFT);
        pwdLabel.setText("Your Password:");
        pwdLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        pwdText = new Text(composite, SWT.PASSWORD | SWT.BORDER);
        pwdText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        pwdText.setText(options.getProperty(MindClient.PASSWORD_KEY));

        quarryEndpointLabel = new CLabel(composite, SWT.LEFT);
        quarryEndpointLabel.setText("Your Quarry Endpoint:");
        quarryEndpointLabel
                .setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        quarryEndpointText = new Text(composite, SWT.SINGLE | SWT.BORDER);
        quarryEndpointText
                .setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        quarryEndpointText
                .setText(options.getProperty(MindClient.ENDPOINT_KEY));

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
    }

    @Override
    protected void okPressed() {
        options.put(MindClient.LOGIN_KEY, loginText.getText());
        options.put(MindClient.PASSWORD_KEY, pwdText.getText());
        options.put(MindClient.ENDPOINT_KEY, quarryEndpointText.getText());

        super.okPressed();
    }
}
