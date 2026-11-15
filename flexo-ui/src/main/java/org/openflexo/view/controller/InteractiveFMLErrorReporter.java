/**
 * 
 * Copyright (c) 2026, Openflexo
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

package org.openflexo.view.controller;

import java.awt.GraphicsEnvironment;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.openflexo.br.ui.SendBugReportServiceTask;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.fml.rt.error.FMLErrorReporter;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.module.FlexoModule;

/**
 * The reporter of an {@link InteractiveFlexoEditor}: on top of the console record, it presents the failure of an outermost action to the
 * user (CORE-F-11):
 * <ul>
 * <li>a foreseen failure (an FML error) in the {@link FMLErrorDialog};</li>
 * <li>a defect of the platform with the bug report (the confirmation, then the issue dialog);</li>
 * <li>nothing more when the action was started by an expression (a binding of the user interface, a command) or an event: the console
 * holds it (a defect with its Java stack in the log), and the expression receives it;</li>
 * <li>a cancellation with nothing.</li>
 * </ul>
 * The presentation runs later on the event dispatch thread, once the failed action has left its execution scope.
 */
public class InteractiveFMLErrorReporter extends FMLErrorReporter {

	private static final Logger logger = FlexoLogger.getLogger(InteractiveFMLErrorReporter.class.getPackage().getName());

	public InteractiveFMLErrorReporter(InteractiveFlexoEditor editor) {
		super(editor);
	}

	@Override
	public InteractiveFlexoEditor getEditor() {
		return (InteractiveFlexoEditor) super.getEditor();
	}

	/** How the failure of an outermost action is presented to the user, on top of the console record */
	public enum Presentation {
		/** The console record only */
		NONE,
		/** The FML error dialog */
		FML_ERROR_DIALOG,
		/** The bug report */
		BUG_REPORT
	}

	/** How the failure of supplied outermost action is presented */
	public static Presentation presentationFor(FlexoAction<?, ?, ?> action, Throwable failure) {
		if (failure == null || isCancellation(failure)) {
			return Presentation.NONE;
		}
		if (action != null && action.isStartedFromExpression()) {
			// The console holds it, a defect with its Java stack in the log: a binding may be evaluated again and again
			return Presentation.NONE;
		}
		return isForeseen(failure) ? Presentation.FML_ERROR_DIALOG : Presentation.BUG_REPORT;
	}

	@Override
	protected void present(FlexoAction<?, ?, ?> action, Throwable failure) {
		switch (presentationFor(action, failure)) {
			case BUG_REPORT:
				onEventDispatchThread(() -> proposeBugReport(failure));
				break;
			case FML_ERROR_DIALOG:
				onEventDispatchThread(() -> showFMLError(action, failure));
				break;
			default:
				break;
		}
	}

	private static void onEventDispatchThread(Runnable runnable) {
		if (GraphicsEnvironment.isHeadless()) {
			return;
		}
		// Always later: the failed action has then left its execution scope, so that an action started from the dialog (open the FML
		// editor) or while it is shown (a binding, a listener) is an outermost one, reported on its own
		SwingUtilities.invokeLater(runnable);
	}

	/** Show the FML error dialog for the foreseen failure of supplied action */
	protected void showFMLError(FlexoAction<?, ?, ?> action, Throwable failure) {
		new FMLErrorDialog(getEditor(), action, failure).showDialog();
	}

	private boolean isProposingBugReport = false;

	/** Propose to send a bug report for supplied defect of the platform, as the uncaught exception handler of the application does */
	protected void proposeBugReport(Throwable failure) {
		if (!(failure instanceof Exception) || isProposingBugReport) {
			return;
		}
		isProposingBugReport = true;
		try {
			String message = FlexoLocalization.getMainLocalizer().localizedForKey("unexpected_exception_occured") + " "
					+ FlexoLocalization.getMainLocalizer().localizedForKey("would_you_like_to_send_a_report");
			if (FlexoController.confirm(message)) {
				FlexoModule<?> module = getEditor().getModuleLoader() != null ? getEditor().getModuleLoader().getActiveModule() : null;
				SendBugReportServiceTask sendBugReport = new SendBugReportServiceTask((Exception) failure, module,
						getEditor().getProject(), getEditor().getApplicationContext());
				getEditor().getApplicationContext().getTaskManager().scheduleExecution(sendBugReport);
			}
		} catch (RuntimeException e) {
			// The bug report must never hide the failure, which the console and the log already hold
			logger.log(Level.WARNING, "Could not propose a bug report", e);
		} finally {
			isProposingBugReport = false;
		}
	}
}
