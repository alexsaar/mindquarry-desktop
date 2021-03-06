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
package com.mindquarry.desktop.model;

import java.io.InputStream;

import junit.framework.TestCase;

import org.junit.Test;

import com.mindquarry.desktop.model.conversation.Conversation;
import com.mindquarry.desktop.model.conversation.Participant;

/**
 * Add summary documentation here.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class LoadModelTest extends TestCase {
    @Test
    public void testLoadParticipant() {
        InputStream is = getClass().getResourceAsStream(
                "/com/mindquarry/desktop/model/participant.xml"); //$NON-NLS-1$
        Participant participant = new Participant(is);
        assertEquals("Alexander Saar", participant.getName()); //$NON-NLS-1$
    }
    
    @Test
    public void testLoadConversation() {
        InputStream is = getClass().getResourceAsStream(
                "/com/mindquarry/desktop/model/conversation.xml"); //$NON-NLS-1$
        Conversation conversation = new Conversation(is);
        assertEquals("discuss something", conversation.getTopic()); //$NON-NLS-1$
    }
}
