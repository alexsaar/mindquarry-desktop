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

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import com.mindquarry.desktop.Messages;
import com.mindquarry.desktop.event.EventBus;
import com.mindquarry.desktop.event.network.ProxySettingsChangedEvent;

/**
 * This class creates a preference page for proxy server settings.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class ProxySettingsPage extends ErrorDisplayingPreferencePage {
    public static final String NAME = "proxy";
    public static final String TITLE = Messages.getString("Proxy Settings");

    public static final String PREF_PROXY_ENABLED = "com.mindquarry.desktop.proxy.enabled";
    public static final String PREF_PROXY_LOGIN = "com.mindquarry.desktop.proxy.login";
    public static final String PREF_PROXY_PASSWORD = "com.mindquarry.desktop.proxy.password";
    public static final String PREF_PROXY_URL = "com.mindquarry.desktop.proxy.url";

    private Button enableProxy;
    private Text login;
    private Text pwd;
    private Text url;

    /**
     * ProxySettingsPage default constructor
     */
    public ProxySettingsPage() {
        super(TITLE);

        // initialize preference page
        setDescription(Messages
                .getString("Manage proxy settings of the application."));
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
        PreferenceStore store = (PreferenceStore) getPreferenceStore();

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, true));

        enableProxy = new Button(composite, SWT.CHECK);
        enableProxy.setText(Messages.getString("Enable proxy support"));
        enableProxy.setSelection(store.getBoolean(PREF_PROXY_ENABLED));
        enableProxy.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                enableProxyFields(enableProxy.getSelection());
                performValidation();
            }
        });

        Group proxyGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
        proxyGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        //settingsGroup.setText(Messages.getString("Profile Settings")); //$NON-NLS-1$
        proxyGroup.setLayout(new GridLayout(1, true));
        
        CLabel label = new CLabel(proxyGroup, SWT.LEFT);
        label.setText(Messages.getString("Proxy URL") + ":");
        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite errorComp = createErrorBorderComposite(proxyGroup);
        url = new Text(errorComp, SWT.SINGLE | SWT.BORDER);
        url.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        registerErrorBorderComposite(errorComp, url);
        url.setText(store.getString(PREF_PROXY_URL));
        url.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                performValidation();
            }
        });
        url.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                url.selectAll();
            }
        });
        
        label = new CLabel(proxyGroup, SWT.LEFT);
        label.setText(Messages.getString("Proxy Login") + ":");
        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        login = new Text(proxyGroup, SWT.SINGLE | SWT.BORDER);
        login.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        login.setText(store.getString(PREF_PROXY_LOGIN));
        login.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                performValidation();
            }
        });
        login.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                login.selectAll();
            }
        });
        label = new CLabel(proxyGroup, SWT.LEFT);
        label.setText(Messages.getString("Proxy Password") + ":");
        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        pwd = new Text(proxyGroup, SWT.PASSWORD | SWT.BORDER);
        pwd.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        pwd.setText(store.getString(PREF_PROXY_PASSWORD));
        pwd.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                performValidation();
            }
        });
        pwd.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                pwd.selectAll();
            }
        });

        enableProxyFields(enableProxy.getSelection());
        performValidation();
        return composite;
    }

    @Override
    public boolean performOk() {
        PreferenceStore store = (PreferenceStore) getPreferenceStore();

        if (enableProxy != null) {
            store.setValue(PREF_PROXY_ENABLED, enableProxy.getSelection());
            store.setValue(PREF_PROXY_LOGIN, login.getText());
            store.setValue(PREF_PROXY_PASSWORD, pwd.getText());
            store.setValue(PREF_PROXY_URL, url.getText());
            
            EventBus.send(new ProxySettingsChangedEvent(this, enableProxy
                    .getSelection(), url.getText(), pwd.getText(), login
                    .getText()));
        }
        return true;
    }

    private void enableProxyFields(boolean enabled) {
        login.setEnabled(enabled);
        pwd.setEnabled(enabled);
        url.setEnabled(enabled);
    }

    private void performValidation() {
        if (enableProxy.getSelection()) {
            try {
                new URL(url.getText());
            } catch (MalformedURLException e) {
                setInvalid(Messages.getString("Proxy URL is not a valid URL ({0})", e.getLocalizedMessage()), url);
                return;
            }
        }
        setValid();
    }
}
