package org.cichonski.ontviewer.servlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.app.Velocity;

/**
 * Utility methods for building out Views. Handle all dynamic Velocity genration.
 * @author paulcichonski
 *
 */
public final class ViewBuilder {
// eventually may want to use this to build out static views for caching to disk.
	
	
	public static void init(){
		Velocity.init();
		// need error checking if this doesn't happen, maybe RuntimeConfiguration.isInitialized() ?
	}
	
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
            	
            	// can call Velocity.evaluate() to dynamically build out a populated template.
            	
                //what i need from the owl:
                // 1) list of all classes and their descriptions
                // 2) classes should form a tree (class...subclass...subclass)
                // 3) properties associated with each class
                // so...really need one map that contains every class (for quick access), and one data structure that shows the tree
            	
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
