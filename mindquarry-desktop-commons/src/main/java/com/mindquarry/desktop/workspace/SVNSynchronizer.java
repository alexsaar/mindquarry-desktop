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
import java.io.IOException;
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
import org.tigris.subversion.javahl.Revision;
import org.tigris.subversion.javahl.Status;
import org.tigris.subversion.javahl.StatusKind;
import org.tigris.subversion.javahl.Status.Kind;
import org.tmatesoft.svn.core.javahl.SVNClientImpl;

import com.mindquarry.desktop.util.RelativePath;
import com.mindquarry.desktop.workspace.conflict.AddConflict;
import com.mindquarry.desktop.workspace.conflict.Conflict;
import com.mindquarry.desktop.workspace.conflict.ConflictHandler;
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

    protected File localPathFile;
    
	protected String username;

	protected String password;

	protected SVNClientImpl client;
	
	protected ConflictHandler handler;

	/**
	 * Constructor for SVNSynchronizer that has all mandatory fields as
	 * parameter.
	 * 
	 * @param repositoryURL URL of the central SVN repository
	 * @param localPath local working copy path (typically the root of the wc)
	 * @param username subversion username
	 * @param password subversion password
	 * @param handler callback handler to resolve conflicts in the GUI
	 */
	public SVNSynchronizer(String repositoryURL, String localPath,
			String username, String password,
			ConflictHandler handler) {
		this.repositoryURL = repositoryURL;
		this.localPath = localPath;
		this.username = username;
		this.password = password;
		this.handler = handler;
		
		this.localPathFile = new File(localPath);
		
		if (handler == null) {
		    throw new NullPointerException("Constructor parameter ConflictHandler handler cannot be null");
		}

		// create SVN client, set authentication info
		client = SVNClientImpl.newInstance();
		if (username != null) {
			client.username(username);
			if (password != null) {
				client.password(password);
			}
		}
	}
	
	/**
	 * Sets an optional notify listener to get notifications directly from the
	 * svn client upon update and commit.
	 */
	public void setNotifyListener(Notify2 notifyListener) {
        // register for svn notifications on update and commit
        client.notification2(notifyListener);
	}
	
	/**
	 * Central method: will do a full synchronization, including update and
	 * commit. During that the ConflictHandler will be asked
	 */
	public void synchronize() {
		try {
			client.cleanup(localPath);
			// TODO: enable locking
			// client.lock(new String[] {localPath}, "locking for
			// synchronization", false);

            // no need to call svn del and add manually for the user
            deleteMissingAndAddUnversioned();
            
			List<Status> remoteAndLocalChanges = getRemoteAndLocalChanges();

            // with UI callback, cancel possible
			List<Conflict> conflicts =
			    analyzeChangesAndAskUser(remoteAndLocalChanges);
			
			handleConflictsBeforeUpdate(conflicts);
			// here something goes over the wire
			client.update(localPath, Revision.HEAD, true);
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
			if (e.getCause() != null) {
			    e.getCause().printStackTrace();
			}
			throw new RuntimeException("synchronized failed", e);
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

		// we need a modifiable list - Arrays.asList is fixed
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
	 * contain the remote change of the same path. The list will be sorted
	 * from top to down.
	 */
	public List<Status> getRemoteAndLocalChanges() throws ClientException {
		log.info("## remote changes:");

        // we need a modifiable list - Arrays.asList is fixed
        List<Status> statusList = new ArrayList<Status>(); 
        statusList.addAll(
		    Arrays.asList(client.status(localPath, true, true, false)));

        // sort the list from top-level folder to bottom which is important
        // for handling multiple conflicts on the parent folder first
        Collections.sort(statusList, new Comparator<Status>() {
            public int compare(Status left, Status right) {
                return left.getPath().compareTo(right.getPath());
            }
        });
        
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
    
    private void presentNewConflict(Conflict conflict, List<Conflict> conflicts) throws CancelException {
        conflict.setSVNClient(client);
        
        log.info("-----------------------------------------------------------");
        log.info("## Found conflict: " + conflict.toString());
        // resolve it, ask the user
        conflict.accept(handler);
        
        conflicts.add(conflict);
    }
    
    /**
     * Important method that looks out for any structure conflicts before an
     * update and creates {@link Conflict} objects for those. Upon each conflict
     * found, the user is asked to resolve it.
     * 
     * If the user cancels during the conflict resolving, a CancelException is
     * thrown.
     */
	private List<Conflict> analyzeChangesAndAskUser(List<Status> remoteAndLocalChanges) throws CancelException {
		List<Conflict> conflicts = new ArrayList<Conflict>();

        // LOCAL status can be everything except:
        //   none/normal          won't be displayed in local changes
        //   unversioned/missing  set to added/deleted (handled anyway)
        //   merged               only happens on update
        //   ignored              can be ignored ;-)
        //   incomplete (on dir)  missing files are set to deleted
        
        // LOCAL status can be any one of those:
        // simple ones:
        //   modified
        //   added
        //   deleted
        //   replaced (only possible with svn client)
        // hard ones:
        //   conflicted
        //   obstructed (eg. deleted file, created dir with same name)
        //   external (only possible with svn client)
        
        // REMOTE status can be only the following:
        //   normal
        //   modified
        //   added
        //   deleted
        //   unversioned
        //   replaced (delete and re-add in one step)
		
		for (Status s : remoteAndLocalChanges) {
		    log.debug("analyzing " + Kind.getDescription(s.getTextStatus()) +
		            " " + nodeKindDesc(s.getNodeKind()) +
                    " <->" +
		            " " + Kind.getDescription(s.getRepositoryTextStatus()) +
		            " " + nodeKindDesc(s.getReposKind()) +
		            " '" + wcPath(s) + "'");
		}
		
		conflicts.addAll(findAllAddConflicts(remoteAndLocalChanges));
		
		conflicts.addAll(findAllLocalContainerDeleteConflicts(remoteAndLocalChanges));
		
        conflicts.addAll(findAllRemoteContainerDeleteConflicts(remoteAndLocalChanges));
        
        conflicts.addAll(findAllDeleteModifiedConflicts(remoteAndLocalChanges));
        
        conflicts.addAll(findAllModifiedDeleteConflicts(remoteAndLocalChanges));
        
        // TODO: following conflicts are also imaginable:
        // local          remote                        notes
        // --------------+-----------------------------+---------------------------------------
        // conflict       deleted/modified/replaced     when file conflict was not yet resolved
        // replaced       replaced...
        // ....
        
		return conflicts;
	}

	/**
	 * Finds all Add/Add conflicts, including file/file, file/dir, dir/file and
	 * dir/dir conflicts.
	 */
    private List<Conflict> findAllAddConflicts(List<Status> remoteAndLocalChanges) throws CancelException {
        List<Conflict> conflicts = new ArrayList<Conflict>();
        
        Iterator<Status> iter = remoteAndLocalChanges.iterator();
        
        while (iter.hasNext()) {
            Status status = iter.next();
            
            // local ADD (as we added everything, unversioned shouldn't happen)
            if (status.getTextStatus() == StatusKind.added
                    || status.getTextStatus() == StatusKind.unversioned) {

                // if remote ADD
                if (status.getRepositoryTextStatus() == StatusKind.added) {
                    Status conflictParent = status;
                    // we remove all files/stati connected to this conflict
                    iter.remove();
                    
                    List<Status> localAdded = new ArrayList<Status>();
                    List<Status> remoteAdded = new ArrayList<Status>();
                    
                    // find all children (locally and remotely)
                    while (iter.hasNext()) {
                        status = iter.next();
                        if (isParent(conflictParent.getPath(), status.getPath())) {
                            if (status.getTextStatus() == StatusKind.added) {
                                localAdded.add(status);
                            } else if (status.getRepositoryTextStatus() == StatusKind.added) {
                                remoteAdded.add(status);
                            }
                            iter.remove();
                        } else {
                            // no more children found, this conflict is done
                            // reset global iterator for next conflict search
                            iter = remoteAndLocalChanges.iterator();
                            break;
                        }
                    }
                    
                    presentNewConflict(new AddConflict(conflictParent, localAdded, remoteAdded), conflicts);
                }
            }
        }
        
        return conflicts;
    }

    /**
     * Finds all conflicts where a local folder delete conflicts with remotely
     * added or modified files in that directory.
     */
    private List<Conflict> findAllLocalContainerDeleteConflicts(List<Status> remoteAndLocalChanges) throws CancelException {
        List<Conflict> conflicts = new ArrayList<Conflict>();
        
        Iterator<Status> iter = remoteAndLocalChanges.iterator();
        
        while (iter.hasNext()) {
            Status status = iter.next();
            
            // DELETED DIRECTORIES (locally)
            if (status.getNodeKind() == NodeKind.dir &&
                    status.getTextStatus() == StatusKind.deleted) {
                
                // conflict if there is a child that is added or removed remotely
                
                Status conflictParent = status;
                List<Status> remoteModList = new ArrayList<Status>();
                
                // find all children
                while (iter.hasNext()) {
                    status = iter.next();
                    if (isParent(conflictParent.getPath(), status.getPath())) {
                        if (status.getRepositoryTextStatus() == StatusKind.added ||
                                status.getRepositoryTextStatus() == StatusKind.replaced ||
                                status.getRepositoryTextStatus() == StatusKind.modified) {
                            remoteModList.add(status);
                        }
                        iter.remove();
                    } else {
                        // no more children found, this conflict is done
                        break;
                    }
                }
                
                if (remoteModList.size() > 0) {
                    // also remove the deleted folder status object
                    remoteAndLocalChanges.remove(conflictParent);
                    
                    presentNewConflict(new DeleteWithModificationConflict(true, conflictParent, remoteModList), conflicts);
                }
                
                // reset global iterator for next conflict search
                iter = remoteAndLocalChanges.iterator();
            }
        }
        
        return conflicts;
    }

    /**
     * Finds all conflicts where a remote folder delete conflicts with locally
     * added or modified files in that directory.
     */
    private List<Conflict> findAllRemoteContainerDeleteConflicts(List<Status> remoteAndLocalChanges) throws CancelException {
        List<Conflict> conflicts = new ArrayList<Conflict>();
        
        Iterator<Status> iter = remoteAndLocalChanges.iterator();
        
        while (iter.hasNext()) {
            Status status = iter.next();
            
            // DELETED DIRECTORIES (remotely)
            if (status.getNodeKind() == NodeKind.dir &&
                    status.getRepositoryTextStatus() == StatusKind.deleted) {
                
                // conflict if there is a child that is added or removed locally
                
                Status conflictParent = status;
                List<Status> localModList = new ArrayList<Status>();
                
                // find all children
                while (iter.hasNext()) {
                    status = iter.next();
                    if (isParent(conflictParent.getPath(), status.getPath())) {
                        if (status.getTextStatus() == StatusKind.added ||
                                status.getTextStatus() == StatusKind.replaced ||
                                status.getTextStatus() == StatusKind.modified) {
                            localModList.add(status);
                        }
                        iter.remove();
                    } else {
                        // no more children found, this conflict is done
                        break;
                    }
                }
                
                if (localModList.size() > 0) {
                    // also remove the deleted folder status object
                    remoteAndLocalChanges.remove(conflictParent);
                    
                    presentNewConflict(new DeleteWithModificationConflict(false, conflictParent, localModList), conflicts);
                }
                
                // reset global iterator for next conflict search
                iter = remoteAndLocalChanges.iterator();
            }
        }
        
        return conflicts;
    }

    private List<Conflict> findAllDeleteModifiedConflicts(List<Status> remoteAndLocalChanges) throws CancelException {
        List<Conflict> conflicts = new ArrayList<Conflict>();
        // TODO Auto-generated method stub
        return conflicts;
    }

    private List<Conflict> findAllModifiedDeleteConflicts(List<Status> remoteAndLocalChanges) throws CancelException {
        List<Conflict> conflicts = new ArrayList<Conflict>();
        // TODO Auto-generated method stub
        return conflicts;
    }

    /**
	 * Calls {@link Conflict.handleBeforeUpdate} on all conflicts in the list.
	 */
	private void handleConflictsBeforeUpdate(List<Conflict> conflicts) throws ClientException {
		for (Conflict conflict : conflicts) {
			log.info(">> Before Update: " + conflict.toString());
			conflict.handleBeforeUpdate();
		}
	}

    /**
     * Calls {@link Conflict.handleAfterUpdate} on all conflicts in the list.
     */
	private void handleConflictsAfterUpdate(List<Conflict> conflicts) {
		for (Conflict conflict : conflicts) {
			log.info(">> After Update: " + conflict.toString());
			conflict.handleAfterUpdate();
		}
	}

	/**
	 * Returns the relative path in the working copy of the status object (for
	 * shorter strings in log output).
	 */
    private String wcPath(Status status) {
        return RelativePath.getRelativePath(localPathFile, new File(status.getPath()));
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
//	public static void removeNestedAdds(Status parent, List<Status> localChanges) {
//		Iterator<Status> iter = localChanges.iterator();
//		while(iter.hasNext()) {
//			if(isParent(parent.getPath(), iter.next().getPath())) {
//				iter.remove();
//			}
//		}
//	}

	/**
	 * Helper method that stringifies a notify object from the notify callback
	 * of svnkit.
	 */
	public static String notifyToString(NotifyInformation info) {
		return NotifyAction.actionNames[info.getAction()] + " "
				+ info.getPath();
	}

	/**
	 * Returns the given nodekind int value as a human-readable string (eg.
	 * "file" or "dir").
	 */
    public static String nodeKindDesc(int nodeKind) {
        if (nodeKind == NodeKind.file) {
            return "file";
        } else if (nodeKind == NodeKind.dir) {
            return "dir";
        } else if (nodeKind == NodeKind.none) {
            return "none";
        } else {
            return "unknown";
        }
    }

}
