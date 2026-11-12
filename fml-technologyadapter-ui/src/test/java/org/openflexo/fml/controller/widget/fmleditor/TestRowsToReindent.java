package org.openflexo.fml.controller.widget.fmleditor;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

/**
 * The rows that the re-indent action of the FML editor works on (P2PP-F-1)
 */
public class TestRowsToReindent {

	// offsets: row 1 is 0-9 (its separator at 9), row 2 is 10-17, row 3 is 18-19, row 4 (empty) starts at 20
	private static final String TEXT = "model M {\n\tint n;\n}\n";

	@Test
	public void noSelectionIsTheWholeText() {
		// the text ends with a line separator: its last row is empty
		assertArrayEquals(new int[] { 1, 4 }, FMLEditor.rowsToReindent(TEXT, 3, 3));
	}

	@Test
	public void aSelectionInsideARowIsThatRow() {
		assertArrayEquals(new int[] { 2, 2 }, FMLEditor.rowsToReindent(TEXT, 11, 13));
	}

	@Test
	public void aSelectionOverSeveralRows() {
		assertArrayEquals(new int[] { 1, 3 }, FMLEditor.rowsToReindent(TEXT, 2, 19));
	}

	@Test
	public void aSelectionEndingAtTheStartOfARowDoesNotTakeIt() {
		// rows 1 and 2 selected whole: the selection ends right after the separator of row 2, at the start of row 3
		assertArrayEquals(new int[] { 1, 2 }, FMLEditor.rowsToReindent(TEXT, 0, 18));
	}

	@Test
	public void aBackwardSelection() {
		assertArrayEquals(new int[] { 1, 3 }, FMLEditor.rowsToReindent(TEXT, 19, 2));
	}
}
