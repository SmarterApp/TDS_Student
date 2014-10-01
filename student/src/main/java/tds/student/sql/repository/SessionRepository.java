/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.repository;

import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import tds.dll.api.ICommonDLL;
import tds.dll.api.IStudentDLL;
import tds.student.sql.abstractions.ISessionRepository;
import tds.student.sql.data.SessionStatus;
import tds.student.sql.data.TestSession;
import AIR.Common.DB.AbstractDAO;
import AIR.Common.DB.SQLConnection;
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
public class SessionRepository extends AbstractDAO implements ISessionRepository
{
  private static final Logger _logger     = LoggerFactory.getLogger (SessionRepository.class);
  @Autowired
  private ICommonDLL          _commonDll  = null;
  @Autowired
  private IStudentDLL         _studentDll = null;

  public SessionRepository () {
    super ();
  }

  public void setiCommonDLL (ICommonDLL _dll) {
    _commonDll = _dll;
  }

  public void setiStudentDLL (IStudentDLL _dll) {
    _studentDll = _dll;
  }

  public TestSession getSession (String sessionID) throws ReturnStatusException {
    TestSession testSession = new TestSession ();

    try (SQLConnection connection = getSQLConnection ()) {
      // build parameters
      MultiDataResultSet resultSets = _studentDll.T_GetSession_SP (connection, getTdsSettings().getClientName (), sessionID);

      Iterator<SingleDataResultSet> results = resultSets.getResultSets ();
      // first expected result set
      if (results.hasNext ()) {

        SingleDataResultSet firstResultSet = results.next ();
//        ReturnStatusException.getInstanceIfAvailable (firstResultSet);
        ReturnStatus returnStatus = ReturnStatus.parse (firstResultSet);
        testSession.setReturnStatus (returnStatus);
        
        if (results.hasNext ()) {
          SingleDataResultSet secondResultSet = results.next ();
          Iterator<DbResultRecord> records = secondResultSet.getRecords ();
          if (records.hasNext ()) {
            DbResultRecord record = records.next ();
            
            testSession.setStatusType (SessionStatus.Parse (returnStatus.getStatus ()));
            testSession.setId (sessionID);
            testSession.setKey (record.<UUID> get ("SessionKey"));
            testSession.setName (record.<String> get ("Name"));
            testSession.setDateCreated (record.<Date> get ("DateCreated"));
            testSession.setDateBegin (record.<Date> get ("DateBegin"));
            testSession.setDateEnd (record.<Date> get ("DateEnd"));
          }
        }
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return testSession;
  }
}
