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
    private static final String URI_MAPPING = "/vocabs";
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        // TODO Auto-generated method stub
        super.init();
        Application.initializeApplication(config.getServletContext().getContextPath() + URI_MAPPING);
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
