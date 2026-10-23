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

import org.fife.ui.rsyntaxtextarea.Token;

/**
 * The token types that the FML editor colours, i.e. the ones produced by <code>FMLTokenMaker</code>. Each one has a style (colour, bold,
 * italic) in a {@link FMLEditorTheme}.
 * 
 * @author sylvain
 */
public enum FMLTokenKind {
	RESERVED_WORD(Token.RESERVED_WORD),
	RESERVED_WORD_2(Token.RESERVED_WORD_2),
	DATA_TYPE(Token.DATA_TYPE),
	FUNCTION(Token.FUNCTION),
	ANNOTATION(Token.ANNOTATION),
	LITERAL_BOOLEAN(Token.LITERAL_BOOLEAN),
	LITERAL_NUMBER_DECIMAL_INT(Token.LITERAL_NUMBER_DECIMAL_INT),
	LITERAL_NUMBER_FLOAT(Token.LITERAL_NUMBER_FLOAT),
	LITERAL_NUMBER_HEXADECIMAL(Token.LITERAL_NUMBER_HEXADECIMAL),
	LITERAL_STRING_DOUBLE_QUOTE(Token.LITERAL_STRING_DOUBLE_QUOTE),
	COMMENT_EOL(Token.COMMENT_EOL),
	COMMENT_MULTILINE(Token.COMMENT_MULTILINE),
	COMMENT_DOCUMENTATION(Token.COMMENT_DOCUMENTATION),
	IDENTIFIER(Token.IDENTIFIER),
	SEPARATOR(Token.SEPARATOR),
	OPERATOR(Token.OPERATOR),
	WHITESPACE(Token.WHITESPACE),
	ERROR_NUMBER_FORMAT(Token.ERROR_NUMBER_FORMAT),
	ERROR_STRING_DOUBLE(Token.ERROR_STRING_DOUBLE);

	private final int tokenType;

	private FMLTokenKind(int tokenType) {
		this.tokenType = tokenType;
	}

	/** The RSyntaxTextArea token type */
	public int getTokenType() {
		return tokenType;
	}

	/** The name used as localization key and as <code>token</code> attribute in the theme files */
	public String getName() {
		return name();
	}
}
