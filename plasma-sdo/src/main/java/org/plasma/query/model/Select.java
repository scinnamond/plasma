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
package org.plasma.query.model;


import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.JaxenException;
import org.jaxen.expr.NameStep;
import org.jaxen.expr.Step;
import org.plasma.query.DataProperty;
import org.plasma.query.QueryException;
import org.plasma.query.Wildcard;
import org.plasma.query.visitor.DefaultQueryVisitor;
import org.plasma.query.visitor.QueryVisitor;
import org.plasma.query.visitor.Traversal;
import org.plasma.query.xpath.QueryPredicateVisitor;
import org.plasma.query.xpath.QueryXPath;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Select", propOrder = {
        "properties",
        "textContent"
})
@XmlRootElement(name = "Select")
public class Select implements org.plasma.query.Select {
    private static Log log = LogFactory.getFactory().getInstance(Select.class);

    @XmlElement(name = "TextContent")
    protected TextContent textContent;

    @XmlElementRef // IMPORTANT - maps single container/field to multiple subclass 'ref' elements in XSD choice 
    protected List<AbstractProperty> properties;

    public Select() {
        super();
    } 

    public Select(Property p1) {
        this();
        getProperties().add(p1);
    }

    public Select(Property p1, Property p2) {
        this();
        getProperties().add(p1);
        getProperties().add(p2);
    }

    public Select(Property p1, Property p2, Property p3) {
        this();
        getProperties().add(p1);
        getProperties().add(p2);
        getProperties().add(p3);
    }

    public Select(Property p1, Property p2, Property p3, Property p4) {
        this();
        getProperties().add(p1);
        getProperties().add(p2);
        getProperties().add(p3);
        getProperties().add(p4);
    }

    public Select(Property[] properties) {
        this();
        for (int i = 0; i < properties.length; i++)
            getProperties().add(properties[i]);
    }

    public Select(WildcardProperty p1) {
        this();
        getProperties().add(p1);
    }

    public Select(WildcardProperty p1, WildcardProperty p2) {
        this();
        getProperties().add(p1);
        getProperties().add(p2);
    }

    public Select(WildcardProperty p1, WildcardProperty p2, WildcardProperty p3) {
        this();
        getProperties().add(p1);
        getProperties().add(p2);
        getProperties().add(p3);
    }

    public Select(WildcardProperty p1, WildcardProperty p2, WildcardProperty p3, WildcardProperty p4) {
        this();
        getProperties().add(p1);
        getProperties().add(p2);
        getProperties().add(p3);
        getProperties().add(p4);
    }

    public Select(WildcardProperty[] properties) {
        this();
        for (int i = 0; i < properties.length; i++)
            getProperties().add(properties[i]);
    }

    public Select(AbstractProperty[] properties) {
        this();
        for (int i = 0; i < properties.length; i++)
            if (properties[i] instanceof Property)
                getProperties().add((Property)properties[i]);
            else if (properties[i] instanceof WildcardProperty)
                getProperties().add((WildcardProperty)properties[i]);
            else
                throw new QueryException("expected instance of Property or WildcardProperty");
    }
    
    /**
     * Accepts an XPath expression, which may include predicates on any 
     * path step, and parses the expressions into an object model representation.
     * @param expression the XPath expression
     */
	public Select(String expression) {
		this(new String[] {expression});
	}
        
    /**
     * Accepts an array of XPath expressions, which may include predicates on
     * any path step, and parses the expressions into an object model 
     * representation.
     * @param expressions the array of XPath expressions
     */
	public Select(String[] expressions) {
        this();
        if (expressions == null || expressions.length == 0)
            throw new IllegalArgumentException("argument 'content' is null or zero length");

    	try {
	        for (int i = 0; i < expressions.length; i++)
	        {
	            Path path = null;
	            PathNode currPathNode = null;
				QueryXPath xpath = new QueryXPath(expressions[i]);
				List<Step> steps = xpath.getSteps();
				for (int j = 0; j < steps.size(); j++) {
					Step step = steps.get(j);
					if (!(step instanceof NameStep))
						throw new QueryException("cannot determine traversal path - "
								+ "expected named path step not, "
								+ step.getClass().getName());
					NameStep nameStep = (NameStep)step;
					if (log.isDebugEnabled())
					    log.debug("step: " + nameStep.getLocalName());
	                if (j+1 == steps.size()) {// last ? expect a property                                                            
                        if (path != null)   
                        	if (!Wildcard.WILDCARD_CHAR.equals(nameStep.getLocalName()))
                                getProperties().add(new Property(nameStep.getLocalName(), path));
                        	else
                                getProperties().add(new WildcardProperty(path));
                        else                                                                                 
                        	if (!Wildcard.WILDCARD_CHAR.equals(nameStep.getLocalName()))
                                getProperties().add(new Property(nameStep.getLocalName()));
                        	else
                                getProperties().add(new WildcardProperty());
	                }
	                else                                                                                         
	                {                                                                                            
	                    if (path == null)                                                                        
	                        path = new Path();    
	                    currPathNode = new PathNode(nameStep.getLocalName());
	                    path.addPathNode(currPathNode); 
	                    if (step.getPredicates() != null && step.getPredicates().size() > 0) { 
		                    QueryPredicateVisitor visitor = new QueryPredicateVisitor();
						    xpath.acceptBreadthFirst(step, visitor);
						    currPathNode.setWhere(visitor.getResult());
	                    }
	                }                                                                                            					
				}
	        }
		} catch (JaxenException e) {
            throw new QueryException(e);
		} 
    }  
    
    public List<AbstractProperty> getProperties() {
        if (properties == null) {
            properties = new ArrayList<AbstractProperty>();
        }
        return this.properties;
    }

    public void addProperty(AbstractProperty property) {
    	this.getProperties().add(property);
    }
    
    public void addAll(AbstractProperty[] properties) {
    	if (properties != null)
    	for (AbstractProperty prop : properties)
    	    this.getProperties().add(prop);
    }
    
    /**
     * Gets the value of the textContent property.
     * 
     * @return
     *     possible object is
     *     {@link TextContent }
     *     
     */
    public TextContent getTextContent() {
        return textContent;
    }

    /**
     * Sets the value of the textContent property.
     * 
     * @param value
     *     allowed object is
     *     {@link TextContent }
     *     
     */
    public void setTextContent(TextContent value) {
        this.textContent = value;
    }

    public void accept(QueryVisitor visitor)
    {
        visitor.start(this);
        if (visitor.getContext().getTraversal().ordinal() == Traversal.CONTINUE.ordinal())
        {
            for (int i = 0; i < this.getProperties().size(); i++)
               this.getProperties().get(i).accept(visitor);  
        }
    	visitor.end(this);
    }

    public DataProperty[] getPropertiesDeep()
    {
        final ArrayList<Property> list = new ArrayList<Property>(12);
        QueryVisitor visitor = new DefaultQueryVisitor() {
            public void start(Property property)                                                                            
            {     
               list.add(property);                                                                                                          
               super.start(property);                                                                  
            }                                                                                                                                                                                                                                                                                                                                                               
        };
        this.accept(visitor);
        Property[] results = new Property[list.size()];
        list.toArray(results);
        return results;
    }
    
    public boolean hasDistinct(DataProperty[] props)
    {
        for (int i = 0; i < props.length; i++)
            if (props[i].isDistinct())
                return true;
        return false;    
    }

    public boolean hasDistinctProperties()
    {
        DataProperty[] props = getPropertiesDeep();
        for (int i = 0; i < props.length; i++)
            if (props[i].isDistinct())
                return true;
        return false;    
    }

}
