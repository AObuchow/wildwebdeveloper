/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Andrew Obuchowicz (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.xml;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;

public class BundleResolver {

	private BundleResolver() {
		// Utility class, not meant to be instantiated
	}

	public static Bundle getBundle(String bundleID) {
		return Platform.getBundle(bundleID);
	}

	public static File getBundleResource(String bundleID, String resourceName) {
		Bundle bundle = getBundle(bundleID);
		File f = null;
		try {
			f = new java.io.File(FileLocator
					.toFileURL(FileLocator.find(bundle, new Path(bundle.getResource(resourceName).getPath())))
					.getPath());
		} catch (IOException e) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
			return null;
		}

		return f;
	}

}
