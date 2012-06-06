package org.cichonski.ontviewer.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Track application data.
 * @author Paul Cichonski
 *
 */
final class Application {
    private static final Logger log = Logger.getLogger(Application.class.getName());
    private static final String ONT_DIR_LOC = "ontologies/";
    
    private static Application application;
    
    public static void initializeApplication() throws ServletException {
        if (application != null) {
            // if servlets are being pooled then this logic will need to change, don't throw exception
            throw new ServletException("application is already initialized!");
        }
        final Map<String, View> views = new HashMap<String, View>();
        final File ontDir = getOntDir();
        if (ontDir != null & ontDir.isDirectory()){
            for (File ont : ontDir.listFiles()){
                final View view = new View(ont);
                views.put(view.getPath(), view);
            }
        }
        application = new Application(views);
    }
    
    public static Application getInstance() {
        assert application != null : "application must be initialized first!";
        return application;
    }
    
    private static File getOntDir(){
        File ontologyDir = null;
        try {
            ontologyDir = new File(Application.class.getClassLoader().getResource(ONT_DIR_LOC).toURI());
        } catch (URISyntaxException e){
            log.log(Level.SEVERE, "could not find ontologies for view generation", e);
        }
        return ontologyDir;
    }

    // the map of all the views to display, keyed by the path
    private final Map<String, View> views;
    
    private Application(Map<String, View> views) {
        this.views = views;
    }
    
    public void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String path = request.getPathInfo();
        if (path == null || path.isEmpty() || path.equals("/")){
            try {
                PrintWriter out = response.getWriter();
                out.println(buildIndex()); //todo: add caching
            }catch (IOException e){
                log.log(Level.WARNING, "error writing index page to response");
                throw e; // let higher level servlet code deal with it
            }
        } else {
            // return view for individual page
            try {
                
                
                // this will not generate the correct path. TODO: follow this advice: http://stackoverflow.com/a/523103
                
                
                
                PrintWriter out = response.getWriter();
                out.println(views.get(path).getView()); //todo: add caching
            }catch (IOException e){
                log.log(Level.WARNING, "error writing view page to response");
                throw e; // let higher level servlet code deal with it
            }
        }
    }
    
    private String buildIndex(){
        //TODO: replace all view gen code with velocity templates.
        final StringBuilder builder = new StringBuilder();
        builder.append("<html><head></head><title>Ontology Index</title><body>");
        for (Map.Entry<String, View> entry : views.entrySet()){
            final String path = entry.getKey();
            final View view = entry.getValue();
            builder.append("<br/>");
            builder.append(view.getCleanName()).append(" - ");
            builder.append("<a href=\"").append(path).append("\">").append(view.getDescription()).append("</a>");
        }
        builder.append("</html>");
        return builder.toString();
    }


}
