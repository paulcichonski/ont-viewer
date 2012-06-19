package org.cichonski.ontviewer.model;

import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Use to build an OwlClass
 * @author Paul Cichonski
 *
 */
public class OwlClassBuilder {
    // must be invariant...would screw up set equality computations.
    private final URI uri;
    private String label;
    private String description;
    private Set<OwlClassBuilder> subClassBuilders;
    private Set<Property> objectProperties;
    private Set<Property> dataProperties;
    
    public OwlClassBuilder(URI uri) {
        this.uri = uri;
    	subClassBuilders = new HashSet<OwlClassBuilder>();
        objectProperties = new HashSet<Property>();
        dataProperties = new HashSet<Property>();
    }
    
	/**
	 * NOTE: This not threadsafe (map will be altered...writes-only, no
	 * deletes), clients should either pass inherently threadsafe map if this is running in multiple threads.
	 * 
	 * @param classMemoizer - cache of pre-built owlClasses, the stored owlClass will be used
	 *        if current builder is in the map. This is required to allow us to
	 *        build both subClasses and superClasses without going into an
	 *        infinite loop; it also may prevent duplicate builds if cache is
	 *        shared accross multiple root level classes.
	 * @return
	 * @throws IllegalStateException
	 *             - if uri is null
	 * @throws NullPointerException
	 *             - if cache is null
	 */
    public OwlClass build(Map<OwlClassBuilder, OwlClass> classMemoizer){
    	if (classMemoizer == null){
    		throw new NullPointerException("cache cannot be null");
    	}
    	if (classMemoizer.containsKey(this)){
    		return classMemoizer.get(this);
    	} else {
            inspect();
            final Set<OwlClass> subClasses = buildSubClasses(classMemoizer);
            final OwlClass owlClass = new OwlClassImpl(uri, label, description, subClasses, objectProperties, dataProperties);
            for (OwlClass subClass : owlClass.getSubClasses()){
            	((OwlClassImpl)subClass).setSuperClass(owlClass);
            }
            classMemoizer.put(this, owlClass);
            return owlClass;
    	}
    }
    
    private Set<OwlClass> buildSubClasses(Map<OwlClassBuilder, OwlClass> classMemoizer){
    	final Set<OwlClass> subClasses = new HashSet<OwlClass>();
    	for (OwlClassBuilder b : subClassBuilders){
    		OwlClass c = b.build(classMemoizer);
    		subClasses.add(c);
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
