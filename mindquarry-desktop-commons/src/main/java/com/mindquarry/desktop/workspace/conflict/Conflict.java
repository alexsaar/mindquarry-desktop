package com.mindquarry.desktop.workspace.conflict;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tigris.subversion.javahl.Status;

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
	
	protected Status localStatus;

	public Conflict(Status localStatus) {
		this.localStatus = localStatus;
		log = LogFactory.getLog(getClass());
	}
	
	public abstract void handleBeforeUpdate();
	public abstract void handleAfterUpdate();
	
	public abstract void accept(ConflictHandler handler) throws CancelException;

	public Status getLocalStatus() {
		return localStatus;
	}
}
