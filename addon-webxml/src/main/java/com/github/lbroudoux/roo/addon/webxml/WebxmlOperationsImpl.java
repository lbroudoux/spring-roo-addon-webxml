package com.github.lbroudoux.roo.addon.webxml;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.DependencyScope;
import org.springframework.roo.project.DependencyType;
import org.springframework.roo.project.Repository;
import org.springframework.roo.support.util.WebXmlUtils;
import org.springframework.roo.support.util.XmlElementBuilder;
import org.springframework.roo.support.util.WebXmlUtils.WebXmlParam;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Implementation of operations this add-on offers.
 *
 * @since 1.1
 */
@Component // Use these Apache Felix annotations to register your commands class in the Roo container
@Service
public class WebxmlOperationsImpl implements WebxmlOperations {
    
    @Reference private FileManager fileManager;
    @Reference private PathResolver pathResolver;
    
    private static final String WEB_APP_XPATH = "/web-app/";
    private static final String WHITESPACE = "[ \t\r\n]";
   
    /**
     * Use ProjectOperations to install new dependencies, plugins, properties, etc into the project configuration
     */
    @Reference private ProjectOperations projectOperations;

    /**
     * Use TypeLocationService to find types which are annotated with a given annotation in the project
     */
    @Reference private TypeLocationService typeLocationService;
    
    /**
     * Use TypeManagementService to change types
     */
    @Reference private TypeManagementService typeManagementService;

    /** {@inheritDoc} */
    public boolean isCommandAvailable() {
        // Check if a project has been created
        return projectOperations.isFocusedProjectAvailable();
    }

    /** {@inheritDoc} */
    public void annotateType(JavaType javaType) {
        // Use Roo's Assert type for null checks
        Validate.notNull(javaType, "Java type required");

        // Obtain ClassOrInterfaceTypeDetails for this java type
        ClassOrInterfaceTypeDetails existing = typeLocationService.getTypeDetails(javaType);

        // Test if the annotation already exists on the target type
        if (existing != null && MemberFindingUtils.getAnnotationOfType(existing.getAnnotations(), new JavaType(RooWebxml.class.getName())) == null) {
            ClassOrInterfaceTypeDetailsBuilder classOrInterfaceTypeDetailsBuilder = new ClassOrInterfaceTypeDetailsBuilder(existing);
            
            // Create JavaType instance for the add-ons trigger annotation
            JavaType rooRooWebxml = new JavaType(RooWebxml.class.getName());

            // Create Annotation metadata
            AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(rooRooWebxml);
            
            // Add annotation to target type
            classOrInterfaceTypeDetailsBuilder.addAnnotation(annotationBuilder.build());
            
            // Save changes to disk
            typeManagementService.createOrUpdateTypeOnDisk(classOrInterfaceTypeDetailsBuilder.build());
        }
    }
    
    /** {@inheritDoc} */
    public void addServlet(String name, String clazz, String mapping, Integer loading, String comment){
       Document webXmlDoc = retrieveWebXmlDocument();
       WebXmlUtils.addServlet(name, clazz, mapping, loading, webXmlDoc, comment);
       writeWebXmlDocument(webXmlDoc);
    }
    
    /** {@inheritDoc} */
    public void addContextParam(String name, String value, String comment){
       Document webXmlDoc = retrieveWebXmlDocument();
       WebXmlUtils.addContextParam(new WebXmlParam(name, value), webXmlDoc, comment);
       writeWebXmlDocument(webXmlDoc);
    }
    
    /** {@inheritDoc} */
    public void addEnvironmentEntry(String name, String type, String value, String comment){
       Document webXmlDoc = retrieveWebXmlDocument();
       addEnvironmentEntry(webXmlDoc, name, type, value, comment);
       writeWebXmlDocument(webXmlDoc);
    }

    /** {@inheritDoc} */
    public void annotateAll() {
        // Use the TypeLocationService to scan project for all types with a specific annotation
        for (JavaType type: typeLocationService.findTypesWithAnnotation(new JavaType("org.springframework.roo.addon.javabean.RooJavaBean"))) {
            annotateType(type);
        }
    }
    
    /** {@inheritDoc} */
    public void setup() {
        // Install the add-on Google code repository needed to get the annotation 
        projectOperations.addRepository("", new Repository("Webxml Roo add-on repository", "Webxml Roo add-on repository", "https://com-github-lbroudoux-roo-addon-webxml.googlecode.com/svn/repo"));
        
        List<Dependency> dependencies = new ArrayList<Dependency>();
        
        // Install the dependency on the add-on jar (
        dependencies.add(new Dependency("com.github.lbroudoux.roo.addon", "com.github.lbroudoux.roo.addon.webxml", "0.1.0.BUILD-SNAPSHOT", DependencyType.JAR, DependencyScope.PROVIDED));
        
        // Install dependencies defined in external XML file
        for (Element dependencyElement : XmlUtils.findElements("/configuration/batch/dependencies/dependency", XmlUtils.getConfiguration(getClass()))) {
            dependencies.add(new Dependency(dependencyElement));
        }

        // Add all new dependencies to pom.xml
        projectOperations.addDependencies("", dependencies);
    }
    
    private Document retrieveWebXmlDocument(){
       // Verify that the web.xml already exists
       final String webXmlPath = pathResolver.getFocusedIdentifier( Path.SRC_MAIN_WEBAPP, "WEB-INF/web.xml");
       Validate.isTrue(fileManager.exists(webXmlPath), "'" + webXmlPath + "' does not exist");

       final Document webXmlDoc = XmlUtils.readXml(fileManager.getInputStream(webXmlPath));
       
       return webXmlDoc;
    }
    
    private void writeWebXmlDocument(Document document){
       final String webXmlPath = pathResolver.getFocusedIdentifier( Path.SRC_MAIN_WEBAPP, "WEB-INF/web.xml");
       Validate.isTrue(fileManager.exists(webXmlPath), "'" + webXmlPath + "' does not exist");
       
       XmlUtils.writeFormattedXml(fileManager.updateFile(webXmlPath).getOutputStream(), document);
    }
    
    private void addEnvironmentEntry(Document document, String name, String type, String value, String comment){
       Validate.notNull(document, "Web XML document required");
       Validate.notNull(name, "Env entry name required");
       
       Element envEntryElement = XmlUtils.findFirstElement(WEB_APP_XPATH
             + "env-entry[env-entry-name = '" + name + "']", document.getDocumentElement());
       
       if (envEntryElement == null) {
          envEntryElement = new XmlElementBuilder("env-entry", document)
             .addChild(new XmlElementBuilder("env-entry-name", document).setText(name).build())
             .build();
          insertBetween(envEntryElement, "error-page[last()]", "the-end", document);
          if (StringUtils.isNotBlank(comment)) {
              addCommentBefore(envEntryElement, comment, document);
          }
      }
       appendChildIfNotPresent(envEntryElement, new XmlElementBuilder(
             "env-entry-type", document).setText(type)
             .build());
      appendChildIfNotPresent(envEntryElement, new XmlElementBuilder(
              "env-entry-value", document).setText(value)
              .build());
    }
    
    private static void addCommentBefore(final Element element,
          final String comment, final Document document) {
       if (null == XmlUtils.findNode("//comment()[.=' " + comment + " ']",
             document.getDocumentElement())) {
          document.getDocumentElement().insertBefore(
                document.createComment(" " + comment + " "), element);
          addLineBreakBefore(element, document);
      }
    }
    
    private static void addLineBreakBefore(final Element element,
          final Document document) {
       document.getDocumentElement().insertBefore(
             document.createTextNode("\n    "), element);
    }
    
    private static void insertBetween(final Element element,
          final String afterElementName, final String beforeElementName,
          final Document document) {
       final Element beforeElement = XmlUtils.findFirstElement(WEB_APP_XPATH
             + beforeElementName, document.getDocumentElement());
       if (beforeElement != null) {
          document.getDocumentElement().insertBefore(element, beforeElement);
          addLineBreakBefore(element, document);
          addLineBreakBefore(element, document);
          return;
       }

       final Element afterElement = XmlUtils.findFirstElement(WEB_APP_XPATH
             + afterElementName, document.getDocumentElement());
       if (afterElement != null && afterElement.getNextSibling() != null
             && afterElement.getNextSibling() instanceof Element) {
          document.getDocumentElement().insertBefore(element,
                afterElement.getNextSibling());
          addLineBreakBefore(element, document);
          addLineBreakBefore(element, document);
          return;
       }

       document.getDocumentElement().appendChild(element);
       addLineBreakBefore(element, document);
       addLineBreakBefore(element, document);
    }
    
    /**
     * Adds the given child to the given parent if it's not already there
     * 
     * @param parent the parent to which to add a child (required)
     * @param child the child to add if not present (required)
     */
    private static void appendChildIfNotPresent(final Node parent,
            final Element child) {
        final NodeList existingChildren = parent.getChildNodes();
        for (int i = 0; i < existingChildren.getLength(); i++) {
            final Node existingChild = existingChildren.item(i);
            if (existingChild instanceof Element) {
                // Attempt matching of possibly nested structures by using of
                // 'getTextContent' as 'isEqualNode' does not match due to line
                // returns, etc
                // Note, this does not work if child nodes are appearing in a
                // different order than expected
                if (existingChild.getNodeName().equals(child.getNodeName())
                        && existingChild
                                .getTextContent()
                                .replaceAll(WHITESPACE, "")
                                .trim()
                                .equals(child.getTextContent().replaceAll(
                                        WHITESPACE, ""))) {
                    // If we found a match, there is no need to append the child
                    // element
                    return;
                }
            }
        }
        parent.appendChild(child);
    }
}