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
package org.plasma.text.lang3gl.java;

import java.util.Map;
import java.util.TreeMap;

import org.plasma.config.InterfaceProvisioning;
import org.plasma.config.Namespace;
import org.plasma.config.PlasmaConfig;
import org.plasma.config.PropertyNameStyle;
import org.plasma.provisioning.Class;
import org.plasma.provisioning.ClassRef;
import org.plasma.provisioning.Package;
import org.plasma.provisioning.Property;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.text.lang3gl.ClassNameResolver;
import org.plasma.text.lang3gl.InterfaceFactory;
import org.plasma.text.lang3gl.Lang3GLContext;


public class SDOInterfaceFactory extends SDODefaultFactory 
    implements InterfaceFactory {

	
	public SDOInterfaceFactory(Lang3GLContext context) {
		super(context);		
	}
		
	public String createContent(Package pkg, Class clss) {
		StringBuilder buf = new StringBuilder();
		
		buf.append(this.createPackageDeclaration(pkg));
		buf.append(LINE_SEP);
		buf.append(this.createThirdPartyImportDeclarations(pkg, clss));
		buf.append(LINE_SEP);
		
		buf.append(this.createSDOInterfaceReferenceImportDeclarations(pkg, clss));
		buf.append(LINE_SEP);
		buf.append(LINE_SEP);
		buf.append(this.createTypeDeclaration(pkg, clss));
		buf.append(LINE_SEP);
		buf.append(this.beginBody());

		buf.append(LINE_SEP);
		buf.append(this.createStaticFieldDeclarations(clss));

		buf.append(LINE_SEP);
		buf.append(this.createMethodDeclarations(clss));
		
		for (Property field : clss.getProperties()) {
			buf.append(LINE_SEP);
			buf.append(this.createMethodDeclarations(clss, field));
		}
		
		buf.append(LINE_SEP);
		buf.append(this.endBody());
		return buf.toString();
	}

	protected String createThirdPartyImportDeclarations(Package pkg, Class clss) {
		StringBuilder buf = new StringBuilder();
		
		// FIXME: add array/list accessor collection config option
		//if (!hasOnlySingilarFields(clss)) {
		//	buf.append(LINE_SEP);
		//	buf.append(this.createImportDeclaration(pkg, clss, List.class.getName()));
		//}
		return buf.toString();
	}

	protected String createTypeDeclaration(Package pkg, Class clss) {
		StringBuilder buf = new StringBuilder();
		
		String javadoc = createTypeDeclarationJavadoc(pkg, clss);
		buf.append(javadoc);
		SDOInterfaceNameResolver interfaceResolver = new SDOInterfaceNameResolver();
		
	    buf.append(LINE_SEP);	
		buf.append("public interface ");
		buf.append(interfaceResolver.getName(clss));
		buf.append(" extends ");
		if (clss.getSuperClasses() != null && clss.getSuperClasses().size() > 0) {
		    int i = 0;
			for (ClassRef ref : clss.getSuperClasses()) {
				if (i > 0)
			        buf.append(", ");
				buf.append(ref.getName());
				i++;
		    }	
		}
		else {
			// always extends DO so we can cast from its impl to any generated interface
			buf.append(PlasmaDataObject.class.getSimpleName());			
		}
		
			
		return buf.toString();
	}	
	
	private String createTypeDeclarationJavadoc(Package pkg, Class clss) {
		StringBuilder buf = new StringBuilder();
		
		buf.append("/**"); // begin javadoc
		
		// add formatted doc from UML if exists		
		// always put model definition first so it appears
		// on package summary line for class
		String docs = getWrappedDocmentations(clss.getDocumentations(), 0);
		if (docs.trim().length() > 0) {
		    buf.append(docs);
		    
		    // if we have model docs, set up the next section w/a "header"
		    buf.append(newline(0));	
			buf.append(" * <p></p>");
		}
		
	    buf.append(newline(0));	
		buf.append(" * Generated interface representing the domain model entity <b>");
		buf.append(clss.getName());
		buf.append("</b>. This <a href=\"http://plasma-sdo.org\">SDO</a> interface directly reflects the");
		buf.append(newline(0));	
		buf.append(" * class (single or multiple) inheritance lattice of the source domain model(s) ");		
		buf.append(" and is part of namespace <b>");
		buf.append(clss.getUri());
		buf.append("</b> defined within the <a href=\"http://docs.plasma-sdo.org/api/org/plasma/config/package-summary.html\">Configuration</a>.");
		
		// data store mapping
		if (clss.getAlias() != null && clss.getAlias().getPhysicalName() != null) {
		    buf.append(newline(0));	
			buf.append(" *"); 
		    buf.append(newline(0));	
			buf.append(" * <p></p>");
		    buf.append(newline(0));	
			buf.append(" * <b>Data Store Mapping:</b>");
		    buf.append(newline(0));	
			buf.append(" * Corresponds to the physical data store entity <b>");
			buf.append(clss.getAlias().getPhysicalName());
			buf.append("</b>.");
		    buf.append(newline(0));	
			buf.append(" * <p></p>");		
		    buf.append(newline(0));	
			buf.append(" *"); 
		}		

		// add @see items for referenced classes
		Map<String, Class> classMap = new TreeMap<String, Class>();
		if (clss.getSuperClasses() != null && clss.getSuperClasses().size() > 0)		
		    this.collectProvisioningSuperclasses(pkg, clss, classMap);
		//for interfaces we have definitions for all methods generated
		// based on local fields, not fields from superclasses
		collectProvisioningClasses(pkg, clss, classMap);
		for (Class refClass : classMap.values()) {
			Namespace sdoNamespace = PlasmaConfig.getInstance().getSDONamespaceByURI(refClass.getUri());
			String packageName = sdoNamespace.getProvisioning().getPackageName();
			String packageQualifiedName = packageName + "." + refClass.getName(); 	
		    buf.append(newline(0));	
			buf.append(" * @see ");
			buf.append(packageQualifiedName);
			buf.append(" ");
			buf.append(refClass.getName());			
		}		
		
	    buf.append(newline(0));	
		buf.append(" */"); // end javadoc
		
		return buf.toString();
	}	
	
	protected String createStaticFieldDeclarations(Class clss) {
		StringBuilder buf = new StringBuilder();
		
		// the namespace URI
		buf.append(this.indent(1));
		buf.append("/** The <a href=\"http://plasma-sdo.org\">SDO</a> namespace URI associated with the <a href=\"http://docs.plasma-sdo.org/api/org/plasma/sdo/PlasmaType.html\">Type</a> for this class. */");
		buf.append(this.newline(1));
		buf.append("public static final String NAMESPACE_URI = \"");
		buf.append(clss.getUri());
		buf.append("\";");
		buf.append(LINE_SEP);
		
		//the entity name
		buf.append(this.newline(1));
		buf.append("/** The entity or <a href=\"http://docs.plasma-sdo.org/api/org/plasma/sdo/PlasmaType.html\">Type</a> logical name associated with this class. */");
		buf.append(this.newline(1));
		buf.append("public static final String TYPE_NAME_");
		buf.append(toConstantName(clss.getName()));
		buf.append(" = \"");
		buf.append(clss.getName());
		buf.append("\";");

		buf.append(this.newline(1));

		switch (this.interfaceProvisioning.getPropertyNameStyle()) {
		case ENUMS:
			// the static enums
			buf.append(this.newline(1));
			buf.append("/** The declared logical property names for this <a href=\"http://docs.plasma-sdo.org/api/org/plasma/sdo/PlasmaType.html\">Type</a>. */");
			buf.append(this.newline(1));
			buf.append("public static enum PROPERTY {");
			int enumCount = 0;
			for (Property field : clss.getProperties()) {
				if (enumCount > 0)
					buf.append(",");
				buf.append(this.newline(2));	
			    String javadoc = createStaticFieldDeclarationJavadoc(clss, field, 2);
			    buf.append(javadoc);				
				buf.append(this.newline(2));
				buf.append(field.getName());
				enumCount++;
			}
			buf.append(this.newline(1));
			buf.append("}");
			break;
		case CONSTANTS:
			// static constants
			buf.append(this.newline(1));
			for (Property field : clss.getProperties()) {
				buf.append(this.newline(1));	
				
			    String javadoc = createStaticFieldDeclarationJavadoc(clss, field, 1);
			    buf.append(javadoc);
				
				buf.append(this.newline(1));
				buf.append("public static final String ");
				buf.append(toConstantName(field.getName()));
				buf.append(" = \"");
				buf.append(field.getName());
				buf.append("\";");
			}
			buf.append(this.newline(1));
			break;
			default:	
		}

		return buf.toString();
	}

	private String createStaticFieldDeclarationJavadoc(Class clss, Property field, int indent)
	{
		StringBuilder buf = new StringBuilder();
		buf.append(this.newline(indent));
		buf.append("/**"); // begin javadoc
		
		// add formatted doc from UML if exists		
		// always put model definition first so it appears
		// on package summary line for class
		String docs = getWrappedDocmentations(field.getDocumentations(), indent);
		if (docs.trim().length() > 0) {
		    buf.append(docs);	
		    buf.append(newline(indent));	
			buf.append(" * <p></p>");
	        buf.append(newline(indent));	
			buf.append(" *");
		}
		
        buf.append(newline(indent));	
		buf.append(" * Represents the logical <a href=\"http://docs.plasma-sdo.org/api/org/plasma/sdo/PlasmaProperty.html\">Property</a> <b>");
		buf.append(field.getName());
		buf.append("</b> which is part of the <a href=\"http://docs.plasma-sdo.org/api/org/plasma/sdo/PlasmaType.html\">Type</a> <b>");
		buf.append(clss.getName());
		buf.append("</b>."); 			
		
		// data store mapping
		if (clss.getAlias() != null && clss.getAlias().getPhysicalName() != null && 
				field.getAlias() != null && field.getAlias().getPhysicalName() != null) {
			buf.append(this.newline(indent));
			buf.append(" *"); 
			buf.append(this.newline(indent));
			buf.append(" * <p></p>");
			buf.append(this.newline(indent));
			buf.append(" * <b>Data Store Mapping:</b>");
			buf.append(this.newline(indent));
			buf.append(" * Corresponds to the physical data store element <b>");
			buf.append(clss.getAlias().getPhysicalName() + "." + field.getAlias().getPhysicalName());
			buf.append("</b>.");
		}		
		
		buf.append(this.newline(indent));
		buf.append(" */"); // end javadoc			
		return buf.toString();
	}	
	
	protected String createMethodDeclarations(Class clss) {
		// TODO Auto-generated method stub
		return "";
	}

	protected String createMethodDeclarations(Class clss, Property field) {
		StringBuilder buf = new StringBuilder();
		TypeClassInfo typeClassName = this.getTypeClassName(field.getType());

		buf.append(LINE_SEP);			    
		createIsSetDeclaration(null, clss, field, typeClassName, buf);
		buf.append(";");
		
		buf.append(LINE_SEP);			    
		createUnsetterDeclaration(null, clss, field, typeClassName, buf);
		buf.append(";");		

		if (field.getType() instanceof ClassRef) {
			Class targetClass = this.context.findClass((ClassRef)field.getType());
			
			if (!targetClass.isAbstract()) { 
			    buf.append(LINE_SEP);			    
			    createCreatorDeclaration(null, clss, field, typeClassName, buf);
			    buf.append(";");
			}
			else { 
			    buf.append(LINE_SEP);			    
			    createCreatorByAbstractClassDeclaration(null, clss, field, typeClassName, buf);
			    buf.append(";");
			}
		}
		
		if (!field.isMany()) {
			buf.append(LINE_SEP);			    
			createSingularGetterDeclaration(null, clss, field, typeClassName, buf);
			buf.append(";");
			
			buf.append(LINE_SEP);			    
			createSingularSetterDeclaration(null, clss, field, typeClassName, buf);
			buf.append(";");
		}
		else {
			buf.append(LINE_SEP);			    
			createManyGetterDeclaration(null, clss, field, typeClassName, buf);
			buf.append(";");
			
			buf.append(LINE_SEP);			    
			createManyIndexGetterDeclaration(null, clss, field, typeClassName, buf);
			buf.append(";");
			
			buf.append(LINE_SEP);			    
			createManyCountDeclaration(null, clss, field, typeClassName, buf);
			buf.append(";");

			buf.append(LINE_SEP);			    
			createManySetterDeclaration(null, clss, field, typeClassName, buf);
			buf.append(";");

			buf.append(LINE_SEP);			    
			createManyAdderDeclaration(null, clss, field, typeClassName, buf);
			buf.append(";");
			
			buf.append(LINE_SEP);			    
			createManyRemoverDeclaration(null, clss, field, typeClassName, buf);
			buf.append(";");			
			
		}		
		
		return buf.toString();
	}
	
	public String createFileName(Class clss) {
		SDOInterfaceNameResolver interfaceResolver = new SDOInterfaceNameResolver();
		StringBuilder buf = new StringBuilder();		
		buf.append(interfaceResolver.getName(clss));
		buf.append(".java");		
		return buf.toString();
	}

	protected String createSDOInterfaceReferenceImportDeclarations(Package pkg, Class clss) {
		StringBuilder buf = new StringBuilder();
		
		// for interfaces we extend our superclasses, so need to reference them
		// FIXME: only 1 level though
		ClassNameResolver resolver = new SDOInterfaceNameResolver();
		Map<String, String> nameMap = new TreeMap<String, String>();
		if (clss.getSuperClasses() != null && clss.getSuperClasses().size() > 0)		
		    this.collectSuperclassNames(pkg, clss, nameMap, resolver);
		else // it extends DataObject, so import it
			nameMap.put(PlasmaDataObject.class.getName(), PlasmaDataObject.class.getName());
		
		//for interfaces we have definitions for all methods generated
		// based on local fields, not fields from superclasses
		collectDataClassNames(pkg, clss, nameMap, resolver);		
		collectReferenceClassNames(pkg, clss, nameMap, resolver);
		
		for (String name : nameMap.values()) {
			if (name.startsWith("java.lang."))
				continue;
		    buf.append(LINE_SEP);	
		    buf.append("import ");
		    buf.append(name);
		    buf.append(";");
		}
		
		return buf.toString();
	}
	
}
