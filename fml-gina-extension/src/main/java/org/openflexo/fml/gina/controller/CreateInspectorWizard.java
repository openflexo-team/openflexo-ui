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
package org.openflexo.fml.gina.controller;

import java.awt.Image;
import java.util.List;


import org.openflexo.ApplicationContext;
import org.openflexo.components.wizard.FlexoActionWizard;
import org.openflexo.components.wizard.WizardStep;
import org.openflexo.fml.gina.FMLGINAIconLibrary;
import org.openflexo.fml.gina.action.CreateInspector;
import org.openflexo.fml.gina.action.InspectorEntryConfiguration;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.view.controller.FlexoController;

/**
 * Wizard of {@link CreateInspector}: names the inspector, and lets the reader select, deselect and edit the entries proposed for the
 * properties of the concept.
 *
 * @author sylvain
 */
public class CreateInspectorWizard extends FlexoActionWizard<CreateInspector> {

	private final DescribeInspector describeInspector;

	public CreateInspectorWizard(CreateInspector action, FlexoController controller) {
		super(action, controller);
		addStep(describeInspector = new DescribeInspector());
	}

	@Override
	public String getWizardTitle() {
		return getAction().getLocales().localizedForKey("create_inspector");
	}

	@Override
	public Image getDefaultPageImage() {
		// The GINA component icon, at the size a wizard page expects
		return IconFactory.getImageIcon(FMLGINAIconLibrary.FIB_COMPONENT_BIG_ICON, IconLibrary.BIG_NEW_MARKER).getImage();
	}

	public DescribeInspector getDescribeInspector() {
		return describeInspector;
	}

	/**
	 * The single step: the name of the component, and the table of entries.
	 */
	@FIBPanel("Fib/Wizard/DescribeInspector.fib")
	public class DescribeInspector extends WizardStep {

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public CreateInspector getAction() {
			return CreateInspectorWizard.this.getAction();
		}

		@Override
		public String getTitle() {
			return getAction().getLocales().localizedForKey("describe_inspector");
		}

		@Override
		public boolean isValid() {

			if (getAction().nameIsAlreadyTaken()) {
				setIssueMessage(getAction().getLocales().localizedForKey("a_component_of_this_name_already_exists"), IssueMessageType.ERROR);
				return false;
			}

			if (!getAction().isValid()) {
				setIssueMessage(getAction().getLocales().localizedForKey("please_supply_a_valid_inspector_name"), IssueMessageType.ERROR);
				return false;
			}

			if (getSelectedEntries() == 0) {
				// Not an error: an empty inspector is a legitimate starting point, filled in the editor afterwards
				setIssueMessage(getAction().getLocales().localizedForKey("no_entry_selected_the_inspector_will_be_empty"),
						IssueMessageType.WARNING);
			}

			return true;
		}

		public String getInspectorName() {
			return getAction().getInspectorName();
		}

		public void setInspectorName(String inspectorName) {
			if (inspectorName != null && !inspectorName.equals(getInspectorName())) {
				String oldValue = getInspectorName();
				getAction().setInspectorName(inspectorName);
				getPropertyChangeSupport().firePropertyChange("inspectorName", oldValue, inspectorName);
				checkValidity();
			}
		}

		public boolean getUseTabbedPanel() {
			return getAction().getUseTabbedPanel();
		}

		public void setUseTabbedPanel(boolean useTabbedPanel) {
			if (useTabbedPanel != getUseTabbedPanel()) {
				getAction().setUseTabbedPanel(useTabbedPanel);
				getPropertyChangeSupport().firePropertyChange("useTabbedPanel", !useTabbedPanel, useTabbedPanel);
			}
		}

		public List<InspectorEntryConfiguration> getEntries() {
			return getAction().getEntries();
		}

		/** Bound by the table, so unchecking a line re-evaluates the step. */
		public void entryChanged() {
			getPropertyChangeSupport().firePropertyChange("entries", null, getEntries());
			checkValidity();
		}

		private int getSelectedEntries() {
			int returned = 0;
			for (InspectorEntryConfiguration entry : getEntries()) {
				if (entry.getSelected()) {
					returned++;
				}
			}
			return returned;
		}
	}
}
