package org.drools.eclipse.core.ui;

import org.drools.eclipse.core.DroolsElement;
import org.drools.eclipse.core.Rule;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Content provider for Drools model elements. 
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class DroolsContentProvider implements ITreeContentProvider {

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof Rule) {
			Rule rule = (Rule) parentElement;
			if (rule.getGroup()==null) {
				return new Object[]{};
			}
			return new Object[]{rule.getGroup()};
		}
		if (parentElement instanceof DroolsElement) {
			return ((DroolsElement) parentElement).getChildren();
		}
		return new Object[0];
	}

	public Object getParent(Object element) {
		if (element instanceof DroolsElement) {
			return ((DroolsElement) element).getParent();
		}
		return null;
	}

	public boolean hasChildren(Object element) {
        return getChildren(element).length > 0;
	}

	public Object[] getElements(Object inputElement) {
        return getChildren(inputElement);
	}

	public void dispose() {
        // do nothing
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // do nothing
	}

}
