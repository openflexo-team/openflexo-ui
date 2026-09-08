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
package org.openflexo.fml.gina;

import org.openflexo.icon.IconLibrary;
import org.openflexo.icon.ImageIconResource;
import org.openflexo.rm.ResourceLocator;

/**
 * The icons of the FML/GINA extension.
 *
 * <p>
 * Copied from <code>gina-ta-ui</code> rather than depended upon: that module is the deprecated bridge this plugin replaces, and is to be
 * deleted. A plugin owning its own icons is also what <code>FMLDiagrammingPlugin</code> does.
 *
 * @author sylvain
 */
public class FMLGINAIconLibrary extends IconLibrary {

	/** 128x128 */
	public static final ImageIconResource FIB_COMPONENT_VERY_BIG_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/FIBComponent.png"));

	/** 64x64, the size a wizard page image expects */
	public static final ImageIconResource FIB_COMPONENT_BIG_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/FIBComponent64x64.png"));

	/** 16x16, the size a menu entry and a browser row expect */
	public static final ImageIconResource FIB_COMPONENT_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/FIBComponent16x16.png"));

	public static final ImageIconResource GINA_LOGO_BIG_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/GinaLogo64x64.png"));

	public static final ImageIconResource GINA_LOGO_MEDIUM_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/GinaLogo32x32.png"));

	/**
	 * Carried over with the rest, though nothing uses it yet: it belonged to the <code>VariableAssignment</code> of the deprecated model
	 * slot, which the container convention replaced.
	 */
	public static final ImageIconResource VARIABLE_ASSIGNMENT_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/VariableAssignment_16x16.png"));
}
