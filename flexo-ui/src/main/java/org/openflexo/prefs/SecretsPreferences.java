/**
 *
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
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
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.foundation.secrets.SecretsService;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.prefs.PreferencesContainer.PreferencesContainerImpl;

/**
 * Preferences for the {@link SecretsService}.
 * <p>
 * Unlike most other {@link PreferencesContainer} subclasses, this one does not itself persist any secret value as a PAMELA
 * {@code @XMLAttribute}: the {@link SecretsService} is the sole source of truth and persistence for secret values (it must remain
 * usable from a headless launch, without the preferences/{@code Flexo.prefs} machinery). This panel is only a thin editor bound to
 * the service.
 */
@ModelEntity
@ImplementationClass(SecretsPreferences.SecretsPreferencesImpl.class)
@XMLElement(xmlTag = "SecretsPreferences")
@Preferences(
		shortName = "Secrets",
		longName = "Secrets preferences",
		FIBPanel = "Fib/Prefs/SecretsPreferences.fib",
		smallIcon = "Icons/Common/SecretsService.png",
		bigIcon = "Icons/Common/SecretsService_64x64.png")
public interface SecretsPreferences extends ServicePreferences<SecretsService> {

	/**
	 * Name of the (non-persisted) bean property fired whenever the underlying {@link SecretsService} secret list changes, so that the
	 * FIB table bound to {@link #getSecretKeys()} refreshes: that list is not a PAMELA-modeled property (its data lives in the service,
	 * not here), so it never gets PAMELA's automatic change notifications and must be fired manually.
	 */
	String SECRET_KEYS_KEY = "secretKeys";

	/**
	 * Return the names of the secrets currently stored in the local {@link SecretsService} store, sorted for stable display.
	 */
	List<String> getSecretKeys();

	/**
	 * Open a modal dialog to create a new secret.
	 */
	void addSecret();

	/**
	 * Open a modal dialog to set a new value for the secret identified by <code>key</code>.
	 */
	void editSecret(String key);

	/**
	 * Remove the secret identified by <code>key</code> from the local store.
	 */
	void removeSecret(String key);

	abstract class SecretsPreferencesImpl extends PreferencesContainerImpl implements SecretsPreferences {

		private static final Logger logger = Logger.getLogger(SecretsPreferences.class.getPackage().getName());

		@Override
		public ApplicationContext getServiceManager() {
			return (ApplicationContext) super.getServiceManager();
		}

		@Override
		public List<String> getSecretKeys() {
			List<String> keys = new ArrayList<>(getService().getSecretKeys());
			Collections.sort(keys);
			return keys;
		}

		@Override
		public void addSecret() {
			if (SecretEntryDialog.askSecret(getServiceManager(), getService(), null)) {
				fireSecretKeysChanged();
			}
		}

		@Override
		public void editSecret(String key) {
			if (key != null) {
				if (SecretEntryDialog.askSecret(getServiceManager(), getService(), key)) {
					fireSecretKeysChanged();
				}
			}
		}

		@Override
		public void removeSecret(String key) {
			if (key != null) {
				getService().removeSecret(key);
				fireSecretKeysChanged();
			}
			else {
				logger.warning("removeSecret() called with no selection");
			}
		}

		private void fireSecretKeysChanged() {
			getPropertyChangeSupport().firePropertyChange(SECRET_KEYS_KEY, null, getSecretKeys());
		}

	}

}
