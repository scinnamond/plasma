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
package org.plasma.sdo.core;

import java.util.UUID;

import org.plasma.sdo.PathAssembler;
import org.plasma.sdo.PlasmaDataGraph;
import org.plasma.sdo.PlasmaDataGraphEventVisitor;
import org.plasma.sdo.PlasmaDataGraphVisitor;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaNode;
import org.plasma.sdo.helper.PlasmaDataFactory;
import org.plasma.sdo.helper.PlasmaTypeHelper;

import commonj.sdo.ChangeSummary;
import commonj.sdo.DataObject;
import commonj.sdo.Type;

public class CoreDataGraph implements PlasmaDataGraph {

    private static final long serialVersionUID = 1L;

    private Object id;
    private PlasmaDataObject rootObject;  
    private CoreChangeSummary changeSummary;

    public CoreDataGraph() {
        this.changeSummary = new CoreChangeSummary(this);
    }

    public CoreDataGraph(PlasmaDataObject root) {
        this();
        this.rootObject = root;
        root.setDataGraph(this);
    }
    
	public String toString() {
        return this.rootObject.toString();
    } 
	
    /**
     * Returns the UUID for this data graph, which is the same
     * UUID used for the root Data Object.
     * @return the UUID for this data graph, which is the same
     * UUID used for the root Data Object.
     */
    public UUID getUUID() {
    	return this.rootObject.getUUID();
    }
    
    /**
     * Returns the UUID for this data graph, which is the same
     * UUID used for the root Data Object, as a 
     * character string.
     * @return the UUID for this data graph, which is the same
     * UUID used for the root Data Object, as a 
     * character string.
     */    
    public String getUUIDAsString() {
    	return this.rootObject.getUUIDAsString();
    }    
    
    /**
     * Sets an object to be used and managed by client 
     * {@link org.plasma.sdo.access.DataAccessService Data Access Services}
     * as an identifier for a Data Graph. 
     * @param id the identifier
     */
    public void setId(Object id) {
    	this.id = id;
    }
    
    /**
     * Returns an object to be used and managed by client 
     * {@link org.plasma.sdo.access.DataAccessService Data Access Services}
     * as an identifier for a Data Graph. 
     * @return the identifier
     */
    public Object getId() {
    	return this.id;
    }
    
    /**
     * Creates a new root data object of the {@link #getType specified type}. An
     * exception is thrown if a root object exists.
     * 
     * @param namespaceURI namespace of the type.
     * @param typeName name of the type.
     * @return the new root.
     * @throws IllegalStateException if the root object already exists.
     * @see #createRootObject(Type)
     * @see #getType(String, String)
     */
    public DataObject createRootObject(String namespaceURI, String typeName) {
        if (this.rootObject != null)
            throw new IllegalStateException("a root data object already exists for this data graph");
        this.rootObject = (PlasmaDataObject)PlasmaDataFactory.INSTANCE.create(namespaceURI, typeName);
        PlasmaDataObject root = ((PlasmaNode)this.rootObject).getDataObject();
        root.setDataGraph(this);
        // FIXME: why does change summary only want a plasma object ??
        this.changeSummary.created(root);        
        return this.rootObject;
    }

    /**
     * Returns the root {@link DataObject data object} of this data graph.
     * @return the root data object.
     * @see DataObject#getDataGraph
     */
    public DataObject createRootObject(Type type) {
        if (this.rootObject != null)
            throw new IllegalStateException("a root data object already exists for this data graph");
        this.rootObject = (PlasmaDataObject)PlasmaDataFactory.INSTANCE.create(type);
        PlasmaDataObject root = ((PlasmaNode)this.rootObject).getDataObject();
        root.setDataGraph(this);
        // FIXME: why does change summary only want a plasma object ??
        this.changeSummary.created(root);        
        return this.rootObject;
    }

    /**
     * Returns the {@link ChangeSummary change summary} associated with this data graph.
     * @return the change summary.
     * @see ChangeSummary#getDataGraph
     */
    public ChangeSummary getChangeSummary() {
        return this.changeSummary;
    }

    /**
     * Returns the root {@link DataObject data object} of this data graph.
     * @return the root data object.
     * @see DataObject#getDataGraph
     */
    public DataObject getRootObject() {
        return rootObject;
    }
    
    /**
     * Detaches and returns the root {@link DataObject data object} of this data graph.
     * @return the root data object.
     * @see DataObject#getDataGraph
     */
    public DataObject removeRootObject() {      
        DataObject oldRoot = this.rootObject;
        ((PlasmaNode)this.rootObject).getDataObject().setDataGraph(null);
        this.rootObject = null;
        this.changeSummary = null;
        return oldRoot;
    }

    /**
     * Returns the {@link Type type} with the given the {@link Type#getURI() URI},
     * or contained by the resource at the given URI,
     * and with the given {@link Type#getName name}.
     * @param uri the namespace URI of a type or the location URI of a resource containing a type.
     * @param typeName name of a type.
     * @return the type with the corresponding namespace and name.
     */
    public Type getType(String uri, String typeName) {
        return PlasmaTypeHelper.INSTANCE.getType(uri, typeName);
    }
    
    /**
     * Calculates and returns the path relative to the data-graph root for the
     * given data object. 
     * @param dataObject the target data object
     * @return the path string
     */
    public String getPath(DataObject dataObject) {        
        PathAssembler visitor = new CorePathAssembler(dataObject);
        ((PlasmaNode)this.rootObject).accept(visitor);
        return visitor.getMinimumPathString();
    }
    
    /**
     * Begin breadth-first traversal of this DataGraph, the given
     * visitor receiving "visit" events for each graph node traversed.  
     * @param visitor the graph visitor receiving traversal events
     * @see commonj.sdo.DataGraph
     * @see commonj.sdo.DataObject
     * @see PlasmaDataGraph 
     * @see PlasmaDataObject 
     * @see PlasmaDataGraphVisitor.visit()
     */
    public void accept(PlasmaDataGraphVisitor visitor)
    {
    	((PlasmaNode)this.rootObject).accept(visitor);
    }

    /**
     * Begin depth-first traversal of this DataGraph, the given
     * visitor receiving "visit" events for each graph node traversed.  
     * @param visitor the graph visitor receiving traversal events
     * @see commonj.sdo.DataGraph
     * @see commonj.sdo.DataObject
     * @see PlasmaDataGraph 
     * @see PlasmaDataObject 
     * @see PlasmaDataGraphVisitor.visit()
     */
    public void acceptDepthFirst(PlasmaDataGraphVisitor visitor)
    {
    	((PlasmaNode)this.rootObject).acceptDepthFirst(visitor);
    }  
    
	/**
	 * Begin breadth-first traversal of this DataGraph, the given 
	 * visitor receiving various events for each graph
	 * node traversed.
	 * 
	 * @param visitor
	 *            the graph visitor receiving traversal events
	 * @see commonj.sdo.DataGraph
	 * @see commonj.sdo.DataObject
	 * @see PlasmaDataGraph
	 * @see PlasmaDataObject
	 * @see PlasmaDataGraphEventVisitor.start()
	 * @see PlasmaDataGraphEventVisitor.end()
	 */
	public void accept(PlasmaDataGraphEventVisitor visitor) {
    	((PlasmaNode)this.rootObject).accept(visitor);
	}
	
    public String dump() {
    	return ((PlasmaDataObject)this.rootObject).dump();
    }

    public String dumpDepthFirst() {
    	return ((PlasmaDataObject)this.rootObject).dumpDepthFirst();
    }
}
