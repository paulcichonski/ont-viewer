package org.cichonski.ontviewer.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cichonski.ontviewer.servlet.ViewBuilder.ViewContainer;

/**
 * Manages application data.
 * @author Paul Cichonski
 *
 */
final class Application {
    private static final Logger log = Logger.getLogger(Application.class.getName());
    private static final String ONT_DIR_LOC = "ontologies/";
    private static final String PROPERTY_FILE_LOC ="ont-viewer.properties";

    // this class is a singleton
    private static Application application;
    
    /**
     * Call during Servlet initialization to initialize application state.
     * @param contextPath
     * @throws ServletException
     */
    public static void initializeApplication(String contextPath) throws ServletException {
        if (application != null) {
            // if servlets are being pooled then this logic will need to change, don't throw exception
            throw new ServletException("application is already initialized!");
        }
        try {
            final Properties props = new Properties();
            props.load(new FileInputStream(getFile(PROPERTY_FILE_LOC)));
            String fileExtension = props.getProperty("file-extension");
            final PathBuilder pathBuilder = new DynamicPathBuilder(contextPath, fileExtension);
        	application = new Application(ViewBuilder.buildViews(getFile(ONT_DIR_LOC), pathBuilder, props)); 
        } catch (FileNotFoundException e){
        	throw new ServletException(e);
        } catch (IOException e){
        	throw new ServletException(e);
        }
    }
    
    /**
     * @return - the application instance for serving pages.
     */
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
    
    private Application(ViewContainer viewContainer) {
        this.viewContainer = viewContainer;
    }
    
    /**
     * Process the request and display the appropriate schema view.
     * @param request
     * @param response - will contain either the view or a 404 error.
     * @throws IOException
     */
    public void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String path = request.getPathInfo();
        if (path == null || path.isEmpty() || path.equals("/")){
            serveIndex(response);
        } else {
            servePage(path, response);
        }
    }
    
    /**
     * will serve the index page displaying all schemas managed by this application.
     * @param response
     * @throws IOException
     */
    private void serveIndex(HttpServletResponse response) throws IOException{
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
    }
    
    /**
     * Serve an individual page.
     * @param path - the path of the page to serve.
     * @param response
     * @throws IOException
     */
    private void servePage(String path, HttpServletResponse response) throws IOException{
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
        } catch (NullPointerException e){
            response.sendError(404, path + " not found");
        }
    }

}
