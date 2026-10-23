/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Flexo-ui, a component of the software infrastructure 
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

package org.openflexo.prefs;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.br.ui.BugReportPreferences;
import org.openflexo.components.PreferencesDialog;
import org.openflexo.foundation.BugReportService;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.FlexoService;
import org.openflexo.foundation.FlexoServiceImpl;
import org.openflexo.foundation.FlexoServiceManager.ServiceRegistered;
import org.openflexo.foundation.FlexoServiceManager.TechnologyAdapterHasBeenActivated;
import org.openflexo.foundation.resource.DefaultResourceCenterService.ResourceCenterAdded;
import org.openflexo.foundation.resource.DefaultResourceCenterService.ResourceCenterRemoved;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenter.ResourceCenterEntry;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.secrets.SecretsService;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.module.Module;
import org.openflexo.module.ModuleLoader;
import org.openflexo.pamela.PamelaMetaModelLibrary;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.prefs.FlexoPreferencesResource.FlexoPreferencesResourceImpl;
import org.openflexo.toolbox.HasPropertyChangeSupport;
import org.openflexo.view.FlexoFrame;
import org.openflexo.view.controller.TechnologyAdapterController;
import org.openflexo.view.controller.TechnologyAdapterControllerService;

/**
 * This service manages preferences<br>
 * 
 * Preferences are organized into logical {@link FlexoPreferences} units identified by a String identifier.
 * 
 * 
 * @author sguerin
 */
public class PreferencesService extends FlexoServiceImpl implements HasPropertyChangeSupport {

	private static final Logger logger = Logger.getLogger(PreferencesService.class.getPackage().getName());

	private FlexoPreferencesResource resource;

	@Override
	public ApplicationContext getServiceManager() {
		return (ApplicationContext) super.getServiceManager();
	}

	public FlexoPreferences getFlexoPreferences() {
		return resource.getFlexoPreferences();
	}

	public <P extends PreferencesContainer> P managePreferences(Class<P> prefClass, PreferencesContainer container) {
		if (prefClass == null) {
			return null;
		}
		P returned = getPreferences(prefClass);
		if (returned == null) {
			returned = getPreferencesFactory().newInstance(prefClass);
			initPreferences(returned);
			container.addToContents(returned);
		}
		returned.setPreferencesService(this);
		return returned;
	}

	private void initPreferences(PreferencesContainer p) {
	}

	@Override
	public String getServiceName() {
		return "PreferencesService";
	}

	@Override
	public void initialize() {
		resource = FlexoPreferencesResourceImpl.makePreferencesResource(getServiceManager());
		getFlexoPreferences().setPreferencesService(this);
		managePreferences(GeneralPreferences.class, getFlexoPreferences());
		managePreferences(PresentationPreferences.class, getFlexoPreferences());
		ApplicationContext ac = getServiceManager();
		// The pages that technologies contribute to the top of the tree come next
		if (ac.getTechnologyAdapterService() != null && ac.getTechnologyAdapterControllerService() != null) {
			for (TechnologyAdapter<?> technologyAdapter : ac.getTechnologyAdapterService().getTechnologyAdapters()) {
				initializeApplicationPreferencesForTechnologyAdapter(technologyAdapter);
			}
		}
		managePreferences(AdvancedPrefs.class, getFlexoPreferences());
		for (FlexoService service : ac.getRegisteredServices()) {
			initializePreferencesForService(service);
		}

		if (ac.getModuleLoader() != null) {
			for (Module<?> m : ac.getModuleLoader().getKnownModules()) {
				initializePreferencesForModule(m);
			}
		}

		if (ac.getTechnologyAdapterService() != null && ac.getTechnologyAdapterControllerService() != null) {
			for (TechnologyAdapter<?> technologyAdapter : ac.getTechnologyAdapterService().getTechnologyAdapters()) {
				initializePreferencesForTechnologyAdapter((TechnologyAdapter) technologyAdapter);
			}
		}

		managePreferences(LoggingPreferences.class, getFlexoPreferences());
		managePreferences(BugReportPreferences.class, getFlexoPreferences());

		status = Status.Started;

		for (ResourceCenterEntry<?> entry : getResourceCenterPreferences().getResourceCenterEntries()) {
			FlexoResourceCenter<?> rc = entry.makeResourceCenter(ac.getResourceCenterService());
			if (rc != null) {
				ac.getResourceCenterService().addToResourceCenters(rc);
			}
		}

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <S extends FlexoService> Class<? extends ServicePreferences<S>> getServicePreferencesClass(S service) {
		if (service instanceof FlexoResourceCenterService) {
			return (Class) ResourceCenterPreferences.class;
		}
		else if (service instanceof TechnologyAdapterService) {
			return (Class) TechnologyAdapterServicePreferences.class;
		}
		else if (service instanceof ModuleLoader) {
			return (Class) ModuleLoaderPreferences.class;
		}
		else if (service instanceof BugReportService) {
			return (Class) BugReportPreferences.class;
		}
		else if (service instanceof SecretsService) {
			return (Class) SecretsPreferences.class;
		}
		return null;
	}

	private <S extends FlexoService> ServicePreferences<S> initializePreferencesForService(S service) {
		Class<? extends ServicePreferences<S>> preferencesClass = getServicePreferencesClass(service);
		if (preferencesClass != null) {
			ServicePreferences<S> preferences = managePreferences(preferencesClass, getFlexoPreferences());
			if (preferences != null) {
				preferences.setService(service);
			}
			return preferences;
		}
		return null;
	}

	private ModulePreferences<?> initializePreferencesForModule(Module<?> module) {
		ModulePreferences<?> preferences = managePreferences(module.getPreferencesClass(), getModuleLoaderPreferences());
		if (preferences != null) {
			preferences.setModule((Module) module);
		}
		return preferences;
	}

	private PreferencesContainer initializeApplicationPreferencesForTechnologyAdapter(TechnologyAdapter<?> technologyAdapter) {
		TechnologyAdapterController<?> controller = getServiceManager().getTechnologyAdapterControllerService()
				.getTechnologyAdapterController((TechnologyAdapter) technologyAdapter);
		if (controller != null && controller.getApplicationPreferencesClass() != null) {
			PreferencesContainer returned = managePreferences(controller.getApplicationPreferencesClass(), getFlexoPreferences());
			// A file saved by an earlier version may hold these preferences somewhere else in the tree
			if (returned != null && returned.getContainer() != getFlexoPreferences()) {
				if (returned.getContainer() != null) {
					returned.getContainer().removeFromContents(returned);
				}
				getFlexoPreferences().addToContents(returned);
			}
			placeAfter(returned, PresentationPreferences.class);
			return returned;
		}
		return null;
	}

	/**
	 * Moves a page of the preferences tree just after the page of the given class. The pages are listed in the order of the contents of the
	 * root, which a preferences file keeps: a page found at the end of the list of a saved file would otherwise stay there.
	 */
	private void placeAfter(PreferencesContainer page, Class<? extends PreferencesContainer> anchorClass) {
		List<PreferencesContainer> order = new ArrayList<>(getFlexoPreferences().getContents());
		PreferencesContainer anchor = getFlexoPreferences().getPreferences(anchorClass);
		int anchorIndex = order.indexOf(anchor);
		if (anchorIndex < 0 || order.indexOf(page) == anchorIndex + 1) {
			return;
		}
		order.remove(page);
		order.add(order.indexOf(anchor) + 1, page);
		List<PreferencesContainer> moved = new ArrayList<>(order.subList(order.indexOf(anchor) + 1, order.size()));
		for (PreferencesContainer container : moved) {
			getFlexoPreferences().removeFromContents(container);
		}
		for (PreferencesContainer container : moved) {
			getFlexoPreferences().addToContents(container);
		}
	}

	private <TA extends TechnologyAdapter<TA>> TechnologyAdapterPreferences<TA> initializePreferencesForTechnologyAdapter(
			TA technologyAdapter) {
		TechnologyAdapterPreferences<TA> preferences = managePreferences(getTechnologyAdapterPreferencesClass(technologyAdapter),
				getTechnologyAdapterServicePreferences());
		if (preferences != null) {
			preferences.setTechnologyAdapter(technologyAdapter);
		}
		return preferences;
	}

	@Override
	public void receiveNotification(FlexoService caller, ServiceNotification notification) {
		logger.fine("PreferencesService received notification " + notification + " from " + caller);
		if (caller instanceof FlexoResourceCenterService) {
			if (notification instanceof ResourceCenterAdded && !readOnly()) {
				FlexoResourceCenter<?> addedFlexoResourceCenter = ((ResourceCenterAdded) notification).getAddedResourceCenter();
				if (!(addedFlexoResourceCenter instanceof FlexoProject)) {
					getResourceCenterPreferences().ensureResourceEntryIsPresent(addedFlexoResourceCenter.getResourceCenterEntry());
				}
				savePreferences();
			}
			else if (notification instanceof ResourceCenterRemoved) {
				getResourceCenterPreferences().ensureResourceEntryIsNoMorePresent(
						((ResourceCenterRemoved) notification).getRemovedResourceCenter().getResourceCenterEntry());
				savePreferences();
			}
		}
		if (caller instanceof TechnologyAdapterControllerService) {
			if (notification instanceof TechnologyAdapterHasBeenActivated) {
				initializePreferencesForTechnologyAdapter(((TechnologyAdapterHasBeenActivated) notification).getTechnologyAdapter());
				initializeApplicationPreferencesForTechnologyAdapter(
						((TechnologyAdapterHasBeenActivated) notification).getTechnologyAdapter());
			}
		}
		if (notification instanceof ServiceRegistered) {
			initializePreferencesForService(caller);
		}
	}

	public FlexoPreferencesFactory makePreferencesFactory(FlexoPreferencesResource resource, ApplicationContext applicationContext)
			throws ModelDefinitionException {
		List<Class<?>> classes = buildClassesListForPreferenceFactory(applicationContext);
		return new FlexoPreferencesFactory(resource,
				PamelaMetaModelLibrary.retrieveMetaModel(classes.toArray(new Class<?>[classes.size()])));
	}

	protected List<Class<?>> buildClassesListForPreferenceFactory(ApplicationContext applicationContext) {
		List<Class<?>> classes = new ArrayList<Class<?>>();
		classes.add(FlexoPreferences.class);
		classes.add(GeneralPreferences.class);
		classes.add(PresentationPreferences.class);
		classes.add(AdvancedPrefs.class);
		classes.add(ResourceCenterPreferences.class);
		classes.add(TechnologyAdapterServicePreferences.class);
		classes.add(ModuleLoaderPreferences.class);
		for (FlexoService service : getServiceManager().getRegisteredServices()) {
			Class<?> servicePreferenceClass = getServicePreferencesClass(service);
			if (servicePreferenceClass != null) {
				classes.add(servicePreferenceClass);
			}
		}
		if (applicationContext.getModuleLoader() != null) {
			for (Module<?> m : applicationContext.getModuleLoader().getKnownModules()) {
				classes.add(m.getPreferencesClass());
			}
		}
		if (applicationContext.getTechnologyAdapterService() != null) {
			for (TechnologyAdapter ta : applicationContext.getTechnologyAdapterService().getTechnologyAdapters()) {
				TechnologyAdapterController tac = applicationContext.getTechnologyAdapterControllerService()
						.getTechnologyAdapterController(ta);
				if (tac != null && tac.getPreferencesClass() != null) {
					classes.add(tac.getPreferencesClass());
				}
				if (tac != null && tac.getApplicationPreferencesClass() != null) {
					classes.add(tac.getApplicationPreferencesClass());
				}
			}
		}

		classes.add(LoggingPreferences.class);
		classes.add(BugReportPreferences.class);

		return classes;
	}

	public void applyPreferences() {
		applyPreferences(getFlexoPreferences());
	}

	private void applyPreferences(PreferencesContainer container) {
		container.applyPreferences();
		for (PreferencesContainer child : new ArrayList<>(container.getContents())) {
			applyPreferences(child);
		}
	}

	public void savePreferences() {
		try {
			resource.save();
		} catch (SaveResourceException e) {
			e.printStackTrace();
		}
	}

	public void revertToSaved() {
		System.out.println("revertToSaved() not implemented yet");
	}

	public FlexoPreferencesFactory getPreferencesFactory() {
		return resource.getFactory();
	}

	public <P extends PreferencesContainer> P getPreferences(Class<P> containerType) {
		return getFlexoPreferences().getPreferences(containerType);
	}

	public void showPreferences() {
		getPreferencesDialog().setVisible(true);
	}

	public PreferencesDialog getPreferencesDialog() {
		return PreferencesDialog.getPreferencesDialog(getServiceManager(), FlexoFrame.getActiveFrame());
	}

	public GeneralPreferences getGeneralPreferences() {
		return managePreferences(GeneralPreferences.class, getFlexoPreferences());
	}

	public PresentationPreferences getPresentationPreferences() {
		return managePreferences(PresentationPreferences.class, getFlexoPreferences());
	}

	public AdvancedPrefs getAdvancedPrefs() {
		return managePreferences(AdvancedPrefs.class, getFlexoPreferences());
	}

	public LoggingPreferences getLoggingPreferences() {
		return managePreferences(LoggingPreferences.class, getFlexoPreferences());
	}

	public BugReportPreferences getBugReportPreferences() {
		return managePreferences(BugReportPreferences.class, getFlexoPreferences());
	}

	public ResourceCenterPreferences getResourceCenterPreferences() {
		return managePreferences(ResourceCenterPreferences.class, getFlexoPreferences());
	}

	public <S extends FlexoService> ServicePreferences<S> getServicePreferences(S service) {
		Class<? extends ServicePreferences<S>> preferencesClass = getServicePreferencesClass(service);
		// System.out.println("preferencesClass for " + service + " is " + preferencesClass);
		if (preferencesClass != null) {
			ServicePreferences<S> preferences = managePreferences(preferencesClass, getFlexoPreferences());
			if (preferences != null) {
				preferences.setService(service);
			}
			return preferences;
		}
		return null;
	}

	public <TA extends TechnologyAdapter<TA>> TechnologyAdapterPreferences<TA> getTechnologyAdapterPreferences(TA technologyAdapter) {
		Class<? extends TechnologyAdapterPreferences<TA>> preferencesClass = getTechnologyAdapterPreferencesClass(technologyAdapter);
		// System.out.println("preferencesClass for " + service + " is " + preferencesClass);
		if (preferencesClass != null) {
			TechnologyAdapterPreferences<TA> preferences = managePreferences(preferencesClass, getTechnologyAdapterServicePreferences());
			if (preferences != null) {
				preferences.setTechnologyAdapter(technologyAdapter);
			}
			return preferences;
		}
		return null;

	}

	private <TA extends TechnologyAdapter<TA>> Class<? extends TechnologyAdapterPreferences<TA>> getTechnologyAdapterPreferencesClass(
			TA technologyAdapter) {
		TechnologyAdapterController<TA> technologyAdapterController = getServiceManager().getTechnologyAdapterControllerService()
				.getTechnologyAdapterController(technologyAdapter);
		if (technologyAdapterController != null) {
			return technologyAdapterController.getPreferencesClass();
		}
		return null;
	}

	public ModuleLoaderPreferences getModuleLoaderPreferences() {
		return (ModuleLoaderPreferences) getServicePreferences(getServiceManager().getModuleLoader());
	}

	public TechnologyAdapterServicePreferences getTechnologyAdapterServicePreferences() {
		return (TechnologyAdapterServicePreferences) getServicePreferences(getServiceManager().getTechnologyAdapterService());
	}

	public boolean readOnly() {
		return false;
	}
}
