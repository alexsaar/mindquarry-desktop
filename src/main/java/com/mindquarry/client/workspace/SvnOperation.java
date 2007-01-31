/*
 * Copyright (C) 2006-2007 MindQuarry GmbH, All Rights Reserved
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
package com.mindquarry.client.workspace;

import java.io.File;

import org.eclipse.jface.operation.IRunnableWithProgress;
import org.springframework.beans.factory.BeanFactory;
import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.SVNClientInterface;
import org.tigris.subversion.javahl.Status;

import com.mindquarry.client.MindClient;

/**
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public abstract class SvnOperation implements IRunnableWithProgress {
    protected final MindClient client;

    protected final SVNClientInterface svnClient;

    public SvnOperation(final MindClient client) {
        this.client = client;

        // get SVN client interface component
        BeanFactory factory = client.getFactory();
        svnClient = (SVNClientInterface) factory
                .getBean(SVNClientInterface.class.getName());
    }
    
    protected boolean isFolderVersionControled(File item) {
        // retrieve local status
        Status status;
        try {
            status = svnClient.singleStatus(item.getAbsolutePath(), false);
        } catch (ClientException e) {
            e.printStackTrace();
            return false;
        }
        // check if the item is managed by SVN
        if (status.isManaged()) {
            return true;
        } else {
            return false;
        }
    }
}
