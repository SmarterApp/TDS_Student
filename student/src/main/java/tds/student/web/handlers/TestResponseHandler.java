/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.web.handlers;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;

import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.ReturnStatusException;
import TDS.Shared.Exceptions.TDSSecurityException;
import tds.blackbox.ContentRequestException;
import tds.student.services.abstractions.IItemScoringService;
import tds.student.services.data.NextItemGroupResult;
import tds.student.services.data.PageList;
import tds.student.services.data.TestOpportunity;
import tds.student.sql.data.ItemResponseUpdate;
import tds.student.sql.data.ItemResponseUpdateStatus;
import tds.student.sql.data.ServerLatency;
import tds.student.web.StudentContext;
import tds.student.web.StudentSettings;
import tds.student.web.TestManager;
import tds.student.web.handlers.TDSHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.FactoryConfigurationError;

import AIR.Common.Helpers.StopWatch;
import AIR.Common.TDSLogger.ITDSLogger;
import AIR.Common.Web.TDSReplyCode;
import AIR.Common.Web.WebHelper;
import AIR.Common.Web.Session.HttpContext;
import AIR.Common.data.ResponseData;

@Controller
@Scope ("request")
public class TestResponseHandler extends TDSHandler
{
  private static final Logger _logger = LoggerFactory.getLogger (TestResponseHandler.class);

  @Autowired
  private IItemScoringService _itemScoringService;

  @Autowired
  private StudentSettings     _studentSettings;
  
  @Autowired
  private ITDSLogger          _tdsLogger;


  // ServiceLocator.resolve<IItemScoringService>();

  // / <summary>
  // / This function is used to handle incoming response updates and provides
  // prefetch generation.
  // / </summary>

  private void UpdateResponsesTemp (HttpServletRequest request, HttpServletResponse response) throws TDSSecurityException
  {
    if (request.getContentLength () == 0)
      return;
    // TODO Shajib: following line is commented temporarily
    long timeServerReceived = System.currentTimeMillis ();

    // make sure the student is authenticated before doing anything
    checkAuthenticated ();
    try {
      URL contentXmlUrl = this.getClass ().getClassLoader ().getResource ("dummyUpdateResponse.xml");
      BufferedReader bfr = new BufferedReader (new FileReader (contentXmlUrl.getFile ()));
      StringBuffer output = new StringBuffer ();
      String line = null;
      while ((line = bfr.readLine ()) != null)
      {
        output.append (line);
      }
      bfr.close ();
      response.setContentType ("text/xml");
      response.getOutputStream ().write (output.toString ().getBytes ());
    } catch (Exception exp) {
      throw new ContentRequestException ("Error loading dummy data: " + exp.toString ());
    }
  }

  @RequestMapping (value = "Response.axd/update", produces = "application/xml")
  @ResponseBody
  private void updateResponses (HttpServletRequest request, HttpServletResponse response) throws Exception
  {
    try {
      if (request.getContentLength () == 0)
        return;
      long timeServerReceived = System.currentTimeMillis ();

      // make sure the student is authenticated before doing anything
      checkAuthenticated ();

      // get context objects
      TestOpportunity testOpp = StudentContext.getTestOpportunity ();
      // validate context
      if (testOpp == null)
        StudentContext.throwMissingException ();

      // get the last page the client has recieved responses for
      int lastPage = WebHelper.getQueryValueInt ("lastPage");

      // create server latency
      ServerLatency latency = ServerLatency.getCurrent (HttpContext.getCurrentContext ());

      /****************
       * SAVE RESPONSES
       ****************/

      // get the request information from tehe browser
      TestResponseReader responseReader = TestResponseReader.parse (request.getInputStream (), testOpp);
      /*
       * Ping
       */

      // TODO Shajib: ProxyService commented out
      /*
       * if (StudentSettings.getIsDataEntry()) { ProxyService _proxyService =
       * ServiceLocator.Resolve<ProxyService>();
       * _proxyService.Ping(testOpp.OppInstance, ProxyContext.GetProctor()); }
       */
      // If scoring is done sychronously, then score and then updateDB
      // If scoring is done asynchronously, then updateDB (with score -1 and
      // status WaitingForMachineScore) and then score
      List<ItemResponseUpdateStatus> responseResults = null;
      try {
        responseResults = _itemScoringService.updateResponses (testOpp.getOppInstance (), responseReader.getResponses ());
/*    } catch (ReturnStatusException rse) {      
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        
        response.setContentType("application/json");
        ObjectMapper mapper = new ObjectMapper();
        ResponseData<ReturnStatus> out = new ResponseData<ReturnStatus> (TDSReplyCode.ReturnStatus.getCode(), rse.getReturnStatus().getReason(), null);
        mapper.writeValue(response.getOutputStream(), out);
        return;      */
      } catch (Exception e) {	
        e.printStackTrace ();
        throw e;
      }
      // save updating responses latency
      for (ItemResponseUpdateStatus responseResult : responseResults)
      {
        latency.setDbLatency (latency.getDbLatency () + responseResult.getDbLatency ());
      }

      /****************
       * PREFETCH
       ****************/

      // get test manager
      TestManager testManager = new TestManager (testOpp);

      // if we didn't update responses then we need to validate (unless we are in
      // read only mode)
      boolean validateResponses = (responseReader.getResponses ().size () == 0);
      if (_studentSettings.isReadOnly ()){
        validateResponses = false;
      }

      // load current responses
      StopWatch dbTimer = new StopWatch ();
      dbTimer.start ();
      testManager.LoadResponses (validateResponses);
      dbTimer.stop ();
      // save loading responses latency
      // TODO Shajib: need verify this is same as dbTimer.ElapsedMilliseconds
      latency.setDbLatency (dbTimer.getTime ());
      // add item info to latency
      LoadServerLatencyItems (latency, responseReader);
      // update the test config
      // UpdateTestConfig(testManager);

      int prefetchCount = 0; // how many times prefetch occured
      int prefetchMax = testOpp.getTestConfig ().getTestLength () + 1; // max # of
                                                                       // times
                                                                       // prefetch
      // can occur (safety)

      // check if test is completed
      try {
        testManager.CheckIfTestComplete ();
      } catch (Exception e) {
        e.printStackTrace ();
      }
      _logger.info (new StringBuilder ("<<<<<<<<< Response update 7 Execution Time : ").append ((System.currentTimeMillis ()-timeServerReceived)).append (" ms. ").append(" prefetchMax: ").append (prefetchMax).append (" ThreadId: ").append (Thread.currentThread ().getId ()).toString ());
      //temp counter for debuging
      // if the test is not completed then check if prefetch is available
      while (testManager.CheckPrefetchAvailability (testOpp.getTestConfig ().getPrefetch ()))
      {
        // call adaptive algorithm to get the next item group
        NextItemGroupResult nextItemGroup = testManager.CreateNextItemGroup ();
        latency.setDbLatency (latency.getDbLatency () + nextItemGroup.getDbLatency ());
        prefetchCount++;

        // check if we exceeded prefetch max
        if (prefetchCount >= prefetchMax)
          break;

        // check if test is completed
        testManager.CheckIfTestComplete ();
      }
      _logger.info (new StringBuilder ("<<<<<<<<< Response update 8 Execution Time : ").append ((System.currentTimeMillis ()-timeServerReceived)).append (" ms. ").append(" prefetchCount: ").append (prefetchCount).append (" ThreadId: ").append (Thread.currentThread ().getId ()).toString ());
      if (prefetchCount == prefetchMax) {

      	String message = String.format (
                "PREFETCH: The # of successful prefetch attempts for this one request reached %s tries. "
                + "The # of prefetch attempts is bounded by the test length. "
                + "Prefetch was stopped and this should be reviewed."
                , testOpp.getTestConfig ().getTestLength ());
      	_tdsLogger.applicationError(message, "updateResponses", request, null);
      }

      // set latency operation type
      boolean prefetched = (prefetchCount > 0);
      latency.setOperation (prefetched ? ServerLatency.OperationType.Selection : ServerLatency.OperationType.Update);

      // new groups
      PageList pageList = testManager.GetVisiblePages (lastPage);
      /****************
       * WRITE XML
       ****************/

      // begin writing

      // TODO Shajib: Revisit following line
      // SetMIMEType (ContentType.Xml);

      TestResponseWriter responseWriter = new TestResponseWriter (response.getOutputStream ());

      // test info
      int eventID = responseReader.getEventID ();
      responseWriter.writeStart (eventID);
      responseWriter.writeSummary (testManager, testOpp, prefetched);

      // timestamps
      long timeClientSent = responseReader.getTimestamp ();
      long timeServerCompleted = System.currentTimeMillis ();
      responseWriter.writeTimestamps (timeClientSent, timeServerReceived, timeServerCompleted);
      // response updates performed
      responseWriter.writeResponseUpdates (responseResults);
      // new groups
      responseWriter.writeGroups (pageList);

      // generate the HTML for each group and write it out
      // xmlResponse.WriteContents(testOpp, itemResponseGroups);
      
      // close writing
      responseWriter.writeEnd ();
      _logger.info ("<<<<<<<<< Response update Total Execution Time : "+((System.currentTimeMillis ()-timeServerReceived)) + " ms. ThreadId: " +Thread.currentThread ().getId ());
    } catch (Exception e) {
      _logger.error (e.toString (),e);
      throw e;
    }
  }

  /*
   * /// <summary> /// Call this function to update the test config ///
   * </summary> private void UpdateTestConfig(TestManager testManager) { // get
   * test config update ItemResponses itemResponses =
   * testManager.GetResponses(); if (itemResponses == null ||
   * itemResponses.Config == null) return;
   * 
   * // check if there is a difference in the current test length vs a test
   * length we got when updating responses if (itemResponses.Config.TestLength >
   * testManager.TestOpportunity.Config.TestLength) { // update test length and
   * save testManager.TestOpportunity.Config.TestLength =
   * itemResponses.Config.TestLength;
   * StudentContext.SaveTestConfig(testManager.TestOpportunity.Config); } }
   */

  private static void LoadServerLatencyItems (ServerLatency latency, TestResponseReader responseReader)
  {
    if (responseReader.getResponses ().size () == 0)
      return;

    HashSet<String> items = new HashSet<String> ();
    HashSet<String> pages = new HashSet<String> ();

    for (ItemResponseUpdate responseUpdate : responseReader.getResponses ())
    {
      items.add (Long.toString (responseUpdate.getItemKey ()));
      pages.add (Integer.toString (responseUpdate.getPage ()));
    }

    latency.setItemList (StringUtils.join (",", items.toArray ()));
    latency.setPageList (StringUtils.join (",", pages.toArray ()));
  }

}
