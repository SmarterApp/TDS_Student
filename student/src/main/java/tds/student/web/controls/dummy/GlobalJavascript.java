/*******************************************************************************
 * Educational Online Test Delivery System Copyright (c) 2014 American
 * Institutes for Research
 * 
 * Distributed under the AIR Open Source License, Version 1.0 See accompanying
 * file AIR-License-1_0.txt or at http://www.smarterapp.org/documents/
 * American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.web.controls.dummy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tds.student.sql.abstractions.IConfigRepository;
import tds.student.sql.abstractions.IItemBankRepository;
import tds.student.web.StudentContext;
import tds.student.web.StudentSettings;
import AIR.Common.Web.FacesContextHelper;
import TDS.Shared.Exceptions.ReadOnlyException;
import TDS.Shared.Exceptions.ReturnStatusException;
import TDS.Shared.Messages.IMessageService;

/**
 * This faces component is responsible for fetching resources from the system and providing initial
 * page-wide javascript values via an embedded <script/> tag.
 * The javascript values produced include TDS configuration values, I18N translation values,
 * system information values, etc.
 */
@FacesComponent (value = "GlobalJavascript")
public class GlobalJavascript extends UIComponentBase
{
  private static final Logger _logger = LoggerFactory.getLogger (GlobalJavascript.class);
  static final String CONTEXT_LOGIN_SHELL = "LoginShell";
  static final String CONTEXT_REVIEW_SHELL = "ReviewShell";
  static final String CONTEXT_DIAGNOSTIC_SHELL = "DiagnosticShell";
  static final String CONTEXT_TEST_SHELL = "TestShell";

  private String              _contextName;
  private String              _messages;

  StudentSettings             _studentSettings;
  IConfigRepository           _configRepository;
  IItemBankRepository         _itemBankRepository;
  IMessageService             _iMessageService;

  public GlobalJavascript () {
    setContextName ("Default");
    init ();
  }

  public GlobalJavascript (String contextName, String messages) {
    setContextName (contextName);
    setMessages (messages);
    init ();
  }

  /**
   * Write the super javascript tag to the output stream.
   *
   * @param context       The faces context
   * @throws IOException
   */
  @Override
  public void encodeAll (FacesContext context) throws IOException {
    ResponseWriter output = context.getResponseWriter ();
    output.write ("<script text=\"text/javascript\">");
    try {
      writeJavascript (output);
    } catch (ReturnStatusException e) {
      _logger.error (e.getMessage ());
    } catch (ReadOnlyException e) {
      _logger.error (e.getMessage ());
    }
    output.write ("</script>");
  }

  /**
   * Write various javascript property values based upon the context.
   *
   * @param output  The output writer
   * @throws IOException
   * @throws ReturnStatusException
   * @throws ReadOnlyException
   */
  public void writeJavascript (ResponseWriter output) throws IOException, ReturnStatusException, ReadOnlyException {
    GlobalJavascriptWriter writer = new GlobalJavascriptWriter (output, _studentSettings, _configRepository, _itemBankRepository, _iMessageService);
    writer.writeProperties (); // required
    // writer.WritePreferences();
    // globalJavascript.SetBrowserInfo(); // used on login shell, useful
    // elsewhere
    writer.writeCustomSettings (); // optional
    writer.writeStyles (); // required
    writer.writeVoicePacks (); // required on login and test shell
    writer.writeForbiddenApps (); // required on login and test shell
    // writer.writeTestAccommodations (); // required on test and review shell
    writer.writeBrowserInfo ();
    writer.WriteAppSettings();

    // required on login shell
    if (StringUtils.equals (getContextName (), CONTEXT_LOGIN_SHELL)) {
      writer.writeLoginRequirements ();
      // writer.writeGrades ();
      writer.writeGlobalAccommodations ();
      writer.writeNetworkDiagnosticsTestInfo ();
    }

    // required for test shell
    if (StringUtils.equals (getContextName (), CONTEXT_TEST_SHELL)) {
      writer.writeTestShellButtons ();
      writer.writeManifest ();
    }

    writeMessages (writer); // required

    // removed in new code
    // required for score entry app
    /*
     * if (_studentSettings.isProxyLogin ()) { // writer.writeCLSProperties ();
     * }
     */
  }

  /**
   * Retrieve the set of contexts required by the given root context.
   * (e.g. the test shell requires many sub-contexts to provide message translations)
   *
   * @param contextName The root context
   * @return The set of contexts required by the root context
   */
  public static List<String> getContexts (String contextName) {
    // TODO GeoSettings
    // GeoType geoServer = GeoSettings.GetServerType();
    List<String> contextList = new ArrayList<String> ();

    // GLOBAL CONTEXT:
    contextList.add ("Global");

    // PAGE CONTEXT:
    if (!StringUtils.isEmpty (contextName))
      contextList.add (contextName);

    // LEGACY CONTEXTS:
    if (StringUtils.equals (contextName, CONTEXT_LOGIN_SHELL)) {
      // TODO
      /*
       * if (geoServer == GeoType.Login) { contextList.AddRange(new[] {
       * "Student.Master", "Default.aspx" }); } else following
       */
      String[] pagesArray = { "Student.Master", "AccType", "AccValue", "Default.aspx", "Diagnostics.aspx", "Opportunity.aspx", "Approval.aspx", "SoundCheck.aspx", "TTSCheck.aspx",
          "TestInstructions.aspx", "Approval.aspx" };
      // ServerSide
      contextList.addAll (Arrays.asList (pagesArray));
    } else if (StringUtils.equals (contextName, CONTEXT_TEST_SHELL)) {

      // Removed in new code
      // ServerSide
      /* contextList.add ("TestShell.aspx"); */

      String[] jsArray = { "tds_common.js", "tds_content_events.js", "tds_content.js", "tds_shell.js", "tds_shell_objects.js", "tds_shell_ui.js", "tds_shell_xhr.js", "TestShell.aspx",
          "tds_audio.js",
          "tds_grid.js", "tds_writing.js", "tds_timeout.js", "grid.js", "grid_question.js", "grid_ui.js", "tds_mc.js", "tds_tts.js", "calculator" };

      // ClientSide
      contextList.addAll (Arrays.asList (jsArray));
    } else if (StringUtils.equals (contextName, CONTEXT_REVIEW_SHELL)) {
      String[] morePages = { "Student.Master", "TestReview.aspx", "TestResults.aspx" };
      // ServerSide
      contextList.addAll (Arrays.asList (morePages));

    } else if (StringUtils.equals(contextName, CONTEXT_DIAGNOSTIC_SHELL)) {
      String[] pagesArray = { "Default.aspx", "Diagnostics.aspx", "SoundCheck.aspx", "TTSCheck.aspx" };
      contextList.addAll (Arrays.asList (pagesArray));
    }

    return contextList;
  }

  /**
   * Write message translations for all contexts as a javascript/json property: "TDS.Config.messages"
   *
   * @param writer  The output writer
   * @throws IOException
   * @throws ReturnStatusException
   */
  private void writeMessages (GlobalJavascriptWriter writer) throws IOException, ReturnStatusException {
    List<String> contextList = getContexts (getContextName ());

    // CUSTOM CONTEXTS:
    if (!StringUtils.isEmpty (getMessages ()))
      contextList.addAll (Arrays.asList (getMessages ().split (",")));

    // write out messages json
    if (StudentContext.hasLanguage ()) {
      // if there is a language specified then just write messages for that
      // language
      String language = StudentContext.getLanguage ();
      String subject = StudentContext.getSubject ();
      String grade = StudentContext.getGrade ();

      List<String> languages = new ArrayList<String> ();
      languages.add (language);
      writer.writeMessages (contextList, languages, subject, grade);
    } else {
      // Removed in new code
      /*
       * // if there is no language specified then write out messages for all //
       * possible languages AccLookup globalAccsLookup =
       * _configRepository.getGlobalAccommodations ().createLookup (-1);
       * 
       * // get all the language codes (HACK: add "ENU" if there are no codes)
       * List<String> languages = (List<String>) globalAccsLookup.getCodes
       * ("Language"); if (languages.size () == 0) languages.add ("ENU");
       */
      List<String> languages = new ArrayList<String> ();
      languages.add ("ENU");

      writer.writeMessages (contextList, languages, null, null);
    }

  }

  @Override
  public String getFamily () {
    // TODO Auto-generated method stub
    return "GlobalJavaScriptWriter";
  }

  public String getContextName () {
    return _contextName;
  }

  public void setContextName (String value) {
    this._contextName = value;
  }

  public String getMessages () {
    return _messages;
  }

  public void setMessages (String _messages) {
    this._messages = _messages;
  }

  private void init () {
    _studentSettings = FacesContextHelper.getBean ("studentSettings", StudentSettings.class);
    _configRepository = FacesContextHelper.getBean ("configRepository", IConfigRepository.class);
    _itemBankRepository = FacesContextHelper.getBean ("itemBankRepository", IItemBankRepository.class);
    _iMessageService = FacesContextHelper.getBean ("messageService", IMessageService.class);
  }
}
