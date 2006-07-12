/**
 * 
 */
package com.mindquarry.client;

/**
 * @author <a href="mailto:lars@trieloff.net">Lars Trieloff</a>
 *
 */
public class Task {
	private String title;
	private boolean done;
	private boolean active;
	
	/**
	 * @param string
	 */
	public Task(String string) {
		this.title = string;
		this.done = false;
		this.active = false;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public boolean isDone() {
		return done;
	}
	public void setDone(boolean done) {
		this.done = done;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
}
