/*
 * XmlElementWrapperPlugin.java
 * 
 * Copyright (C) 2009, Bjarne Hansen, http://www.conspicio.dk.
 * All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package com.sun.tools.xjc.addon.xew;

import static com.sun.tools.xjc.addon.xew.CommonUtils.addAnnotation;
import static com.sun.tools.xjc.addon.xew.CommonUtils.copyFields;
import static com.sun.tools.xjc.addon.xew.CommonUtils.generableToString;
import static com.sun.tools.xjc.addon.xew.CommonUtils.getAnnotation;
import static com.sun.tools.xjc.addon.xew.CommonUtils.getAnnotationMember;
import static com.sun.tools.xjc.addon.xew.CommonUtils.getAnnotationMemberExpression;
import static com.sun.tools.xjc.addon.xew.CommonUtils.getPrivateField;
import static com.sun.tools.xjc.addon.xew.CommonUtils.getXsdDeclaration;
import static com.sun.tools.xjc.addon.xew.CommonUtils.hasPropertyNameCustomization;
import static com.sun.tools.xjc.addon.xew.CommonUtils.isHiddenClass;
import static com.sun.tools.xjc.addon.xew.CommonUtils.isListedAsParametrisation;
import static com.sun.tools.xjc.addon.xew.CommonUtils.removeAnnotation;
import static com.sun.tools.xjc.addon.xew.CommonUtils.setPrivateField;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;

import com.sun.codemodel.JAnnotatable;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JAnnotationValue;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JJavaName;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.addon.xew.CommonConfiguration.ConfigurationOption;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CCustomizations;
import com.sun.tools.xjc.model.CElementPropertyInfo;
import com.sun.tools.xjc.model.CElementPropertyInfo.CollectionMode;
import com.sun.tools.xjc.model.CPluginCustomization;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.model.CReferencePropertyInfo;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.tools.xjc.reader.Ring;
import com.sun.xml.bind.api.impl.NameConverter;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSDeclaration;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.logging.LogFactory;
import org.jvnet.jaxb2_commons.plugin.AbstractParameterizablePlugin;
import org.jvnet.jaxb2_commons.util.CustomizationUtils;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

/**
 * The XML Element Wrapper plugin is a JAXB plugin for the XJC compiler enabling generation of "natural" Java classes
 * for handling collection types. The code generated will be annotated with {@link XmlElementWrapper} and
 * {@link XmlElement} annotations and will have no extra inner classes representing the immediate collection type.
 * 
 * @see <a href="https://github.com/dmak/jaxb-xew-plugin">plugin site</a>
 * @see <a href="http://www.conspicio.dk/blog/bjarne/jaxb-xmlelementwrapper-plugin">original plugin site</a>
 * @see <a href="http://www.conspicio.dk/projects/overview">source code and binary packages</a>
 * 
 * @author Bjarne Hansen
 * @author Dmitry Katsubo
 */
public class XmlElementWrapperPlugin extends AbstractParameterizablePlugin {

	private static final QName	XEW_QNAME							   = new QName("http://github.com/jaxb-xew-plugin",
	            "xew");

	private static final String	PLUGIN_NAME							   = "Xxew";

	private GlobalConfiguration	globalConfiguration					   = new GlobalConfiguration();

	private JClass				xmlElementDeclModelClass;

	private static final String	FACTORY_CLASS_NAME					   = "ObjectFactory";

	static final String			COMMONS_LOGGING_LOG_LEVEL_PROPERTY_KEY = "org.apache.commons.logging.simplelog.defaultlog";

	public XmlElementWrapperPlugin() {
		logger = null;
	}

	@Override
	public String getOptionName() {
		return PLUGIN_NAME;
	}

	@Override
	public String getUsage() {
		return "  " + getArgumentName("")
		            + " Replace collection types with fields having the @XmlElementWrapper and @XmlElement annotations.";
	}

	@Override
	public Collection<QName> getCustomizationElementNames() {
		return Arrays.asList(XEW_QNAME);
	}

	void initLoggerIfNecessary(Options opts) {
		if (logger != null) {
			return;
		}

		// Allow the caller to control the log level by explicitly setting this system variable:
		if (System.getProperty(COMMONS_LOGGING_LOG_LEVEL_PROPERTY_KEY) == null) {
			String logLevel = "WARN";

			if (opts.quiet) {
				logLevel = "FATAL";
			}
			else if (opts.debugMode) {
				logLevel = "DEBUG";
			}
			else if (opts.verbose) {
				logLevel = "INFO";
			}

			System.setProperty(COMMONS_LOGGING_LOG_LEVEL_PROPERTY_KEY, logLevel);
		}

		// The logger needs to be re-created and not taken from cache:
		LogFactory.getFactory().release();

		logger = LogFactory.getLog(getClass());
		globalConfiguration.setLogger(logger);
	}

	@Override
	public void onActivated(Options opts) {
		initLoggerIfNecessary(opts);
	}

	/**
	 * Generate argument name from option name.
	 */
	private static String getArgumentName(String optionName) {
		return "-" + PLUGIN_NAME + ":" + optionName;
	}

	/**
	 * Parse argument at a given index. Option value may go within the same argument (separated with equals), or as
	 * following argument.
	 * 
	 * @param args
	 *            list of arguments
	 * @param index
	 *            current index
	 * @param optionName
	 *            the option to match
	 * @return number of arguments processed
	 */
	private int parseArgument(String[] args, int index, ConfigurationOption option) throws BadCommandLineException {
		int recognized = 0;
		String arg = args[index];
		String argumentName = getArgumentName(option.optionName());

		if (arg.startsWith(argumentName)) {
			recognized++;

			try {
				if (arg.length() > argumentName.length()) {
					applyConfigurationOption(globalConfiguration, option, arg.substring(argumentName.length()).trim());
				}
				else {
					applyConfigurationOption(globalConfiguration, option, args[index + 1].trim());
					recognized++;
				}
			}
			catch (ClassNotFoundException e) {
				throw new BadCommandLineException("Invalid class", e);
			}
			catch (IOException e) {
				throw new BadCommandLineException("Failed to read from file", e);
			}
		}

		return recognized;
	}

	@Override
	public int parseArgument(Options opts, String[] args, int i) throws BadCommandLineException {
		initLoggerIfNecessary(opts);

		int recognized = 0;

		String arg = args[i];
		logger.trace("Argument[" + i + "] = " + arg);

		if (arg.equals(getArgumentName(ConfigurationOption.APPLY_PLURAL_FORM.optionName()))) {
			globalConfiguration.setApplyPluralForm(true);
			return 1;
		}
		else if ((recognized = parseArgument(args, i, ConfigurationOption.CONTROL)) == 0
		            && (recognized = parseArgument(args, i, ConfigurationOption.SUMMARY)) == 0
		            && (recognized = parseArgument(args, i, ConfigurationOption.COLLECTION_INTERFACE)) == 0 // longer option name comes first
		            && (recognized = parseArgument(args, i, ConfigurationOption.COLLECTION_IMPLEMENTATION)) == 0
		            && (recognized = parseArgument(args, i, ConfigurationOption.INSTANTIATION_MODE)) == 0) {
			if (arg.startsWith(getArgumentName(""))) {
				throw new BadCommandLineException("Invalid argument " + arg);
			}
		}

		return recognized;
	}

	private static void applyConfigurationOption(CommonConfiguration configuration, ConfigurationOption option,
	            String value) throws IOException, ClassNotFoundException {
		switch (option) {
		case CONTROL:
			if (!(configuration instanceof GlobalConfiguration)) {
				throw new IllegalArgumentException("The option " + option + " is not applicable");
			}
			((GlobalConfiguration) configuration).readControlFile(value);
			break;
		case SUMMARY:
			if (!(configuration instanceof GlobalConfiguration)) {
				throw new IllegalArgumentException("The option " + option + " is not applicable");
			}
			((GlobalConfiguration) configuration).initSummaryWriter(value);
			break;
		case COLLECTION_IMPLEMENTATION:
			configuration.setCollectionImplClass(Class.forName(value));
			break;
		case COLLECTION_INTERFACE:
			configuration.setCollectionInterfaceClass(Class.forName(value));
			break;
		case INSTANTIATION_MODE:
			try {
				configuration.setInstantiationMode(CommonConfiguration.InstantiationMode.valueOf(value.toUpperCase()));
			}
			catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("Unknown instantiation mode \"" + value + "\"");
			}
			break;
		case APPLY_PLURAL_FORM:
			configuration.setApplyPluralForm(Boolean.parseBoolean(value));
			break;
		case ANNOTATE:
			if (!(configuration instanceof ClassConfiguration)) {
				throw new IllegalArgumentException("The option " + option + " is not applicable");
			}
			((ClassConfiguration) configuration).setAnnotatable(Boolean.parseBoolean(value));
			break;
		}
	}

	/**
	 * Clone given configuration and apply settings from global/class/field JAXB customization.
	 */
	private <T extends CommonConfiguration> T applyConfigurationFromCustomizations(CommonConfiguration configuration,
	            CCustomizations customizations, boolean cloneClassConfiguration)
	            throws IOException, ClassNotFoundException {
		CPluginCustomization customization = customizations.find(XEW_QNAME.getNamespaceURI(), XEW_QNAME.getLocalPart());

		if (customization == null) {
			if (cloneClassConfiguration) {
				return (T) new ClassConfiguration(configuration);
			}

			return (T) configuration;
		}

		customization.markAsAcknowledged();

		NamedNodeMap attributes = customization.element.getAttributes();

		if (cloneClassConfiguration) {
			configuration = new ClassConfiguration(configuration);
		}

		for (int i = 0; i < attributes.getLength(); i++) {
			Node attribute = attributes.item(i);
			if (attribute.getNamespaceURI() == null) {
				applyConfigurationOption(configuration, ConfigurationOption.byOption(attribute.getNodeName()),
				            attribute.getNodeValue());
			}
		}

		return (T) configuration;
	}

	@Override
	public boolean run(Outline outline, Options opt, ErrorHandler errorHandler) throws SAXException {
		try {
			runInternal(outline);

			return true;
		}
		catch (IOException e) {
			logger.error("Failed to read the file", e);
			throw new SAXException(e);
		}
		catch (ClassNotFoundException e) {
			logger.error("Invalid class", e);
			throw new SAXException(e);
		}
	}

	private void runInternal(Outline outline) throws ClassNotFoundException, IOException {
		JCodeModel codeModel = outline.getCodeModel();
		JClass xmlElementWrapperModelClass = codeModel.ref(XmlElementWrapper.class);
		JClass xmlElementModelClass = codeModel.ref(XmlElement.class);
		JClass xmlAnyElementModelClass = codeModel.ref(XmlAnyElement.class);
		JClass xmlMixedModelClass = codeModel.ref(XmlMixed.class);
		JClass xmlElementRefModelClass = codeModel.ref(XmlElementRef.class);
		JClass xmlElementRefsModelClass = codeModel.ref(XmlElementRefs.class);
		JClass xmlElementsModelClass = codeModel.ref(XmlElements.class);
		JClass xmlJavaTypeAdapterModelClass = codeModel.ref(XmlJavaTypeAdapter.class);
		JClass xmlTypeModelClass = codeModel.ref(XmlType.class);
		xmlElementDeclModelClass = codeModel.ref(XmlElementDecl.class);

		Ring.begin();
		Ring.add(outline.getModel());

		logger.debug("JAXB Process Model (run)...");

		applyConfigurationFromCustomizations(globalConfiguration,
		            CustomizationUtils.getCustomizations(outline.getModel()), false);

		// Write summary information on the option for this compilation.
		writeSummary("Compilation:");
		writeSummary("  JAXB version         : " + Options.getBuildID());
		writeSummary("  Control file         : "
		            + ObjectUtils.defaultIfNull(globalConfiguration.getControlFileName(), "<none>"));
		writeSummary("  Summary file         : "
		            + ObjectUtils.defaultIfNull(globalConfiguration.getSummaryFileName(), "<none>"));
		writeSummary("  Instantiation mode   : " + globalConfiguration.getInstantiationMode());
		writeSummary("  Collection impl      : " + globalConfiguration.getCollectionImplClass().getName());
		writeSummary("  Collection interface : " + globalConfiguration.getCollectionInterfaceClass().getName());
		writeSummary("  Plural form          : " + globalConfiguration.isApplyPluralForm());
		writeSummary("");

		// Visit all classes generated by JAXB and find candidate classes for transformation.
		Map<String, Candidate> candidatesMap = new HashMap<String, Candidate>();

		// Write information on candidate classes to summary file.
		writeSummary("Candidates:");

		for (Iterator<Candidate> iter = findCandidateClasses(outline).iterator(); iter.hasNext();) {
			Candidate candidate = iter.next();

			if (globalConfiguration.isClassIncluded(candidate.getClassName())) {
				if (globalConfiguration.isClassUnmarkedForRemoval(candidate.getClassName())) {
					candidate.unmarkForRemoval();
					writeSummary("\t[!]: " + candidate.getClassName());
				}
				else {
					writeSummary("\t[+]: " + candidate.getClassName());
				}

				candidatesMap.put(candidate.getClassName(), candidate);
			}
			else {
				writeSummary("\t[-]: " + candidate.getClassName());
			}
		}

		writeSummary("\t" + candidatesMap.size() + " candidate(s) being considered.");
		writeSummary("");

		writeSummary("Modifications:");

		int modificationCount = 0;

		// Visit all classes again to check if the candidate is not eligible for removal:
		// * If there are classes that extend the candidate
		// * If there are class fields, that refer the candidate by e.g. @XmlElementRef annotation
		for (ClassOutline outlineClass : outline.getClasses()) {
			// Get the implementation class for the current class.
			JDefinedClass targetClass = outlineClass.implClass;

			ClassConfiguration classConfiguration = applyConfigurationFromCustomizations(globalConfiguration,
			            CustomizationUtils.getCustomizations(outlineClass), true);

			// We cannot remove candidates that have parent classes, but we can still substitute them:
			Candidate parentCandidate = candidatesMap.get(targetClass._extends().fullName());

			if (parentCandidate != null) {
				logger.debug("Candidate " + parentCandidate.getClassName() + " is a parent of " + targetClass.name()
				            + " and hence won't be removed.");
				parentCandidate.unmarkForRemoval();
			}

			// Visit all fields in this class.
			for (FieldOutline field : outlineClass.getDeclaredFields()) {
				// Only non-primitive fields are interesting.
				// Consider only PropertyKind.ELEMENT as (for example) PropertyKind.ATTRIBUTE (stands for XSD attribute) is always simple type:
				if (!(field.getRawType() instanceof JClass)
				            || !(field.getPropertyInfo() instanceof CElementPropertyInfo)) {
					continue;
				}

				final JClass fieldType = (JClass) field.getRawType();
				final CPropertyInfo fieldPropertyInfo = field.getPropertyInfo();
				String fieldName = fieldPropertyInfo.getName(false);
				Candidate candidate = null;

				for (Candidate c : candidatesMap.values()) {
					// Skip fields with basic types as for example any class can be casted to Object.
					if (fieldType.isAssignableFrom(c.getClazz()) && !isHiddenClass(fieldType)) {
						// If the given field has type T, it cannot be also in the list of parametrisations (e.g. T<T>).
						candidate = c;
						break;
					}
					// If the candidate T is referred from list of parametrisations (e.g. List<T>), it cannot be removed.
					// However field substitutions will take place.
					else if (isListedAsParametrisation(c.getClazz(), fieldType)) {
						logger.debug("Candidate " + c.getClassName() + " is listed as parametrisation of "
						            + targetClass.fullName() + "#" + fieldName + " and hence won't be removed.");
						c.unmarkForRemoval();
					}
				}

				final JFieldVar originalImplField = targetClass.fields().get(fieldName);

				if (candidate == null || !classConfiguration.isAnnotatable()) {
					checkAnnotationReference(candidatesMap, originalImplField);

					continue;
				}

				ClassConfiguration fieldConfiguration = applyConfigurationFromCustomizations(classConfiguration,
				            CustomizationUtils.getCustomizations(field), true);

				if (!fieldConfiguration.isAnnotatable()) {
					logger.debug("Field " + fieldName + " is excluded for processing.");
					candidate.unmarkForRemoval();

					continue;
				}

				// We have a candidate field to be replaced with a wrapped version. Report finding to summary file.
				writeSummary("\tReplacing field [" + fieldType.name() + " " + targetClass.fullName() + "#" + fieldName
				            + "]");
				candidate.incrementSubstitutions();
				modificationCount++;

				// The container class has to be deleted. Check that inner class has to be moved to it's parent.
				if (moveInnerClassToParent(outline, candidate)) {
					modificationCount++;
				}

				List<JClass> fieldTypeParametrisations = candidate.getFieldClass().getTypeParameters();

				// Create the new interface and collection classes using the specified interface and
				// collection classes (configuration) with an element type corresponding to
				// the element type from the collection present in the candidate class (narrowing).
				JClass collectionInterfaceClass = codeModel.ref(fieldConfiguration.getCollectionInterfaceClass())
				            .narrow(fieldTypeParametrisations);
				JClass collectionImplClass = codeModel.ref(fieldConfiguration.getCollectionImplClass())
				            .narrow(fieldTypeParametrisations);

				boolean pluralFormWasApplied = false;

				// Apply the plural form if there are no customizations. Assuming that customization is correct as may define the
				// plural form in more correct way, e.g. "field[s]OfScience" instead of "fieldOfScience[s]".
				if (fieldConfiguration.isApplyPluralForm() && !hasPropertyNameCustomization(fieldPropertyInfo)) {
					String oldFieldName = fieldName;

					// Taken from com.sun.tools.xjc.reader.xmlschema.ParticleBinder#makeJavaName():
					fieldName = JJavaName.getPluralForm(fieldName);

					// The field e.g. "return" was escaped as "_return", but after conversion to plural
					// it became valid Java identifier, so we remove the leading "_":
					if (fieldName.startsWith("_") && JJavaName.isJavaIdentifier(fieldName.substring(1))) {
						fieldName = fieldName.substring(1);
					}

					if (!fieldName.equals(oldFieldName)) {
						pluralFormWasApplied = true;

						originalImplField.name(fieldName);

						// Correct the @XmlType class-level annotation:
						JAnnotationArrayMember propOrderValue = (JAnnotationArrayMember) getAnnotation(targetClass,
						            xmlTypeModelClass).getAnnotationMembers().get("propOrder");

						if (propOrderValue != null) {
							for (JAnnotationValue annotationValue : propOrderValue.annotations()) {
								if (oldFieldName.equals(generableToString(annotationValue))) {
									setPrivateField(annotationValue, "value", JExpr.lit(fieldName));
									break;
								}
							}
						}
					}
				}

				// Transform the field accordingly.
				originalImplField.type(collectionInterfaceClass);

				// If instantiation is specified to be "early", add code for creating new instance of the collection class.
				if (fieldConfiguration.getInstantiationMode() == CommonConfiguration.InstantiationMode.EARLY) {
					logger.debug("Applying EARLY instantiation...");
					// GENERATED CODE: ... fieldName = new C<T>();
					originalImplField.init(JExpr._new(collectionImplClass));
				}

				// Annotate the field with the @XmlElementWrapper annotation using the original field name.
				JAnnotationUse xmlElementWrapperAnnotation = originalImplField.annotate(xmlElementWrapperModelClass);
				JAnnotationUse xmlElementOriginalAnnotation = getAnnotation(originalImplField, xmlElementModelClass);

				// xmlElementOriginalAnnotation can be null:
				JExpression wrapperXmlName = getAnnotationMemberExpression(xmlElementOriginalAnnotation, "name");
				if (wrapperXmlName != null) {
					xmlElementWrapperAnnotation.param("name", wrapperXmlName);
				}
				else if (fieldConfiguration.isApplyPluralForm()) {
					xmlElementWrapperAnnotation.param("name", getXsdDeclaration(fieldPropertyInfo).getName());
				}

				JExpression wrapperXmlRequired = getAnnotationMemberExpression(xmlElementOriginalAnnotation,
				            "required");
				if (wrapperXmlRequired != null) {
					xmlElementWrapperAnnotation.param("required", wrapperXmlRequired);
				}

				JExpression wrapperXmlNillable = getAnnotationMemberExpression(xmlElementOriginalAnnotation,
				            "nillable");
				if (wrapperXmlNillable != null) {
					xmlElementWrapperAnnotation.param("nillable", wrapperXmlNillable);
				}

				// Namespace of the wrapper element
				JExpression wrapperXmlNamespace = getAnnotationMemberExpression(xmlElementOriginalAnnotation,
				            "namespace");
				if (wrapperXmlNamespace != null) {
					xmlElementWrapperAnnotation.param("namespace", wrapperXmlNamespace);
				}

				if (xmlElementOriginalAnnotation != null) {
					removeAnnotation(originalImplField, xmlElementOriginalAnnotation);
				}

				boolean xmlElementInfoWasTransferred = false;

				// Transfer @XmlAnyElement, @XmlElementRefs, @XmlElements:
				for (JClass annotationModelClass : new JClass[] { xmlAnyElementModelClass, xmlMixedModelClass,
				        xmlElementRefModelClass, xmlElementRefsModelClass, xmlElementsModelClass }) {
					JAnnotationUse annotation = getAnnotation(candidate.getField(), annotationModelClass);

					if (annotation != null) {
						if (candidate.getFieldTargetNamespace() != null) {
							JAnnotationArrayMember annotationArrayMember = (JAnnotationArrayMember) getAnnotationMember(
							            annotation, "value");

							if (annotationArrayMember != null) {
								for (JAnnotationUse subAnnotation : annotationArrayMember.annotations()) {
									if (getAnnotationMemberExpression(subAnnotation, "namespace") == null) {
										subAnnotation.param("namespace", candidate.getFieldTargetNamespace());
									}
								}
							}
						}

						xmlElementInfoWasTransferred = true;

						addAnnotation(originalImplField, annotation);
					}
				}

				if (!xmlElementInfoWasTransferred) {
					// Annotate the field with the @XmlElement annotation using the field name from the wrapped type as name.
					// We cannot just re-use the same annotation object instance, as for example, we need to set XML name and this
					// will impact the candidate field annotation in case candidate is unmarked from removal.
					JAnnotationUse xmlElementAnnotation = originalImplField.annotate(xmlElementModelClass);
					JAnnotationUse xmlElementCandidateAnnotation = getAnnotation(candidate.getField(),
					            xmlElementModelClass);

					// xmlElementOriginalAnnotation can be null:
					JExpression xmlName = getAnnotationMemberExpression(xmlElementCandidateAnnotation, "name");
					if (xmlName != null) {
						xmlElementAnnotation.param("name", xmlName);
					}
					else {
						xmlElementAnnotation.param("name", candidate.getFieldName());
					}

					JExpression xmlNamespace = getAnnotationMemberExpression(xmlElementCandidateAnnotation,
					            "namespace");
					if (xmlNamespace != null) {
						xmlElementAnnotation.param("namespace", xmlNamespace);
					}
					else if (candidate.getFieldTargetNamespace() != null) {
						xmlElementAnnotation.param("namespace", candidate.getFieldTargetNamespace());
					}

					JExpression type = getAnnotationMemberExpression(xmlElementCandidateAnnotation, "type");
					if (type != null) {
						xmlElementAnnotation.param("type", type);
					}

					JExpression required = getAnnotationMemberExpression(xmlElementCandidateAnnotation, "defaultValue");
					if (required != null) {
						xmlElementAnnotation.param("defaultValue", required);
					}

					JExpression nillable = getAnnotationMemberExpression(xmlElementCandidateAnnotation, "nillable");
					if (nillable != null) {
						xmlElementAnnotation.param("nillable", nillable);
					}
				}

				JAnnotationUse adapterAnnotation = getAnnotation(candidate.getField(), xmlJavaTypeAdapterModelClass);

				if (adapterAnnotation != null) {
					addAnnotation(originalImplField, adapterAnnotation);
				}

				// Same as fieldName, but used as getter/setter method name:
				String propertyName = fieldPropertyInfo.getName(true);

				JDefinedClass implementationInterface = null;

				for (Iterator<JClass> iter = targetClass._implements(); iter.hasNext();) {
					JClass interfaceClass = iter.next();

					// If value class implements some JVM interface it is not considered as such interface cannot be modified:
					if (interfaceClass instanceof JDefinedClass
					            && deleteSettersGetters((JDefinedClass) interfaceClass, propertyName)) {
						implementationInterface = (JDefinedClass) interfaceClass;
						break;
					}
				}

				// Find original getter and setter methods to remove.
				deleteSettersGetters(targetClass, propertyName);

				// The type in property info should correspond to field type. For that we clone the candidate property info:
				CPropertyInfo candidateFieldPropertyInfo = candidate.getFieldPropertyInfo();
				CPropertyInfo propertyInfoClone = null;

				if (candidateFieldPropertyInfo instanceof CElementPropertyInfo) {
					propertyInfoClone = new CElementPropertyInfo("", CollectionMode.NOT_REPEATED, null, null, null,
					            null, null, false);
				}
				else if (candidateFieldPropertyInfo instanceof CReferencePropertyInfo) {
					propertyInfoClone = new CReferencePropertyInfo("", false, false, false, null, null, null, false,
					            false, false);
				}
				else {
					// There could be no other option as candidate field is a collection, hence not simple property.
					assert false;
				}

				copyFields(candidateFieldPropertyInfo, propertyInfoClone);

				if (pluralFormWasApplied) {
					propertyName = JJavaName.getPluralForm(propertyName);
				}

				propertyInfoClone.setName(false, fieldName);
				propertyInfoClone.setName(true, propertyName);

				setPrivateField(field, "prop", propertyInfoClone);

				// Add a new getter method returning the (wrapped) field added.
				// GENERATED CODE: public I<T> getFieldName() { ... return fieldName; }
				JMethod getterMethod = targetClass.method(JMod.PUBLIC, collectionInterfaceClass, "get" + propertyName);

				if (fieldConfiguration.getInstantiationMode() == CommonConfiguration.InstantiationMode.LAZY) {
					logger.debug("Applying LAZY instantiation...");
					// GENERATED CODE: if (fieldName == null) fieldName = new C<T>();
					getterMethod.body()._if(JExpr.ref(fieldName).eq(JExpr._null()))._then().assign(JExpr.ref(fieldName),
					            JExpr._new(collectionImplClass));
				}

				// GENERATED CODE: return "fieldName";
				getterMethod.body()._return(JExpr.ref(fieldName));

				// Add a new setter method:
				// GENERATED CODE: public void setFieldName(I<T> fieldName) { this.fieldName = fieldName; }
				JMethod setterMethod = targetClass.method(JMod.PUBLIC, codeModel.VOID, "set" + propertyName);

				setterMethod.body().assign(JExpr._this().ref(fieldName),
				            setterMethod.param(collectionInterfaceClass, fieldName));

				// Modify interface as well:
				if (implementationInterface != null) {
					writeSummary("\tCorrecting interface " + implementationInterface.fullName());

					implementationInterface.method(JMod.PUBLIC, collectionInterfaceClass, "get" + propertyName);
					setterMethod = implementationInterface.method(JMod.PUBLIC, codeModel.VOID, "set" + propertyName);
					setterMethod.param(collectionInterfaceClass, fieldName);
				}

				modificationCount += createScopedFactoryMethods(codeModel, candidate.getValueObjectFactoryClass(),
				            candidate.getScopedElementInfos().values(), targetClass);

				if (candidate.isValueObjectDisabled()) {
					modificationCount += createScopedFactoryMethods(codeModel, candidate.getObjectFactoryClass(),
					            candidate.getScopedElementInfos().values(), targetClass);
				}
			}
		}

		writeSummary("\t" + modificationCount + " modification(s) to original code.");
		writeSummary("");

		int deletionCount = deleteCandidates(outline, candidatesMap.values());

		writeSummary("\t" + deletionCount + " deletion(s) from original code.");
		writeSummary("");

		globalConfiguration.closeSummary();

		Ring.end(null);

		logger.debug("Done");
	}

	/**
	 * If candidate class contains the inner class which is collection parametrisation (type), then this inner class has
	 * to be moved to top class. For example from<br>
	 * {@code TypeClass (is a collection type) -> ContainerClass (marked for removal) -> ElementClass}<br>
	 * we need to get<br>
	 * {@code TypeClass -> ElementClass}.<br>
	 * Also this move should be reflected on factory method names.
	 */
	private boolean moveInnerClassToParent(Outline outline, Candidate candidate) {
		// Skip basic parametrisations like "List<String>":
		if (candidate.getFieldParametrisationClass() == null) {
			return false;
		}

		JDefinedClass fieldParametrisationImpl = candidate.getFieldParametrisationImpl();

		if (candidate.getClazz() != fieldParametrisationImpl.parentContainer()) {
			// Field parametrisation class is not inner class of the candidate:
			return false;
		}

		JDefinedClass fieldParametrisationClass = candidate.getFieldParametrisationClass();

		String oldFactoryMethodName = fieldParametrisationClass.outer().name() + fieldParametrisationClass.name();

		moveClassLevelUp(outline, fieldParametrisationImpl);

		renameFactoryMethod(fieldParametrisationImpl._package()._getClass(FACTORY_CLASS_NAME), oldFactoryMethodName,
		            fieldParametrisationClass.name());

		if (candidate.isValueObjectDisabled()) {
			moveClassLevelUp(outline, fieldParametrisationClass);

			renameFactoryMethod(fieldParametrisationClass._package()._getClass(FACTORY_CLASS_NAME),
			            oldFactoryMethodName, fieldParametrisationClass.name());
		}

		return true;
	}

	/**
	 * Create factory methods with a new scope for elements that should be scoped.
	 * 
	 * @param targetClass
	 *            the class that is applied the transformation of properties
	 * @return number of created methods
	 * @see com.sun.tools.xjc.generator.bean.ObjectFactoryGenerator
	 */
	private int createScopedFactoryMethods(JCodeModel codeModel, JDefinedClass factoryClass,
	            Collection<ScopedElementInfo> scopedElementInfos, JDefinedClass targetClass) {
		int createdMethods = 0;

		NEXT: for (ScopedElementInfo info : scopedElementInfos) {
			String dotClazz = targetClass.fullName() + ".class";

			// First check that such factory method has not yet been created. It can be the case if target class
			// is substituted with e.g. two candidates, each candidate having a field with the same name.
			// FIXME: Could it be the case that these two fields have different namespaces?
			for (JMethod method : factoryClass.methods()) {
				JAnnotationUse xmlElementDeclAnnotation = getAnnotation(method, xmlElementDeclModelClass);

				JExpression scope = getAnnotationMemberExpression(xmlElementDeclAnnotation, "scope");
				JExpression name = getAnnotationMemberExpression(xmlElementDeclAnnotation, "name");

				if (scope != null && dotClazz.equals(generableToString(scope))
				            && generableToString(info.name).equals(generableToString(name))) {
					continue NEXT;
				}
			}

			// Generate the scoped factory method:
			//   @XmlElementDecl(..., scope = T.class)
			//   public JAXBElement<X> createT...(X value) { return new JAXBElement<...>(QNAME, X.class, T.class, value); }
			StringBuilder methodName = new StringBuilder();

			JDefinedClass container = targetClass;

			while (true) {
				methodName.insert(0, container.name());

				if (container.parentContainer().isClass()) {
					container = (JDefinedClass) container.parentContainer();
				}
				else {
					break;
				}
			}

			methodName.insert(0, "create").append(NameConverter.standard.toPropertyName(generableToString(info.name)));

			JClass wrapperType = codeModel.ref(JAXBElement.class).narrow(info.type);

			JMethod method = factoryClass.method(JMod.PUBLIC, wrapperType, methodName.toString());

			method.annotate(xmlElementDeclModelClass).param("namespace", info.namespace).param("name", info.name)
			            .param("scope", targetClass);

			// FIXME: Make a try to load constants and (a) rename it appropriately (b) use it?
			JInvocation qname = JExpr._new(codeModel.ref(QName.class)).arg(info.namespace).arg(info.name);

			method.body()._return(JExpr._new(wrapperType).arg(qname).arg(info.type.boxify().dotclass())
			            .arg(targetClass.dotclass()).arg(method.param(info.type, "value")));

			createdMethods++;
		}

		return createdMethods;
	}

	/**
	 * Locate the candidates classes for substitution/removal.
	 * 
	 * @return a map className -> Candidate
	 */
	private Collection<Candidate> findCandidateClasses(Outline outline) {
		Map<String, ClassOutline> interfaceImplementations = new HashMap<String, ClassOutline>();

		// Visit all classes to create a map "interfaceName -> ClassOutline".
		// This map is later used to resolve implementations from interfaces.
		for (ClassOutline classOutline : outline.getClasses()) {
			for (Iterator<JClass> iter = classOutline.implClass._implements(); iter.hasNext();) {
				JClass interfaceClass = iter.next();

				if (interfaceClass instanceof JDefinedClass) {
					// Don't care if some interfaces collide: value classes have exactly one implementation
					interfaceImplementations.put(interfaceClass.fullName(), classOutline);
				}
			}
		}

		Collection<Candidate> candidates = new ArrayList<Candidate>();

		JClass collectionModelClass = outline.getCodeModel().ref(Collection.class);
		JClass xmlSchemaModelClass = outline.getCodeModel().ref(XmlSchema.class);

		// Visit all classes created by JAXB processing to collect all potential wrapper classes to be removed:
		for (ClassOutline classOutline : outline.getClasses()) {
			JDefinedClass candidateClass = classOutline.implClass;

			// * The candidate class should not extend any other model class (as the total number of properties in this case will be more than 1)
			if (!isHiddenClass(candidateClass._extends())) {
				continue;
			}

			JFieldVar field = null;

			// * The candidate class should have exactly one property
			for (JFieldVar f : candidateClass.fields().values()) {
				if ((f.mods().getValue() & JMod.STATIC) == JMod.STATIC) {
					continue;
				}

				// If there are at least two non-static fields, we discard this candidate:
				if (field != null) {
					field = null;
					break;
				}

				field = f;
			}

			// "field" is null if there are no fields (or all fields are static) or there are more then two fields.
			// The only property should be a collection, hence it should be class:
			if (field == null || !(field.type() instanceof JClass)) {
				continue;
			}

			JClass fieldType = (JClass) field.type();

			// * The property should be a collection
			if (!collectionModelClass.isAssignableFrom(fieldType)) {
				continue;
			}

			List<JClass> fieldParametrisations = fieldType.getTypeParameters();

			// FIXME: All known collections have exactly one parametrisation type.
			assert fieldParametrisations.size() == 1;

			JDefinedClass fieldParametrisationClass = null;
			JDefinedClass fieldParametrisationImpl = null;

			// Parametrisations like "List<String>" or "List<Serialazable>" are not considered.
			// They are substituted as is and do not require moving of classes.
			if (fieldParametrisations.get(0) instanceof JDefinedClass) {
				fieldParametrisationClass = (JDefinedClass) fieldParametrisations.get(0);

				ClassOutline fieldParametrisationClassOutline = interfaceImplementations
				            .get(fieldParametrisationClass.fullName());

				if (fieldParametrisationClassOutline != null) {
					assert fieldParametrisationClassOutline.ref == fieldParametrisationClass;

					fieldParametrisationImpl = fieldParametrisationClassOutline.implClass;
				}
				else {
					fieldParametrisationImpl = fieldParametrisationClass;
				}
			}

			JDefinedClass objectFactoryClass = null;

			// If class has a non-hidden interface, then there is object factory in another package.
			for (Iterator<JClass> iter = candidateClass._implements(); iter.hasNext();) {
				JClass interfaceClass = iter.next();

				if (!isHiddenClass(interfaceClass)) {
					objectFactoryClass = interfaceClass._package()._getClass(FACTORY_CLASS_NAME);

					if (objectFactoryClass != null) {
						break;
					}
				}
			}

			JDefinedClass valueObjectFactoryClass = candidateClass._package()._getClass(FACTORY_CLASS_NAME);

			assert objectFactoryClass != valueObjectFactoryClass;

			String fieldTargetNamespace = null;

			XSDeclaration xsdDeclaration = getXsdDeclaration(classOutline.target.getProperty(field.name()));

			if (xsdDeclaration != null && !xsdDeclaration.getTargetNamespace().isEmpty()) {
				fieldTargetNamespace = xsdDeclaration.getTargetNamespace();
			}
			else {
				// Default (mostly used) namespace is generated as annotation for the package,
				// see com.sun.tools.xjc.generator.bean.PackageOutlineImpl#calcDefaultValues()
				JAnnotationUse schemaAnnotation = getAnnotation(
				            (objectFactoryClass != null ? objectFactoryClass : valueObjectFactoryClass).getPackage(),
				            xmlSchemaModelClass);
				JExpression elementFormDefault = getAnnotationMemberExpression(schemaAnnotation, "elementFormDefault");

				if (elementFormDefault != null && generableToString(elementFormDefault).endsWith(".QUALIFIED")) {
					JExpression packageWideNamespace = getAnnotationMemberExpression(schemaAnnotation, "namespace");

					if (packageWideNamespace != null) {
						fieldTargetNamespace = generableToString(packageWideNamespace);
					}
				}
			}

			// We have a candidate class:
			Candidate candidate = new Candidate(candidateClass, classOutline.target, field, fieldTargetNamespace,
			            fieldParametrisationClass, fieldParametrisationImpl, objectFactoryClass,
			            valueObjectFactoryClass, xmlElementDeclModelClass);
			candidates.add(candidate);

			logger.debug("Found " + candidate);
		}

		return candidates;
	}

	/**
	 * Delete all candidate classes together with setter/getter methods and helper methods from
	 * <code>ObjectFactory</code>.
	 * 
	 * @return the number of deletions performed
	 */
	private int deleteCandidates(Outline outline, Collection<Candidate> candidates) {
		int deletionCount = 0;

		writeSummary("Deletions:");

		// Visit all candidate classes.
		for (Candidate candidate : candidates) {
			if (!candidate.canBeRemoved()) {
				continue;
			}

			// Get the defined class for candidate class.
			JDefinedClass candidateClass = candidate.getClazz();

			deletionCount += deleteFactoryMethod(candidate.getValueObjectFactoryClass(), candidate);

			deleteClass(outline, candidateClass);
			deletionCount++;

			// Replay the same for interface:
			if (candidate.isValueObjectDisabled()) {
				deletionCount += deleteFactoryMethod(candidate.getObjectFactoryClass(), candidate);

				for (Iterator<JClass> iter = candidateClass._implements(); iter.hasNext();) {
					JClass interfaceClass = iter.next();

					if (!isHiddenClass(interfaceClass)) {
						deleteClass(outline, (JDefinedClass) interfaceClass);
						deletionCount++;
					}
				}
			}
            
            candidate.getField();
		}
        
        

		return deletionCount;
	}

	//
	// Model factory manipulation helpers.
	//

	/**
	 * Rename methods in factory class: {@code createABC() -> createAC()}.
	 */
	private void renameFactoryMethod(JDefinedClass factoryClass, String oldMethodName, String newMethodName) {
		for (JMethod method : factoryClass.methods()) {
			String methodName = method.name();

			if (!methodName.contains(oldMethodName)) {
				continue;
			}

			method.name(methodName.replace(oldMethodName, newMethodName));

			writeSummary("\tRenamed " + methodName + " -> " + method.name() + " in " + factoryClass.fullName());
		}
	}

	/**
	 * Remove method {@code ObjectFactory} that creates an object of a given {@code clazz}.
	 * 
	 * @return {@code 1} if such method was successfully located and removed
	 */
	private int deleteFactoryMethod(JDefinedClass factoryClass, Candidate candidate) {
		int deletedMethods = 0;

		for (Iterator<JMethod> iter = factoryClass.methods().iterator(); iter.hasNext();) {
			JMethod method = iter.next();

			// Remove the methods:
			// * public T createT() { return new T(); }
			// * public JAXBElement<T> createT(T value) { return new JAXBElement<T>(QNAME, T.class, null, value); }
			// * @XmlElementDecl(..., scope = X.class)
			//   public JAXBElement<T> createT...(T value) { return new JAXBElement<...>(QNAME, T.class, X.class, value); }
			if ((method.type() instanceof JDefinedClass
			            && ((JDefinedClass) method.type()).isAssignableFrom(candidate.getClazz()))
			            || isListedAsParametrisation(candidate.getClazz(), method.type())
			            || candidate.getScopedElementInfos().containsKey(method.name())) {
				writeSummary("\tRemoving factory method [" + method.type().fullName() + "#" + method.name()
				            + "()] from " + factoryClass.fullName());
				iter.remove();

				deletedMethods++;
			}
		}

		return deletedMethods;
	}

	//
	// Model manipulation helpers.
	//

	/**
	 * Returns {@code true} if setter/getter with given public name was successfully removed from given class/interface.
	 */
	private boolean deleteSettersGetters(JDefinedClass clazz, String fieldPublicName) {
		boolean result = false;

		for (Iterator<JMethod> iter = clazz.methods().iterator(); iter.hasNext();) {
			JMethod m = iter.next();

			if (m.name().equals("set" + fieldPublicName) || m.name().equals("get" + fieldPublicName)) {
				iter.remove();
				result = true;
			}
		}

		return result;
	}

	/**
	 * Move the given class to his grandparent (either class or package). The given {@code clazz} should be inner class.
	 */
	private void moveClassLevelUp(Outline outline, JDefinedClass clazz) {
		// Modify the container so it now refers the class. Container can be a class or package.
		JDefinedClass parent = (JDefinedClass) clazz.parentContainer();
		JClassContainer grandParent = parent.parentContainer();
		Map<String, JDefinedClass> classes;

		// FIXME: Pending https://java.net/jira/browse/JAXB-957
		if (grandParent.isClass()) {
			// Element class should be added as its container child:
			JDefinedClass grandParentClass = (JDefinedClass) grandParent;

			writeSummary("\tMoving inner class " + clazz.fullName() + " to class " + grandParentClass.fullName());

			classes = getPrivateField(grandParentClass, "classes");
		}
		else {
			JPackage grandParentPackage = (JPackage) grandParent;

			writeSummary("\tMoving inner class " + clazz.fullName() + " to package " + grandParentPackage.name());

			classes = getPrivateField(grandParentPackage, "classes");

			// In this scenario class should have "static" modifier reset otherwise it won't compile:
			setPrivateField(clazz.mods(), "mods", Integer.valueOf(clazz.mods().getValue() & ~JMod.STATIC));

			for (ClassOutline classOutline : outline.getClasses()) {
				if (classOutline.implClass == clazz) {
					XSComponent sc = classOutline.target.getSchemaComponent();

					// FIXME: Inner class is always a local declaration.
					assert (sc instanceof XSDeclaration && ((XSDeclaration) sc).isLocal());

					setPrivateField(sc, "anonymous", Boolean.FALSE);

					break;
				}
			}
		}

		if (classes.containsKey(clazz.name())) {
			writeSummary("\tRenaming class " + clazz.fullName() + " to class " + parent.name() + clazz.name());
			setPrivateField(clazz, "name", parent.name() + clazz.name());
		}

		classes.put(clazz.name(), clazz);

		// Finally modify the class so that it refers back the container:
		setPrivateField(clazz, "outer", grandParent);
	}

	/**
	 * Remove the given class from it's parent class or package it is defined in.
	 */
	private void deleteClass(Outline outline, JDefinedClass clazz) {
		if (clazz.parentContainer().isClass()) {
			// The candidate class is an inner class. Remove the class from its parent class.
			JDefinedClass parentClass = (JDefinedClass) clazz.parentContainer();

			writeSummary("\tRemoving class " + clazz.fullName() + " from class " + parentClass.fullName());

			for (Iterator<JDefinedClass> iter = parentClass.classes(); iter.hasNext();) {
				if (iter.next().equals(clazz)) {
					iter.remove();
					break;
				}
			}
		}
		else {
			// The candidate class is in a package. Remove the class from the package.
			JPackage parentPackage = (JPackage) clazz.parentContainer();

			writeSummary("\tRemoving class " + clazz.fullName() + " from package " + parentPackage.name());

			parentPackage.remove(clazz);

			// And also remove the class from model.
			for (Iterator<? extends ClassOutline> iter = outline.getClasses().iterator(); iter.hasNext();) {
				ClassOutline classOutline = iter.next();
				if (classOutline.implClass == clazz) {
					outline.getModel().beans().remove(classOutline.target);
					Set<Object> packageClasses = getPrivateField(classOutline._package(), "classes");
					packageClasses.remove(classOutline);
					iter.remove();
					break;
				}
			}
		}
	}

	/**
	 * For the given annotatable check that all annotations (and all annotations within annotations recursively) do not
	 * refer any candidate for removal.
	 */
	private void checkAnnotationReference(Map<String, Candidate> candidatesMap, JAnnotatable annotatable) {
		for (JAnnotationUse annotation : annotatable.annotations()) {
			JAnnotationValue annotationMember = getAnnotationMember(annotation, "value");

			if (annotationMember instanceof JAnnotationArrayMember) {
				checkAnnotationReference(candidatesMap, (JAnnotationArrayMember) annotationMember);

				continue;
			}

			JExpression type = getAnnotationMemberExpression(annotation, "type");

			if (type == null) {
				// Can be the case for @XmlElement(name = "publication-reference", namespace = "http://mycompany.org/exchange")
				// or any other annotation without "type" 
				continue;
			}

			Candidate candidate = candidatesMap.get(generableToString(type).replace(".class", ""));

			if (candidate != null) {
				logger.debug("Candidate " + candidate.getClassName()
				            + " is used in XmlElements/XmlElementRef and hence won't be removed.");
				candidate.unmarkForRemoval();
			}
		}
	}

	private void writeSummary(String s) {
		globalConfiguration.writeSummary(s);
	}

	/**
	 * Container for information relative to scoped elements.
	 */
	private static class ScopedElementInfo {
		/**
		 * Element name ("post-office").
		 */
		public JExpression name;

		/**
		 * Element namespace ("http://foo.bar").
		 */
		public JExpression namespace;

		/**
		 * Element type ({@link String}).
		 */
		public JType	   type;

		public ScopedElementInfo(JExpression name, JExpression namespace, JType type) {
			this.name = name;
			this.namespace = namespace;
			this.type = type;
		}

		@Override
		public String toString() {
			return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}
	}

	/**
	 * Describes the collection container class - a candidate for removal. This class class has only one field -
	 * collection of objects.
	 */
	private static class Candidate {
		private final JDefinedClass					 candidateClass;

		private final JFieldVar						 field;

		private final CPropertyInfo					 fieldPropertyInfo;

		private final String						 fieldTargetNamespace;

		private final JDefinedClass					 fieldParametrisationClass;

		private final JDefinedClass					 fieldParametrisationImpl;

		private final JDefinedClass					 objectFactoryClass;

		private final JDefinedClass					 valueObjectFactoryClass;

		private final Map<String, ScopedElementInfo> scopedElementInfos	= new HashMap<String, ScopedElementInfo>();

		/**
		 * By default the candidate is marked for removal unless something prevents it from being removed.
		 */
		private boolean								 markedForRemoval	= true;

		/**
		 * Number of times this candidate has been substituted in the model.
		 */
		private int									 substitutionsCount;

		Candidate(JDefinedClass candidateClass, CClassInfo candidateClassInfo, JFieldVar field,
		            String fieldTargetNamespace, JDefinedClass fieldParametrizationClass,
		            JDefinedClass fieldParametrisationImpl, JDefinedClass objectFactoryClass,
		            JDefinedClass valueObjectFactoryClass, JClass xmlElementDeclModelClass) {
			this.candidateClass = candidateClass;
			this.field = field;
			this.fieldPropertyInfo = candidateClassInfo.getProperty(field.name());
			this.fieldTargetNamespace = fieldTargetNamespace;
			this.fieldParametrisationClass = fieldParametrizationClass;
			this.fieldParametrisationImpl = fieldParametrisationImpl;
			this.objectFactoryClass = objectFactoryClass;
			this.valueObjectFactoryClass = valueObjectFactoryClass;
            
			String dotClazz = candidateClass.fullName() + ".class";

			for (JMethod method : valueObjectFactoryClass.methods()) {
				JAnnotationUse xmlElementDeclAnnotation = getAnnotation(method, xmlElementDeclModelClass);
				JExpression scope = getAnnotationMemberExpression(xmlElementDeclAnnotation, "scope");

				if (scope == null || !dotClazz.equals(generableToString(scope))) {
					continue;
				}

				scopedElementInfos.put(method.name(),
				            new ScopedElementInfo(getAnnotationMemberExpression(xmlElementDeclAnnotation, "name"),
				                        getAnnotationMemberExpression(xmlElementDeclAnnotation, "namespace"),
				                        method.params().get(0).type()));
			}
            
            this.fieldPropertyInfo.getCustomizations().addAll(candidateClassInfo.getCustomizations());
		}

		/**
		 * Container class
		 */
		public JDefinedClass getClazz() {
			return candidateClass;
		}

		/**
		 * Container class name
		 */
		public String getClassName() {
			return candidateClass.fullName();
		}

		/**
		 * The only field in container class (collection property).
		 */
		public JFieldVar getField() {
			return field;
		}

		/**
		 * The name of the only field in container class.
		 */
		public String getFieldName() {
			return field.name();
		}

		/**
		 * The class of the only field in container class (collection interface or concrete implementation).
		 */
		public JClass getFieldClass() {
			return (JClass) field.type();
		}

		/**
		 * The corresponding property info of the only field in container class.
		 */
		public CPropertyInfo getFieldPropertyInfo() {
			return fieldPropertyInfo;
		}

		/**
		 * The XSD namespace of the property associated with a field.
		 */
		public String getFieldTargetNamespace() {
			return fieldTargetNamespace;
		}

		/**
		 * The only parametrisation class of the field (collection type). In case of basic parametrisation like
		 * {@link List<String>} this property is {@code null}.
		 */
		public JDefinedClass getFieldParametrisationClass() {
			return fieldParametrisationClass;
		}

		/**
		 * If {@link #getFieldParametrisationClass()} is an interface, then this holds the same value. Otherwise it
		 * holds the implementation (value object) of {@link #getFieldParametrisationClass()}. In case of basic
		 * parametrisation like {@code List<String>} this property is {@code null}.
		 */
		public JDefinedClass getFieldParametrisationImpl() {
			return fieldParametrisationImpl;
		}

		/**
		 * Return information about scoped elements, that have this candidate as a scope.
		 * 
		 * @return object factory method name -to- element info map
		 */
		public Map<String, ScopedElementInfo> getScopedElementInfos() {
			return scopedElementInfos;
		}

		/**
		 * Object Factory class for interface classes. It's usually located in {@code impl.} subpackage relative to
		 * {@code valueObjectFactoryClass} package. May be {@code null}.
		 */
		public JDefinedClass getObjectFactoryClass() {
			return objectFactoryClass;
		}

		/**
		 * Object Factory class for value (implementation) classes. Is not {@code null}.
		 */
		public JDefinedClass getValueObjectFactoryClass() {
			return valueObjectFactoryClass;
		}

		/**
		 * Returns {@code true} if the setting {@code <jaxb:globalBindings generateValueClass="false">} is active.
		 */
		public boolean isValueObjectDisabled() {
			return objectFactoryClass != null;
		}

		/**
		 * Has given candidate green light to be removed?
		 */
		public boolean canBeRemoved() {
			return markedForRemoval && substitutionsCount > 0;
		}

		/**
		 * Increments number of substitutions for this candidate.
		 */
		public void incrementSubstitutions() {
			substitutionsCount++;
		}

		/**
		 * Signal that this candidate should not be removed from model on some reason.
		 */
		public void unmarkForRemoval() {
			this.markedForRemoval = false;
		}

		@Override
		public String toString() {
			return "Candidate[" + getClassName() + " field " + getFieldClass().name() + " " + getFieldName() + "]";
		}
	}
}
