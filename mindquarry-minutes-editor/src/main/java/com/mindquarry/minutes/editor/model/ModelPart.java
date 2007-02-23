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

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import dax.Transformer;

/**
 * Abstract base class for all model types. Provides base functionality for
 * parsing model data from {@link InputStream}.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public abstract class ModelPart {
    public ModelPart(InputStream data, Transformer transformer) {
        parseInput(data, transformer);
    }

    private void parseInput(InputStream data, Transformer transformer) {
        SAXReader reader = new SAXReader();
        Document doc;
        try {
            doc = reader.read(data);
        } catch (DocumentException e) {
            e.printStackTrace();
            return;
        }
        transformer.execute(doc);
    }
}
