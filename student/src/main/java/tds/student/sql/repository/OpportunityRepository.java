/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.repository;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tds.dll.api.ICommonDLL;
import tds.dll.api.IStudentDLL;
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
  private static final Logger _logger     = LoggerFactory.getLogger (OpportunityRepository.class);

  @Autowired
  private ICommonDLL          _commonDll  = null;
  @Autowired
  private IStudentDLL         _studentDll = null;

  public OpportunityRepository () {
    super ();
  }

  public List<TestSelection> getEligibleTests (long testeeKey, UUID sessionKey, String grade) throws ReturnStatusException {
    List<TestSelection> summaries = new ArrayList<TestSelection> ();

    try (SQLConnection connection = getSQLConnection ()) {
      SingleDataResultSet firstResultSet = _studentDll.T_GetEligibleTests_SP (connection, testeeKey, sessionKey, grade);
//      ReturnStatusException.getInstanceIfAvailable (firstResultSet);
      
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
        testSelection.getRequirements ().lookup (getTdsSettings ().getClientName (), testSelection);
        summaries.add (testSelection);
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return summaries;
  }

  public ReturnStatus approveAccommodations (OpportunityInstance oppInstance, int segment, String segmentAccoms) throws ReturnStatusException {
    ReturnStatus returnStatus = new ReturnStatus ("success");
    try (SQLConnection connection = getSQLConnection ()) {
      SingleDataResultSet firstResultSet = _studentDll.T_ApproveAccommodations_SP (connection, segment, oppInstance.getKey (), oppInstance.getSessionKey (), oppInstance.getBrowserKey (),
          segmentAccoms);
      ReturnStatusException.getInstanceIfAvailable (firstResultSet);

    } catch (ReturnStatusException e1) {
      _logger.error (e1.getMessage ());
      throw e1;
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return returnStatus;
  }

  public OpportunityInfo openTestOpportunity (long testeeKey, String testKey, UUID sessionKey, UUID browserKey) throws ReturnStatusException {
    OpportunityInfo result = new OpportunityInfo ();
    try (SQLConnection connection = getSQLConnection ()) {

      SingleDataResultSet firstResultSet = _studentDll.T_OpenTestOpportunity_SP (connection, testeeKey, testKey, sessionKey, browserKey, null);
      ReturnStatusException.getInstanceIfAvailable (firstResultSet);

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

  public TestConfig startTestOpportunity (OpportunityInstance oppInstance, String testKey, String formKeyList) throws ReturnStatusException {
    TestConfig oppConfig = new TestConfig ();
    try (SQLConnection connection = getSQLConnection ()) {
      
      SingleDataResultSet firstResultSet = null;
      Integer transactionIsolation = null;
      try {
        transactionIsolation = connection.getTransactionIsolation ();
        connection.setTransactionIsolation (Connection.TRANSACTION_READ_COMMITTED);
        firstResultSet = _studentDll.T_StartTestOpportunity_SP (connection, oppInstance.getKey (), oppInstance.getSessionKey (), oppInstance.getBrowserKey (), formKeyList);
      } catch (Exception e) {
        throw e;
      } finally {
        if(transactionIsolation!=null) {
          connection.setTransactionIsolation (transactionIsolation);
        }
      }
      
      
      ReturnStatusException.getInstanceIfAvailable (firstResultSet);

      ReturnStatus status = ReturnStatus.parse (firstResultSet);
      oppConfig.setReturnStatus (status);

      Iterator<DbResultRecord> records = firstResultSet.getRecords ();

      if (records.hasNext ()) {

        DbResultRecord record = records.next ();
        // check if we "started"
        if (record.hasColumn ("restart")) {
          // TestConfig oppConfig = new TestConfig ();
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
          // result = oppConfig;
        }
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    // return result;
    return oppConfig;
  }

  public OpportunitySegments getOpportunitySegments (OpportunityInstance oppInstance) throws ReturnStatusException {
    try {
      return getOpportunitySegments (oppInstance.getKey (), oppInstance.getSessionKey (), oppInstance.getBrowserKey ());
    } catch (ReturnStatusException e) {
      e.printStackTrace ();
      throw new ReturnStatusException (e);
    }
  }

  public OpportunitySegments getOpportunitySegments (UUID oppKey) throws ReturnStatusException {
    return getOpportunitySegments (oppKey, null, null);
  }

  private OpportunitySegments getOpportunitySegments (UUID oppKey, UUID sessionKey, UUID browserKey) throws ReturnStatusException {
    OpportunitySegments oppSegments = new OpportunitySegment ().new OpportunitySegments ();
    try (SQLConnection connection = getSQLConnection ()) {

      SingleDataResultSet firstResultSet = _studentDll.T_GetOpportunitySegments_SP (connection, oppKey, sessionKey, browserKey);
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

  public List<OpportunityAccommodation> getOpportunityAccommodations (OpportunityInstance oppInstance, String testKey) throws ReturnStatusException {
    List<OpportunityAccommodation> oppAccs = new ArrayList<OpportunityAccommodation> ();
    try (SQLConnection connection = getSQLConnection ()) {

      SingleDataResultSet firstResultSet = _studentDll.T_GetOpportunityAccommodations_SP (connection, oppInstance.getKey (), (oppInstance.getSessionKey () != null) ? oppInstance.getSessionKey ()
          : null, (oppInstance.getBrowserKey () != null) ? oppInstance.getBrowserKey () : null);
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

  public void logOpportunityClient (OpportunityInstance oppInstance, int restart, BrowserCapabilities browserCapabilities) throws ReturnStatusException {
    final String SQL_QUERY_OPPORTUNITY_CLIENT = "insert into ${ArchiveDB}.opportunityclient (_fk_TestOpportunity, _fk_Browser, Restart, ServerIP, ClientIP, ProxyIP, UserAgent, ScreenRez, IsSecure, MACAddress, LocalIP, TextToSpeech, dbname)"
        + " values (${OppKey},${browserkey},${Restart},@@hostname,${ClientIP},${ProxyIP},${UserAgent},${ScreenRez},${IsSecure},${MACAddress},${LocalIP},${TextToSpeech}, ${dbname})";
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
    String sessionDB = getTdsSettings ().getTDSSessionDBName ();
    parametersQuery.put ("dbname", sessionDB);
    try (SQLConnection connection = getSQLConnection ()) {

      executeStatement (connection, fixDataBaseNames (SQL_QUERY_OPPORTUNITY_CLIENT), parametersQuery, false);

    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
  }

  public OpportunityStatus validateAccess (OpportunityInstance oppInstance) throws ReturnStatusException {
    OpportunityStatus result = null;
    try (SQLConnection connection = getSQLConnection ()) {

      SingleDataResultSet firstResultSet = _studentDll.T_ValidateAccess_SP (connection, oppInstance.getKey (), oppInstance.getSessionKey (), oppInstance.getBrowserKey ());
      ReturnStatusException.getInstanceIfAvailable (firstResultSet);

      Iterator<DbResultRecord> records = firstResultSet.getRecords ();
      if (records.hasNext ()) {
        DbResultRecord record = records.next ();
        if (record.hasColumn ("oppStatus")) {
          result = new OpportunityStatus ();
          result.setStatus (OpportunityStatusType.parse (record.<String> get ("oppStatus")));
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
    try (SQLConnection connection = getSQLConnection ()) {
      SingleDataResultSet firstResultSet = _commonDll.SetOpportunityStatus_SP (connection, oppKey, status.toLowerCase (), false, "testee", comment);
      ReturnStatusException.getInstanceIfAvailable (firstResultSet);
      return ReturnStatus.readAndParse (firstResultSet);
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
  }

  public ReturnStatus setStatusWithValidation (OpportunityInstance oppInstance, String status, String comment) throws ReturnStatusException {
    try (SQLConnection connection = getSQLConnection ()) {
      SingleDataResultSet resultSet = _studentDll.T_SetOpportunityStatus_SP (connection, oppInstance.getKey (), status.toLowerCase (), oppInstance.getSessionKey (), oppInstance.getBrowserKey (),
          comment);
      ReturnStatusException.getInstanceIfAvailable (resultSet);
      return ReturnStatus.readAndParse (resultSet);
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
  }

  public int recordServerLatency (OpportunityInstance oppInstance, ServerLatency serverLatency) throws ReturnStatusException {
    int updateCount = 0;
    try (SQLConnection connection = getSQLConnection ()) {
      updateCount = _studentDll.T_RecordServerLatency_SP (connection, serverLatency.getOperationName (), oppInstance.getKey (), oppInstance.getSessionKey (), oppInstance.getBrowserKey (),
          Integer.valueOf ((int) serverLatency.getLatency ()), Integer.valueOf ((int) serverLatency.getDbLatency ()), serverLatency.getPageList (), serverLatency.getItemList ());

    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }

    return updateCount;
  }

  public int recordClientLatency (OpportunityInstance oppInstance, ClientLatency clientLatency) throws ReturnStatusException {
    try (SQLConnection connection = getSQLConnection ()) {
      _studentDll.T_RecordClientLatency_SP (connection, oppInstance.getKey (), oppInstance.getSessionKey (), oppInstance.getBrowserKey (), clientLatency.getItemPage (), clientLatency.getNumItems (),
          clientLatency.getVisitCount (), clientLatency.getCreateDate (), clientLatency.getLoadDate (), clientLatency.getLoadTime (), clientLatency.getRequestTime (), clientLatency.getVisitTime (),
          Integer.valueOf (clientLatency.getLoadAttempts ()), clientLatency.getLoadDate (), clientLatency.getToolsUsed ());
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

    DateFormat formatter = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss.SSS");
    String date = null;
    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance ();
    DocumentBuilder docBuilder;
    StreamResult result = new StreamResult (new StringWriter ());
 
    try {
      docBuilder = docFactory.newDocumentBuilder ();
      Document doc = docBuilder.newDocument ();
      Element rootElement = doc.createElement ("latencies");
      doc.appendChild (rootElement);
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
        if (latency.getLoadDate () != null) {
          date = formatter.format (latency.getLoadDate ());
          loaddate.setValue (date);
        } else {
          loaddate.setValue (null);
        }
        latencyRoot.setAttributeNode (loaddate);

        Attr loadtime = doc.createAttribute ("loadtime");
        loadtime.setValue (latency.getLoadTime () + "");
        latencyRoot.setAttributeNode (loadtime);

        Attr createdate = doc.createAttribute ("createdate");
        if (latency.getCreateDate () != null) {
          date = formatter.format (latency.getCreateDate ());
          createdate.setValue (date);
        } else {
          createdate.setValue (null);
        }
        latencyRoot.setAttributeNode (createdate);

        Attr visitcount = doc.createAttribute ("visitcount");
        visitcount.setValue (latency.getVisitCount () + "");
        latencyRoot.setAttributeNode (visitcount);

        Attr visittime = doc.createAttribute ("visittime");
        visittime.setValue (latency.getVisitTime () + "");
        latencyRoot.setAttributeNode (visittime);

        Attr visitdate = doc.createAttribute ("visitdate");
        if (latency.getVisitDate () != null) {
          date = formatter.format (latency.getVisitDate ());
          visitdate.setValue (date);
        } else {
          visitdate.setValue (null);
        }
        latencyRoot.setAttributeNode (visitdate);
      }

      TransformerFactory transformerFactory = TransformerFactory.newInstance ();

      try {
        Transformer transformer = transformerFactory.newTransformer ();
        // System.out.println ("DOCUMENT...." + doc.toString ());
        DOMSource domSource = new DOMSource (doc);
        try {
          transformer.transform (domSource, result);
        } catch (TransformerException e) {
          _logger.error (e.getMessage ());
          throw new ReturnStatusException (e);
        }

      } catch (TransformerConfigurationException e) {
        _logger.error (e.getMessage ());
        throw new ReturnStatusException (e);
      }
      try (SQLConnection connection = getSQLConnection ()) {
        _studentDll.T_RecordClientLatency_XML_SP (connection, oppInstance.getKey (), oppInstance.getSessionKey (), oppInstance.getBrowserKey (), result.getWriter ().toString ());
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
        rootElement.appendChild (toolRoot);

        Attr attr = doc.createAttribute ("page");
        attr.setValue (toolUsed.getPage () + "");
        toolRoot.setAttributeNode (attr);

        attr = doc.createAttribute ("tooltype");
        attr.setValue (toolUsed.getType () + "");
        toolRoot.setAttributeNode (attr);

        attr = doc.createAttribute ("toolcode");
        attr.setValue (toolUsed.getCode () + "");
        toolRoot.setAttributeNode (attr);

        attr = doc.createAttribute ("count");
        attr.setValue (toolUsed.getCount () + "");
        toolRoot.setAttributeNode (attr);

      }
      TransformerFactory transformerFactory = TransformerFactory.newInstance ();

      try {
        Transformer transformer = transformerFactory.newTransformer ();
        DOMSource domSource = new DOMSource (doc);
        try {
          transformer.transform (domSource, result);
        } catch (TransformerException e) {
          _logger.error (e.getMessage ());
          throw new ReturnStatusException (e);
        } 

      } catch (TransformerConfigurationException e) {
        _logger.error (e.getMessage ());
        throw new ReturnStatusException (e);
      }
      try (SQLConnection connection = getSQLConnection ()) {

        _studentDll.T_RecordToolsUsed_SP (connection, oppKey, result.getWriter ().toString ());

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
    try (SQLConnection connection = getSQLConnection ()) {
      _studentDll.T_SubmitRequest_SP (connection, oppInstance.getSessionKey (), oppInstance.getKey (), itemPage, itemPosition, requestType, requestValue, requestParameters, requestDescription);

    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return 0;
  }

  public ReturnStatus waitForSegment (OpportunityInstance oppInstance, int segment, boolean entry, boolean exit) throws ReturnStatusException {
    ReturnStatus returnStatus = null;
    try (SQLConnection connection = getSQLConnection ()) {
      SingleDataResultSet firstResultSet = _studentDll.T_WaitForSegment_SP (connection, oppInstance.getKey (), oppInstance.getSessionKey (), oppInstance.getBrowserKey (), segment, entry, exit);
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
    try (SQLConnection connection = getSQLConnection ()) {

      SingleDataResultSet firstResultSet = _studentDll.T_ExitSegment_SP (connection, segment, oppInstance.getKey (), oppInstance.getSessionKey (), oppInstance.getBrowserKey ());
      returnStatus = ReturnStatus.readAndParse (firstResultSet);

    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return returnStatus;
  }

  public void recordComment (UUID sessionKey, long testeeKey, UUID oppKey, String comment) throws ReturnStatusException {
    try (SQLConnection connection = getSQLConnection ()) {
      _studentDll.T_RecordComment_SP (connection, sessionKey, testeeKey, comment, "GlobalNotes", oppKey);

    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }

  }

  public String getComment (UUID oppKey) throws ReturnStatusException {
    String comment = null;
    try (SQLConnection connection = getSQLConnection ()) {
      SingleDataResultSet firstResultSet = _studentDll.T_GetOpportunityComment_SP (connection, oppKey, "GlobalNotes");
      ReturnStatusException.getInstanceIfAvailable (firstResultSet);
      Iterator<DbResultRecord> records = firstResultSet.getRecords ();
      if (records.hasNext ()) {
        DbResultRecord record = records.next ();
        comment = record.<String> get ("comment");
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }

    return comment;
  }

  @Override
  public int getOpportunityNumber (UUID oppKey) throws ReturnStatusException {
    try (SQLConnection connection = getSQLConnection ()) {
      return _studentDll.getOpportunityNumber (connection, oppKey);
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
  }
}
