/**
 * 
 */
package com.mindquarry.client;

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;

/**
 * @author <a href="mailto:lars@trieloff.net">Lars Trieloff</a>
 *
 */
public class TaskTableCellModifier implements ICellModifier {
	private TableViewer table;
	/**
	 * @param taskTableViewer
	 */
	public TaskTableCellModifier(TableViewer taskTableViewer) {
		this.table = taskTableViewer;
	}

	/**
	 * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
	 */
	public boolean canModify(Object element, String property) {
		return true;
	}

	/**
	 * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object, java.lang.String)
	 */
	public Object getValue(Object element, String property) {
		Task task = (Task) element;
		return new Boolean(task.isActive());
	}

	/**
	 * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object, java.lang.String, java.lang.Object)
	 */
	public void modify(Object element, String property, Object value) {
		// TODO Auto-generated method stub

	}

}
