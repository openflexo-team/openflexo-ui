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

import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.docgenerator.TADocGenerator;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.editionaction.FetchRequest;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.logging.FlexoLogger;

/**
 * Generate documentation for a {@link TechnologyAdapter}
 * 
 */
public class MDTADocGenerator<TA extends TechnologyAdapter<TA>> extends TADocGenerator<TA> {

	private static final Logger logger = FlexoLogger.getLogger(MDTADocGenerator.class.getPackage().getName());

	/*private Class<? extends TechnologyAdapter<?>> taClass;
	// private String fullPath;
	// private String globalPath;
	// private String relativePath;
	private ApplicationContext applicationContext;
	private TechnologyAdapterService technologyAdapterService;
	private TechnologyAdapterControllerService taControllerService;
	private TA technologyAdapter;
	private TechnologyAdapterController<TA> technologyAdapterController;
	
	private String repositoryName;
	private String modelProjectName;
	private String mvnArtefactName;
	
	private File root;
	private File globalTADir;
	private File globalTASiteDir;
	private File globalMDDir;
	private File taDir;
	private File taSiteDir;
	private File mdDir;
	private File imageDir;
	
	private Map<Class<? extends FMLObject>, AbstractGenerator> generators;
	
	private FMLModelFactory fmlModelFactory;
	
	private StringBuffer globalMenu;
	private StringBuffer localMenu;*/

	// private static final String START_MARKER = "<!-- BEGIN GENERATED -->";
	// private static final String END_MARKER = "<!-- END GENERATED -->";

	// private static Map<TechnologyAdapter<?>, MDTADocGenerator<?>> knownTADocGenerators = new HashMap<>();

	public MDTADocGenerator(Class<TA> taClass, String repositoryName, String modelProjectName, ApplicationContext applicationContext) {

		super(taClass, repositoryName, modelProjectName, applicationContext);
		/*
		this.taClass = taClass;
		this.repositoryName = repositoryName;
		this.modelProjectName = modelProjectName;
		this.mvnArtefactName = mvnArtefactName;
		this.applicationContext = applicationContext;
		technologyAdapterService = applicationContext.getService(TechnologyAdapterService.class);
		technologyAdapter = technologyAdapterService.getTechnologyAdapter(taClass);
		
		knownTADocGenerators.put(technologyAdapter, this);
		
		// technologyAdapterService.activateTechnologyAdapter(technologyAdapter, true);
		
		taControllerService = applicationContext.getService(TechnologyAdapterControllerService.class);
		taControllerService.activateTechnology(technologyAdapter);
		technologyAdapterController = taControllerService.getTechnologyAdapterController(technologyAdapter);
		
		try {
			fmlModelFactory = new FMLModelFactory(null, applicationContext);
		} catch (ModelDefinitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		localMenu = new StringBuffer();
		
		System.out.println("Generator for " + technologyAdapter);
		String currentDir = System.getProperty("user.dir");
		File current = new File(currentDir);
		File root = current.getParentFile().getParentFile();
		globalTADir = new File(root, repositoryName);
		globalTASiteDir = new File(globalTADir, "src/site");
		globalTADir = new File(globalTASiteDir, "markdown");
		taDir = new File(root, repositoryName + "/" + modelProjectName);
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
		
		generators = new HashMap<>();
		
		for (Class<?> modelSlotClass : technologyAdapter.getAvailableModelSlotTypes()) {
			prepareDocGenerationForModelSlot((Class) modelSlotClass);
		}
		if (!(technologyAdapter instanceof FMLTechnologyAdapter)) {
			makeGlobalMenu();
			makeLocalMenu();
		}*/
	}

	/*@Override
	public <O extends FMLObject> AbstractGenerator<O> getGenerator(Class<O> objectClass) {
		return generators.get(objectClass);
	}*/

	/*public void generate() {
		System.out.println("Generate doc for " + getTechnologyAdapter());
		for (Class<? extends FMLObject> objectClass : generators.keySet()) {
			AbstractGenerator<?> generator = generators.get(objectClass);
			generator.generate();
		}
	
		// if (!(technologyAdapter instanceof FMLTechnologyAdapter)) {
		// generateGlobalMenu();
		// generateLocalMenu();
		// }
	}*/

	@Override
	public String getTemplateDirectoryRelativePath() {
		return "Templates/MD";
	}

	@Override
	protected MDModelSlotGenerator<?> makeModelSlotGenerator(Class<? extends ModelSlot<?>> modelSlotClass) {
		return new MDModelSlotGenerator<>(modelSlotClass, this);
	}

	@Override
	protected MDFlexoRoleGenerator<?> makeFlexoRoleGenerator(Class<? extends FlexoRole<?>> roleClass) {
		return new MDFlexoRoleGenerator<>(roleClass, this);
	}

	@Override
	protected MDFlexoBehaviourGenerator<?> makeFlexoBehaviourGenerator(Class<? extends FlexoBehaviour> behaviourClass) {
		return new MDFlexoBehaviourGenerator<>(behaviourClass, this);
	}

	@Override
	protected MDEditionActionGenerator<?> makeEditionActionGenerator(Class<? extends EditionAction> editionActionClass) {
		return new MDEditionActionGenerator<>(editionActionClass, this);
	}

	@Override
	protected MDFetchRequestGenerator<?> makeFetchRequestGenerator(Class<? extends FetchRequest<?, ?, ?>> fetchRequestClass) {
		return new MDFetchRequestGenerator<>(fetchRequestClass, this);
	}

	/*protected void prepareDocGenerationForModelSlot(Class<? extends ModelSlot<?>> modelSlotClass) {
		System.out.println("ModelSlot class : " + modelSlotClass);
		MDModelSlotGenerator<?> generator = new MDModelSlotGenerator<>(modelSlotClass, this);
		generators.put(modelSlotClass, generator);
		for (Class<? extends FlexoRole<?>> roleClass : getTechnologyAdapterService().getAvailableFlexoRoleTypes(modelSlotClass)) {
			prepareDocGenerationForRole(roleClass);
		}
		for (Class<? extends FlexoBehaviour> behaviourClass : getTechnologyAdapterService()
				.getAvailableFlexoBehaviourTypes(modelSlotClass)) {
			prepareDocGenerationForBehaviour(behaviourClass);
		}
		for (Class<? extends EditionAction> editionActionClass : getTechnologyAdapterService()
				.getAvailableEditionActionTypes(modelSlotClass)) {
			prepareDocGenerationForEditionAction(editionActionClass);
		}
		for (Class<? extends FetchRequest<?, ?, ?>> fetchRequestClass : getTechnologyAdapterService()
				.getAvailableFetchRequestActionTypes(modelSlotClass)) {
			prepareDocGenerationForEditionAction(fetchRequestClass);
		}
	}
	
	private void prepareDocGenerationForRole(Class<? extends FlexoRole<?>> roleClass) {
		System.out.println("  > Role: " + roleClass);
		FlexoRoleGenerator<?> generator = new FlexoRoleGenerator<>(roleClass, this);
		generators.put(roleClass, generator);
	}
	
	private void prepareDocGenerationForBehaviour(Class<? extends FlexoBehaviour> behaviourClass) {
		System.out.println("  > Behaviour: " + behaviourClass);
		FlexoBehaviourGenerator<?> generator = new FlexoBehaviourGenerator<>(behaviourClass, this);
		generators.put(behaviourClass, generator);
	}
	
	private void prepareDocGenerationForEditionAction(Class<? extends EditionAction> editionActionClass) {
		System.out.println("  > EditionAction: " + editionActionClass);
		EditionActionGenerator<?> generator = new EditionActionGenerator<>(editionActionClass, this);
		generators.put(editionActionClass, generator);
	
	}
	
	private void prepareDocGenerationForFetchRequest(Class<? extends FetchRequest<?, ?, ?>> fetchRequestClass) {
		System.out.println("  > FetchRequest: " + fetchRequestClass);
		FetchRequestGenerator<?> generator = new FetchRequestGenerator<>(fetchRequestClass, this);
		generators.put(fetchRequestClass, generator);
	
	}*/

	/*@Override
	public TA getTechnologyAdapter() {
		return technologyAdapter;
	}
	
	@Override
	public TechnologyAdapterService getTechnologyAdapterService() {
		return technologyAdapterService;
	}
	
	@Override
	public TechnologyAdapterController<TA> getTechnologyAdapterController() {
		return technologyAdapterController;
	}
	
	@Override
	public String getRelativePath() {
		return mvnArtefactName;
	}
	
	@Override
	public File getMDDir() {
		return mdDir;
	}
	
	@Override
	public File getImageDir() {
		return imageDir;
	}
	
	public String getMVNArtefactName() {
		return mvnArtefactName;
	}
	
	@Override
	public FMLModelFactory getFMLModelFactory() {
		return fmlModelFactory;
	}
	
	private FMLEntity<?> getFMLEntityForModelSlotClass(Class<? extends ModelSlot<?>> modelSlotClass) {
		return FMLModelContext.getFMLEntity(modelSlotClass, getFMLModelFactory());
	}
	
	private String getFMLKeywordForModelSlotClass(Class<? extends ModelSlot<?>> modelSlotClass) {
		return getFMLEntityForModelSlotClass(modelSlotClass).getFmlAnnotation().value();
	}*/

	/*private void makeGlobalModelSlotMenu(Class<? extends ModelSlot<?>> modelSlotClass) {
		AbstractGenerator<? extends ModelSlot<?>> generator = getGenerator(modelSlotClass);
		globalMenu.append("<menu name=\"" + getFMLKeywordForModelSlotClass(modelSlotClass) + "\">" + StringUtils.LINE_SEPARATOR);
	
		// Roles
		if (technologyAdapterService.getAvailableFlexoRoleTypes(modelSlotClass).size() > 0) {
			globalMenu
					.append("    <item name=\"Roles\" href=\"" + generator.getGlobalRolesReference() + "\">" + StringUtils.LINE_SEPARATOR);
			for (Class<? extends FlexoRole<?>> roleClass : technologyAdapterService.getAvailableFlexoRoleTypes(modelSlotClass)) {
				AbstractGenerator<? extends FlexoRole<?>> roleGenerator = getGenerator(roleClass);
				globalMenu.append("        <item name=\"" + roleClass.getSimpleName() + "\" href=\"" + roleGenerator.getGlobalReference()
						+ "\"/>" + StringUtils.LINE_SEPARATOR);
			}
			globalMenu.append("    </item>" + StringUtils.LINE_SEPARATOR);
		}
	
		// Behaviours
		if (technologyAdapterService.getAvailableFlexoBehaviourTypes(modelSlotClass).size() > 0) {
			globalMenu.append("    <item name=\"Behaviours\" href=\"" + generator.getGlobalBehavioursReference() + "\">"
					+ StringUtils.LINE_SEPARATOR);
			for (Class<? extends FlexoBehaviour> behaviourClass : technologyAdapterService
					.getAvailableFlexoBehaviourTypes(modelSlotClass)) {
				AbstractGenerator<? extends FlexoBehaviour> behaviourGenerator = getGenerator(behaviourClass);
				globalMenu.append("        <item name=\"" + behaviourClass.getSimpleName() + "\" href=\""
						+ behaviourGenerator.getGlobalReference() + "\"/>" + StringUtils.LINE_SEPARATOR);
			}
			globalMenu.append("    </item>" + StringUtils.LINE_SEPARATOR);
		}
	
		// EditionAction
		if (technologyAdapterService.getAvailableEditionActionTypes(modelSlotClass).size() > 0) {
			globalMenu.append("    <item name=\"Edition actions\" href=\"" + generator.getGlobalEditionActionsReference() + "\">"
					+ StringUtils.LINE_SEPARATOR);
			for (Class<? extends EditionAction> eaClass : technologyAdapterService.getAvailableEditionActionTypes(modelSlotClass)) {
				AbstractGenerator<? extends EditionAction> eaGenerator = getGenerator(eaClass);
				globalMenu.append("        <item name=\"" + eaClass.getSimpleName() + "\" href=\"" + eaGenerator.getGlobalReference()
						+ "\"/>" + StringUtils.LINE_SEPARATOR);
			}
			globalMenu.append("    </item>" + StringUtils.LINE_SEPARATOR);
		}
	
		// FetchRequests
		if (technologyAdapterService.getAvailableFetchRequestActionTypes(modelSlotClass).size() > 0) {
			globalMenu.append("    <item name=\"Fetch requests\" href=\"" + generator.getGlobalFetchRequestsReference() + "\">"
					+ StringUtils.LINE_SEPARATOR);
			for (Class<? extends FetchRequest<?, ?, ?>> eaClass : technologyAdapterService
					.getAvailableFetchRequestActionTypes(modelSlotClass)) {
				AbstractGenerator<? extends FetchRequest<?, ?, ?>> eaGenerator = getGenerator(eaClass);
				globalMenu.append("        <item name=\"" + eaClass.getSimpleName() + "\" href=\"" + eaGenerator.getGlobalReference()
						+ "\"/>" + StringUtils.LINE_SEPARATOR);
			}
			globalMenu.append("    </item>" + StringUtils.LINE_SEPARATOR);
		}
	
		globalMenu.append("</menu>" + StringUtils.LINE_SEPARATOR);
		globalMenu.append(StringUtils.LINE_SEPARATOR);
	}
	
	private String makeGlobalMenu() {
		globalMenu = new StringBuffer();
	
		globalMenu.append("<menu name=\"FML / Usage\">" + StringUtils.LINE_SEPARATOR);
	
		if (technologyAdapter instanceof FMLRTTechnologyAdapter) {
			// Special case: we generate doc for FML and FML@RT in the same maven site
			for (Class<? extends ModelSlot<?>> modelSlotClass : technologyAdapter.getAvailableModelSlotTypes()) {
				AbstractGenerator<? extends ModelSlot<?>> generator = getGenerator(modelSlotClass);
				globalMenu.append("    <item name=\"" + getFMLKeywordForModelSlotClass(modelSlotClass) + "\" href=\""
						+ generator.getGlobalReference() + "\"/>" + StringUtils.LINE_SEPARATOR);
			}
			FMLTechnologyAdapter fmlTA = technologyAdapterService.getTechnologyAdapter(FMLTechnologyAdapter.class);
			for (Class<? extends ModelSlot<?>> modelSlotClass : fmlTA.getAvailableModelSlotTypes()) {
				AbstractGenerator<? extends ModelSlot<?>> generator = knownTADocGenerators.get(fmlTA).getGenerator(modelSlotClass);
				globalMenu.append("    <item name=\"" + getFMLKeywordForModelSlotClass(modelSlotClass) + "\" href=\""
						+ generator.getGlobalReference() + "\"/>" + StringUtils.LINE_SEPARATOR);
			}
		}
		else {
			for (Class<? extends ModelSlot<?>> modelSlotClass : technologyAdapter.getAvailableModelSlotTypes()) {
				AbstractGenerator<? extends ModelSlot<?>> generator = getGenerator(modelSlotClass);
				globalMenu.append("    <item name=\"" + getFMLKeywordForModelSlotClass(modelSlotClass) + "\" href=\""
						+ generator.getGlobalReference() + "\"/>" + StringUtils.LINE_SEPARATOR);
			}
		}
		globalMenu.append("</menu>" + StringUtils.LINE_SEPARATOR);
		globalMenu.append(StringUtils.LINE_SEPARATOR);
	
		for (Class<? extends ModelSlot<?>> modelSlotClass : technologyAdapter.getAvailableModelSlotTypes()) {
			makeGlobalModelSlotMenu(modelSlotClass);
		}
		System.out.println(globalMenu.toString());
		return globalMenu.toString();
	}*/

	// private void generateGlobalMenu() {
	// File globalMenuFile = new File(globalTASiteDir, "site.xml");
	// generateMenu(globalMenu.toString(), globalMenuFile);
	// System.out.println("Generated " + globalMenuFile);
	//
	// }

	/*private void makeLocalModelSlotMenu(Class<? extends ModelSlot<?>> modelSlotClass) {
		AbstractGenerator<? extends ModelSlot<?>> generator = getGenerator(modelSlotClass);
		localMenu.append("<menu name=\"" + getFMLKeywordForModelSlotClass(modelSlotClass) + "\">" + StringUtils.LINE_SEPARATOR);
	
		// Roles
		if (technologyAdapterService.getAvailableFlexoRoleTypes(modelSlotClass).size() > 0) {
			localMenu.append("    <item name=\"Roles\" href=\"" + generator.getGlobalRolesReference() + "\">" + StringUtils.LINE_SEPARATOR);
			for (Class<? extends FlexoRole<?>> roleClass : technologyAdapterService.getAvailableFlexoRoleTypes(modelSlotClass)) {
				AbstractGenerator<? extends FlexoRole<?>> roleGenerator = getGenerator(roleClass);
				localMenu.append("        <item name=\"" + roleClass.getSimpleName() + "\" href=\"" + roleGenerator.getLocalReference()
						+ "\"/>" + StringUtils.LINE_SEPARATOR);
			}
			localMenu.append("    </item>" + StringUtils.LINE_SEPARATOR);
		}
	
		// Behaviours
		if (technologyAdapterService.getAvailableFlexoBehaviourTypes(modelSlotClass).size() > 0) {
			localMenu.append("    <item name=\"Behaviours\" href=\"" + generator.getGlobalBehavioursReference() + "\">"
					+ StringUtils.LINE_SEPARATOR);
			for (Class<? extends FlexoBehaviour> behaviourClass : technologyAdapterService
					.getAvailableFlexoBehaviourTypes(modelSlotClass)) {
				AbstractGenerator<? extends FlexoBehaviour> behaviourGenerator = getGenerator(behaviourClass);
				localMenu.append("        <item name=\"" + behaviourClass.getSimpleName() + "\" href=\""
						+ behaviourGenerator.getLocalReference() + "\"/>" + StringUtils.LINE_SEPARATOR);
			}
			localMenu.append("    </item>" + StringUtils.LINE_SEPARATOR);
		}
	
		// EditionAction
		if (technologyAdapterService.getAvailableEditionActionTypes(modelSlotClass).size() > 0) {
			localMenu.append("    <item name=\"Edition actions\" href=\"" + generator.getGlobalEditionActionsReference() + "\">"
					+ StringUtils.LINE_SEPARATOR);
			for (Class<? extends EditionAction> eaClass : technologyAdapterService.getAvailableEditionActionTypes(modelSlotClass)) {
				AbstractGenerator<? extends EditionAction> eaGenerator = getGenerator(eaClass);
				localMenu.append("        <item name=\"" + eaClass.getSimpleName() + "\" href=\"" + eaGenerator.getLocalReference() + "\"/>"
						+ StringUtils.LINE_SEPARATOR);
			}
			localMenu.append("    </item>" + StringUtils.LINE_SEPARATOR);
		}
	
		// FetchRequests
		if (technologyAdapterService.getAvailableFetchRequestActionTypes(modelSlotClass).size() > 0) {
			localMenu.append("    <item name=\"Fetch requests\" href=\"" + generator.getGlobalFetchRequestsReference() + "\">"
					+ StringUtils.LINE_SEPARATOR);
			for (Class<? extends FetchRequest<?, ?, ?>> eaClass : technologyAdapterService
					.getAvailableFetchRequestActionTypes(modelSlotClass)) {
				AbstractGenerator<? extends FetchRequest<?, ?, ?>> eaGenerator = getGenerator(eaClass);
				localMenu.append("        <item name=\"" + eaClass.getSimpleName() + "\" href=\"" + eaGenerator.getLocalReference() + "\"/>"
						+ StringUtils.LINE_SEPARATOR);
			}
			localMenu.append("    </item>" + StringUtils.LINE_SEPARATOR);
		}
	
		localMenu.append("</menu>" + StringUtils.LINE_SEPARATOR);
		localMenu.append(StringUtils.LINE_SEPARATOR);
	}
	
	private String makeLocalMenu() {
		localMenu = new StringBuffer();
	
		localMenu.append("<menu name=\"FML / Usage\">" + StringUtils.LINE_SEPARATOR);
	
		if (technologyAdapter instanceof FMLRTTechnologyAdapter) {
			// Special case: we generate doc for FML and FML@RT in the same maven site
			for (Class<? extends ModelSlot<?>> modelSlotClass : technologyAdapter.getAvailableModelSlotTypes()) {
				AbstractGenerator<? extends ModelSlot<?>> generator = getGenerator(modelSlotClass);
				localMenu.append("    <item name=\"" + getFMLKeywordForModelSlotClass(modelSlotClass) + "\" href=\""
						+ generator.getLocalReference() + "\"/>" + StringUtils.LINE_SEPARATOR);
			}
			FMLTechnologyAdapter fmlTA = technologyAdapterService.getTechnologyAdapter(FMLTechnologyAdapter.class);
			for (Class<? extends ModelSlot<?>> modelSlotClass : fmlTA.getAvailableModelSlotTypes()) {
				AbstractGenerator<? extends ModelSlot<?>> generator = knownTADocGenerators.get(fmlTA).getGenerator(modelSlotClass);
				localMenu.append("    <item name=\"" + getFMLKeywordForModelSlotClass(modelSlotClass) + "\" href=\""
						+ generator.getLocalReference() + "\"/>" + StringUtils.LINE_SEPARATOR);
			}
		}
		else {
			for (Class<? extends ModelSlot<?>> modelSlotClass : technologyAdapter.getAvailableModelSlotTypes()) {
				AbstractGenerator<? extends ModelSlot<?>> generator = getGenerator(modelSlotClass);
				localMenu.append("    <item name=\"" + getFMLKeywordForModelSlotClass(modelSlotClass) + "\" href=\""
						+ generator.getLocalReference() + "\"/>" + StringUtils.LINE_SEPARATOR);
			}
		}
		localMenu.append("</menu>" + StringUtils.LINE_SEPARATOR);
		localMenu.append(StringUtils.LINE_SEPARATOR);
	
		for (Class<? extends ModelSlot<?>> modelSlotClass : technologyAdapter.getAvailableModelSlotTypes()) {
			makeLocalModelSlotMenu(modelSlotClass);
		}
	
		System.out.println(localMenu.toString());
		return localMenu.toString();
	}
	
	// private void generateLocalMenu() {
	// File localMenuFile = new File(taSiteDir, "site.xml");
	// generateMenu(localMenu.toString(), localMenuFile);
	// System.out.println("Generated " + localMenuFile);
	//
	// }
	
	private void generateMenu(String menuContents, File file) {
		try {
			String currentContents = FileUtils.fileContents(file);
			int firstIndex = currentContents.indexOf(START_MARKER);
			int endIndex = currentContents.indexOf(END_MARKER);
			if (firstIndex > -1 && endIndex > -1) {
				StringBuffer generatedMenu = new StringBuffer();
				generatedMenu.append(currentContents.substring(0, firstIndex));
				generatedMenu.append(START_MARKER + StringUtils.LINE_SEPARATOR);
				generatedMenu.append(menuContents);
				generatedMenu.append(END_MARKER + StringUtils.LINE_SEPARATOR);
				generatedMenu.append(currentContents.substring(endIndex + END_MARKER.length() + 1));
				FileUtils.saveToFile(file, generatedMenu.toString());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}*/

}
