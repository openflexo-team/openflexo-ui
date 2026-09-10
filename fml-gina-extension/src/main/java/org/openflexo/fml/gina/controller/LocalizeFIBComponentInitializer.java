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

import org.openflexo.fml.gina.FMLGINAIconLibrary;
import org.openflexo.fml.gina.FMLGINAPlugin;
import org.openflexo.fml.gina.action.LocalizeFIBComponent;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.action.FlexoActionRunnable;
import org.openflexo.gina.swing.editor.EditedFIBComponent;
import org.openflexo.gina.swing.editor.FIBEditor;
import org.openflexo.view.controller.ActionInitializer;
import org.openflexo.view.controller.ControllerActionInitializer;

/**
 * Wires {@link LocalizeFIBComponent} into the module: opening the localization window IS the action, and a window belongs here rather than
 * in the action.
 *
 * <p>
 * Instantiating this is also what LOADS {@link LocalizeFIBComponent}, whose static block registers the action on the component resource and
 * on its data.
 *
 * @author sylvain
 */
public class LocalizeFIBComponentInitializer extends ActionInitializer<LocalizeFIBComponent, FlexoObject, FlexoObject> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(LocalizeFIBComponentInitializer.class.getPackage().getName());

	public LocalizeFIBComponentInitializer(ControllerActionInitializer actionInitializer) {
		super(LocalizeFIBComponent.actionType, actionInitializer);
	}

	@Override
	protected Icon getEnabledIcon(FlexoActionFactory<LocalizeFIBComponent, FlexoObject, FlexoObject> actionFactory) {
		return FMLGINAIconLibrary.LOCALIZE_COMPONENT_ICON;
	}

	/**
	 * The editor is asked for WITHOUT building it: the action is enabled only when a session already exists, so one does, and building the
	 * editor from here would be both useless and dangerous - it creates Swing widgets under locks the caller may hold.
	 */
	@Override
	protected FlexoActionRunnable<LocalizeFIBComponent, FlexoObject, FlexoObject> getDefaultFinalizer() {
		return (e, action) -> {

			EditedFIBComponent editedComponent = action.getEditedComponent();
			FMLGINAPlugin plugin = getController().getApplicationContext().getTechnologyAdapterControllerService()
					.getPlugin(FMLGINAPlugin.class);
			FIBEditor editor = plugin != null ? plugin.getExistingFIBEditor() : null;

			if (editedComponent != null && editor != null) {
				editor.localizeFIB(editedComponent, getController().getFlexoFrame());
			}
			return true;
		};
	}
}
