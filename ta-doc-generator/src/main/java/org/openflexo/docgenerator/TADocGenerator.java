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

package org.openflexo.docgenerator;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.velocity.app.VelocityEngine;
import org.openflexo.ApplicationContext;
import org.openflexo.foundation.fml.FMLModelContext;
import org.openflexo.foundation.fml.FMLModelContext.FMLEntity;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.editionaction.FetchRequest;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.rm.FileResourceImpl;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.view.controller.TechnologyAdapterController;
import org.openflexo.view.controller.TechnologyAdapterControllerService;

/**
 * Abstract representation of a document generator dedicated to a {@link TechnologyAdapter}
 * 
 */
public abstract class TADocGenerator<TA extends TechnologyAdapter<TA>> {

	private static final Logger logger = FlexoLogger.getLogger(TADocGenerator.class.getPackage().getName());

	private Class<? extends TechnologyAdapter<?>> taClass;
	private ApplicationContext applicationContext;
	private TechnologyAdapterService technologyAdapterService;
	private TechnologyAdapterControllerService taControllerService;
	private TA technologyAdapter;
	private TechnologyAdapterController<TA> technologyAdapterController;

	protected Map<Class<? extends FMLObject>, AbstractGenerator> generators;

	private FMLModelFactory fmlModelFactory;

	// Local name of repository in which the TA is located
	private String repositoryName;
	// Name of the project in which the FML declarations (ModelSlot) are defined
	private String mainProjectName;
	// private String mvnArtefactName;

	private File globalTADir;
	private File globalTASiteDir;
	private File globalMDDir;
	private File taDir;
	private File taSiteDir;
	private File mdDir;
	private File imageDir;

	public TADocGenerator(Class<TA> taClass, String repositoryName, String mainProjectName, ApplicationContext applicationContext) {
		this.taClass = taClass;
		this.applicationContext = applicationContext;
		this.repositoryName = repositoryName;
		this.mainProjectName = mainProjectName;
		// this.mvnArtefactName = mvnArtefactName;
		technologyAdapterService = applicationContext.getService(TechnologyAdapterService.class);
		technologyAdapter = technologyAdapterService.getTechnologyAdapter(taClass);

		taControllerService = applicationContext.getService(TechnologyAdapterControllerService.class);
		taControllerService.activateTechnology(technologyAdapter);
		technologyAdapterController = taControllerService.getTechnologyAdapterController(technologyAdapter);

		try {
			fmlModelFactory = new FMLModelFactory(null, applicationContext);
		} catch (ModelDefinitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Generator for " + technologyAdapter);
		String currentDir = System.getProperty("user.dir");
		File current = new File(currentDir);
		File root = current.getParentFile().getParentFile();
		globalTADir = new File(root, repositoryName);
		globalTASiteDir = new File(globalTADir, "src/site");
		globalTADir = new File(globalTASiteDir, "markdown");
		taDir = new File(root, repositoryName + "/" + mainProjectName);
		if (!taDir.exists()) {
			System.out.println("Ca existe pas: " + taDir.getAbsolutePath());
			System.exit(-1);
		}
		taSiteDir = new File(taDir, "src/site");
		mdDir = new File(taSiteDir, "markdown");
		imageDir = new File(taSiteDir, "resources/images");
		System.out.println("taDir=" + taDir.getAbsolutePath() + " exists=" + taDir.exists());
		System.out.println("taSiteDir=" + taSiteDir.getAbsolutePath() + " exists=" + taSiteDir.exists());
		System.out.println("mdDir=" + mdDir.getAbsolutePath() + " exists=" + mdDir.exists());

		velocityEngine = new VelocityEngine();
		Resource templateResource = ResourceLocator.locateResource(getTemplateDirectoryRelativePath());
		if (templateResource instanceof FileResourceImpl) {
			velocityEngine.setProperty("file.resource.loader.path", ((FileResourceImpl) templateResource).getFile().getAbsolutePath());
		}

		generators = new HashMap<>();

		for (Class<?> modelSlotClass : technologyAdapter.getAvailableModelSlotTypes()) {
			prepareDocGenerationForModelSlot((Class) modelSlotClass);
		}

	}

	private VelocityEngine velocityEngine;

	public abstract String getTemplateDirectoryRelativePath();

	public VelocityEngine getVelocityEngine() {
		return velocityEngine;
	}

	public <O extends FMLObject> AbstractGenerator<O> getGenerator(Class<O> objectClass) {
		return generators.get(objectClass);
	}

	public TA getTechnologyAdapter() {
		return technologyAdapter;
	}

	public TechnologyAdapterService getTechnologyAdapterService() {
		return technologyAdapterService;
	}

	public TechnologyAdapterController<TA> getTechnologyAdapterController() {
		return technologyAdapterController;
	}

	public String getRelativePath() {
		return mainProjectName;
	}

	public File getMDDir() {
		return mdDir;
	}

	public File getImageDir() {
		return imageDir;
	}

	/*public String getMVNArtefactName() {
		return mvnArtefactName;
	}*/

	public FMLModelFactory getFMLModelFactory() {
		return fmlModelFactory;
	}

	private FMLEntity<?> getFMLEntityForModelSlotClass(Class<? extends ModelSlot<?>> modelSlotClass) {
		return FMLModelContext.getFMLEntity(modelSlotClass, getFMLModelFactory());
	}

	private String getFMLKeywordForModelSlotClass(Class<? extends ModelSlot<?>> modelSlotClass) {
		return getFMLEntityForModelSlotClass(modelSlotClass).getFmlAnnotation().value();
	}

	public void generate() {
		System.out.println("Generate doc for " + technologyAdapter);
		for (Class<? extends FMLObject> objectClass : generators.keySet()) {
			AbstractGenerator<?> generator = generators.get(objectClass);
			generator.generate();
		}

		// if (!(technologyAdapter instanceof FMLTechnologyAdapter)) {
		// generateGlobalMenu();
		// generateLocalMenu();
		// }
	}

	private void prepareDocGenerationForModelSlot(Class<? extends ModelSlot<?>> modelSlotClass) {
		System.out.println("ModelSlot class : " + modelSlotClass);
		ModelSlotGenerator<?> generator = makeModelSlotGenerator(modelSlotClass);
		generators.put(modelSlotClass, generator);
		for (Class<? extends FlexoRole<?>> roleClass : technologyAdapterService.getAvailableFlexoRoleTypes(modelSlotClass)) {
			prepareDocGenerationForRole(roleClass);
		}
		for (Class<? extends FlexoBehaviour> behaviourClass : technologyAdapterService.getAvailableFlexoBehaviourTypes(modelSlotClass)) {
			prepareDocGenerationForBehaviour(behaviourClass);
		}
		for (Class<? extends EditionAction> editionActionClass : technologyAdapterService.getAvailableEditionActionTypes(modelSlotClass)) {
			prepareDocGenerationForEditionAction(editionActionClass);
		}
		for (Class<? extends FetchRequest<?, ?, ?>> fetchRequestClass : technologyAdapterService
				.getAvailableFetchRequestActionTypes(modelSlotClass)) {
			prepareDocGenerationForFetchRequest(fetchRequestClass);
		}
	}

	private void prepareDocGenerationForRole(Class<? extends FlexoRole<?>> roleClass) {
		System.out.println("  > Role: " + roleClass);
		FlexoRoleGenerator<?> generator = makeFlexoRoleGenerator(roleClass);
		generators.put(roleClass, generator);
	}

	private void prepareDocGenerationForBehaviour(Class<? extends FlexoBehaviour> behaviourClass) {
		System.out.println("  > Behaviour: " + behaviourClass);
		FlexoBehaviourGenerator<?> generator = makeFlexoBehaviourGenerator(behaviourClass);
		generators.put(behaviourClass, generator);
	}

	private void prepareDocGenerationForEditionAction(Class<? extends EditionAction> editionActionClass) {
		System.out.println("  > EditionAction: " + editionActionClass);
		EditionActionGenerator<?> generator = makeEditionActionGenerator(editionActionClass);
		generators.put(editionActionClass, generator);

	}

	private void prepareDocGenerationForFetchRequest(Class<? extends FetchRequest<?, ?, ?>> fetchRequestClass) {
		System.out.println("  > FetchRequest: " + fetchRequestClass);
		FetchRequestGenerator<?> generator = makeFetchRequestGenerator(fetchRequestClass);
		generators.put(fetchRequestClass, generator);

	}

	protected abstract ModelSlotGenerator<?> makeModelSlotGenerator(Class<? extends ModelSlot<?>> modelSlotClass);

	protected abstract FlexoRoleGenerator<?> makeFlexoRoleGenerator(Class<? extends FlexoRole<?>> roleClass);

	protected abstract FlexoBehaviourGenerator<?> makeFlexoBehaviourGenerator(Class<? extends FlexoBehaviour> behaviourClass);

	protected abstract EditionActionGenerator<?> makeEditionActionGenerator(Class<? extends EditionAction> editionActionClass);

	protected abstract FetchRequestGenerator<?> makeFetchRequestGenerator(Class<? extends FetchRequest<?, ?, ?>> fetchRequestClass);
}
