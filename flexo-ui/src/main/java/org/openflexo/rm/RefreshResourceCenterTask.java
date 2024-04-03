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

package org.openflexo.rm;

import java.io.IOException;

import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.task.Progress;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.task.FlexoApplicationTask;

/**
 * A task used to load a Flexo module
 * 
 * @author sylvain
 *
 */
public class RefreshResourceCenterTask extends FlexoApplicationTask {
	/**
	 * 
	 */
	private final FlexoResourceCenterService rcService;
	private final FlexoResourceCenter<?> resourceCenter;

	public RefreshResourceCenterTask(FlexoResourceCenterService rcService, FlexoResourceCenter<?> resourceCenter) {
		super("RefreshResourceCenter",
				FlexoLocalization.getMainLocalizer().localizedForKey("refreshing_resource_center") + " " + resourceCenter.toString(),
				rcService.getServiceManager());
		this.rcService = rcService;
		this.resourceCenter = resourceCenter;
	}

	@Override
	public void performTask() {

		Progress.setExpectedProgressSteps(getServiceManager().getTechnologyAdapterService().getTechnologyAdapters().size() + 2);
		try {
			resourceCenter.update();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isCancellable() {
		return true;
	}
}
