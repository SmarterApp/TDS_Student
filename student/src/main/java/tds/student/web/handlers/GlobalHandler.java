/*******************************************************************************
 * Educational Online Test Delivery System Copyright (c) 2014 American
 * Institutes for Research
 * 
 * Distributed under the AIR Open Source License, Version 1.0 See accompanying
 * file AIR-License-1_0.txt or at http://www.smarterapp.org/documents/
 * American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.web.handlers;

import AIR.Common.TDSLogger.ITDSLogger;
import AIR.Common.Web.WebHelper;
import TDS.Shared.Exceptions.ReturnStatusException;
import TDS.Shared.Messages.IMessageService;
import TDS.Shared.Messages.MessageJson;
import TDS.Shared.Messages.MessageSystem;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import tds.blackbox.web.handlers.TDSHandler;
import tds.student.web.controls.dummy.GlobalJavascript;
import tds.student.web.data.JavascriptStack;
import tds.student.web.data.JavascriptStackFrame;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

/**
 * @author mpatel
 * 
 */
@Controller
public class GlobalHandler extends TDSHandler
{
  @Autowired
  private ITDSLogger          _tdsLogger;
  @Autowired
  private IMessageService     _messageService;

  private static final Logger _logger = LoggerFactory.getLogger (GlobalHandler.class);

  /**
   * This function is called when a JS error occurs and the stack needs to be
   * logged.
   * 
   */
  @RequestMapping ("Global.axd/LogException")
  @ResponseBody
  private void LogException (HttpServletRequest request)
  {
    JavascriptStack stackInfo;
    try {

      ObjectMapper mapper = new ObjectMapper ();
      stackInfo = mapper.readValue (request.getInputStream (), JavascriptStack.class);
      StringBuilder details = new StringBuilder ();

      if (stackInfo.getStack () != null && (stackInfo.getName () == null || !stackInfo.getName ().equals ("failed")))
      {
        for (int i = 0; i < stackInfo.getStack ().size (); i++)
        {
          JavascriptStackFrame frame = stackInfo.getStack ().get (i);

          details.append (String.format ("FUNCTION: '%s' %s (%s)", frame.getFunc (), frame.getUrl (), frame.getLine ()));
          details.append (System.lineSeparator ());
          details.append ("CONTEXT:");
          details.append (System.lineSeparator ());

          if (frame.getContext () != null)
          {
            for (int j = 0; j < frame.getContext ().size (); j++)
            {
              details.append (String.format ("%d: %s", j + 1, frame.getContext ().get (j)));
            }
          }
          details.append (System.lineSeparator ());
        }
      }

      StringBuilder message = new StringBuilder ();
      message.append ("JAVASCRIPT EXCEPTION: ");
      if (!StringUtils.isEmpty (stackInfo.getName ())) {
        message.append (String.format ("%s - ", stackInfo.getName ()));
      }
      message.append (stackInfo.getMessage ());

      if ("onerror".equalsIgnoreCase (stackInfo.getMode ()))
        _tdsLogger.javascriptCritical (message.toString (), details.toString (), "logException", request);
      else
        _tdsLogger.javascriptError (message.toString (), details.toString (), "logException", request);

    } catch (IOException e) {
      e.printStackTrace ();
    }
  }

  // / This function is called when you want to log a message from JS.
  @RequestMapping ("Global.axd/LogError")
  @ResponseBody
  private void LogError (HttpServletRequest request)
  {
    // CheckAuthenticated();

    String message = request.getParameter ("message");
    String details = request.getParameter ("details");

    // WriteString(Trace.CorrelationManager.ActivityId.ToString());

    _tdsLogger.javascriptInfo (message, details, "LogError", request);

  }

  @RequestMapping ("Global.axd/getMessages")
  @ResponseBody
  private void getMessages (HttpServletRequest request) throws ReturnStatusException, IOException
  {
    String language = WebHelper.getQueryString ("language");
    String subject = WebHelper.getQueryString ("subject");
    String grade = WebHelper.getQueryString ("grade");
    String contextName = WebHelper.getQueryString ("context");
    List<String> contextList = GlobalJavascript.getContexts (contextName);

    MessageSystem messageSystem = _messageService.load (language, contextList);

    MessageJson messageJson = new MessageJson (messageSystem, language, subject, grade);
    String json = messageJson.create ();

    // SB-366
    writeJsonString (json);

  }

  // private void internalWebTrace (HttpServletRequest request, String source,
  // String eventType, String message, String stackTrace) {
  //
  // TDSIdentity tdsIdentity = TDSIdentity.getCurrentTDSIdentity ();
  // String testeekeyStr = tdsIdentity.get ("T_KEY");
  // long testee = 0;
  // if (testeekeyStr != null)
  // testee = Long.parseLong (testeekeyStr);
  //
  // String test = tdsIdentity.get ("O_TKEY");
  // String oppKeyStr = tdsIdentity.get("O_KEY");
  // UUID oppKey = null;
  // if (oppKeyStr != null)
  // oppKey = UUID.fromString (oppKeyStr);
  // String application = tdsIdentity.getPath ();
  // String clientIP = request.getRemoteAddr ();
  // //String uri = request.getRequestURI ();
  // //String url = request.getRequestURL ().toString ();
  // //String servletpath = request.getServletPath ();
  // String pathInfo = request.getPathInfo (); //in place of URL
  // //String pathTr = request.getPathTranslated ();
  // //String serverName = request.getServerName ();
  //
  // UUID applicationContextId = UUID.randomUUID ();
  //
  // String proc = String.format ("%s:%s:%s", eventType, source, pathInfo);
  // _globalService.recordError (application, message, stackTrace,
  // applicationContextId,
  // clientIP, testee, test, oppKey, proc);
  // }
}
