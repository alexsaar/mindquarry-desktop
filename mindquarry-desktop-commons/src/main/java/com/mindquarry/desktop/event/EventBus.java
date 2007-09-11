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
package com.mindquarry.desktop.event;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Local broker for inter application events. Follows the singleton pattern.
 * 
 * @author <a href="mailto:alexander(dot)klimetschek(at)mindquarry(dot)com">
 *         Alexander Klimetschek</a>
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class EventBus {
    
    private static Log log = LogFactory.getLog(EventBus.class);
    
    private static EventBus instance = null;
    
    private Collection<EventListener> registeredListeners;
    
    /**
     * Singleton getter
     */
    public static EventBus get() {
        if (instance == null) {
            instance = new EventBus();
        }
        return instance;
    }
    
    /**
     * Sends an event synchronously. Convenience static method.
     */
    public static void send(Event event) {
        get().sendEvent(event);
    }

    /**
     * Sends an event asynchronously, ie. it returns immediately and the event
     * is delivered inside a new thread. Convenience static method.
     */
    public static void sendAsync(Event event) {
        get().sendAsyncEvent(event);
    }

    /**
     * Registers an event listener to receive all events sent over this
     * EventBus. Convenience static method.
     */
    public static void registerListener(EventListener listener) {
        get().registerEventListener(listener);
    }
    
    /**
     * Private constructor because of singleton pattern.
     */
    private EventBus() {
        registeredListeners = new ArrayList<EventListener>();
    }

    /**
     * Registers an event listener to receive all events sent over this
     * EventBus.
     */
    public void registerEventListener(EventListener listener) {
        log.info("registerEventListener: listener=" + listener.getClass().getName());
        registeredListeners.add(listener);
    }
    
    /**
     * Sends an event synchronously.
     */
    public void sendEvent(Event event) {
        publishEvent(event, true);
    }
    
    /**
     * Sends an event asynchronously, ie. it returns immediately and the event
     * is delivered inside a new thread.
     */
    public void sendAsyncEvent(Event event) {
        publishEvent(event, false);
    }

    public void publishEvent(final Event event, boolean block) {
        log.info("publishEvent: event=" + event + ", block="+block);

        if (block) {
            // deliver event synchronously
            deliverEvent(event);
        } else {
            // deliver event asynchronously
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    // FIXME: exception handling in event deliver Thread?
                    deliverEvent(event);
                }
            });
            thread.setDaemon(true);
            thread.start();
        }
    }

    private void deliverEvent(Event event) {
        for (EventListener listener : registeredListeners) {
            listener.onEvent(event);
        }
    }
}
