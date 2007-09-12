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

import com.mindquarry.desktop.util.NotAuthorizedException;

/**
 * A model base for collection types that only fetches its
 * items when init() is called (not during construction time
 * like ModelBase does).
 * 
 * @author <a href="naber(at)mindquarry(dot)com">Daniel Naber</a>
 */
public abstract class ManualTransformModelBase extends ModelBase {

    public ManualTransformModelBase() {
        super();
    }
    
    /**
     * This constructor does not automatically call the transformer. You need to call init(Transformer)
     * yourfeld. The advantage of this is that the extending class can provide a getSize() method
     * that can return the size of a collection before the transformer is used, i.e. before
     * all items of the collection are fetched (e.g. TaskList).
     */
	public ManualTransformModelBase(String url, String login, String password) throws NotAuthorizedException, Exception {
		initModel();
		InputStream content = getContent(url, login, password);
		doc = parseInput(content);
	}

}
