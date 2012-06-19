package org.cichonski.ontviewer.servlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cichonski.ontviewer.servlet.ViewBuilder.ViewContainer;

/**
 * Track application data.
 * @author Paul Cichonski
 *
 */
final class Application {
    private static final Logger log = Logger.getLogger(Application.class.getName());
    private static final String ONT_DIR_LOC = "ontologies/";
    private static final String SCHEMA_TEMPLATE = "templates/ont-view-full.vm";
    private static final String CLASS_TEMPLATE = "templates/class-view.vm";
    
    private static Application application;
    
    public static void initializeApplication(String contextPath) throws ServletException {
        if (application != null) {
            // if servlets are being pooled then this logic will need to change, don't throw exception
            throw new ServletException("application is already initialized!");
        }
        try {
        	application = new Application(ViewBuilder.buildViews(getFile(ONT_DIR_LOC), SCHEMA_TEMPLATE, CLASS_TEMPLATE, contextPath), contextPath); 
        } catch (FileNotFoundException e){
        	throw new ServletException(e);
        }
    }
    
    public static Application getInstance() {
        assert application != null : "application must be initialized first!";
        return application;
    }
    
    private static File getFile(String fileLoc){
        File file = null;
        try {
            file = new File(Application.class.getClassLoader().getResource(fileLoc).toURI());
        } catch (URISyntaxException e){
            log.log(Level.SEVERE, "could not find ontologies for view generation", e);
        }
        return file;
    }

    private final ViewContainer viewContainer;
    private final String contextPath;
    
    private Application(ViewContainer viewContainer, String contextPath) {
        this.viewContainer = viewContainer;
        this.contextPath = contextPath;
    }
    
    public void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String path = request.getPathInfo();
        if (path == null || path.isEmpty() || path.equals("/")){
            try {
                PrintWriter out = response.getWriter();
                if (viewContainer.getIndex() != null && !viewContainer.getIndex().isEmpty()){
                	out.println(viewContainer.getIndex()); //todo: add caching
                } else {
                	out.write("no index available");
                }
            }catch (IOException e){
                log.log(Level.WARNING, "error writing index page to response");
                throw e; // let higher level servlet code deal with it
            }
        } else {
            // return view for individual page
            try {
                
                PrintWriter out = response.getWriter();
                View view = viewContainer.getViews().get(path);
                if (view == null){
                    throw new NullPointerException("can't find view for path: " + path);
                }
                out.println(view.getView()); //todo: add caching
            }catch (IOException e){
                log.log(Level.WARNING, "error writing view page to response");
                throw e; // let higher level servlet code deal with it
            }
        }
    }



}
