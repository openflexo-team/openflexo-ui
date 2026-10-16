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
import org.openflexo.fml.gina.action.AbstractCreateFIBComponent;
import org.openflexo.fml.gina.action.InspectorEntryConfiguration;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.view.controller.FlexoController;

/**
 * The wizard shared by the actions creating a component in a VirtualModel container ({@link AbstractCreateFIBComponent}): names the
 * component, lets the reader select, deselect and edit the entries proposed for the properties of the concept, and offers what is specific
 * to the action - a tabbed layout for an inspector, a named variant for a user interface. A single page serves both: it binds to
 * properties every action has, with neutral defaults where the action has no use for them.
 *
 * @author sylvain
 */
public abstract class AbstractCreateFIBComponentWizard<A extends AbstractCreateFIBComponent<A>> extends FlexoActionWizard<A> {

	private final DescribeComponent describeComponent;

	public AbstractCreateFIBComponentWizard(A action, FlexoController controller) {
		super(action, controller);
		addStep(describeComponent = new DescribeComponent());
	}

	/** Localization key of the title of the single step. */
	protected abstract String getDescribeStepTitleKey();

	/** Localization key of the message shown for a name missing its suffix. */
	protected abstract String getInvalidNameMessageKey();

	/** Localization key of the warning shown when no entry is selected. */
	protected abstract String getEmptyComponentWarningKey();

	@Override
	public Image getDefaultPageImage() {
		// The GINA component icon, at the size a wizard page expects
		return IconFactory.getImageIcon(FMLGINAIconLibrary.FIB_COMPONENT_BIG_ICON, IconLibrary.BIG_NEW_MARKER).getImage();
	}

	public DescribeComponent getDescribeComponent() {
		return describeComponent;
	}

	/**
	 * The single step: the name of the component, what is specific to the action, and the table of entries.
	 */
	@FIBPanel("Fib/Wizard/DescribeFIBComponent.fib")
	public class DescribeComponent extends WizardStep {

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public AbstractCreateFIBComponent<?> getAction() {
			return AbstractCreateFIBComponentWizard.this.getAction();
		}

		@Override
		public String getTitle() {
			return getAction().getLocales().localizedForKey(getDescribeStepTitleKey());
		}

		@Override
		public boolean isValid() {

			if (getAction().nameIsAlreadyTaken()) {
				setIssueMessage(getAction().getLocales().localizedForKey("a_component_of_this_name_already_exists"), IssueMessageType.ERROR);
				return false;
			}

			if (getAction().getDeclareAsVariant()) {
				if (!getAction().hasValidVariantName()) {
					setIssueMessage(getAction().getLocales().localizedForKey("please_supply_a_valid_variant_name"), IssueMessageType.ERROR);
					return false;
				}
				if (getAction().variantIsAlreadyDeclared()) {
					setIssueMessage(getAction().getLocales().localizedForKey("this_variant_is_already_declared"), IssueMessageType.ERROR);
					return false;
				}
			}

			if (!getAction().isValid()) {
				setIssueMessage(getAction().getLocales().localizedForKey(getInvalidNameMessageKey()), IssueMessageType.ERROR);
				return false;
			}

			if (getSelectedEntries() == 0) {
				// Not an error: an empty component is a legitimate starting point, filled in the editor afterwards
				setIssueMessage(getAction().getLocales().localizedForKey(getEmptyComponentWarningKey()), IssueMessageType.WARNING);
			}

			return true;
		}

		public String getComponentName() {
			return getAction().getComponentName();
		}

		public void setComponentName(String componentName) {
			if (componentName != null && !componentName.equals(getComponentName())) {
				String oldValue = getComponentName();
				getAction().setComponentName(componentName);
				getPropertyChangeSupport().firePropertyChange("componentName", oldValue, componentName);
				// The default variant name follows the component name
				getPropertyChangeSupport().firePropertyChange("variantName", null, getVariantName());
				checkValidity();
			}
		}

		public boolean getSupportsTabbedPanel() {
			return getAction().supportsTabbedPanel();
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

		public boolean getSupportsVariantDeclaration() {
			return getAction().supportsVariantDeclaration();
		}

		public boolean getDeclareAsVariant() {
			return getAction().getDeclareAsVariant();
		}

		public void setDeclareAsVariant(boolean declareAsVariant) {
			if (declareAsVariant != getDeclareAsVariant()) {
				getAction().setDeclareAsVariant(declareAsVariant);
				getPropertyChangeSupport().firePropertyChange("declareAsVariant", !declareAsVariant, declareAsVariant);
				checkValidity();
			}
		}

		public String getVariantName() {
			return getAction().getVariantName();
		}

		public void setVariantName(String variantName) {
			if (variantName != null && !variantName.equals(getVariantName())) {
				String oldValue = getVariantName();
				getAction().setVariantName(variantName);
				getPropertyChangeSupport().firePropertyChange("variantName", oldValue, variantName);
				checkValidity();
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
