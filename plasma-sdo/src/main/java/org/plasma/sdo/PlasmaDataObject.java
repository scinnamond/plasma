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
package org.plasma.sdo;

// java imports

import java.util.UUID;

import org.plasma.sdo.core.SnapshotMap;

import commonj.sdo.DataGraph;
import commonj.sdo.DataObject;
import commonj.sdo.Property;

/**
 * A data object is a representation of some structured data. 
 * It is the fundamental component in the SDO (Service Data Objects) package.
 * Data objects support reflection, path-based accesss, convenience creation and deletion methods, 
 * and the ability to be part of a {@link DataGraph data graph}.
 * <p>
 * Each data object holds its data as a series of {@link Property Properties}. 
 * Properties can be accessed by name, property index, or using the property meta object itself. 
 * A data object can also contain references to other data objects, through reference-type Properties.
 * <p>
 * A data object has a series of convenience accessors for its Properties. 
 * These methods either use a path (String), 
 * a property index, 
 * or the {@link Property property's meta object} itself, to identify the property.
 * Some examples of the path-based accessors are as follows:
 *<pre>
 * DataObject company = ...;
 * company.get("name");                   is the same as company.get(company.getType().getProperty("name"))
 * company.set("name", "acme");
 * company.get("department.0/name")       is the same as ((DataObject)((List)company.get("department")).get(0)).get("name")
 *                                        .n  indexes from 0 ... implies the name property of the first department
 * company.get("department[1]/name")      [] indexes from 1 ... implies the name property of the first department
 * company.get("department[number=123]")  returns the first department where number=123
 * company.get("..")                      returns the containing data object
 * company.get("/")                       returns the root containing data object
 *</pre> 
 * <p> There are general accessors for Properties, i.e., {@link #get(Property) get} and {@link #set(Property, Object) set}, 
 * as well as specific accessors for the primitive types and commonly used data types like 
 * String, Date, List, BigInteger, and BigDecimal.
 */
public interface PlasmaDataObject extends DataObject 
{
    public void setDataGraph(DataGraph dataGraph);
    

    /**
     * Sets the current container for this DataObject
     */
    public void setContainer(DataObject container);

    /** 
     * Sets the declared reference Property within the Type for the 
     * container which is our current containment reference property 
     */
    public void setContainmentProperty(Property containmentProperty);

    
    /**
     * Adds the given value to the given multi=valued property. 
     * @param property the multi-valued property
     * @param value the value to add - can be a list of values
     */
    public void add(Property property, Object value);

    /**
     * Removes the given value from this data object for 
     * the given multi-valued property. 
     * @param property multi-valued the property
     * @param value the value to remove
     * @throws UnsupportedOperationException if the given property
     * is not multi-valued 
     */
    public void remove(Property property, Object value);
    
    
    /**
     * Synchronizes the state of this data object to that of it's data store
     * given the map of UUID's and associated info resulting from
     * a successful commit.   
     * @param commitMap - the map of UUID's mapped to, e.g. data store 
     * generated sequence numbers.  
     * @param username - the user who committed the modifications.
     */
     public void reset(SnapshotMap commitMap, String username);
     

     public void remove();
     
     
     
     /**
      * Returns true if this data object is the container for the
      * given data object. 
      * @param dataObject the data object
      * @return true if this data object is the container for the
      * given data object
      */
     public boolean contains(DataObject dataObject);
     
     /**
      * Returns the UUID for this data object.
      * @return the UUID for this data object.
      */
     public UUID getUUID();
     
     /**
      * Resets the UUID after creation for cases where the UUID
      * is stored externally and services creating data objects
      * need to preserve the stored UUIDs across service calls.
      * Refreshes the integral hash and other elements dependent
      * on the cached UUID. 
      * @param uuid the UUID
      */
     public void resetUUID(UUID uuid);
     
     /**
      * Returns the UUID for this data object as a 
      * character string.
      * @return the UUID for this data object as a 
      * character string.
      */
     public String getUUIDAsString();
     
     public PlasmaDataObject getDataObject();
     
     /**
      * Begin breadth-first traversal of a DataGraph with this DataObject as the graph root, the given
      * visitor receiving "visit" events for each graph node traversed.  
      * @param visitor the graph visitor receiving traversal events
      * @see commonj.sdo.DataGraph
      * @see commonj.sdo.DataObject
      * @see PlasmaDataGraph 
      * @see PlasmaDataObject 
      * @see PlasmaDataGraphVisitor.visit()
      */
     public void accept(PlasmaDataGraphVisitor visitor);

     /**
      * Begin breadth-first traversal of a DataGraph with this DataObject as the graph root, the given
      * visitor receiving "visit" events for each graph node traversed.  
      * @param visitor the graph visitor receiving traversal events
      * @param maxLevel the maximum number of "hierarchical" levels to traverse 
      * @see commonj.sdo.DataGraph
      * @see commonj.sdo.DataObject
      * @see PlasmaDataGraph 
      * @see PlasmaDataObject 
      * @see PlasmaDataGraphVisitor.visit()
      */
     public void accept(PlasmaDataGraphVisitor visitor, int maxLevel);

     /**
      * Begin depth-first traversal of a DataGraph with this DataObject as the graph root, the given
      * visitor receiving "visit" events for each graph node traversed.  
      * @param visitor the graph visitor receiving traversal events
      * @see commonj.sdo.DataGraph
      * @see commonj.sdo.DataObject
      * @see PlasmaDataGraph 
      * @see PlasmaDataObject 
      * @see PlasmaDataGraphVisitor.visit()
      */
     public void acceptDepthFirst(PlasmaDataGraphVisitor visitor);

     
	/**
	 * Begin breadth-first traversal of a DataGraph with this DataObject as the
	 * graph root, the given visitor receiving various events for each graph
	 * node traversed.
	 * 
	 * @param visitor
	 *            the graph visitor receiving traversal events
	 * @see commonj.sdo.DataGraph
	 * @see commonj.sdo.DataObject
	 * @see PlasmaDataGraph
	 * @see PlasmaDataObject
	 * @see PlasmaDataGraphVisitor.visit()
	 */
	public void accept(PlasmaDataGraphEventVisitor visitor);


    public DataObject find(String key);
     

    public String dump();

    public String dumpDepthFirst();
    
}