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

import java.util.List;

import org.modeldriven.fuml.repository.Package;
import org.modeldriven.fuml.repository.Stereotype;
import org.plasma.common.exception.PlasmaRuntimeException;
import org.plasma.sdo.profile.SDONamespace;

public abstract class Element {
	
	
    public String getNamespaceURI(org.modeldriven.fuml.repository.Classifier classifier) {
        org.modeldriven.fuml.repository.Package p = classifier.getPackage();
        String uri = findSDONamespaceURI(p);
        if (uri == null)
            throw new RepositoryException("no SDO Namespace uri found for classifier, '"
                + classifier.getName() + "'");
        return uri;
    } 
    
    private String findSDONamespaceURI(Package pkg) {
    	
    	SDONamespace sdoNamespaceStereotype = findSDONamespace(pkg);
    	if (sdoNamespaceStereotype != null) {
    		return sdoNamespaceStereotype.getUri();
    	}
        
        if (pkg.getNestingPackage() != null)
            return findSDONamespaceURI(pkg.getNestingPackage());
            
        return null;
    } 
    
    private SDONamespace findSDONamespace(Package pkg) {
        List<Stereotype> stereotypes = PlasmaRepository.getInstance().getStereotypes(pkg);
        if (stereotypes != null) {
            for (Stereotype stereotype : stereotypes)
                if (stereotype.getDelegate() instanceof SDONamespace) {
                    return (SDONamespace)stereotype.getDelegate();
                }
        }
        return null;
    } 

}
