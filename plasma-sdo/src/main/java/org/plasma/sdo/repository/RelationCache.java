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
package org.plasma.sdo.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plasma.sdo.AssociationPath;



/**
 * Caches results for relation path queries 
 */
public class RelationCache {
    private static Log log = LogFactory.getFactory().getInstance(
    		RelationCache.class);
    private Map<String, RelationPathResult> map = new HashMap<String, RelationPathResult>();    
    
	public boolean isRelation(Classifier target, Classifier source, AssociationPath relation) {
		switch (relation) {
		case singular:
			String key = createHashKey(target, source);
			RelationPathResult result = this.map.get(key);
			if (result == null) {
			    boolean isRelated = isLateralSingularRelation(target, source, null,
			    		new HashMap<String, Integer>());
			    result = new RelationPathResult(AssociationPath.singular, isRelated);
		        if (log.isDebugEnabled())
		            log.debug("cacheing ("+isRelated+") lateral-singular relation: "+ target.getName() 
		                    + "/" + source.getName() + " ("+key+")");
			    synchronized (map) {
			    	this.map.put(key, result);
			    }
			}
			else {
		        if (log.isDebugEnabled())
		            log.debug("cache hit ("+result.isRelated()+") lateral-singular relation: "+ target.getName() 
		                    + "/" + source.getName() + " ("+key+")");
			}
		    return result.isRelated();
		default:
			//TODO: other cases
			return false;
		}
	}
    
	public void clear() {
		this.map.clear();
	}
	
    private String createHashKey(Classifier targetType, Classifier sourceType) {
    	return targetType.getId() + "/" + sourceType.getId();
    }

    private String createHashKey(org.modeldriven.fuml.repository.Property target, 
    		org.modeldriven.fuml.repository.Property source) {
    	return target.getXmiId() + "/" + source.getXmiId();
    }
        
    /**
     * Returns true if the given target type is related to the given source type
     * through one or more singular reference properties.
     * @param targetType the target type
     * @param sourceType the source type
     * @return true if the given target type is related to the given source type
     * through one or more singular reference properties.
     */
    private boolean isLateralSingularRelation(Classifier targetType, Classifier sourceType, 
    		org.modeldriven.fuml.repository.Property traversalSourceProperty,
    		Map<String, Integer> visited) {
        
        if (log.isDebugEnabled())
            log.debug("comparing targetType/sourceType "+ targetType.toString() 
                    + " / " + sourceType.toString());
                
        List<org.modeldriven.fuml.repository.Property> targetProperties = targetType.getAllProperties();
        if (log.isDebugEnabled()) {
            log.debug("checking targetType  "+ targetType.toString() 
                    + " with " + targetProperties.size() + " properties");
        }
        
        for (org.modeldriven.fuml.repository.Property targetProperty : targetProperties) {
            if (log.isDebugEnabled())  
                log.debug("checking "+ targetType.toString() + "." + getDebugName(targetProperty));
            

            if (targetProperty.getType().isDataType()) { 
                if (log.isDebugEnabled()) 
                    log.debug("not a reference property "+ targetType.toString() + "." + getDebugName(targetProperty));
                continue; // not a reference       
            }
            
            // We are traversing BACK-TO the source type, so need to look at the multiplicity of the opposite
            // properties.
            org.modeldriven.fuml.repository.Property targetPropertyOpposite = targetProperty.getOpposite();
            if (targetPropertyOpposite == null) {
                if (log.isDebugEnabled()) 
                    log.debug("no opposite "+ targetType.toString() + "." + getDebugName(targetProperty));
            	continue;
            }
            
            if (!targetPropertyOpposite.isSingular())
            {
                if (log.isDebugEnabled()) 
                    log.debug("opposite not singular "+ targetType.toString() + "." + getDebugName(targetProperty));
            	continue;
            }                        
            	
            if (log.isDebugEnabled()) {
                log.debug("processing "+ targetType.toString() + "." + getDebugName(targetProperty));
            }

            // Our metadata is not a directed graph so we 
        	// can't bypass traversals based on directionality, so
        	// detect a loop based on source and target properties
        	// And only visit a particular link
        	// ever once.
        	if (traversalSourceProperty != null) {
            	String linkKey = createHashKey(targetProperty, traversalSourceProperty);
            	Integer count = null;
            	if ((count = visited.get(linkKey)) == null)
            		visited.put(linkKey, Integer.valueOf(1));
            	else {	
            		/*
                	org.modeldriven.fuml.repository.Property opposite = targetProperty.getOpposite();
                	if (opposite != null && opposite.isSingular()) {
            		    log.warn("singular property "
                				+ targetType.getName() + "."
                        		+ (targetProperty.getName() == null ? targetProperty.getXmiId() : targetProperty.getName())
                        		+ " has opposite " 
                        		+ opposite.getClass_().getName()+"."
                        		+ (opposite.getName() == null ? opposite.getXmiId() : opposite.getName())
                        		+ " which is also singular");
                	}
                	*/
                    if (log.isDebugEnabled()) {
                        log.debug("exiting - visited  "+ targetType.toString() + "." + getDebugName(targetProperty));
                    }

                	return false;
            	}
        	}
        	
            
            if (log.isDebugEnabled()) {
                log.debug("comparing " + targetType.toString()
                        + "." + getDebugName(targetProperty) + " ("+targetProperty.getType().getXmiId()+")"
                        + "->" + sourceType.toString() + " ("+sourceType.getId()+")");
            }
            // if we've arrived at the target via singular relations
        	if (targetProperty.getType().getXmiId().equals(sourceType.getId())) {
                if (targetType.getId().equals(sourceType.getId())) {
                    log.warn("potential circular reference: " + targetType.toString()
	                        + "." + getDebugName(targetProperty)
	                        + "->" + sourceType.toString());
                }
                if (log.isDebugEnabled())
                    log.debug("found child link property " + targetType.toString()
                        + "." + getDebugName(targetProperty)
                        + "->" + sourceType.toString());
                return true; 
        	}
        	else { 
        		// if we've not arrived at the target/root
                if (!(targetProperty.getType().getXmiId().equals(targetType.getId()))) {
                		
                    if (log.isDebugEnabled())
                        log.debug("traversing "+ targetType.toString() + "."
                        		+ getDebugName(targetProperty));
                    //FIXME: classifiers returned via XMI ID have no properties !!
                    // See re-lookup by qualified name below
                    org.modeldriven.fuml.repository.Classifier tempClassifier = (org.modeldriven.fuml.repository.Classifier)Repository.INSTANCE.getElementById(
                    		targetProperty.getType().getXmiId());
                    
                    String urn = tempClassifier.getArtifact().getNamespaceURI();
                    String qualifiedName = urn + "#" + tempClassifier.getName();
                    org.modeldriven.fuml.repository.Classifier targetClassifier = PlasmaRepository.getInstance().getClassifier(
                    		qualifiedName);
                    Classifier nextClassifier = null;
                    if (org.modeldriven.fuml.repository.Class_.class.isAssignableFrom(targetClassifier.getClass()))        	
                    	nextClassifier = new Class_(
                    		(org.modeldriven.fuml.repository.Class_)targetClassifier);
                    else
                    	nextClassifier = new Classifier(targetClassifier);
                    
        		    if (isLateralSingularRelation(nextClassifier, 
        		    		sourceType, // Note: stays constant across traversals 
        		    		targetProperty, 
        		    		visited))
        		    	return true;
                }
        	}
        }
        
        return false;
    }
    
    /**
     * UML properties are not required to be named, so at the repository level
     * for debugging, construct some recognizable name. 
     * @param prop the property
     * @return the debug name
     */
    private String getDebugName(org.modeldriven.fuml.repository.Property prop) {
    	if (prop.getName() != null && prop.getName().trim().length() > 0) {
    		return prop.getName();
    	}
    	else {
    		return prop.getType().getName();
    	}
    }
    
    class RelationPathResult {
        private AssociationPath relationPath;
        private boolean related;
        @SuppressWarnings("unused")
    	private RelationPathResult() {}
    	public RelationPathResult(AssociationPath relationPath, boolean related) {
    		super();
    		this.relationPath = relationPath;
    		this.related = related;
    	}
    	public AssociationPath getRelationPath() {
    		return relationPath;
    	}
    	public boolean isRelated() {
    		return related;
    	}    
    }    
}
