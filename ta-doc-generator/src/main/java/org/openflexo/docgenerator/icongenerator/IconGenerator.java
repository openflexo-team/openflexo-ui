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

import java.awt.Graphics;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.openflexo.docgenerator.AbstractGenerator;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.logging.FlexoLogger;

/**
 * Documentation generator for {@link EditionAction}
 * 
 */
public abstract class IconGenerator<O extends FMLObject> extends AbstractGenerator<O> {

	private static final Logger logger = FlexoLogger.getLogger(IconGenerator.class.getPackage().getName());

	protected File smallIconFile;
	protected File bigIconFile;

	public IconGenerator(Class<O> objectClass, IconsTADocGenerator<?> masterGenerator) {
		super(objectClass, masterGenerator);
	}

	@Override
	public IconsTADocGenerator<?> getTADocGenerator() {
		return (IconsTADocGenerator<?>) super.getTADocGenerator();
	}

	public static String getSmallIconFileName(Class<? extends FMLObject> fmlClass) {
		return fmlClass.getSimpleName() + ".png";
	}

	public static String getBigIconFileName(Class<? extends FMLObject> fmlClass) {
		return fmlClass.getSimpleName() + "_BIG.png";
	}

	@Override
	public Image generate() {
		Image icon = getIcon();
		if (icon != null) {
			smallIconFile = new File(getTADocGenerator().getImageDir(), getSmallIconFileName(getObjectClass()));
			saveImage(getIcon(), smallIconFile);
			logger.info("Generated " + smallIconFile.getAbsolutePath());
			bigIconFile = new File(getTADocGenerator().getImageDir(), getBigIconFileName(getObjectClass()));
			saveImage(getIcon(), bigIconFile, 2.0);
			logger.info("Generated " + bigIconFile.getAbsolutePath());
		}
		else {
			logger.warning("Cannot find icon for " + getObjectClass().getSimpleName());
		}
		return icon;
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

}
