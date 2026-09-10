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
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.fml.gina.action.CreateInspector;
import org.openflexo.fml.gina.action.InspectorEntryConfiguration;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.fml.Visibility;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * When the action creating an inspector is offered, and when it is not.
 *
 * <p>
 * The rule that matters: a concept must not be given a SECOND inspector - editing the one it has is the editor's job - but a concept that
 * merely INHERITS one from a parent must still be able to declare its own.
 */
@RunWith(OrderedRunner.class)
public class TestCreateInspectorAvailability extends OpenflexoTestCase {

	private static final String FIXTURE_URI = "http://openflexo.org/test/TestResourceCenter/TestContainerUI.fml";

	private static VirtualModel virtualModel;
	private static FlexoEditor editor;

	@Test
	@TestOrder(1)
	public void test0LoadFixture() {

		instanciateTestServiceManager();
		editor = new DefaultFlexoEditor(null, serviceManager);
		CompilationUnitResource resource = serviceManager.getVirtualModelLibrary().getCompilationUnitResource(FIXTURE_URI);
		assertNotNull("No compilation unit for " + FIXTURE_URI, resource);

		virtualModel = resource.getCompilationUnit().getVirtualModel();
		assertNotNull(virtualModel);
		assertEquals("The fixture did not parse", 11, virtualModel.getFlexoConcepts().size());
	}

	/** Simple ships Simple.inspector: no second one. */
	@Test
	@TestOrder(2)
	public void test1DisabledWhenTheConceptAlreadyHasOne() {

		FlexoConcept simple = concept("Simple");
		assertNotNull(simple.getInspectorComponentResource());

		assertTrue(CreateInspector.actionType.isVisibleForSelection(simple, null));
		assertFalse("A concept with an inspector must not be offered a second one",
				CreateInspector.actionType.isEnabledForSelection(simple, null));
	}

	/** A concept with none is offered the action. */
	@Test
	@TestOrder(3)
	public void test2EnabledWhenTheConceptHasNone() {

		FlexoConcept without = concept("WithoutAnyComponent");

		assertTrue(CreateInspector.actionType.isVisibleForSelection(without, null));
		assertTrue(CreateInspector.actionType.isEnabledForSelection(without, null));
	}

	/**
	 * The case a naive check gets wrong: InheritingFromSimple has no inspector of its OWN, it only resolves to its parent's. Refusing the
	 * action there would make it impossible to ever give a child concept its own inspector.
	 */
	@Test
	@TestOrder(4)
	public void test3EnabledWhenTheInspectorIsOnlyInherited() {

		FlexoConcept inheriting = concept("InheritingFromSimple");

		// It does resolve one - the parent's
		assertNotNull(inheriting.getInspectorComponentResource());

		assertTrue("A concept inheriting an inspector must still be able to declare its own",
				CreateInspector.actionType.isEnabledForSelection(inheriting, null));
	}

	/** A VirtualModel is a FlexoConcept, and TestContainerUI.inspector is its own. */
	@Test
	@TestOrder(5)
	public void test4VirtualModelFollowsTheSameRule() {

		assertNotNull(virtualModel.getInspectorComponentResource());
		assertFalse(CreateInspector.actionType.isEnabledForSelection(virtualModel, null));
	}

	/**
	 * Every accessible property is offered, but only the public ones are checked: the rest are internals of the concept, which an inspector
	 * of its instances has no business showing unless asked.
	 */
	@Test
	@TestOrder(6)
	public void test5OnlyPublicPropertiesAreCheckedByDefault() {

		CreateInspector action = CreateInspector.actionType.makeNewAction(concept("Simple"), null, editor);

		int publicProperties = 0;
		int others = 0;

		for (InspectorEntryConfiguration entry : action.getEntries()) {
			assertEquals("Wrong default for " + entry.getPropertyName() + " (" + entry.getVisibility() + ")",
					entry.getVisibility() == Visibility.Public, entry.getSelected());
			if (entry.getVisibility() == Visibility.Public) {
				publicProperties++;
			}
			else {
				others++;
			}
		}

		// Both branches must actually occur, or the assertion above proves nothing
		assertTrue("The fixture concept declares no public property", publicProperties > 0);
		assertTrue("The fixture concept declares no non-public property", others > 0);
	}

	/** A plain two-column panel by default; the tabbed panel is opt-in. */
	@Test
	@TestOrder(7)
	public void test6PlainPanelByDefault() {

		CreateInspector action = CreateInspector.actionType.makeNewAction(concept("Simple"), null, editor);

		assertFalse("An inspector must be a plain panel unless asked otherwise", action.getUseTabbedPanel());
	}

	private static FlexoConcept concept(String name) {
		FlexoConcept returned = virtualModel.getFlexoConcept(name);
		assertNotNull("No concept " + name + " in the fixture", returned);
		return returned;
	}
}
