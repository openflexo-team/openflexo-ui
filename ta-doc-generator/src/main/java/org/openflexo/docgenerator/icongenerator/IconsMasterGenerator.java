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

package org.openflexo.docgenerator.icongenerator;

import java.awt.Image;
import java.io.File;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import org.openflexo.ApplicationContext;
import org.openflexo.docgenerator.AbstractMasterGenerator;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.editionaction.FetchRequest;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.logging.FlexoLogger;

/**
 * Generate all icons for a {@link TechnologyAdapter}
 * 
 */
public class IconsMasterGenerator<TA extends TechnologyAdapter<TA>> extends AbstractMasterGenerator<TA> {

	private static final Logger logger = FlexoLogger.getLogger(IconsMasterGenerator.class.getPackage().getName());

	private File imageDir;

	public IconsMasterGenerator(Class<TA> taClass, String repositoryName, String modelProjectName, ApplicationContext applicationContext) {

		super(taClass, repositoryName, modelProjectName, applicationContext);
	}

	@Override
	protected void initFilePaths() {
		super.initFilePaths();
		imageDir = new File(getTASiteDir(), "resources/images");
		System.out.println("imageDir=" + imageDir.getAbsolutePath() + " exists=" + imageDir.exists());
	}

	public File getImageDir() {
		return imageDir;
	}

	@Override
	protected <MS extends ModelSlot<?>> IconGenerator<MS> makeModelSlotGenerator(Class<MS> modelSlotClass) {
		return new IconGenerator<MS>(modelSlotClass, this) {
			@Override
			protected Image getIcon() {
				ImageIcon icon = getTechnologyAdapterController().getIconForModelSlot(getObjectClass());
				if (icon != null) {
					return icon.getImage();
				}
				logger.warning("No icon for " + modelSlotClass);
				return null;
			}
		};
	}

	@Override
	protected <R extends FlexoRole<?>> IconGenerator<R> makeFlexoRoleGenerator(Class<R> roleClass) {
		return new IconGenerator<R>(roleClass, this) {
			@Override
			protected Image getIcon() {
				ImageIcon icon = getTechnologyAdapterController().getIconForFlexoRole(getObjectClass());
				if (icon != null) {
					return icon.getImage();
				}
				logger.warning("No icon for " + roleClass);
				return null;
			}
		};
	}

	@Override
	protected <B extends FlexoBehaviour> IconGenerator<B> makeFlexoBehaviourGenerator(Class<B> behaviourClass) {
		return new IconGenerator<B>(behaviourClass, this) {
			@Override
			protected Image getIcon() {
				ImageIcon icon = getTechnologyAdapterController().getIconForFlexoBehaviour(getObjectClass());
				if (icon != null) {
					return icon.getImage();
				}
				logger.warning("No icon for " + behaviourClass);
				return null;
			}
		};
	}

	@Override
	protected <EA extends EditionAction> IconGenerator<EA> makeEditionActionGenerator(Class<EA> editionActionClass) {
		return new IconGenerator<EA>(editionActionClass, this) {
			@Override
			protected Image getIcon() {
				ImageIcon icon = getTechnologyAdapterController().getIconForEditionAction(getObjectClass());
				if (icon != null) {
					return icon.getImage();
				}
				logger.warning("No icon for " + editionActionClass);
				return null;
			}
		};
	}

	@Override
	protected <FR extends FetchRequest<?, ?, ?>> IconGenerator<FR> makeFetchRequestGenerator(Class<FR> fetchRequestClass) {
		return new IconGenerator<FR>(fetchRequestClass, this) {
			@Override
			protected Image getIcon() {
				return getTechnologyAdapterController().getIconForEditionAction(getObjectClass()).getImage();
			}
		};
	}

}
