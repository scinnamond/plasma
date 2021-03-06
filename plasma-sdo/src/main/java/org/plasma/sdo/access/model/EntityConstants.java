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
package org.plasma.sdo.access.model;

import org.plasma.common.env.EnvConstants;



@Deprecated
public interface EntityConstants
{

    //public static final String PLASMA_SDO_MODEL_PACKAGE_PROPERTY = "plasma.sdo.package";
    //public static final String PLASMA_SDO_ACCESS_MODEL_PACKAGE_PROPERTY = "plasma.sdo.access.package";

    public static final String JAVAX_JDO_OPTION_CONNECTIONUSERNAME = "javax.jdo.option.ConnectionUserName";
    public static final String JAVAX_JDO_OPTION_CONNECTIONURL = "javax.jdo.option.ConnectionURL";
    public static final String JAVAX_JDO_OPTION_CONNECTIONPASSWORD = "javax.jdo.option.ConnectionPassword";
    public static final String JAVAX_JDO_OPTION_CONNECTIONDRIVERNAME = "javax.jdo.option.ConnectionDriverName";
    public static final String JAVAX_JDO_OPTION_CONNECTIONFACTORYNAME = "javax.jdo.option.ConnectionFactoryName";

    
    
    public static final String JPA_METADATA_INIT_ON_STARTUP = "org.plasma.sdo.jpa.metadataInitOnStartup";
    

    public static final String DATA_ACCESS_CLASS_MEMBER_PREFIX = "";
    public static final String DATA_ACCESS_CLASS_MEMBER_MULTI_VALUED_SUFFIX = "";
    public static final String DATA_ACCESS_TRAVERSAL_PATH_DELIMITER = ".";
    public static final String DATA_ACCESS_DECLARATION_DELIMITER = ",";
}