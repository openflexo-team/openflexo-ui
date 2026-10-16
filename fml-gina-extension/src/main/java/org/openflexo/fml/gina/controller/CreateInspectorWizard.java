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

import org.openflexo.fml.gina.action.CreateInspector;
import org.openflexo.view.controller.FlexoController;

/**
 * Wizard of {@link CreateInspector}: names the inspector, offers a tabbed layout, and lets the reader select, deselect and edit the entries
 * proposed for the properties of the concept.
 *
 * @author sylvain
 */
public class CreateInspectorWizard extends AbstractCreateFIBComponentWizard<CreateInspector> {

	public CreateInspectorWizard(CreateInspector action, FlexoController controller) {
		super(action, controller);
	}

	@Override
	public String getWizardTitle() {
		return getAction().getLocales().localizedForKey("create_inspector");
	}

	@Override
	protected String getDescribeStepTitleKey() {
		return "describe_inspector";
	}

	@Override
	protected String getInvalidNameMessageKey() {
		return "please_supply_a_valid_inspector_name";
	}

	@Override
	protected String getEmptyComponentWarningKey() {
		return "no_entry_selected_the_inspector_will_be_empty";
	}
}
