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

import java.awt.Image;
import java.util.logging.Logger;

import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.logging.FlexoLogger;

/**
 * Documentation generator for {@link TechnologyAdapter}
 * 
 */
public abstract class TechnologyAdapterGenerator<TA extends TechnologyAdapter<TA>> extends VelocityGenerator<TA> {

	private static final Logger logger = FlexoLogger.getLogger(TechnologyAdapterGenerator.class.getPackage().getName());

	public TechnologyAdapterGenerator(Class<TA> taClass, VelocityMasterGenerator<?> taDocGenerator) {
		super(taClass, taDocGenerator);
	}

	@Override
	protected Image getIcon() {
		return getTechnologyAdapterController().getTechnologyIcon().getImage();
	}

	public String getTechnologyAdapterName() {
		FML annotation = getObjectClass().getAnnotation(FML.class);
		if (annotation != null) {
			return annotation.value();
		}
		return getObjectClass().getSimpleName();
	}

	public String getTechnologyAdapterDescription() {
		FML annotation = getObjectClass().getAnnotation(FML.class);
		if (annotation != null) {
			return annotation.description();
		}
		return "No description available";
	}
}
