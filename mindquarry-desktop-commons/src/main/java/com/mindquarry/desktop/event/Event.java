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
 * An event is a message that is sent over an EventBus.
 * 
 * @author <a href="mailto:alexander(dot)klimetschek(at)mindquarry(dot)com">
 *         Alexander Klimetschek</a>
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 *
 */
public interface Event {
    /**
     * The time the event was created.
     */
    public long getTimestamp();

    /**
     * A human-readable message describing the event.
     */
    public String getMessage();

    /**
     * The sender of the event. Can be null.
     */
    public Object getSource();

    /**
     * Whether a listener has handled the event (the semantics for that are
     * event-specific). Programmatically it means that a listener has called
     * {@link consume()} on the event. All events are created un-consumed.
     */
    public boolean isConsumed();
    
    /**
     * Marks this event as handled (the semantics for that are event-specific).
     * This should be called by the first listener that has handled the event
     * so that following listeners can check with {@link isConsumed()} and see
     * that they might not have to do anything with it.
     */
    public void consume();
}
