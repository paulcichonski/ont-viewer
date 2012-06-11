package org.cichonski.ontviewer.servlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.cichonski.ontviewer.model.OwlClass;
import org.cichonski.ontviewer.parse.OwlSaxHandler;
import org.xml.sax.SAXException;

/**
 * Utility methods for building out Views. Handle all dynamic Velocity genration.
 * @author paulcichonski
 *
 */
public final class ViewBuilder {
// eventually may want to use this to build out static views for caching to disk.
    private static final Logger log = Logger.getLogger(ViewBuilder.class.getName());
    private static final String DEFAULT_OUTPUT_ENCODING = "UTF-8";

    /**
	 * Helper method to build out views for a set of ontologies found in the specified directory.
	 * @param ontologyDirectory
	 * @param contextPath
	 * @return Map<String, View> - Key = view path, Value = actual view.
	 * @throws FileNotFoundException - if the ontology directory is invalid.
	 */
	public static ViewContainer buildViews(File ontologyDirectory, String fullSchemaTemplateLoc, String contextPath) throws FileNotFoundException{
	    initVelocity();
		final Map<String, View> views = new HashMap<String, View>();
		final Map<String, View> rootViews = new HashMap<String, View>();
		final PathBuilder pathBuilder = new PathBuilder(contextPath);
        if (ontologyDirectory != null & ontologyDirectory.isDirectory()){
            for (File ont : ontologyDirectory.listFiles()){
                final OwlSaxHandler handler = parseOntology(ont);
                final VelocityContext context = new VelocityContext();
                context.put("root", handler.getRoot());
                context.put("pathBuilder", pathBuilder);
                final StringWriter w = new StringWriter();
                Velocity.mergeTemplate(fullSchemaTemplateLoc, DEFAULT_OUTPUT_ENCODING, context, w);
                final View rootView = new View(w.toString(), "a test description", genPath(ont.getName()), ont.getName());
                views.put(rootView.getPath(), rootView);
                rootViews.put(rootView.getPath(), rootView);
                views.putAll(generateClassViews(handler.getClassCache(), pathBuilder));
            }
        } else {
        	throw new FileNotFoundException("could not find ontology directory");
        }
        return new ViewContainer(buildViewIndex(rootViews, contextPath), views);
	}
	
	/**
	 * Helper function for generating a path for the ontology itself, classes should use a PathBuilder to generate their servlet paths.
	 * @param fileName
	 * @return
	 */
    private static String genPath(String fileName){
        int index = fileName.lastIndexOf("."); //assuming there is not multiple file extensions (i.e., .owl.bak)
        return "/" + fileName.substring(0, index);
    }
	
	private static Map<String, View> generateClassViews(Map<URI, OwlClass> classes, PathBuilder pathBuilder) {
	   
	    final Map<String, View> views = new HashMap<String, View>();
	    for (OwlClass owlClass : classes.values()){
	        String path = pathBuilder.buildKeyedPath(owlClass.getLabel());
	        final View view = new View(owlClass.getLabel(), owlClass.getDescritpion(), path, owlClass.getLabel());
	        views.put(path, view);
	    }
	    
	    return views;
    }

    private static void initVelocity(){
	    Properties p = new Properties();
	    p.setProperty("resource.loader", "class");
	    p.setProperty("class.resource.loader.description", "Velocity Classpath Resource Loader");
	    p.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
	    Velocity.init(p);
	}
	
	/**
	 * 
	 * @return - fully populated handler
	 */
	private static OwlSaxHandler parseOntology(File ont){
	    OwlSaxHandler handler = null;
	    try {
            handler = new OwlSaxHandler();
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature("http://xml.org/sax/features/namespaces", true);
            factory.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
            SAXParser parser = factory.newSAXParser();
            parser.parse(ont, handler);
        } catch (IOException e){
            log.log(Level.WARNING, "couldn't parse file: " + ont.getName(), e);
        } catch (SAXException e) {
            log.log(Level.WARNING, "couldn't parse file: " + ont.getName(), e);
        } catch (ParserConfigurationException e) {
            log.log(Level.WARNING, "couldn't parse file: " + ont.getName(), e);
        }
        return handler;
	}
	
	/**
	 * Returns a simple index of the views contained in the specified map.
	 * @param views
	 * @param contextPath of app
	 * @return
	 */
	private static String buildViewIndex(Map<String, View> views, String contextPath){
        //TODO: replace all view gen code with velocity templates.
        final StringBuilder builder = new StringBuilder();
        builder.append("<html><head></head><title>Ontology Index</title><body>");
        for (Map.Entry<String, View> entry : views.entrySet()){
            final String path = entry.getKey();
            final View view = entry.getValue();
            builder.append("<br/>");
            builder.append(view.getCleanName()).append(" - ");
            builder.append("<a href=\"").append(contextPath).append(path).append("\">").append(view.getDescription()).append("</a>");
        }
        builder.append("</html>");
        return builder.toString();
	}
	
	
	/**
	 * Contains the views and the index of the root ontology
	 * @author Paul Cichonski
	 *
	 */
	public static class ViewContainer{
	    private final String index;
	    private final Map<String, View> views;
	    
	   
	    
	    public ViewContainer(String index, Map<String, View> views) {
            this.index = index;
            this.views = views;
        }

        public String getIndex() {
            return index;
        }
	    
	    public Map<String, View> getViews() {
            return Collections.unmodifiableMap(views);
        }
	}
	
	
	
	/** path builder for servlet paths */
	public static class PathBuilder{
	    private final String localContextPath;
	    private final StringBuilder builder;
	    
	    private PathBuilder(String localContextPath) {
            this.localContextPath = localContextPath;
            builder = new StringBuilder();
        }
	    
	    /**
	     * will build the correct servlet path this class to embed in a vm template
	     * @param classLabel
	     * @return
	     */
	    public String buildPagePath(String classLabel){
	        if (builder.length() > 0){
	            builder.setLength(0);
	        }
	        builder.append(localContextPath).append("/");
	        builder.append(StringUtils.deleteWhitespace(classLabel));
	        return builder.toString();
	    }
	    
	    /**
	     * Will build the equivalent representation as buildPagePath(), but without the local context, since servlet will not provide that when serving a request.
	     */
        public String buildKeyedPath(String classLabel) {
            if (builder.length() > 0) {
                builder.setLength(0);
            }
            builder.append("/").append(StringUtils.deleteWhitespace(classLabel));
            return builder.toString();
        }
	    
	}
	


}
