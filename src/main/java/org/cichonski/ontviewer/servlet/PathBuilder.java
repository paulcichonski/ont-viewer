package org.cichonski.ontviewer.servlet;

public interface PathBuilder {

    /**
     * Will build the correct relative path to embed in HTML pages. This method
     * will embed any localPaths pushed to the pushLocalPath() method.
     * 
     * @param classLabel
     * @return
     */
    String buildPath(String classLabel);
    
    /**
     * Will build the same logical Path as buildPath() (i.e., will represent the
     * same entity, but it will be a in a form that will match how the path will
     * look to the servlet handling the doGet or doPut. In most cases, this is
     * just a leading '/'.
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
     * Returns the servlet path that the app is mapped to.
     * @return
     */
    String getServletPath();

    void pushLocalPath(String path);

    String popLocalPath();

}
