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
package com.mindquarry.desktop.client.widget.util.container;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;

/**
 * Add summary documentation here.
 * 
 * @author <a href="mailto:saar@mindquarry.com">Alexander Saar</a>
 */
public abstract class UpdateContainerRunnable<V extends Viewer> implements
        Runnable {
    protected ContainerWidget<V> containerWidget;

    private final boolean empty;
    private final String errorMessage;
    private final boolean refreshing;
    private final String refreshMessage;
    private final String emptyMessage;

    private Image icon;
    private String message;
    
    private static Image networkErrorIcon = new Image(null, UpdateContainerRunnable.class.getResourceAsStream(
        "/org/tango-project/tango-icon-theme/22x22/status/network-error.png")); //$NON-NLS-1$

    private static Image infoIcon = new Image(null, UpdateContainerRunnable.class.getResourceAsStream(
        "/org/tango-project/tango-icon-theme/22x22/status/dialog-information.png")); //$NON-NLS-1$

    private static Image warningIcon = new Image(null, UpdateContainerRunnable.class.getResourceAsStream(
        "/org/tango-project/tango-icon-theme/22x22/status/dialog-warning.png")); //$NON-NLS-1$
    
    @Deprecated
    public UpdateContainerRunnable(ContainerWidget<V> containerWidget,
            boolean refreshing, String refreshMessage, boolean empty, 
            String emptyMessage, String errorMessage) {
        this.containerWidget = containerWidget;

        this.refreshing = refreshing;
        this.refreshMessage = refreshMessage;
        this.empty = empty;
        this.emptyMessage = emptyMessage;
        this.errorMessage = errorMessage;

        // replicated structure of the if statement in run()
        if (refreshing) {
            // show a progress bar
            message = refreshMessage;
        } else if (errorMessage == null && !empty) {
            // show the content itself
        } else if (errorMessage == null && empty) {
            // show a message that there is no content
            icon = infoIcon;
            message = emptyMessage;
        } else {
            // show an error message
            icon = networkErrorIcon;
            message = errorMessage;
        }
    }

    public UpdateContainerRunnable(ContainerWidget<V> containerWidget,
            boolean refreshing, boolean empty, String icon, String message) {
        this.containerWidget = containerWidget;

        this.refreshing = refreshing;
        this.icon = null;
        this.empty = empty;
        this.message = message;

        if ("info".equals(icon)) {
            this.icon = infoIcon;
        } else if ("warn".equals(icon)) {
            this.icon = warningIcon;
        } else if ("networkerror".equals(icon)) {
            this.icon = networkErrorIcon;
        }
        
        // TODO: deprecated ...
        this.emptyMessage = null;
        this.errorMessage = null;
        this.refreshMessage = null;
    }

    public void run() {
        if (refreshing) {
            // an action is running, so we show a progress bar:
            destroyContent();
            containerWidget.refreshWidget = new UpdateWidget(containerWidget,
                    message);
        } else if (errorMessage == null && !empty) {
            // show the content itself:
            destroyContent();
            createContainerContent();
        } else {
            // show a message with icon (e.g. empty or error)
            destroyContent();
            containerWidget.messageWidget = new IconTextWidget(containerWidget,
                    icon, message);
        }
        containerWidget.layout(true);
    }

    /**
     * Sets the refresh message if the refresh widget is visible.
     */
    public void setMessage(String message) {
        if (containerWidget.refreshWidget != null) {
            ((UpdateWidget)containerWidget.refreshWidget).setMessage(message);
        }
    }

    /**
     * Sets an additional text if the refresh widget is visible.
     * TODO: change it so that any type of widget supports setting
     * an update message
     */
    public void setUpdateMessage(String message) {
        if (containerWidget.refreshWidget != null) {
            ((UpdateWidget)containerWidget.refreshWidget).setUpdateMessage(message);
        }
    }
    
    protected abstract void createContainerContent();

    private void destroyContent() {
        destroyContainerContent();
        if (containerWidget.refreshWidget != null) {
            containerWidget.refreshWidget.dispose();
            containerWidget.refreshWidget = null;
        }
        if (containerWidget.messageWidget != null) {
            containerWidget.messageWidget.dispose();
            containerWidget.messageWidget = null;
        }
    }

    protected abstract void destroyContainerContent();
}