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

package org.openflexo.docgenerator.latex;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.docgenerator.AbstractGenerator;
import org.openflexo.docgenerator.TechnologyAdapterGenerator;
import org.openflexo.docgenerator.VelocityMasterGenerator;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.editionaction.FetchRequest;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.toolbox.FileUtils;
import org.openflexo.toolbox.HTMLUtils;

/**
 * HTML documentation generator for a dedicated {@link TechnologyAdapter}
 * 
 */
public class LatexMasterGenerator<TA extends TechnologyAdapter<TA>> extends VelocityMasterGenerator<TA> {

	private static final Logger logger = FlexoLogger.getLogger(LatexMasterGenerator.class.getPackage().getName());

	private File latexDocDirectory;
	private File imageDir;

	public LatexMasterGenerator(Class<TA> taClass, String repositoryName, String mainProjectName, File latexDocDirectory,
			ApplicationContext applicationContext) {
		super(taClass, repositoryName, mainProjectName, applicationContext);
		this.latexDocDirectory = latexDocDirectory;
		logger.info("Will generate Latex in : " + latexDocDirectory.getAbsolutePath());
	}

	@Override
	protected void initFilePaths() {
		super.initFilePaths();

		/*latexDocDirectory = new File(getTADir(), "src/main/resources/Documentation/" + getTechnologyAdapter().getIdentifier() + "/Latex");
		if (!latexDocDirectory.exists()) {
			latexDocDirectory.mkdirs();
		}*/

		imageDir = new File(getTASiteDir(), "resources/images");
		logger.info("Images will be found in =" + imageDir.getAbsolutePath());
	}

	public File getLatexDocDirectory() {
		return latexDocDirectory;
	}

	@Override
	public String getTemplateDirectoryRelativePath() {
		return "Templates/Latex";
	}

	@Override
	public void generate() {
		System.out.println("Copying files from " + imageDir.getAbsolutePath() + " to " + latexDocDirectory.getAbsolutePath());
		try {
			FileUtils.copyContentDirToDir(imageDir, new File(latexDocDirectory, "resources/images"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.generate();
	}

	public String toLatex(String text) {
		return HTMLUtils.htmlToLatex(text);
	}

	@Override
	public List<File> getFilesToBeGenerated(AbstractGenerator<?> generator) {

		if (generator instanceof LatexTechnologyAdapterGenerator) {
			String identifier = getTechnologyAdapter().getIdentifier();
			return Collections.singletonList(new File(getLatexDocDirectory(), identifier + "_main.tex"));
		}
		return Collections.singletonList(new File(getLatexDocDirectory(), generator.getObjectClass().getSimpleName() + ".tex"));
	}

	@Override
	protected TechnologyAdapterGenerator<TA> makeTechnologyAdapterGenerator(Class<TA> taClass) {
		return new LatexTechnologyAdapterGenerator<>(taClass, this);
	}

	@Override
	protected <MS extends ModelSlot<?>> LatexModelSlotGenerator<MS> makeModelSlotGenerator(Class<MS> modelSlotClass) {
		return new LatexModelSlotGenerator<>(modelSlotClass, this);
	}

	@Override
	protected <R extends FlexoRole<?>> LatexFlexoRoleGenerator<R> makeFlexoRoleGenerator(Class<R> roleClass) {
		return new LatexFlexoRoleGenerator<>(roleClass, this);
	}

	@Override
	protected <B extends FlexoBehaviour> LatexFlexoBehaviourGenerator<B> makeFlexoBehaviourGenerator(Class<B> behaviourClass) {
		return new LatexFlexoBehaviourGenerator<>(behaviourClass, this);
	}

	@Override
	protected <EA extends EditionAction> LatexEditionActionGenerator<EA> makeEditionActionGenerator(Class<EA> editionActionClass) {
		return new LatexEditionActionGenerator<>(editionActionClass, this);
	}

	@Override
	protected <FR extends FetchRequest<?, ?, ?>> LatexFetchRequestGenerator<FR> makeFetchRequestGenerator(Class<FR> fetchRequestClass) {
		return new LatexFetchRequestGenerator<>(fetchRequestClass, this);
	}

	/*private Map<Class<?>, File> smallIconFiles = new HashMap<>();
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
	
	public String getSmallIconReference(Class<?> objectClass) {
		File smallIconFile = getSmallIconFile(objectClass);
		if (!smallIconFile.exists()) {
			logger.warning("File not found : " + smallIconFile);
			return "???";
		}
		return "<img src=\"" + "/" + getRepositoryName() + "/images/" + smallIconFile.getName() + "\" alt=\"" + objectClass.getSimpleName()
				+ "\"/>";
	}
	
	public String getBigIconReference(Class<?> objectClass) {
		// 	\includegraphics[width=0.5cm]{TA/DIAGRAM/resources/images/TypedDiagramModelSlot.png} & 
	
		
		return "\\includegraphics[width=0.5cm]{TA/DIAGRAM/resources/images/TypedDiagramModelSlot.png} & \n"
				+ "
		
		
		File bigIconFile = getBigIconFile(objectClass);
		if (!bigIconFile.exists()) {
			return "";
		}
		return "<img src=\"" + "/" + getRepositoryName() + "/images/" + bigIconFile.getName() + "\" alt=\"" + objectClass.getSimpleName()
				+ "\"/>";
	}
	
	public String getJavadocReference(Class<?> objectClass) {
		return "Prout la javadoc en HTML";
	}*/
}
