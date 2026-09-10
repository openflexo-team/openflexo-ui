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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.fml.gina.FMLGINAIconLibrary;
import org.openflexo.fml.gina.action.FIBComponentAction;
import org.openflexo.fml.gina.action.LocalizeFIBComponent;
import org.openflexo.fml.gina.action.SaveFIBComponent;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rm.FIBComponentResource;
import org.openflexo.foundation.fml.rm.FMLFIBComponent;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * When the two actions of a container component - save it, localize it - are offered, and on which objects.
 *
 * <p>
 * They used to be two buttons at the bottom of the editor view, reachable only while that view was open. As actions they are offered
 * wherever the component is: on the {@link FIBComponentResource} a browser shows, and on the {@link FMLFIBComponent} the editor view
 * represents. Registering an action on a class fails SILENTLY when it is forgotten, which is what the registration assertions here are for.
 */
@RunWith(OrderedRunner.class)
public class TestFIBComponentActionsAvailability extends OpenflexoTestCase {

	private static final String FIXTURE_URI = "http://openflexo.org/test/TestResourceCenter/TestContainerUI.fml";

	private static VirtualModel virtualModel;

	@Test
	@TestOrder(1)
	public void test0LoadFixture() {

		instanciateTestServiceManager();
		CompilationUnitResource resource = serviceManager.getVirtualModelLibrary().getCompilationUnitResource(FIXTURE_URI);
		assertNotNull("No compilation unit for " + FIXTURE_URI, resource);

		virtualModel = resource.getCompilationUnit().getVirtualModel();
		assertNotNull(virtualModel);
		assertEquals("The fixture did not parse", 11, virtualModel.getFlexoConcepts().size());
	}

	/** Both actions are registered on both objects a component is reached through. */
	@Test
	@TestOrder(2)
	public void test1BothActionsAreRegisteredOnBothEntryPoints() {

		// Touching the factory is what LOADS the action class, and its static block is what registers it - which is why the plugin
		// instantiates an initializer per action at module startup. It has to happen before an action list is computed: the list is
		// cached per class, and a registration arriving later invalidates that cache but not a list already handed out.
		assertNotNull(SaveFIBComponent.actionType);
		assertNotNull(LocalizeFIBComponent.actionType);

		FIBComponentResource resource = virtualModel.getInspectorComponentFlexoResource();
		assertNotNull(resource);

		assertTrue("SaveFIBComponent is not registered on the component resource",
				resource.getActionList().contains(SaveFIBComponent.actionType));
		assertTrue("LocalizeFIBComponent is not registered on the component resource",
				resource.getActionList().contains(LocalizeFIBComponent.actionType));

		FMLFIBComponent data = resource.getLoadedResourceData() != null ? resource.getLoadedResourceData() : load(resource);

		assertTrue("SaveFIBComponent is not registered on the component holder", data.getActionList().contains(SaveFIBComponent.actionType));
		assertTrue("LocalizeFIBComponent is not registered on the component holder",
				data.getActionList().contains(LocalizeFIBComponent.actionType));
	}

	/** Both entry points narrow to the same resource, and nothing else does. */
	@Test
	@TestOrder(3)
	public void test2BothEntryPointsNarrowToTheSameResource() {

		FIBComponentResource resource = virtualModel.getInspectorComponentFlexoResource();
		FMLFIBComponent data = load(resource);

		assertEquals(resource, FIBComponentAction.componentResourceOf(resource));
		assertEquals(resource, FIBComponentAction.componentResourceOf(data));

		// An object that is not a component is not one: the actions are typed on FlexoObject, so this is what keeps them out of every
		// contextual menu of the application
		assertNull(FIBComponentAction.componentResourceOf(virtualModel));
		assertFalse(SaveFIBComponent.actionType.isVisibleForSelection(virtualModel, null));
		assertFalse(LocalizeFIBComponent.actionType.isVisibleForSelection(virtualModel, null));
	}

	/**
	 * Saving is offered on a loaded component, and only on a loaded one: an unloaded resource has nothing to write, and asking it would
	 * read the file just to write it back.
	 */
	@Test
	@TestOrder(4)
	public void test3SaveIsEnabledOnceTheComponentIsLoaded() {

		FIBComponentResource resource = virtualModel.getUIComponentFlexoResource();
		assertNotNull(resource);

		assertTrue(SaveFIBComponent.actionType.isVisibleForSelection(resource, null));

		if (resource.getLoadedResourceData() == null) {
			assertFalse("Save must not be offered on a resource that was never loaded",
					SaveFIBComponent.actionType.isEnabledForSelection(resource, null));
		}

		load(resource);
		assertTrue(SaveFIBComponent.actionType.isEnabledForSelection(resource, null));
	}

	/**
	 * Localizing is offered on a component, but enabled only on one the GINA editor has OPEN: the localization window is built around the
	 * editing session, not around the component. Headless there is no editor at all, which is the case asserted here - the enabled case
	 * needs a running application and is not covered.
	 */
	@Test
	@TestOrder(5)
	public void test4LocalizeIsDisabledWithoutAnEditingSession() {

		FIBComponentResource resource = virtualModel.getUIComponentFlexoResource();
		load(resource);

		assertTrue(LocalizeFIBComponent.actionType.isVisibleForSelection(resource, null));
		assertFalse("Localize cannot apply to a component no editor has open",
				LocalizeFIBComponent.actionType.isEnabledForSelection(resource, null));
	}

	/**
	 * Both actions carry an icon, and it is a LOADED one.
	 *
	 * <p>
	 * An {@link org.openflexo.icon.ImageIconResource} built on a path that resolves to nothing is not an error, just an icon of no size -
	 * which shows up as a menu entry with a blank where its icon should be, and nothing else.
	 */
	@Test
	@TestOrder(6)
	public void test5BothActionsCarryALoadedIcon() {

		assertTrue("The save icon did not load", FMLGINAIconLibrary.SAVE_COMPONENT_ICON.getIconWidth() > 0);
		assertTrue("The disabled save icon did not load", FMLGINAIconLibrary.SAVE_COMPONENT_DISABLED_ICON.getIconWidth() > 0);
		assertTrue("The localization icon did not load", FMLGINAIconLibrary.LOCALIZE_COMPONENT_ICON.getIconWidth() > 0);
	}

	private static FMLFIBComponent load(FIBComponentResource resource) {
		assertNotNull("Could not load " + resource.getURI(), resource.getComponent());
		return resource.getLoadedResourceData();
	}
}
