package org.cichonski.ontviewer.model;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

/**
 * This implementation expects all of the data necessary to represent a full OwlClass upon instantiation.
 * @author Paul Cichonski
 *
 */
public class OwlClassImpl  implements OwlClass, Comparable<OwlClassImpl> {
    private final URI uri;
    private final String label;
    private final String description;
    private final Set<OwlClass> subClasses;
    private final Set<ObjectProperty> objectProperties;
    private final Set<DataTypeProperty> dataProperties;
    

    
    OwlClassImpl(
            URI uri,
            String label,
            String description,
            Set<OwlClass> subClasses,
            Set<ObjectProperty> objectProperties,
            Set<DataTypeProperty> dataProperties) {
        this.uri = uri;
        this.label = label;
        this.description = description;
        this.subClasses = subClasses;
        this.objectProperties = objectProperties;
        this.dataProperties = dataProperties;
    }

    @Override
    public URI getURI() {
        return uri;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getDescritpion() {
        return description;
    }

    @Override
    public Set<OwlClass> getSubClasses() {
        // TODO Auto-generated method stub
        return Collections.unmodifiableSet(subClasses);
    }

    @Override
    public Set<ObjectProperty> getObjectProperties() {
        return Collections.unmodifiableSet(objectProperties);
    }

    @Override
    public Set<DataTypeProperty> getDataTypeProperty() {
        return Collections.unmodifiableSet(dataProperties);
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
