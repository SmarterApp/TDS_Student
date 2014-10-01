/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.repositorysp;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import tds.student.sql.abstractions.ITesteeRepository;
import tds.student.sql.data.RTSAccommodation;
import tds.student.sql.data.RTSAttribute;
import tds.student.sql.data.TesteeAttributes;
import AIR.Common.Configuration.AppSettings;
import AIR.Common.DB.AbstractDAO;
import AIR.Common.DB.SQLConnection;
import AIR.Common.DB.SqlParametersMaps;
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
public class TesteeRepository extends AbstractDAO implements ITesteeRepository
{
  private static final Logger _logger = LoggerFactory.getLogger (TesteeRepository.class);

  public TesteeRepository () {
    super ();
  }

  // / <summary>
  // / Validates the session ID and key values for a student. If successful
  // returns student information.
  // / </summary>
  // / <example>
  // / exec T_Login 'Oregon', 'ID:9999999002;Firstname:JOHN', @sessID
  // / </example>
  public TesteeAttributes login (String keyValues, String sessionID) throws ReturnStatusException {
    // check if this is a guest key (only if compiled in debug mode)
    // TODO
    // #if (_logger.debug (message))
    if (_logger.isDebugEnabled ()) {
      if (IsGuestResume (keyValues)) {
        return createGuestLogin (keyValues);
      }
    }
    // #endif
    final String CMD_GET_LOGIN = "BEGIN; SET NOCOUNT ON; exec T_Login ${clientName}, ${keyValues}, ${sessionID}; end;";
    // get RTS key
    TesteeAttributes testeeAttributes = new TesteeAttributes ();

    try (SQLConnection connection = getSQLConnection ()) {

      SqlParametersMaps parametersQuery = new SqlParametersMaps ();
      
      parametersQuery.put ("clientname", getTdsSettings().getClientName ());
      parametersQuery.put ("keyValues", keyValues);
      parametersQuery.put ("sessionID", sessionID);

      MultiDataResultSet resultSets = executeStatement (connection, CMD_GET_LOGIN, parametersQuery, false);

      Iterator<SingleDataResultSet> results = resultSets.getResultSets ();
      // first expected result set
      if (results.hasNext ()) {

        SingleDataResultSet firstResultSet = results.next ();
        ReturnStatusException.getInstanceIfAvailable (firstResultSet);
        ReturnStatus returnStatus = ReturnStatus.parse (firstResultSet);

        DbResultRecord record = firstResultSet.getRecords ().next ();
        if (record != null) {
          testeeAttributes.setKey (record.<Long> get ("entityKey"));
        }

        if (results.hasNext ()) {
          SingleDataResultSet secondResultSet = results.next ();
          Iterator<DbResultRecord> records = secondResultSet.getRecords ();
          while (records.hasNext ()) {
            record = records.next ();
            RTSAttribute attribute = RTSAttribute.parse (record);
            testeeAttributes.add (attribute);
          }
        }
      }

      Collections.sort (testeeAttributes, new Comparator<RTSAttribute> ()
      {
        @Override
        public int compare (RTSAttribute o1, RTSAttribute o2) {
          return o1.getSortOrder () - o2.getSortOrder ();
        }
      });
    } catch (SQLException e1) {
      _logger.error (e1.getMessage ());
      e1.printStackTrace ();
      throw new ReturnStatusException (e1);
    }
    return testeeAttributes;
  }

  // / <summary>
  // / Gets a students RTS (used for PT)
  // / </summary>
  // / <param name="testeeKey"></param>
  // / <returns></returns>
  public List<RTSAccommodation> getAccommodations (long testeeKey) throws ReturnStatusException {
    final String CMD_GET_TEST_PROPERTIES = "BEGIN; SET NOCOUNT ON; exec T_GetRTSAccommodations ${clientName}, ${testee}; end;";
    List<RTSAccommodation> rtsAccommodations = new ArrayList<RTSAccommodation> ();

    try (SQLConnection connection = getSQLConnection ()) {
      SqlParametersMaps parametersQuery = new SqlParametersMaps ();
      
      parametersQuery.put ("clientname", getTdsSettings().getClientName ());
      parametersQuery.put ("testee", testeeKey);

      Iterator<SingleDataResultSet> results = executeStatement (connection, CMD_GET_TEST_PROPERTIES, parametersQuery, false).getResultSets ();

      SingleDataResultSet firstResultSet = results.next ();
      ReturnStatusException.getInstanceIfAvailable (firstResultSet);
      Iterator<DbResultRecord> records = firstResultSet.getRecords ();

      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        RTSAccommodation accommodation = new RTSAccommodation ();
        // TODO
        // accommodation.AccFamily = reader.IsDBNull("Subject") ? null :
        // reader.GetString("Subject");
        accommodation.setAccFamily (record.<String> get ("Subject"));
        accommodation.setAccCode (record.<String> get ("AccCode"));
        rtsAccommodations.add (accommodation);
      }
    } catch (SQLException e1) {
      _logger.error (e1.getMessage ());
      throw new ReturnStatusException (e1);
    }
    return rtsAccommodations;
  }

  // / <summary>
  // / Check if this is a guest key value.
  // / </summary>
  private boolean IsGuestResume (String keyValues) {
    final String guestKey = "FirstName:GUEST;ID:-";
    // TODO
    // bool allowResume = AppSettings.Get<bool>("debug.allowGuestResume");
    boolean allowResume = AppSettings.getBoolean ("debug.allowGuestResume").getValue ();
    // TODO
    // return (allowResume && keyValues.StartsWith(guestKey,
    // StringComparison.InvariantCultureIgnoreCase));
    return (allowResume && keyValues.startsWith (guestKey));
  }

  // / <summary>
  // / Create a guest login.
  // / </summary>
  private TesteeAttributes createGuestLogin (String keyValues) {
    // TODO
    // string id = keyValues.Split(new[] { "ID:" }, StringSplitOptions.None)[1];
    String[] splitarray = keyValues.split ("ID:");
    String id = splitarray[1];
    List<RTSAttribute> rtsAttributes = new ArrayList<RTSAttribute> ();
    rtsAttributes.add (new RTSAttribute ("FirstName", "GUEST", "First Name", 1, "REQUIRE"));
    rtsAttributes.add (new RTSAttribute ("ID", "GUEST " + id, "First Name", 2, "REQUIRE"));
    rtsAttributes.add (new RTSAttribute ("LastName", "GUEST", "Last Name", 3, "VERIFY"));
    rtsAttributes.add (new RTSAttribute ("Grade", null, "Grade", 4, "VERIFY"));
    rtsAttributes.add (new RTSAttribute ("DOB", null, "Date of Birth", 5, "VERIFY"));
    rtsAttributes.add (new RTSAttribute ("School", "GUEST School", "School", 7, "VERIFY"));
    rtsAttributes.add (new RTSAttribute ("State", "GUEST State", "State", 9, "VERIFY"));

    TesteeAttributes testeeAttributes = new TesteeAttributes ();
    testeeAttributes.setKey (Long.parseLong (id));

    for (RTSAttribute attribute : rtsAttributes) {
      testeeAttributes.add (attribute);
    }

    return testeeAttributes;
  }
}
