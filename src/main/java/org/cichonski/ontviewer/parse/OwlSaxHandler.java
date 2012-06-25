package org.cichonski.ontviewer.parse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.cichonski.ontviewer.model.OwlClass;
import org.cichonski.ontviewer.model.OwlClassBuilder;
import org.cichonski.ontviewer.model.Property;
import org.cichonski.ontviewer.model.PropertyBuilder;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Simple handler that pulls out only a subset of the RDF/XML encoded OWL ontology.
 * @author Paul Cichonski
 *
 */
public class OwlSaxHandler extends DefaultHandler {
    private static final Logger log = Logger.getLogger(OwlSaxHandler.class.getName());
    private static final String OWL_NS = "http://www.w3.org/2002/07/owl#";
    private static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    private static final String RDFS_NS = "http://www.w3.org/2000/01/rdf-schema#";
    private static final String OWL_THING = "http://www.w3.org/2002/07/owl#Thing";
    
    // all classes for easy access
    private final Map<URI, OwlClass> classCache = new HashMap<URI, OwlClass>();
    
    // safegaurd calls until the end of processing
    private boolean finished = false;
    
    // only safe when finished=true
    private OwlClass owlThing;
    
    // ***************************
    // stream state 
    // ***************************
    
    private final Map<URI, OwlClassBuilder> classBuilders = new HashMap<URI, OwlClassBuilder>();    
    // key=subclass URI, value = superclass URI.
    private final Map<URI, URI> subClassMap = new HashMap<URI, URI>();
    private final Set<Property> objectProperties = new HashSet<Property>();
    private final Set<Property> dataTypeProperties = new HashSet<Property>();
    
    

    private String xmlBase;
    
    private OwlClassBuilder currentClassBuilder = null;
    
    private PropertyBuilder currentPropertyBuilder = null;
    
    private StringBuilder charBuffer = new StringBuilder();

    private boolean root = true; //assume root until first element is past
    private Stack<Integer> currentClasses = new Stack<Integer>(); //place holder for nested classes
    private Stack<Integer> unknonwElements = new Stack<Integer>(); //keep track of unknown elements we are traversing.
    private boolean property = false;
    
    private OwlSaxHandler() {
    }

    /**
     * <p>
     * Static call that will instantiate an instance of the class with the
     * default settings, parse an ontology file, and return the fully populated
     * handler. Clients should use this method to parse an ontology unless they
     * need to override default settings.
     * </p>
     * 
     * @param ont - the ontology to parse
     * @return fully populated instance of this class
     * @throws ParserConfigurationException 
     * @throws IOException 
     * @throws SAXException 
     */
    public static OwlSaxHandler parseOntology(File ont) throws ParserConfigurationException, SAXException, IOException{
        OwlSaxHandler handler = null;
        handler = new OwlSaxHandler();
        getDefaultInstance().parse(ont, handler);
        return handler;
    }
    
    /**
     * <p>
     * Static call that will instantiate an instance of the class with the
     * default settings, parse an ontology file, and return the fully populated
     * handler. Clients should use this method to parse an ontology unless they
     * need to override default settings.
     * </p>
     * 
     * @param ont - the ontology to parse
     * @return fully populated instance of this class
     * @throws ParserConfigurationException 
     * @throws IOException 
     * @throws SAXException 
     */
    public static OwlSaxHandler parseOntology(InputStream ont) throws ParserConfigurationException, SAXException, IOException{
        OwlSaxHandler handler = null;
        handler = new OwlSaxHandler();
        getDefaultInstance().parse(ont, handler);
        return handler;
    }
    
    private static SAXParser getDefaultInstance() throws ParserConfigurationException, SAXException{
        final SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setFeature("http://xml.org/sax/features/namespaces", true);
        factory.setFeature(
            "http://xml.org/sax/features/namespace-prefixes",
            true);
        return factory.newSAXParser();
    }
    
    
    @Override
    public void startDocument() throws SAXException {
    	super.startDocument();
    	finished = false;
    	owlThing = null;
    }

    
    @Override
    public void startElement(
            String uri,
            String localName,
            String qName,
            Attributes attributes) throws SAXException {
        if (charBuffer.length() > 0){
            charBuffer.setLength(0); 
        }
        if (root){
            root = false;
            xmlBase = attributes.getValue("xml:base");
        } else if (isOwlClass(uri, localName, qName)){
            currentClasses.push(1);
            if (currentClasses.size() == 1){
                startOwlClass(attributes);
            } else {
            	/* assume subclasses only nest 1 deep ---> this is only one way to find the subClass, when it looks like:
            	    	<rdfs:subClassOf>
      						<owl:Class rdf:ID="InternalIndicator"/>
    					</rdfs:subClassOf>
    			*/
            	parseSubClass(attributes);
            }
        } else if (isSubClassElement(uri, localName, qName)){
        	/* second way to find a subClass, when it looks like, only enter if there is an attribute. luckily the attribute is the same:
        	 *  <rdfs:subClassOf rdf:resource="#ExternalIndicator"/>
        	 */
        	if (attributes.getLength() > 0){
        		parseSubClass(attributes);
        	}
        } else if (isUnknownElementInOwlClass(uri, localName, qName)){ 
				// in an element somewhere (n-depth) under owl:Class that we don't know about...but it could have labels and comments...need to ignore them
				unknonwElements.push(1);
        } else if (isObjectProperty(uri, localName, qName) || isDataTypeProperty(uri, localName, qName)) {
        	property = true;
        	startProperty(attributes);
        } else if (isPropertyDomain(uri, localName, qName)){
        	parseDomain(attributes);
        } else if (isPropertyRange(uri, localName, qName)){
        	parseRange(attributes);
        }
    }
    
    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
    	charBuffer.append(ch, start, length);
    }
    
    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (isOwlClass(uri, localName, qName)){
            currentClasses.pop();
            if (currentClasses.isEmpty()){
            	// done with the current class
            	finalizeOwlClass();
                currentClassBuilder = null;  
            }
        } else if (isUnknownElementInOwlClass(uri, localName, qName)){
        	unknonwElements.pop();
    	} else if (isTopLevelOwlClassLabel(uri, localName, qName)){
            currentClassBuilder.setLabel(charBuffer.toString());
        } else if (isTopLevelOwlClassComment(uri, localName, qName)){
            currentClassBuilder.setDescription(charBuffer.toString());
        } else if (isObjectProperty(uri, localName, qName)) {
        	property = false;
        	objectProperties.add(currentPropertyBuilder.build());
        	currentPropertyBuilder = null;
        } else if (isDataTypeProperty(uri, localName, qName)){
        	property = false;
        	dataTypeProperties.add(currentPropertyBuilder.build());
        	currentPropertyBuilder = null;
    	} else if (isPropertyLabel(uri, localName, qName)){
        	currentPropertyBuilder.setLabel(charBuffer.toString());
        } else if (isPropertyComment(uri, localName, qName)){
        	currentPropertyBuilder.setDescription(charBuffer.toString());
        } 
    }
    
    
    
    @Override
    public void endDocument() throws SAXException {
    	/* NOTES:
    	 * There are some optimizations that can be done here:
    	 * 1) calling build() on every class will result in many duplacate build() being called
    	 * since each build() recursively calles build() on its subClasses, which are also in the classbuilders map.
    	 * So....if this causes performance issues add some sort of caching/memoization (it probably won't though).
    	 */
    	OwlClassBuilder owlThingBuilder = assembleOwlThingBuilder();
    	
    	for (Property p : objectProperties){
    		populateProperties(p, PropertyType.OBJECT);
    	}
    	for (Property p : dataTypeProperties){
    		populateProperties(p, PropertyType.DATA);
    	}
    	
    	assembleSubClasses(owlThingBuilder);
        
    	final Map<OwlClassBuilder, OwlClass> classMemoizer = new HashMap<OwlClassBuilder, OwlClass>();
        for (OwlClassBuilder builder : classBuilders.values()){
            OwlClass owlClass = builder.build(classMemoizer);
            classCache.put(owlClass.getURI(), owlClass);
        }
        owlThing = owlThingBuilder.build(classMemoizer);
        finished = true;
    }
    
//***********************************
// Methods for building out OwlClass and Predicates
//***********************************
    private void startOwlClass(Attributes attributes){
        URI uri = resolveUriIdentifier(attributes);
        OwlClassBuilder builder = new OwlClassBuilder(uri);
        currentClassBuilder = builder;
        classBuilders.put(uri, builder);
    }
    
    private void finalizeOwlClass(){
    	// if current class did not declare a superclass, assume owlThing.
    	final URI subClassURI = currentClassBuilder.getUri();
    	if (!subClassMap.containsKey(subClassURI)){
    		log.info(subClassURI.toString() + " did not have a superclass declared, assuming OWL Thing");
    		subClassMap.put(subClassURI, URI.create(OWL_THING)); //avoid URI exception
    	}
    	
    }
    
    private void parseSubClass(Attributes attributes){
    	// **** !!!! Important: This assumes that a class is only a subClass of one other class !!! ****
    	// **** !!!! While not valid according to the spec, we shouldn't be seeing any ontologies that break this rule !!!! ****
    	
    	// the class being built is the actual subclass
		final URI subClassURI = currentClassBuilder.getUri();
    	if (subClassMap.containsKey(subClassURI)) {
			throw new RuntimeException(
					subClassURI.toString()
							+ " seems to declare two subclassOf relationships. This is not currently supported");
		}
    	final URI superClassUri = resolveUriIdentifier(attributes);
    	subClassMap.put(subClassURI, superClassUri); 
    }
    
	/**
	 * should only be called after the document has been parsed, or on the
	 * endDocument() method.
	 * @param root - the root of the tree, aka the builder for OWL THING.
	 */
	private void assembleSubClasses(OwlClassBuilder root) {
		for (Map.Entry<URI, URI> entry : subClassMap.entrySet()) {
			OwlClassBuilder subClass = classBuilders.get(entry.getKey());
			OwlClassBuilder superClass = classBuilders.get(entry.getValue());
			if (subClass != null && superClass != null) {
				superClass.addSubClass(subClass);
			} else {
				try { // need to handle the root specially, since it won't be in the map
					if (entry.getValue().equals(new URI(OWL_THING))) {
						root.addSubClass(subClass);
					} else {
						throw new NullPointerException("class not found");
					}
				} catch (URISyntaxException e) {
					log.log(Level.WARNING, "could not build OWL THING URI", e);
				}
			}
		}
	}
	
	private OwlClassBuilder assembleOwlThingBuilder(){
	    OwlClassBuilder owlThingBuilder = null;
	    try {
            owlThingBuilder = new OwlClassBuilder(new URI(OWL_THING));
            owlThingBuilder.setDescription("OWL Thing, the root of the tree");
            owlThingBuilder.setLabel("OWL Thing");
    	} catch (URISyntaxException e){
        	log.info("couldn't buld OWL THING URI");
        }
    	return owlThingBuilder;
	}
    
    private void populateProperties(Property p, PropertyType type){
		Set<URI> domains = p.getDomains();
		for (URI domain : domains){
			OwlClassBuilder classBuilder = classBuilders.get(domain);
			if (classBuilder != null){
				if (type.equals(PropertyType.OBJECT)) {
					classBuilder.addObjectProperty(p);
				} else if (type.equals(PropertyType.DATA)){
					classBuilder.addDataTypeProperty(p);
				}
			}
		}
    }
    
    

    
    private void startProperty(Attributes attributes){
    	URI uri = resolveUriIdentifier(attributes);
    	PropertyBuilder builder = new PropertyBuilder();
    	builder.setUri(uri);
    	currentPropertyBuilder = builder;
    }
    
    private void parseDomain(Attributes attributes){
    	if (attributes.getLength() == 0){
    		log.warning("rdfs:domain statement for " + currentPropertyBuilder.toString() + " has no attributes, not supporting embedded domain declarations");
    	} else {
        	URI uri = resolveUriIdentifier(attributes);
        	currentPropertyBuilder.addDomain(uri);
    	}
    }
    
    private void parseRange(Attributes attributes){
    	if (attributes.getLength() == 0){
    		log.warning("rdfs:range statement for " + currentPropertyBuilder.toString() + " has no attributes, not supporting embedded range declarations");
    	} else {
    		URI uri = resolveUriIdentifier(attributes);
    		currentPropertyBuilder.addRange(uri);
    	}
    }
    

    
    /**
     * Will resolve URI identifier, returning the absolute URI.
     * @param attributes - the attributes that contain either the rdf:ID or rdf:about signifying the identifier of the resource.
     * @return
     */
    private URI resolveUriIdentifier(Attributes attributes){
		URI uri = resolveFullURI(attributes);
		if (uri == null) {
			uri = resolveRelativeURI(attributes);
		}
		if (uri == null){
			// if the identifier isn't created, the app is dead
			throw new RuntimeException("could not build a URI for the resource");
		}
		return uri;
    }

    /**
     * Builds out the URI assuming the reference is fully-qualified (i.e., has scheme component). 
     * @param attributes
     * @return null if the URI is not fully qualified.
     */
	private URI resolveFullURI(Attributes attributes){
		String resourceName = resolveUriResourceName(attributes, false);
		try {
			URI uri = new URI(resourceName);
			if (uri.isAbsolute()){
				return uri;
			}
		} catch (URISyntaxException e) {
			log.log(Level.WARNING, "class: " + resourceName, e);
		}
		return null;
	}

	/**
	 * Build out the URI assuming the reference is relative.
	 * @param attributes
	 * @return null if the URI could not be built/found.
	 */
	 //right now this is assuming resource identifiers are in the xml:base..
	private URI resolveRelativeURI(Attributes attributes){
    	if (xmlBase == null || xmlBase.isEmpty()){
            throw new RuntimeException("no xml:base specified");
        }
		String resourceName = resolveUriResourceName(attributes, true);
		try {
			String fullUri = xmlBase + resourceName;
			return new URI(fullUri);
		} catch (URISyntaxException e) {
			log.log(Level.WARNING, "class: " + resourceName, e);
		}
		return null;
	}
	
	/**
	 * Iterates through the possible ways the URI identifier can be expressed
	 * and returns it. Logic modeled off of spec
	 * (http://www.w3.org/TR/REC-rdf-syntax/#section-Syntax-ID-xml-base).
	 * 
	 * @param attributes
	 * @param prependPound - should be true if you think URI is relative
	 * @return null if no match is found
	 */
	private String resolveUriResourceName(Attributes attributes, boolean prependPound){
		String resourceName = attributes.getValue(RDF_NS, "ID");  // rdf:ID gives a relative URI index without the #
        if (resourceName != null && !resourceName.isEmpty()){ 
            return prependPound ? "#" + resourceName : resourceName;
        } 
        resourceName = attributes.getValue(RDF_NS, "about"); // rdf:about will already have prepended pound if it is relative
        if (resourceName != null && !resourceName.isEmpty()){ 
        	 return resourceName;
        } 
        resourceName = attributes.getValue(RDF_NS, "resource"); // rdf:resource will already have prepended pound if it is relative
        if (resourceName != null && !resourceName.isEmpty()){ 
        	 return resourceName;
        }
        return null;
	}
    

//***********************************
// centralize element inspection logic
//***********************************
    
	/** return true if parser hits owlClass that is NOT INSIDE a property declaration */
    private boolean isOwlClass(String uri, String localName, String qName){
        return currentPropertyBuilder == null && OWL_NS.equals(uri) && "class".equals(localName.toLowerCase());
    }
    
    private boolean isUnknownElementInOwlClass(String uri, String localName, String qName){
    	// in an element somewhere (n-depth) under owl:Class that we don't know about..
    	// obviously not scaleable...find better way.
    	return currentClasses.size() == 1 && !isSubClassElement(uri, localName, qName) && !isTopLevelOwlClassLabel(uri, localName, qName) 
    			&& !isTopLevelOwlClassComment(uri, localName, qName);
    }

    private boolean isTopLevelOwlClassLabel(String uri, String localName, String qName){
        return isParserInTopLevelOwlClass(uri, localName, qName) && isRdfsLabel(uri, localName, qName); // assume subClass statements don't have comments
    }
    
    private boolean isTopLevelOwlClassComment(String uri, String localName, String qName){
        return isParserInTopLevelOwlClass(uri, localName, qName) && isRdfsComment(uri, localName, qName); // assume subClass statements don't have comments
    }
    
    private boolean isSubClassElement(String uri, String localName, String qName){
    	return isParserInTopLevelOwlClass(uri, localName, qName) && RDFS_NS.equals(uri) && "subclassof".equals(localName.toLowerCase());
    }
    
    private boolean isParserInTopLevelOwlClass(String uri, String localName, String qName){
    	return unknonwElements.isEmpty() && !currentClasses.isEmpty();
    }
    
    private boolean isObjectProperty(String uri, String localName, String qName){
    	return OWL_NS.equals(uri) && "objectproperty".equals(localName.toLowerCase());
    }
    
    private boolean isDataTypeProperty(String uri, String localName, String qName){
    	return OWL_NS.equals(uri) && "datatypeproperty".equals(localName.toLowerCase());
    }
    
    private boolean isPropertyComment(String uri, String localName, String qName){
    	return property && isRdfsComment(uri, localName, qName);
    }
    
    private boolean isPropertyLabel(String uri, String localName, String qName){
    	return property && isRdfsLabel(uri, localName, qName);
    }
    
    private boolean isPropertyDomain(String uri, String localName, String qName){
    	return property && RDFS_NS.equals(uri) && "domain".equals(localName.toLowerCase());
    }
    
    private boolean isPropertyRange(String uri, String localName, String qName){
    	return property && RDFS_NS.equals(uri) && "range".equals(localName.toLowerCase());
    }
    
    private boolean isRdfsComment(String uri, String localName, String qName){
        return RDFS_NS.equals(uri) && "comment".equals(localName.toLowerCase());
    }
    
    private boolean isRdfsLabel(String uri, String localName, String qName){
    	return RDFS_NS.equals(uri) && "label".equals(localName.toLowerCase());
    }
    
    private static enum PropertyType {
    	OBJECT, DATA;
    }
    
    
    public Map<URI, OwlClass> getClassCache() {
    	verifyEndState();
        return Collections.unmodifiableMap(classCache);
    }
    
    /**
     * REturns the class representing owl:thing
     * @return
     */
    public OwlClass getRoot() {
    	verifyEndState();
    	return owlThing;
	}
    
    private void verifyEndState(){
    	if (!finished || owlThing == null){
    		throw new IllegalStateException("handler not in good end state, can't provide data");
    	} 

    
    }


}
