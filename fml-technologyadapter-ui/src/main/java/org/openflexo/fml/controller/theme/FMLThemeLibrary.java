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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.toolbox.HasPropertyChangeSupport;

/**
 * The themes of the FML editor: the shipped ones (read-only, bundled as resources) and the user's ones, one RSyntaxTextArea theme file each
 * in a directory.<br>
 * 
 * An edit of a user theme is written to its file at once. Notifies {@link #THEMES} when a theme is added or removed.
 * 
 * @author sylvain
 */
public class FMLThemeLibrary implements HasPropertyChangeSupport {

	private static final Logger logger = Logger.getLogger(FMLThemeLibrary.class.getPackage().getName());

	public static final String THEMES = "themes";
	/** Notified, with the theme as new value, when the content of a theme has changed */
	public static final String THEME_CHANGED = "themeChanged";

	/** The shipped themes, in the order they are listed; the first one is the default */
	public static final String[] SHIPPED_THEMES = { "default", "eclipse", "dark" };

	private static final String FILE_EXTENSION = ".xml";

	private final PropertyChangeSupport pcSupport = new PropertyChangeSupport(this);
	private final FMLThemeFactory factory;
	private final File directory;
	private final List<FMLEditorTheme> themes = new ArrayList<>();

	private final PropertyChangeListener autoSave = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			FMLEditorTheme changed = null;
			if (evt.getSource() instanceof FMLTokenStyle) {
				changed = ((FMLTokenStyle) evt.getSource()).getTheme();
			}
			else if (evt.getSource() instanceof FMLEditorTheme && !FMLEditorTheme.TOKEN_STYLES.equals(evt.getPropertyName())) {
				changed = (FMLEditorTheme) evt.getSource();
			}
			if (changed != null) {
				save(changed);
				pcSupport.firePropertyChange(THEME_CHANGED, null, changed);
			}
		}
	};

	/**
	 * @param directory
	 *            where the user's themes are stored; created on the first save
	 */
	public FMLThemeLibrary(File directory) throws ModelDefinitionException {
		this.factory = new FMLThemeFactory();
		this.directory = directory;
		for (String name : SHIPPED_THEMES) {
			try (InputStream in = ResourceLocator.locateResource("FMLThemes/" + name + FILE_EXTENSION).openInputStream()) {
				themes.add(factory.loadTheme(in, name, true));
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Cannot load shipped FML editor theme " + name, e);
			}
		}
		File[] files = directory.listFiles((dir, fileName) -> fileName.endsWith(FILE_EXTENSION));
		if (files != null) {
			java.util.Arrays.sort(files);
			for (File file : files) {
				try {
					register(readTheme(file));
				} catch (IOException e) {
					logger.log(Level.WARNING, "Ignoring invalid FML editor theme file " + file, e);
				}
			}
		}
	}

	@Override
	public PropertyChangeSupport getPropertyChangeSupport() {
		return pcSupport;
	}

	@Override
	public String getDeletedProperty() {
		return null;
	}

	public File getDirectory() {
		return directory;
	}

	public List<FMLEditorTheme> getThemes() {
		return Collections.unmodifiableList(themes);
	}

	public FMLEditorTheme getTheme(String name) {
		for (FMLEditorTheme theme : themes) {
			if (theme.getName().equals(name)) {
				return theme;
			}
		}
		return null;
	}

	public FMLEditorTheme getDefaultTheme() {
		return getTheme(SHIPPED_THEMES[0]);
	}

	/** A new theme, a copy of the default one */
	public FMLEditorTheme createTheme() {
		return duplicate(getDefaultTheme());
	}

	/** A copy of the given theme under a new name; the copy is editable and stored */
	public FMLEditorTheme duplicate(FMLEditorTheme theme) {
		FMLEditorTheme returned = factory.duplicate(theme, newName(theme.getName() + " copy"));
		register(returned);
		save(returned);
		return returned;
	}

	/**
	 * Deletes a theme and its file
	 * 
	 * @return false if the theme is a shipped one, which is never deleted
	 */
	public boolean delete(FMLEditorTheme theme) {
		if (theme == null || theme.isReadOnly() || !themes.contains(theme)) {
			return false;
		}
		theme.getPropertyChangeSupport().removePropertyChangeListener(autoSave);
		for (FMLTokenStyle style : theme.getTokenStyles()) {
			style.getPropertyChangeSupport().removePropertyChangeListener(autoSave);
		}
		int index = themes.indexOf(theme);
		themes.remove(theme);
		fileFor(theme).delete();
		pcSupport.fireIndexedPropertyChange(THEMES, index, theme, null);
		return true;
	}

	/**
	 * Renames a user theme: its file follows
	 * 
	 * @return false if the theme is a shipped one, or if the name is empty or already used
	 */
	public boolean rename(FMLEditorTheme theme, String name) {
		if (theme.isReadOnly() || name == null || name.trim().isEmpty() || getTheme(name) != null || !isValidFileName(name)) {
			return false;
		}
		fileFor(theme).delete();
		theme.setName(name);
		save(theme);
		return true;
	}

	/** Adds the theme stored in the given RSyntaxTextArea theme file, as a user theme named after the file */
	public FMLEditorTheme importTheme(File file) throws IOException {
		String baseName = file.getName().endsWith(FILE_EXTENSION) ? file.getName().substring(0, file.getName().length() - 4)
				: file.getName();
		FMLEditorTheme returned = readTheme(file);
		returned.setName(newName(baseName));
		register(returned);
		save(returned);
		return returned;
	}

	public void exportTheme(FMLEditorTheme theme, File file) throws IOException {
		try (OutputStream out = new FileOutputStream(file)) {
			theme.save(out);
		}
	}

	private FMLEditorTheme readTheme(File file) throws IOException {
		try (InputStream in = new FileInputStream(file)) {
			String name = file.getName().endsWith(FILE_EXTENSION) ? file.getName().substring(0, file.getName().length() - 4)
					: file.getName();
			return factory.loadTheme(in, name, false);
		}
	}

	private void register(FMLEditorTheme theme) {
		// A user theme cannot take the name of another one
		theme.setName(newName(theme.getName()));
		themes.add(theme);
		theme.getPropertyChangeSupport().addPropertyChangeListener(autoSave);
		for (FMLTokenStyle style : theme.getTokenStyles()) {
			style.getPropertyChangeSupport().addPropertyChangeListener(autoSave);
		}
		pcSupport.fireIndexedPropertyChange(THEMES, themes.size() - 1, null, theme);
	}

	private String newName(String wanted) {
		String returned = isValidFileName(wanted) ? wanted.trim() : "theme";
		if (getTheme(returned) == null) {
			return returned;
		}
		int i = 2;
		while (getTheme(returned + " " + i) != null) {
			i++;
		}
		return returned + " " + i;
	}

	private static boolean isValidFileName(String name) {
		return name != null && !name.trim().isEmpty() && name.indexOf('/') < 0 && name.indexOf('\\') < 0 && !name.startsWith(".");
	}

	private File fileFor(FMLEditorTheme theme) {
		return new File(directory, theme.getName() + FILE_EXTENSION);
	}

	private void save(FMLEditorTheme theme) {
		if (theme == null || theme.isReadOnly()) {
			return;
		}
		directory.mkdirs();
		try {
			exportTheme(theme, fileFor(theme));
		} catch (IOException e) {
			logger.log(Level.WARNING, "Cannot save FML editor theme " + theme.getName(), e);
		}
	}
}
