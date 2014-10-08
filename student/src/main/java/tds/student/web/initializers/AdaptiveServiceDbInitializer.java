/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.web.initializers;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.web.WebApplicationInitializer;

/**
 * @author temp_rreddy
 * 
 */
public class AdaptiveServiceDbInitializer implements WebApplicationInitializer
{
  @Override
  public void onStartup (ServletContext container) throws ServletException {
   /* DataSource ds = null;
    try {
      Context initCtx = new InitialContext ();
      Context envCtx = (Context) initCtx.lookup ("java:comp/env");
      ds = (DataSource) envCtx.lookup ("jdbc/itembankdb");

      // TODO Shiva get the driver class name here. The problem I have is that I
      // do not want to create a separate parameter and would rather try to
      // extract
      // that information from the Resource specification for the DataSource.
      String jdbcDriverClassName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
      // once the datasource has been obtained. lets register it.
      DataSourceConnectionManager connectionManager = new DataSourceConnectionManager (ds, jdbcDriverClassName);
      try {
        ResourceManager.getInstance ().addToResources ("AdaptiveServiceDb", connectionManager, true);
      } catch (ResourceExistsException exp) {
        // its ok if it exists. we will not be doing anything more here.
      } catch (ResourceInitializationException initializationExp) {
        throw new ServletException (TDSStringUtils.format ("Exception while adding resource: {0}", initializationExp.toString ()), initializationExp);
      }
    } catch (NamingException namingExp) {
      throw new ServletException (TDSStringUtils.format ("Exception while initializing datasource : {0}", namingExp.toString ()), namingExp);
    }*/
  }
}
