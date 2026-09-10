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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.WeakHashMap;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.KeyStroke;

import org.openflexo.fml.gina.action.LocalizeFIBComponent;
import org.openflexo.fml.gina.action.SaveFIBComponent;
import org.openflexo.fml.gina.controller.CreateInspectorInitializer;
import org.openflexo.fml.gina.controller.LocalizeFIBComponentInitializer;
import org.openflexo.fml.gina.controller.SaveFIBComponentInitializer;
import org.openflexo.fml.gina.view.FIBComponentModuleView;
import org.openflexo.fml.gina.view.FMLControlledFIBModuleView;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.rm.FMLFIBComponent;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.task.FlexoTask;
import org.openflexo.foundation.task.Progress;
import org.openflexo.gina.FIBLibrary.FIBLibraryImpl;
import org.openflexo.gina.model.FIBModelObject;
import org.openflexo.gina.swing.editor.FIBEditor;
import org.openflexo.gina.swing.editor.controller.ContextualMenu;
import org.openflexo.gina.swing.editor.controller.FIBEditorController;
import org.openflexo.gina.swing.editor.controller.action.EditorAction;
import org.openflexo.gina.swing.utils.FIBEditorLoadingProgress;
import org.openflexo.module.FlexoModule;
import org.openflexo.view.ModuleView;
import org.openflexo.view.controller.ControllerActionInitializer;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.TechnologyAdapterPluginController;
import org.openflexo.view.controller.model.FlexoPerspective;

/**
 * The FML/GINA extension: the user interfaces a VirtualModel stores in its own <code>Xxx.fml/</code> container.
 *
 * <p>
 * A <code>&lt;ConceptName&gt;.fib</code> found there drives the module view of the instances of that concept, and any component of the
 * container is edited in the GINA component editor, which this plugin hosts. The convention itself, and the
 * {@link org.openflexo.foundation.fml.rm.FIBComponentResource} that makes such a component a first-class resource, live in
 * <code>flexo-foundation</code>: a {@link TechnologyAdapterPluginController} is a purely UI extension point and cannot contribute a resource
 * factory.
 *
 * <p>
 * Modelled on <code>FMLDiagrammingPlugin</code>. Like it, this plugin is found by <code>ServiceLoader</code> through
 * <code>META-INF/services/org.openflexo.view.controller.TechnologyAdapterPluginController</code> and activated by
 * <code>TechnologyAdapterController.activateActivablePlugins()</code>; a module obtains the feature by depending on this project.
 *
 * @author sylvain
 */
public class FMLGINAPlugin extends TechnologyAdapterPluginController<FMLTechnologyAdapter> {

	private static final Logger logger = Logger.getLogger(FMLGINAPlugin.class.getPackage().getName());

	@Override
	public Class<FMLTechnologyAdapter> getTargetTechnologyAdapterClass() {
		return FMLTechnologyAdapter.class;
	}

	@Override
	protected String getLocalizationDirectory() {
		return "FlexoLocalization/FMLGINAPlugin";
	}

	/**
	 * Always. Unlike the diagramming plugin, which bridges two technology adapters and needs the Diagram one activated, this plugin needs
	 * only GINA - and <code>flexo-ui</code>, which every module builds on, is a GINA application.
	 */
	@Override
	public boolean isActivable(FlexoModule<?> module) {
		return true;
	}

	/**
	 * Instantiating an initializer is also what LOADS its action class, whose static block registers it on the classes it applies to - the
	 * mechanism by which a plugin contributes an action.
	 */
	@Override
	protected void initializeActions(ControllerActionInitializer actionInitializer) {
		new CreateInspectorInitializer(actionInitializer);
		new SaveFIBComponentInitializer(actionInitializer);
		new LocalizeFIBComponentInitializer(actionInitializer);
	}

	/**
	 * The objects this plugin renders: a component of a container (edited in the GINA editor), and an instance whose concept drives one
	 * (shown through that component).
	 */
	@Override
	public boolean isRepresentableInModuleView(FlexoObject object) {

		if (object instanceof FMLFIBComponent) {
			return true;
		}
		if (object instanceof FlexoConceptInstance) {
			FlexoConceptInstance instance = (FlexoConceptInstance) object;
			return instance.getFlexoConcept() != null && instance.getFlexoConcept().getUIComponentFlexoResource() != null;
		}
		return false;
	}

	@Override
	public FlexoObject getRepresentableMasterObject(FlexoObject object) {
		return isRepresentableInModuleView(object) ? object : null;
	}

	/**
	 * Kept in step with {@link #isRepresentableInModuleView(FlexoObject)} on purpose:
	 * <code>SpecificNaturePerspective.createModuleViewForMasterObject</code> filters plugins on <code>handleObject</code> while its two
	 * sibling methods filter on <code>isRepresentableInModuleView</code>. A plugin answering only one of the two is silently never asked for
	 * a view in the nature-based perspectives.
	 */
	@Override
	public boolean handleObject(FlexoObject object) {
		return isRepresentableInModuleView(object);
	}

	@Override
	public FlexoObject getRelevantObject(FlexoObject object) {
		return object;
	}

	@Override
	public ModuleView<?> createModuleViewForMasterObject(FlexoObject object, FlexoController controller, FlexoPerspective perspective) {

		if (object instanceof FMLFIBComponent) {
			return new FIBComponentModuleView((FMLFIBComponent) object, controller, perspective);
		}

		if (object instanceof FlexoConceptInstance) {
			return FMLControlledFIBModuleView.viewFor((FlexoConceptInstance) object, controller, perspective, getLocales());
		}

		return null;
	}

	private final Map<FlexoConcept, String> selectedVariants = new HashMap<>();

	/**
	 * The user interface variant currently chosen for supplied concept, defaulting to {@link FlexoConcept#DEFAULT_VARIANT}.
	 *
	 * <p>
	 * Held by the plugin rather than by a view, so the choice survives closing and reopening the view of an instance, and is shared by every
	 * instance of the concept - which is what a reader expects from "show me these compactly".
	 */
	public String getSelectedVariant(FlexoConcept concept) {
		String selected = selectedVariants.get(concept);
		return selected != null ? selected : FlexoConcept.DEFAULT_VARIANT;
	}

	public void setSelectedVariant(FlexoConcept concept, String variant) {
		if (variant == null || FlexoConcept.DEFAULT_VARIANT.equals(variant)) {
			selectedVariants.remove(concept);
		}
		else {
			selectedVariants.put(concept, variant);
		}
	}

	private FIBEditor fibEditor;

	/**
	 * The GINA component editor of this module, built on first use.
	 *
	 * <p>
	 * Held by the plugin rather than by a view, because it is a heavy application-wide object whose palette and inspectors are shared by
	 * every component being edited.
	 *
	 * @param launchInTask
	 *            build it through the task manager, so the user sees the loading progress. <b>Only ever pass true from a context that holds
	 *            no AWT lock</b> - typically module activation. A module view is created inside
	 *            <code>synchronized (flexoFrame.getTreeLock())</code>, <code>FlexoTaskManager.waitTask</code> is a busy-wait that keeps the
	 *            caller's locks, and this builds Swing widgets needing that same lock: waiting on a task from there freezes the application.
	 */
	public FIBEditor getFIBEditor(boolean launchInTask) {

		if (fibEditor != null) {
			return fibEditor;
		}

		if (launchInTask && getServiceManager() != null && getServiceManager().getTaskManager() != null) {
			FlexoTask loadEditor = new FlexoTask("LoadFIBEditor", getLocales().localizedForKey("loading_fib_editor")) {
				@Override
				public void performTask() throws InterruptedException {
					setExpectedProgressSteps(20);
					fibEditor = makeFIBEditor();
				}
			};
			getServiceManager().getTaskManager().scheduleExecution(loadEditor);
			getServiceManager().getTaskManager().waitTask(loadEditor);
		}
		else {
			fibEditor = makeFIBEditor();
		}

		return fibEditor;
	}

	/**
	 * Weakly held, because a {@link FIBEditorController} outlives the module view: {@link FIBEditor} caches one per edited component and
	 * hands the same one back when the view is closed and reopened. Contributing to its menu twice would show the entries twice.
	 */
	private final Set<FIBEditorController> editorSessionsWithContributedActions = Collections
			.newSetFromMap(new WeakHashMap<FIBEditorController, Boolean>());

	/**
	 * Offer the actions of a container component in the contextual menu of the GINA editor's own structure browser.
	 *
	 * <p>
	 * That browser - the one the editor view puts in the perspective's bottom-left slot - is pure GINA: its nodes are
	 * {@link org.openflexo.gina.model.FIBModelObject}s, which are not {@link FlexoObject}s, and its right-click goes to
	 * <code>ComponentSwingEditorFIBController.rightClick</code>, i.e. to the editor's OWN {@link ContextualMenu} rather than to the
	 * platform's <code>ContextualMenuManager</code>. Openflexo actions can therefore never appear there on their own - GINA sits below
	 * openflexo-core and knows nothing of {@link FlexoAction}.
	 *
	 * <p>
	 * What that menu does expose is {@link ContextualMenu#addToActions(EditorAction)}, so the two actions are contributed as
	 * {@link EditorAction}s delegating to the platform. Whichever widget is right-clicked, they act on the COMPONENT: saving or localizing
	 * one widget of it means nothing.
	 */
	public void contributeActionsToEditorSession(FIBEditorController editorController, FMLFIBComponent component,
			FlexoController controller) {

		if (editorController == null || component == null || controller == null) {
			return;
		}

		if (!editorSessionsWithContributedActions.add(editorController)) {
			return;
		}

		contribute(editorController, SaveFIBComponent.actionType, FMLGINAIconLibrary.SAVE_COMPONENT_ICON, component, controller);
		contribute(editorController, LocalizeFIBComponent.actionType, FMLGINAIconLibrary.LOCALIZE_COMPONENT_ICON, component, controller);
	}

	/**
	 * One entry, delegating everything to the action factory: what it is called, whether it applies, and its execution - through
	 * <code>performActionFactory</code>, which is what runs the initializers, the finalizers and the exception handler the platform menu
	 * would have run.
	 */
	private <A extends FlexoAction<A, FlexoObject, FlexoObject>> void contribute(FIBEditorController editorController,
			FlexoActionFactory<A, FlexoObject, FlexoObject> actionFactory, Icon icon, FMLFIBComponent component,
			FlexoController controller) {

		editorController.getContextualMenu().addToActions(new EditorAction() {

			/** Used VERBATIM as the menu item text: ContextualMenu localizes nothing. */
			@Override
			public String getActionName() {
				return getLocales().localizedForKey(actionFactory.getActionName());
			}

			/** The same icon the platform's own menu shows for this action - see the initializer's getEnabledIcon(). */
			@Override
			public Icon getActionIcon() {
				return icon;
			}

			@Override
			public KeyStroke getShortcut() {
				return null;
			}

			@Override
			public FIBModelObject performAction(FIBModelObject object) {
				controller.getEditor().performActionFactory(actionFactory, component, new Vector<FlexoObject>(), null);
				// Nothing to select afterwards: the effect is on the component, not on the widget that was right-clicked
				return null;
			}

			@Override
			public boolean isVisibleFor(FIBModelObject object) {
				return actionFactory.isVisibleForSelection(component, null);
			}

			@Override
			public boolean isEnabledFor(FIBModelObject object) {
				return actionFactory.isEnabledForSelection(component, null);
			}
		});
	}

	/**
	 * The editor if one has already been built, null otherwise.
	 *
	 * <p>
	 * For callers that must NOT trigger its construction - an action deciding whether it applies, which runs every time a contextual menu is
	 * built. {@link #getFIBEditor(boolean)} would build palette and widget inspectors just to answer.
	 */
	public FIBEditor getExistingFIBEditor() {
		return fibEditor;
	}

	private FIBEditor makeFIBEditor() {

		logger.fine("Building the GINA component editor");

		FIBEditor editor = new FIBEditor(FIBLibraryImpl.createInstance(getTargetTechnologyAdapter().getTechnologyAdapterService()),
				new FIBEditorLoadingProgress() {
					@Override
					public void progress(String stepName) {
						Progress.progress(stepName);
					}
				});

		// The palette and the widget inspectors are shared by every component being edited, and are what
		// FIBComponentModuleView hands to the perspective. They MUST exist before the first openFIBComponent:
		// FIBEditorController registers the inspectors as observers only if they already do, and activate() wires the
		// palette. getPalettes()/getInspectors() answer null until made.
		editor.makePalette();
		editor.makeInspectors();

		return editor;
	}
}
