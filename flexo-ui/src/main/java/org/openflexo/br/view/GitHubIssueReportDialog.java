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

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Deflater;

import org.openflexo.ApplicationContext;
import org.openflexo.ApplicationVersion;
import org.openflexo.Flexo;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.task.Progress;
import org.openflexo.gina.swing.utils.JFIBDialog;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.swing.ImageUtils;
import org.openflexo.swing.ImageUtils.ImageType;
import org.openflexo.toolbox.FileUtils;
import org.openflexo.toolbox.PropertyChangedSupportDefaultImplementation;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.toolbox.ToolBox;
import org.openflexo.toolbox.ZipUtils;
import org.openflexo.view.FlexoDialog;
import org.openflexo.view.FlexoFrame;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.ws.github.GitHubClient;
import org.openflexo.ws.github.GitHubException;
import org.openflexo.ws.github.UnauthorizedGitHubAccessException;
import org.openflexo.ws.github.model.GitHubIssue;
import org.openflexo.ws.github.model.GitHubMilestone;
import org.openflexo.ws.github.model.GitHubRepository;
import org.openflexo.ws.github.model.GitHubResult;

/**
 * Data model for the GitHub issue report dialog.
 * Replaces JIRAIssueReportDialog.
 *
 * The user fills in a title, description, selects the target repository and
 * optionally a milestone. Logs, screenshots, and other attachments are either
 * uploaded as Gists (text) or saved locally (binary).
 */
public class GitHubIssueReportDialog extends PropertyChangedSupportDefaultImplementation {

	// -----------------------------------------------------------------------
	// Inner class: submission result
	// -----------------------------------------------------------------------

	public static class SubmitIssueReport {

		private String issueLink;
		private final List<String> errors = new ArrayList<>();
		private final List<String> warnings = new ArrayList<>();

		public boolean hasErrors() {
			return !errors.isEmpty();
		}

		public boolean hasWarnings() {
			return !warnings.isEmpty();
		}

		public String getIssueLink() {
			return issueLink;
		}

		public void setIssueLink(String issueLink) {
			this.issueLink = issueLink;
		}

		public List<String> getErrors() {
			return errors;
		}

		public List<String> getWarnings() {
			return warnings;
		}

		public void addToErrors(String error) {
			errors.add(error);
		}

		public void addToWarning(String warning) {
			warnings.add(warning);
		}

		public String errorsToString() {
			return String.join("\n", errors);
		}

		public String warningsToString() {
			return String.join("\n", warnings);
		}

		public String issueLinkHyperlink() {
			return "<html><a href=\"" + issueLink + "\">" + issueLink + "</a></html>";
		}

		public void openIssueLink() {
			ToolBox.openURL(issueLink);
		}
	}

	// -----------------------------------------------------------------------
	// Inner class: async submission
	// -----------------------------------------------------------------------

	private class SubmitIssueToGitHub implements Runnable {

		private final SubmitIssueReport report;
		private final GitHubClient client;
		private Exception exception;

		protected SubmitIssueToGitHub(GitHubClient client, SubmitIssueReport report) {
			this.client = client;
			this.report = report;
		}

		public Exception getException() {
			return exception;
		}

		@Override
		public void run() {
			try {
				Progress.progress(getLocales().localizedForKey("creating_issue"));

				String body = buildIssueBody(client, report);
				issue.setBody(body);

				if (milestone != null) {
					issue.setMilestone(milestone.getNumber());
				}

				issue.setLabels(Collections.singletonList("bug"));

				GitHubResult result = client.createIssue(repository, issue);

				if (result == null || !result.isSuccess()) {
					report.addToErrors(getLocales().localizedForKey("could_not_send_bug_report"));
					return;
				}

				report.setIssueLink(result.getHtmlUrl());

			} catch (UnauthorizedGitHubAccessException e) {
				this.exception = e;
			} catch (UnknownHostException e) {
				logger.severe("Cannot connect to GitHub. Check internet connection.");
				this.exception = e;
			} catch (IOException | GitHubException e) {
				e.printStackTrace();
				this.exception = e;
			}
		}

		private String buildIssueBody(GitHubClient client, SubmitIssueReport report) {
			StringBuilder body = new StringBuilder();

			// Description
			if (StringUtils.isNotEmpty(issue.getDescription())) {
				body.append("## Description\n\n").append(issue.getDescription()).append("\n\n");
			}

			// Build info + system properties
			String buildInfo = "- Build: `" + ApplicationVersion.BUILD_ID + "`\n"
					+ "- Commit: `" + ApplicationVersion.COMMIT_ID + "`";
			if (sendSystemProperties) {
				buildInfo += "\n\n```\n" + ToolBox.getSystemProperties(true) + "\n```";
			}
			body.append("## Environment\n\n").append(buildInfo).append("\n\n");

			// Stack trace (embedded directly – always compact enough)
			if (StringUtils.isNotEmpty(issue.getStacktrace())) {
				body.append("## Stack Trace\n\n```\n").append(issue.getStacktrace()).append("\n```\n\n");
			}

			// Log file – upload as Gist
			if (sendLogs) {
				File logFile = Flexo.getErrLogFile();
				if (logFile != null && logFile.exists()) {
					Progress.progress(getLocales().localizedForKey("sending_logs"));
					try {
						String logContent = readFileWithTruncation(logFile, 512 * 1024); // 512 KB max
						String gistUrl = client.createGist("OpenFlexo error log", logFile.getName(), logContent);
						if (gistUrl != null) {
							body.append("## Log File\n\n").append(gistUrl).append("\n\n");
						}
					} catch (Exception e) {
						report.addToWarning(getLocales().localizedForKey("could_not_attach_file") + " " + logFile.getName()
								+ "\n\t" + e.getMessage());
					}
				}
			}

			// Attached file – upload as Gist if readable as text
			if (attachFile != null && attachFile.exists()) {
				Progress.progress(getLocales().localizedForKey("sending_file") + " " + attachFile.getName());
				try {
					String content = readFileWithTruncation(attachFile, 512 * 1024);
					String gistUrl = client.createGist("Attached: " + attachFile.getName(), attachFile.getName(), content);
					body.append("## Attached File\n\n").append(gistUrl != null ? gistUrl : attachFile.getAbsolutePath()).append("\n\n");
				} catch (Exception e) {
					body.append("## Attached File\n\nLocal path: ").append(attachFile.getAbsolutePath()).append("\n\n");
				}
			}

			// Screenshots – capture as PNG, save locally, list paths in body
			if (sendScreenshots) {
				List<String> captured = new ArrayList<>();
				for (Frame frame : Frame.getFrames()) {
					if (frame instanceof FlexoFrame && frame.isVisible()
							&& frame.getWidth() > 0 && frame.getHeight() > 0) {
						captured.addAll(captureWindow(frame, frame.getTitle(), report));
						for (Window w : frame.getOwnedWindows()) {
							if ((w instanceof FlexoDialog || w instanceof JFIBDialog) && w.isVisible()) {
								captured.addAll(captureWindow(w, ((Dialog) w).getTitle(), report));
							}
						}
					}
				}
				if (!captured.isEmpty()) {
					body.append("## Screenshots\n\nSaved locally:\n");
					for (String path : captured) {
						body.append("- `").append(path).append("`\n");
					}
					body.append("\n");
				}
			}

			// Project archive – zip locally, note path in body
			if (sendProject && flexoProject != null) {
				Progress.progress(getLocales().localizedForKey("compressing_project"));
				File projectDir = (File) flexoProject.getProjectDirectory();
				String dirName = projectDir.getName();
				String zipName = dirName.endsWith(".prj")
						? dirName.substring(0, dirName.length() - 4) + ".zip"
						: dirName + ".zip";
				File zipFile = new File(System.getProperty("java.io.tmpdir"), zipName);
				try {
					ZipUtils.makeZip(zipFile, projectDir, f -> !f.getName().endsWith("~"), Deflater.BEST_COMPRESSION);
					body.append("## Project Archive\n\nSaved locally: `").append(zipFile.getAbsolutePath()).append("`\n\n");
				} catch (IOException e) {
					report.addToWarning(getLocales().localizedForKey("could_not_zip_project") + " " + e.getMessage());
				}
			}

			return body.toString();
		}

		private List<String> captureWindow(Window window, String title, SubmitIssueReport report) {
			List<String> paths = new ArrayList<>();
			if (window.isVisible() && window.getWidth() > 0 && window.getHeight() > 0) {
				try {
					File file = new File(System.getProperty("java.io.tmpdir"),
							FileUtils.getValidFileName(title + ".png"));
					ImageUtils.saveImageToFile(ImageUtils.createImageFromComponent(window), file, ImageType.PNG);
					paths.add(file.getAbsolutePath());
				} catch (Exception e) {
					report.addToWarning(getLocales().localizedForKey("could_not_attach_screenshot") + " " + title
							+ "\n\t" + e.getMessage());
					logger.log(Level.SEVERE, "Error capturing screenshot: " + title, e);
				}
			}
			return paths;
		}
	}

	// -----------------------------------------------------------------------
	// State
	// -----------------------------------------------------------------------

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

	// -----------------------------------------------------------------------
	// Constructor
	// -----------------------------------------------------------------------

	public GitHubIssueReportDialog(Exception e, ApplicationContext serviceManager) {
		this.serviceManager = serviceManager;
		this.issue = new GitHubIssue();

		issue.getPropertyChangeSupport().addPropertyChangeListener(
				evt -> getPropertyChangeSupport().firePropertyChange("isValid", !isValid(), isValid()));

		sendLogs = true;
		sendScreenshots = false;
		sendSystemProperties = false;
		sendProject = false;

		if (e != null) {
			issue.setStacktrace(e.getClass().getName() + ": " + e.getMessage() + "\n" + ToolBox.getStackTraceAsString(e));
		}
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
			// Lazy-load milestones for the selected repository
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
		return issue != null
				&& repository != null
				&& StringUtils.isNotEmpty(issue.getTitle())
				&& StringUtils.isNotEmpty(issue.getDescription());
	}

	// -----------------------------------------------------------------------
	// Submission
	// -----------------------------------------------------------------------

	public boolean send() throws Exception {
		String token = serviceManager.getBugReportPreferences().getGithubToken();
		GitHubClient client = new GitHubClient(token);
		SubmitIssueReport report = new SubmitIssueReport();
		SubmitIssueToGitHub submitter = new SubmitIssueToGitHub(client, report);

		boolean retry = true;
		while (retry) {
			submitter.run();
			if (submitter.getException() != null) {
				if (submitter.getException() instanceof SocketTimeoutException) {
					retry = FlexoController.confirm(
							getLocales().localizedForKey("could_not_send_incident_so_far_keep_trying") + "? ");
					if (retry) {
						client.setTimeout(client.getTimeout() * 2);
					}
				} else if (submitter.getException() instanceof UnknownHostException) {
					retry = FlexoController.confirm(
							getLocales().localizedForKey("could_not_send_to_host_check_internet_connection_and_try_again") + "? ");
					if (!retry) {
						throw submitter.getException();
					}
				} else {
					throw submitter.getException();
				}
			} else {
				retry = false;
			}
		}

		Progress.hideTaskBar();
		JFIBDialog.instanciateAndShowDialog(REPORT_FIB_FILE, report,
				serviceManager.getApplicationFIBLibraryService().getApplicationFIBLibrary(),
				FlexoFrame.getActiveFrame(), true, FlexoLocalization.getMainLocalizer());
		return !report.hasErrors();
	}

	// -----------------------------------------------------------------------
	// Utilities
	// -----------------------------------------------------------------------

	/**
	 * Reads a text file, truncating from the beginning if it exceeds maxBytes,
	 * so that we always include the most recent content (tail).
	 */
	private static String readFileWithTruncation(File file, int maxBytes) throws IOException {
		long length = file.length();
		try (FileInputStream fis = new FileInputStream(file)) {
			byte[] buf;
			if (length <= maxBytes) {
				buf = new byte[(int) length];
				fis.read(buf);
			} else {
				// Skip the beginning, keep the last maxBytes
				long skip = length - maxBytes;
				fis.skip(skip);
				buf = new byte[maxBytes];
				fis.read(buf);
			}
			return new String(buf, "UTF-8");
		}
	}
}
