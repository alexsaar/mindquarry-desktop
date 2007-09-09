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
package com.mindquarry.desktop.client.action.app;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.mindquarry.desktop.client.Messages;
import com.mindquarry.desktop.client.MindClient;
import com.mindquarry.desktop.util.JarUtilities;

/**
 * "About" dialog.
 * 
 * @author dnaber
 */
public class AboutAction extends Action {
  private static final Log log = LogFactory.getLog(AboutAction.class);
  
  public static final String ID = AboutAction.class.getSimpleName();

  private MindClient client;
  
  public AboutAction(MindClient client) {
    super();
    this.client = client;
    setId(ID);
    setActionDefinitionId(ID);
    setText(Messages.getString("About..."));  //$NON-NLS-1$
  }

  public void run() {
    AboutDialog dlg = new AboutDialog(client.getShell());
    dlg.open();
  }

  class AboutDialog extends TitleAreaDialog {

    public AboutDialog(Shell shell) {
      super(shell);
      setBlockOnOpen(true);
      setShellStyle(SWT.CLOSE | SWT.MIN | SWT.MAX | SWT.RESIZE);
    }

    protected Control createContents(Composite parent) {
      Control contents = super.createContents(parent);
      setTitle(Messages.getString("About")); //$NON-NLS-1$
      setMessage(Messages.getString("Information about the Mindquarry desktop client."), //$NON-NLS-1$
          IMessageProvider.INFORMATION);
      getShell().redraw();
      return contents;
    }

    protected Control createDialogArea(Composite parent) {
      Composite composite = new Composite(parent, SWT.NONE);
      composite.setLayout(new GridLayout(1, false));
      composite.setLayoutData(new GridData(GridData.FILL_BOTH));

      Label titleBarSeparator = new Label(composite, SWT.HORIZONTAL
          | SWT.SEPARATOR);
      titleBarSeparator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

      // no need to translate these I think:
      Label label = new Label(composite, SWT.NONE);
      String version = "unknown";  //$NON-NLS-1$
      String buildDate = "unknown";  //$NON-NLS-1$
      try {
        String jarFileName = JarUtilities.getJar(MindClient.JAR_NAMES);
        version = JarUtilities.getVersion(jarFileName);
        buildDate = JarUtilities.getBuildDate(jarFileName);
      } catch (IOException e) {
        // also happens during development when there is not JAR
        log.error("Could not get version number or build date from JAR", e);
      }
      label.setText("Version: " + version);  //$NON-NLS-1$
      label = new Label(composite, SWT.NONE);
      label.setText("Build date: " + buildDate);  //$NON-NLS-1$
      label = new Label(composite, SWT.NONE);
      label.setText("Copyright (C) 2006-2007 Mindquarry GmbH, All Rights Reserved\n" +  //$NON-NLS-1$
          "This software is published under the Mozilla Public License Version 1.1"); //$NON-NLS-1$
      
      composite = new Composite(composite, SWT.NONE);
      composite.setLayout(new GridLayout(1, false));

      return composite;
    }
    
    protected void createButtonsForButtonBar(Composite parent) {  
      createButton(parent, IDialogConstants.OK_ID,
                Messages.getString("OK"), true);  
    }
    
  }

}
