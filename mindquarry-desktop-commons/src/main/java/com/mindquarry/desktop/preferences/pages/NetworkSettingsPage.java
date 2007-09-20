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

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.mindquarry.desktop.Messages;

/**
 * This class creates a preference page for network settings.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class NetworkSettingsPage extends PreferencePage {
    public static final String NAME = "network";

    /**
     * ShortcutsPage default constructor
     */
    public NetworkSettingsPage() {
        super(Messages.getString("Network"));

        // initialize preference page
        // setDescription(Messages
        // .getString("Manage network settings of the application."));
        setImageDescriptor(ImageDescriptor
                .createFromImage(new Image(
                        null,
                        getClass()
                                .getResourceAsStream(
                                        "/org/tango-project/tango-icon-theme/16x16/places/network-server.png")))); //$NON-NLS-1$
        noDefaultAndApplyButton();
    }

    /**
     * Creates the controls for this page
     */
    @Override
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, true));

        Label label = new Label(composite, SWT.LEFT | SWT.WRAP);
        label
                .setText(Messages
                        .getString("This section allows you to configure the network settings for the application."));
        label = new Label(composite, SWT.LEFT | SWT.WRAP);
        label
                .setText(Messages
                        .getString(
                                "You can manage your Mindquarry {0} as well as the {1} for your network.",
                                ServerProfilesPage.TITLE,
                                ProxySettingsPage.TITLE));
        return composite;
    }
}
