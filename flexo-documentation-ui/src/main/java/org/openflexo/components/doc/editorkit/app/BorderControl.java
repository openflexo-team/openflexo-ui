package org.openflexo.components.doc.editorkit.app;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.openflexo.components.doc.editorkit.BorderAttributes;
import org.openflexo.localization.LocalizedSwing;
import org.openflexo.components.doc.editorkit.DocumentEditorKitLocalization;

/**
 * Contains border attributes properties. BorderColor and values.
 *
 * @author Stanislav Lapitsky
 */
public class BorderControl extends JPanel {

	public JComboBox<Color> colorCombo;

	public JCheckBox checkLeft = LocalizedSwing.bind(new JCheckBox(DocumentEditorKitLocalization.locales().localizedForKey("left"), true), DocumentEditorKitLocalization.locales(), "left");
	public JCheckBox checkRight = LocalizedSwing.bind(new JCheckBox(DocumentEditorKitLocalization.locales().localizedForKey("right"), true), DocumentEditorKitLocalization.locales(), "right");
	public JCheckBox checkTop = LocalizedSwing.bind(new JCheckBox(DocumentEditorKitLocalization.locales().localizedForKey("top"), true), DocumentEditorKitLocalization.locales(), "top");
	public JCheckBox checkBottom = LocalizedSwing.bind(new JCheckBox(DocumentEditorKitLocalization.locales().localizedForKey("bottom"), true), DocumentEditorKitLocalization.locales(), "bottom");
	public JCheckBox checkHorizontal = LocalizedSwing.bind(new JCheckBox(DocumentEditorKitLocalization.locales().localizedForKey("horizontal"), true), DocumentEditorKitLocalization.locales(), "horizontal");
	public JCheckBox checkVertical = LocalizedSwing.bind(new JCheckBox(DocumentEditorKitLocalization.locales().localizedForKey("vertical"), true), DocumentEditorKitLocalization.locales(), "vertical");

	/**
	 * Constructs new instance
	 */
	public BorderControl() {
		super();
		setBorder(new TitledBorder(new EtchedBorder(), DocumentEditorKitLocalization.locales().localizedForKey("borders:")));

		colorCombo = new JComboBox<>(new Color[] { Color.black, Color.red, Color.green, Color.blue, Color.white });
		colorCombo.setRenderer(new ColorComboRenderer());
		setLayout(new GridBagLayout());
		add(LocalizedSwing.bind(new JLabel(DocumentEditorKitLocalization.locales().localizedForKey("color")), DocumentEditorKitLocalization.locales(), "color"), new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		add(colorCombo, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.NORTHEAST, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));

		add(checkLeft, new GridBagConstraints(0, 1, 2, 1, 1, 0, GridBagConstraints.NORTHEAST, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		add(checkRight, new GridBagConstraints(0, 2, 2, 1, 1, 0, GridBagConstraints.NORTHEAST, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		add(checkTop, new GridBagConstraints(0, 3, 2, 1, 1, 0, GridBagConstraints.NORTHEAST, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		add(checkBottom, new GridBagConstraints(0, 4, 2, 1, 1, 0, GridBagConstraints.NORTHEAST, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		add(checkHorizontal, new GridBagConstraints(0, 5, 2, 1, 1, 0, GridBagConstraints.NORTHEAST, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		add(checkVertical, new GridBagConstraints(0, 6, 2, 1, 1, 0, GridBagConstraints.NORTHEAST, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
	}

	/**
	 * Constructs new instance with defined border attributes.
	 * 
	 * @param ba
	 *            Border attributes.
	 */
	public BorderControl(BorderAttributes ba) {
		this();
		setBorderAttributes(ba);
	}

	/**
	 * Sets inner controls to reflect specified attributes.
	 * 
	 * @param ba
	 *            Border attributes.
	 */
	public void setBorderAttributes(BorderAttributes ba) {
		colorCombo.setSelectedItem(ba.lineColor);

		checkLeft.setSelected(ba.borderLeft != 0);
		checkRight.setSelected(ba.borderRight != 0);
		checkTop.setSelected(ba.borderTop != 0);
		checkBottom.setSelected(ba.borderBottom != 0);
		checkHorizontal.setSelected(ba.borderHorizontal != 0);
		checkVertical.setSelected(ba.borderVertical != 0);
	}

	/**
	 * Gets user choice. Creates and returns Border attributes according to user's selection.
	 * 
	 * @return
	 */
	public BorderAttributes getBorderAttributes() {
		BorderAttributes ba = new BorderAttributes();
		ba.setBorders(0);
		ba.lineColor = (Color) colorCombo.getSelectedItem();

		if (checkLeft.isSelected())
			ba.borderLeft = 1;
		if (checkRight.isSelected())
			ba.borderRight = 1;
		if (checkTop.isSelected())
			ba.borderTop = 1;
		if (checkBottom.isSelected())
			ba.borderBottom = 1;
		if (checkHorizontal.isSelected())
			ba.borderHorizontal = 1;
		if (checkVertical.isSelected())
			ba.borderVertical = 1;
		return ba;
	}

}
