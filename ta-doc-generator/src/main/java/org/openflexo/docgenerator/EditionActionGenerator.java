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
import org.openflexo.foundation.fml.editionaction.AssignableAction;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.toolbox.StringUtils;

/**
 * Documentation generator for {@link EditionAction}
 * 
 */
public abstract class EditionActionGenerator<EA extends EditionAction> extends VelocityGenerator<EA> {

	private static final Logger logger = FlexoLogger.getLogger(EditionActionGenerator.class.getPackage().getName());

	private EA ea;

	public EditionActionGenerator(Class<EA> objectClass, VelocityMasterGenerator<?> taDocGenerator) {
		super(objectClass, taDocGenerator);
		ea = getFMLModelFactory().newInstance(getObjectClass());
	}

	@Override
	protected Image getIcon() {
		if (getTechnologyAdapterController().getIconForEditionAction(getObjectClass()) != null) {
			return getTechnologyAdapterController().getIconForEditionAction(getObjectClass()).getImage();
		}
		return null;
	}

	public String getUsage(boolean fullQualified) {
		StringBuffer returned = new StringBuffer();
		if (ea instanceof AssignableAction) {
			returned.append("[" + TypeUtils.simpleRepresentation(((AssignableAction) ea).getAssignableType()) + " <value> =]"
					+ StringUtils.LINE_SEPARATOR);
		}
		if (fullQualified) {
			returned.append(getTechnologyAdapter().getIdentifier() + "::");
		}
		returned.append(getFMLKeyword() + "(");
		boolean hasOptionalProperties = false;
		boolean isFirst = true;
		for (FMLProperty fmlProperty : getFMLProperties()) {
			if (fmlProperty.isRequired()) {
				if (!isFirst) {
					returned.append(",");
				}
				returned.append(fmlProperty.getLabel() + "=<" + fmlProperty.getPathNameInUsage() + ">");
				isFirst = false;
			}
			else {
				hasOptionalProperties = true;
			}
		}
		if (hasOptionalProperties) {
			returned.append("[" + (isFirst ? "" : ",") + "options]");
		}
		returned.append(")");
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
