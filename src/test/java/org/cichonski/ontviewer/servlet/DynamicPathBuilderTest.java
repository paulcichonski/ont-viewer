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
    	String fakeResource = "css/resource.css";
    	assertEquals(pathBuilder.buildRootPath(fakeResource),  "../" + fakeResource);
    	pathBuilder.pushLocalPath("local1");
    	assertEquals(pathBuilder.buildRootPath(fakeResource),  "../../" + fakeResource);
    	pathBuilder.pushLocalPath("local2");
    	assertEquals(pathBuilder.buildRootPath(fakeResource),  "../../../" + fakeResource);
    	pathBuilder.pushLocalPath("local3");
    	assertEquals(pathBuilder.buildRootPath(fakeResource),  "../../../../" + fakeResource);
    	//now pop
    	pathBuilder.popLocalPath();
    	assertEquals(pathBuilder.buildRootPath(fakeResource),  "../../../" + fakeResource);
    	// push once more just for good measure
    	pathBuilder.pushLocalPath("local3");
    	assertEquals(pathBuilder.buildRootPath(fakeResource),  "../../../../" + fakeResource);
    	// pop all
    	pathBuilder.popLocalPath();
    	assertEquals(pathBuilder.buildRootPath(fakeResource),  "../../../" + fakeResource);
    	pathBuilder.popLocalPath();
    	assertEquals(pathBuilder.buildRootPath(fakeResource),  "../../" + fakeResource);
    	pathBuilder.popLocalPath();
    	assertEquals(pathBuilder.buildRootPath(fakeResource),  "../" + fakeResource);
    	pathBuilder.popLocalPath(); // shouldn't do anything
    	assertEquals(pathBuilder.buildRootPath(fakeResource),  "../" + fakeResource);
    }
    	
    public void testBuildPath(){
    	
    }
    
    public void testBuildIncomingRequestPath(){
    	
    }
    
    public void testFileExtension(){
    	
    }
}
