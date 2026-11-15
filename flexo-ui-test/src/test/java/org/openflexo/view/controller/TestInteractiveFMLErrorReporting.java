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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.fml.rt.error.FMLCancellation;
import org.openflexo.foundation.fml.rt.error.FMLErrorReporter;
import org.openflexo.foundation.fml.rt.error.FMLInternalError;
import org.openflexo.foundation.fml.rt.error.FMLTypeError;
import org.openflexo.gina.test.TestApplicationContext;
import org.openflexo.pamela.undo.AtomicEdit;
import org.openflexo.test.UITest;
import org.openflexo.view.controller.InteractiveFMLErrorReporter.Presentation;

/**
 * How the interactive editor reports the failure of an action (CORE-F-11, phase 4): its edits are undone before the report, the console
 * holds one record, the failure is presented once (the FML error dialog, the bug report, nothing for a cancellation or an action started
 * by an expression), and it is marked reported, so that a GINA binding or the uncaught exception handler does not report it again. The
 * dialogs themselves are not shown: the presentation is recorded. A UI test: the application context needs a display.
 */
@Category(UITest.class)
public class TestInteractiveFMLErrorReporting {

	private static TestApplicationContext applicationContext;

	/** An action whose body is given by the test */
	private static class TestAction extends FlexoAction<TestAction, FlexoObject, FlexoObject> {

		interface Body {
			void run() throws FlexoException;
		}

		private final Body body;

		TestAction(FlexoEditor editor, Body body) {
			super(null, null, null, editor);
			this.body = body;
		}

		@Override
		protected void doAction(Object context) throws FlexoException {
			body.run();
		}
	}

	/** An edit recorded by the undo manager, which tells whether it was undone */
	private static class TestEdit extends AtomicEdit<Object> {
		boolean undone = false;

		TestEdit() {
			super(null, null);
		}

		@Override
		public Object getObject() {
			return null;
		}

		@Override
		public String getType() {
			return "test";
		}

		@Override
		public void undo() {
			undone = true;
		}

		@Override
		public boolean canUndo() {
			return true;
		}

		@Override
		public void redo() {
			undone = false;
		}

		@Override
		public boolean canRedo() {
			return true;
		}

		@Override
		public boolean isSignificant() {
			return true;
		}

		@Override
		public String getPresentationName() {
			return "test edit";
		}
	}

	private InteractiveFlexoEditor editor;
	private final List<Presentation> presented = new ArrayList<>();

	@BeforeClass
	public static void setUpClass() {
		applicationContext = new TestApplicationContext();
	}

	@AfterClass
	public static void tearDownClass() {
		applicationContext.stopAllServices();
		applicationContext = null;
	}

	@Before
	public void setUp() {
		presented.clear();
		editor = new InteractiveFlexoEditor(applicationContext, null) {
			@Override
			protected FMLErrorReporter makeFMLErrorReporter() {
				return new InteractiveFMLErrorReporter(this) {
					@Override
					protected void present(FlexoAction<?, ?, ?> action, Throwable failure) {
						presented.add(presentationFor(action, failure));
					}
				};
			}
		};
	}

	private int severe() {
		return editor.getFMLConsole().getSevereLogs();
	}

	@Test
	public void aForeseenFailureIsUndoneThenPresentedOnce() {
		TestEdit edit = new TestEdit();
		FMLTypeError failure = new FMLTypeError("foreseen");
		TestAction action = new TestAction(editor, () -> {
			editor.getUndoManager().addEdit(edit);
			throw failure;
		});
		action.doAction();
		assertTrue(action.hasActionExecutionFailed());
		assertSame(failure, action.getFailure());
		assertTrue("The edits of a failing action are undone", edit.undone);
		assertEquals(1, severe());
		assertEquals(List.of(Presentation.FML_ERROR_DIALOG), presented);
		assertTrue(FMLErrorReporter.isReported(failure));
		assertFalse("The recording is closed", editor.getUndoManager().isBeeingRecording());
	}

	@Test
	public void anUnforeseenFailureIsUndonePresentedAsABugAndRethrown() {
		TestEdit edit = new TestEdit();
		FMLInternalError failure = new FMLInternalError(new IllegalStateException("platform defect"));
		TestAction action = new TestAction(editor, () -> {
			editor.getUndoManager().addEdit(edit);
			throw failure;
		});
		try {
			action.doAction();
			fail("An unforeseen failure must not be swallowed");
		} catch (FMLInternalError e) {
			assertSame(failure, e);
		}
		assertTrue(edit.undone);
		assertEquals(1, severe());
		assertEquals(List.of(Presentation.BUG_REPORT), presented);
		assertTrue("The uncaught exception handler must not report it again", FMLErrorReporter.isReported(failure));
		assertFalse(editor.getUndoManager().isBeeingRecording());
	}

	@Test
	public void anActionStartedFromAnExpressionGoesToTheConsoleOnly() {
		TestAction action = new TestAction(editor, () -> {
			throw new FMLTypeError("from a GINA button");
		});
		action.setStartedFromExpression(true);
		action.doAction();
		assertEquals(1, severe());
		assertEquals(List.of(Presentation.NONE), presented);
		// The binding that started it receives the failure, and does not report it again
		editor.getFMLErrorReporter().reportToConsole("Failure in the user interface", action.getFailure());
		assertEquals(1, severe());
	}

	@Test
	public void aCancellationIsUndoneAndNotReported() {
		TestEdit edit = new TestEdit();
		TestAction action = new TestAction(editor, () -> {
			editor.getUndoManager().addEdit(edit);
			throw new FMLCancellation("cancelled");
		});
		action.doAction();
		assertTrue(action.hasBeenCancelled());
		assertTrue(edit.undone);
		assertEquals(0, severe());
		assertTrue(presented.isEmpty());
	}

	@Test
	public void aNestedFailureIsLeftToItsCaller() {
		FMLTypeError nested = new FMLTypeError("nested");
		TestAction outer = new TestAction(editor, () -> {
			TestAction inner = new TestAction(editor, () -> {
				throw nested;
			});
			inner.doAction();
			assertTrue(inner.hasActionExecutionFailed());
			throw inner.getThrownException();
		});
		outer.doAction();
		assertEquals(1, severe());
		assertEquals(List.of(Presentation.FML_ERROR_DIALOG), presented);
	}

	@Test
	public void presentationDecision() {
		TestAction action = new TestAction(editor, () -> {
		});
		assertEquals(Presentation.FML_ERROR_DIALOG, InteractiveFMLErrorReporter.presentationFor(action, new FMLTypeError("x")));
		assertEquals(Presentation.BUG_REPORT, InteractiveFMLErrorReporter.presentationFor(action, new NullPointerException()));
		assertEquals(Presentation.NONE, InteractiveFMLErrorReporter.presentationFor(action, new FMLCancellation("x")));
		action.setStartedFromExpression(true);
		assertEquals(Presentation.NONE, InteractiveFMLErrorReporter.presentationFor(action, new FMLTypeError("x")));
		// Started by an expression, a defect of the platform goes to the console and the log only: a binding may be evaluated again
		assertEquals(Presentation.NONE, InteractiveFMLErrorReporter.presentationFor(action, new NullPointerException()));
	}
}
