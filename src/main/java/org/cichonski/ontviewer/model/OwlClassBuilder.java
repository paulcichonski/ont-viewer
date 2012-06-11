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
    private Set<OwlClassBuilder> subClassBuilders;
    private Set<Property> objectProperties;
    private Set<Property> dataProperties;
    
    public OwlClassBuilder() {
    	subClassBuilders = new HashSet<OwlClassBuilder>();
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
        Set<OwlClass> subClasses = buildSubClasses();
        return new OwlClassImpl(uri, label, description, subClasses, objectProperties, dataProperties);
    }
    
    private Set<OwlClass> buildSubClasses(){
    	/* this is going to create a lot of duplication (when subClasses are called build() independently, 
    	 * may want to add some sort of caching/memoization if performance is an issue */
    	Set<OwlClass> subClasses = new HashSet<OwlClass>();
    	for (OwlClassBuilder b : subClassBuilders){
    		subClasses.add(b.build());
    	}
    	return subClasses;
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

    public OwlClassBuilder addSubClass(OwlClassBuilder subClass) {
        if (subClass != null){
        	subClassBuilders.add(subClass);
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
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj){
            return true;
        } else if (obj instanceof OwlClassBuilder){
        	OwlClassBuilder that = (OwlClassBuilder) obj;
            return this.uri.equals(that.uri); 
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int hashCode = 37;
        hashCode += 31 * uri.hashCode();
        return hashCode;
    }
    
    @Override
    public String toString() {
        return "builder for " + label;
    }
    
    
}
