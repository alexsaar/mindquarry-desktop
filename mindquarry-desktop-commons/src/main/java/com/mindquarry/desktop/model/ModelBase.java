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
import java.net.MalformedURLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import com.mindquarry.desktop.util.HttpUtilities;
import com.mindquarry.desktop.util.NotAuthorizedException;

/**
 * Abstract base class for all model types. Provides base functionality for
 * parsing model data from {@link InputStream}.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public abstract class ModelBase {
	protected Log log = LogFactory.getLog(this.getClass());
    protected Document doc;

	public ModelBase() {
		initModel();
	}

	public ModelBase(InputStream data, TransformerBase transformer) {
		initModel();
		doc = parseInput(data);
        init(transformer);
	}

    public ModelBase(String url, String login, String password, TransformerBase transformer) 
            throws NotAuthorizedException, MalformedURLException {
        initModel();
        InputStream content = getContent(url, login, password);
        doc = parseInput(content);
        init(transformer);
    }

	/**
	 * Can be overidden by subclasses for initializing member variables when
	 * constructor initialization with transformer is used.
	 */
	protected void initModel() {
		// nothing to do here
	}

	/**
     * Calls the transformer, to be used only together with the 
     * ModelBase(String,String,String) constructor.
     */
    protected void init(TransformerBase transformer) {
        if (doc != null) {
            transformDoc(doc, transformer);
        }
    }

	protected InputStream getContent(String url, String login, String password)
			throws NotAuthorizedException, MalformedURLException {
		InputStream content = HttpUtilities.getContentAsXML(login, password, url);
		return content;
	}

	protected Document parseInput(InputStream data) {
        SAXReader reader = new SAXReader();
        try {
            return reader.read(data);
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
	}
	
	protected void transformDoc(Document doc, TransformerBase transformer) {
		transformer.execute(this, doc);
	}
}
