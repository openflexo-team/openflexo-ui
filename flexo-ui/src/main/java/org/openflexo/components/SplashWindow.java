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

package org.openflexo.components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import org.openflexo.ApplicationVersion;
import org.openflexo.FlexoCst;
import org.openflexo.foundation.task.FlexoTask;
import org.openflexo.icon.IconLibrary;
import org.openflexo.toolbox.ToolBox;

@SuppressWarnings("serial")
public class SplashWindow extends JDialog {

	private final JLabel splash;

	// The startup of the application, whose progress is shown at the bottom of the splash (null: no progress shown)
	private final FlexoTask startupTask;
	private JLabel stepLabel;
	private ProgressStrip progressStrip;
	private Timer progressTimer;

	public SplashWindow(Frame f) {
		this(f, null);
	}

	/**
	 * Build and show the splash window, showing the progress of supplied startup task (see {@link FlexoTask#begin()}), which is ended
	 * when the splash window is disposed
	 */
	public SplashWindow(Frame f, FlexoTask startupTask) {
		super(f);
		this.startupTask = startupTask;
		setUndecorated(true);
		Dimension imageDim = new Dimension(IconLibrary.SPLASH_IMAGE.getIconWidth(), IconLibrary.SPLASH_IMAGE.getIconHeight());

		// cree un label avec notre image
		splash = new JLabel(IconLibrary.SPLASH_IMAGE);
		splash.setBorder(BorderFactory.createLineBorder(FlexoCst.UNDECORATED_DIALOG_BORDER_COLOR));

		// ajoute le label au panel
		getContentPane().setLayout(null);
		JLabel flexoLabel = new JLabel(IconLibrary.OPENFLEXO_TEXT_ICON, SwingConstants.LEFT);
		flexoLabel.setForeground(FlexoCst.WELCOME_FLEXO_COLOR);
		flexoLabel.setBackground(Color.RED);
		flexoLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
		getContentPane().add(flexoLabel);
		flexoLabel.setBounds(190, 30, 280, 59);

		JLabel businessLabel = new JLabel(ApplicationVersion.DISTRIBUTION, SwingConstants.LEFT);
		businessLabel.setForeground(FlexoCst.WELCOME_FLEXO_COLOR);
		businessLabel.setFont(new Font("SansSerif", Font.ITALIC, 15));
		getContentPane().add(businessLabel);
		businessLabel.setBounds(200, 95, 280, 15);

		JLabel versionLabel = new JLabel(
				"Version " + FlexoCst.BUSINESS_APPLICATION_VERSION + " (build " + ApplicationVersion.BUILD_ID + ")", SwingConstants.LEFT);
		versionLabel.setForeground(Color.DARK_GRAY);
		versionLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
		getContentPane().add(versionLabel);
		versionLabel.setBounds(202, 115, 280, 15);

		JLabel urlLabel = new JLabel("<html><u>www.openflexo.org</u></html>", SwingConstants.RIGHT);
		urlLabel.addMouseListener(new MouseAdapter() {

			/**
			 * Overrides mouseEntered
			 * 
			 * @see java.awt.event.MouseAdapter#mouseEntered(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseEntered(MouseEvent e) {
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}

			/**
			 * Overrides mouseEntered
			 * 
			 * @see java.awt.event.MouseAdapter#mouseEntered(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseExited(MouseEvent e) {
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}

			/**
			 * Overrides mouseClicked
			 * 
			 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseClicked(MouseEvent e) {
				ToolBox.openURL("https://www.openflexo.org");
			}
		});
		urlLabel.setForeground(new Color(180, 150, 200));
		urlLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
		getContentPane().add(urlLabel);
		urlLabel.setBounds(140, 158, 280, 12);

		JLabel copyrightLabel = new JLabel("(c) Copyright Openflexo, 2013-2018, Lab-STICC 2018-2025", SwingConstants.RIGHT);
		copyrightLabel.setForeground(Color.DARK_GRAY);
		copyrightLabel.setFont(new Font("SansSerif", Font.PLAIN, 9));
		getContentPane().add(copyrightLabel);
		copyrightLabel.setBounds(140, 172, 280, 12);

		if (startupTask != null) {
			// Added before the image, so that they are painted over it
			stepLabel = new JLabel("", SwingConstants.LEFT);
			stepLabel.setForeground(Color.GRAY);
			stepLabel.setFont(new Font("SansSerif", Font.PLAIN, 9));
			getContentPane().add(stepLabel);
			stepLabel.setBounds(10, 189, 300, 11);
			progressStrip = new ProgressStrip();
			getContentPane().add(progressStrip);
			progressStrip.setBounds(10, imageDim.height - 9, imageDim.width - 20, 4);
			progressTimer = new Timer(100, e -> updateProgress());
			progressTimer.start();
		}

		getContentPane().add(splash);
		splash.setBounds(0, 0, imageDim.width, imageDim.height);
		validate();
		setSize(imageDim);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private void updateProgress() {
		progressStrip.setFraction(startupTask.getProgressFraction());
		String step = startupTask.getCurrentStepName();
		stepLabel.setText(step != null ? step : "");
	}

	/**
	 * Disposing the splash window ends the startup task
	 */
	@Override
	public void dispose() {
		if (progressTimer != null) {
			progressTimer.stop();
			progressTimer = null;
		}
		if (startupTask != null) {
			startupTask.end();
		}
		super.dispose();
	}

	/**
	 * A thin bar, drawn by hand: the look and feel progress bars ignore such a small height
	 */
	private static class ProgressStrip extends JComponent {

		private static final Color TRACK = new Color(225, 235, 215);
		private static final Color FILL = new Color(130, 180, 70);

		private double fraction;

		void setFraction(double fraction) {
			if (fraction != this.fraction) {
				this.fraction = fraction;
				repaint();
			}
		}

		@Override
		protected void paintComponent(Graphics g) {
			g.setColor(TRACK);
			g.fillRect(0, 0, getWidth(), getHeight());
			g.setColor(FILL);
			g.fillRect(0, 0, (int) Math.round(getWidth() * fraction), getHeight());
		}
	}
}
