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

import java.util.Vector;
import java.util.regex.Pattern;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.md.MultiValuedMetaData;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rm.FIBComponentResourceFactory;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.gina.model.FIBModelFactory;
import org.openflexo.gina.model.container.FIBPanel;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.toolbox.StringUtils;

/**
 * Creates a user interface component - a <code>.fib</code> - for a {@link FlexoConcept} or for the VirtualModel itself, in the
 * <code>Xxx.fml/</code> container of the VirtualModel declaring it. See {@link AbstractCreateFIBComponent} for what the component holds.
 *
 * <p>
 * Unlike an inspector, a concept may drive several such components - its default view, named variants, fragments shown elsewhere - so the
 * action is always offered. Named <code>&lt;Concept&gt;.fib</code>, the component is the default view of the concept by convention. Under
 * any other name nothing links it to the concept, unless it is also declared as a NAMED VARIANT: the action then adds it to the
 * <code>@UI</code> annotation of the concept, and saves the compilation unit.
 *
 * @author sylvain
 */
public class CreateFIBComponent extends AbstractCreateFIBComponent<CreateFIBComponent> {

	public static FlexoActionFactory<CreateFIBComponent, FMLObject, FMLObject> actionType = new FlexoActionFactory<CreateFIBComponent, FMLObject, FMLObject>(
			"user_interface", FlexoActionFactory.generateMenu, FlexoActionFactory.defaultGroup, FlexoActionFactory.NORMAL_ACTION_TYPE) {

		@Override
		public CreateFIBComponent makeNewAction(FMLObject focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
			return new CreateFIBComponent(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FMLObject object, Vector<FMLObject> globalSelection) {
			return conceptOf(object) != null;
		}

		/** Always: a concept may drive as many components as it needs. */
		@Override
		public boolean isEnabledForSelection(FMLObject object, Vector<FMLObject> globalSelection) {
			return conceptOf(object) != null;
		}

		@Override
		public LocalizedDelegate getLocales(FlexoServiceManager serviceManager) {
			return pluginLocales(serviceManager);
		}
	};

	static {
		FlexoObjectImpl.addActionForClass(CreateFIBComponent.actionType, FlexoConcept.class);
		FlexoObjectImpl.addActionForClass(CreateFIBComponent.actionType, FMLCompilationUnit.class);
	}

	/** What an FML annotation accepts as a key. */
	private static final Pattern VARIANT_NAME = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

	/** The controller of an Openflexo view: what a module view showing the component builds it with. */
	private static final String CONTROLLER_CLASS_NAME = "org.openflexo.view.controller.FlexoFIBController";

	private boolean declareAsVariant = false;
	private String variantName;

	private CreateFIBComponent(FMLObject focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	public String getComponentSuffix() {
		return FIBComponentResourceFactory.COMPONENT_SUFFIX;
	}

	/**
	 * <code>&lt;Concept&gt;.fib</code> - the default view by convention - or, when the container already holds it,
	 * <code>&lt;Concept&gt;2.fib</code>, <code>&lt;Concept&gt;3.fib</code>…
	 */
	@Override
	protected String getDefaultComponentName(FlexoConcept concept) {
		String name = concept.getName() + getComponentSuffix();
		for (int index = 2; nameIsTaken(name); index++) {
			name = concept.getName() + index + getComponentSuffix();
		}
		return name;
	}

	@Override
	protected FIBPanel makeRootComponent(FIBModelFactory factory, FlexoConcept concept) {
		FIBPanel panel = factory.newFIBPanel();
		panel.setName(getComponentBaseName());
		panel.setControllerClassName(CONTROLLER_CLASS_NAME);
		return panel;
	}

	@Override
	public boolean supportsVariantDeclaration() {
		return true;
	}

	@Override
	public boolean getDeclareAsVariant() {
		return declareAsVariant;
	}

	@Override
	public void setDeclareAsVariant(boolean declareAsVariant) {
		if (declareAsVariant != this.declareAsVariant) {
			this.declareAsVariant = declareAsVariant;
			getPropertyChangeSupport().firePropertyChange("declareAsVariant", !declareAsVariant, declareAsVariant);
		}
	}

	/** The key the component is declared under in <code>@UI(…)</code>: by default, its name with a lower-case initial. */
	@Override
	public String getVariantName() {
		if (variantName == null) {
			String baseName = getComponentBaseName();
			return StringUtils.isNotEmpty(baseName) ? Character.toLowerCase(baseName.charAt(0)) + baseName.substring(1) : null;
		}
		return variantName;
	}

	@Override
	public void setVariantName(String variantName) {
		if (variantName != null && !variantName.equals(this.variantName)) {
			String oldValue = this.variantName;
			this.variantName = variantName;
			getPropertyChangeSupport().firePropertyChange("variantName", oldValue, variantName);
		}
	}

	@Override
	public boolean hasValidVariantName() {
		return getVariantName() != null && VARIANT_NAME.matcher(getVariantName()).matches();
	}

	/**
	 * Whether the concept already declares a variant under the chosen name - redeclaring it would silently replace the component it names.
	 * A single-valued <code>@UI("A.fib")</code> declares the <code>default</code> variant.
	 */
	@Override
	public boolean variantIsAlreadyDeclared() {
		FlexoConcept concept = getFlexoConcept();
		if (concept == null || getVariantName() == null) {
			return false;
		}
		MultiValuedMetaData metaData = concept.getMultiValuedMetaData(FlexoConcept.UI_METADATA);
		if (metaData != null) {
			return metaData.hasKeyValue(getVariantName());
		}
		return concept.hasMetaData(FlexoConcept.UI_METADATA) && FlexoConcept.DEFAULT_VARIANT.equals(getVariantName());
	}

	@Override
	public boolean isValid() {
		return super.isValid() && (!getDeclareAsVariant() || (hasValidVariantName() && !variantIsAlreadyDeclared()));
	}

	@Override
	protected void componentCreated(FlexoConcept concept, CompilationUnitResource compilationUnitResource) throws FlexoException {
		if (getDeclareAsVariant()) {
			declareVariant(concept);
			try {
				compilationUnitResource.save();
			} catch (SaveResourceException e) {
				throw new FlexoException("Could not save " + compilationUnitResource.getURI() + " after declaring " + getComponentName(), e);
			}
		}
	}

	/**
	 * Declare the component as a named variant of supplied concept: <code>@UI(&lt;variant&gt;="&lt;component&gt;")</code>.
	 *
	 * <p>
	 * A single-valued <code>@UI("A.fib")</code> names the DEFAULT view: it is converted into <code>@UI(default="A.fib", …)</code>, or adding a
	 * variant would lose it. Public because it only changes the model: it is what a test can assert on without writing the FML source.
	 */
	public void declareVariant(FlexoConcept concept) {

		MultiValuedMetaData metaData = concept.getMultiValuedMetaData(FlexoConcept.UI_METADATA);

		if (metaData == null) {
			String defaultComponent = concept.hasMetaData(FlexoConcept.UI_METADATA)
					? concept.getSingleMetaData(FlexoConcept.UI_METADATA, String.class)
					: null;
			if (concept.hasMetaData(FlexoConcept.UI_METADATA)) {
				concept.removeFromMetaData(concept.getMetaData(FlexoConcept.UI_METADATA));
			}
			metaData = concept.getFMLModelFactory().newMultiValuedMetaData(FlexoConcept.UI_METADATA);
			if (StringUtils.isNotEmpty(defaultComponent)) {
				metaData.setValue(FlexoConcept.DEFAULT_VARIANT, defaultComponent, String.class);
			}
			metaData.setValue(getVariantName(), getComponentName(), String.class);
			concept.addToMetaData(metaData);
			return;
		}

		metaData.setValue(getVariantName(), getComponentName(), String.class);
	}
}
