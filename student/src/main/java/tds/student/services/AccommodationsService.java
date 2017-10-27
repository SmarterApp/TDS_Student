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
import java.util.Collection;
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

import tds.student.performance.services.ItemBankService;
import tds.student.services.abstractions.IAccommodationsService;
import tds.student.services.data.OpenTestAcc;
import tds.student.sql.abstractions.IItemBankRepository;
import tds.student.sql.abstractions.IOpportunityRepository;
import tds.student.sql.abstractions.ITesteeRepository;
import tds.student.sql.data.AccList;
import tds.student.sql.data.AccommodationType;
import tds.student.sql.data.AccommodationValue;
import tds.student.sql.data.Accommodations;
import tds.student.sql.data.Data;
import tds.student.sql.data.OpportunityAccommodation;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.data.RTSAccommodation;
import tds.student.sql.data.TestProperties;
import tds.student.sql.data.TestSegment;
import AIR.Common.collections.IGrouping;
import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.ReadOnlyException;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.springframework.stereotype.Service;

/**
 * @author temp_rreddy
 * 
 */

@Component
@Scope ("prototype")
@Service("legacyAccommodationsService")
public class AccommodationsService implements IAccommodationsService
{
  @Autowired
  private IItemBankRepository    _ibRepository;

  @Autowired
  private ITesteeRepository      _testeeRepository;

  @Autowired
  private IOpportunityRepository _oppRepository;

  @Autowired
  private ItemBankService itemBankService;

  private static final Logger    _logger = LoggerFactory.getLogger (AccommodationsService.class);

  // / <summary>
  // / Get the test and segments accommodations for a student.
  // / </summary>
  // / <remarks>
  // / This is only used in PT mode for the student to choose
  // / own settings when using a guest session.
  // / </remarks>
  public List<Accommodations> getTestee (String testKey, boolean isGuestSession, long testeeKey) throws ReturnStatusException {
    List<Accommodations> accommodationsList = new ArrayList<Accommodations> ();
    try {
      // FW Performance Changes - switched from _ibRepository to new ItemBankService which utilizes caching and other optimizations
      TestProperties testProps = itemBankService.getTestProperties (testKey);

      // load test/segment accommodations
      List<Accommodations> accSegmentsList = getTestSegments (testProps, isGuestSession);

      for (Accommodations accommodations : accSegmentsList) {
        // check if this is a real student in a guest session and the test
        // accommodations
        if (testeeKey > 0 && isGuestSession && accommodations.getPosition () == 0) {
          // preselect the students RTS accommodations
          setTesteeDefaults (testeeKey, testProps.getAccFamily (), accommodations);
        }
        accommodationsList.add (accommodations);
      }
    } catch (ReturnStatusException e1) {
      _logger.error (e1.getMessage ());
      throw new ReturnStatusException (e1);
    }
    return accommodationsList;
  }

  // / <summary>
  // / This function gets the approved accommodations for the opportunity.
  // / </summary>
  // / <remarks>
  // / This is used after the the proctor approves the test for the student to
  // review.
  // / </remarks>
  public List<Accommodations> getApprovedOrig (OpportunityInstance oppInstance, String testKey, boolean isGuestSession) throws ReturnStatusException {
  
    List<OpportunityAccommodation> oppAccs = null;
    List<Accommodations> segmentAccsList = null;
    // get all the test and segment accommodations
    TestProperties testProps = null;
    try {
      // FW Performance Changes - switched from _ibRepository to new ItemBankService which utilizes caching and other optimizations
      testProps = itemBankService.getTestProperties (testKey);

      segmentAccsList = getTestSegments (testProps, isGuestSession); // get the
                                                                     // approved
                                                                     // accommodation
                                                                     // types/codes
                                                                     // from the
                                                                     // DB

      oppAccs = _oppRepository.getOpportunityAccommodations (oppInstance, testKey);

      Transformer groupTransformer = new Transformer ()
      {
        @Override
        public Object transform (Object itsDocument) {
          return ((OpportunityAccommodation) itsDocument).getSegmentPosition ();
        }
      };
      List<IGrouping<String, OpportunityAccommodation>> oppAccsLookup = IGrouping.<String, OpportunityAccommodation> createGroups (oppAccs, groupTransformer);
      Collections.sort (oppAccsLookup, new Comparator<IGrouping<String, OpportunityAccommodation>> ()
      {
        @Override
        public int compare (IGrouping<String, OpportunityAccommodation> o1, IGrouping<String, OpportunityAccommodation> o2) {
          return o1.getKey ().compareTo (o2.getKey ());
        }
      });

      // take all the approved accommodations and make a dictionary lookup by
      // segment position
      // var oppAccsLookup = oppAccs.OrderBy(OA =>
      // OA.SegmentPosition).GroupBy(OA
      // => OA.SegmentPosition).ToDictionary(OA => OA.Key);
      List<Accommodations> accommodationsList = new ArrayList<Accommodations> ();
      for (Accommodations accommodations : segmentAccsList) {
        // check if there are any approved accommodations for this segment

        if (oppAccsLookup.contains (accommodations.getPosition ())) {
          // for this test segments accommodation lookup the approved
          // accommodations
          
          OpportunityAccommodation codes = oppAccsLookup.get (accommodations.getPosition ()).get (0);
          
          List<String> list = new ArrayList<String> ();
          list.add (codes.getAccCode ());
          // return only the approved subset

          // return accommodations.getSubset(codes);
          try {
            accommodationsList.add (accommodations.getSubset (list));
          } catch (ReadOnlyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace ();
          }
        } else {
          // return empty subset
          // return accommodations.getSubset(new List<String>());
          accommodationsList.add (accommodations.getSubset (new ArrayList<String> ()));
        }
      }
      return accommodationsList;
    } catch (ReadOnlyException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
  }

  public List<Accommodations> getApproved (OpportunityInstance oppInstance, String testKey, boolean isGuestSession) throws ReturnStatusException {
    List<OpportunityAccommodation> oppAccs = null;
    List<Accommodations> segmentAccsList = null;
    // get all the test and segment accommodations
    TestProperties testProps = null;
    try {
      // FW Performance Changes - switched from _ibRepository to new ItemBankService which utilizes caching and other optimizations
      testProps = itemBankService.getTestProperties (testKey);
      segmentAccsList = getTestSegments (testProps, isGuestSession); 
      oppAccs = _oppRepository.getOpportunityAccommodations (oppInstance, testKey);
      Transformer groupTransformer = new Transformer ()
      {
        @Override
        public Object transform (Object itsDocument) {
          return ((OpportunityAccommodation) itsDocument).getSegmentPosition ();
        }
      };
      List<IGrouping<Integer, OpportunityAccommodation>> oppAccsLookup = IGrouping.<Integer, OpportunityAccommodation> createGroups (oppAccs, groupTransformer);
      //Collections.sort (oppAccsLookup, new Comparator<IGrouping<String, OpportunityAccommodation>> ()
      //{
      //  @Override
      //  public int compare (IGrouping<String, OpportunityAccommodation> o1, IGrouping<String, OpportunityAccommodation> o2) {
      //    return o1.getKey ().compareTo (o2.getKey ());
      //  }
      //});

      // take all the approved accommodations and make a dictionary lookup by
      // segment position
      // var oppAccsLookup = oppAccs.OrderBy(OA =>
      // OA.SegmentPosition).GroupBy(OA
      // => OA.SegmentPosition).ToDictionary(OA => OA.Key);
      List<Accommodations> accommodationsList = new ArrayList<Accommodations> ();
      for (Accommodations accommodations : segmentAccsList) {
        
        // check if there are any approved accommodations for this segment
        IGrouping<Integer, OpportunityAccommodation> a = findOppAcctLookupEntry (oppAccsLookup, accommodations.getPosition());
        if (a  != null) {
          // for this test segments accommodation lookup the approved
          // accommodations
          
          List<String> listAcc = getAccCodes(a);
         
          try {
            accommodations.selectCodes (listAcc);
            accommodationsList.add (accommodations.getSubset (listAcc));
          } catch (ReadOnlyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace ();
          }
        } else {
          // return empty subset
          // return accommodations.getSubset(new List<String>());
          accommodationsList.add (accommodations.getSubset (new ArrayList<String> ()));
        }
      }
      return accommodationsList;
    } catch (ReadOnlyException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    } catch (Exception ex) {
      throw new ReturnStatusException (ex);
    }
  }
  
  private List<String> getAccCodes (IGrouping<Integer, OpportunityAccommodation> acc) {
    List<String> aLst = new ArrayList<String>();
    
    for (OpportunityAccommodation a : acc) {
      aLst.add (a.getAccCode ());
    }
    return aLst;
  }
  private IGrouping<Integer, OpportunityAccommodation> findOppAcctLookupEntry (List<IGrouping<Integer, OpportunityAccommodation>> oppAccsLookup, int position) {
    String tmp = Integer.toString (position);
     for (IGrouping<Integer, OpportunityAccommodation> a : oppAccsLookup) {
       Integer theKey = a.getKey ();
       if (theKey == position)
         return a;
     }
    return null;
  }
  // / <summary>
  // / Approve an opportunities accommodations.
  // / </summary>
  // / <remarks>
  // / This is used in proxy mode only. Normally the proctor app would approve
  // accommodations.
  // / </remarks>
  public void approve (OpportunityInstance oppInstance, List<String> segmentsData) throws ReturnStatusException {
    try {
      if (segmentsData == null)
        return;

      // create accommodtions to approve
      ArrayList<OpenTestAcc> openTestAccs = new ArrayList<OpenTestAcc> ();

      for (String segmentData : segmentsData) {
        String[] segmentPieces = segmentData.split ("#");

        OpenTestAcc openTestAcc = new OpenTestAcc ();
        openTestAcc.setSegmentPosition (Integer.parseInt (segmentPieces[0]));

        if (segmentPieces.length > 1) {
          String[] segmentCodes = segmentPieces[1].split (",");

          for (String segmentCode : segmentCodes) {
            openTestAcc.getCodes ().add (segmentCode.replace("&amp;", "&"));
          }
        }
        openTestAccs.add (openTestAcc);
      }

      approveListOfOpenTestAcc (oppInstance, openTestAccs);
    } catch (ReturnStatusException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
  }

  // / <summary>
  // / Takes a group of accommodations and sets the defaults to match a student
  // in RTS.
  // / </summary>
  private void setTesteeDefaults (long testee, final String accFamily, Accommodations accommodations) throws ReturnStatusException {
    // get all accommodations from RTS that are preselected for this student
    List<RTSAccommodation> allRTSAccCodes = null;
    try {
      allRTSAccCodes = _testeeRepository.getAccommodations (testee);
      // get all the RTS accommodations that match this tests subject
      Collection<RTSAccommodation> subjectRTSAccCodes = (List<RTSAccommodation>) CollectionUtils.select(allRTSAccCodes, new Predicate ()
      {
        @Override
        public boolean evaluate (Object object) {
          RTSAccommodation rtsAttr = (RTSAccommodation) object;
          return (rtsAttr.getAccFamily () == null || rtsAttr.getAccFamily ().equalsIgnoreCase (accFamily));
        }
      });

      // get all the test accommodations for this subject's codes
      Collection<AccommodationValue> subjectAccValues = new ArrayList<AccommodationValue> ();

      if (subjectRTSAccCodes != null)
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

      List<IGrouping<AccommodationType, AccommodationValue>> subjectAccValuesGroupedByType = IGrouping.<AccommodationType, AccommodationValue> createGroups (subjectAccValues, groupTransformer);
      Collections.sort (subjectAccValuesGroupedByType, new Comparator<IGrouping<AccommodationType, AccommodationValue>> ()
      {
        @Override
        public int compare (IGrouping<AccommodationType, AccommodationValue> o1, IGrouping<AccommodationType, AccommodationValue> o2) {
          if (o1.getKey () == o2.getKey ())
            return 0;
          else
            return 1;
          // return o1.getKey ().Comparable (o2.getKey ());
        }
      });

      // group the test accommodations by type
      // var subjectAccValuesGroupedByType = subjectAccValues.GroupBy(value =>
      // value.ParentType);

      // select the new defaults for this type
      for (IGrouping<AccommodationType, AccommodationValue> subjectAccValuesGroup : subjectAccValuesGroupedByType) {
        AccommodationType subjectAccType = subjectAccValuesGroup.getKey ();

        // deselect the current default value
        AccommodationValue defaultAccValue = subjectAccType.getDefault ();

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

  // / <summary>
  // / A helper method for loading all the test and segment accommodations for a
  // test.
  // / </summary>
  // / <param name="testProps">
  // / The loaded test properties object.
  // / </param>
  // / <param name="isGuestSession">
  // / Is this for a guest session (PT)? If it is we need to filter
  // / out acc's that are supposed to be disabled for guest session.
  // / </param>
  private List<Accommodations> getTestSegments (TestProperties testProps, boolean isGuestSession) throws ReturnStatusException {
    long startTime = System.currentTimeMillis ();
    List<Accommodations> accommodationsList = new ArrayList<Accommodations> ();
    // FW Performance - changed from _ibRepository to ItemBankService to utilize caching
    AccList accList = itemBankService.getTestAccommodations (testProps.getTestKey ());
    // if this is PT then remove all acc's that are disabled for guest sessions
    try {
      if (isGuestSession) {
        accList.getData().removeAll(CollectionUtils.select(accList.getData(), object -> ((Data) object).isDisableOnGuestSession()));
      }
      // first create the test accommodations
      Accommodations testAccs = null;
      testAccs = accList.createAccommodations (0, testProps.getTestID (), testProps.getDisplayName ());
      accommodationsList.add (testAccs);
      
      // then create the segments accommodations
      for (TestSegment testSegment : testProps.getSegments ()) {
        Accommodations segmentAccs = null;
        try {
          segmentAccs = accList.createAccommodations (testSegment.getPosition (), testSegment.getId (), testSegment.getLabel ());
        } catch (ReadOnlyException e) {
          e.printStackTrace ();
        }
        accommodationsList.add (segmentAccs);
      }
    } catch (ReadOnlyException e1) {
      _logger.error (e1.getMessage ());
      throw new ReturnStatusException (e1);
    }

    return accommodationsList;
  }

  // / <summary>
  // / Approve an opportunities accommodations.
  // / </summary>
  // / <remarks>
  // / This is used in proxy mode only. Normally the proctor app would approve
  // accommodations.
  // / </remarks>
  void approveListOfOpenTestAcc (OpportunityInstance oppInstance, List<OpenTestAcc> oppTestAccs) throws ReturnStatusException {
    // approve each segments accommodations
    for (Object oppTestAcc : oppTestAccs) {
      try {
        OpenTestAcc segmentAccs = (OpenTestAcc) oppTestAcc;
        int segmentPos = segmentAccs.getSegmentPosition ();
        String segmentCodes = segmentAccs.getCodesDelimited ();

        ReturnStatus returnStatus = null;
        returnStatus = _oppRepository.approveAccommodations (oppInstance, segmentPos, segmentCodes);
        // check for error
        if (returnStatus != null && returnStatus.getStatus ().equalsIgnoreCase ("failed")) {
          throw new ReturnStatusException (returnStatus);
        }

      } catch (ReturnStatusException e) {
        _logger.error (e.getMessage ());
        throw new ReturnStatusException (e);
      }
    }
  }
}
