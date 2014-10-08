/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.tdslogger;

import java.sql.SQLException;
import java.util.Enumeration;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import tds.dll.api.ICommonDLL;
import AIR.Common.DB.AbstractDAO;
import AIR.Common.DB.SQLConnection;
import AIR.Common.TDSLogger.ITDSLogger;
import TDS.Shared.Exceptions.ReturnStatusException;
import TDS.Shared.Security.TDSIdentity;

/**
 * @author efurman
 *
 */
public class TDSLogger extends AbstractDAO implements ITDSLogger
{

  @Autowired
  private ICommonDLL          _commonDll = null;

  private static final Logger _logger    = LoggerFactory.getLogger (TDSLogger.class);
  // used slf4j Marker as described in
  // http://www.slf4j.org/faq.html#fatal
  // to substitute for lack of 'fatal' method in slf4j
  // Supposedly, it supported by logback and ignored by log4j  
  private static final Marker _fatal     = MarkerFactory.getMarker("FATAL");
  
  public void applicationError (String msg,  String methodName, HttpServletRequest request, Exception ex) {
    if (msg == null && ex != null)
      msg = ex.getMessage ();
    
    String details = buildDetails (request, ex);
    
    _logger.error ( String.format("'%s'  %s", methodName, msg));
    
    tdsLoggerInternal ("TDSApplication", "Error", msg, details, methodName, 
        (request == null ? null : request.getRemoteAddr ()));
  }

  public void applicationWarn (String msg, String methodName,HttpServletRequest request, Exception ex) {
    if (msg == null && ex != null)
      msg = ex.getMessage ();
    
    String details = buildDetails (request, ex);   
    
    _logger.warn (String.format("'%s'  %s", methodName, msg));
    
    tdsLoggerInternal ("TDSApplication", "Warning", msg, details, methodName, 
        (request == null ? null : request.getRemoteAddr ()));
  }

  public void applicationInfo (String msg, String methodName, HttpServletRequest request) {
    String details = buildDetails (request, null); 
    
    _logger.info (String.format("'%s'  %s", methodName, msg));
    
    tdsLoggerInternal ("TDSApplication", "Information", msg, details, methodName, 
        (request == null ? null : request.getRemoteAddr ()));
  }

  public void applicationFatal (String msg, String methodName, Exception ex) {
    if (msg == null && ex != null)
      msg = ex.getMessage ();
    String details = buildDetails (null, ex); 
    
    _logger.error (_fatal, String.format("'%s'  %s", methodName, msg));
    
    tdsLoggerInternal ("TDSApplication", "Fatal", msg, details, methodName, null);
  }

  public void sqlWarn (String msg,  String methodName) {
    _logger.warn  (String.format("'%s'  %s", methodName, msg));
    tdsLoggerInternal ("TDSSql", "Warning", msg, null, methodName, null);
  }

  //public void sqlError (String msg,  String methodName) {
  //  tdsLoggerInternal ("TDSSql", "Error", msg, stackTrace, methodName, null);
  //}

  public void configFatal (String msg, String methodName, Exception ex) {
    if (msg == null && ex != null)
      msg = ex.getMessage ();
    
    String details = buildDetails (null, ex);   
    
    _logger.error (_fatal, String.format("'%s'  %s", methodName, msg));
    
    tdsLoggerInternal ("TDSConfig", "Critical", msg, details, methodName, null);
  }
  
  public void configError (String msg, String methodName, Exception ex) {
    if (msg == null && ex != null)
      msg = ex.getMessage ();
    
    String details = buildDetails (null, ex); 
    
    _logger.error (String.format("'%s'  %s", methodName, msg));
    
    tdsLoggerInternal ("TDSConfig", "Fatal", msg, details, methodName, null);
  }
  public void javascriptError (String msg, String details, String methodName, HttpServletRequest request) {
  
    _logger.error ("Javascript : ");
    _logger.error ("Message::"+msg);
    _logger.error ("Details::"+details);
    
    tdsLoggerInternal ("JavaScript", "Error", msg, details, methodName, request.getRemoteAddr ());
  }

  public void javascriptCritical (String msg, String details, String methodName,  HttpServletRequest request) {
    _logger.error ("Javascript : ");
    _logger.error ("Message::"+msg);
    _logger.error ("Details::"+details);
    tdsLoggerInternal ("JavaScript", "Critical", msg, details, methodName, request.getRemoteAddr ());
  }

  public void javascriptInfo (String msg, String details, String methodName, HttpServletRequest request) {
    _logger.error ("Javascript : ");
    _logger.error ("Message::"+msg);
    _logger.error ("Details::"+details);
    
    tdsLoggerInternal ("JavaScript", "Information", msg, details, methodName, request.getRemoteAddr ());
  }
 

  // renderer, accommodations or layout errors
  public void rendererWarn(String msg,  String methodName) {
	_logger.warn(String.format("'%s'  %s", methodName, msg));
	tdsLoggerInternal("TDSRenderer", "Warning", msg, null, methodName, null);
  }

  private String buildDetails (HttpServletRequest request, Exception ex) {
    
    String requestDetails = null;
    if (request != null)
      requestDetails = buildRequestDetails (request);
    
    String exDetails = null;
    if (ex != null)
      exDetails = buildExceptionDetails (ex);
    
    StringBuilder tmpBuilder = new StringBuilder();
    if (exDetails != null && requestDetails != null)
      tmpBuilder.append ("@Exception: ");
    if (exDetails != null)
      tmpBuilder.append (exDetails);
    if (exDetails != null && requestDetails != null)
      tmpBuilder.append ("  @WEB REQUEST: ");  
    if (requestDetails != null)
      tmpBuilder.append (requestDetails);
     
    return tmpBuilder.toString ();
  }
  
  private String buildExceptionDetails (Exception ex) {
    StringBuilder tmpBuilder = new StringBuilder();
    
    tmpBuilder.append ("An exception occured and was caught. Message : ");
    tmpBuilder.append (ex.getMessage ());
    if (ex.getCause () != null)
      tmpBuilder.append (String.format ("  Cause : %s", ex.getCause ().getMessage ()));
    
    tmpBuilder.append ("  Stack Trace : ");  
    StackTraceElement[] stackTrace = ex.getStackTrace ();
    for (StackTraceElement t : stackTrace) {
      tmpBuilder.append (t.toString ());
      tmpBuilder.append ("  ");
    }
    return tmpBuilder.toString ();
  }
  
  private String buildRequestDetails (HttpServletRequest request) {
    StringBuilder details = new StringBuilder();
    details.append (String.format ("  Url : %s  ", request.getRequestURL ().toString ()));    

    details.append (String.format ("  Method: %s  ", request.getMethod ()));
    details.append (String.format ("  Client IP : %s", request.getRemoteAddr ()));
    details.append (getRequestHeaders (request)); 
    details.append (getRequestParamaters (request));

    //details.append (String.format("  Querystring : %s  ", request.getQueryString ()));

    //String b = request.getPathInfo ();
    //String e = request.getParameterMap ().toString ();
    //String f = request.getRequestURI ();
    //String h = request.getPathTranslated ();
    
    return details.toString ();
  }
  
  private String getRequestHeaders (HttpServletRequest request) {
    if (request == null)
      return null;
    StringBuilder themAll = new StringBuilder ("REQUEST_HEADERS= ");
    
    Enumeration<String> headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();
      String headerValue = request.getHeader(headerName);
      themAll.append (String.format ("%s=%s  ", headerName, headerValue));
    }
    return themAll.toString ();
  }
  
  private String getRequestParamaters (HttpServletRequest request) {
    if (request == null)
      return null;
    StringBuilder themAll = new StringBuilder ("REQUEST_PARAMETERS= ");
    
    Enumeration<String> paramsNames = request.getParameterNames ();
    while (paramsNames.hasMoreElements()) {
      String paramName = paramsNames.nextElement ();
      String paramValue = request.getParameter (paramName);
      themAll.append(String.format ("%s=%s&", paramName, paramValue));
    }
    String t = themAll.toString ();
    if (t.endsWith ("&"))
      return t.substring (0, t.length() - 1);
    else
      return t;
  }
  
  private void tdsLoggerInternal (String source, String eventType, String message, String stackTrace, String methodName, String clientIp) {
    long testee = 0;
    String test = null;
    String oppKeyStr = null;
    UUID oppKey = null;
    
    TDSIdentity tdsIdentity = TDSIdentity.getCurrentTDSIdentity ();

    String testeekeyStr = tdsIdentity.get ("T_KEY");
    if (testeekeyStr != null)
      testee = Long.parseLong (testeekeyStr);

    test = tdsIdentity.get ("O_TKEY");

    oppKeyStr = tdsIdentity.get ("O_KEY");
    if (oppKeyStr != null)
      oppKey = UUID.fromString (oppKeyStr);

    String application = tdsIdentity.getPath ();
 
    UUID applicationContextId = UUID.randomUUID ();

    String proc = String.format ("%s:%s:%s", eventType, source, methodName);
    
    try (SQLConnection connection = getSQLConnection ()) {
      
      String clientName = getTdsSettings ().getClientName ();
      _commonDll._RecordSystemError_SP (connection, proc, message, testee,
          test, null, application, clientIp, applicationContextId, stackTrace, oppKey, clientName);
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      //throw new ReturnStatusException (e);
    } catch (ReturnStatusException re) {
      _logger.error (re.getMessage ());
    }
  }

}
