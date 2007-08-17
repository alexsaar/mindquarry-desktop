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

	public ModelBase() {
		initModel();
	}

	public ModelBase(InputStream data, TransformerBase transformer) {
		initModel();
		parseInput(data, transformer);
	}

	public ModelBase(String url, String login, String password,
			TransformerBase transformer) throws NotAuthorizedException,
			Exception {
		initModel();
		InputStream content = getContent(url, login, password);
		parseInput(content, transformer);
	}

	/**
	 * Can be overidden by subclasses for initializing member variables when
	 * constructor initialization with transformer is used.
	 */
	protected void initModel() {
		// nothing to do here
	}

	private InputStream getContent(String url, String login, String password)
			throws NotAuthorizedException, Exception {
		InputStream content = HttpUtilities.getContentAsXML(login, password,
				url);
		return content;
	}

	private void parseInput(InputStream data, TransformerBase transformer) {
		SAXReader reader = new SAXReader();
		Document doc;
		try {
			doc = reader.read(data);
		} catch (DocumentException e) {
			log.error("Error while reading document.", e); //$NON-NLS-1$
			return;
		}
		transformer.execute(this, doc);
	}
}
