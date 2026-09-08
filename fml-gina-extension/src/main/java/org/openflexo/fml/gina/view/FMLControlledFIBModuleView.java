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

package org.openflexo.fml.gina.view;

import java.awt.FlowLayout;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openflexo.fib.binding.FMLControlledComponent;
import org.openflexo.fml.gina.FMLGINAPlugin;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.view.FIBModuleView;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.model.FlexoPerspective;

/**
 * The module view of a {@link FlexoConceptInstance} whose concept drives a user interface stored in the <code>Xxx.fml/</code> container of
 * its VirtualModel.
 *
 * <p>
 * This replaces the <code>gina-ta</code> bridge, and differs from it in what decides that a view exists: not a
 * <code>FIBComponentModelSlot</code> and the four <code>FMLControlledFIB*Nature</code>s, but simply whether the container ships a
 * <code>&lt;ConceptName&gt;.fib</code>. Since a VirtualModel is a {@link FlexoConcept}, the same class serves a VirtualModelInstance.
 *
 * <p>
 * Because it hangs off the FML-RT adapter controller rather than off a dedicated perspective, every module gets it without declaring
 * anything.
 *
 * @author sylvain
 */
@SuppressWarnings("serial")
public class FMLControlledFIBModuleView extends FIBModuleView<FlexoConceptInstance> {

	private static final Logger logger = Logger.getLogger(FMLControlledFIBModuleView.class.getPackage().getName());

	private final FlexoPerspective perspective;

	/**
	 * Build the view of supplied instance, or return null when its concept drives no component.
	 *
	 * <p>
	 * A factory method rather than a constructor: whether a view exists is decided by the presence of a component in the container, which
	 * can only be answered by loading it.
	 */
	public static FMLControlledFIBModuleView viewFor(FlexoConceptInstance instance, FlexoController controller,
			FlexoPerspective perspective, LocalizedDelegate locales) {

		if (instance == null || instance.getFlexoConcept() == null || controller == null) {
			return null;
		}

		FMLGINAPlugin plugin = controller.getApplicationContext().getTechnologyAdapterControllerService().getPlugin(FMLGINAPlugin.class);

		FIBComponent component = FMLControlledComponent.loadUIComponent(instance.getFlexoConcept(),
				plugin != null ? plugin.getSelectedVariant(instance.getFlexoConcept()) : FlexoConcept.DEFAULT_VARIANT,
				controller.getApplicationContext() != null ? controller.getApplicationContext().getTechnologyAdapterControllerService()
						: null);

		if (component == null) {
			return null;
		}

		logger.fine("Building container-driven module view for " + instance);

		// The component and the dictionaries now live in the same container, so a VirtualModel localizes its own user
		// interface through its Localized/ directory rather than through the module's dictionaries.
		LocalizedDelegate componentLocales = locales;
		if (instance.getFlexoConcept().getDeclaringCompilationUnit() != null
				&& instance.getFlexoConcept().getDeclaringCompilationUnit().getLocalizedDictionary() != null) {
			componentLocales = instance.getFlexoConcept().getDeclaringCompilationUnit().getLocalizedDictionary();
		}

		return new FMLControlledFIBModuleView(instance, controller, component, perspective, componentLocales);
	}

	private FMLControlledFIBModuleView(FlexoConceptInstance instance, FlexoController controller, FIBComponent component,
			FlexoPerspective perspective, LocalizedDelegate locales) {
		super(instance, controller, component, locales, false);
		this.perspective = perspective;
	}

	@Override
	public FlexoPerspective getPerspective() {
		return perspective;
	}

	/**
	 * A concept may drive several named user interfaces (<code>@UI(default=…, compact=…)</code>). When it does, the reader chooses which one
	 * is shown, from a selector placed above the view.
	 *
	 * <p>
	 * Switching rebuilds the view rather than swapping the component in place: a {@link FIBModuleView} is built around one component and does
	 * not support exchanging it. Dropping the cached view and re-selecting the instance is what the framework offers.
	 */
	/**
	 * Deliberately in show() rather than willShow(): <code>FlexoMainPane.setModuleView</code> calls willShow() BEFORE adding the view to its
	 * container, and show() after - the same ordering that matters for the component editor.
	 */
	@Override
	public void show(FlexoController controller, FlexoPerspective perspective) {

		List<String> variants = getRepresentedObject().getFlexoConcept().getUIComponentVariants();

		perspective.setTopCenterView(variants.size() > 1 ? makeVariantSelector(variants) : null);
	}

	@Override
	public void willHide() {
		perspective.setTopCenterView(null);
	}

	private JComponent makeVariantSelector(List<String> variants) {

		FMLGINAPlugin plugin = getFlexoController().getApplicationContext().getTechnologyAdapterControllerService()
				.getPlugin(FMLGINAPlugin.class);
		FlexoConcept concept = getRepresentedObject().getFlexoConcept();

		JComboBox<String> selector = new JComboBox<>(variants.toArray(new String[variants.size()]));
		selector.setSelectedItem(plugin != null ? plugin.getSelectedVariant(concept) : FlexoConcept.DEFAULT_VARIANT);

		selector.addActionListener(e -> {
			String chosen = (String) selector.getSelectedItem();
			if (plugin == null || chosen == null || chosen.equals(plugin.getSelectedVariant(concept))) {
				return;
			}
			plugin.setSelectedVariant(concept, chosen);
			FlexoConceptInstance instance = getRepresentedObject();
			FlexoController controller = getFlexoController();
			controller.removeModuleView(this);
			controller.selectAndFocusObject(instance);
		});

		JPanel selectorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		selectorPanel.add(new JLabel(plugin != null ? plugin.getLocales().localizedForKey("user_interface") : "user_interface"));
		selectorPanel.add(selector);
		return selectorPanel;
	}
}
