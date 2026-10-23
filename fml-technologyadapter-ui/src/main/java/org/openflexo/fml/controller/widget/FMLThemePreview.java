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
package org.openflexo.fml.controller.widget;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.openflexo.fml.controller.theme.FMLEditorTheme;
import org.openflexo.fml.controller.theme.FMLTokenStyle;
import org.openflexo.fml.rstasupport.FMLLanguageSupport;
import org.openflexo.gina.controller.FIBController;
import org.openflexo.gina.model.widget.FIBCustom;
import org.openflexo.gina.model.widget.FIBCustom.FIBCustomComponent;
import org.openflexo.swing.CustomPopup.ApplyCancelListener;

/**
 * Shows a sample of FML coloured with a {@link FMLEditorTheme}, and follows the edition of that theme.<br>
 * 
 * Used by the preferences page of the FML editor.
 * 
 * @author sylvain
 */
@SuppressWarnings("serial")
public class FMLThemePreview extends JPanel implements FIBCustomComponent<FMLEditorTheme> {

	static {
		AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
		atmf.putMapping(FMLLanguageSupport.SYNTAX_STYLE_FML, "org.openflexo.fml.controller.view.FMLTokenMaker");
	}

	private static final String SAMPLE = "/**\n" + " * A documented concept\n" + " */\n" + "@URI(\"http://www.openflexo.org/Sample\")\n"
			+ "model Sample {\n" + "\n" + "  // A line comment\n" + "  String name;\n" + "  Integer count = 42;\n" + "  Float ratio = 0.5;\n"
			+ "  Boolean done = true;\n" + "\n" + "  /* A block comment */\n" + "  concept Item {\n" + "    String label = \"Item\" + count;\n"
			+ "    void update(Integer delta) {\n" + "      count = count + delta;\n" + "      log(label);\n" + "    }\n" + "  }\n" + "}\n";

	private final RSyntaxTextArea textArea;
	private FMLEditorTheme theme;
	private FMLEditorTheme revertValue;

	private final PropertyChangeListener themeListener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			SwingUtilities.invokeLater(() -> applyTheme());
		}
	};

	public FMLThemePreview(FMLEditorTheme theme) {
		super(new BorderLayout());
		textArea = new RSyntaxTextArea(14, 50);
		textArea.setSyntaxEditingStyle(FMLLanguageSupport.SYNTAX_STYLE_FML);
		textArea.setText(SAMPLE);
		textArea.setEditable(false);
		textArea.setCaretPosition(0);
		add(new RTextScrollPane(textArea), BorderLayout.CENTER);
		setEditedObject(theme);
	}

	private void applyTheme() {
		if (theme != null) {
			theme.applyTo(textArea);
		}
	}

	@Override
	public FMLEditorTheme getEditedObject() {
		return theme;
	}

	@Override
	public void setEditedObject(FMLEditorTheme newTheme) {
		if (newTheme != theme) {
			if (theme != null) {
				theme.getPropertyChangeSupport().removePropertyChangeListener(themeListener);
				for (FMLTokenStyle style : theme.getTokenStyles()) {
					style.getPropertyChangeSupport().removePropertyChangeListener(themeListener);
				}
			}
			theme = newTheme;
			if (theme != null) {
				theme.getPropertyChangeSupport().addPropertyChangeListener(themeListener);
				for (FMLTokenStyle style : theme.getTokenStyles()) {
					style.getPropertyChangeSupport().addPropertyChangeListener(themeListener);
				}
			}
			applyTheme();
		}
	}

	@Override
	public FMLEditorTheme getRevertValue() {
		return revertValue;
	}

	@Override
	public void setRevertValue(FMLEditorTheme revertValue) {
		this.revertValue = revertValue;
	}

	@Override
	public void addApplyCancelListener(ApplyCancelListener l) {
	}

	@Override
	public void removeApplyCancelListener(ApplyCancelListener l) {
	}

	@Override
	public Class<FMLEditorTheme> getRepresentedType() {
		return FMLEditorTheme.class;
	}

	@Override
	public void init(FIBCustom component, FIBController controller) {
	}

	@Override
	public void delete() {
		setEditedObject(null);
	}
}
