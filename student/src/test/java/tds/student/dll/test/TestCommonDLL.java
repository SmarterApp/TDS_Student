/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.dll.test;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import tds.dll.api.ICommonDLL;
import tds.dll.api.IStudentDLL;
import AIR.Common.DB.AbstractConnectionManager;
import AIR.Common.DB.AbstractDAO;
import AIR.Common.DB.DataBaseTable;
import AIR.Common.DB.SQLConnection;
import AIR.Common.DB.SqlParametersMaps;
import AIR.Common.DB.results.DbResultRecord;
import AIR.Common.DB.results.MultiDataResultSet;
import AIR.Common.DB.results.SingleDataResultSet;
import AIR.Common.Helpers.CaseInsensitiveMap;
import AIR.Common.Helpers._Ref;
import AIR.Common.Sql.AbstractDateUtilDll;
import AIR.test.framework.AbstractTest;
import TDS.Shared.Exceptions.ReturnStatusException;

@Configuration
@ContextConfiguration("classpath:test-context.xml")
public class TestCommonDLL extends AbstractTest {
	private static final Logger _logger = LoggerFactory.getLogger(TestCommonDLL.class);
	
	@Autowired
	@Qualifier("iCommonDLL")
	ICommonDLL _dll = null;
	
	@Autowired
	DLLHelper _myDllHelper;

	@Autowired
	@Qualifier("iStudentDLL")
	IStudentDLL _dllStudent;
	
	@Autowired
	private AbstractDateUtilDll  _dateUtil;
	
	@Autowired
	private AbstractConnectionManager abstractConnectionManager;
	
  // select * from dbo._SplitAccomCodes ('Hawaii',
  // '(Hawaii)HSA_OP-Mathematics-8-Fall-2011-2012',
  // 'MA:A402;MA:A501;SS:A208;SS:A204;SS:A307;SS:A402;SS:A104;SS:A302;SS:A212;SS:A213;SS:A107;SS:A308;SS:A501;SS:A103;SS:A401;SS:A105;SS:A303;SS:A101;SS:A404;SC:ENU;RE:ENU-Braille;WR:ENU;SS:TDS_TTS0;MA:TDS_TTS_Item;SC:TDS_TTS0')
  //@Test
  public void text_SplitAccomCodes_FN () throws SQLException, ReturnStatusException {

    String clientname = "Hawaii";// "Oregon";
    String testkey = "(Hawaii)HSA_OP-Mathematics-8-Fall-2011-2012";// "(Oregon)OAKS-Math-3-Fall-2012-2013";
    // String accoms =
    // "NF1;MA:12345|kuku6789;123:abc;123456789012345678901234567890123456789012345678901234567890;1234567890123MA:123;TDS_PS_L3;TDS_ExpandablePassages1;TDS_ItemTypeExcl_GI;";

    // String accoms =
    // "TDS_T1|TDS_TTX_A203|TDS_LR0|TDS_FT_San-Serif|TDS_PS_L4|TDS_ExpandablePassages1|"
    // +
    // "TDS_TTSPause1|TDS_ParentExempt0|TDS_TTS_Inst|TDS_MfR1|TDS_PoD_Stim&TDS_PoD_Item|TDS_BP0|TDS_F_S12|"
    // +
    // "A003|A008|A016|A020|A021|TDS_Highlight1|ENU|TDS_SS0|TDS_TTSAA_Volume&TDS_TTSAA_Pitch|TDS_ST1|TDS_CC0|";

    String accoms = "MA:A402;MA:A501;SS:A208;SS:A204;SS:A307;SS:A402;SS:A104;SS:A302;SS:A212;SS:A213;SS:A107;SS:A308;SS:A501;SS:A103;SS:A401;SS:A105;SS:A303;SS:A101;SS:A404;SC:ENU;RE:ENU-Braille;WR:ENU;SS:TDS_TTS0;MA:TDS_TTS_Item;SC:TDS_TTS0";
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      DataBaseTable tbl = _dll._SplitAccomCodes_FN (connection, clientname, testkey, accoms);

      final String SQL_QUERY = "select idx, code from ${tblName}";
      Map<String, String> par = new HashMap<String, String> ();
      par.put ("tblName", tbl.getTableName ());

      SingleDataResultSet result = _myDllHelper.executeStatement (connection, _myDllHelper.fixDataBasenames (SQL_QUERY, par), null, false).getResultSets ().next ();
      Iterator<DbResultRecord> records = result.getRecords ();
      assertTrue(result.getCount () == 3);
      int i = 1;
      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        _logger.info (String.format ("code: %s", record.<String> get ("code")));
        _logger.info (String.format ("idx: %d", record.<Integer> get ("idx")));
        assertTrue(i == record.<Integer> get ("idx"));
        if (i == 1)
          assertTrue("A402".equalsIgnoreCase (record.<String> get ("code")));
        else if (i == 2)
          assertTrue("A501".equalsIgnoreCase (record.<String> get ("code")));
        else if (i == 3)
          assertTrue("TDS_TTS_Item".equalsIgnoreCase (record.<String> get ("code")));
        i++;
      }
    }
  }


	@Test
	@Ignore("Autowire issues")
	public void testBuildTable_FN() throws SQLException, ReturnStatusException {
		try (SQLConnection connection = abstractConnectionManager.getConnection()) {
			Date starttime = null;
			Date now = null;
			Long diff = 0L;

			String theLine = "line1, line2, line3, line4, asdasdasdasdasdas, asdasdasdasdasdadasdasdadasdadadadaasdasdasdad, asdadadasd, asdadasdasdasdasd";
			starttime = new Date();
			DataBaseTable tbl = _dll._BuildTable_FN(connection, "xxx", theLine,
					",");
			now = new Date();
			diff = now.getTime() - starttime.getTime();
			System.out.println(String.format("BuildTable latency: %d millisec",
					(long) diff));
			final String query = "select idx, record from ${tblname}";
			Map<String, String> unpar = new HashMap<>();
			unpar.put("tblname", tbl.getTableName());
			SingleDataResultSet res = _myDllHelper
					.executeStatement(connection,
							_myDllHelper.fixDataBaseNames(query, unpar), null,
							false).getResultSets().next();
			Iterator<DbResultRecord> records = res.getRecords();
			while (records.hasNext()) {
				DbResultRecord record = records.next();
				_logger.info(String.format("idx: %d,  record: %s",
						record.<Integer> get("idx"),
						record.<String> get("record")));
			}
			connection.dropTemporaryTable(tbl);
		}
	}
  // declare @error varchar(200);
  // exec _UpdateOpportunityAccommodations
  // '5A68DF75-3C91-4414-88AE-B33EA48E571E', 0,
  // 'TDS_BT0|TDS_PS_L0|TDS_Highlight1|TDS_TTSAA_Volume&TDS_TTSAA_Pitch&TDS_TTSAA_Rate',
  // 30, 0, 0, @error output, 1
  // select @error;

  // test code by Udaya Kommineni
   //@Test
  public void test_UpdateOppAccoms_SP () throws ReturnStatusException, SQLException {
    UUID oppKey = UUID.fromString ("5A68DF75-3C91-4414-88AE-B33EA48E571E");// ("FCEC4C40-0556-42D0-BF42-7A64C491AD56");
    int segment = 0;
    String accoms = "TDS_BT0|TDS_PS_L0|TDS_Highlight1|TDS_TTSAA_Volume&TDS_TTSAA_Pitch&TDS_TTSAA_Rate";// "TDS_BP1";
    int isStarted = 30;
    boolean approved = true;
    boolean restoreRTS = false;
    int debug = 0;
    Long diff = 0L;
    Date starttime = null;
    Date now = null;

    _Ref<String> error = new _Ref<String> ();
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      starttime = new Date();
      MultiDataResultSet resultsets = _dll._UpdateOpportunityAccommodations_SP (connection, oppKey, segment, accoms, isStarted, approved, restoreRTS, error, debug);
      now = new Date();
      diff = now.getTime () - starttime.getTime ();
      System.out.println (String.format ("_UpdateOpportunityAccommodations latency: %d millisec", (long) diff));

      _logger.info (String.format ("Error: %s", error.get ()));
      assertTrue(error.get () == null);
      // resultsets is not empty only in case debug parameter was turned on
      if (resultsets.getUpdateCount () > 0) {
        Iterator<SingleDataResultSet> iterator = resultsets.getResultSets ();
        int resultIdx = 0;
        while (iterator.hasNext ()) {

          SingleDataResultSet set = iterator.next ();

          if (resultIdx == 0) {
            assertTrue(set.getCount () == 1);
            DbResultRecord record = set.getRecords ().next ();
            if (record != null) {
              _logger.info (String.format ("segment: %d", record.<Integer> get ("segment")));
              _logger.info (String.format ("clientname: %s", record.<String> get ("clientname")));
              _logger.info (String.format ("testkey: %s", record.<String> get ("testkey")));
              _logger.info (String.format ("accoms: %s", record.<String> get ("accoms")));

            }
            _logger.info (String.format ("Total no: of records -- %d", set.getCount ()));
          }
          if (resultIdx == 1) {

            Iterator<DbResultRecord> iteratorRec = set.getRecords ();
            assertTrue(set.getCount () == 4);
            while (iteratorRec.hasNext ()) {
              _logger.info ("===============_SplitAccomCodes Record=======================");
              DbResultRecord record = iteratorRec.next ();
              _logger.info (String.format ("idx: %d", record.<Integer> get ("idx")));
              _logger.info (String.format ("Code : %s ", record.<String> get ("code")));
            }
            _logger.info (String.format ("Total no: of records -- %d", set.getCount ()));
          }
          if (resultIdx == 2) {
            assertTrue(set.getCount () == 4);
            Iterator<DbResultRecord> iteratorRec = set.getRecords ();
            while (iteratorRec.hasNext ()) {
              DbResultRecord record = iteratorRec.next ();
              _logger.info ("==================Records=======================");
              _logger.info (String.format ("AccType: %s", record.<String> get ("AccType")));
              _logger.info (String.format ("AccCode: %s", record.<String> get ("AccCode")));
              _logger.info (String.format ("AccValue: %s", record.<String> get ("AccValue")));
              _logger.info (String.format ("AllowChange: %s", record.<Boolean> get ("AllowChange")));
              _logger.info (String.format ("studentControl: %s", record.<Boolean> get ("StudentControl")));
              _logger.info (String.format ("IsDefault : %s", record.<Boolean> get ("IsDefault")));
              _logger.info (String.format ("IsSelectable: %s", record.<Boolean> get ("IsSelectable")));
              _logger.info (String.format ("ValCount: %d", record.<Integer> get ("ValCount")));
            }
            _logger.info (String.format ("Total no: of records -- %d", set.getCount ()));

          }
          if (resultIdx == 3) {
            assertTrue(set.getCount () == 4);
            Iterator<DbResultRecord> iteratorRec = set.getRecords ();
            while (iteratorRec.hasNext ()) {
              DbResultRecord record = iteratorRec.next ();
              _logger.info ("==================Records=======================");
              _logger.info (String.format ("atype: %s", record.<String> get ("atype")));
              _logger.info (String.format ("acode: %s", record.<String> get ("acode")));
              _logger.info (String.format ("avalue: %s", record.<String> get ("avalue")));
              _logger.info (String.format ("allow: %s", record.<Boolean> get ("allow")));
              _logger.info (String.format ("control: %s", record.<Boolean> get ("control")));
              _logger.info (String.format ("recordUsage: %s", record.<Boolean> get ("recordUsage")));
              _logger.info (String.format ("isDefault : %s", record.<Boolean> get ("isDefault")));
              _logger.info (String.format ("isSelectable: %s", record.<Boolean> get ("isSelectable")));
              _logger.info (String.format ("ValCount: %d", record.<Integer> get ("ValCount")));
            }
            _logger.info (String.format ("Total no: of records -- %d", set.getCount ()));
          }
          resultIdx++;
        }
      }
    }
  }


  //@Test
  public void test_UpdateOppAccoms_SP1 () throws ReturnStatusException, SQLException {
    UUID oppKey = UUID.fromString ("FCEC4C40-0556-42D0-BF42-7A64C491AD56");
    int segment = 0;
    String accoms = "TDS_BP1";
    int isStarted = 30;
    boolean approved = true;
    boolean restoreRTS = false;
    int debug = 0;
    _Ref<String> error = new _Ref<String> ();
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      _dll._UpdateOpportunityAccommodations_SP (connection, oppKey, segment, accoms, isStarted, approved, restoreRTS, error, debug);
      _logger.info (String.format ("error: %s ", error.get ()));
      assertTrue(error.get () == null);
    }
  }
  //@Test
  public void testClientTestAccoms_FN () throws SQLException, ReturnStatusException {
    String clientName = "Delaware";
    String testId = "DCAS-EOC-ALGEBRAII-Mathematics-12";
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      DataBaseTable table = _dll.ClientTestAccommodations_FN (connection, clientName, testId);

      final String SQL_QUERY = "select * from ${tblName}";
      Map<String, String> par = new HashMap<String, String> ();
      par.put ("tblName", table.getTableName ());
      SingleDataResultSet result = _myDllHelper.executeStatement (connection, _myDllHelper.fixDataBasenames (SQL_QUERY, par), null, false).getResultSets ().next ();
      Iterator<DbResultRecord> records = result.getRecords ();
      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        _logger.info ("===================Record=======================");
        _logger.info (String.format ("segment: %d", record.<Integer> get ("Segment")));
        _logger.info (String.format ("AccType: %s", record.<String> get ("AccType")));
        _logger.info (String.format ("AccValue: %s", record.<String> get ("AccValue")));
        _logger.info (String.format ("AccCode: %s", record.<String> get ("AccCode")));
        _logger.info (String.format ("AllowCombine: %s", record.<Boolean> get ("AllowCombine")));
        _logger.info (String.format ("IsDefault : %s", record.<Boolean> get ("IsDefault")));
        _logger.info (String.format ("AllowChange: %s", record.<Boolean> get ("AllowChange")));
        _logger.info (String.format ("IsSelectable: %s", record.<Boolean> get ("IsSelectable")));
        _logger.info (String.format ("IsVisible: %s", record.<Boolean> get ("IsVisible")));
        _logger.info (String.format ("studentControl: %s", record.<Boolean> get ("StudentControl")));
        _logger.info (String.format ("ValCount: %d", record.<Integer> get ("ValCount")));
        _logger.info (String.format ("DependsOnToolType: %s", record.<String> get ("DependsOnToolType")));
      }
      _logger.info ("Total no: of records -- " + result.getCount ());
      assertTrue(result.getCount () == 26);
    }
  }

  //@Test
  public void testItemFile_FN () throws ReturnStatusException, SQLException {
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      long bankkey = 148;
      long itemkey = 100006;
      String path = _dll.ITEMBANK_ItemFile_FN (connection, bankkey, itemkey);
      _logger.info (String.format ("ItemFile path -- : %s", path));
      assertTrue( "D:\\DataFiles\\BB_Files\\tds_airws_org\\TDSCore_2012-2013\\Bank-148\\Items\\Item-148-100006\\item-148-100006.xml".equalsIgnoreCase (path));
    }
  }

  //@Test
  public void test_P_FormatAccommodations_FN () throws SQLException, ReturnStatusException {
    UUID oppKey = UUID.fromString ("9CC6B36B-6A38-436D-9EDB-00010D25F2A7");
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      String result = _dll.P_FormatAccommodations_FN (connection, oppKey);
      _logger.info (String.format ("Accommodations result  -- : %s", result));

      assertTrue("Accommodation Codes: None | Block Pausing: Block pausing with unanswered items | Calculator: Standard | Color Choices: None | Expandable Passages: Expandable Passages On | Font Size: 12pt | Font Type: Verdana | Highlight: True | Item Score Report: Summarize Scores And Allow Viewing Responses | Language: English | Mark for Review: True | Print Size: No default zoom applied | Strikethrough: True | TTS: Instructions and Items | TTS Audio Adjustments: Allow TTS Volume and Pitch Adjustments | TTS Pausing: TTS Pausing On | TTX Business Rules: Standard Text-to-Speech"
          .equalsIgnoreCase (result));
    }
  }


	// declare @errmsg varchar(200);
	// exec _FormatMessage 'Minnesota', 'ESN', 'TestShell.aspx', 'ActionError',
	// @errmsg output, null, ',', '--ANY--', '--ANY--'
	// select @errmsg
	@Test
	@Ignore("Autowire issues")
	public void test_FormatMessage_SP1() throws SQLException,
			ReturnStatusException {
		try (SQLConnection connection = abstractConnectionManager.getConnection()) {
			_Ref<String> errmsg = new _Ref<String>();
			String clientname = "Minnesota";
			String language = "ESN";
			String context = "TestShell.aspx";
			String appkey = "ActionError";
			String argstring = null;
			Character delimiter = ',';
			String subject = "--ANY--";
			String grade = "--ANY--";

			_dll._FormatMessage_SP(connection, clientname, language, context,
					appkey, errmsg, argstring, delimiter, subject, grade);
		 assertTrue(errmsg.get() != null);
		 assertTrue("ActionError [-----]".equals(errmsg.get()));
			_logger.info(String.format("Formatted msg: %s", errmsg.get()));
		}
	}

	// declare @errmsg varchar(200);
	// exec _FormatMessage 'Oregon', 'ENU', '_CanOpenTestOpportunity', 'Your
	// next test opportunity is not yet available.',
	// @errmsg output, '2013-10-16'
	// select @errmsg
	@Test
	@Ignore("Autowire issues")
	public void test_FormatMessage_SP2() throws SQLException,
			ReturnStatusException {
		try (SQLConnection connection = abstractConnectionManager.getConnection()) {

			Date dateCompleted = _dateUtil.getDate(connection);

			String argstring = dateCompleted.toString();
			_Ref<String> errmsg = new _Ref<String>();
			String clientname = "Oregon";
			String language = "ENU";
			String context = "_CanOpenTestOpportunity";
			String appkey = "Your next test opportunity is not yet available.";

			_dll._FormatMessage_SP(connection, clientname, language, context,
					appkey, errmsg, argstring);
		 assertTrue(errmsg.get() != null);

		 // the message includes the specific datetime, so we will check that the code is the right code
		 // example message: You cannot take this test until Wed Jan 04 07:15:44 PST 2017. [10211]
		 assertTrue("You cannot take this test now.   This test will not be available until tomorrow. [10211]"
				 .endsWith("[10211]"));
			_logger.info(String.format("Formatted msg: %s", errmsg.get()));
		}
	}

	// select dbo.TDSCONFIGS_TDS_GetMessagekey ('Minnesota','ScoreEntry',
	// 'ClientSide','TestShell.aspx','ActionError',
	// 'ESN', '--ANY--', '--ANY--')
	@Test
	@Ignore("Autowire issues")
	public void testTDSCONFIGS_TDS_GetMessagekey_FN() throws SQLException,
			ReturnStatusException {
		try (SQLConnection connection = abstractConnectionManager.getConnection()) {

			String clientname = "Minnesota";
			String language = "ESN";
			String context = "TestShell.aspx";
			String appkey = "ActionError";
			String subject = "--ANY--";
			String grade = "--ANY--";
			String application = "ScoreEntry";
			String contextType = "ClientSide";
			String msg = _dll.TDS_GetMessagekey_FN(connection, clientname,
					application, contextType, context, appkey, language, grade,
					subject);

		 assertTrue(msg != null);
			_logger.info(String.format("Formatted msg: %s", msg));
		}
	}

	@Test
	@Ignore("Autowire issues")
	public void test_ReturnError_SP() throws SQLException,
			ReturnStatusException {

		String procname = "abc";
		String appkey = "ActionError";
		String argstring = null;
		UUID oppkey = UUID.randomUUID();
		String context = "TestShell.aspx";
		String status = "failed";
		String client = "Minnesota";

		try (SQLConnection connection = abstractConnectionManager.getConnection()) {
			SingleDataResultSet result = _dll._ReturnError_SP(connection,
					client, procname, appkey, argstring, oppkey, context,
					status);
			DbResultRecord record = result.getCount() > 0 ? result.getRecords()
					.next() : null;
		 assertTrue(record != null);
			if (record != null) {
				_logger.info(String.format("Status: %s",
						record.<String> get("status")));
				_logger.info(String.format("Reason: %s",
						record.<String> get("reason")));
				_logger.info(String.format("Context: %s",
						record.<String> get("context")));
				_logger.info(String.format("Appkey: %s",
						record.<String> get("appkey")));

			}
		}
	}

	@Test
	@Ignore("Autowire issues")
	public void test_ReturnError_SP2() throws SQLException,
			ReturnStatusException {

		String clientname = "Oregon";
		String procname = "T_GetSession"; // need to be a stored proc that is
											// configured to use this error msg
		Date dateBegin = new Date();
		String msgarg = new SimpleDateFormat("MM/dd/yyyy").format(dateBegin);
		String msgstatus = "denied";
		String msgkey = "The testing session starts on {0}. For further assistance, please check with your test administrator.";
		try (SQLConnection connection = abstractConnectionManager.getConnection()) {
			SingleDataResultSet result = _dll
					._ReturnError_SP(connection, clientname, procname, msgkey,
							msgarg, null, null, msgstatus);

			DbResultRecord record = result.getCount() > 0 ? result.getRecords()
					.next() : null;
		 assertTrue(record != null);
			if (record != null) {
				_logger.info(String.format("Status: %s",
						record.<String> get("status")));
				_logger.info(String.format("Reason: %s",
						record.<String> get("reason")));
				_logger.info(String.format("Context: %s",
						record.<String> get("context")));
				_logger.info(String.format("Appkey: %s",
						record.<String> get("appkey")));

			}
		}
	}

	@Test
	@Ignore("Autowire issues")
	public void test_RecordSystemError_SP() throws SQLException,
			ReturnStatusException {

		try (SQLConnection connection = abstractConnectionManager.getConnection()) {
			String proc = "";
			String msg = "myErrorMsg";
			Long testee = 2345L;
			String test = "myTest";
			Integer opportunity = 2;
			String application = null;
			String clientIP = "1.2.3.4";
			UUID applicationContextID = UUID.randomUUID();
			String stackTrace = null;
			UUID testoppkey = UUID.randomUUID();
			String clientname = "Minnesota";

			_dll._RecordSystemError_SP(connection, proc, msg, testee, test,
					opportunity, application, clientIP, applicationContextID,
					stackTrace, testoppkey, clientname);

		}
	}

	// declare @session uniqueidentifier;
	// declare @browser uniqueidentifier;
	// set @session = newid();
	// set @browser =newid();
	// select dbo.ValidateProctorSession (1, @session, @browser);
	@Test
	@Ignore("Autowire issues")
	public void testValidateProctorSession() throws SQLException,
			ReturnStatusException {

		Long proctorkey = 1L;
		UUID sessionkey = UUID.randomUUID();
		UUID browserkey = UUID.randomUUID();

		try (SQLConnection connection = abstractConnectionManager.getConnection()) {
			String reason = _dll.ValidateProctorSession_FN(connection,
					proctorkey, sessionkey, browserkey);
		 assertTrue(reason != null);
		 assertTrue("The session is closed.".equalsIgnoreCase(reason));
			_logger.info(String.format("Reson: %s", reason));
		}
	}

	@Test
	@Ignore("Autowire issues")
	public void testAuditOpportunities() throws SQLException,
			ReturnStatusException {
		String clientname = "Oregon";

		try (SQLConnection connection = abstractConnectionManager.getConnection()) {

			Integer auditOpportunities = _dll.AuditOpportunities_FN(connection,
					clientname);
			Integer auditSessions = _dll.AuditSessions_FN(connection,
					clientname);
			_logger.info(String.format("Opportunities. %d,  Sessions: %d",
					auditOpportunities, auditSessions));
		}
	}

	// select dbo.CanScoreOpportunity('C5FAD533-8F95-4ECF-AB79-02A8D2AB3C40')
	@Test
	@Ignore("Seed data needed")
	public void testCanScoreOpportunity() throws SQLException,
			ReturnStatusException {
		// UUID oppkey = UUID.fromString
		// ("7483AC2F-F74F-4D6C-9EBD-1E4ADC89859E");
		// UUID oppkey = UUID.fromString
		// ("BFC5216A-FD8A-4590-84AF-68C1962E494A");
		UUID oppkey = UUID.fromString("C5FAD533-8F95-4ECF-AB79-02A8D2AB3C40");
		try (SQLConnection connection = abstractConnectionManager.getConnection()) {
			String msg = _dll.CanScoreOpportunity_FN(connection, oppkey);
		 assertTrue(msg != null);
		 assertTrue("Unofficial score only".equalsIgnoreCase(msg));
			_logger.info(String.format("CanScoreOpporrtinity msg: %s", msg));
		}
	}

	// select dbo._CanChangeOppStatus ('pending', 'scored')
	@Test
	@Ignore("Autowire issues")
	public void test_CanChangeOppStatus() throws SQLException,
			ReturnStatusException {
		String oldstatus = "pending";
		String newstatus = "scored";

		try (SQLConnection connection = abstractConnectionManager.getConnection()) {
			String msg = _dll._CanChangeOppStatus_FN(connection, oldstatus,
					newstatus);
		 assertTrue(msg != null);
		 assertTrue("Cannot change opportunity from pending to scored"
					.equalsIgnoreCase(msg));
			_logger.info(String.format("CanChangeOppStatus msg: %s", msg));
		}
	}

	// exec _OnStatus_Scored 'BFC5216A-FD8A-4590-84AF-68C1962E494A'
	@Test
	@Ignore("Seed data needed")
	public void test_OnStatus_Scored_SP() throws ReturnStatusException,
			SQLException {
		UUID oppkey = UUID.fromString("BFC5216A-FD8A-4590-84AF-68C1962E494A");

		try (SQLConnection connection = abstractConnectionManager.getConnection()) {
			SingleDataResultSet result = _dll._OnStatus_Scored_SP(connection,
					oppkey);
		 assertTrue(result == null);
			if (result != null) {
				DbResultRecord record = result.getCount() > 0 ? result
						.getRecords().next() : null;
				if (record != null) {
					String status = record.<String> get("status");
					String reason = record.<String> get("reason");
					_logger.info(String.format("Status: %1$s,  reason: %2$s",
							status, reason));
				}
			}
		}
	}

	@Test
	@Ignore("Autowire issues")
	public void test_OnStatus_Paused_SP() throws ReturnStatusException,
			SQLException {
		try (SQLConnection connection = abstractConnectionManager.getConnection()) {
			UUID oppkey = UUID
					.fromString("4308172C-7FCF-42A9-8CE7-005299111EED"); // got
																			// from
																			// Sai
			String prevStatus = "started";

			_dll._OnStatus_Paused_SP(connection, oppkey, prevStatus);
			_logger.info("done");
		}
	}

	@Test
	@Ignore("Autowire issues")
	public void test_RecordBPSatisfaction_SP() throws ReturnStatusException,
			SQLException {
		try (SQLConnection connection = abstractConnectionManager.getConnection()) {
			UUID oppkey = UUID
					.fromString("9F1DCD39-111A-4417-8464-00A0E1291E4D"); // ("5E2A95D0-B9A5-441A-B38A-F4B479911213");
																			// ("04EFD6B8-2223-4565-B743-50B9424B5419");

			_dll._RecordBPSatisfaction_SP(connection, oppkey);
		}
	}

	@Test
	@Ignore("Seed data needed")
	public void test_OnStatus_Completed_SP() throws ReturnStatusException,
			SQLException {
		try (SQLConnection connection = abstractConnectionManager.getConnection()) {
			UUID oppkey = UUID
					.fromString("8DAA4B41-0B74-4415-9A98-BCB45EAF2BCB");

			_dll._OnStatus_Completed_SP(connection, oppkey);
		}
	}

	// old status should have been completed or scored
	// run script:
	// update TetsOpportunity set status = 'completed' where _key =
	// '8FED0410-8818-406A-803C-A6D04B9B462A'
	// exec SetOpportunityStatus '8FED0410-8818-406A-803C-A6D04B9B462A',
	// 'submitted', 0, null, null
	@Test
	@Ignore("Seed data needed")
	public void testSetOpportunityStatus_SP() throws ReturnStatusException,
			SQLException {
		try (SQLConnection connection = abstractConnectionManager.getConnection()) {
			UUID oppkey = UUID
					.fromString("8FED0410-8818-406A-803C-A6D04B9B462A");// ("8DAA4B41-0B74-4415-9A98-BCB45EAF2BCB");
			String status = "submitted";
			Boolean suppressReport = false;

			// old status should have been completed or scored
			final String query = "update testopportunity set status = 'completed' where _key = ${oppkey}";
			SqlParametersMaps parms = (new SqlParametersMaps()).put("oppkey",
					oppkey);
			_myDllHelper.executeStatement(connection, query, parms, false);

			SingleDataResultSet result = _dll.SetOpportunityStatus_SP(
					connection, oppkey, status, suppressReport, null, null);
			// Note that result will be null if suppressReport is true
		 assertTrue(result != null);

			if (result.getRecords().hasNext()) {
				DbResultRecord record = result.getRecords().next();
			 assertTrue("submitted".equalsIgnoreCase(record
						.<String> get("status")));
			 assertTrue(record.<String> get("reason") == null);
				_logger.info(String.format("Status: %s, reason: %s",
						record.<String> get("status"),
						record.<String> get("reason")));
			}
		}
	}

	// select dbo.ScoreByTDS ('Oregon', 'abc')
	@Test
	@Ignore("Autowire issues")
	public void testScoreByTDS_FN() throws ReturnStatusException, SQLException {
		String clientName = "Oregon";
		String testId = "abc";
		try (SQLConnection connection = abstractConnectionManager.getConnection()) {
			final String query1 = "select  isApproved from testeeaccommodations limit 1";
			SingleDataResultSet res = _myDllHelper
					.executeStatement(connection, query1, null, false)
					.getResultSets().next();
			DbResultRecord rec = res.getRecords().next();
			Boolean isApproved = rec.<Boolean> get("isApproved");
			_logger.info(isApproved.toString());

			Boolean score = _dll.ScoreByTDS_FN(connection, clientName, testId);
		 assertTrue(false == score);

			List<CaseInsensitiveMap<Object>> resultList = new ArrayList<CaseInsensitiveMap<Object>>();
			CaseInsensitiveMap<Object> rcd = new CaseInsensitiveMap<Object>();
			rcd.put("testScore", score);
			resultList.add(rcd);
			SingleDataResultSet rs = new SingleDataResultSet(resultList);
			rec = rs.getRecords().next();
			Boolean testScore = rec.<Boolean> get("testScore");

		}
	}

	@Test
	@Ignore("Autowire issues")
	public void testDaysDiff() throws Exception {
		String toStr = "2014-01-01 00:01:01";
		String fromStr = "2012-12-31 23:59:59";

		// Date to = new SimpleDateFormat
		// (AbstractDateUtilDll.DB_DATETIME_FORMAT).parse (toStr);
		// Date from = new SimpleDateFormat
		// (AbstractDateUtilDll.DB_DATETIME_FORMAT).parse (fromStr);
		// long datediff = _myDllHelper.daysDiff (from, to);

		try (SQLConnection con = abstractConnectionManager.getConnection()) {
			Date now = _dateUtil.getDate(con);
			String nowStr = new SimpleDateFormat(
					AbstractDateUtilDll.DB_DATETIME_FORMAT_MS_PRECISION)
					.format(now);
			_logger.info(nowStr);
		}

	}

	// Open followed by Pause
	// exec P_OpenSession 'DB5AACDA-D724-4F49-98B1-C89E5267AB32', 12, 0,
	// '505497A1-3D19-4231-91B7-C25751D0BC41'
	// exec P_PauseSession 'DB5AACDA-D724-4F49-98B1-C89E5267AB32', 21545,
	// '505497A1-3D19-4231-91B7-C25751D0BC41', 'closed', 1
	@Test
	@Ignore("Seed data needed")
	public void testP_PauseSession_SP() throws ReturnStatusException,
			SQLException {

		UUID sessionKey = UUID
				.fromString("DB5AACDA-D724-4F49-98B1-C89E5267AB32");
		Long proctorKey = 21545L;
		UUID browserKey = UUID
				.fromString("505497A1-3D19-4231-91B7-C25751D0BC41");
		String reason = "closed";
		Boolean report = true;
		try (SQLConnection connection = abstractConnectionManager.getConnection()) {
			// first make sure session is open
			Boolean suppressReport = true;
			_dllStudent.P_OpenSession_SP(connection, sessionKey, 12,
					suppressReport, browserKey);
			// now try to pause it
			SingleDataResultSet rs = _dll.P_PauseSession_SP(connection,
					sessionKey, proctorKey, browserKey, reason, report);
			// result is not null only if 'report' parameter is true
			if (rs != null) {
				DbResultRecord record = rs.getRecords().next();
				System.err.println(String.format("Status: %s, Reason:%s",
						record.<String> get("status"),
						record.<String> get("reason")));
			 assertTrue("closed"
						.equalsIgnoreCase(record.<String> get("status")));
			 assertTrue(record.<String> get("reason") == null);
			}
		}
	}

	@Test
	@Ignore("Seed data needed")
	public void testTestKeyAccomDepndcs_FN() throws SQLException,
			ReturnStatusException {
		String testKey = "(Oregon)Oregon-Student Help-NA-Winter-2011-2012";
		try (SQLConnection connection = abstractConnectionManager.getConnection()) {
			DataBaseTable table = _dll.TestKeyAccommodationDependencies_FN(
					connection, testKey);

			final String SQL_QUERY = "select * from ${tblName}";
			Map<String, String> tableName = new HashMap<String, String>();
			tableName.put("tblName", table.getTableName());
			String finalQuery = _myDllHelper.fixDataBaseNames(SQL_QUERY,
					tableName);
			SqlParametersMaps parameters = new SqlParametersMaps();
			SingleDataResultSet result = _myDllHelper
					.executeStatement(connection, finalQuery, parameters, false)
					.getResultSets().next();
			Iterator<DbResultRecord> records = result.getRecords();
			while (records.hasNext()) {
				DbResultRecord record = records.next();
				_logger.info("===================Record=======================");
				_logger.info(String.format("clientname: %s",
						record.<String> get("clientname")));
				_logger.info(String.format("TestKey: %s",
						record.<String> get("TestKey")));
				_logger.info(String.format("contextType: %s",
						record.<String> get("contextType")));
				_logger.info(String.format("context: %s",
						record.<String> get("context")));
				_logger.info(String.format("TestMode: %s",
						record.<String> get("TestMode")));
				_logger.info(String.format("ifType : %s",
						record.<String> get("ifType")));
				_logger.info(String.format("ifvalue: %s",
						record.<String> get("ifvalue")));
				_logger.info(String.format("thenType: %s",
						record.<String> get("thenType")));
				_logger.info(String.format("thenValue: %s",
						record.<String> get("thenValue")));
				_logger.info(String.format("IsDefault: %s",
						record.<Boolean> get("IsDefault")));

			 assertTrue("Oregon".equalsIgnoreCase(record
						.<String> get("clientname")));
			 assertTrue("(Oregon)Oregon-Student Help-NA-Winter-2011-2012"
						.equalsIgnoreCase(record.<String> get("TestKey")));
			}
			_logger.info("Total no: of records -- " + result.getCount());
		 assertTrue(result.getCount() == 24);
		}
	}

	@Test
	@Ignore("Seed data needed")
	public void testClientItemFile_FN() throws ReturnStatusException,
			SQLException {
		try (SQLConnection connection = abstractConnectionManager.getConnection()) {
			String clientName = "Minnesota";
			String itemKey = "157-9440";
			String path = _dll.ClientItemFile_FN(connection, clientName,
					itemKey);
			_logger.info(String.format("Client ItemFile path -- : %s", path));
			// D:DataFilesBB_Files ds_airws_orgTDSCore_2012-2013 Bank-157 Items
			// Item-157-9440 item-157-9440.xml
		 assertTrue("D:\\DataFiles\\BB_Files\\tds_airws_org\\TDSCore_2012-2013\\Bank-157\\Items\\Item-157-9440\\item-157-9440.xml"
					.equalsIgnoreCase(path));
		}
	}

	@Test
	@Ignore("Autowire issues")
	public void test_MakeItemFile_FN() throws ReturnStatusException,
			SQLException {
		try (SQLConnection connection = abstractConnectionManager.getConnection()) {
			long bankKey = 148;
			long itemKey = 100006;
			String itemKeyStr = _dll.MakeItemKey_FN(connection, bankKey,
					itemKey);
			_logger.info(String.format(" MakeItemFile_FN result -- : %s",
					itemKeyStr));
		 assertTrue("148-100006".equalsIgnoreCase(itemKeyStr));
		}
	}

	@Test
	@Ignore("Autowire issues")
	public void test_MakeStimulusKey_FN() throws ReturnStatusException,
			SQLException {
		try (SQLConnection connection = abstractConnectionManager.getConnection()) {
			long bankKey = 148;
			long stimulusKey = 100006;
			String stimulusKeyStr = _dll.MakeStimulusKey_FN(connection,
					bankKey, stimulusKey);
			_logger.info(String.format(" MakeStimulusKey_FN result -- : %s",
					stimulusKeyStr));
		 assertTrue("148-100006".equalsIgnoreCase(stimulusKeyStr));
		}
	}

	@Test
	@Ignore("Seed data needed")
	public void test_ITEMBANK_TestLanguages_FN() throws SQLException,
			ReturnStatusException {
		String testKey = "(Delaware)DCAS-Alt1-PAPER-Mathematics-10-Fall-2012-2013";
		try (SQLConnection connection = abstractConnectionManager.getConnection()) {
			String code = _dll.ITEMBANK_TestLanguages_FN(connection, testKey);
			_logger.info(String.format("TestLanguages codes -- : %s", code));
		 assertTrue("'ENU'".equalsIgnoreCase(code));
		}
	}

	@Test
	@Ignore("Seed data needed")
	public void testTestKeyAccoms_FN() throws SQLException,
			ReturnStatusException {
		String testKey = "(Delaware)DCAS-EOC-ALGEBRAII-Mathematics-12-Winter-2011-2012";
		try (SQLConnection connection = abstractConnectionManager.getConnection()) {
			DataBaseTable table = _dll.TestKeyAccommodations_FN(connection,
					testKey);

			final String SQL_QUERY = "select * from ${tblName}";
			Map<String, String> tableName = new HashMap<String, String>();
			tableName.put("tblName", table.getTableName());
			String finalQuery = _myDllHelper.fixDataBaseNames(SQL_QUERY,
					tableName);
			SqlParametersMaps parameters = new SqlParametersMaps();
			SingleDataResultSet result = _myDllHelper
					.executeStatement(connection, finalQuery, parameters, false)
					.getResultSets().next();
			Iterator<DbResultRecord> records = result.getRecords();
			while (records.hasNext()) {
				DbResultRecord record = records.next();
				_logger.info("===================Record=======================");
				_logger.info(String.format("segment: %d",
						record.<Integer> get("Segment")));
				_logger.info(String.format("DisableOnGuestSession: %s",
						record.<Boolean> get("DisableOnGuestSession")));
				_logger.info(String.format("ToolTypeSortOrder: %s",
						record.<Boolean> get("ToolTypeSortOrder")));
				_logger.info(String.format("ToolValueSortOrder: %d",
						record.<Integer> get("ToolValueSortOrder")));
				_logger.info(String.format("TypeMode: %s",
						record.<String> get("TypeMode")));
				_logger.info(String.format("ToolMode: %s",
						record.<String> get("ToolMode")));
				_logger.info(String.format("AccType: %s",
						record.<String> get("AccType")));
				_logger.info(String.format("AccValue: %s",
						record.<String> get("AccValue")));
				_logger.info(String.format("AccCode: %s",
						record.<String> get("AccCode")));
				_logger.info(String.format("IsDefault : %s",
						record.<Boolean> get("IsDefault")));
				_logger.info(String.format("AllowCombine: %s",
						record.<Boolean> get("AllowCombine")));
				_logger.info(String.format("IsFunctional: %s",
						record.<Boolean> get("IsFunctional")));
				_logger.info(String.format("AllowChange: %s",
						record.<Boolean> get("AllowChange")));
				_logger.info(String.format("IsSelectable: %s",
						record.<Boolean> get("IsSelectable")));
				_logger.info(String.format("IsVisible: %s",
						record.<Boolean> get("IsVisible")));
				_logger.info(String.format("studentControl: %s",
						record.<Boolean> get("StudentControl")));
				_logger.info(String.format("ValCount: %d",
						record.<Integer> get("ValCount")));
				_logger.info(String.format("DependsOnToolType: %s",
						record.<String> get("DependsOnToolType")));
			}
			_logger.info("Total no: of records -- " + result.getCount());
		 assertTrue(result.getCount() == 25);
		}
	}

	@Test
	@Ignore("Seed data needed")
	public void test_IB_GetTestAccoms_SP() throws SQLException,
			ReturnStatusException {
		String testKey = "(Oregon)Oregon-Student Help-NA-Winter-2011-2012";
		try (SQLConnection connection = abstractConnectionManager.getConnection()) {
			MultiDataResultSet resultsets = _dll.IB_GetTestAccommodations_SP(
					connection, testKey);
			Iterator<SingleDataResultSet> iterator = resultsets.getResultSets();
			int resultIdx = 0;
			while (iterator.hasNext()) {
				SingleDataResultSet set = iterator.next();
				if (resultIdx == 0) {
					Iterator<DbResultRecord> iteratorRec = set.getRecords();
					while (iteratorRec.hasNext()) {
						DbResultRecord record = iteratorRec.next();
						_logger.info("===================TestKeyAccoms Result=======================");
						_logger.info(String.format("segment: %d",
								record.<Integer> get("Segment")));
						_logger.info(String.format("DisableOnGuestSession: %s",
								record.<Boolean> get("DisableOnGuestSession")));
						_logger.info(String.format("ToolTypeSortOrder: %s",
								record.<Boolean> get("ToolTypeSortOrder")));
						_logger.info(String.format("ToolValueSortOrder: %d",
								record.<Integer> get("ToolValueSortOrder")));
						_logger.info(String.format("TypeMode: %s",
								record.<String> get("TypeMode")));
						_logger.info(String.format("ToolMode: %s",
								record.<String> get("ToolMode")));
						_logger.info(String.format("AccType: %s",
								record.<String> get("AccType")));
						_logger.info(String.format("AccValue: %s",
								record.<String> get("AccValue")));
						_logger.info(String.format("AccCode: %s",
								record.<String> get("AccCode")));
						_logger.info(String.format("IsDefault : %s",
								record.<Boolean> get("IsDefault")));
						_logger.info(String.format("AllowCombine: %s",
								record.<Boolean> get("AllowCombine")));
						_logger.info(String.format("IsFunctional: %s",
								record.<Boolean> get("IsFunctional")));
						_logger.info(String.format("AllowChange: %s",
								record.<Boolean> get("AllowChange")));
						_logger.info(String.format("IsSelectable: %s",
								record.<Boolean> get("IsSelectable")));
						_logger.info(String.format("IsVisible: %s",
								record.<Boolean> get("IsVisible")));
						_logger.info(String.format("studentControl: %s",
								record.<Boolean> get("StudentControl")));
						_logger.info(String.format("ValCount: %d",
								record.<Integer> get("ValCount")));
						_logger.info(String.format("DependsOnToolType: %s",
								record.<String> get("DependsOnToolType")));

					 assertTrue(set.getCount() == 21);
					}
				}
				if (resultIdx == 1) {
					Iterator<DbResultRecord> iteratorRec = set.getRecords();
					while (iteratorRec.hasNext()) {
						DbResultRecord record = iteratorRec.next();
						_logger.info("===================TestKeyAccomsDependencies Result====================");
						_logger.info(String.format("clientname: %s",
								record.<String> get("clientname")));
						_logger.info(String.format("TestKey: %s",
								record.<String> get("TestKey")));
						_logger.info(String.format("contextType: %s",
								record.<String> get("contextType")));
						_logger.info(String.format("context: %s",
								record.<String> get("context")));
						_logger.info(String.format("TestMode: %s",
								record.<String> get("TestMode")));
						_logger.info(String.format("ifType : %s",
								record.<String> get("ifType")));
						_logger.info(String.format("ifvalue: %s",
								record.<String> get("ifvalue")));
						_logger.info(String.format("thenType: %s",
								record.<String> get("thenType")));
						_logger.info(String.format("thenValue: %s",
								record.<String> get("thenValue")));
						_logger.info(String.format("IsDefault: %s",
								record.<Boolean> get("IsDefault")));

					 assertTrue(set.getCount() == 24);
					}
				}
				resultIdx++;
			}
		}
	}

}
