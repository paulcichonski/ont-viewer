package org.cichonski.ontviewer.model;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

/**
 * Use to build an OwlClass
 * @author Paul Cichonski
 *
 */
public class OwlClassBuilder {
    private URI uri;
    private String label;
    private String description;
    private Set<OwlClass> subClasses;
    private Set<Property> objectProperties;
    private Set<Property> dataProperties;
    
    public OwlClassBuilder() {
        // TODO Auto-generated constructor stub
    }
    
    /**
     * 
     * @return
     * @throws IllegalStateException if uri is null
     */
    public OwlClass build(){
        inspect();
        return new OwlClassImpl(uri, label, description, subClasses, objectProperties, dataProperties);
    }
    
    private void inspect(){
        if (uri == null){
            throw new IllegalStateException("uri null");
        }
        if (label == null || label.isEmpty()){
            label = uri.toString();
        }
        if (subClasses == null){
            subClasses = new HashSet<OwlClass>();
        }
        if (objectProperties == null){
            objectProperties = new HashSet<Property>();
        }
        if (dataProperties == null){
            dataProperties = new HashSet<Property>();
        }
    }

    public OwlClassBuilder setUri(URI uri) {
        this.uri = uri;
        return this;
    }

    public OwlClassBuilder setLabel(String label) {
        this.label = label;
        return this;
    }

    public OwlClassBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public OwlClassBuilder setSubClasses(Set<OwlClass> subClasses) {
        this.subClasses = subClasses;
        return this;
    }

    public OwlClassBuilder setObjectProperties(Set<Property> objectProperties) {
        this.objectProperties = objectProperties;
        return this;
    }

    public OwlClassBuilder setDataProperties(Set<Property> dataProperties) {
        this.dataProperties = dataProperties;
        return this;
    }
    
    
}
