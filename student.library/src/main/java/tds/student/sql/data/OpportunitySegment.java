/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import com.fasterxml.jackson.annotation.JsonProperty;

import TDS.Shared.Data.ReturnStatus;

import tds.student.sql.data.Data;

/**
 * @author temp_rreddy
 * 
 */
public class OpportunitySegment
{
  public enum SelectorType {
    Unknown(-1), Adaptive(0), Sequential(1), Segmented(2);

    private int _code;

    SelectorType (int code) {
      _code = code;
    }

    public int getCode () {
      return _code;
    }
  }

  private String       _key;
  private String       _id;
  private int          _position;
  private String       _formKey;
  private String       _formID;
  private SelectorType _algorithm;
  private int          _isPermeable;
  private String       _restorePermOn;
  private String       _ftItems;

  @JsonProperty ("Key")
  public String getKey () {
    return _key;
  }

  public void setKey (String _key) {
    this._key = _key;
  }

  @JsonProperty ("ID")
  public String getId () {
    return _id;
  }

  public void setId (String _id) {
    this._id = _id;
  }

  @JsonProperty ("Position")
  public int getPosition () {
    return _position;
  }

  public void setPosition (int _position) {
    this._position = _position;
  }

  @JsonProperty ("FormKey")
  public String getFormKey () {
    return _formKey;
  }

  public void setFormKey (String _formKey) {
    this._formKey = _formKey;
  }

  @JsonProperty ("FormID")
  public String getFormID () {
    return _formID;
  }

  public void setFormID (String _formID) {
    this._formID = _formID;
  }

  @JsonProperty ("Algorithm")
  public SelectorType getAlgorithm () {
    return _algorithm;
  }

  public void setAlgorithm (SelectorType _algorithm) {
    this._algorithm = _algorithm;
  }

  @JsonProperty ("IsPermeable")
  public int getIsPermeable () {
    return _isPermeable;
  }

  public void setIsPermeable (int _isPermeable) {
    this._isPermeable = _isPermeable;
  }

  @JsonProperty ("RestorePermOn")
  public String getRestorePermOn () {
    return _restorePermOn;
  }

  public void setRestorePermOn (String _restorePermOn) {
    this._restorePermOn = _restorePermOn;
  }

  @JsonProperty ("FTItems")
  public String getFtItems () {
    return _ftItems;
  }

  public void setFtItems (String _ftItems) {
    this._ftItems = _ftItems;
  }

  public String toString () {
    return _id;
  }

  // / <summary>
  // / A collection of opportunity segments
  // / </summary>
  public class OpportunitySegments extends ArrayList<OpportunitySegment>
  {
    ReturnStatus _returnStatus = null;
    
    /**
     * @return the _returnStatus
     */
    public ReturnStatus getReturnStatus () {
      return _returnStatus;
    }

    /**
     * @param _returnStatus the _returnStatus to set
     */
    public void setReturnStatus (ReturnStatus _returnStatus) {
      this._returnStatus = _returnStatus;
    }

    public List<TestSegment> mergeWithTestSegments (List<TestSegment> testSegments, boolean isProctorless, boolean isReviewMode) {
      List<TestSegment> testSegmentsList = new ArrayList<TestSegment>();
      for (TestSegment testSegment : testSegments) {
        // TODO
        OpportunitySegment oppSegment = (OpportunitySegment) CollectionUtils.find (this, new PositionPredicate (testSegment.getId ()));
        // OpportunitySegment oppSegment = this.Find(s => s.ID ==
        // testSegment.getId ());

        // figure out segment permeability
        int isPermeable = testSegment.getIsPermeable ();
        int updatePermeable = isPermeable;

        // these are local override rules (reviewed with Larry)
        if (oppSegment != null) {
          /*
           * if -1, use the defined value for the segment as returned by
           * IB_GetSegments if not -1, then the local value defines the
           * temporary segment permeability
           */
          if (oppSegment.getIsPermeable () != -1) {
            isPermeable = oppSegment.getIsPermeable ();

            /*
             * The default permeability is restored when the student leaves the
             * segment while testing. Assuming the segment is impermeable, this
             * allows the student one entry into the segment during the sitting.
             * When the student leaves the segment, is membrane is enforced by
             * the student app. The database will restore the default value of
             * the segment membrane when the test is paused.
             */
            if (oppSegment.getRestorePermOn () != "segment") {
              updatePermeable = oppSegment.getIsPermeable ();
            }
          }

          // NOTE: When student enters segment, set isPermeable =
          // updatePermeable
        }

        // figure out segment approval
        int entryApproval = testSegment.getEntryApproval ();
        int exitApproval = testSegment.getExitApproval ();

        // NOTE: If proctorless test then don't require entry/exit approval
        // (nobody to approve it)
        if (isProctorless) {
          entryApproval = 0;
          exitApproval = 0;
        }
        // BUG #22642: Entry and Exit approvals are not needed from Test level
        // review screen when approval = 2
        else if (isReviewMode) {
          if (testSegment.getEntryApproval () == 2)
            entryApproval = 0;
          if (testSegment.getExitApproval () == 2)
            exitApproval = 0;
        }
        // TODO
        testSegmentsList.add (new TestSegment (testSegment.getId (), testSegment.getPosition (), testSegment.getLabel (), isPermeable, entryApproval, exitApproval, testSegment.isItemReview ()));
      }
      return testSegmentsList;
    }
  }

  class PositionPredicate implements Predicate
  {
    private String _position = null;

    public PositionPredicate (String position) {
      this._position = position;
    }

    public boolean evaluate (Object object) {
      OpportunitySegment data = (OpportunitySegment) object;
      return data.getId ().equalsIgnoreCase (_position);
    }
  }

}
