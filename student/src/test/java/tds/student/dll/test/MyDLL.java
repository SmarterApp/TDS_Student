/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.dll.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import TDS.Shared.Exceptions.ReturnStatusException;
import AIR.Common.DB.AbstractDLL;
import AIR.Common.DB.SQLConnection;
import AIR.Common.DB.SQL_TYPE_To_JAVA_TYPE;
import AIR.Common.DB.results.SingleDataResultSet;
import AIR.Common.Helpers.CaseInsensitiveMap;

public class MyDLL extends AbstractDLL
{
 SingleDataResultSet example1 (SQLConnection connection  ) throws ReturnStatusException {
   
   //create empty SingledataResultSet and describe which columns and their types 
   // it is going to have
   SingleDataResultSet rs = new SingleDataResultSet ();
   rs.addColumn ("status", SQL_TYPE_To_JAVA_TYPE.VARCHAR);
   rs.addColumn ("page", SQL_TYPE_To_JAVA_TYPE.INT);
   rs.addColumn ("reason", SQL_TYPE_To_JAVA_TYPE.VARCHAR);
   rs.addColumn ("rowdelim", SQL_TYPE_To_JAVA_TYPE.VARCHAR);
   rs.addColumn ("coldelim", SQL_TYPE_To_JAVA_TYPE.VARCHAR);
   rs.addColumn ("dateCompleted", SQL_TYPE_To_JAVA_TYPE.DATETIME);
   
   List<CaseInsensitiveMap<Object>> resultList = new ArrayList<CaseInsensitiveMap<Object>> ();
   //add individual records to resultList list
   Character rowdelim = ';';
   Character coldelim = ',';
   Date dateCompleted = null;
   CaseInsensitiveMap<Object> rcd = new CaseInsensitiveMap<Object> ();
   rcd.put ("status", "success");
   rcd.put ("page", 5);
   rcd.put ("reason", null);
   rcd.put ("rowdelim", rowdelim.toString ());
   rcd.put ("coldelim", coldelim.toString ());
   rcd.put ("dateCompleted", dateCompleted);
   resultList.add (rcd);
   
   // have data from resultList populate _rows List in SingelDataResultSet
   rs.addRecords (resultList);
   return rs;
 }
}
