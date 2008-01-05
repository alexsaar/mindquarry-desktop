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
package com.mindquarry.desktop.workspace.conflict;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.PropertyData;
import org.tigris.subversion.javahl.Status;
import org.tigris.subversion.javahl.StatusKind;

import com.mindquarry.desktop.I18N;
import com.mindquarry.desktop.workspace.exception.CancelException;

/**
 * Conflicting changes to a property of an versioned object.
 * 
 * @author <a href="mailto:victor(dot)saar(at)mindquarry(dot)com">Victor Saar</a>
 */
public class PropertyConflict extends Conflict {
	private Action action = Action.UNKNOWN;
	
	public enum Action {
        /**
         * Indicating no conflict solution action was chosen yet.
         */
		UNKNOWN,
		/**
		 * Property conflict is resolved automatically (e.g. for svn:ignore, svn:externals).
		 */
		RESOLVE_AUTOMATICALLY,
		/**
		 * Use the locally defined property value.
		 */
		USE_LOCAL_VALUE,
        /**
         * Use the remotely defined property value.
         */
        USE_REMOTE_VALUE,
        /**
         * Use a completely new value.
         */
        USE_NEW_VALUE;
	}
	
	private String value = new String("");
    
    private PropertyData localProp;
    private PropertyData remoteProp;
    
	public PropertyConflict(Status status, PropertyData localProp, PropertyData remoteProp, boolean resolveAutomatically) {
		super(status);
        
        this.localProp = localProp;
        this.remoteProp = remoteProp;
        
        if(resolveAutomatically) {
            this.action = Action.RESOLVE_AUTOMATICALLY;
            this.value = mergeMultiLineProperties(localProp, remoteProp);
        }
	}

    private String mergeMultiLineProperties(PropertyData localProp, PropertyData remoteProp) {
        log.info("local property value is: " + localProp.getValue());
        log.info("remote property value is: " + remoteProp.getValue());
        
        List<String> remoteValues = Arrays.asList(remoteProp.getValue().split("\\n|\\r\\n"));
        List<String> mergedValues = new ArrayList<String>();
        mergedValues.addAll(Arrays.asList(localProp.getValue().split("\\n|\\r\\n")));

        for(String value : remoteValues) {
            if(!mergedValues.contains(value)) {
                mergedValues.add(value);
            }
        }
        
        StringBuffer buffer = new StringBuffer();
        
        for(String value : mergedValues) {
            buffer.append(value);
            buffer.append("\n");
        }
        
        log.info("merged value: " + buffer.toString());
        
        return buffer.toString();
    }

	public void afterUpdate() throws ClientException {
        switch (action) {
        case UNKNOWN:
            // client did not set a conflict resolution
            log.error("PropertyConflict with no action set: " + status.getPath());
            break;
            
        case RESOLVE_AUTOMATICALLY:
            log.info("property conflict resolved automatically for " + status.getPath());
            break;
            
        case USE_LOCAL_VALUE:
            log.info("using local value for property \"" + localProp.getName() + "\" of " + status.getPath());
            value = localProp.getValue();
            break;
        
        case USE_REMOTE_VALUE:
            log.info("using remote value for property \"" + remoteProp.getName() + "\" of " + status.getPath());
            value = remoteProp.getValue();
            break;
            
        case USE_NEW_VALUE:
            log.info("using new value \"" + value + "\" for property \"" + remoteProp.getName() + "\" of " + status.getPath());
            break;
        }

        client.propertySet(status.getPath(), localProp.getName(), value, false);
        
        if(status.getTextStatus() != StatusKind.conflicted) {
            client.resolved(status.getPath(), false);
        }
	}

	public void accept(ConflictHandler handler) throws CancelException {
        if(action == Action.UNKNOWN)
            handler.handle(this);
	}
	
	/**
	 * Resolves the conflict by using the local property value and throwing away
	 * the remote value.
	 */
	public void doUseLocalValue() {
		this.action = Action.USE_LOCAL_VALUE;
        this.value = localProp.getValue();
	}
	
    /**
     * Resolves the conflict by using the remote property value and throwing
     * away the local value.
     */
    public void doUseRemoteValue() {
        this.action = Action.USE_REMOTE_VALUE;
        this.value = remoteProp.getValue();
    }
    
    /**
     * Resolves the conflict by setting a new value (eg. given by the user).
     * @param value the new value for the property to use
     */
    public void doUseNewValue(String value) {
        this.action = Action.USE_NEW_VALUE;
        this.value = value;
    }
    
	public String toString() {
		return "Property Conflict: " + status.getPath() + (action == Action.UNKNOWN ? "" : " " + action.name());
	}

    public PropertyData getLocalProperty() {
        return localProp;
    }

    public PropertyData getRemoteProperty() {
        return remoteProp;
    }

    public String getLongDescription() {
        if (file.isDirectory()) {
            return I18N.get("This directory's properties have been " +
            		"modified locally and on the server.");
        } else {
            return I18N.get("This files's properties have been " +
                    "modified locally and on the server.");
        }
    }
   
}
