/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.web.dummy;

import javax.servlet.ServletContextEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import tds.itemrenderer.apip.APIPCsvLoader;
import tds.student.sql.singletons.ClientManager;
import tds.student.sql.singletons.ClientSingleton;
import TDS.Shared.Configuration.ITDSSettingsSource;
import AIR.Common.Helpers.StopWatch;
import AIR.Common.Helpers._Ref;
import AIR.Common.TDSLogger.ITDSLogger;
import AIR.Common.Utilities.SpringApplicationContext;
import AIR.Common.Web.Session.BaseServletContextListener;

public class TDSApplication extends BaseServletContextListener
{

  @Autowired
  private static ITDSLogger   _tdsLogger;
	
  private static final Logger _logger = LoggerFactory.getLogger (TDSApplication.class);

  // / <summary>
  // / This is fired when the ASP.NET app starts, this will preload all tests.
  // / </summary>
  public void contextInitialized (ServletContextEvent sce) {
    super.contextInitialized (sce);
    // log app start and list out assemblies
    StringBuilder logBuilder = new StringBuilder ("Student application Started: ");
    // String path = sce.getServletContext ().getRealPath (".");
     setup();
    _logger.info (logBuilder.toString ());
   }

  public void contextDestroyed (ServletContextEvent sce) {
    super.contextDestroyed (sce);
    // log app shutdown and the reason why
    _logger.info ("Student application shutdown");
  }

  // / <summary>
  // / Gets the current client object.
  // / </summary>
  // / <remarks>
  // / This could be the default client hard coded in the web.config or a manual
  // one provided in the URL.
  // / </remarks>
  public static ClientSingleton getClient () {
    ITDSSettingsSource tdsSettings = SpringApplicationContext.getBean ("tdsSettings", ITDSSettingsSource.class);
    ClientManager clientManager = SpringApplicationContext.getBean ("clientManager", ClientManager.class);
    return clientManager.getClient (tdsSettings.getClientName ());
  }

  // / <summary>
  // / Gets the current client object.
  // / </summary>
  // / <remarks>
  // / This could be the default client hard coded in the web.config or a manual
  // one provided in the URL.
  // / </remarks>
  public static ClientSingleton getClient (ApplicationContext context) {
    ITDSSettingsSource tdsSettings = context.getBean ("tdsSettings", ITDSSettingsSource.class);
    ClientManager clientManager = context.getBean ("clientManager", ClientManager.class);
    return clientManager.getClient (tdsSettings.getClientName ());
  }

  // / <summary>
  // / Try and get the current client. If it fails then ignore exception.
  // / </summary>
  // TODO Shiva: do we need this API?
  public static ClientSingleton tryGetClient () {
    ClientSingleton client = null;
    try {
      client = getClient ();
    } catch (Exception exp) {
      _logger.error ("Error getting ClientSingleton", exp);
    }
    return client;
  }

  // / <summary>
  // / Overrides the remote certificate status
  // / </summary>
  // / <remarks>
  // /
  // http://blog.jameshiggs.com/2008/05/01/c-how-to-accept-an-invalid-ssl-certificate-programmatically/
  // / </remarks>
  public static boolean ValidateServerCertificate () {
    // TODO Shiva
    return true;
  }

  public static void OnAppDomainShutdown () {

  }

  // TODO Shiva: implement this method.
  // / <summary>
  // / Begin setting up the student site.
  // / </summary>
  private static void setup () {
    loadAPIP ();
    //throw new NotImplementedException ();
  }

  /**
   * Load the APIP.
   */
  private static void loadAPIP () {
    try {
      APIPCsvLoader.loadRules();
    } catch (Exception ex) {
    	_tdsLogger.applicationError("TDSApplication.loadAPIP: " + ex.getMessage (), "loadAPIP", null, ex);
    }
  }

  // / <summary>
  // / Load the manifest.
  // / </summary>
  private static void loadManifest () {

  }

  // / <summary>
  // / Setup the item scoring engine.
  // / </summary>
  private static void setupItemScoring () {

  }

  // / <summary>
  // / This function is called when a client singleton is loaded.
  // / </summary>
  private static void setupTestScoring () {
  }

  // / <summary>
  // / Implementation of the TDS settings delegate for student site.
  // / </summary>
  private static boolean getTDSSettings (String name, _Ref<Object> value) {
    ClientSingleton client = getClient ();
    if (client != null)
      value.set (client.getConfig ().getAppSetting (name));
    return (value.get () != null);
  }

  private static void LogStartup (ClientSingleton client, StopWatch stopWatch) {
    // TODO Shiva
  }

}
