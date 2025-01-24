/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Fml-technologyadapter-ui, a component of the software infrastructure 
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

package org.openflexo.fml.controller.action;

import java.awt.Dimension;
import java.awt.Image;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.openflexo.ApplicationContext;
import org.openflexo.components.wizard.FlexoActionWizard;
import org.openflexo.components.wizard.WizardStep;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.action.CreateTypeDeclaration;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.icon.FMLIconLibrary;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.toolbox.JavaUtils;
import org.openflexo.view.controller.FlexoController;

public class CreateTypeDeclarationWizard extends FlexoActionWizard<CreateTypeDeclaration> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CreateTypeDeclarationWizard.class.getPackage().getName());

	private static final String NO_ABBREV = "please_choose_an_abbreviation_for_the_new_type";
	private static final String DUPLICATED_ABBREV = "duplicated_type_abbreviation_in_this_compilation_unit";
	private static final String WRONG_TYPE_NAME_SYNTAX = "wrong_type_name_syntax";
	private static final String NO_TYPE = "please_choose_a_referenced_type";

	private final DescribeTypeDeclaration describeTypeDeclaration;

	private static final Dimension DIMENSIONS = new Dimension(600, 500);

	public CreateTypeDeclarationWizard(CreateTypeDeclaration action, FlexoController controller) {
		super(action, controller);
		addStep(describeTypeDeclaration = new DescribeTypeDeclaration());
	}

	@Override
	public String getWizardTitle() {
		return getAction().getLocales().localizedForKey("create_type_declaration");
	}

	@Override
	public Image getDefaultPageImage() {
		return IconFactory.getImageIcon(FMLIconLibrary.TYPEDEF_BIG_ICON, IconLibrary.BIG_NEW_MARKER).getImage();
	}

	public DescribeTypeDeclaration getDescribeModelSlot() {
		return describeTypeDeclaration;
	}

	@Override
	public Dimension getPreferredSize() {
		return DIMENSIONS;
	}

	/**
	 * This step is used to set {@link VirtualModel} to be used, as well as name and title of the {@link FMLRTVirtualModelInstance}
	 * 
	 * @author sylvain
	 *
	 */
	@FIBPanel("Fib/Wizard/CreateFMLElement/DescribeTypeDeclaration.fib")
	public class DescribeTypeDeclaration extends WizardStep {

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public CreateTypeDeclaration getAction() {
			return CreateTypeDeclarationWizard.this.getAction();
		}

		@Override
		public String getTitle() {
			return getAction().getLocales().localizedForKey("describe_type_declaration");
		}

		@Override
		public boolean isValid() {

			if (StringUtils.isEmpty(getAbbrev())) {
				setIssueMessage(getAction().getLocales().localizedForKey(NO_ABBREV), IssueMessageType.ERROR);
				return false;
			}
			if (getAction().getFocusedObject().getTypeDeclaration(getAbbrev()) != null) {
				setIssueMessage(getAction().getLocales().localizedForKey(DUPLICATED_ABBREV), IssueMessageType.ERROR);
				return false;
			}
			if (!getAbbrev().equals(JavaUtils.getClassName(getAbbrev()))) {
				setIssueMessage(getAction().getLocales().localizedForKey(WRONG_TYPE_NAME_SYNTAX), IssueMessageType.ERROR);
				return false;
			}
			if (getReferencedType() == null) {
				setIssueMessage(getAction().getLocales().localizedForKey(NO_TYPE), IssueMessageType.ERROR);
				return false;
			}
			return true;
		}

		public String getAbbrev() {
			return getAction().getAbbrev();
		}

		public void setAbbrev(String abbrev) {
			if ((abbrev == null && getAbbrev() != null) || (abbrev != null && !abbrev.equals(getAbbrev()))) {
				String oldValue = getAbbrev();
				getAction().setAbbrev(abbrev);
				getPropertyChangeSupport().firePropertyChange("abbrev", oldValue, abbrev);
				checkValidity();
			}
		}

		public Type getReferencedType() {
			return getAction().getReferencedType();
		}

		public void setReferencedType(Type referencedType) {
			if ((referencedType == null && getReferencedType() != null)
					|| (referencedType != null && !referencedType.equals(getReferencedType()))) {
				Type oldValue = getReferencedType();
				getAction().setReferencedType(referencedType);
				getPropertyChangeSupport().firePropertyChange("referencedType", oldValue, referencedType);
				checkValidity();
			}
		}
	}

}
