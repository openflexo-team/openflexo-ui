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

package org.openflexo.fml.gina.action;

import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.fml.gina.FMLGINAPlugin;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.fml.rm.FIBComponentResource;
import org.openflexo.foundation.fml.rm.FMLFIBComponent;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.swing.editor.FIBEditor;
import org.openflexo.gina.swing.editor.EditedFIBComponent;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.view.controller.TechnologyAdapterControllerService;

/**
 * Opens the localization editor on a container component: the window listing every localizable string it declares, and the translations
 * held in its localized dictionary.
 *
 * <p>
 * The whole effect is a window, and a window is the controller layer's business: {@link #doAction(Object)} does nothing and
 * <code>LocalizeFIBComponentInitializer</code> is what opens it. What the action carries is the DECISION - which edited component to
 * localize - and the guarantee that it is offered only where it applies.
 *
 * <p>
 * It applies only to a component the editor has open: {@link FIBEditor#localizeFIB} works on an {@link EditedFIBComponent}, the per-session
 * state the editor builds when a component is opened, not on the component itself.
 *
 * @author sylvain
 */
public class LocalizeFIBComponent extends FIBComponentAction<LocalizeFIBComponent> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(LocalizeFIBComponent.class.getPackage().getName());

	public static FlexoActionFactory<LocalizeFIBComponent, FlexoObject, FlexoObject> actionType = new FlexoActionFactory<LocalizeFIBComponent, FlexoObject, FlexoObject>(
			"localize", null, FlexoActionFactory.defaultGroup, FlexoActionFactory.NORMAL_ACTION_TYPE) {

		@Override
		public LocalizeFIBComponent makeNewAction(FlexoObject focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
			return new LocalizeFIBComponent(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FlexoObject object, Vector<FlexoObject> globalSelection) {
			return componentResourceOf(object) != null;
		}

		@Override
		public boolean isEnabledForSelection(FlexoObject object, Vector<FlexoObject> globalSelection) {
			return editedComponentOf(object, serviceManagerOf(object)) != null;
		}

		@Override
		public LocalizedDelegate getLocales(FlexoServiceManager serviceManager) {
			return serviceManager.getService(TechnologyAdapterControllerService.class).getPlugin(FMLGINAPlugin.class).getLocales();
		}
	};

	static {
		FlexoObjectImpl.addActionForClass(LocalizeFIBComponent.actionType, FIBComponentResource.class);
		FlexoObjectImpl.addActionForClass(LocalizeFIBComponent.actionType, FMLFIBComponent.class);
	}

	/**
	 * The editing session the localization window is opened on, or null when supplied object is not open in the editor.
	 *
	 * <p>
	 * Asks the plugin for the editor it ALREADY holds: {@link FMLGINAPlugin#getFIBEditor(boolean)} would build one - palette, widget
	 * inspectors, the lot - and this runs every time a contextual menu is built.
	 */
	public static EditedFIBComponent editedComponentOf(FlexoObject object, FlexoServiceManager serviceManager) {

		FIBComponent component = loadedComponentOf(object);
		if (component == null || serviceManager == null) {
			return null;
		}

		// Null outside a running application - a headless test, typically - where nothing is ever open in an editor
		TechnologyAdapterControllerService controllerService = serviceManager.getService(TechnologyAdapterControllerService.class);
		FMLGINAPlugin plugin = controllerService != null ? controllerService.getPlugin(FMLGINAPlugin.class) : null;
		FIBEditor editor = plugin != null ? plugin.getExistingFIBEditor() : null;

		return editor != null ? editor.getEditedFIBComponent(component) : null;
	}

	private static FlexoServiceManager serviceManagerOf(FlexoObject object) {
		FIBComponentResource resource = componentResourceOf(object);
		return resource != null ? resource.getServiceManager() : null;
	}

	private LocalizeFIBComponent(FlexoObject focusedObject, List<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	public EditedFIBComponent getEditedComponent() {
		return editedComponentOf(getFocusedObject(), getComponentResource() != null ? getComponentResource().getServiceManager() : null);
	}

	/** Nothing to do on the model: the effect of this action is the window its initializer opens. */
	@Override
	protected void doAction(Object context) {
	}
}
