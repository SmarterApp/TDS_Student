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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import tds.dll.api.ICommonDLL;
import tds.dll.api.IRtsDLL;
import tds.dll.api.IStudentDLL;
import tds.dll.mysql.RtsPackageDLL;
import tds.student.performance.dao.StudentDao;
import tds.student.performance.services.StudentLoginService;
import tds.student.services.abstractions.IStudentPackageService;
import tds.student.sql.abstractions.IConfigRepository;
import tds.student.sql.abstractions.ITesteeRepository;
import tds.student.sql.data.RTSAccommodation;
import tds.student.sql.data.RTSAttribute;
import tds.student.sql.data.TesteeAttributeMetadata;
import tds.student.sql.data.TesteeAttributes;
import AIR.Common.Configuration.AppSettings;
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
public class TesteeRepository extends AbstractDAO implements ITesteeRepository
{
  private static final Logger    _logger                = LoggerFactory.getLogger (SessionRepository.class);
  @Autowired
  private ICommonDLL             _commonDll             = null;
  @Autowired
  private IStudentDLL            _studentDll            = null;
  @Autowired
  private IRtsDLL                _rtsDll                = null;
  
  @Autowired
  private IConfigRepository      _configsRepository     = null;

  @Autowired
  private IStudentPackageService _studentPackageService = null;

  @Autowired
  StudentLoginService studentLoginService;
  
  public TesteeRepository () {
    super ();
  }

  public void setiCommonDLL (ICommonDLL _dll) {
    _commonDll = _dll;
  }

  public void setiStudentDLL (IStudentDLL _dll) {
    _studentDll = _dll;
  }

  public void setiRtsDLL (IRtsDLL _dll) {
    _rtsDll = _dll;
  }

  public TesteeAttributes login (String keyValuesString, String sessionID) throws ReturnStatusException {
    // email, April 7, 2014
    // Thank you, John.
    //
    // So the final version of the login process that I am going to implement is
    // this:
    //
    // 1. Determine whether this client uses “SSID” or “External SSID” for login
    // ID from configs DB
    // 2. Request student package from test registration using “client name” and
    // “ID” from login form.
    // 3. (??Optional: test for consistency of ID in the student package with
    // “client name” and “ID” that was requested)
    // 4. Check for existence of “client name” and “ID” in “r_studentkeyid”
    // a. If exists, retrieve “student key” from that table
    // b. If doesn’t exist, create a new record in “r_studentkeyid.” Use
    // the autoincrement to generate a new “student key”
    // 5. Create or update record in “r_studentpackage” table to hold student
    // package, by calling RtsPackageDll.createAndUpdateStudentIsCurrent().
    //
    // Does anybody see any problems with this?

    // check if this is a guest key (only if compiled in debug mode)
    // TODO
    if (_logger.isDebugEnabled ()) {
      if (IsGuestResume (keyValuesString)) {
        return createGuestLogin (keyValuesString);
      }
    }
    TesteeAttributes testeeAttributes = new TesteeAttributes ();
    try (SQLConnection connection = getSQLConnection ()) {

      // START: Parse information from login request
      String clientName = getTdsSettings ().getClientName ();
     
      Map<String, String> keyValues = new HashMap<> ();
      for (String line : StringUtils.split (keyValuesString, ';')) {
        String[] parts = StringUtils.split (line, ":", 2);
        if (parts.length < 2
            || StringUtils.isEmpty (parts[0])) {
          continue;
        }
        keyValues.put (parts[0], parts[1]);
      }

      // retrievalId may be either "real SSID" or "External SSID," depending on
      // client configuration
      final String retrievalId = keyValues.get ("ID");
      // END: Parse information from login request

      if (retrievalId != null && _rtsDll instanceof RtsPackageDLL) {
        // START: 1. Determine whether this client uses “SSID” or “External
        // SSID” for login ID from configs DB
        TesteeAttributeMetadata idMetadata = _configsRepository.getTesteeAttributeMetadata (
            new String[] { "ID" }, null, new String[] { "REQUIRE" })
            .get ("ID");
        if (idMetadata == null) {
          throw new ReturnStatusException ("Client configuration error: required testee attributes does not include ID.");
        }
        boolean isRealSSID = !"ExternalID".equalsIgnoreCase (idMetadata.getRtsName ());
        // END: 1. Determine whether this client uses “SSID” or “External SSID”
        // for login ID from configs DB

        // BEGIN: 2. Request student package from test registration using
        // “client name” and “ID” from login form.
        // Note: to maintain consistency with StudentDLL logic, we are _not_
        // checking for a special guest id.
        // In this way, a client that does not allow guest logins will be able
        // to have an actual student
        // whose ID is GUEST.
        final String studentPackage = _studentPackageService.getStudentPackage (retrievalId, isRealSSID);
        // END: 2. Request student package from test registration using “client
        // name” and “ID” from login form.

        // BEGIN: Get correspondence between testee ID and RTS entity key
        // NOTE: THIS IS BEING DONE TWICE DURING THE COURSE OF A REQUEST

        if ( studentPackage != null ) {
          // 3. (??Optional: test for consistency of ID in the student package
          // with “client name” and “ID” that was requested)
          // 4. Check for existence of “client name” and “ID” in
          // “client_studentkeyid”
          // 4a. If exists, retrieve “student key” from that table
          // 4b. If doesn’t exist, create a new record in “client_studentkeyid.”
          // Use the autoincrement to generate a new “student key”
          Long testeeKey =  _rtsDll.getOrCreateStudentKey (connection, retrievalId, clientName);
          // END: 4. Check for existence of “client name” and “ID” in
          // “client_studentkeyid”
  
          // BEGIN: 5. Create or update record in “r_studentpackage” table to hold
          // student package, by calling
          // RtsPackageDll.createAndUpdateStudentIsCurrent().
          _rtsDll.createAndUpdateStudentIsCurrent (connection, testeeKey, clientName, studentPackage);
          // END: 5. Create or update record in “r_studentpackage” table to hold student package, by calling RtsPackageDll.createAndUpdateStudentIsCurrent().
          
          // Create records in “r_studentpackagedetails” table by parsing the XML recieved from ART
          //TODO Shiva: talked to milan and the following line needs to remain commented out.
          //_rtsDll._InsertStudentPackageDetails (connection, testeeKey, clientName, studentPackage);
          
        }
      }

//
      //MultiDataResultSet resultSets = _studentDll.T_Login_SP (connection, clientName, keyValues, sessionID);
      MultiDataResultSet resultSets = studentLoginService.login(connection, clientName, keyValues, sessionID);

      Iterator<SingleDataResultSet> results = resultSets.getResultSets ();
      // first expected result set
      if (results.hasNext ()) {

        SingleDataResultSet firstResultSet = results.next ();
        ReturnStatusException.getInstanceIfAvailable (firstResultSet);
        ReturnStatus.parse (firstResultSet);

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
      throw new ReturnStatusException (e1);
    }
    return testeeAttributes;
  }

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

  // / <summary>
  // / Gets a students RTS (used for PT)
  // / </summary>
  // / <param name="testeeKey"></param>
  // / <returns></returns>
  public List<RTSAccommodation> getAccommodations (long testeeKey) throws ReturnStatusException {
    List<RTSAccommodation> rtsAccommodations = new ArrayList<RTSAccommodation> ();

    try (SQLConnection connection = getSQLConnection ()) {

      SingleDataResultSet firstResultSet = _rtsDll.T_GetRTSAccommodations_SP (connection, getTdsSettings ().getClientName (), testeeKey);
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

}
