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
package org.plasma.query;


/**
 *  A non-reference property with a type which is a String data type.  
 */
public interface IntegralDataProperty extends DataProperty {

	
	/**
	 * Constructs a value function, which returns absolute value of the data, and 
	 * returns this data property for use in subsequent operations
	 * @return this data property for use in subsequent operations
	 */
	public IntegralDataProperty abs();
	
}