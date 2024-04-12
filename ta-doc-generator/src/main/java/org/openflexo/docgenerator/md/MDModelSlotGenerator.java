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

import java.io.File;
import java.util.logging.Logger;

import org.openflexo.docgenerator.ModelSlotGenerator;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.logging.FlexoLogger;

/**
 * Documentation generator for {@link ModelSlot}
 * 
 */
public class MDModelSlotGenerator<MS extends ModelSlot<?>> extends ModelSlotGenerator<MS> implements AbstractMDGenerator<MS> {

	private static final Logger logger = FlexoLogger.getLogger(MDModelSlotGenerator.class.getPackage().getName());

	private MS ms;

	private MDModelSlotRoleListGenerator<MS> roleListGenerator;
	private MDModelSlotBehaviourListGenerator<MS> behaviourListGenerator;
	private MDModelSlotEditionActionListGenerator<MS> editionActionListGenerator;
	private MDModelSlotFetchRequestListGenerator<MS> fetchRequestListGenerator;

	public MDModelSlotGenerator(Class<MS> objectClass, MDMasterGenerator<?> taDocGenerator) {
		super(objectClass, taDocGenerator);
		ms = getFMLModelFactory().newInstance(getObjectClass());
		roleListGenerator = new MDModelSlotRoleListGenerator<>(objectClass, this, taDocGenerator);
		behaviourListGenerator = new MDModelSlotBehaviourListGenerator<>(objectClass, this, taDocGenerator);
		editionActionListGenerator = new MDModelSlotEditionActionListGenerator<>(objectClass, this, taDocGenerator);
		fetchRequestListGenerator = new MDModelSlotFetchRequestListGenerator<>(objectClass, this, taDocGenerator);
	}

	@Override
	public MDMasterGenerator<?> getMasterGenerator() {
		return (MDMasterGenerator<?>) super.getMasterGenerator();
	}

	@Override
	public File getMDDir() {
		return getMasterGenerator().getMDDir();
	}

	@Override
	public String getTemplateName() {
		return "ModelSlot.md";
	}

	@Override
	public String generate() {
		String returned = super.generate();
		roleListGenerator.generate();
		behaviourListGenerator.generate();
		editionActionListGenerator.generate();
		fetchRequestListGenerator.generate();
		return returned;
	}

	public File getRolesDirectory() {
		return new File(getMasterGenerator().getModelSlotDirectory(getObjectClass()), "Roles");
	}

	public File getBehavioursDirectory() {
		return new File(getMasterGenerator().getModelSlotDirectory(getObjectClass()), "Behaviours");
	}

	public File getEditionActionsDirectory() {
		return new File(getMasterGenerator().getModelSlotDirectory(getObjectClass()), "EditionActions");
	}

	public File getFetchRequestsDirectory() {
		return new File(getMasterGenerator().getModelSlotDirectory(getObjectClass()), "FetchRequests");
	}

}
