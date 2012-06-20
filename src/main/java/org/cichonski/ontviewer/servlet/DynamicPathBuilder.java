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
    
    // path to use for webpages
    private String currentPath;
    
    // logically equivalent to currentPath, but matches how it will look coming on an HTTPServletRequest (i.e., servlet mapping stripped).
    private String pathSansServletPath;
    
    /**
     * 
     * @param basePath - should be the base relative path (i.e., /[application-path]/[server-path]).
     */
    DynamicPathBuilder(String basePath) {
        this.servletPath = basePath;
        reset();
    }
    
    private void reset(){
        this.currentPath = servletPath;
        this.pathSansServletPath = "";
    }
    
    @Override
    public String buildPath(String classLabel){
        return assemblePath(currentPath, classLabel);
    }
    
    @Override
    public String buildIncomingRequestPath(String classLabel) {
        return assemblePath(pathSansServletPath, classLabel);
    }
    
    private String assemblePath(String path, String classLabel){
        final StringBuilder builder = new StringBuilder();
        if (path != null && !path.isEmpty()){
            builder.append(path).append("/");
        }
        builder.append(StringUtils.deleteWhitespace(classLabel));
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
