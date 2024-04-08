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

import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.FMLModelContext.FMLProperty;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.toolbox.StringUtils;

/**
 * Abstract documentation generator for {@link ModelSlot}
 * 
 */
public abstract class ModelSlotGenerator<MS extends ModelSlot<?>> extends AbstractGenerator<MS> {

	private static final Logger logger = FlexoLogger.getLogger(ModelSlotGenerator.class.getPackage().getName());

	private MS ms;

	public ModelSlotGenerator(Class<MS> objectClass, TADocGenerator<?> taDocGenerator) {
		super(objectClass, taDocGenerator);
		ms = getFMLModelFactory().newInstance(getObjectClass());
		generateIconFiles();
	}

	@Override
	protected Image getIcon() {
		return getTechnologyAdapterController().getIconForModelSlot(getObjectClass()).getImage();
	}

	/*public String getUsageAsCode(boolean fullQualified) {
		return toCode(getUsage(fullQualified));
	}*/

	public String getUsage(boolean fullQualified) {
		StringBuffer returned = new StringBuffer();

		int optionalProperties = 0;
		int requiredProperties = 0;
		for (FMLProperty fmlProperty : getFMLProperties()) {
			if (fmlProperty.isRequired()) {
				requiredProperties++;
			}
			else {
				optionalProperties++;
			}
		}

		returned.append("[visibility] [cardinality]" + " ");
		returned.append(TypeUtils.simpleRepresentation(TypeUtils.getRawType(ms.getType())) + " ");
		returned.append("<identifier>" + (requiredProperties > 0 ? StringUtils.LINE_SEPARATOR : " "));
		returned.append("with ");

		if (fullQualified) {
			returned.append(getTechnologyAdapter().getIdentifier() + "::");
		}
		returned.append(getFMLKeyword() + "(");
		boolean isFirst = true;
		for (FMLProperty fmlProperty : getFMLProperties()) {
			if (fmlProperty.isRequired()) {
				if (!isFirst) {
					returned.append(",");
				}
				returned.append(fmlProperty.getLabel() + "=<" + fmlProperty.getPathNameInUsage() + ">");
				isFirst = false;
			}
		}
		if (optionalProperties > 0) {
			returned.append("[" + (isFirst ? "" : ",") + "options]");
		}
		returned.append(");");
		return returned.toString();
	}

}
