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
package org.openflexo.fib.binding;

import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rm.FIBComponentResource;
import org.openflexo.gina.controller.CustomTypeEditorProvider;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.model.FIBModelFactory;
import org.openflexo.gina.model.FIBVariable;

/**
 * Loads and binds the GINA components a {@link FlexoConcept} drives - the <code>.fib</code> and <code>.inspector</code> stored in the
 * <code>Xxx.fml/</code> container beside the FML source.
 *
 * <p>
 * This replaces the <code>gina-ta</code> bridge, where the component was reached through a <code>FIBComponentModelSlot</code> pointing at
 * it by raw URI. There is no model slot here and no technology adapter: the component is found by convention (see
 * {@link FlexoConcept#getUIComponentResource()}), and what the model slot used to carry - the binding context - is set up by
 * {@link #bindToConcept(FIBComponent, FlexoConcept, CustomTypeEditorProvider)}.
 *
 * <p>
 * Deliberately free of any dependency on a Swing controller, so that the mechanism can be exercised headlessly.
 *
 * @author sylvain
 */
public class FMLControlledComponent {

	private static final Logger logger = Logger.getLogger(FMLControlledComponent.class.getPackage().getName());

	/**
	 * Name of the variable giving the bindings of a driven component typed access to the inspected FlexoConceptInstance.<br>
	 * Same name and same meaning as the variable the generated inspector tabs declare, so that a legacy inspector translated into a
	 * container component keeps its bindings unchanged.
	 */
	public static final String CONCEPT_INSTANCE_VARIABLE = "fci";

	/**
	 * Load the user interface component of supplied concept, bound and ready to be shown, or null when its container ships none.
	 */
	public static FIBComponent loadUIComponent(FlexoConcept concept, CustomTypeEditorProvider customTypeEditorProvider) {
		return loadUIComponent(concept, FlexoConcept.DEFAULT_VARIANT, customTypeEditorProvider);
	}

	/**
	 * Load a named <b>variant</b> of the user interface of supplied concept - see {@link FlexoConcept#getUIComponentResource(String)}.
	 */
	public static FIBComponent loadUIComponent(FlexoConcept concept, String variant, CustomTypeEditorProvider customTypeEditorProvider) {
		return load(concept, concept != null ? concept.getUIComponentFlexoResource(variant) : null, customTypeEditorProvider);
	}

	/**
	 * Load the inspector component of supplied concept, bound and ready to be shown, or null when its container ships none.
	 */
	public static FIBComponent loadInspectorComponent(FlexoConcept concept, CustomTypeEditorProvider customTypeEditorProvider) {
		return load(concept, concept.getInspectorComponentFlexoResource(), customTypeEditorProvider);
	}

	private static FIBComponent load(FlexoConcept concept, FIBComponentResource componentResource,
			CustomTypeEditorProvider customTypeEditorProvider) {

		if (concept == null || componentResource == null) {
			return null;
		}

		// The resource owns the deserialization: it roots the model factory at the Xxx.fml/ container, declares
		// FIBInspector, and - the one that fails obscurely when missing - builds the type converter from the technology
		// adapter service, without which the FML types a driven component declares do not resolve.
		FIBComponent returned = componentResource.getComponent();

		if (returned == null) {
			logger.warning("Could not load component " + componentResource.getURI() + " driven by " + concept);
			return null;
		}

		bindToConcept(returned, concept, customTypeEditorProvider);

		return returned;
	}

	/**
	 * Make supplied component speak FML about supplied concept: its bindings are parsed against the VirtualModel declaring that concept,
	 * and a <code>fci</code> variable typed by the concept gives them typed access to the inspected FlexoConceptInstance.
	 *
	 * <p>
	 * One consequence is easy to misdiagnose: expressions are then parsed by the <b>FML</b> parser, under which a path element starting
	 * with a capital is read as a type name. <b>Name the widgets of a driven component in lowerCamelCase</b>, or every binding going
	 * through them fails to parse.
	 *
	 * @param customTypeEditorProvider
	 *            may be null in a headless context
	 */
	public static void bindToConcept(FIBComponent component, FlexoConcept concept, CustomTypeEditorProvider customTypeEditorProvider) {

		if (component == null || concept == null) {
			return;
		}

		VirtualModel virtualModel = concept.getDeclaringCompilationUnit() != null ? concept.getDeclaringCompilationUnit().getVirtualModel()
				: null;

		if (virtualModel != null) {
			component.setBindingFactory(new FMLFIBBindingFactory(virtualModel));
		}

		if (concept.getServiceManager() != null) {
			// Same manager the component was deserialized with: its bindings resolve FML types at runtime too
			component.setCustomTypeManager(concept.getServiceManager().getTechnologyAdapterService());
		}
		else {
			logger.warning("No declaring VirtualModel for " + concept + ": its component keeps Java binding rules");
		}

		if (customTypeEditorProvider != null) {
			component.setCustomTypeEditorProvider(customTypeEditorProvider);
		}

		// The component is shown on an instance of THIS concept, so that is what 'data' is - not the bare
		// FlexoConceptInstance its dataClassName declares. This is what the model slot's assignments used to do, and
		// what makes a binding like 'data.someRole' resolve, in the editor as well as at runtime.
		FIBVariable<?> dataVariable = component.getVariable(FIBComponent.DEFAULT_DATA_VARIABLE);
		if (dataVariable == null) {
			dataVariable = factoryOf(component).newFIBVariable(component, FIBComponent.DEFAULT_DATA_VARIABLE, concept.getInstanceType());
			component.addToVariables(dataVariable);
		}
		else {
			dataVariable.setType(concept.getInstanceType());
		}

		// Kept beside 'data', and pointing at it: the inspector tabs the platform generates from the deprecated
		// FlexoConceptInspector declare 'fci', so an inspector translated into a container component keeps its bindings.
		if (component.getVariable(CONCEPT_INSTANCE_VARIABLE) == null) {
			FIBVariable<?> conceptInstanceVariable = factoryOf(component).newFIBVariable(component, CONCEPT_INSTANCE_VARIABLE,
					concept.getInstanceType());
			conceptInstanceVariable.setValue(new DataBinding<>(FIBComponent.DEFAULT_DATA_VARIABLE));
			component.addToVariables(conceptInstanceVariable);
		}

		// Types have changed, so every binding has to be revalidated against them
		component.revalidateBindings();
	}

	private static FIBModelFactory factoryOf(FIBComponent component) {
		return component.getModelFactory();
	}
}
