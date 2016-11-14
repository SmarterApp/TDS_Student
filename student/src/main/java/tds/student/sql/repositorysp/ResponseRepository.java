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
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import tds.student.sql.abstractions.IResponseRepository;
import tds.student.sql.data.AdaptiveGroup;
import tds.student.sql.data.AdaptiveItem;
import tds.student.sql.data.IItemResponseUpdate;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.data.OpportunityItem;
import tds.student.sql.data.OpportunityItem.OpportunityItems;
import tds.student.sql.data.TestConfig;
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
public class ResponseRepository extends AbstractDAO implements IResponseRepository
{

  private static final Logger _logger = LoggerFactory.getLogger (ResponseRepository.class);

  public ResponseRepository () {
    super ();
  }

  private String getItemKeys (List<AdaptiveItem> items) {
    int insertCount = items.size (); // # of responses to insert
    String[] itemKeys = new String[insertCount];
    for (int i = 0; i < insertCount; i++) {
      itemKeys[i] = items.get (i).getItemID ();
    }
    // TODO
    return itemKeys.toString ();
    // return Join (itemKeys);
  }

  public OpportunityItems insertItems (OpportunityInstance oppInstance, AdaptiveGroup adaptiveGroup, boolean isMsb) throws ReturnStatusException {
    // create item keys delimited string
    OpportunityItems opportunityItems = new OpportunityItem ().new OpportunityItems ();

    // String itemKeys = getItemKeys (adaptiveGroup.getItems ());
    String itemKeys = null;
    final String CMD_GET_INSERT_ITEMS = "BEGIN; SET NOCOUNT ON; exec T_InsertItems ${oppkey}, ${session}, ${browserID}, ${segment}, ${segmentID}, ${page}, ${groupID}, ${itemkeys},  ${delimiter},  ${groupItemsRequired}; end;";

    try (SQLConnection connection = getSQLConnection ()) {
      SqlParametersMaps parametersQuery = new SqlParametersMaps ();
      parametersQuery.put ("oppkey", oppInstance.getKey ());
      parametersQuery.put ("session", oppInstance.getSessionKey ());
      parametersQuery.put ("browserID", oppInstance.getBrowserKey ());
      parametersQuery.put ("segment", adaptiveGroup.getSegmentPosition ());
      parametersQuery.put ("segmentID", adaptiveGroup.getSegmentID ());
      parametersQuery.put ("page", adaptiveGroup.getPage ());
      parametersQuery.put ("groupID", adaptiveGroup.getGroupID ());
      parametersQuery.put ("itemkeys", itemKeys);
      parametersQuery.put ("delimiter", "|");
      parametersQuery.put ("groupItemsRequired", adaptiveGroup.getNumItemsRequired ());

      Iterator<SingleDataResultSet> results = executeStatement (connection, CMD_GET_INSERT_ITEMS, parametersQuery, false).getResultSets ();

      SingleDataResultSet firstResultSet = results.next ();
      ReturnStatusException.getInstanceIfAvailable (firstResultSet);
      Iterator<DbResultRecord> records = firstResultSet.getRecords ();
      if (records.hasNext ()) {
        DbResultRecord record = records.next ();

        if (!record.hasColumn ("dateCreated"))
          return opportunityItems;
        // set datecreated for each item
        String dateCreated = record.<String> get ("dateCreated");
        while (records.hasNext ()) {
          OpportunityItem oppItem = new OpportunityItem ();
          // String itemID = record.<String> get ("bankitemkey");
          // get data from SP
          oppItem.setBankKey (record.<Integer> get ("bankkey"));
          oppItem.setItemKey (record.<Integer> get ("itemkey"));
          oppItem.setPage (record.<Integer> get ("page"));
          oppItem.setPosition (record.<Integer> get ("position"));
          oppItem.setFormat (record.<String> get ("format"));
          oppItem.setDateCreated (dateCreated);

          AdaptiveItem adaptiveItem = (AdaptiveItem) CollectionUtils.find (adaptiveGroup.getItems (), new Predicate ()
          {
            @Override
            public boolean evaluate (Object object) {
              if (((AdaptiveItem) object).getItemID () != null)
                return true;
              return false;
            }
          });
          // find matching adaptive item
          // AdaptiveItem adaptiveItem = adaptiveGroup.getItems ().Find(ai =>
          // ai.ItemID == itemID);
          // check if item was found
          if (adaptiveItem == null) {
            String error = "T_InsertItems: The item key  %1$d was returned but was not found in [%2$s].";
            throw new ReturnStatusException (String.format (error, oppItem.getItemKey (), itemKeys));
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
      if (results.hasNext ()) {
        SingleDataResultSet secondResultSet = results.next ();
        records = secondResultSet.getRecords ();
        while (records.hasNext ()) {
          DbResultRecord record = records.next ();
        }
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return opportunityItems;
  }

  // / <summary>
  // / Update the response for an item.
  // / </summary>
  // / <exception cref="SqlException">
  // / If the response sequence is out of order could return a SQL exception:
  // / Conversion failed when converting the varchar value 'Responses out of
  // sequence: Position=1; Stored sequence =0; New sequence=' to data type int.
  // / </exception>
  public ReturnStatus updateScoredResponse (OpportunityInstance oppInstance, IItemResponseUpdate responseUpdate, int score, String scoreStatus, String scoreRationale, long scoreLatency, Float itemDuration)
      throws ReturnStatusException {
    ReturnStatus returnStatus = null;
    final String CMD_GET_INSERT_ITEMS = "BEGIN; SET NOCOUNT ON; exec T_UpdateScoredResponse ${oppkey}, ${session}, ${browserID}, ${itemID}, ${page}, ${position}, ${dateCreated}, ${responseSequence}, ${score}, ${response},  ${isSelected},  ${isValid},  ${scoreLatency}, ${scorestatus}, ${scoreRationale}; end;";

    // UUID oppKey = UUID.fromString ("9F1DCD39-111A-4417-8464-00A0E1291E4D");
    // UUID sessionKey = UUID.fromString
    // ("38893E8D-A5D2-4BF8-906E-3C2CBFBACC30");
    // UUID browserKey = UUID.fromString
    // ("99CDB138-17B3-4DFA-B892-D4E0060FD477");
    // String itemId = "131-100550";
    // int page = 1;
    // int position = 1;
    // String dateCreated = null;
    // int responseSequence = 1;
    // int score = -1;
    // String response = "A";
    // boolean isSelected = true;
    // boolean isValid = true;
    // int scoreLatency = 0;
    // String scoreStatus = "WaitingForMachineScore";
    // String scoreRationale = null;

    try (SQLConnection connection = getSQLConnection ()) {
      SqlParametersMaps parametersQuery = new SqlParametersMaps ();
      parametersQuery.put ("oppkey", "9F1DCD39-111A-4417-8464-00A0E1291E4D");
      parametersQuery.put ("session", "38893E8D-A5D2-4BF8-906E-3C2CBFBACC30");
      parametersQuery.put ("browserID", "99CDB138-17B3-4DFA-B892-D4E0060FD477");
      parametersQuery.put ("itemID", "131-100550");
      parametersQuery.put ("page", 1);
      parametersQuery.put ("position", 1);
      parametersQuery.put ("dateCreated", null);
      parametersQuery.put ("responseSequence", 1);
      parametersQuery.put ("score", -1);
      parametersQuery.put ("response", "A");
      parametersQuery.put ("isSelected", true);
      parametersQuery.put ("isValid", true);

      parametersQuery.put ("scoreLatency", 0);
      parametersQuery.put ("scorestatus", "WaitingForMachineScore");
      parametersQuery.put ("scoreRationale", null);

      SingleDataResultSet results = executeStatement (connection, CMD_GET_INSERT_ITEMS, parametersQuery, false).getResultSets ().next ();

      ReturnStatusException.getInstanceIfAvailable (results);
      Iterator<DbResultRecord> records = results.getRecords ();

      if (records.hasNext ()) {
        DbResultRecord record = records.next ();
        returnStatus = ReturnStatus.parse (record);
        // NOTE: You will only get a score mark back if the score is NULL or
        // less than 0
        if (record.hasColumn ("scoremark")) {
          responseUpdate.setScoreMark (record.<UUID> get ("scoremark"));
        }
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }

    // try (SQLConnection connection = getSQLConnection ()) {
    // SqlParametersMaps parametersQuery = new SqlParametersMaps ();
    // parametersQuery.put ("oppkey", oppInstance.getKey ());
    // parametersQuery.put ("session", oppInstance.getSessionKey ());
    // parametersQuery.put ("browserID", oppInstance.getSessionBrowser ());
    // parametersQuery.put ("itemID", responseUpdate.getItemID ());
    // parametersQuery.put ("page", responseUpdate.getPage ());
    // parametersQuery.put ("position", responseUpdate.getPosition ());
    // parametersQuery.put ("dateCreated", responseUpdate.getDateCreated ());
    // parametersQuery.put ("responseSequence", responseUpdate.getSequence ());
    // parametersQuery.put ("score", score);
    // parametersQuery.put ("response", responseUpdate.getValue ());
    // parametersQuery.put ("isSelected", responseUpdate.isSelected ());
    // parametersQuery.put ("isValid", responseUpdate.isValid ());
    //
    // parametersQuery.put ("scoreLatency", scoreLatency);
    // parametersQuery.put ("scorestatus", scoreStatus);
    // parametersQuery.put ("scoreRationale", scoreRationale);
    //
    // SingleDataResultSet results = executeStatement (connection,
    // CMD_GET_INSERT_ITEMS, parametersQuery, false).getResultSets ().next ();
    //
    // ReturnStatusException.getInstanceIfAvailable (results);
    // Iterator<DbResultRecord> records = results.getRecords ();
    //
    // if (records.hasNext ()) {
    // DbResultRecord record = records.next ();
    // returnStatus = ReturnStatus.parse (record);
    // // NOTE: You will only get a score mark back if the score is NULL or
    // // less than 0
    // if (record.hasColumn ("scoremark")) {
    // responseUpdate.setScoreMark (record.<UUID> get ("scoremark"));
    // }
    // }
    // } catch (SQLException e) {
    // _logger.error (e.getMessage ());
    // throw new ReturnStatusException (e);
    // }
    return returnStatus;
  }

  // / <summary>
  // / This returns all items that have been administered, whether responded to
  // or not, scored or not.
  // / </summary>
  // / <returns>ItemBank, Item, position, page, GroupID, ResponseSequence,
  // IsRequired, dateCreated,
  // / Score, Mark, OpportunityRestart, Response, IsFieldTest, IsSelected,
  // Format, IsValid</returns>
  public OpportunityItems getOpportunityItems (UUID oppKey) throws ReturnStatusException {
    OpportunityItems opportunityItems = new OpportunityItem ().new OpportunityItems ();
    final String CMD_GET_OPP_ITEMS = "BEGIN; SET NOCOUNT ON; exec T_GetOpportunityItems ${oppkey}; end;";

    try (SQLConnection connection = getSQLConnection ()) {
      SqlParametersMaps parametersQuery = new SqlParametersMaps ();
      parametersQuery.put ("oppkey", oppKey);

      SingleDataResultSet firstResultSet = executeStatement (connection, CMD_GET_OPP_ITEMS, parametersQuery, false).getResultSets ().next ();

      opportunityItems = readOpportunityItems (firstResultSet, false);
      
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return opportunityItems;
  }

  // / <summary>
  // / This returns all items that have been administered, whether responded to
  // or not, scored or not.
  // / </summary>
  // / <returns>ItemBank, Item, position, page, GroupID, ResponseSequence,
  // IsRequired, dateCreated,
  // / Score, Mark, OpportunityRestart, Response, IsFieldTest, IsSelected,
  // Format, IsValid</returns>
  public OpportunityItems getOpportunityItemsWithValidation (OpportunityInstance oppInstance) throws ReturnStatusException {
    // OpportunityItems result = new OpportunityItem ().new OpportunityItems ();
    ReturnStatus returnStatus = null;
    OpportunityItems opportunityItems = null;
    final String CMD_GET_OPP_ITEMS = "BEGIN; SET NOCOUNT ON; exec T_GetOpportunityItemsWithValidation ${oppkey}, ${session}, ${browserID}; end;";

    try (SQLConnection connection = getSQLConnection ()) {
      SqlParametersMaps parametersQuery = new SqlParametersMaps ();
      parametersQuery.put ("oppkey", oppInstance.getKey ());
      parametersQuery.put ("session", oppInstance.getSessionKey ());
      parametersQuery.put ("browserID", oppInstance.getBrowserKey ());

      SingleDataResultSet firstResultSet = executeStatement (connection, CMD_GET_OPP_ITEMS, parametersQuery, false).getResultSets ().next ();
      ReturnStatusException.getInstanceIfAvailable (firstResultSet);

      ReturnStatus status = ReturnStatus.parse (firstResultSet);

      opportunityItems = readOpportunityItems (firstResultSet, false);
      
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return opportunityItems;
  }

  // / <summary>
  // / T_GetItemGroup returns item data comparable to T_GetOpportunityItems, but
  // only for a single
  // / item group (indicated by page number). GroupID is a validity check. Since
  // all available items
  // / are retrieved at resumption of opportunity by T_GetOpportunityItems, it
  // is expected that the
  // / caller will provide the group ID to verify the call.
  // / </summary>
  // / <remarks>
  // / TesteeApp Usage: When rendering an existing item to a resumed test.
  // / Note: This item should persist in the browser until the student pauses or
  // otherwise closes the session.
  // / </remarks>
  // / <returns>ItemBank, Item, position, page, Score, Mark, Response,
  // IsFieldTest, IsSelected, IsRequired,
  // / Format, GroupID, DateCreated, ResponseSequence, ResponseLength,
  // OpportunityRestart, IsValid</returns>
  public OpportunityItems getItemGroup (OpportunityInstance oppInstance, int pageNumber, String groupID, String dateCreated, boolean validateAccess) throws ReturnStatusException {
	OpportunityItems opportunityItems = null;
	final String CMD_GET_OPP_ITEMS = "BEGIN; SET NOCOUNT ON; exec T_GetItemGroup ${oppkey}, ${pageNumber}, ${groupID}, ${dateCreated}, ${sessionID}, ${browserID}, ${validateAccess}; end;";
    try (SQLConnection connection = getSQLConnection ()) {
      SqlParametersMaps parametersQuery = new SqlParametersMaps ();
      parametersQuery.put ("oppkey", oppInstance.getKey ());
      parametersQuery.put ("pageNumber", pageNumber);
      parametersQuery.put ("groupID", groupID);
      parametersQuery.put ("dateCreated", dateCreated);
      parametersQuery.put ("sessionID", oppInstance.getSessionKey ());
      parametersQuery.put ("browserID", oppInstance.getBrowserKey ());
      parametersQuery.put ("validateAccess", validateAccess);
      Iterator<SingleDataResultSet> results = executeStatement (connection, CMD_GET_OPP_ITEMS, parametersQuery, false).getResultSets ();

      SingleDataResultSet firstResultSet = results.next ();
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

      response.setIsVisible (reader.<Boolean> get ("isVisible"));
      //response.setScore (reader.<Integer> get ("Score"));
      response.setMarkForReview (reader.<Boolean> get ("Mark"));

      response.setGroupItemsRequired (reader.<Integer> get ("GroupItemsRequired"));
      response.setIsRequired (reader.<Boolean> get ("IsRequired"));
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
        try { //TODO: jmambo determine cause of change in  data type from Long to Integer
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
    ReturnStatus returnStatus = null;
    final String CMD_GET_SET_ITEM_MARK = "BEGIN; SET NOCOUNT ON; exec T_SetItemMark ${oppkey}, ${session},  ${browserID},  ${position},  ${mark}; end;";

    try (SQLConnection connection = getSQLConnection ()) {
      SqlParametersMaps parametersQuery = new SqlParametersMaps ();
      parametersQuery.put ("oppkey", oppInstance.getKey ());
      parametersQuery.put ("session", oppInstance.getSessionKey ());
      parametersQuery.put ("browserID", oppInstance.getBrowserKey ());
      parametersQuery.put ("position", position);
      parametersQuery.put ("mark", mark);
      SingleDataResultSet firstResultSet = executeStatement (connection, CMD_GET_SET_ITEM_MARK, parametersQuery, false).getResultSets ().next ();
      ReturnStatusException.getInstanceIfAvailable (firstResultSet);
      Iterator<DbResultRecord> records = firstResultSet.getRecords ();
      returnStatus = ReturnStatus.readAndParse (firstResultSet);

      // while (records.hasNext ()) {
      // DbResultRecord record = records.next ();
      // returnStatus = ReturnStatus.readAndParse (record);
      // }

    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return returnStatus;
  }

  // / <summary>
  // / Record a comment for the item.
  // / </summary>
  public void recordComment (UUID sessionKey, long testeeKey, UUID oppKey, int position, String comment) throws ReturnStatusException {
    final String CMD_GET_RECORD_COMMENT = "BEGIN; SET NOCOUNT ON; exec T_RecordComment ${sessionKey}, ${testee}, ${comment}, ${context}, ${oppKey}, ${itemposition}; end;";

    try (SQLConnection connection = getSQLConnection ()) {
      SqlParametersMaps parametersQuery = new SqlParametersMaps ();
      parametersQuery.put ("sessionKey", sessionKey);
      parametersQuery.put ("testee", testeeKey);
      parametersQuery.put ("comment", comment);
      parametersQuery.put ("context", null);
      parametersQuery.put ("oppKey", oppKey);
      parametersQuery.put ("itemposition", position);
      executeStatement (connection, CMD_GET_RECORD_COMMENT, parametersQuery, false);

    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }

  }

  public boolean isTestComplete (UUID oppKey) throws ReturnStatusException {
    boolean isComplete = false;
    final String CMD_GET_TEST_COMPLETE = "BEGIN; SET NOCOUNT ON; exec T_IsTestComplete ${oppkey}; end;";

    try (SQLConnection connection = getSQLConnection ()) {
      SqlParametersMaps parametersQuery = new SqlParametersMaps ();
      parametersQuery.put ("oppkey", oppKey);
      SingleDataResultSet results = executeStatement (connection, CMD_GET_TEST_COMPLETE, parametersQuery, false).getResultSets ().next ();

      ReturnStatusException.getInstanceIfAvailable (results);
      Iterator<DbResultRecord> records = results.getRecords ();

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

    final String CMD_GET_OPP_ITEMS = "BEGIN; SET NOCOUNT ON; exec T_RemoveResponse ${oppkey}, ${itemID}, ${position}, ${dateCreated}, ${session}, ${browser}; end;";
    ReturnStatus returnStatus = new ReturnStatus ("success");
    try (SQLConnection connection = getSQLConnection ()) {
      SqlParametersMaps parametersQuery = new SqlParametersMaps ();
      parametersQuery.put ("oppkey", oppInstance.getKey ());
      parametersQuery.put ("itemID", itemID);
      parametersQuery.put ("position", position);
      parametersQuery.put ("dateCreated", dateCreated);
      parametersQuery.put ("session", oppInstance.getSessionKey ());
      parametersQuery.put ("browser", oppInstance.getBrowserKey ());
      executeStatement (connection, CMD_GET_OPP_ITEMS, parametersQuery, false);
    } catch (SQLException e) {
      returnStatus = new ReturnStatus ("Failed");
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    // SP returns nothing on SUCCESS.
    return returnStatus;
  }

}
