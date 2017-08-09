/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.dll.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import tds.dll.api.ICommonDLL;
import tds.dll.api.IReportingDLL;
import tds.dll.api.IStudentDLL;
import AIR.Common.DB.AbstractDAO;
import AIR.Common.DB.DATABASE_TYPE;
import AIR.Common.DB.DataBaseTable;
import AIR.Common.DB.SQLConnection;
import AIR.Common.DB.SQL_TYPE_To_JAVA_TYPE;
import AIR.Common.DB.SqlParametersMaps;
import AIR.Common.DB.results.DbResultRecord;
import AIR.Common.DB.results.MultiDataResultSet;
import AIR.Common.DB.results.SingleDataResultSet;
import AIR.Common.Helpers._Ref;
import AIR.Common.Sql.AbstractDateUtilDll;
import AIR.test.framework.AbstractTest;
import TDS.Shared.Exceptions.ReturnStatusException;

@Configuration
@ContextConfiguration ("classpath:test-context.xml")
public class TestStudentDLL extends AbstractTest
{
  private static final Logger _logger = LoggerFactory.getLogger (TestStudentDLL.class);
  @Autowired
  @Qualifier ("iStudentDLL")
  IStudentDLL                 _dll    = null;

  @Autowired
  @Qualifier ("iCommonDLL")
  ICommonDLL                 _commondll    = null;

  @Autowired
  AbstractDateUtilDll         _dateUtil;

  @Autowired
  @Qualifier ("applicationDataSource")
  private DataSource          applicationDataSource;

  @Autowired
  DLLHelper          _myDllHelper;

  // declare msg varchar(200);
  // exec _ValidateTesteeAccessProc 'AAA3217A-2A82-405C-8ED9-20F1AAF29ECE',
  // 'C4967141-EEE0-4F3C-9FA3-B7CD4C0BC4B1','99F2085F-0E3F-48B7-AA5E-A325C5D6A84C',1,
  // msg output;
  // select msg
  // @Test
  public void test_ValidateTesteeAccessProc_SP1 () throws SQLException, ReturnStatusException {
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {

      UUID testoppkey = UUID.fromString ("AAA3217A-2A82-405C-8ED9-20F1AAF29ECE");
      UUID session = UUID.fromString ("C4967141-EEE0-4F3C-9FA3-B7CD4C0BC4B1");
      UUID browserId = UUID.fromString ("99F2085F-0E3F-48B7-AA5E-A325C5D6A84C");
      Boolean checkSession = true;
      _Ref<String> message = new _Ref<String> ();

      _dll._ValidateTesteeAccessProc_SP (connection, testoppkey, session, browserId, checkSession, message);
      assertTrue (message.get () != null);
      assertTrue ("The session keys do not match; please consult your test administrator".equalsIgnoreCase (message.get ()));

      _logger.info (message.get ());

    }
  }

  // declare @msg varchar(200);
  // exec _ValidateTesteeAccessProc '7F36B073-1956-48B3-ACE2-52E7BFE1B0E0',
  // 'E483B532-264C-4F27-A16E-005FF85CA7C7','8E154CF5-480A-49A3-AAE6-B223586AD29A',1,
  // @msg output;
  // select @msg
  // @Test
  public void test_ValidateTesteeAccessProc_SP2 () throws SQLException, ReturnStatusException {
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {

      UUID testoppkey = UUID.fromString ("7F36B073-1956-48B3-ACE2-52E7BFE1B0E0");
      UUID session = UUID.fromString ("E483B532-264C-4F27-A16E-005FF85CA7C7");
      UUID browserId = UUID.fromString ("8E154CF5-480A-49A3-AAE6-B223586AD29A");
      Boolean checkSession = true;
      _Ref<String> message = new _Ref<String> ();

      _dll._ValidateTesteeAccessProc_SP (connection, testoppkey, session, browserId, checkSession, message);
      assertTrue (message.get () == null);
    }
  }

  // @Test
  public void testLatencies () throws SQLException, ReturnStatusException {
    String clientname = "Oregon";

    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      Integer auditLatencies = _dll.AuditLatencies_FN (connection, clientname);

      _logger.info (String.format ("Latensies: %d", auditLatencies));
    }
  }

  // @Test
  public void test_CreateResponseSet_SP () throws ReturnStatusException, SQLException {
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {

      UUID testoppKey = UUID.randomUUID ();// UUID.fromString
                                           // ("BFC5216A-FD8A-4590-84AF-68C1962E494A");
      Integer maxitems = 5;
      Integer reset = 1;
      int num = 10;
      Long cumDiff = 0L;
      Long diff = 0L;
      Date starttime = null;
      Date now = null;

      String query = null;
      DATABASE_TYPE dbDialect = _myDllHelper.getDatabaseDialect ();
      if (dbDialect == DATABASE_TYPE.MYSQL)
        query = "select _key from testopportunity order by RAND() limit ${num}";
      else if (dbDialect == DATABASE_TYPE.SQLSERVER)
        query = "select top ${num} _key from testopportunity order by newid()";
      else
        throw new ReturnStatusException ("what are you doing?");

      SqlParametersMaps parms = (new SqlParametersMaps ()).put ("num", num);
      SingleDataResultSet res = _myDllHelper.executeStatement (connection, query, parms, false).getResultSets ().next ();
      Iterator<DbResultRecord> records = res.getRecords ();
      int i = 0;
      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        testoppKey = record.<UUID> get ("_key");
        starttime = new Date ();
        _dll._CreateResponseSet_SP (connection, testoppKey, maxitems, reset);
        now = new Date ();
        diff = now.getTime () - starttime.getTime ();
        System.out.println (String.format ("CreateResponseSet latency: %d millisec", (long) diff));

        cumDiff += diff;
        i++;
      }
      System.out.println (String.format ("Average: %d millisec", (long) cumDiff / i));
    }
  }

  // @Test
  public void test_CreateResponseSet_StoredProc () throws ReturnStatusException, SQLException {
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      // UUID testoppKey = UUID.fromString
      // ("BFC5216A-FD8A-4590-84AF-68C1962E494A");
      Integer maxitems = 5;
      Integer reset = 1;
      int num = 10;
      Long cumDiff = 0L;
      Long diff = 0L;
      Date starttime = null;
      Date now = null;

      String query = null;
      String CMD = null;
      DATABASE_TYPE dbDialect = _myDllHelper.getDatabaseDialect ();
      if (dbDialect == DATABASE_TYPE.MYSQL) {
        CMD = "call _createresponseset(${testoppkey}, ${maxitems}, ${reset});";
        query = "select _key from testopportunity order by RAND() limit ${num}";
      } else if (dbDialect == DATABASE_TYPE.SQLSERVER) {
        CMD = "BEGIN; SET NOCOUNT ON; exec _CreateResponseSet ${testoppkey}, ${maxitems}, ${reset}; end;";
        query = "select top ${num} _key from testopportunity order by newid()";
      } else
        throw new ReturnStatusException ("what are you doing?");

      SqlParametersMaps parms = (new SqlParametersMaps ()).put ("num", num);
      SingleDataResultSet res = _myDllHelper.executeStatement (connection, query, parms, false).getResultSets ().next ();
      Iterator<DbResultRecord> records = res.getRecords ();
      int i = 0;
      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        UUID testoppKey = record.<UUID> get ("_key");
        SqlParametersMaps parms1 = (new SqlParametersMaps ()).put ("testoppkey", testoppKey).put ("maxitems", maxitems).put ("reset", reset);

        starttime = new Date ();
        _myDllHelper.executeStatement (connection, CMD, parms1, false);
        now = new Date ();

        diff = now.getTime () - starttime.getTime ();
        cumDiff += diff;
        i++;
      }
      System.out.println (String.format ("Average for SP: %d millisec", (long) cumDiff / i));

      // for (int i = 0; i < num; i++) {
      // starttime = new Date ();
      // _myDllHelper.executeStatement (connection, CMD, parms1, false);
      // now = new Date ();
      // diff = now.getTime () - starttime.getTime ();
      // cumDiff += diff;
      // }
      // System.out.println (String.format ("Average for SP: %d millisec",
      // (long) cumDiff / num));
    }
  }

  // select * from dbo.GetTestFormWindows ('Oregon', 'OAKS-Writing-HS', 1)
  // @Test
  public void testGetTestFormWindows_FN () throws ReturnStatusException, SQLException {
    String clientname = "Oregon";
    String testId = "OAKS-Writing-HS";
    Integer sessionType = 1;
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      DataBaseTable tbl = _dll.GetTestFormWindows_FN (connection, clientname, testId, sessionType);

      final String SQL_QUERY = "select * from ${tblName}";
      Map<String, String> par = new HashMap<String, String> ();
      par.put ("tblName", tbl.getTableName ());

      SingleDataResultSet result = _myDllHelper.executeStatement (connection, _myDllHelper.fixDataBasenames (SQL_QUERY, par), null, false).getResultSets ().next ();
      Iterator<DbResultRecord> records = result.getRecords ();
      assertTrue (result.getCount () == 8);
      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        _logger.info (String.format ("windiwID: %s", record.<String> get ("windowid")));
        _logger.info (String.format ("windowMax: %d", record.<Integer> get ("windowmax")));
        _logger.info (String.format ("modeMax: %d", record.<Integer> get ("modemax")));
        _logger.info (String.format ("startDate: %s", record.<Date> get ("startDate")));
        _logger.info (String.format ("endDate: %s", record.<Date> get ("endDate")));
        _logger.info (String.format ("formStart: %s", record.<Date> get ("formStart")));
        _logger.info (String.format ("formEnd: %s", record.<Date> get ("formEnd")));
        _logger.info (String.format ("formKey: %s", record.<String> get ("formKey")));
        _logger.info (String.format ("formID: %s", record.<String> get ("formId")));
        _logger.info (String.format ("language: %s", record.<String> get ("language")));
        _logger.info (String.format ("mode: %s", record.<String> get ("mode")));
        _logger.info (String.format ("testKey: %s", record.<String> get ("testKey")));
        _logger.info (String.format ("windowSession: %d", record.<Integer> get ("windowsession")));
        _logger.info (String.format ("modeSession: %d", record.<Integer> get ("modesession")));
      }
    }
  }

  // @Test
  public void test_GetTesteeTestForms_SP () throws ReturnStatusException, SQLException {
    String clientname = "Oregon";
    String testId = "OAKS-Writing-HS";
    Integer sessionType = 1;
    Long testee = 30000L;
    String formList = "annual:FORM123;annual:104-1067;annual:104-1068";
    // "INTEGRATED MATHEMATICS III EOC Form C1 Seg 1::SP13::ESN::OL;English II EOC::SP13::C1::OL;Biology EOC::SP13::PP;Pilot12345;FormC::GRADR::FA11;";

    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      SingleDataResultSet result = _dll._GetTesteeTestForms_SP (connection, clientname, testId, testee, sessionType, formList);
      Iterator<DbResultRecord> records = result.getRecords ();
      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        // windowID, windowMax, startDate, endDate, formkey, mode, modeMax,
        // testkey
        _logger.info (String.format ("windowID: %s", record.<String> get ("windowid")));
        _logger.info (String.format ("windowMax: %d", record.<Integer> get ("windowmax")));
        _logger.info (String.format ("modeMax: %d", record.<Integer> get ("modemax")));
        _logger.info (String.format ("startDate: %s", record.<Date> get ("startDate")));
        _logger.info (String.format ("endDate: %s", record.<Date> get ("endDate")));
        _logger.info (String.format ("formKey: %s", record.<String> get ("formKey")));
        _logger.info (String.format ("mode: %s", record.<String> get ("mode")));
        _logger.info (String.format ("testKey: %s", record.<String> get ("testKey")));

      }
    }
  }

  // @Test
  public void test_SelectTestForm_Predetermined_SP1 () throws ReturnStatusException, SQLException {

    _Ref<String> formkeyRef = new _Ref<String> ();
    _Ref<String> formIdRef = new _Ref<String> ();
    _Ref<Integer> itemcntRef = new _Ref<Integer> ();
    UUID oppkey = UUID.fromString ("3A690E6E-692D-41FD-8E1E-0740BF20AED6");
    String testkey = "(Oregon)OAKS-Writing-HS-Fall-2012-2013";
    String lang = "ENU";
    Integer sessionType = 0;
    String formList = null;
    Long cumDiff = 0L;
    Long diff = 0L;
    Date starttime = null;
    Date now = null;

    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {

      starttime = new Date ();
      _dll._SelectTestForm_Predetermined_SP (connection, oppkey, testkey, lang, formkeyRef, formIdRef, itemcntRef, sessionType, formList);
      now = new Date ();
      diff = now.getTime () - starttime.getTime ();
      System.out.println (String.format ("_SelectTestForm_Predetermined latency: %d millisec", (long) diff));
      _logger.info (String.format ("FormKey: %s, FormID: %s, ItemCnt: %d", formkeyRef.get (), formIdRef.get (), itemcntRef.get ()));
      assertTrue (formkeyRef.get () == null);
      assertTrue (formIdRef.get () == null);
      assertTrue (itemcntRef.get () == null);
    }
  }

  // declare @formkey varchar(200);
  // declare @formid varchar(200);
  // declare @formlength int;
  // exec _SelectTestForm_Predetermined 'F4CA5368-D32B-4754-9124-3543B2BED3BF',
  // '(Hawaii_PT)HSA-Reading-3-Fall-2012-2013', 'ENU',
  // @formkey output, @formid output, @formlength output, 0, null;
  // select @formkey, @formid, @formlength;
  // @Test
  public void test_SelectTestForm_Predetermined_SP2 () throws ReturnStatusException, SQLException {

    _Ref<String> formkeyRef = new _Ref<String> ();
    _Ref<String> formIdRef = new _Ref<String> ();
    _Ref<Integer> itemcntRef = new _Ref<Integer> ();
    UUID oppkey = UUID.fromString ("F4CA5368-D32B-4754-9124-3543B2BED3BF");
    String testkey = "(Hawaii_PT)HSA-Reading-3-Fall-2012-2013";
    String lang = "ENU";
    Integer sessionType = 0;
    String formList = null;
    // valid testid = 'HSA-Reading-3';
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {

      _dll._SelectTestForm_Predetermined_SP (connection, oppkey, testkey, lang, formkeyRef, formIdRef, itemcntRef, sessionType, formList);
      _logger.info (String.format ("FormKey: %s, FormID: %s, ItemCnt: %d", formkeyRef.get (), formIdRef.get (), itemcntRef.get ()));
      assertTrue ("74-434".equalsIgnoreCase (formkeyRef.get ()));
      assertTrue ("Training::G3R::FA12".equalsIgnoreCase (formIdRef.get ()));
      assertTrue (13 == itemcntRef.get ());
    }
  }

  // declare @formkey varchar(200);
  // declare @formid varchar(200);
  // declare @formlength int;
  // exec _SelectTestForm_EqDist 'FE731D91-D591-43BB-85CC-B979785FF00D',
  // '(Minnesota)GRAD-FX-Mathematics-11-Fall-2011-2012', 'ENU',
  // @formkey output, @formid output, @formlength output, null;
  // select @formkey, @formid, @formlength;
  // @Test
  public void test_SelectTestForm_EqDist_SP () throws ReturnStatusException, SQLException {
    // THis method returns randomly chosen rec, if more than one match selection
    // algorithm
    // i.e. repeating the same test may produce different results
    // That's why wedo not assertTruereturn values
    _Ref<String> formkeyRef = new _Ref<String> ();
    _Ref<String> formIdRef = new _Ref<String> ();
    _Ref<Integer> formLengthRef = new _Ref<Integer> ();
    UUID oppkey = UUID.fromString ("FE731D91-D591-43BB-85CC-B979785FF00D");// ("F4CA5368-D32B-4754-9124-3543B2BED3BF");
    String testkey = "(Minnesota)GRAD-FX-Mathematics-11-Fall-2011-2012";// "(Hawaii_PT)HSA-Reading-3-Fall-2012-2013";
    String lang = "ENU";
    String formCohort = null;
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      _dll._SelectTestForm_EqDist_SP (connection, oppkey, testkey, lang, formkeyRef, formIdRef, formLengthRef, formCohort);
      _logger.info (String.format ("FormKey: %s, FormID: %s, FormLength: %d", formkeyRef.get (), formIdRef.get (), formLengthRef.get ()));
      assertTrue (formkeyRef.get () != null);
      assertTrue (formIdRef.get () != null);
      assertTrue (formLengthRef.get () != null);
    }
  }

  // declare @formkey varchar(200);
  // declare @formid varchar(200);
  // declare @formlength int;
  // exec _SelectTestForm_Driver 'FE731D91-D591-43BB-85CC-B979785FF00D',
  // '(Minnesota)GRAD-FX-Mathematics-11-Fall-2011-2012', 'ENU',
  // @formkey output, @formid output, @formlength output, null, null;
  // select @formkey, @formid, @formlength;
  // @Test
  public void test_SelectTestForm_Driver_SP () throws ReturnStatusException, SQLException {

    _Ref<String> formkeyRef = new _Ref<String> ();
    _Ref<String> formIdRef = new _Ref<String> ();
    _Ref<Integer> formLengthRef = new _Ref<Integer> ();
    UUID oppkey = UUID.fromString ("FE731D91-D591-43BB-85CC-B979785FF00D");
    String testkey = "(Minnesota)GRAD-FX-Mathematics-11-Fall-2011-2012";
    String lang = "ENU";
    String formList = null;
    String formCohort = null;
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      _dll._SelectTestForm_Driver_SP (connection, oppkey, testkey, lang, formkeyRef, formIdRef, formLengthRef, formList, formCohort);
      _logger.info (String.format ("FormKey: %s, FormID: %s, FormLength: %d", formkeyRef.get (), formIdRef.get (), formLengthRef.get ()));
      // this test case will result in use of Form_EqDist which return random
      // selection from matching set
      assertTrue (formkeyRef.get () != null);
      assertTrue (formIdRef.get () != null);
      assertTrue (formLengthRef.get () != null);
    }
  }

  // declare @number int;
  // declare @msg varchar(200);
  // exec _CanOpenExistingOpportunity 'Oregon', 29138, 'OAKS-Math-3',
  // 'D09CC4F9-FF95-4679-A441-0115E1BCFB7C', 100,
  // @number output, @msg output;
  // select @number, @msg;
  // @Test
  public void test_CanOpenExistingOpportunity_SP1 () throws ReturnStatusException, SQLException {

    String client = "Oregon";
    Long testee = 29138L;
    String testID = "OAKS-Math-3";
    UUID sessionID = UUID.fromString ("D09CC4F9-FF95-4679-A441-0115E1BCFB7C");
    Integer maxOpportunities = 100;

    _Ref<Integer> numberRef = new _Ref<Integer> ();
    _Ref<String> msgRef = new _Ref<String> ();
    // exit 4
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      _dll._CanOpenExistingOpportunity_SP (connection, client, testee, testID, sessionID, maxOpportunities, numberRef, msgRef);
      _logger.info (String.format ("Msg: %s, Number: %d", msgRef.get (), numberRef.get ()));
      assertTrue (1 == numberRef.get ());
      assertTrue (msgRef.get () == null);
    }
  }

  // declare @number int;
  // declare @msg varchar(200);
  // exec _CanOpenExistingOpportunity 'minnesota_pt', -1048, 'MCA-Science-5',
  // '7BAC27F9-DB55-4B41-810E-0D9DFECB49DC', 100,
  // @number output, @msg output;
  // select @number, @msg;
  // @Test
  public void test_CanOpenExistingOpportunity_SP2 () throws ReturnStatusException, SQLException {

    String client = "minnesota_pt";
    Long testee = -1048L;
    String testID = "MCA-Science-5";
    UUID sessionID = UUID.fromString ("7BAC27F9-DB55-4B41-810E-0D9DFECB49DC");
    Integer maxOpportunities = 100;

    _Ref<Integer> numberRef = new _Ref<Integer> ();
    _Ref<String> msgRef = new _Ref<String> ();
    // exit 3
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      _dll._CanOpenExistingOpportunity_SP (connection, client, testee, testID, sessionID, maxOpportunities, numberRef, msgRef);
      _logger.info (String.format ("Msg: %s, Number: %d", msgRef.get (), numberRef.get ()));
      assertTrue (1 == numberRef.get ());
      assertTrue (msgRef.get () == null);
    }
  }

  // declare @number int;
  // declare @msg varchar(200);
  // exec _CanOpenExistingOpportunity 'hawaii_pt', -18245,
  // 'HI EOC Exp Writing-Training-10', null, 100,
  // @number output, @msg output;
  // select @number, @msg;
  // @Test
  public void test_CanOpenExistingOpportunity_SP3 () throws ReturnStatusException, SQLException {

    String client = "hawaii_pt";
    Long testee = -18245L;
    String testID = "HI EOC Exp Writing-Training-10";
    UUID sessionID = null;
    Integer maxOpportunities = 100;

    _Ref<Integer> numberRef = new _Ref<Integer> ();
    _Ref<String> msgRef = new _Ref<String> ();
    // exit 3
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      _dll._CanOpenExistingOpportunity_SP (connection, client, testee, testID, sessionID, maxOpportunities, numberRef, msgRef);
      _logger.info (String.format ("Msg: %s, Number: %d", msgRef.get (), numberRef.get ()));
      assertTrue (1 == numberRef.get ());
      assertTrue (msgRef.get () == null);
    }
  }

  // declare @number int;
  // declare @msg varchar(200);
  // exec _CanOpenExistingOpportunity 'delaware', 2633,
  // 'DCAS-Alt1-SR-PAPER-Reading-3-5', '8CB52865-E5A5-4E2A-AB56-B56424904FAE',
  // 100,
  // @number output, @msg output;
  // select @number, @msg;
  // @Test
  public void test_CanOpenExistingOpportunity_SP4 () throws ReturnStatusException, SQLException {

    String client = "Delaware";
    Long testee = 2633L;
    String testID = "DCAS-Alt1-SR-PAPER-Reading-3-5";
    UUID sessionID = UUID.fromString ("8CB52865-E5A5-4E2A-AB56-B56424904FAE");
    Integer maxOpportunities = 100;

    _Ref<Integer> numberRef = new _Ref<Integer> ();
    _Ref<String> msgRef = new _Ref<String> ();

    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      _dll._CanOpenExistingOpportunity_SP (connection, client, testee, testID, sessionID, maxOpportunities, numberRef, msgRef);
      _logger.info (String.format ("Msg: %s, Number: %d", msgRef.get (), numberRef.get ()));
      assertTrue (1 == numberRef.get ());
      assertTrue (msgRef.get () == null);
    }
  }

  // declare @number int;
  // declare @msg varchar(200);
  // exec _CanOpenNewOpportunity 'oregon', 29138,
  // '(Oregon)OAKS-Math-3-Fall-2012-2013', 100, 1,
  // @number output, @msg output, '8CB52865-E5A5-4E2A-AB56-B56424904FAE'
  // select @number, @msg;
  // @Test
  public void test_CanOpenNewOpportunity_SP1 () throws ReturnStatusException, SQLException {

    String client = "Oregon";
    Long testee = 29138L;
    String testID = "(Oregon)OAKS-Math-3-Fall-2012-2013";
    Integer maxOpportunities = 100;
    Integer delayDays = 1;
    UUID session = UUID.fromString ("D09CC4F9-FF95-4679-A441-0115E1BCFB7C");
    _Ref<Integer> numberRef = new _Ref<Integer> ();
    _Ref<String> msgRef = new _Ref<String> ();

    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      _dll._CanOpenNewOpportunity_SP (connection, client, testee, testID, maxOpportunities, delayDays, numberRef, msgRef, session);
      _logger.info (String.format ("Msg: %s, Number: %d", msgRef.get (), numberRef.get ()));
      assertTrue (msgRef.get () == null);
      assertTrue (1 == numberRef.get ());
    }
  }

  // declare @number int;
  // declare @msg varchar(200);
  // exec _CanOpenNewOpportunity 'hawaii', -24619, 'ASISample-Mathematics-8',
  // 100, 1,
  // @number output, @msg output, 'D09CC4F9-FF95-4679-A441-0115E1BCFB7C'
  // select @number, @msg;
  // @Test
  public void test_CanOpenNewOpportunity_SP2 () throws ReturnStatusException, SQLException {

    String client = "Hawaii";
    Long testee = -24619L;
    String testID = "ASISample-Mathematics-8";
    Integer maxOpportunities = 100;
    Integer delayDays = 1;
    UUID session = UUID.fromString ("D09CC4F9-FF95-4679-A441-0115E1BCFB7C");
    _Ref<Integer> numberRef = new _Ref<Integer> ();
    _Ref<String> msgRef = new _Ref<String> ();

    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      _dll._CanOpenNewOpportunity_SP (connection, client, testee, testID, maxOpportunities, delayDays, numberRef, msgRef, session);
      _logger.info (String.format ("Msg: %s, Number: %d", msgRef.get (), numberRef.get ()));
      assertTrue (msgRef.get () == null);
      assertTrue (0 == numberRef.get ());
    }
  }

  // exec _GetTesteeTestModes 'Oregon','OAKS-Math-3', 29138,
  // 0, null
  // @Test
  public void test_GetTesteeTestModes_SP () throws ReturnStatusException, SQLException {
    String clientname = "Oregon";
    String testID = "OAKS-Math-3";
    Long testee = 29138L;
    Integer sessionType = 0;
    String modeList = null; // "tideid:1;tideid&winid02:2;tideid:3;tideid&winid04:4";
    // <TIDEID>:<Mode> OR <TIDEID>&<WindowID>:<Mode>
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      SingleDataResultSet result = _dll._GetTesteeTestModes_SP (connection, clientname, testID, testee, sessionType, modeList);
      assertTrue (result.getCount () == 2);
      Iterator<DbResultRecord> records = result.getRecords ();
      while (records.hasNext ()) {
        DbResultRecord record = records.next ();

        _logger.info (String.format ("windowID: %s", record.<String> get ("windowID")));
        _logger.info (String.format ("windowMax: %d", record.<Integer> get ("windowMax")));
        _logger.info (String.format ("startDate: %s", record.<Date> get ("startDate")));
        _logger.info (String.format ("endDate: %s", record.<Date> get ("endDate")));
        _logger.info (String.format ("modeMax: %d", record.<Integer> get ("modeMax")));
        _logger.info (String.format ("testkey: %s", record.<String> get ("testkey")));
      }
    }

  }

  // exec _GetTesteeTestWindows 'hawaii_pt', 'HSA-Science-10', 29138, 0, null,
  // null
  // @Test
  public void test_GetTesteeTestWindows_SP () throws ReturnStatusException, SQLException {
    String clientname = "Hawaii_PT";
    String testID = "HSA-Science-10";
    Long testee = 29138L;
    Integer sessionType = 0;
    String windowList = null, formList = null;

    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      SingleDataResultSet result = _dll._GetTesteeTestWindows_SP (connection, clientname, testID, testee, sessionType, windowList, formList);
      assertTrue (result.getCount () == 1);
      Iterator<DbResultRecord> records = result.getRecords ();
      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        // windowID, windowMax, startDate, endDate, null as formkey, mode,
        // modeMax, testkey
        _logger.info (String.format ("windowID: %s", record.<String> get ("windowID")));
        _logger.info (String.format ("windowMax: %d", record.<Integer> get ("windowMax")));
        _logger.info (String.format ("startDate: %s", record.<Date> get ("startDate")));
        _logger.info (String.format ("endDate: %s", record.<Date> get ("endDate")));
        _logger.info (String.format ("formKey: %s", record.<String> get ("formKey")));
        _logger.info (String.format ("mode: %s", record.<String> get ("mode")));
        _logger.info (String.format ("modeMax: %d", record.<Integer> get ("modeMax")));
        _logger.info (String.format ("testkey: %s", record.<String> get ("testkey")));
        assertTrue (10 == record.<Integer> get ("windowMax"));
        assertTrue (50 == record.<Integer> get ("modeMax"));
        assertTrue ("(Hawaii_PT)HSA-Science-10-Fall-2012-2013".equalsIgnoreCase (record.<String> get ("testkey")));
      }
    }
  }

  // declare @reason varchar(200);
  // exec _IsOpportunityBlocked 'oregon', 29138, 'Oregon-CoreOnly-ELPA-4-5', 5,
  // @reason
  // output, 0;
  // select @reason
  // @Test
  public void test_IsOpportunityBlocked_SP () throws ReturnStatusException, SQLException {

    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      String clientname = "Oregon";// "Hawaii_PT";
      String testID = "Oregon-CoreOnly-ELPA-4-5";// "HSA-Science-10";
      Long testee = 29138L;
      Integer sessionType = 0;
      Integer maxopps = 5;
      _Ref<String> reasonBlockedRef = new _Ref<> ();
      _dll._IsOpportunityBlocked_SP (connection, clientname, testee, testID, maxopps, reasonBlockedRef, sessionType);
      _logger.info (String.format ("ReasonBlocked: %s", reasonBlockedRef.get ()));
      assertTrue (reasonBlockedRef.get () == null);
    }

  }

  // declare @newref bit;
  // declare @numberRef int;
  // declare @reasonRef varchar(200);
  // '(Hawaii_PT)HSA-Science-8-Fall-2012-2013', null, 5,
  // @newRef output, @numberRef output, @reasonRef output;
  // select @newRef, @numberRef, @reasonRef;
  // @Test
  public void test_CanOpenTestOpportunit_SP () throws ReturnStatusException, SQLException {

    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      String clientname = "Oregon";// "Hawaii_PT";
      // String testID = "HSA-Science-8";
      Long testee = 29138L;
      Integer maxopps = 100;
      String testkey = "(Oregon)Oregon-CoreOnly-ELPA-4-5-Winter-2012-2013";// "(Hawaii_PT)HSA-Science-8-Fall-2012-2013";
      UUID sessionId = UUID.fromString ("007B0242-B169-4344-AB0F-EB73550FA17C");
      _Ref<String> reasonRef = new _Ref<> ();
      _Ref<Boolean> newRef = new _Ref<> ();
      _Ref<Integer> numberRef = new _Ref<> ();

      _dll._CanOpenTestOpportunity_SP (connection, clientname, testee, testkey, sessionId, maxopps, newRef, numberRef, reasonRef);
      _logger.info (String.format ("New but: %s, NUmber: %d, Reason: %s", newRef.get (), numberRef.get (), reasonRef.get ()));
      assertTrue (newRef.get () == true);
      assertTrue (numberRef.get () == 1);
      assertTrue (reasonRef.get () == null);
    }
  }

  // select _AllowAnonymousTestee ('oregon_pt');
  // @Test
  public void test_AllowAnonymousTestee_FN () throws ReturnStatusException, SQLException {

    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      String clientname = "oregon_PT";
      boolean allow = _dll._AllowAnonymousTestee_FN (connection, clientname);
      _logger.info (String.format ("Allow: %s", allow));
      assertTrue (allow == true);
    }
  }

  // @Test
  public void test_AllowProctorlessSessions_FN () throws ReturnStatusException, SQLException {

    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      String clientname = "kuku";
      boolean allow = _dll._AllowProctorlessSessions_FN (connection, clientname);
      _logger.info (String.format ("Allow: %s", allow));
      assertTrue (allow == false);
    }
  }

  // select dbo.IsSessionOpen ('0A94BDC9-86E7-43B7-82FD-4CDB0AF08EC2')
  // @Test
  public void testIsSessionOpen_FN1 () throws ReturnStatusException, SQLException {

    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      UUID sessionKey = UUID.fromString ("0A94BDC9-86E7-43B7-82FD-4CDB0AF08EC2"); // ("3F1315ED-E084-4A7C-BBF8-585B1580BA5A");
      int isOpen = _dll.IsSessionOpen_FN (connection, sessionKey);
      _logger.info (String.format ("isOpen: %d", isOpen));
      assertTrue (isOpen == 1);
    }
  }

  // select dbo.IsSessionOpen ('3F1315ED-E084-4A7C-BBF8-585B1580BA5A')
  // @Test
  public void testIsSessionOpen_FN2 () throws ReturnStatusException, SQLException {

    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      UUID sessionKey = UUID.fromString ("3F1315ED-E084-4A7C-BBF8-585B1580BA5A");
      int isOpen = _dll.IsSessionOpen_FN (connection, sessionKey);
      _logger.info (String.format ("isOpen: %d", isOpen));
      assertTrue (isOpen == 0);
    }
  }

  // select dbo._RestoreRTSAccommodations ('Hawaii_pt')
  // @Test
  public void test_RestoreRTSAccommodations_FN () throws ReturnStatusException, SQLException {

    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      String clientname = "Hawaii_PT";
      boolean allow = _dll._RestoreRTSAccommodations_FN (connection, clientname);
      _logger.info (String.format ("RestoreAccommodations allow: %s", allow));
      assertTrue (allow == true);
    }
  }

  // select dbo.TestSubject ('(Hawaii_PT)HSA-Reading-6-Fall-2012-2013')
  // @Test
  public void testTestSubject_FN () throws ReturnStatusException, SQLException {

    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      String testKey = "(Hawaii_PT)HSA-Reading-6-Fall-2012-2013";
      String rslt = _dll.TestSubject_FN (connection, testKey);
      _logger.info (String.format ("Result : %s", rslt));
      assertTrue ("Reading".equalsIgnoreCase (rslt));
    }
  }

  // declare @subject varchar(200)
  // declare @tstid varchar(200)
  // declare @segmented bit
  // declare @algorithm varchat(200)
  // exec _GetTestParms '', '(Hawaii_PT)HSA-Reading-6-Fall-2012-2013', @subject
  // output,
  // @tstid output, @segmented output, @algorithm output
  // select @subject, @tstid, @segmented, @algorithm
  // @Test
  public void test_GetTestParms_SP () throws ReturnStatusException, SQLException {
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      String clientname = "";
      String testkey = "(Hawaii_PT)HSA-Reading-6-Fall-2012-2013";
      _Ref<String> subjectRef = new _Ref<> ();
      _Ref<String> testIdRef = new _Ref<> ();
      _Ref<Boolean> segmentedRef = new _Ref<> ();
      _Ref<String> algorithmRef = new _Ref<> ();

      _dll._GetTestParms_SP (connection, clientname, testkey, subjectRef, testIdRef, segmentedRef, algorithmRef);
      _logger.info (String.format ("Subject: %s", subjectRef.get ()));
      _logger.info (String.format ("TestId: %s", testIdRef.get ()));
      _logger.info (String.format ("Segmented: %s", segmentedRef.get ()));
      _logger.info (String.format ("Algorithm: %s", algorithmRef.get ()));
      assertTrue ("reading".equalsIgnoreCase (subjectRef.get ()));
      assertTrue ("HSA-Reading-6".equalsIgnoreCase (testIdRef.get ()));
      assertTrue (false == segmentedRef.get ());
      assertTrue ("fixedform".equalsIgnoreCase (algorithmRef.get ()));
    }
  }

  // select * from dbo.GetActiveTests ('Oregon', 0)
  // @Test
  public void testGetActiveTests_FN () throws SQLException, ReturnStatusException {

    String clientname = "Oregon";

    Integer sessionType = 0;
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      DataBaseTable tbl = _dll.GetActiveTests_FN (connection, clientname, sessionType);

      final String SQL_QUERY = "select * from ${tblName}";
      Map<String, String> par = new HashMap<String, String> ();
      par.put ("tblName", tbl.getTableName ());

      SingleDataResultSet result = _myDllHelper.executeStatement (connection, _myDllHelper.fixDataBasenames (SQL_QUERY, par), null, false).getResultSets ().next ();
      assertTrue (result.getCount () == 57);
      Iterator<DbResultRecord> records = result.getRecords ();
      while (records.hasNext ()) {
        DbResultRecord record = records.next ();

        _logger.info (String.format ("testID: %s", record.<String> get ("testid")));
        _logger.info (String.format ("subject: %s", record.<String> get ("subject")));
        _logger.info (String.format ("maxOpportunities: %d", record.<Integer> get ("maxopportunities")));
        _logger.info (String.format ("label: %s", record.<String> get ("label")));
        _logger.info (String.format ("isSelectable: %s", record.<String> get ("isselectable")));
        _logger.info (String.format ("sortOrder: %d", record.<Integer> get ("sortorder")));
        _logger.info (String.format ("windiwID: %s", record.<String> get ("windowid")));
        _logger.info (String.format ("windowMax: %d", record.<Integer> get ("windowmax")));
        _logger.info (String.format ("modeMax: %d", record.<Integer> get ("modemax")));
        _logger.info (String.format ("startDate: %s", record.<Date> get ("startDate")));
        _logger.info (String.format ("endDate: %s", record.<Date> get ("endDate")));
        _logger.info (String.format ("modeMax: %d", record.<Integer> get ("modemax")));
        _logger.info (String.format ("mode: %s", record.<String> get ("mode")));
        _logger.info (String.format ("testKey: %s", record.<String> get ("testKey")));
        _logger.info (String.format ("windowSession: %d", record.<Integer> get ("windowsession")));
        _logger.info (String.format ("modeSession: %d", record.<Integer> get ("modesession")));
      }
    }
  }

  // declare @sessionKey uniqueidentifier
  // declare @sessionid varchar(200)
  // exec _SetupProctorlessSession 'Hawaii_pt', @sessionkey output, @sessionid
  // output
  // select @essionkey, @sessionid
  // @Test
  public void test_SetupProctorlessSession_SP () throws SQLException, ReturnStatusException {
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {

      String clientname = "oregon_PT";
      _Ref<UUID> sessionKeyRef = new _Ref<> ();
      _Ref<String> sessionIdRef = new _Ref<> ();
      _dll._SetupProctorlessSession_SP (connection, clientname, sessionKeyRef, sessionIdRef);
      _logger.info (String.format ("SessionKey: %s, SessionID: %s", sessionKeyRef.get ().toString (), sessionIdRef.get ()));
      assertTrue ("GUEST session".equalsIgnoreCase (sessionIdRef.get ()));
      // TODO Elena: double check for oregon after views are populated
      // assert (sessionKeyRef.get ().equals (UUID.fromString
      // ("b501827a-12a8-4eef-a7a7-49256fb2b69a")));

      // connection.setAutoCommit (false);
      // String resourceName = UUID.randomUUID ().toString ();

      // Integer appLock = _dll.getAppLock (connection, resourceName,
      // "Exclusive");
      // if (DbComparator.greaterOrEqual (appLock, 0)) {
      // _dll.releaseAppLock (connection, resourceName);
      // }
      // connection.commit ();
    }
  }

  // declare @newid bigint
  // exec _CreateClientReportingID 'hawaii_pt',
  // 'AAA3217A-2A82-405C-8ED9-20F1AAF29ECE',
  // @newid output
  // select @newid
  // @Test
  public void test_CreateClientReportingID_SP1 () throws ReturnStatusException, SQLException {
    String clientname = "Hawaii_PT";
    UUID oppKey = UUID.fromString ("AAA3217A-2A82-405C-8ED9-20F1AAF29ECE");
    _Ref<Long> newIdRef = new _Ref<> ();

    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      _commondll._CreateClientReportingID_SP (connection, clientname, oppKey, newIdRef);
      _logger.info (String.format ("NewID: %d", newIdRef.get ()));
      // each call c=increases newid number, that's why we do not check for
      // known
      // value here
      assertTrue (newIdRef.get () != null && newIdRef.get () > 0);
    }
  }

  // @Test
  public void test_CreateClientReportingID_SP2 () throws ReturnStatusException, SQLException {
    String clientname = "abc";
    UUID oppKey = UUID.fromString ("AAA3217A-2A82-405C-8ED9-20F1AAF29ECE");
    _Ref<Long> newIdRef = new _Ref<> ();

    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      _commondll._CreateClientReportingID_SP (connection, clientname, oppKey, newIdRef);
      _logger.info (String.format ("NewID: %d", newIdRef.get ()));
      assertTrue (newIdRef.get () == null);
    }
  }

  // declare @newstatus varchar(50);
  // declare @oppkey uniqueidentifier;
  // exec _OpenExistingOpportunity 'Hawaii_PT',
  // 107347,'(Hawaii_PT)HSA-Mathematics-5-Fall-2012-2013', 1,
  // 'C4967141-EEE0-4F3C-9FA3-B7CD4C0BC4B1',
  // '99F2085F-0E3F-48B7-AA5E-A325C5D6A84C', @newstatus output, null, 1,
  // @oppkey output;
  // select @newstatus, @oppkey;

  // @Test
  public void test_OpenExistingOpportunity_SP () throws ReturnStatusException, SQLException {
    String clientname = "Hawaii_PT";
    Long testee = 107347L; // -6078
    String testkey = "(Hawaii_PT)HSA-Mathematics-5-Fall-2012-2013"; // "(Hawaii_PT)HSA-Reading-5-Fall-2012-2013"
    Integer opportunity = 1;
    UUID sessionId = UUID.randomUUID ();
    UUID browserId = UUID.randomUUID ();
    _Ref<String> newstatusRef = new _Ref<> ();
    // String accommodations = null;
    // String accommodations =
    // "TDS_T1|TDS_TTX_A203|TDS_LR0|TDS_FT_San-Serif|TDS_PS_L4|TDS_ExpandablePassages1|"
    // +
    // "TDS_TTSPause1|TDS_ParentExempt0|TDS_TTS_Inst|TDS_MfR1|TDS_PoD_Stim&TDS_PoD_Item|TDS_BP0|TDS_F_S12|"
    // +
    // "A003|A008|A016|A020|A021|TDS_Highlight1|ENU|TDS_SS0|TDS_TTSAA_Volume&TDS_TTSAA_Pitch|TDS_ST1|TDS_CC0|";

    String accommodations = "MA:A402;MA:A501;SS:A208;SS:A204;SS:A307;SS:A402;SS:A104;SS:A302;SS:A212;SS:A213;SS:A107;SS:A308;SS:A501;SS:A103;SS:A401;SS:A105;SS:A303;SS:A101;SS:A404;SC:ENU;RE:ENU-Braille;WR:ENU;SS:TDS_TTS0;MA:TDS_TTS_Item;SC:TDS_TTS0";

    Boolean restoreRTS = null;
    _Ref<UUID> testoppkeyRef = new _Ref<> ();
    Long diff = 0L;
    Date starttime = null;
    Date now = null;

    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      starttime = new Date ();
      _dll._OpenExistingOpportunity_SP (connection, clientname, testee, testkey, opportunity, sessionId, browserId,
          newstatusRef, accommodations, restoreRTS, testoppkeyRef);
      now = new Date ();
      diff = now.getTime () - starttime.getTime ();
      System.out.println (String.format ("_OpenExistingOpportunity latency: %d millisec", (long) diff));

      assertTrue ("pending".equalsIgnoreCase (newstatusRef.get ()));
      assertTrue (testoppkeyRef.get () != null);
      assertTrue (UUID.fromString ("29487C57-C237-423F-9D5A-E65310BE3D08").equals (testoppkeyRef.get ()));
      _logger.info (String.format ("NewStatus: %s", newstatusRef.get ()));
      _logger.info (String.format ("testoppkey: %s", testoppkeyRef.get ().toString ()));
    }
  }

  // declare @oppkey uniqueidentifier;
  // exec _OpenNewOpportunity 'Hawaii_PT', 29138,
  // '(Hawaii_PT)HSA-Science-8-Fall-2012-2013', 1,
  // '681CB7E6-05C0-4122-8132-007F1692FE83',
  // '29487C57-C237-423F-9D5A-E65310BE3D08', 'GUEST', 'GUEST',
  // 'pending', null, @oppkey output
  // select @oppkey
  // @Test
  public void test_OpenNewOpportunity_SP1 () throws ReturnStatusException, SQLException {
    // success case
    Long testee = 29138L;
    // Long testee = -717L;
    // String testkey = "(Hawaii_PT)HSA-Mathematics-5-Fall-2012-2013";
    String clientname = "Hawaii_PT";
    String testkey = "(Hawaii_PT)HSA-Science-8-Fall-2012-2013";

    Integer opportunity = 1;
    UUID sessionKey = UUID.fromString ("681CB7E6-05C0-4122-8132-007F1692FE83");
    UUID browserKey = UUID.fromString ("29487C57-C237-423F-9D5A-E65310BE3D08");
    String testeeID = "GUEST";
    String testeeName = "GUEST";
    String status = "pending";
    String guestAccommodations = null;
    _Ref<UUID> testoppkeyRef = new _Ref<> ();
    // this method returns result set only in case of failure
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      SingleDataResultSet result = _dll._OpenNewOpportunity_SP (connection, clientname, testee, testkey, opportunity,
          sessionKey, browserKey, testeeID, testeeName, status, guestAccommodations, testoppkeyRef);

      _logger.info (String.format ("testoppkey: %s", testoppkeyRef.get ().toString ()));
      // new opportunity has different oppkey each time
      assertTrue (testoppkeyRef.get () != null);
      assertTrue (result == null);
    }
  }

  // declare @oppkey uniqueidentifier;
  // exec _OpenNewOpportunity 'Oregon_PT', 29138,
  // '(Hawaii_PT)HSA-Science-8-Fall-2012-2013', 1,
  // '681CB7E6-05C0-4122-8132-007F1692FE83',
  // '29487C57-C237-423F-9D5A-E65310BE3D08', 'GUEST', 'GUEST',
  // 'pending', null, @oppkey output
  // select @oppkey
  // @Test
  public void test_OpenNewOpportunity_SP2 () throws ReturnStatusException, SQLException {
    // obvious failure case
    Long testee = 29138L;
    String clientname = "Oregon_PT";
    String testkey = "(Hawaii_PT)HSA-Science-8-Fall-2012-2013";

    Integer opportunity = 1;
    UUID sessionKey = UUID.fromString ("681CB7E6-05C0-4122-8132-007F1692FE83");
    UUID browserKey = UUID.fromString ("29487C57-C237-423F-9D5A-E65310BE3D08");
    String testeeID = "GUEST";
    String testeeName = "GUEST";
    String status = "pending";
    String guestAccommodations = null;
    _Ref<UUID> testoppkeyRef = new _Ref<> ();
    // this method returns result set only in case of failure
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      SingleDataResultSet result = _dll._OpenNewOpportunity_SP (connection, clientname, testee, testkey, opportunity,
          sessionKey, browserKey, testeeID, testeeName, status, guestAccommodations, testoppkeyRef);

      assertTrue (result != null);

      DbResultRecord record = (result.getCount () > 0 ? result.getRecords ().next () : null);
      assertTrue (record != null);
      assertTrue ("failed".equalsIgnoreCase (record.<String> get ("status")));
      assertTrue ("There is no active testing window for this student at this time [11712]".equalsIgnoreCase (record.<String> get ("reason")));
      _logger.info (String.format ("Status: %s", record.<String> get ("status")));
      _logger.info (String.format ("Reason: %s", record.<String> get ("reason")));

    }
  }

  // select dbo.MakeItemscoreString ('9CC6B36B-6A38-436D-9EDB-00010D25F2A7',
  // ';', ',')

  // TODO: Elena use this statement to get oppkey values
  // select _fk_TestOpportunity, position
  // from testeeresponse where _efk_ITSItem is not null and (IsInactive is null
  // or IsInactive = 0)
  // order by RAND() limit 20
  // @Test
  public void testMakeItemscoreString_FN () throws SQLException, ReturnStatusException {

    UUID oppkey = UUID.fromString ("9CC6B36B-6A38-436D-9EDB-00010D25F2A7"); // ("DBC443E7-2628-4E34-8709-00B9E4182B25");
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      String str = _dll.MakeItemscoreString_FN (connection, oppkey);
      _logger.info (String.format ("itemScoreString: %s", str));

      assertTrue ("159-472,,OP;159-473,,OP;159-474,,OP".equalsIgnoreCase (str));
    }
  }

  // @Test
  public void testAuditScores_FN () throws SQLException, ReturnStatusException {

    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      String clientname = "Oregon";
      boolean flag = _dll.AuditScores_FN (connection, clientname);
      assertTrue (flag == true);
      clientname = "oregon_pt";
      flag = _dll.AuditScores_FN (connection, clientname);
      assertTrue (flag == false);
    }
  }

  // execute this on server side:
  // delete from testeeaccommodations where _fk_TestOpportunity =
  // '5a68df75-3c91-4414-88ae-b33ea48e571e'
  // exec _InitOpportunityAccommodations '5A68DF75-3C91-4414-88AE-B33EA48E571E',
  // null
  // @Test
  public void test_InitOpportunityAccommodations_SP () throws SQLException, ReturnStatusException {
    UUID oppkey = UUID.fromString ("5A68DF75-3C91-4414-88AE-B33EA48E571E");
    String accoms = null; // "TDS_BT0|TDS_PS_L0|TDS_Highlight1|TDS_TTSAA_Volume&TDS_TTSAA_Pitch&TDS_TTSAA_Rate";

    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      final String query = "delete from testeeaccommodations where _fk_TestOpportunity = ${oppkey}";
      SqlParametersMaps parms = (new SqlParametersMaps ()).put ("oppkey", oppkey);

      _myDllHelper.executeStatement (connection, query, parms, false);

      SingleDataResultSet result = _dll._InitOpportunityAccommodations_SP (connection, oppkey, accoms);
      // returns non-null result only in case of error
      assertTrue (result == null);
    }
  }

  // @Test
  public void testRandom () throws Exception {
    Random generator = new Random ();
    double r = Math.round (generator.nextDouble () * 1000);
    Integer i = (int) r;
    Integer thisIntSize = 50015;
    Integer intervalIndex = 20;
    int nextPos = (i / thisIntSize) + intervalIndex;
    _logger.info (String.format ("nextPos: %d", nextPos));
  }

  // @Test
  public void testCreateTempTbl () throws Exception {
    // check that we can creae two temp tables with the same original name.
    // Framework should create unique final names
    try (SQLConnection con = _myDllHelper.getSQLConnection ()) {

      DataBaseTable tbl1 = new DataBaseTable ("abc", _myDllHelper.getDatabaseDialect ()).addColumn ("colimn1", SQL_TYPE_To_JAVA_TYPE.INT);
      con.createTemporaryTable (tbl1);
      _logger.info (String.format ("tbl1: %s", tbl1.getTableName ()));

      DataBaseTable tbl2 = new DataBaseTable ("abc", _myDllHelper.getDatabaseDialect ()).addColumn ("column2", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 20);
      con.createTemporaryTable (tbl2);
      _logger.info (String.format ("tbl2: %s", tbl2.getTableName ()));

      con.dropTemporaryTable (tbl1);
      con.dropTemporaryTable (tbl2);
    }
  }

  // @Test
  public void testIB_GetTestProperties_SP () throws SQLException, ReturnStatusException {
    String testKey = "(Oregon)ELPA_6-8-Winter-2012-2013";
    Date starttime = null;
    Date now = null;
    Long diff = 0L;

    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      starttime = new Date ();
      MultiDataResultSet result = _dll.IB_GetTestProperties_SP (connection, testKey);
      now = new Date ();
      diff = now.getTime () - starttime.getTime ();
      System.out.println (String.format ("IB_GetTestProperties latency: %d millisec", (long) diff));

      Iterator<SingleDataResultSet> resultSetIterator = result.getResultSets ();
      int resultSetNumber = 0;
      while (resultSetIterator.hasNext ()) {
        SingleDataResultSet resultSet = resultSetIterator.next ();

        Iterator<DbResultRecord> records = resultSet.getRecords ();

        while (records.hasNext ()) {
          DbResultRecord record = records.next ();
          if (resultSetNumber == 0)
            dumpRecord1 (record);
          else if (resultSetNumber == 1)
            dumpRecord2 (record);
        }
        resultSetNumber++;
      }
    } catch (SQLException exp) {

      _logger.error (exp.getMessage ());
      throw exp;
    }
  }

  private void dumpRecord1 (DbResultRecord record) throws ReturnStatusException {
    _logger.info (String.format ("TestID: %s", record.<String> get ("testID")));
    _logger.info (String.format ("GradeCode: %s", record.<String> get ("GradeCode")));
    _logger.info (String.format ("Subject: %s", record.<String> get ("Subject")));
    _logger.info (String.format ("SelectionAlgorithm: %s", record.<String> get ("selectionAlgorithm")));
    _logger.info (String.format ("DisplayName: %s", record.<String> get ("DisplayName")));
    _logger.info (String.format ("SortOrder: %d", record.<Integer> get ("SortOrder")));
    _logger.info (String.format ("AccommodationFamily: %s", record.<String> get ("AccommodationFamily")));
    _logger.info (String.format ("IsSelectable: %s", record.<Boolean> get ("IsSelectable")));
    _logger.info (String.format ("ScoreByTDS: %s", record.<Boolean> get ("ScoreByTDS")));
    _logger.info (String.format ("ValidateCompleteness: %s", record.<Boolean> get ("validateCompleteness")));
    _logger.info (String.format ("MaxOpportunities: %d", record.<Integer> get ("MaxOpportunities")));
    _logger.info (String.format ("MinItems: %d", record.<Integer> get ("MinItems")));
    _logger.info (String.format ("MaxItems: %d", record.<Integer> get ("MaxItems")));
    _logger.info (String.format ("Prefetch: %d", record.<Integer> get ("Prefetch")));
    _logger.info (String.format ("IsSegmented: %s", record.<Boolean> get ("IsSegmented")));
    _logger.info (String.format ("_Key: %s", record.<String> get ("_Key")));
    _logger.info (String.format ("formSelection: %s", record.<String> get ("formSelection")));
  }

  private void dumpRecord2 (DbResultRecord record) throws ReturnStatusException {
    _logger.info (String.format ("segmentID: %s", record.<String> get ("segmentID")));
    _logger.info (String.format ("segmentPosition: %d", record.<Integer> get ("segmentPosition")));
    _logger.info (String.format ("SegmentLabel: %s", record.<String> get ("SegmentLabel")));
    _logger.info (String.format ("IsPermeable: %d", record.<Integer> get ("IsPermeable")));
    _logger.info (String.format ("entryApproval: %d", record.<Integer> get ("entryApproval")));
    _logger.info (String.format ("exitApproval: %d", record.<Integer> get ("exitApproval")));
    _logger.info (String.format ("itemReview: %s", record.<Boolean> get ("itemReview")));

  }

  // @Test
  public void testIB_GetTestGrades_SP () throws SQLException, ReturnStatusException {

    String clientname = "Oregon";
    String testkey = "(Oregon)Oregon-SpeakingOnly-ELPA Speaking-K-1-Winter-2012-2013";
    Integer sessionType = 0;

    Long cumDiff = 0L;
    Long diff = 0L;
    Date starttime = null;
    Date now = null;
    int num = 10;
    SingleDataResultSet result = null;
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      for (int i = 0; i < num; i++) {
        starttime = new Date ();
        result = _dll.IB_GetTestGrades_SP (connection, clientname, testkey, sessionType);
        now = new Date ();
        diff = now.getTime () - starttime.getTime ();
        cumDiff += diff;
      }
      System.out.println (String.format ("Average: %d millisec", (long) cumDiff / num));

      // final String CMD =
      // "BEGIN; SET NOCOUNT ON; exec IB_GetTestGrades ${clientname}, ${testkey}, ${sessionType}; end;";
      // SqlParametersMaps parms = (new SqlParametersMaps ()).put ("clientname",
      // clientname).put ("testkey", testkey).put ("sessionType", sessionType);
      // cumDiff = 0L;
      // for (int i = 0;i < num; i++) {
      // starttime = new Date ();
      // result = _myDllHelper.executeStatement (connection, CMD, parms,
      // false).getResultSets ().next ();
      // now = new Date ();
      // diff = now.getTime () - starttime.getTime ();
      // cumDiff += diff;
      // }
      // System.out.println (String.format ("Average for SP: %d millisec",
      // (long) cumDiff / num));

      Iterator<DbResultRecord> records = result.getRecords ();

      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        _logger.info (String.format ("===Record==="));
        _logger.info (String.format ("TestID: %s", record.<String> get ("TestID")));
        _logger.info (String.format ("GradeCode: %s", record.<String> get ("Grade")));
        _logger.info (String.format ("Suject: %s", record.<String> get ("EnrolledSubject")));

      }
    } catch (SQLException exp) {

      _logger.error (exp.getMessage ());
      throw exp;
    }
  }

  // exec P_OpenSession 'DB5AACDA-D724-4F49-98B1-C89E5267AB32', 12, 0,
  // '505497A1-3D19-4231-91B7-C25751D0BC41'
  // @Test
  public void testP_OpenSession_SP () throws ReturnStatusException, SQLException {

    UUID sessionKey = UUID.fromString ("DB5AACDA-D724-4F49-98B1-C89E5267AB32");
    Integer numhours = 12;
    Boolean suppressReport = false;
    UUID browserKey = UUID.fromString ("505497A1-3D19-4231-91B7-C25751D0BC41");
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      SingleDataResultSet rs = _dll.P_OpenSession_SP (connection, sessionKey, numhours, suppressReport, browserKey);
      // Note that if suppressREport is true, this method returns null
      assertTrue (rs != null);
      DbResultRecord record = (rs.getCount () > 0 ? rs.getRecords ().next () : null);
      if (record != null) {
        _logger.info (String.format ("Status: %s, Reason:%s", record.<String> get ("status"), record.<String> get ("reason")));
        assertTrue ("open".equalsIgnoreCase (record.<String> get ("status")));
      }
    }
  }

  // exec P_GetTestForms 'Oregon', 'OAKS-Writing-HS', 1
  // @Test
  public void testP_GetTestForms_SP () throws ReturnStatusException, SQLException {
    String clientname = "Oregon";
    String testId = "OAKS-Writing-HS";
    Integer sessionType = 1;

    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {

      SingleDataResultSet result = _dll.P_GetTestForms_SP (connection, clientname, testId, sessionType);
      Iterator<DbResultRecord> records = result.getRecords ();
      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        _logger.info (String.format ("windiwID: %s", record.<String> get ("windowid")));
        _logger.info (String.format ("windowMax: %d", record.<Integer> get ("windowmax")));
        _logger.info (String.format ("modeMax: %d", record.<Integer> get ("modemax")));
        _logger.info (String.format ("startDate: %s", record.<Date> get ("startDate")));
        _logger.info (String.format ("endDate: %s", record.<Date> get ("endDate")));
        _logger.info (String.format ("formStart: %s", record.<Date> get ("formStart")));
        _logger.info (String.format ("formEnd: %s", record.<Date> get ("formEnd")));
        _logger.info (String.format ("formKey: %s", record.<String> get ("formKey")));
        _logger.info (String.format ("formID: %s", record.<String> get ("formId")));
        _logger.info (String.format ("language: %s", record.<String> get ("language")));
        _logger.info (String.format ("mode: %s", record.<String> get ("mode")));
        _logger.info (String.format ("testKey: %s", record.<String> get ("testKey")));
        _logger.info (String.format ("windowSession: %d", record.<Integer> get ("windowsession")));
        _logger.info (String.format ("modeSession: %d", record.<Integer> get ("modesession")));
      }
      _logger.info (String.format ("Total Number of recs: %d", result.getCount ()));
      assertTrue (result.getCount () == 8);
    }
  }

  // exec T_GetOpportunityComment 'F4E3EA6C-301D-448F-854C-2899A897B2B2',
  // 'TESTITEM', 4
  // @Test
  public void testT_GetOpportunityComment_SP () throws SQLException, ReturnStatusException {

    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      UUID oppkey = UUID.fromString ("F4E3EA6C-301D-448F-854C-2899A897B2B2");
      String context = "TESTITEM";
      Integer itemposition = 4;
      SingleDataResultSet res = _dll.T_GetOpportunityComment_SP (connection, oppkey, context, itemposition);
      Iterator<DbResultRecord> records = res.getRecords ();
      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        System.err.println (record.<String> get ("comment"));
        assertTrue ("Test Comment".equalsIgnoreCase (record.<String> get ("comment")));
      }
    }
    _logger.info ("T_GetOpportunityComment_SP w/o exception!");
  }

  // exec T_GetOpportunityItems '1447A2F5-517E-4915-B981-033EF02DAFB6'
  // @Test
  public void testT_GetOpportunityItems_SP () throws SQLException, ReturnStatusException {
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      UUID oppkey = UUID.fromString ("1447A2F5-517E-4915-B981-033EF02DAFB6");

      SingleDataResultSet res = _dll.T_GetOpportunityItems_SP (connection, oppkey);
      Iterator<DbResultRecord> records = res.getRecords ();
      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        System.err.println ("ItemBank: " + record.<Long> get ("ItemBank"));
        System.err.println ("Item: " + record.<Long> get ("Item"));
        System.err.println ("position: " + record.<Integer> get ("position"));
        System.err.println ("page: " + record.<Integer> get ("page"));
        System.err.println ("segment: " + record.<Integer> get ("segment"));
        System.err.println ("segmentID: " + record.<String> get ("segmentID"));
        System.err.println ("GroupID: " + record.<String> get ("GroupID"));
        System.err.println ("ResponseSequence: " + record.<Integer> get ("ResponseSequence"));
        System.err.println ("IsRequired: " + record.<Boolean> get ("IsRequired"));
        System.err.println ("dateCreated: " + record.<String> get ("dateCreated"));
        System.err.println ("groupItemsRequired: " + record.<Integer> get ("groupItemsRequired"));
        System.err.println ("Score: " + record.<Integer> get ("Score"));
        System.err.println ("Mark: " + record.<Boolean> get ("Mark"));
        System.err.println ("OpportunityRestart: " + record.<Integer> get ("OpportunityRestart"));
        System.err.println ("IsFieldTest: " + record.<Boolean> get ("IsFieldTest"));
        System.err.println ("IsSelected: " + record.<Boolean> get ("IsSelected"));
        System.err.println ("Isvalid: " + record.<Boolean> get ("IsValid"));
        System.err.println ("Format: " + record.<String> get ("Format"));
        Boolean isVisible = record.<Boolean> get ("isVisible");
        System.err.println ("isVisible: " + isVisible);
        Boolean readOnly = record.<Boolean> get ("readOnly");
        System.err.println ("readOnly: " + readOnly);

        if (record.hasColumn ("visitcount"))
          System.err.println ("visitvount: " + record.<Integer> get ("visitcount"));
        if (record.hasColumn ("dateLastVisited")) {
          if (record.<Date> get ("dateLastVisited") != null)
            System.err.println (String.format ("dateLastVisited: %s", AbstractDateUtilDll.getDateAsFormattedString (record.<Date> get ("dateLastVisited"))));
          else
            System.err.println ("dateLastVisited: null");
        }
      }
      _logger.info (String.format ("Total records: %d", res.getCount ()));
      assertTrue (res.getCount () == 3);
    }
  }

  // exec T_ValidateAccess
  // '29487C57-C237-423F-9D5A-E65310BE3D08','CDB8BF0C-3F0E-47A8-8609-96E2D4D61E3D',
  // '99F2085F-0E3F-48B7-AA5E-A325C5D6A84C'
  // @Test
  public void testT_ValidateAccess_SP1 () throws SQLException, ReturnStatusException {
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      // success case
      UUID oppkey = UUID.fromString ("29487C57-C237-423F-9D5A-E65310BE3D08");// ("AAA3217A-2A82-405C-8ED9-20F1AAF29ECE");
      UUID session = UUID.fromString ("CDB8BF0C-3F0E-47A8-8609-96E2D4D61E3D");// ("C4967141-EEE0-4F3C-9FA3-B7CD4C0BC4B1");
      UUID browserId = UUID.fromString ("99F2085F-0E3F-48B7-AA5E-A325C5D6A84C");

      SingleDataResultSet result = _dll.T_ValidateAccess_SP (connection, oppkey, session, browserId);
      DbResultRecord record = result.getCount () > 0 ? result.getRecords ().next () : null;
      assertTrue (record != null);
      String reason = record.<String> get ("reason");
      System.err.println (String.format ("Status: %s", record.<String> get ("status")));
      System.err.println (String.format ("Reason: %s", reason));
      assertTrue ("Sorry! You're not allowed into this system.  Please give this number to your test administrator. [10208]".equalsIgnoreCase (reason));
    }
  }

  // exec T_ValidateAccess
  // 'AAA3217A-2A82-405C-8ED9-20F1AAF29ECE','C4967141-EEE0-4F3C-9FA3-B7CD4C0BC4B1',
  // '99F2085F-0E3F-48B7-AA5E-A325C5D6A84F'
  // @Test
  public void testT_ValidateAccess_SP2 () throws SQLException, ReturnStatusException {
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      // failure case
      UUID oppkey = UUID.fromString ("AAA3217A-2A82-405C-8ED9-20F1AAF29ECE");
      UUID session = UUID.fromString ("C4967141-EEE0-4F3C-9FA3-B7CD4C0BC4B1");
      UUID browserId = UUID.fromString ("99F2085F-0E3F-48B7-AA5E-A325C5D6A84F");
      SingleDataResultSet result = _dll.T_ValidateAccess_SP (connection, oppkey, session, browserId);
      DbResultRecord record = result.getCount () > 0 ? result.getRecords ().next () : null;
      assertTrue (record != null);
      String reason = record.<String> get ("reason");
      assertTrue ("There was a problem with the system.  Please give this number to your test administrator. [10206]".equalsIgnoreCase (reason));
      System.err.println (String.format ("Status: %s", record.<String> get ("status")));
      System.err.println (String.format ("Reason: %s", reason));

    }
  }

  // exec T_ValidateAccess
  // 'AAA3217A-2A82-405C-8ED9-20F1AAF29ECE','C4967141-EEE0-4F3C-9FA3-B7CD4C0BC4B3',
  // '99F2085F-0E3F-48B7-AA5E-A325C5D6A84C'
  // @Test
  public void testT_ValidateAccess_SP3 () throws SQLException, ReturnStatusException {
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      // failure case
      UUID oppkey = UUID.fromString ("AAA3217A-2A82-405C-8ED9-20F1AAF29ECE");
      UUID session = UUID.fromString ("C4967141-EEE0-4F3C-9FA3-B7CD4C0BC4B3");
      UUID browserId = UUID.fromString ("99F2085F-0E3F-48B7-AA5E-A325C5D6A84C");
      SingleDataResultSet result = _dll.T_ValidateAccess_SP (connection, oppkey, session, browserId);
      DbResultRecord record = result.getCount () > 0 ? result.getRecords ().next () : null;
      assertTrue (record != null);
      String reason = record.<String> get ("reason");
      assertTrue ("There was a problem with the system.  Please give this number to your test administrator. [10206]".equalsIgnoreCase (reason));
      System.err.println (String.format ("Status: %s", record.<String> get ("status")));
      System.err.println (String.format ("Reason: %s", reason));
    }
  }

  // exec T_ValidateAccess
  // 'BFEC30A7-EA34-4732-8AAC-0CE53DE3618B','D14B409C-3795-474B-86B5-C72DAA22A300',
  // '10F734B9-D289-4485-B475-ABED21BE120C'
  // @Test
  public void testT_ValidateAccess_SP4 () throws SQLException, ReturnStatusException {
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      // failure case

      UUID oppkey = UUID.fromString ("BFEC30A7-EA34-4732-8AAC-0CE53DE3618B");
      UUID session = UUID.fromString ("D14B409C-3795-474B-86B5-C72DAA22A300");
      UUID browserId = UUID.fromString ("10F734B9-D289-4485-B475-ABED21BE120C");
      SingleDataResultSet result = _dll.T_ValidateAccess_SP (connection, oppkey, session, browserId);
      DbResultRecord record = result.getCount () > 0 ? result.getRecords ().next () : null;
      assertTrue (record != null);
      String reason = record.<String> get ("reason");
      assertTrue ("You cannot test in this session. Ask your Test Administrator for help. [10207]".equalsIgnoreCase (reason));
      System.err.println (String.format ("Status: %s", record.<String> get ("status")));
      System.err.println (String.format ("Reason: %s", reason));
    }
  }

  // exec T_ValidateCompleteness '7483AC2F-F74F-4D6C-9EBD-1E4ADC89859E',
  // 'Overall:Attempted:99:x;Overall:Attempted:1:x;Overall:Attempted:0:x;Overall:Attempted:aa:x'
  // @Test
  public void testT_ValidateCompleteness_SP () throws SQLException, ReturnStatusException {

    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {

      UUID oppKey = UUID.fromString ("7483AC2F-F74F-4D6C-9EBD-1E4ADC89859E");

      // String scoresString =
      // "Overall:Attempted:99:x;Overall:Attempted:1:x;Overall:Attempted:0:x;Overall:Attempted:aa:x";
      String scoresString = "Overall:Attempted:99:x;asdasd:asasd:00;Overall:Attempted:1:x;Overall:Attempted:0:x;1231:12312";
      Integer rs = _dll.T_ValidateCompleteness_SP (connection, oppKey, scoresString);
      assertTrue (rs == 0);
      System.err.println (String.format ("T_ValidateCompleteness returned : %s", (rs == null ? "null" : rs.toString ())));

      oppKey = UUID.fromString ("7483AC2F-F74F-4D6C-9EBD-1E4ADC89859A");
      rs = _dll.T_ValidateCompleteness_SP (connection, oppKey, scoresString);
      assertTrue (rs == null);
      System.err.println (String.format ("T_ValidateCompleteness returned : %s", (rs == null ? "null" : rs.toString ())));
    }
  }

  // declare @s uniqueidentifier, @b uniqueidentifier;
  // set @s =newid();
  // set @b = newid();
  // exec T_RecordServerLatency 'abc', 'F4E3EA6C-301D-448F-854C-2899A897B2B2',
  // @s, @b, 4
  // @Test
  public void testT_RecordServerLatency_SP () throws SQLException, ReturnStatusException {
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {

      String operation = "abc";
      UUID oppkey = UUID.fromString ("F4E3EA6C-301D-448F-854C-2899A897B2B2");

      UUID session = UUID.randomUUID ();
      UUID browser = UUID.randomUUID ();
      Integer serverLatency = 4;

      int cntInsert = _dll.T_RecordServerLatency_SP (connection, operation, oppkey, session, browser, serverLatency);
      System.err.println (String.format ("Number inserted: %d", cntInsert));
    }
  }

  // exec T_RecordClientLatency '29A51278-BC85-4887-88CF-7457CF2C5421',
  // 'F4E3EA6C-301D-448F-854C-2899A897B2B2',
  // '9CC6B36B-6A38-436D-9EDB-00010D25F2A7', 1, 2, 3, null, null, 40, 50, 60,
  // null, null,
  // 'TDS_CalcSci&TDS_CalcGraphing&TDS_CalcRegress&TDS_CalcMatrices;TDS_FT_San-Serif;TDS_SC0;'
  // @Test
  public void testT_RecordClientLatency_SP () throws SQLException, ReturnStatusException {
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      UUID oppkey = UUID.fromString ("29A51278-BC85-4887-88CF-7457CF2C5421");
      UUID session = UUID.fromString ("F4E3EA6C-301D-448F-854C-2899A897B2B2");
      UUID browser = UUID.fromString ("9CC6B36B-6A38-436D-9EDB-00010D25F2A7");
      int itempage = 1;
      int numitems = 2;
      int visitCount = 3;
      Date createDate = null;
      Date loadDate = null;
      int loadTime = 40;
      int requestTime = 50;
      int visitTime = 60;
      Integer loadAttempts = null;
      Date visitDate = null;
      String toolsUsed = "TDS_CalcSci&TDS_CalcGraphing&TDS_CalcRegress&TDS_CalcMatrices;TDS_FT_San-Serif;TDS_SC0;";

      _dll.T_RecordClientLatency_SP (connection, oppkey, session, browser, itempage, numitems, visitCount, createDate, loadDate,
          loadTime, requestTime, visitTime, loadAttempts, visitDate, toolsUsed);
    }
  }

  // @Test
  public void testT_RecordClientLatency_XML_SP () throws SQLException, ReturnStatusException {
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      String latencies = "<latencies> "
          + "<latency page=\"1\" numitems=\"2\" visitcount=\"3\" createdate=\"2012/11/12\" loaddate=\"2012/12/15\" loadtime=\"10\" requesttime=\"15\" visittime=\"20\" loadattempts=\"4\" visitdate=\"12/30/2012\"> "
          + "</latency> "
          + "<latency page=\"10\" numitems=\"2\" visitcount=\"3\" createdate=\"2000/12/30\" loaddate=\"2012/12/25\" loadtime=\"10\" requesttime=\"15\" visittime=\"20\" loadattempts=\"4\" visitdate=\"12/30/2012\"> "
          + "</latency> "
          + "</latencies>";
      UUID oppkey = UUID.fromString ("29A51278-BC85-4887-88CF-7457CF2C5421");
      UUID session = UUID.randomUUID ();
      UUID browser = UUID.randomUUID ();
      _dll.T_RecordClientLatency_XML_SP (connection, oppkey, session, browser, latencies);
    }
  }

  // @Test
  public void testT_RecordToolsUsed_SP () throws SQLException, ReturnStatusException {
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      String toolsused = "<toolsused> "
          + "<tool page=\"1\" tooltype=\"tooltype1\" toolcode=\"toolcode1\"/>"
          + "<tool page=\"2\" tooltype=\"tooltype2\" toolcode=\"toolcode2\"/>"
          + "</toolsused>";
      UUID oppkey = UUID.fromString ("29A51278-BC85-4887-88CF-7457CF2C5421");

      _dll.T_RecordToolsUsed_SP (connection, oppkey, toolsused);
    }
  }

  // @Test
  public void testT_RecordComment_SP () throws SQLException, ReturnStatusException {
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {

      UUID sessionKey = UUID.fromString ("7385099D-0E67-4B74-86EF-88F9CB27234D");
      Long testee = 0L;
      String comment = "my comments";
      String context = "my context";
      UUID oppkey = UUID.fromString ("9CC6B36B-6A38-436D-9EDB-00010D25F2A7");
      Integer itemposition = 1;

      _dll.T_RecordComment_SP (connection, sessionKey, testee, comment, context, oppkey, itemposition);
    }
  }

  // exec T_REmoveResponse '9CC6B36B-6A38-436D-9EDB-00010D25F2A7', '159-471', 1,
  // '2012/09/26 19:12:49.480',
  // '38893e8d-a5d2-4bf8-906e-3c2cbfbacc30','A3161C78-314F-4337-90D4-2B0FCB50C9DF'
  // @Test
  public void testT_RemoveResponse_SP () throws ReturnStatusException, SQLException {
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      // success case
      UUID oppkey = UUID.fromString ("9CC6B36B-6A38-436D-9EDB-00010D25F2A7");
      String itemId = "159-472";
      Integer position = 2;
      String dateCreated = "2012-09-26 19:12:49.593";
      UUID session = UUID.fromString ("38893e8d-a5d2-4bf8-906e-3c2cbfbacc30");
      UUID browser = UUID.fromString ("A3161C78-314F-4337-90D4-2B0FCB50C9DF");

      // this method returns not null result only in case of failure
      SingleDataResultSet result = _dll.T_RemoveResponse_SP (connection, oppkey, itemId, position, dateCreated, session, browser);
      // assertTrue(result == null);
      if (result != null) {
        DbResultRecord record = (result.getCount () > 0 ? result.getRecords ().next () : null);
        if (record != null) {
          _logger.info (String.format ("Status: %s", record.<String> get ("status")));
          _logger.info (String.format ("Reason: %s", record.<String> get ("reason")));
        }
      }
    }
  }

  // exec T_SetItemMark '9CC6B36B-6A38-436D-9EDB-00010D25F2A7',
  // '38893e8d-a5d2-4bf8-906e-3c2cbfbacc30',
  // 'A3161C78-314F-4337-90D4-2B0FCB50C9DF', 1, 0
  // @Test
  public void testT_SetItemMark_SP () throws ReturnStatusException, SQLException {
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      UUID oppkey = UUID.fromString ("9CC6B36B-6A38-436D-9EDB-00010D25F2A7");
      UUID session = UUID.fromString ("38893e8d-a5d2-4bf8-906e-3c2cbfbacc30");
      UUID browserId = UUID.fromString ("A3161C78-314F-4337-90D4-2B0FCB50C9DF");
      Integer position = 1;
      Boolean mark = false;
      // this method always returns result, both in case of success and failure
      SingleDataResultSet result = _dll.T_SetItemMark_SP (connection, oppkey, session, browserId, position, mark);
      assertTrue (result != null);
      DbResultRecord record = (result.getCount () > 0 ? result.getRecords ().next () : null);
      if (record != null) {
        _logger.info (String.format ("Status: %s", record.<String> get ("status")));
        _logger.info (String.format ("Reason: %s", record.<String> get ("reason")));
      }
    }
  }

  // @Test
  public void testT_SetOpportunityStatus_SP () throws ReturnStatusException, SQLException {
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      UUID oppkey = UUID.fromString ("9CC6B36B-6A38-436D-9EDB-00010D25F2A7");
      String status = "paused";
      UUID sessionKey = UUID.fromString ("38893e8d-a5d2-4bf8-906e-3c2cbfbacc30");
      UUID browserKey = UUID.fromString ("A3161C78-314F-4337-90D4-2B0FCB50C9DF");
      String comment = "my lovely comments";

      // this method always returns result, both in case of success and failure
      SingleDataResultSet result = _dll.T_SetOpportunityStatus_SP (connection, oppkey, status, sessionKey, browserKey, comment);
      assertTrue (result != null);
      DbResultRecord record = (result.getCount () > 0 ? result.getRecords ().next () : null);
      if (record != null) {
        _logger.info (String.format ("Status: %s", record.<String> get ("status")));
        _logger.info (String.format ("Reason: %s", record.<String> get ("reason")));
      }
    }
  }

  // execT_OpenTestOpportunity 2633,
  // '(Delaware)DCAS-Alt1-PAPER-Reading-3-5-Fall-2011-2012',
  // '38893e8d-a5d2-4bf8-906e-3c2cbfbacc30',
  // 'A3161C78-314F-4337-90D4-2B0FCB50C9DF'
  // @Test
  public void testT_OpenTestOpportunity_SP1 () throws ReturnStatusException, SQLException {
    // This case does not work!
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {

      Long testee = 2633L;
      UUID sessionKey = UUID.fromString ("38893e8d-a5d2-4bf8-906e-3c2cbfbacc30");
      UUID browserKey = UUID.randomUUID ();
      String testkey = "(Delaware)DCAS-Alt1-PAPER-Reading-3-5-Fall-2011-2012";
      SingleDataResultSet result = _dll.T_OpenTestOpportunity_SP (connection, testee, testkey, sessionKey, browserKey);
      if (result != null) {
        DbResultRecord record = (result.getCount () > 0 ? result.getRecords ().next () : null);
        if (record != null) {
          _logger.info (String.format ("Status: %s", record.<String> get ("status")));
          _logger.info (String.format ("Reason: %s", record.<String> get ("reason")));
        }
      }
    }
  }

  // Elena: To find out suitable test parameters I ran the following query:
  // select distinct I._key as testkey, _fk_Session as sessionKey
  // from ITEMBANK_tblSetofAdminsubjects I, sessiontests S,
  // TDSCONFIGS_Client_TestProperties T
  // where I._Key like '(Oregon)%'
  // and S._efk_testid = I.testid
  // and T.testid = S._efk_testid
  // and dbo.IsSessionOpen (_fk_Session) = 1
  // @Test
  public void testT_OpenTestOpportunity_SP2 () throws ReturnStatusException, SQLException {

    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {

      Long testee = 2633L;
      UUID sessionKey = UUID.fromString ("D09CC4F9-FF95-4679-A441-0115E1BCFB7C");// ("C2E01F55-0FA4-4E43-AAD9-B62233F8A490");
      UUID browserKey = UUID.randomUUID ();
      String testkey = "(Oregon)OAKS-Math-3-Fall-2012-2013";
      SingleDataResultSet result = _dll.T_OpenTestOpportunity_SP (connection, testee, testkey, sessionKey, browserKey);
      if (result != null) {
        DbResultRecord record = (result.getCount () > 0 ? result.getRecords ().next () : null);
        if (record != null) {
          _logger.info (String.format ("Status: %s", record.<String> get ("status")));
          _logger.info (String.format ("Reason: %s", record.<String> get ("reason")));
        }
      }
    }
  }

  // declare @reasonRef varchar(200);
  // declare @tsteeKeyRef bigint;
  // exec _T_ValidateTesteeLogin 'delaware', '9999999005', 'Stat-2', @reasonRef
  // output, @TsteekeyRef output;
  // select @reasonRef output, @TsteekeyRef output;
  // @Test
  public void test_T_ValidateTesteeLogin_SP1 () throws ReturnStatusException, SQLException {
    String clientname = "Delaware";
    String testeeId = "9999999005";
    String sessionId = "Stat-2";
    _Ref<String> reasonRef = new _Ref<> ();
    _Ref<Long> testeeKeyRef = new _Ref<> ();
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      _dll._T_ValidateTesteeLogin_SP (connection, clientname, testeeId, sessionId, reasonRef, testeeKeyRef);
      _logger.info (String.format ("Reason: %s", reasonRef.get ()));
      _logger.info (String.format ("TesteeKey: %d", testeeKeyRef.get ()));
    }
  }

  // declare @reasonRef varchar(200);
  // declare @TsteeKeyRef bigint;
  // exec _T_ValidateTesteeLogin 'Hawaii', '9999999227', 'abd', @reasonRef
  // output, @TsteekeyRef output;
  // select @reasonRef output, @TsteekeyRef output;
  // @Test
  public void test_T_ValidateTesteeLogin_SP2 () throws ReturnStatusException, SQLException {
    String clientname = "Hawaii";
    String testeeId = "9999999227";
    String sessionId = "abd";
    _Ref<String> reasonRef = new _Ref<> ();
    _Ref<Long> testeeKeyRef = new _Ref<> ();
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      _dll._T_ValidateTesteeLogin_SP (connection, clientname, testeeId, sessionId, reasonRef, testeeKeyRef);
      _logger.info (String.format ("Reason: %s", reasonRef.get ()));
      _logger.info (String.format ("TesteeKey: %d", testeeKeyRef.get ()));
    }
  }

  // declare @reasonRef varchar(200);
  // declare @TsteeKeyRef bigint;
  // exec _T_ValidateTesteeLogin 'Oregon', '999999932', 'Mercury-9', @reasonRef
  // output, @TsteekeyRef output;
  // select @reasonRef output, @TsteekeyRef output;
  // @Test
  public void test_T_ValidateTesteeLogin_SP3 () throws ReturnStatusException, SQLException {
    String clientname = "Oregon";
    String testeeId = "999999932";
    String sessionId = "Mercury-9";
    _Ref<String> reasonRef = new _Ref<> ();
    _Ref<Long> testeeKeyRef = new _Ref<> ();
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      _dll._T_ValidateTesteeLogin_SP (connection, clientname, testeeId, sessionId, reasonRef, testeeKeyRef);
      _logger.info (String.format ("Reason: %s", reasonRef.get ()));
      _logger.info (String.format ("TesteeKey: %d", testeeKeyRef.get ()));
    }
  }

  // exec T_Login 'Oregon_PT', 'ID:GUEST;Firstname:JOHN', 'GUEST Session'
  // @Test
  public void testT_Login_SP () throws ReturnStatusException, SQLException {

    String clientname = "Oregon_PT";
    Map<String, String> keyValues = new HashMap<String, String> ();
    keyValues.put ("ID", "GUEST");
    keyValues.put ("Firstname", "JOHN");
    String sessionId = "GUEST Session";

    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {

      MultiDataResultSet sets = _dll.T_Login_SP (connection, clientname, keyValues, sessionId);
      Iterator<SingleDataResultSet> iter = sets.getResultSets ();
      int setIdx = 0;
      while (iter.hasNext ()) {
        SingleDataResultSet set = iter.next ();
        if (setIdx == 0) {
          DbResultRecord record = set.getRecords ().next ();
          if (record != null) {
            _logger.info (String.format ("Status: %s", record.<String> get ("Status")));
            if (record.hasColumn ("entityKey"))
              _logger.info (String.format ("EntityKey: %d", record.<Long> get ("entityKey")));
          }
        }
        if (setIdx == 1) {
          Iterator<DbResultRecord> recIter = set.getRecords ();
          while (recIter.hasNext ()) {
            DbResultRecord record = recIter.next (); // TDS_ID, outval as
                                                     // [Value], Label,
                                                     // SortOrder, atLogin
            _logger.info (String.format ("TDS_ID: %s", record.<String> get ("TDS_ID")));
            _logger.info (String.format ("value: %s", record.<String> get ("value")));
            _logger.info (String.format ("label: %s", record.<String> get ("label")));
            _logger.info (String.format ("sortOrder: %d", record.<Integer> get ("sortOrder")));
            _logger.info (String.format ("atLogin: %s", record.<String> get ("atLogin")));
          }
        }
        setIdx++;
      }

    }
  }

  // exec T_GetTestforScoring '42863E38-DD83-45CC-A72B-4EF2B8B32E69', ';', ','
  // @Test
  public void testT_GetTestforScoring_SP1 () throws SQLException, ReturnStatusException {

    UUID oppkey = UUID.fromString ("42863E38-DD83-45CC-A72B-4EF2B8B32E69");// ("C5FAD533-8F95-4ECF-AB79-02A8D2AB3C40");
                                                                           // ("9CC6B36B-6A38-436D-9EDB-00010D25F2A7");
    Character rowdelim = ';';
    Character coldelim = ',';
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      SingleDataResultSet result = _dll.T_GetTestforScoring_SP (connection, oppkey, rowdelim, coldelim);
      DbResultRecord record = (result.getCount () > 0 ? result.getRecords ().next () : null);
      assertTrue (record != null);
      String status = record.<String> get ("status");
      String reason = record.<String> get ("reason");
      _logger.info (String.format ("Status: %s", status));
      _logger.info (String.format ("Reason: %s", reason));
      assertTrue ("failed".equalsIgnoreCase (status));
      assertTrue ("COMPLETE: Do Not Score".equalsIgnoreCase (reason));
    }
  }

  // exec T_GetTestforScoring 'C5FAD533-8F95-4ECF-AB79-02A8D2AB3C40', ';', ','
  // @Test
  public void testT_GetTestforScoring_SP2 () throws SQLException, ReturnStatusException {

    UUID oppkey = UUID.fromString ("C5FAD533-8F95-4ECF-AB79-02A8D2AB3C40");

    Character rowdelim = ';';
    Character coldelim = ',';
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      SingleDataResultSet result = _dll.T_GetTestforScoring_SP (connection, oppkey, rowdelim, coldelim);
      DbResultRecord record = (result.getCount () > 0 ? result.getRecords ().next () : null);
      assertTrue (record != null);
      String status = record.<String> get ("status");
      String reason = record.<String> get ("reason");
      _logger.info (String.format ("Status: %s", status));
      _logger.info (String.format ("Reason: %s", reason));
      assertTrue ("Unofficial score".equalsIgnoreCase (status));
      assertTrue (reason == null);
      assertTrue (record.hasColumn ("itemstring") == true);
      assertTrue (record.hasColumn ("dateCompleted") == true);
      assertTrue ("".equals (record.<String> get ("itemstring")));
      assertTrue ("2012-08-09 10:17:32.763".equalsIgnoreCase (record.<Date> get ("dateCompleted").toString ()));
      if (record.hasColumn ("itemstring"))
        _logger.info (String.format ("itemString: %s", record.<String> get ("itemstring")));
      if (record.hasColumn ("rowdelim"))
        _logger.info (String.format ("rowdelim: %s", record.<String> get ("rowdelim")));
      if (record.hasColumn ("coldelim"))
        _logger.info (String.format ("coldelim: %s", record.<String> get ("coldelim")));
      if (record.hasColumn ("dateCompleted")) {
        Date dateCompleted = record.<Date> get ("dateCompleted");

        _logger.info (String.format ("dateCompleted: %s", (dateCompleted == null ? "null" : dateCompleted.toString ())));
      }
    }
  }

  // exec T_GetTestforScoring '9CC6B36B-6A38-436D-9EDB-00010D25F2A7', ';', ','
  // @Test
  public void testT_GetTestforScoring_SP3 () throws SQLException, ReturnStatusException {

    UUID oppkey = UUID.fromString ("9CC6B36B-6A38-436D-9EDB-00010D25F2A7");
    Character rowdelim = ';';
    Character coldelim = ',';
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      SingleDataResultSet result = _dll.T_GetTestforScoring_SP (connection, oppkey, rowdelim, coldelim);
      DbResultRecord record = (result.getCount () > 0 ? result.getRecords ().next () : null);
      assertTrue (record != null);
      String status = record.<String> get ("status");
      String reason = record.<String> get ("reason");
      _logger.info (String.format ("Status: %s", status));
      _logger.info (String.format ("Reason: %s", reason));
      assertTrue ("failed".equalsIgnoreCase (status));
      assertTrue ("Blueprint not satisfied".equalsIgnoreCase (reason));
    }
  }

  // exec T_GetTestforCompleteness '9CC6B36B-6A38-436D-9EDB-00010D25F2A7', ';',
  // ','
  // @Test
  public void testT_GetTestforCompleteness_SP1 () throws SQLException, ReturnStatusException {

    UUID oppkey = UUID.fromString ("9CC6B36B-6A38-436D-9EDB-00010D25F2A7"); // ("DBC443E7-2628-4E34-8709-00B9E4182B25");
    Character rowdelim = ';';
    Character coldelim = ',';

    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {

      SingleDataResultSet result = _dll.T_GetTestforCompleteness_SP (connection, oppkey, rowdelim, coldelim);
      DbResultRecord record = (result.getCount () > 0 ? result.getRecords ().next () : null);
      assertTrue (record != null);
      String scoreString = record.<String> get ("scorestring");
      _logger.info (String.format ("scoreString: %s", scoreString));
      assertTrue ("159-472,,OP;159-473,,OP;159-474,,OP".equalsIgnoreCase (scoreString));
    }
  }

  // exec T_GetSession 'Oregon', 'GUEST Session'
  // @Test
  public void testT_GetSession_SP1 () throws SQLException, ReturnStatusException {
    // failure case
    String clientname = "Oregon";
    String sessionId = "GUEST Session";
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {

      MultiDataResultSet sets = _dll.T_GetSession_SP (connection, clientname, sessionId);
      assertTrue (sets.getUpdateCount () == 1);
      SingleDataResultSet set = sets.getResultSets ().next ();

      DbResultRecord record = set.getRecords ().next ();
      assertTrue (record != null);
      assertTrue (record.hasColumn ("reason") == true);
      String status = record.<String> get ("Status");
      String reason = record.<String> get ("reason");
      _logger.info (String.format ("Status: %s", status));
      _logger.info (String.format ("Reason: %s", reason));
      assertTrue ("failed".equalsIgnoreCase (status));
      assertTrue ("You are not allowed to log in without a Test Administrator [11716]".equalsIgnoreCase (reason));
    }
  }

  // exec T_GetSession 'Oregon', 'Mercury-9'
  // @Test
  public void testT_GetSession_SP2 () throws SQLException, ReturnStatusException {

    String clientname = "Oregon";
    String sessionId = "Mercury-9";
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {

      MultiDataResultSet sets = _dll.T_GetSession_SP (connection, clientname, sessionId);
      assertTrue (sets.getUpdateCount () == 2);
      Iterator<SingleDataResultSet> iter = sets.getResultSets ();
      int setIdx = 0;
      while (iter.hasNext ()) {
        SingleDataResultSet set = iter.next ();

        if (setIdx == 0) {
          DbResultRecord record = set.getRecords ().next ();
          assertTrue (record != null);
          String status = record.<String> get ("Status");
          String reason = record.<String> get ("reason");
          _logger.info (String.format ("Status: %s", status));
          _logger.info (String.format ("Reason: %s", reason));
          assertTrue ("open".equalsIgnoreCase (status));
          assertTrue ("".equals (reason));
        }

        if (setIdx == 1) {
          DbResultRecord record = set.getRecords ().next ();
          UUID sessionKey = record.<UUID> get ("sessionKey");
          Long proctorKey = record.<Long> get ("proctorKey");
          String proctorId = record.<String> get ("proctorId");
          String proctorName = record.<String> get ("proctorName");
          String name = record.<String> get ("name");
          _logger.info (String.format ("SessionKey: %s", sessionKey));
          _logger.info (String.format ("proctorKey: %d", proctorKey));
          _logger.info (String.format ("proctorId: %s", proctorId));
          _logger.info (String.format ("proctorName: %s", proctorName));
          _logger.info (String.format ("name: %s", name));
          _logger.info (String.format ("dateCreated: %s", record.<Date> get ("dateCreated")));
          _logger.info (String.format ("dateBegin: %s", record.<Date> get ("dateBegin")));
          _logger.info (String.format ("dateEnd: %s", record.<Date> get ("dateEnd")));
          assertTrue (sessionKey.equals (UUID.fromString ("9773CE47-2EB6-4E14-82A7-6A7E6F43C41B")));
          assertTrue (proctorKey.equals (263L));
          assertTrue ("dtsa@air.org".equalsIgnoreCase (proctorId));
          assertTrue ("DTSA AIRORGNew".equalsIgnoreCase (proctorName));
          assertTrue ("".equals (name));
        }
        setIdx++;
      }
    }
  }

  // exec T_GetResponseRationales '9CC6B36B-6A38-436D-9EDB-00010D25F2A7',
  // '38893E8D-A5D2-4BF8-906E-3C2CBFBACC30',
  // 'A3161C78-314F-4337-90D4-2B0FCB50C9DF'
  // @Test
  public void testT_GetResponseRationales_SP1 () throws SQLException, ReturnStatusException {
    // success case
    UUID oppkey = UUID.fromString ("9CC6B36B-6A38-436D-9EDB-00010D25F2A7");
    UUID sessionKey = UUID.fromString ("38893E8D-A5D2-4BF8-906E-3C2CBFBACC30");
    UUID browserKey = UUID.fromString ("A3161C78-314F-4337-90D4-2B0FCB50C9DF");

    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      SingleDataResultSet result = _dll.T_GetResponseRationales_SP (connection, oppkey, sessionKey, browserKey);
      assertTrue (result.getCount () == 22);
      Iterator<DbResultRecord> iter = result.getRecords ();
      while (iter.hasNext ()) {
        DbResultRecord record = iter.next ();

        _logger.info (String.format ("Position: %d", record.<Integer> get ("position")));
        _logger.info (String.format ("page: %d", record.<Integer> get ("page")));
        _logger.info (String.format ("score: %d", record.<Integer> get ("score")));
        _logger.info (String.format ("scorepoint: %d", record.<Integer> get ("scorepoint")));
        _logger.info (String.format ("format: %s", record.<String> get ("format")));
        _logger.info (String.format ("response: %s", record.<String> get ("response")));
        _logger.info (String.format ("scorerationale: %s", record.<String> get ("scorerationale")));

      }
    }
  }

  // exec T_GetResponseRationales '9CC6B36B-6A38-436D-9EDB-00010D25F2A8',
  // '38893E8D-A5D2-4BF8-906E-3C2CBFBACC30',
  // 'A3161C78-314F-4337-90D4-2B0FCB50C9DF'

  // @Test
  public void testT_GetResponseRationales_SP2 () throws SQLException, ReturnStatusException {
    // failure case
    UUID oppkey = UUID.fromString ("9CC6B36B-6A38-436D-9EDB-00010D25F2A8");
    UUID sessionKey = UUID.fromString ("38893E8D-A5D2-4BF8-906E-3C2CBFBACC30");
    UUID browserKey = UUID.fromString ("A3161C78-314F-4337-90D4-2B0FCB50C9DF");

    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      SingleDataResultSet result = _dll.T_GetResponseRationales_SP (connection, oppkey, sessionKey, browserKey);
      assertTrue (result.getCount () == 1);
      DbResultRecord record = result.getRecords ().next ();
      assertTrue (record != null);
      String status = record.<String> get ("status");
      String reason = record.<String> get ("Reason");
      _logger.info (String.format ("Status: %s", status));

      _logger.info (String.format ("Reason: %s", reason));
      assertTrue ("failed".equalsIgnoreCase (status));
      assertTrue ("The session keys do not match; please consult your test administrator [-----]".equalsIgnoreCase (reason));

    }
  }

  // exec T_GetPTSetup 'Oregon_PT'
  // @Test
  public void testT_GetPTSetup_SP1 () throws SQLException, ReturnStatusException {
    // success case
    String clientname = "Oregon_PT";
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      SingleDataResultSet result = _dll.T_GetPTSetup_SP (connection, clientname);
      DbResultRecord record = result.getRecords ().next ();
      assertTrue (record != null);

      String loginAs = record.<String> get ("loginAs");
      String firstname = record.<String> get ("firstname");
      String sessionId = record.<String> get ("sessionId");
      UUID sessionKey = record.<UUID> get ("sessionKey");
      _logger.info (String.format ("loginAs: %s", loginAs));
      _logger.info (String.format ("firstname: %s", firstname));
      _logger.info (String.format ("sessionId: %s", sessionId));
      _logger.info (String.format ("sessionKey: %s", sessionKey.toString ()));
      assertTrue ("GUEST".equalsIgnoreCase (loginAs));
      assertTrue ("GUEST".equalsIgnoreCase (firstname));
      assertTrue ("GUEST session".equalsIgnoreCase (sessionId));
      assertTrue (UUID.fromString ("502165F2-909C-4FFB-8AB1-0EB2A398C8FF").equals (sessionKey));
    }
  }

  // exec T_GetPTSetup 'Oregon'
  // @Test
  public void testT_GetPTSetup_SP2 () throws SQLException, ReturnStatusException {
    // failure case
    String clientname = "Oregon";
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      SingleDataResultSet result = _dll.T_GetPTSetup_SP (connection, clientname);
      DbResultRecord record = result.getRecords ().next ();
      assertTrue (record != null);
      String status = record.<String> get ("status");
      String reason = record.<String> get ("Reason");
      _logger.info (String.format ("Status: %s", status));
      _logger.info (String.format ("Reason: %s", reason));

      assertTrue ("failed".equalsIgnoreCase (status));
      assertTrue ("This system is not available for guest login [10179]".equalsIgnoreCase (reason));
    }
  }

  // exec T_GetEligibleTests -1, 'D09CC4F9-FF95-4679-A441-0115E1BCFB7C', '2'
  // @Test
  public void testT_GetEligibleTests () throws SQLException, ReturnStatusException {

    Long testee = -1L;// 421021L;
    UUID sessionKey = UUID.fromString ("D09CC4F9-FF95-4679-A441-0115E1BCFB7C");
    String grade = "2";
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      SingleDataResultSet result = _dll.T_GetEligibleTests_SP (connection, testee, sessionKey, grade);
      assertTrue (result.getCount () == 3);
      Iterator<DbResultRecord> iter = result.getRecords ();
      int i = 0;
      while (iter.hasNext ()) {
        DbResultRecord record = iter.next ();

        _logger.info (String.format ("testKey: %s", record.<String> get ("testKey")));
        _logger.info (String.format ("Test: %s", record.<String> get ("test")));
        if (i == 0)
          assertTrue ("Oregon-CoreOnly-ELPA-2-3".equalsIgnoreCase (record.<String> get ("test")));
        else if (i == 1)
          assertTrue ("Oregon-SpeakingOnly-ELPA Speaking-2-3".equalsIgnoreCase (record.<String> get ("test")));
        else if (i == 2)
          assertTrue ("ELPA_2-3".equalsIgnoreCase (record.<String> get ("test")));
        _logger.info (String.format ("opportunity: %d", record.<Integer> get ("opportunity")));
        _logger.info (String.format ("mode: %s", record.<String> get ("mode")));
        _logger.info (String.format ("displayName: %s", record.<String> get ("displayname")));
        _logger.info (String.format ("maxopps: %d", record.<Integer> get ("maxopps")));
        assertTrue (record.<Integer> get ("maxopps") == 1);
        _logger.info (String.format ("subject: %s", record.<String> get ("subject")));
        assertTrue ("ELPA".equalsIgnoreCase (record.<String> get ("subject")));
        _logger.info (String.format ("grade: %s", record.<String> get ("grade")));
        _logger.info (String.format ("sortOrder: %d", record.<Integer> get ("sortOrder")));
        _logger.info (String.format ("status: %s", record.<String> get ("status")));
        assertTrue ("pending".equalsIgnoreCase (record.<String> get ("status")));
        _logger.info (String.format ("reason: %s", record.<String> get ("reason")));
        assertTrue (record.<String> get ("reason") == null);
        i++;
      }
    }
  }

  // declare @msg varchar(300);
  // exec _InsertTestoppScores 'DBC443E7-2628-4E34-8709-00B9E4182B25',
  // '159-457,0,OP,12.345E-3;159-458,-1,OP;159-459,-1,OP;159-460,-1,OP;159-461,-1,OP;159-462,1,OP;159-475,0,OP;159-463,0,OP;159-464,1,OP;159-465,-1,OP;159-466,0,OP;159-467,0,OP;159-468,1,OP;159-469,0,OP;159-470,-1,OP;159-526,1,OP;159-518,0,OP;159-519,-1,OP;159-520,1,OP;159-515,,OP;159-523,,OP;159-522,,OP;159-521,,OP;159-524,,OP',
  // ',', ';', @msg output
  // select @msg
  // @Test
  public void test_InsertTestoppScores_SP () throws SQLException, ReturnStatusException {
    UUID testoppkey = UUID.fromString ("DBC443E7-2628-4E34-8709-00B9E4182B25");
    String scoreString = "159-457,0,OP,12.345E-3;159-458,-1,OP;159-459,-1,OP;159-460,-1,OP;159-461,-1,OP;159-462,1,OP;159-475,0,OP;159-463,0,OP;159-464,1,OP;159-465,-1,OP;159-466,0,OP;159-467,0,OP;159-468,1,OP;159-469,0,OP;159-470,-1,OP;159-526,1,OP;159-518,0,OP;159-519,-1,OP;159-520,1,OP;159-515,,OP;159-523,,OP;159-522,,OP;159-521,,OP;159-524,,OP";
    Character coldelim = ',';
    Character rowdelim = ';';
    _Ref<String> msgRef = new _Ref<> ();
    Date starttime = null, now = null;
    Long diff = 0L;
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      starttime = new Date ();
      _dll._InsertTestoppScores_SP (connection, testoppkey, scoreString, coldelim, rowdelim, msgRef);
      now = new Date ();
      diff = now.getTime () - starttime.getTime ();
      System.out.println (String.format ("_InsertTestOppScores latency: %d millisec", (long) diff));
      _logger.info (String.format ("errMsg: %s", msgRef.get ()));
      assertTrue (msgRef.get () == null);
    }
  }

  // exec S_InsertTestScores 'DBC443E7-2628-4E34-8709-00B9E4182B25',
  // '159-457,0,OP,12.345E-3;159-458,-1,OP;159-459,-1,OP;159-460,-1,OP;159-461,-1,OP;159-462,1,OP;159-475,0,OP;159-463,0,OP;159-464,1,OP;159-465,-1,OP;159-466,0,OP;159-467,0,OP;159-468,1,OP;159-469,0,OP;159-470,-1,OP;159-526,1,OP;159-518,0,OP;159-519,-1,OP;159-520,1,OP;159-515,,OP;159-523,,OP;159-522,,OP;159-521,,OP;159-524,,OP',
  // ';', ',';

  // exec T_GetTestforCompleteness 'DBC443E7-2628-4E34-8709-00B9E4182B25',
  // ';',','
  // @Test
  public void testS_InsertTestScores_SP () throws SQLException, ReturnStatusException {

    UUID oppkey = UUID.fromString ("DBC443E7-2628-4E34-8709-00B9E4182B25");

    String scoreString = "159-457,0,OP,12.345E-3;159-458,-1,OP;159-459,-1,OP;159-460,-1,OP;159-461,-1,OP;159-462,1,OP;159-475,0,OP;159-463,0,OP;159-464,1,OP;159-465,-1,OP;159-466,0,OP;159-467,0,OP;159-468,1,OP;159-469,0,OP;159-470,-1,OP;159-526,1,OP;159-518,0,OP;159-519,-1,OP;159-520,1,OP;159-515,,OP;159-523,,OP;159-522,,OP;159-521,,OP;159-524,,OP";
    Character coldelim = ',';
    Character rowdelim = ';';
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      SingleDataResultSet result = _dll.S_InsertTestScores_SP (connection, oppkey, scoreString, rowdelim, coldelim);
      DbResultRecord record = result.getRecords ().next ();
      assertTrue (record != null);
      assertTrue ("success".equals (record.<String> get ("status")));
      _logger.info (String.format ("Status: %s", record.<String> get ("status")));
      _logger.info (String.format ("Reason: %s", record.<String> get ("Reason")));

      result = _dll.T_GetTestforCompleteness_SP (connection, oppkey, rowdelim, coldelim);
      record = (result.getCount () > 0 ? result.getRecords ().next () : null);
      assertTrue (record != null);
      String myScoreString = record.<String> get ("scorestring");
      _logger.info (String.format ("scoreString: %s", myScoreString));
      assertTrue ("159-457,0,OP;159-458,-1,OP;159-459,-1,OP;159-460,-1,OP;159-461,-1,OP;159-462,1,OP;159-475,0,OP;159-463,0,OP;159-464,1,OP;159-465,-1,OP;159-466,0,OP;159-467,0,OP;159-468,1,OP;159-469,0,OP;159-470,-1,OP;159-526,1,OP;159-518,0,OP;159-519,-1,OP;159-520,1,OP;159-515,,OP;159-523,,OP;159-522,,OP;159-521,,OP;159-524,,OP"
          .equalsIgnoreCase (myScoreString));
    }
  }

  // TODO: Elena. On mysql tried this search to find clientname that would
  // producde nont-empty result.
  // Found none.
  // select distinct hex (R._fk_TestOpportunity) , O.clientname
  // from testeeresponse R, testeeaccommodations A, testeeresponsescore S,
  // testopportunity O, session SS
  // where R._fk_TestOpportunity = A._fk_TestOpportunity
  // and R._fk_TestOpportunity = S._fk_TestOpportunity
  // and A.AccType = 'Language'
  // and S.position = R.position and S.ResponseSequence = R.ResponseSequence
  // and SS.sessionType = 0
  // and R._fk_TestOpportunity = O._key
  // and S.ScoreStatus = 'WaitingForMachineScore'
  // and O._fk_Session = SS._Key

  // exec S_GetScoreItems 'Oregon_PT', 0, 0, 20000, 0
  // @Test
  public void testS_GetScoreItems_SP () throws SQLException, ReturnStatusException {
    String clientname = "Oregon_PT";
    // it is set to 0 so that we can repeat this test without waiting for
    // pendingMinutes to pass
    Integer pendingMinutes = 0;
    Integer minAttempts = 0;
    // very high number in place of default =9 to avoid going over max attempts
    // while repeating tests
    Integer maxAttempts = 20000;
    Integer sessionType = 0;
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {

      SingleDataResultSet result = _dll.S_GetScoreItems_SP (connection, clientname, pendingMinutes, minAttempts, maxAttempts, sessionType);
      // assertTrue(result.getCount () == 3);
      Iterator<DbResultRecord> iter = result.getRecords ();
      while (iter.hasNext ()) {
        DbResultRecord record = iter.next ();
        _logger.info (String.format ("oppKey: %s", record.<UUID> get ("oppKey").toString ()));
        _logger.info (String.format ("testKey: %s", record.<String> get ("testKey")));
        _logger.info (String.format ("itemFile: %s", record.<String> get ("itemFile")));
        Integer position = record.<Integer> get ("position");
        Integer responseSequence = record.<Integer> get ("responseSequence");
        Long bankKey = record.<Long> get ("bankKey");
        Long itemKey = record.<Long> get ("itemKey");
        String response = record.<String> get ("response");
        UUID scoremark = record.<UUID> get ("scoremark");
        // UUID scoremark = UUID.fromString (record.<String> get ("scoremark"));
        String language = record.<String> get ("language");
        Integer attempts = record.<Integer> get ("attempts");
        String segmentId = record.<String> get ("segmentId");
        _logger.info (String.format ("position: %d", position));
      }
    }
  }

  // exec T_IsTestComplete '8F9972ED-C7C0-4910-A517-0E16D8D50FDE'
  // @Test
  public void testT_IsTestComplete_SP () throws SQLException, ReturnStatusException {

    UUID oppkey = UUID.fromString ("8F9972ED-C7C0-4910-A517-0E16D8D50FDE");

    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      SingleDataResultSet result = _dll.T_IsTestComplete_SP (connection, oppkey);
      DbResultRecord record = result.getRecords ().next ();
      assertTrue (record != null);
      assertTrue (record.<Integer> get ("segmentsIncomplete") == 4);
      assertTrue (record.<Boolean> get ("iscomplete") == false);
      _logger.info (String.format ("IsComplete: %s", record.<Boolean> get ("iscomplete")));
      _logger.info (String.format ("segmentsIncomplete: %d", record.<Integer> get ("segmentsIncomplete")));
    }
  }

  // @Test
  public void testGetCurrentTests_FN () throws SQLException,
      ReturnStatusException {

    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      System.err.println ("Testing - CurrentTests_FN");
      String clientname =
          "Oregon";
      int sessionType = 0;
      DataBaseTable tbl = _dll.GetCurrentTests_FN
          (connection, clientname, sessionType);

      final String SQL_QUERY = "select * from ${tblName}";
      Map<String, String> par = new HashMap<String, String> ();
      par.put ("tblName", tbl.getTableName
          ());

      SingleDataResultSet result = _myDllHelper.executeStatement (connection,
          _myDllHelper.fixDataBasenames (SQL_QUERY, par), null, false).getResultSets
          ().next ();

      Iterator<DbResultRecord> records = result.getRecords ();
      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        System.err.println ("testID : " + record.<String> get ("TestID"));
        System.err.println ("subject : " + record.<String> get ("subject"));
        System.err.println ("language : " + record.<String> get ("language"));
        System.err.println ("maxOpportunities : " + record.<Integer> get
            ("maxOpportunities"));
        System.err.println ("Label : " + record.<String> get
            ("label"));
        System.err.println ("grade : " + record.<String> get ("grade"));
        System.err.println ("requireEnrollment : " + record.<Boolean> get
            ("requireEnrollment"));
        System.err.println ("enrolledsubject : " +
            record.<String> get ("enrolledsubject"));
        System.err.println ("IsSelectable : " + record.<Boolean> get
            ("IsSelectable"));
        System.err.println ("sortOrder : " + record.<Integer> get
            ("sortOrder"));
        System.err.println ("windowMax : " + record.<Integer> get
            ("windowMax"));
        System.err.println ("windowID : " + record.<String> get
            ("windowID"));
        System.err.println ("startdate : " + record.<Date> get
            ("startdate"));
        System.err.println ("endDate : " + record.<Date> get
            ("endDate"));
        System.err.println ("mode : " + record.<String> get ("mode"));
        System.err.println ("testkey : " + record.<String> get ("testkey"));
        System.err.println ("modeMax : " + record.<Integer> get ("modeMax"));
        System.err.println ("windowSession : " + record.<Integer> get
            ("windowSession"));
        System.err.println ("modesession : " + record.<Integer>
            get ("modeSession"));
      }
      System.err.println ("Testing - CurrentTests_FN - Complete");
    } catch (SQLException exp) {
      _logger.error (exp.getMessage ());
      throw exp;
    }
  }

  // @Test
  public void test__IsGradeEquiv_FN () throws SQLException,
      ReturnStatusException {

    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      System.err.println ("Testing - __IsGradeEquiv_FN");
      String grade1 = "1234";
      String grade2 = "1234";
      Boolean res = _dll.__IsGradeEquiv_FN (grade1,
          grade2);
      System.err.println ("Result = " + res);

      grade1 = "1234";
      grade2 = "5678";
      res = _dll.__IsGradeEquiv_FN (grade1,
          grade2);
      System.err.println ("Result = " + res);

      grade1 = "Test";
      grade2 = "Test";
      res = _dll.__IsGradeEquiv_FN (grade1,
          grade2);
      System.err.println ("Result = " + res);

      grade1 = "1234.0";
      grade2 = "1234.0";
      res = _dll.__IsGradeEquiv_FN (grade1,
          grade2);
      System.err.println ("Result = " + res);
      System.err.println ("Testing - __IsGradeEquiv_FN - Complete");
    } catch (SQLException exp) {
      _logger.error (exp.getMessage ());
      throw exp;
    }
  }

  @Test
  @Ignore("Autowire issues")
  public void test__EligibleTests_SP () throws SQLException, ReturnStatusException {

    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      System.err.println ("Testing - EligibleTests_SP");
      String clientname = "Oregon";
      long testee = 421021;
      // long testee = -1;
      Integer sessiontype = 0;
      String grade = "10";
      Boolean debug = true;
      Date starttime = null;
      Date now = null;
      Long diff = 0L;

      starttime = new Date ();
      SingleDataResultSet result = _dll.__EligibleTests_SP (connection, clientname, testee, sessiontype, grade, debug);
      now = new Date ();
      diff = now.getTime () - starttime.getTime ();
      System.out.println (String.format ("__EligibleTests latency: %d millisec", (long) diff));
      Iterator<DbResultRecord> records = result.getRecords ();
      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        _logger.info ("testkey : " + record.<String> get ("testkey"));
        _logger.info ("testid : " + record.<String> get ("testid"));
        _logger.info ("maxopps : " + record.<Integer> get ("maxopps"));
        _logger.info ("mode : " + record.<String> get ("mode"));
      }

    } catch (SQLException exp) {
      _logger.error (exp.getMessage ());
      throw exp;
    }
  }

  // @Test
  public void ClientItemStimulusPath_FN () throws SQLException, ReturnStatusException {

    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      System.err.println ("Testing - ClientItemStimulusPath_FN");
      String clientname = "Delaware";
      String itemkey = "148-10000";
      String testkey = "(Delaware)DCAS-Reading-4-Fall-2012-2013";
      String path = _dll.ClientItemStimulusPath_FN (connection, clientname, testkey, itemkey);
      _logger.info (String.format ("path = %s", path));
      String output = "D:\\DataFiles\\BB_Files\\tds_airws_org\\TDSCore_2012-2013\\Bank-148\\Stimuli\\Stim-148-331\\stim-148-331.xml";
      assertTrue (output.equals (path) == true);
      _logger.info ("Testing - ClientItemStimulusPath_FN - Complete");
    } catch (SQLException exp) {
      _logger.error (exp.getMessage ());
      throw exp;
    }
  }

  // @Test
  public void testT_GetOpportunityAccommodations_SP () throws SQLException, ReturnStatusException {

    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      UUID oppkey = UUID.fromString ("846FFB4B-B2AE-4ADD-BA1C-0984D6DFE681");
      UUID session = UUID.fromString ("81C5D50D-4FAC-43C9-AE85-FCC5F4081154");
      UUID browserId = UUID.fromString ("B1277D06-547A-4D3B-91B6-5BD4F06C4DCB");
      SingleDataResultSet res = _dll.T_GetOpportunityAccommodations_SP (connection, oppkey, session, browserId);
      Iterator<DbResultRecord> records = res.getRecords ();
      System.out.println ("Segment    | AccType                                        | AccCode                                               "
          + "   |  Isapproved |             RecordUsage");
      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        System.out.format (" %1d         |  %25s                     |  %25s                             |  %b        |   %b",
            record.<Integer> get ("Segment"), record.<String> get ("accType"), record.<String> get ("accCode"),
            record.<Boolean> get ("isApproved"), record.<Boolean> get ("recordUsage"));
        System.out.println ();
        System.err.println ("Segment: " + record.<Integer> get ("Segment"));
        System.err.println ("AccType: " + record.<String> get ("accType"));
        System.err.println ("AccCode: " + record.<String> get ("accCode"));
        System.err.println ("IsApproved: " + record.<Boolean> get ("isApproved"));
        System.err.println ("RecordUsage: " + record.<Boolean> get ("recordUsage"));
      }
    } catch (SQLException exp) {
      _logger.error (exp.getMessage ());
      throw exp;
    }
  }

  // @Test
  public void testT_GetItemGroup_SP () throws SQLException, ReturnStatusException {
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      UUID oppkey = UUID.fromString ("13D734F8-A604-47AF-BF0C-55D08E7839FA");
      int pageNumber = 1;
      String groupID = null;
      String dateCreated = null;
      UUID session = null;
      UUID browserId = null;
      boolean validateAccess = true;
      Long diff = 0L;
      Date starttime = null;
      Date now = null;

      starttime = new Date ();
      SingleDataResultSet res = _dll.T_GetItemGroup_SP (connection, oppkey, pageNumber, groupID, dateCreated, session, browserId, validateAccess);
      now = new Date ();
      diff = now.getTime () - starttime.getTime ();
      System.out.println (String.format ("T_GetItemGroup latency: %d millisec", (long) diff));

      Iterator<DbResultRecord> records = res.getRecords ();
      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        Long itemBank = record.<Long> get ("ItemBank");
        Long Item = record.<Long> get ("Item");
        Integer position = record.<Integer> get ("Position");
        Integer page = record.<Integer> get ("Page");
        Integer score = record.<Integer> get ("Score");
        Boolean mark = record.<Boolean> get ("Mark");
        String response = record.<String> get ("Response");
        Boolean isFieldTest = record.<Boolean> get ("IsFieldTest");
        Boolean isSelected = record.<Boolean> get ("IsSelected");
        Boolean isRequired = record.<Boolean> get ("IsRequired");
        String format = record.<String> get ("Format");
        Boolean isVisible = record.<Boolean> get ("Isvisible");
        Boolean readnOnly = record.<Boolean> get ("ReadOnly");
        Integer isPrintable = record.<Integer> get ("IsPrintable");
        Long opportunityRestart = record.<Long> get ("OpportunityRestart");
        System.err.println ("ItemBank : " + record.<Long> get ("ItemBank"));
        System.err.println ("Item : " + record.<Long> get ("Item"));
        System.err.println ("Position : " + record.<Integer> get ("Position"));
        System.err.println ("Page : " + record.<Integer> get ("Page"));
        System.err.println ("Score : " + record.<Integer> get ("Score"));
        System.err.println ("Mark : " + record.<Boolean> get ("Mark"));
        System.err.println ("Response : " + record.<String> get ("Response"));
        System.err.println ("IsFieldTest : " + record.<Boolean> get ("IsFieldTest"));
        System.err.println ("IsSelected : " + record.<Boolean> get ("IsSelected"));
        System.err.println ("IsRequired : " + record.<Boolean> get ("IsRequired"));
        System.err.println ("Format : " + record.<String> get ("Format"));
        System.err.println ("IsVisible : " + record.<Boolean> get ("Isvisible"));
        System.err.println ("ReadOnly : " + record.<Boolean> get ("ReadOnly"));
        System.err.println ("GroupId : " + record.<String> get ("GroupId"));
        System.err.println ("Datecreated : " + record.<Date> get ("Datecreated"));
        System.err.println ("ResponseSequence : " + record.<Integer> get ("ResponseSequence"));
        System.err.println ("ResponseLength : " + record.<Integer> get ("ResponseLength"));
        System.err.println ("IsValid : " + record.<Boolean> get ("Isvalid"));
        System.err.println ("GroupItemsRequired : " + record.<Integer> get ("GroupItemsRequired"));
        System.err.println ("Segment : " + record.<Integer> get ("Segment"));
        System.err.println ("SegmentId : " + record.<String> get ("SegmentId"));
        System.err.println ("ItemFile : " + record.<String> get ("ItemFile"));
        System.err.println ("Stimulusfile : " + record.<String> get ("StimulusFile"));
        System.err.println ("IsPrintable : " + record.<Integer> get ("IsPrintable"));
        System.err.println ("OpportunityRestart : " + record.<Long> get ("OpportunityRestart"));
      }
    } catch (SQLException exp) {
      _logger.error (exp.getMessage ());
      throw exp;
    }
  }

  /*
   * Test Cases from Sai:
   * 
   * --CASE#1: Failure Case - line 36
   * 
   * select dbo._ValidateItemsAccess ('9D7AA6E1-60E3-4F33-99EF-DD0330B75F06',
   * null, null)
   * 
   * --CASE#2: Failure Case - line 43
   * 
   * select dbo._ValidateItemsAccess ('089AC62A-5F23-4560-9B16-00022FFD1A13',
   * 'D3EC8568-6BB6-4929-8674-D2299B798AF7',
   * '616AB32B-60C9-4BE3-8924-8EF06158823C')
   * 
   * --CASE#3: Failure Case - line 47
   * 
   * select dbo._ValidateItemsAccess ('9D7AA6E1-60E3-4F33-99EF-DD0330B75F06',
   * '0A94BDC9-86E7-43B7-82FD-4CDB0AF08EC2',
   * '616AB32B-60C9-4BE3-8924-8EF06158823C')
   * 
   * --CASE#4(3): Failure Case - line 51
   * 
   * select dbo._ValidateItemsAccess ('B54DDDF6-80D9-4D22-98CA-0006EBB09710',
   * '8B758F90-E048-4FC7-B4E4-C08DD8A66AC9',
   * '6018A101-464C-41FC-94F3-798C5DB02797')
   * 
   * --CASE#5(4): Success Case
   * 
   * select dbo._ValidateItemsAccess ('EDEA2279-BF19-406F-A41D-74BF47813B4B',
   * '0A94BDC9-86E7-43B7-82FD-4CDB0AF08EC2',
   * '6D301A09-A325-4D06-AC0E-10DB5C7DF0E3')
   * 
   * -------------------------------------------
   * 
   * --CASE#1: Success Case
   * 
   * exec T_GetOpportunitySegments @oppkey =
   * '9D7AA6E1-60E3-4F33-99EF-DD0330B75F06' ,
   * 
   * @session = null ,
   * 
   * @browser = null
   * 
   * --CASE#2: Failure Case
   * 
   * exec T_GetOpportunitySegments @oppkey =
   * '846FFB4B-B2AE-4ADD-BA1C-0984D6DFE681' ,
   * 
   * @session = '81C5D50D-4FAC-43C9-AE85-FCC5F4081154' ,
   * 
   * @browser = 'B1277D06-547A-4D3B-91B6-5BD4F06C4DCB'
   * 
   * --CASE#3: Failure Case
   * 
   * exec T_GetOpportunitySegments @oppkey =
   * '089AC62A-5F23-4560-9B16-00022FFD1A13' ,
   * 
   * @session = '616AB32B-60C9-4BE3-8924-8EF06158823C' ,
   * 
   * @browser = 'D3EC8568-6BB6-4929-8674-D2299B798AF7'
   * 
   * -------------------------------------------
   * 
   * --CASE#1: Failure Case
   * 
   * exec T_GetOpportunityItemsWithValidation
   * 
   * @oppkey = 'B54DDDF6-80D9-4D22-98CA-0006EBB09710' , @session =
   * '8B758F90-E048-4FC7-B4E4-C08DD8A66AC9' , @browserID =
   * '6018A101-464C-41FC-94F3-798C5DB02797'
   * 
   * 
   * --CASE#2: Success Case
   * 
   * exec T_GetOpportunityItemsWithValidation
   * 
   * @oppkey = 'F3F99ABD-B24E-4776-9DD7-045F5F579ED1' , @session =
   * '502165F2-909C-4FFB-8AB1-0EB2A398C8FF' , @browserID =
   * '4A10B61A-68BB-4506-9DE0-DE166CBD49E4'
   */

  /**
   * Test 1 - test for Failure.
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   */
  // @Test
  public void test_ValidateItemsAccess_FN_1 () throws SQLException, ReturnStatusException {
    String expected = "The session keys do not match; please consult your test administrator";
    UUID testoppkey = UUID.fromString ("9D7AA6E1-60E3-4F33-99EF-DD0330B75F06");
    UUID session = null;
    UUID browserId = null;
    String message = null;
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      message = _dll._ValidateItemsAccess_FN (connection, testoppkey, session, browserId);
      assertTrue ("Invalid message.", expected.equalsIgnoreCase (message));
    }
  }

  /**
   * Test 2 - test for Failure.
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   */
  // @Test
  public void test_ValidateItemsAccess_FN_2 () throws SQLException, ReturnStatusException {
    String expected = "The session is not available for testing, please check with your test administrator.";
    UUID testoppkey = UUID.fromString ("089AC62A-5F23-4560-9B16-00022FFD1A13");
    UUID session = UUID.fromString ("D3EC8568-6BB6-4929-8674-D2299B798AF7");
    UUID browserId = UUID.fromString ("616AB32B-60C9-4BE3-8924-8EF06158823C");
    String message = null;
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      message = _dll._ValidateItemsAccess_FN (connection, testoppkey, session, browserId);
      assertTrue ("Invalid message.", expected.equalsIgnoreCase (message));
    }
  }

  /**
   * Test 3 - test for Failure.
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   */
  // @Test
  public void test_ValidateItemsAccess_FN_3 () throws SQLException, ReturnStatusException {
    String expected = "Access violation: System access denied";
    UUID testoppkey = UUID.fromString ("9D7AA6E1-60E3-4F33-99EF-DD0330B75F06");
    UUID session = UUID.fromString ("0A94BDC9-86E7-43B7-82FD-4CDB0AF08EC2");
    UUID browserId = UUID.fromString ("616AB32B-60C9-4BE3-8924-8EF06158823C");
    String message = null;
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      message = _dll._ValidateItemsAccess_FN (connection, testoppkey, session, browserId);
      assertTrue ("Invalid message.", expected.equalsIgnoreCase (message));
    }
  }

  /**
   * Test 4 - test for Failure.
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   */
  // @Test
  public void test_ValidateItemsAccess_FN_4 () throws SQLException, ReturnStatusException {
    String expected = "The test opportunity has been paused";
    UUID testoppkey = UUID.fromString ("B54DDDF6-80D9-4D22-98CA-0006EBB09710");
    UUID session = UUID.fromString ("8B758F90-E048-4FC7-B4E4-C08DD8A66AC9");
    UUID browserId = UUID.fromString ("6018A101-464C-41FC-94F3-798C5DB02797");
    String message = null;
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      message = _dll._ValidateItemsAccess_FN (connection, testoppkey, session, browserId);
      assertTrue ("Invalid message.", expected.equalsIgnoreCase (message));
    }
  }

  /**
   * Test 5 - test for Success.
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   */
  // @Test
  public void test_ValidateItemsAccess_FN_5 () throws SQLException, ReturnStatusException {
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      UUID testoppkey = UUID.fromString ("EDEA2279-BF19-406F-A41D-74BF47813B4B");
      UUID session = UUID.fromString ("0A94BDC9-86E7-43B7-82FD-4CDB0AF08EC2");
      UUID browserId = UUID.fromString ("6D301A09-A325-4D06-AC0E-10DB5C7DF0E3");
      String message = null;
      message = _dll._ValidateItemsAccess_FN (connection, testoppkey, session, browserId);
      assertNull ("Message should be empty", message);
    }
  }

  // @Test
  public void test_ValidateItemsAccess_FN_6 () throws SQLException, ReturnStatusException {
    String expected = "The test opportunity has been paused";
    UUID testoppkey = UUID.fromString ("40BC36A2-DD03-41C8-8901-54D3BEF3BD73");
    UUID session = UUID.fromString ("960BF4EE-20A7-45CC-9E0E-06ADA65A21C8");
    UUID browserId = UUID.fromString ("0E69FD54-C2CC-4B00-834B-DE06DFF7B84F");
    String message = null;
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      message = _dll._ValidateItemsAccess_FN (connection, testoppkey, session, browserId);
      assertTrue ("Invalid message.", expected.equalsIgnoreCase (message));
    }
  }

  /**
   * Test 1 - test for Success.
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   */
  // @Test
  public void testT_GetOpportunitySegments_SP_1 () throws SQLException, ReturnStatusException {
    UUID oppkey = UUID.fromString ("9D7AA6E1-60E3-4F33-99EF-DD0330B75F06");
    UUID session = null;
    UUID browser = null;
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      SingleDataResultSet result = _dll.T_GetOpportunitySegments_SP (connection, oppkey, session, browser);
      assertNotNull ("No result set returned.", result);
      if (result != null) {
        Iterator<DbResultRecord> records = result.getRecords ();
        int count = 0;
        while (records.hasNext ()) {
          DbResultRecord record = records.next ();
          assertTrue ("Invalid record returned.", (record.hasColumn ("status") && record.hasColumn ("reason") && record.hasColumn ("context") && record.hasColumn ("appkey")));
          assertTrue ("Failed", record.hasColumn ("segmentID"));
          count++;
        }
        assertEquals ("Invalid number of records found.", 3, count);
        String output = formatTestOutputRows (result);
        _logger.info (output);
        System.out.println (output);
      }
    }
  }

  /**
   * Test 2 - test for Failure.
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   */
  // @Test
  public void testT_GetOpportunitySegments_SP_2 () throws SQLException, ReturnStatusException {
    UUID oppkey = UUID.fromString ("846FFB4B-B2AE-4ADD-BA1C-0984D6DFE681");
    UUID session = UUID.fromString ("81C5D50D-4FAC-43C9-AE85-FCC5F4081154");
    UUID browser = UUID.fromString ("B1277D06-547A-4D3B-91B6-5BD4F06C4DCB");
    String expectedStatus = "failed";
    String expectedReason = "The test opportunity has been paused [10205]";
    String expectedContext = "_ValidateItemsAccess";
    String expectedAppKey = "The test opportunity has been paused";
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      SingleDataResultSet result = _dll.T_GetOpportunitySegments_SP (connection, oppkey, session, browser);
      assertNotNull ("No result set returned.", result);
      if (result != null) {
        Iterator<DbResultRecord> records = result.getRecords ();
        int count = 0;
        while (records.hasNext ()) {
          DbResultRecord record = records.next ();
          count++;
          assertTrue ("Invalid record returned.", (record.hasColumn ("status") && record.hasColumn ("reason") && record.hasColumn ("context") && record.hasColumn ("appkey")));
          assertTrue ("Invalid record returned.", record.hasColumn ("segmentID"));
          String status = record.<String> get ("status");
          String reason = record.<String> get ("reason");
          String context = record.<String> get ("context");
          String appkey = record.<String> get ("appkey");
          assertEquals ("Invalid data retrieved for status.", expectedStatus, status);
          assertEquals ("Invalid data retrieved for reason.", expectedReason, reason);
          assertEquals ("Invalid data retrieved for context.", expectedContext, status);
          assertEquals ("Invalid data retrieved for appkey.", expectedAppKey, reason);
        }
        assertEquals ("Invalid number of records found.", 1, count);
      }
    }
  }

  /**
   * Test 3 - test for Failure.
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   */
  // @Test
  public void testT_GetOpportunitySegments_SP_3 () throws SQLException, ReturnStatusException {
    UUID oppkey = UUID.fromString ("089AC62A-5F23-4560-9B16-00022FFD1A13");
    UUID session = UUID.fromString ("616AB32B-60C9-4BE3-8924-8EF06158823C");
    UUID browser = UUID.fromString ("D3EC8568-6BB6-4929-8674-D2299B798AF7");
    String expectedStatus = "failed";
    String expectedReason = "There was a problem with the system.  Please give this number to your test administrator. [10202]";
    String expectedContext = "_ValidateItemsAccess";
    String expectedAppKey = "The session keys do not match; please consult your test administrator";
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      SingleDataResultSet result = _dll.T_GetOpportunitySegments_SP (connection, oppkey, session, browser);
      assertNotNull ("No result set returned.", result);
      if (result != null) {
        Iterator<DbResultRecord> records = result.getRecords ();
        int count = 0;
        while (records.hasNext ()) {
          DbResultRecord record = records.next ();
          count++;
          assertTrue ("Invalid record returned.", (record.hasColumn ("status") && record.hasColumn ("reason") && record.hasColumn ("context") && record.hasColumn ("appkey")));
          assertTrue ("Invalid record returned.", record.hasColumn ("segmentID"));
          String status = record.<String> get ("status");
          String reason = record.<String> get ("reason");
          String context = record.<String> get ("context");
          String appkey = record.<String> get ("appkey");
          assertEquals ("Invalid data retrieved for status.", expectedStatus, status);
          assertEquals ("Invalid data retrieved for reason.", expectedReason, reason);
          assertEquals ("Invalid data retrieved for context.", expectedContext, status);
          assertEquals ("Invalid data retrieved for appkey.", expectedAppKey, reason);
        }
        assertEquals ("Invalid number of records found.", 1, count);
      }
    }
  }

  /**
   * Test 1 - test for Failure.
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   */
  // @Test
  public void testT_GetOpportunityItemsWithValidation_SP_1 () throws SQLException, ReturnStatusException {
    UUID oppkey = UUID.fromString ("B54DDDF6-80D9-4D22-98CA-0006EBB09710");
    UUID session = UUID.fromString ("8B758F90-E048-4FC7-B4E4-C08DD8A66AC9");
    UUID browser = UUID.fromString ("6018A101-464C-41FC-94F3-798C5DB02797");
    String expectedStatus = "InvalidAccess";
    String expectedReason = "Test opportunity is not available for viewing [10170]";
    String expectedContext = "T_GetOpportunityItemsWithValidation";
    String expectedAppKey = "Test opportunity is not available for viewing";
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      SingleDataResultSet result = _dll.T_GetOpportunityItemsWithValidation_SP (connection, oppkey, session, browser);
      assertNotNull ("No result set returned.", result);
      if (result != null) {
        Iterator<DbResultRecord> records = result.getRecords ();
        int count = 0;
        while (records.hasNext ()) {
          DbResultRecord record = records.next ();
          count++;
          assertTrue ("Invalid record returned.", (record.hasColumn ("status") && record.hasColumn ("reason") && record.hasColumn ("context") && record.hasColumn ("appkey")));
          assertTrue ("Invalid record returned.", record.hasColumn ("segmentID"));
          String status = record.<String> get ("status");
          String reason = record.<String> get ("reason");
          String context = record.<String> get ("context");
          String appkey = record.<String> get ("appkey");
          assertEquals ("Invalid data retrieved for status.", expectedStatus, status);
          assertEquals ("Invalid data retrieved for reason.", expectedReason, reason);
          assertEquals ("Invalid data retrieved for context.", expectedContext, status);
          assertEquals ("Invalid data retrieved for appkey.", expectedAppKey, reason);
        }
        assertEquals ("Invalid number of records found.", 1, count);
      }
    }
  }

  /**
   * Test 2 - test for Success.
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   */
  // @Test
  public void testT_GetOpportunityItemsWithValidation_SP_2 () throws SQLException, ReturnStatusException {
    UUID oppkey = UUID.fromString ("F3F99ABD-B24E-4776-9DD7-045F5F579ED1");
    UUID session = UUID.fromString ("502165F2-909C-4FFB-8AB1-0EB2A398C8FF");
    UUID browser = UUID.fromString ("4A10B61A-68BB-4506-9DE0-DE166CBD49E4");
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      SingleDataResultSet result = _dll.T_GetOpportunityItemsWithValidation_SP (connection, oppkey, session, browser);
      assertNotNull ("No result set returned.", result);
      if (result != null) {
        Iterator<DbResultRecord> records = result.getRecords ();
        int count = 0;
        while (records.hasNext ()) {
          DbResultRecord record = records.next ();
          assertTrue ("Invalid record returned.", (record.hasColumn ("status") && record.hasColumn ("reason") && record.hasColumn ("context") && record.hasColumn ("appkey")));
          assertTrue ("Failed.", record.hasColumn ("segmentID"));
          count++;
        }
        assertEquals ("Invalid number of records found.", 13, count);
        String output = formatTestOutputRows (result);
        _logger.info (output);
        System.out.println (output);
      }
    }
  }

  /**
   * Test 1 - test for Success - Comment found from multiple records for same
   * keys.
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   */
  // @Test
  public void testT_GetOpportunityComment_SP_1 () throws SQLException, ReturnStatusException {
    UUID oppkey = UUID.fromString ("9CC6B36B-6A38-436D-9EDB-00010D25F2A7");
    String context = "my context";
    Integer itemposition = 1;
    String expectedComment = "my comments";
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      SingleDataResultSet result = _dll.T_GetOpportunityComment_SP (connection, oppkey, context, itemposition);
      assertNotNull ("No result set returned.", result);
      if (result != null) {
        Iterator<DbResultRecord> records = result.getRecords ();
        int count = 0;
        while (records.hasNext ()) {
          DbResultRecord record = records.next ();
          assertTrue ("Invalid record returned.", (record.hasColumn ("status") && record.hasColumn ("reason") && record.hasColumn ("context") && record.hasColumn ("appkey")));
          assertTrue ("Failed.", record.hasColumn ("comment"));
          String comment = record.<String> get ("comment");
          assertEquals ("Invalid data retrieved for comment.", expectedComment, comment);
          count++;
        }
        assertEquals ("Invalid number of records found.", 1, count);
        String output = formatTestOutputRows (result);
        _logger.info (output);
        System.out.println (output);
      }
    }
  }

  /**
   * Test 2 - test for Success - No comment found.
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   */
  // @Test
  public void testT_GetOpportunityComment_SP_2 () throws SQLException, ReturnStatusException {
    UUID oppkey = UUID.fromString ("6B243BDA-487E-4311-BE9B-DEF40640AFBD");
    String context = null;
    Integer itemposition = 5;
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      SingleDataResultSet result = _dll.T_GetOpportunityComment_SP (connection, oppkey, context, itemposition);
      assertNotNull ("No result set returned.", result);
      if (result != null) {
        Iterator<DbResultRecord> records = result.getRecords ();
        int count = 0;
        while (records.hasNext ()) {
          DbResultRecord record = records.next ();
          assertTrue ("Invalid record returned.", (record.hasColumn ("status") && record.hasColumn ("reason") && record.hasColumn ("context") && record.hasColumn ("appkey")));
          assertTrue ("Failed.", record.hasColumn ("comment"));
          count++;
        }
        assertEquals ("Invalid number of records found.", 0, count);
        String output = formatTestOutputRows (result);
        _logger.info (output);
        System.out.println (output);
      }
    }
  }

  // Elena: Used this SQL statement to find test case values:
  // select A._fk_testopportunity as opppkey, A.position, A.scoremark,
  // A.responsesequence as sequence,
  // C._efk_ITSITem as itemkey, C.score, C.scorestatus , C.scorerationale
  // from testeeresponsescore A,testopportunity B, testeeresponse C
  // where B._key = A._fk_testopportunity and C._fk_TestOpportunity =
  // A._fk_testopportunity
  // and C.position = A.position and C.responsesequence = A.responsesequence
  // and datediff (d, C.scoringDate, getdate()) < 48

  /**
   * Test 1 - test for Failure. All inputs null.
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   */
  // @Test (expected = NullPointerException.class)
  public void testS_UpdateItemScore_1 () throws SQLException, ReturnStatusException {
    UUID oppkey = null;
    Long itemKey = null;
    Integer position = null;
    Integer sequence = null;
    Integer score = null;
    String scorestatus = null;
    String scoreRationale = null;
    UUID scoremark = null;
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      SingleDataResultSet result = _dll.S_UpdateItemScore_SP (connection, oppkey, itemKey, position, sequence, score, scorestatus, scoreRationale, scoremark);
    }
  }

  /**
   * Test 2 - test for Failure. All inputs null except for oppkey.
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   */
  // @Test (expected = NullPointerException.class)
  public void testS_UpdateItemScore_2 () throws SQLException, ReturnStatusException {
    UUID oppkey = UUID.fromString ("6B243BDA-487E-4311-BE9B-DEF40640AFBD");
    Long itemKey = null;
    Integer position = null;
    Integer sequence = null;
    Integer score = null;
    String scorestatus = null;
    String scoreRationale = null;
    UUID scoremark = null;
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      SingleDataResultSet result = _dll.S_UpdateItemScore_SP (connection, oppkey, itemKey, position, sequence, score, scorestatus, scoreRationale, scoremark);
    }
  }

  /**
   * Test 3 - test for Failure. All inputs null except for oppkey and itemKey.
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   */
  // @Test (expected = NullPointerException.class)
  public void testS_UpdateItemScore_3 () throws SQLException, ReturnStatusException {
    UUID oppkey = UUID.fromString ("6B243BDA-487E-4311-BE9B-DEF40640AFBD");
    Long itemKey = 0L;
    Integer position = null;
    Integer sequence = null;
    Integer score = null;
    String scorestatus = null;
    String scoreRationale = null;
    UUID scoremark = null;
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      SingleDataResultSet result = _dll.S_UpdateItemScore_SP (connection, oppkey, itemKey, position, sequence, score, scorestatus, scoreRationale, scoremark);
    }
  }

  /**
   * Test 4 - test for Failure. All inputs null except for oppkey, itemKey,
   * position.
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   */
  // @Test (expected = NullPointerException.class)
  public void testS_UpdateItemScore_4 () throws SQLException, ReturnStatusException {
    UUID oppkey = UUID.fromString ("6B243BDA-487E-4311-BE9B-DEF40640AFBD");
    Long itemKey = 0L;
    Integer position = 0;
    Integer sequence = null;
    Integer score = null;
    String scorestatus = null;
    String scoreRationale = null;
    UUID scoremark = null;
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      SingleDataResultSet result = _dll.S_UpdateItemScore_SP (connection, oppkey, itemKey, position, sequence, score, scorestatus, scoreRationale, scoremark);
    }
  }

  /**
   * Test 5 - test for Failure. All inputs null except for oppkey, itemKey,
   * position, sequence.
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   */
  // @Test (expected = NullPointerException.class)
  public void testS_UpdateItemScore_5 () throws SQLException, ReturnStatusException {
    UUID oppkey = UUID.fromString ("6B243BDA-487E-4311-BE9B-DEF40640AFBD");
    Long itemKey = 0L;
    Integer position = 0;
    Integer sequence = 0;
    Integer score = null;
    String scorestatus = null;
    String scoreRationale = null;
    UUID scoremark = null;
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      SingleDataResultSet result = _dll.S_UpdateItemScore_SP (connection, oppkey, itemKey, position, sequence, score, scorestatus, scoreRationale, scoremark);
    }
  }

  /**
   * Test 6 - test for Failure. All inputs null except for oppkey, itemKey,
   * position, sequence, score.
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   */
  // @Test (expected = NullPointerException.class)
  public void testS_UpdateItemScore_6 () throws SQLException, ReturnStatusException {
    UUID oppkey = UUID.fromString ("6B243BDA-487E-4311-BE9B-DEF40640AFBD");
    Long itemKey = 0L;
    Integer position = 0;
    Integer sequence = 0;
    Integer score = 0;
    String scorestatus = null;
    String scoreRationale = null;
    UUID scoremark = null;
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      SingleDataResultSet result = _dll.S_UpdateItemScore_SP (connection, oppkey, itemKey, position, sequence, score, scorestatus, scoreRationale, scoremark);
    }
  }

  /**
   * Test 7 - test for Failure. scoreRationale and scoremark null.
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   */
  // @Test (expected = NullPointerException.class)
  public void testS_UpdateItemScore_7 () throws SQLException, ReturnStatusException {
    UUID oppkey = UUID.fromString ("6B243BDA-487E-4311-BE9B-DEF40640AFBD");
    Long itemKey = 0L;
    Integer position = 0;
    Integer sequence = 0;
    Integer score = 0;
    String scorestatus = "";
    String scoreRationale = null;
    UUID scoremark = null;
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      SingleDataResultSet result = _dll.S_UpdateItemScore_SP (connection, oppkey, itemKey, position, sequence, score, scorestatus, scoreRationale, scoremark);
    }
  }

  /**
   * Test 8 - test for Failure. scoremark null.
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   */
  // @Test (expected = NullPointerException.class)
  public void testS_UpdateItemScore_8 () throws SQLException, ReturnStatusException {
    UUID oppkey = UUID.fromString ("6B243BDA-487E-4311-BE9B-DEF40640AFBD");
    Long itemKey = 0L;
    Integer position = 0;
    Integer sequence = 0;
    Integer score = 0;
    String scorestatus = "";
    String scoreRationale = "";
    UUID scoremark = null;
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      SingleDataResultSet result = _dll.S_UpdateItemScore_SP (connection, oppkey, itemKey, position, sequence, score, scorestatus, scoreRationale, scoremark);
    }
  }

  /**
   * Test 9 - test for Failure. Invalid inputs - not found.
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   */
  // @Test
  public void testS_UpdateItemScore_9 () throws SQLException, ReturnStatusException {
    UUID oppkey = UUID.fromString ("6B243BDA-487E-4311-BE9B-DEF40640AFBD");
    Long itemKey = 0L;
    Integer position = 0;
    Integer sequence = 0;
    Integer score = 0;
    String scorestatus = "";
    String scoreRationale = "";
    UUID scoremark = UUID.fromString ("6B243BDA-487E-4311-BE9B-DEF40640AFBD");
    String expectedStatus = "failed";
    String expectedReason = "No such item: 0 [10159]";
    String expectedContext = "UpdateItemScore";
    String expectedAppKey = "No such item: {0}";
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      SingleDataResultSet result = _dll.S_UpdateItemScore_SP (connection, oppkey, itemKey, position, sequence, score, scorestatus, scoreRationale, scoremark);
      assertNotNull ("No result set returned.", result);
      if (result != null) {
        Iterator<DbResultRecord> records = result.getRecords ();
        int count = 0;
        while (records.hasNext ()) {
          DbResultRecord record = records.next ();
          count++;
          assertTrue ("Invalid record returned.", (record.hasColumn ("status") && record.hasColumn ("reason") && record.hasColumn ("context") && record.hasColumn ("appkey")));
          assertTrue ("Invalid record returned.", record.hasColumn ("segmentID"));
          String status = record.<String> get ("status");
          String reason = record.<String> get ("reason");
          String context = record.<String> get ("context");
          String appkey = record.<String> get ("appkey");
          assertEquals ("Invalid data retrieved for status.", expectedStatus, status);
          assertEquals ("Invalid data retrieved for reason.", expectedReason, reason);
          assertEquals ("Invalid data retrieved for context.", expectedContext, status);
          assertEquals ("Invalid data retrieved for appkey.", expectedAppKey, reason);
        }
        assertEquals ("Invalid number of records found.", 1, count);
      }
    }
  }

  /**
   * Test 10 - test for Failure. Blows up datediff().
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   */
  // @Test (expected = ReturnStatusException.class)
  public void testS_UpdateItemScore_10 () throws SQLException, ReturnStatusException {
    UUID oppkey = UUID.fromString ("5E90B402-665D-4D89-AEEE-601817C9626B");
    Long itemKey = 14573L;
    Integer position = 2;
    Integer sequence = 1;
    Integer score = 0;
    String scorestatus = "";
    String scoreRationale = "";
    UUID scoremark = UUID.fromString ("82FEF30E-2CB4-488F-AF93-2F2A2D0EA3A1");
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      SingleDataResultSet result = _dll.S_UpdateItemScore_SP (connection, oppkey, itemKey, position, sequence, score, scorestatus, scoreRationale, scoremark);
    }
  }

  /**
   * Test 11 - test for Success - without update to testeeresponseAudit.
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   */
  // @Test
  public void testS_UpdateItemScore_11 () throws SQLException, ReturnStatusException {
    UUID oppkey = UUID.fromString ("42863E38-DD83-45CC-A72B-4EF2B8B32E69");
    Long itemKey = 100637L;
    Integer position = 2;
    Integer sequence = 1;
    Integer score = -999;
    String scorestatus = "test scorestatus";
    String scoreRationale = "test scoreRationale";
    UUID scoremark = UUID.fromString ("4020A4F0-B33F-4AFA-889A-C32E50C2284B");
    String expectedStatus = "updated";

    // Update existing record(s) for test.
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {

      Date now = _dateUtil.getDate (connection);
      String updateSql = "update testeeresponse set ScoringDate = ${now} where _fk_TestOpportunity = ${oppkey}";
      SqlParametersMaps updateParams = new SqlParametersMaps ();
      updateParams.put ("oppkey", oppkey).put ("now", now);
      Integer updateCount = _myDllHelper.executeStatement (connection, updateSql, updateParams, false).getUpdateCount ();
    }

    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      SingleDataResultSet result = _dll.S_UpdateItemScore_SP (connection, oppkey, itemKey, position, sequence, score, scorestatus, scoreRationale, scoremark);
      assertNotNull ("No result set returned.", result);
      if (result != null) {
        Iterator<DbResultRecord> records = result.getRecords ();
        int count = 0;
        while (records.hasNext ()) {
          DbResultRecord record = records.next ();
          count++;
          assertTrue ("Failed.", (record.hasColumn ("context") && record.hasColumn ("appkey")));
          assertTrue ("Invalid record returned.", (record.hasColumn ("status") && record.hasColumn ("reason")));
          String status = record.<String> get ("status");
          String reason = record.<String> get ("reason");
          assertEquals ("Invalid data retrieved for status.", expectedStatus, status);
          assertNull ("Invalid data retrieved for reason. Reason should be 'null'", reason);
        }
        assertEquals ("Invalid number of records found.", 1, count);
      }
    }
  }

  /**
   * Test 12 - test for Success - with update to testeeresponseAudit.
   * 
   * @throws SQLException
   * @throws ReturnStatusException
   */
  // @Test
  public void testS_UpdateItemScore_12 () throws SQLException, ReturnStatusException {
    UUID oppkey = UUID.fromString ("42863E38-DD83-45CC-A72B-4EF2B8B32E69");
    Long itemKey = 100639L;
    Integer position = 4;
    Integer sequence = 1;
    Integer score = -999;
    String scorestatus = "test for S_UpdateItemScore - scorestatus";
    String scoreRationale = "test for S_UpdateItemScore - scoreRationale";
    UUID scoremark = UUID.fromString ("ED35FE96-82D1-49B3-9BAB-34E535B59A5C");
    String expectedStatus = "updated";

    // Update existing testeeresponse record(s) for test.
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {

      Date now = _dateUtil.getDate (connection);

      String updateSql = "update testeeresponse set ScoringDate = ${now} where _fk_TestOpportunity = ${oppkey}";
      SqlParametersMaps updateParams = new SqlParametersMaps ();
      updateParams.put ("oppkey", oppkey).put ("now", now);
      Integer updateCount = _myDllHelper.executeStatement (connection, updateSql, updateParams, false).getUpdateCount ();
    }

    // Update existing Client_SystemFlags record for test.
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      String updateSql = "update ${ConfigDB}.client_systemflags set IsOn = 1 where ClientName = ${ClientName} and AuditObject = 'responses'";
      SqlParametersMaps updateParams = new SqlParametersMaps ();
      updateParams.put ("ClientName", "Oregon_PT");
      Integer updateCount = _myDllHelper.executeStatement (connection, _myDllHelper.fixDataBaseNames (updateSql), updateParams, false).getUpdateCount ();
    }

    // Insert new testeeresponseauditrecord for test.
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      String insertSql = "insert into  testeeresponseaudit(_fk_TestOpportunity, sequence, _date, response, position, scoremark ) "
          + " values (${oppkey}, ${sequence}, ${now}, 'test for S_UpdateItemScore - response' , ${position}, ${scoremark})";

      Date now = _dateUtil.getDateWRetStatus (connection);
      SqlParametersMaps insertParams = new SqlParametersMaps ();
      insertParams.put ("oppkey", oppkey);
      insertParams.put ("sequence", sequence);
      insertParams.put ("position", position).put ("scoremark", scoremark).put ("now", now);
      _myDllHelper.executeStatement (connection, insertSql, insertParams, false);
    }

    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      SingleDataResultSet result = _dll.S_UpdateItemScore_SP (connection, oppkey, itemKey, position, sequence, score, scorestatus, scoreRationale, scoremark);
      assertNotNull ("No result set returned.", result);
      if (result != null) {
        Iterator<DbResultRecord> records = result.getRecords ();
        int count = 0;
        while (records.hasNext ()) {
          DbResultRecord record = records.next ();
          count++;
          assertTrue ("Failed.", (record.hasColumn ("context") && record.hasColumn ("appkey")));
          assertTrue ("Invalid record returned.", (record.hasColumn ("status") && record.hasColumn ("reason")));
          String status = record.<String> get ("status");
          String reason = record.<String> get ("reason");
          assertEquals ("Invalid data retrieved for status.", expectedStatus, status);
          assertNull ("Invalid data retrieved for reason. Reason should be 'null'", reason);
        }
        assertEquals ("Invalid number of records found.", 1, count);
      }
    }
  }

  // @Test
  public void testClientStimulusFile_FN () throws ReturnStatusException, SQLException {
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      String clientName = "Delaware";
      String stimulusKey = "148-178";
      String path = _dll.ClientStimulusFile_FN (connection, clientName, stimulusKey);
      _logger.info (String.format ("Client SimulusFile path -- : %s", path));
      assertTrue ("D:\\DataFiles\\BB_Files\\tds_airws_org\\TDSCore_2012-2013\\Bank-148\\Stimuli\\Stim-148-178\\stim-148-178.xml".equalsIgnoreCase (path));
    }
  }

  // @Test
  public void testGetForbiddenApps_SP () throws ReturnStatusException, SQLException {
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      String clientname = "Oregon";
      MultiDataResultSet resultsets = _dll.GetForbiddenApps_SP (connection, clientname);
      Iterator<SingleDataResultSet> iterator = resultsets.getResultSets ();
      int resultIdx = 0;
      while (iterator.hasNext ()) {
        SingleDataResultSet set = iterator.next ();
        if (resultIdx == 0) {
          Iterator<DbResultRecord> iteratorRec = set.getRecords ();
          while (iteratorRec.hasNext ()) {
            DbResultRecord record = iteratorRec.next ();
            assertTrue (record != null);
            _logger.info (String.format ("AgentOS: %s", record.<String> get ("agentOs")));
            _logger.info (String.format ("OS_ID: %s", record.<String> get ("OS_ID")));
            assertTrue (set.getCount () == 1);
          }
        }
        if (resultIdx == 1) {
          Iterator<DbResultRecord> iteratorRec = set.getRecords ();
          while (iteratorRec.hasNext ()) {
            DbResultRecord record = iteratorRec.next ();
            assertTrue (record != null);
            _logger.info (String.format ("OS_ID: %s", record.<String> get ("OS_ID")));
            _logger.info (String.format ("ProcessName: %s", record.<String> get ("processName")));
            _logger.info (String.format ("ProcessDescription: %s", record.<String> get ("processDescription")));
            assertTrue (set.getCount () == 111);
          }
        }
        if (resultIdx == 2) {
          Iterator<DbResultRecord> iteratorRec = set.getRecords ();
          while (iteratorRec.hasNext ()) {
            DbResultRecord record = iteratorRec.next ();
            assertTrue (record != null);
            _logger.info (String.format ("DistrictName: %s", record.<String> get ("districtName")));
            _logger.info (String.format ("DistrictID: %s", record.<String> get ("districtId")));
            _logger.info (String.format ("SchoolName: %s", record.<String> get ("schoolName")));
            _logger.info (String.format ("SchoolID: %s", record.<String> get ("schoolId")));
            assertTrue (set.getCount () == 0);
          }
        }
        resultIdx++;
      }
    }
  }

  // @Test
  public void testAuditResponses_FN () throws SQLException, ReturnStatusException {
    String clientName = "Oregon";
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      Integer auditResponses = _dll.AuditResponses_FN (connection, clientName);
      _logger.info (String.format ("Responses: %d", auditResponses));
      assertTrue (auditResponses == 1);
    }
  }

  // @Test
  public void testGetInitialAbility_FN () throws SQLException, ReturnStatusException {
    String testKey = "(Delaware)DCAS-Alt1-PAPER-Mathematics-10-Fall-2012-2013";
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      float ability = _dll.GetInitialAbility_FN (connection, testKey);
      _logger.info (String.format ("GetInitialAbility: %f", ability));
      assertEquals (-6.151711, ability, 2.0e-6);
    }
  }

  // @Test
  public void test_ActiveOpps_FN () throws SQLException, ReturnStatusException {
    String clientName = "Delaware";
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      Integer n = _dll._ActiveOpps_FN (connection, clientName);
      _logger.info (String.format ("ActiveOpps --> : %d", n));
      assertTrue (n == 1);
    }
  }

  // @Test
  public void testIsSimulation_FN () throws SQLException, ReturnStatusException {
    UUID oppKey = UUID.fromString ("4C57EF78-BE20-43AB-883C-0000E03AF7B6");
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      Boolean sim = _dll.IsSimulation_FN (connection, oppKey);
      _logger.info (String.format ("ISSimulation Result  --> : %s", sim));
      assertTrue (sim == false);
    }
  }

  // @Test
  public void testFT_IsEligible_FN () throws SQLException, ReturnStatusException {
    UUID oppKey = UUID.fromString ("51BCBEEA-E7C8-4886-AB6D-49990908E5A4");
    String testKey = "(Delaware)DCAS-Mathematics-9-Fall-2012-2013";
    String parentKey = "(Delaware)DCAS-Mathematics-9-Fall-2012-2013";
    String language = "ENU";
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      int testOkay = _dll.FT_IsEligible_FN (connection, oppKey, testKey, parentKey, language);
      _logger.info (String.format ("FT_IsEligible_FN result -- : %d", testOkay));
      assertTrue (testOkay == 1);
    }
  }

  // @Test
  public void testFT_IsEligible_FN1 () throws SQLException, ReturnStatusException {
    UUID oppKey = UUID.fromString ("51BCBEEA-E7C8-4886-AB6D-49990908E5A4");
    String testKey = "(Delaware)DCAS-Alt1-PAPER-Mathematics-6-8-Fall-2012-2013";
    String parentKey = "(Delaware)DCAS-Mathematics-9-Fall-2012-2013";
    String language = "ENU";
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      int testOkay = _dll.FT_IsEligible_FN (connection, oppKey, testKey, parentKey, language);
      _logger.info (String.format ("FT_IsEligible_FN result -- : %d", testOkay));
      assertTrue (testOkay == 0);
    }
  }

  // @Test
  public void testGetOpportunityLanguage_FN () throws SQLException, ReturnStatusException {
    UUID oppKey = UUID.fromString ("CFC1A510-1264-46E3-86CA-00027B426E5F");
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      String lang = _dll.GetOpportunityLanguage_FN (connection, oppKey);
      _logger.info (String.format ("Opportunity Language -- : %s", lang));
      assertTrue ("ENU".equalsIgnoreCase (lang));
    }
  }

  // @Test
  public void test_AA_IsSegmentSatisfied_FN1 () throws SQLException, ReturnStatusException {
    UUID oppKey = UUID.fromString ("7AF448B7-25A1-4755-A207-41276D7598E0");
    int segment = 1;
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      Boolean result = _dll._AA_IsSegmentSatisfied_FN (connection, oppKey, segment);
      _logger.info (String.format ("_AA_IsSegmentSatisfied_FN Result -- : %s", result));
      assertTrue (result == true);
    }
  }

  // @Test
  public void test_AA_IsSegmentSatisfied_FN () throws SQLException, ReturnStatusException {
    UUID oppKey = UUID.fromString ("9CC6B36B-6A38-436D-9EDB-00010D25F2A7");
    int segment = 0;
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      Boolean result = _dll._AA_IsSegmentSatisfied_FN (connection, oppKey, segment);
      _logger.info (String.format ("_AA_IsSegmentSatisfied_FN Result -- : %s", result));
      assertTrue (result == false);
    }
  }

  // @Test
//  public void testListClientTests_FN () throws SQLException, ReturnStatusException {
//    String clientName = "Oregon";
//    int sessionType = 0;
//    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
//      DataBaseTable table = _dll.ListClientTests_FN (connection, clientName, sessionType);
//
//      final String SQL_QUERY = "select * from ${tblName}";
//      Map<String, String> params = new HashMap<String, String> ();
//      params.put ("tblName", table.getTableName ());
//      SingleDataResultSet result = _myDllHelper.executeStatement (connection, _myDllHelper.fixDataBasenames (SQL_QUERY, params), null, false).getResultSets ().next ();
//      Iterator<DbResultRecord> records = result.getRecords ();
//      while (records.hasNext ()) {
//        DbResultRecord record = records.next ();
//        _logger.info ("===================Record=======================");
//        _logger.info (String.format ("TestID: %s", record.<String> get ("TestID")));
//        _logger.info (String.format ("GradeCode: %s", record.<String> get ("GradeCode")));
//        _logger.info (String.format ("Subject: %s", record.<String> get ("Subject")));
//        _logger.info (String.format ("LanguageCode: %s", record.<String> get ("LanguageCode")));
//        _logger.info (String.format ("Language: %s", record.<String> get ("Language")));
//        _logger.info (String.format ("SelectionAlgorithm: %s", record.<String> get ("selectionAlgorithm")));
//        _logger.info (String.format ("DisplayName: %s", record.<String> get ("DisplayName")));
//        _logger.info (String.format ("SortOrder: %d", record.<Integer> get ("SortOrder")));
//        _logger.info (String.format ("AccommodationFamily: %s", record.<String> get ("AccommodationFamily")));
//        _logger.info (String.format ("IsSelectable: %s", record.<Boolean> get ("IsSelectable")));
//        _logger.info (String.format ("scoreByTDS: %s", record.<Boolean> get ("ScoreByTDS")));
//        _logger.info (String.format ("validateCompleteness: %s", record.<Boolean> get ("validateCompleteness")));
//        _logger.info (String.format ("MaxOpportunities: %d", record.<Integer> get ("MaxOpportunities")));
//        _logger.info (String.format ("MinItems: %d", record.<Integer> get ("MinItems")));
//        _logger.info (String.format ("MaxItems: %d", record.<Integer> get ("MaxItems")));
//        _logger.info (String.format ("Prefetch: %d", record.<Integer> get ("Prefetch")));
//        _logger.info (String.format ("StartAbility: %f", record.<Float> get ("StartAbility")));
//        _logger.info (String.format ("windowStart: %s", record.<Date> get ("windowStart")));
//        _logger.info (String.format ("windowEnd: %s", record.<Date> get ("windowEnd")));
//        _logger.info (String.format ("FTStartDate: %s", record.<Date> get ("FTStartDate")));
//        _logger.info (String.format ("FTEndDate: %s", record.<Date> get ("FTEndDate")));
//        _logger.info (String.format ("FTMinItems: %d", record.<Integer> get ("FTMinItems")));
//        _logger.info (String.format ("FTMaxItems: %d", record.<Integer> get ("FTMaxItems")));
//        _logger.info (String.format ("IsSegmented: %s", record.<Boolean> get ("IsSegmented")));
//        _logger.info (String.format ("_Key: %s", record.<String> get ("_Key")));
//        _logger.info (String.format ("formSelection: %s", record.<String> get ("formSelection")));
//
//      }
//      _logger.info ("Total no: of records -- " + result.getCount ());
//      assertTrue (result.getCount () == 128);
//    }
//  }

  // @Test
  public void test_AA_TestoppItempool_FN () throws SQLException, ReturnStatusException {
    String clientName = "Delaware";
    UUID oppKey = UUID.fromString ("FE0620E9-3530-4FA8-8A0F-0004AEA29F40");
    String segmentKey = "(Delaware)DCAS-Alt1-PAPER-Mathematics-10-Fall-2012-2013";
    String testId = "DCAS-Reading-4";
    Boolean fieldTest = false;
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      DataBaseTable table = _dll._AA_TestoppItempool_FN (connection, clientName, oppKey, segmentKey, testId, fieldTest);

      final String SQL_QUERY = "select * from ${tblName}";
      Map<String, String> params = new HashMap<String, String> ();
      params.put ("tblName", table.getTableName ());
      SingleDataResultSet result = _myDllHelper.executeStatement (connection, _myDllHelper.fixDataBasenames (SQL_QUERY, params), null, false).getResultSets ().next ();
      Iterator<DbResultRecord> records = result.getRecords ();
      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        _logger.info ("===================Record=======================");
        _logger.info (String.format ("itemkey: %s", record.<String> get ("itemkey")));
        _logger.info (String.format ("groupKey: %s", record.<String> get ("groupKey")));
        _logger.info (String.format ("groupID: %s", record.<String> get ("groupID")));
        _logger.info (String.format ("blockID: %s", record.<String> get ("blockID")));
        _logger.info (String.format ("isActive: %s", record.<Boolean> get ("isActive")));
      }
      _logger.info ("Total no: of records -- " + result.getCount ());
      assertTrue (result.getCount () == 69);
    }
  }

  // @Test
  public void test_AA_ItempoolString_FN () throws ReturnStatusException, SQLException {
    UUID oppKey = UUID.fromString ("0865E91D-FD3B-4232-88FD-117DA874DF39");// ("51bcbeea-e7c8-4886-ab6d-49990908e5a4");
                                                                           // //("9CC6B36B-6A38-436D-9EDB-00010D25F2A7");
    String segmentKey = "(Delaware)DCAS-Reading-10-Fall-2012-2013";// "(Delaware)DCAS-Alt1-PAPER-Mathematics-10-Fall-2012-2013";
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      String itemString = _dll._AA_ItempoolString_FN (connection, oppKey, segmentKey);
      _logger.info (String.format ("ItempoolString -- : %s", itemString));
      assertTrue (itemString != null);
      assertTrue (itemString.startsWith ("148-100011,148-100015") == true);
      // assertTrue
      // ("153-1057,153-1058,153-1059,153-1060,153-1061,153-1062,153-1087,153-1088,153-1091,153-1092,153-1117,153-1118,153-1119,153-1120,153-1121,153-1334,153-1335,153-1336,153-1337,153-1338,153-1339,153-1819,153-1820,153-1821,153-1822,153-1823,153-1824,153-1825,153-1826,153-1827,153-1828,153-1829,153-1830,153-1831,153-1832,153-1833,153-1834,153-1894,153-1895,153-1896,153-1973,153-1974,153-1975,153-1976,153-1977,153-265,153-266,153-267,153-268,153-269,153-271,153-33,153-34,153-35,153-36,153-368,153-369,153-370,153-371,153-372,153-373,153-39,153-390,153-391,153-392,153-393,153-40,153-400,153-407,153-486,153-490,153-491,153-497,153-500,153-501,153-560,153-561,153-562,153-567,153-570,153-571,153-587,153-593,153-594,153-595,153-596,153-597,153-616,153-617,153-618,153-619,153-620,153-621"
      // .equalsIgnoreCase (itemString));
    }
  }

  // @Test
  public void test_GetInitialAbility_SP () throws SQLException, ReturnStatusException {
    UUID oppKey = UUID.fromString ("EA978D5D-FD2A-4F03-B095-0378CFE50B78");
    _Ref<Float> ability = new _Ref<Float> ();
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      _dll._GetInitialAbility_SP (connection, oppKey, ability);
      _logger.info (String.format ("_GetInitialAbility_SP -- : %f", ability.get ()));
      assertEquals (-6.151711, ability.get (), 2.0e-6);
    }
  }

  // @Test
  // Success case
  public void test_FT_Prioritize_2012_SP () throws ReturnStatusException, SQLException {
    UUID oppKey = UUID.fromString ("8F9972ED-C7C0-4910-A517-0E16D8D50FDE");
    String testKey = "(Delaware)DCAS-Alt1-PAPER-Mathematics-10-Fall-2012-2013";
    int debug = 0;
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      SingleDataResultSet result = _dll._FT_Prioritize_2012_SP (connection, oppKey, testKey, debug);
      Iterator<DbResultRecord> records = result.getRecords ();
      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        assertTrue (record != null);
        _logger.info ("===================_FT_Prioritize_2012_SP Result=======================");
        _logger.info (String.format ("grpkey: %s", record.<String> get ("grpkey")));
        _logger.info (String.format ("groupID: %s ", record.<String> get ("groupID")));
        _logger.info (String.format ("blockID: %s ", record.<String> get ("blockID")));
        _logger.info (String.format ("activeItems %d ", record.<Integer> get ("activeItems")));
        _logger.info (String.format ("tier: %d", record.<Integer> get ("tier")));
        _logger.info (String.format ("admins %d ", record.<Integer> get ("admins")));

      }
      _logger.info (String.format ("FT_Prioritize_2012 Result size ---" + result.getCount ()));
      assertTrue (result.getCount () == 3);
    }
  }

  // TODO Elena: ft_opportunityitem tbl is empty
  // @Test
  public void test_FT_SelectItemgroups_SP () throws ReturnStatusException, SQLException {
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      UUID testOppKey = UUID.fromString ("51BCBEEA-E7C8-4886-AB6D-49990908E5A4");
      String testKey = "(Delaware)DCAS-Reading-10-Fall-2012-2013";
      int segment = 1;
      String segmentId = "DCAS-Reading-10";
      String language = "English";
      _Ref<Integer> ftCount = new _Ref<> ();
      Integer debug = 0;
      Integer noinsert = 0;
      _dll._FT_SelectItemgroups_SP (connection, testOppKey, testKey, segment, segmentId, language, ftCount, debug, noinsert);
      _logger.info (String.format ("_FT_SelectItemgroups_SP  result -- : %d", ftCount.get ()));
      assertTrue (4 == ftCount.get ());
    }
  }

  // @Test
  // failure case
  public void test_FT_SelectItemgroups_SP1 () throws ReturnStatusException, SQLException {
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      UUID testOppKey = UUID.fromString ("51BCBEEA-E7C8-4286-AB6D-49990908E5A4");
      String testKey = "(Delaware)DCAS-Reading-10-Fall-2012-2013";
      int segment = 1;
      String segmentId = "DCAS-Reading-10";
      String language = "English";
      _Ref<Integer> ftCount = new _Ref<> ();
      Integer debug = 0;
      Integer noinsert = 0;
      _dll._FT_SelectItemgroups_SP (connection, testOppKey, testKey, segment, segmentId, language, ftCount, debug, noinsert);
      _logger.info (String.format ("_FT_SelectItemgroups_SP  result -- : %d", ftCount.get ()));
      assertTrue (ftCount.get () == null);
    }
  }

  // @Test
  // success case #1
  public void test_InitializeTestSegments_SP () throws ReturnStatusException, SQLException {
    UUID oppKey = UUID.fromString ("51BCBEEA-E7C8-4886-AB6D-49990908E5A4");
    _Ref<String> error = new _Ref<> ();
    String formKeyList = null;
    boolean debug = true;
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      MultiDataResultSet resultsets = _dll._InitializeTestSegments_SP (connection, oppKey, error, formKeyList, debug);
      assertTrue (error.get () == null);
      Iterator<SingleDataResultSet> iterator = resultsets.getResultSets ();
      int resultIdx = 0;
      while (iterator.hasNext ()) {
        SingleDataResultSet set = iterator.next ();
        if (resultIdx == 0) {
          DbResultRecord record = set.getRecords ().next ();
          if (record != null) {
            _logger.info (String.format ("testKey: %s", record.<String> get ("testKey")));
            _logger.info (String.format ("lang: %s", record.<String> get ("lang")));
            _logger.info (String.format ("segmented: %s", record.<Boolean> get ("segmented")));
            _logger.info (String.format ("algorithm: %s", record.<String> get ("algorithm")));

            assertTrue ("(Delaware)DCAS-Reading-10-Fall-2012-2013".equalsIgnoreCase (record.<String> get ("testKey")));
            assertTrue ("ENU".equalsIgnoreCase (record.<String> get ("lang")));
          }
        }
        if (resultIdx == 1) {
          Iterator<DbResultRecord> iteratorRec = set.getRecords ();
          assertTrue (iteratorRec != null);
          while (iteratorRec.hasNext ()) {
            DbResultRecord record = iteratorRec.next ();
            _logger.info (String.format ("_fk_TestOpportunity: %s", record.<String> get ("_fk_TestOpportunity")));
            _logger.info (String.format ("_efk_Segment : %s ", record.<String> get ("_efk_Segment")));
            _logger.info (String.format ("SegmentPosition: %d", record.<Integer> get ("SegmentPosition")));
            _logger.info (String.format ("formKey: %s", record.<String> get ("formKey")));
            _logger.info (String.format ("FormID: %s", record.<String> get ("FormID")));
            _logger.info (String.format ("algorithm: %s", record.<String> get ("algorithm")));
            _logger.info (String.format ("opItemCnt: %d", record.<Integer> get ("opItemCnt")));
            _logger.info (String.format ("ftItemCnt: %d", record.<Integer> get ("ftItemCnt")));
            _logger.info (String.format ("ftItems: %s", record.<String> get ("ftItems")));
            _logger.info (String.format ("IsPermeable: %d", record.<Integer> get ("IsPermeable")));
            _logger.info (String.format ("restorePermOn: %s", record.<String> get ("restorePermOn")));
            _logger.info (String.format ("segmentID: %s", record.<String> get ("segmentID")));
            _logger.info (String.format ("entryApproved: %s", record.<Date> get ("entryApproved")));
            _logger.info (String.format ("exitApproved: %s", record.<Date> get ("exitApproved")));
            _logger.info (String.format ("formCohort: %s", record.<String> get ("formCohort")));
            _logger.info (String.format ("IsSatisfied: %s", record.<Boolean> get ("IsSatisfied")));
            _logger.info (String.format ("initialAbility: %f", record.<Float> get ("initialAbility")));
            _logger.info (String.format ("currentAbility: %f", record.<Float> get ("currentAbility")));
            _logger.info (String.format ("_date: %s", record.<Date> get ("_date")));
            _logger.info (String.format ("dateExited: %s", record.<Date> get ("dateExited")));
            _logger.info (String.format ("itempool: %s", record.<String> get ("itempool")));
            _logger.info (String.format ("poolcount: %d", record.<Integer> get ("poolcount")));

            assertTrue (record.<Integer> get ("ftItemCnt") == null);
            assertTrue (record.<Integer> get ("poolcount") == null);
            assertTrue (record.<String> get ("itempool") == null);
          }
        }
        if (resultIdx == 2) {
          Iterator<DbResultRecord> iteratorRec = set.getRecords ();
          assertTrue (iteratorRec != null);
          while (iteratorRec.hasNext ()) {
            DbResultRecord record = iteratorRec.next ();
            _logger.info (String.format ("_fk_TestOpportunity: %s", record.<String> get ("_fk_TestOpportunity")));
            _logger.info (String.format ("_efk_Segment : %s ", record.<String> get ("_efk_Segment")));
            _logger.info (String.format ("SegmentPosition: %d", record.<Integer> get ("SegmentPosition")));
            _logger.info (String.format ("formKey: %s", record.<String> get ("formKey")));
            _logger.info (String.format ("FormID: %s", record.<String> get ("FormID")));
            _logger.info (String.format ("algorithm: %s", record.<String> get ("algorithm")));
            _logger.info (String.format ("opItemCnt: %d", record.<Integer> get ("opItemCnt")));
            _logger.info (String.format ("ftItemCnt: %d", record.<Integer> get ("ftItemCnt")));
            _logger.info (String.format ("ftItems: %s", record.<String> get ("ftItems")));
            _logger.info (String.format ("IsPermeable: %d", record.<Integer> get ("IsPermeable")));
            _logger.info (String.format ("restorePermOn: %s", record.<String> get ("restorePermOn")));
            _logger.info (String.format ("segmentID: %s", record.<String> get ("segmentID")));
            _logger.info (String.format ("entryApproved: %s", record.<Date> get ("entryApproved")));
            _logger.info (String.format ("exitApproved: %s", record.<Date> get ("exitApproved")));
            _logger.info (String.format ("formCohort: %s", record.<String> get ("formCohort")));
            _logger.info (String.format ("IsSatisfied: %s", record.<Boolean> get ("IsSatisfied")));
            _logger.info (String.format ("initialAbility: %f", record.<Float> get ("initialAbility")));
            _logger.info (String.format ("currentAbility: %f", record.<Float> get ("currentAbility")));
            _logger.info (String.format ("_date: %s", record.<Date> get ("_date")));
            _logger.info (String.format ("dateExited: %s", record.<Date> get ("dateExited")));
            _logger.info (String.format ("itempool: %s", record.<String> get ("itempool")));
            _logger.info (String.format ("poolcount: %d", record.<Integer> get ("poolcount")));

            assertTrue (record.<Integer> get ("ftItemCnt") == 4);
            assertTrue (record.<Integer> get ("poolcount") == 551);
            assertTrue ("148-100011,148-100015,148-100016,148-100018,148-100020,148-100023,148-100025,148-100028,148-100062,148-100064,148-100066,148-100068,148-100071,148-100111,148-100115,148-100120,148-100122,148-100125,148-100128,148-100138,148-100143,148-100146,148-100148,148-100156,148-100161,148-100162,148-100164,148-100170,148-100172,148-100177,148-100181,148-100184,148-100185,148-100190,148-100194,148-100198,148-100201,148-100203,148-100228,148-100232,148-100237,148-100239,148-100241,148-100331,148-100332,148-100335,148-100336,148-100337,148-100338,148-100339,148-100340,148-100341,148-100343,148-100344,148-100346,148-100385,148-100386,148-100388,148-100389,148-100390,148-100392,148-100393,148-100394,148-100395,148-100397,148-100398,148-100399,148-100488,148-100489,148-100490,148-100491,148-100519,148-100520,148-100521,148-100522,148-100525,148-100526,148-100527,148-100528,148-100529,148-100530,148-100577,148-100578,148-100580,148-100581,148-100582,148-100583,148-100584,148-100585,148-100586,148-100587,148-100588,148-100589,148-100592,148-100594,148-100595,148-100596,148-100598,148-100599,148-100612,148-100613,148-100614,148-100615,148-100616,148-100617,148-100618,148-100619,148-100628,148-100629,148-100631,148-100632,148-100633,148-100634,148-100635,148-100636,148-100637,148-100638,148-100957,148-100958,148-100959,148-100960,148-100961,148-100962,148-100963,148-100964,148-100965,148-100966,148-101040,148-101041,148-101043,148-101044,148-101045,148-101046,148-101047,148-101048,148-101049,148-101060,148-101061,148-101062,148-101063,148-101064,148-101065,148-101066,148-101067,148-101069,148-101070,148-101071,148-101072,148-101073,148-101074,148-101075,148-101076,148-101078,148-101079,148-101080,148-101283,148-101334,148-101335,148-101336,148-101337,148-101343,148-101344,148-101345,148-101346,148-101347,148-10135,148-101354,148-101355,148-101356,148-101357,148-101479,148-101480,148-101481,148-101482,148-101491,148-101492,148-101494,148-101507,148-101508,148-101509,148-101510,148-101519,148-101520,148-101521,148-101522,148-101527,148-101529,148-101532,148-101533,148-101534,148-101535,148-101537,148-101538,148-101540,148-101541,148-101542,148-101543,148-101544,148-101545,148-101546,148-101547,148-101548,148-101549,148-101550,148-10157,148-10160,148-10162,148-10164,148-10167,148-10169,148-10171,148-101711,148-101712,148-101713,148-101714,148-101715,148-101716,148-101717,148-101718,148-101727,148-101728,148-101729,148-101730,148-101731,148-101732,148-101733,148-101734,148-10174,148-10176,148-101775,148-101776,148-101777,148-101778,148-101779,148-101781,148-101783,148-101784,148-101785,148-101786,148-101787,148-101788,148-101789,148-101790,148-101799,148-101800,148-101801,148-101802,148-101803,148-101804,148-101805,148-101806,148-10181,148-10183,148-10188,148-101927,148-101928,148-101929,148-101930,148-101931,148-101932,148-101933,148-101934,148-10194,148-10196,148-10200,148-10202,148-102024,148-102026,148-102027,148-102028,148-102029,148-10203,148-102030,148-102031,148-102032,148-102033,148-102034,148-102036,148-102037,148-102038,148-102039,148-102040,148-102042,148-102043,148-102044,148-102045,148-102046,148-102047,148-102048,148-102049,148-10205,148-102050,148-102051,148-102052,148-102053,148-102054,148-10206,148-10207,148-102102,148-102103,148-10211,148-102117,148-102120,148-10213,148-10215,148-10217,148-102205,148-102206,148-102207,148-102208,148-102209,148-102210,148-102211,148-102212,148-102213,148-102214,148-102216,148-102217,148-102220,148-102313,148-102316,148-102317,148-102319,148-102323,148-102325,148-102383,148-102384,148-102386,148-102387,148-102388,148-102389,148-102390,148-102423,148-102464,148-102472,148-102506,148-102507,148-102508,148-102509,148-102510,148-102511,148-102512,148-102513,148-102530,148-102531,148-102532,148-102533,148-102534,148-102535,148-102536,148-102537,148-102546,148-102547,148-102554,148-102555,148-102556,148-102557,148-102559,148-102584,148-102585,148-102586,148-102587,148-102588,148-102590,148-102592,148-102593,148-102594,148-102595,148-102596,148-102597,148-102599,148-10762,148-10764,148-111234,148-111246,148-111247,148-111249,148-111251,148-111259,148-111261,148-111263,148-111264,148-111265,148-111266,148-111272,148-111274,148-111280,148-111283,148-111290,148-111292,148-111294,148-111301,148-111303,148-111308,148-111310,148-111311,148-111315,148-111316,148-111319,148-111321,148-111322,148-111325,148-111326,148-111327,148-111333,148-111335,148-111336,148-111338,148-111341,148-111343,148-111345,148-111381,148-111384,148-111385,148-111419,148-111421,148-111423,148-111430,148-111431,148-13275,148-13276,148-13277,148-13278,148-13279,148-13280,148-13281,148-13282,148-13283,148-13285,148-13286,148-13318,148-13319,148-13320,148-13321,148-13322,148-13323,148-13324,148-13325,148-13326,148-13327,148-13328,148-14799,148-14800,148-14801,148-14802,148-14804,148-14809,148-14863,148-14868,148-14874,148-14915,148-14928,148-14936,148-14938,148-14941,148-14944,148-14948,148-14951,148-14984,148-14985,148-14988,148-14993,148-14995,148-14996,148-15002,148-15005,148-15024,148-15109,148-15196,148-15260,148-15277,148-15339,148-15406,148-15409,148-15434,148-15443,148-15622,148-15688,148-15690,148-15691,148-15692,148-15694,148-15697,148-15698,148-15699,148-15700,148-15701,148-15703,148-15704,148-15706,148-15708,148-15775,148-15909,148-15910,148-15911,148-15912,148-15913,148-15920,148-15922,148-15923,148-15924,148-15925,148-15927,148-16001,148-16006,148-16011,148-16012,148-16017,148-16018,148-16019,148-16020,148-16021,148-16023,148-16025,148-16026,148-16027,148-16029,148-16031,148-16032,148-16033,148-16034,148-16035,148-16037,148-16058,148-16076,148-16078,148-16086,148-16099,148-16104,148-16107,148-16110,148-16116,148-16165,148-16166,148-3631,148-3632,148-3633,148-3635,148-3636,148-3637,148-3667,148-3669,148-3670,148-3671,148-3672,148-3674,148-3675,148-3690,148-3691,148-3695,148-3698,148-3699,148-3778,148-3780,148-3781,148-3783,148-3784,148-3785,148-4622,148-4663,148-4665,148-4666,148-4668,148-4669,148-4676,148-4677,148-4682,148-4744,148-5079,148-6339,148-6415,148-6470,148-6477,148-6500,148-6505,148-6507,148-6508,148-6511,148-6514,148-6515,148-6517,148-6519,148-6526,148-6527,148-6528,148-6529,148-6530,148-6531,148-6532,148-6534,148-6539,148-6609,148-6621,148-6623,148-6624,148-6626,148-6627,148-6628,148-6629,148-6630,148-6631,148-6632,148-6633,148-6634,148-6635,148-6637,148-6638,148-6639,148-6640,148-6641,148-6642,148-6643,148-6670,148-6800,148-6801,148-6802,148-6912,148-8064,148-8069,148-8087,148-8155,148-9739,148-9757,148-9872,148-9873,148-9874,148-9875,148-9876,148-9877,148-9878,148-9879,148-9880,148-9881,148-9883,148-9885,148-9886,148-9891,148-9927,148-9938,148-9942,148-9951,148-9963,148-9968,148-9998"
                .equalsIgnoreCase (record.<String> get ("itempool")));
          }
        }
        resultIdx++;
      }
    }
  }

  // @Test
  // success case #2
  public void test_InitializeTestSegments_SP1 () throws ReturnStatusException, SQLException {
    UUID oppKey = UUID.fromString ("51BCBEEA-E7C8-4886-AB6D-49990908E5A4");
    _Ref<String> error = new _Ref<> ();
    String formKeyList = null;
    boolean debug = false;
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      _dll._InitializeTestSegments_SP (connection, oppKey, error, formKeyList, debug);
      _logger.info (String.format ("error: %s ", error.get ()));
      assertTrue (error.get () == null);
    }
  }

  // @Test
  // failure case
  public void test_InitializeTestSegments_SP2 () throws ReturnStatusException, SQLException {
    UUID oppKey = UUID.fromString ("51BCBEEA-E7C8-4886-AB6D-49990938E5A4");
    _Ref<String> error = new _Ref<> ();
    String formKeyList = null;
    boolean debug = false;
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      _dll._InitializeTestSegments_SP (connection, oppKey, error, formKeyList, debug);
      _logger.info (String.format ("error: %s ", error.get ()));
      assertTrue ("Segment initialization failed".equalsIgnoreCase (error.get ()));
    }
  }

  // @Test
  public void test_TestOppLastActivity_FN () throws ReturnStatusException, SQLException {
    UUID oppKey = UUID.fromString ("51BCBEEA-E7C8-4886-AB6D-49990908E5A4");
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      Date fromTime = _dll._TestOppLastActivity_FN (connection, oppKey);
      _logger.info (String.format ("_TestOppLastActivity_FN Result -- : %s", fromTime));
      assertTrue (fromTime != null);
    }
  }

  // @Test
  public void testResumeItemPosition_FN () throws ReturnStatusException, SQLException {
    UUID oppKey = UUID.fromString ("9CC6B36B-6A38-436D-9EDB-00010D25F2A7");
    int restart = 0;
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      Integer itemposition = _dll.ResumeItemPosition_FN (connection, oppKey, restart);
      _logger.info (String.format ("ResumeItemPosition_FN Result -- : %d", itemposition));
      assertTrue (itemposition == 2);
    }
  }

  // @Test
  public void test_UnfinishedResponsePages_SP () throws ReturnStatusException, SQLException {
    UUID oppKey = UUID.fromString ("9CC6B36B-6A38-436D-9EDB-00010D25F2A7");
    int newRestart = 0;
    boolean doUpdate = false;
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      SingleDataResultSet result = _dll._UnfinishedResponsePages_SP (connection, oppKey, newRestart, doUpdate);
      Iterator<DbResultRecord> records = result.getRecords ();
      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        assertTrue (record != null);
        _logger.info ("===================_UnfinishedResponsePages_SP Result=======================");
        _logger.info (String.format ("page: %d", record.<Integer> get ("page")));
        _logger.info (String.format ("groupRequired: %d ", record.<Integer> get ("groupRequired")));
        _logger.info (String.format ("numitems: %d ", record.<Integer> get ("numitems")));
        _logger.info (String.format ("validCount %d ", record.<Integer> get ("validCount")));
        _logger.info (String.format ("requiredItems: %d", record.<Integer> get ("requiredItems")));
        _logger.info (String.format ("requiredResponses: %d ", record.<Integer> get ("requiredResponses")));
        _logger.info (String.format ("isVisible: %s ", record.<Boolean> get ("isVisible")));
      }
      _logger.info ("Total no: of records -- " + result.getCount ());
      assertTrue (result.getCount () == 2);
    }
  }

  // @Test
  // Success case
  public void test_ValidateItemInsert_SP () throws ReturnStatusException, SQLException {
    UUID oppKey = UUID.fromString ("8B4EF859-4866-4F7C-A6C5-FF9CEDAB01B7");
    int page = 14;
    int segment = 1;
    String segmentId = "MCA-Science-5";
    String groupId = "G-74-181";
    _Ref<String> msg = new _Ref<> ();
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      _dll._ValidateItemInsert_SP (connection, oppKey, page, segment, segmentId, groupId, msg);
      _logger.info (String.format ("Msg: %s", msg.get ()));
      assertTrue (msg.get () == null);
    }
  }

  // @Test
  // Failure case #1
  public void test_ValidateItemInsert_SP1 () throws ReturnStatusException, SQLException {
    UUID oppKey = UUID.fromString ("4308172C-7FCF-42A9-8CE7-005299111EED");
    int page = 2;
    int segment = 2;
    String segmentId = "OAKS-Read-7";
    String groupId = "G-131-1161";
    _Ref<String> msg = new _Ref<> ();
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      _dll._ValidateItemInsert_SP (connection, oppKey, page, segment, segmentId, groupId, msg);
      _logger.info (String.format ("Msg: %s", msg.get ()));
      assertTrue ("Attempt to overwrite existing page: 2".equalsIgnoreCase (msg.get ()));
    }
  }

  // @Test
  // Failure case #2
  public void test_ValidateItemInsert_SP2 () throws ReturnStatusException, SQLException {
    UUID oppKey = UUID.fromString ("4308172C-7FCF-42A9-8CE7-005299111EED");
    int page = 4;
    int segment = 2;
    String segmentId = "OAKS-Read-7";
    String groupId = "G-131-1161";
    _Ref<String> msg = new _Ref<> ();
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      _dll._ValidateItemInsert_SP (connection, oppKey, page, segment, segmentId, groupId, msg);
      _logger.info (String.format ("Msg: %s", msg.get ()));
      assertTrue ("Segment ID does not match segment position".equalsIgnoreCase (msg.get ()));
    }
  }

  // @Test
  // Failure case #3
  public void test_ValidateItemInsert_SP3 () throws ReturnStatusException, SQLException {
    UUID oppKey = UUID.fromString ("4308172C-7FCF-42A9-8CE7-005299111EED");
    int page = 4;
    int segment = 4;
    String segmentId = "OAKS-Read-7";
    String groupId = "G-131-1161";
    _Ref<String> msg = new _Ref<> ();
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      _dll._ValidateItemInsert_SP (connection, oppKey, page, segment, segmentId, groupId, msg);
      _logger.info (String.format ("Msg: %s", msg.get ()));
      assertTrue ("Attempt to write non-contiguous segment: 4".equalsIgnoreCase (msg.get ()));
    }
  }

  // @Test
  // Failure case #4
  public void test_ValidateItemInsert_SP4 () throws ReturnStatusException, SQLException {
    UUID oppKey = UUID.fromString ("4308172C-7FCF-42A9-8CE7-005299111EED");
    int page = 5;
    int segment = 3;
    String segmentId = "OAKS-Read-7";
    String groupId = "G-131-1161";
    _Ref<String> msg = new _Ref<> ();
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      _dll._ValidateItemInsert_SP (connection, oppKey, page, segment, segmentId, groupId, msg);
      _logger.info (String.format ("Msg: %s", msg.get ()));
      assertTrue ("Attempt to write non-contiguous page: 5".equalsIgnoreCase (msg.get ()));
    }
  }

  // TODO Elena: need script to restore records which would produce not-empty
  // set for this statement for given {oppkey}
  // because this method resets testeereponse records values for given {oppkey}
  // (mostly to nulls and zeros)
  // select * from testeeresponse R, testopportunitysegment S where
  // R._fk_TestOpportunity = ${oppkey} and S._fk_TestOpportunity = ${oppkey}
  // and S.algorithm = 'adaptive' and dateGenerated is not null and R.segment =
  // S.segmentPosition
  // and exists (select * from testeeresponse R1 where R1._fk_TestOpportunity =
  // ${oppkey} and dateLastVisited is not null
  // and R.groupID = R1.groupID and R1.isFieldTest = 0)
  // and exists (select * from testeeresponse R2 where R2._fk_TestOpportunity =
  // ${oppkey} and dateSubmitted is null
  // and R.groupID = R2.groupID and R2.isFieldTest = 0);

  // THis is script I used to find suitable oppKey, but it only valid for one
  // test:
  // select hex(R._fk_TestOpportunity) from testeeresponse R,
  // testopportunitysegment S
  // where R._fk_TestOpportunity = S._fk_TestOpportunity and S.algorithm =
  // 'adaptive' and dateGenerated is not null
  // and R.segment = S.segmentPosition
  // and exists (select * from testeeresponse R1 where R1._fk_TestOpportunity =
  // R._fk_TestOpportunity
  // and dateLastVisited is not null and R.groupID = R1.groupID and
  // R1.isFieldTest = 0)
  // and exists (select * from testeeresponse R2 where R2._fk_TestOpportunity =
  // R._fk_TestOpportunity
  // and dateSubmitted is null and R.groupID = R2.groupID and R2.isFieldTest =
  // 0);
  // @Test
  public void test_RemoveUnanswered_SP () throws ReturnStatusException, SQLException {
    UUID oppKey = UUID.fromString ("18033E19-4F5B-4724-97AD-1E7FF23EFB44");// ("3462D73B-C024-463E-AD6E-D630A40DF69B");//("5C198AAA-6C2E-478B-9ACD-D724043DFC84");
    boolean debug = false;
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      _dll._RemoveUnanswered_SP (connection, oppKey, debug);
    }
  }

  // @Test
  public void testITEMBANK_TestItemGroupData_FN () throws SQLException, ReturnStatusException {
    String testKey = "(Delaware)DCAS-Alt1-PAPER-Mathematics-10-Fall-2012-2013";
    String groupId = "G-153-235";
    String language = "ENU";
    String formkey = "153-83";
    Date starttime = null, now = null;
    Long diff = 0L;

    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      starttime = new Date ();
      DataBaseTable table = _dll.ITEMBANK_TestItemGroupData_FN (connection, testKey, groupId, language, formkey);
      now = new Date ();
      diff = now.getTime () - starttime.getTime ();
      System.out.println (String.format ("ITEMBANK_TestItemGroupData latency: %d millisec", (long) diff));

      // final HelperAbstractDLL dll = new HelperAbstractDLL ();
      final String SQL_QUERY = "select * from ${tblName}";
      Map<String, String> tableName = new HashMap<String, String> ();
      tableName.put ("tblName", table.getTableName ());
      String finalQuery = _myDllHelper.fixDataBasenames (SQL_QUERY, tableName);
      SqlParametersMaps parameters = new SqlParametersMaps ();
      SingleDataResultSet result = _myDllHelper.executeStatement (connection, finalQuery, parameters, false).getResultSets ().next ();
      Iterator<DbResultRecord> records = result.getRecords ();
      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        _logger.info ("=======ITEMBANK_TestItemGroupData_FN Records========");
        _logger.info (String.format ("groupKey: %s", record.<String> get ("groupKey")));
        _logger.info (String.format ("GroupID: %s", record.<String> get ("GroupID")));
        _logger.info (String.format ("ItemPosition: %d", record.<Integer> get ("ItemPosition")));
        _logger.info (String.format ("IsFieldTest: %s", record.<Boolean> get ("IsFieldTest")));
        _logger.info (String.format ("IsActive: %s", record.<Boolean> get ("IsActive")));
        _logger.info (String.format ("BlockID: %s", record.<String> get ("BlockID")));
        _logger.info (String.format ("IsRequired: %s", record.<Boolean> get ("IsRequired")));
        _logger.info (String.format ("ContentLevel: %s", record.<String> get ("ContentLevel")));
        _logger.info (String.format ("ItemType: %s", record.<String> get ("ItemType")));
        _logger.info (String.format ("answerKey : %s", record.<String> get ("answerKey")));
        _logger.info (String.format ("ScorePoint: %d", record.<Integer> get ("ScorePoint")));
        _logger.info (String.format ("ContentSize: %d", record.<Integer> get ("ContentSize")));
        _logger.info (String.format ("bankkey: %d", record.<Long> get ("bankkey")));
        _logger.info (String.format ("itemkey: %d", record.<Long> get ("itemkey")));
        _logger.info (String.format ("bankitemkey: %s", record.<String> get ("bankitemkey")));
        _logger.info (String.format ("itemFile: %s", record.<String> get ("itemFile")));
        _logger.info (String.format ("IRT_b: %s", record.<String> get ("IRT_b")));
        _logger.info (String.format ("FormPosition: %d", record.<Integer> get ("FormPosition")));
        _logger.info (String.format ("strandName: %s", record.<String> get ("strandName")));
      }
      _logger.info ("Total no: of records -- " + result.getCount ());
      assertTrue (result.getCount () == 6);
    }
  }

  // @Test
  public void test_TestReportScores_SP () throws ReturnStatusException, SQLException {
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      UUID oppKey = UUID.fromString ("C5FAD533-8F95-4ECF-AB79-02A8D2AB3C40");
      String which = "Testee";
      SingleDataResultSet result = _dll._TestReportScores_SP (connection, oppKey, which);
      Iterator<DbResultRecord> records = result.getRecords ();
      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        assertTrue (record != null);
        _logger.info ("===================_TestReportScores_SP Result=======================");
        _logger.info (String.format ("ReportLabel: %s", record.<String> get ("ReportLabel")));
        _logger.info (String.format ("[value]: %s ", record.<String> get ("value")));
        _logger.info (String.format ("ReportOrder: %d ", record.<Integer> get ("ReportOrder")));

        assertTrue ("Your score is: ".equalsIgnoreCase (record.<String> get ("ReportLabel")));
        assertTrue ("9/26".equalsIgnoreCase (record.<String> get ("value")));
        assertTrue (record.<Integer> get ("ReportOrder") == 1);
      }
      _logger.info ("Total no: of records -- " + result.getCount ());
    }
  }

  // @Test
  public void test_ComputeSegmentPool_SP () throws ReturnStatusException, SQLException {
    UUID oppKey = UUID.fromString ("9CC6B36B-6A38-436D-9EDB-00010D25F2A7");
    String segmentKey = "(Delaware)DCAS-Alt1-PAPER-Mathematics-10-Fall-2012-2013";
    boolean debug = true;
    UUID sessionKey = null;
    _Ref<Integer> testlen = new _Ref<> ();
    _Ref<Integer> poolcount = new _Ref<> ();
    _Ref<String> poolString = new _Ref<> ();
    Long diff = 0L;
    Date starttime = null;
    Date now = null;

    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      starttime = new Date ();
      MultiDataResultSet resultsets = _dll._ComputeSegmentPool_SP (connection, oppKey, segmentKey, testlen, poolcount, poolString, debug, sessionKey);
      now = new Date ();
      diff = now.getTime () - starttime.getTime ();
      System.out.println (String.format ("_ComputeSegmentPool latency: %d millisec", (long) diff));

      Iterator<SingleDataResultSet> iterator = resultsets.getResultSets ();
      int resultIdx = 0;
      while (iterator.hasNext ()) {
        SingleDataResultSet set = iterator.next ();
        if (resultIdx == 0) {
          DbResultRecord record = set.getRecords ().next ();
          if (record != null) {
            _logger.info (String.format ("testlen: %d", record.<Integer> get ("testlen")));
            _logger.info (String.format ("shortfall: %d", record.<Integer> get ("shortfall")));
            _logger.info (String.format ("strandcnt: %d", record.<Integer> get ("strandcnt")));
            _logger.info (String.format ("newlen: %d", record.<Integer> get ("newlen")));
            _logger.info (String.format ("poolstring: %s", record.<String> get ("poolstring")));

            assertTrue (testlen.get () == 69);
            assertTrue ("153-1057,153-1058,153-1059,153-1060,153-1061,153-1062,153-1087,153-1088,153-1091,153-1092,153-1117,153-1118,153-1119,153-1120,153-1121,153-1334,153-1335,153-1336,153-1337,153-1338,153-1339,153-1819,153-1820,153-1821,153-1822,153-1823,153-1824,153-1825,153-1826,153-1827,153-1828,153-1829,153-1830,153-1831,153-1832,153-1833,153-1834,153-1894,153-1895,153-1896,153-1973,153-1974,153-1975,153-1976,153-1977,153-265,153-266,153-267,153-268,153-269,153-271,153-33,153-34,153-35,153-36,153-368,153-369,153-370,153-371,153-372,153-373,153-39,153-390,153-391,153-392,153-393,153-40,153-400,153-407,153-486,153-490,153-491,153-497,153-500,153-501,153-560,153-561,153-562,153-567,153-570,153-571,153-587,153-593,153-594,153-595,153-596,153-597,153-616,153-617,153-618,153-619,153-620,153-621"
                .equalsIgnoreCase (poolString.get ()));
          }
        }
        if (resultIdx == 1) {
          Iterator<DbResultRecord> iteratorRec = set.getRecords ();
          while (iteratorRec.hasNext ()) {
            DbResultRecord record = iteratorRec.next ();
            _logger.info (String.format ("strand: %s", record.<String> get ("strand")));
            _logger.info (String.format ("minItems: %d", record.<Integer> get ("minItems")));
            _logger.info (String.format ("maxItems: %d", record.<Integer> get ("maxItems")));
            _logger.info (String.format ("poolcnt: %d", record.<Integer> get ("poolcnt")));

            assertTrue (poolcount.get () == 69);
          }
        }
        if (resultIdx == 2) {
          Iterator<DbResultRecord> iteratorRec = set.getRecords ();
          while (iteratorRec.hasNext ()) {
            DbResultRecord record = iteratorRec.next ();
            _logger.info (String.format ("itemkey: %s", record.<String> get ("itemkey")));
            _logger.info (String.format ("isFT: %s", record.<Boolean> get ("isFT")));
            _logger.info (String.format ("isActive: %s", record.<Boolean> get ("isActive")));
            _logger.info (String.format ("strand: %s", record.<String> get ("strand")));
          }
          _logger.info ("Total no: of records -- " + set.getCount ());
          assertTrue (set.getCount () == 93);
        }
        resultIdx++;
      }
    }
  }

  // @Test
  // success case
  public void test_InitializeOpportunity_SP () throws ReturnStatusException, SQLException {
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      UUID oppKey = UUID.fromString ("51BCBEEA-E7C8-4886-AB6D-49990908E5A4");
      _Ref<Integer> testLength = new _Ref<> ();
      _Ref<String> reason = new _Ref<> ();
      String formKeyList = null;
      Long cumDiff = 0L;
      Long diff = 0L;
      Date starttime = null;
      Date now = null;

      starttime = new Date ();
      _dll._InitializeOpportunity_SP (connection, oppKey, testLength, reason, formKeyList);
      now = new Date ();
      diff = now.getTime () - starttime.getTime ();
      System.out.println (String.format ("_InitializeOpportunity latency: %d millisec", (long) diff));

      _logger.info (String.format ("TestLength : %d", testLength.get ()));
      _logger.info (String.format ("Reason: %s", reason.get ()));
      assertTrue (testLength.get () == 54);
      assertTrue (reason.get () == null);
    }
  }

  // @Test
  // Failure case
  public void test_InitializeOpportunity_SP1 () throws ReturnStatusException, SQLException {
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      UUID oppKey = UUID.fromString ("51BCBEEA-E7C8-4886-AB4D-49990908E5A4");
      _Ref<Integer> testLength = new _Ref<> ();
      _Ref<String> reason = new _Ref<> ();
      String formKeyList = null;
      _dll._InitializeOpportunity_SP (connection, oppKey, testLength, reason, formKeyList);
      _logger.info (String.format ("TestLength : %d", testLength.get ()));
      _logger.info (String.format ("Reason: %s", reason.get ()));
      assertTrue (testLength.get () == null);
      assertTrue ("Segment initialization failed".equalsIgnoreCase (reason.get ()));
    }
  }

  // @Test
  public void testIB_GetItemPath_SP () throws SQLException, ReturnStatusException {
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      String clientName = "Minnesota";
      long bankKey = 157;
      long itemKey = 9440;
      SingleDataResultSet result = _dll.IB_GetItemPath_SP (connection, clientName, bankKey, itemKey);
      Iterator<DbResultRecord> records = result.getRecords ();
      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        _logger.info ("========== IB_GetItemPath Record ==========");
        _logger.info (String.format ("itemPath: %s", record.<String> get ("itemPath")));
        assertTrue ("D:\\DataFiles\\BB_Files\\tds_airws_org\\TDSCore_2012-2013\\Bank-157\\Items\\Item-157-9440\\item-157-9440.xml".equalsIgnoreCase (record.<String> get ("itemPath")));
      }
    }
  }

  // @Test
  public void testIB_GetStimulusPath_SP () throws SQLException, ReturnStatusException {
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      String clientName = "Delaware";
      long bankKey = 148;
      long stimulusKey = 178;
      SingleDataResultSet result = _dll.IB_GetStimulusPath_SP (connection, clientName, bankKey, stimulusKey);
      Iterator<DbResultRecord> records = result.getRecords ();
      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        _logger.info ("============= IB_GetStimulusPath Record ==============");
        String path = record.<String> get ("stimulusPath");
        _logger.info (String.format ("stimulusPath: %s", path));
        assertTrue ("D:\\DataFiles\\BB_Files\\tds_airws_org\\TDSCore_2012-2013\\Bank-148\\Stimuli\\Stim-148-178\\stim-148-178.xml".equalsIgnoreCase (path));
      }
    }
  }

  // @Test
  public void testIB_GlobalAccommodations_SP () throws SQLException, ReturnStatusException {
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      String clientName = "Oregon";
      String context = "StudentGLobal";
      MultiDataResultSet resultsets = _dll.IB_GlobalAccommodations_SP (connection, clientName, context);
      Iterator<SingleDataResultSet> iterator = resultsets.getResultSets ();
      int resultIdx = 0;
      while (iterator.hasNext ()) {
        SingleDataResultSet set = iterator.next ();
        if (resultIdx == 0) {
          Iterator<DbResultRecord> iteratorRec = set.getRecords ();
          assertTrue (iteratorRec != null);
          while (iteratorRec.hasNext ()) {
            DbResultRecord record = iteratorRec.next ();
            _logger.info (String.format ("ContextType: %s", record.<String> get ("contextType")));
            _logger.info (String.format ("Context: %s", record.<String> get ("context")));
            _logger.info (String.format ("AccType: %s", record.<String> get ("accType")));
            _logger.info (String.format ("AccValue: %s", record.<String> get ("accValue")));
            _logger.info (String.format ("AccCode: %s", record.<String> get ("accCode")));
            _logger.info (String.format ("IsDefault: %s", record.<Boolean> get ("isDefault")));
            _logger.info (String.format ("AllowCombine: %s", record.<Boolean> get ("allowCombine")));
            _logger.info (String.format ("AllowChange: %s", record.<Boolean> get ("allowChange")));
            _logger.info (String.format ("IsSelectable: %s", record.<Boolean> get ("isSelectable")));
            _logger.info (String.format ("IsVisible: %s", record.<Boolean> get ("isVisible")));
            _logger.info (String.format ("StudentControl: %s", record.<Boolean> get ("studentControl")));
            _logger.info (String.format ("IsFunctional: %s", record.<Boolean> get ("isFunctional")));
            _logger.info (String.format ("DependsOnToolType: %s", record.<String> get ("dependsOnToolType")));

            assertTrue (set.getCount () == 8);
          }
        }
        if (resultIdx == 1) {
          Iterator<DbResultRecord> iteratorRec = set.getRecords ();
          assertTrue (iteratorRec != null);
          while (iteratorRec.hasNext ()) {
            DbResultRecord record = iteratorRec.next ();
            _logger.info (String.format ("clientName: %s", record.<String> get ("clientName")));
            _logger.info (String.format ("ContextType: %s", record.<String> get ("contextType")));
            _logger.info (String.format ("Context: %s", record.<String> get ("context")));
            _logger.info (String.format ("IfType: %s", record.<String> get ("ifType")));
            _logger.info (String.format ("IfValue: %s", record.<String> get ("ifValue")));
            _logger.info (String.format ("ThenType: %s", record.<String> get ("thenType")));
            _logger.info (String.format ("ThenValue: %s", record.<String> get ("thenValue")));
            _logger.info (String.format ("IsDefault: %s", record.<String> get ("isDefault")));

            assertTrue (set.getCount () == 1);
          }
        }
        resultIdx++;
      }
    }
  }

  // @Test
  public void test_IB_ListTests_StoredProc () throws SQLException, ReturnStatusException {
    String clientName = "Oregon";
    int sessionType = 0;
    Long diff = 0L;
    Date starttime = null;
    Date now = null;
    int numRuns = 10;

    String CMD = null;
    DATABASE_TYPE dbDialect = _myDllHelper.getDatabaseDialect ();
    if (dbDialect == DATABASE_TYPE.MYSQL)
      CMD = " call ib_listtests( ${clientname}, ${sessiontype});";
    else if (dbDialect == DATABASE_TYPE.SQLSERVER)
      CMD = "BEGIN; SET NOCOUNT ON; exec IB_ListTests ${clientname}, ${sessiontype}; end;";
    else
      throw new ReturnStatusException ("what are you doing?");
    SqlParametersMaps parms = (new SqlParametersMaps ()).put ("clientname", clientName).put ("sessiontype", sessionType);

    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      for (int i = 0; i < numRuns; i++) {
        starttime = new Date ();
        SingleDataResultSet result = _myDllHelper.executeStatement (connection, CMD, parms, false).getResultSets ().next ();
        now = new Date ();
        diff = now.getTime () - starttime.getTime ();
        System.out.println (String.format ("IB_ListTests StoredProc latency: %d millisec", (long) diff));
      }
    }
  }

  // @Test
  public void test_IB_ListTests_SP () throws SQLException, ReturnStatusException {
    String clientName = "Oregon";
    int sessionType = 0;
    Long diff = 0L;
    Date starttime = null;
    Date now = null;

    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      starttime = new Date ();
      SingleDataResultSet result = _dll.IB_ListTests_SP (connection, clientName, sessionType);
      now = new Date ();
      diff = now.getTime () - starttime.getTime ();
      System.out.println (String.format ("IB_ListTests latency: %d millisec", (long) diff));

      assertTrue (result.getCount () == 128);
      Iterator<DbResultRecord> records = result.getRecords ();

      while (records.hasNext ()) {
        DbResultRecord record = records.next ();

        // _logger.info
        // ("===================IB_ListTests Results=======================");
        // _logger.info (String.format ("TestID: %s", record.<String> get
        // ("TestID")));
        // _logger.info (String.format ("GradeCode: %s", record.<String> get
        // ("GradeCode")));
        // _logger.info (String.format ("Subject: %s", record.<String> get
        // ("Subject")));
        // _logger.info (String.format ("LanguageCode: %s", record.<String> get
        // ("LanguageCode")));
        // _logger.info (String.format ("Language: %s", record.<String> get
        // ("Language")));
        // _logger.info (String.format ("SelectionAlgorithm: %s",
        // record.<String> get ("selectionAlgorithm")));
        // _logger.info (String.format ("DisplayName: %s", record.<String> get
        // ("DisplayName")));

        // _logger.info (String.format ("SortOrder: %d", record.<Integer> get
        // ("SortOrder")));

        // _logger.info (String.format ("AccommodationFamily: %s",
        // record.<String> get ("AccommodationFamily")));
        // _logger.info (String.format ("IsSelectable: %s", record.<Boolean> get
        // ("IsSelectable")));
        Boolean scorebytds = record.<Boolean> get ("ScoreByTDS");
        _logger.info (String.format ("scoreByTDS: %s", scorebytds));
        Boolean validatecompleteness = record.<Boolean> get ("validateCompleteness");
        _logger.info (String.format ("validateCompleteness: %s", validatecompleteness));
        // _logger.info (String.format ("MaxOpportunities: %d", record.<Integer>
        // get ("MaxOpportunities")));
        // _logger.info (String.format ("MinItems: %d", record.<Integer> get
        // ("MinItems")));
        // _logger.info (String.format ("MaxItems: %d", record.<Integer> get
        // ("MaxItems")));
        // _logger.info (String.format ("Prefetch: %d", record.<Integer> get
        // ("Prefetch")));
        // _logger.info (String.format ("StartAbility: %f", record.<Float> get
        // ("StartAbility")));
        // _logger.info (String.format ("windowStart: %s", record.<Date> get
        // ("windowStart")));
        // _logger.info (String.format ("windowEnd: %s", record.<Date> get
        // ("windowEnd")));
        // _logger.info (String.format ("FTStartDate: %s", record.<Date> get
        // ("FTStartDate")));
        // _logger.info (String.format ("FTEndDate: %s", record.<Date> get
        // ("FTEndDate")));
        // _logger.info (String.format ("FTMinItems: %d", record.<Integer> get
        // ("FTMinItems")));
        // _logger.info (String.format ("FTMaxItems: %d", record.<Integer> get
        // ("FTMaxItems")));
        // _logger.info (String.format ("IsSegmented: %s", record.<Boolean> get
        // ("IsSegmented")));
        // _logger.info (String.format ("validateCompletenessKey: %s",
        // record.<String> get ("_Key")));
        // _logger.info (String.format ("formSelection: %s", record.<String> get
        // ("formSelection")));

      }

    }
  }

  // @Test
  public void testT_GetNetworkDiagnostics_SP () throws SQLException, ReturnStatusException {
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      String clientName = "Minnesota_PT";
      String appName = "Student";
      SingleDataResultSet result = _dll.T_GetNetworkDiagnostics_SP (connection, clientName, appName);
      Iterator<DbResultRecord> records = result.getRecords ();
      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        assertTrue (record != null);
        _logger.info ("===================T_GetNetworkDiagnostics_SP Result=======================");
        _logger.info (String.format ("TestLabel: %s", record.<String> get ("TestLabel")));
        _logger.info (String.format ("MinDataRateReqd : %d", record.<Integer> get ("MinDataRateReqd")));
        _logger.info (String.format ("AveItemSize : %d", record.<Integer> get ("AveItemSize")));
        _logger.info (String.format ("ResponseTime : %d", record.<Integer> get ("ResponseTime")));
      }
      _logger.info ("Total no: of records -- " + result.getCount ());
      assertTrue (result.getCount () == 8);
    }
  }

  // @Test
  public void testT_GetVoicePacks_SP () throws SQLException, ReturnStatusException {
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      String clientname = "Delaware";
      String OS_ID = "Linux";
      SingleDataResultSet result = _dll.T_GetVoicePacks_SP (connection, clientname, OS_ID);
      Iterator<DbResultRecord> records = result.getRecords ();
      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        assertTrue (record != null);
        _logger.info ("===================T_GetVoicePacks_SP Result=======================");
        _logger.info (String.format ("OS_ID: %s", record.<String> get ("OS_ID")));
        _logger.info (String.format ("VoicePackName: %s", record.<String> get ("VoicePackName")));
        _logger.info (String.format ("Priority : %d", record.<Integer> get ("Priority")));
        _logger.info (String.format ("LanguageCode: %s", record.<String> get ("LanguageCode")));
      }
      _logger.info ("Total no: of records -- " + result.getCount ());
      assertTrue (result.getCount () == 8);
    }
  }

  
  // @Test
  public void testT_GetApplicationSettings_SP () throws SQLException, ReturnStatusException {
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      String clientName = "Oregon";
      String appName = "ScoreEntry";
      SingleDataResultSet result = _dll.T_GetApplicationSettings_SP (connection, clientName, appName);
      Iterator<DbResultRecord> records = result.getRecords ();
      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        assertTrue (record != null);
        _logger.info ("===================T_GetApplicationSettings_SP Result=======================");
        _logger.info (String.format ("Property: %s", record.<String> get ("Property")));
        _logger.info (String.format ("Type: %s", record.<String> get ("Type")));
        _logger.info (String.format ("Value: %s", record.<String> get ("Value")));
      }
      _logger.info ("Total no: of records -- " + result.getCount ());
      assertTrue (result.getCount () == 7);
    }
  }

  // @Test
  public void testT_LoginRequirements_SP () throws SQLException, ReturnStatusException {
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      String clientName = "Oregon";
      SingleDataResultSet result = _dll.T_TesteeAttributeMetadata_SP (connection, clientName);
      Iterator<DbResultRecord> records = result.getRecords ();
      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        assertTrue (record != null);
        _logger.info ("===================T_LoginRequirements_SP Result=======================");
        _logger.info (String.format ("TDS_ID: %s", record.<String> get ("TDS_ID")));
        _logger.info (String.format ("Label: %s", record.<String> get ("Label")));
        _logger.info (String.format ("SortOrder: %d", record.<Integer> get ("SortOrder")));
      }
      _logger.info ("Total no: of records -- " + result.getCount ());
      assertTrue (result.getCount () == 2);
    }
  }

  // @Test
  // Run the below script each time before you run this test
  // delete
  // from testopprequest
  // where _fk_Session = '18597734-367C-4F90-B2C6-92A36615481C'
  // and _fk_TestOpportunity = '17062A57-B857-451D-B817-8BEC587787CE'
  // and RequestType = 'EMBOSSITEM'
  // and RequestValue =
  // 'D:\DataFiles\BB_Files\tds_airws_org\TDSCore_2012-2013\Bank-104\Items\Item-104-37527\item_37527_enu_nemeth.brf'
  // and ItemPage = 16 and ItemPosition = 16
  // and DateFulfilled is null;
  public void testT_SubmitRequest_SP () throws SQLException, ReturnStatusException {
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      UUID sessionKey = UUID.fromString ("18597734-367C-4F90-B2C6-92A36615481C");
      UUID oppKey = UUID.fromString ("17062A57-B857-451D-B817-8BEC587787CE");
      int itempage = 16;
      int itemposition = 16;
      String requestType = "EMBOSSITEM";
      String requestValue = " D:/DataFiles/BB_Files/tds_airws_org/TDSCore_2012-2013/Bank-104/Items/Item-104-37527/item_37527_enu_nemeth.brf";
      String requestParameters = "FileFormat:BRF";
      String requestDescription = "Item 16 (BRF)";

      final String query = "delete from testopprequest where _fk_Session = ${sessionKey} and _fk_TestOpportunity = ${oppkey} and RequestType = ${requestType} "
          + " and ItemPage = ${itempage} and ItemPosition = ${itemPosition} and DateFulfilled is null";
      SqlParametersMaps parms = (new SqlParametersMaps ()).put ("sessionKey", sessionKey).put ("oppKey", oppKey).put ("requestType", requestType).
          put ("itemPage", itempage).put ("itemPosition", itemposition);
      _myDllHelper.executeStatement (connection, query, parms, false);

      _dll.T_SubmitRequest_SP (connection, sessionKey, oppKey, itempage, itemposition, requestType, requestValue, requestParameters, requestDescription);
    }
  }

  // @Test
  // success case
  // Run the below script each time before you run this test
  // update testopportunity
  // set status = 'started'
  // , prevstatus = 'approved'
  // , datestarted = getdate()
  // , waitingForSegment = null
  // where _fk_session = '8B758F90-E048-4FC7-B4E4-C08DD8A66AC9'
  // and _fk_browser = '6018A101-464C-41FC-94F3-798C5DB02797'
  public void testT_WaitForSegment_SP () throws SQLException, ReturnStatusException {
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      UUID oppKey = UUID.fromString ("B54DDDF6-80D9-4D22-98CA-0006EBB09710");
      UUID sessionKey = UUID.fromString ("8B758F90-E048-4FC7-B4E4-C08DD8A66AC9");
      UUID browserKey = UUID.fromString ("6018A101-464C-41FC-94F3-798C5DB02797");
      int segment = 2;
      boolean entry = false;
      boolean exit = true;

      // execute this statement to prepare for successful test
      final String cmd = "update testopportunity set status = 'started', prevstatus = 'approved', "
          + " datestarted = ${now}, waitingForSegment = null where _fk_session = ${sessionkey} and _fk_browser = ${browserkey}";
      Date now = _dateUtil.getDateWRetStatus (connection);
      SqlParametersMaps parms = (new SqlParametersMaps ()).put ("sessionkey", sessionKey).
          put ("browserkey", browserKey).put ("now", now);
      _myDllHelper.executeStatement (connection, cmd, parms, false);

      SingleDataResultSet result = _dll.T_WaitForSegment_SP (connection, oppKey, sessionKey, browserKey, segment, entry, exit);
      DbResultRecord record = result.getCount () > 0 ? result.getRecords ().next () : null;
      assertTrue (record != null);
      if (record != null) {
        _logger.info (String.format ("Status: %s", record.<String> get ("status")));
        _logger.info (String.format ("Reason: %s", record.<String> get ("reason")));
        assertTrue ("segmentExit".equalsIgnoreCase (record.<String> get ("status")));
        assertTrue (record.<String> get ("reason") == null);
      }
    }
  }

  // @Test
  // failure case
  public void testT_WaitForSegment_SP1 () throws SQLException, ReturnStatusException {
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      UUID oppKey = UUID.fromString ("B54DDDF6-80D9-4D22-48CA-0006EBB09710");
      UUID sessionKey = UUID.fromString ("8B758F90-E048-4FC7-B4E4-C08DD8A66AC9");
      UUID browserKey = UUID.fromString ("6018A101-464C-41FC-94F3-798C5DB02797");
      int segment = 2;
      boolean entry = false;
      boolean exit = true;
      SingleDataResultSet result = _dll.T_WaitForSegment_SP (connection, oppKey, sessionKey, browserKey, segment, entry, exit);
      DbResultRecord record = result.getRecords ().next ();
      assertTrue (record != null);
      String status = record.<String> get ("status");
      String reason = record.<String> get ("Reason");
      String context = record.<String> get ("context");
      String appkey = record.<String> get ("appkey");
      _logger.info (String.format ("Status: %s", status));
      _logger.info (String.format ("Reason: %s", reason));
      _logger.info (String.format ("context: %s", context));
      _logger.info (String.format ("appkey: %s", appkey));
      assertTrue ("denied".equalsIgnoreCase (status));
      assertTrue (("There was a problem with the system.  Please give this number to your test administrator. [10206]").equalsIgnoreCase (reason));
      assertTrue ("_ValidateTesteeAccess".equalsIgnoreCase (context));
      assertTrue ("The session keys do not match; please consult your test administrator".equalsIgnoreCase (appkey));
    }
  }

  // @Test
  // success case
  // need to run the below query each time before you run this test case
  // update testopportunity set status = 'started', prevstatus = 'approved'
  // where _fk_session = '38893E8D-A5D2-4BF8-906E-3C2CBFBACC30' and _fk_browser
  // = '99CDB138-17B3-4DFA-B892-D4E0060FD477'
  public void testT_UpdateScoredResponse_SP () throws ReturnStatusException, SQLException {
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      UUID oppKey = UUID.fromString ("9F1DCD39-111A-4417-8464-00A0E1291E4D");
      UUID session = UUID.fromString ("38893E8D-A5D2-4BF8-906E-3C2CBFBACC30");
      UUID browserId = UUID.fromString ("99CDB138-17B3-4DFA-B892-D4E0060FD477");
      String itemId = "131-100550";
      int page = 1;
      int position = 1;
      String dateCreated = null;
      int responseSequence = 1;
      int score = -1;
      String response = "A";
      boolean isSelected = true;
      boolean isValid = true;
      int scoreLatency = 0;
      String scoreStatus = "WaitingForMachineScore";
      String scoreRationale = null;
      Long diff = 0L;
      Date starttime = null;
      Date now = null;
      // execute this statement to prepare for successful test
      final String cmd = "update testopportunity set status = 'started', prevstatus = 'approved' "
          + "  where _fk_session = ${session} and _fk_browser = ${browserId}";
      SqlParametersMaps parms = (new SqlParametersMaps ()).put ("session", session).put ("browserid", browserId);
      _myDllHelper.executeStatement (connection, cmd, parms, false);

      starttime = new Date ();
      SingleDataResultSet result = _dll.T_UpdateScoredResponse_SP (connection, oppKey, session, browserId, itemId, page, position, dateCreated, responseSequence, score, response, isSelected,
          isValid,
          scoreLatency, scoreStatus, scoreRationale, 1F);
      now = new Date ();
      diff = now.getTime () - starttime.getTime ();
      System.out.println (String.format ("T_UpdateScoredResponse latency: %d millisec", (long) diff));

      DbResultRecord record = result.getCount () > 0 ? result.getRecords ().next () : null;
      assertTrue (record != null);
      _logger.info ("===================T_UpdateScoredResponse_SP Result=======================");
      _logger.info (String.format ("Status: %s", record.<String> get ("status")));
      _logger.info (String.format ("number: %d", record.<Integer> get ("number")));
      _logger.info (String.format ("Reason: %s", record.<String> get ("reason")));
      _logger.info (String.format ("scoremark: %s", record.<String> get ("scoremark")));
      _logger.info (String.format ("responseCount: %d", record.<Integer> get ("responseCount")));
      _logger.info (String.format ("abortSim: %s", record.<Boolean> get ("abortSim")));

      assertTrue ("updated".equalsIgnoreCase (record.<String> get ("status")));
      assertTrue (record.<Integer> get ("number") == 1);
      assertTrue (record.<String> get ("reason") == null);
      assertTrue (record.<Integer> get ("responseCount") == 1);
      assertTrue (record.<Boolean> get ("abortSim") == false);
    }
  }

  // @Test
  // failure case #1
  public void testT_UpdateScoredResponse_SP1 () throws ReturnStatusException, SQLException {
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      UUID oppKey = UUID.fromString ("47D78741-6DBD-4EDC-A698-28454C404FB4");
      UUID session = UUID.fromString ("1FCEBD1D-A7A2-498C-AA6E-E6AB8BF7A31F");
      UUID browserId = UUID.fromString ("DF0CEB29-0F57-4AAE-B576-7AF6737B9577");
      String itemId = "131-100550";
      int page = 1;
      int position = 1;
      String dateCreated = null;
      int responseSequence = 1;
      int score = -1;
      String response = "A";
      boolean isSelected = true;
      boolean isValid = true;
      int scoreLatency = 0;
      String scoreStatus = "WaitingForMachineScore";
      String scoreRationale = null;
      Long diff = 0L;
      Date starttime = null;
      Date now = null;

      starttime = new Date ();
      SingleDataResultSet result = _dll.T_UpdateScoredResponse_SP (connection, oppKey, session, browserId, itemId, page, position, dateCreated, responseSequence, score, response, isSelected,
          isValid,
          scoreLatency, scoreStatus, scoreRationale, 1F);
      now = new Date ();
      diff = now.getTime () - starttime.getTime ();
      System.out.println (String.format ("T_UpdateScoredResponse latency: %d millisec", (long) diff));

      DbResultRecord record = result.getRecords ().next ();
      assertTrue (record != null);
      String status = record.<String> get ("status");
      String reason = record.<String> get ("Reason");
      String context = record.<String> get ("context");
      String appkey = record.<String> get ("appkey");
      _logger.info (String.format ("Status: %s", status));
      _logger.info (String.format ("Reason: %s", reason));
      _logger.info (String.format ("context: %s", context));
      _logger.info (String.format ("appkey: %s", appkey));
      assertTrue ("denied".equalsIgnoreCase (status));
      assertTrue (("There was a problem with the system.  Please give this number to your test administrator. [10206]").equalsIgnoreCase (reason));
      assertTrue ("_ValidateTesteeAccess".equalsIgnoreCase (context));
      assertTrue ("The session keys do not match; please consult your test administrator".equalsIgnoreCase (appkey));
    }
  }

  // @Test
  // failure case #2
  public void testT_UpdateScoredResponse_SP2 () throws ReturnStatusException, SQLException {
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      UUID oppKey = UUID.fromString ("BD788A67-0304-4206-A5BB-357EB25A039E");
      UUID session = UUID.fromString ("8E04ABFD-556D-483A-8C74-830A380ECF78");
      UUID browserId = UUID.fromString ("0CDCEF12-4B80-451D-8611-686CC3421BB0");
      String itemId = "131-100550";
      int page = 1;
      int position = 1;
      String dateCreated = null;
      int responseSequence = 1;
      int score = -1;
      String response = "A";
      boolean isSelected = true;
      boolean isValid = true;
      int scoreLatency = 0;
      String scoreStatus = "WaitingForMachineScore";
      String scoreRationale = null;
      SingleDataResultSet result = _dll.T_UpdateScoredResponse_SP (connection, oppKey, session, browserId, itemId, page, position, dateCreated, responseSequence, score, response, isSelected,
          isValid,
          scoreLatency, scoreStatus, scoreRationale, 1F);
      DbResultRecord record = result.getRecords ().next ();
      assertTrue (record != null);
      String status = record.<String> get ("status");
      String reason = record.<String> get ("Reason");
      String context = record.<String> get ("context");
      String appkey = record.<String> get ("appkey");
      _logger.info (String.format ("Status: %s", status));
      _logger.info (String.format ("Reason: %s", reason));
      _logger.info (String.format ("context: %s", context));
      _logger.info (String.format ("appkey: %s", appkey));
      assertTrue ("denied".equalsIgnoreCase (status));
      assertTrue (("Your test has been interrupted. Please check with your Test Administrator to resume your test. [10136]").equalsIgnoreCase (reason));
      assertTrue ("T_UpdateScoredResponse".equalsIgnoreCase (context));
      assertTrue ("Your test opportunity has been interrupted. Please check with your Test Administrator to resume your test.".equalsIgnoreCase (appkey));
    }
  }

  // @Test
  // success case
  public void testT_GetDisplayScores_SP () throws ReturnStatusException, SQLException {
    UUID oppKey = UUID.fromString ("C5FAD533-8F95-4ECF-AB79-02A8D2AB3C40");
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      MultiDataResultSet resultsets = _dll.T_GetDisplayScores_SP (connection, oppKey);
      Iterator<SingleDataResultSet> iterator = resultsets.getResultSets ();
      int resultIdx = 0;
      while (iterator.hasNext ()) {
        SingleDataResultSet set = iterator.next ();
        if (resultIdx == 0) {
          Iterator<DbResultRecord> iteratorRec = set.getRecords ();
          while (iteratorRec.hasNext ()) {
            DbResultRecord record = iteratorRec.next ();
            assertTrue (record != null);
            _logger.info (String.format ("ReportLabel: %s", record.<String> get ("ReportLabel")));
            _logger.info (String.format ("value: %s ", record.<String> get ("value")));
            _logger.info (String.format ("ReportOrder: %d ", record.<Integer> get ("ReportOrder")));

            assertTrue ("Your score is: ".equalsIgnoreCase (record.<String> get ("ReportLabel")));
            assertTrue ("9/26".equalsIgnoreCase (record.<String> get ("value")));
            assertTrue (record.<Integer> get ("ReportOrder") == 1);
          }
        }
        if (resultIdx == 1) {
          DbResultRecord record = set.getRecords ().next ();
          assertTrue (record != null);
          if (record != null) {
            // TODO Udaya, fix scoreByTDS column value
            _logger.info (String.format ("scoreByTDS: %s", record.<Boolean> get ("scoreByTDS")));
            _logger.info (String.format ("showscores: %s", record.<Integer> get ("showscores")));
            _logger.info (String.format ("scorestatus: %s", record.<String> get ("scorestatus")));

            assertTrue (record.<Boolean> get ("scoreByTDS") == true);
            assertTrue (record.<Integer> get ("showscores") == 1);
            assertTrue ("SCORED".equalsIgnoreCase (record.<String> get ("scorestatus")));
          }
        }
        resultIdx++;
      }
    }
  }

  // @Test
  // failure case
  public void testT_GetDisplayScores_SP1 () throws ReturnStatusException, SQLException {
    UUID oppKey = UUID.fromString ("C5FAD533-8F95-4ECF-AB79-02A852AB3C40");
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      MultiDataResultSet resultsets = _dll.T_GetDisplayScores_SP (connection, oppKey);
      Iterator<SingleDataResultSet> iterator = resultsets.getResultSets ();
      int resultIdx = 0;
      while (iterator.hasNext ()) {
        SingleDataResultSet set = iterator.next ();
        if (resultIdx == 0) {
          Iterator<DbResultRecord> iteratorRec = set.getRecords ();
          while (iteratorRec.hasNext ()) {
            DbResultRecord record = iteratorRec.next ();
            assertTrue (record != null);
            _logger.info (String.format ("ReportLabel: %s", record.<String> get ("ReportLabel")));
            _logger.info (String.format ("value: %s ", record.<String> get ("value")));
            _logger.info (String.format ("ReportOrder: %d ", record.<Integer> get ("ReportOrder")));

            assertTrue (record.<String> get ("ReportLabel") == null);
            assertTrue (record.<String> get ("value") == null);
            assertTrue (record.<Integer> get ("ReportOrder") == null);
          }
        }
        if (resultIdx == 1) {
          DbResultRecord record = set.getRecords ().next ();
          assertTrue (record != null);
          if (record != null) {
            _logger.info (String.format ("scoreByTDS: %s", record.<Boolean> get ("scoreByTDS")));
            _logger.info (String.format ("showscores: %s", record.<Integer> get ("showscores")));
            _logger.info (String.format ("scorestatus: %s", record.<String> get ("scorestatus")));

            assertTrue (record.<Boolean> get ("scoreByTDS") == false);
            assertTrue (record.<Integer> get ("showscores") == 0);
            assertTrue ("Test has not completed".equalsIgnoreCase (record.<String> get ("scorestatus")));
          }
        }
        resultIdx++;
      }
    }
  }

  // @Test
  // Success case
  public void test_T_ExitSegment_SP () throws SQLException, ReturnStatusException {
    UUID oppKey = UUID.fromString ("9F1DCD39-111A-4417-8464-00A0E1291E4D");
    UUID sessionKey = UUID.fromString ("38893E8D-A5D2-4BF8-906E-3C2CBFBACC30");
    UUID browserKey = UUID.fromString ("99CDB138-17B3-4DFA-B892-D4E0060FD477");
    int segment = 1;
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      SingleDataResultSet result = _dll.T_ExitSegment_SP (connection, segment, oppKey, sessionKey, browserKey);
      DbResultRecord record = result.getCount () > 0 ? result.getRecords ().next () : null;
      assertTrue (record != null);
      if (record != null) {
        _logger.info (String.format ("Status: %s", record.<String> get ("status")));
        _logger.info (String.format ("Reason: %s", record.<String> get ("reason")));

        assertTrue ("SUCCESS".equalsIgnoreCase (record.<String> get ("status")));
        assertTrue (record.<String> get ("reason") == null);
      }
    }
  }

  // @Test
  // Failure case
  public void test_T_ExitSegment_SP1 () throws SQLException, ReturnStatusException {
    UUID oppKey = UUID.fromString ("AAA3217A-2A82-405C-8ED9-20F1AAF29ECE");
    UUID sessionKey = UUID.fromString ("C4967141-EEE0-4F3C-9FA3-B7CD4C0BC4B1");
    UUID browserKey = UUID.fromString ("99F2085F-0E3F-48B7-AA5E-A325C5D6A84C");
    int segment = 1;
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      SingleDataResultSet result = _dll.T_ExitSegment_SP (connection, segment, oppKey, sessionKey, browserKey);
      DbResultRecord record = result.getRecords ().next ();
      assertTrue (record != null);
      String status = record.<String> get ("status");
      String reason = record.<String> get ("Reason");
      String context = record.<String> get ("context");
      String appkey = record.<String> get ("appkey");
      _logger.info (String.format ("Status: %s", status));
      _logger.info (String.format ("Reason: %s", reason));
      _logger.info (String.format ("context: %s", context));
      _logger.info (String.format ("appkey: %s", appkey));
      assertTrue ("failed".equalsIgnoreCase (status));
      assertTrue (("The session keys do not match; please consult your test administrator [-----]").equalsIgnoreCase (reason));
      assertTrue ("T_ExitSegment".equalsIgnoreCase (context));
      assertTrue ("The session keys do not match; please consult your test administrator".equalsIgnoreCase (appkey));
    }
  }

  // @Test
  // sucess case
  public void test_T_ApproveAccommodations_SP () throws SQLException, ReturnStatusException {
    UUID oppKey = UUID.fromString ("FD4C3B16-2D78-4234-A97A-F3168237B478");
    UUID sessionKey = UUID.fromString ("B717F914-6C57-414B-A875-CBC3286A4A8A");
    UUID browserKey = UUID.fromString ("A8B90459-DFD0-4551-880A-DC00633CDCF8");
    int segment = 0;
    String segmentAccoms = "TDS_BP1";
    Long diff = 0L;
    Date starttime = null;
    Date now = null;

    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      starttime = new Date ();
      _dll.T_ApproveAccommodations_SP (connection, segment, oppKey, sessionKey, browserKey, segmentAccoms);
      now = new Date ();
      diff = now.getTime () - starttime.getTime ();
      System.out.println (String.format ("_T_ApproveAccommodations latency: %d millisec", (long) diff));
    }
  }

  // @Test
  // failure case
  public void test_T_ApproveAccommodations_SP1 () throws SQLException, ReturnStatusException {
    UUID oppKey = UUID.fromString ("FCEC4C40-0556-42D0-BF42-7A64C491AD56");
    UUID sessionKey = UUID.fromString ("C19F428D-4395-4B65-A567-6D418711A2DC");
    UUID browserKey = UUID.fromString ("5DCE7D2A-E724-4930-AC9E-B0F2FD4D263D");
    int segment = 0;
    String segmentAccoms = "TDS_BP1";
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      SingleDataResultSet result = _dll.T_ApproveAccommodations_SP (connection, segment, oppKey, sessionKey, browserKey, segmentAccoms);
      DbResultRecord record = result.getRecords ().next ();
      assertTrue (record != null);
      String status = record.<String> get ("status");
      String reason = record.<String> get ("Reason");
      String context = record.<String> get ("context");
      String appkey = record.<String> get ("appkey");
      _logger.info (String.format ("Status: %s", status));
      _logger.info (String.format ("Reason: %s", reason));
      _logger.info (String.format ("context: %s", context));
      _logger.info (String.format ("appkey: %s", appkey));
      assertTrue ("failed".equalsIgnoreCase (status));
      assertTrue (("Student can only self-approve unproctored sessions [-----]").equalsIgnoreCase (reason));
      assertTrue ("T_ApproveAccommodations".equalsIgnoreCase (context));
      assertTrue ("Student can only self-approve unproctored sessions".equalsIgnoreCase (appkey));
    }
  }

  // Run the below script each time when you run the below test
  // DELETE from testeeresponse where _fk_TestOpportunity =
  // 'D2AAA0AA-E338-4A96-A336-871838F6CB00' and position = 1

  // @Test
  // success case
  public void test_T_InsertItems_SP () throws ReturnStatusException, SQLException {
    UUID oppKey = UUID.fromString ("D2AAA0AA-E338-4A96-A336-871838F6CB00");
    UUID sessionKey = UUID.fromString ("9B6CF5E5-E202-451D-8C89-9207440A2A84");
    UUID browserId = UUID.fromString ("9CFCD4AA-14A6-462D-96C3-D4831CC408B5");
    int segment = 1;
    String segmentId = "MCA-Science-HS";
    int page = 1;
    String groupId = "G-159-59";
    String itemKeys = null;
    char delimiter = ',';
    int groupItemsRequired = -1;
    Float groupB = null;
    int debug = 0;
    boolean noinsert = false;
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      // execute this statement to prepared for inserts
      final String cmd = "DELETE from testeeresponse where _fk_TestOpportunity = ${oppkey} and position=1";
      SqlParametersMaps parms = (new SqlParametersMaps ()).put ("oppkey", oppKey);
      _myDllHelper.executeStatement (connection, cmd, parms, false);

      MultiDataResultSet resultsets = _dll.T_InsertItems_SP (connection, oppKey, sessionKey, browserId, segment, segmentId, page, groupId, itemKeys,
          delimiter, groupItemsRequired, groupB, debug, noinsert);
      Iterator<SingleDataResultSet> iterator = resultsets.getResultSets ();
      int resultIdx = 0;
      while (iterator.hasNext ()) {
        SingleDataResultSet set = iterator.next ();
        if (resultIdx == 0) {
          Iterator<DbResultRecord> iteratorRec = set.getRecords ();
          while (iteratorRec.hasNext ()) {
            DbResultRecord record = iteratorRec.next ();
            assertTrue (record != null);
            _logger.info (String.format ("status: %s", record.<String> get ("status")));
            _logger.info (String.format ("number: %d ", record.<Integer> get ("number")));
            _logger.info (String.format ("dateCreated: %s ", record.<String> get ("dateCreated")));
            _logger.info (String.format ("reason: %s ", record.<String> get ("reason")));

            assertTrue ("inserted".equalsIgnoreCase (record.<String> get ("status")));
            assertTrue (record.<Integer> get ("number") == 1);
            assertTrue (record.<String> get ("reason") == null);
          }
        }
        if (resultIdx == 1) {
          DbResultRecord record = set.getRecords ().next ();
          assertTrue (record != null);
          if (record != null) {
            _logger.info (String.format ("bankitemkey: %s", record.<String> get ("bankitemkey")));
            _logger.info (String.format ("bankkey: %d", record.<Long> get ("bankkey")));
            _logger.info (String.format ("itemkey: %d", record.<Long> get ("itemkey")));
            _logger.info (String.format ("page: %d", record.<Integer> get ("page")));
            _logger.info (String.format ("position: %d", record.<Integer> get ("position")));
            _logger.info (String.format ("format: %s", record.<String> get ("format")));

            assertTrue ("159-446".equalsIgnoreCase (record.<String> get ("bankitemkey")));
            assertTrue (record.<Long> get ("bankkey") == 159);
            assertTrue (record.<Long> get ("itemkey") == 446);
            assertTrue (record.<Integer> get ("page") == 1);
            assertTrue (record.<Integer> get ("position") == 1);
            assertTrue ("GI".equalsIgnoreCase (record.<String> get ("format")));
          }
        }
        resultIdx++;
      }
    }
  }

  // @Test
  // failure case #1
  public void test_T_InsertItems_SP1 () throws SQLException, ReturnStatusException {
    UUID oppKey = UUID.fromString ("9F1DCD39-111A-4417-8464-00A0E1291E4D");
    UUID sessionKey = UUID.fromString ("38893E8D-A5D2-4BF8-906E-3C2CBFBACC30");
    UUID browserId = UUID.fromString ("99CDB138-17B3-4DFA-B892-D4E0060FD477");
    int segment = 1;
    String segmentId = "MCA-Science-5";
    int page = 14;
    String groupId = "G-74-181";
    String itemKeys = "148-100006";
    char delimiter = ',';
    int groupItemsRequired = -1;
    Float groupB = null;
    int debug = 0;
    boolean noinsert = false;
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      MultiDataResultSet resultsets = _dll.T_InsertItems_SP (connection, oppKey, sessionKey, browserId, segment, segmentId, page, groupId, itemKeys, delimiter, groupItemsRequired, groupB, debug,
          noinsert);

      SingleDataResultSet set = resultsets.getResultSets ().next ();
      DbResultRecord record = set.getRecords ().next ();
      assertTrue (record != null);
      assertTrue (record.hasColumn ("reason") == true);
      String status = record.<String> get ("Status");
      String reason = record.<String> get ("reason");
      String context = record.<String> get ("context");
      String appkey = record.<String> get ("appkey");
      _logger.info (String.format ("Status: %s", status));
      _logger.info (String.format ("Reason: %s", reason));
      _logger.info (String.format ("context: %s", context));
      _logger.info (String.format ("appkey: %s", appkey));
      assertTrue ("failed".equalsIgnoreCase (status));
      assertTrue (("Database record insertion failed for new test items [-----]").equalsIgnoreCase (reason));
      assertTrue ("T_InsertItems".equalsIgnoreCase (context));
      assertTrue ("Database record insertion failed for new test items".equalsIgnoreCase (appkey));
    }
  }

  // @Test
  // failure case #2
  public void test_T_InsertItems_SP2 () throws SQLException, ReturnStatusException {
    UUID oppKey = UUID.fromString ("8B4EF859-4866-4F7C-A6C5-FF9CEDA201B7");
    UUID sessionKey = UUID.fromString ("38893E8D-A5D2-4BF8-906E-3C2CBFBACC30");
    UUID browserId = UUID.fromString ("EC224E28-5983-4166-B4B1-17A545DA888C");
    int segment = 1;
    String segmentId = "MCA-Science-5";
    int page = 14;
    String groupId = "G-74-181";
    String itemKeys = "148-100006";
    char delimiter = ',';
    int groupItemsRequired = -1;
    Float groupB = null;
    int debug = 0;
    boolean noinsert = false;
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      MultiDataResultSet resultsets = _dll.T_InsertItems_SP (connection, oppKey, sessionKey, browserId, segment, segmentId, page, groupId, itemKeys, delimiter, groupItemsRequired, groupB, debug,
          noinsert);
      SingleDataResultSet set = resultsets.getResultSets ().next ();

      DbResultRecord record = set.getRecords ().next ();
      assertTrue (record != null);
      assertTrue (record.hasColumn ("reason") == true);
      String status = record.<String> get ("Status");
      String reason = record.<String> get ("reason");
      String context = record.<String> get ("context");
      String appkey = record.<String> get ("appkey");
      _logger.info (String.format ("Status: %s", status));
      _logger.info (String.format ("Reason: %s", reason));
      _logger.info (String.format ("context: %s", context));
      _logger.info (String.format ("appkey: %s", appkey));
      assertTrue ("denied".equalsIgnoreCase (status));
      assertTrue (("The session keys do not match; please consult your test administrator [-----]").equalsIgnoreCase (reason));
      assertTrue ("_ValidateTesteeAccesss".equalsIgnoreCase (context));
      assertTrue ("The session keys do not match; please consult your test administrator".equalsIgnoreCase (appkey));
    }
  }

  // Run the below script each time when you run the below test
  // update testopportunity
  // set status = 'approved'
  // , prevstatus = 'started'
  // , datestarted = getdate()
  // where _fk_session = 'C91FF07A-5F44-4446-BEA4-E1D7AA4FC8CC'
  // and _fk_browser = 'A10304D5-B1EA-4408-B068-5D8EC28ACA71'

  // @Test
  public void test_T_StartTestOpportunity_SP () throws SQLException, ReturnStatusException {
    UUID oppKey = UUID.fromString ("E995CA55-1DAA-48AB-AB50-D0623B0C1427");
    UUID sessionKey = UUID.fromString ("C91FF07A-5F44-4446-BEA4-E1D7AA4FC8CC");
    UUID browserId = UUID.fromString ("A10304D5-B1EA-4408-B068-5D8EC28ACA71");
    String formKeyList = null;
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {

      // execute this statement to prepare for successful test
      final String cmd = "update testopportunity set status = 'approved', prevstatus = 'started', "
          + " datestarted = ${now} where _fk_session = ${sessionkey} and _fk_browser = ${browserId}";
      Date now = _dateUtil.getDateWRetStatus (connection);
      SqlParametersMaps parms = (new SqlParametersMaps ()).put ("sessionkey", sessionKey).
          put ("browserid", browserId).put ("now", now);
      _myDllHelper.executeStatement (connection, cmd, parms, false);

      SingleDataResultSet result = _dll.T_StartTestOpportunity_SP (connection, oppKey, sessionKey, browserId, formKeyList);
      Iterator<DbResultRecord> records = result.getRecords ();
      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        assertTrue (record != null);
        if (record != null) {
          _logger.info ("===================T_StartTestOpportunity_SP Result=======================");
          _logger.info (String.format ("Status: %s", record.<String> get ("status")));
          _logger.info (String.format ("restart: %d", record.<Integer> get ("restart")));
          _logger.info (String.format ("TestLength: %d", record.<Integer> get ("TestLength")));
          _logger.info (String.format ("reason: %s", record.<String> get ("reason")));
          _logger.info (String.format ("interfaceTimeout: %d", record.<Integer> get ("interfaceTimeout")));
          _logger.info (String.format ("OppRestart: %d", record.<Integer> get ("OppRestart")));
          Float ability = record.<Float> get ("initialability");
          _logger.info (String.format ("initialability: %f", record.<Float> get ("initialability")));
          _logger.info (String.format ("excludeItemTypes: %s", record.<String> get ("excludeItemTypes")));
          _logger.info (String.format ("contentloadtimeout: %d", record.<Integer> get ("contentloadtimeout")));
          _logger.info (String.format ("requestInterfaceTimeout: %d", record.<Integer> get ("requestInterfaceTimeout")));
          _logger.info (String.format ("prefetch: %d", record.<Integer> get ("Prefetch")));
          _logger.info (String.format ("validateCompleteness: %s", record.<Boolean> get ("validateCompleteness")));
          _logger.info (String.format ("ScoreByTDS: %s", record.<Boolean> get ("scoreByTds")));
          _logger.info (String.format ("startPosition: %d", record.<Integer> get ("startPosition")));
          _logger.info (String.format ("Segments: %d", record.<Integer> get ("Segments")));

          assertTrue ("started".equalsIgnoreCase (record.<String> get ("status")));
          assertTrue (record.<Integer> get ("TestLength") == 54);
          assertTrue (record.<String> get ("reason") == null);
          assertTrue (record.<Integer> get ("interfaceTimeout") == 20);
          assertTrue (record.<Integer> get ("OppRestart") == 20);
          assertEquals (0.0, ability, 2.0e-6);
          // assertTrue(record.<Float> get ("initialability") == null);
          assertTrue (record.<String> get ("excludeItemTypes") == null);
          assertTrue (record.<Integer> get ("contentloadtimeout") == 120);
          assertTrue (record.<Integer> get ("requestInterfaceTimeout") == 120);
          assertTrue (record.<Integer> get ("Prefetch") == 2);
          assertTrue (record.<Boolean> get ("validateCompleteness") == false);
          assertTrue (record.<Boolean> get ("scoreByTds") == false);
          assertTrue (record.<Integer> get ("startPosition") == 1);
          assertTrue (record.<Integer> get ("Segments") == 0);
        }
      }
    }
  }

  // @Test
  // failure case
  public void test_T_StartTestOpportunity_SP1 () throws SQLException, ReturnStatusException {
    UUID oppKey = UUID.fromString ("E995CA55-1DAA-18AB-AB50-D0623B0C1427");
    UUID sessionKey = UUID.fromString ("C91FF07A-5F44-4446-BEA4-E1D7AA4FC8CC");
    UUID browserId = UUID.fromString ("A10304D5-B1EA-4408-B068-5D8EC28ACA71");
    String formKeyList = null;
    try (SQLConnection connection = _myDllHelper.getSQLConnection ()) {
      SingleDataResultSet result = _dll.T_StartTestOpportunity_SP (connection, oppKey, sessionKey, browserId, formKeyList);
      DbResultRecord record = result.getRecords ().next ();
      assertTrue (record != null);
      String status = record.<String> get ("status");
      String reason = record.<String> get ("Reason");
      String context = record.<String> get ("context");
      String appkey = record.<String> get ("appkey");
      _logger.info (String.format ("Status: %s", status));
      _logger.info (String.format ("Reason: %s", reason));
      _logger.info (String.format ("context: %s", context));
      _logger.info (String.format ("appkey: %s", appkey));
      assertTrue ("denied".equalsIgnoreCase (status));
      assertTrue (("There was a problem with the system.  Please give this number to your test administrator. [10206]").equalsIgnoreCase (reason));
      assertTrue ("_ValidateTesteeAccess".equalsIgnoreCase (context));
      assertTrue ("The session keys do not match; please consult your test administrator".equalsIgnoreCase (appkey));
    }
  }

  /**
   * Formats the rows in a SingleDataResultSet as a string with tabs between
   * columns and newlines between rows. The first line contains the column names
   * from the query.
   * 
   * @param result
   *          SingleDataResultSet containing all of the rows to be formatted.
   * @return String containing all of the output rows in a tab formatted layout.
   */
  public String formatTestOutputRows (SingleDataResultSet result) {
    StringBuilder output = new StringBuilder ("\n");
    StringBuilder header = new StringBuilder ("Record#");
    Iterator<DbResultRecord> records = result.getRecords ();
    int count = 0;
    while (records.hasNext ()) {
      DbResultRecord record = records.next ();
      StringBuilder rowOutput = new StringBuilder ();
      for (int index = 0; index < record.getNumberOfColumns (); ++index) {
        if (count == 0) {
          header.append ("\t").append (record.getIndexToColumn (index + 1));
        }
        rowOutput.append ("\t").append (record.get (index + 1));
      }
      if (count == 0) {
        output.append (header).append ("\n");
      }
      output.append (count).append (rowOutput).append ("\n");
      count++;
    }

    return output.toString ();
  }

}

class MyStudentDLLHelper extends AbstractDAO
{
  // TODO Just put it for removing compilation errors
  public String fixDataBasenames (String query, Map<String, String> paramMap) {
    return query;
  }
}
