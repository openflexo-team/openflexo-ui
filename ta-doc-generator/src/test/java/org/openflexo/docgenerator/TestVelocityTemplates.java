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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.junit.Test;

/**
 * Every Velocity template of the documentation generator is parsed and rendered by the engine, configured as the master generators
 * configure it. The generator object is an empty map: this checks the syntax of the templates and that no directive raises an error,
 * not the generated text (BUILD-F-7: Velocity 1.7 to 2.4).
 */
public class TestVelocityTemplates {

	@Test
	public void allTemplatesAreParsedAndRendered() throws URISyntaxException {
		File templates = new File(getClass().getClassLoader().getResource("Templates").toURI());
		assertTrue(templates.isDirectory());

		List<String> rendered = new ArrayList<>();
		for (File directory : templates.listFiles(File::isDirectory)) {
			VelocityEngine engine = new VelocityEngine();
			engine.setProperty("space.gobbling", "bc");
			engine.setProperty("file.resource.loader.path", directory.getAbsolutePath());
			for (File file : directory.listFiles(File::isFile)) {
				Template template = engine.getTemplate(file.getName());
				assertNotNull(file.getName(), template);
				VelocityContext context = new VelocityContext();
				context.put("generator", new HashMap<String, Object>());
				StringWriter writer = new StringWriter();
				template.merge(context, writer);
				assertFalse(file.getName() + " renders nothing", writer.toString().isEmpty());
				rendered.add(directory.getName() + "/" + file.getName());
			}
		}
		assertTrue("no template found", rendered.size() >= 12);
	}
}
