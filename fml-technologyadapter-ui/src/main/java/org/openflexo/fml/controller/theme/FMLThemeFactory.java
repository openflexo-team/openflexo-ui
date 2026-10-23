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

import java.io.IOException;
import java.io.InputStream;

import org.fife.ui.rsyntaxtextarea.Theme;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.PamelaMetaModelLibrary;
import org.openflexo.pamela.exceptions.ModelDefinitionException;

/**
 * Factory of {@link FMLEditorTheme}, which are not part of the preferences model: a theme is stored in its own file
 * 
 * @author sylvain
 */
public class FMLThemeFactory extends PamelaModelFactory {

	public FMLThemeFactory() throws ModelDefinitionException {
		super(PamelaMetaModelLibrary.retrieveMetaModel(FMLEditorTheme.class, FMLTokenStyle.class));
	}

	/**
	 * Reads a theme in the RSyntaxTextArea format
	 * 
	 * @throws IOException
	 *             if the stream is not a valid theme
	 */
	public FMLEditorTheme loadTheme(InputStream in, String name, boolean readOnly) throws IOException {
		FMLEditorTheme returned = newInstance(FMLEditorTheme.class);
		returned.setRSyntaxTheme(Theme.load(in), this);
		returned.setName(name);
		returned.setReadOnly(readOnly);
		return returned;
	}

	/** A deep copy of the given theme, which is editable whatever the original is */
	public FMLEditorTheme duplicate(FMLEditorTheme theme, String name) {
		FMLEditorTheme returned = newInstance(FMLEditorTheme.class);
		returned.setRSyntaxTheme(theme.toRSyntaxTheme(), this);
		returned.setName(name);
		return returned;
	}
}
