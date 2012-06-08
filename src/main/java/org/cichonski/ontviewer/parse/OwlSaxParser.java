package org.cichonski.ontviewer.parse;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.cichonski.ontviewer.model.OwlClass;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Simple handler that pulls out only a subset of the RDF/XML encoded OWL ontology.
 * @author Paul Cichonski
 *
 */
public class OwlSaxParser extends DefaultHandler {

    // all classes for easy access
    private final Map<URI, OwlClass> classCache = new HashMap<URI, OwlClass>();
    
    // preserves the tree structure of the classes
    private final Set<OwlClass> classTree = new TreeSet<OwlClass>(); 
    
    

}
