package org.cichonski.ontviewer.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author Paul Cichonski
 *
 */
public final class OntologyViewer extends HttpServlet {
    /** */
    private static final long serialVersionUID = 1L;
    // this is ugly, TODO: figure out way to not hardcode this.
    private static final String SERVLET_PATH = "/vocabs";
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        // TODO Auto-generated method stub
        super.init();
        Application.initializeApplication(config.getServletContext().getContextPath() + SERVLET_PATH);
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doRequest(request, response);
    }
    
    private void doRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        Application.getInstance().processRequest(request, response);
    }
}
