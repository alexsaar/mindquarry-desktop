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

/**
 * Abstract base class for events that implements the Event interface and
 * contains common logic.
 * 
 * @author <a href="mailto:alexander(dot)klimetschek(at)mindquarry(dot)com">
 *         Alexander Klimetschek</a>
 *
 */
public abstract class EventBase implements Event {
    private String message;

    private Object source;

    private long timestamp;

    private boolean consumed = false;

    public EventBase(Object source) {
        this.source = source;

        message = ""; //$NON-NLS-1$
        timestamp = System.currentTimeMillis();
    }

    public EventBase(Object source, String message) {
        this.source = source;

        this.message = message;
        timestamp = System.currentTimeMillis();
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.mindquarry.events.Event#getMessage()
     */
    public String getMessage() {
        return message;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.mindquarry.events.Event#getSource()
     */
    public Object getSource() {
        return source;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.mindquarry.events.Event#getTimestamp()
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.mindquarry.events.Event#isConsumed()
     */
    public boolean isConsumed() {
        return consumed;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.mindquarry.events.Event#setConsumed()
     */
    public void consume() {
        consumed = true;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append('{');
        buffer.append("EVENT: name="); //$NON-NLS-1$
        buffer.append(getClass().getName());
        buffer.append(", timestamp="); //$NON-NLS-1$
        buffer.append(getTimestamp());
        buffer.append(", message="); //$NON-NLS-1$
        buffer.append(getMessage());
        if (getSource() != null) {
            buffer.append(", source="); //$NON-NLS-1$
            buffer.append(getSource().getClass().getName());
        }
        buffer.append('}');
        return buffer.toString();
    }
}
