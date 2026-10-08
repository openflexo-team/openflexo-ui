/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
 * 
 * This file is part of Flexo-ui, a component of the software infrastructure 
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

package org.openflexo.inspector;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.ParseException;
import org.openflexo.connie.binding.BindingPathElement;
import org.openflexo.connie.exception.TransformException;
import org.openflexo.connie.expr.BindingPath;
import org.openflexo.connie.expr.Expression;
import org.openflexo.connie.expr.ExpressionTransformer;
import org.openflexo.connie.expr.UnresolvedBindingVariable;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.fib.binding.FMLControlledComponent;
import org.openflexo.gina.controller.CustomTypeEditorProvider;
import org.openflexo.gina.model.container.layout.ComponentConstraints;
import org.openflexo.gina.model.container.FIBTabPanel;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.inspector.FlexoConceptInspector;
import org.openflexo.foundation.fml.inspector.InspectorEntry;
import org.openflexo.foundation.fml.rm.FIBComponentResource;
import org.openflexo.foundation.fml.rm.FMLFIBComponent;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.task.Progress;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.gina.ApplicationFIBLibrary.ApplicationFIBLibraryImpl;
import org.openflexo.gina.FIBLibrary;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.model.FIBContainer;
import org.openflexo.gina.model.FIBModelFactory;
import org.openflexo.gina.model.FIBVariable;
import org.openflexo.gina.model.FIBWidget;
import org.openflexo.gina.model.container.FIBPanel;
import org.openflexo.gina.model.container.FIBPanel.Layout;
import org.openflexo.gina.model.container.FIBTab;
import org.openflexo.gina.model.container.layout.TwoColsLayoutConstraints;
import org.openflexo.gina.model.container.layout.TwoColsLayoutConstraints.TwoColsLayoutLocation;
import org.openflexo.gina.model.widget.FIBLabel;
import org.openflexo.gina.utils.FIBInspector;
import org.openflexo.gina.utils.InspectorGroup;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.FlexoFIBController;
import org.openflexo.view.controller.TechnologyAdapterController;

/**
 * Represents the controller for all inspectors managed in the context of a module<br>
 * It is connected with one or many FIBInspectorPanels sharing the same selection. In particular, manage the inspector dialog of the module.
 * 
 * @author sylvain
 * 
 */
public class ModuleInspectorController extends Observable implements Observer {

	// private static final String CONTROLLER_EDITABLE_BINDING = "controller.flexoController.isEditable(data)";

	static final Logger logger = Logger.getLogger(ModuleInspectorController.class.getPackage().getName());

	private final FIBInspectorDialog inspectorDialog;

	private final FlexoController flexoController;

	private final Map<FlexoConcept, FIBInspector> flexoConceptInspectors;

	/**
	 * For each concept whose inspector is cached, the container components it was built from - those of its whole concept hierarchy, the
	 * most general first (see {@link FlexoConcept#getInspectorComponentResources()}); empty when it was built without any. A cached
	 * inspector is stale as soon as the components its concept resolves to are no longer those ones: see
	 * {@link #isStale(FlexoConcept, Map, Function)}.
	 */
	private final Map<FlexoConcept, List<FIBComponent>> containerComponentsOfInspectors = new HashMap<>();

	/**
	 * The resource data of the container components merged so far, listened to so that the inspected object is shown again with the new
	 * component when a generator or an editor replaces it.
	 */
	private final List<FMLFIBComponent> listenedContainerComponents = new ArrayList<>();

	/**
	 * The resources of the container components merged so far, listened to for {@link FIBComponentResource#COMPONENT_SAVED_KEY} and {@link FIBComponentResource#COMPONENT_EDITED_KEY}: the GINA
	 * editor edits a component in place, which nothing else announces.
	 */
	private final List<FIBComponentResource> listenedContainerResources = new ArrayList<>();

	private final PropertyChangeListener containerComponentSavedListener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if ((FIBComponentResource.COMPONENT_SAVED_KEY.equals(evt.getPropertyName())
					|| FIBComponentResource.COMPONENT_EDITED_KEY.equals(evt.getPropertyName()))
					&& evt.getSource() instanceof FIBComponentResource
					&& dropInspectorsBuiltFrom((FIBComponentResource) evt.getSource(), flexoConceptInspectors,
							containerComponentsOfInspectors)) {
				reinspectCurrentConceptInstance();
			}
		}
	};

	private final PropertyChangeListener containerComponentListener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (FMLFIBComponent.COMPONENT_KEY.equals(evt.getPropertyName())) {
				reinspectCurrentConceptInstance();
			}
		}
	};

	private FIBInspector currentInspector = null;

	private final InspectorGroup coreInspectorGroup;
	private final List<InspectorGroup> inspectorGroups;

	private Object currentInspectedObject = null;

	public ModuleInspectorController(final FlexoController flexoController) {
		this.flexoController = flexoController;

		inspectorGroups = new ArrayList<>();
		coreInspectorGroup = new InspectorGroup(ResourceLocator.locateResource("Inspectors/COMMON"), getInspectorsFIBLibrary(),
				flexoController.getFlexoLocales());
		inspectorGroups.add(coreInspectorGroup);

		flexoConceptInspectors = new Hashtable<>();
		inspectorDialog = new FIBInspectorDialog(this);
		Boolean visible = null;
		if (flexoController.getApplicationContext().getGeneralPreferences() != null) {
			visible = flexoController.getApplicationContext().getPresentationPreferences().getInspectorVisible();
		}
		inspectorDialog.setVisible(visible == null || visible);
		inspectorDialog.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				flexoController.getApplicationContext().getPresentationPreferences().setInspectorVisible(true);
				flexoController.getApplicationContext().getPreferencesService().savePreferences();
			}

			@Override
			public void componentHidden(ComponentEvent e) {
				flexoController.getApplicationContext().getPresentationPreferences().setInspectorVisible(false);
				flexoController.getApplicationContext().getPreferencesService().savePreferences();
			};
		});

		// Resource inspectorsDir = ResourceLocator.locateResource("Inspectors/COMMON");
		// loadDirectory(inspectorsDir);
	}

	public InspectorGroup getCoreInspectorGroup() {
		return coreInspectorGroup;
	}

	/*private InspectorGroup loadInspectorGroup(Resource inspectorGroupFolder, InspectorGroup... parentInspectorGroups) {
		InspectorGroup returned = new InspectorGroup(inspectorGroupFolder) {
			@Override
			public void progress(Resource f, FIBInspector inspector) {
				super.progress(f, inspector);
				appendVisibleFor(inspector);
				appendEditableCondition(inspector);
				Progress.progress(FlexoLocalization.localizedForKey("loaded_inspector") + " " + inspector.getDataClass().getSimpleName());
			}
		};
		return returned;
	}*/

	private FIBLibrary getInspectorsFIBLibrary() {
		if (getFlexoController() != null) {
			return getFlexoController().getApplicationContext().getApplicationFIBLibraryService().getApplicationFIBLibrary();
		}
		return ApplicationFIBLibraryImpl.instance();
	}

	public InspectorGroup loadDirectory(Resource inspectorsDirectory, LocalizedDelegate locales, InspectorGroup... parentInspectorGroups) {
		InspectorGroup newInspectorGroup = new InspectorGroup(inspectorsDirectory, getInspectorsFIBLibrary(), locales,
				parentInspectorGroups) {
			@Override
			public void progress(Resource f, FIBInspector inspector) {
				super.progress(f, inspector);
				appendVisibleFor(inspector);

				// Dont do it anymore: perfs issues
				// appendEditableCondition(inspector);
				Progress.progress(FlexoLocalization.getMainLocalizer().localizedForKey("loaded_inspector") + " "
						+ inspector.getInspectedClass().getSimpleName());
			}
		};
		inspectorGroups.add(newInspectorGroup);
		return newInspectorGroup;
	}

	public FlexoController getFlexoController() {
		return flexoController;
	}

	/*public void loadDirectory(Resource dir) {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Loading directory: " + dir);
		}
		if (dir != null) {
			for (Resource f : dir.getContents(Pattern.compile(".*[.]inspector"))) {
	
				logger.fine("Loading: " + f.getURI());
				FIBInspector inspector = (FIBInspector) FIBLibrary.instance().retrieveFIBComponent(f, false, INSPECTOR_FACTORY);
				if (inspector != null) {
					appendVisibleFor(inspector);
					appendEditableCondition(inspector);
					if (inspector.getDataClass() != null) {
						// try {
						inspectors.put(inspector.getDataClass(), inspector);
						if (logger.isLoggable(Level.FINE)) {
							logger.fine("Loaded inspector: " + f.getRelativePath() + " for " + inspector.getDataClass());
						}
						Progress.progress(FlexoLocalization.localizedForKey("loaded_inspector") + " "
								+ inspector.getDataClass().getSimpleName());
					}
				} else {
					logger.warning("Not found: " + f.getURI());
				}
			}
	
			for (FIBInspector inspector : inspectors.values()) {
				// logger.info("Merging inspector: " + inspector);
				inspector.appendSuperInspectors(this);
			}
	
			for (FIBInspector inspector : inspectors.values()) {
				if (logger.isLoggable(Level.FINE)) {
					logger.fine("Initialized inspector for " + inspector.getDataClass());
				}
			}
	
			setChanged();
			notifyObservers(new NewInspectorsLoaded());
		}
	}*/

	/*private void appendEditableCondition(FIBComponent component) {
		if (component instanceof FIBWidget) {
			FIBWidget widget = (FIBWidget) component;
			DataBinding<Boolean> enable = widget.getEnable();
			if (enable != null && enable.isValid()) {
				widget.setEnable(new DataBinding<Boolean>(enable.toString() + " & " + CONTROLLER_EDITABLE_BINDING));
			}
			else {
				widget.setEnable(new DataBinding<Boolean>(CONTROLLER_EDITABLE_BINDING));
			}
		}
		else if (component instanceof FIBContainer) {
			for (FIBComponent child : ((FIBContainer) component).getSubComponents()) {
				appendEditableCondition(child);
			}
		}
	}*/

	private void appendVisibleFor(FIBComponent component) {
		/*String visibleForParam = component.getParameter("visibleFor");
		if (visibleForParam != null) {
			String[] s = visibleForParam.split("[;,\"]");
			if (s.length > 0) {
				UserType userType = UserType.getCurrentUserType();
				boolean ok = false;
				for (String string : s) {
					ok |= userType.getName().equalsIgnoreCase(string);
					ok |= userType.getIdentifier().equalsIgnoreCase(string);
					if (ok) {
						break;
					}
				}
				if (!ok) {
					component.setVisible(new DataBinding<Boolean>("false"));
				}
			}
		}*/
		if (component instanceof FIBContainer) {
			for (FIBComponent child : ((FIBContainer) component).getSubComponents()) {
				appendVisibleFor(child);
			}
		}
	}

	/**
	 * Return the inspector matching supplied objectClass<br>
	 * The research is performed on all inspector groups declared in this module<br>
	 * In case of multiple possibilities, the most specialized inspector is returned.
	 * 
	 * @param objectClass
	 * @return
	 */
	public FIBInspector inspectorForClass(Class<?> objectClass) {
		if (objectClass == null) {
			return null;
		}

		Map<Class<?>, FIBInspector> potentialInspectors = new HashMap<>();
		for (InspectorGroup inspectorGroup : new ArrayList<>(inspectorGroups)) {
			FIBInspector inspector = inspectorGroup.inspectorForClass(objectClass);
			if (inspector != null) {
				FIBInspector existingInspector = potentialInspectors.get(inspector.getInspectedClass());
				if (existingInspector != null) {
					// Already found an inspector with exactely same class, giving up (first found is the right one)
				}
				else {
					potentialInspectors.put(inspector.getInspectedClass(), inspector);
				}
			}
		}

		if (potentialInspectors.size() == 0) {
			logger.warning("Could not find inspector for " + objectClass);
			return null;
		}

		Class<?> mostSpecializedClass = TypeUtils.getMostSpecializedClass(potentialInspectors.keySet());

		FIBInspector returned = potentialInspectors.get(mostSpecializedClass);

		// System.out.println("Pour la classe " + objectClass + " je retourne:");
		// System.out.println(getFactory().stringRepresentation(returned));

		return returned;
	}

	/**
	 * Return all inspectors matching supplied objectClass<br>
	 * The research is performed on all inspector groups declared in this module<br>
	 * 
	 * @param objectClass
	 * @return
	 */
	public List<FIBInspector> inspectorsForClass(Class<?> objectClass) {
		if (objectClass == null) {
			return null;
		}

		List<FIBInspector> returned = new ArrayList<>();

		for (InspectorGroup inspectorGroup : inspectorGroups) {
			for (FIBInspector inspector : inspectorGroup.inspectorsForClass(objectClass)) {
				if (!returned.contains(inspector)) {
					returned.add(inspector);
				}
			}
		}

		return returned;

	}

	public FIBInspector inspectorForObject(Object object) {
		if (object == null) {
			return null;
		}
		if (object instanceof FlexoConceptInstance) {
			FlexoConceptInstance fci = ((FlexoConceptInstance) object);
			if (fci.getInspectedObject() != null) {
				return inspectorForFlexoConceptInstance(fci.getInspectedObject());
			}
		}

		return inspectorForClass(object.getClass());
	}

	/**
	 * Internally called to build an inspector for a given {@link FlexoConceptInstance}<br>
	 * Inspector is build from {@link FlexoConceptInstance} classical inspector augmented with tab as defined in
	 * {@link FlexoConceptInspector}
	 * 
	 * @param conceptInstance
	 * @return
	 */
	private FIBInspector inspectorForFlexoConceptInstance(FlexoConceptInstance conceptInstance) {
		if (conceptInstance == null) {
			return null;
		}
		FlexoConcept concept = conceptInstance.getFlexoConcept();
		if (concept == null) {
			return null;
		}
		FIBInspector returned = flexoConceptInspectors.get(concept);
		if (returned != null && !isStale(concept, containerComponentsOfInspectors, ModuleInspectorController::currentHierarchyComponents)) {
			return returned;
		}
		else {
			// First retrieve basic inspector (as defined in FlexoConceptInstance.inspector)
			returned = inspectorForClass(conceptInstance.getImplementedInterface());
			// Clone it
			returned = (FIBInspector) returned.cloneObject();
			returned.setLocales(getFlexoController().getModuleLocales());

			// A .inspector shipped by the container of the concept wins over the tab generated from the deprecated
			// FlexoConceptInspector entries. append() merges by component name, so its <TabPanel name="Tab"> lands in
			// the TabPanel this inspector already has - the same way a super inspector is merged in.
			// The inspector is ADDITIVE: the components of the whole hierarchy, ancestors first, then the concept's own
			List<FIBComponent> loaded = new ArrayList<>();
			List<FIBContainer> containers = new ArrayList<>();
			for (FlexoConcept contributor : concept.getInspectorContributingConcepts()) {
				FIBComponent component = loadContainerInspector(contributor);
				if (component != null) {
					loaded.add(component);
					if (component instanceof FIBContainer) {
						containers.add((FIBContainer) component);
					}
				}
			}

			containerComponentsOfInspectors.put(concept, loaded);

			if (!containers.isEmpty()) {
				mergeContainerInspectors(returned, containers, concept, customTypeEditorProvider());
				listenToContainerComponent(concept);
				flexoConceptInspectors.put(concept, returned);
				// No FlexoConceptInstanceInspectorUpdater here: there are no InspectorEntry to listen to, and asking
				// for concept.getInspector() would lazily create an empty one.
				return returned;
			}

			if (concept.getInspector() == null) {
				// The concept ships no .inspector and declares no deprecated inspector either: the basic inspector is all there is
				flexoConceptInspectors.put(concept, returned);
				return returned;
			}

			// And append tab matching FlexoConceptInspector
			appendFlexoConceptInspector(concept, returned);
			flexoConceptInspectors.put(concept, returned);

			FlexoConceptInstanceInspectorUpdater updater = new FlexoConceptInstanceInspectorUpdater(returned, concept);
			concept.getInspector().getPropertyChangeSupport().addPropertyChangeListener(updater);
			for (InspectorEntry entry : concept.getInspector().getEntries()) {
				entry.getPropertyChangeSupport().addPropertyChangeListener(updater);
			}

			return returned;
		}
	}

	/**
	 * Internal listener of an inspector tab of a FlexoConcept
	 * 
	 * @author sylvain
	 *
	 */
	class FlexoConceptInstanceInspectorUpdater implements PropertyChangeListener {

		private FIBInspector inspector;
		private FlexoConcept concept;

		public FlexoConceptInstanceInspectorUpdater(FIBInspector inspector, FlexoConcept concept) {
			this.inspector = inspector;
			this.concept = concept;
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getSource() == concept.getInspector() && evt.getPropertyName().equals(FlexoConceptInspector.ENTRIES_KEY)) {
				updateFlexoConceptInspector(concept, inspector);
				if (evt.getNewValue() instanceof InspectorEntry) {
					((InspectorEntry) evt.getNewValue()).getPropertyChangeSupport().addPropertyChangeListener(this);
				}
				else if (evt.getOldValue() instanceof InspectorEntry) {
					((InspectorEntry) evt.getOldValue()).getPropertyChangeSupport().removePropertyChangeListener(this);
				}
			}
			if (evt.getSource() instanceof InspectorEntry) {
				updateFlexoConceptInspector(concept, inspector);
			}
		}

	}

	public FIBInspectorDialog getInspectorDialog() {
		return inspectorDialog;
	}

	public void refreshComponentVisibility() {
		inspectorDialog.getInspectorPanel().refreshComponentVisibility();
	}

	protected void switchToEmptyContent() {
		// logger.info("switchToEmptyContent()");
		currentInspectedObject = null;
		currentInspector = null;
		setChanged();
		notifyObservers(new EmptySelectionActivated());
	}

	private void switchToMultipleSelection() {
		// logger.info("switchToMultipleSelection()");
		currentInspectedObject = null;
		currentInspector = null;
		setChanged();
		notifyObservers(new MultipleSelectionActivated());
	}

	private void switchToInspector(FIBInspector newInspector/*, boolean updateEPTabs*/) {
		// System.out.println("switchToInspector " + newInspector);
		currentInspector = newInspector;
		setChanged();
		notifyObservers(new InspectorSwitching(newInspector/*, updateEPTabs*/));
	}

	private void displayObject(Object object) {

		if (object instanceof FlexoConceptInstance) {
			setChanged();
			notifyObservers(new InspectedObjectChanged(((FlexoConceptInstance) object).getInspectedObject()));
		}
		else {
			setChanged();
			notifyObservers(new InspectedObjectChanged(object));
		}
	}

	/**
	 * Returns boolean indicating if inspection change
	 * 
	 * @param object
	 * @return
	 */
	public boolean inspectObject(Object object) {
		if (object == currentInspectedObject) {
			return false;
		}

		// logger.info("ModuleInspectorController: inspectObject with " + object);
		// logger.info("currentInspectedObject=" + currentInspectedObject);

		currentInspectedObject = object;

		FIBInspector newInspector = inspectorForObject(object);

		if (newInspector == null) {
			logger.warning("No inspector for " + object);
			switchToEmptyContent();
		}
		else {
			if (newInspector != currentInspector) {
				switchToInspector(newInspector);
			}
			displayObject(object);
		}

		return true;
	}

	public void resetInspector() {
		switchToEmptyContent();
	}

	@Override
	public void update(Observable o, Object notification) {
		if (notification instanceof InspectorSelection) {
			InspectorSelection inspectorSelection = (InspectorSelection) notification;
			if (inspectorSelection instanceof EmptySelection) {
				switchToEmptyContent();
			}
			else if (inspectorSelection instanceof MultipleSelection) {
				switchToMultipleSelection();
			}
			else if (inspectorSelection instanceof UniqueSelection) {
				inspectObject(((UniqueSelection) inspectorSelection).getInspectedObject());
			}
		}

		// Reforward notification to all in inspector panels
		setChanged();
		notifyObservers(notification);
	}

	public static class NewInspectorsLoaded {

	}

	public static class EmptySelectionActivated {

	}

	public static class MultipleSelectionActivated {

	}

	public static class InspectorSwitching {
		private final FIBInspector newInspector;

		public InspectorSwitching(FIBInspector newInspector/*, boolean updateEPTabs*/) {
			this.newInspector = newInspector;
		}

		public FIBInspector getNewInspector() {
			return newInspector;
		}
	}

	public static class InspectedObjectChanged {
		private final Object inspectedObject;

		public InspectedObjectChanged(Object inspectedObject) {
			this.inspectedObject = inspectedObject;
		}

		public Object getInspectedObject() {
			return inspectedObject;
		}
	}

	public void delete() {
		for (FMLFIBComponent resourceData : listenedContainerComponents) {
			resourceData.getPropertyChangeSupport().removePropertyChangeListener(containerComponentListener);
		}
		listenedContainerComponents.clear();
		for (FIBComponentResource resource : listenedContainerResources) {
			resource.getPropertyChangeSupport().removePropertyChangeListener(containerComponentSavedListener);
		}
		listenedContainerResources.clear();
		inspectorDialog.delete();
		currentInspectedObject = null;
		currentInspector = null;
	}

	/**
	 * Whether the inspector cached for supplied concept was built from another container component than the one the concept resolves to now:
	 * a component was created, removed or replaced since - typically by a generator such as the free modelling editor's, which (re)writes
	 * the inspector of a concept as the user adds properties to it.
	 *
	 * <p>
	 * Checked on every lookup rather than only notified, because a component that APPEARS has no resource data to listen to beforehand.
	 */
	private static boolean isStale(FlexoConcept concept, Map<FlexoConcept, List<FIBComponent>> containerComponentsInUse,
			Function<FlexoConcept, List<FIBComponent>> currentComponents) {
		if (!containerComponentsInUse.containsKey(concept)) {
			// Never recorded: built before this check existed for it, nothing to compare with
			return false;
		}
		List<FIBComponent> inUse = containerComponentsInUse.get(concept);
		List<FIBComponent> current = currentComponents.apply(concept);
		if (inUse.size() != current.size()) {
			return true;
		}
		for (int i = 0; i < inUse.size(); i++) {
			if (inUse.get(i) != current.get(i)) {
				return true;
			}
		}
		return false;
	}

	/** The components the inspector of supplied concept is made of now: those of its whole hierarchy, the most general first. */
	private static List<FIBComponent> currentHierarchyComponents(FlexoConcept concept) {
		List<FIBComponent> returned = new ArrayList<>();
		for (FIBComponentResource resource : concept.getInspectorComponentFlexoResources()) {
			if (resource.getComponent() != null) {
				returned.add(resource.getComponent());
			}
		}
		return returned;
	}

	/** The single component {@link FlexoConcept#getInspectorComponentFlexoResource()} gives, for what is not composed. */
	private static List<FIBComponent> currentSingleComponent(FlexoConcept concept) {
		FIBComponentResource resource = concept.getInspectorComponentFlexoResource();
		FIBComponent component = resource != null ? resource.getComponent() : null;
		return component != null ? Collections.singletonList(component) : Collections.<FIBComponent> emptyList();
	}

	/**
	 * Listen to the resource data of the container component of supplied concept, so that replacing the component shows the inspected object
	 * again - with an inspector rebuilt from the new component, since the cached one is then stale.
	 */
	private void listenToContainerComponent(FlexoConcept concept) {
		// Every component of the hierarchy: saving an ancestor's inspector makes the cached inspector of its descendants stale
		for (FIBComponentResource componentResource : concept.getInspectorComponentFlexoResources()) {
			if (!listenedContainerResources.contains(componentResource)) {
				componentResource.getPropertyChangeSupport().addPropertyChangeListener(containerComponentSavedListener);
				listenedContainerResources.add(componentResource);
			}
			FMLFIBComponent resourceData = componentResource.getLoadedResourceData();
			if (resourceData != null && !listenedContainerComponents.contains(resourceData)) {
				resourceData.getPropertyChangeSupport().addPropertyChangeListener(containerComponentListener);
				listenedContainerComponents.add(resourceData);
			}
		}
	}

	/**
	 * Forget the inspectors that were built from the component of supplied resource, which was edited in place and saved: they are clones
	 * of what it held before, and {@link #isStale} cannot tell, the component being the same object. The next lookup rebuilds them.
	 *
	 * @return whether anything was dropped
	 */
	static boolean dropInspectorsBuiltFrom(FIBComponentResource resource, Map<FlexoConcept, FIBInspector> inspectors,
			Map<FlexoConcept, List<FIBComponent>> builtFrom) {
		FIBComponent saved = resource.getComponent();
		boolean dropped = false;
		for (Iterator<Map.Entry<FlexoConcept, List<FIBComponent>>> i = builtFrom.entrySet().iterator(); i.hasNext();) {
			Map.Entry<FlexoConcept, List<FIBComponent>> entry = i.next();
			if (saved != null && containsSame(entry.getValue(), saved)) {
				inspectors.remove(entry.getKey());
				i.remove();
				dropped = true;
			}
		}
		return dropped;
	}

	private static boolean containsSame(List<FIBComponent> components, FIBComponent searched) {
		for (FIBComponent component : components) {
			if (component == searched) {
				return true;
			}
		}
		return false;
	}

	private void reinspectCurrentConceptInstance() {
		Object wasInspected = currentInspectedObject;
		if (wasInspected instanceof FlexoConceptInstance) {
			switchToEmptyContent();
			inspectObject(wasInspected);
		}
	}

	/**
	 * Load the <code>.inspector</code> component that the container of supplied {@link FlexoConcept} ships, bound to the typing space of the
	 * VirtualModel declaring that concept, or null when the container ships none.
	 *
	 * <p>
	 * This is the replacement for the tab generated from the deprecated {@link FlexoConceptInspector} entries: rather than being built
	 * widget by widget from the FML model, the inspector of a concept is an ordinary GINA component stored beside the FML source, in the
	 * <code>Xxx.fml/</code> container. See {@link FlexoConcept#getInspectorComponentResource()} for how it is named and found.
	 *
	 * @return the component its resource holds, which is SHARED - with the GINA editor and with every sub-concept inheriting it. Never merge
	 *         it as it is: see {@link #mergeContainerInspector}, which merges a clone
	 */
	private FIBComponent loadContainerInspector(FlexoConcept concept) {
		return FMLControlledComponent.loadInspectorComponent(concept, customTypeEditorProvider());
	}

	/**
	 * The technology adapter controllers, which provide the editors of FML custom types, or null outside an application.
	 */
	private CustomTypeEditorProvider customTypeEditorProvider() {
		return getFlexoController() != null && getFlexoController().getApplicationContext() != null
				? getFlexoController().getApplicationContext().getTechnologyAdapterControllerService()
				: null;
	}

	/**
	 * Complete supplied class inspector - a CLONE of the inspector of the FlexoConceptInstance class - with the inspector the container of
	 * supplied concept ships.
	 *
	 * <p>
	 * Three things make this more than an <code>append()</code>:
	 * <ul>
	 * <li>the container component is SHARED: it is the data of its resource, shown by the GINA editor and inherited by every sub-concept.
	 * And {@link FIBContainer#append(FIBContainer)} does not take what it merges away from where it came, it re-parents it - leaving the
	 * resource's component listing children that now belong to another. So a clone is merged, never the original;</li>
	 * <li>the component comes in either shape. A <code>&lt;TabPanel name="Tab"&gt;</code> is merged by that name into the tabs the
	 * platform shows. A PLAIN panel - what the CreateInspector action generates by default - is wrapped into one tab of its own, titled
	 * after the concept and put first, where the tab generated from the deprecated FlexoConceptInspector goes. Appended as it is, its first
	 * widget lands in front of the TabPanel, which is where {@link FIBInspector#getTabPanel()} used to expect it;</li>
	 * <li><code>append()</code> carries sub-components only. The <code>data</code> variable typed by the concept and the FML binding factory,
	 * which {@link FMLControlledComponent#bindToConcept} set on the root of the container, stay behind on it - and every
	 * <code>data.someProperty</code> of the merged tabs is then read against the bare FlexoConceptInstance of the class inspector, i.e.
	 * resolves to nothing. Each contributed tab is therefore bound to the concept exactly as that root was. The <code>data</code> variable
	 * this declares on the tab has no value of its own, and does not hide the inspected object: a view looks a variable up by NAME and,
	 * finding no value, asks its parent view.</li>
	 * </ul>
	 *
	 * @param customTypeEditorProvider
	 *            may be null in a headless context
	 */
	static void mergeContainerInspector(FIBInspector classInspector, FIBContainer containerInspector, FlexoConcept concept,
			CustomTypeEditorProvider customTypeEditorProvider) {

		FIBContainer contributed = (FIBContainer) containerInspector.cloneObject();
		FIBTabPanel classTabPanel = classInspector.getTabPanel();

		FIBComponent containerTabPanel = contributed.getSubComponentNamed(CONTAINER_TAB_PANEL_NAME);

		if (containerTabPanel instanceof FIBTabPanel || classTabPanel == null) {
			if (classTabPanel == null) {
				Logger.getLogger(ModuleInspectorController.class.getPackage().getName()).warning("The class inspector of " + concept + " has no TabPanel: its container inspector is appended as it is");
			}
			List<FIBComponent> contributedTabs = containerTabPanel instanceof FIBTabPanel
					? new ArrayList<>(((FIBTabPanel) containerTabPanel).getSubComponents())
					: new ArrayList<>();
			classInspector.append(contributed);
			for (FIBComponent tab : contributedTabs) {
				FMLControlledComponent.bindToConcept(tab, concept, customTypeEditorProvider);
			}
			return;
		}

		FIBTab tab = classInspector.getModelFactory().newFIBTab();
		tab.setName(concept.getName() + "Panel");
		tab.setTitle(concept.getName());
		tab.setLayout(contributed instanceof FIBPanel && ((FIBPanel) contributed).getLayout() != null ? ((FIBPanel) contributed).getLayout()
				: Layout.twocols);
		// A tab may scroll: it is not the root of what the module view shows
		tab.setUseScrollBar(true);

		// The clone is ours: emptying it is how its children move into the tab, keeping the constraints they were laid out with
		for (FIBComponent child : new ArrayList<>(contributed.getSubComponents())) {
			ComponentConstraints constraints = child.getConstraints();
			contributed.removeFromSubComponents(child);
			tab.addToSubComponents(child, constraints);
		}

		// Parent first, as appendFlexoConceptInspector() does: bindings of the tab resolve through it while it is being added
		tab.setParent(classTabPanel);
		classTabPanel.addToSubComponents(tab, null, 0);

		if (contributed.getLocalizedDictionary() != null) {
			classInspector.retrieveFIBLocalizedDictionary().append(contributed.getLocalizedDictionary());
		}

		FMLControlledComponent.bindToConcept(tab, concept, customTypeEditorProvider);
	}

	/**
	 * Complete supplied class inspector with the inspector of an instance of supplied concept: the container components of its whole
	 * hierarchy, given the most general first and the concept's own last (see {@link FlexoConcept#getInspectorComponentResources()}).
	 *
	 * <p>
	 * A single component is merged exactly as {@link #mergeContainerInspector} does. Several are composed, the widgets being merged by
	 * {@link FIBContainer#append(FIBContainer)} so that the <code>index</code> of the widgets decides the order, with an ancestor's
	 * widget before a descendant's on equal indexes. Tabs are composed like this:
	 * <ul>
	 * <li>a component declaring SEVERAL tabs in its TabPanel contributes each of them to the tab of the same <b>name</b>: the tabs
	 * <code>Single</code> and <code>Advanced</code> of a parent and of a child give a <code>Single</code> and an <code>Advanced</code>
	 * tab, each made of the widgets of both. A tab the other components do not have is kept as a tab of its own, after the tabs of the
	 * ancestors. The title of a merged tab is the most specialized one;</li>
	 * <li>a component declaring a SINGLE tab - or a plain panel, which is what the generated inspectors are - contributes its widgets to
	 * the FIRST tab composed so far, whatever its name; if there is none yet, the widgets make a tab of their own, named
	 * <code>&lt;Concept&gt;Panel</code> and titled after the concept.</li>
	 * </ul>
	 * A widget NAMED like one an ancestor contributed to the same tab replaces it, the descendant redefining what the ancestor says.
	 *
	 * <p>
	 * Every contributed widget is bound to the concept of the INSTANCE, a subtype of every ancestor: <code>data.x</code> of an ancestor
	 * still resolves. As in {@link #mergeContainerInspector}, clones are composed and the components of the resources are left untouched.
	 *
	 * <p>
	 * Public so that a test can compose the inspector of a concept without a {@link FlexoController}.
	 *
	 * @param containerInspectors
	 *            the components, ancestors first; not empty
	 */
	public static void mergeContainerInspectors(FIBInspector classInspector, List<FIBContainer> containerInspectors, FlexoConcept concept,
			CustomTypeEditorProvider customTypeEditorProvider) {

		if (containerInspectors.size() == 1) {
			mergeContainerInspector(classInspector, containerInspectors.get(0), concept, customTypeEditorProvider);
			return;
		}

		FIBTabPanel classTabPanel = classInspector.getTabPanel();
		List<FIBTab> composedTabs = new ArrayList<>();

		for (FIBContainer containerInspector : containerInspectors) {

			// The clone is ours: append() re-parents its widgets
			FIBContainer contributed = (FIBContainer) containerInspector.cloneObject();

			List<FIBContainer> tabs = new ArrayList<>();
			FIBComponent containerTabPanel = contributed.getSubComponentNamed(CONTAINER_TAB_PANEL_NAME);
			if (containerTabPanel instanceof FIBTabPanel) {
				for (FIBComponent contributedTab : ((FIBTabPanel) containerTabPanel).getSubComponents()) {
					if (contributedTab instanceof FIBContainer) {
						tabs.add((FIBContainer) contributedTab);
					}
				}
			}

			if (tabs.size() > 1) {
				// Several tabs: each one is merged by name
				for (FIBContainer source : tabs) {
					FIBTab target = null;
					for (FIBTab composed : composedTabs) {
						if (source.getName() != null && source.getName().equals(composed.getName())) {
							target = composed;
							break;
						}
					}
					if (target == null) {
						target = newComposedTab(classInspector, source.getName(), null);
						composedTabs.add(target);
					}
					if (source instanceof FIBTab && ((FIBTab) source).getTitle() != null) {
						target.setTitle(((FIBTab) source).getTitle());
					}
					composeInto(target, source);
				}
			}
			else {
				// A single tab, or a plain panel: its widgets join the first tab
				if (composedTabs.isEmpty()) {
					composedTabs.add(newComposedTab(classInspector, concept.getName() + "Panel", concept.getName()));
				}
				composeInto(composedTabs.get(0), tabs.size() == 1 ? tabs.get(0) : contributed);
			}

			if (contributed.getLocalizedDictionary() != null) {
				classInspector.retrieveFIBLocalizedDictionary().append(contributed.getLocalizedDictionary());
			}
		}

		// Parent first, as mergeContainerInspector() does: bindings of the tab resolve through it while it is being added
		for (int i = 0; i < composedTabs.size(); i++) {
			FIBTab tab = composedTabs.get(i);
			tab.setParent(classTabPanel);
			classTabPanel.addToSubComponents(tab, null, i);
			FMLControlledComponent.bindToConcept(tab, concept, customTypeEditorProvider);
		}
	}

	private static FIBTab newComposedTab(FIBInspector classInspector, String name, String title) {
		FIBTab tab = classInspector.getModelFactory().newFIBTab();
		tab.setName(name);
		tab.setTitle(title != null ? title : name);
		tab.setUseScrollBar(true);
		tab.setLayout(Layout.twocols);
		return tab;
	}

	/** Merge the widgets of supplied source - a clone, so it is consumed - into supplied composed tab. */
	private static void composeInto(FIBTab tab, FIBContainer source) {
		if (source instanceof FIBPanel && ((FIBPanel) source).getLayout() != null) {
			tab.setLayout(((FIBPanel) source).getLayout());
		}

		// append() keeps what is already there when a widget has the same name: the descendant must redefine instead
		for (FIBComponent child : source.getSubComponents()) {
			FIBComponent redefined = child.getName() != null ? tab.getSubComponentNamed(child.getName()) : null;
			if (redefined != null && !(redefined instanceof FIBContainer && child instanceof FIBContainer)) {
				tab.removeFromSubComponents(redefined);
			}
		}

		// The widgets stay where they are, with the constraints (the index!) they were laid out with: moving them out of the
		// clone one by one rewrites those constraints
		tab.append(source);
	}

	/**
	 * The name of the TabPanel every platform inspector declares, and the key a container inspector's own TabPanel is merged by.
	 */
	private static final String CONTAINER_TAB_PANEL_NAME = "Tab";

	private FIBTab appendFlexoConceptInspector(FlexoConcept concept, FIBInspector inspector) {
		FIBTab newTab = makeFIBTab(concept);
		// TODO: we have to set the parent first, otherwise in FIBViewImpl.java
		// The value of FIBVariable fci is still invalid when component beeing added
		// Thus, this is not listened
		newTab.setParent(inspector.getTabPanel());
		inspector.getTabPanel().addToSubComponents(newTab, null, 0);
		return newTab;
	}

	private FIBTab updateFlexoConceptInspector(FlexoConcept concept, FIBInspector inspector) {
		Object wasInspected = currentInspectedObject;
		if (concept != null && inspector != null && inspector.getTabPanel() != null
				&& inspector.getTabPanel().getSubComponents().size() > 0) {
			FIBTab existingTab = (FIBTab) inspector.getTabPanel().getSubComponents().get(0);
			if (concept.getInspector() != null && existingTab.getTitle().equals(concept.getInspector().getInspectorTitle())) {
				inspector.getTabPanel().removeFromSubComponents(existingTab);
			}
		}

		FIBTab returned = appendFlexoConceptInspector(concept, inspector);
		if (wasInspected != null) {
			switchToEmptyContent();
			inspectObject(wasInspected);
		}
		return returned;
	}

	private Map<FlexoConcept, FIBPanel> flexoConceptInspectorPanels = new HashMap<>();

	/** Same as {@link #containerComponentsOfInspectors}, for {@link #flexoConceptInspectorPanels} */
	private final Map<FlexoConcept, List<FIBComponent>> containerComponentsOfPanels = new HashMap<>();

	public FIBPanel getFIBInspectorPanel(FlexoConcept flexoConcept) {
		return getFIBInspectorPanel(flexoConcept, FlexoFIBController.class);
	}

	public FIBPanel getFIBInspectorPanel(FlexoConcept flexoConcept, Class<? extends FlexoFIBController> controllerClass) {
		FIBPanel returned = flexoConceptInspectorPanels.get(flexoConcept);
		if (returned == null || isStale(flexoConcept, containerComponentsOfPanels, ModuleInspectorController::currentSingleComponent)) {
			returned = makeFIBInspectorPanel(flexoConcept, controllerClass);
			flexoConceptInspectorPanels.put(flexoConcept, returned);
		}
		return returned;

	}

	private FIBPanel makeFIBInspectorPanel(FlexoConcept flexoConcept, Class<? extends FlexoFIBController> controllerClass) {

		// A .inspector shipped by the container of the concept is used as-is: it IS the panel, and needs no generation.
		// This is what makes a container inspector show up in the standard FML-RT VirtualModelInstanceView, whose
		// FIBReferencedComponent renders controller.inspectorForFlexoConceptInstance(browser.selected).
		FIBComponent containerInspector = loadContainerInspector(flexoConcept);
		containerComponentsOfPanels.put(flexoConcept,
				containerInspector != null ? Collections.singletonList(containerInspector) : Collections.<FIBComponent> emptyList());
		if (containerInspector instanceof FIBPanel) {
			FIBPanel returned = (FIBPanel) containerInspector;
			if (returned.getControllerClass() == null) {
				returned.setControllerClass(controllerClass);
			}
			returned.finalizeDeserialization();
			return returned;
		}

		FIBPanel inspector = getFactory().newFIBPanel();
		inspector.setLayout(Layout.twocols);
		inspector.setUseScrollBar(true);

		inspector.setControllerClass(controllerClass);

		// We create a variable for inspector data
		// This variable is called fci, with type FlexoConceptInstanceType<FlexoConcept>, and value 'data' (which is the
		// FlexoConceptInstance)
		// The goal of that variable definition is to provide type for inspected FlexoConceptInstance
		FIBVariable<?> dataVariable = getFactory().newFIBVariable(inspector, "fci", flexoConcept.getInstanceType());
		dataVariable.setValue(new DataBinding<>("data"));
		inspector.addToVariables(dataVariable);
		inspector.setName(flexoConcept.getName() + "Panel");

		logger.info("Building inspector for " + flexoConcept);
		appendInspectorEntries(flexoConcept, inspector);
		logger.info("Built inspector for " + flexoConcept);
		inspector.finalizeDeserialization();

		return inspector;
	}

	/**
	 * Internally called to create {@link FIBTab} matching inspector of supplied {@link FlexoConcept}
	 * 
	 * @param flexoConcept
	 * @param inspector
	 * @return
	 */
	private FIBTab makeFIBTab(FlexoConcept flexoConcept) {
		FIBTab newTab = getFactory().newFIBTab();
		newTab.setTitle(flexoConcept.getInspector() != null ? flexoConcept.getInspector().getInspectorTitle() : flexoConcept.getName());
		newTab.setLayout(Layout.twocols);
		newTab.setUseScrollBar(true);

		// We create a variable for inspector data
		// This variable is called fci, with type FlexoConceptInstanceType<FlexoConcept>, and value 'data' (which is the
		// FlexoConceptInstance)
		// The goal of that variable definition is to provide type for inspected FlexoConceptInstance
		FIBVariable<?> dataVariable = getFactory().newFIBVariable(newTab, "fci", flexoConcept.getInstanceType());
		dataVariable.setValue(new DataBinding<>("data"));
		newTab.addToVariables(dataVariable);
		newTab.setName(flexoConcept.getName() + "Panel");

		appendInspectorEntries(flexoConcept, newTab);
		newTab.finalizeDeserialization();

		/*for (FIBComponent c : newTab.getSubComponents()) {
			System.out.println("> component " + c);
			if (c instanceof FIBWidget) {
				System.out.println("value=" + ((FIBWidget) c).getData());
				System.out.println(
						"valid=" + ((FIBWidget) c).getData().isValid() + " reason=" + ((FIBWidget) c).getData().invalidBindingReason());
			}
		}*/

		return newTab;
	}

	/**
	 * Internally called to append all entries of supplied flexo concept
	 * 
	 * @param flexoConcept
	 * @param newTab
	 */
	private void appendInspectorEntries(FlexoConcept flexoConcept, FIBPanel newTab) {
		if (flexoConcept == null) {
			logger.warning("Unexpected null concept ");
			return;
		}
		for (FlexoConcept parentEP : flexoConcept.getParentFlexoConcepts()) {
			appendInspectorEntries(parentEP, newTab);
		}
		if (flexoConcept.getDeclaringCompilationUnit() == null) {
			logger.warning("Unexpected null virtual model for concept " + flexoConcept);
			return;
		}
		LocalizedDelegate localizedDictionary = flexoConcept.getDeclaringCompilationUnit().getLocalizedDictionary();
		if (flexoConcept.getInspector() == null) {
			return;
		}
		for (final InspectorEntry entry : flexoConcept.getInspector().getEntries()) {
			FIBLabel label = getFactory().newFIBLabel();
			String entryLabel = localizedDictionary.localizedForKeyAndLanguage(entry.getLabel(), FlexoLocalization.getCurrentLanguage());
			if (entryLabel == null) {
				entryLabel = entry.getLabel();
			}
			label.setLabel(entryLabel);
			newTab.addToSubComponentsNoNotification(label, new TwoColsLayoutConstraints(TwoColsLayoutLocation.left, false, false));
			FIBWidget widget = makeWidget(entry, newTab);
			if (widget != null) {
				widget.setBindingFactory(entry.getBindingFactory());
				String bindingPath = entry.getData().toString();
				String normalizedBindingPath = normalizeBindingPath(bindingPath, widget);
				widget.setData(new DataBinding<>(normalizedBindingPath));
				widget.setReadOnly(entry.getIsReadOnly());
			}
			/*System.out.println("Widget " + widget + " data=" + entry.getData());
			System.out.println("valid:" + entry.getData().isValid());
			System.out.println("reason=" + entry.getData().invalidBindingReason());
			System.out.println("Widget data " + widget.getData());
			System.out.println("valid:" + widget.getData().isValid());
			System.out.println("reason=" + widget.getData().invalidBindingReason());*/
		}

		newTab.fireSubComponentsChanged();

		// System.out.println("Je retourne " + getFactory().stringRepresentation(newTab));
	}

	/**
	 * Normalized BindingPath so that all {@link BindingPath} starts with 'fci.' (name of FCI beeing represented)
	 * 
	 * @param bindingPath
	 * @return
	 */
	private static String normalizeBindingPath(String bindingPath, Bindable bindable) {
		Expression expression = null;
		try {

			expression = bindable.getBindingFactory().parseExpression(bindingPath, bindable);

			expression = expression.transform(new ExpressionTransformer() {
				@Override
				public Expression performTransformation(Expression e) throws TransformException {
					if (e instanceof BindingPath) {
						BindingPath bindingPath = (BindingPath) e;
						if (bindingPath.getBindingVariable() == null) {
							UnresolvedBindingVariable objectBV = new UnresolvedBindingVariable("fci");
							bindingPath.setBindingVariable(objectBV);
							return bindingPath;
						}
						else if (!bindingPath.getBindingVariable().getVariableName().equals("fci")) {
							UnresolvedBindingVariable objectBV = new UnresolvedBindingVariable("fci");
							List<BindingPathElement> bp2 = new ArrayList<>(bindingPath.getBindingPath());
							bp2.add(0, bindable.getBindingFactory().makeSimplePathElement(objectBV,
									bindingPath.getBindingVariable().getVariableName(), bindable));
							bindingPath.setBindingVariable(objectBV);
							bindingPath.setBindingPath(bp2);
						}
						return bindingPath;
					}
					return e;
				}
			});

			return expression.toString();
		} catch (ParseException e) {
			System.out.println("Could not parse: " + bindingPath);
			e.printStackTrace();
			return bindingPath;
		} catch (TransformException e) {
			System.out.println("TransformException while parsing: " + bindingPath);
			e.printStackTrace();
			return bindingPath;
		}
	}

	/**
	 * Factory method used to instanciate a technology-specific FIBWidget for a given {@link InspectorEntry}<br>
	 * We iterate on all known technologies to use the delegated {@link TechnologyAdapterController}
	 * 
	 * @param entry
	 * @param newTab
	 * @param factory
	 * @return
	 */
	private FIBWidget makeWidget(final InspectorEntry entry, FIBPanel newTab) {
		for (TechnologyAdapter ta : flexoController.getApplicationContext().getTechnologyAdapterService().getTechnologyAdapters()) {
			TechnologyAdapterController<?> tac = FlexoController.getTechnologyAdapterController(ta);
			boolean[] expand = { true, false };
			FIBWidget returned = tac.makeWidget(entry, null, getFactory(), "fci", expand);
			if (returned != null) {
				newTab.addToSubComponentsNoNotification(returned,
						new TwoColsLayoutConstraints(TwoColsLayoutLocation.right, expand[0], expand[1]));
				return returned;
			}
			else {
				logger.warning("Cannot make widget for inspector entry " + entry);
			}
		}

		return null;
	}

	public FIBModelFactory getFactory() {
		return coreInspectorGroup.getFIBModelFactory();
	}
}
