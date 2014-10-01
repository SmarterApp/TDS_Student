/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.tdslogger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import tds.itemscoringengine.ScoringStatus;
import tds.student.configuration.TestScoringSettings;
import tds.student.dll.test.DLLHelper;
import tds.student.sql.repository.ResponseRepositoryTest;
import AIR.Common.DB.SQLConnection;
import AIR.Common.TDSLogger.ITDSLogger;

@RunWith (SpringJUnit4ClassRunner.class)
@ContextConfiguration (locations = "/test-context.xml")
public class TDSLoggerTest {
	
	@Autowired
	DLLHelper _myDllHelper;
	
	@Autowired
	private ITDSLogger          _tdsLogger;

	private static final Logger _logger = LoggerFactory.getLogger(ResponseRepositoryTest.class);
	private SQLConnection _connection = null;
	private Boolean _preexistingAutoCommitMode = null;
	@Before
	public void setUp() throws Exception {
		try {
			_connection  = _myDllHelper.getSQLConnection();
			_preexistingAutoCommitMode = _connection.getAutoCommit();
			_connection.setAutoCommit(false);
		} catch (Exception e) {
			System.out.println("Exception in this test: " + e);
			_logger.error(e.getMessage());
			throw e;
		}
	}

	@After
	public void tearDown() throws Exception {
		try {
			_connection.rollback();
			_connection.setAutoCommit(_preexistingAutoCommitMode);
			_logger.info("All tranzactions are rollbacked");

		} catch (Exception e) {
			_connection.rollback();
			_connection.setAutoCommit(_preexistingAutoCommitMode);
			_logger.info("All tranzactions are rollbacked");
			_logger.info("Exception in the testGetNextItemGroup test: " + e);
			_logger.error(e.getMessage());
			throw e;
		}
	}

	@Test
	public void testItemScoringServiceCall()
	{
		String itemID = "12345";
		String scoreRationale ="We don't score this item";
		String status = "started";
		try{
			throwException("ItemScoringService");
		} catch(Exception ex){
			_tdsLogger.applicationError(tdsLog(itemID,"Web proxy returned NULL score."), "scoreItem", null, null);
			String message = String.format("Web proxy returned a scoring error status: '%s'.", (scoreRationale));
			_tdsLogger.applicationError(tdsLog(itemID, message),"scoreItem", null, null);
			message = String.format("Web proxy is in asynchronous mode and returned a score status of %s. It should return %s.",
					status, ScoringStatus.WaitingForMachineScore.toString());
			_tdsLogger.applicationError(tdsLog(itemID, message),"scoreItem", null, null);
			message = String.format("Web proxy is in synchronous mode but returned incorrect status of %s.", status);
			_tdsLogger.applicationError(tdsLog(itemID, message),"scoreItem", null, null);
			message = String.format("EXCEPTION = '%s'.", ex.getMessage());
			_tdsLogger.applicationError(tdsLog(itemID, message), "scoreItem", null, ex);
		}

	}
	@Test
	public void testMessageServiceCall()
	{
		boolean isAppkey = true;
		String messageID = "[1777]";
		try{
			throwException("MessageService");
		} catch(Exception ex){
	        String message = String.format ("MESSAGES: Error loading translation \"%1$b\" (%2$s).", isAppkey, messageID);
	        _tdsLogger.applicationError(message, "MessageService.load", null, ex);
		}
	}
	@Test
	public void testOpportunityServiceCall()
	{
		String status = "started";
        String error = String.format ("WARNING: When parsing the returned status from SetStatus could not understand the returned value '{0}'. Instead will use the passed in status of '{1}'. This should be reviewed.",
        		status, status);
        _tdsLogger.applicationWarn(error, "setStatus", null, null);
	}
	@Test
	public void tesPrintServiceCall()
	{
		String testKey = "ELA 13 BLA_BLA";
		String groupID = "500-3426-1009";
		String error = String.format("PrintPassageBraille: Cannot find a matching braille attachment for the test %1$s and passage %2$s.",
						testKey, groupID);
		_tdsLogger.rendererWarn(error, "printPassageBraille");
		error = String.format("PrintItemBraille: Cannot find a matching braille attachment for the test %1$s and item %2$s.",
				testKey, groupID);
		_tdsLogger.rendererWarn(error, "printItemBraille");
	}
	@Test
	public void testTestScoringServiceCall()
	{
		String status = "started";
		String reason = "testTestScoringService";
	      logError ("Cannot score this test because the SP T_GetTestForScoring returned an empty item string.", new Object[] {}, "scoreTest");
	      logError ("Test Scoring Engine returned an empty score string.", new Object[] {}, "scoreTest");
	      logDebug ("Get test for scoring - %s \" %s\"",  new Object[] { status, reason },  "scoreTest");
	}
	@Test
	public void testTimerScoringServiceCall()
	{
		String itemID = "I-500-3426-1009";
		try{
			throwException("TimerScoringService");
		} catch(Exception ex) {
	     
			_tdsLogger.applicationError("ProcessUnscoredItems: Exception while trying to get items", "processExpiredItem", null, ex);
	    
			String errorMessage = String.format ("ProcesssExpiredItem: Exception while trying to update item score %1$s", itemID);
			_tdsLogger.applicationError(errorMessage, "processExpiredItem", null, ex);
	      
			errorMessage = String.format ("ProcessUnscoredItems: Exception while trying to score item %1$s", itemID);
			_tdsLogger.applicationError(errorMessage, "processScoreableItem", null, ex);
	      
			errorMessage = String.format ("ProcessUnscoredItems: Exception while trying to update item score %1$s", itemID);
			_tdsLogger.applicationError(errorMessage, "processScoreableItem", null, ex);
	       
			errorMessage = String.format ("ProcessUnscoredItems: Return status error while trying to update item score %1$s", itemID);
			_tdsLogger.applicationError(errorMessage, "processScoreableItem", null, null);
		}
	}
	@Test
	public void testResponseRepositoryCall()
	{
  	  _tdsLogger.sqlWarn("Test: Item positions were not returned.", "insertItems");
	}
	@Test
	public void testClientManagerCall()
	{
		String clientName = "SBAC_PT_TEST";
		try{
			throwException("ClientManager");
		} catch(Exception ex){
	          String error = String.format ("The client %1$s failed to load.", clientName);
	          _tdsLogger.configFatal(error, "getClient", ex);
	     
		}  	  
	}
//	@Test
//	public void testGlobalHandlerCall()
//	{
//  	 
//	}
//	@Test
//	public void testMasterShellHandlerCall()
//	{
//  	 
//	}
//	@Test
//	public void testTestResponseHandlerCall()
//	{
//  	  
//	}
//	@Test
//	public void testTestShellHandlerCall()
//	{
//  	  
//	}
	@Test
	public void testStudentContextCall()
	{
		try{
			throwException("StudentContext");
		} catch(Exception ex){		
			_tdsLogger.applicationError(ex.getMessage (), "getFromCookie", null, ex); 
		}
	}
	@Test
	public void testMessageXmlCall()
	{
		try{
			throwException("MessageXml");
		} catch(Exception ex){		
			_tdsLogger.configError("Test: Failed to load xml messages", "MessageXml.load", ex);
		}
	}
	private void throwException(String testName) throws Exception {
	
		throw new Exception(String.format("Test exception with name: %s", testName));
	}

	// function for logging item scorer errors and use TDSLogger
	private String tdsLog(String itemID, String message) {
		// SB-507 //
		// TDSLogger.Application.Error(string.Format("ITEM SCORER ({0}): {1}",
		// responseScorable.ItemID, message));
		String error = String.format("ITEM SCORER ({%s}): {%s}",
				itemID, message);
		return error;
	};
	private void logError (String message, Object[] args, String methodName) {
		    if (TestScoringSettings.getLogErrors () != null) {
		      String formattedMessage = String.format ("TestScoring: " + message, args);
			  // SB-507
		      _tdsLogger.applicationError(formattedMessage, methodName, null, null);
		      //_logger.error (formattedMessage);
		    }
		  }
	private void logDebug (String message, Object[] args, String methodName) {
		    if (TestScoringSettings.getLogDebug () != null) {
		      String formattedMessage = String.format ("TestScoring: " + message, args);
			  // SB-507
		      _tdsLogger.applicationInfo(formattedMessage, methodName, null);
		      //_logger.error (formattedMessage);
		    }
		  }

}
