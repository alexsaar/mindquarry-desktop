package com.mindquarry.desktop.preferences.dialog;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.mindquarry.desktop.I18N;
import com.mindquarry.desktop.preferences.profile.Profile;

public class SVNRepoEditDialog extends TitleAreaDialog {
	
	private Profile.SVNRepoData data;
	private Text id;
	private Text svnURL;
	private Text username;
	private Text password;
	private Text localPath;

	public SVNRepoEditDialog(Shell parentShell, Profile.SVNRepoData data) {
		super(parentShell);
		this.data = data;
        setBlockOnOpen(true);
        setShellStyle(SWT.CLOSE | SWT.MIN | SWT.MAX | SWT.RESIZE);
	}
	
	public Profile.SVNRepoData getData() {
		return data;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText(I18N.get("Edit SVN"));
		setTitle(I18N.get("Edit single SVN checkout"));
		setMessage(I18N.get("Configure the checkout of a certain SVN URL into a local " +
				"working directory.\nYou can choose an already checked out working directory."));
				
//				"but please note\nthat this SVN client will store the .svn metadata folder " +
//				"in a different location.\nThis is necessary for the one-click synchronize " +
//				"feature (eg. detecting deleted files\nautomatically without a svn del)."));
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout(3, false));
		
		id = createLabelAndText(I18N.get("Name:"), composite, false, true);
        id.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
        		if (id.getText().equals("")) {
        			getButton(OK).setEnabled(false);
        		} else {
        			getButton(OK).setEnabled(true);
        		}
            }
        });
		
		svnURL = createLabelAndText(I18N.get("SVN URL:"), composite, false, true);
		localPath = createLabelAndText(I18N.get("Local Folder:"), composite, false, false);
		
        Button browseButton = new Button(composite, SWT.PUSH);
        browseButton.setText(I18N.get("Browse"));
        browseButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                DirectoryDialog fd = new DirectoryDialog(getShell(), SWT.OPEN);
                fd.setText(I18N.get("Select folder for checkout."));

                String path = fd.open();
                if (path != null) {
                	localPath.setText(path);
                }
            }
        });
        
		username = createLabelAndText(I18N.get("Username:"), composite, false, true);
		password = createLabelAndText(I18N.get("Password:"), composite, true, true);
		
		return composite;
	}
	
	@Override
	public void create() {
		super.create();

		fillInFields();
	}

	private void fillInFields() {
		id.setText(data.id);
		svnURL.setText(data.svnURL);
		localPath.setText(data.localPath);
		username.setText(data.username);
		password.setText(data.password);
	}

	@Override
	protected void okPressed() {
    	data.id = id.getText();
    	data.svnURL = svnURL.getText();
    	data.localPath = localPath.getText();
    	data.username = username.getText();
    	data.password = password.getText();
    	
		super.okPressed();
	}

	private Text createLabelAndText(String label, Composite parent, boolean password, boolean span) {
        CLabel idLabel = new CLabel(parent, SWT.LEFT);
        idLabel.setText(label);
        
        final Text text;
        if (password) {
        	text = new Text(parent, SWT.PASSWORD | SWT.BORDER);
        } else {
        	text = new Text(parent, SWT.SINGLE | SWT.BORDER);
        }
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        if (span) {
        	gridData.horizontalSpan = 2;
        }
        text.setLayoutData(gridData);
        text.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                text.selectAll();
            }
        });
        return text;
	}
}
