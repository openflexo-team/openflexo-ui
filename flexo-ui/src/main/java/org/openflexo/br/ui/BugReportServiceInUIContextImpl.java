/**
 *
 * Copyright (c) 2014, Openflexo
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

package org.openflexo.br.ui;

import java.util.List;

import org.openflexo.ApplicationContext;
import org.openflexo.br.BugReportServiceImpl;
import org.openflexo.br.github.GitHubClient;
import org.openflexo.br.github.model.GitHubRepository;
import org.openflexo.foundation.task.Progress;
import org.openflexo.module.FlexoModule;
import org.openflexo.toolbox.StringUtils;

/**
 * Service responsible for fetching GitHub repositories from the openflexo-team organization and providing them for bug report submission.
 *
 * Replaces the former JIRA-based implementation.
 */
public class BugReportServiceInUIContextImpl extends BugReportServiceImpl {

	private List<GitHubRepository> repositories;

	public BugReportServiceInUIContextImpl() {
	}

	@Override
	public ApplicationContext getServiceManager() {
		return (ApplicationContext) super.getServiceManager();
	}

	/**
	 * Tries to find the most relevant repository for the active module using a simple name-based heuristic. Returns the first repository as
	 * a fallback.
	 */
	public GitHubRepository getMostProbableRepository(Exception e, FlexoModule<?> activeModule) {
		if (repositories == null || repositories.isEmpty()) {
			return null;
		}
		if (activeModule != null) {
			String moduleName = activeModule.getModule().getName().toLowerCase().replace(" ", "-");
			for (GitHubRepository repo : repositories) {
				if (repo.getName().toLowerCase().contains(moduleName)) {
					return repo;
				}
			}
			// Try with the short name
			String shortName = activeModule.getModule().getShortName().toLowerCase();
			for (GitHubRepository repo : repositories) {
				if (repo.getName().toLowerCase().contains(shortName)) {
					return repo;
				}
			}
		}
		// Default to openflexo-modules if present, otherwise first repo
		GitHubRepository modules = getRepositoryByName("openflexo-modules");
		return modules != null ? modules : repositories.get(0);
	}

	private boolean testGitHubConnection() {
		String token = getServiceManager().getBugReportPreferences().getGithubToken();
		if (StringUtils.isNotEmpty(token)) {
			GitHubClient client = new GitHubClient(token);
			return client.testConnection();
		}
		return false;
	}

	/**
	 * Ensures a valid GitHub token is available, prompting the user if needed. Returns the token, or null if the user cancels.
	 */
	@Override
	public String askTokenWhenRequired() {
		boolean valid = testGitHubConnection();

		while (!valid) {
			Progress.forceHideTaskBar();
			if (!GitHubTokenDialog.askToken(getServiceManager())) {
				Progress.stopForceHideTaskBar();
				return null;
			}
			valid = testGitHubConnection();
		}

		Progress.stopForceHideTaskBar();
		return getServiceManager().getBugReportPreferences().getGithubToken();
	}

}
