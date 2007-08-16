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
package com.mindquarry.desktop.workspace;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.Notify2;
import org.tigris.subversion.javahl.NotifyAction;
import org.tigris.subversion.javahl.NotifyInformation;
import org.tigris.subversion.javahl.Revision;
import org.tigris.subversion.javahl.Status;
import org.tigris.subversion.javahl.StatusKind;
import org.tmatesoft.svn.core.javahl.SVNClientImpl;

/**
 * Helper class for working with SVNkit.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class SVNHelper2 {
	private static final String REPOSITORY_PREFIX = "/trunk/";

	protected String repositoryURL;

	protected String localPath;

	protected String username;

	protected String password;

	protected SVNClientImpl client;

	public SVNHelper2(String repositoryURL, String localPath, String username,
			String password, Notify2 notifyListener) {
		this.repositoryURL = repositoryURL;
		this.localPath = localPath;
		this.username = username;
		this.password = password;

		// create SVN client, set authentication info
		client = SVNClientImpl.newInstance();
		if (username != null) {
			client.username(username);
			if (password != null) {
				client.password(password);
			}
		}
		// register for svn notifications on update and commit
		client.notification2(notifyListener);
	}

	public void synchronize(ConflictHandler handler) {
		try {
			client.cleanup(localPath);
			// client.lock(new String[] {localPath}, "locking for
			// synchronization", false);

			List<Status> localChanges = getLocalChanges();
			List<Status> remoteAndLocalChanges = getRemoteAndLocalChanges();

			List<Conflict> conflicts = analyzeChanges(localChanges,
					remoteAndLocalChanges);

			// UI, cancel possible
			askUserForConflicts(conflicts, handler);

			handleConflictsBeforeUpdate(conflicts);
			// here something goes over the wire
			client.update(localPath, Revision.HEAD, true);
			handleConflictsAfterUpdate(conflicts);

			
			/*
			// UI, cancel possible
			handleContentConflicts();
			// UI, cancel possible
			String message = askForCommitMessage();
			
			// add all unversioned (new) files and folder
			client.add(localPath, true, true);
			// here something goes over the wire
			client.commit(new String[] { localPath }, message, true);
			*/

			// client.revert(localPath, true);
		} catch (ClientException e) {
			// TODO think about exception handling
			e.printStackTrace();
		} catch (CancelException e) {
			System.out.println("Canceled");
		} finally {
			// try {
			// client.unlock(new String[] {localPath}, false);
			// } catch (ClientException e) {
			// e.printStackTrace();
			// }
		}
	}

	public List<Status> getLocalChanges() throws ClientException {
		System.out.println("local changes:");

		Status[] statusArray = client.status(localPath, true, false, false);

		List<Status> statusList = Arrays.asList(statusArray);
		Collections.sort(statusList, new Comparator<Status>() {
			public int compare(Status left, Status right) {
				return left.getPath().compareTo(right.getPath());
			}
		});

		return statusList;
	}

	/**
	 * Returns a list with all local and remote changes combined. It's not
	 * easily possible to get only the remote changes, that's why we use this
	 * combined list throughout the code. The status inside this list will be
	 * different from the one returned by getLocalChanges() since it might
	 * contain the remote change of the same path.
	 */
	public List<Status> getRemoteAndLocalChanges() throws ClientException {
		System.out.println("remote changes:");
		return Arrays.asList(client.status(localPath, true, true, false));
	}

	private List<Conflict> analyzeChanges(List<Status> localChanges,
			List<Status> remoteAndLocalChanges) {
		List<Conflict> conflicts = new ArrayList<Conflict>();
		
		// for easy look-up by path
		Map<String, Status> remoteAndLocalMap = createRemoteStatusMap(remoteAndLocalChanges);

		// go through local changes
		for (Status status : localChanges) {
			// local add conflicts with remote (unversioned is the typical case,
			// since the user has just created a new file/folder and doesn't
			// use svn add - that's what this client does automatically)
			if (status.getTextStatus() == StatusKind.added
					|| status.getTextStatus() == StatusKind.unversioned) {
				
				// check for remote version
				if (remoteAndLocalMap.containsKey(status.getPath())) {
					Status remoteStatus = remoteAndLocalMap.get(status.getPath());
					// if the file exists remotely, it will have a URL set
					if (remoteStatus.getUrl() != null) {
						conflicts.add(new AddConflict(status, remoteStatus));
					}
				}
			}
		}
		return conflicts;
	}

	private void askUserForConflicts(List<Conflict> conflicts,
			ConflictHandler handler) throws CancelException {
		for (Conflict conflict : conflicts) {
			System.out.println("Asking user for : " + conflict.toString());
			conflict.accept(handler);
		}
	}

	private void handleConflictsBeforeUpdate(List<Conflict> conflicts) {
		for (Conflict conflict : conflicts) {
			System.out.println("Before Update: " + conflict.toString());
			conflict.handleBeforeUpdate();
		}
	}

	private void handleConflictsAfterUpdate(List<Conflict> conflicts) {
		for (Conflict conflict : conflicts) {
			System.out.println("After Update: " + conflict.toString());
			conflict.handleAfterUpdate();
		}
	}

	public Map<String, Status> createRemoteStatusMap(List<Status> stati) {
		Map<String, Status> map = new HashMap<String, Status>();
		for (Status s : stati) {
			map.put(s.getPath(), s);
		}
		return map;
	}

	public static String notifyToString(NotifyInformation info) {
		return NotifyAction.actionNames[info.getAction()] + " "
				+ info.getPath();
	}
}
