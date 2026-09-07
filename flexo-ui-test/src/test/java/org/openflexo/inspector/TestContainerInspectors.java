/**
 * Openflexo is a computer program whose purpose is to provide an open-source, free and
 * open-source model federation platform.
 */

package org.openflexo.inspector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.TreeSet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.fib.binding.FMLControlledComponent;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.model.FIBContainer;
import org.openflexo.gina.model.FIBModelFactory;
import org.openflexo.gina.model.container.FIBPanel;
import org.openflexo.gina.model.container.FIBTab;
import org.openflexo.gina.model.container.FIBTabPanel;
import org.openflexo.gina.model.FIBVariable;
import org.openflexo.gina.utils.FIBInspector;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationReport;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * Test that the GINA components stored in the <code>Xxx.fml/</code> container of a VirtualModel are loaded and bound to the typing space of
 * the concept that drives them.
 *
 * <p>
 * This is the replacement of the <code>gina-ta</code> bridge, exercised here headlessly: {@link FMLControlledComponent} deliberately carries
 * no dependency on a Swing controller, so what a module view or {@link ModuleInspectorController} will show can be validated without one.
 *
 * <p>
 * The fixture is <code>TestContainerUI.fml</code> of <code>flexo-test-resources</code>; its header lists what its container ships.
 */
@RunWith(OrderedRunner.class)
public class TestContainerInspectors extends OpenflexoTestCase {

	private static final String FIXTURE_URI = "http://openflexo.org/test/TestResourceCenter/TestContainerUI.fml";

	private static VirtualModel virtualModel;

	@Test
	@TestOrder(1)
	public void test0LoadFixture() throws Exception {

		instanciateTestServiceManager();
		assertNotNull(serviceManager);

		CompilationUnitResource resource = serviceManager.getVirtualModelLibrary().getCompilationUnitResource(FIXTURE_URI);
		assertNotNull("No compilation unit for " + FIXTURE_URI, resource);

		virtualModel = resource.getCompilationUnit().getVirtualModel();
		assertNotNull(virtualModel);
		// A failed parse leaves an EMPTY compilation unit behind, which validates with zero errors
		assertEquals("The fixture did not parse", 10, virtualModel.getFlexoConcepts().size());

	}

	/** The inspector of a concept is loaded from the container, and is a genuine FIBInspector. */
	@Test
	@TestOrder(2)
	public void test1ConceptInspectorIsLoadedFromContainer() {

		FlexoConcept simple = concept("Simple");

		FIBComponent component = FMLControlledComponent.loadInspectorComponent(simple, null);

		assertNotNull("No inspector loaded for " + simple, component);
		assertTrue("Expected a FIBInspector, got " + component.getClass(), component instanceof FIBInspector);
		assertEquals("SimpleInspector", component.getName());
	}

	/** Loading installs the FML binding context: a 'fci' variable typed by the concept. */
	@Test
	@TestOrder(3)
	public void test2LoadedComponentIsBoundToTheConcept() {

		FlexoConcept simple = concept("Simple");

		FIBComponent component = FMLControlledComponent.loadInspectorComponent(simple, null);

		FIBVariable<?> conceptInstance = component.getVariable(FMLControlledComponent.CONCEPT_INSTANCE_VARIABLE);
		assertNotNull("No '" + FMLControlledComponent.CONCEPT_INSTANCE_VARIABLE + "' variable on the loaded component", conceptInstance);
		assertEquals(simple.getInstanceType(), conceptInstance.getType());
		assertEquals("data", conceptInstance.getValue().toString());

		// Bindings are parsed by the FML parser, not by the Java one
		assertTrue("The component did not get the FML binding factory",
				component.getBindingFactory() instanceof org.openflexo.fib.binding.FMLFIBBindingFactory);
	}

	/**
	 * <code>data</code> is typed by the concept that drives the component, not by the bare FlexoConceptInstance its <code>dataClassName</code>
	 * declares. This is what the model slot's assignments used to do, and it is what makes <code>data.someRole</code> resolve - in the FIB
	 * editor as well as at runtime.
	 */
	@Test
	@TestOrder(4)
	public void test2bDataIsTypedByTheDrivingConcept() {

		FlexoConcept simple = concept("Simple");

		FIBComponent component = FMLControlledComponent.loadInspectorComponent(simple, null);

		FIBVariable<?> data = component.getVariable(org.openflexo.gina.model.FIBComponent.DEFAULT_DATA_VARIABLE);
		assertNotNull("No 'data' variable on the loaded component", data);
		assertEquals(simple.getInstanceType(), data.getType());
	}

	/** And the reverse resolution, which is what the editor uses to know what it is editing. */
	@Test
	@TestOrder(5)
	public void test2cResourceKnowsTheConceptDrivingIt() {

		FlexoConcept simple = concept("Simple");

		assertEquals(simple, simple.getInspectorComponentFlexoResource().getDrivingConcept());
		assertEquals(simple, simple.getUIComponentFlexoResource().getDrivingConcept());

		// A VirtualModel is a FlexoConcept, and Xxx.fml/Xxx.fib is its own component
		assertEquals(virtualModel, virtualModel.getUIComponentFlexoResource().getDrivingConcept());
	}

	/** Every binding of a container component is valid, in the context it will be shown in. */
	@Test
	@TestOrder(4)
	public void test3EveryBindingOfAContainerComponentIsValid() throws InterruptedException {

		for (FlexoConcept concept : virtualModel.getFlexoConcepts()) {

			FIBComponent component = FMLControlledComponent.loadInspectorComponent(concept, null);
			if (component == null) {
				continue;
			}

			ValidationReport report = component.validate();
			TreeSet<String> invalid = new TreeSet<>();
			for (ValidationError<?, ?> error : report.getAllErrors()) {
				invalid.add(report.getValidationModel().localizedIssueMessage(error));
			}
			assertEquals("Invalid binding(s) in the inspector of " + concept.getName() + ": " + invalid, new TreeSet<String>(), invalid);
		}
	}

	/** The VirtualModel itself is a FlexoConcept, so it gets its own component the same way. */
	@Test
	@TestOrder(5)
	public void test4VirtualModelGetsItsOwnComponents() {

		assertNotNull(FMLControlledComponent.loadInspectorComponent(virtualModel, null));

		FIBComponent ui = FMLControlledComponent.loadUIComponent(virtualModel, null);
		assertNotNull(ui);
		assertEquals("TestContainerUI", ui.getName());
	}

	/** A concept whose container ships nothing yields null rather than an empty component. */
	@Test
	@TestOrder(6)
	public void test5ConceptWithoutComponentLoadsNothing() {

		assertNull(FMLControlledComponent.loadInspectorComponent(concept("WithoutAnyComponent"), null));
		assertNull(FMLControlledComponent.loadUIComponent(concept("WithoutAnyComponent"), null));
	}

	/** An @UI annotation is honoured all the way to the loaded component. */
	@Test
	@TestOrder(7)
	public void test6AnnotatedConceptLoadsTheDeclaredComponent() {

		FIBComponent component = FMLControlledComponent.loadUIComponent(concept("Annotated"), null);
		assertNotNull(component);
		assertEquals("CustomScreen", component.getName());
	}

	/**
	 * The container inspector is merged into the inspector of the FlexoConceptInstance CLASS, rather than shown alone: this is what
	 * {@link ModuleInspectorController} does, and it is why a concept inspector keeps the generic tabs (name, actors, flexoID).
	 *
	 * <p>
	 * {@link FIBContainer#append(FIBContainer)} merges sub-containers by NAME, so the container component must declare its tabs inside a
	 * <code>&lt;TabPanel name="Tab"&gt;</code> - the name every platform inspector uses.
	 */
	@Test
	@TestOrder(8)
	public void test7ContainerInspectorMergesIntoAClassInspector() throws Exception {

		FIBComponent containerInspector = FMLControlledComponent.loadInspectorComponent(concept("Simple"), null);
		assertTrue(containerInspector instanceof FIBContainer);

		// The merge key: without a TabPanel named "Tab", the tabs would be appended beside the platform ones, not into them
		FIBComponent tabPanel = ((FIBContainer) containerInspector).getSubComponentNamed("Tab");
		assertNotNull("The container inspector declares no TabPanel named 'Tab'", tabPanel);
		assertNotNull(((FIBContainer) tabPanel).getSubComponentNamed("SimpleInspectorTab"));

		// A stand-in for the platform inspector of the FlexoConceptInstance class: a TabPanel named "Tab" holding one tab
		FIBModelFactory factory = new FIBModelFactory(null, serviceManager.getTechnologyAdapterService(), FIBInspector.class);
		FIBPanel classInspector = factory.newFIBPanel();
		FIBTabPanel classTabPanel = factory.newInstance(FIBTabPanel.class);
		classTabPanel.setName("Tab");
		FIBTab basicTab = factory.newFIBTab();
		basicTab.setName("BasicTab");
		classTabPanel.addToSubComponents(basicTab);
		classInspector.addToSubComponents(classTabPanel);

		classInspector.append((FIBContainer) containerInspector);

		// The container tab landed INSIDE the existing TabPanel, beside the platform tab
		FIBComponent mergedTabPanel = classInspector.getSubComponentNamed("Tab");
		assertNotNull(mergedTabPanel);
		assertNotNull("The platform tab was lost", ((FIBContainer) mergedTabPanel).getSubComponentNamed("BasicTab"));
		assertNotNull("The container tab was not merged in", ((FIBContainer) mergedTabPanel).getSubComponentNamed("SimpleInspectorTab"));
	}

	private static FlexoConcept concept(String name) {
		FlexoConcept returned = virtualModel.getFlexoConcept(name);
		assertNotNull("No concept " + name + " in the fixture", returned);
		return returned;
	}
}
