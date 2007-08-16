package com.mindquarry.desktop.workspace.conflict;

import org.tigris.subversion.javahl.Status;

import com.mindquarry.desktop.workspace.exception.CancelException;

/**
 * Abstract base class for structure conflicts.
 *
 * @author <a href="mailto:saar@mindquarry.com">Alexander Saar</a>
 *
 */
public abstract class Conflict {
	protected Status localStatus;

	public Conflict(Status localStatus) {
		this.localStatus = localStatus;
	}
	
	public abstract void handleBeforeUpdate();
	public abstract void handleAfterUpdate();
	
	public abstract void accept(ConflictHandler handler) throws CancelException;

	public Status getLocalStatus() {
		return localStatus;
	}
}
