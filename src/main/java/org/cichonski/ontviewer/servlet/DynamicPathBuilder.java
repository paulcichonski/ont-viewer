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
    
    private String currentPath;
    
    DynamicPathBuilder(String servletPath) {
        this.servletPath = servletPath;
        this.currentPath = "";
    }
    
    @Override
    public String buildPath(String classLabel){
        final StringBuilder builder = new StringBuilder();
        if (currentPath != null && !currentPath.isEmpty()){
            builder.append(currentPath).append("/");
        }
        builder.append(StringUtils.deleteWhitespace(classLabel));
        return builder.toString();
    }
    
    @Override
    public String buildIncomingRequestPath(String classLabel) {
        final StringBuilder builder = new StringBuilder();
        builder.append("/").append(buildPath(classLabel));
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
    public String getServletPath() {
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
            boolean first = true;
            while (i.hasNext()){
                if (first){
                    first = false;
                } else {
                    builder.append("/");
                }
                builder.append(i.next());
            }
            currentPath = builder.toString();
        } else {
            currentPath = "";
        }
    }
    
    
    
}
