/**
 *
 * Copyright (c) 2013-2014, Openflexo
 *
 * This file is part of Flexo-ui, a component of the software infrastructure
 * developed at Openflexo.
 *
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either
 * version 1.1 of the License, or any later version ), which is available at
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 *
 * You can redistribute it and/or modify under the terms of either of these licenses
 *
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 *
 */

package org.openflexo.prefs;

import org.openflexo.ApplicationContext;
import org.openflexo.components.PreferencesDialog;
import org.openflexo.foundation.secrets.SecretsService;
import org.openflexo.gina.controller.FIBController.Status;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.swing.utils.JFIBDialog;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;

/**
 * Small modal dialog prompting for a secret name and value, backing the "Add" / "Edit" actions of {@link SecretsPreferences}.
 * <p>
 * The value is never pre-filled when editing an existing secret: this dialog only ever asks for a new value to set, it never displays
 * an existing secret value.
 */
public class SecretEntryDialog {

	public static final Resource FIB_FILE = ResourceLocator.locateResource("Fib/Prefs/SecretEntryDialog.fib");

	private String name;
	private String value;
	private final boolean editingExistingKey;

	public SecretEntryDialog(String existingKey) {
		this.name = existingKey;
		this.editingExistingKey = existingKey != null;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean isEditingExistingKey() {
		return editingExistingKey;
	}

	/**
	 * Show the dialog modally. If the user validates, the entered secret is written to <code>secretsService</code>.
	 *
	 * @param existingKey
	 *            the name of the secret to set a new value for, or <code>null</code> to create a new secret
	 */
	public static boolean askSecret(ApplicationContext applicationContext, SecretsService secretsService, String existingKey) {
		SecretEntryDialog data = new SecretEntryDialog(existingKey);
		// Owned by the (modal) PreferencesDialog itself, not the main FlexoFrame: this dialog is always opened from within the
		// Preferences window, and a dialog owned by a window other than the currently displayed modal one can end up stacked behind it.
		PreferencesDialog preferencesDialog = PreferencesDialog.getPreferencesDialog(applicationContext, null);
		String title = existingKey != null ? "Edit secret \"" + existingKey + "\"" : "Add a secret";
		FIBComponent fibComponent = applicationContext.getApplicationFIBLibraryService().getApplicationFIBLibrary()
				.retrieveFIBComponent(FIB_FILE);
		JFIBDialog<SecretEntryDialog> dialog = JFIBDialog.instanciateAndShowDialog(title, fibComponent, data, preferencesDialog, true,
				FlexoLocalization.getMainLocalizer());
		if (dialog.getStatus() == Status.VALIDATED && data.getName() != null && !data.getName().trim().isEmpty()
				&& data.getValue() != null && !data.getValue().isEmpty()) {
			secretsService.setSecret(data.getName().trim(), data.getValue());
			return true;
		}
		return false;
	}
}
