/**
 * Openflexo is a computer program whose purpose is to provide an open-source, free and
 * open-source model federation platform.
 */

package org.openflexo.fml.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * The rule that the container selector of a concept and the drag and drop of the FML browsers apply (CORE-F-19): a concept is declared in
 * its own VirtualModel (as a root concept) or in a concept of that VirtualModel, never in another VirtualModel, and never in itself or in a
 * concept nested in it.
 */
@RunWith(OrderedRunner.class)
public class TestAcceptableContainer extends OpenflexoTestCase {

	private static final String FIXTURE_URI = "http://openflexo.org/test/TestResourceCenter/TestContainerUI.fml";
	private static final String OTHER_URI = "http://openflexo.org/test/TestResourceCenter/TestVirtualModelA.fml";

	private static VirtualModel virtualModel;
	private static VirtualModel otherVirtualModel;

	@Test
	@TestOrder(1)
	public void test0LoadFixtures() throws Exception {
		instanciateTestServiceManager();
		CompilationUnitResource resource = serviceManager.getVirtualModelLibrary().getCompilationUnitResource(FIXTURE_URI);
		assertNotNull("No compilation unit for " + FIXTURE_URI, resource);
		virtualModel = resource.getCompilationUnit().getVirtualModel();
		assertEquals("The fixture did not parse", 21, virtualModel.getAllFlexoConcepts().size());
		otherVirtualModel = serviceManager.getVirtualModelLibrary().getVirtualModel(OTHER_URI);
		assertNotNull("No VirtualModel " + OTHER_URI, otherVirtualModel);
	}

	@Test
	@TestOrder(2)
	public void test1RootAndSiblingAreAcceptable() {
		FlexoConcept simple = concept("Simple");
		FlexoConcept plain = concept("Plain");
		assertTrue(FMLFIBController.isAcceptableContainer(virtualModel, simple, virtualModel));
		assertTrue(FMLFIBController.isAcceptableContainer(virtualModel, simple, plain));
		// A concept still to be created (the creation wizard)
		assertTrue(FMLFIBController.isAcceptableContainer(virtualModel, null, plain));
		assertTrue(FMLFIBController.isAcceptableContainer(virtualModel, null, virtualModel));
	}

	@Test
	@TestOrder(3)
	public void test2NeverAnotherVirtualModel() {
		FlexoConcept simple = concept("Simple");
		assertFalse(FMLFIBController.isAcceptableContainer(virtualModel, simple, otherVirtualModel));
		// A concept is never accepted relatively to another VirtualModel than its own
		assertFalse(FMLFIBController.isAcceptableContainer(otherVirtualModel, null, simple));
		assertFalse(FMLFIBController.isAcceptableContainer(virtualModel, simple, null));
		assertFalse(FMLFIBController.isAcceptableContainer(null, simple, virtualModel));
		// A VirtualModel has no container
		assertFalse(FMLFIBController.isAcceptableContainer(virtualModel, virtualModel, virtualModel));
	}

	@Test
	@TestOrder(4)
	public void test3NeverItselfNorANestedConcept() {
		FlexoConcept simple = concept("Simple");
		FlexoConcept plain = concept("Plain");
		assertFalse(FMLFIBController.isAcceptableContainer(virtualModel, simple, simple));
		// Nest Plain in Simple: Plain is then nested in Simple, and cannot contain it
		simple.addToContents(plain);
		try {
			assertSame(simple, plain.getContainer());
			assertFalse(FMLFIBController.isAcceptableContainer(virtualModel, simple, plain));
			assertTrue(FMLFIBController.isAcceptableContainer(virtualModel, plain, virtualModel));
		} finally {
			virtualModel.addToContents(plain);
		}
		assertSame(virtualModel, plain.getContainer());
		assertTrue(FMLFIBController.isAcceptableContainer(virtualModel, simple, plain));
	}

	/** A move through the browser never announces a null owner, not even transiently */
	@Test
	@TestOrder(5)
	public void test4MoveNeverAnnouncesANullOwner() {
		FlexoConcept simple = concept("Simple");
		FlexoConcept plain = concept("Plain");
		FMLFIBController controller = new FMLFIBController(null, null);
		List<Object> owners = new ArrayList<>();
		PropertyChangeListener listener = evt -> {
			if (FlexoConcept.OWNER_KEY.equals(evt.getPropertyName())) {
				owners.add(evt.getNewValue());
			}
		};
		plain.getPropertyChangeSupport().addPropertyChangeListener(listener);
		try {
			assertTrue(controller.canMoveFlexoConcept(plain, simple));
			controller.moveFlexoConcept(plain, simple);
			assertSame(simple, plain.getContainer());
			assertTrue(simple.getContents().contains(plain));
			assertFalse(virtualModel.getContents().contains(plain));
			// Moving it back to the root
			assertTrue(controller.canMoveFlexoConcept(plain, virtualModel));
			controller.moveFlexoConcept(plain, virtualModel);
			assertSame(virtualModel, plain.getContainer());
			assertTrue(virtualModel.getContents().contains(plain));
			// A refused move changes nothing
			assertFalse(controller.canMoveFlexoConcept(plain, otherVirtualModel));
			controller.moveFlexoConcept(plain, otherVirtualModel);
			assertSame(virtualModel, plain.getContainer());
		} finally {
			plain.getPropertyChangeSupport().removePropertyChangeListener(listener);
		}
		assertFalse("A null owner was announced: " + owners, owners.contains(null));
	}

	private static FlexoConcept concept(String name) {
		FlexoConcept concept = virtualModel.getFlexoConcept(name);
		assertNotNull("No concept " + name, concept);
		return concept;
	}
}
