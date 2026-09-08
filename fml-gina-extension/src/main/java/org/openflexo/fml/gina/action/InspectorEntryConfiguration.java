/**
 * 
 * Copyright (c) 2014-2026, Openflexo
 * 
 * This file is part of openflexo-ui, a component of the software infrastructure 
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
package org.openflexo.fml.gina.action;

import java.lang.reflect.Type;

import org.openflexo.foundation.fml.FlexoBehaviourParameter.WidgetType;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.Visibility;

/**
 * One line of the inspector being created: a property of the concept, whether it is kept, under which label, and with which widget.
 *
 * <p>
 * This is what the wizard lets the user select, deselect and edit. The defaults come from the property itself - its name as the label, and
 * the first widget type {@link org.openflexo.foundation.fml.FlexoBehaviourParameter#getAvailableWidgetTypes(Type)} offers for its type,
 * which is the platform's own answer to "which widget represents this type".
 *
 * @author sylvain
 */
public class InspectorEntryConfiguration {

	private final FlexoProperty<?> property;

	private boolean selected;
	private String label;
	private WidgetType widget;

	public InspectorEntryConfiguration(FlexoProperty<?> property) {
		this.property = property;
		this.label = property.getPropertyName();
		this.widget = defaultWidgetFor(property.getType());
		// Every accessible property is OFFERED, but only the public ones are proposed for inclusion: the others are
		// internals of the concept, which an inspector of its instances has no business showing by default.
		this.selected = property.getVisibility() == Visibility.Public;
	}

	/** Shown by the wizard, so the reader understands why a line is checked or not. */
	public Visibility getVisibility() {
		return property.getVisibility();
	}

	private static WidgetType defaultWidgetFor(Type type) {
		java.util.List<WidgetType> available = org.openflexo.foundation.fml.FlexoBehaviourParameter.FlexoBehaviourParameterImpl.getAvailableWidgetTypes(type);
		return available != null && available.size() > 0 ? available.get(0) : WidgetType.CUSTOM_WIDGET;
	}

	public FlexoProperty<?> getProperty() {
		return property;
	}

	public String getPropertyName() {
		return property.getPropertyName();
	}

	/** The type of the property, shown by the wizard so the reader understands why a widget was proposed. */
	public Type getType() {
		return property.getType();
	}

	public boolean getSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public WidgetType getWidget() {
		return widget;
	}

	public void setWidget(WidgetType widget) {
		this.widget = widget;
	}

	/** The widgets that make sense for this property's type - what the wizard offers in its dropdown. */
	public java.util.List<WidgetType> getAvailableWidgetTypes() {
		return org.openflexo.foundation.fml.FlexoBehaviourParameter.FlexoBehaviourParameterImpl.getAvailableWidgetTypes(getType());
	}

	@Override
	public String toString() {
		return getPropertyName() + " : " + widget;
	}
}
