/**
 * 
 */
package com.mindquarry.client;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * @author <a href="mailto:lars@trieloff.net">Lars Trieloff</a>
 *
 */
public class TaskTableLabelProvider extends org.eclipse.jface.viewers.LabelProvider implements ITableLabelProvider {

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof Task) {
			Task task = (Task) element;
			switch (columnIndex) {
			case 0: break;
			case 1: return task.getTitle();
			case 2: break;
			}
		}
		return null;
	}

}
