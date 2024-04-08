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

import java.awt.Graphics;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.FMLModelContext;
import org.openflexo.foundation.fml.FMLModelContext.FMLEntity;
import org.openflexo.foundation.fml.FMLModelContext.FMLProperty;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.annotations.SeeAlso;
import org.openflexo.foundation.fml.annotations.UsageExample;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.toolbox.FileUtils;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.view.controller.TechnologyAdapterController;

/**
 * Abstract document generator
 * 
 */
public abstract class AbstractGenerator<O extends FMLObject> {

	private static final Logger logger = FlexoLogger.getLogger(AbstractGenerator.class.getPackage().getName());

	private Class<O> objectClass;
	private TADocGenerator<?> taDocGenerator;
	private File generatedFile;
	protected File smallIconFile;
	protected File bigIconFile;

	public AbstractGenerator(Class<O> objectClass, TADocGenerator<?> taDocGenerator) {
		this.objectClass = objectClass;
		this.taDocGenerator = taDocGenerator;
		generatedFile = new File(taDocGenerator.getMDDir(), objectClass.getSimpleName() + ".md");
		System.out.println("Will generate: " + generatedFile.getAbsolutePath());
	}

	public void generate() {
		String contents = generateContents();
		System.out.println(contents);
	}

	public TADocGenerator<?> getTADocGenerator() {
		return taDocGenerator;
	}

	protected void render(StringBuffer sb) {
		try {
			FileUtils.saveToFile(generatedFile, sb.toString());
			System.out.println("Generated " + generatedFile.getAbsolutePath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected String getGlobalReference() {
		return taDocGenerator.getRelativePath() + "/" + objectClass.getSimpleName() + ".md";
	}

	protected String getGlobalRolesReference() {
		return taDocGenerator.getRelativePath() + "/" + objectClass.getSimpleName() + "_roles.md";
	}

	protected String getGlobalBehavioursReference() {
		return taDocGenerator.getRelativePath() + "/" + objectClass.getSimpleName() + "_behaviours.md";
	}

	protected String getGlobalEditionActionsReference() {
		return taDocGenerator.getRelativePath() + "/" + objectClass.getSimpleName() + "_edition_actions.md";
	}

	protected String getGlobalFetchRequestsReference() {
		return taDocGenerator.getRelativePath() + "/" + objectClass.getSimpleName() + "_fetch_requests.md";
	}

	protected String getLocalReference() {
		return "./" + objectClass.getSimpleName() + ".md";
	}

	protected String getLocalRolesReference() {
		return "./" + objectClass.getSimpleName() + "_roles.md";
	}

	protected String getLocalBehavioursReference() {
		return "./" + objectClass.getSimpleName() + "_behaviours.md";
	}

	protected String getLocalEditionActionsReference() {
		return "./" + objectClass.getSimpleName() + "_edition_actions.md";
	}

	protected String getLocalFetchRequestsReference() {
		return "./" + objectClass.getSimpleName() + "_fetch_requests.md";
	}

	protected String getJavadocReference() {
		return "[" + getObjectClass().getName() + "](./apidocs/" + getJavadocPath(getObjectClass()) + ".md)" + StringUtils.LINE_SEPARATOR;
	}

	private String getJavadocPath(Class<?> clazz) {
		String className = clazz.getName();
		return className.replace(".", "/");
	}

	@Deprecated
	protected String getHTMLReference(Class<? extends FMLObject> objectReference) {
		StringBuffer sb = new StringBuffer();
		AbstractGenerator<? extends FMLObject> generatorReference = getReference(objectReference);
		sb.append(" - \u200E" + generatorReference.getSmallIconAsHTML());
		sb.append(" [" + generatorReference.getFMLKeyword() + "](" + generatorReference.getObjectClass().getSimpleName() + ".md) : "
				+ generatorReference.getFMLShortDescription());
		return sb.toString();
	}

	protected abstract Image getIcon();

	public String getSmallIconAsHTML() {
		if (smallIconFile == null) {
			return "";
		}
		return "<img src=\"" + "/openflexo-diagram/images/" + smallIconFile.getName() + "\" alt=\"" + getObjectClass().getSimpleName()
				+ "\"/>";
	}

	public String getBigIconAsHTML() {
		if (bigIconFile == null) {
			return "";
		}
		return "<img src=\"" + "/openflexo-diagram/images/" + bigIconFile.getName() + "\" alt=\"" + getObjectClass().getSimpleName()
				+ "\"/>";
	}

	protected void generateIconFiles() {
		if (getIcon() != null) {
			smallIconFile = new File(taDocGenerator.getImageDir(), objectClass.getSimpleName() + ".png");
			saveImage(getIcon(), smallIconFile);
			System.out.println("Generated " + smallIconFile.getAbsolutePath());
			bigIconFile = new File(taDocGenerator.getImageDir(), objectClass.getSimpleName() + "_BIG.png");
			saveImage(getIcon(), bigIconFile, 2.0);
			System.out.println("Generated " + bigIconFile.getAbsolutePath());
		}
		else {
			System.out.println("Cannot find icon for " + objectClass.getSimpleName());
		}
	}

	protected static void saveImage(Image image, File file) {
		saveImage(image, file, null);
	}

	protected static void saveImage(Image image, File file, Double scaleFactor) {
		try {
			ImageIO.write(imageToBufferedImage(image, scaleFactor), "png", file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static BufferedImage imageToBufferedImage(Image im, Double scaleFactor) {
		final int w = im.getWidth(null);
		final int h = im.getHeight(null);
		BufferedImage imageToSave;
		if (scaleFactor != null) {
			BufferedImage scaledImage = new BufferedImage((int) (w * scaleFactor), (int) (h * scaleFactor), BufferedImage.TYPE_INT_ARGB);
			final AffineTransform at = AffineTransform.getScaleInstance(scaleFactor, scaleFactor);
			final AffineTransformOp ato = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
			BufferedImage initialImage = imageToBufferedImage(im, null);
			scaledImage = ato.filter(initialImage, scaledImage);
			imageToSave = scaledImage;
		}
		else {
			imageToSave = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			Graphics bg = imageToSave.getGraphics();
			bg.drawImage(im, 0, 0, null);
			bg.dispose();
		}
		return imageToSave;
	}

	public Class<O> getObjectClass() {
		return objectClass;
	}

	public <R extends FMLObject> AbstractGenerator<R> getReference(Class<R> objectClass) {
		return taDocGenerator.getGenerator(objectClass);
	}

	public String toMD(String text) {
		String returned = text;
		if (returned.startsWith("<html>")) {
			returned = returned.substring(6);
		}
		if (returned.endsWith("</html>")) {
			returned = returned.substring(0, returned.length() - 7);
		}
		returned = returned.replace("<br>", StringUtils.LINE_SEPARATOR + StringUtils.LINE_SEPARATOR);
		return returned;
	}

	public String toCode(String text) {
		StringBuffer sb = new StringBuffer();
		int indent = 0;
		StringBuffer current = null;
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) == '(' && indent == 0) {
				sb.append(text.charAt(i));
				// propertyList = new ArrayList<>();
				current = new StringBuffer();
				indent++;
			}
			else if (text.charAt(i) == ')' && indent == 1) {
				if (current.length() > 60) {
					boolean isFirst = true;
					StringTokenizer st = new StringTokenizer(current.toString(), ",");
					while (st.hasMoreElements()) {
						String next = st.nextToken();
						sb.append((isFirst ? "" : ",") + StringUtils.LINE_SEPARATOR + "        ");
						isFirst = false;
						sb.append(next);
					}
				}
				else {
					sb.append(current);
				}
				sb.append(text.charAt(i));
				indent--;
			}
			/*else if (text.charAt(i) == ',') {
				if (indent == 1) {
					propertyList.add(current.toString());
					current.append(text.charAt(i));
				}
				else {
					sb.append(text.charAt(i));
				}
			}*/
			/*else if (indent == 0) {
				sb.append(text.charAt(i));
			}*/
			else if (indent == 1) {
				current.append(text.charAt(i));
			}
			else {
				sb.append(text.charAt(i));
				// System.err.println("Unexpected indent>1");
			}
		}
		return sb.toString();

	}

	protected FMLEntity<?> getFMLEntity() {
		return FMLModelContext.getFMLEntity(getObjectClass(), getFMLModelFactory());
	}

	public final String getFMLKeyword() {
		if (getFMLEntity() != null) {
			return getFMLEntity().getFmlAnnotation().value();
		}
		return getObjectClass().getSimpleName();
	}

	public final String getLocalMDPath() {
		return getObjectClass().getSimpleName() + ".md";
	}

	public final String getFMLDescription() {
		if (getFMLEntity() != null) {
			String returned = getFMLEntity().getFmlAnnotation().description();
			if (StringUtils.isEmpty(returned)) {
				return "No documentation yet";
			}
			return toMD(getFMLEntity().getFmlAnnotation().description());
		}
		return "No documentation yet";
	}

	public final String getFMLShortDescription() {
		if (getFMLEntity() != null) {
			String returned = getFMLEntity().getFmlAnnotation().description();
			if (StringUtils.isEmpty(returned)) {
				return "No documentation yet";
			}
			if (returned.contains("<br>")) {
				returned = returned.substring(0, returned.indexOf("<br>"));
			}
			return toMD(returned);
		}
		return "No documentation yet";
	}

	public List<UsageExample> getFMLExamples() {
		if (getFMLEntity() != null) {
			return Arrays.asList(getFMLEntity().getFmlAnnotation().examples());
		}
		return Collections.emptyList();
	}

	public List<SeeAlso> getReferences() {
		if (getFMLEntity() != null) {
			return Arrays.asList(getFMLEntity().getFmlAnnotation().references());
		}
		return Collections.emptyList();
	}

	public final boolean hasFMLProperties() {
		if (getFMLEntity() != null) {
			return getFMLEntity().getProperties().size() > 0;
		}
		return false;
	}

	public List<FMLProperty> getFMLProperties() {
		if (getFMLEntity() != null) {
			return (List) getFMLEntity().getProperties();
		}
		return null;
	}

	public FMLProperty getFMLProperty(String propertyName) {
		List<FMLProperty> fmlProperties = getFMLProperties();
		if (fmlProperties != null) {
			for (FMLProperty fmlProperty : getFMLProperties()) {
				if (fmlProperty.getName().equals(propertyName)) {
					return fmlProperty;
				}
			}
		}
		return null;
	}

	public TechnologyAdapter<?> getTechnologyAdapter() {
		return taDocGenerator.getTechnologyAdapter();
	}

	public TechnologyAdapterService getTechnologyAdapterService() {
		return taDocGenerator.getTechnologyAdapterService();
	}

	public TechnologyAdapterController<?> getTechnologyAdapterController() {
		return taDocGenerator.getTechnologyAdapterController();
	}

	public FMLModelFactory getFMLModelFactory() {
		return taDocGenerator.getFMLModelFactory();
	}

	public File getGeneratedFile() {
		return generatedFile;
	}

	/*public String getMVNArtefactName() {
		return taDocGenerator.getMVNArtefactName();
	}*/

	public File getMDDir() {
		return taDocGenerator.getMDDir();
	}

	public abstract String getTemplateName();

	public String generateContents() {

		System.out.println("generateContents() for " + getFMLKeyword());

		Template t = getTADocGenerator().getVelocityEngine().getTemplate(getTemplateName());

		System.out.println("Template: " + t);

		VelocityContext context = new VelocityContext();
		// context.put("name", "World");
		context.put("generator", this);

		StringWriter writer = new StringWriter();
		t.merge(context, writer);

		return writer.toString();

	}

	/**
	 * Return resulting type of supplied property:
	 * <ul>
	 * <li>If property addresses a DataBinding (dynamic access), return expected type of DataBinding</li>
	 * <li>If property adresses a static value, return type of that value
	 * </ul>
	 * 
	 * @param fmlProperty
	 * @return
	 */
	public Type getPropertyType(FMLProperty fmlProperty) {
		Type propertyType = fmlProperty.getModelProperty().getGetterMethod().getGenericReturnType();
		if (propertyType instanceof ParameterizedType && ((ParameterizedType) propertyType).getRawType().equals(DataBinding.class)) {
			return ((ParameterizedType) propertyType).getActualTypeArguments()[0];
		}
		else {
			return propertyType;
		}
	}

	/**
	 * Return simple String representation of supplied property
	 * 
	 * @param fmlProperty
	 * @return
	 */
	public String getPropertyTypeAsString(FMLProperty fmlProperty) {
		return TypeUtils.simpleRepresentation(getPropertyType(fmlProperty));
	}

}
