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

import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.fml.gina.FMLGINAPlugin;
import org.openflexo.foundation.fml.rm.FIBComponentResource;
import org.openflexo.foundation.fml.rm.FMLFIBComponent;
import org.openflexo.foundation.resource.StreamIODelegate;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.view.controller.TechnologyAdapterControllerService;

/**
 * Writes a container component back to the artefact it was read from.
 *
 * <p>
 * The GINA editor edits the very component the resource holds, in place: what this saves is whatever the reader has done to it since it was
 * loaded, whether or not the editor is the one showing it.
 *
 * @author sylvain
 */
public class SaveFIBComponent extends FIBComponentAction<SaveFIBComponent> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SaveFIBComponent.class.getPackage().getName());

	public static FlexoActionFactory<SaveFIBComponent, FlexoObject, FlexoObject> actionType = new FlexoActionFactory<SaveFIBComponent, FlexoObject, FlexoObject>(
			"save", null, FlexoActionFactory.defaultGroup, FlexoActionFactory.NORMAL_ACTION_TYPE) {

		@Override
		public SaveFIBComponent makeNewAction(FlexoObject focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
			return new SaveFIBComponent(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FlexoObject object, Vector<FlexoObject> globalSelection) {
			return componentResourceOf(object) != null;
		}

		/**
		 * Only what is loaded can have been edited - and asking an unloaded resource to save would read the file just to write it back.
		 */
		@Override
		public boolean isEnabledForSelection(FlexoObject object, Vector<FlexoObject> globalSelection) {
			FIBComponentResource resource = componentResourceOf(object);
			return resource != null && resource.getLoadedResourceData() != null && resource.getIODelegate() != null;
		}

		@Override
		public LocalizedDelegate getLocales(FlexoServiceManager serviceManager) {
			return serviceManager.getService(TechnologyAdapterControllerService.class).getPlugin(FMLGINAPlugin.class).getLocales();
		}
	};

	static {
		FlexoObjectImpl.addActionForClass(SaveFIBComponent.actionType, FIBComponentResource.class);
		FlexoObjectImpl.addActionForClass(SaveFIBComponent.actionType, FMLFIBComponent.class);
	}

	private SaveFIBComponent(FlexoObject focusedObject, List<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	/**
	 * <code>setSaveToSourceResource(true)</code> is what makes the edit survive: without it the component is written to wherever the
	 * resource center was READ from - under <code>build/</code> for a project run from Gradle - and the next build overwrites it with the
	 * copy in <code>src/main/resources</code>.
	 */
	@Override
	protected void doAction(Object context) throws FlexoException {

		FIBComponentResource resource = getComponentResource();

		if (resource == null || resource.getIODelegate() == null) {
			return;
		}

		if (resource.getIODelegate() instanceof StreamIODelegate) {
			((StreamIODelegate<?>) resource.getIODelegate()).setSaveToSourceResource(true);
		}

		resource.save();
	}
}
