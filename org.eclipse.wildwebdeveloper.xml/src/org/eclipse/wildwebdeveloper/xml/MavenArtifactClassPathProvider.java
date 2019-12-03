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

public class MavenArtifactClassPathProvider implements XMLLSClasspathExtensionProvider {

	@Override
	public File get() {
		return BundleResolver.getBundleResource("org.eclipse.m2e.maven.runtime", "/jars/maven-artifact-3.6.1.jar");
	}

}
