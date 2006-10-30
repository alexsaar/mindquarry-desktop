/**
 * Copyright (C) 2005 MindQuarry GmbH, All Rights Reserved
 */
package com.mindquarry.client.task;

/**
 * @author <a href="mailto:lars(dot)trieloff(at)mindquarry(dot)com">Lars
 *         Trieloff</a>
 */
public class Task {
    private String id;

    private String teamspace;

    private String title;
    
    private String status;

    private boolean active;

    public Task() {
        active = false;
    }

    public Task(String id, String teamspace, String title, String status) {
        this.id = id;
        this.teamspace = teamspace;
        this.title = title;
        this.status = status;
        active = false;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTeamspace() {
        return teamspace;
    }

    public void setTeamspace(String teamspace) {
        this.teamspace = teamspace;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
