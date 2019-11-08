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

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

public class XMLExtensionRegistry {

	private static final String EXTENSION_POINT_ID = Activator.PLUGIN_ID + ".xmllsExtension"; //$NON-NLS-1$
	private Map<IConfigurationElement, String> extensions = new HashMap<>();
	private boolean outOfSync = true;

	public XMLExtensionRegistry() {
		Platform.getExtensionRegistry().addRegistryChangeListener((event -> outOfSync = true), EXTENSION_POINT_ID);
	}

	/**
	 * Returns the XML extension jar paths relative to the root of their
	 * contributing plug-in.
	 *
	 * @return List of XML extension jar paths (relative to the root of their
	 *         contributing plug-in).
	 */
	public List<String> getXMLExtensionJars() {
		if (this.outOfSync) {
			sync();
		}
		// Filter for .jar files and retrieve their paths relative to their contributing
		// plug-in
		return this.extensions.entrySet().stream().filter(extension -> extension.getValue().endsWith(".jar"))
				.map(extension -> {
					try {
						return new java.io.File(FileLocator.toFileURL(
								FileLocator.find(Platform.getBundle(extension.getKey().getContributor().getName()),
										new Path(extension.getValue())))
								.getPath()).getAbsolutePath();
					} catch (InvalidRegistryObjectException | IOException e) {
						Activator.getDefault().getLog()
								.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
						return null;
					}
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	private void sync() {
		Set<IConfigurationElement> toRemoveExtensions = new HashSet<>(this.extensions.keySet());
		for (IConfigurationElement extension : Platform.getExtensionRegistry()
				.getConfigurationElementsFor(EXTENSION_POINT_ID)) {
			toRemoveExtensions.remove(extension);
			if (!this.extensions.containsKey(extension)) {
				try {
					this.extensions.put(extension, extension.getAttribute("path"));
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
