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

package org.openflexo.docgenerator.html;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.docgenerator.AbstractGenerator;
import org.openflexo.docgenerator.TechnologyAdapterGenerator;
import org.openflexo.docgenerator.VelocityMasterGenerator;
import org.openflexo.docgenerator.icongenerator.IconGenerator;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.editionaction.FetchRequest;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.toolbox.HTMLUtils;

/**
 * HTML documentation generator for a dedicated {@link TechnologyAdapter}
 * 
 */
public class HTMLMasterGenerator<TA extends TechnologyAdapter<TA>> extends VelocityMasterGenerator<TA> {

	private static final Logger logger = FlexoLogger.getLogger(HTMLMasterGenerator.class.getPackage().getName());

	private File htmlDocDirectory;
	private File imageDir;

	public HTMLMasterGenerator(Class<TA> taClass, String repositoryName, String mainProjectName, ApplicationContext applicationContext) {
		super(taClass, repositoryName, mainProjectName, applicationContext);
	}

	@Override
	protected void initFilePaths() {
		super.initFilePaths();

		htmlDocDirectory = new File(getTADir(), "src/main/resources/Documentation/" + getTechnologyAdapter().getIdentifier() + "/HTML");
		if (!htmlDocDirectory.exists()) {
			htmlDocDirectory.mkdirs();
		}
		logger.info("Will generate HTML in : " + htmlDocDirectory.getAbsolutePath());
		imageDir = new File(getTASiteDir(), "resources/images");
		logger.info("Images will be found in =" + imageDir.getAbsolutePath());
	}

	public File getHTMLDocDirectory() {
		return htmlDocDirectory;
	}

	@Override
	public String getTemplateDirectoryRelativePath() {
		return "Templates/HTML";
	}

	@Override
	public List<File> getFilesToBeGenerated(AbstractGenerator<?> generator) {

		return Collections.singletonList(new File(getHTMLDocDirectory(), generator.getObjectClass().getSimpleName() + ".html"));
	}

	@Override
	protected TechnologyAdapterGenerator<TA> makeTechnologyAdapterGenerator(Class<TA> taClass) {
		return new HTMLTechnologyAdapterGenerator<>(taClass, this);
	}

	@Override
	protected <MS extends ModelSlot<?>> HTMLModelSlotGenerator<MS> makeModelSlotGenerator(Class<MS> modelSlotClass) {
		return new HTMLModelSlotGenerator<>(modelSlotClass, this);
	}

	@Override
	protected <R extends FlexoRole<?>> HTMLFlexoRoleGenerator<R> makeFlexoRoleGenerator(Class<R> roleClass) {
		return new HTMLFlexoRoleGenerator<>(roleClass, this);
	}

	@Override
	protected <B extends FlexoBehaviour> HTMLFlexoBehaviourGenerator<B> makeFlexoBehaviourGenerator(Class<B> behaviourClass) {
		return new HTMLFlexoBehaviourGenerator<>(behaviourClass, this);
	}

	@Override
	protected <EA extends EditionAction> HTMLEditionActionGenerator<EA> makeEditionActionGenerator(Class<EA> editionActionClass) {
		return new HTMLEditionActionGenerator<>(editionActionClass, this);
	}

	@Override
	protected <FR extends FetchRequest<?, ?, ?>> HTMLFetchRequestGenerator<FR> makeFetchRequestGenerator(Class<FR> fetchRequestClass) {
		return new HTMLFetchRequestGenerator<>(fetchRequestClass, this);
	}

	private Map<Class<?>, File> smallIconFiles = new HashMap<>();
	private Map<Class<?>, File> bigIconFiles = new HashMap<>();

	private File getSmallIconFile(Class<?> objectClass) {
		File returned = smallIconFiles.get(objectClass);
		if (returned == null) {
			returned = new File(imageDir, IconGenerator.getSmallIconFileName(objectClass));
			smallIconFiles.put(objectClass, returned);
		}
		return returned;
	}

	private File getBigIconFile(Class<?> objectClass) {
		File returned = bigIconFiles.get(objectClass);
		if (returned == null) {
			returned = new File(imageDir, IconGenerator.getBigIconFileName(objectClass));
			bigIconFiles.put(objectClass, returned);
		}
		return returned;
	}

	public String removeHTMLTags(String html) {
		return HTMLUtils.removeHTMLTags(html);
	}

	public String toHTML(String text) {
		return HTMLUtils.toHTML(removeHTMLTags(text));
	}

	public String getSmallIconAsHTML(Class<?> objectClass) {
		File smallIconFile = getSmallIconFile(objectClass);
		if (!smallIconFile.exists()) {
			logger.warning("File not found : " + smallIconFile);
			return "???";
		}
		return "<img src=\"" + "/" + getRepositoryName() + "/images/" + smallIconFile.getName() + "\" alt=\"" + objectClass.getSimpleName()
				+ "\"/>";
	}

	public String getBigIconAsHTML(Class<?> objectClass) {
		File bigIconFile = getBigIconFile(objectClass);
		if (!bigIconFile.exists()) {
			return "";
		}
		return "<img src=\"" + "/" + getRepositoryName() + "/images/" + bigIconFile.getName() + "\" alt=\"" + objectClass.getSimpleName()
				+ "\"/>";
	}

	public String getJavadocReference(Class<?> objectClass) {
		return "Prout la javadoc en HTML";
	}
}
