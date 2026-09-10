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

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Icon;

import org.openflexo.fml.gina.FMLGINAIconLibrary;
import org.openflexo.fml.gina.action.SaveFIBComponent;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.action.FlexoExceptionHandler;
import org.openflexo.view.controller.ActionInitializer;
import org.openflexo.view.controller.ControllerActionInitializer;

/**
 * Wires {@link SaveFIBComponent} into the module.
 *
 * <p>
 * Instantiating this is also what LOADS {@link SaveFIBComponent}, whose static block registers the action on the component resource and on
 * its data - the mechanism by which a plugin contributes an action.
 *
 * @author sylvain
 */
public class SaveFIBComponentInitializer extends ActionInitializer<SaveFIBComponent, FlexoObject, FlexoObject> {

	private static final Logger logger = Logger.getLogger(SaveFIBComponentInitializer.class.getPackage().getName());

	public SaveFIBComponentInitializer(ControllerActionInitializer actionInitializer) {
		super(SaveFIBComponent.actionType, actionInitializer);
	}

	@Override
	protected Icon getEnabledIcon(FlexoActionFactory<SaveFIBComponent, FlexoObject, FlexoObject> actionFactory) {
		return FMLGINAIconLibrary.SAVE_COMPONENT_ICON;
	}

	@Override
	protected Icon getDisabledIcon(FlexoActionFactory<SaveFIBComponent, FlexoObject, FlexoObject> actionFactory) {
		return FMLGINAIconLibrary.SAVE_COMPONENT_DISABLED_ICON;
	}

	/**
	 * A failed write is worth telling the reader about: it means the edit is still only in memory.
	 */
	@Override
	protected FlexoExceptionHandler<SaveFIBComponent, FlexoObject, FlexoObject> getDefaultExceptionHandler() {
		return (exception, action) -> {
			logger.log(Level.WARNING,
					"Could not save " + (action.getComponentResource() != null ? action.getComponentResource().getURI() : null), exception);
			// The plugin's own dictionary, which is where the action's strings live - not the module's
			getController().notify(SaveFIBComponent.actionType.getLocales(getController().getApplicationContext())
					.localizedForKey("could_not_save_component"));
			return false;
		};
	}
}
