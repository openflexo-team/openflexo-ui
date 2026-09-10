/**
 * Openflexo is a computer program whose purpose is to provide an open-source, free and
 * open-source model federation platform.
 */

package org.openflexo.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.gina.model.FIBModelFactory;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;
import org.openflexo.view.controller.FlexoFIBController;

/**
 * An <code>Xxx.fml/</code> container is never displayed as a folder of the resource browsers, and nothing it holds is lost for that: what
 * no resource links is listed under its compilation unit.
 *
 * <p>
 * The container used to come back as soon as it held a resource of its own: the technology adapters were asked before the rule hiding it,
 * and the default policy of the FML one - show any folder holding resources - answered first. Storing <code>.fib</code> /
 * <code>.inspector</code> components in the container is precisely what gave it resources.
 *
 * <p>
 * Nothing here relies on how a resource center files its resources in folders: a jar-based one - which is what the fixture is loaded from
 * under Gradle - files them all in its root folder. The folder shown is therefore built on the real container directory of the fixture.
 *
 * <p>
 * What this does not cover: the rendering of the browsers themselves, and diagram specifications stored in a container, for which
 * openflexo-core has no fixture.
 */
@RunWith(OrderedRunner.class)
public class TestVirtualModelContainerFolderIsNotDisplayed extends OpenflexoTestCase {

	private static final String FIXTURE_URI = "http://openflexo.org/test/TestResourceCenter/TestContainerUI.fml";

	private static CompilationUnitResource compilationUnitResource;
	private static VirtualModel virtualModel;

	@Test
	@TestOrder(1)
	public void test0LoadFixture() {

		instanciateTestServiceManager();

		compilationUnitResource = serviceManager.getVirtualModelLibrary().getCompilationUnitResource(FIXTURE_URI);
		assertNotNull("No compilation unit for " + FIXTURE_URI, compilationUnitResource);

		virtualModel = compilationUnitResource.getCompilationUnit().getVirtualModel();
		assertNotNull(virtualModel);
		assertEquals("The fixture did not parse", 11, virtualModel.getFlexoConcepts().size());
	}

	/**
	 * Never displayed, WHATEVER it holds - and in particular when it holds a resource of the FML technology, the case that brought it back.
	 */
	@Test
	@TestOrder(2)
	@SuppressWarnings("unchecked")
	public void test1ContainerFolderIsNeverDisplayed() throws Exception {

		FlexoResourceCenter<Object> resourceCenter = (FlexoResourceCenter<Object>) compilationUnitResource.getResourceCenter();
		Object containerDirectory = resourceCenter.getContainer(compilationUnitResource.getIODelegate().getSerializationArtefact());

		// Detached from the tree of the resource center: built on the real container directory, holding a component of its own
		RepositoryFolder<FlexoResource<?>, Object> container = new RepositoryFolder<>(containerDirectory, null, resourceCenter);
		container.addToResources(virtualModel.getFlexoConcept("Simple").getInspectorComponentFlexoResource());

		assertEquals("TestContainerUI.fml", container.getName());
		assertTrue(FlexoFIBController.isVirtualModelContainerFolder(container));

		FIBModelFactory factory = new FIBModelFactory(null, serviceManager.getTechnologyAdapterService());
		FlexoFIBController controller = new FlexoFIBController(factory.newFIBPanel(), null);
		assertFalse("A VirtualModel container holding a component is displayed as a folder", controller.shouldBeDisplayed(container));

		// And the rule does not catch the folder holding the container
		RepositoryFolder<FlexoResource<?>, Object> parent = new RepositoryFolder<>(resourceCenter.getContainer(containerDirectory), null,
				resourceCenter);
		assertFalse("The folder holding the container is not one: " + parent.getName(), FlexoFIBController.isVirtualModelContainerFolder(parent));
	}

	/**
	 * What the container holds and no resource links is listed under the compilation unit - and only that: what IS linked comes through the
	 * contents of the compilation unit, and listing it here too would show it twice.
	 */
	@Test
	@TestOrder(3)
	public void test2UnlinkedResourcesAreListedUnderTheCompilationUnit() {

		List<FlexoResource<?>> listed = FlexoFIBController.uncontainedResourcesInContainerOf(compilationUnitResource);

		boolean nestedScreenListed = false;
		for (FlexoResource<?> resource : listed) {
			assertNull(resource + " is contained by another resource, and shown under it already", resource.getContainer());
			assertNotSame(compilationUnitResource, resource);
			if ("NestedScreen.fib".equals(resource.getName())) {
				nestedScreenListed = true;
			}
		}
		assertTrue("UI/NestedScreen.fib, which nothing links, is not listed under its compilation unit: it would be unreachable - listed: "
				+ listed, nestedScreenListed);

		// And what is linked is reachable through the contents instead
		FlexoConcept simple = virtualModel.getFlexoConcept("Simple");
		assertTrue("Simple.inspector is not among the contents of its compilation unit",
				compilationUnitResource.getContents().contains(simple.getInspectorComponentFlexoResource()));
		assertFalse("Simple.inspector is listed twice", listed.contains(simple.getInspectorComponentFlexoResource()));
	}
}
