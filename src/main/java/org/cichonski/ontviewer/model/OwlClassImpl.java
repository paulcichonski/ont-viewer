package org.cichonski.ontviewer.model;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

/**
 * This implementation expects all of the data necessary to represent a full OwlClass upon instantiation.
 * @author Paul Cichonski
 *
 */
public final class OwlClassImpl implements OwlClass, Comparable<OwlClassImpl> {
    private final URI uri;
    private final String label;
    private final String description;
    private final Set<OwlClass> subClasses;
    private final Set<Property> objectProperties;
    private final Set<Property> dataProperties;
    
    private OwlClass superClass;
    

    // only builder and tests should build
    OwlClassImpl(
            URI uri,
            String label,
            String description,
            Set<OwlClass> subClasses,
            Set<Property> objectProperties,
            Set<Property> dataProperties) {
        this.uri = uri;
        this.label = label;
        this.description = description;
        this.subClasses = subClasses;
        this.objectProperties = objectProperties;
        this.dataProperties = dataProperties;
    }
    
    void setSuperClass(OwlClass superClass) {
    	// need to allow this to be set after construction or infinite loop results from building subClasses and superClasses
    	// a bit ugly.
		this.superClass = superClass;
	}

    @Override
    public URI getURI() {
        return uri;
    }

    @Override
    public String getLabel() {
        return label != null ? label : "";
    }

    @Override
    public String getDescritpion() {
        return description != null ? description : "";
    }

    @Override
    public Set<OwlClass> getSubClasses() {
        return Collections.unmodifiableSet(subClasses);
    }

    @Override
    public Set<Property> getObjectProperties() {
        return Collections.unmodifiableSet(objectProperties);
    }

    @Override
    public Set<Property> getDataTypeProperty() {
        return Collections.unmodifiableSet(dataProperties);
    }
    
    @Override
    public OwlClass getSuperClass() {
    	return superClass;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj){
            return true;
        } else if (obj instanceof OwlClassImpl){
            OwlClassImpl that = (OwlClassImpl) obj;
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
        return label;
    }

    @Override
    public int compareTo(OwlClassImpl that) {
        if (this.equals(that)){
            return 0;
        }
        return this.label.compareTo(that.label);
    }

}
