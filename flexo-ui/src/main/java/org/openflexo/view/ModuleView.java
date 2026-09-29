/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 * 
 * This file is part of Flexo-ui, a component of the software infrastructure 
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

package org.openflexo.view;

import org.openflexo.foundation.FlexoObject;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.model.FlexoPerspective;

/**
 * This interface is implemented by all views that will be displayed as a top-level view of a module. This abstract representation is used
 * by a general scheme implemented in module controller to manage the navigation and includes a control panel.
 * 
 * @author sguerin
 */
public interface ModuleView<O extends FlexoObject> {

	public O getRepresentedObject();

	/**
	 * Delete the ModuleView
	 * 
	 * VERY IMPORTANT: in all implementations, DO NOT FORGET TO CALL {@link FlexoController.removeModuleView(this)}
	 */
	public void deleteModuleView();

	/**
	 * This method should return the perspective in which this view is supposed to be seen. DO NOT return null!!!
	 * 
	 * @return
	 */
	public FlexoPerspective getPerspective();

	/**
	 * This method is called before the module view is about to be shown in its module, i.e. when it becomes the view of the main pane.
	 * Not called when the module itself is activated again: see {@link #moduleActivated(FlexoController)}.
	 */
	public void willShow();

	/**
	 * This method is called before the module view is about to be hidden in its module, i.e. when another view replaces it in the main
	 * pane. Not called when another module is activated: see {@link #moduleDeactivated()}.
	 */
	public void willHide();

	/**
	 * Called on the current view of a module when another module is activated.
	 *
	 * <p>
	 * The view stays displayed in the window of its module, and so do the components it put in the side columns (palettes, inspectors):
	 * hiding them - as {@link #willHide()} does - made them vanish as soon as the user clicked in another module. Override only to release
	 * what is shared by the whole application and must not stay bound to an inactive module, such as a paste handler registered in the
	 * editing context.
	 */
	public default void moduleDeactivated() {
	}

	/**
	 * Called on the current view of a module when that module is activated again.
	 *
	 * <p>
	 * By default {@link #show(FlexoController, FlexoPerspective)} again, which is repeatable: a component shared by the whole application
	 * (the inspectors of a technology adapter, a palette) may have been taken by a view of the other module meanwhile - a Swing component
	 * has one parent only - and is put back.
	 */
	public default void moduleActivated(FlexoController controller) {
		show(controller, controller.getCurrentPerspective());
	}

	/**
	 * This method is called when the module view is shown with a controller and perspective
	 * 
	 */
	public void show(FlexoController controller, FlexoPerspective perspective);

	/**
	 * Returns flag indicating if this view is itself responsible for scroll management When not, Flexo will manage it's own scrollbar for
	 * you
	 * 
	 * @return
	 */
	public boolean isAutoscrolled();

}
