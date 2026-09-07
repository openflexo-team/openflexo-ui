/**
 * Openflexo is a computer program whose purpose is to provide an open-source, free and
 * open-source model federation platform.
 */

package org.openflexo.fml.controller.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.openflexo.fib.binding.FMLControlledComponent;
import org.openflexo.fml.controller.FMLTechnologyAdapterController;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.rm.FMLFIBComponent;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.resource.StreamIODelegate;
import org.openflexo.gina.swing.editor.FIBEditor;
import org.openflexo.gina.swing.editor.controller.FIBEditorController;
import org.openflexo.gina.swing.editor.validation.ValidationPanel;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.view.ModuleView;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.model.FlexoPerspective;

/**
 * The module view of a GINA component stored in the <code>Xxx.fml/</code> container of a VirtualModel: the GINA component editor, opened on
 * that component.
 *
 * <p>
 * The editor is not one widget but a set of them, and it is only usable when they are all present. The reference assembly is
 * <code>JFIBEditor</code> - the standalone window ALT+click opens on any component of the application - and this view reproduces it, mapping
 * its <code>JXMultiSplitPane</code> positions onto the slots of the enclosing {@link FlexoPerspective}:
 * <ul>
 * <li>CENTER, this view itself: the editor panel, with the validation panel below it;</li>
 * <li>BOTTOM_LEFT: the browser of the component's own structure;</li>
 * <li>TOP_RIGHT: the widget palette;</li>
 * <li>BOTTOM_RIGHT: the inspectors of the selected widget.</li>
 * </ul>
 * The library browser <code>JFIBEditor</code> puts TOP_LEFT is deliberately left out: the module's own browser already lists the components.
 *
 * <p>
 * Selecting the <code>.fib</code> / <code>.inspector</code> in a browser reaches this view through the generic resource route -
 * {@link FlexoController} loads the resource and focuses its data, which is the {@link FMLFIBComponent} holder.
 *
 * @author sylvain
 */
@SuppressWarnings("serial")
public class FIBComponentModuleView extends JPanel implements ModuleView<FMLFIBComponent> {

	private static final Logger logger = Logger.getLogger(FIBComponentModuleView.class.getPackage().getName());

	private final FMLFIBComponent representedObject;
	private final FlexoController controller;
	private final FlexoPerspective perspective;
	private final LocalizedDelegate locales;

	private FIBEditorController editorController;
	private ValidationPanel validationPanel;

	public FIBComponentModuleView(FMLFIBComponent representedObject, FlexoController controller, FlexoPerspective perspective,
			LocalizedDelegate locales) {
		super(new BorderLayout());

		this.representedObject = representedObject;
		this.controller = controller;
		this.perspective = perspective;
		this.locales = locales;

		FIBEditor editor = getFIBEditor();

		if (editor == null || representedObject.getComponent() == null) {
			logger.warning("Cannot edit " + representedObject + ": no editor or no component");
			return;
		}

		// Bind BEFORE opening: the editor reads the component's typing space as it builds its views, so 'data' has to be
		// typed by the driving concept by then. Otherwise the editor shows it as the bare FlexoConceptInstance the
		// dataClassName declares, and every binding on a role of the concept reads as unresolved.
		FlexoConcept drivingConcept = representedObject.getResource().getDrivingConcept();
		if (drivingConcept != null) {
			FMLControlledComponent.bindToConcept(representedObject.getComponent(), drivingConcept,
					controller.getApplicationContext() != null ? controller.getApplicationContext().getTechnologyAdapterControllerService()
							: null);
		}

		editorController = editor.openFIBComponent(representedObject.getResource().getIODelegate().getSerializationArtefactAsResource(),
				representedObject.getComponent(), null, controller.getFlexoFrame());

		add(editorController.getEditorPanel(), BorderLayout.CENTER);
		add(makeSouthPanel(editor), BorderLayout.SOUTH);
	}

	/**
	 * The validation report of the edited component, and the actions on it.
	 */
	private JPanel makeSouthPanel(FIBEditor editor) {

		JPanel southPanel = new JPanel(new BorderLayout());

		validationPanel = new ValidationPanel(editorController, editor.getFIBLibrary(), FIBEditor.EDITOR_LOCALIZATION) {
			@Override
			protected void performSelect(ValidationIssue<?, ?> validationIssue) {
				if (validationIssue != null && validationIssue.getValidable() instanceof org.openflexo.gina.model.FIBModelObject) {
					editorController.setSelectedObject((org.openflexo.gina.model.FIBModelObject) validationIssue.getValidable());
				}
			}
		};
		southPanel.add(validationPanel, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		JButton localizeButton = new JButton(locales.localizedForKey("localize"));
		localizeButton.addActionListener(e -> getFIBEditor().localizeFIB(editorController.getEditedComponent(),
				controller.getFlexoFrame()));
		buttonPanel.add(localizeButton);

		JButton saveButton = new JButton(locales.localizedForKey("save"));
		saveButton.addActionListener(e -> save());
		buttonPanel.add(saveButton);

		southPanel.add(buttonPanel, BorderLayout.SOUTH);

		return southPanel;
	}

	/**
	 * Save the edited component back to the artefact it came from.
	 *
	 * <p>
	 * {@link StreamIODelegate#setSaveToSourceResource(boolean)} matters here: without it the component is written to wherever the resource
	 * center was READ from - under <code>build/</code> for a project run from Gradle - and the edit is lost on the next build.
	 */
	public void save() {
		try {
			if (representedObject.getResource().getIODelegate() instanceof StreamIODelegate) {
				((StreamIODelegate<?>) representedObject.getResource().getIODelegate()).setSaveToSourceResource(true);
			}
			representedObject.getResource().save();
		} catch (SaveResourceException e) {
			logger.log(Level.WARNING, "Could not save " + representedObject.getResource().getURI(), e);
			controller.notify(locales.localizedForKey("could_not_save_component"));
		}
	}

	/**
	 * The shared editor, built synchronously on the calling thread.
	 *
	 * <p>
	 * <b>Never ask for the task-based variant from here.</b> A module view is created inside
	 * <code>synchronized (flexoFrame.getTreeLock())</code>, and building the editor creates Swing widgets - the palette and the inspectors -
	 * which need that same AWT tree lock. Scheduling that work on the task manager and waiting for it therefore freezes the application.
	 * Every view of the former <code>gina-ta</code> called <code>getFIBEditor(false)</code> for exactly this reason.
	 */
	private FIBEditor getFIBEditor() {
		FMLTechnologyAdapter fmlTA = controller.getApplicationContext().getTechnologyAdapterService()
				.getTechnologyAdapter(FMLTechnologyAdapter.class);
		FMLTechnologyAdapterController adapterController = controller.getApplicationContext().getTechnologyAdapterControllerService()
				.getTechnologyAdapterController(fmlTA);
		return adapterController != null ? adapterController.getFIBEditor(false) : null;
	}

	@Override
	public FMLFIBComponent getRepresentedObject() {
		return representedObject;
	}

	@Override
	public FlexoPerspective getPerspective() {
		return perspective;
	}

	@Override
	public void deleteModuleView() {
		if (editorController != null) {
			getFIBEditor().disactivate(editorController);
			editorController = null;
		}
		controller.removeModuleView(this);
	}

	@Override
	public void show(FlexoController controller, FlexoPerspective perspective) {
		// Nothing specific: the editor panel is already this view's content
	}

	@Override
	public boolean isAutoscrolled() {
		return false;
	}

	@Override
	public void willShow() {

		FIBEditor editor = getFIBEditor();
		if (editor == null || editorController == null) {
			return;
		}

		// The editor is shared between components; re-point its widgets at the one being shown
		editor.activate(editorController);

		perspective.setBottomLeftView(editorController.getEditorBrowser());
		perspective.setTopRightView(editor.getPalettes());
		perspective.setBottomRightView(editor.getInspectors() != null ? editor.getInspectors().getPanelGroup() : null);

		// Filling the slots is not enough: FlexoMainPane hides a whole column when the controller model says so, and
		// the FML views one navigates here from - StandardCompilationUnitView, VirtualModelInstanceView - all turn the
		// right column OFF in their own willShow(). Without this the palette and the inspectors are built, attached,
		// and invisible.
		controller.getControllerModel().setLeftViewVisible(true);
		controller.getControllerModel().setRightViewVisible(true);
	}

	@Override
	public void willHide() {
		perspective.setBottomLeftView(null);
		perspective.setTopRightView(null);
		perspective.setBottomRightView(null);
		controller.getControllerModel().setRightViewVisible(false);
	}
}
