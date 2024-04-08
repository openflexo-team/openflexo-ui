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

import javax.swing.ImageIcon;

import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.FMLModelContext.FMLProperty;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.toolbox.StringUtils;

/**
 * Documentation generator for {@link FlexoRole}
 * 
 */
public abstract class FlexoRoleGenerator<R extends FlexoRole<?>> extends AbstractGenerator<R> {

	private static final Logger logger = FlexoLogger.getLogger(FlexoRoleGenerator.class.getPackage().getName());

	private R role;

	public FlexoRoleGenerator(Class<R> objectClass, TADocGenerator<?> taDocGenerator) {
		super(objectClass, taDocGenerator);
		role = getFMLModelFactory().newInstance(getObjectClass());
		generateIconFiles();
	}

	@Override
	protected Image getIcon() {
		System.out.println("getTechnologyAdapterController()=" + getTechnologyAdapterController());

		ImageIcon icon = getTechnologyAdapterController().getIconForFlexoRole(getObjectClass());
		if (icon != null) {
			return icon.getImage();
		}
		return null;
	}

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
		returned.append(TypeUtils.simpleRepresentation(TypeUtils.getRawType(role.getType())) + " ");
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

	// @formatter:off	
	/*
	append(dynamicContents(() -> getVisibilityAsString(getModelObject().getVisibility()), SPACE), getVisibilityFragment());
	append(dynamicContents(() -> serializeType(getModelObject().getType())), getTypeFragment());
	append(dynamicContents(() -> serializeCardinality(getModelObject().getCardinality())), getCardinalityFragment());
	append(dynamicContents(SPACE,() -> getModelObject().getName(), SPACE), getNameFragment());
	append(staticContents("", "with", SPACE), getWithFragment());
	when(() -> isFullQualified())
	.thenAppend(dynamicContents(() -> getFMLFactory().serializeTAId(getModelObject())), getTaIdFragment())
	.thenAppend(staticContents("::"), getColonColonFragment());
	append(dynamicContents(() -> serializeFlexoRoleName(getModelObject())), getRoleFragment());
	when(() -> hasFMLProperties())
	.thenAppend(staticContents("("), getFMLParametersLParFragment())
	.thenAppend(childrenContents("","", () -> getModelObject().getFMLPropertyValues(getFactory()), ", ","", Indentation.DoNotIndent,
			FMLPropertyValue.class))
	.thenAppend(staticContents(")"), getFMLParametersRParFragment());
	append(staticContents(";"), getSemiFragment());
	 */
	// @formatter:on	

}
