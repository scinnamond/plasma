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

import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plasma.sdo.PlasmaNode;
import org.plasma.sdo.PlasmaSetting;
import org.plasma.sdo.helper.PlasmaTypeHelper;

import commonj.sdo.DataObject;
import commonj.sdo.Property;
import commonj.sdo.Type;

public class CoreSetting implements PlasmaSetting {

    private static Log log = LogFactory.getLog(CoreSetting.class);
    private DataObject source;
    private transient Property property;
    /** used for serialization only */
    private String propertyName;
    /** used for serialization only */
    private String propertyTypeName;
    /** used for serialization only */
    private String propertyTypeUri;
    private Object value;
    private String valuePath; // need to capture before it changes
    private boolean isSet;
    private long createdMillis = System.currentTimeMillis();
    
    @SuppressWarnings("unused")
    private CoreSetting() {}
    
    public CoreSetting(DataObject source, Property property, Object value) {
        this.source = source;
        this.property = property;
        this.value = value;
        this.isSet = source.isSet(property); // captured as setting is created and added to summary/history
        if (!property.getType().isDataType()) {
            
            if (this.value instanceof List) {
                List<DataObject> list = (List<DataObject>)this.value;
                StringBuffer buf = new StringBuffer();
                buf.append("list[");
                if (list != null) {
                    
                    for (int i = 0; i < list.size(); i++) {
                        DataObject dataObject = list.get(i);
                        if (i > 0)
                            buf.append(", ");
                        buf.append(((PlasmaNode)dataObject).getUUIDAsString());
                    }
                }
                buf.append("]");
                valuePath = buf.toString();
            }
            else {
                if (this.value != null)
                    if (this.value instanceof PlasmaNode) {
                        valuePath = ((PlasmaNode)this.value).getUUIDAsString();
                    }
                    else if (!(this.value instanceof NullValue))
                        log.error("expected instance of PlasmaNode or NullValue");                    
            }
        }
    }
    
    /**
     * Writes out metadata logical names as string
     * for serialization.
     * @param out the stream
     * @throws IOException
     */
    private void writeObject(java.io.ObjectOutputStream out) 
    		throws IOException {
	    this.propertyName = this.property.getName();
	    this.propertyTypeName = this.property.getContainingType().getName();
	    this.propertyTypeUri = this.property.getContainingType().getURI();
	    this.property = null;
    	out.defaultWriteObject();
    }
    
    /**
     * Reads in metadata logical names as string
     * during de-serialization looks up and
     * restores references.
     * @param in
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject(java.io.ObjectInputStream in)
    	     throws IOException, ClassNotFoundException {
    	in.defaultReadObject();
		Type propertyType = PlasmaTypeHelper.INSTANCE.getType(
				this.propertyTypeUri, this.propertyTypeName);
		this.property = propertyType.getProperty(this.propertyName);
	    this.propertyName = null;
	    this.propertyTypeName = null;
	    this.propertyTypeUri = null;
    }
    
    /**
     * Returns the property of the setting.
     * @return the setting property.
     */
    public Property getProperty() {
        return property;
    }

    /**
     * Returns the value of the setting.
     * @return the setting value.
     */
    public Object getValue() {
        return value;
    }

    
    public String getValuePath() {
        return valuePath;
    }

    /**
     * Returns whether or not the property is set.
     * @return <code>true</code> if the property is set.
     */
    public boolean isSet() {
        return this.isSet;
    }
    
    public long getCreatedMillis() {
        return createdMillis;
    }
    
}
