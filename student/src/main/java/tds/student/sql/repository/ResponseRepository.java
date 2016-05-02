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
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import tds.dll.api.ICommonDLL;
import tds.dll.api.IStudentDLL;
import tds.student.performance.services.StudentInsertItemsService;
import tds.student.sql.abstractions.IResponseRepository;
import tds.student.sql.data.AdaptiveGroup;
import tds.student.sql.data.AdaptiveItem;
import tds.student.sql.data.IItemResponseUpdate;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.data.OpportunityItem;
import tds.student.sql.data.OpportunityItem.OpportunityItems;
import AIR.Common.Configuration.AppSettings;
import AIR.Common.DB.AbstractDAO;
import AIR.Common.DB.SQLConnection;
import AIR.Common.DB.results.DbResultRecord;
import AIR.Common.DB.results.MultiDataResultSet;
import AIR.Common.DB.results.SingleDataResultSet;
import AIR.Common.TDSLogger.ITDSLogger;
import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.ReturnStatusException;

/**
 * @author temp_rreddy
 * 
 */
@Component
@Scope ("prototype")
public class ResponseRepository extends AbstractDAO implements IResponseRepository
{
  private static final Logger _logger     = LoggerFactory.getLogger (SessionRepository.class);

  private static final Float MS_TO_SECS_DIVISOR = 1000F;

  @Autowired
  private ICommonDLL          _commonDll  = null;
  @Autowired
  private IStudentDLL         _studentDll = null;
  @Autowired
  private ITDSLogger          _tdsLogger;

  @Autowired
  private StudentInsertItemsService studentInsertItemsService;

  public ResponseRepository () {
    super ();
  }

  public void setiCommonDLL (ICommonDLL _dll) {
    _commonDll = _dll;
  }

  public void setiStudentDLL (IStudentDLL _dll) {
    _studentDll = _dll;
  }

  public OpportunityItems insertItems (OpportunityInstance oppInstance, AdaptiveGroup adaptiveGroup) throws ReturnStatusException {
    // create item keys delimited string
    OpportunityItems opportunityItems = new OpportunityItem ().new OpportunityItems ();

     String itemKeys = getItemKeys (adaptiveGroup.getItems ());
//    String itemKeys = null;
    try (SQLConnection connection = getSQLConnection ()) {

      // Original call to insert items.
      // MultiDataResultSet resultSets = _studentDll.T_InsertItems_SP (connection, oppInstance.getKey (), oppInstance.getSessionKey (), oppInstance.getBrowserKey (), adaptiveGroup.getSegmentPosition (),
      //    adaptiveGroup.getSegmentID (), adaptiveGroup.getPage (), adaptiveGroup.getGroupID (), itemKeys, '|', new Integer (adaptiveGroup.getNumItemsRequired ()), new Float (0), new Integer (0),
      //    false);

      // New insert items method to improve performance.  See comments in module for further details.
      MultiDataResultSet resultSets = studentInsertItemsService.insertItems (connection, oppInstance.getKey (), oppInstance.getSessionKey (), oppInstance.getBrowserKey (), adaptiveGroup.getSegmentPosition (),
              adaptiveGroup.getSegmentID (), adaptiveGroup.getPage (), adaptiveGroup.getGroupID (), itemKeys, '|', adaptiveGroup.getNumItemsRequired (), 0f);

      Iterator<SingleDataResultSet> results = resultSets.getResultSets ();
      // first expected result set

      SingleDataResultSet firstResultSet = results.next ();
      ReturnStatusException.getInstanceIfAvailable (firstResultSet);

      ReturnStatus status = ReturnStatus.parse (firstResultSet);
      opportunityItems.setReturnStatus (status);
      DbResultRecord  record = firstResultSet.getRecords ().next ();
      if (!record.hasColumn ("dateCreated"))
        return opportunityItems;
      
      String dateCreated = record.<String> get ("dateCreated");

      if(!results.hasNext ())
      {
    	  _tdsLogger.sqlWarn("Item positions were not returned.", "insertItems");
    	  return opportunityItems;
      }
      else  {
        SingleDataResultSet res = results.next();
        Iterator<DbResultRecord> records = res.getRecords ();
       
        while (records.hasNext ()) {
          record = records.next ();
          OpportunityItem oppItem = new OpportunityItem ();
          String itemID = record.<String> get ("bankitemkey");
          // get data from SP
          oppItem.setBankKey (record.<Long> get ("bankkey"));
          oppItem.setItemKey (record.<Long> get ("itemkey"));
          oppItem.setPage (record.<Integer> get ("page"));
          oppItem.setPosition (record.<Integer> get ("position"));
          oppItem.setFormat (record.<String> get ("format"));
          oppItem.setDateCreated (dateCreated);

          AdaptiveItem adaptiveItem = (AdaptiveItem) CollectionUtils.find (adaptiveGroup.getItems (), new MyPredicate (itemID) );
//          {
//            @Override
//            public boolean evaluate (Object object) {
//              if (((AdaptiveItem) object).getItemID ().equals (itemID))
//                return true;
//              return false;
//            }
//          });
          // find matching adaptive item
          // AdaptiveItem adaptiveItem = adaptiveGroup.getItems ().Find(ai =>
          // ai.ItemID == itemID);
          // check if item was found
          if (adaptiveItem == null) {
            _logger.error ("itemID:: "+itemID);
            _logger.error ("adaptiveGroup.getItems ():: "+adaptiveGroup.getItems ());
            String error = "T_InsertItems: The item key  %1$s was returned but was not found in [%2$s].";
            throw new ReturnStatusException (String.format (error, itemID, adaptiveGroup.getItems ()));
          }
          // get data from adaptive algorithm
          oppItem.setGroupID (adaptiveGroup.getGroupID ());
          oppItem.setSegment (adaptiveGroup.getSegmentPosition ());
          oppItem.setSegmentID (adaptiveGroup.getSegmentID ());
          oppItem.setIsRequired (adaptiveItem.isRequired ());
          //oppItem.setIsFieldTest (adaptiveItem.isFieldTest ());
          oppItem.setGroupItemsRequired (adaptiveGroup.getNumItemsRequired ());

          // manually set data
          oppItem.setIsVisible (true);
          oppItem.setIsSelected (false);
          oppItem.setIsValid (false);
          oppItem.setMarkForReview (false);
          oppItem.setSequence (0);
          oppItem.setStimulusFile (null);
          oppItem.setItemFile (null);

          // DEBUG: Check if items should all be marked as not required

          if (_logger.isDebugEnabled ()) {
            boolean itemsNeverRequired = AppSettings.getBoolean ("debug.itemsNeverRequired").getValue ();
            if (itemsNeverRequired) {
              oppItem.setIsRequired (false);
              oppItem.setGroupItemsRequired (0);
            }
          }
          opportunityItems.add (oppItem);
        }
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return opportunityItems;
  }
  
  private String getItemKeys(List<AdaptiveItem> itemList)  {
    String itemKeys = null;
    if(itemList!=null) {
      StringBuilder sb = new StringBuilder ();
      for(AdaptiveItem item: itemList ) {
         sb.append (item.getItemID ()).append ("|");
      }
      if(!sb.toString ().isEmpty ()) {
        itemKeys = sb.substring (0, sb.length ()-1);
      }
    }
    return itemKeys;
  }

  public ReturnStatus updateScoredResponse (OpportunityInstance oppInstance, IItemResponseUpdate responseUpdate, int score, String scoreStatus, String scoreRationale, long scoreLatency, Float itemDuration)
      throws ReturnStatusException {
    ReturnStatus returnStatus = null;

    try (SQLConnection connection = getSQLConnection ()) {
      SingleDataResultSet firstResultSet = _studentDll.T_UpdateScoredResponse_SP (connection, oppInstance.getKey (), oppInstance.getSessionKey (), oppInstance.getBrowserKey (),
          responseUpdate.getItemID (), responseUpdate.getPage (), responseUpdate.getPosition (), responseUpdate.getDateCreated (), responseUpdate.getSequence (), score, responseUpdate.getValue (),
          responseUpdate.getIsSelected (), responseUpdate.getIsValid (), (int) scoreLatency, scoreStatus, scoreRationale, itemDuration / MS_TO_SECS_DIVISOR);
      ReturnStatusException.getInstanceIfAvailable (firstResultSet);
      Iterator<DbResultRecord> records = firstResultSet.getRecords ();
      if (records.hasNext ()) {
        DbResultRecord record = records.next ();
        returnStatus = ReturnStatus.parse (record);
        if (record.hasColumn ("scoremark")) {
          responseUpdate.setScoreMark (record.<UUID> get ("scoremark"));
        }
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }

    return returnStatus;
  }

  public OpportunityItems getOpportunityItems (UUID oppKey) throws ReturnStatusException {
    OpportunityItems opportunityItems = null;
   
    try (SQLConnection connection = getSQLConnection ()) {
      SingleDataResultSet firstResultSet = _studentDll.T_GetOpportunityItems_SP (connection, oppKey);

      opportunityItems = readOpportunityItems (firstResultSet, false);
      
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return opportunityItems;
  }

  public OpportunityItems getOpportunityItemsWithValidation (OpportunityInstance oppInstance) throws ReturnStatusException {
    OpportunityItems opportunityItems = null;

    try (SQLConnection connection = getSQLConnection ()) {
      SingleDataResultSet firstResultSet = _studentDll.T_GetOpportunityItemsWithValidation_SP (connection, oppInstance.getKey (), oppInstance.getSessionKey (), oppInstance.getBrowserKey ());
      ReturnStatusException.getInstanceIfAvailable (firstResultSet);

      ReturnStatus status = ReturnStatus.parse (firstResultSet);
      if (status != null && status.getStatus() != null && !status.getStatus().isEmpty())
    	  throw new ReturnStatusException(status);      

      opportunityItems = readOpportunityItems (firstResultSet, false);
      //opportunityItems.setReturnStatus (status);
      
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return opportunityItems;
  }

  public OpportunityItems getItemGroup (OpportunityInstance oppInstance, int pageNumber, String groupID, String dateCreated, boolean validateAccess) throws ReturnStatusException {
    OpportunityItems opportunityItems = null;
    try (SQLConnection connection = getSQLConnection ()) {
      SingleDataResultSet firstResultSet = _studentDll.T_GetItemGroup_SP (connection, oppInstance.getKey (), pageNumber, groupID, dateCreated, oppInstance.getSessionKey (),
          oppInstance.getBrowserKey (), validateAccess);
      ReturnStatusException.getInstanceIfAvailable (firstResultSet);
      
      opportunityItems = readOpportunityItems (firstResultSet, true);
            
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return opportunityItems;
  }

  protected OpportunityItems readOpportunityItems (SingleDataResultSet resultSet, boolean parseContent) {
	OpportunityItems responseCollection = new OpportunityItem().new OpportunityItems ();

    Iterator<DbResultRecord> records = resultSet.getRecords ();
    while (records.hasNext ()) {
      DbResultRecord reader = records.next ();
      OpportunityItem response = new OpportunityItem ();

      response.setBankKey (reader.<Long> get ("ItemBank"));
      response.setItemKey (reader.<Long> get ("Item"));
      response.setPosition (reader.<Integer> get ("position"));
      response.setPage (reader.<Integer> get ("page"));
      response.setGroupID (reader.<String> get ("GroupID"));
      response.setSegment (reader.<Integer> get ("segment"));
      response.setSegmentID (reader.<String> get ("SegmentID"));

      response.setSequence (reader.<Integer> get ("ResponseSequence"));
      response.setDateCreated (reader.<String> get ("dateCreated"));
      response.setFormat (reader.<String> get ("Format").toUpperCase ());

      if(reader.get ("isVisible")!=null){
        response.setIsVisible (Boolean.parseBoolean (reader.get ("isVisible").toString ()));
      }
      
      //response.setScore (reader.<Integer> get ("Score"));
      response.setMarkForReview (reader.<Boolean> get ("Mark"));

      response.setGroupItemsRequired (reader.<Integer> get ("GroupItemsRequired"));
      if(reader.get ("IsRequired")!=null){
        response.setIsRequired (Boolean.parseBoolean (reader.get ("IsRequired").toString ()));
      }
      
      response.setIsSelected (reader.<Boolean> get ("IsSelected"));
      //response.setIsFieldTest (reader.<Boolean> get ("IsFieldTest"));
      response.setIsValid (reader.<Boolean> get ("IsValid"));

      // check if we are parsing the content info (for T_GetItemGroup)
      if (parseContent) {
        response.setValue ((reader.<String> get ("Response") == null) ? null : reader.<String> get ("Response"));

        // check if there is item path (this should always exist)
        if (reader.<String> get ("itemFile") != null)
          response.setItemFile (reader.<String> get ("itemFile"));

        // check if there is stimulus path (optional)
        if (reader.<String> get ("stimulusFile") != null)
          response.setStimulusFile (reader.<String> get ("stimulusFile"));

        // check if this item is printable
        Long printable = null;
        try { //TODO: jmambo determine cause of change in data type from Long to Integer
          printable =  reader.<Long> get ("IsPrintable");
        } catch (Exception ex) {
          printable = new Long(reader.<Integer> get ("IsPrintable"));
        }
        response.setIsPrintable (printable == 1);
      }

      // DEBUG: Check if items should all be marked as not required and always
      // visible
      // #if (DEBUG)
      if (_logger.isDebugEnabled ()) {
        boolean itemsNeverRequired = AppSettings.getBoolean ("debug.itemsNeverRequired").getValue ();
        if (itemsNeverRequired) {
          response.setIsRequired (false);
          response.setGroupItemsRequired (0);
        }
        boolean itemsAlwaysVisible = AppSettings.getBoolean ("debug.itemsAlwaysVisible").getValue ();
        if (itemsAlwaysVisible) {
          response.setIsVisible (true);
        }
      }
      // #endif

      responseCollection.add (response);
    }
    return responseCollection;
  }

  public List<OpportunityItem> getItemGroup (OpportunityInstance oppInstance, int pageNumber) throws ReturnStatusException {
    try {
      return getItemGroup (oppInstance, pageNumber, null, null, false);
    } catch (ReturnStatusException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
  }

  public ReturnStatus setItemMarkForReview (OpportunityInstance oppInstance, int position, boolean mark) throws ReturnStatusException {
    try (SQLConnection connection = getSQLConnection ()) {
      SingleDataResultSet firstResultSet = _studentDll.T_SetItemMark_SP (connection, oppInstance.getKey (), oppInstance.getSessionKey (), oppInstance.getBrowserKey (), position, mark);
      ReturnStatusException.getInstanceIfAvailable (firstResultSet);

    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return null;
  }

  public void recordComment (UUID sessionKey, long testeeKey, UUID oppKey, int position, String comment) throws ReturnStatusException {

    try (SQLConnection connection = getSQLConnection ()) {
      _studentDll.T_RecordComment_SP (connection, sessionKey, testeeKey, comment, null, oppKey, position);

    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }

  }

  public boolean isTestComplete (UUID oppKey) throws ReturnStatusException {
    boolean isComplete = false;

    try (SQLConnection connection = getSQLConnection ()) {
      SingleDataResultSet firstResultSet = _studentDll.T_IsTestComplete_SP (connection, oppKey);
      ReturnStatusException.getInstanceIfAvailable (firstResultSet);
      Iterator<DbResultRecord> records = firstResultSet.getRecords ();

      if (records.hasNext ()) {
        DbResultRecord record = records.next ();
        isComplete = record.<Boolean> get ("IsComplete");
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return isComplete;
  }

  public ReturnStatus removeResponse (OpportunityInstance oppInstance, int position, String itemID, String dateCreated) throws ReturnStatusException {

    ReturnStatus returnStatus = new ReturnStatus ("success");
    try (SQLConnection connection = getSQLConnection ()) {
      SingleDataResultSet firstResultSet = _studentDll.T_RemoveResponse_SP (connection, oppInstance.getKey (), itemID, position, dateCreated, oppInstance.getSessionKey (),
          oppInstance.getBrowserKey ());
      ReturnStatusException.getInstanceIfAvailable (firstResultSet);

    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    // SP returns nothing on SUCCESS.
    return returnStatus;
  }
  public  class MyPredicate implements Predicate {

    private String theItemId;

    public MyPredicate(String theItemId) {
        super();
        this.theItemId = theItemId;
    }

    public boolean evaluate(Object object) {
      if (((AdaptiveItem) object).getItemID ().equals (theItemId))
        return true;
      return false;
    }
  }
}
