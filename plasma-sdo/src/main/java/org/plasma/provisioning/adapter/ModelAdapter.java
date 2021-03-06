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
package org.plasma.provisioning.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plasma.provisioning.Alias;
import org.plasma.provisioning.Class;
import org.plasma.provisioning.Package;
import org.plasma.provisioning.ClassRef;
import org.plasma.provisioning.Enumeration;
import org.plasma.provisioning.EnumerationRef;
import org.plasma.provisioning.Model;
import org.plasma.provisioning.Property;
import org.plasma.provisioning.ProvisioningException;
import org.plasma.provisioning.TypeNameCollisionException;
import org.plasma.provisioning.PropertyNameCollisionException;

public class ModelAdapter {
    private static Log log = LogFactory.getLog(
			   ModelAdapter.class); 

    private Model model;
    private Map<String, TypeAdapter> typeMap = new HashMap<String, TypeAdapter>();
    
    @SuppressWarnings("unused")
	private ModelAdapter() {}
    
    public ModelAdapter(Model model) {
    	this.model = model;
    	construct();
    }
    
    public Model getModel() {
		return model;
	}
    
    public Collection<TypeAdapter> getTypes() {
    	return typeMap.values();
    }
    
    public TypeAdapter[] getTypesArray() {
    	TypeAdapter[] result = new TypeAdapter[typeMap.size()];
    	typeMap.values().toArray(result);
    	return result;
    }

	public TypeAdapter findType(String key) {
    	TypeAdapter result = typeMap.get(key);
    	return result;
    }
	
	private void findPackages(Model root, List<Package> packages) {
		packages.add(root);
    	for (Package childPkg : root.getPackages()) {
    		packages.add(childPkg);
    	}
	}
    
    private void construct() {
		if (log.isDebugEnabled())
			log.debug("constructing...");
		
		List<Package> allPkgs = new ArrayList<Package>();
		findPackages(this.model, allPkgs);
		
    	for (Package pkg : allPkgs)
    		mapEnumerations(pkg);
    	
    	for (Package pkg : allPkgs)
    		mapClasses(pkg);
    	
    	for (TypeAdapter adapter : typeMap.values()) {
    		if (adapter.getType() instanceof Class) {    		
    			if (log.isDebugEnabled())
    				log.debug("constructing class: " + adapter.getKey());
    		    construct(adapter, null);
    		}
    	} 
    	
    	for (TypeAdapter adapter : typeMap.values()) {
    		if (adapter.getType() instanceof Class) { 
    	    	for (ClassRef baseClassRef : ((Class)adapter.getType()).getSuperClasses()) {
    	    		String key = baseClassRef.getUri() + "#" + baseClassRef.getName();
    	    		TypeAdapter baseAdapter = typeMap.get(key);
    	    		if (baseAdapter == null)
    	    			throw new IllegalStateException("no mapping found for base type: " + key); 
    						
        			if (log.isDebugEnabled())
        				log.debug("construct deep: " + adapter.getKey());
    				constructDeep(adapter, baseAdapter);
    			}
    		}
    	}    	
    }
    
    private void mapEnumerations(Package pkg) {
    	for (Enumeration enm : pkg.getEnumerations()) {
    		String key = enm.getUri() + "#" + enm.getName();
    		if (log.isDebugEnabled())
    			log.debug("mapping enumeration: " + key);
    		if (typeMap.get(key) != null)
    			throw new TypeNameCollisionException(
    				"detected multiple types named '"+enm.getName()+"' under the same URI '"
    				+ enm.getUri() + "'");
    		typeMap.put(key, new TypeAdapter(enm));
    	}
    }   
    
    private void mapClasses(Package pkg) {
		for (Class cls : pkg.getClazzs()) {
			String key = cls.getUri() + "#" + cls.getName();
			if (log.isDebugEnabled())
				log.debug("mapping class: " + key);
			if (typeMap.get(key) != null)
				throw new TypeNameCollisionException(
					"detected multiple types named '"+cls.getName()+"' under the same URI '"
					+ cls.getUri() + "'");
			
			TypeAdapter adapter = new TypeAdapter(cls);
			typeMap.put(key, adapter);
			if (log.isDebugEnabled())
				log.debug("map: " + adapter.getKey());
		}
    }

    private void construct(TypeAdapter adapter, TypeAdapter source)
    {    	
    	for (Property prop: ((Class)adapter.getType()).getProperties()) {
		    if (adapter.getDeclaredProperty(prop.getName()) != null)
		    	throw new PropertyNameCollisionException(
	    			"detected multiple properties with the same logical name '"
		    		+ prop.getName() + "' defined for class '"
	    			+ adapter.getKey() + "' the set of logical names for a class "
	    			+ "must be unique");
		    adapter.putDeclaredProperty(prop.getName(), prop);
		    adapter.putProperty(prop.getName(), prop); // note: all property collection not declared only
		    if (prop.getAlias() != null) {
		    	Alias alias = prop.getAlias();
			    if (alias.getPhysicalName() != null && alias.getPhysicalName().trim().length() > 0) {
				    String physicalName = alias.getPhysicalName().trim();
			    	if (adapter.getAliasedProperty(physicalName) != null)
				    	throw new PropertyNameCollisionException(
			    			"detected multiple properties with the same physical name '"
				    		+ alias + "' defined for class '"
			    			+ adapter.getKey() + "' the set of physical names for a class "
			    			+ "must be unique");
				    adapter.putAliasedProperty(physicalName, prop);
			    }
			    if (alias.getLocalName() != null && alias.getLocalName().trim().length() > 0) {
				    String localName = prop.getAlias().getLocalName().trim();
			    	if (adapter.getAliasedProperty(localName) != null)
				    	throw new PropertyNameCollisionException(
			    			"detected multiple properties with the same local name '"
				    		+ alias + "' defined for class '"
			    			+ adapter.getKey() + "' the set of local names for a class "
			    			+ "must be unique");
				    adapter.putAliasedProperty(localName, prop);
			    }
		    }
		}
    }

    private void constructDeep(TypeAdapter adapter, TypeAdapter baseAdapter)
    {
    	// copy base properties into subclass
    	for (Property prop: ((Class)baseAdapter.getType()).getProperties()) {
		    if (adapter.getProperty(prop.getName()) != null)
		    	throw new PropertyNameCollisionException(
	    			"detected multiple properties with the same logical name '"
		    		+ prop.getName() + "' defined for class '"
	    			+ adapter.getKey() + "' as well as its superclass '"
	    			+ baseAdapter.getKey() + "' - the set of logical names for a class and "
	    			+ "superclasses must be unique");
		    validate(baseAdapter, adapter, prop);
		    adapter.putProperty(prop.getName(), prop); // note: all property collection not declared only
		    if (prop.getAlias() != null && prop.getAlias().getPhysicalName() != null && prop.getAlias().getPhysicalName().trim().length() > 0) {
			    String alias = prop.getAlias().getPhysicalName().trim();
		    	if (adapter.getAliasedProperty(alias) != null)
			    	throw new PropertyNameCollisionException(
			    			"detected multiple properties with the same physical name '"
				    		+ alias + "' defined for class '"
			    			+ adapter.getKey() + "' as well as its superclass '"
			    			+ baseAdapter.getKey() + "' - the set of logical names for a class and "
			    			+ "superclasses must be unique");
		    	adapter.putAliasedProperty(alias, prop);
		    }
		}

    	for (ClassRef baseClassRef : ((Class)baseAdapter.getType()).getSuperClasses()) {
    		String key2 = baseClassRef.getUri() + "#" + baseClassRef.getName();
    		TypeAdapter baseTypeAdapter = typeMap.get(key2);
    		if (baseTypeAdapter == null)
    			throw new IllegalStateException("no mapping found for base type: " + key2); 
					
			constructDeep(adapter, baseTypeAdapter);
		}
    }
    
    private void validate(TypeAdapter adapter, TypeAdapter source, Property prop)
    {
	    if (prop.getType() instanceof ClassRef) {
	    	ClassRef ref = (ClassRef)prop.getType();
	    	String refkey = ref.getUri() + "#" + ref.getName();
    		if (typeMap.get(refkey) == null)
		    	throw new ProvisioningException(
		    			"invalid type reference detected for property '"
			    		+ adapter.getKey() + "." + prop.getName() + "' no class or enumeration '"
		    			+ refkey + "' is defined");
    		if (prop.getOpposite() != null) {
    			Class oppositeClass = (Class)typeMap.get(refkey).getType();
    			Property oppositeProperty = findPropertyByName(oppositeClass, prop.getOpposite());
    			if (oppositeProperty == null)
			    	throw new ProvisioningException(
			    			"invalid opposite reference detected for property '"
				    		+ adapter.getKey() + "." + prop.getName() + "' no opposite property '"
				    		+ prop.getOpposite() + "' is defined for class '"
			    			+ refkey + "'");
    		}
	    }
	    if (prop.getType() instanceof EnumerationRef) {
	    	EnumerationRef ref = (EnumerationRef)prop.getType();
	    	String refkey = ref.getUri() + "#" + ref.getName();
    		if (typeMap.get(refkey) == null)
		    	throw new ProvisioningException(
		    			"invalid type reference detected for property '"
			    		+ prop.getName() + "' defined for class '"
		    			+ adapter.getKey() + "'");
	    }
    }    
    
    private Property findPropertyByName(Class clss, String name) {
        for (Property prop : clss.getProperties()) {
        	if (name.equals(prop.getName()))
        		return prop;
        }
        return null;
    }
    
}
