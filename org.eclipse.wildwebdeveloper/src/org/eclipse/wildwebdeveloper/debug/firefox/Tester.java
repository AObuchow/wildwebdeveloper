package org.eclipse.wildwebdeveloper.debug.firefox;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;

public class Tester extends PropertyTester {
	private static final String PROPERTY_NAME = "isHTMLProject"; //$NON-NLS-1$
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (property.equals(PROPERTY_NAME)) {
			IResource resource = toResource(receiver);
			if (resource == null) {
				return false;
			}
			return isHTMLProject(resource.getProject());
		}
		return false;
	}

	private IResource toResource(Object o) {
		if (o instanceof IResource) {
			return (IResource) o;
		} else if (o instanceof IAdaptable) {
			return ((IAdaptable) o).getAdapter(IResource.class);
		} else {
			return null;
		}
	}

	public static boolean isHTMLProject(IProject p) {
		if (p == null || !p.isAccessible()) {
			return false;
		}
		try {
			for (IResource projItem : p.members()) {
				if (projItem.getName().equals("index.html")) { //$NON-NLS-1$ 
					return true;
				}
			}
		} catch (CoreException e) {
		}
		return false;
	}
}