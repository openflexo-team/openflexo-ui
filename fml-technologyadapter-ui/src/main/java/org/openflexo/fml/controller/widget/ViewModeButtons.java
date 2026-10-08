/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Openflexo-technology-adapters-ui, a component of the software infrastructure 
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

package org.openflexo.fml.controller.widget;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import org.openflexo.fml.controller.widget.FIBCompilationUnitBrowserFIBController.ViewMode;
import org.openflexo.gina.controller.FIBController;
import org.openflexo.gina.swing.view.widget.JFIBImageWidget;

/**
 * Handles the three buttons (images of the component, named after the three {@link ViewMode}s) allowing to switch between the ways a
 * browser of concepts organizes them: the one corresponding to the current mode is highlighted.
 * 
 * @author sylvain
 */
public class ViewModeButtons {

	private static final Color FOCUS_COLOR = new Color(212, 212, 212);
	private static final Color SELECTION_COLOR = new Color(180, 180, 180);

	private final FIBController controller;
	private final String flatIconName;
	private final String hierarchicalIconName;
	private final String embeddingIconName;

	private ButtonMouseAdapter flatButtonAdapter = null;
	private ButtonMouseAdapter hierarchicalButtonAdapter = null;
	private ButtonMouseAdapter embeddingButtonAdapter = null;

	/**
	 * @param controller
	 *            the controller of the component holding the buttons
	 * @param flatIconName
	 *            name of the image component of the button showing the concepts "flat"
	 * @param hierarchicalIconName
	 *            name of the image component of the button showing the concepts organized by inheritance
	 * @param embeddingIconName
	 *            name of the image component of the button showing the concepts organized by containment
	 */
	public ViewModeButtons(FIBController controller, String flatIconName, String hierarchicalIconName, String embeddingIconName) {
		this.controller = controller;
		this.flatIconName = flatIconName;
		this.hierarchicalIconName = hierarchicalIconName;
		this.embeddingIconName = embeddingIconName;
	}

	/**
	 * Highlights the button of supplied mode (the buttons might not be there yet: nothing happens then)
	 */
	public void select(ViewMode viewMode) {
		flatButtonAdapter = update(flatIconName, flatButtonAdapter, viewMode == ViewMode.Flat);
		hierarchicalButtonAdapter = update(hierarchicalIconName, hierarchicalButtonAdapter, viewMode == ViewMode.Hierarchical);
		embeddingButtonAdapter = update(embeddingIconName, embeddingButtonAdapter, viewMode == ViewMode.Embedding);
	}

	private ButtonMouseAdapter update(String iconName, ButtonMouseAdapter adapter, boolean selected) {
		JFIBImageWidget iconWidget = (JFIBImageWidget) controller.viewForComponent(iconName);
		if (iconWidget != null) {
			if (adapter == null) {
				adapter = new ButtonMouseAdapter(iconWidget);
				iconWidget.getJComponent().addMouseListener(adapter);
			}
			adapter.setSelected(selected);
		}
		return adapter;
	}

	private static class ButtonMouseAdapter extends MouseAdapter {
		private final JFIBImageWidget imageWidget;
		private boolean selected;

		public ButtonMouseAdapter(JFIBImageWidget imageWidget) {
			this.imageWidget = imageWidget;
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			if (imageWidget.getJComponent().isEnabled()) {
				imageWidget.getJComponent().setOpaque(true);
				imageWidget.getJComponent().setBackground(selected ? SELECTION_COLOR : FOCUS_COLOR);
			}
		}

		@Override
		public void mouseExited(MouseEvent e) {
			if (selected) {
				imageWidget.getJComponent().setOpaque(true);
				imageWidget.getJComponent().setBackground(SELECTION_COLOR);
			}
			else {
				imageWidget.getJComponent().setOpaque(false);
				imageWidget.getJComponent().setBackground(null);
			}
		}

		public void setSelected(boolean selected) {
			this.selected = selected;
			if (selected) {
				imageWidget.getJComponent().setOpaque(true);
				imageWidget.getJComponent().setBackground(SELECTION_COLOR);
			}
			else {
				imageWidget.getJComponent().setOpaque(false);
				imageWidget.getJComponent().setBackground(null);
			}
		}
	}

}
