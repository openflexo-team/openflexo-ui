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

import java.util.Vector;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.rm.FIBComponentResourceFactory;
import org.openflexo.gina.model.FIBModelFactory;
import org.openflexo.gina.model.container.FIBPanel;
import org.openflexo.gina.model.container.FIBTab;
import org.openflexo.gina.model.container.FIBTabPanel;
import org.openflexo.gina.utils.FIBInspector;
import org.openflexo.localization.LocalizedDelegate;

/**
 * Creates the inspector of a {@link FlexoConcept}, as a <code>.inspector</code> component of the <code>Xxx.fml/</code> container of the
 * VirtualModel declaring it. See {@link AbstractCreateFIBComponent} for what the component holds.
 *
 * <p>
 * A concept has ONE inspector of its own: the action is offered only while it has none - editing the one it has is the editor's job.
 *
 * @author sylvain
 */
public class CreateInspector extends AbstractCreateFIBComponent<CreateInspector> {

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
			return pluginLocales(serviceManager);
		}
	};

	static {
		// A VirtualModel IS a FlexoConcept, so one registration covers both; a compilation unit is offered the action of
		// the VirtualModel it declares, which is what a reader selecting the .fml file expects.
		FlexoObjectImpl.addActionForClass(CreateInspector.actionType, FlexoConcept.class);
		FlexoObjectImpl.addActionForClass(CreateInspector.actionType, FMLCompilationUnit.class);
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

	private CreateInspector(FMLObject focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	public String getComponentSuffix() {
		return FIBComponentResourceFactory.INSPECTOR_SUFFIX;
	}

	@Override
	protected String getDefaultComponentName(FlexoConcept concept) {
		return concept.getName() + FIBComponentResourceFactory.INSPECTOR_SUFFIX;
	}

	@Override
	public boolean supportsTabbedPanel() {
		return true;
	}

	/**
	 * Whether the inspector is laid out inside a tabbed panel, rather than as a plain two-column panel.
	 *
	 * <p>
	 * Off by default: a plain panel is what an inspector of a single concept needs, and <code>ModuleInspectorController</code> shows it as one
	 * tab of the inspector of the FlexoConceptInstance class, titled after the concept. Turn it on to contribute tabs of one's own: a
	 * <code>&lt;TabPanel name="Tab"&gt;</code> is merged, by that name, into the tabs the platform already shows.
	 */
	@Override
	public boolean getUseTabbedPanel() {
		return useTabbedPanel;
	}

	@Override
	public void setUseTabbedPanel(boolean useTabbedPanel) {
		if (useTabbedPanel != this.useTabbedPanel) {
			this.useTabbedPanel = useTabbedPanel;
			getPropertyChangeSupport().firePropertyChange("useTabbedPanel", !useTabbedPanel, useTabbedPanel);
		}
	}

	@Override
	protected FIBPanel makeRootComponent(FIBModelFactory factory, FlexoConcept concept) {
		FIBInspector inspector = factory.newInstance(FIBInspector.class);
		inspector.setName(concept.getName() + "Inspector");
		inspector.setDataClass(org.openflexo.foundation.fml.rt.FlexoConceptInstance.class);
		inspector.setControllerClassName("org.openflexo.inspector.FIBInspectorController");
		return inspector;
	}

	/**
	 * The inspector itself, or the single tab of a tabbed panel.
	 *
	 * <p>
	 * When a tabbed panel is asked for, the <code>TabPanel</code> is named as the platform names its own: that name is the key its tabs are
	 * merged by into the inspector of the FlexoConceptInstance class.
	 */
	@Override
	protected FIBPanel makeContentPanel(FIBPanel root, FIBModelFactory factory, FlexoConcept concept) {

		if (!getUseTabbedPanel()) {
			return root;
		}

		FIBTabPanel tabPanel = factory.newInstance(FIBTabPanel.class);
		tabPanel.setName("Tab");
		root.addToSubComponents(tabPanel);

		FIBTab tab = factory.newFIBTab();
		tab.setTitle(concept.getName());
		tab.setName(concept.getName() + "Tab");
		tabPanel.addToSubComponents(tab);

		// A tab may scroll: it is not the root. The ROOT must not - the module view showing the component is already wrapped in a
		// JScrollPane by FlexoMainPane, and a scrolling root makes SwingRenderingAdapter.getResultingJComponent() build a JScrollPane
		// around it and REPARENT it, which pulls the whole component out of the editor panel the first time focus or selection is painted.
		tab.setUseScrollBar(true);

		return tab;
	}
}
