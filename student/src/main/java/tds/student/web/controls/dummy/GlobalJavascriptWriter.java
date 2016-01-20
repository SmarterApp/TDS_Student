/*******************************************************************************
 * Educational Online Test Delivery System Copyright (c) 2014 American
 * Institutes for Research
 * 
 * Distributed under the AIR Open Source License, Version 1.0 See accompanying
 * file AIR-License-1_0.txt or at http://www.smarterapp.org/documents/
 * American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.web.controls.dummy;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.ResponseWriter;

import org.apache.commons.lang.StringUtils;

import tds.itemrenderer.configuration.ITSConfig;
import tds.itemrenderer.data.AccLookup;
import tds.student.sql.abstractions.IConfigRepository;
import tds.student.sql.abstractions.IItemBankRepository;
import tds.student.sql.data.AccList;
import tds.student.sql.data.Accommodations;
import tds.student.sql.data.ForbiddenApps;
import tds.student.sql.data.NetworkDiagnostic;
import tds.student.sql.data.TTSVoicePack;
import tds.student.sql.data.TesteeAttributeMetadata;
import tds.student.sql.data.dummy.AccommodationsExtensions;
import tds.student.sql.repository.ConfigRepository;
import tds.student.web.DebugSettings;
import tds.student.web.StudentContext;
import tds.student.web.StudentSettings;
import tds.student.web.dummy.ResourcesSingleton;
import AIR.Common.Json.JsonHelper;
import AIR.Common.Utilities.SpringApplicationContext;
import AIR.Common.Utilities.TDSStringUtils;
import AIR.Common.Web.BrowserParser;
import AIR.Common.Web.UrlHelper;
import AIR.Common.Web.Session.HttpContext;
import TDS.Shared.Exceptions.ReadOnlyException;
import TDS.Shared.Exceptions.ReturnStatusException;
import TDS.Shared.Messages.IMessageService;
import TDS.Shared.Messages.MessageJson;
import TDS.Shared.Messages.MessageSystem;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;

public class GlobalJavascriptWriter
{
  private ResponseWriter      _writer           = null;
  private StudentSettings     _studentSettings  = null;
  private IConfigRepository   _configRepository = null;
  private IItemBankRepository _ibRepository     = null;
  private IMessageService     _iMessageService  = null;

  public GlobalJavascriptWriter (ResponseWriter textWriter, StudentSettings settings, IConfigRepository configRepositoryParam, IItemBankRepository ibRepository, IMessageService imessageService) {
    _writer = textWriter;
    _studentSettings = settings;
    _configRepository = configRepositoryParam;
    _ibRepository = ibRepository;
    _iMessageService = imessageService;
  }

  public void writeProperties () throws IOException, ReturnStatusException {
    int timeout = 20; // global timeout (use to be from Hoai-Anh's AppConfig
                      // class)

    // add java folder used by audio applet (NOTE: same variable name as
    // blackbox)
    _writer.write (String.format ("var javaFolder = '%s';", UrlHelper.resolveUrl (ITSConfig.getJavaFolder ())));

    _writer.write ("\n\r");

    _writer.write ("if (typeof(TDS) == 'undefined') var TDS = {}; if (typeof(TDS.Config) == 'undefined') TDS.Config = {};");

    _writer.write ("\n\r");
    // base url of web site
    _writer.write (String.format ("TDS.baseUrl = \"%s\";", UrlHelper.getBase ()));

    _writer.write ("\n\r");
    // client
    // _writer.WriteLine("TDS.clientName = \"{0}\"; ",
    // StudentSettings.GetClientName());

    // client style name (NOTE: used for content manager frame styles)
    _writer.write (String.format ("TDS.clientStylePath = \"%s\";", _studentSettings.getClientStylePath ()));

    _writer.write ("\n\r");
    // app name
    // _writer.WriteLine("TDS.appName = \"{0}\"; ", AppSettings.GetAppName());

    // are we logging in through CLS or as a standalone proctor
    _writer.write (String.format ("TDS.isProxyLogin = %s;", getBooleanJs (new Boolean (_studentSettings.isProxyLogin ()))));

    _writer.write ("\n\r");
    // Are we in a geographically distributed architecture?
    String testeeCheckin = _studentSettings.getTesteeCheckin ();

    /*
     * if (!StringUtils.isEmpty (testeeCheckin)) { _writer.write (String.format
     * ("TDS.testeeCheckin= \"%s\";", testeeCheckin)); _writer.write ("\n\r"); }
     */

    // are we entering scores/responses
    _writer.write (String.format ("TDS.isDataEntry = %s;", getBooleanJs (_studentSettings.getIsDataEntry ())));

    _writer.write ("\n\r");

    // is read only - not necessarily SIRVE
    _writer.write (String.format ("TDS.isReadOnly = %s;", getBooleanJs (_studentSettings.isReadOnly ())));

    _writer.write ("\n\r");

    // is SIRVE
    _writer.write (String.format ("TDS.isSIRVE = %s;", getBooleanJs (_studentSettings.getIsSIRVE ())));

    _writer.write ("\n\r");

    // set PT
    _writer.write (String.format ("TDS.inPTMode = %s;", getBooleanJs (_studentSettings.getInPTMode ())));

    _writer.write ("\n\r");

    // show item scores
    _writer.write (String.format ("TDS.showItemScores = %s;", getBooleanJs (_studentSettings.getShowItemScores ())));

    _writer.write ("\n\r");

    // set global timeout
    _writer.write (String.format ("TDS.timeout = %s;", timeout));

    _writer.write ("\n\r");

    // set path used for creating cookies
    // TODO Shajib: Uncomment following line when CookieModule class is
    // implemented
    // _writer.write (String.format ("TDS.cookiePath = \"%s\";",
    // CookieModule.CookiePath));

    _writer.write ("\n\r");

    // add cache info
    _writer.write (String.format ("TDS.Cache.id = '%s'; ", (ResourcesSingleton.getCacheId () != null) ? ResourcesSingleton.getCacheId () : StringUtils.EMPTY));

    _writer.write ("\n\r");

    _writer.write (String.format ("TDS.Cache.validate = %s; ", getBooleanJs (ResourcesSingleton.getCacheValidate ())));

    _writer.write ("\n\r");

  }

  public void writeStyles () throws JsonGenerationException, JsonMappingException, IOException {
    List<String> styles = new ArrayList<String> ();

    // YUI
    addClass ("yui-skin-sam", styles);

    // browser info
    BrowserParser browser = new BrowserParser ();
    addClass ("browser_" + browser.getName ().toLowerCase (), styles);
    addClass ("browserVer_" + Double.toString (browser.getVersion ()).replace ('.', '_'), styles);
    addClass ("platform_" + String.valueOf (browser.getOsName ()).toLowerCase (), styles);

    // add class for PT
    if (_studentSettings.getInPTMode ())
      addClass ("practiceTest", styles);

    // get session
    /* TestSession testSession = StudentContext.getSession (); */

    // add class if the test is unproctored
    /*
     * if (testSession != null && testSession.isProctorless ()) { addClass
     * ("unproctored", styles); }
     */

    // add app class
    addClass ("app_" + _studentSettings.getAppName (), styles);

    // add mode class
    addClass ("mode_" + _studentSettings.getModeName (), styles);

    /* TestConfig testConfig = StudentContext.getTestConfig (); */

    // add test config flags
    /*
     * if (testConfig != null) { if (_studentSettings.isReadOnly ()) addClass
     * ("readonly", styles); if (_studentSettings.getShowItemScores ()) addClass
     * ("itemscores", styles); }
     */

    String stylesJson = JsonHelper.serialize (styles);
    _writer.write (String.format ("TDS.Config.styles = %s; ", stylesJson));
    _writer.write ("\n\r");
  }

  public void writeBrowserInfo () throws JsonGenerationException, IOException {
    StringWriter sw = new StringWriter ();
    JsonFactory jsonFactory = new JsonFactory ();
    JsonGenerator jsonWriter = jsonFactory.createGenerator (sw);

    jsonWriter.writeStartObject ();// {

    // browser info
    BrowserParser browser = new BrowserParser ();
    jsonWriter.writeStringField ("userAgent", browser.getUserAgent ());
    jsonWriter.writeStringField ("osLabel", browser.getOSFullName ());
    jsonWriter.writeStringField ("osName", String.valueOf (browser.getOsName ()));
    jsonWriter.writeNumberField ("osVersion", browser.getVersion ());
    jsonWriter.writeStringField ("architecture", browser.getHardwareArchitecture ());
    jsonWriter.writeStringField ("name", browser.getName ());
    jsonWriter.writeNumberField ("version", browser.getVersion ());
    jsonWriter.writeBooleanField ("isSecure", browser.isSecureBrowser ());

    String label;
    if (browser.isSecureBrowser ())
      label = String.format ("Secure v%s", "" + browser.getVersion ());
    else
      label = String.format ("%s v%s", browser.getName (), "" + browser.getVersion ());
    jsonWriter.writeStringField ("label", label);

    jsonWriter.writeEndObject (); // }
    jsonWriter.close ();
    // TODO shiva: do we need to do sw.getBuffer () below.
    _writer.write (String.format ("TDS.BrowserInfo = %s; ", sw.getBuffer ().toString ()));
    _writer.write ("\n\r");
  }

  public void writeCustomSettings () throws IOException {
    StringWriter sw = new StringWriter ();
    JsonFactory jsonFactory = new JsonFactory ();
    JsonGenerator jsonWriter = jsonFactory.createGenerator (sw);

    jsonWriter.writeStartObject ();

    // TODO Shajib:
    // jsonWriter.writeBooleanField ("showExceptions",
    // DebugSettings.showClientExceptions());
    jsonWriter.writeBooleanField ("ignoreForbiddenApps", DebugSettings.ignoreForbiddenApps ());
    jsonWriter.writeBooleanField ("ignoreBrowserChecks", DebugSettings.ignoreBrowserChecks ());
    jsonWriter.writeEndObject ();
    jsonWriter.close ();
    _writer.write (String.format ("TDS.Debug = %s; ", sw.toString ()));
    _writer.write ("\n\r");
  }

  // try here
  // https://tds2.airws.org/test_student_2012/Pages/LoginShell.aspx?c=Hawaii_PT
  // TODO Shiva: this and the one below give infinite loop
  public void writeGlobalAccommodations () throws JsonGenerationException, JsonMappingException, IOException, ReturnStatusException, ReadOnlyException {

    AccList globalAccList = _configRepository.getGlobalAccommodations ();
    Accommodations globalAccommodations = globalAccList.createAccommodations (-1, null, null);

    String globalAccommodationsJson = JsonHelper.serialize (globalAccommodations);
    _writer.write (String.format ("TDS.Config.accs_global = %s; ", globalAccommodationsJson)); //
    _writer.write ("\n\r");
  }

  // TODO Shiva
  public void writeTestAccommodations () throws IOException {

    // get acc lists in cookies
    List<AccLookup> accLists = StudentContext.getAccommodationsList ();
    if (accLists == null)
      return;

    List<Accommodations> segmentsAccommodations = new ArrayList<Accommodations> ();

    for (AccLookup accLookup : accLists) {
      segmentsAccommodations.add (AccommodationsExtensions.createAccommodations (accLookup));
    }

    // serialize accommodations
    String segmentsAccommodationsJson = JsonHelper.serialize (segmentsAccommodations);
    _writer.write (String.format ("TDS.Config.accs_segments = %s;", segmentsAccommodationsJson));
    _writer.write ("\n\r");
  }

  public void writeGrades () throws ReturnStatusException, JsonGenerationException, JsonMappingException, IOException {
    List<String> grades = _ibRepository.getGrades ();

    String json = JsonHelper.serialize (grades);
    _writer.write (String.format ("TDS.Config.grades = %s; ", json)); // tdsGrades
    _writer.write ("\n\r");
  }

  public void writeVoicePacks () throws IOException, ReturnStatusException {
    BrowserParser browser = new BrowserParser ();
    List<TTSVoicePack> voicePacks = new ArrayList<TTSVoicePack> ();
    for (Iterator<TTSVoicePack> it = _configRepository.getVoicePacks ().iterator (); it.hasNext ();) {
      voicePacks.add (it.next ());
    }

    _writer.write ("TDS.Config.voicePacks = ["); // tdsVoicePacks
    _writer.write ("\n\r");

    int voicePackCount = 0;
    for (TTSVoicePack voicePack : voicePacks) {
      // check if this voice pack belongs to the current OS platform
      if (String.valueOf (browser.getOsName ()).toUpperCase () != voicePack.getOs ().toUpperCase ())
        continue;

      if (voicePackCount > 0)
        _writer.write (", ");
      _writer.write ("{");
      _writer.write (String.format ("priority: %s, language: \"%s\", name: \"%s\"", voicePack.getPriority (), voicePack.getLanguageCode (), voicePack.getName ()));
      _writer.write ("\n\r");
      _writer.write ("}");
      _writer.write ("\n\r");
      voicePackCount++;
    }

    _writer.write ("];");
    _writer.write ("\n");
  }

  // add forbidden apps array
  public void writeForbiddenApps () throws IOException, ReturnStatusException {
    BrowserParser browser = new BrowserParser ();
    ForbiddenApps forbiddenApps = _configRepository.getForbiddenApps ();

    List<ForbiddenApps.Process> apps = forbiddenApps.getApps (browser.getOsName ());
    if (apps == null)
      return;

    _writer.write ("TDS.Config.forbiddenApps = ["); // tdsForbiddenApps

    int appCount = 0;
    for (ForbiddenApps.Process app : apps) {
      if (appCount > 0)
        _writer.write (", ");
      _writer.write ("{");
      _writer.write (String.format ("name: \"%s\", desc: \"%s\"", app.getName (), app.getDescription ()));

      // TODO Shajib, no Exemptions property now in app
      /*
       * if (StringUtils.isEmpty (app.Exemptions)) {
       * _writer.Write("name: \"{0}\", desc: \"{1}\"", app.Name,
       * app.Description); } else {
       * _writer.Write("name: \"{0}\", desc: \"{1}\", exemptions: \"{2}\"",
       * app.Name, app.Description, app.Exemptions); }
       */

      _writer.write ("}");
      appCount++;
    }

    _writer.write ("];");
    _writer.write ("\n");
  }

  // TODO shiva:
  /*
   * // add checksums if available
   */
  public void writeManifest () {
    /*
     * // TODO: Need a better way to do this. This list is coming from //
     * content_renderer.js and unfortunately, we dont have any way to know what
     * // is being inserted dynamically. List<String> clientSideManifest = new
     * ArrayList<String> ();
     * 
     * // add JS from content_renderer.js String[] jsList = {
     * "Scripts/Libraries/YUI/yahoo-dom-event/yahoo-dom-event.js",
     * "Scripts/Libraries/YUI/dragdrop/dragdrop-min.js",
     * "Scripts/Libraries/jwplayer/jwplayer.js" }; clientSideManifest.addAll
     * (Arrays.asList (jsList));
     * 
     * // add CSS from content_renderer.js String[] cssList = {
     * "Scripts/Libraries/YUI/menu/assets/skins/sam/menu.css",
     * "Scripts/Libraries/YUI/button/assets/skins/sam/button.css",
     * "Scripts/Libraries/YUI/container/assets/skins/sam/container.css",
     * "Scripts/Libraries/YUI/editor/assets/skins/sam/editor.css",
     * 
     * "Shared/CSS/items.css", "Shared/CSS/elpa.css",
     * "Shared/CSS/accommodations.css", "Shared/CSS/frame.css",
     * 
     * "Scripts/Interaction/CSS/Interaction.css",
     * "Scripts/EquationEditor/CSS/equations.css",
     * 
     * "Projects/{0}/css/items.css", "Projects/{0}/css/elpa.css" };
     * clientSideManifest.addAll (Arrays.asList (cssList));
     * 
     * _writer.write ("TDS.Config.resourceManifest = [");
     * 
     * int fileCount = 0; for (String file : clientSideManifest) { String
     * manifestKey = String.format (file, _studentSettings.getClientStylePath
     * ()); // This // is // incase // we // have // a // client // specific //
     * CSS // to // deal // with
     * 
     * if (ManifestSingleton.GetFileHash (manifestKey) != null) { if (fileCount
     * > 0) _writer.write (", "); _writer.write ("{"); _writer.write
     * ("name: \"{0}\", chksum: \"{1}\"", manifestKey,
     * ManifestSingleton.GetFileHash (manifestKey)); _writer.write ("}");
     * fileCount++; } }
     * 
     * _writer.write ("];"); _writer.write ("\n");
     */
  }

  // Removed in new code
  /*
   * public void writeCLSProperties () throws IOException { _writer.write
   * ("if (typeof(TDS.CLS) == 'undefined') TDS.CLS = {}; ");
   * 
   * // build json _writer.write (String.format ("TDS.CLS.isCLSLogin = %s; ",
   * getBooleanJs (false))); _writer.write ("\n\r"); _writer.write
   * (String.format ("TDS.CLS.loginPage = '%s'; ",
   * FormsAuthentication.getLoginUrl ())); _writer.write ("\n\r"); _writer.write
   * (String.format ("TDS.CLS.logoutPage = '%s/Pages/Proxy/logout.aspx'; ",
   * UrlHelper.getBase ())); _writer.write ("\n\r"); _writer.write
   * (String.format
   * ("TDS.CLS.confirmExitPage = '%s/Pages/Proxy/ConfirmExit.aspx'; ",
   * UrlHelper.getBase ())); _writer.write ("\n\r"); _writer.write
   * (String.format ("TDS.CLS.defaultPage = '%s'; ",
   * FormsAuthentication.getDefaultUrl ())); _writer.write ("\n\r");
   * _writer.write (String.format ("TDS.CLS.isScoreEntry = %s; ", getBooleanJs
   * (_studentSettings.isProxyLogin ()))); _writer.write ("\n\r"); // we use
   * local domains to detect when a user is navigating away from the // proxy
   * site. String defaultDomains = "localhost|airws.org|tds.airast.org";
   * 
   * // TODO Shajib: Uncomment following line when AppSettings.get() method is
   * // implemented // _writer.write (String.format
   * ("TDS.CLS.localDomains = '%s'; ", // AppSettings.get ("CLS.localDomains",
   * defaultDomains)));
   * 
   * // get proctor info Proctor proctorUser = ProxyContext.GetProctor ();
   * 
   * _writer.write (String.format ("TDS.CLS.isProctorLoggedIn = %s; ",
   * getBooleanJs (proctorUser.isAuth ()))); _writer.write ("\n\r"); }
   */

  public void writeLoginRequirements () throws ReturnStatusException, JsonGenerationException, JsonMappingException, IOException {
    List<TesteeAttributeMetadata> loginRequirements = new ArrayList<TesteeAttributeMetadata> ();

    for (Iterator<TesteeAttributeMetadata> it = _configRepository.getLoginRequirements ().iterator (); it.hasNext ();) {
      loginRequirements.add (it.next ());
    }

    String loginRequirementsJson = JsonHelper.serialize (loginRequirements);

    _writer.write ("TDS.Config.loginRequirements = ");
    _writer.write (loginRequirementsJson);
    _writer.write (";");
    _writer.write ("\n");
  }

  public void writeMessages (List<String> contextList, List<String> languages, String subject, String grade) throws IOException, ReturnStatusException {
    // load all the languages
    MessageSystem messageSystem = null;

    for (String language : languages) {
      messageSystem = _iMessageService.load (language, contextList);
    }

    if (messageSystem == null)
      return;

    messageSystem.buildIndex ();

    // create json
    MessageJson messageJson;

    if (languages.size () == 1) {
      messageJson = new MessageJson (messageSystem, languages.get (0), subject, grade);
    } else {
      messageJson = new MessageJson (messageSystem);
    }

    _writer.write ("TDS.Config.messages = ");
    _writer.write (messageJson.create ());
    _writer.write (";");
    _writer.write ("\n");
  }

  public void writeNetworkDiagnosticsTestInfo () throws IOException, ReturnStatusException {
    List<NetworkDiagnostic> networkDiagnosticsInfo = new ArrayList<NetworkDiagnostic> ();

    for (Iterator<NetworkDiagnostic> it = _configRepository.getNetworkDiagnostics ().iterator (); it.hasNext ();) {
      networkDiagnosticsInfo.add (it.next ());
    }

    // create a json represenation of the network diagnostics info
    List<String> jsonArray = new ArrayList<String> ();
    String diagFormat = "{subject:\"%s\", datarate:\"%d\", itemsize:\"%d\", responsetime:\"%d\"}";
    for (NetworkDiagnostic networkDiagnostic : networkDiagnosticsInfo) {
      jsonArray.add (String.format (diagFormat, networkDiagnostic.getTestLabel (), networkDiagnostic.getMinDataRateRequired (), networkDiagnostic.getAverageItemSize (),
          networkDiagnostic.getResponseTime ()));
    }

    _writer.write ("TDS.Config.NetworkDiagnosticsTestInfo = [");
    _writer.write (StringUtils.join (jsonArray.toArray (), ","));
    _writer.write ("];");
    _writer.write ("\n");
  }

  public void writeTestShellButtons () throws IOException
  {
    String toolbarsFile = HttpContext.getCurrentContext ().getServer ().mapPath ("~/Templates/Shells/toolbars.json");
    BufferedReader br = new BufferedReader (new FileReader (toolbarsFile));

    String toolbarsJson = null;
    try {
      StringBuilder sb = new StringBuilder ();
      String line = null;
      while ((line = br.readLine ()) != null)
      {
        sb.append (line);
        sb.append (System.lineSeparator ());
      }
      br.close ();
      toolbarsJson = sb.toString ();
    } catch (Exception e) {

    }
    _writer.write (TDSStringUtils.format ("TDS.Config.testShellButtons = {0};", toolbarsJson));
    _writer.write ("\n");
  }

  private <T> String getBooleanJs (T value) {
    if (value == null)
      return "false";
    return value.toString ().toLowerCase ();
  }

  private void addClass (String name, List<String> styles) {
    if (StringUtils.isEmpty (name))
      return;

    name = name.replace (" ", "_").replace (",", "");

    if (!styles.contains (name)) {
      styles.add (name);
    }
  }

  public String getScript ()
  {
    return _writer.toString ();
  }

  // / <summary>
  // / Write out client side app settings.
  // / </summary>
  public void WriteAppSettings () throws IOException, ReturnStatusException
  {
    // get all the app settings
    IConfigRepository configRepo = SpringApplicationContext.getBean (ConfigRepository.class);
    Map<String, Object> appSettings = configRepo.getClientAppSettings ();

    // write out json
    String serializedSettings = JsonHelper.serialize (appSettings);
    _writer.write ("TDS.Config.appSettings = " + serializedSettings);
    _writer.write ("\n\r");

  }

}
