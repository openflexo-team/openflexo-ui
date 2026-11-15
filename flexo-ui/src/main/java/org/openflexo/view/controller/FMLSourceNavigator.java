/**
 * 
 * Copyright (c) 2026, Openflexo
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

package org.openflexo.view.controller;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.fml.rt.error.FMLStack;
import org.openflexo.foundation.fml.rt.error.FMLStackFrame;

/**
 * Something that can show the FML source of a compilation unit at a line: implemented by the controller of the FML technology adapter,
 * used to go from the location of a failure (an FML stack frame, CORE-F-11) to the FML editor.
 */
public interface FMLSourceNavigator {

	/**
	 * Tell if supplied controller can show, in an FML editor, the compilation unit of the virtual model whose URI is supplied
	 */
	public boolean canShowFMLSource(FlexoController controller, String virtualModelURI);

	/**
	 * Show the compilation unit of the virtual model whose URI is supplied in the FML editor of supplied controller, at supplied line
	 * (1-based)
	 */
	public void showFMLSource(FlexoController controller, String virtualModelURI, int line);

	/**
	 * The navigator of the controllers of the technology adapters loaded in supplied controller's application, or null when none is
	 */
	public static FMLSourceNavigator find(FlexoController controller) {
		if (controller == null || controller.getApplicationContext() == null
				|| controller.getApplicationContext().getTechnologyAdapterControllerService() == null) {
			return null;
		}
		for (Object tac : controller.getApplicationContext().getTechnologyAdapterControllerService().getLoadedAdapterControllers()) {
			if (tac instanceof FMLSourceNavigator) {
				return (FMLSourceNavigator) tac;
			}
		}
		return null;
	}

	/** The controller of the active module of supplied editor, when it is interactive, or null */
	public static FlexoController activeController(FlexoEditor editor) {
		if (editor instanceof InteractiveFlexoEditor) {
			InteractiveFlexoEditor interactive = (InteractiveFlexoEditor) editor;
			if (interactive.getModuleLoader() != null && interactive.getModuleLoader().getActiveModule() != null) {
				return interactive.getModuleLoader().getActiveModule().getFlexoController();
			}
		}
		return null;
	}

	/** Tell if the FML source of supplied frame can be shown by the active controller of supplied editor */
	public static boolean canShow(FlexoEditor editor, FMLStackFrame frame) {
		if (frame == null || frame.getCompilationUnitURI() == null || frame.getLine() <= 0) {
			return false;
		}
		FlexoController controller = activeController(editor);
		FMLSourceNavigator navigator = find(controller);
		return navigator != null && navigator.canShowFMLSource(controller, frame.getCompilationUnitURI());
	}

	/** Show the FML source of supplied frame in the active controller of supplied editor, when it can */
	public static void show(FlexoEditor editor, FMLStackFrame frame) {
		if (canShow(editor, frame)) {
			FlexoController controller = activeController(editor);
			find(controller).showFMLSource(controller, frame.getCompilationUnitURI(), frame.getLine());
		}
	}

	/** The innermost frame of supplied stack whose FML source can be shown by the active controller of supplied editor, or null */
	public static FMLStackFrame firstShowableFrame(FlexoEditor editor, FMLStack stack) {
		if (stack != null) {
			for (FMLStackFrame frame : stack.getFrames()) {
				if (canShow(editor, frame)) {
					return frame;
				}
			}
		}
		return null;
	}
}
