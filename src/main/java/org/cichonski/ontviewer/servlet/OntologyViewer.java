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
public class OntologyViewer extends HttpServlet {
    /** */
    private static final long serialVersionUID = 1L;
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        // TODO Auto-generated method stub
        super.init();
        Application.initializeApplication(config.getServletContext().getContextPath());
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // TODO Auto-generated method stub
        Application.getInstance().processRequest(request, response);
    }
}
