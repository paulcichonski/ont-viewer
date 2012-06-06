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
    
    public View(File ont) {
        //TODO: implement
        String fileName = ont.getName();
        this.view = fileName + " the view!";
        this.description = fileName + " the descrition!";
        this.cleanName = fileName; // just use file name
        this.path = genPath(fileName);
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
    
    private String genPath(String fileName){
        int index = fileName.lastIndexOf("."); //assuming there is not multiple file extensions (i.e., .owl.bak)
        return fileName.substring(0, index);
    }

}
