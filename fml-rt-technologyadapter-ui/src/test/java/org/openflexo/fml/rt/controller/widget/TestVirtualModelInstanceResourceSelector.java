/**
 *
 * Copyright (c) 2026, Openflexo
 *
 * This file is part of Fml-rt-technologyadapter-ui, a component of the software infrastructure
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

package org.openflexo.fml.rt.controller.widget;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rt.action.CreateBasicVirtualModelInstance;
import org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResource;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.test.OpenflexoProjectAtRunTimeTestCase;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;
import org.openflexo.test.UITest;

/**
 * A selector does not care whether a resource is loaded, so its browser shows unloaded instances as enabled - which evaluates
 * {@link FIBVirtualModelInstanceResourceSelector#isAcceptableValue(Object)} for every displayed resource, and for every candidate while
 * filtering. This asserts that this test answers correctly while never loading the instance it is asked about.
 */
@RunWith(OrderedRunner.class)
@Category(UITest.class)
public class TestVirtualModelInstanceResourceSelector extends OpenflexoProjectAtRunTimeTestCase {

	/** The VirtualModel the tested instance conforms to */
	private static VirtualModel virtualModelB;
	/** A VirtualModel unrelated to {@link #virtualModelB} */
	private static VirtualModel virtualModelA;
	private static FlexoEditor editor;
	private static FlexoProject<File> project;

	private static FMLRTVirtualModelInstanceResource vmiResource;

	@Test
	@TestOrder(1)
	public void test0LoadVirtualModels() throws Exception {

		instanciateTestServiceManager();
		virtualModelB = serviceManager.getVirtualModelLibrary().getVirtualModel("http://openflexo.org/test/TestResourceCenter/TestVirtualModelB.fml");
		virtualModelA = serviceManager.getVirtualModelLibrary().getVirtualModel("http://openflexo.org/test/TestResourceCenter/TestVirtualModelA.fml");
		assertNotNull(virtualModelB);
		assertNotNull(virtualModelA);
		// Otherwise the non-conforming case below proves nothing
		assertFalse(virtualModelA.isAssignableFrom(virtualModelB));
	}

	@Test
	@TestOrder(2)
	public void test1CreateUnloadedInstance() {

		editor = createStandaloneProject("TestVMIResourceSelector");
		project = (FlexoProject<File>) editor.getProject();
		assertNotNull(project);

		RepositoryFolder<?, ?> folder = project.getVirtualModelInstanceRepository().getRootFolder();
		CreateBasicVirtualModelInstance action = CreateBasicVirtualModelInstance.actionType.makeNewAction(folder, null, editor);
		action.setNewVirtualModelInstanceName("MyInstance");
		action.setNewVirtualModelInstanceTitle("MyInstance");
		action.setVirtualModel(virtualModelB);
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());
		assertNotNull(action.getNewVirtualModelInstance());

		vmiResource = (FMLRTVirtualModelInstanceResource) action.getNewVirtualModelInstance().getResource();
		assertNotNull(vmiResource);

		vmiResource.unloadResourceData(false);
		assertFalse(vmiResource.isLoaded());
	}

	@Test
	@TestOrder(3)
	public void test2ConformingInstanceIsAcceptedWithoutLoading() {

		FIBVirtualModelInstanceResourceSelector selector = new FIBVirtualModelInstanceResourceSelector(null);
		try {
			selector.setVirtualModel(virtualModelB);
			assertTrue(selector.isAcceptableValue(vmiResource));
			assertFalse("Testing acceptability loaded the instance", vmiResource.isLoaded());
		} finally {
			selector.delete();
		}
	}

	@Test
	@TestOrder(4)
	public void test3NonConformingInstanceIsRejectedWithoutLoading() {

		FIBVirtualModelInstanceResourceSelector selector = new FIBVirtualModelInstanceResourceSelector(null);
		try {
			selector.setVirtualModel(virtualModelA);
			assertFalse(selector.isAcceptableValue(vmiResource));
			assertFalse("Testing acceptability loaded the instance", vmiResource.isLoaded());
		} finally {
			selector.delete();
		}
	}
}
