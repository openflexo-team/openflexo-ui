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

import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.fml.rm.FIBComponentResource;
import org.openflexo.foundation.fml.rm.FMLFIBComponent;
import org.openflexo.gina.model.FIBComponent;

/**
 * Base of the actions applying to a GINA component stored in the <code>Xxx.fml/</code> container of a VirtualModel.
 *
 * <p>
 * Such a component is reachable under TWO objects, and a reader legitimately right-clicks either: the {@link FIBComponentResource}, which is
 * what the resource browsers show, and the {@link FMLFIBComponent} holding its data, which is what the editor view represents and what
 * <code>FlexoController.setCurrentEditedObject</code> switches to. Both are {@link FlexoObject}s with no common ancestor below it, hence a
 * focused object typed as {@link FlexoObject} and a {@link #componentResourceOf(FlexoObject)} narrowing it - the shape
 * {@link CreateInspector} uses for its own two entry points.
 *
 * @author sylvain
 */
public abstract class FIBComponentAction<A extends FlexoAction<A, FlexoObject, FlexoObject>> extends FlexoAction<A, FlexoObject, FlexoObject> {

	protected FIBComponentAction(FlexoActionFactory<A, FlexoObject, FlexoObject> actionFactory, FlexoObject focusedObject,
			List<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionFactory, focusedObject, globalSelection, editor);
	}

	/**
	 * The container component resource supplied object stands for, or null when it stands for none.
	 */
	public static FIBComponentResource componentResourceOf(FlexoObject object) {

		if (object instanceof FIBComponentResource) {
			return (FIBComponentResource) object;
		}
		if (object instanceof FMLFIBComponent) {
			return (FIBComponentResource) ((FMLFIBComponent) object).getResource();
		}
		return null;
	}

	/**
	 * The component supplied object stands for, <b>without loading it</b>.
	 *
	 * <p>
	 * Deliberately not {@link FIBComponentResource#getComponent()}, which loads the resource: this is called while a contextual menu is
	 * being built, to decide whether an action applies, and reading a file from there would be paid on every right-click.
	 */
	public static FIBComponent loadedComponentOf(FlexoObject object) {

		FIBComponentResource resource = componentResourceOf(object);
		FMLFIBComponent data = resource != null ? resource.getLoadedResourceData() : null;
		return data != null ? data.getComponent() : null;
	}

	public FIBComponentResource getComponentResource() {
		return componentResourceOf(getFocusedObject());
	}
}
