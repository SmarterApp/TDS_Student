/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import tds.student.services.abstractions.ILoginService;
import tds.student.services.data.LoginInfo;
import tds.student.services.data.LoginKeyValues;
import tds.student.sql.abstractions.ISessionRepository;
import tds.student.sql.abstractions.ITesteeRepository;
import tds.student.sql.data.AccommodationValue;
import tds.student.sql.data.Accommodations;
import tds.student.sql.data.RTSAccommodation;
import tds.student.sql.data.SessionStatus.SessionStatusType;
import tds.student.sql.data.TestSession;
import tds.student.sql.data.Testee;
import tds.student.sql.data.TesteeAttributes;
import AIR.Common.collections.IGrouping;
import TDS.Shared.Exceptions.ReturnStatusException;

/**
 * @author temp_rreddy
 * 
 */
@Component
@Scope ("prototype")
public class LoginService implements ILoginService
{
  @Autowired
  private ISessionRepository  _sessionRepository;

  @Autowired
  private ITesteeRepository   _testeeRepository;

  private static final Logger _logger = LoggerFactory.getLogger (LoginService.class);

  public LoginInfo login (String sessionID, LoginKeyValues keyValues) throws ReturnStatusException {
    LoginInfo loginInfo = new LoginInfo ();
    // validate session ID
    /*
     * if (string.IsNullOrEmpty(sessionID)) {
     * loginInfo.AddValidationError("RequiredSessionID"); return loginInfo; }
     */
    // get session
    TestSession sqlSession;
    try {
      sqlSession = _sessionRepository.getSession (sessionID);

      // check for closed session
      if (sqlSession == null || sqlSession.getStatusType () != SessionStatusType.Open) {
        loginInfo.addValidationMessage (sqlSession.getReturnStatus ().getReason ());
        return loginInfo;
      }
      loginInfo.setSession (sqlSession);

      // get delimited key values
      String keyValuesString = keyValues.toString ();
      
      try {
    	  // call DB to get testee info
    	  TesteeAttributes sqlTesteeResult = _testeeRepository.login (keyValuesString, sessionID);
    	  if (sqlTesteeResult.getReturnStatus () != null) {
    		  loginInfo.addValidationMessage (sqlTesteeResult.getReturnStatus ().getReason ());
    		  return loginInfo;
    	  }
    	  loginInfo.setTestee (new Testee (sqlTesteeResult));
      }
      catch (ReturnStatusException e) {
    	  // check for error
    	  loginInfo.addValidationMessage (e.getReturnStatus().getReason ());
    	  return loginInfo;
      }
      
      
      
    } catch (ReturnStatusException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
        
    return loginInfo;
  }

  // / <summary>
  // / For a collection of accommodations set all the matching RTS defaults
  // / </summary>
  @SuppressWarnings ("unchecked")
  public void setRTSAccommodations (long testee, final String accFamily, Accommodations accommodations) throws ReturnStatusException {
    // get all accommodations from RTS that are preselected for this student
    List<RTSAccommodation> allRTSAccCodes = null;
    try {
      allRTSAccCodes = _testeeRepository.getAccommodations (testee);
      // List<RTSAccommodation> subjectRTSAccCodes = allRTSAccCodes.FindAll(RTS
      // =>
      // RTS.AccFamily == null || RTS.AccFamily == accFamily);
      // TODO
      // get all the RTS accommodations that match this tests subject
      // (accommodation family)
      List<RTSAccommodation> subjectRTSAccCodes = (List<RTSAccommodation>) CollectionUtils.find (allRTSAccCodes, new Predicate ()
      {
        @Override
        public boolean evaluate (Object object) {
          RTSAccommodation rtsAttr = (RTSAccommodation) object;
          return rtsAttr.getAccFamily ().equalsIgnoreCase (accFamily);
        }
      });

      // get all the test accommodations for this subject's codes
      List<AccommodationValue> subjectAccValues = new ArrayList<AccommodationValue> ();

      for (RTSAccommodation subjectRTSAccCode : subjectRTSAccCodes) {
        AccommodationValue accValue = accommodations.getValue (subjectRTSAccCode.getAccCode ());
        if (accValue != null)
          subjectAccValues.add (accValue);
      }
      // TODO
      Transformer groupTransformer = new Transformer ()
      {

        @Override
        public Object transform (Object itsDocument) {
          return ((AccommodationValue) itsDocument).getParentType ();
        }
      };

      List<IGrouping<String, AccommodationValue>> subjectAccValuesGroupedByType = IGrouping.<String, AccommodationValue> createGroups (subjectAccValues, groupTransformer);

      Collections.sort (subjectAccValuesGroupedByType, new Comparator<IGrouping<String, AccommodationValue>> ()
      {
        @Override
        public int compare (IGrouping<String, AccommodationValue> o1, IGrouping<String, AccommodationValue> o2) {
          return o1.getKey ().compareTo (o2.getKey ());
        }
      });

      // group the test accommodations by type
      // var subjectAccValuesGroupedByType = subjectAccValues.GroupBy(value =>
      // value.ParentType);

      // select the new defaults for this type
      for (IGrouping<String, AccommodationValue> subjectAccValuesGroup : subjectAccValuesGroupedByType) {
        AccommodationValue defaultAccValue = subjectAccValuesGroup.get (0);

        // deselect the current default value
        // AccommodationValue defaultAccValue = subjectAccType.getDefault();

        if (defaultAccValue != null) {
          defaultAccValue.setIsDefault (false);
        }

        // select new default values
        for (AccommodationValue accValue : subjectAccValuesGroup) {
          accValue.setIsDefault (true);
        }
      }
    } catch (ReturnStatusException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
  }
}
