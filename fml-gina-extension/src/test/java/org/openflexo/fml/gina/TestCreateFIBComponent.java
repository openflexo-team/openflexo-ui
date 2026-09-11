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

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.fml.gina.action.AbstractCreateFIBComponent;
import org.openflexo.fml.gina.action.CreateFIBComponent;
import org.openflexo.fml.gina.action.CreateInspector;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.md.MultiValuedMetaData;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitParser;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.model.FIBContainer;
import org.openflexo.gina.model.container.FIBPanel;
import org.openflexo.gina.utils.FIBInspector;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * The action creating a user interface component - a <code>.fib</code> - for a FlexoConcept or its VirtualModel, and the refactoring that
 * came with it: the inspector action builds its component through the same base, {@link AbstractCreateFIBComponent}.
 *
 * <p>
 * Everything here runs in memory. {@link AbstractCreateFIBComponent#buildComponent()} and {@link CreateFIBComponent#declareVariant} write no
 * file, and the FML source a declaration would save is checked through {@link FMLCompilationUnit#getFMLPrettyPrint()} - what
 * <code>CompilationUnitResource.save()</code> writes - then parsed back. What is not covered: the action run end to end, which writes a
 * component into the resource center, and the wizard, a Swing dialog.
 *
 * <p>
 * Nor are the WIDGETS covered: the widget of each property is asked of the technology adapter controllers, which only an application
 * context provides, and the test one (<code>TestApplicationContext</code>) needs a display - it raises a HeadlessException under the
 * headless <code>test</code> task. What is asserted instead is the label of each selected property, which is what the selection decides.
 */
@RunWith(OrderedRunner.class)
public class TestCreateFIBComponent extends OpenflexoTestCase {

	private static final String FIXTURE_URI = "http://openflexo.org/test/TestResourceCenter/TestContainerUI.fml";

	private static VirtualModel virtualModel;
	private static FMLCompilationUnit compilationUnit;
	private static FlexoEditor editor;

	@Test
	@TestOrder(1)
	public void test0LoadFixture() {

		instanciateTestServiceManager();
		editor = new DefaultFlexoEditor(null, serviceManager);
		CompilationUnitResource resource = serviceManager.getVirtualModelLibrary().getCompilationUnitResource(FIXTURE_URI);
		assertNotNull("No compilation unit for " + FIXTURE_URI, resource);

		compilationUnit = resource.getCompilationUnit();
		virtualModel = compilationUnit.getVirtualModel();
		assertNotNull(virtualModel);
		assertEquals("The fixture did not parse", 11, virtualModel.getFlexoConcepts().size());
	}

	/** Unlike the inspector, offered even to a concept that already shows a component: a concept may drive several. */
	@Test
	@TestOrder(2)
	public void test1OfferedEvenToAConceptThatHasAComponent() {

		FlexoConcept simple = concept("Simple");
		assertNotNull(simple.getUIComponentResource());

		assertTrue(CreateFIBComponent.actionType.isVisibleForSelection(simple, null));
		assertTrue("A concept already showing a component must still be offered another",
				CreateFIBComponent.actionType.isEnabledForSelection(simple, null));

		// A VirtualModel is a FlexoConcept, and a compilation unit is offered the action of its VirtualModel
		assertTrue(CreateFIBComponent.actionType.isEnabledForSelection(virtualModel, null));
		assertTrue(CreateFIBComponent.actionType.isVisibleForSelection(compilationUnit, null));
		assertTrue(CreateFIBComponent.actionType.isEnabledForSelection(compilationUnit, null));
	}

	/** <Concept>.fib - the default view by convention - unless the container holds it already. */
	@Test
	@TestOrder(3)
	public void test2DefaultNameFollowsTheConventionUnlessTaken() {

		assertEquals("WithoutAnyComponent.fib", newAction(concept("WithoutAnyComponent")).getComponentName());

		CreateFIBComponent onSimple = newAction(concept("Simple"));
		assertEquals("Simple.fib is taken: the next free name is proposed", "Simple2.fib", onSimple.getComponentName());
		assertTrue(onSimple.isValid());

		onSimple.setComponentName("Simple.fib");
		assertTrue(onSimple.nameIsAlreadyTaken());
		assertFalse(onSimple.isValid());

		onSimple.setComponentName("Simple2.inspector");
		assertFalse("A user interface is a .fib", onSimple.hasValidComponentName());
	}

	/** A plain two-column panel - not an inspector - whose data is typed by the concept, and whose bindings resolve once bound. */
	@Test
	@TestOrder(4)
	public void test3BuildsAPanelTypedByTheConcept() throws Exception {

		FlexoConcept simple = concept("Simple");
		FIBComponent component = newAction(simple).buildComponent();

		assertTrue(component instanceof FIBPanel);
		assertFalse("A user interface is not an inspector", component instanceof FIBInspector);
		assertEquals("Simple2", component.getName());
		assertEquals("org.openflexo.view.controller.FlexoFIBController", component.getControllerClassName());
		assertEquals(simple.getInstanceType(), component.getVariable(FIBComponent.DEFAULT_DATA_VARIABLE).getType());

		assertNotNull("No label for the public property 'description'", ((FIBContainer) component).getSubComponentNamed("descriptionLabel"));
		assertNull("Only the public properties are proposed by default", ((FIBContainer) component).getSubComponentNamed("nameLabel"));
	}

	/** The refactoring: the inspector is still an inspector, and its tabbed layout still lands in the TabPanel the platform merges by. */
	@Test
	@TestOrder(5)
	public void test4InspectorIsStillBuiltAsAnInspector() throws Exception {

		FlexoConcept simple = concept("Simple");

		FIBComponent plain = CreateInspector.actionType.makeNewAction(simple, null, editor).buildComponent();
		assertTrue(plain instanceof FIBInspector);
		assertEquals("SimpleInspector", plain.getName());
		assertEquals("org.openflexo.inspector.FIBInspectorController", plain.getControllerClassName());
		assertEquals(simple.getInstanceType(), plain.getVariable(FIBComponent.DEFAULT_DATA_VARIABLE).getType());
		assertNotNull(((FIBContainer) plain).getSubComponentNamed("descriptionLabel"));

		CreateInspector tabbedAction = CreateInspector.actionType.makeNewAction(simple, null, editor);
		tabbedAction.setUseTabbedPanel(true);
		FIBInspector tabbed = (FIBInspector) tabbedAction.buildComponent();
		assertNotNull("No TabPanel in a tabbed inspector", tabbed.getTabPanel());
		assertEquals("Tab", tabbed.getTabPanel().getName());
		FIBComponent tab = tabbed.getTabPanel().getSubComponentNamed("SimpleTab");
		assertNotNull(tab);
		assertNotNull("The entries are not in the tab", ((FIBContainer) tab).getSubComponentNamed("descriptionLabel"));
	}

	/** A variant name is an FML annotation key, and must not replace a variant the concept declares already. */
	@Test
	@TestOrder(6)
	public void test5VariantNameIsValidated() {

		CreateFIBComponent action = newAction(concept("WithVariants"));
		action.setDeclareAsVariant(true);

		action.setVariantName("compact");
		assertTrue(action.variantIsAlreadyDeclared());
		assertFalse(action.isValid());

		action.setVariantName("2compact");
		assertFalse(action.hasValidVariantName());
		assertFalse(action.isValid());

		action.setVariantName("large");
		assertFalse(action.variantIsAlreadyDeclared());
		assertTrue(action.isValid());

		// A single-valued @UI("…") declares the default variant
		CreateFIBComponent onAnnotated = newAction(concept("Annotated"));
		onAnnotated.setDeclareAsVariant(true);
		onAnnotated.setVariantName("default");
		assertTrue(onAnnotated.variantIsAlreadyDeclared());
	}

	/**
	 * Declaring a variant rewrites the FML source: nothing else in it may change, and what would be saved has to parse back with the
	 * variant declared.
	 */
	@Test
	@TestOrder(7)
	public void test6DeclaringAVariantChangesOnlyTheAnnotation() throws Exception {

		String before = compilationUnit.getFMLPrettyPrint();
		// Otherwise any difference below would be the printer's, not the declaration's
		assertSameLines("The pretty print of the unchanged compilation unit is not stable", withoutAnnotations(before),
				withoutAnnotations(compilationUnit.getFMLPrettyPrint()));

		FlexoConcept concept = concept("WithoutAnyComponent");
		CreateFIBComponent action = newAction(concept);
		action.setComponentName("WithoutAnyComponentCompact.fib");
		action.setDeclareAsVariant(true);
		action.setVariantName("compact");
		assertTrue(action.isValid());

		action.declareVariant(concept);
		assertEquals("WithoutAnyComponentCompact.fib", concept.getMultiValuedMetaData(FlexoConcept.UI_METADATA).getValue("compact", String.class));

		String after = compilationUnit.getFMLPrettyPrint();
		assertTrue("The FML source does not declare the variant:\n" + after, after.contains("WithoutAnyComponentCompact.fib"));


		FMLCompilationUnit reparsed = new FMLCompilationUnitParser().parse(after, compilationUnit.getFMLModelFactory(),
				(modelSlotClasses) -> null, false);
		FlexoConcept reparsedConcept = reparsed.getVirtualModel().getFlexoConcept("WithoutAnyComponent");
		assertNotNull(reparsedConcept);
		MultiValuedMetaData reparsedMetaData = reparsedConcept.getMultiValuedMetaData(FlexoConcept.UI_METADATA);
		assertNotNull("The saved source would not declare the variant any more once parsed back", reparsedMetaData);
		assertEquals("WithoutAnyComponentCompact.fib", reparsedMetaData.getValue("compact", String.class));

		assertSameLines("Declaring a variant changed something else than the @UI annotations of the FML source", withoutAnnotations(before),
				withoutAnnotations(after));
	}

	/**
	 * A single-valued @UI("CustomScreen.fib") names the DEFAULT view: adding a variant converts it into @UI(default="CustomScreen.fib", …),
	 * and the default view stays the one it was.
	 */
	@Test
	@TestOrder(8)
	public void test7ASingleValuedDeclarationKeepsItsDefault() {

		FlexoConcept annotated = concept("Annotated");
		CreateFIBComponent action = newAction(annotated);
		action.setComponentName("AnnotatedCompact.fib");
		action.setDeclareAsVariant(true);
		action.setVariantName("compact");
		assertTrue(action.isValid());

		action.declareVariant(annotated);

		MultiValuedMetaData metaData = annotated.getMultiValuedMetaData(FlexoConcept.UI_METADATA);
		assertNotNull("The single-valued @UI was not converted", metaData);
		assertEquals("CustomScreen.fib", metaData.getValue(FlexoConcept.DEFAULT_VARIANT, String.class));
		assertEquals("AnnotatedCompact.fib", metaData.getValue("compact", String.class));

		assertNotNull(annotated.getUIComponentResource());
		assertTrue("The default view changed: " + annotated.getUIComponentResource().getURI(),
				annotated.getUIComponentResource().getURI().endsWith("CustomScreen.fib"));
	}

	private static CreateFIBComponent newAction(FMLObject focusedObject) {
		return CreateFIBComponent.actionType.makeNewAction(focusedObject, null, editor);
	}

	private static FlexoConcept concept(String name) {
		FlexoConcept returned = virtualModel.getFlexoConcept(name);
		assertNotNull("No concept " + name + " in the fixture", returned);
		return returned;
	}

	/** A line of FML source, and whether it directly follows a <code>@UI</code> annotation - see {@link #withoutAnnotations(String)}. */
	private static class SourceLine {
		private final String text;
		private final boolean followsAnnotation;

		private SourceLine(String text, boolean followsAnnotation) {
			this.text = text;
			this.followsAnnotation = followsAnnotation;
		}
	}

	/**
	 * Compare line by line, reporting the first difference with its context rather than two whole files. Exact, except for the indentation
	 * of a line that follows an annotation in either text - see {@link #withoutAnnotations(String)}.
	 */
	private static void assertSameLines(String message, List<SourceLine> expected, List<SourceLine> actual) {
		int max = Math.max(expected.size(), actual.size());
		for (int i = 0; i < max; i++) {
			SourceLine e = i < expected.size() ? expected.get(i) : null;
			SourceLine a = i < actual.size() ? actual.get(i) : null;
			boolean same;
			if (e == null || a == null) {
				same = false;
			}
			else if (e.followsAnnotation || a.followsAnnotation) {
				same = e.text.replaceAll("^\\s+", "").equals(a.text.replaceAll("^\\s+", ""));
			}
			else {
				same = e.text.equals(a.text);
			}
			if (!same) {
				StringBuilder context = new StringBuilder();
				for (int j = Math.max(0, i - 3); j <= Math.min(max - 1, i + 3); j++) {
					context.append(String.format("%n  %3d  expected: [%s]%n       actual:   [%s]", j, j < expected.size() ? expected.get(j).text : "<none>",
							j < actual.size() ? actual.get(j).text : "<none>"));
				}
				org.junit.Assert.fail(message + " - first difference at line " + i + " (" + expected.size() + " vs " + actual.size()
						+ " lines):" + context);
			}
		}
	}

	/**
	 * The lines of supplied FML source minus its <code>@UI</code> annotations, each line remembering whether it directly followed one.
	 *
	 * <p>
	 * The indentation of such a line is the one tolerance of the comparison, and it is a known defect of the pretty-printer, not of the
	 * declaration: a child inserted in front of an indented node - an annotation added to a concept - takes the place of the node's first
	 * fragment, AFTER its indentation, so the declaration that follows restarts at column 0. The source stays valid FML. See "Inserting
	 * a child in front of an indented node" in <code>openflexo-utils/flexo-p2pp/KNOWN_DEFECTS.md</code>. Every other line is compared
	 * exactly.
	 */
	private static List<SourceLine> withoutAnnotations(String fml) {
		List<SourceLine> returned = new ArrayList<>();
		boolean followsAnnotation = false;
		for (String line : fml.split("\n")) {
			if (line.contains("@UI")) {
				followsAnnotation = true;
				continue;
			}
			returned.add(new SourceLine(line, followsAnnotation));
			followsAnnotation = false;
		}
		return returned;
	}
}
