/**
 * 
 */
package com.mindquarry.client;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * @author <a href="mailto:lars@trieloff.net">Lars Trieloff</a>
 *
 */
public class TaskManager {
	private List<Task> tasks = new Vector<Task>();
	private List<TaskListChangeListener> listeners = new Vector<TaskListChangeListener>();
	
	public void startTask(Task t) {
		for (Task task : tasks) {
			task.setActive(false);
		}
		t.setActive(true);
	}
	
	public void addTask(Task t) {
		tasks.add(t);
		startTask(t);
	}

	/**
	 * @param provider
	 */
	public void removeChangeListener(TaskListChangeListener provider) {
		this.listeners.remove(provider);
	}
	
	public void addChangeListener(TaskListChangeListener provider) {
		this.listeners.remove(provider);
	}

	/**
	 * @return
	 */
	public Task[] getTasks() {
		return tasks.toArray(new Task[] {});
	}
}
