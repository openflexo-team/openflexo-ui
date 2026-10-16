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

import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.openflexo.ApplicationContext;
import org.openflexo.br.BugReportServiceImpl;
import org.openflexo.br.BugReportSubmission;
import org.openflexo.br.SubmitIssueReport;
import org.openflexo.br.github.GitHubClient;
import org.openflexo.br.github.UnauthorizedGitHubAccessException;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.task.Progress;
import org.openflexo.gina.controller.FIBController.Status;
import org.openflexo.gina.swing.utils.JFIBDialog;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.module.FlexoModule;
import org.openflexo.task.FlexoApplicationTask;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.view.FlexoFrame;
import org.openflexo.view.controller.FlexoController;

/**
 * Task that opens the bug report form and, once validated, delegates submission to
 * {@link BugReportServiceImpl#submitIssue(BugReportSubmission)}.
 * Handles user-facing retry logic for network timeouts and token re-prompting.
 */
public class SendBugReportServiceTask extends FlexoApplicationTask {

	private final BugReportServiceImpl bugReportService;

	private final Exception causeException;
	private final FlexoProject<?> project;
	private final FlexoModule<?> module;

	private GitHubIssueReportDialog report;
	private JFIBDialog<GitHubIssueReportDialog> dialog;

	public SendBugReportServiceTask(Exception e, FlexoModule<?> module, FlexoProject<?> project, ApplicationContext applicationContext) {
		super("SendBugReport", FlexoLocalization.getMainLocalizer().localizedForKey("send_issue"), applicationContext);
		this.bugReportService = applicationContext.getBugReportService();
		this.module = module;
		this.project = project;
		this.causeException = e;
		openDialog();
	}

	private void openDialog() {
		ApplicationContext serviceManager = (ApplicationContext) getServiceManager();

		try {
			report = new GitHubIssueReportDialog(causeException, serviceManager);

			if (module != null) {
				report.setRepository(serviceManager.getBugReportService().getMostProbableRepository(causeException, module));
			}

			report.setFlexoProject(project);
			report.setServiceManager(serviceManager);

			dialog = JFIBDialog.instanciateAndShowDialog(GitHubIssueReportDialog.FIB_FILE, report,
					serviceManager.getApplicationFIBLibraryService().getApplicationFIBLibrary(), FlexoFrame.getActiveFrame(), true,
					FlexoLocalization.getMainLocalizer());

		} catch (Exception e1) {
			e1.printStackTrace();
			FlexoController
					.showError(serviceManager.getLocalizationService().getFlexoLocalizer().localizedForKey("cannot_open_bug_report_dialog")
							+ ": " + e1.getMessage());
		}
	}

	@Override
	public void performTask() {
		Progress.setExpectedProgressSteps(10);

		ApplicationContext serviceManager = (ApplicationContext) getServiceManager();

		while (true) {
			if (dialog == null || dialog.getStatus() != Status.VALIDATED) {
				break;
			}

			while (StringUtils.isEmpty(serviceManager.getBugReportPreferences().getGithubToken())) {
				if (!GitHubTokenDialog.askToken(serviceManager)) {
					return;
				}
			}

			BugReportSubmission submission = dialog.getData().toBugReportSubmission();
			SubmitIssueReport result = null;

			try {
				Progress.progress("sending...");
				result = bugReportService.submitIssue(submission);

			} catch (SocketTimeoutException e) {
				boolean retry = FlexoController
						.confirm(serviceManager.getLocalizationService().getFlexoLocalizer()
								.localizedForKey("could_not_send_incident_so_far_keep_trying") + "? ");
				if (retry) {
					continue;
				}
				break;

			} catch (UnknownHostException e) {
				boolean retry = FlexoController.confirm(serviceManager.getLocalizationService().getFlexoLocalizer()
						.localizedForKey("could_not_send_to_host_check_internet_connection_and_try_again") + "? ");
				if (!retry) {
					break;
				}
				continue;

			} catch (UnauthorizedGitHubAccessException e) {
				Progress.progress("ask_token");
				if (GitHubTokenDialog.askToken(serviceManager)) {
					continue;
				}
				break;

			} catch (MalformedURLException e) {
				FlexoController.showError(
						serviceManager.getLocalizationService().getFlexoLocalizer().localizedForKey("could_not_send_bug_report") + " "
								+ e.getMessage());
				break;

			} catch (Exception e) {
				e.printStackTrace();
				FlexoController.showError(
						serviceManager.getLocalizationService().getFlexoLocalizer().localizedForKey("could_not_send_bug_report") + ":\n"
								+ e.getMessage());
				break;
			}

			// Show result dialog
			Progress.hideTaskBar();
			JFIBDialog.instanciateAndShowDialog(GitHubIssueReportDialog.REPORT_FIB_FILE, result,
					serviceManager.getApplicationFIBLibraryService().getApplicationFIBLibrary(), FlexoFrame.getActiveFrame(), true,
					FlexoLocalization.getMainLocalizer());

			if (!result.hasErrors()) {
				break;
			}
			dialog.setVisible(true);
		}
	}

	public BugReportServiceImpl getBugReportService() {
		return bugReportService;
	}

	@Override
	public boolean isCancellable() {
		return true;
	}

	@Override
	protected synchronized void finishedExecution() {
		super.finishedExecution();
	}
}
