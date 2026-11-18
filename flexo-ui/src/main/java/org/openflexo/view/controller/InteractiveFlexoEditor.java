/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
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

import java.util.EventObject;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.openflexo.ApplicationContext;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.OpenflexoRuntimeException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoAction.ExecutionStatus;
import org.openflexo.foundation.action.FlexoActionEnableCondition;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.action.FlexoActionRunnable;
import org.openflexo.foundation.action.FlexoActionVisibleCondition;
import org.openflexo.foundation.action.FlexoExceptionHandler;
import org.openflexo.foundation.action.FlexoUndoManager;
import org.openflexo.foundation.action.LongRunningAction;
import org.openflexo.foundation.fml.rt.action.ActionSchemeActionFactory;
import org.openflexo.foundation.fml.rt.action.DeletionSchemeActionFactory;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.foundation.fml.rt.error.FMLCancellation;
import org.openflexo.foundation.fml.rt.error.FMLErrorReporter;
import org.openflexo.foundation.fml.rt.error.FMLErrorReporter.ExecutionScope;
import org.openflexo.foundation.nature.FlexoNature;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.ResourceUpdateHandler;
import org.openflexo.foundation.task.LongRunningActionTask;
import org.openflexo.foundation.task.Progress;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.module.FlexoModule;
import org.openflexo.module.ModuleLoader;

/**
 * Interactive implementation of a {@link FlexoEditor}
 * 
 * @author sylvain
 *
 */
public class InteractiveFlexoEditor extends DefaultFlexoEditor {

	private static final Logger logger = FlexoLogger.getLogger(InteractiveFlexoEditor.class.getPackage().getName());

	private ScenarioRecorder _scenarioRecorder;

	private final ApplicationContext applicationContext;

	private Map<FlexoModule<?>, ControllerActionInitializer> actionInitializers;

	public InteractiveFlexoEditor(ApplicationContext applicationContext, FlexoProject<?> project) {
		super(project, applicationContext);
		this.applicationContext = applicationContext;
		actionInitializers = new Hashtable<>();
		if (ScenarioRecorder.ENABLE) {
			_scenarioRecorder = new ScenarioRecorder();
		}
	}

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public ModuleLoader getModuleLoader() {
		return applicationContext.getModuleLoader();
	}

	@Override
	public ResourceUpdateHandler getResourceUpdateHandler() {
		return new ResourceUpdateHandler() {
			@Override
			public void resourceChanged(FlexoResource<?> resource) {
				// TODO to be implemented !
				logger.warning("Please implement resource update handler");
			}
		};
	}

	@Override
	public boolean isInteractive() {
		return true;
	}

	@Override
	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> A performAction(final A action,
			final EventObject e) {
		// NPE Protection
		if (action != null) {
			FlexoActionFactory<A, T1, T2> at = action.getActionFactory();
			if (at != null) {
				// A condition that fails is not taken as disabled: the execution raises its failure and sets the status of the action
				// (CORE-F-11)
				boolean enabled;
				try {
					enabled = at.isEnabledForExecution(action.getFocusedObject(), action.getGlobalSelection());
				} catch (OpenflexoRuntimeException | RuntimeException failure) {
					enabled = true;
				}
				if (!enabled) {
					return null;
				}
				executeAction(action, e);
				return action;
			}
			else { // No factory supplied, execute action inconditionnaly
				executeAction(action, e);
				return action;
			}
		}
		else {
			logger.warning("Action was NULL!");
			return null;
		}
	}

	/**
	 * This is the CORE execution method
	 * 
	 * @param action
	 * @param event
	 * @return
	 */
	private <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> A executeAction(final A action,
			final EventObject event) {
		// We do it sooner to embed eventual initializer execution in the record session of the undo manager
		if (!action.isEmbedded()) {
			actionWillBePerformed(action);
		}

		// If action is embedded and valid, we skip initializer
		boolean confirmDoAction = (action.isEmbedded() && action.isValid() && !action.getForceExecuteConfirmationPanel()) ? true
				: runInitializer(action, event);
		if (!confirmDoAction) {
			if (action instanceof FlexoBehaviourAction) {
				// The user cancelled the behaviour (its parameters dialog): a calling action is cancelled too, and the edits of the
				// initializer are undone (CORE-F-11)
				action.cancelExecution(new FMLCancellation("Cancelled by the user: " + action.getLocalizedName()));
				if (!action.isEmbedded()) {
					actionHasBeenPerformed(action, false);
				}
			}
			else if (!action.isEmbedded()) {
				// Another action not executed: its recording is closed, the edits of its initializer kept
				getUndoManager().actionHasNotBeenPerformed(action);
			}
			return action;
		}
		if (action instanceof LongRunningAction && (!action.isEmbedded()) && SwingUtilities.isEventDispatchThread()) {
			LongRunningActionTask task = new LongRunningActionTask((LongRunningAction) action) {
				@Override
				public void performTask() throws InterruptedException {
					Progress.setExpectedProgressSteps(((LongRunningAction) action).getExpectedProgressSteps());
					try {
						doExecuteAction(action, event);
					} catch (RuntimeException e) {
						// Already reported by doExecuteAction, and nobody waits for the outcome of a task
						if (!FMLErrorReporter.isReported(e)) {
							throw e;
						}
					}
				}
			};
			applicationContext.getTaskManager().scheduleExecution(task);
		}
		else {
			// Do it now, in this thread
			doExecuteAction(action, event);
		}

		return action;
	}

	/**
	 * Execute supplied action, whose initializer accepted it. When it fails, its edits are undone, then its failure goes to the exception
	 * handler of its initializer, if any, otherwise it is reported once, by the outermost action run on this thread (CORE-F-11); a nested
	 * action leaves it to its caller. An unforeseen failure is rethrown once reported. The finalizer runs once the action has left its
	 * execution scope, so that the actions it starts are outermost ones.
	 */
	private <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> void doExecuteAction(final A action,
			final EventObject event) {
		ExecutionScope scope = FMLErrorReporter.enterAction();
		boolean outermost = (scope != null);
		boolean succeeded = false;
		boolean handled = false;
		try {
			runAction(action);
			succeeded = true;
		} catch (OpenflexoRuntimeException exception) {
			if (!action.isEmbedded()) {
				actionHasBeenPerformed(action, false); // Action failed: its edits are undone before reporting
			}
			if (runExceptionHandler(exception, action)) {
				// The exception has been handled, we may still have to execute finalizer, if any
				handled = true;
			}
			else if (!outermost) {
				// A nested action leaves its failure to its caller
				FMLErrorReporter.currentScope().nestedActionFailed(action);
			}
			else {
				getFMLErrorReporter().report(action, exception);
			}
		} catch (RuntimeException | Error exception) {
			// Unforeseen failure: a defect of the platform, never swallowed
			if (!action.isEmbedded()) {
				actionHasBeenPerformed(action, false);
			}
			if (outermost) {
				getFMLErrorReporter().report(action, exception);
			}
			throw exception;
		} finally {
			if (outermost) {
				FMLErrorReporter.exitOutermostAction();
				List<FlexoAction<?, ?, ?>> unreported = FMLErrorReporter.unreportedNestedFailures(scope, action);
				if (!unreported.isEmpty()) {
					getFMLErrorReporter().reportIgnoredFailures(unreported);
				}
			}
		}
		if ((succeeded || handled) && !action.isEmbedded()) {
			runFinalizer(action, event);
		}

		// We do it later to embed eventual finalizer execution in the record session of the undo manager
		if (succeeded && !action.isEmbedded()) {
			actionHasBeenPerformed(action, true); // Action succeeded
		}
	}

	private <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> boolean runInitializer(A action,
			EventObject event) {
		ActionInitializer<A, T1, T2> actionInitializer = getActionInitializer(action);
		if (actionInitializer != null) {
			FlexoActionRunnable<A, T1, T2> initializer = actionInitializer.getDefaultInitializer();
			if (initializer != null) {
				return initializer.run(event, action);
			}
		}
		return true;
	}

	private <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> void runAction(final A action)
			throws OpenflexoRuntimeException {
		action.doActionInContext();
	}

	private <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> void runFinalizer(final A action,
			EventObject event) {
		ActionInitializer<A, ?, ?> actionInitializer = getActionInitializer(action);
		if (actionInitializer != null) {
			FlexoActionRunnable<A, ?, ?> finalizer = actionInitializer.getDefaultFinalizer();
			if (finalizer != null) {
				finalizer.run(event, action);
			}
		}
	}

	/**
	 * Give the failure of supplied action to the exception handler of its initializer, if any
	 * 
	 * @return true when the handler handled it: nothing else reports it
	 */
	private <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> boolean runExceptionHandler(
			OpenflexoRuntimeException exception, final A action) {
		ActionInitializer<A, ?, ?> actionInitializer = getActionInitializer(action);
		FlexoExceptionHandler<A, ?, ?> exceptionHandler = actionInitializer != null ? actionInitializer.getDefaultExceptionHandler() : null;
		return exceptionHandler != null && exceptionHandler.handleException(exception, action);
	}

	/**
	 * The reporter of this editor presents the failures to the user: a dialog for a foreseen failure, the bug report for a defect of
	 * the platform (CORE-F-11)
	 */
	@Override
	protected FMLErrorReporter makeFMLErrorReporter() {
		return new InteractiveFMLErrorReporter(this);
	}

	@Override
	public FlexoUndoManager getUndoManager() {
		return applicationContext.getEditingContext().getUndoManager();
	}

	private <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> void actionWillBePerformed(A action) {
		getUndoManager().actionWillBePerformed(action);
	}

	private <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> void actionHasBeenPerformed(A action,
			boolean success) {
		getUndoManager().actionHasBeenPerformed(action, success);
		if (success) {
			if (_scenarioRecorder != null) {
				if (!action.isEmbedded() || action.getOwnerAction().getExecutionStatus() != ExecutionStatus.EXECUTING_CORE) {
					_scenarioRecorder.registerDoneAction(action);
				}
			}
		}
	}

	@Override
	public boolean performResourceScanning() {
		return true;
	}

	public boolean isTestEditor() {
		return false;
	}

	/**
	 * Focus on supplied object, trying to display a view adapted to supplied displayNature
	 * 
	 * @param object
	 * @param displayNature
	 */
	@Override
	public <O extends FlexoObject> void focusOn(O object, FlexoNature<O> displayNature) {
		// Only interactive editor handle this

		if (displayNature != null) {
			getModuleLoader().getActiveModule().getFlexoController().switchToPerspective(displayNature);
		}
		getModuleLoader().getActiveModule().getFlexoController().setCurrentEditedObject(object);
		getModuleLoader().getActiveModule().getFlexoController().getSelectionManager().setSelectedObject(object);
	}

	public void registerControllerActionInitializer(ControllerActionInitializer controllerActionInitializer) {
		actionInitializers.put(controllerActionInitializer.getModule(), controllerActionInitializer);
	}

	public void unregisterControllerActionInitializer(ControllerActionInitializer controllerActionInitializer) {
		actionInitializers.remove(controllerActionInitializer.getModule());
	}

	private ControllerActionInitializer getCurrentControllerActionInitializer() {
		if (getModuleLoader() != null && getModuleLoader().getActiveModule() != null) {
			return actionInitializers.get(getModuleLoader().getActiveModule());
		}
		return null;
	}

	private <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> ActionInitializer<A, T1, T2> getActionInitializer(
			FlexoActionFactory<A, T1, T2> actionFactory) {
		ControllerActionInitializer currentControllerActionInitializer = getCurrentControllerActionInitializer();
		if (currentControllerActionInitializer != null) {
			return currentControllerActionInitializer.getActionInitializer(actionFactory);
		}
		return null;
	}

	private <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> ActionInitializer<A, T1, T2> getActionInitializer(
			A action) {
		ControllerActionInitializer currentControllerActionInitializer = getCurrentControllerActionInitializer();
		if (currentControllerActionInitializer != null) {
			return currentControllerActionInitializer.getActionInitializer(action);
		}
		return null;
	}

	@Override
	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> boolean isActionEnabled(
			FlexoActionFactory<A, T1, T2> actionFactory, T1 focusedObject, Vector<T2> globalSelection) {
		if (actionFactory instanceof ActionSchemeActionFactory) {
			// The behaviour tells itself whether it is enabled on this instance (@Enable)
			return actionFactory.isEnabled(focusedObject, globalSelection);
		}
		if (actionFactory instanceof DeletionSchemeActionFactory) {
			return true;
		}
		if (actionFactory.isEnabled(focusedObject, globalSelection)) {
			ActionInitializer<A, T1, T2> actionInitializer = getActionInitializer(actionFactory);
			if (actionInitializer != null) {
				FlexoActionEnableCondition<A, T1, T2> condition = actionInitializer.getEnableCondition();
				if (condition != null) {
					return condition.isEnabled(actionFactory, focusedObject, globalSelection, this);
				}
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
		return true;
	}

	@Override
	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> boolean isActionVisible(
			FlexoActionFactory<A, T1, T2> actionFactory, T1 focusedObject, Vector<T2> globalSelection) {
		try {
			if (actionFactory.isVisibleForSelection(focusedObject, globalSelection)) {
				ActionInitializer<A, T1, T2> actionInitializer = getActionInitializer(actionFactory);
				if (actionInitializer != null) {
					FlexoActionVisibleCondition<A, T1, T2> condition = actionInitializer.getVisibleCondition();
					if (condition != null) {
						return condition.isVisible(actionFactory, focusedObject, globalSelection, this);
					}
				}
				else {
					return false;
				}
			}
			else {
				return false;
			}
			return true;
		} catch (ClassCastException e) {
			// Might happen in some circonstances, when trying to lookup focused object and global selection
			return false;
		}
	}

	@Override
	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> Icon getEnabledIconFor(
			FlexoActionFactory<A, T1, T2> actionFactory) {
		ActionInitializer<A, T1, T2> actionInitializer = getActionInitializer(actionFactory);
		if (actionInitializer != null) {
			return actionInitializer.getEnabledIcon(actionFactory);
		}
		if (actionFactory instanceof DeletionSchemeActionFactory) {
			return FlexoController.statelessIconForObject(((DeletionSchemeActionFactory) actionFactory).getDeletionScheme());
		}
		else if (actionFactory instanceof ActionSchemeActionFactory) {
			return FlexoController.statelessIconForObject(((ActionSchemeActionFactory) actionFactory).getActionScheme());
		}

		return null;
	}

	@Override
	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> Icon getDisabledIconFor(
			FlexoActionFactory<A, T1, T2> actionFactory) {
		ActionInitializer<A, T1, T2> actionInitializer = getActionInitializer(actionFactory);
		if (actionInitializer != null) {
			return actionInitializer.getDisabledIcon(actionFactory);
		}
		return null;
	}

	@Override
	public KeyStroke getKeyStrokeFor(FlexoActionFactory<?, ?, ?> actionFactory) {
		ActionInitializer<?, ?, ?> actionInitializer = getActionInitializer(actionFactory);
		if (actionInitializer != null) {
			return actionInitializer.getShortcut();
		}
		return null;
	}

}
