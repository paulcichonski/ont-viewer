package org.cichonski.ontviewer.parse;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.cichonski.ontviewer.model.OwlClass;
import org.cichonski.ontviewer.model.OwlClassBuilder;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Simple handler that pulls out only a subset of the RDF/XML encoded OWL ontology.
 * @author Paul Cichonski
 *
 */
public class OwlSaxHandler extends DefaultHandler {
    private static Logger log = Logger.getLogger(OwlSaxHandler.class.getName());
    private static final String OWL_NS = "http://www.w3.org/2002/07/owl#";
    private static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    private static final String RDFS_NS = "http://www.w3.org/2000/01/rdf-schema#";
    
    // all classes for easy access
    private final Map<URI, OwlClass> classCache = new HashMap<URI, OwlClass>();
    
    // preserves the tree structure of the classes
    private final Set<OwlClass> classTree = new TreeSet<OwlClass>(); 
    
    
    // ***************************
    // stream state 
    // ***************************
    
    private final Map<URI, OwlClassBuilder> builders = new HashMap<URI, OwlClassBuilder>();    

    private String xmlBase;
    
    private OwlClassBuilder currentBuilder = null;
    
    private StringBuilder charBuffer = new StringBuilder();

    private boolean root = true; //assume root until first element is past
    private Stack<Integer> currentClasses = new Stack<Integer>(); //place holder for nested classes
    private boolean owlClassLabel = false;
    private boolean owlClassComment = false;
    
    public OwlSaxHandler() {
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
            }
        } else if (isOwlClassLabel(uri, localName, qName)){
            owlClassLabel = true;
        } else if (isOwlClassComment(uri, localName, qName)){
            owlClassComment = true;
        }
        
        //todo: object properties
        
        //todo: data properties
        
    }
    
    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        // only capture for things we care about
        if (owlClassLabel || owlClassComment){
            charBuffer.append(ch, start, length);
        }
    }
    
    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (isOwlClass(uri, localName, qName)){
            currentClasses.pop();
            if (currentClasses.isEmpty()){
                currentBuilder = null;  
            }
        }
        if (isOwlClassLabel(uri, localName, qName)){
            owlClassLabel = false;
            currentBuilder.setLabel(charBuffer.toString());
        }
        if (isOwlClassComment(uri, localName, qName)){
            owlClassComment = false;
            currentBuilder.setDescription(charBuffer.toString());
        }
    }
    
    @Override
    public void endDocument() throws SAXException {
        /* TODO:
         * 1. set up all subClass relationships
         * 2. build all builders and add to both tree and cache
         */
        
        
        //just for testing....need to redo
        for (OwlClassBuilder builder : builders.values()){
            OwlClass owlClass = builder.build();
            classCache.put(owlClass.getURI(), owlClass);
            
        }
    }
    
//***********************************
// Methods for building out OwlClass and Predicates
//***********************************
    private void startOwlClass(Attributes attributes){
        URI uri = resolveFullUriIdentifier(attributes);
        OwlClassBuilder builder = new OwlClassBuilder();
        builder.setUri(uri);
        currentBuilder = builder;
        builders.put(uri, builder);
    }
    

    

    
    /**
     * Logic modeled off of spec (http://www.w3.org/TR/REC-rdf-syntax/#section-Syntax-ID-xml-base).
     * @param attributes - the attributes that contain either the rdf:ID or rdf:about signifying the identifier of the resource.
     * @return
     */
    //right now this is assuming resource identifiers are in the xml:base..
    private URI resolveFullUriIdentifier(Attributes attributes){
        if (xmlBase == null || xmlBase.isEmpty()){
            throw new RuntimeException("no default namespace");
        }
        String className = attributes.getValue(RDF_NS, "ID");  // rdf:ID gives a relative URI index without the #
        try {
            if (className != null && !className.isEmpty()){ 
                String fullUri = xmlBase + "#" + className;
                return new URI(fullUri);
            } 
            //assume rdf:about, relative URI with prepended #
            className = attributes.getValue(RDF_NS, "about");
            if (className != null && !className.isEmpty()){ 
                String fullUri = xmlBase + className;
                return new URI(fullUri);
            } 
        } catch (URISyntaxException e){
            log.log(Level.WARNING, "class: " + className, e);
        }
        throw new RuntimeException("could not build URI"); // if it didn't work, everything else is dead
    }
    

    
    private boolean isOwlClass(String uri, String localName, String qName){
        return OWL_NS.equals(uri) && "class".equals(localName.toLowerCase());
    }
    

    private boolean isOwlClassLabel(String uri, String localName, String qName){
        return !currentClasses.isEmpty() && RDFS_NS.equals(uri) && "label".equals(localName.toLowerCase()); // assume subClass statements don't have comments
    }
    
    private boolean isOwlClassComment(String uri, String localName, String qName){
        return !currentClasses.isEmpty() && isRdfsComment(uri, localName, qName); // assume subClass statements don't have comments
    }
    
    private boolean isRdfsComment(String uri, String localName, String qName){
        return RDFS_NS.equals(uri) && "comment".equals(localName.toLowerCase());
    }
    
    
    public Map<URI, OwlClass> getClassCache() {
        return Collections.unmodifiableMap(classCache);
    }


}
