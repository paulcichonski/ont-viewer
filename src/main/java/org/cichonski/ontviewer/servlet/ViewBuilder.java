package org.cichonski.ontviewer.servlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
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
	public static Map<String, View> buildViews(File ontologyDirectory, String fullSchemaTemplateLoc, String contextPath) throws FileNotFoundException{
	    initVelocity();
		final Map<String, View> views = new HashMap<String, View>();
        if (ontologyDirectory != null & ontologyDirectory.isDirectory()){
            for (File ont : ontologyDirectory.listFiles()){
                try {
                    OwlSaxHandler handler = new OwlSaxHandler();
                    SAXParserFactory factory = SAXParserFactory.newInstance();
                    factory.setFeature("http://xml.org/sax/features/namespaces", true);
                    factory.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
                    SAXParser parser = factory.newSAXParser();
                    parser.parse(ont, handler);
                    
                    
                    // test view ************ replace with real code
//                    StringBuilder builder = new StringBuilder();
//                    builder.append("<html><head></head><title>").append(ont.getName()).append("</title><body>");
//                    for (OwlClass owlClass : handler.getClassCache().values()){
//                        builder.append("<br/>");
//                        builder.append("class name: ").append(owlClass.getLabel()).append("<br/>");
//                        builder.append("class uri: ").append(owlClass.getURI()).append("<br/>");
//                        builder.append("class description: ").append(owlClass.getDescritpion()).append("<br/>");
//                    }
//                    final View view = new View(builder.toString(), "a test description", ont.getName(), ont.getName());
                 // end test view ************ replace with real code
                	
                    
                    
                 // can call Velocity.evaluate() to dynamically build out a populated template.
                    VelocityContext context = new VelocityContext();
                    context.put("root", handler.getRoot());
                    StringWriter w = new StringWriter();
                    
                    
                    Velocity.mergeTemplate(fullSchemaTemplateLoc, DEFAULT_OUTPUT_ENCODING, context, w);
                    final View view = new View(w.toString(), "a test description", ont.getName(), ont.getName());
                    views.put(view.getPath(), view);
                } catch (IOException e){
                    log.log(Level.WARNING, "couldn't parse file: " + ont.getName(), e);
                } catch (SAXException e) {
                    log.log(Level.WARNING, "couldn't parse file: " + ont.getName(), e);
                } catch (ParserConfigurationException e) {
                    log.log(Level.WARNING, "couldn't parse file: " + ont.getName(), e);
                }
            }
        } else {
        	throw new FileNotFoundException("could not find ontology directory");
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
            builder.append("<a href=\"").append(contextPath).append(path).append("\">").append(view.getDescription()).append("</a>");
        }
        builder.append("</html>");
        return builder.toString();
	}
	


}
