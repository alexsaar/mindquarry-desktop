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
	
	/**
	 * This setter is ensured to be called before {@link accept()} so that an
	 * implementation has full access to the svn client.
	 */
	public void setSVNClient(SVNClientImpl client) {
	    this.client = client;
	}

	/**
	 * Returns the status (including local and remote) of the central object
	 * (ie. file or folder) in the conflict.
	 */
    public Status getStatus() {
        return status;
    }
    
    /**
     * Pass this conflict to a ConflictHandler implementation that will choose
     * one of the possible actions to resolve the conflict. This is typically
     * a user-interface based, presenting the local and remote files connected
     * to this conflict along with the possible actions.
     * 
     * This is called once before the action-resolving methods like
     * {@link beforeUpdate()} and {@link afterUpdate()} are called.
     * 
     * @param handler follows the visitor pattern - has to choose an action that
     * resolves the conflict
     * @throws CancelException if the user cancels the operation, this exception
     * must be thrown
     */
    public abstract void accept(ConflictHandler handler) throws CancelException;
	
	/**
	 * Called before the svn update. Here any conflict that would break the
	 * update must be resolved.
	 * @throws ClientException an implementation might need to access the svn
	 * client to do it's work and that can cause this exception to be thrown
	 */
    public abstract void beforeUpdate() throws ClientException;
    
    /**
     * Called after the svn update. Here any post-work can be done, eg. undoing
     * certain actions done in {@link beforeUpdate} or user actions that have
     * to be executed once the remote modifications are applied to the working
     * copy.
     */
	public abstract void afterUpdate();
}
