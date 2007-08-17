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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.NodeKind;
import org.tigris.subversion.javahl.Notify2;
import org.tigris.subversion.javahl.NotifyAction;
import org.tigris.subversion.javahl.NotifyInformation;
import org.tigris.subversion.javahl.Status;
import org.tigris.subversion.javahl.StatusKind;
import org.tigris.subversion.javahl.Status.Kind;
import org.tmatesoft.svn.core.javahl.SVNClientImpl;

import com.mindquarry.desktop.workspace.conflict.AddConflict;
import com.mindquarry.desktop.workspace.conflict.AddInDeletedConflict;
import com.mindquarry.desktop.workspace.conflict.Conflict;
import com.mindquarry.desktop.workspace.conflict.ConflictHandler;
import com.mindquarry.desktop.workspace.conflict.ConflictList;
import com.mindquarry.desktop.workspace.conflict.DeleteWithModificationConflict;
import com.mindquarry.desktop.workspace.exception.CancelException;

/**
 * Helper class that implements desktop synchronization using the SVN kit. It
 * provides callback hooks for handling conflicts that need user interaction.
 * 
 * Callback handler for conflict resolving must implement the
 * {@link ConflictHandler} interface.
 * 
 * @author <a href="mailto:saar@mindquarry.com">Alexander Saar</a>
 * @author <a href="mailto:victor.saar@mindquarry.com">Victor Saar</a>
 * @author <a href="mailto:alexander.klimetschek@mindquarry.com">Alexander Klimetschek</a>
 */
public class SVNSynchronizer {
	private static final Log log = LogFactory.getLog(SVNSynchronizer.class);

	protected String repositoryURL;

	protected String localPath;

	protected String username;

	protected String password;

	protected SVNClientImpl client;
	
	protected ConflictHandler handler;

	public SVNSynchronizer(String repositoryURL, String localPath,
			String username, String password,
			ConflictHandler handler) {
		this.repositoryURL = repositoryURL;
		this.localPath = localPath;
		this.username = username;
		this.password = password;
		this.handler = handler;

		// create SVN client, set authentication info
		client = SVNClientImpl.newInstance();
		if (username != null) {
			client.username(username);
			if (password != null) {
				client.password(password);
			}
		}
	}
	
	public void setNotifyListener(Notify2 notifyListener) {
        // register for svn notifications on update and commit
        client.notification2(notifyListener);
	}
	
	public void synchronize() {
		try {
			client.cleanup(localPath);
			// TODO: enable locking
			// client.lock(new String[] {localPath}, "locking for
			// synchronization", false);

            // no need to call svn del and add manually for the user
            deleteMissingAndAddUnversioned();
            
			List<Status> localChanges = getLocalChanges();
			List<Status> remoteAndLocalChanges = getRemoteAndLocalChanges();

            // with UI callback, cancel possible
			List<Conflict> conflicts =
			    analyzeChangesAndAskUser(localChanges, remoteAndLocalChanges);
			
			handleConflictsBeforeUpdate(conflicts);
			// here something goes over the wire
			// client.update(localPath, Revision.HEAD, true);
			handleConflictsAfterUpdate(conflicts);

//			// UI, cancel possible
//			handleContentConflicts();
//			// UI, cancel possible
//			String message = askForCommitMessage();
			
//			// here something goes over the wire
//			client.commit(new String[] { localPath }, message, true);

		} catch (ClientException e) {
			// TODO think about exception handling
			e.printStackTrace();
		} catch (CancelException e) {
			log.info("Canceled");
		} finally {
			// try {
			// client.unlock(new String[] {localPath}, false);
			// } catch (ClientException e) {
			// e.printStackTrace();
			// }
		}
	}

	/**
	 * This removes the need for calling svn del and svn add manually. It will
	 * do this only for items not part of a conflict.
     */
    private void deleteMissingAndAddUnversioned() throws ClientException {
        for (Status s : getLocalChanges()) {
            if (s.getTextStatus() == StatusKind.missing) {
                // if the first parameter would be an URL, it would do a commit
                // (and use the second parameter as commit message) - but we
                // use a local filesystem path here and thus we only schedule
                // for a deletion
                client.remove(new String[] { s.getPath() }, null, true);
            }
        }
        
        // add all unversioned (new) files and folder recursively
        client.add(localPath, true, true);
    }

    /**
     * Retrieves local changes as a list that is sorted with the top-most
     * folder or file first.
     */
    public List<Status> getLocalChanges() throws ClientException {
		log.info("## local changes:");

		List<Status> statusList = new ArrayList<Status>(); 
		statusList.addAll(
		        Arrays.asList(client.status(localPath, true, false, false)));
		
		// sort the list from top-level folder to bottom which is important
		// for handling multiple conflicts on the parent folder first
		Collections.sort(statusList, new Comparator<Status>() {
			public int compare(Status left, Status right) {
				return left.getPath().compareTo(right.getPath());
			}
		});

		for (Status s : statusList) {
			log.info(s.getTextStatusDescription() + " " + s.getPath());
		}
		
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
		log.info("## remote changes:");

		List<Status> statusList =
		    Arrays.asList(client.status(localPath, true, true, false));

		for (Status s : statusList) {
			log.info(Kind.getDescription(s.getRepositoryTextStatus()) + " "
					+ s.getPath());
		}

		return statusList;
	}

	/**
	 * Helper method that creates a map that maps file paths to status objects
	 * for faster lookup.
	 */
    private Map<String, Status> createStatusMap(List<Status> stati) {
        Map<String, Status> map = new HashMap<String, Status>();
        for (Status s : stati) {
            map.put(s.getPath(), s);
        }
        return map;
    }
    
    /**
     * Important method that looks out for any structure conflicts before an
     * update and creates {@link Conflict} objects for those. Upon each conflict
     * found, the user is asked to resolve it.
     * 
     * If the user cancels during the conflict resolving, a CancelException is
     * thrown.
     */
	private List<Conflict> analyzeChangesAndAskUser(List<Status> localChanges,
			List<Status> remoteAndLocalChanges) throws CancelException {
		ConflictList<Conflict> conflicts = new ConflictList<Conflict>(handler);

		// for easy look-up by path
		Map<String, Status> remoteAndLocalMap = createStatusMap(remoteAndLocalChanges);
		
		// go through local changes
		for (Status status : localChanges) {
		    
		    // LOCAL status can be everything except:
            //   none/normal          won't be displayed in local changes
		    //   unversioned/missing  set to added/deleted (handled anyway)
            //   merged               only happens on update
		    //   ignored              can be ignored ;-)
		    
		    // LOCAL status can be any one of those:
		    //   modified             the simple ones
		    //   added
		    //   deleted
		    //   replaced
		    //   conflicted           the hard ones
		    //   obstructed
		    //   incomplete (on dir)
		    //   external (only possible with svn client)
		    
		    // REMOTE status can be only the following:
	        //   normal
	        //   modified
	        //   added
	        //   deleted
	        //   unversioned
	        //   replaced (delete and re-add in one step)

		    // local ADD (as we added everything, unversioned shouldn't happen)
			if (status.getTextStatus() == StatusKind.added
					|| status.getTextStatus() == StatusKind.unversioned) {

				// check for existence of a remote version (ADD vs ADD)
				if (remoteAndLocalMap.containsKey(status.getPath())) {
					Status remoteStatus = remoteAndLocalMap.get(status
							.getPath());
					// if the file exists remotely, it will have a URL set
					if (remoteStatus.getUrl() != null) {
						conflicts.addConflict(new AddConflict(status));
					}
				}
				// check for adds in remotely deleted folders (ADD in DELETED)
				for (Status remoteStatus : remoteAndLocalChanges) {
					if ((remoteStatus.getRepositoryTextStatus() == StatusKind.deleted) 
							&& (isParent(remoteStatus.getPath(), status.getPath()))) {
						conflicts.addConflict(new AddInDeletedConflict(status));
					}
				}
				
			// locally DELETED (as we deleted all missing, missing shouldn't happen)
			} else if (status.getTextStatus() == StatusKind.deleted ||
			        status.getTextStatus() == StatusKind.missing) {
			    // collect *all* remote adds/mods and add them to the conflict object
			    List<Status> remoteModList = new ArrayList<Status>();
			    
			    if (status.getNodeKind() == NodeKind.dir) {
			        // FOLDER DELETE vs MODIFIED/ADD inside
                    List<Status> children = getChildren(status.getPath(), remoteAndLocalChanges);
                    for (Status childStatus : children) {
                        // when it's locally gone, it can only be an added or replaced
                        // (deleted is ok as it matches with our delete)
                        if (childStatus.getRepositoryTextStatus() == StatusKind.added ||
                                childStatus.getRepositoryTextStatus() == StatusKind.replaced) {
                            remoteModList.add(childStatus);
                        }
                    }

                    // only if there was any modification found, we have a conflict
                    if (remoteModList.size() > 0) {
                        conflicts.addConflict(new DeleteWithModificationConflict(status, remoteModList));
                    }
			    } else {
			        // TODO: check this with a test case
	                // check for existence of a modified remote version
			        // DELETE vs MODIFIED
	                if (remoteAndLocalMap.containsKey(status.getPath())) {
	                    Status remoteStatus = remoteAndLocalMap.get(status.getPath());
	                    if (remoteStatus.getRepositoryTextStatus() != StatusKind.normal) {
	                        remoteModList.add(remoteStatus);
	                        conflicts.addConflict(new DeleteWithModificationConflict(status, remoteModList));
	                    }
	                }
			    }
			}
		}
		return conflicts;
	}

	private void handleConflictsBeforeUpdate(List<Conflict> conflicts) {
		for (Conflict conflict : conflicts) {
			log.info(">> Before Update: " + conflict.toString());
			conflict.handleBeforeUpdate();
		}
	}

	private void handleConflictsAfterUpdate(List<Conflict> conflicts) {
		for (Conflict conflict : conflicts) {
			log.info(">> After Update: " + conflict.toString());
			conflict.handleAfterUpdate();
		}
	}

    /**
     * Gets all children and grand-children and so on for the path.
     */
    public static List<Status> getChildren(String path, List<Status> remoteAndLocalChanges) {
        List<Status> result = new ArrayList<Status>();
        // FIXME: not the fastest way (iterate over all + isParent for each)
        for (Status s : remoteAndLocalChanges) {
            if (isParent(path, s.getPath())) {
                result.add(s);
            }
        }
        return result;
    }

    /**
     * Checks if the file path 'parentPath' is a filesystem parent of the path
     * 'childPath'. Uses the java File api to check that correctly.
     */
    public static boolean isParent(String parentPath, String childPath) {
        File parent = new File(parentPath);
        File child = new File(childPath);

        while ((child = child.getParentFile()) != null) {
            if (child.equals(parent)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes all status objects that are added and lie below the parent path.
     */
	public static void removeNestedAdds(Status parent, List<Status> localChanges) {
		Iterator<Status> iter = localChanges.iterator();
		while(iter.hasNext()) {
			if(isParent(parent.getPath(), iter.next().getPath())) {
				iter.remove();
			}
		}
	}

	/**
	 * Helper method that stringifies a notify object from the notify callback
	 * of svnkit.
	 */
	public static String notifyToString(NotifyInformation info) {
		return NotifyAction.actionNames[info.getAction()] + " "
				+ info.getPath();
	}
}
