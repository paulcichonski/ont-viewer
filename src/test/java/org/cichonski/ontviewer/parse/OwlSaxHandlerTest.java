package org.cichonski.ontviewer.parse;

import java.io.InputStream;
import java.net.URI;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.cichonski.ontviewer.model.OwlClass;

public class OwlSaxHandlerTest extends TestCase {

    
    public OwlSaxHandlerTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        // adds all methods beginning with 'test' to the suite.
        return new TestSuite(OwlSaxHandlerTest.class);
    }
    
    
    public void testClassCache() throws Exception {
    	try {
        	InputStream ont = Thread.currentThread().getContextClassLoader().getResourceAsStream("ontologies/subset_indicators-vocabulary.owl");
            OwlSaxHandler handler = new OwlSaxHandler();
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature("http://xml.org/sax/features/namespaces", true);
            factory.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
            SAXParser parser = factory.newSAXParser();
            parser.parse(ont, handler);
            
            Map<URI, OwlClass> classCache = handler.getClassCache();
            
            assertEquals(classCache.size(), 6);
            
            
            ont.close();
            
    	} catch (Exception e){
    		e.printStackTrace();
    		throw new Exception(e);
    	}

    }
}
