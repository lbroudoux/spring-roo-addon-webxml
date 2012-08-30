package com.github.lbroudoux.roo.addon.webxml;

import org.springframework.roo.model.JavaType;

/**
 * Interface of operations this add-on offers. Typically used by a command type or an external add-on.
 *
 * @since 1.1
 */
public interface WebxmlOperations {

    /**
     * Indicate commands should be available
     * 
     * @return true if it should be available, otherwise false
     */
    boolean isCommandAvailable();

    /**
     * Annotate the provided Java type with the trigger of this add-on
     */
    void annotateType(JavaType type);
    
    /**
     * 
     */
    void addServlet(String name, String clazz, String mapping, Integer loading, String comment);
    
    /**
     * 
     */
    void addContextParam(String name, String value, String comment);
    
    /**
     * 
     */
    void addEnvironmentEntry(String name, String type, String value, String comment);
    
    /**
     * Annotate all Java types with the trigger of this add-on
     */
    void annotateAll();
    
    /**
     * Setup all add-on artifacts (dependencies in this case)
     */
    void setup();
}