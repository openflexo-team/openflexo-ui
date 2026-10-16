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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.fml.gina.FMLGINAPlugin;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.InvalidNameException;
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
import org.openflexo.gina.model.FIBWidget;
import org.openflexo.gina.model.container.FIBPanel;
import org.openflexo.gina.model.container.FIBPanel.Layout;
import org.openflexo.gina.model.container.layout.TwoColsLayoutConstraints;
import org.openflexo.gina.model.container.layout.TwoColsLayoutConstraints.TwoColsLayoutLocation;
import org.openflexo.gina.model.widget.FIBLabel;
import org.openflexo.gina.model.widget.FIBMultipleValues;
import org.openflexo.gina.utils.FIBInspector;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.TechnologyAdapterController;
import org.openflexo.view.controller.TechnologyAdapterControllerService;

/**
 * Base of the actions creating a GINA component in the <code>Xxx.fml/</code> container of a VirtualModel, for a {@link FlexoConcept}
 * declared there or for the VirtualModel itself: {@link CreateInspector} creates a <code>.inspector</code>, {@link CreateFIBComponent} a
 * <code>.fib</code>.
 *
 * <p>
 * The component is proposed pre-filled: laid out in two columns, with, for each accessible property of the concept, a label and the widget
 * that represents its type. Both are what the wizard lets the user select, deselect and edit. The result is an ordinary component, edited
 * afterwards in the GINA editor like any other, whose <code>data</code> variable is typed by the concept.
 *
 * <p>
 * The widget of a property is not chosen here: each {@link TechnologyAdapterController} is asked in turn, and the first to answer wins -
 * which is how a technology adapter contributes its own selectors (a VirtualModelInstance, a FlexoConcept, an ontology individual…) rather
 * than a bare text field. That call takes a <code>WidgetContext</code>, and the only implementation of it is the deprecated
 * {@link InspectorEntry}: these actions therefore build throwaway entries, use them as scaffolding, and discard them. They are never
 * attached to the concept and never serialized.
 *
 * <p>
 * What differs between the two actions - a tabbed layout for an inspector, a named variant for a component - is declared here with neutral
 * defaults, so that the single wizard page serving both binds to properties that always exist.
 *
 * @author sylvain
 */
public abstract class AbstractCreateFIBComponent<A extends AbstractCreateFIBComponent<A>> extends FlexoAction<A, FMLObject, FMLObject> {

	private static final Logger logger = Logger.getLogger(AbstractCreateFIBComponent.class.getPackage().getName());

	private String componentName;
	private List<InspectorEntryConfiguration> entries;
	private FIBComponentResource newComponentResource;

	protected AbstractCreateFIBComponent(FlexoActionFactory<A, FMLObject, FMLObject> actionFactory, FMLObject focusedObject,
			List<FMLObject> globalSelection, FlexoEditor editor) {
		super(actionFactory, focusedObject, globalSelection, editor);
	}

	/**
	 * The concept a component would be created for: the object itself, or the VirtualModel of a compilation unit - which is what a reader
	 * selecting the <code>.fml</code> file expects.
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

	/** The dictionary of the plugin, where the strings of these actions live. */
	protected static LocalizedDelegate pluginLocales(FlexoServiceManager serviceManager) {
		return serviceManager.getService(TechnologyAdapterControllerService.class).getPlugin(FMLGINAPlugin.class).getLocales();
	}

	public FlexoConcept getFlexoConcept() {
		return conceptOf(getFocusedObject());
	}

	/** The suffix of the component file. */
	public abstract String getComponentSuffix();

	/** The name proposed for a component of supplied concept. */
	protected abstract String getDefaultComponentName(FlexoConcept concept);

	/** The root of the component, before it is laid out and filled. */
	protected abstract FIBPanel makeRootComponent(FIBModelFactory factory, FlexoConcept concept);

	/** Where the widgets go: the root itself by default. */
	protected FIBPanel makeContentPanel(FIBPanel root, FIBModelFactory factory, FlexoConcept concept) {
		return root;
	}

	/** Called once the component is written. Nothing by default. */
	protected void componentCreated(FlexoConcept concept, CompilationUnitResource compilationUnitResource) throws FlexoException {
	}

	public String getComponentName() {
		if (componentName == null && getFlexoConcept() != null) {
			componentName = getDefaultComponentName(getFlexoConcept());
		}
		return componentName;
	}

	public void setComponentName(String componentName) {
		if (componentName != null && !componentName.equals(this.componentName)) {
			String oldValue = this.componentName;
			this.componentName = componentName;
			getPropertyChangeSupport().firePropertyChange("componentName", oldValue, componentName);
		}
	}

	/** The name of the component without its suffix. */
	public String getComponentBaseName() {
		String name = getComponentName();
		return name != null && name.endsWith(getComponentSuffix()) ? name.substring(0, name.length() - getComponentSuffix().length()) : name;
	}

	/**
	 * One line per accessible property of the concept: what the wizard shows and lets the user edit.
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

	/** Whether the component may be laid out inside a tabbed panel. No, by default. */
	public boolean supportsTabbedPanel() {
		return false;
	}

	public boolean getUseTabbedPanel() {
		return false;
	}

	public void setUseTabbedPanel(boolean useTabbedPanel) {
	}

	/** Whether the component may be declared as a named variant of the concept. No, by default. */
	public boolean supportsVariantDeclaration() {
		return false;
	}

	public boolean getDeclareAsVariant() {
		return false;
	}

	public void setDeclareAsVariant(boolean declareAsVariant) {
	}

	public String getVariantName() {
		return null;
	}

	public void setVariantName(String variantName) {
	}

	public boolean hasValidVariantName() {
		return true;
	}

	public boolean variantIsAlreadyDeclared() {
		return false;
	}

	public boolean isValid() {
		return getFlexoConcept() != null && getFlexoConcept().getDeclaringCompilationUnit() != null && hasValidComponentName()
				&& !nameIsAlreadyTaken();
	}

	public boolean hasValidComponentName() {
		String name = getComponentName();
		return name != null && name.endsWith(getComponentSuffix()) && name.length() > getComponentSuffix().length();
	}

	/**
	 * Whether the container already holds an artefact under the chosen name. Creating over it would silently replace a component - possibly
	 * a fragment nothing names, which no other check would notice.
	 */
	public boolean nameIsAlreadyTaken() {
		return nameIsTaken(getComponentName());
	}

	protected boolean nameIsTaken(String name) {
		FMLCompilationUnit compilationUnit = getFlexoConcept() != null ? getFlexoConcept().getDeclaringCompilationUnit() : null;
		return compilationUnit != null && name != null && compilationUnit.getContainedArtefact(name) != null;
	}

	/** The component that was created, so that the caller may open it in the editor. */
	public FIBComponentResource getNewComponentResource() {
		return newComponentResource;
	}

	@Override
	protected void doAction(Object context) throws FlexoException {

		FlexoConcept concept = getFlexoConcept();
		CompilationUnitResource compilationUnitResource = (CompilationUnitResource) concept.getDeclaringCompilationUnit().getResource();
		FlexoResourceCenter<?> resourceCenter = compilationUnitResource.getResourceCenter();

		newComponentResource = makeResource(compilationUnitResource, resourceCenter);
		try {
			newComponentResource.getResourceData().setComponent(buildComponent());
			newComponentResource.save();
		} catch (Exception e) {
			throw new FlexoException("Could not fill " + newComponentResource.getURI(), e);
		}

		logger.info("Created " + newComponentResource.getURI() + " for " + concept);

		componentCreated(concept, compilationUnitResource);
	}

	@SuppressWarnings("unchecked")
	private <I> FIBComponentResource makeResource(CompilationUnitResource compilationUnitResource, FlexoResourceCenter<I> resourceCenter)
			throws FlexoException {

		// The component goes beside the FML source, in the Xxx.fml/ container, which is where the naming convention and the @UI /
		// @Inspector annotations of FlexoConcept look for it
		I containerDirectory = resourceCenter.getContainer((I) compilationUnitResource.getIODelegate().getSerializationArtefact());
		I artefact = resourceCenter.createEntry(getComponentName(), containerDirectory);

		FIBComponentResourceFactory factory = resourceCenter.getServiceManager().getTechnologyAdapterService()
				.getTechnologyAdapter(org.openflexo.foundation.fml.FMLTechnologyAdapter.class)
				.getResourceFactory(FIBComponentResourceFactory.class);

		try {
			return factory.makeResource(artefact, resourceCenter, true);
		} catch (Exception e) {
			throw new FlexoException("Could not create " + getComponentName() + " in " + containerDirectory, e);
		}
	}

	/**
	 * Build the component, in memory: its root, laid out in two columns - directly, or inside the content panel a concrete action provides
	 * - with a label and a widget per selected entry.
	 *
	 * <p>
	 * Public because it writes nothing: it is what a test can assert on without creating a file in a resource center.
	 */
	public FIBComponent buildComponent() throws FlexoException {

		FlexoConcept concept = getFlexoConcept();
		FIBModelFactory factory = makeFIBModelFactory(concept);

		FIBPanel root = makeRootComponent(factory, concept);
		FIBPanel content = makeContentPanel(root, factory, concept);
		content.setLayout(Layout.twocols);

		// Declared on the ROOT, and typed by the concept rather than by the bare FlexoConceptInstance: this is what makes 'data.someRole'
		// resolve, and it makes the file self-describing rather than relying on FMLControlledComponent.bindToConcept to retype it at load
		// time. (newFIBVariable adds it to the root itself.)
		factory.newFIBVariable(root, FIBComponent.DEFAULT_DATA_VARIABLE, concept.getInstanceType());

		for (InspectorEntryConfiguration entry : getEntries()) {
			if (entry.getSelected()) {
				appendEntry(entry, content, factory, concept);
			}
		}

		content.finalizeDeserialization();

		return root;
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
			throw new FlexoException("Could not build a model factory for a component of " + concept, e);
		}
	}
}
