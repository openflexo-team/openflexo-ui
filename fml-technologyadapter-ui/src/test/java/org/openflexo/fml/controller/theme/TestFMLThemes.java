/**
 * 
 * Copyright (c) 2013-2017, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 * 
 * This file is part of Flexovieweditor, a component of the software infrastructure 
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

package org.openflexo.fml.controller.theme;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.fife.ui.rsyntaxtextarea.Style;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rsyntaxtextarea.Token;
import org.junit.Test;
import org.openflexo.pamela.exceptions.ModelDefinitionException;

/**
 * Tests the themes of the FML editor: shipped themes, protection, round trip through the RSyntaxTextArea format
 */
public class TestFMLThemes {

	private static final Color GOLD = new Color(0xA0, 0x78, 0x00);

	private FMLThemeLibrary newLibrary(File directory) throws ModelDefinitionException {
		return new FMLThemeLibrary(directory);
	}

	private File newDirectory() throws IOException {
		File returned = Files.createTempDirectory("fmlthemes").toFile();
		returned.deleteOnExit();
		return new File(returned, "themes");
	}

	@Test
	public void testShippedThemes() throws Exception {
		FMLThemeLibrary library = newLibrary(newDirectory());
		assertEquals(3, library.getThemes().size());
		for (String name : FMLThemeLibrary.SHIPPED_THEMES) {
			assertNotNull(name, library.getTheme(name));
			assertTrue(name, library.getTheme(name).isReadOnly());
			assertEquals(FMLTokenKind.values().length, library.getTheme(name).getTokenStyles().size());
		}
		assertEquals(GOLD, library.getDefaultTheme().getTokenStyle(FMLTokenKind.ANNOTATION).getForeground());
	}

	@Test
	public void testShippedThemesAreProtected() throws Exception {
		File directory = newDirectory();
		FMLThemeLibrary library = newLibrary(directory);
		FMLEditorTheme theme = library.getDefaultTheme();
		Color background = theme.getBackground();
		FMLTokenStyle keyword = theme.getTokenStyle(FMLTokenKind.RESERVED_WORD);
		Color keywordColor = keyword.getForeground();

		theme.setBackground(Color.PINK);
		theme.setName("renamed");
		keyword.setForeground(Color.PINK);
		keyword.setBold(!keyword.isBold());
		assertEquals(background, theme.getBackground());
		assertEquals("default", theme.getName());
		assertEquals(keywordColor, keyword.getForeground());

		assertFalse(library.delete(theme));
		assertFalse(library.rename(theme, "renamed"));
		assertEquals(3, library.getThemes().size());
		// Nothing is written for a shipped theme
		assertFalse(directory.exists());
	}

	@Test
	public void testDuplicateEditAndRoundTrip() throws Exception {
		File directory = newDirectory();
		FMLThemeLibrary library = newLibrary(directory);
		FMLEditorTheme mine = library.duplicate(library.getTheme("dark"));
		assertFalse(mine.isReadOnly());
		assertEquals("dark copy", mine.getName());
		assertEquals(4, library.getThemes().size());

		mine.setBackground(new Color(0x10, 0x20, 0x30));
		mine.setCurrentLineHighlight(new Color(0x40, 0x50, 0x60));
		FMLTokenStyle keyword = mine.getTokenStyle(FMLTokenKind.RESERVED_WORD);
		keyword.setForeground(new Color(0x11, 0x22, 0x33));
		keyword.setBold(true);
		keyword.setItalic(true);
		FMLTokenStyle comment = mine.getTokenStyle(FMLTokenKind.COMMENT_EOL);
		comment.setForeground(new Color(0x44, 0x55, 0x66));
		comment.setBold(false);
		comment.setItalic(false);

		File file = new File(directory, "dark copy.xml");
		assertTrue(file.exists());

		// What RSyntaxTextArea reads is what was edited
		try (InputStream in = new FileInputStream(file)) {
			Theme theme = Theme.load(in);
			assertEquals(new Color(0x10, 0x20, 0x30), theme.bgColor);
			assertEquals(new Color(0x40, 0x50, 0x60), theme.currentLineHighlight);
			Style style = theme.scheme.getStyle(Token.RESERVED_WORD);
			assertEquals(new Color(0x11, 0x22, 0x33), style.foreground);
			assertTrue(style.font.isBold());
			assertTrue(style.font.isItalic());
			style = theme.scheme.getStyle(Token.COMMENT_EOL);
			assertEquals(new Color(0x44, 0x55, 0x66), style.foreground);
			assertTrue(style.font == null || (!style.font.isBold() && !style.font.isItalic()));
		}

		// And a new session finds the same theme
		FMLThemeLibrary reloaded = newLibrary(directory);
		FMLEditorTheme again = reloaded.getTheme("dark copy");
		assertNotNull(again);
		assertFalse(again.isReadOnly());
		assertEquals(mine.getBackground(), again.getBackground());
		assertEquals(mine.getCurrentLineHighlight(), again.getCurrentLineHighlight());
		for (FMLTokenKind kind : FMLTokenKind.values()) {
			FMLTokenStyle expected = mine.getTokenStyle(kind);
			FMLTokenStyle actual = again.getTokenStyle(kind);
			assertEquals(kind.name(), expected.getForeground(), actual.getForeground());
			assertEquals(kind.name(), expected.isBold(), actual.isBold());
			assertEquals(kind.name(), expected.isItalic(), actual.isItalic());
		}
	}

	@Test
	public void testEditionIsNotified() throws Exception {
		FMLThemeLibrary library = newLibrary(newDirectory());
		FMLEditorTheme mine = library.createTheme();
		java.util.List<String> events = new java.util.ArrayList<>();
		library.getPropertyChangeSupport().addPropertyChangeListener(evt -> events.add(evt.getPropertyName()));
		FMLTokenStyle keyword = mine.getTokenStyle(FMLTokenKind.RESERVED_WORD);
		java.util.List<String> styleEvents = new java.util.ArrayList<>();
		keyword.getPropertyChangeSupport().addPropertyChangeListener(evt -> styleEvents.add(evt.getPropertyName()));

		keyword.setForeground(Color.MAGENTA);
		keyword.setBold(!keyword.isBold());
		mine.setBackground(Color.BLACK);

		assertTrue(styleEvents.contains(FMLTokenStyle.FOREGROUND));
		assertTrue(styleEvents.contains(FMLTokenStyle.BOLD));
		assertEquals(3, events.stream().filter(FMLThemeLibrary.THEME_CHANGED::equals).count());
	}

	@Test
	public void testRenameAndDelete() throws Exception {
		File directory = newDirectory();
		FMLThemeLibrary library = newLibrary(directory);
		FMLEditorTheme mine = library.createTheme();
		assertTrue(new File(directory, mine.getName() + ".xml").exists());

		assertFalse("name already used", library.rename(mine, "default"));
		assertFalse("not a file name", library.rename(mine, "a/b"));
		assertTrue(library.rename(mine, "mine"));
		assertFalse(new File(directory, "default copy.xml").exists());
		assertTrue(new File(directory, "mine.xml").exists());

		assertTrue(library.delete(mine));
		assertNull(library.getTheme("mine"));
		assertFalse(new File(directory, "mine.xml").exists());
		assertEquals(3, library.getThemes().size());
	}

	@Test
	public void testImportExport() throws Exception {
		File directory = newDirectory();
		FMLThemeLibrary library = newLibrary(directory);
		File exported = new File(directory.getParentFile(), "exported.xml");
		library.exportTheme(library.getTheme("eclipse"), exported);

		FMLEditorTheme imported = library.importTheme(exported);
		assertEquals("exported", imported.getName());
		assertFalse(imported.isReadOnly());
		assertEquals(library.getTheme("eclipse").getBackground(), imported.getBackground());
		// Importing again does not clash
		assertEquals("exported 2", library.importTheme(exported).getName());
	}
}
