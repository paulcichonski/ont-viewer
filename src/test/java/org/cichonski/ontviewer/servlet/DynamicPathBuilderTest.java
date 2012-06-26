package org.cichonski.ontviewer.servlet;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * 
 * @author paulcichonski
 *
 */
public class DynamicPathBuilderTest extends TestCase {
	private static final String SERVLET_PATH = "/ont-viewer/vocabs";
	
    public DynamicPathBuilderTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        // adds all methods beginning with 'test' to the suite.
        return new TestSuite(DynamicPathBuilderTest.class);
    }
    
    public void testBuildRootPath(){
    	final DynamicPathBuilder pathBuilder = new DynamicPathBuilder(SERVLET_PATH);
    }
    
    public void testBuildPath(){
    	
    }
    
    public void testBuildIncomingRequestPath(){
    	
    }
    
    public void testFileExtension(){
    	
    }
}
