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

import javax.swing.ImageIcon;

import org.openflexo.fml.gina.FMLGINAIconLibrary;
import org.openflexo.fml.gina.FMLGINAPlugin;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.view.ModuleView;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.GenericPerspective;

/**
 * The GUI perspective: the one where the user interfaces a VirtualModel stores in its <code>Xxx.fml/</code> container are instantiated
 * (the instances of a concept driving a component) and edited (the components themselves).
 *
 * <p>
 * Everything is delegated to {@link FMLGINAPlugin}, whose instance views are {@link FMLGINAPlugin#isPerspectiveScoped(FlexoObject) perspective-scoped}: the textual
 * FML perspective never sees it. For any object the plugin does not claim, the generic views apply. Unlike the former
 * <code>FMLControlledFIBNaturePerspective</code>, nothing here depends on the GINA technology adapter or on the
 * <code>FMLControlledFIB*Nature</code>s.
 *
 * @author sylvain
 */
public class FMLGUIPerspective extends GenericPerspective {

	public FMLGUIPerspective(FlexoController controller) {
		super(controller);
	}

	@Override
	public String getName() {
		return "GUI_perspective";
	}

	@Override
	public ImageIcon getActiveIcon() {
		return FMLGINAIconLibrary.FIB_COMPONENT_ICON;
	}

	private FMLGINAPlugin getPlugin() {
		return getController().getApplicationContext().getTechnologyAdapterControllerService().getPlugin(FMLGINAPlugin.class);
	}

	@Override
	public boolean isRepresentableInModuleView(FlexoObject object) {
		FMLGINAPlugin plugin = getPlugin();
		if (plugin != null && plugin.isRepresentableInModuleView(object)) {
			return true;
		}
		return super.isRepresentableInModuleView(object);
	}

	@Override
	public FlexoObject getRepresentableMasterObject(FlexoObject object) {
		FMLGINAPlugin plugin = getPlugin();
		if (plugin != null && plugin.isRepresentableInModuleView(object)) {
			return plugin.getRepresentableMasterObject(object);
		}
		return super.getRepresentableMasterObject(object);
	}

	@Override
	public ModuleView<?> createModuleViewForMasterObject(FlexoObject object) {
		FMLGINAPlugin plugin = getPlugin();
		if (plugin != null && plugin.isRepresentableInModuleView(object)) {
			ModuleView<?> view = plugin.createModuleViewForMasterObject(object, getController(), this);
			if (view != null) {
				return view;
			}
		}
		return super.createModuleViewForMasterObject(object);
	}

}
