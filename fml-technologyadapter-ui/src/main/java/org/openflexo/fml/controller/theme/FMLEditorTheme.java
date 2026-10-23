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

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Style;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.openflexo.pamela.AccessibleProxyObject;
import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Getter.Cardinality;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;

/**
 * A colouring theme of the FML editor: a {@link FMLTokenStyle} for each {@link FMLTokenKind}, the editor background and the current-line
 * highlight.<br>
 * 
 * A theme is stored in the RSyntaxTextArea theme format (<code>theme.dtd</code>), see {@link FMLThemeFactory}. The attributes of that format
 * that this class does not edit (caret, selection, gutter...) are kept as they were loaded and written back unchanged.<br>
 * 
 * A read-only theme is one of the shipped themes: it cannot be modified nor deleted, only duplicated.
 * 
 * @author sylvain
 */
@ModelEntity
@ImplementationClass(FMLEditorTheme.FMLEditorThemeImpl.class)
public interface FMLEditorTheme extends AccessibleProxyObject {

	@PropertyIdentifier(type = String.class)
	public static final String NAME = "name";
	@PropertyIdentifier(type = Boolean.class)
	public static final String READ_ONLY = "readOnly";
	@PropertyIdentifier(type = Color.class)
	public static final String BACKGROUND = "background";
	@PropertyIdentifier(type = Color.class)
	public static final String CURRENT_LINE_HIGHLIGHT = "currentLineHighlight";
	@PropertyIdentifier(type = FMLTokenStyle.class, cardinality = Cardinality.LIST)
	public static final String TOKEN_STYLES = "tokenStyles";

	@Getter(NAME)
	public String getName();

	@Setter(NAME)
	public void setName(String name);

	@Getter(value = READ_ONLY, defaultValue = "false")
	public boolean isReadOnly();

	@Setter(READ_ONLY)
	public void setReadOnly(boolean readOnly);

	@Getter(BACKGROUND)
	public Color getBackground();

	@Setter(BACKGROUND)
	public void setBackground(Color background);

	@Getter(CURRENT_LINE_HIGHLIGHT)
	public Color getCurrentLineHighlight();

	@Setter(CURRENT_LINE_HIGHLIGHT)
	public void setCurrentLineHighlight(Color currentLineHighlight);

	@Getter(value = TOKEN_STYLES, cardinality = Cardinality.LIST, inverse = FMLTokenStyle.THEME)
	public List<FMLTokenStyle> getTokenStyles();

	@Adder(TOKEN_STYLES)
	public void addToTokenStyles(FMLTokenStyle style);

	@Remover(TOKEN_STYLES)
	public void removeFromTokenStyles(FMLTokenStyle style);

	public FMLTokenStyle getTokenStyle(FMLTokenKind kind);

	/** Whether the values of this theme may be edited */
	public boolean isEditable();

	/** Builds the RSyntaxTextArea theme that this one stands for */
	public Theme toRSyntaxTheme();

	/** Takes the values that this class edits from a RSyntaxTextArea theme; the other ones are kept for {@link #toRSyntaxTheme()} */
	public void setRSyntaxTheme(Theme theme, FMLThemeFactory factory);

	public void applyTo(RSyntaxTextArea textArea);

	public void save(OutputStream out) throws IOException;

	public abstract class FMLEditorThemeImpl implements FMLEditorTheme {

		/** The loaded theme, source of every attribute that is not edited here */
		private Theme base;

		@Override
		public boolean isEditable() {
			return !isReadOnly();
		}

		// A shipped theme cannot be modified

		@Override
		public void setName(String name) {
			if (isEditable()) {
				performSuperSetter(NAME, name);
			}
		}

		@Override
		public void setBackground(Color background) {
			if (isEditable()) {
				performSuperSetter(BACKGROUND, background);
			}
		}

		@Override
		public void setCurrentLineHighlight(Color currentLineHighlight) {
			if (isEditable()) {
				performSuperSetter(CURRENT_LINE_HIGHLIGHT, currentLineHighlight);
			}
		}

		@Override
		public FMLTokenStyle getTokenStyle(FMLTokenKind kind) {
			for (FMLTokenStyle style : getTokenStyles()) {
				if (style.getKind() == kind) {
					return style;
				}
			}
			return null;
		}

		@Override
		public void setRSyntaxTheme(Theme theme, FMLThemeFactory factory) {
			base = theme;
			performSuperSetter(BACKGROUND, theme.bgColor);
			performSuperSetter(CURRENT_LINE_HIGHLIGHT, theme.currentLineHighlight);
			for (FMLTokenKind kind : FMLTokenKind.values()) {
				FMLTokenStyle tokenStyle = getTokenStyle(kind);
				if (tokenStyle == null) {
					tokenStyle = factory.newInstance(FMLTokenStyle.class);
					tokenStyle.setKind(kind);
					addToTokenStyles(tokenStyle);
				}
				Style style = theme.scheme.getStyle(kind.getTokenType());
				if (style != null) {
					tokenStyle.performSuperSetter(FMLTokenStyle.FOREGROUND, style.foreground);
					tokenStyle.performSuperSetter(FMLTokenStyle.BOLD, style.font != null && style.font.isBold());
					tokenStyle.performSuperSetter(FMLTokenStyle.ITALIC, style.font != null && style.font.isItalic());
				}
			}
		}

		@Override
		public Theme toRSyntaxTheme() {
			Theme returned = base != null ? copy(base) : new Theme(new RSyntaxTextArea());
			if (getBackground() != null) {
				returned.bgColor = getBackground();
			}
			if (getCurrentLineHighlight() != null) {
				returned.currentLineHighlight = getCurrentLineHighlight();
			}
			Font baseFont = returned.baseFont;
			for (FMLTokenStyle tokenStyle : getTokenStyles()) {
				Style style = returned.scheme.getStyle(tokenStyle.getKind().getTokenType());
				if (style == null) {
					style = new Style();
					returned.scheme.setStyle(tokenStyle.getKind().getTokenType(), style);
				}
				style.foreground = tokenStyle.getForeground();
				int fontStyle = (tokenStyle.isBold() ? Font.BOLD : Font.PLAIN) | (tokenStyle.isItalic() ? Font.ITALIC : Font.PLAIN);
				style.font = fontStyle == Font.PLAIN ? null : baseFont.deriveFont(fontStyle);
			}
			return returned;
		}

		/**
		 * A theme that shares nothing mutable with the given one (the scheme is the only part that is modified). What the given theme does
		 * not define (a theme file may omit attributes) keeps the RSyntaxTextArea default.
		 */
		private Theme copy(Theme theme) {
			Theme returned = new Theme(new RSyntaxTextArea());
			for (java.lang.reflect.Field field : Theme.class.getFields()) {
				if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
					try {
						Object value = field.get(theme);
						if (value != null) {
							field.set(returned, value);
						}
					} catch (IllegalAccessException e) {
						throw new IllegalStateException(e);
					}
				}
			}
			returned.scheme = (SyntaxScheme) theme.scheme.clone();
			return returned;
		}

		@Override
		public void applyTo(RSyntaxTextArea textArea) {
			toRSyntaxTheme().apply(textArea);
		}

		@Override
		public void save(OutputStream out) throws IOException {
			toRSyntaxTheme().save(out);
		}
	}
}
