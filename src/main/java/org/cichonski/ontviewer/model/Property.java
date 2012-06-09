package org.cichonski.ontviewer.model;

import java.net.URI;
import java.util.Set;

/**
 * High level predicate data
 * @author paulcichonski
 *
 */
public interface Property {

	String getLabel();
	URI getURI();
	Set<URI> getDomains();
	Set<URI> getRanges(); // assuming dataType ranges are still encoded by URIs of XSD types
    
}
