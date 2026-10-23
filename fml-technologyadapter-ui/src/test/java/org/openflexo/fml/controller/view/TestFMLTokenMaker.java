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

package org.openflexo.fml.controller.view;

import static org.junit.Assert.assertEquals;

import javax.swing.text.Segment;

import org.fife.ui.rsyntaxtextarea.Token;
import org.junit.Test;

/**
 * Tests the colouring of the keywords and types of FML
 */
public class TestFMLTokenMaker {

	private int typeOf(String word) {
		FMLTokenMaker tokenMaker = new FMLTokenMaker();
		Token token = tokenMaker.getTokenList(new Segment(word.toCharArray(), 0, word.length()), Token.NULL, 0);
		return token.getType();
	}

	@Test
	public void testTypes() {
		for (String type : new String[] { "String", "boolean", "byte", "char", "double", "float", "int", "long", "short", "void" }) {
			assertEquals(type, Token.DATA_TYPE, typeOf(type));
		}
	}

	@Test
	public void testCreateAndDeleteAreKeywords() {
		assertEquals(Token.RESERVED_WORD_2, typeOf("create"));
		assertEquals(Token.RESERVED_WORD_2, typeOf("delete"));
	}

	@Test
	public void testNamesThatContainThemAreIdentifiers() {
		assertEquals(Token.IDENTIFIER, typeOf("Strings"));
		assertEquals(Token.IDENTIFIER, typeOf("created"));
		assertEquals(Token.IDENTIFIER, typeOf("string"));
	}
}
