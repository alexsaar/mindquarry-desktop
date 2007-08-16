package com.mindquarry.desktop.workspace;

import org.tigris.subversion.javahl.Status;

public abstract class Conflict {
	protected Status localStatus;
	
	protected Status remoteStatus;

	public Conflict(Status localStatus, Status remoteStatus) {
		this.localStatus = localStatus;
		this.remoteStatus = remoteStatus;
	}
	
	public abstract void handleBeforeUpdate();
	public abstract void handleAfterUpdate();
	
	public abstract void accept(ConflictHandler handler) throws CancelException;

	public Status getLocalStatus() {
		return localStatus;
	}

	public Status getRemoteStatus() {
		return remoteStatus;
	}
}
