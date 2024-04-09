/**
 * 
 * Copyright (c) 2014-2015, Openflexo
 * 
 * This file is part of Flexo-foundation, a component of the software infrastructure 
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

package org.openflexo.docgenerator.md;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.docgenerator.AbstractGenerator;
import org.openflexo.docgenerator.VelocityMasterGenerator;
import org.openflexo.docgenerator.icongenerator.IconGenerator;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.editionaction.FetchRequest;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.toolbox.StringUtils;

/**
 * Generate documentation for a {@link TechnologyAdapter}
 * 
 */
public class MDTADocGenerator<TA extends TechnologyAdapter<TA>> extends VelocityMasterGenerator<TA> {

	private static final Logger logger = FlexoLogger.getLogger(MDTADocGenerator.class.getPackage().getName());

	private File mdDir;
	private File imageDir;

	public MDTADocGenerator(Class<TA> taClass, String repositoryName, String modelProjectName, ApplicationContext applicationContext) {

		super(taClass, repositoryName, modelProjectName, applicationContext);
	}

	@Override
	protected void initFilePaths() {
		super.initFilePaths();
		mdDir = new File(getTASiteDir(), "markdown");
		System.out.println("mdDir=" + mdDir.getAbsolutePath() + " exists=" + mdDir.exists());
		imageDir = new File(getTASiteDir(), "resources/images");
		System.out.println("imageDir=" + imageDir.getAbsolutePath() + " exists=" + imageDir.exists());
	}

	@Override
	public File getFileToBeGenerated(AbstractGenerator<?> generator) {
		return new File(getMDDir(), generator.getObjectClass().getSimpleName() + ".md");
	}

	public File getMDDir() {
		return mdDir;
	}

	@Override
	public String getTemplateDirectoryRelativePath() {
		return "Templates/MD";
	}

	@Override
	protected <MS extends ModelSlot<?>> MDModelSlotGenerator<MS> makeModelSlotGenerator(Class<MS> modelSlotClass) {
		return new MDModelSlotGenerator<>(modelSlotClass, this);
	}

	@Override
	protected <R extends FlexoRole<?>> MDFlexoRoleGenerator<R> makeFlexoRoleGenerator(Class<R> roleClass) {
		return new MDFlexoRoleGenerator<>(roleClass, this);
	}

	@Override
	protected <B extends FlexoBehaviour> MDFlexoBehaviourGenerator<B> makeFlexoBehaviourGenerator(Class<B> behaviourClass) {
		return new MDFlexoBehaviourGenerator<>(behaviourClass, this);
	}

	@Override
	protected <EA extends EditionAction> MDEditionActionGenerator<EA> makeEditionActionGenerator(Class<EA> editionActionClass) {
		return new MDEditionActionGenerator<>(editionActionClass, this);
	}

	@Override
	protected <FR extends FetchRequest<?, ?, ?>> MDFetchRequestGenerator<FR> makeFetchRequestGenerator(Class<FR> fetchRequestClass) {
		return new MDFetchRequestGenerator<>(fetchRequestClass, this);
	}

	public String toMD(String text) {
		String returned = text;
		if (returned.startsWith("<html>")) {
			returned = returned.substring(6);
		}
		if (returned.endsWith("</html>")) {
			returned = returned.substring(0, returned.length() - 7);
		}
		returned = returned.replace("<br>", StringUtils.LINE_SEPARATOR + StringUtils.LINE_SEPARATOR);
		return returned;
	}

	private Map<Class<? extends FMLObject>, File> smallIconFiles = new HashMap<>();
	private Map<Class<? extends FMLObject>, File> bigIconFiles = new HashMap<>();

	private File getSmallIconFile(Class<? extends FMLObject> objectClass) {
		File returned = smallIconFiles.get(objectClass);
		if (returned == null) {
			returned = new File(imageDir, IconGenerator.getSmallIconFileName(objectClass));
			smallIconFiles.put(objectClass, returned);
		}
		return returned;
	}

	private File getBigIconFile(Class<? extends FMLObject> objectClass) {
		File returned = bigIconFiles.get(objectClass);
		if (returned == null) {
			returned = new File(imageDir, IconGenerator.getBigIconFileName(objectClass));
			bigIconFiles.put(objectClass, returned);
		}
		return returned;
	}

	public String getSmallIconAsHTML(Class<? extends FMLObject> objectClass) {
		File smallIconFile = getSmallIconFile(objectClass);
		if (!smallIconFile.exists()) {
			logger.warning("File not found : " + smallIconFile);
			return "???";
		}
		return "<img src=\"" + "/" + getRepositoryName() + "/images/" + smallIconFile.getName() + "\" alt=\"" + objectClass.getSimpleName()
				+ "\"/>";
	}

	public String getBigIconAsHTML(Class<? extends FMLObject> objectClass) {
		File bigIconFile = getBigIconFile(objectClass);
		if (!bigIconFile.exists()) {
			return "";
		}
		return "<img src=\"" + "/" + getRepositoryName() + "/images/" + bigIconFile.getName() + "\" alt=\"" + objectClass.getSimpleName()
				+ "\"/>";
	}

	public String getLocalMDPath(Class<? extends FMLObject> objectClass) {
		return objectClass.getSimpleName() + ".md";
	}

}
