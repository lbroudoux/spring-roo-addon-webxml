package com.github.lbroudoux.roo.addon.webxml;

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
}