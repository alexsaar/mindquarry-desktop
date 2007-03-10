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
package com.mindquarry.desktop.model.conversation;

import org.dom4j.Element;
import org.dom4j.Node;

import com.mindquarry.desktop.model.ModelBase;
import com.mindquarry.desktop.model.TransformerBase;

import dax.Path;

/**
 * Transformer for processing XML descriptions of participants.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class ConversationTransformer extends TransformerBase {
    private Conversation conversation;

    @Path("//topic")
    public void topic(Node node) {
        if (node instanceof Element) {
            Element element = (Element) node;
            conversation.setTopic(element.getText());
        }
    }

    /**
     * {@inheritJavaDoc}
     * 
     * @see com.mindquarry.minutes.editor.model.tranformer.TransformerBase#handleModelPart(com.mindquarry.minutes.editor.model.ModelBase)
     */
    @Override
    protected void handleModelPart(ModelBase model) {
        conversation = (Conversation) model;
    }
}
