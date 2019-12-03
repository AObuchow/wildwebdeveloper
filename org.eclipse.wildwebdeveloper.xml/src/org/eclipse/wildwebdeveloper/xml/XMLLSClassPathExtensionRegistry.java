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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

public class XMLLSClassPathExtensionRegistry {
	private static final String EXTENSION_POINT_ID = Activator.PLUGIN_ID + ".xmllsClasspathExtensionProvider"; //$NON-NLS-1$
	private Map<IConfigurationElement, XMLLSClasspathExtensionProvider> extensions = new HashMap<>();
	private boolean outOfSync = true;

	public XMLLSClassPathExtensionRegistry() {
		Platform.getExtensionRegistry().addRegistryChangeListener((event -> outOfSync = true), EXTENSION_POINT_ID);
	}

	public List<String> getXMLLSClassPathExtensions() {
		if (this.outOfSync) {
			sync();
		}

		// TODO: Maybe a try/catch should be used in the event that the
		// classpathProvider throws an exception?
		return this.extensions.entrySet().stream()
				.map(Entry<IConfigurationElement, XMLLSClasspathExtensionProvider>::getValue)
				.map(XMLLSClasspathExtensionProvider::get).map(File::getAbsolutePath)
				.collect(Collectors.toList());
	}

	private void sync() {
		Set<IConfigurationElement> toRemoveExtensions = new HashSet<>(this.extensions.keySet());
		for (IConfigurationElement extension : Platform.getExtensionRegistry()
				.getConfigurationElementsFor(EXTENSION_POINT_ID)) {
			toRemoveExtensions.remove(extension);
			if (!this.extensions.containsKey(extension)) {
				try {
					// TODO: Consider if we really need to keep the IConfigurationElements
					final Object executableExtension = extension.createExecutableExtension("provider");
					if (executableExtension instanceof XMLLSClasspathExtensionProvider) {
						this.extensions.put(extension, (XMLLSClasspathExtensionProvider) executableExtension);
					}

				} catch (Exception ex) {
					Activator.getDefault().getLog()
							.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, ex.getMessage(), ex));
				}
			}
		}
		for (IConfigurationElement toRemove : toRemoveExtensions) {
			this.extensions.remove(toRemove);
		}
		this.outOfSync = false;
	}

}
