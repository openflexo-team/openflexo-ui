/**
 * 
 * Copyright (c) 2014-2026, Openflexo
 * 
 * This file is part of openflexo-ui, a component of the software infrastructure 
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
package org.openflexo.fml.gina.controller;

import java.util.logging.Logger;

import javax.swing.Icon;

import org.openflexo.components.wizard.Wizard;
import org.openflexo.components.wizard.WizardDialog;
import org.openflexo.fml.gina.FMLGINAIconLibrary;
import org.openflexo.fml.gina.action.CreateFIBComponent;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.action.FlexoActionRunnable;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.gina.controller.FIBController.Status;
import org.openflexo.view.controller.ActionInitializer;
import org.openflexo.view.controller.ControllerActionInitializer;

/**
 * Wires {@link CreateFIBComponent} into the module: its wizard, and what happens once it ran.
 *
 * <p>
 * Instantiating this is also what LOADS {@link CreateFIBComponent}, whose static block registers the action on FlexoConcept and
 * FMLCompilationUnit. That is the mechanism by which a plugin contributes an action - the same one FMLDiagrammingPlugin relies on.
 *
 * @author sylvain
 */
public class CreateFIBComponentInitializer extends ActionInitializer<CreateFIBComponent, FMLObject, FMLObject> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ControllerActionInitializer.class.getPackage().getName());

	public CreateFIBComponentInitializer(ControllerActionInitializer actionInitializer) {
		super(CreateFIBComponent.actionType, actionInitializer);
	}

	@Override
	protected FlexoActionRunnable<CreateFIBComponent, FMLObject, FMLObject> getDefaultInitializer() {
		return (e, action) -> {
			Wizard wizard = new CreateFIBComponentWizard(action, getController());
			WizardDialog dialog = new WizardDialog(wizard, getController());
			dialog.showDialog();
			return dialog.getStatus() == Status.VALIDATED;
		};
	}

	/**
	 * Open the freshly created component in the GINA editor, which is where the user carries on.
	 *
	 * <p>
	 * The object to switch to is the resource DATA, not the resource: the module view is registered for
	 * {@link org.openflexo.foundation.fml.rm.FMLFIBComponent}, and <code>setCurrentEditedObject</code> switches to the view representing it.
	 * The data is already loaded at this point - the action set the component on it - so
	 * {@link org.openflexo.foundation.resource.FlexoResource#getLoadedResourceData()} is enough and raises nothing.
	 */
	@Override
	protected FlexoActionRunnable<CreateFIBComponent, FMLObject, FMLObject> getDefaultFinalizer() {
		return (e, action) -> {
			if (action.getNewComponentResource() != null && action.getNewComponentResource().getLoadedResourceData() != null) {
				getController().setCurrentEditedObject(action.getNewComponentResource().getLoadedResourceData());
			}
			return true;
		};
	}

	@Override
	protected Icon getEnabledIcon(FlexoActionFactory<CreateFIBComponent, FMLObject, FMLObject> actionType) {
		return FMLGINAIconLibrary.FIB_COMPONENT_ICON;
	}
}
