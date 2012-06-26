package org.cichonski.ontviewer.servlet;

import java.util.Iterator;
import java.util.Stack;

import org.apache.commons.lang.StringUtils;

/**
 * Path Builder for dynamically served pages. This implementation is not threadsafe.
 * @author Paul Cichonski
 *
 */
public final class DynamicPathBuilder implements PathBuilder {
    private final String servletPath;
    private final Stack<String> localPaths = new Stack<String>();
    private final String fileExtension;
    
    // path to use for webpages
    private String currentPath;
    
    // logically equivalent to currentPath, but matches how it will look coming on an HTTPServletRequest (i.e., servlet mapping stripped).
    private String pathSansServletPath;
    
    /**
     * Build a pathBuilder with no fileExtension.
     * @param basePath - should be the base relative path (i.e., /[application-path]/[server-path]).
     */
    public DynamicPathBuilder(String basePath) {
        this(basePath, null);
    }
    
    /**
     * Constructor to use for defining any arbitrary file extension.
     * @param basePath
     * @param fileExtension - should be null if no fileExtension id desired.
     * @throws IllegalStateException - if fileExtension is populated and does not start with a '.'.
     */
    public DynamicPathBuilder(String basePath, String fileExtension) {
    	if (fileExtension != null && !fileExtension.isEmpty() && !fileExtension.startsWith(".")){
    		throw new IllegalStateException("fileExtension must start with '.'");
    	}
        this.servletPath = basePath;
        this.fileExtension = fileExtension;
        reset();
    }
    
    private void reset(){
        this.currentPath = servletPath;
        this.pathSansServletPath = "";
    }
    
    @Override
    public String buildPath(String classLabel){
        validateClassLabel(classLabel);
        return assemblePath(currentPath, classLabel);
    }
    
    @Override
    public String buildIncomingRequestPath(String classLabel) {
        validateClassLabel(classLabel);
        if (pathSansServletPath == null || pathSansServletPath.isEmpty()){
            // only time to append a '/' is if there is no preceding slash, since servlet doRequest will always have a '/' to start the path.
            return assemblePath(pathSansServletPath, "/" + classLabel); 
        }
        return assemblePath(pathSansServletPath, classLabel);
    }
    
    private void validateClassLabel(String classLabel){
        if (classLabel.contains("/") && classLabel.contains("\\")){
            throw new IllegalStateException(classLabel + " contains slashes");
        }
    }
    
    private String assemblePath(String path, String classLabel){
        final StringBuilder builder = new StringBuilder();
        if (path != null && !path.isEmpty()){
            builder.append(path).append("/");
        }
        builder.append(StringUtils.deleteWhitespace(classLabel));
        if (fileExtension != null && !fileExtension.isEmpty()){
            builder.append(fileExtension);
        }
        return builder.toString();
    }
    
    @Override
    public String buildRootPath(String resourcePath) {
        StringBuilder builder = new StringBuilder();
        builder.append("../"); // for the servlet path
        if (!localPaths.isEmpty()){
            for (int i=0; i < localPaths.size(); i++){
                builder.append("../");
            }
        }
        builder.append(resourcePath);
        return builder.toString();
    }
    
    public void pushLocalPath(String path){
        localPaths.push(path);
        buildCurrentPath();
    }
    
    @Override
    public String getBasePath() {
        return servletPath;
    }
    
    @Override
    public String popLocalPath() {
        if (!localPaths.isEmpty()){
            String s = localPaths.pop();
            buildCurrentPath();
            return s;
        }
        return null;
    }

    private void buildCurrentPath(){
        if (!localPaths.isEmpty()){
            final StringBuilder builder = new StringBuilder();
            final Iterator<String> i = localPaths.iterator();
            while (i.hasNext()){
                builder.append("/");
                builder.append(i.next());
            }
            pathSansServletPath = builder.toString();
            builder.insert(0, servletPath);
            currentPath = builder.toString();
        } else {
            reset();
        }
    }
    
    
    
}
