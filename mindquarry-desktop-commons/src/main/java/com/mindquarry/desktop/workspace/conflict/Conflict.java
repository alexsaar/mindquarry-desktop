package com.mindquarry.desktop.workspace.conflict;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.Status;
import org.tmatesoft.svn.core.javahl.SVNClientImpl;

import com.mindquarry.desktop.workspace.exception.CancelException;

/**
 * Abstract base class for structure conflicts.
 *
 * @author <a href="mailto:saar@mindquarry.com">Alexander Saar</a>
 * @author <a href="mailto:victor.saar@mindquarry.com">Victor Saar</a>
 * @author <a href="mailto:klimetschek@mindquarry.com">Alexander Klimetschek</a>
 */
public abstract class Conflict {
	protected static Log log;
	
    protected SVNClientImpl client;

	protected Status status;
	
	public Conflict(Status status) {
		this.status = status;
		log = LogFactory.getLog(getClass());
	}
	
	public void setSVNClient(SVNClientImpl client) {
	    this.client = client;
	}
	
	public abstract void handleBeforeUpdate() throws ClientException;
	public abstract void handleAfterUpdate();
	
	public abstract void accept(ConflictHandler handler) throws CancelException;

	public Status getStatus() {
		return status;
	}
}
