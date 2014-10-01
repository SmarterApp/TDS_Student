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

import javax.sql.DataSource;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import tds.dll.api.IRtsDLL;
import AIR.Common.DB.AbstractConnectionManager;
import AIR.Common.DB.SQLConnection;
import AIR.Common.Helpers._Ref;
import AIR.test.framework.AbstractTest;
import TDS.Shared.Exceptions.ReturnStatusException;

@Configuration
@ContextConfiguration("classpath:root-context.xml")
public class TestRtsDLL extends AbstractTest
{
  private static final Logger _logger = LoggerFactory.getLogger(TestRtsDLL.class);
  @Autowired
  @Qualifier("iRtsDLL")
  IRtsDLL _dll = null;
  
  @Autowired
  @Qualifier( "applicationDataSource" )
  private DataSource applicationDataSource;

  
  @Autowired
	private AbstractConnectionManager abstractConnectionManager;

  // declare @instkey bigint;
  // exec _ValidateInstitutionMatch 'Oregon',553163, 20424, @instkey output;
  // select @instkey
  @Test
  public void test_ValidateInstitutionMatch_SP () throws SQLException, ReturnStatusException {
    try (SQLConnection connection = abstractConnectionManager.getConnection ()) {
      String clientname = "Oregon";
      Long testeeKey = 553163L;
      Long proctorKey = 20424L;
      _Ref<String> instKey = new _Ref<String> ();

      _dll._ValidateInstitutionMatch_SP (connection, clientname, testeeKey, proctorKey, instKey);
      assertTrue(instKey.get () != null);
      assertTrue(instKey.get ().equals ("370331"));
      _logger.info ("InstitutionKey: " + instKey.get ());

    }
  }

  // declare @attValue varchar(200);
  // exec _GetRTSAttribute 'Oregon', 30000, '--ETHNICITY--', @attvalue output;
  // select @attvalue;
  @Test
  public void test_GetRTSAttribute_SP () throws SQLException, ReturnStatusException {
    String clientname = "Oregon";
    Long testee = 30000L;
    String attname = "--ETHNICITY--";
    _Ref<String> attValue = new _Ref<String> ();

    try (SQLConnection connection = abstractConnectionManager.getConnection ()) {
      _dll._GetRTSAttribute_SP (connection, clientname, testee, attname, attValue);
      assertTrue(attValue.get () != null);
      assertTrue(attValue.get ().equalsIgnoreCase ("6"));
      _logger.info (String.format ("Attvalue: %s", attValue.get ()));

    }
  }

  // declare @attValue varchar(200);
  // exec _GetRTSAttribute 'Oregon', 552981, 'LglFNm', @attvalue output;
  // select @attvalue;
  @Test
  public void test_GetRTSAttribute_SP2 () throws SQLException, ReturnStatusException {
    String clientname = "Oregon";
    Long testee = 552981L;
    String attname = "LglFNm";
    _Ref<String> attValue = new _Ref<String> ();

    try (SQLConnection connection = abstractConnectionManager.getConnection ()) {
      _dll._GetRTSAttribute_SP (connection, clientname, testee, attname, attValue);
      assertTrue(attValue.get () != null);
      assertTrue(attValue.get ().equalsIgnoreCase ("John"));
      _logger.info (String.format ("Attvalue: %s", attValue.get ()));

    }
  }

  // declare @entityKey bigint;
  // declare @entityId varchar(300);
  // declare @entityName varchar(300);
  // exec _GetRTSRelationship 'Oregon', 421021, 'ENRLINST-STUDENT',
  // @entityKey output, @entityId output, @entityName output;
  // select @entityKey, @entityId, @entityName;
  @Test
  public void test_GetRTSRelationship_SP () throws SQLException, ReturnStatusException {

    String clientname = "Oregon";
    Long testee = 421021L;
    String relationship = "ENRLINST-STUDENT";
    _Ref<Long> entityKey = new _Ref<Long> ();
    _Ref<String> entityId = new _Ref<String> ();
    _Ref<String> entityName = new _Ref<String> ();

    try (SQLConnection connection = abstractConnectionManager.getConnection ()) {
      _dll._GetRTSRelationship_SP (connection, clientname, testee, relationship, entityKey, entityId, entityName);
      assertTrue(entityKey.get () != null);
      assertTrue(entityId.get () != null);
      assertTrue(entityName.get () != null);
      assertTrue(entityKey.get () == 4);
      assertTrue(entityId.get ().equalsIgnoreCase ("20632063"));
      assertTrue(entityName.get ().equalsIgnoreCase ("Adel SD 21"));
      _logger.info (String.format ("entityKey: %d", entityKey.get ()));
      _logger.info (String.format ("entityId: %s", entityId.get ()));
      _logger.info (String.format ("entityName: %s", entityName.get ()));
    }
  }

  @Test
  public void test_GetRTSEntity_SP () throws SQLException, ReturnStatusException {
    String externalId = "999999932";
    String entityType = "STUDENT";
    String clientname = "Oregon";
    _Ref<Long> entityKeyRef = new _Ref<> ();
    try (SQLConnection connection = abstractConnectionManager.getConnection ()) {
      _dll._GetRTSEntity_SP (connection, clientname, externalId, entityType, entityKeyRef);
      _logger.info (String.format ("EntityKey: %s", entityKeyRef.get ()));
      assertTrue(entityKeyRef.get () == 538874);
    }
  }

}
