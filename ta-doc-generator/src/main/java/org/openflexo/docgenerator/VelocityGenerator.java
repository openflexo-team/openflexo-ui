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
import java.io.IOException;
import java.io.StringWriter;
import java.util.logging.Logger;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.toolbox.FileUtils;

/**
 * Abstract document generator
 * 
 * This generator is based on a Velocity template declared in the context of master {@link AbstractMasterGenerator}
 * 
 */
public abstract class VelocityGenerator<O/* extends FMLObject*/> extends AbstractGenerator<O> {

	private static final Logger logger = FlexoLogger.getLogger(VelocityGenerator.class.getPackage().getName());

	private File generatedFile;

	public VelocityGenerator(Class<O> objectClass, VelocityMasterGenerator<?> masterGenerator) {
		super(objectClass, masterGenerator);
		generatedFile = getFileToBeGenerated();
		// System.out.println("Will generate: " + generatedFile.getAbsolutePath());
	}

	protected File getFileToBeGenerated() {
		return getMasterGenerator().getFileToBeGenerated(this);
	}

	@Override
	public VelocityMasterGenerator<?> getMasterGenerator() {
		return (VelocityMasterGenerator<?>) super.getMasterGenerator();
	}

	/**
	 * Retrieve template name to be applied to generate contents
	 * 
	 * @return
	 */
	public abstract String getTemplateName();

	/**
	 * Returns file being generated
	 * 
	 * @return
	 */
	public File getGeneratedFile() {
		return generatedFile;
	}

	/**
	 * Applies Velocity template to generate contents
	 * 
	 * @return the generated contents, as String
	 */
	private String generateContents() {

		// System.out.println("generateContents() for " + getFMLKeyword());

		Template t = getMasterGenerator().getVelocityEngine().getTemplate(getTemplateName());

		// System.out.println("Template: " + t);

		VelocityContext context = new VelocityContext();
		context.put("generator", this);

		StringWriter writer = new StringWriter();
		t.merge(context, writer);

		return writer.toString();

	}

	@Override
	public String generate() {
		String contents = generateContents();
		if (generatedFile != null) {
			try {
				FileUtils.saveToFile(generatedFile, contents);
				logger.info("Generated " + generatedFile.getName() + " in " + generatedFile.getParentFile().getAbsolutePath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// System.out.println(contents);
		return contents;
	}

}
