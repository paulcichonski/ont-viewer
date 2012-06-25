package org.cichonski.ontviewer.servlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeSingleton;
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
    private static final String SCHEMA_TEMPLATE = "templates/ont-view-full.vm";
    private static final String CLASS_TEMPLATE = "templates/class-view.vm";
    private static final String INDEX_TEMPLATE = "templates/index.vm";

    /**
	 * <p>Helper method to build out views for a set of ontologies found in the
	 * specified directory. This use the pathBuilder to build out the dynamic
	 * http paths for every class view, but it does create the top-level
	 * schema-summary views in the top-level servlet directory (i.e., a relative
	 * path of "/[servlet-path]/[ontology-name]". All class views are created in
	 * the logical directory of [servlet-path]/[ontology-name]/[class-name].</p>
	 * 
	 * <p>Method accepts the following configuration properties (passed in through the props variable):
	 * <ul>
	 * <ol>site-title = the website title.</ol>
	 * </ul>
	 * </p>
	 * 
	 * @param ontologyDirectory
	 *            - directory containing the ontologies
	 * @param pathBuilder
	 *            - builder that will be used to generate paths for all views.
	 * @param props - configuration properties.
	 * @return
	 * @throws FileNotFoundException
	 *             - if any files (ontologies or templates) are not found.
	 */
	public static ViewContainer buildViews(File ontologyDirectory, PathBuilder pathBuilder, Properties props) throws FileNotFoundException{
	    initVelocity();
		final VelocityContext propertiesContext = createPropertiesContext(props);
	    final Map<String, View> views = new HashMap<String, View>();
		final Set<View> rootViews = new HashSet<View>();
        if (ontologyDirectory != null & ontologyDirectory.isDirectory()){
            final Template schemaViewTemplate = RuntimeSingleton.getTemplate(SCHEMA_TEMPLATE, DEFAULT_OUTPUT_ENCODING);
            for (File ont : ontologyDirectory.listFiles()){
                String ontologyName = stripExtensions(ont.getName());
                pathBuilder.pushLocalPath(stripExtensions(ontologyName));
                final OwlSaxHandler handler = parseOntology(ont);
                final View fullSchemaView = generateSchemaView(schemaViewTemplate, ontologyName, handler.getRoot(), pathBuilder, propertiesContext);
                rootViews.add(fullSchemaView);
                views.putAll(generateClassViews(handler.getClassCache(), pathBuilder, propertiesContext));
                pathBuilder.popLocalPath();
            }
        } else {
        	throw new FileNotFoundException("could not find ontology directory");
        }
        String indexHTML = processRootViews(rootViews, views, pathBuilder, propertiesContext);
        return new ViewContainer(indexHTML, views);
	}
	
	private static String processRootViews(Set<View> rootViews, Map<String, View> allViews, 
	        PathBuilder pathBuilder, VelocityContext propContext){
	    for (View rootView : rootViews){
	        allViews.put(pathBuilder.buildIncomingRequestPath(rootView.getPath()), rootView);
	    }
	    return buildViewIndex(rootViews, pathBuilder, propContext);
	}
	
	private static VelocityContext createPropertiesContext(Properties props){
		final VelocityContext context = new VelocityContext();
		context.put("site-title", props.getProperty("site-title"));
		return context;
	}
	

    private static String stripExtensions(String fileName){
        int index = fileName.lastIndexOf("."); 
        while (index != -1){
            fileName = fileName.substring(0, index);
            index = fileName.lastIndexOf("."); 
        }
        return fileName;
    }
    
    private static View generateSchemaView(Template template, String ontologyName, OwlClass root, PathBuilder pathBuilder, VelocityContext propContext){
        final VelocityContext context = new VelocityContext(propContext);
        context.put("root", root);
        context.put("pathBuilder", pathBuilder);
        final StringWriter w = new StringWriter();
        template.merge(context, w);
        String schemaViewPath = ontologyName;
        return new View(w.toString(), "a test description", schemaViewPath, ontologyName);
    }
	
	private static Map<String, View> generateClassViews(Map<URI, OwlClass> classes, PathBuilder pathBuilder, VelocityContext propContext) {
	    final Template template = RuntimeSingleton.getTemplate(CLASS_TEMPLATE, DEFAULT_OUTPUT_ENCODING);
	    final Map<String, View> views = new HashMap<String, View>();
	    for (OwlClass owlClass : classes.values()){
	        final View view = generateClassView(owlClass, template, pathBuilder, propContext);
	        views.put(view.getPath(), view);
	    }
	    return views;
    }
	
	private static View generateClassView(OwlClass owlClass, Template classTemplate, PathBuilder pathBuilder, VelocityContext propContext){
		final VelocityContext context = new VelocityContext(propContext);
		final StringWriter w = new StringWriter();
		String path = pathBuilder.buildIncomingRequestPath(owlClass.getLabel());
        context.put("class", owlClass);
        context.put("pathBuilder", pathBuilder);
        classTemplate.merge(context, w);
        return new View(w.toString(), "a test class description", path, owlClass.getLabel());
	}

    private static void initVelocity(){
	    final Properties p = new Properties();
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
            final SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature("http://xml.org/sax/features/namespaces", true);
            factory.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
            final SAXParser parser = factory.newSAXParser();
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
	 * Build an index page of all rootViews
	 * @param rootViews
	 * @param pathBuilder
	 * @param propContext
	 * @return
	 */
	private static String buildViewIndex(Set<View> rootViews, PathBuilder pathBuilder, VelocityContext propContext){
	    final VelocityContext context = new VelocityContext(propContext);
	    final Template template = RuntimeSingleton.getTemplate(INDEX_TEMPLATE, DEFAULT_OUTPUT_ENCODING);
        context.put("rootViews", rootViews);
        context.put("pathBuilder", pathBuilder);
        final StringWriter w = new StringWriter();
        template.merge(context, w);
        return w.toString();
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
	
	
	


}
