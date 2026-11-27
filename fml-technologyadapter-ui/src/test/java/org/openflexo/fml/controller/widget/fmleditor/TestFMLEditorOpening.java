/**
 *
 * Copyright (c) 2026, Openflexo
 *
 * This file is part of Openflexo-technology-adapter-ui, a component of the software infrastructure
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
 *          of EPL 1.0, the licensors of this Program grant you additional
 *          permission to convey the resulting work. *
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.
 *
 * See http://www.openflexo.org/license.html for details.
 *
 *
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 *
 */

package org.openflexo.fml.controller.widget.fmleditor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import javax.swing.SwingUtilities;

import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.parser.ParserNotice;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLPrettyPrintDelegate;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.gina.test.OpenflexoFIBTestCase;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;
import org.openflexo.test.UITest;

/**
 * What the {@link FMLEditor} parses when it opens: the text a unit was loaded from is not parsed again when the unit is in sync with it,
 * and is parsed when it is not (unit edited before the editor opens, source that does not parse).<br>
 * A real parsing merges a new unit into the loaded one, which gives it a new pretty-print delegate: that is what tells here whether the
 * parsing happened.
 *
 * @author sylvain
 *
 */
@RunWith(OrderedRunner.class)
public class TestFMLEditorOpening extends OpenflexoFIBTestCase {

	private static final String URI = "http://openflexo.org/test/TestResourceCenter/TestCompletion.fml";

	private static CompilationUnitResource fmlResource;

	@BeforeClass
	public static void setupClass() {
		instanciateTestServiceManager();
	}

	/** Open an editor on the resource, then run the parsing the text area asks for when its text is set */
	private static FMLEditorParser openAndRunFirstParsing() throws Exception {
		FMLEditor[] editor = new FMLEditor[1];
		SwingUtilities.invokeAndWait(() -> editor[0] = new FMLEditor(fmlResource, null));
		FMLEditorParser parser = editor[0].getTextArea().getParser();
		SwingUtilities.invokeAndWait(() -> parser.parse((RSyntaxDocument) editor[0].getTextArea().getDocument(),
				editor[0].getTextArea().getSyntaxEditingStyle()));
		return parser;
	}

	@Test
	@TestOrder(1)
	@Category(UITest.class)
	public void testOpeningUnchangedUnitDoesNotParseAgain() throws Exception {
		VirtualModel vm = serviceManager.getVirtualModelLibrary().getVirtualModel(URI);
		assertNotNull(vm);
		fmlResource = vm.getResource();
		FMLCompilationUnit cu = fmlResource.getLoadedResourceData();
		assertFalse(cu.isModified());
		FMLPrettyPrintDelegate<?> delegateBefore = cu.getPrettyPrintDelegate();
		assertNotNull(delegateBefore);

		openAndRunFirstParsing();

		assertSame("The text the unit was loaded from was parsed again", delegateBefore, cu.getPrettyPrintDelegate());
		assertFalse(cu.isModified());
	}

	@Test
	@TestOrder(2)
	@Category(UITest.class)
	public void testOpeningEditedUnitParsesIt() throws Exception {
		FMLCompilationUnit cu = fmlResource.getLoadedResourceData();
		FMLPrettyPrintDelegate<?> delegateBefore = cu.getPrettyPrintDelegate();
		cu.setIsModified();

		openAndRunFirstParsing();

		assertNotSame("A unit edited before the editor opens was not parsed again", delegateBefore, cu.getPrettyPrintDelegate());
	}

	@Test
	@TestOrder(3)
	@Category(UITest.class)
	public void testOpeningUnparseableSourceShowsParseError() throws Exception {
		// Stands for a source that failed to parse at loading: its text is kept as unparseable contents, shown by the editor
		fmlResource.setUnparseableContents("model Broken {{{ this does not parse");

		FMLEditorParser parser = openAndRunFirstParsing();

		boolean hasParseError = false;
		for (ParserNotice notice : parser.getParseResult().getNotices()) {
			if (notice instanceof ParseErrorNotice) {
				hasParseError = true;
			}
		}
		assertTrue("The editor opened on a source that does not parse shows no parse error", hasParseError);
	}
}
