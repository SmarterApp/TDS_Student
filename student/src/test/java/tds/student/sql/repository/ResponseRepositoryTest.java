/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.repository;

import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import tds.student.dll.test.DLLHelper;
import tds.student.sql.data.AdaptiveGroup;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.data.OpportunityItem.OpportunityItems;
import tds.student.sql.repository.ResponseRepository;
import AIR.Common.DB.AbstractConnectionManager;
import AIR.Common.DB.AbstractDAO;
import AIR.Common.DB.SQLConnection;
import AIR.Common.DB.SqlParametersMaps;
import AIR.Common.DB.results.DbResultRecord;
import AIR.Common.DB.results.SingleDataResultSet;
import AIR.Common.Helpers._Ref;
import AIR.test.framework.AbstractTest;
import TDS.Shared.Exceptions.ReturnStatusException;

@RunWith (SpringJUnit4ClassRunner.class)
@ContextConfiguration (locations = "/test-context.xml")

//@Configuration
//@ContextConfiguration("classpath:root-context.xml")
public class ResponseRepositoryTest   extends AbstractTest  {

	@Autowired
	@Qualifier("applicationDataSource")
	private DataSource applicationDataSource;
	
	@Autowired
	DLLHelper _myDllHelper;

	@Autowired
	ResponseRepository responseRepository;

	private static final Logger _logger = LoggerFactory.getLogger(ResponseRepositoryTest.class);
	private SQLConnection _connection = null;
	private Boolean _preexistingAutoCommitMode = null;
	//private boolean _debug = true;

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
	public final void testInsertItems() throws Exception {
		System.out.println();
		_logger.info("Test of getNextItemGroup (Connection connection, UUID oppkey, _Ref<String> errorRef) "
				+ "for AdaptiveSelector2013: ");
		System.out.println();

		try {//''
			//String OPPKEY = "a1674ef0-9042-428e-beab-9f082bdc93f8"; // This is student with 3 previous items!
			String OPPKEY = "79CAF065-40F4-43CB-9290-E1B2580AE8DB"; 

			UUID oppkey = (UUID.fromString(OPPKEY));
			_logger.info("Oppkey =  " + OPPKEY);

	    	_Ref<String> errorRef = new _Ref<>();
	    	
			
			OpportunityInstance oppInstance = new OpportunityInstance(oppkey, 
//					UUID.fromString("C60BDD2F-D4C4-4011-93BA-E046A4D3E794"),
//					UUID.fromString("A222C529-6F4A-4CDD-B2F2-8FC4A9C39E88"));
			UUID.fromString("6BEDFAFD-AC1E-495F-BAB3-AA577A3530C1"), // sessionkey
			UUID.fromString("DBB38430-9499-4BF3-BB6D-FDC7D9AA090F")); // browserkey

			AdaptiveGroup adaptiveGroup = new AdaptiveGroup ();
		    adaptiveGroup.setPage (1);
		    adaptiveGroup.setBankKey (0);
		    adaptiveGroup.setGroupID ("I-200-9727");
		    adaptiveGroup.setSegmentPosition (1);
		    adaptiveGroup.setSegmentID ("CAT-M3-ONON-S1-A1-MATH-3");
		    adaptiveGroup.setNumItemsRequired (0);
		    
		    try{
		    	OpportunityItems oppItems = responseRepository.insertItems(oppInstance, adaptiveGroup);
		    	
		        
		        final String SQL_QUERY1 = "select *  from ${ArchiveDB}.systemerrors "
		        		+ " where _fk_session = ${sessionID} and _fk_testopportunity = ${oppkey}";

		        SqlParametersMaps parms1 = (new SqlParametersMaps ()).put ("sessionID", oppInstance.getSessionKey())
		        		.put ("oppkey", oppInstance.getKey());
		        SingleDataResultSet result = _myDllHelper.executeStatement (_connection, _myDllHelper.fixDataBaseNames (SQL_QUERY1), parms1, true).getResultSets ().next ();
		        DbResultRecord record = (result.getCount () > 0 ? result.getRecords ().next () : null);
		        
		        if (record != null) {
		          System.out.println(String.format("Application: %s", record.<String> get ("application")));
		          System.out.println(String.format("Procname: %s", record.<String> get ("procname")));
		          System.out.println(String.format("ErrorMessage: %s",  record.<String> get ("errormessage")));
		          System.out.println(String.format("ServerID: %s",  record.<String> get ("serverid")));
		          System.out.println(String.format("ClientName: %s",  record.<String> get ("clientname")));
		          System.out.println(String.format("DBName: %s",  record.<String> get ("dbname")));
		        } else
		        {
			      System.out.println(String.format("Record is empty: no records in ${ArchiveDB}.systemerrors"));
		        }

		    } catch (Exception e)
		    {
		    	_logger.error(e.getMessage());
		    	System.out.println("Error: " + e.getMessage());
		    }
			
	        if(errorRef.get() != null  && !errorRef.get().isEmpty())
	        {
	        	 _logger.error (errorRef.get());
	        	 System.out.println(String.format(errorRef.get()));
	        	 throw new ReturnStatusException (errorRef.get());
	        }
	        
//			// TODO delete System.out.println() !!!
//			System.out.println(String.format("groupID: %s", itemGr.groupID));
//			System.out.println(String.format("itemsRequired: %s", itemGr.getNumRequired()));
//			System.out.println(String.format("maxReqItems: %s", itemGr.getTestLength()));
//			_logger.info(String.format("groupID: %s", itemGr.groupID));
//			_logger.info(String.format("itemsRequired: %s", itemGr.getNumRequired()));
//			_logger.info(String.format("maxReqItems: %s", itemGr.getTestLength()));

//			List<TestItem> items = itemGr.getItems();
//			int itemsNumber = items.size();
//			System.out.println(String.format("Number of items: %s",
//					itemsNumber));
//			_logger.info(String.format("Number of items: %s", itemsNumber));
			
	      } catch (Exception e) {
			System.out.println("Exception in the testGetNextItemGroup test: "
					+ e);
			if (e instanceof java.lang.NullPointerException) {
				System.out.println("Stack: " + e.getStackTrace());
			}
			_logger.error(e.getMessage());
			throw e;
		} catch (AssertionError error) {
			System.out.println("AssertionError in the testGetNextItemGroup test: " + error);
			_logger.error(error.getMessage());
			throw error;
		}
	}
	
}

