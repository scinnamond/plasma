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
package org.plasma.xml.wsdl.v11;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.plasma.xml.wsdl.v11 package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _TOperationOutput_QNAME = new QName("http://schemas.xmlsoap.org/wsdl/", "output");
    private final static QName _TOperationInput_QNAME = new QName("http://schemas.xmlsoap.org/wsdl/", "input");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.plasma.xml.wsdl.v11
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link TBindingOperationMessage }
     * 
     */
    public TBindingOperationMessage createTBindingOperationMessage() {
        return new TBindingOperationMessage();
    }

    /**
     * Create an instance of {@link TPart }
     * 
     */
    public TPart createTPart() {
        return new TPart();
    }

    /**
     * Create an instance of {@link TDocumented }
     * 
     */
    public TDocumented createTDocumented() {
        return new TDocumented();
    }

    /**
     * Create an instance of {@link TService }
     * 
     */
    public TService createTService() {
        return new TService();
    }

    /**
     * Create an instance of {@link TPortType }
     * 
     */
    public TPortType createTPortType() {
        return new TPortType();
    }

    /**
     * Create an instance of {@link Definitions }
     * 
     */
    public Definitions createDefinitions() {
        return new Definitions();
    }

    /**
     * Create an instance of {@link TPort }
     * 
     */
    public TPort createTPort() {
        return new TPort();
    }

    /**
     * Create an instance of {@link TFault }
     * 
     */
    public TFault createTFault() {
        return new TFault();
    }

    /**
     * Create an instance of {@link TTypes }
     * 
     */
    public TTypes createTTypes() {
        return new TTypes();
    }

    /**
     * Create an instance of {@link TParam }
     * 
     */
    public TParam createTParam() {
        return new TParam();
    }

    /**
     * Create an instance of {@link TImport }
     * 
     */
    public TImport createTImport() {
        return new TImport();
    }

    /**
     * Create an instance of {@link TMessage }
     * 
     */
    public TMessage createTMessage() {
        return new TMessage();
    }

    /**
     * Create an instance of {@link TDocumentation }
     * 
     */
    public TDocumentation createTDocumentation() {
        return new TDocumentation();
    }

    /**
     * Create an instance of {@link TOperation }
     * 
     */
    public TOperation createTOperation() {
        return new TOperation();
    }

    /**
     * Create an instance of {@link TBinding }
     * 
     */
    public TBinding createTBinding() {
        return new TBinding();
    }

    /**
     * Create an instance of {@link TBindingOperation }
     * 
     */
    public TBindingOperation createTBindingOperation() {
        return new TBindingOperation();
    }

    /**
     * Create an instance of {@link TBindingOperationFault }
     * 
     */
    public TBindingOperationFault createTBindingOperationFault() {
        return new TBindingOperationFault();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TParam }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/wsdl/", name = "output", scope = TOperation.class)
    public JAXBElement<TParam> createTOperationOutput(TParam value) {
        return new JAXBElement<TParam>(_TOperationOutput_QNAME, TParam.class, TOperation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TParam }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/wsdl/", name = "input", scope = TOperation.class)
    public JAXBElement<TParam> createTOperationInput(TParam value) {
        return new JAXBElement<TParam>(_TOperationInput_QNAME, TParam.class, TOperation.class, value);
    }

}
