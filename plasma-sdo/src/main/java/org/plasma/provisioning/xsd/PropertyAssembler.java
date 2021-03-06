/**
 *         PlasmaSDO™ License
 * 
 * This is a community release of PlasmaSDO™, a dual-license 
 * Service Data Object (SDO) 2.1 implementation. 
 * This particular copy of the software is released under the 
 * version 2 of the GNU General Public License. PlasmaSDO™ was developed by 
 * TerraMeta Software, Inc.
 * 
 * Copyright (c) 2013, TerraMeta Software, Inc. All rights reserved.
 * 
 * General License information can be found below.
 * 
 * This distribution may include materials developed by third
 * parties. For license and attribution notices for these
 * materials, please refer to the documentation that accompanies
 * this distribution (see the "Licenses for Third-Party Components"
 * appendix) or view the online documentation at 
 * <http://plasma-sdo.org/licenses/>.
 *  
 */
package org.plasma.provisioning.xsd;

import java.math.BigInteger;
import java.util.UUID;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plasma.config.PlasmaConfig;
import org.plasma.provisioning.Alias;
import org.plasma.provisioning.Body;
import org.plasma.provisioning.Class;
import org.plasma.provisioning.ClassRef;
import org.plasma.provisioning.DataTypeRef;
import org.plasma.provisioning.Documentation;
import org.plasma.provisioning.DocumentationType;
import org.plasma.provisioning.NameUtils;
import org.plasma.provisioning.Property;
import org.plasma.provisioning.Sort;
import org.plasma.provisioning.ValueConstraint;
import org.plasma.provisioning.VisibilityType;
import org.plasma.provisioning.XmlNodeType;
import org.plasma.provisioning.XmlProperty;
import org.plasma.sdo.DataType;
import org.plasma.xml.schema.AbstractComplexType;
import org.plasma.xml.schema.AbstractSimpleType;
import org.plasma.xml.schema.Attribute;
import org.plasma.xml.schema.ExplicitGroup;
import org.plasma.xml.schema.LocalComplexType;
import org.plasma.xml.schema.LocalElement;
import org.plasma.xml.schema.LocalSimpleType;
import org.plasma.xml.schema.Restriction;
import org.plasma.xml.schema.SchemaConstants;
import org.plasma.xml.schema.SimpleType;
import org.plasma.xml.schema.XSDBuiltInType;

public class PropertyAssembler extends AbstractAssembler {
	private static Log log = LogFactory.getLog(
			PropertyAssembler.class); 
	
	private QName appNamespaceQName;
	
	public PropertyAssembler(ConverterSupport converterSupport,
	    QName appNamespaceQName) {
		
		super(converterSupport.getDestNamespaceURI(),
			   converterSupport.getDestNamespacePrefix(),
				converterSupport);
		this.appNamespaceQName = appNamespaceQName;
	}	

	/**
	 * Build a property from the given XDS structures.
     * Max occurs and min-occurs values are taken 
     * from the local element, child group, and then parent group
     * in that order and if still null are given the XSD defaults.  
	 * @param clss the provisioning class
	 * @param complexType the XSD complex type
	 * @param explicitGroup the XSD parent group
	 * @param childExplicitGroup the XSD child group, may be null
	 * @param element the local element
	 * @param sequenceNum the group sequence
	 * @return the property
	 */
	public Property buildProperty(Class clss, AbstractComplexType complexType, 
			ExplicitGroup explicitGroup, ExplicitGroup childExplicitGroup, 
			LocalElement element, int sequenceNum)
    {
        Property property = new Property();
        property.setId(UUID.randomUUID().toString());
		XmlProperty xmlProp = new XmlProperty();
		xmlProp.setNodeType(XmlNodeType.ELEMENT);
		property.setXmlProperty(xmlProp);
        Documentation documentation = createDocumentation(
            	DocumentationType.DEFINITION,
            	getDocumentationContent(element));
		property.getDocumentations().add(documentation);

        property.setVisibility(VisibilityType.PUBLIC); 
        
        // set up multiplicity
        String maxOccurs = "1"; // the default
        if (element.hasMaxOccurs()) {
        	maxOccurs = element.getMaxOccurs();
        }
        else if (childExplicitGroup != null && childExplicitGroup.hasMaxOccurs()) {
        	maxOccurs = childExplicitGroup.getMaxOccurs();
        }
        else if (explicitGroup.hasMaxOccurs()) {
        	maxOccurs = explicitGroup.getMaxOccurs();
        }
        property.setMany("unbounded".equals(maxOccurs) ||
            	(!"0".equals(maxOccurs) && !"1".equals(maxOccurs)));
        	
        // set up nullable/required
        BigInteger minOccurs = BigInteger.valueOf(1); // the default
        if (element.hasMinOccurs()) {
        	minOccurs = element.getMinOccurs();
        }
        else if (childExplicitGroup != null && childExplicitGroup.hasMinOccurs()) {
        	minOccurs = childExplicitGroup.getMinOccurs();
        }
        else if (explicitGroup.hasMinOccurs()) {
        	minOccurs = explicitGroup.getMinOccurs();
        }
        if ("1".equals(String.valueOf(minOccurs)))
    	    property.setNullable(false);
        
		Alias alias = new Alias();
        property.setAlias(alias);  
        
    	// check for complex type or element ref
        QName refQName = element.getRef();
        if (refQName != null) { // reference prop
            Class targetDef = this.support.getClassLocalNameMap().get(refQName.getLocalPart());
            if (targetDef != null) {
                ClassRef targetClassRef = new ClassRef();
                targetClassRef.setName(targetDef.getName());
                targetClassRef.setUri(targetDef.getUri());
                property.setType(targetClassRef);                
	            property.setContainment(true);  
                setupNames(clss, property, alias, 
                		targetDef.getName(), refQName.getLocalPart());
            }
            else {
            	throw new IllegalStateException("could not find target class from, "
            			+ refQName);
            }
        } 
        else { // no ref, check type
            QName typeQName = element.getType();
            if (typeQName != null) {
	            // if a reference to another "local" entity
	            //TODO: what if elementFormDefault is not qualified?
	            if (typeQName.getNamespaceURI() != null && !typeQName.getNamespaceURI().equals(SchemaConstants.XMLSCHEMA_NAMESPACE_URI)) {
	
	                Class targetDef = this.support.getClassLocalNameMap().get(typeQName.getLocalPart());
	                if (targetDef != null) {
	                    ClassRef targetClassRef = new ClassRef();
	                    targetClassRef.setName(targetDef.getName());
	                    targetClassRef.setUri(targetDef.getUri());
	                    property.setType(targetClassRef);                
	    	            property.setContainment(true); 
	    	            if (element.getName() != null)
	    	                setupNames(clss, property, alias, 
	    	                	element.getName(), element.getName());
	    	            else
    	                    setupNames(clss, property, alias, 
    	                	    targetDef.getName(), typeQName.getLocalPart());
	                }
	                else {
	                	SimpleType simpleType = this.support.getSimpleTypeMap().get(typeQName.getLocalPart());
	                	if (simpleType != null) {
	    	                if (element.getName() == null)
	    	                	throw new IllegalStateException("expected name for element");
	    	                setupNames(clss, property, alias, element.getName(), element.getName());
	                    	ConstraintAssembler constraintAssembler = new ConstraintAssembler(
	                    			this.support,
	                    			this.destNamespaceURI, this.destNamespacePrefix);
	        	        	collect(clss, property,  simpleType,constraintAssembler);
	                	}
	                	else
	                	   throw new IllegalStateException("could not find target class or simple type from, "
	                			+ typeQName);
	                }
	            }
	            else { // XSD datatype        	.               
	            	String xsdTypeName = typeQName.getLocalPart();
	            	XSDBuiltInType xsdType = XSDBuiltInType.valueOf("xsd_" + xsdTypeName);
	                DataType sdoType = this.support.mapType(xsdType);;
	            	DataTypeRef dataTypeRef = new DataTypeRef();
	                dataTypeRef.setName(sdoType.name());
	                dataTypeRef.setUri(PlasmaConfig.getInstance().getSDODataTypesNamespace().getUri());
	                property.setType(dataTypeRef);        	
	
	                if (element.getName() == null)
	                	throw new IllegalStateException("expected name for element");
	                setupNames(clss, property, alias, 
	                		element.getName(), element.getName());
	            }
            } 
            else {
            	LocalSimpleType simpleType = element.getSimpleType();
            	if (simpleType != null) {
	                if (element.getName() == null)
	                	throw new IllegalStateException("expected name for element");
	                setupNames(clss, property, alias, element.getName(), element.getName());
                	ConstraintAssembler constraintAssembler = new ConstraintAssembler(
                			this.support,
                			this.destNamespaceURI, this.destNamespacePrefix);
    	        	collect(clss, property, simpleType, constraintAssembler);
            	}
            	else {
            		LocalComplexType localComplexType = element.getComplexType();
            		if (localComplexType != null) {
    	                if (element.getName() == null || element.getName().trim().length() == 0)
    	                	throw new IllegalStateException("expected name for element while processing class, " 
        		                + clss.getName());
            			log.warn("ignoring local complex type for element, "
            				+ element.getName() + ", - using string datatype");
    	            	XSDBuiltInType xsdType = XSDBuiltInType.xsd_string;
    	                DataType sdoType = this.support.mapType(xsdType);;
    	            	DataTypeRef dataTypeRef = new DataTypeRef();
    	                dataTypeRef.setName(sdoType.name());
    	                dataTypeRef.setUri(PlasmaConfig.getInstance().getSDODataTypesNamespace().getUri());
    	                property.setType(dataTypeRef);        	
    	                setupNames(clss, property, alias, 
    	                		element.getName(), element.getName());
            		}
                	else
                    	throw new IllegalStateException("expected 'ref' or 'type' attributes or local-simple-type or "
                    			+ "local-complex-type for element, "
                    			+ element.getName());
            	}
            		
            }
        }
            
        // FIXME; add isSequence and isUnique and maxlength added to plasma specific XML annotation ??
        //Integer maxLength = (Integer)property.get(PlasmaProperty.INSTANCE_PROPERTY_INT_MAXLENGTH);           
        //if (maxLength != null && maxLength.intValue() > 0)
        //	pdef.setMaxLength(maxLength.intValue());
        if (sequenceNum > -1) {
        	Sort seq = new Sort();
        	seq.setKey(String.valueOf(sequenceNum));
        	property.setSort(seq);
        }
        
    	return property;
    }
	
    /**
     * Creates non-reference property definitions. 
     * @param clss the owner class
     * @param complexType the Schema Complex Type
     * @param attribute the Schema Attribute
     * @return the property definition
     */
	public Property buildDatatypeProperty(Class clss, AbstractComplexType complexType, Attribute attribute)
    {
        if (attribute.getName() == null || attribute.getName().trim().length() == 0)
        	throw new IllegalStateException("expected name for attribute while processing class, " 
        		+ clss.getName());
    	Property property = new Property();
        property.setId(UUID.randomUUID().toString());
        // set property names and aliases
        Alias alias = new Alias();
        property.setAlias(alias);       
        setupNames(clss, property, alias, 
        		attribute.getName(), attribute.getName());
        XmlProperty xmlProp = new XmlProperty();
		xmlProp.setNodeType(XmlNodeType.ATTRIBUTE);
		property.setXmlProperty(xmlProp);
        
        Documentation documentation = new Documentation();
		documentation.setType(DocumentationType.DEFINITION);
		Body body = new Body();
		body.setValue(getDocumentationContent(attribute));
		documentation.setBody(body);
		property.getDocumentations().add(documentation);

        property.setVisibility(VisibilityType.PUBLIC); // FIXME
        
        // nullable
    	if ("required".equals(attribute.getUse()))
    		property.setNullable(false);
    	else
    		property.setNullable(true);
 
    	// multiplicity
    	property.setMany(false); // unless the type defined as a list below
    	     	
        QName typeQName = attribute.getType();
    	    	
    	// if local restriction will not have a simple type
    	ConstraintAssembler constraintAssembler = new ConstraintAssembler(
    			this.support,
    			this.destNamespaceURI, this.destNamespacePrefix);
    	if (typeQName == null) {
        	LocalSimpleType lst = attribute.getSimpleType();
        	Restriction rest = lst.getRestriction();
        	if (rest != null) {
        	    ValueConstraint constraint = constraintAssembler.buildValueConstraint(rest);
        	    property.setValueConstraint(constraint);
        	}
    	}
    	else {  
        	if (typeQName.getNamespaceURI() != null && typeQName.getNamespaceURI().equals(this.support.getSchema().getTargetNamespace())) {
	        	SimpleType simpleType = this.support.getSimpleTypeMap().get(typeQName.getLocalPart());
	        	collect(clss, property,  simpleType,constraintAssembler);
        	}
			else if (typeQName.getNamespaceURI().equals(SchemaConstants.XMLSCHEMA_NAMESPACE_URI)) {
				buildDataTypeReference(property, typeQName);
			}
			else 
    			log.warn("could not process namespace URI found for type, "
    					+ typeQName.toString());   
    	}
    	
    	if (property.getType() == null) {
        	DataType sdoType = this.support.mapType(XSDBuiltInType.xsd_string);
        	DataTypeRef dataTypeRef = new DataTypeRef();
            dataTypeRef.setName(sdoType.name());
            dataTypeRef.setUri(PlasmaConfig.getInstance().getSDODataTypesNamespace().getUri());
            property.setType(dataTypeRef);    
    	}
    	
    	return property;
    }
	
 
	public Property buildElementContentDatatypeProperty(Class clss,
    		QName xsdTypeNameQName)
    {
    	Property property = new Property();
        property.setId(UUID.randomUUID().toString());
        // set property names and aliases
        Alias alias = new Alias();
        property.setAlias(alias);   
        setupNames(clss, property, alias, 
        	"value", "value");
        
        //TODO: how to annotate such that serialization can know
        // this property is an element text value
        XmlProperty xmlProp = new XmlProperty();
		xmlProp.setNodeType(XmlNodeType.ELEMENT);
		property.setXmlProperty(xmlProp);
        
        Documentation documentation = new Documentation();
		documentation.setType(DocumentationType.DEFINITION);
		Body body = new Body();
		body.setValue("A synthetic property to accommodate simple type, "
				+ xsdTypeNameQName.toString());
		documentation.setBody(body);
		property.getDocumentations().add(documentation);

        property.setVisibility(VisibilityType.PUBLIC); // FIXME
        
        // nullable
    	property.setNullable(false);
 
    	// multiplicity
    	property.setMany(false); 
    	     	
    	buildDataTypeReference(property, xsdTypeNameQName);
    	
    	return property;
    }
    
	public Property createDerivedPropertyOpposite(Class clss, Property sourceProperty)
    {
    	Property targetProperty = new Property();
        targetProperty.setId(UUID.randomUUID().toString());
        if (sourceProperty.getOpposite() == null || sourceProperty.getOpposite().trim().length() == 0)
        	throw new IllegalStateException("expected opposite name for property, " 
                + clss.getName() + "." + sourceProperty.getName());
        targetProperty.setName(sourceProperty.getOpposite()); // actual SDO type name stored as sdox name        
        Documentation documentation = createDocumentation(
        	DocumentationType.DEFINITION,
        	"private derived opposite for, "
        		+ clss.getUri() + "#" + clss.getName() + "." + sourceProperty.getName());
		targetProperty.getDocumentations().add(documentation);
        targetProperty.setVisibility(VisibilityType.PRIVATE); 
        
        targetProperty.setNullable(true);
        targetProperty.setMany(true); 
        
        targetProperty.setDerived(true);
        targetProperty.setContainment(false);
              
        targetProperty.setOpposite(sourceProperty.getName());   
        
        ClassRef targetClassRef = new ClassRef();
        targetClassRef.setName(clss.getName());
        targetClassRef.setUri(clss.getUri());
        targetProperty.setType(targetClassRef);
        
        return targetProperty;
    }

	public void buildDataTypeReference(Property property, QName xsdTypeName)
    {
    	XSDBuiltInType xsdType = null;
    	try {
    	    xsdType = XSDBuiltInType.valueOf("xsd_" + xsdTypeName.getLocalPart());
    	}
    	catch (IllegalArgumentException e) {
    		throw new IllegalStateException("could not create SDO type from name, "
    				+ xsdTypeName.toString());
    	}
    	DataType sdoType = this.support.mapType(xsdType);
    	if (property.getType() != null) {
    		DataType existingSdoType = DataType.valueOf(property.getType().getName());
    		if (existingSdoType.ordinal() == sdoType.ordinal()) {
    			return;
    		}
    		else {
    		    log.warn("property '" + property.getName() 
    			    + "' has an existing datatype ("+existingSdoType.name()+") - could not set to comflicting type, "
    			    + sdoType.name());
    		    return;
    		}
    	}	    	
    	
    	DataTypeRef dataTypeRef = new DataTypeRef();
        dataTypeRef.setName(sdoType.name());
        dataTypeRef.setUri(PlasmaConfig.getInstance().getSDODataTypesNamespace().getUri());
        property.setType(dataTypeRef);        	
    }
	
	private void collect(Class clss, Property property, 
			AbstractSimpleType simpleType, ConstraintAssembler constraintAssembler) 
	{
    	ConstraintCollector constraintCollector = 
    		new ConstraintCollector(this.support.getSchema(),
    				this.support.getSimpleTypeMap(), constraintAssembler);
    	simpleType.accept(constraintCollector);
    	if (constraintCollector.getEnumerationConstraints().size() > 0) {
    		if (constraintCollector.getEnumerationConstraints().size() == 1)
    			property.setEnumerationConstraint(constraintCollector.getEnumerationConstraints().get(0));
    		else
    			log.warn("collected more than one enumeration constraint for "
    				+ clss.getName() + "." + property.getName() + " - ignoring");
    	}
    	if (constraintCollector.getValueConstraints().size() > 0) {
    		if (constraintCollector.getValueConstraints().size() == 1)
    			property.setValueConstraint(constraintCollector.getValueConstraints().get(0));
    		else
    			log.warn("collected more than one value constraint for "
    				+ clss.getName() + "." + property.getName() + " - ignoring");
    	}
    	DatatypeCollector datatypeCollector = new DatatypeCollector(this.support.getSchema(),
    			this.support.getSimpleTypeMap());
    	simpleType.accept(datatypeCollector);
    	if (datatypeCollector.isListType())
    		property.setMany(true);
    	if (datatypeCollector.getResult().size() > 0) {
			QName first = datatypeCollector.getResult().get(0);
    		if (datatypeCollector.getResult().size() > 1) {
    			for (QName name : datatypeCollector.getResult())
    				if (!name.getLocalPart().equals(first.getLocalPart())) {
	    			    log.warn("detected hetergeneous XSD datatype '"+name.getLocalPart()+"' for "
		        				+ clss.getName() + "." + property.getName() + " - using first type '"
		        				+ first.getLocalPart() + "'");
    				}
    		}
    		buildDataTypeReference(property, first);
    	}
    	else
			log.warn("collected no XSD datatypes for "
    				+ clss.getName() + "." + property.getName() + " - using default xsd:string");		
	}
	
	private void setupNames(Class clss, Property property, Alias alias, 
			String candidateNameLogicalName, 
			String localName) {
        String logicalName = this.formatLocalPropertyName(candidateNameLogicalName);
        boolean logicalNameConflict = this.support.logicalNameConflict(clss, logicalName);
        logicalName = this.support.buildLogicalPropertyName(clss, logicalName);
        property.setName(logicalName); 
        String physicalNameAlias = NameUtils.toAbbreviatedName(logicalName);
        alias.setPhysicalName(physicalNameAlias);
        // else there likely will be a local-name conflict
        // as well. 
        if (!logicalNameConflict)
            alias.setLocalName(localName); // because XML schema "projection" names could differ
        else
        	alias.setLocalName(logicalName);		
	} 
    
}
