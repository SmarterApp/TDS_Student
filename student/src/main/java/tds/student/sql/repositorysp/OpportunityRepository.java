/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.repositorysp;

import java.io.StringWriter;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tds.student.sql.abstractions.IOpportunityRepository;
import tds.student.sql.data.BrowserCapabilities;
import tds.student.sql.data.ClientLatency;
import tds.student.sql.data.OpportunityAccommodation;
import tds.student.sql.data.OpportunityInfo;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.data.OpportunitySegment;
import tds.student.sql.data.OpportunitySegment.OpportunitySegments;
import tds.student.sql.data.OpportunityStatus;
import tds.student.sql.data.OpportunityStatusType;
import tds.student.sql.data.ServerLatency;
import tds.student.sql.data.TestConfig;
import tds.student.sql.data.TestSelection;
import tds.student.sql.data.ToolUsed;
import AIR.Common.DB.AbstractDAO;
import AIR.Common.DB.SQLConnection;
import AIR.Common.DB.SqlParametersMaps;
import AIR.Common.DB.results.DbResultRecord;
import AIR.Common.DB.results.MultiDataResultSet;
import AIR.Common.DB.results.SingleDataResultSet;
import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.ReturnStatusException;

/**
 * @author temp_rreddy
 * 
 */
@Component
@Scope ("prototype")
public class OpportunityRepository extends AbstractDAO implements IOpportunityRepository
{
  private static final Logger _logger = LoggerFactory.getLogger (OpportunityRepository.class);

  public OpportunityRepository () {
    super ();
  }

  // / <summary>
  // / Returns a list of tests and availability for the student to take the
  // test. If the student cannot take
  // / the test then this will return a reason why.
  // / </summary>
  public List<TestSelection> getEligibleTests (long testeeKey, UUID sessionKey, String grade) throws ReturnStatusException {
    List<TestSelection> summaries = new ArrayList<TestSelection> ();
    final String CMD_GET_TEST_PROPERTIES = "BEGIN; SET NOCOUNT ON; exec T_GetEligibleTests ${testee}, ${sessionKey}, ${grade}; end;";

    try (SQLConnection connection = getSQLConnection ()) {
      SqlParametersMaps parametersQuery = new SqlParametersMaps ();
      parametersQuery.put ("testee", testeeKey);
      parametersQuery.put ("sessionKey", sessionKey);
      parametersQuery.put ("grade", grade);

      SingleDataResultSet firstResultSet = executeStatement (connection, CMD_GET_TEST_PROPERTIES, parametersQuery, false).getResultSets ().next ();
      ReturnStatusException.getInstanceIfAvailable (firstResultSet);
      Iterator<DbResultRecord> records = firstResultSet.getRecords ();
      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        TestSelection testSelection = new TestSelection ();
        // BUG: If display name is null then the test is not included in
        // the
        // session
        if (record.<String> get ("displayName").equalsIgnoreCase (null))
          continue;
        // get status message
        testSelection.SetReturnStatus (ReturnStatus.parse (record));
        // get properties
        testSelection.setTestKey (record.<String> get ("testkey"));
        testSelection.setTestID (record.<String> get ("test"));
        testSelection.setOpportunity (record.<Integer> get ("opportunity"));
        testSelection.setMode ((record.<String> get ("mode") != null) ? record.<String> get ("mode") : "online");
        testSelection.setDisplayName (record.<String> get ("displayName"));
        testSelection.setMaxOpportunities (record.<Integer> get ("maxopps"));
        // check if null (seems like it can be sometimes)
        testSelection.setSortOrder ((record.<Integer> get ("sortOrder") == 0) ? 0 : record.<Integer> get ("sortOrder"));
        if (record.<String> get ("subject") != null)
          testSelection.setSubject (record.<String> get ("subject"));
        if (record.<String> get ("grade") != null)
          testSelection.setGrade (record.<String> get ("grade"));
        // get requirements
        testSelection.getRequirements ().lookup (getTdsSettings().getClientName (), testSelection);
        summaries.add (testSelection);
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return summaries;
  }

  // / <summary>
  // / Approve test accommodations for a practice test guest session segment.
  // / </summary>
  // / <returns>If everything is successful return status will be
  // NULL</returns>
  // / <remarks>
  // / This SP is not being used right now but the other overload function is.
  // I
  // am not sure the reason why.
  // / </remarks>
  public ReturnStatus approveAccommodations (OpportunityInstance oppInstance, int segment, String segmentAccoms) throws ReturnStatusException {
    final String CMD_GET_TEST_PROPERTIES = "BEGIN; SET NOCOUNT ON; exec T_ApproveAccommodations ${oppkey}, ${sessionKey}, ${browserKey}, ${segment}, ${segmentAccoms}; end;";
    ReturnStatus returnStatus = new ReturnStatus ("success");
    try (SQLConnection connection = getSQLConnection ()) {
      SqlParametersMaps parametersQuery = new SqlParametersMaps ();
      parametersQuery.put ("oppkey", oppInstance.getKey ());
      parametersQuery.put ("sessionKey", oppInstance.getSessionKey ());
      parametersQuery.put ("browserKey", oppInstance.getBrowserKey ());
      parametersQuery.put ("segment", segment);
      parametersQuery.put ("segmentAccoms", segmentAccoms);
      executeStatement (connection, CMD_GET_TEST_PROPERTIES, parametersQuery, false);
      // MultiDataResultSet multiDataResultSet = executeStatement (connection,
      // CMD_GET_TEST_PROPERTIES, parametersQuery, false);

      // if (multiDataResultSet.getUpdateCount () == 0)
      // ; // if expected success, this is success
      // else {
      // SingleDataResultSet firstResultSet = multiDataResultSet.getResultSets
      // ().next ();
      // ReturnStatusException.getInstanceIfAvailable (firstResultSet);
      // returnStatus = ReturnStatus.readAndParse (firstResultSet);
      // }

      /*
       * while (records.hasNext ()) { DbResultRecord record = records.next ();
       * returnStatus = ReturnStatus.readAndParse (record); // TODO // while
       * (records.hasNext ()) { // DbResultRecord record = records.next (); //
       * returnStatus = returnStatus.readAndParse (record); // } }
       */
    } catch (ReturnStatusException e1) {
      returnStatus = new ReturnStatus ("Failed");
      _logger.error (e1.getMessage ());
      throw new ReturnStatusException (e1);
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return returnStatus;
  }

  public OpportunityInfo openTestOpportunity (long testeeKey, String testKey, UUID sessionKey, UUID browserKey) throws ReturnStatusException {
    final String CMD_GET_TEST_PROPERTIES = "BEGIN; SET NOCOUNT ON; exec T_OpenTestOpportunity ${testee}, ${testkey}, ${sessionKey}, ${browserKey}; end;";
    OpportunityInfo result = new OpportunityInfo ();
    try (SQLConnection connection = getSQLConnection ()) {
      SqlParametersMaps parametersQuery = new SqlParametersMaps ();
      parametersQuery.put ("testee", testeeKey);
      parametersQuery.put ("testkey", testKey);
      parametersQuery.put ("sessionKey", sessionKey);
      parametersQuery.put ("browserKey", browserKey);

      SingleDataResultSet firstResultSet = executeStatement (connection, CMD_GET_TEST_PROPERTIES, parametersQuery, false).getResultSets ().next ();
      ReturnStatusException.getInstanceIfAvailable (firstResultSet, "Invalid rows returned from T_OpenTestOpportunity");
      Iterator<DbResultRecord> records = firstResultSet.getRecords ();

      if (records.hasNext ()) {
        DbResultRecord record = records.next ();
        OpportunityInfo oppInfo = new OpportunityInfo ();
        oppInfo.setStatus (OpportunityStatusType.parse ((record.<String> get ("status"))));

        if (record.<UUID> get ("oppkey") != null) {
          oppInfo.setBrowserKey (browserKey);
          oppInfo.setOppKey (record.<UUID> get ("oppkey"));
        }
        result = oppInfo;
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return result;
  }

  // / <summary>
  // / Start the test opportunity.
  // / </summary>
  // / <param name="oppInstance"></param>
  // / <param name="formKeyList">
  // / If more than one form is passed into @formkeyList they must be
  // separated
  // by semicolons.
  // / This case is generally only applicable for segmented tests and will NOT
  // BE USED in the
  // / foreseeable future</param>
  // / <remarks>
  // / It seems like formkeyList is only working if (requireRTSForm or
  // requireRTSFormWindow)
  // / [_SelectTestForm_Driver] line 40
  // / </remarks>
  public TestConfig startTestOpportunity (OpportunityInstance oppInstance, String testKey, String formKeyList) throws ReturnStatusException {
    final String CMD_GET_TEST_PROPERTIES = "BEGIN; SET NOCOUNT ON; exec T_StartTestOpportunity ${oppkey}, ${sessionKey}, ${browserID}, ${formkeyList}; end;";
    TestConfig result = new TestConfig ();
    try (SQLConnection connection = getSQLConnection ()) {
      SqlParametersMaps parametersQuery = new SqlParametersMaps ();
      parametersQuery.put ("oppkey", oppInstance.getKey ());
      parametersQuery.put ("sessionKey", oppInstance.getSessionKey ());
      parametersQuery.put ("browserID", oppInstance.getBrowserKey ());
      parametersQuery.put ("formkeyList", formKeyList);

      SingleDataResultSet firstResultSet = executeStatement (connection, CMD_GET_TEST_PROPERTIES, parametersQuery, false).getResultSets ().next ();
      ReturnStatusException.getInstanceIfAvailable (firstResultSet);
      ReturnStatus status = ReturnStatus.parse (firstResultSet);

      Iterator<DbResultRecord> records = firstResultSet.getRecords ();

      if (records.hasNext ()) {
        DbResultRecord record = records.next ();
        // check if we "started"
        if (record.hasColumn ("restart")) {
          TestConfig oppConfig = new TestConfig ();
          oppConfig.setStatus (OpportunityStatusType.parse (status.getStatus ()));

          oppConfig.setRestart (record.<Integer> get ("restart"));
          oppConfig.setTestLength (record.<Integer> get ("testLength"));
          oppConfig.setInterfaceTimeout (record.<Integer> get ("interfaceTimeout"));
          oppConfig.setOppRestartMins (record.<Integer> get ("OppRestart"));
          oppConfig.setContentLoadTimeout (record.<Integer> get ("contentloadtimeout"));
          oppConfig.setRequestInterfaceTimeout (record.<Integer> get ("requestInterfaceTimeout"));
          oppConfig.setStartPosition (record.<Integer> get ("startPosition"));
          oppConfig.setPrefetch (record.<Integer> get ("prefetch"));
          oppConfig.setScoreByTDS (record.<Boolean> get ("scoreByTDS"));
          oppConfig.setValidateCompleteness (record.<Boolean> get ("validateCompleteness"));

          // not used:
          // oppConfig.InitialAbility =
          // reader.GetDouble("initialability");
          // oppConfig.ExcludeItemTypes =
          // reader.GetString("excludeItemTypes");
          // oppConfig.Segments = reader.GetInt32("segments");

          result = oppConfig;
        }
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return result;
  }

  // / <summary>
  // / Get the opportunity segments.
  // / </summary>
  public OpportunitySegments getOpportunitySegments (OpportunityInstance oppInstance) throws ReturnStatusException {
    try {
      return getOpportunitySegments (oppInstance.getKey (), oppInstance.getSessionKey (), oppInstance.getBrowserKey ());
    } catch (ReturnStatusException e) {
      e.printStackTrace ();
      throw new ReturnStatusException (e);
    }
  }

  // / <summary>
  // / Get the opportunity segments without validation.
  // / </summary>
  public OpportunitySegments getOpportunitySegments (UUID oppKey) throws ReturnStatusException {
    return getOpportunitySegments (oppKey, null, null);
  }

  private OpportunitySegments getOpportunitySegments (UUID oppKey, UUID sessionKey, UUID browserKey) throws ReturnStatusException {
    final String CMD_GET_TEST_PROPERTIES = "BEGIN; SET NOCOUNT ON; exec T_GetOpportunitySegments ${oppkey}, ${session}, ${browser}; end;";
    OpportunitySegments oppSegments = new OpportunitySegment ().new OpportunitySegments ();
    try (SQLConnection connection = getSQLConnection ()) {
      SqlParametersMaps parametersQuery = new SqlParametersMaps ();
      parametersQuery.put ("oppkey", oppKey);
      parametersQuery.put ("session", sessionKey);
      parametersQuery.put ("browser", browserKey);

      SingleDataResultSet firstResultSet = executeStatement (connection, CMD_GET_TEST_PROPERTIES, parametersQuery, false).getResultSets ().next ();
      ReturnStatusException.getInstanceIfAvailable (firstResultSet);
      Iterator<DbResultRecord> records = firstResultSet.getRecords ();
      while (records.hasNext ()) {
        DbResultRecord record = records.next ();

        OpportunitySegment oppSegment = new OpportunitySegment ();

        oppSegment.setKey (record.<String> get ("segmentKey"));
        oppSegment.setId (record.<String> get ("segmentID"));
        oppSegment.setPosition (record.<Integer> get ("position"));
        oppSegment.setFormKey (record.<String> get ("formKey"));
        oppSegment.setFormID (record.<String> get ("formID"));
        oppSegment.setIsPermeable (record.<Integer> get ("isPermeable"));
        oppSegment.setRestorePermOn (record.<String> get ("restorePermOn"));
        oppSegment.setFtItems (record.<String> get ("ftItems"));

        oppSegments.add (oppSegment);
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return oppSegments;
  }

  // / <summary>
  // / Get the opportunities accommodations for the test and all segments.
  // / </summary>
  // / <param name="oppInstance"></param>
  // / <returns></returns>
  // / <remarks>
  // / Larry:
  // "This is a very economical proc, so it’s okay to call for each browser page (not item page)."
  // / </remarks>
  public List<OpportunityAccommodation> getOpportunityAccommodations (OpportunityInstance oppInstance, String testKey) throws ReturnStatusException {
    final String CMD_GET_TEST_PROPERTIES = "BEGIN; SET NOCOUNT ON; exec T_GetOpportunityAccommodations ${oppkey}, ${session}, ${browser}; end;";
    List<OpportunityAccommodation> oppAccs = new ArrayList<OpportunityAccommodation> ();
    try (SQLConnection connection = getSQLConnection ()) {
      SqlParametersMaps parametersQuery = new SqlParametersMaps ();
      parametersQuery.put ("oppkey", oppInstance.getKey ());
      // TODO
      parametersQuery.put ("session", ((oppInstance.getSessionKey () != null) ? oppInstance.getSessionKey () : null));
      parametersQuery.put ("browser", ((oppInstance.getBrowserKey () != null) ? oppInstance.getBrowserKey () : null));

      SingleDataResultSet firstResultSet = executeStatement (connection, CMD_GET_TEST_PROPERTIES, parametersQuery, false).getResultSets ().next ();
      ReturnStatusException.getInstanceIfAvailable (firstResultSet);
      Iterator<DbResultRecord> records = firstResultSet.getRecords ();
      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        OpportunityAccommodation oppAcc = new OpportunityAccommodation ();
        oppAcc.setSegmentPosition (record.<Integer> get ("segment"));
        oppAcc.setAccType (record.<String> get ("accType"));
        oppAcc.setAccCode (record.<String> get ("accCode"));
        oppAccs.add (oppAcc);
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return oppAccs;
  }

  // / <summary>
  // / Used for logging opportunity client info
  // / </summary>
  public void logOpportunityClient (OpportunityInstance oppInstance, int restart, BrowserCapabilities browserCapabilities) throws ReturnStatusException {
    final String SQL_QUERY_OPPORTUNITY_CLIENT = "insert into ${ArchiveDB}.OpportunityClient (_fk_TestOpportunity, _fk_Browser, Restart, ServerIP, ClientIP, ProxyIP, UserAgent, ScreenRez, IsSecure, MACAddress, LocalIP, TextToSpeech)"
        + " values (${OppKey},${browserkey},${Restart},host_name(),${ClientIP},${ProxyIP},${UserAgent},${ScreenRez},${IsSecure},${MACAddress},${LocalIP},${TextToSpeech})";
    SqlParametersMaps parametersQuery = new SqlParametersMaps ();
    parametersQuery.put ("OppKey", oppInstance.getKey ());
    parametersQuery.put ("browserkey", oppInstance.getBrowserKey ());
    parametersQuery.put ("Restart", restart);
    parametersQuery.put ("ClientIP", browserCapabilities.getClientIP ());
    parametersQuery.put ("ProxyIP", browserCapabilities.getProxyIP ());
    parametersQuery.put ("UserAgent", browserCapabilities.getUserAgent ());
    parametersQuery.put ("ScreenRez", browserCapabilities.getScreenRez ());
    parametersQuery.put ("IsSecure", browserCapabilities.isSecure ());
    parametersQuery.put ("MACAddress", browserCapabilities.getMacAddress ());
    parametersQuery.put ("LocalIP", browserCapabilities.getLocalIP ());
    parametersQuery.put ("TextToSpeech", browserCapabilities.getTextToSpeech ());
    try (SQLConnection connection = getSQLConnection ()) {

      MultiDataResultSet sets = executeStatement (connection, fixDataBaseNames (SQL_QUERY_OPPORTUNITY_CLIENT), parametersQuery, false);
      sets.getUpdateCount ();

    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
  }

  // / <summary>
  // / Validate if the opp is still valid and return status.
  // / </summary>
  // / <returns>
  // / If this returns a value then the opp is valid and you can check the
  // status. Otherwise
  // / if ReturnStatus is not null then the opp is not valid.
  // / </returns>
  // / <remarks>
  // / An example of the opp not being valid is if the session has been
  // closed.
  // / </remarks>
  public OpportunityStatus validateAccess (OpportunityInstance oppInstance) throws ReturnStatusException {
    final String CMD_GET_VALIDATE_ACCESS = "BEGIN; SET NOCOUNT ON; exec T_ValidateAccess ${oppkey}, ${session}, ${browserID}; end;";
    OpportunityStatus result = null;
    try (SQLConnection connection = getSQLConnection ()) {
      SqlParametersMaps parametersQuery = new SqlParametersMaps ();
      parametersQuery.put ("oppkey", oppInstance.getKey ());
      parametersQuery.put ("session", oppInstance.getSessionKey ());
      parametersQuery.put ("browserID", oppInstance.getBrowserKey ());

      SingleDataResultSet firstResultSet = executeStatement (connection, CMD_GET_VALIDATE_ACCESS, parametersQuery, false).getResultSets ().next ();
      ReturnStatusException.getInstanceIfAvailable (firstResultSet);
      Iterator<DbResultRecord> records = firstResultSet.getRecords ();
      if (records.hasNext ()) {
        DbResultRecord record = records.next ();
        if (record.hasColumn ("oppStatus")) {
          result = new OpportunityStatus ();
          result.setStatus (OpportunityStatusType.parse (record.<String> get ("oppStatus")));
          if (record.<String> get ("comment") != null)
            result.setComment (record.<String> get ("comment"));
        }
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return result;
  }

  @Override
  public ReturnStatus setStatus (UUID oppKey, String status, String comment) throws ReturnStatusException {
    final String CMD_SET_OPP_STATUS = "BEGIN; SET NOCOUNT ON; exec SetOpportunityStatus ${oppkey}, ${status}, ${suppressReport}, ${requestor}, ${comment}; end;";
    ReturnStatus returnStatus = null;
    try (SQLConnection connection = getSQLConnection ()) {
      SqlParametersMaps parametersQuery = new SqlParametersMaps ();
      parametersQuery.put ("oppkey", oppKey);
      parametersQuery.put ("status", status.toLowerCase ());
      parametersQuery.put ("suppressReport", 0);
      parametersQuery.put ("requestor", "testee");
      parametersQuery.put ("comment", comment);

      SingleDataResultSet firstResultSet = executeStatement (connection, CMD_SET_OPP_STATUS, parametersQuery, false).getResultSets ().next ();
      returnStatus = ReturnStatus.readAndParse (firstResultSet);

    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return returnStatus;
  }

  public ReturnStatus setStatusWithValidation (OpportunityInstance oppInstance, String status, String comment) throws ReturnStatusException {
    final String CMD_GET_OPP_STATUS = "BEGIN; SET NOCOUNT ON; exec T_SetOpportunityStatus ${oppkey}, ${status}, ${sessionKey}, ${browserKey}, ${comment}; end;";
    ReturnStatus returnStatus = null;
    try (SQLConnection connection = getSQLConnection ()) {
      SqlParametersMaps parametersQuery = new SqlParametersMaps ();
      parametersQuery.put ("oppkey", oppInstance.getKey ());
      parametersQuery.put ("status", status.toLowerCase ());
      parametersQuery.put ("sessionKey", oppInstance.getSessionKey ());
      parametersQuery.put ("browserKey", oppInstance.getBrowserKey ());
      parametersQuery.put ("comment", comment);

      SingleDataResultSet firstResultSet = executeStatement (connection, CMD_GET_OPP_STATUS, parametersQuery, false).getResultSets ().next ();
      returnStatus = ReturnStatus.readAndParse (firstResultSet);
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return returnStatus;
  }

  // / <summary>
  // / Record client or server latency
  // / </summary>
  public int recordServerLatency (OpportunityInstance oppInstance, ServerLatency serverLatency) throws ReturnStatusException {
    MultiDataResultSet multiDataResultSet = null;
    final String CMD_GET_RECORD_SERVER_LATENCY = "BEGIN; SET NOCOUNT ON; exec T_RecordServerLatency ${operation}, ${oppkey}, ${session}, ${browser}, ${serverLatency}, ${dbLatency}, ${pageList}, ${itemList}; end;";
    try (SQLConnection connection = getSQLConnection ()) {
      SqlParametersMaps parametersQuery = new SqlParametersMaps ();
      parametersQuery.put ("operation", serverLatency.getOperationName ());
      parametersQuery.put ("oppkey", oppInstance.getKey ());
      parametersQuery.put ("session", oppInstance.getSessionKey ());
      parametersQuery.put ("browser", oppInstance.getBrowserKey ());
      parametersQuery.put ("serverLatency", serverLatency.getLatency ());
      parametersQuery.put ("dbLatency", serverLatency.getDbLatency ());
      parametersQuery.put ("pageList", serverLatency.getPageList ());
      parametersQuery.put ("itemList", serverLatency.getItemList ());

      multiDataResultSet = executeStatement (connection, CMD_GET_RECORD_SERVER_LATENCY, parametersQuery, false);

    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }

    // TODO Ravi/Shiva needs to return a value but it is not clear what
    // values
    // does it return.
    return multiDataResultSet.getUpdateCount ();
  }

  public int recordClientLatency (OpportunityInstance oppInstance, ClientLatency clientLatency) throws ReturnStatusException {
    final String CMD_GET_RECORD_SERVER_LATENCY = "BEGIN; SET NOCOUNT ON; exec T_RecordClientLatency ${oppkey}, ${session}, ${browser}, ${itempage}, ${numitems}, ${visitCount}, ${createDate}, ${loadDate}, ${loadTime}, ${requestTime}, ${visitTime}, ${loadAttempts}, ${toolsUsed}; end;";
    try (SQLConnection connection = getSQLConnection ()) {
      SqlParametersMaps parametersQuery = new SqlParametersMaps ();
      parametersQuery.put ("oppkey", oppInstance.getKey ());
      parametersQuery.put ("session", oppInstance.getSessionKey ());
      parametersQuery.put ("browser", oppInstance.getBrowserKey ());
      parametersQuery.put ("itempage", clientLatency.getItemPage ());
      parametersQuery.put ("numitems", clientLatency.getNumItems ());
      parametersQuery.put ("visitCount", clientLatency.getVisitCount ());
      parametersQuery.put ("createDate", clientLatency.getCreateDate ());
      parametersQuery.put ("loadDate", clientLatency.getLoadDate ());
      parametersQuery.put ("loadTime", clientLatency.getLoadTime ());
      parametersQuery.put ("requestTime", clientLatency.getRequestTime ());
      parametersQuery.put ("visitTime", clientLatency.getVisitTime ());
      parametersQuery.put ("loadAttempts", clientLatency.getLoadAttempts ());
      parametersQuery.put ("toolsUsed", clientLatency.getToolsUsed ());

      SingleDataResultSet firstResultSet = executeStatement (connection, CMD_GET_RECORD_SERVER_LATENCY, parametersQuery, false).getResultSets ().next ();
      ReturnStatusException.getInstanceIfAvailable (firstResultSet);

    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    // TODO Ravi/Shiva needs to return a value but it is not clear what
    // values
    // does it return.
    return 0;
  }

  public int recordClientLatencies (OpportunityInstance oppInstance, List<ClientLatency> clientLatencies) throws ReturnStatusException {
    Date d = new Date ();
    DateFormat formatter = new SimpleDateFormat ("yyyy-mm-dd HH:mm:ss.SSS");
    String date = formatter.format (d);
    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance ();
    DocumentBuilder docBuilder;
    StreamResult result = new StreamResult (new StringWriter ());
    try {
      docBuilder = docFactory.newDocumentBuilder ();
      Document doc = docBuilder.newDocument ();
      Element rootElement = doc.createElement ("latencies");
      // build xml
      for (ClientLatency latency : clientLatencies) {

        Element latencyRoot = doc.createElement ("latency");
        rootElement.appendChild (latencyRoot);

        Attr attr = doc.createAttribute ("page");
        attr.setValue (latency.getItemPage () + "");
        latencyRoot.setAttributeNode (attr);

        Attr numitems = doc.createAttribute ("numitems");
        numitems.setValue (latency.getNumItems () + "");
        latencyRoot.setAttributeNode (numitems);

        Attr requesttime = doc.createAttribute ("requesttime");
        requesttime.setValue (latency.getRequestTime () + "");
        latencyRoot.setAttributeNode (requesttime);

        Attr loadattempts = doc.createAttribute ("loadattempts");
        loadattempts.setValue (latency.getLoadAttempts () + "");
        latencyRoot.setAttributeNode (loadattempts);

        Attr loaddate = doc.createAttribute ("loaddate");
        loaddate.setValue (date);
        latencyRoot.setAttributeNode (loaddate);

        Attr loadtime = doc.createAttribute ("loadtime");
        loadtime.setValue (latency.getLoadTime () + "");
        latencyRoot.setAttributeNode (loadtime);

        Attr createdate = doc.createAttribute ("createdate");
        createdate.setValue (date);
        latencyRoot.setAttributeNode (createdate);

        Attr visitcount = doc.createAttribute ("visitcount");
        visitcount.setValue (latency.getVisitCount () + "");
        latencyRoot.setAttributeNode (visitcount);

        Attr visittime = doc.createAttribute ("visittime");
        visittime.setValue (latency.getVisitTime () + "");
        latencyRoot.setAttributeNode (visittime);

        Attr visitdate = doc.createAttribute ("visitdate");
        visitdate.setValue (date);
        latencyRoot.setAttributeNode (visitdate);

        rootElement.appendChild (latencyRoot);
        doc.appendChild (rootElement);
        TransformerFactory transformerFactory = TransformerFactory.newInstance ();

        try {
          Transformer transformer = transformerFactory.newTransformer ();
          System.out.println ("DOCUMENT...." + doc.toString ());
          DOMSource domSource = new DOMSource (doc);
          try {
            transformer.transform (domSource, result);
          } catch (TransformerException e) {
            e.printStackTrace ();
          }

        } catch (TransformerConfigurationException e) {
          _logger.error (e.getMessage ());
          throw new ReturnStatusException (e);
        }
      }
      final String CMD_GET_RECORD_CLIENT_LATENCY_XML = "BEGIN; SET NOCOUNT ON; exec T_RecordClientLatency_XML ${oppkey}, ${session}, ${browser}, ${latencies}; end;";
      try (SQLConnection connection = getSQLConnection ()) {
        SqlParametersMaps parametersQuery = new SqlParametersMaps ();
        parametersQuery.put ("oppkey", oppInstance.getKey ());
        parametersQuery.put ("session", oppInstance.getSessionKey ());
        parametersQuery.put ("browser", oppInstance.getBrowserKey ());
        parametersQuery.put ("latencies", result.getWriter ().toString ());
        executeStatement (connection, CMD_GET_RECORD_CLIENT_LATENCY_XML, parametersQuery, false);
      } catch (SQLException e) {
        _logger.error (e.getMessage ());
        throw new ReturnStatusException (e);
      }
    } catch (ParserConfigurationException e1) {
      _logger.error (e1.getMessage ());
      throw new ReturnStatusException (e1);
    }
    return 0;
  }

  public int recordToolsUsed (UUID oppKey, List<ToolUsed> toolsUsed) throws ReturnStatusException {

    try {
      DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance ();
      DocumentBuilder docBuilder = docFactory.newDocumentBuilder ();
      Document doc = docBuilder.newDocument ();
      Element rootElement = doc.createElement ("toolsused");
      doc.appendChild (rootElement);
      StreamResult result = new StreamResult (new StringWriter ());
      // build xml
      for (ToolUsed toolUsed : toolsUsed) {

        Element toolRoot = doc.createElement ("tool");

        Element page = doc.createElement ("page");
        page.appendChild (doc.createTextNode (toolUsed.getPage () + ""));
        toolRoot.appendChild (page);

        Element tooltype = doc.createElement ("tooltype");
        page.appendChild (doc.createTextNode (toolUsed.getType ()));
        toolRoot.appendChild (tooltype);

        Element toolcodes = doc.createElement ("toolcode");
        toolcodes.appendChild (doc.createTextNode (toolUsed.getCode ()));
        toolRoot.appendChild (toolcodes);

        Element count = doc.createElement ("count");
        count.appendChild (doc.createTextNode (toolUsed.getCount () + ""));
        toolRoot.appendChild (count);
        rootElement.appendChild (toolRoot);

        TransformerFactory transformerFactory = TransformerFactory.newInstance ();

        try {
          Transformer transformer = transformerFactory.newTransformer ();
          DOMSource domSource = new DOMSource (doc);
          try {
            transformer.transform (domSource, result);
          } catch (TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace ();
          }

        } catch (TransformerConfigurationException e) {
          _logger.error (e.getMessage ());
          throw new ReturnStatusException (e);
        }

      }
      final String CMD_GET_RECORD_TOOLS_USED = "BEGIN; SET NOCOUNT ON; exec T_RecordToolsUsed ${oppkey}, ${toolsused}; end;";
      try (SQLConnection connection = getSQLConnection ()) {
        SqlParametersMaps parametersQuery = new SqlParametersMaps ();
        parametersQuery.put ("oppkey", oppKey);
        parametersQuery.put ("toolsused", result.getWriter ().toString ());
        System.out.println ("OUTPUT::" + result.getWriter ().toString ());
        executeStatement (connection, CMD_GET_RECORD_TOOLS_USED, parametersQuery, false);

      } catch (SQLException e) {
        _logger.error (e.getMessage ());
        throw new ReturnStatusException (e);
      }

    } catch (ParserConfigurationException e1) {
      _logger.error (e1.getMessage ());
      throw new ReturnStatusException (e1);
    }
    return 0;
  }

  public int submitRequest (OpportunityInstance oppInstance, int itemPage, int itemPosition, String requestType, String requestDescription, String requestValue, String requestParameters)
      throws ReturnStatusException {
    final String CMD_GET_SUBMIT_REQUEST = "BEGIN; SET NOCOUNT ON; exec T_SubmitRequest ${sessionKey}, ${oppkey}, ${itempage}, ${itemposition}, ${requestType}, ${requestValue}, ${requestParameters}, ${requestDescription}; end;";
    try (SQLConnection connection = getSQLConnection ()) {
      SqlParametersMaps parametersQuery = new SqlParametersMaps ();
      parametersQuery.put ("sessionKey", oppInstance.getSessionKey ());
      parametersQuery.put ("oppkey", oppInstance.getKey ());
      parametersQuery.put ("itempage", itemPage);
      parametersQuery.put ("itemposition", itemPosition);
      parametersQuery.put ("requestType", requestType);
      parametersQuery.put ("requestValue", requestValue);
      parametersQuery.put ("requestParameters", requestParameters);
      parametersQuery.put ("requestDescription", requestDescription);
      // Stored procedure not return anything
      executeStatement (connection, CMD_GET_SUBMIT_REQUEST, parametersQuery, false);
      // MultiDataResultSet multiDataResultSet = executeStatement
      // (connection, CMD_GET_SUBMIT_REQUEST, parametersQuery, false);
      // if (multiDataResultSet.getUpdateCount () == 0) ; // if expected
      // success, this is success
      // else {
      // SingleDataResultSet firstResultSet =
      // multiDataResultSet.getResultSets ().next ();
      // ReturnStatusException.getInstanceIfAvailable (firstResultSet);
      // }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return 0;
  }

  public ReturnStatus waitForSegment (OpportunityInstance oppInstance, int segment, boolean entry, boolean exit) throws ReturnStatusException {
    ReturnStatus returnStatus = null;
    final String CMD_GET_SUBMIT_REQUEST = "BEGIN; SET NOCOUNT ON; exec T_WaitForSegment ${oppkey}, ${sessionkey}, ${browserkey}, ${segment}, ${entry}, ${exit}; end;";
    try (SQLConnection connection = getSQLConnection ()) {
      SqlParametersMaps parametersQuery = new SqlParametersMaps ();
      parametersQuery.put ("oppkey", oppInstance.getKey ());
      parametersQuery.put ("sessionkey", oppInstance.getSessionKey ());
      parametersQuery.put ("browserkey", oppInstance.getBrowserKey ());
      parametersQuery.put ("segment", segment);
      parametersQuery.put ("entry", entry);
      parametersQuery.put ("exit", exit);

      SingleDataResultSet firstResultSet = executeStatement (connection, CMD_GET_SUBMIT_REQUEST, parametersQuery, false).getResultSets ().next ();
      ReturnStatusException.getInstanceIfAvailable (firstResultSet);
      returnStatus = ReturnStatus.readAndParse (firstResultSet);

    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return returnStatus;
  }

  public ReturnStatus exitSegment (OpportunityInstance oppInstance, int segment) throws ReturnStatusException {
    ReturnStatus returnStatus = null;
    final String CMD_GET_SUBMIT_REQUEST = "BEGIN; SET NOCOUNT ON; exec T_ExitSegment ${oppkey},${segment}, ${sessionkey}, ${browserkey} ; end;";
    try (SQLConnection connection = getSQLConnection ()) {
      SqlParametersMaps parametersQuery = new SqlParametersMaps ();
      parametersQuery.put ("oppkey", oppInstance.getKey ());
      parametersQuery.put ("segment", segment);
      parametersQuery.put ("sessionkey", oppInstance.getSessionKey ());
      parametersQuery.put ("browserkey", oppInstance.getBrowserKey ());

      SingleDataResultSet firstResultSet = executeStatement (connection, CMD_GET_SUBMIT_REQUEST, parametersQuery, false).getResultSets ().next ();
      ReturnStatusException.getInstanceIfAvailable (firstResultSet);
      returnStatus = ReturnStatus.readAndParse (firstResultSet);

    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return returnStatus;
  }

  // / <summary>
  // / Record a comment for the opportunity.
  // / </summary>
  public void recordComment (UUID sessionKey, long testeeKey, UUID oppKey, String comment) throws ReturnStatusException {
    final String CMD_GET_SUBMIT_REQUEST = "BEGIN; SET NOCOUNT ON; exec T_RecordComment ${sessionKey}, ${testee}, ${comment}, ${context}, ${oppKey}; end;";
    try (SQLConnection connection = getSQLConnection ()) {
      SqlParametersMaps parametersQuery = new SqlParametersMaps ();
      parametersQuery.put ("sessionKey", sessionKey);
      parametersQuery.put ("testee", testeeKey);
      parametersQuery.put ("comment", comment);
      parametersQuery.put ("context", "GlobalNotes");
      parametersQuery.put ("oppKey", oppKey);
      executeStatement (connection, CMD_GET_SUBMIT_REQUEST, parametersQuery, false);
      // SingleDataResultSet firstResultSet = executeStatement
      // (connection, CMD_GET_SUBMIT_REQUEST, parametersQuery,
      // false).getResultSets ().next ();
      // ReturnStatusException.getInstanceIfAvailable (firstResultSet);

    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }

  }

  // / <summary>
  // / Record a comment for the opportunity.
  // / </summary>
  public String getComment (UUID oppKey) throws ReturnStatusException {
    final String CMD_GET_TEST_PROPERTIES = "BEGIN; SET NOCOUNT ON; exec T_GetOpportunityComment ${oppkey}, ${context}; end;";
    String comment = null;
    try (SQLConnection connection = getSQLConnection ()) {
      SqlParametersMaps parametersQuery = new SqlParametersMaps ();
      parametersQuery.put ("oppkey", oppKey);
      parametersQuery.put ("context", "GlobalNotes");

      Iterator<SingleDataResultSet> results = executeStatement (connection, CMD_GET_TEST_PROPERTIES, parametersQuery, false).getResultSets ();
      if (results.hasNext ()) {
        SingleDataResultSet firstResultSet = results.next ();
        ReturnStatusException.getInstanceIfAvailable (firstResultSet);
        Iterator<DbResultRecord> records = firstResultSet.getRecords ();
        if (records.hasNext ()) {
          DbResultRecord record = records.next ();
          comment = record.<String> get ("comment");

        }
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }

    return comment;
  }

  @Override
  public int getOpportunityNumber (UUID oppKey) throws ReturnStatusException {
    // TODO Auto-generated method stub
    return 0;
  }

}
