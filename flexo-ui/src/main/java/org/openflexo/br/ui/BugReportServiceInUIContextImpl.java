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

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.openflexo.ApplicationContext;
import org.openflexo.br.BugReportServiceImpl;
import org.openflexo.br.SubmitIssueReport;
import org.openflexo.br.github.GitHubClient;
import org.openflexo.br.github.model.GitHubRepository;
import org.openflexo.foundation.task.Progress;
import org.openflexo.gina.swing.utils.JFIBDialog;
import org.openflexo.module.FlexoModule;
import org.openflexo.swing.ImageUtils;
import org.openflexo.swing.ImageUtils.ImageType;
import org.openflexo.toolbox.FileUtils;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.view.FlexoDialog;
import org.openflexo.view.FlexoFrame;

/**
 * UI specialization of BugReportServiceImpl.
 *
 * Adds:
 * - Token management via a dialog prompt (askTokenWhenRequired)
 * - Screenshot capture from AWT windows (collectScreenshots)
 * - Repository heuristic based on the active module (getMostProbableRepository)
 */
public class BugReportServiceInUIContextImpl extends BugReportServiceImpl {

	public BugReportServiceInUIContextImpl() {
	}

	@Override
	public ApplicationContext getServiceManager() {
		return (ApplicationContext) super.getServiceManager();
	}

	// -----------------------------------------------------------------------
	// Token management
	// -----------------------------------------------------------------------

	@Override
	protected String getStoredToken() {
		return getServiceManager().getBugReportPreferences().getGithubToken();
	}

	/**
	 * Ensures a valid GitHub token is available, prompting the user if needed.
	 * Returns the token, or null if the user cancels.
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
		return getStoredToken();
	}

	private boolean testGitHubConnection() {
		String token = getStoredToken();
		if (StringUtils.isNotEmpty(token)) {
			GitHubClient client = new GitHubClient(token);
			return client.testConnection();
		}
		return false;
	}

	// -----------------------------------------------------------------------
	// Screenshot capture (UI-specific)
	// -----------------------------------------------------------------------

	/**
	 * Captures all visible FlexoFrames and their owned dialogs as PNG files in the temp directory.
	 */
	@Override
	protected List<File> collectScreenshots(SubmitIssueReport report) {
		List<File> files = new ArrayList<>();
		for (Frame frame : Frame.getFrames()) {
			if (frame instanceof FlexoFrame && frame.isVisible() && frame.getWidth() > 0 && frame.getHeight() > 0) {
				File f = captureWindow(frame, frame.getTitle(), report);
				if (f != null) {
					files.add(f);
				}
				for (Window w : frame.getOwnedWindows()) {
					if ((w instanceof FlexoDialog || w instanceof JFIBDialog) && w.isVisible()) {
						File wf = captureWindow(w, ((Dialog) w).getTitle(), report);
						if (wf != null) {
							files.add(wf);
						}
					}
				}
			}
		}
		return files;
	}

	private File captureWindow(Window window, String title, SubmitIssueReport report) {
		if (window.isVisible() && window.getWidth() > 0 && window.getHeight() > 0) {
			try {
				File file = new File(System.getProperty("java.io.tmpdir"), FileUtils.getValidFileName(title + ".png"));
				ImageUtils.saveImageToFile(ImageUtils.createImageFromComponent(window), file, ImageType.PNG);
				return file;
			} catch (Exception e) {
				report.addToWarning("Could not capture screenshot: " + title + "\n\t" + e.getMessage());
				logger.log(Level.SEVERE, "Error capturing screenshot: " + title, e);
			}
		}
		return null;
	}

	// -----------------------------------------------------------------------
	// Repository heuristic
	// -----------------------------------------------------------------------

	/**
	 * Tries to find the most relevant repository for the active module using a simple name-based heuristic.
	 * Falls back to openflexo-modules, then to the first repository in the list.
	 */
	public GitHubRepository getMostProbableRepository(Exception e, FlexoModule<?> activeModule) {
		List<GitHubRepository> repositories = getRepositories();
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
			String shortName = activeModule.getModule().getShortName().toLowerCase();
			for (GitHubRepository repo : repositories) {
				if (repo.getName().toLowerCase().contains(shortName)) {
					return repo;
				}
			}
		}
		GitHubRepository modules = getRepositoryByName("openflexo-modules");
		return modules != null ? modules : repositories.get(0);
	}
}
