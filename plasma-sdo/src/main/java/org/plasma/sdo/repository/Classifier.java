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

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.modeldriven.fuml.repository.Class_;
import org.modeldriven.fuml.repository.Stereotype;
import org.modeldriven.fuml.repository.Package;
import org.plasma.common.exception.PlasmaRuntimeException;
import org.plasma.sdo.Alias;
import org.plasma.sdo.AssociationPath;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.profile.SDOAlias;
import org.plasma.sdo.profile.SDODerivation;

import fUML.Syntax.Classes.Kernel.NamedElement;
import fUML.Syntax.Classes.Kernel.VisibilityKind;


public class Classifier extends Element {

	private org.modeldriven.fuml.repository.Classifier classifier;
 
	@SuppressWarnings("unused")
	private Classifier() {}
	
	public Classifier(org.modeldriven.fuml.repository.Classifier classifier) {
		super();
		this.classifier = classifier;
	}
	
	public String getName() {
		return this.classifier.getName();
	}
	
	public String getId() {
		return this.classifier.getDelegate().getXmiId();
	}	
	
	public String toString() {
		return this.getNamespaceURI() + "#" + this.getName();
	}
	
	public String getPackageName()
	{
    	if (classifier.getPackage() != null) 
		    return this.classifier.getPackage().getName();
		else
			return null;
	}
	
	public String getPackagePhysicalName()
	{
    	SDOAlias alias = findPackageAlias();
    	if (alias != null)
    		return alias.getPhysicalName();
        return null;
	}
	
    public SDOAlias findPackageAlias() 
    {
    	if (classifier.getPackage() != null) {
	        List<Stereotype> stereotypes = PlasmaRepository.getInstance().getStereotypes(classifier.getPackage());
	        if (stereotypes != null) {
	            for (Stereotype stereotype : stereotypes)
	                if (stereotype.getDelegate() instanceof SDOAlias) {
	                	return (SDOAlias)stereotype.getDelegate();
	                }
	        }
    	}
        return null;
    }  
    
    public SDODerivation findDerivation() {
        List<Stereotype> stereotypes = PlasmaRepository.getInstance().getStereotypes(classifier);
        if (stereotypes != null) {
            for (Stereotype stereotype : stereotypes)
                if (stereotype.getDelegate() instanceof SDODerivation) {
                	return (SDODerivation)stereotype.getDelegate();
                }
        }
        return null;
	}    
	
	public List<Comment> getComments() {
		List<Comment> result = new ArrayList<Comment>();
		for (fUML.Syntax.Classes.Kernel.Comment comment : classifier.getDelegate().ownedComment)
			result.add(new Comment(comment));
		return result;	
	}
	
	public VisibilityKind getVisibility()
	{
        return this.classifier.getDelegate().visibility;		
	}
	
    public boolean isAbstract() {
        return this.classifier.isAbstract(); 
    }
    
    public boolean isDataType() {
        return this.classifier.isDataType();
    }
    
    public List<org.modeldriven.fuml.repository.Classifier> getGeneralization() {
    	return classifier.getGeneralization();
    }
    
    public List<org.modeldriven.fuml.repository.Classifier> getSpecializations() {
    	return PlasmaRepository.getInstance().getSpecializations(this.classifier);
    }
    
    public List<org.modeldriven.fuml.repository.Property> getDeclaredProperties() {
    	return ((Class_)this.classifier).getDeclaredProperties();
    }
    
    public List<org.modeldriven.fuml.repository.Property> getAllProperties() {
    	return ((Class_)this.classifier).getAllProperties();
    }

    /**
     * Traverses the given classifier's package ancestry looking for
     * an SDO namespace stereotype, and if found, returns the URI.
     * @param classifier the classifier
     * @return the SDO namespace URI or null if not found.
     * @throws RepositoryException if the URI is not found
     */
    public String getNamespaceURI() {
        return getNamespaceURI(this.classifier);
    } 
     
    public String getPhysicalName() 
    {
    	SDOAlias alias = findAlias();
    	if (alias != null)
    		return alias.getPhysicalName();
        return null;
    }    

    public String getLocalName() 
    {
    	SDOAlias alias = findAlias();
    	if (alias != null)
    		return alias.getLocalName();
        return null;
    }    

    public String getBusinessName() 
    {
    	SDOAlias alias = findAlias();
    	if (alias != null)
    		return alias.getBusinessName();
        return null;
    }    

    public SDOAlias findAlias() 
    {
        List<Stereotype> stereotypes = PlasmaRepository.getInstance().getStereotypes(classifier);
        if (stereotypes != null) {
            for (Stereotype stereotype : stereotypes)
                if (stereotype.getDelegate() instanceof SDOAlias) {
                	return (SDOAlias)stereotype.getDelegate();
                }
        }
        return null;
    }        
    
    public org.modeldriven.fuml.repository.Classifier getDerivationSupplier() 
    {
        List<Stereotype> stereotypes = PlasmaRepository.getInstance().getStereotypes(classifier);
        if (stereotypes != null) {
            for (Stereotype stereotype : stereotypes)
                if (stereotype.getDelegate() instanceof SDODerivation) {
                	SDODerivation sdoDerivationStereotype = (SDODerivation)stereotype.getDelegate();
                    if (sdoDerivationStereotype.getSupplier() != null) {
                    	SDODerivation deriv = (SDODerivation)stereotype.getDelegate();
                       	fUML.Syntax.Classes.Kernel.NamedElement namedElem = deriv.getSupplier();
                       	org.modeldriven.fuml.repository.Element elem = PlasmaRepository.getInstance().getElementById(namedElem.getXmiId());
                    	if (elem instanceof org.modeldriven.fuml.repository.Classifier) {
                    		return (org.modeldriven.fuml.repository.Classifier)elem;               		
                    	}
                    }
                }
        }
        return null;
    }  
    
	public boolean isRelation(Classifier other, AssociationPath relation) {
		return PlasmaRepository.getInstance().getRelationCache().isRelation(
				this, other, relation);
	}
    
        
}
