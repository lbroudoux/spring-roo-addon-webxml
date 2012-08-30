package com.github.lbroudoux.roo.addon.webxml;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

/**
 * Sample of a command class. The command class is registered by the Roo shell following an
 * automatic classpath scan. You can provide simple user presentation-related logic in this
 * class. You can return any objects from each method, or use the logger directly if you'd
 * like to emit messages of different severity (and therefore different colours on 
 * non-Windows systems).
 * 
 * @since 1.1
 */
@Component // Use these Apache Felix annotations to register your commands class in the Roo container
@Service
public class WebxmlCommands implements CommandMarker { // All command types must implement the CommandMarker interface
    
    /**
     * Get a reference to the WebxmlOperations from the underlying OSGi container
     */
    @Reference private WebxmlOperations operations;
    
    /**
     * This method is optional. It allows automatic command hiding in situations when the command should not be visible.
     * For example the 'entity' command will not be made available before the user has defined his persistence settings 
     * in the Roo shell or directly in the project.
     * 
     * You can define multiple methods annotated with {@link CliAvailabilityIndicator} if your commands have differing
     * visibility requirements.
     * 
     * @return true (default) if the command should be visible at this stage, false otherwise
     */
    @CliAvailabilityIndicator({ "webxml setup", "webxml add", "webxml all" })
    public boolean isCommandAvailable() {
        return operations.isCommandAvailable();
    }
    
    /**
     * This method registers a command with the Roo shell. It also offers a mandatory command attribute.
     * 
     * @param type 
     */
    @CliCommand(value = "webxml add", help = "Some helpful description")
    public void add(@CliOption(key = "type", mandatory = true, help = "The java type to apply this annotation to") JavaType target) {
        operations.annotateType(target);
    }
    
    @CliCommand(value = "webxml add-servlet", help = "Add a new servlet definition to web.xml")
    public void addServlet(@CliOption(key = "name", mandatory = true, help = "The servlet-name web.xml tag value") String name, 
          @CliOption(key = "class", mandatory = true, help = "The servlet-class web.xml tag value") String clazz,
          @CliOption(key = "mapping", mandatory = true, help = "The servlet mapping url-pattern web.xml tag value") String mapping,
          @CliOption(key = "loading", mandatory = false, help = "The value for load-on-startup web.xml tag") Integer loading,
          @CliOption(key = "comment", mandatory = false, help = "A xml comment to add into web.xml for this servlet") String comment){
       operations.addServlet(name, clazz, mapping, loading, comment);
    }
    
    @CliCommand(value = "webxml add-context-param", help = "Add a new context parameter to web.xml")
    public void addContextParam(@CliOption(key = "name", mandatory = true, help = "The param-name web.xml tag value") String name, 
          @CliOption(key = "value", mandatory = true, help = "The param-value web.xml tag value") String value,
          @CliOption(key = "comment", mandatory = false, help = "A xml comment to add into web.xml for this param") String comment){
       operations.addContextParam(name, value, comment);
    }
    
    @CliCommand(value = "webxml add-env-entry", help = "Add an environment entry to web.xml")
    public void addEnvEntry(@CliOption(key = "name", mandatory = true, help = "The env-entry-name web.xml tag value") String name,
          @CliOption(key = "type", mandatory = true, help = "The env-entry-type web.xml tag value (must be valid value defined by JEE reference") String type,
          @CliOption(key = "value", mandatory = true, help = "The env-entry-value web.xml tag value") String value,
          @CliOption(key = "comment", mandatory = false, help = "A xml comment to add into web.xml for this entry") String comment){
       operations.addEnvironmentEntry(name, type, value, comment);
    }
    
    /**
     * This method registers a command with the Roo shell. It has no command attribute.
     * 
     */
    @CliCommand(value = "webxml all", help = "Some helpful description")
    public void all() {
        operations.annotateAll();
    }
    
    /**
     * This method registers a command with the Roo shell. It has no command attribute.
     * 
     */
    @CliCommand(value = "webxml setup", help = "Setup Webxml addon")
    public void setup() {
        operations.setup();
    }
}