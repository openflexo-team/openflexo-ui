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

import java.util.logging.Logger;

import org.openflexo.docgenerator.EditionActionGenerator;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.logging.FlexoLogger;

/**
 * Documentation generator for {@link EditionAction}
 * 
 */
public class HTMLEditionActionGenerator<EA extends EditionAction> extends EditionActionGenerator<EA> implements AbstractHTMLGenerator<EA> {

	private static final Logger logger = FlexoLogger.getLogger(HTMLEditionActionGenerator.class.getPackage().getName());

	public HTMLEditionActionGenerator(Class<EA> objectClass, HTMLMasterGenerator<?> taDocGenerator) {
		super(objectClass, taDocGenerator);
	}

	@Override
	public HTMLMasterGenerator<?> getMasterGenerator() {
		return (HTMLMasterGenerator<?>) super.getMasterGenerator();
	}

	@Override
	public String getTemplateName() {
		return "EditionAction.html";
	}

}
