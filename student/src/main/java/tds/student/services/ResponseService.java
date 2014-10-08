/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.services;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import tds.student.services.abstractions.IResponseService;
import tds.student.services.data.PageGroup;
import tds.student.services.data.PageList;
import tds.student.sql.abstractions.IResponseRepository;
import tds.student.sql.data.AdaptiveGroup;
import tds.student.sql.data.AdaptiveItem;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.data.OpportunityItem;
import tds.student.sql.data.OpportunityItem.OpportunityItems;
import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.ReturnStatusException;

/**
 * @author temp_rreddy
 * 
 */
@Component
@Scope ("prototype")
public class ResponseService implements IResponseService
{

  protected final IResponseRepository _responseRepository;
  private static final Logger         _logger = LoggerFactory.getLogger (ResponseService.class);

  @Autowired
  public ResponseService (IResponseRepository responseRepository) {
    _responseRepository = responseRepository;
  }

  // insert items we got from adaptive algorithm to the session db
  public PageGroup insertItems (OpportunityInstance oppInstance, AdaptiveGroup adaptiveGroup) throws ReturnStatusException {
    OpportunityItems sqlOppItems = null;
    if (adaptiveGroup == null)
      return null;
    try {
      List<AdaptiveItem> items = adaptiveGroup.getItems ();
      int insertCount = items.size (); // # of responses to insert
      // nothing to do, return
      if (insertCount == 0)
        return null;
      sqlOppItems = _responseRepository.insertItems (oppInstance, adaptiveGroup);
      ReturnStatus returnedStatus = sqlOppItems.getReturnStatus ();
      // check if the return status is "inserted", otherwise it failed
      if (returnedStatus == null || !returnedStatus.getStatus ().equalsIgnoreCase ("inserted")) {
        throw new ReturnStatusException (returnedStatus);
      }
    } catch (ReturnStatusException e) {
      _logger.error (e.getMessage ());
      throw e;
    }
    return PageGroup.Create (sqlOppItems);
  }

  public PageList getOpportunityItems (OpportunityInstance oppInstance, boolean validate) throws ReturnStatusException {
    OpportunityItems opportunityItems;
    try {
      if (validate) {
        OpportunityItems sqlResult = _responseRepository.getOpportunityItemsWithValidation (oppInstance);

        if (sqlResult != null && sqlResult.getReturnStatus () != null) {
          throw new ReturnStatusException (sqlResult.getReturnStatus ());
        }

        opportunityItems = sqlResult;
      } else {
        opportunityItems = _responseRepository.getOpportunityItems (oppInstance.getKey ());
      }
    } catch (ReturnStatusException e) {
      _logger.error (e.getMessage ());
      throw e;
    }
    return PageList.Create (opportunityItems);
  }

  // / <summary>
  // / Get a specific group responses that the student is able to view.
  // / </summary>
  public PageGroup getItemGroup (OpportunityInstance oppInstance, int page, String groupID, String dateCreated, boolean validate) throws ReturnStatusException {
    // get testee responses for this groupID
	OpportunityItems sqlResult = _responseRepository.getItemGroup (oppInstance, page, groupID, dateCreated, validate);

  _logger.info("loadTest: ResponseService.getItemGroup sqlResult : " + sqlResult.size ());
    try {
      for (int i = 0; i < sqlResult.size (); i++) {
        OpportunityItem opportunityItem = sqlResult.get (i);
        // check for return status error
        if (opportunityItem.getReturnStatus () != null && "failed".equalsIgnoreCase (opportunityItem.getReturnStatus ().getStatus ())) {
          throw new ReturnStatusException (opportunityItem.getReturnStatus ());
        }
      }
    } catch (ReturnStatusException e) {
      _logger.error (e.getMessage ());
      throw e;
    }
    return PageGroup.Create (sqlResult);
  }

  public boolean isTestComplete (UUID oppKey) throws ReturnStatusException {
    try {
      return _responseRepository.isTestComplete (oppKey);
    } catch (ReturnStatusException e) {
      _logger.error (e.getMessage ());
      throw e;
    }
  }

  public void removeResponse (OpportunityInstance oppInstance, int position, String itemID, String dateCreated) throws ReturnStatusException {
    try {
      ReturnStatus removeReturnStatus = _responseRepository.removeResponse (oppInstance, position, itemID, dateCreated);
      // check if the remove was successful.
      // if there is a return status then the SP called failed.
      if (removeReturnStatus != null) {
        throw new ReturnStatusException (removeReturnStatus);
      }
    } catch (ReturnStatusException e) {
      _logger.error (e.getMessage ());
      throw e;
    }
  }
}
