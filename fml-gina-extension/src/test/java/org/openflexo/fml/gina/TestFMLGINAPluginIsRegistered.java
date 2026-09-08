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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ServiceLoader;

import org.junit.Test;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.view.controller.TechnologyAdapterPluginController;

/**
 * The plugin is reached only through <code>META-INF/services</code>, and a registration that is missing, misspelled or shadowed fails
 * <b>silently</b>: the application simply has no container user interfaces, with nothing logged. This test is what turns that into a
 * failure.
 */
public class TestFMLGINAPluginIsRegistered {

	@Test
	public void testPluginIsDeclaredToTheServiceLoader() {

		FMLGINAPlugin found = null;
		for (TechnologyAdapterPluginController<?> plugin : ServiceLoader.load(TechnologyAdapterPluginController.class)) {
			if (plugin instanceof FMLGINAPlugin) {
				found = (FMLGINAPlugin) plugin;
			}
		}

		assertNotNull("FMLGINAPlugin is not declared in META-INF/services/" + TechnologyAdapterPluginController.class.getName(), found);
	}

	/**
	 * The icons resolve. A ResourceLocator that finds nothing yields an empty icon with nothing logged, so the wizard would simply show a
	 * blank where its image belongs.
	 */
	@Test
	public void testIconsResolve() {
		assertNotNull("Icons/FIBComponent.png not found", FMLGINAIconLibrary.FIB_COMPONENT_VERY_BIG_ICON.getImage());
		assertNotNull("Icons/FIBComponent64x64.png not found", FMLGINAIconLibrary.FIB_COMPONENT_BIG_ICON.getImage());
		assertNotNull("Icons/FIBComponent16x16.png not found", FMLGINAIconLibrary.FIB_COMPONENT_ICON.getImage());
		assertNotNull("Icons/GinaLogo64x64.png not found", FMLGINAIconLibrary.GINA_LOGO_BIG_ICON.getImage());
		assertNotNull("Icons/GinaLogo32x32.png not found", FMLGINAIconLibrary.GINA_LOGO_MEDIUM_ICON.getImage());
		assertNotNull("Icons/VariableAssignment_16x16.png not found", FMLGINAIconLibrary.VARIABLE_ASSIGNMENT_ICON.getImage());
	}

	/** It must target the FML adapter, or it is registered on a controller that is never asked. */
	@Test
	public void testPluginTargetsTheFMLTechnologyAdapter() {
		assertEquals(FMLTechnologyAdapter.class, new FMLGINAPlugin().getTargetTechnologyAdapterClass());
	}

	/** And it must be activable, or activateActivablePlugins() skips it. */
	@Test
	public void testPluginIsActivable() {
		assertTrue(new FMLGINAPlugin().isActivable(null));
	}

	/**
	 * SpecificNaturePerspective.createModuleViewForMasterObject filters plugins on handleObject() while its two sibling methods filter on
	 * isRepresentableInModuleView(). A plugin answering only one of the two is silently never asked for a view in the nature-based
	 * perspectives - which is what ea-module uses.
	 */
	@Test
	public void testHandleObjectAgreesWithIsRepresentableInModuleView() {

		FMLGINAPlugin plugin = new FMLGINAPlugin();

		assertEquals(plugin.isRepresentableInModuleView(null), plugin.handleObject(null));
	}
}
