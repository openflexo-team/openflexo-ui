/**
 * Openflexo is a computer program whose purpose is to provide an open-source, free and
 * open-source model federation platform.
 */

package org.openflexo.fml.rt.controller.view;

import java.util.logging.Logger;

import org.openflexo.fib.binding.FMLControlledComponent;
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

		FIBComponent component = FMLControlledComponent.loadUIComponent(instance.getFlexoConcept(),
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
}
