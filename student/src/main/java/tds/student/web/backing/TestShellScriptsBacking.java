/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.web.backing;

import java.io.IOException;
import java.io.StringWriter;

import javax.annotation.PostConstruct;
import javax.servlet.ServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;

import tds.itemrenderer.data.AccProperties;
import tds.student.performance.services.ItemBankService;
import tds.student.services.OpportunityService;
import tds.student.services.abstractions.IOpportunityService;
import tds.student.services.data.TestOpportunity;
import tds.student.sql.abstractions.IItemBankRepository;
import tds.student.sql.data.OpportunitySegment;
import tds.student.sql.data.OpportunitySegment.OpportunitySegments;
import tds.student.sql.data.TestConfig;
import tds.student.sql.data.TestProperties;
import tds.student.sql.data.TestSegment;
import tds.student.sql.data.TestSession;
import tds.student.sql.repositorysp.ItemBankRepository;
import tds.student.web.StudentContext;
import tds.student.web.StudentContextException;
import tds.student.web.StudentSettings;
import tds.student.web.configuration.TestShellSettings;
import AIR.Common.Web.BrowserParser;
import AIR.Common.Web.UrlHelper;
import AIR.Common.Web.taglib.ClientScript;
import TDS.Shared.Exceptions.ReturnStatusException;
import TDS.Shared.Web.BasePage;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;

public class TestShellScriptsBacking extends BasePage
{
  private ServletRequest      _request;
  private ItemBankService itemBankService;
  private IOpportunityService _iOpportunityService;
  private StudentSettings     _studentSettings;
  private TestProperties      _testProps;
  public TestOpportunity      _testOpportunity;

  @Override
  public ClientScript getClientScript ()
  {
    return super.getClientScript ();
  }

  @PostConstruct
  public void onInit ()
  {
    // TODO Shajib: Get beans instantiated by @ManagedBean property
    _request = getRequest ();
    _studentSettings = getBean ("studentSettings", StudentSettings.class);
    itemBankService = getBean ("itemBankService", ItemBankService.class);
    _iOpportunityService = getBean ("opportunityService", OpportunityService.class);

    // get test opp
    _testOpportunity = StudentContext.getTestOpportunity ();

    if (_testOpportunity == null)
      try {
        StudentContext.throwMissingException ();
      } catch (StudentContextException e) {
        // TODO Auto-generated catch block
        e.printStackTrace ();
      }

    // get test props
    String testKey = StudentContext.getTestKey ();
    try {
      // FW Performance - changed from ItemBankRepository to ItemBankService to utilize the new methods with caching
      _testProps = itemBankService.getTestProperties (testKey);
    } catch (ReturnStatusException e) {
      // TODO Auto-generated catch block
      e.printStackTrace ();
    }

    // add inline javascript variables
    try {
      addTestSegments ();
      addTestConfig ();
      addComments ();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace ();
    } catch (ReturnStatusException e) {
      // TODO Auto-generated catch block
      e.printStackTrace ();
    }

  }

  private void addTestSegments () throws IOException, ReturnStatusException
  {
    TestSession testSession = StudentContext.getSession ();
    // _studentSettings = FacesContextHelper.getBean ("studentSettings",
    // StudentSettings.class);
    OpportunitySegments oppSegments = null;

    // load opp segments only if there are any test segments
    if (_testProps.getSegments ().size () > 0)
    {
      oppSegments = this._iOpportunityService.getSegments (this._testOpportunity.getOppInstance (), !this._studentSettings.isReadOnly ());
    }

    StringWriter sw = new StringWriter ();
    JsonFactory jsonFactory = new JsonFactory ();
    JsonGenerator writer = jsonFactory.createGenerator (sw);

    writer.writeStartArray (); // [

    for (final TestSegment testSegment : _testProps.getSegments ())
    {
      OpportunitySegment oppSegment = null;

      // find opportunity segment
      if (oppSegments != null)
      {
        oppSegment = (OpportunitySegment) CollectionUtils.find (oppSegments, new Predicate ()
        {
          @Override
          public boolean evaluate (Object arg0) {
            if (StringUtils.equals (((OpportunitySegment) arg0).getId (), testSegment.getId ()))
              return true;
            return false;
          }
        });
      }

      // figure out segment permeability
      int isPermeable = testSegment.getIsPermeable ();
      int updatePermeable = isPermeable;

      // these are local override rules (reviewed with Larry)
      if (oppSegment != null)
      {
        /*
         * if -1, use the defined value for the segment as returned by
         * IB_GetSegments if not -1, then the local value defines the temporary
         * segment permeability
         */
        if (oppSegment.getIsPermeable () != -1)
        {
          isPermeable = oppSegment.getIsPermeable ();

          /*
           * The default permeability is restored when the student leaves the
           * segment while testing. Assuming the segment is impermeable, this
           * allows the student one entry into the segment during the sitting.
           * When the student leaves the segment, is membrane is enforced by the
           * student app. The database will restore the default value of the
           * segment membrane when the test is paused.
           */
          if (oppSegment.getRestorePermOn () != "segment")
          {
            updatePermeable = oppSegment.getIsPermeable ();
          }
        }

        // NOTE: When student enters segment, set isPermeable = updatePermeable
      }

      // if read only mode is enabled then we should let user have access
      if (_studentSettings.isReadOnly ())
      {
        isPermeable = 1;
        updatePermeable = 1;
      }

      // figure out segment approval
      int entryApproval = testSegment.getEntryApproval ();
      int exitApproval = testSegment.getExitApproval ();

      // NOTE: If proctorless test then don't require entry/exit approval
      // (nobody to approve it)
      if (testSession.isProctorless () || _studentSettings.isReadOnly ())
      {
        entryApproval = 0;
        exitApproval = 0;
      }
      // BUG #22642: Entry and Exit approvals are not needed from Test level
      // review screen when approval = 2
      else if (getViewPageNumber () > 0)
      {
        if (testSegment.getEntryApproval () == 2)
          entryApproval = 0;
        if (testSegment.getExitApproval () == 2)
          exitApproval = 0;
      }

      // write segment json
      writer.writeStartObject ();
      writer.writeStringField ("type", "object"); // {
      writer.writeStringField ("id", testSegment.getId ());
      writer.writeNumberField ("position", testSegment.getPosition ());
      writer.writeStringField ("label", testSegment.getLabel ());
      writer.writeBooleanField ("itemReview", testSegment.isItemReview ());
      writer.writeNumberField ("isPermeable", isPermeable);
      writer.writeNumberField ("updatePermeable", updatePermeable);
      writer.writeNumberField ("entryApproval", entryApproval);
      writer.writeNumberField ("exitApproval", exitApproval);

      // Looks like we don't use this variable in javascript (removed for 2012)
      // Test adaptiveSegment =
      // TestOpportunity.AdaptiveTest.GetSegmentTest(testSegment.ID);
      // writer.WriteObject("length", (adaptiveSegment != null) ?
      // adaptiveSegment.TotalMinLength : 0);

      writer.writeEndObject (); // }
    }

    writer.writeEndArray (); // ]

    writer.close ();

    // write out javascript
    StringBuilder javascript = new StringBuilder ();
    javascript.append ("var tdsSegments = ");
    javascript.append (sw.toString ());
    javascript.append ("; ");

    this.getClientScript ().addToJsCode (javascript.toString ());

  }

  private void addTestConfig () throws JsonGenerationException, IOException
  {
    // get test configp[
    TestConfig testConfig = _testOpportunity.getTestConfig ();

    // get json config
    StringWriter sw = new StringWriter ();
    JsonFactory jsonFactory = new JsonFactory ();
    JsonGenerator writer = jsonFactory.createGenerator (sw);

    // create test config json
    writer.writeStartObject ();// {
    // writer.writeObjectField ("type", "object");

    // properties
    writer.writeStringField ("urlBase", UrlHelper.getBase ());
    writer.writeNumberField ("reviewPage", getViewPageNumber ());
    writer.writeBooleanField ("hasAudio", _testProps.getRequirements ().isHasAudio ());
    writer.writeBooleanField ("autoMute", shouldAutoMuteVolume ());

    // test config (DB)
    writer.writeStringField ("testName", _testProps.getDisplayName ());
    writer.writeNumberField ("testLength", testConfig.getTestLength ());
    writer.writeNumberField ("startPosition", testConfig.getStartPosition ());
    writer.writeNumberField ("contentLoadTimeout", testConfig.getContentLoadTimeout ());
    writer.writeNumberField ("requestInterfaceTimeout", testConfig.getRequestInterfaceTimeout ());
    writer.writeNumberField ("oppRestartMins", testConfig.getOppRestartMins ());
    writer.writeNumberField ("interfaceTimeout", testConfig.getInterfaceTimeout ());

    // app settings (DB or settings.config)
    writer.writeNumberField ("interfaceTimeoutDialog", TestShellSettings.getTimeoutDialog ().getValue ());
    writer.writeNumberField ("autoSaveInterval", TestShellSettings.getAutoSaveInterval ().getValue ());
    writer.writeNumberField ("forbiddenAppsInterval", TestShellSettings.getForbiddenAppsInterval ().getValue ());
    writer.writeBooleanField ("disableSaveWhenInactive", TestShellSettings.isDisableSaveWhenInactive ().getValue ());
    writer.writeBooleanField ("disableSaveWhenForbiddenApps", TestShellSettings.isDisableSaveWhenForbiddenApps ().getValue ());
    writer.writeBooleanField ("allowSkipAudio", TestShellSettings.isAllowSkipAudio ().getValue ());
    writer.writeBooleanField ("showSegmentLabels", TestShellSettings.isShowSegmentLabels ().getValue ());
    writer.writeNumberField ("audioTimeout", TestShellSettings.getAudioTimeout ().getValue ());
    writer.writeBooleanField ("enableLogging", TestShellSettings.isEnableLogging ().getValue ());

    writer.writeEndObject (); // }

    writer.close ();

    // write out javascript
    StringBuilder javascript = new StringBuilder ();
    javascript.append ("var tdsTestConfig = ");
    javascript.append (sw.toString ());
    javascript.append ("; ");

    this.getClientScript ().addToJsCode (javascript.toString ());
  }

  private void addComments () throws ReturnStatusException
  {
    Iterable<String> comments = this._studentSettings.getComments ();

    // write out javascript
    StringBuilder javascript = new StringBuilder ();
    javascript.append ("TDS.Comments = []; ");

    if (comments != null)
      for (String commentLine : comments)
      {
        javascript.append (String.format ("TDS.Comments.push('%s'); ", commentLine.replace ("'", "\\'")));
      }

    this.getClientScript ().addToJsCode (javascript.toString ());
  }

  // / <summary>
  // / Determine if we should mute the volume for reading.
  // / </summary>
  // / <returns>If this is true we need to mute the volume when the test shell
  // starts.</returns>
  private boolean shouldAutoMuteVolume ()
  {
    // check for windows 7
    // TODO: Shajib Using BrowserParser instead of Request
    // if (!Request.getUserAgent ().contains ("Windows NT 6.1"))
    if (BrowserParser.getCurrent ().getUserAgent ().contains ("Windows NT 6.1"))
      return false;

    // make sure oregon
    // NOTE: this is now applicable for Hawaii so we commented this part out
    // string clientName = StudentSettings.GetClientName();
    // if (!clientName.Contains("Oregon")) return false;

    // make sure reading
    if (!_testProps.getDisplayName ().contains ("Reading"))
      return false;

    // make sure in braille mode
    AccProperties accProps = StudentContext.getAccLookup ().getProperties ();
    if (!accProps.isBrailleEnabled ())
      return false;

    // if we get here then auto mute the volume
    return true;
  }

  private int getViewPageNumber ()
  {
    String pageValue = getRequest ().getParameter ("page");
    int page = 0;

    if (!StringUtils.isEmpty (pageValue))
    {
      page = Integer.valueOf (pageValue);
    }

    return page;
  }

}
