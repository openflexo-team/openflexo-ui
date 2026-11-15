/**
 * 
 * Copyright (c) 2026, Openflexo
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

package org.openflexo.view.controller;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;

import org.openflexo.components.FlexoUILocalization;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.foundation.fml.rt.error.FMLErrorReporter;
import org.openflexo.foundation.fml.rt.error.FMLExecutionFailure;
import org.openflexo.foundation.fml.rt.error.FMLStack;
import org.openflexo.foundation.fml.rt.error.FMLStackFrame;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.localization.LocalizedSwing;
import org.openflexo.view.FMLConsoleViewer;
import org.openflexo.view.FlexoFrame;

/**
 * The dialog presenting the foreseen failure of an action to the user (CORE-F-11): the kind of the error, the action that failed, the
 * message, and the FML stack, one row per frame, innermost first. "Open in the FML editor" (or a double click on a frame) shows the FML
 * source of the selected frame, "Show in console" opens the FML console, which holds the record of the failure; the dialog is not modal
 * and stays open, to go from frame to frame or to the console, until "Close".
 */
@SuppressWarnings("serial")
public class FMLErrorDialog extends JDialog {

	private final InteractiveFlexoEditor editor;
	private final List<FMLStackFrame> frames;
	private final JList<FMLStackFrame> frameList;
	private final JButton openInEditorButton;

	public FMLErrorDialog(InteractiveFlexoEditor editor, FlexoAction<?, ?, ?> action, Throwable failure) {
		// Not modal: the user goes from frame to frame in the FML editor while the dialog stays open
		super(FlexoFrame.getActiveFrame(false), false);
		this.editor = editor;
		LocalizedDelegate locales = FlexoUILocalization.locales();
		setTitle(locales.localizedForKey("fml_error"));

		FMLStack stack = FMLExecutionFailure.findFMLStack(failure);
		frames = stack != null ? stack.getFrames() : Collections.emptyList();

		// Header: kind of the error, the action that failed, the message
		JPanel header = new JPanel(new BorderLayout(10, 4));
		header.add(new JLabel(UIManager.getIcon("OptionPane.errorIcon")), BorderLayout.WEST);
		JPanel texts = new JPanel(new BorderLayout(0, 4));
		JLabel kindLabel = new JLabel(FMLErrorReporter.localizedKind(failure));
		kindLabel.setFont(kindLabel.getFont().deriveFont(Font.BOLD));
		texts.add(kindLabel, BorderLayout.NORTH);
		texts.add(new JLabel(locales.localizedForKeyWithParams("action_($0)_failed", actionName(action))), BorderLayout.CENTER);
		JTextArea message = new JTextArea(failure.getMessage() != null ? failure.getMessage() : failure.getClass().getSimpleName());
		message.setEditable(false);
		message.setLineWrap(true);
		message.setWrapStyleWord(true);
		message.setOpaque(false);
		message.setColumns(60);
		texts.add(message, BorderLayout.SOUTH);
		header.add(texts, BorderLayout.CENTER);

		// The FML stack
		DefaultListModel<FMLStackFrame> model = new DefaultListModel<>();
		frames.forEach(model::addElement);
		frameList = new JList<>(model);
		frameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		frameList.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				return super.getListCellRendererComponent(list, value != null ? value.toString().trim() : "", index, isSelected,
						cellHasFocus);
			}
		});
		JPanel stackPanel = new JPanel(new BorderLayout(0, 4));
		stackPanel.add(LocalizedSwing.bind(new JLabel(locales.localizedForKey("fml_stack")), locales, "fml_stack"), BorderLayout.NORTH);
		JScrollPane scroll = new JScrollPane(frameList);
		scroll.setPreferredSize(new Dimension(640, 140));
		stackPanel.add(scroll, BorderLayout.CENTER);

		// Buttons
		JButton showInConsoleButton = LocalizedSwing.bind(new JButton(locales.localizedForKey("show_in_console")), locales, "show_in_console");
		showInConsoleButton.addActionListener(e -> showInConsole());
		showInConsoleButton.setEnabled(getController() != null && editor.getFMLConsole() != null);
		openInEditorButton = LocalizedSwing.bind(new JButton(locales.localizedForKey("open_in_fml_editor")), locales, "open_in_fml_editor");
		openInEditorButton.addActionListener(e -> openInFMLEditor());
		JButton closeButton = LocalizedSwing.bind(new JButton(locales.localizedForKey("close")), locales, "close");
		closeButton.addActionListener(e -> dispose());
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttons.add(showInConsoleButton);
		buttons.add(openInEditorButton);
		buttons.add(closeButton);

		frameList.addListSelectionListener(e -> updateOpenInEditorButton());
		frameList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					openInFMLEditor();
				}
			}
		});
		if (!frames.isEmpty()) {
			frameList.setSelectedIndex(0);
		}
		updateOpenInEditorButton();

		JPanel content = new JPanel(new BorderLayout(0, 10));
		content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		content.add(header, BorderLayout.NORTH);
		if (!frames.isEmpty()) {
			content.add(stackPanel, BorderLayout.CENTER);
		}
		content.add(buttons, BorderLayout.SOUTH);
		setContentPane(content);
		getRootPane().setDefaultButton(closeButton);
		pack();
		setLocationRelativeTo(getOwner());
	}

	public void showDialog() {
		setVisible(true);
	}

	/** The frames of the FML stack shown, innermost first */
	public List<FMLStackFrame> getFrames() {
		return frames;
	}

	private static String actionName(FlexoAction<?, ?, ?> action) {
		if (action instanceof FlexoBehaviourAction && ((FlexoBehaviourAction<?, ?, ?>) action).getFlexoBehaviour() != null) {
			return ((FlexoBehaviourAction<?, ?, ?>) action).getFlexoBehaviour().getSignature();
		}
		return action.getLocalizedName();
	}

	private FlexoController getController() {
		return FMLSourceNavigator.activeController(editor);
	}

	private void updateOpenInEditorButton() {
		boolean enabled = FMLSourceNavigator.canShow(editor, frameList.getSelectedValue());
		openInEditorButton.setEnabled(enabled);
		openInEditorButton.setToolTipText(enabled ? null : FlexoUILocalization.locales().localizedForKey("no_fml_source_for_this_frame"));
	}

	private void openInFMLEditor() {
		FMLStackFrame frame = frameList.getSelectedValue();
		if (FMLSourceNavigator.canShow(editor, frame)) {
			FMLSourceNavigator.show(editor, frame);
		}
	}

	private void showInConsole() {
		FlexoController controller = getController();
		if (controller != null) {
			// The dialog stays open, as for the FML editor: it is closed by "Close"
			FMLConsoleViewer.showConsoleViewer(editor.getFMLConsole(),
					controller.getApplicationFIBLibraryService().getApplicationFIBLibrary(), controller.getFlexoFrame());
		}
	}
}
