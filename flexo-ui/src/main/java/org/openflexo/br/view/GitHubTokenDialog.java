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

package org.openflexo.br.view;

import org.openflexo.ApplicationContext;
import org.openflexo.gina.controller.FIBController.Status;
import org.openflexo.gina.swing.utils.JFIBDialog;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.toolbox.ToolBox;
import org.openflexo.view.FlexoFrame;

/**
 * Dialog that prompts the user for a GitHub Personal Access Token.
 * Replaces JIRAURLCredentialsDialog.
 */
public class GitHubTokenDialog {

	public static final Resource FIB_FILE = ResourceLocator.locateResource("Fib/GitHubTokenDialog.fib");

	private static final String GITHUB_TOKEN_URL = "https://github.com/settings/tokens/new?scopes=public_repo&description=OpenFlexo+Bug+Report";

	private String token;
	private final ApplicationContext applicationContext;

	public GitHubTokenDialog(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
		this.token = applicationContext.getBugReportPreferences().getGithubToken();
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getTokenUrlLabel() {
		return "<html><a href=\"" + GITHUB_TOKEN_URL + "\">" + GITHUB_TOKEN_URL + "</a></html>";
	}

	public void openTokenUrl() {
		ToolBox.openURL(GITHUB_TOKEN_URL);
	}

	/**
	 * Shows the token dialog modally. If the user validates, saves the token to preferences.
	 *
	 * @return true if the user provided a token and validated
	 */
	public static boolean askToken(ApplicationContext applicationContext) {
		GitHubTokenDialog data = new GitHubTokenDialog(applicationContext);
		JFIBDialog<GitHubTokenDialog> dialog = JFIBDialog.instanciateAndShowDialog(FIB_FILE, data,
				applicationContext.getApplicationFIBLibraryService().getApplicationFIBLibrary(), FlexoFrame.getActiveFrame(), true,
				FlexoLocalization.getMainLocalizer());
		if (dialog.getStatus() == Status.VALIDATED) {
			applicationContext.getBugReportPreferences().setGithubToken(data.token);
			applicationContext.getPreferencesService().savePreferences();
			return true;
		}
		return false;
	}
}
