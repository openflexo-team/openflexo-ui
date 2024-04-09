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

package org.openflexo.docgenerator.md;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.docgenerator.AbstractGenerator;
import org.openflexo.docgenerator.ModelSlotGenerator;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.editionaction.FetchRequest;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.toolbox.FileUtils;
import org.openflexo.toolbox.StringUtils;

/**
 * Documentation generator for {@link ModelSlot}
 * 
 */
public class MDModelSlotGenerator<MS extends ModelSlot<?>> extends ModelSlotGenerator<MS> {

	private static final Logger logger = FlexoLogger.getLogger(MDModelSlotGenerator.class.getPackage().getName());

	private MS ms;

	public MDModelSlotGenerator(Class<MS> objectClass, MDTADocGenerator<?> taDocGenerator) {
		super(objectClass, taDocGenerator);
		ms = getFMLModelFactory().newInstance(getObjectClass());
	}

	@Override
	public MDTADocGenerator<?> getMasterGenerator() {
		return (MDTADocGenerator<?>) super.getMasterGenerator();
	}

	public String toMD(String text) {
		return getMasterGenerator().toMD(text);
	}

	public File getMDDir() {
		return getMasterGenerator().getMDDir();
	}

	@Override
	public String getTemplateName() {
		return "ModelSlot.md";
	}

	@Override
	protected Image getIcon() {
		return getTechnologyAdapterController().getIconForModelSlot(getObjectClass()).getImage();
	}

	@Override
	public String generate() {
		String returned = super.generate();
		if (getObjectClass().getName().contains("TypedDiagramModelSlot")) {
			System.exit(-1);
		}
		return returned;
	}

	/*@Override
	public void generate() {
	
		generateIconFiles();
	
		generateRolesFile();
		generateBehavioursFile();
		generateEditionActionsFile();
		generateFetchRequestsFile();
	
		StringBuffer sb = new StringBuffer();
	
		sb.append("# " + getFMLKeyword());
		sb.append(StringUtils.LINE_SEPARATOR);
	
		sb.append("[//]: # (Do not edit this file, which is automatically generated)" + StringUtils.LINE_SEPARATOR);
		sb.append(StringUtils.LINE_SEPARATOR);
	
		sb.append(getBigIconAsHTML() + " " + getFMLDescription() + StringUtils.LINE_SEPARATOR);
		sb.append(StringUtils.LINE_SEPARATOR);
	
		sb.append(StringUtils.LINE_SEPARATOR);
		sb.append("---" + StringUtils.LINE_SEPARATOR);
		sb.append(StringUtils.LINE_SEPARATOR);
	
		sb.append("## Usage" + StringUtils.LINE_SEPARATOR);
		sb.append(StringUtils.LINE_SEPARATOR);
	
		sb.append("```java" + StringUtils.LINE_SEPARATOR);
		sb.append(toCode(getUsage(false)) + StringUtils.LINE_SEPARATOR);
		sb.append("```" + StringUtils.LINE_SEPARATOR);
		sb.append(StringUtils.LINE_SEPARATOR);
	
		sb.append("or" + StringUtils.LINE_SEPARATOR);
		sb.append(StringUtils.LINE_SEPARATOR);
	
		sb.append("```java" + StringUtils.LINE_SEPARATOR);
		sb.append(toCode(getUsage(true)) + StringUtils.LINE_SEPARATOR);
		sb.append("```" + StringUtils.LINE_SEPARATOR);
		sb.append(StringUtils.LINE_SEPARATOR);
	
		sb.append("where" + StringUtils.LINE_SEPARATOR);
		sb.append(StringUtils.LINE_SEPARATOR);
	
		sb.append("- `visibility` is default (unspecified), 'public', 'protected' or 'private'" + StringUtils.LINE_SEPARATOR);
		sb.append("- `cardinality` is \\[0,1\\] (unspecified), \\[0,\\*\\] or \\[1,\\*\\]" + StringUtils.LINE_SEPARATOR);
		sb.append("- \\<identifier\\> is the name of declared model slot variable" + StringUtils.LINE_SEPARATOR);
	
		for (FMLProperty fmlProperty : getFMLProperties()) {
			if (fmlProperty.isRequired()) {
				Type propertyType = fmlProperty.getModelProperty().getGetterMethod().getGenericReturnType();
				if (propertyType instanceof ParameterizedType
						&& ((ParameterizedType) propertyType).getRawType().equals(DataBinding.class)) {
					Type argType = ((ParameterizedType) propertyType).getActualTypeArguments()[0];
					sb.append("- \\<" + fmlProperty.getPathNameInUsage() + "\\>");
					sb.append(" addresses a `" + TypeUtils.simpleRepresentation(argType) + "`" + StringUtils.LINE_SEPARATOR);
				}
				else {
					sb.append("- \\<" + fmlProperty.getPathNameInUsage() + "\\>");
					sb.append(" addresses a `" + TypeUtils.simpleRepresentation(propertyType) + "`" + StringUtils.LINE_SEPARATOR);
				}
			}
		}
		sb.append(StringUtils.LINE_SEPARATOR);
	
		if (getFMLProperties().size() > 0) {
	
			sb.append("---" + StringUtils.LINE_SEPARATOR);
			sb.append(StringUtils.LINE_SEPARATOR);
	
			sb.append("## Configuration" + StringUtils.LINE_SEPARATOR);
			sb.append(StringUtils.LINE_SEPARATOR);
	
			sb.append("| Property        | Type                    | &nbsp;Required&nbsp;  |" + StringUtils.LINE_SEPARATOR);
			sb.append("| --------------- |-------------------------| :------:|" + StringUtils.LINE_SEPARATOR);
	
			for (FMLProperty fmlProperty : getFMLProperties()) {
				String propertyName = fmlProperty.getLabel();
				String propertyType;
				Type pType = fmlProperty.getModelProperty().getGetterMethod().getGenericReturnType();
				if (pType instanceof ParameterizedType && ((ParameterizedType) pType).getRawType().equals(DataBinding.class)) {
					Type argType = ((ParameterizedType) pType).getActualTypeArguments()[0];
					propertyType = TypeUtils.simpleRepresentation(argType);
				}
				else {
					propertyType = TypeUtils.simpleRepresentation(pType);
				}
				sb.append("| `" + propertyName + "` &nbsp; | `" + propertyType + "` &nbsp; | " + (fmlProperty.isRequired() ? "yes" : "no")
						+ " |" + StringUtils.LINE_SEPARATOR);
			}
			sb.append(StringUtils.LINE_SEPARATOR);
	
			sb.append("---" + StringUtils.LINE_SEPARATOR);
			sb.append(StringUtils.LINE_SEPARATOR);
	
			for (FMLProperty fmlProperty : getFMLProperties()) {
				String propertyName = fmlProperty.getLabel();
				sb.append("- `" + propertyName + "` : " + toMD(fmlProperty.getDescription()) + StringUtils.LINE_SEPARATOR);
			}
	
			sb.append(StringUtils.LINE_SEPARATOR);
			sb.append("---" + StringUtils.LINE_SEPARATOR);
			sb.append(StringUtils.LINE_SEPARATOR);
		}
	
		if (getFMLExamples().size() > 0) {
			sb.append("## Examples" + StringUtils.LINE_SEPARATOR);
			sb.append(StringUtils.LINE_SEPARATOR);
	
			for (UsageExample usageExample : getFMLExamples()) {
	
				sb.append("```java" + StringUtils.LINE_SEPARATOR);
				sb.append(toCode(usageExample.example()) + StringUtils.LINE_SEPARATOR);
				sb.append("```" + StringUtils.LINE_SEPARATOR);
				sb.append(StringUtils.LINE_SEPARATOR);
				sb.append(toMD(usageExample.description()) + StringUtils.LINE_SEPARATOR);
				sb.append(StringUtils.LINE_SEPARATOR);
	
			}
	
			sb.append(StringUtils.LINE_SEPARATOR);
			sb.append("---" + StringUtils.LINE_SEPARATOR);
			sb.append(StringUtils.LINE_SEPARATOR);
		}
	
		List<Class<? extends FlexoRole<?>>> availableFlexoRoleTypes = getTechnologyAdapterService()
				.getAvailableFlexoRoleTypes(getObjectClass());
		if (availableFlexoRoleTypes.size() > 0) {
			sb.append("## Roles" + StringUtils.LINE_SEPARATOR);
			sb.append(StringUtils.LINE_SEPARATOR);
			for (Class<? extends FlexoRole<?>> roleClass : availableFlexoRoleTypes) {
				sb.append(getHTMLReference(roleClass) + StringUtils.LINE_SEPARATOR);
			}
			sb.append(StringUtils.LINE_SEPARATOR);
			sb.append("---" + StringUtils.LINE_SEPARATOR);
			sb.append(StringUtils.LINE_SEPARATOR);
		}
	
		List<Class<? extends FlexoBehaviour>> availableFlexoBehaviourTypes = getTechnologyAdapterService()
				.getAvailableFlexoBehaviourTypes(getObjectClass());
		if (availableFlexoBehaviourTypes.size() > 0) {
			sb.append("## Behaviours" + StringUtils.LINE_SEPARATOR);
			sb.append(StringUtils.LINE_SEPARATOR);
			for (Class<? extends FlexoBehaviour> behaviourClass : availableFlexoBehaviourTypes) {
				sb.append(getHTMLReference(behaviourClass) + StringUtils.LINE_SEPARATOR);
			}
			sb.append(StringUtils.LINE_SEPARATOR);
			sb.append("---" + StringUtils.LINE_SEPARATOR);
			sb.append(StringUtils.LINE_SEPARATOR);
		}
	
		List<Class<? extends EditionAction>> availableEditionActionTypes = getTechnologyAdapterService()
				.getAvailableEditionActionTypes(getObjectClass());
		if (availableEditionActionTypes.size() > 0) {
			sb.append("## Edition actions" + StringUtils.LINE_SEPARATOR);
			sb.append(StringUtils.LINE_SEPARATOR);
			for (Class<? extends EditionAction> editionActionClass : availableEditionActionTypes) {
				sb.append(getHTMLReference(editionActionClass) + StringUtils.LINE_SEPARATOR);
			}
			sb.append(StringUtils.LINE_SEPARATOR);
			sb.append("---" + StringUtils.LINE_SEPARATOR);
			sb.append(StringUtils.LINE_SEPARATOR);
		}
	
		List<Class<? extends FetchRequest<?, ?, ?>>> availableFetchRequestsTypes = getTechnologyAdapterService()
				.getAvailableFetchRequestActionTypes(getObjectClass());
		if (availableFetchRequestsTypes.size() > 0) {
			sb.append("## Fetch requests" + StringUtils.LINE_SEPARATOR);
			sb.append(StringUtils.LINE_SEPARATOR);
			for (Class<? extends FetchRequest<?, ?, ?>> fetchRequestClass : availableFetchRequestsTypes) {
				sb.append(getHTMLReference(fetchRequestClass) + StringUtils.LINE_SEPARATOR);
			}
			sb.append(StringUtils.LINE_SEPARATOR);
			sb.append("---" + StringUtils.LINE_SEPARATOR);
			sb.append(StringUtils.LINE_SEPARATOR);
		}
	
		sb.append("## Javadoc" + StringUtils.LINE_SEPARATOR);
		sb.append(StringUtils.LINE_SEPARATOR);
	
		sb.append(getJavadocReference());
		sb.append(StringUtils.LINE_SEPARATOR);
	
		// if (getReferences().size() > 0) {
		// sb.append("---" + StringUtils.LINE_SEPARATOR);
		// sb.append(StringUtils.LINE_SEPARATOR);
		// sb.append("## See also" + StringUtils.LINE_SEPARATOR);
		// sb.append(StringUtils.LINE_SEPARATOR);
		//
		// for (SeeAlso reference : getReferences()) {
		//
		// // AbstractGenerator<? extends FMLObject> generatorReference = getReference(reference.value());
		// // sb.append(" - " + generatorReference.getSmallIconAsHTML());
		// // sb.append(" [`" + generatorReference.getFMLKeyword() + "`](" + generatorReference.getObjectClass().getSimpleName()
		// // + ".html) : " + generatorReference.getFMLShortDescription());
		// sb.append(getHTMLReference(reference.value()) + StringUtils.LINE_SEPARATOR);
		//
		// }
		// sb.append(StringUtils.LINE_SEPARATOR);
		// }
	
		render(sb);
	
	}*/

	private void generateRolesFile() {
		File rolesFile = new File(getMDDir(), getObjectClass().getSimpleName() + "_roles" + ".md");
		StringBuffer sb = new StringBuffer();

		sb.append("# " + getFMLKeyword() + " Roles");
		sb.append(StringUtils.LINE_SEPARATOR);

		sb.append("[//]: # (Do not edit this file, which is automatically generated)" + StringUtils.LINE_SEPARATOR);
		sb.append(StringUtils.LINE_SEPARATOR);

		sb.append(getMasterGenerator().getBigIconAsHTML(getObjectClass()) + " " + getFMLDescription() + StringUtils.LINE_SEPARATOR);
		sb.append(StringUtils.LINE_SEPARATOR);

		sb.append("---" + StringUtils.LINE_SEPARATOR);
		sb.append(StringUtils.LINE_SEPARATOR);

		List<Class<? extends FlexoRole<?>>> availableFlexoRoleTypes = getTechnologyAdapterService()
				.getAvailableFlexoRoleTypes(getObjectClass());
		sb.append("## Roles" + StringUtils.LINE_SEPARATOR);
		sb.append(StringUtils.LINE_SEPARATOR);
		for (Class<? extends FlexoRole<?>> roleClass : availableFlexoRoleTypes) {
			sb.append(getHTMLReference(roleClass) + StringUtils.LINE_SEPARATOR);
		}
		sb.append(StringUtils.LINE_SEPARATOR);

		try {
			FileUtils.saveToFile(rolesFile, sb.toString());
			System.out.println("Generated " + rolesFile.getAbsolutePath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void generateBehavioursFile() {
		File behavioursFile = new File(getMDDir(), getObjectClass().getSimpleName() + "_behaviours" + ".md");
		StringBuffer sb = new StringBuffer();

		sb.append("# " + getFMLKeyword() + " Behaviours");
		sb.append(StringUtils.LINE_SEPARATOR);

		sb.append("[//]: # (Do not edit this file, which is automatically generated)" + StringUtils.LINE_SEPARATOR);
		sb.append(StringUtils.LINE_SEPARATOR);

		sb.append(getMasterGenerator().getBigIconAsHTML(getObjectClass()) + " " + getFMLDescription() + StringUtils.LINE_SEPARATOR);
		sb.append(StringUtils.LINE_SEPARATOR);

		sb.append("---" + StringUtils.LINE_SEPARATOR);
		sb.append(StringUtils.LINE_SEPARATOR);

		List<Class<? extends FlexoBehaviour>> availableFlexoBehaviourTypes = getTechnologyAdapterService()
				.getAvailableFlexoBehaviourTypes(getObjectClass());
		sb.append("## Behaviours" + StringUtils.LINE_SEPARATOR);
		sb.append(StringUtils.LINE_SEPARATOR);
		for (Class<? extends FlexoBehaviour> behaviourClass : availableFlexoBehaviourTypes) {
			sb.append(getHTMLReference(behaviourClass) + StringUtils.LINE_SEPARATOR);
		}
		sb.append(StringUtils.LINE_SEPARATOR);

		try {
			FileUtils.saveToFile(behavioursFile, sb.toString());
			System.out.println("Generated " + behavioursFile.getAbsolutePath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void generateEditionActionsFile() {
		File editionActionsFile = new File(getMDDir(), getObjectClass().getSimpleName() + "_edition_actions" + ".md");
		StringBuffer sb = new StringBuffer();

		sb.append("# " + getFMLKeyword() + " Edition Actions");
		sb.append(StringUtils.LINE_SEPARATOR);

		sb.append("[//]: # (Do not edit this file, which is automatically generated)" + StringUtils.LINE_SEPARATOR);
		sb.append(StringUtils.LINE_SEPARATOR);

		sb.append(getMasterGenerator().getBigIconAsHTML(getObjectClass()) + " " + getFMLDescription() + StringUtils.LINE_SEPARATOR);
		sb.append(StringUtils.LINE_SEPARATOR);

		sb.append("---" + StringUtils.LINE_SEPARATOR);
		sb.append(StringUtils.LINE_SEPARATOR);

		List<Class<? extends EditionAction>> availableEditionActionTypes = getTechnologyAdapterService()
				.getAvailableEditionActionTypes(getObjectClass());
		sb.append("## Edition actions" + StringUtils.LINE_SEPARATOR);
		sb.append(StringUtils.LINE_SEPARATOR);
		for (Class<? extends EditionAction> editionActionClass : availableEditionActionTypes) {
			sb.append(getHTMLReference(editionActionClass) + StringUtils.LINE_SEPARATOR);
		}
		sb.append(StringUtils.LINE_SEPARATOR);

		try {
			FileUtils.saveToFile(editionActionsFile, sb.toString());
			System.out.println("Generated " + editionActionsFile.getAbsolutePath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void generateFetchRequestsFile() {
		File fetchRequestsFile = new File(getMDDir(), getObjectClass().getSimpleName() + "_fetch_requests" + ".md");
		StringBuffer sb = new StringBuffer();

		sb.append("# " + getFMLKeyword() + " Fetch Requests");
		sb.append(StringUtils.LINE_SEPARATOR);

		sb.append("[//]: # (Do not edit this file, which is automatically generated)" + StringUtils.LINE_SEPARATOR);
		sb.append(StringUtils.LINE_SEPARATOR);

		sb.append(getMasterGenerator().getBigIconAsHTML(getObjectClass()) + " " + getFMLDescription() + StringUtils.LINE_SEPARATOR);
		sb.append(StringUtils.LINE_SEPARATOR);

		sb.append("---" + StringUtils.LINE_SEPARATOR);
		sb.append(StringUtils.LINE_SEPARATOR);

		List<Class<? extends FetchRequest<?, ?, ?>>> availableFetchRequestsTypes = getTechnologyAdapterService()
				.getAvailableFetchRequestActionTypes(getObjectClass());
		sb.append("## Fetch requests" + StringUtils.LINE_SEPARATOR);
		sb.append(StringUtils.LINE_SEPARATOR);
		for (Class<? extends FetchRequest<?, ?, ?>> fetchRequestClass : availableFetchRequestsTypes) {
			sb.append(getHTMLReference(fetchRequestClass) + StringUtils.LINE_SEPARATOR);
		}
		sb.append(StringUtils.LINE_SEPARATOR);

		try {
			FileUtils.saveToFile(fetchRequestsFile, sb.toString());
			System.out.println("Generated " + fetchRequestsFile.getAbsolutePath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Deprecated
	protected String getHTMLReference(Class<? extends FMLObject> objectReference) {
		StringBuffer sb = new StringBuffer();
		AbstractGenerator<? extends FMLObject> generatorReference = getReference(objectReference);
		sb.append(" - \u200E" + getMasterGenerator().getSmallIconAsHTML(generatorReference.getObjectClass()));
		sb.append(" [" + generatorReference.getFMLKeyword() + "](" + generatorReference.getObjectClass().getSimpleName() + ".md) : "
				+ generatorReference.getFMLShortDescription());
		return sb.toString();
	}

}
