package org.cichonski.ontviewer.model;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

public class PropertyImpl implements Property, Comparable<PropertyImpl> {
	private final String label;
	private final String description;
	private final URI uri;
	private final Set<URI> domains;
	private final Set<URI> ranges;
	
	
	PropertyImpl(String label, String description, URI uri, Set<URI> domains,
			Set<URI> ranges) {
		this.label = label;
		this.description = description;
		this.uri = uri;
		this.domains = domains;
		this.ranges = ranges;
	}
	
	public String getDescription() {
		return description != null ? description : "";
	}
	
	public String getLabel() {
		return label != null ? label : "";
	}
	public URI getURI() {
		return uri;
	}
	public Set<URI> getDomains() {
		return Collections.unmodifiableSet(domains);
	}
	public Set<URI> getRanges() {
		return Collections.unmodifiableSet(ranges);
	}
	
	
    @Override
    public boolean equals(Object obj) {
        if (this == obj){
            return true;
        } else if (obj instanceof PropertyImpl){
        	PropertyImpl that = (PropertyImpl) obj;
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
    public int compareTo(PropertyImpl that) {
        if (this.equals(that)){
            return 0;
        }
        return this.label.compareTo(that.label);
    }
	


}
