package org.cichonski.ontviewer.servlet;

import java.util.Iterator;
import java.util.Stack;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;

/**
 * Path Builder for statically served pages. This implementation is not threadsafe.
 * @author Paul Cichonski
 *
 */
public final class StaticPathBuilder implements PathBuilder {
    private final String servletPath;
	private final int basePathDepth;
    private final Stack<String> localPaths = new Stack<String>();
    private final String fileExtension;
    
    // path representing current relative fileSystem location to store files.
    private String currentPath;

    

    
    /**
     * Build a pathBuilder with no fileExtension.
     * @param basePath - should be the base relative path (i.e., /[application-path]/[server-path]). Should start with a '/', but should not end with a '/'.
     * @throws IllegalStateException - if fileExtension is populated and does not start with a '.' OR basePath does not start with a '/' OR basePath ends with a '/'.
     */
    public StaticPathBuilder(String basePath) {
        this(basePath, null);
    }
    
    /**
     * Constructor to use for defining any arbitrary file extension.
     * @param basePath
     * @param fileExtension - should be null if no fileExtension id desired.
     * @throws IllegalStateException - if fileExtension is populated and does not start with a '.' OR basePath does not start with a '/' OR basePath ends with a '/'.
     */
    public StaticPathBuilder(String basePath, String fileExtension) {
    	if (fileExtension != null && !fileExtension.isEmpty() && !fileExtension.startsWith(".")){
    		throw new IllegalStateException("fileExtension must start with '.'");
    	}
    	if (basePath != null && !basePath.isEmpty() && !basePath.startsWith("/") && basePath.endsWith("/")){
    		if (basePath.startsWith("/")){
    			throw new IllegalStateException("basePath must start with '/'");
    		} else {
    			throw new IllegalStateException("basePath must not end with '/'");
    		}
    	}
    	this.servletPath = basePath;
        this.basePathDepth = parseBasePath(basePath);
        this.fileExtension = fileExtension;
    }

    
    private int parseBasePath(String basePath){
    	if (basePath == null || basePath.isEmpty()){
    		return 0;
    	}
    	int depth = 0;
    	StringTokenizer t = new StringTokenizer("/");
    	while (t.hasMoreElements()){
    		t.nextElement();
    		depth++;
    	}
    	return depth;
    }
    
    private void reset(){
        this.currentPath = servletPath;
    }

    // build the path that will reach the class starting from a parent directory
    public String buildPathFromParent(String classLabel){
        validateClassLabel(classLabel);
        final StringBuilder builder = new StringBuilder();
        int parentDepth = basePathDepth + localPaths.size() > 0 ? basePathDepth + localPaths.size() - 1 : 0;
        for (int i=0; i<parentDepth; i++){
        	builder.append("../");
        }
        if (currentPath != null && !currentPath.isEmpty()){
        	builder.deleteCharAt(builder.length()-1); //remove last '/' since currentPath will start with one.
        	builder.append(currentPath).append("/");
        }
        builder.append(StringUtils.deleteWhitespace(classLabel));
        if (fileExtension != null && !fileExtension.isEmpty()){
            builder.append(fileExtension);
        }
        return builder.toString();
    }
    
    @Override
    public String buildPath(String classLabel){
        validateClassLabel(classLabel);
        final StringBuilder builder = new StringBuilder();
        for (int i=0; i<basePathDepth+localPaths.size(); i++){
        	builder.append("../");
        }
        if (currentPath != null && !currentPath.isEmpty()){
            builder.deleteCharAt(builder.length()-1); //remove last '/' since currentPath will start with one.
        	builder.append(currentPath).append("/");
        }
        builder.append(StringUtils.deleteWhitespace(classLabel));
        if (fileExtension != null && !fileExtension.isEmpty()){
            builder.append(fileExtension);
        }
        return builder.toString();
    }
    
    /**
     * {@inheritDoc}
     * This will essentially return the path where the view for the classLabel should be stored in the file system.
     */
    @Override
    public String buildIncomingRequestPath(String classLabel) {
    	validateClassLabel(classLabel);
        final StringBuilder builder = new StringBuilder();
        if (currentPath != null && !currentPath.isEmpty()){
            builder.append(currentPath).append("/");
        }
        builder.append(StringUtils.deleteWhitespace(classLabel));
        if (fileExtension != null && !fileExtension.isEmpty()){
            builder.append(fileExtension);
        }
        return builder.toString();
    	
    }
    
    private void validateClassLabel(String classLabel){
        if (classLabel.contains("/") || classLabel.contains("\\")){
            throw new IllegalStateException(classLabel + " contains slashes");
        }
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
            if (servletPath != null && !servletPath.isEmpty()){
            	builder.insert(0, servletPath);
            }
            currentPath = builder.toString();
        } else {
            reset();
        }
    }
    
    
    

    
    
    
}
