/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Fml-technologyadapter-ui, a component of the software infrastructure 
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

package org.openflexo.fml.controller.action;

import javax.swing.Icon;

import org.openflexo.components.wizard.Wizard;
import org.openflexo.components.wizard.WizardDialog;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.action.FlexoActionRunnable;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.action.CreateTypeDeclaration;
import org.openflexo.gina.controller.FIBController.Status;
import org.openflexo.icon.FMLIconLibrary;
import org.openflexo.view.controller.ActionInitializer;
import org.openflexo.view.controller.ControllerActionInitializer;

public class CreateTypeDeclarationInitializer extends ActionInitializer<CreateTypeDeclaration, FMLCompilationUnit, FMLObject> {
	public CreateTypeDeclarationInitializer(ControllerActionInitializer actionInitializer) {
		super(CreateTypeDeclaration.actionType, actionInitializer);
	}

	@Override
	protected FlexoActionRunnable<CreateTypeDeclaration, FMLCompilationUnit, FMLObject> getDefaultInitializer() {
		return (e, action) -> {
			Wizard wizard = new CreateTypeDeclarationWizard(action, getController());
			WizardDialog dialog = new WizardDialog(wizard, getController());
			dialog.showDialog();
			if (dialog.getStatus() != Status.VALIDATED) {
				// Operation cancelled
				return false;
			}
			return true;
		};
	}

	@Override
	protected FlexoActionRunnable<CreateTypeDeclaration, FMLCompilationUnit, FMLObject> getDefaultFinalizer() {
		return (e, action) -> {
			/*if (action.getModelSlotTechnologyAdapter() != null) {
				TechnologyAdapterService taService = getController().getApplicationContext().getTechnologyAdapterService();
				taService.activateTechnologyAdapter((TechnologyAdapter) action.getModelSlotTechnologyAdapter(), true);
				return true;
			}
			return false;*/
			return true;
		};
	}

	@Override
	protected Icon getEnabledIcon(FlexoActionFactory<CreateTypeDeclaration, FMLCompilationUnit, FMLObject> actionType) {
		return FMLIconLibrary.TYPEDEF_ICON;
	}

}
