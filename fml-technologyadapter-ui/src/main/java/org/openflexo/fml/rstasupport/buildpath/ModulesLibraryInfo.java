/*
 * 04/21/2012
 *
 * Copyright (C) 2010 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com/rsyntaxtextarea
 *
 * This library is distributed under a modified BSD license.  See the included
 * RSTALanguageSupport.License.txt file for details.
 */
package org.openflexo.fml.rstasupport.buildpath;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.openflexo.fml.rstasupport.PackageMapNode;
import org.openflexo.fml.rstasupport.classreader.ClassFile;

/**
 * Information about the classes of a "modern" (modularized, Java 9+) JRE or JDK.
 * 
 * The classes are read from the runtime image of the JRE (the <code>jrt:/</code> file system), which every JRE and JDK holds. The
 * <code>jmods</code> directory is not used: it is only found in some JDKs (the JDK builds since Java 24 may be linked without it) and it
 * holds <code>.jmod</code> archives, not class files.
 */
public class ModulesLibraryInfo extends LibraryInfo {

	// Home of the JRE or JDK
	private File javaHome;

	// Class files by entry name (for instance "java/lang/String.class"), built on demand
	private Map<String, Path> classFiles;

	private FileSystem fileSystem;

	public ModulesLibraryInfo(File javaHome) {
		this(javaHome, null);
	}

	public ModulesLibraryInfo(String javaHome) {
		this(new File(javaHome));
	}

	public ModulesLibraryInfo(File javaHome, SourceLocation sourceLoc) {
		setJavaHome(javaHome);
		setSourceLocation(sourceLoc);
	}

	public ModulesLibraryInfo(String javaHome, SourceLocation sourceLoc) {
		this(new File(javaHome), sourceLoc);
	}

	/**
	 * Returns whether the given directory is the home of a modularized JRE or JDK (it holds a runtime image).
	 */
	public static boolean isModularJavaHome(File javaHome) {
		return new File(javaHome, "lib/modules").isFile();
	}

	@Override
	public void bulkClassFileCreationEnd() {
		// Do nothing
	}

	@Override
	public void bulkClassFileCreationStart() {
		// Do nothing
	}

	/**
	 * Compares this <code>LibraryInfo</code> to another one. Two instances of this class are only considered equal if they represent the
	 * same Java home. Source attachment is irrelevant.
	 *
	 * @return The sort order of these two library infos.
	 */
	@Override
	public int compareTo(LibraryInfo info) {
		if (info == this) {
			return 0;
		}
		if (info instanceof ModulesLibraryInfo) {
			return javaHome.compareTo(((ModulesLibraryInfo) info).javaHome);
		}
		return -1;
	}

	@Override
	public ClassFile createClassFile(String entryName) throws IOException {
		return createClassFileBulk(entryName);
	}

	@Override
	public ClassFile createClassFileBulk(String entryName) throws IOException {
		Path path = getClassFiles().get(entryName);
		if (path == null) {
			System.err.println("ERROR: Invalid class file: " + entryName);
			return null;
		}
		try (InputStream is = Files.newInputStream(path); DataInputStream in = new DataInputStream(new BufferedInputStream(is))) {
			return new ClassFile(in);
		}
	}

	@Override
	public PackageMapNode createPackageMap() throws IOException {
		PackageMapNode root = new PackageMapNode();
		for (String entryName : getClassFiles().keySet()) {
			root.add(entryName);
		}
		return root;
	}

	@Override
	public long getLastModified() {
		return new File(javaHome, "lib/modules").lastModified();
	}

	@Override
	public String getLocationAsString() {
		return javaHome.getAbsolutePath();
	}

	@Override
	public int hashCodeImpl() {
		return javaHome.hashCode();
	}

	private synchronized Map<String, Path> getClassFiles() throws IOException {
		if (classFiles == null) {
			Map<String, Path> returned = new HashMap<>();
			Path modules = getFileSystem().getPath("/modules");
			try (Stream<Path> moduleDirs = Files.list(modules)) {
				for (Path moduleDir : (Iterable<Path>) moduleDirs::iterator) {
					try (Stream<Path> files = Files.walk(moduleDir)) {
						for (Path file : (Iterable<Path>) files::iterator) {
							String name = file.getFileName() != null ? file.getFileName().toString() : "";
							if (name.endsWith(".class") && !name.equals("module-info.class")) {
								// jar-like entry name: "java/lang/String.class"
								returned.put(moduleDir.relativize(file).toString(), file);
							}
						}
					}
				}
			}
			classFiles = returned;
		}
		return classFiles;
	}

	private FileSystem getFileSystem() throws IOException {
		if (fileSystem == null) {
			File runningJavaHome = new File(System.getProperty("java.home"));
			if (runningJavaHome.getCanonicalFile().equals(javaHome.getCanonicalFile())) {
				fileSystem = FileSystems.getFileSystem(URI.create("jrt:/"));
			}
			else {
				// Another JRE: its runtime image is read through the jrt-fs.jar that it holds
				Map<String, String> env = new HashMap<>();
				env.put("java.home", javaHome.getAbsolutePath());
				fileSystem = FileSystems.newFileSystem(URI.create("jrt:/"), env);
			}
		}
		return fileSystem;
	}

	/**
	 * Sets the home of the JRE.
	 *
	 * @param javaHome
	 *            The directory. This cannot be <code>null</code>.
	 */
	private void setJavaHome(File javaHome) {
		if (javaHome == null || !javaHome.isDirectory()) {
			String name = javaHome == null ? "null" : javaHome.getAbsolutePath();
			throw new IllegalArgumentException("Directory does not exist: " + name);
		}
		this.javaHome = javaHome;
	}

	/**
	 * Returns a string representation of this library information. Useful for debugging.
	 *
	 * @return A string representation of this object.
	 */
	@Override
	public String toString() {
		return "[ModulesLibraryInfo: " + "javaHome=" + javaHome.getAbsolutePath() + "; source=" + getSourceLocation() + "]";
	}
}
