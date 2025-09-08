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

import java.io.File;
import java.io.IOException;

import org.openflexo.fml.rstasupport.PackageMapNode;
import org.openflexo.fml.rstasupport.classreader.ClassFile;

/**
 * Information about a jmods folder
 * 
 * TODO: work in progress
 *
 */
public class ModulesLibraryInfo extends LibraryInfo {

	// jmods directory
	private File dir;

	public ModulesLibraryInfo(File jmodsDir) {
		this(jmodsDir, null);
	}

	public ModulesLibraryInfo(String jmodsDir) {
		this(new File(jmodsDir));
	}

	public ModulesLibraryInfo(File dir, SourceLocation sourceLoc) {
		setDirectory(dir);
		setSourceLocation(sourceLoc);
	}

	public ModulesLibraryInfo(String dir, SourceLocation sourceLoc) {
		this(new File(dir), sourceLoc);
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
	 * same class file location. Source attachment is irrelevant.
	 *
	 * @return The sort order of these two library infos.
	 */
	@Override
	public int compareTo(LibraryInfo info) {
		if (info == this) {
			return 0;
		}
		int result = -1;
		if (info instanceof ModulesLibraryInfo) {
			return dir.compareTo(((ModulesLibraryInfo) info).dir);
		}
		return result;
	}

	@Override
	public ClassFile createClassFile(String entryName) throws IOException {
		return createClassFileBulk(entryName);
	}

	@Override
	public ClassFile createClassFileBulk(String entryName) throws IOException {
		File file = new File(dir, entryName);
		if (!file.isFile()) {
			System.err.println("ERROR: Invalid class file: " + file.getAbsolutePath());
			return null;
		}
		return new ClassFile(file);
	}

	@Override
	public PackageMapNode createPackageMap() {
		PackageMapNode root = new PackageMapNode();
		getPackageMapImpl(dir, null, root);
		return root;
	}

	@Override
	public long getLastModified() {
		return dir.lastModified();
	}

	@Override
	public String getLocationAsString() {
		return dir.getAbsolutePath();
	}

	/**
	 * Does the dirty-work of finding all class files in a directory tree.
	 *
	 * @param dir
	 *            The directory to scan.
	 * @param pkg
	 *            The package name scanned so far, in the form "<code>com/company/pkgname</code>"...
	 */
	private void getPackageMapImpl(File dir, String pkg, PackageMapNode root) {

		File[] children = dir.listFiles();

		for (File child : children) {
			if (child.isFile() && child.getName().endsWith(".class")) {
				if (pkg != null) { // will be null the first time through
					// TODO: Split pkg here to prevent repeated splits
					// for performance
					root.add(pkg + "/" + child.getName());
				}
				else {
					root.add(child.getName());
				}
			}
			else if (child.isDirectory()) {
				String subpkg = pkg == null ? child.getName() : (pkg + "/" + child.getName());
				getPackageMapImpl(child, subpkg, root);
			}
		}

	}

	@Override
	public int hashCodeImpl() {
		return dir.hashCode();
	}

	/**
	 * Sets the directory containing the classes.
	 *
	 * @param dir
	 *            The directory. This cannot be <code>null</code>.
	 */
	private void setDirectory(File dir) {
		if (dir == null || !dir.isDirectory()) {
			String name = dir == null ? "null" : dir.getAbsolutePath();
			throw new IllegalArgumentException("Directory does not exist: " + name);
		}
		this.dir = dir;
	}

	/**
	 * Returns a string representation of this jar information. Useful for debugging.
	 *
	 * @return A string representation of this object.
	 */
	@Override
	public String toString() {
		return "[ModulesLibraryInfo: " + "jmods=" + dir.getAbsolutePath() + "; source=" + getSourceLocation() + "]";
	}

}
