/**
 * 
 * Copyright (c) 2014-2015, Openflexo
 * 
 * This file is part of Flexo-foundation, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * 
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either 
 * version 1.1 of the License, or any later version ), which is available at 
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any 
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 * 
 * You can redistribute it and/or modify under the terms of either of these licenses
 * 
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or 
 *          combining it with software containing parts covered by the terms 
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. * 
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 *
 * See http://www.openflexo.org/license.html for details.
 * 
 * 
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 * 
 */

package org.openflexo.docgenerator;

import java.io.File;
import java.util.logging.Logger;

import org.apache.velocity.app.VelocityEngine;
import org.openflexo.ApplicationContext;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.rm.FileResourceImpl;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;

/**
 * Abstract representation of a document generator using Velocity dedicated to a {@link TechnologyAdapter}
 * 
 */
public abstract class VelocityMasterGenerator<TA extends TechnologyAdapter<TA>> extends DocumentationMasterGenerator<TA> {

	private static final Logger logger = FlexoLogger.getLogger(VelocityMasterGenerator.class.getPackage().getName());

	// The VelocityEngine to use for all generators whose master generator is this
	private VelocityEngine velocityEngine;

	public VelocityMasterGenerator(Class<TA> taClass, String repositoryName, String mainProjectName, ApplicationContext applicationContext) {

		super(taClass, repositoryName, mainProjectName, applicationContext);
		velocityEngine = new VelocityEngine();
		Resource templateResource = ResourceLocator.locateResource(getTemplateDirectoryRelativePath());
		if (templateResource instanceof FileResourceImpl) {
			velocityEngine.setProperty("file.resource.loader.path", ((FileResourceImpl) templateResource).getFile().getAbsolutePath());
		}
	}

	/**
	 * Retrieve path relative to src/main/resources directory where templates are to be found
	 * 
	 * @return
	 */
	public abstract String getTemplateDirectoryRelativePath();

	public VelocityEngine getVelocityEngine() {
		return velocityEngine;
	}

	/**
	 * Return file to be generated for supplied generator (might be null if no file should be written)
	 * 
	 * @param generator
	 * @return
	 */
	public abstract File getFileToBeGenerated(AbstractGenerator<?> generator);

}
