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

package org.openflexo.br.ui;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.ApplicationVersion;
import org.openflexo.Flexo;
import org.openflexo.br.BugReportSubmission;
import org.openflexo.br.github.GitHubClient;
import org.openflexo.br.github.model.GitHubIssue;
import org.openflexo.br.github.model.GitHubMilestone;
import org.openflexo.br.github.model.GitHubRepository;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.toolbox.PropertyChangedSupportDefaultImplementation;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.toolbox.ToolBox;

/**
 * Pure data model for the GitHub issue report dialog (FIB).
 *
 * Holds the form fields filled in by the user. Call {@link #toBugReportSubmission()} to
 * build the submission DTO that is passed to {@link org.openflexo.br.BugReportServiceImpl#submitIssue}.
 */
public class GitHubIssueReportDialog extends PropertyChangedSupportDefaultImplementation {

	private static final Logger logger = FlexoLogger.getLogger(GitHubIssueReportDialog.class.getPackage().getName());

	public static final Resource FIB_FILE = ResourceLocator.locateResource("Fib/GitHubIssueReportDialog.fib");
	public static final Resource REPORT_FIB_FILE = ResourceLocator.locateResource("Fib/GitHubSubmitIssueReportDialog.fib");

	private final GitHubIssue issue;
	private GitHubRepository repository;
	private GitHubMilestone milestone;

	private boolean sendLogs;
	private boolean sendScreenshots;
	private boolean sendProject;
	private boolean sendSystemProperties;

	private File attachFile;

	private ApplicationContext serviceManager;
	private FlexoProject<?> flexoProject;

	public GitHubIssueReportDialog(Exception e, ApplicationContext serviceManager) {
		this.serviceManager = serviceManager;
		this.issue = new GitHubIssue();

		issue.getPropertyChangeSupport()
				.addPropertyChangeListener(evt -> getPropertyChangeSupport().firePropertyChange("isValid", !isValid(), isValid()));

		sendLogs = true;
		sendScreenshots = false;
		sendSystemProperties = false;
		sendProject = false;

		if (e != null) {
			issue.setStacktrace(e.getClass().getName() + ": " + e.getMessage() + "\n" + ToolBox.getStackTraceAsString(e));
		}
	}

	// -----------------------------------------------------------------------
	// DTO factory
	// -----------------------------------------------------------------------

	/**
	 * Builds a {@link BugReportSubmission} from the current form state.
	 * Called by {@link SendBugReportServiceTask} before delegating to the service.
	 */
	public BugReportSubmission toBugReportSubmission() {
		BugReportSubmission s = new BugReportSubmission();
		s.setIssue(issue);
		s.setRepository(repository);
		s.setMilestone(milestone);
		s.setSendLogs(sendLogs);
		s.setSendScreenshots(sendScreenshots);
		s.setSendSystemProperties(sendSystemProperties);
		s.setSendProject(sendProject);
		s.setLogFile(Flexo.getErrLogFile());
		s.setAttachFile(attachFile);
		s.setProjectDirectory(flexoProject != null ? (File) flexoProject.getProjectDirectory() : null);
		s.setBuildId(ApplicationVersion.BUILD_ID);
		s.setCommitId(ApplicationVersion.COMMIT_ID);
		return s;
	}

	// -----------------------------------------------------------------------
	// Accessors used by the FIB
	// -----------------------------------------------------------------------

	public ApplicationContext getServiceManager() {
		return serviceManager;
	}

	public void setServiceManager(ApplicationContext serviceManager) {
		this.serviceManager = serviceManager;
	}

	public void setFlexoProject(FlexoProject<?> project) {
		this.flexoProject = project;
	}

	public static LocalizedDelegate getLocales(FlexoServiceManager serviceManager) {
		if (serviceManager != null) {
			return serviceManager.getLocalizationService().getFlexoLocalizer();
		}
		return FlexoLocalization.getMainLocalizer();
	}

	public LocalizedDelegate getLocales() {
		return getLocales(serviceManager);
	}

	public List<GitHubRepository> getRepositories() {
		return serviceManager.getBugReportService().getRepositories();
	}

	public GitHubRepository getRepository() {
		return repository;
	}

	public void setRepository(GitHubRepository repository) {
		if (repository != this.repository) {
			GitHubRepository old = this.repository;
			this.repository = repository;
			this.milestone = null;
			getPropertyChangeSupport().firePropertyChange("repository", old, repository);
			getPropertyChangeSupport().firePropertyChange("milestones", null, getMilestones());
			getPropertyChangeSupport().firePropertyChange("milestone", null, null);
		}
	}

	public List<GitHubMilestone> getMilestones() {
		if (repository == null) {
			return Collections.emptyList();
		}
		List<GitHubMilestone> ms = repository.getMilestones();
		if (ms.isEmpty()) {
			String token = serviceManager.getBugReportPreferences().getGithubToken();
			if (StringUtils.isNotEmpty(token)) {
				try {
					GitHubClient client = new GitHubClient(token);
					List<GitHubMilestone> fetched = client.listMilestones(repository);
					if (fetched != null) {
						repository.setMilestones(fetched);
					}
				} catch (Exception e) {
					logger.warning("Could not load milestones: " + e.getMessage());
				}
			}
		}
		return repository.getMilestones();
	}

	public GitHubMilestone getMilestone() {
		return milestone;
	}

	public void setMilestone(GitHubMilestone milestone) {
		GitHubMilestone old = this.milestone;
		this.milestone = milestone;
		getPropertyChangeSupport().firePropertyChange("milestone", old, milestone);
	}

	public GitHubIssue getIssue() {
		return issue;
	}

	public boolean isSendLogs() {
		return sendLogs;
	}

	public void setSendLogs(boolean sendLogs) {
		this.sendLogs = sendLogs;
	}

	public boolean isSendScreenshots() {
		return sendScreenshots;
	}

	public void setSendScreenshots(boolean sendScreenshots) {
		this.sendScreenshots = sendScreenshots;
	}

	public boolean isSendSystemProperties() {
		return sendSystemProperties;
	}

	public void setSendSystemProperties(boolean sendSystemProperties) {
		this.sendSystemProperties = sendSystemProperties;
	}

	public boolean isSendProject() {
		return sendProject;
	}

	public void setSendProject(boolean sendProject) {
		this.sendProject = sendProject;
	}

	public File getAttachFile() {
		return attachFile;
	}

	public void setAttachFile(File attachFile) {
		this.attachFile = attachFile;
	}

	public boolean isValid() {
		return issue != null && repository != null && StringUtils.isNotEmpty(issue.getTitle())
				&& StringUtils.isNotEmpty(issue.getDescription());
	}
}
