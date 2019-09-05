package org.eclipse.wildwebdeveloper.debug.firefox;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.ui.actions.ILaunchable;

public class FirefoxLaunchableAdapterFactory implements IAdapterFactory {
	
	private static final ILaunchable DUMMY = new ILaunchable() {
	};

	@Override
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		IResource resource = Adapters.adapt(adaptableObject, IResource.class);
		if (adapterType.equals(ILaunchable.class) && Tester.isHTMLProject(resource.getProject())) {
			return adapterType.cast(DUMMY);
		}
		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return new Class<?>[] { ILaunchable.class };
	}
	

}
