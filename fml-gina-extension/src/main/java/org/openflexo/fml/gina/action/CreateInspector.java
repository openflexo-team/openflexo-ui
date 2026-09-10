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
package org.openflexo.fml.gina.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.fml.gina.FMLGINAPlugin;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.InvalidNameException;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.inspector.InspectorEntry;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rm.FIBComponentResource;
import org.openflexo.foundation.fml.rm.FIBComponentResourceFactory;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.model.FIBModelFactory;
import org.openflexo.gina.model.FIBVariable;
import org.openflexo.gina.model.FIBWidget;
import org.openflexo.gina.model.widget.FIBMultipleValues;
import org.openflexo.gina.model.container.FIBPanel;
import org.openflexo.gina.model.container.FIBPanel.Layout;
import org.openflexo.gina.model.container.FIBTab;
import org.openflexo.gina.model.container.FIBTabPanel;
import org.openflexo.gina.model.container.layout.TwoColsLayoutConstraints;
import org.openflexo.gina.model.container.layout.TwoColsLayoutConstraints.TwoColsLayoutLocation;
import org.openflexo.gina.model.widget.FIBLabel;
import org.openflexo.gina.utils.FIBInspector;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.TechnologyAdapterController;
import org.openflexo.view.controller.TechnologyAdapterControllerService;

/**
 * Creates the inspector of a {@link FlexoConcept}, as a <code>.inspector</code> component of the <code>Xxx.fml/</code> container of the
 * VirtualModel declaring it.
 *
 * <p>
 * The component is proposed pre-filled: a two-column panel with, for each accessible property of the concept, a label and the widget that
 * represents its type. Both are what the wizard lets the user select, deselect and edit. The result is an ordinary component, edited
 * afterwards in the GINA editor like any other.
 *
 * <p>
 * The widget of a property is not chosen here: each {@link TechnologyAdapterController} is asked in turn, and the first to answer wins -
 * which is how a technology adapter contributes its own selectors (a VirtualModelInstance, a FlexoConcept, an ontology individual…) rather
 * than a bare text field. That call takes a <code>WidgetContext</code>, and the only implementation of it is the deprecated
 * {@link InspectorEntry}: this action therefore builds throwaway entries, uses them as scaffolding, and discards them. They are never
 * attached to the concept and never serialized.
 *
 * @author sylvain
 */
public class CreateInspector extends FlexoAction<CreateInspector, FMLObject, FMLObject> {

	private static final Logger logger = Logger.getLogger(CreateInspector.class.getPackage().getName());

	public static FlexoActionFactory<CreateInspector, FMLObject, FMLObject> actionType = new FlexoActionFactory<CreateInspector, FMLObject, FMLObject>(
			"inspector", FlexoActionFactory.generateMenu, FlexoActionFactory.defaultGroup, FlexoActionFactory.NORMAL_ACTION_TYPE) {

		@Override
		public CreateInspector makeNewAction(FMLObject focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
			return new CreateInspector(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FMLObject object, Vector<FMLObject> globalSelection) {
			return conceptOf(object) != null;
		}

		@Override
		public boolean isEnabledForSelection(FMLObject object, Vector<FMLObject> globalSelection) {
			// Only when the concept has no inspector of its OWN: editing the one it has is the editor's job
			FlexoConcept concept = conceptOf(object);
			return concept != null && !declaresItsOwnInspector(concept);
		}

		@Override
		public LocalizedDelegate getLocales(FlexoServiceManager serviceManager) {
			return serviceManager.getService(TechnologyAdapterControllerService.class).getPlugin(FMLGINAPlugin.class).getLocales();
		}
	};

	static {
		// A VirtualModel IS a FlexoConcept, so one registration covers both; a compilation unit is offered the action of
		// the VirtualModel it declares, which is what a reader selecting the .fml file expects.
		FlexoObjectImpl.addActionForClass(CreateInspector.actionType, FlexoConcept.class);
		FlexoObjectImpl.addActionForClass(CreateInspector.actionType, FMLCompilationUnit.class);
	}

	/**
	 * The concept an inspector would be created for: the object itself, or the VirtualModel of a compilation unit.
	 */
	public static FlexoConcept conceptOf(FMLObject object) {
		if (object instanceof FlexoConcept) {
			return (FlexoConcept) object;
		}
		if (object instanceof FMLCompilationUnit) {
			return ((FMLCompilationUnit) object).getVirtualModel();
		}
		return null;
	}

	/**
	 * Whether supplied concept declares an inspector <b>of its own</b>.
	 *
	 * <p>
	 * Deliberately not <code>getInspectorComponentResource() != null</code>, which also answers for an inspector merely INHERITED from a
	 * parent concept: refusing the action there would make it impossible to ever give a child concept its own. These are exactly the two
	 * branches the resolution tries before walking up the hierarchy - an explicit annotation, or the naming convention.
	 */
	public static boolean declaresItsOwnInspector(FlexoConcept concept) {

		if (concept.hasMetaData(FlexoConcept.INSPECTOR_METADATA)) {
			return true;
		}

		FMLCompilationUnit compilationUnit = concept.getDeclaringCompilationUnit();
		return compilationUnit != null && concept.getName() != null
				&& compilationUnit.getContainedArtefact(concept.getName() + FIBComponentResourceFactory.INSPECTOR_SUFFIX) != null;
	}

	private boolean useTabbedPanel = false;

	/**
	 * Whether the inspector is laid out inside a tabbed panel, rather than as a plain two-column panel.
	 *
	 * <p>
	 * Off by default: a plain panel is what an inspector of a single concept needs, and <code>ModuleInspectorController</code> shows it as one
	 * tab of the inspector of the FlexoConceptInstance class, titled after the concept. Turn it on to contribute tabs of one's own: a
	 * <code>&lt;TabPanel name="Tab"&gt;</code> is merged, by that name, into the tabs the platform already shows.
	 */
	public boolean getUseTabbedPanel() {
		return useTabbedPanel;
	}

	public void setUseTabbedPanel(boolean useTabbedPanel) {
		if (useTabbedPanel != this.useTabbedPanel) {
			this.useTabbedPanel = useTabbedPanel;
			getPropertyChangeSupport().firePropertyChange("useTabbedPanel", !useTabbedPanel, useTabbedPanel);
		}
	}

	private String inspectorName;
	private List<InspectorEntryConfiguration> entries;
	private FIBComponentResource newInspectorResource;

	private CreateInspector(FMLObject focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	public FlexoConcept getFlexoConcept() {
		return conceptOf(getFocusedObject());
	}

	public String getInspectorName() {
		if (inspectorName == null && getFlexoConcept() != null) {
			inspectorName = getFlexoConcept().getName() + FIBComponentResourceFactory.INSPECTOR_SUFFIX;
		}
		return inspectorName;
	}

	public void setInspectorName(String inspectorName) {
		if (inspectorName != null && !inspectorName.equals(this.inspectorName)) {
			String oldValue = this.inspectorName;
			this.inspectorName = inspectorName;
			getPropertyChangeSupport().firePropertyChange("inspectorName", oldValue, inspectorName);
		}
	}

	/**
	 * One line per accessible property of the concept, all selected: what the wizard shows and lets the user edit.
	 */
	public List<InspectorEntryConfiguration> getEntries() {
		if (entries == null) {
			entries = new ArrayList<>();
			if (getFlexoConcept() != null) {
				for (FlexoProperty<?> property : getFlexoConcept().getAccessibleProperties()) {
					entries.add(new InspectorEntryConfiguration(property));
				}
			}
		}
		return entries;
	}

	public boolean isValid() {
		return getFlexoConcept() != null && getFlexoConcept().getDeclaringCompilationUnit() != null && getInspectorName() != null
				&& getInspectorName().endsWith(FIBComponentResourceFactory.INSPECTOR_SUFFIX) && !nameIsAlreadyTaken();
	}

	/**
	 * Whether the container already holds an artefact under the chosen name. Creating over it would silently replace a component - possibly
	 * a fragment nothing names, which no other check would notice.
	 */
	public boolean nameIsAlreadyTaken() {
		FMLCompilationUnit compilationUnit = getFlexoConcept() != null ? getFlexoConcept().getDeclaringCompilationUnit() : null;
		return compilationUnit != null && compilationUnit.getContainedArtefact(getInspectorName()) != null;
	}

	/** The component that was created, so that the caller may open it in the editor. */
	public FIBComponentResource getNewInspectorResource() {
		return newInspectorResource;
	}

	@Override
	protected void doAction(Object context) throws FlexoException {

		FlexoConcept concept = getFlexoConcept();
		CompilationUnitResource compilationUnitResource = (CompilationUnitResource) concept.getDeclaringCompilationUnit().getResource();
		FlexoResourceCenter<?> resourceCenter = compilationUnitResource.getResourceCenter();

		newInspectorResource = makeResource(compilationUnitResource, resourceCenter);
		try {
			newInspectorResource.getResourceData().setComponent(makeInspectorComponent(concept));
			newInspectorResource.save();
		} catch (Exception e) {
			throw new FlexoException("Could not fill " + newInspectorResource.getURI(), e);
		}

		logger.info("Created inspector " + newInspectorResource.getURI() + " for " + concept);
	}

	@SuppressWarnings("unchecked")
	private <I> FIBComponentResource makeResource(CompilationUnitResource compilationUnitResource, FlexoResourceCenter<I> resourceCenter)
			throws FlexoException {

		// The component goes beside the FML source, in the Xxx.fml/ container, which is what the naming convention of
		// FlexoConcept.getInspectorComponentResource() then resolves
		I containerDirectory = resourceCenter.getContainer((I) compilationUnitResource.getIODelegate().getSerializationArtefact());
		I artefact = resourceCenter.createEntry(getInspectorName(), containerDirectory);

		FIBComponentResourceFactory factory = resourceCenter.getServiceManager().getTechnologyAdapterService()
				.getTechnologyAdapter(org.openflexo.foundation.fml.FMLTechnologyAdapter.class)
				.getResourceFactory(FIBComponentResourceFactory.class);

		try {
			return factory.makeResource(artefact, resourceCenter, true);
		} catch (Exception e) {
			throw new FlexoException("Could not create " + getInspectorName() + " in " + containerDirectory, e);
		}
	}

	/**
	 * Build the component: a FIBInspector laid out in two columns - directly, or inside the single tab of a tabbed panel - with a label and a
	 * widget per selected property.
	 *
	 * <p>
	 * When a tabbed panel is asked for, the <code>TabPanel</code> is named as the platform names its own: that name is the key its tabs are
	 * merged by into the inspector of the FlexoConceptInstance class.
	 */
	private FIBComponent makeInspectorComponent(FlexoConcept concept) throws FlexoException {

		FIBModelFactory factory = makeFIBModelFactory(concept);

		FIBPanel inspector = factory.newInstance(FIBInspector.class);
		inspector.setName(concept.getName() + "Inspector");
		inspector.setDataClass(org.openflexo.foundation.fml.rt.FlexoConceptInstance.class);
		inspector.setControllerClassName("org.openflexo.inspector.FIBInspectorController");

		// Where the widgets go: the inspector itself, or the single tab of a tabbed panel
		FIBPanel content;

		if (getUseTabbedPanel()) {
			FIBTabPanel tabPanel = factory.newInstance(FIBTabPanel.class);
			// The name IS the merge key: ModuleInspectorController merges by component name, so this is what makes the
			// tab land inside the tabs of the FlexoConceptInstance class inspector
			tabPanel.setName("Tab");
			inspector.addToSubComponents(tabPanel);

			FIBTab tab = factory.newFIBTab();
			tab.setTitle(concept.getName());
			tab.setName(concept.getName() + "Tab");
			tabPanel.addToSubComponents(tab);
			content = tab;
		}
		else {
			content = inspector;
		}

		content.setLayout(Layout.twocols);

		// Deliberately NO useScrollBar on the ROOT: the module view showing this component is already wrapped in a
		// JScrollPane by FlexoMainPane. Worse, a scrolling root makes SwingRenderingAdapter.getResultingJComponent()
		// build a JScrollPane around the root and REPARENT it, which pulls the whole component out of the editor panel
		// the first time focus or selection is painted. A tab may scroll: it is not the root.
		if (getUseTabbedPanel()) {
			content.setUseScrollBar(true);
		}

		// Declared on the ROOT, and typed by the concept rather than by the bare FlexoConceptInstance the dataClassName
		// names: this is what makes 'data.someRole' resolve, and it makes the file self-describing rather than relying on
		// FMLControlledComponent.bindToConcept to retype it at load time.
		FIBVariable<?> dataVariable = factory.newFIBVariable(inspector, FIBComponent.DEFAULT_DATA_VARIABLE, concept.getInstanceType());
		inspector.addToVariables(dataVariable);

		for (InspectorEntryConfiguration entry : getEntries()) {
			if (entry.getSelected()) {
				appendEntry(entry, content, factory, concept);
			}
		}

		content.finalizeDeserialization();

		return inspector;
	}

	/**
	 * A label on the left column and the widget of the property on the right - the shape every inspector of the infrastructure has.
	 */
	private void appendEntry(InspectorEntryConfiguration entry, FIBPanel content, FIBModelFactory factory, FlexoConcept concept) {

		FIBLabel label = factory.newFIBLabel();
		label.setLabel(entry.getLabel());
		label.setName(entry.getPropertyName() + "Label");
		content.addToSubComponentsNoNotification(label, new TwoColsLayoutConstraints(TwoColsLayoutLocation.left, false, false));

		boolean[] expand = { true, false };
		FIBWidget widget = makeWidget(entry, factory, expand, concept);

		if (widget == null) {
			logger.warning("No widget for " + entry.getPropertyName() + " of type " + entry.getType());
			return;
		}

		widget.setData(new DataBinding<>(FIBComponent.DEFAULT_DATA_VARIABLE + "." + entry.getPropertyName()));

		// A widget offering a CHOICE gets its list from getWidgetDefinitionAccess(), which points into the deprecated
		// FlexoConceptInspector ('flexoConcept.inspector.getEntry("…")'). That entry is scaffolding and does not exist in
		// the generated file, so the binding would be dead on arrival. Better an empty list the author fills in the
		// editor than a path that silently resolves to nothing.
		if (widget instanceof FIBMultipleValues) {
			@SuppressWarnings("rawtypes")
			FIBMultipleValues multipleValues = (FIBMultipleValues) widget;
			multipleValues.setList(null);
		}
		content.addToSubComponentsNoNotification(widget, new TwoColsLayoutConstraints(TwoColsLayoutLocation.right, expand[0], expand[1]));
	}

	/**
	 * Ask each technology adapter controller for the widget of this entry, the first to answer winning - so a property typed by a technology
	 * gets that technology's own selector.
	 *
	 * <p>
	 * The {@link InspectorEntry} built here is scaffolding: it exists only to carry the label, type and widget kind into
	 * {@link TechnologyAdapterController#makeWidget}, whose parameter type has no other implementation. It is never added to the concept.
	 */
	private FIBWidget makeWidget(InspectorEntryConfiguration entry, FIBModelFactory factory, boolean[] expand, FlexoConcept concept) {

		InspectorEntry scaffolding = concept.getFMLModelFactory().newInspectorEntry();
		try {
			scaffolding.setName(entry.getPropertyName());
		} catch (InvalidNameException e) {
			// The property name is already a valid FML identifier; nothing to recover from
			logger.log(Level.FINE, "Unexpected invalid name " + entry.getPropertyName(), e);
		}
		scaffolding.setLabel(entry.getLabel());
		scaffolding.setWidget(entry.getWidget());
		scaffolding.setData(new DataBinding<>(entry.getPropertyName()));

		// Raw type deliberately, as ModuleInspectorController does: the static accessor is parameterized on the adapter's
		// own type, which a wildcard cannot satisfy
		for (@SuppressWarnings("rawtypes")
		TechnologyAdapter technologyAdapter : getServiceManager().getTechnologyAdapterService().getTechnologyAdapters()) {
			TechnologyAdapterController<?> controller = FlexoController.getTechnologyAdapterController(technologyAdapter);
			if (controller != null) {
				FIBWidget returned = controller.makeWidget(scaffolding, null, factory, FIBComponent.DEFAULT_DATA_VARIABLE, expand);
				if (returned != null) {
					return returned;
				}
			}
		}
		return null;
	}

	private FIBModelFactory makeFIBModelFactory(FlexoConcept concept) throws FlexoException {
		try {
			return new FIBModelFactory(null, getServiceManager().getTechnologyAdapterService(), FIBInspector.class);
		} catch (Exception e) {
			throw new FlexoException("Could not build a model factory for the inspector of " + concept, e);
		}
	}
}
