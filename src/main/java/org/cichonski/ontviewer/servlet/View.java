package org.cichonski.ontviewer.servlet;

import java.io.File;

/**
 * Provides a schema.org like view into an ontology.
 * @author Paul Cichonski
 *
 */
final class View {
    private final String view;
    private final String description;
    private final String path;
    private final String cleanName;
    

    
    
    public View(String view, String description, String path, String cleanName) {
        this.view = view;
        this.description = description;
        this.path = path;
        this.cleanName = cleanName;
    }

    /**
     * The actual view to provide as an HTTP response.
     * @return
     */
    public String getView(){
        //todo implement
        return view;
    }
    
    /**
     * The path for the view (i.e., what the server should respond at).
     * @return
     */
    public String getPath(){
        return path;
    }
    
    /**
     * The description of the ontolgoy, derived from the comment at the ont level.
     * @return
     */
    public String getDescription(){
        return description;
    }
    
    /**
     * The clean name for the view to show a user.
     * @return
     */
    public String getCleanName() {
        return cleanName;
    }
    


}
