/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.services;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import tds.itemselection.api.IAIROnline;
import tds.itemselection.base.ItemGroup;
import tds.itemselection.base.TestItem;
import tds.student.services.abstractions.IAdaptiveService;
import tds.student.services.abstractions.IResponseService;
import tds.student.services.data.ItemResponse;
import tds.student.services.data.PageGroup;
import tds.student.sql.data.AdaptiveGroup;
import tds.student.sql.data.AdaptiveItem;
import tds.student.sql.data.OpportunityInstance;
import AIR.Common.DB.AbstractDAO;
import AIR.Common.DB.SQLConnection;
import AIR.Common.Helpers._Ref;
import TDS.Shared.Exceptions.ReturnStatusException;

/**
 * @author temp_rreddy
 * 
 */
@Component
@Scope ("prototype")
public class AdaptiveService extends AbstractDAO implements IAdaptiveService
{

  @Autowired
  private IAIROnline _aironline;
  
  @Autowired
  private IResponseService _responseService;
  
  private static final Logger    _logger = LoggerFactory.getLogger (AdaptiveService.class);

  public AdaptiveService () {
    super ();
  }

  /*
   * public AdaptiveService (String connectionString, IResponseService
   * responseService) { _connectionString = connectionString; _responseService =
   * responseService;
   * 
   * SqlConnectionStringBuilder sessionConnectionBuilder = new
   * SqlConnectionStringBuilder (connectionString); _database =
   * sessionConnectionBuilder.InitialCatalog; }
   */

  private AdaptiveGroup createItemGroupAdaptive (ItemGroup itemGroup, int page) {
    AdaptiveGroup adaptiveGroup = new AdaptiveGroup ();
    adaptiveGroup.setPage (page);
    adaptiveGroup.setBankKey (itemGroup.getBankkey ());
    adaptiveGroup.setGroupID (itemGroup.getGroupID ());
    adaptiveGroup.setSegmentPosition (itemGroup.getSegmentPosition ());
    adaptiveGroup.setSegmentID (itemGroup.getSegmentID ());
    adaptiveGroup.setNumItemsRequired (itemGroup.getNumRequired ());
    return adaptiveGroup;
  }

  private AdaptiveItem createItemAdaptive (AdaptiveGroup adaptiveGroup, TestItem testItem, int position) {
    AdaptiveItem adaptiveItem = new AdaptiveItem ();
    adaptiveItem.setPage (adaptiveGroup.getPage ());
    adaptiveItem.setPosition (position);
    adaptiveItem.setGroupID (testItem.getGroupID ());
    adaptiveItem.setItemID (testItem.getItemID ());
    adaptiveItem.setSegment (adaptiveGroup.getSegmentPosition ());
    adaptiveItem.setSegmentID (adaptiveGroup.getSegmentID ());
    adaptiveItem.setIsRequired (testItem.isRequired ());
    adaptiveItem.setIsFieldTest (testItem.isFieldTest ());
    return adaptiveItem;
  }

  public PageGroup createNextItemGroup (OpportunityInstance oppInstance, int lastPage, int lastPosition) throws ReturnStatusException {
    PageGroup pageGroup = null;
    try {
      // generate next item group
      ItemGroup itemGroup;
      try (SQLConnection connection = getSQLConnection ()) {

    	  // this is main command! error is referenced String
    	_Ref<String> errorRef = new _Ref<>();
        itemGroup = _aironline.getNextItemGroup (connection, oppInstance.getKey (), errorRef);
          
        if(errorRef.get() != null  && !errorRef.get().isEmpty())
        {
        	 _logger.error (errorRef.get());
        	 throw new ReturnStatusException (errorRef.get());
        }
        
      } catch (SQLException se) {
        _logger.error (se.getMessage (),se);
        throw new ReturnStatusException (se);
      }

      // create own adaptive group wrapper
      lastPage++;
      AdaptiveGroup adaptiveGroup = createItemGroupAdaptive (itemGroup, lastPage);
      for (TestItem testItem : itemGroup.getItems ()) {
        lastPosition++;

        AdaptiveItem adaptiveItem = createItemAdaptive (adaptiveGroup, testItem, lastPosition);
        adaptiveGroup.getItems ().add (adaptiveItem);
      }
      try {
        pageGroup = _responseService.insertItems (oppInstance, adaptiveGroup);
      } catch (Exception e) {
        _logger.error (e.getMessage (),e);
        throw new ReturnStatusException (e);
      }

      for (ItemResponse itemResponse : pageGroup) {
        itemResponse.setPrefetched (true);
      }
    } catch (ReturnStatusException e) {
      _logger.error (e.getMessage (),e);
      throw new ReturnStatusException (e);
    }
    return pageGroup;
  }
}
