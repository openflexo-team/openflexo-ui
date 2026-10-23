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
package org.openflexo.fml.controller;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;

import org.openflexo.fml.controller.theme.FMLEditorTheme;
import org.openflexo.fml.controller.theme.FMLThemeLibrary;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.prefs.Preferences;
import org.openflexo.prefs.PreferencesContainer;
import org.openflexo.toolbox.FileUtils;

/**
 * Preferences of the FML editor: the colouring themes, and which one is used.<br>
 * 
 * The themes are not stored in the preferences file but in their own files (see {@link FMLThemeLibrary}); only the name of the used theme
 * is. The used theme is the one that the user chose; the editors are coloured with the <i>active</i> theme, which becomes the used one (and
 * shows its latest edits) when the preferences are applied: {@link #ACTIVE_THEME} is notified then, and an editor that listens to it is
 * coloured as the user wants.
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(FMLPreferences.FMLPreferencesImpl.class)
@XMLElement(xmlTag = "FMLPreferences")
@Preferences(
		shortName = "FML editor",
		longName = "FML editor preferences",
		FIBPanel = "Fib/Prefs/FMLPreferences.fib",
		smallIcon = "Icons/Common/PresentationPreferences.png",
		bigIcon = "Icons/Common/PresentationPreferences_64x64.png")
public interface FMLPreferences extends PreferencesContainer {

	public static final String USED_THEME = "usedTheme";
	/** Notified when the preferences are applied: the editors take the used theme, as it is now */
	public static final String ACTIVE_THEME = "activeTheme";
	public static final String SELECTED_THEME = "selectedTheme";
	public static final String THEMES = "themes";

	public FMLThemeLibrary getThemeLibrary();

	public List<FMLEditorTheme> getThemes();

	/** The theme the FML editors are coloured with (the used theme as it was when the preferences were last applied); never null */
	public FMLEditorTheme getActiveTheme();

	/** The theme that the user chose, to be applied to the editors with the preferences; never null */
	public FMLEditorTheme getUsedTheme();

	public void setUsedTheme(FMLEditorTheme theme);

	/** The theme shown in the details of the preferences panel, the one being edited */
	public FMLEditorTheme getSelectedTheme();

	public void setSelectedTheme(FMLEditorTheme theme);

	/** Name of the selected theme; setting it renames the theme if possible (not for a shipped theme, nor to a name already used) */
	public String getSelectedThemeName();

	public void setSelectedThemeName(String name);

	public boolean canDeleteSelectedTheme();

	public boolean canRenameSelectedTheme();

	public void useSelectedTheme();

	public void createTheme();

	public void duplicateSelectedTheme();

	public void deleteSelectedTheme();

	public void importTheme();

	public void exportSelectedTheme();

	public abstract class FMLPreferencesImpl extends PreferencesContainerImpl implements FMLPreferences {

		private static final Logger logger = Logger.getLogger(FMLPreferences.class.getPackage().getName());

		private FMLThemeLibrary themeLibrary;
		private FMLEditorTheme selectedTheme;
		private FMLEditorTheme activeTheme;

		@Override
		public FMLThemeLibrary getThemeLibrary() {
			if (themeLibrary == null) {
				try {
					themeLibrary = new FMLThemeLibrary(new File(FileUtils.getApplicationDataDirectory(), "FMLThemes"));
				} catch (ModelDefinitionException e) {
					throw new IllegalStateException("Cannot build the FML themes model", e);
				}
				themeLibrary.getPropertyChangeSupport().addPropertyChangeListener(new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						if (evt.getPropertyName().equals(FMLThemeLibrary.THEMES)) {
							getPropertyChangeSupport().firePropertyChange(THEMES, null, getThemes());
							if (evt.getOldValue() != null && evt.getNewValue() == null) {
								themeHasBeenDeleted((FMLEditorTheme) evt.getOldValue());
							}
						}
					}
				});
			}
			return themeLibrary;
		}

		private void themeHasBeenDeleted(FMLEditorTheme deleted) {
			if (selectedTheme == deleted) {
				setSelectedTheme(getUsedTheme());
			}
			// The used theme is looked up by name: if it was the deleted one, it is now the default theme
			getPropertyChangeSupport().firePropertyChange(USED_THEME, deleted, getUsedTheme());
		}

		@Override
		public List<FMLEditorTheme> getThemes() {
			return getThemeLibrary().getThemes();
		}

		@Override
		public FMLEditorTheme getUsedTheme() {
			FMLEditorTheme returned = getThemeLibrary().getTheme(assertProperty(USED_THEME).getValue());
			return returned != null ? returned : getThemeLibrary().getDefaultTheme();
		}

		@Override
		public void setUsedTheme(FMLEditorTheme theme) {
			FMLEditorTheme old = getUsedTheme();
			if (theme != null && theme != old && getThemes().contains(theme)) {
				assertProperty(USED_THEME).setValue(theme.getName());
				getPropertyChangeSupport().firePropertyChange(USED_THEME, old, theme);
			}
		}

		@Override
		public FMLEditorTheme getActiveTheme() {
			if (activeTheme == null) {
				activeTheme = getUsedTheme();
			}
			return activeTheme;
		}

		@Override
		public void applyPreferences() {
			activeTheme = getUsedTheme();
			// The old value is null: the editors must be coloured again even if the theme is the same, as it may have been edited
			getPropertyChangeSupport().firePropertyChange(ACTIVE_THEME, null, activeTheme);
		}

		@Override
		public FMLEditorTheme getSelectedTheme() {
			if (selectedTheme == null) {
				selectedTheme = getUsedTheme();
			}
			return selectedTheme;
		}

		@Override
		public void setSelectedTheme(FMLEditorTheme theme) {
			FMLEditorTheme old = selectedTheme;
			if (theme != old) {
				selectedTheme = theme;
				getPropertyChangeSupport().firePropertyChange(SELECTED_THEME, old, theme);
			}

		}

		@Override
		public String getSelectedThemeName() {
			return getSelectedTheme() != null ? getSelectedTheme().getName() : null;
		}

		@Override
		public void setSelectedThemeName(String name) {
			FMLEditorTheme theme = getSelectedTheme();
			boolean wasUsed = theme == getUsedTheme();
			if (theme != null && !theme.getName().equals(name) && getThemeLibrary().rename(theme, name)) {
				// The used theme is kept by name
				if (wasUsed) {
					assertProperty(USED_THEME).setValue(name);
				}
				getPropertyChangeSupport().firePropertyChange(THEMES, null, getThemes());
			}
			// Also tells the field to show the actual name when the new one was refused
			getPropertyChangeSupport().firePropertyChange("selectedThemeName", null, getSelectedThemeName());
		}

		@Override
		public boolean canDeleteSelectedTheme() {
			return getSelectedTheme() != null && !getSelectedTheme().isReadOnly();
		}

		@Override
		public boolean canRenameSelectedTheme() {
			return canDeleteSelectedTheme();
		}

		@Override
		public void useSelectedTheme() {
			setUsedTheme(getSelectedTheme());
		}

		@Override
		public void createTheme() {
			setSelectedTheme(getThemeLibrary().createTheme());
		}

		@Override
		public void duplicateSelectedTheme() {
			if (getSelectedTheme() != null) {
				setSelectedTheme(getThemeLibrary().duplicate(getSelectedTheme()));
			}
		}

		@Override
		public void deleteSelectedTheme() {
			if (canDeleteSelectedTheme()) {
				getThemeLibrary().delete(getSelectedTheme());
			}
		}

		@Override
		public void importTheme() {
			JFileChooser chooser = new JFileChooser();
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				try {
					setSelectedTheme(getThemeLibrary().importTheme(chooser.getSelectedFile()));
				} catch (IOException e) {
					logger.log(Level.WARNING, "Cannot import FML editor theme " + chooser.getSelectedFile(), e);
				}
			}
		}

		@Override
		public void exportSelectedTheme() {
			if (getSelectedTheme() == null) {
				return;
			}
			JFileChooser chooser = new JFileChooser();
			chooser.setSelectedFile(new File(getSelectedTheme().getName() + ".xml"));
			if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
				try {
					getThemeLibrary().exportTheme(getSelectedTheme(), chooser.getSelectedFile());
				} catch (IOException e) {
					logger.log(Level.WARNING, "Cannot export FML editor theme to " + chooser.getSelectedFile(), e);
				}
			}
		}
	}
}
