package org.cichonski.ontviewer.model;

import java.net.URI;
import java.util.Set;

/**
 * Most of the information about an OWL class (not everything the spec has though).
 * @author Paul Cichonski
 *
 */
public interface OwlClass {

    /**
     * @return the identifier for the class
     */
    URI getURI();
    
    /**
     * @return Class label (i.e., human-friendly name)
     */
    String getLabel();
    
    /**
     * @return class description
     */
    String getDescritpion();
    
    Set<OwlClass> getSubClasses();
    
    Set<ObjectProperty> getObjectProperties();
    
    Set<DataTypeProperty> getDataTypeProperty();
}
