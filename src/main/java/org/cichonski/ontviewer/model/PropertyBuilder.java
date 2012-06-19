package org.cichonski.ontviewer.model;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

/**
 * Use to build a Property
 * @author Paul Cichonski
 *
 */
public class PropertyBuilder {
    private URI uri;
    private String label;
    private String description;
    private Set<URI> domains;
    private Set<URI> ranges;
    
    public PropertyBuilder() {
    	domains = new HashSet<URI>();
    	ranges = new HashSet<URI>();
    }
    
    /**
     * 
     * @return
     * @throws IllegalStateException if uri is null
     */
    public Property build(){
        inspect();
        return new PropertyImpl(label, description, uri, domains, ranges);
    }
    
    private void inspect(){
        if (uri == null){
            throw new IllegalStateException("uri null");
        }
        if (label == null || label.isEmpty()){
            label = uri.toString();
        }
    }

    public PropertyBuilder setUri(URI uri) {
        this.uri = uri;
        return this;
    }

    public PropertyBuilder setLabel(String label) {
        this.label = label;
        return this;
    }

    public PropertyBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public PropertyBuilder addDomain(URI domain) {
        if (domain != null){
        	domains.add(domain);
        }
        return this;
    }

    public PropertyBuilder addRange(URI range) {
        if (range != null){
        	ranges.add(range);
        }
        return this;
    }
    
    @Override
    public String toString() {
    	// TODO Auto-generated method stub
    	return uri.toString();
    }
    
    
}
