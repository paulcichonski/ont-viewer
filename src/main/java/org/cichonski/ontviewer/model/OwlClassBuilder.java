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
        subClasses = new HashSet<OwlClass>();
        objectProperties = new HashSet<Property>();
        dataProperties = new HashSet<Property>();
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
    }

    /**
     * Convenience method for inspecting the URI being built.
     * @return
     */
    public URI getUri() {
		return uri;
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

    public OwlClassBuilder addSubClass(OwlClass subClass) {
        if (subClass != null){
        	subClasses.add(subClass);
        }
        return this;
    }

    public OwlClassBuilder addObjectProperty(Property objectProperty) {
        if (objectProperty != null){
        	objectProperties.add(objectProperty);
        }
        return this;
    }


    public OwlClassBuilder addDataTypeProperty(Property dataProperty) {
        if (dataProperty != null){
        	dataProperties.add(dataProperty);
        }
        return this;
    }
    
    
}
