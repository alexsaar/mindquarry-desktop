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
package com.mindquarry.minutes.editor.model;

import java.io.InputStream;

import org.eclipse.swt.graphics.Image;

import com.mindquarry.minutes.editor.model.tranformer.ParticipantTransformer;

/**
 * Model type that represents a conversation participant.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class Participant extends ModelPart {
    private Image picture = null;

    private String name = null;

    public Participant() {
        super();
    }
    
    public Participant(InputStream data) {
        super(data, new ParticipantTransformer());
    }
    
    public Participant(Image picture, String name) {
        super();
        this.picture = picture;
        this.name = name;
    }

    /**
     * Getter for name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for name.
     *
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for picture.
     *
     * @return the picture
     */
    public Image getPicture() {
        return picture;
    }

    /**
     * Setter for picture.
     *
     * @param picture the picture to set
     */
    public void setPicture(Image picture) {
        this.picture = picture;
    }
}
