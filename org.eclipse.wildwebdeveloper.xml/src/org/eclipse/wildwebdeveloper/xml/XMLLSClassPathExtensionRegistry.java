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
import java.util.ArrayList;
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
	private Map<IConfigurationElement, XMLLSClasspathExtensionProviders> listExtensions = new HashMap<>();
	private Map<IConfigurationElement, XMLLSClasspathExtensionProvider> singleExtensions = new HashMap<>();
	private boolean outOfSync = true;

	public XMLLSClassPathExtensionRegistry() {
		Platform.getExtensionRegistry().addRegistryChangeListener((event -> outOfSync = true), EXTENSION_POINT_ID);
	}

	public List<String> getXMLLSClassPathExtensions() {
		if (this.outOfSync) {
			sync();
		}

		List<String> classpathExtensions = new ArrayList<>();
		this.listExtensions.entrySet().stream()
				.map(Entry<IConfigurationElement, XMLLSClasspathExtensionProviders>::getValue)
				.map(XMLLSClasspathExtensionProviders::get)
				.forEach(list -> list.forEach(jar -> classpathExtensions.add(jar.getAbsolutePath())));

		classpathExtensions.addAll(this.singleExtensions.entrySet().stream()
				.map(Entry<IConfigurationElement, XMLLSClasspathExtensionProvider>::getValue)
				.map(XMLLSClasspathExtensionProvider::get).map(File::getAbsolutePath).collect(Collectors.toList()));
		return classpathExtensions;
	}

	private void sync() {
		Set<IConfigurationElement> toRemoveExtensions = new HashSet<>(this.listExtensions.keySet());
		for (IConfigurationElement extension : Platform.getExtensionRegistry()
				.getConfigurationElementsFor(EXTENSION_POINT_ID)) {
			toRemoveExtensions.remove(extension);
			if (!this.listExtensions.containsKey(extension)) {
				try {
					// NOTE: using an else-if means that an extension must contribute the providers
					// attribute XOR provider attribute
					if (extension.getAttribute("providers") != null) {
						final Object executableExtension = extension.createExecutableExtension("providers");
						if (executableExtension instanceof XMLLSClasspathExtensionProviders) {
							this.listExtensions.put(extension, (XMLLSClasspathExtensionProviders) executableExtension);
						}
					} else if (extension.getAttribute("provider") != null) {
						final Object executableExtension = extension.createExecutableExtension("provider");
						if (executableExtension instanceof XMLLSClasspathExtensionProvider) {
							this.singleExtensions.put(extension, (XMLLSClasspathExtensionProvider) executableExtension);
						}
					}
				} catch (Exception ex) {
					Activator.getDefault().getLog()
							.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, ex.getMessage(), ex));

				}
			}
		}
		for (IConfigurationElement toRemove : toRemoveExtensions) {
			// TODO: Now that we're dealing with two lists of extensions, this code needs to
			// take that into consideration
			this.listExtensions.remove(toRemove);
		}
		this.outOfSync = false;
	}

}
