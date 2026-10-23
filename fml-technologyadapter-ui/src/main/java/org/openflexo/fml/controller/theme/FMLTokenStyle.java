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

import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.AccessibleProxyObject;

/**
 * The style (foreground colour, bold, italic) that a {@link FMLEditorTheme} gives to one {@link FMLTokenKind}
 * 
 * @author sylvain
 */
@ModelEntity
@ImplementationClass(FMLTokenStyle.FMLTokenStyleImpl.class)
public interface FMLTokenStyle extends AccessibleProxyObject {

	@PropertyIdentifier(type = FMLEditorTheme.class)
	public static final String THEME = "theme";
	@PropertyIdentifier(type = FMLTokenKind.class)
	public static final String KIND = "kind";
	@PropertyIdentifier(type = Color.class)
	public static final String FOREGROUND = "foreground";
	@PropertyIdentifier(type = Boolean.class)
	public static final String BOLD = "bold";
	@PropertyIdentifier(type = Boolean.class)
	public static final String ITALIC = "italic";

	@Getter(value = THEME, inverse = FMLEditorTheme.TOKEN_STYLES)
	public FMLEditorTheme getTheme();

	@Setter(THEME)
	public void setTheme(FMLEditorTheme theme);

	@Getter(KIND)
	public FMLTokenKind getKind();

	@Setter(KIND)
	public void setKind(FMLTokenKind kind);

	@Getter(FOREGROUND)
	public Color getForeground();

	@Setter(FOREGROUND)
	public void setForeground(Color foreground);

	@Getter(value = BOLD, defaultValue = "false")
	public boolean isBold();

	@Setter(BOLD)
	public void setBold(boolean bold);

	@Getter(value = ITALIC, defaultValue = "false")
	public boolean isItalic();

	@Setter(ITALIC)
	public void setItalic(boolean italic);

	/** Localization key of the token kind */
	public String getLabel();

	/** False if the owning theme is read-only: the editing widgets are disabled */
	public boolean isEditable();

	public abstract class FMLTokenStyleImpl implements FMLTokenStyle {

		@Override
		public String getLabel() {
			return getKind() != null ? getKind().getName() : null;
		}

		@Override
		public void setForeground(Color foreground) {
			if (isEditable()) {
				performSuperSetter(FOREGROUND, foreground);
			}
		}

		@Override
		public void setBold(boolean bold) {
			if (isEditable()) {
				performSuperSetter(BOLD, bold);
			}
		}

		@Override
		public void setItalic(boolean italic) {
			if (isEditable()) {
				performSuperSetter(ITALIC, italic);
			}
		}

		@Override
		public boolean isEditable() {
			return getTheme() != null && !getTheme().isReadOnly();
		}
	}
}
