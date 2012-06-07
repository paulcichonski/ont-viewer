package org.cichonski.ontviewer.servlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility methods for building out Views.
 * @author paulcichonski
 *
 */
public final class ViewBuilder {
// eventually may want to use this to build out static views for caching to disk.
	
	/**
	 * Helper method to build out views for a set of ontologies found in the specified directory.
	 * @param ontologyDirectory
	 * @param contextPath
	 * @return Map<String, View> - Key = view path, Value = actual view.
	 * @throws FileNotFoundException - if the ontology directory is invalid.
	 */
	public static Map<String, View> buildViews(File ontologyDirectory, String contextPath) throws FileNotFoundException{
		final Map<String, View> views = new HashMap<String, View>();
        if (ontologyDirectory != null & ontologyDirectory.isDirectory()){
            for (File ont : ontologyDirectory.listFiles()){
                final View view = new View(ont);
                views.put(view.getPath(), view);
            }
        } else {
        	throw new FileNotFoundException("could not find ontology directory");
        }
        return views;
	}
	
	/**
	 * Returns a simple index of the views contained in the specified map.
	 * @param views
	 * @param contextPath of app
	 * @return
	 */
	public static String buildViewIndex(Map<String, View> views, String contextPath){
        //TODO: replace all view gen code with velocity templates.
        final StringBuilder builder = new StringBuilder();
        builder.append("<html><head></head><title>Ontology Index</title><body>");
        for (Map.Entry<String, View> entry : views.entrySet()){
            final String path = entry.getKey();
            final View view = entry.getValue();
            builder.append("<br/>");
            builder.append(view.getCleanName()).append(" - ");
            builder.append("<a href=\"").append(contextPath).append("/").append(path).append("\">").append(view.getDescription()).append("</a>");
        }
        builder.append("</html>");
        return builder.toString();
	}
}
