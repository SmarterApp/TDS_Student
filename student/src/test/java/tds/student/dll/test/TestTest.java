/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.dll.test;

import java.sql.SQLException;
import java.util.Date;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import tds.dll.api.IStudentDLL;
import AIR.Common.DB.AbstractConnectionManager;
import AIR.Common.DB.SQLConnection;
import AIR.Common.DB.SqlParametersMaps;
import AIR.Common.DB.results.DbResultRecord;
import AIR.Common.DB.results.SingleDataResultSet;
import AIR.test.framework.AbstractTest;
import TDS.Shared.Exceptions.ReturnStatusException;

@Configuration
@ContextConfiguration("classpath:test-context.xml")
public class TestTest extends AbstractTest
{
  @Autowired
  @Qualifier("iStudentDLL")
  IStudentDLL _dll = null;

  @Autowired
  private AbstractConnectionManager abstractConnectionManager;

	@Autowired
	DLLHelper _myDllHelper;

  //@Test
  public void testExample1() throws SQLException, ReturnStatusException {
    MyDLL myDll = new MyDLL();
    try (SQLConnection connection = abstractConnectionManager.getConnection ()) {
      SingleDataResultSet rs = myDll.example1 (connection);
      rs.setFixNulls (true);
      DbResultRecord record = rs.getRecords ().next ();
      String status = record.<String>get("status");
      Integer page = record.<Integer> get ("page");
      String reason = record.<String> get ("reason");
      String rowdelim = record.<String> get("rowdelim");
      String coldelim = record.<String> get("coldelim");
      Date dateCompleted = record.<Date> get ("dateCompleted");
      System.out.println ("done");
    }
  }
  
  @Test
  public void testFloat ()throws Exception {
    
    try (SQLConnection connection = abstractConnectionManager.getConnection ()) {
      Float fl =1.2F;
      final String cmd = "select cast (${fl} as float) as fl";
      SqlParametersMaps parms = (new SqlParametersMaps ()).put ("fl", fl);

      SingleDataResultSet rs = _myDllHelper.executeStatement (connection, cmd, parms, false).getResultSets ().next ();
      DbResultRecord rec = rs.getRecords ().next ();
      Float myFl = rec.<Float> get ("fl");
      System.out.println (String.format ("myFl: %f", myFl));
          
    } catch (Exception ex) {
      System.out.println ("Exception: " + ex.getMessage ());
    }
    
  }
  
//use jdbc:mysql://ec2-54-237-18-215.compute-1.amazonaws.com:3306/test
 @Test
 public void testLocks() throws Exception {
   try (SQLConnection connection = abstractConnectionManager.getConnection ()) {
    
     Long lock = null;
     Long lock2 = null;
     Long unlock = null;
     Long unlock2 = null;
     String clientname = "Oregon";
     String resource = String.format("GUESTSESSION%s", clientname);     
     SqlParametersMaps parm1 = (new SqlParametersMaps ()).put ("resource", resource);
     
     String clientname2 = "Delaware";
     String resource2 = String.format("GUESTSESSION%s", clientname2);
     SqlParametersMaps parm2 = (new SqlParametersMaps ()).put ("resource", resource2);
     
     final String cmd1= "select GET_LOCK (${resource}, 0) as lk";
     
     SingleDataResultSet result = _myDllHelper.executeStatement (connection, cmd1, parm1, false).getResultSets ().next ();
     DbResultRecord record = (result.getCount () > 0 ? result.getRecords ().next () : null);
     if (record != null) {
       lock = record.<Long>get ("lk");
     }
   
     try (SQLConnection connection2 = abstractConnectionManager.getConnection ()) {
       
       result = _myDllHelper.executeStatement (connection, cmd1, parm2, false).getResultSets ().next ();
       record = (result.getCount () > 0 ? result.getRecords ().next () : null);
       if (record != null) {
         lock2 = record.<Long>get ("lk");
       }
      
      final String cmd3 = "select release_lock(${resource}) as unlk";
      // try release lock1 on connection1
      result = _myDllHelper.executeStatement (connection, cmd3, parm1, false).getResultSets ().next ();
      record = (result.getCount () > 0 ? result.getRecords ().next () : null);
      if (record != null) {
        unlock = record.<Long>get ("unlk");
      }
      
      //try releaselock2 on connection2
      result = _myDllHelper.executeStatement (connection, cmd3, parm2, false).getResultSets ().next ();
      record = (result.getCount () > 0 ? result.getRecords ().next () : null);
      if (record != null) {
        unlock2 = record.<Long>get ("unlk");
      }
      System.out.print ("done");
     }
     System.out.println ("lock: " + lock + " lock2: " + lock2 + " unlock: " + unlock + " unlock2: " + unlock2);
     
   } catch (Exception e) {
     System.out.println (String.format ("Exception: %s", e.getMessage ()));
   }
 }
}
