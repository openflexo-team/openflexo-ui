/**
 * 
 * Copyright (c) 2020, Openflexo
 * 
 * This file is part of Fml-technologyadapter-ui, a component of the software infrastructure 
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
package org.openflexo.fml.controller.widget.fmleditor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.text.BadLocationException;

import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.parser.AbstractParser;
import org.fife.ui.rsyntaxtextarea.parser.ParseResult;
import org.fife.ui.rsyntaxtextarea.parser.ParserNotice;
import org.openflexo.fml.controller.FMLTechnologyAdapterController;
import org.openflexo.foundation.fml.ElementImportDeclaration;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLValidationReport;
import org.openflexo.foundation.fml.SemanticAnalysisIssue;
import org.openflexo.foundation.fml.SemanticAnalysisWarning;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitParser;
import org.openflexo.foundation.fml.parser.ParseException;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rm.TextUpdate;
import org.openflexo.pamela.validation.ValidationIssue;

/**
 * An {@link AbstractParser} implementation for FML language
 * 
 * @author sylvain
 *
 */
public class FMLEditorParser extends AbstractParser {

	static final Logger logger = Logger.getLogger(FMLEditor.class.getPackage().getName());

	/**
	 * The property change event that's fired when the document is re-parsed. Applications can listen for this property change and update
	 * themselves accordingly.
	 */
	public static final String PROPERTY_COMPILATION_UNIT = "CompilationUnit";

	private final FMLEditor editor;
	private final FMLCompilationUnitParser fmlParser;
	private FMLParseResult result;

	private boolean isInitialized = false;

	private PropertyChangeSupport pcSupport;

	public FMLEditorParser(FMLEditor editor, FMLCompilationUnit compilationUnit) {
		pcSupport = new PropertyChangeSupport(this);
		this.editor = editor;
		fmlParser = new FMLCompilationUnitParser();
		result = new FMLParseResult(this);
		if (compilationUnit != null) {
			// The report computed when the unit was loaded (in the background) is up to date: validating it again here took the event
			// dispatch thread for about a second at each opening
			FMLValidationReport validationReport = validate(compilationUnit, false);
			result.setValidationReport(validationReport);
		}
		isInitialized = true;
	}

	/**
	 * When true, the next parsing of a text equal to the one the compilation unit was loaded from is skipped (see
	 * {@link #skipParsingOfLoadedText()})
	 */
	private boolean skipParsingOfLoadedText = false;

	/**
	 * Skip the parsing that the editor triggers when it is filled with the text its compilation unit was loaded from.<br>
	 * The unit is already in sync with this text and its validation notices are known: parsing it again, merging the result and
	 * validating it took about 1.5 s of the event dispatch thread at each opening. The parsing still happens (the text area asks for it),
	 * but returns the current result at once, so that the text area displays its notices.
	 */
	public void skipParsingOfLoadedText() {
		skipParsingOfLoadedText = true;
	}

	/**
	 * Cancel {@link #skipParsingOfLoadedText()}: the next parsing is a real one
	 */
	public void cancelSkippingOfLoadedText() {
		skipParsingOfLoadedText = false;
	}

	public PropertyChangeSupport getPropertyChangeSupport() {
		return pcSupport;
	}

	public FMLCompilationUnitParser getFMLParser() {
		return fmlParser;
	}

	public CompilationUnitResource getFMLResource() {
		if (editor != null) {
			return editor.getFMLResource();
		}
		return null;
	}

	public FMLCompilationUnit getCompilationUnit() {
		if (getFMLResource() != null) {
			return getFMLResource().getCompilationUnit();
		}
		return null;
	}

	public FMLParseResult getParseResult() {
		return result;
	}

	public FMLValidationReport getValidationReport() {
		return result.getValidationReport();
	}

	/**
	 * Parse contents of editor, and update model accordingly
	 */
	@Override
	public ParseResult parse(RSyntaxDocument doc, String style) {

		/*if (fmlWillChange) {
			return result;
		}*/

		/*if (!editor.isDocumentModified()) {
			return result;
		}*/

		if (skipParsingOfLoadedText) {
			skipParsingOfLoadedText = false;
			if (editor.getTextArea().getText().equals(editor.getLastParsedText())) {
				result.setParsedLines(0, editor.getTextArea().getLineCount());
				// The notices were computed while the text area was still empty: the margin icons of all lines but the first were left
				// out, they are added now that the text is there
				updateEditorGutter();
				// What a parsing tells its listeners (code completion among them): the unit of the editor
				getPropertyChangeSupport().firePropertyChange(PROPERTY_COMPILATION_UNIT, null, getCompilationUnit());
				return result;
			}
		}

		// We keep this log unless debug of 2.99 is performed, because a parsing is really impacting and should be tracked
		logger.info("Parsing FML document......");
		// Thread.dumpStack();

		result.clearNotices();
		editor.getGutter().removeAllTrackingIcons();

		boolean requiresNewPrettyPrint = false;

		result.setParsedLines(0, editor.getTextArea().getLineCount());

		try {
			// Prevent editor from concurrent modification
			editor.modelWillChange();

			// System.out.println("Bon ok on reparse [");
			// System.out.println(editor.getTextArea().getText());
			// System.out.println("]");

			String parsedText = editor.getTextArea().getText();
			FMLCompilationUnit parsedCompilationUnit = getFMLParser().parse(parsedText, editor.getFactory(),
					(modelSlotClasses) -> {
						// System.out.println("Parsing: " + editor.getTextArea().getText());
						// System.out.println("Uses model slot classes : " + modelSlotClasses);
						return editor.getFMLResource().updateFMLModelFactory(modelSlotClasses);
					}, true); // Finalize deserialization now

			getFMLResource().setUnparseableContents(null);
			parsedCompilationUnit.setResource(getFMLResource());

			// System.out.println("Parsing succeeded");

			for (ElementImportDeclaration elementImportDeclaration : parsedCompilationUnit.getElementImports()) {
				elementImportDeclaration.clearReferencedObject();
			}

			// This is the update process
			// Parsing the same text again is not an edition: the merge sets new pretty-print delegates, IDs and types on every object, and
			// the resource of a FML that was only opened would be marked modified (see TextUpdate)
			FMLCompilationUnit existingData = editor.getFMLResource().getCompilationUnit();
			RequiresNewPrettyPrintListener pcListener = new RequiresNewPrettyPrintListener(existingData);
			try (TextUpdate textUpdate = new TextUpdate(editor.getFMLResource(), parsedText.equals(editor.getLastParsedText()))) {
				existingData.updateWith(parsedCompilationUnit);
				existingData.getTypingSpace().resolveUnresolvedTypes();

				// There is a trick here: the update has nullified the resource
				// (this is normal, just parsed FMLCompilationUnit does not knows the resource)
				// So set the resource again at the end of the update process
				existingData.setResource(editor.getFMLResource());
				// System.out.println("Check also: " + (editor.getFMLResource().getResourceData() == existingData));

				// Checking imports
				existingData.getPropertyChangeSupport().addPropertyChangeListener(pcListener);
				existingData.manageImports();
				existingData.getPropertyChangeSupport().removePropertyChangeListener(pcListener);
				requiresNewPrettyPrint = pcListener.requiresNewPrettyPrint();

				// Now revalidate all bindings
				existingData.revalidateAllBindings();
			}
			// The model is now in sync with this text
			editor.setLastParsedText(parsedText);

			// We perform a full validation to detect validation issues
			validate(existingData);

			/*FMLValidationReport validationReport = validate(existingData);
			result.setValidationReport(validationReport);
			
			// Then we browse SemanticAnalysisIssue as raised by semantics analyzing
			for (SemanticAnalysisIssue semanticAnalysisIssue : existingData.getPrettyPrintDelegate().getSemanticAnalysisIssues()) {
				result.addNotice(new SemanticAnalyzerNotice(this, semanticAnalysisIssue));
				result.addSemanticAnalysisIssue(semanticAnalysisIssue);
			}
			
			// Adding notices from validation
			for (ValidationIssue<?, ?> validationIssue : validationReport.getAllErrors()) {
				result.addNotice(new ValidationIssueNotice(this, validationIssue));
			}
			for (ValidationIssue<?, ?> validationIssue : validationReport.getAllWarnings()) {
				result.addNotice(new ValidationIssueNotice(this, validationIssue));
			}
			for (ValidationIssue<?, ?> validationIssue : validationReport.getAllInfoIssues()) {
				result.addNotice(new ValidationIssueNotice(this, validationIssue));
			}*/

			getPropertyChangeSupport().firePropertyChange(PROPERTY_COMPILATION_UNIT, null, existingData);

		} catch (ParseException e) {
			// Parse error: cannot do more than display position when parsing failed
			// TODO: handle errors during parsing
			/*if (result.getValidationReport() == null) {
				FMLValidationReport validationReport = validate(null);
				result.setValidationReport(validationReport);
			}*/
			/*if (result.getValidationReport() != null) {
				result.getValidationReport().clear();
			}
			result.clearNotices();
			result.addNotice(new ParseErrorNotice(this, e));
			result.addParseError(e);*/
			handleParseError(e);
		} catch (Exception e) {
			e.printStackTrace();
			handleUnexpectedException(e);
		} finally {
			editor.modelHasChanged(requiresNewPrettyPrint);
		}

		// result.setParsedLines(0, editor.getTextArea().getLineCount());

		/*for (ParserNotice parserNotice : result.getNotices()) {
			try {
				// System.out.println("Adding line " + parserNotice.getLine() + " message: " + parserNotice.getMessage());
				if (parserNotice.getLine() > 0 && parserNotice.getLine() <= editor.getTextArea().getLineCount()) {
					editor.getGutter().addLineTrackingIcon(parserNotice.getLine() - 1, ((FMLNotice) parserNotice).getIcon());
				}
				else {
					logger.warning("Unexpected notice at line:" + parserNotice.getLength() + " " + parserNotice);
				}
			} catch (BadLocationException e) {
				System.out.println("BadLocationException when trying to add at line: " + (parserNotice.getLine() - 1));
				e.printStackTrace();
			}
		}*/

		return result;

	}

	public List<ParserNotice> getParserNotices() {
		if (result != null) {
			return result.getNotices();
		}
		return Collections.emptyList();
	}

	public List<ParserNotice> getParserNotices(int line) {
		List<ParserNotice> returned = new ArrayList<>();
		for (ParserNotice parserNotice : result.getNotices()) {
			if (parserNotice.getLine() == line) {
				returned.add(parserNotice);
			}
		}
		return returned;
	}

	public FMLTechnologyAdapterController getFMLTechnologyAdapterController() {
		return editor.getFMLTechnologyAdapterController();
	}

	private void handleParseError(ParseException e) {

		getFMLResource().setUnparseableContents(e.getRawSource().getRawText());

		if (result.getValidationReport() != null) {
			result.getValidationReport().clear();
		}
		result.clearNotices();
		result.addNotice(new ParseErrorNotice(this, e));
		result.addParseError(e);

		result.setParsedLines(0, e.getLine());

		updateEditorGutter();
	}

	private void handleUnexpectedException(Exception e) {
		if (result.getValidationReport() != null) {
			result.getValidationReport().clear();
		}
		result.clearNotices();
		result.addNotice(new UnexpectedExceptionNotice(this, e));
		result.addUnexpectedException(e);

		// result.setParsedLines(0, 0);

		updateEditorGutter();
	}

	public FMLValidationReport validate(FMLCompilationUnit compilationUnit) {
		return validate(compilationUnit, true);
	}

	/**
	 * Update the notices of the editor from the validation report of supplied compilation unit, and from the issues of its semantic
	 * analysis
	 *
	 * @param revalidate
	 *            false when the report is known to be up to date (just computed for this unit): it is then used as it is
	 */
	private FMLValidationReport validate(FMLCompilationUnit compilationUnit, boolean revalidate) {

		result.clearNotices();
		editor.getGutter().removeAllTrackingIcons();

		FMLValidationReport virtualModelReport = (FMLValidationReport) getFMLTechnologyAdapterController()
				.getValidationReport(compilationUnit);
		result.setValidationReport(virtualModelReport);

		if (revalidate) {
			try {
				virtualModelReport.revalidate(compilationUnit);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}

		// Then we browse SemanticAnalysisIssue as raised by semantics analyzing
		if (compilationUnit.getPrettyPrintDelegate() != null) {
			for (SemanticAnalysisIssue semanticAnalysisIssue : compilationUnit.getPrettyPrintDelegate().getSemanticAnalysisIssues()) {
				result.addNotice(new SemanticAnalyzerNotice(this, semanticAnalysisIssue));
				result.addSemanticAnalysisIssue(semanticAnalysisIssue);
			}
			for (SemanticAnalysisWarning semanticAnalysisWarning : compilationUnit.getPrettyPrintDelegate().getSemanticAnalysisWarnings()) {
				result.addNotice(new SemanticAnalyzerNotice(this, semanticAnalysisWarning));
				result.addSemanticAnalysisWarning(semanticAnalysisWarning);
			}
		}

		// Adding notices from validation
		for (ValidationIssue<?, ?> validationIssue : virtualModelReport.getAllErrors()) {
			result.addNotice(new ValidationIssueNotice(this, validationIssue));
		}
		for (ValidationIssue<?, ?> validationIssue : virtualModelReport.getAllWarnings()) {
			result.addNotice(new ValidationIssueNotice(this, validationIssue));
		}
		for (ValidationIssue<?, ?> validationIssue : virtualModelReport.getAllInfoIssues()) {
			result.addNotice(new ValidationIssueNotice(this, validationIssue));
		}

		result.setParsedLines(0, editor.getTextArea().getLineCount());

		updateEditorGutter();

		return virtualModelReport;
	}

	private void updateEditorGutter() {
		for (ParserNotice parserNotice : result.getNotices()) {
			try {
				// System.out.println("Adding line " + parserNotice.getLine() + " message: " + parserNotice.getMessage());
				if (parserNotice.getLine() > 0 && parserNotice.getLine() <= editor.getTextArea().getLineCount()) {
					editor.getGutter().addLineTrackingIcon(parserNotice.getLine() - 1, ((FMLNotice) parserNotice).getIcon());
				}
				else if (isInitialized) {
					// We warn only if it is initialized
					logger.warning("Unexpected notice at line:" + parserNotice.getLength() + " " + parserNotice);
				}
			} catch (BadLocationException e) {
				System.out.println("BadLocationException when trying to add at line: " + (parserNotice.getLine() - 1));
				e.printStackTrace();
			}
		}

	}

	/**
	 * A {@link PropertyChangeListener} tracking needs to rebuild new pretty-print
	 * 
	 * @author sylvain
	 *
	 */
	class RequiresNewPrettyPrintListener implements PropertyChangeListener {
		private final FMLCompilationUnit existingData;
		private boolean requiresNewPrettyPrint = false;

		private RequiresNewPrettyPrintListener(FMLCompilationUnit existingData) {
			this.existingData = existingData;
		}

		public boolean requiresNewPrettyPrint() {
			return requiresNewPrettyPrint;
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getSource() == existingData) {
				if (evt.getPropertyName().equals(FMLCompilationUnit.JAVA_IMPORTS_KEY)) {
					// System.out.println("Attention, nouveau Java import");
					requiresNewPrettyPrint = true;
				}
				else if (evt.getPropertyName().equals(FMLCompilationUnit.ELEMENT_IMPORTS_KEY)) {
					requiresNewPrettyPrint = true;
				}
				else if (evt.getPropertyName().equals(FMLCompilationUnit.USE_DECLARATIONS_KEY)) {
					requiresNewPrettyPrint = true;
				}
				else if (evt.getPropertyName().equals(FMLCompilationUnit.NAMESPACES_KEY)) {
					requiresNewPrettyPrint = true;
				}
			}
		}
	}

}
