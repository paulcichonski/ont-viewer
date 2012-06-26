package org.cichonski.ontviewer.servlet;

/**
 * Clients should not send any '/' or '\' characters to the buildPath() or buildIncomingRequestPath method.
 *
 */
public interface PathBuilder {

	/**
	 * Will build a path for the classLabel assuming it needs to be used by a
	 * page in a parent directory. Some implementations may just delegate to
	 * buildPath() if they are building absolute paths for everything.
	 * 
	 * TODO: if this is actually a common feature, then this will probably have to be recursive... i.e., build from n-higher or n-lower paths.
	 * 
	 * @param classLabel
	 * @return
	 */
	 String buildPathFromParent(String classLabel);
	
    /**
     * Will build the correct relative path to embed in HTML pages. This method
     * will include any localPaths pushed to the pushLocalPath() method.
     * 
     * @param classLabel
     * @return
     */
    String buildPath(String classLabel);
    
    /**
     * <p>Will build the same logical Path as buildPath() (i.e., will represent the
     * same entity, but it will be a in a form that will match how the path will
     * look to the servlet handling the doGet or doPut. In most cases, this is
     * involves stripping the application context and servlet path.</p>
     * 
     * <p>Clients should use this for generating the keys for views</p>
     * 
     * @param classLabel
     * @return
     */
    String buildIncomingRequestPath(String classLabel);
    
    /**
     * Will append the resourcePath to the correct '../../' structure to ensure
     * it is resolved relative to the root of the web container. This uses the following logic:
     * 
     * <ol>
     * <li>Assumes a servlet mapping (i.e., one directory level)</li>
     * <li>appends a '../' for each localPath pushed to the pushLocalPath() method.</li>
     * </ol>
     * 
     * @param resourceName - should NOT start with a '/'.
     * @return
     */
    String buildRootPath(String resourcePath);
    
    /**
     * @return - the application context + servlet path (i.e., /[application-context]/[servlet-path]).
     */
    String getBasePath();

    void pushLocalPath(String path);

    String popLocalPath();

}
