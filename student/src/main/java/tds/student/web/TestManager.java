/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.web;

import org.apache.commons.collections.Predicate;

import tds.student.services.abstractions.IAdaptiveService;
import tds.student.services.abstractions.IResponseService;
import tds.student.services.data.ItemResponse;
import tds.student.services.data.NextItemGroupResult;
import tds.student.services.data.PageGroup;
import tds.student.services.data.PageList;
import tds.student.services.data.TestOpportunity;
import AIR.Common.Utilities.SpringApplicationContext;
import TDS.Shared.Exceptions.ReturnStatusException;

/**
 * @author temp_rreddy
 * 
 */
public class TestManager
{
  private final TestOpportunity _testOpp;

  private IResponseService      _responseService;

  private IAdaptiveService      _adaptiveService;

  public TestManager (TestOpportunity testOpp)
  {
    _testOpp = testOpp;
    _responseService = SpringApplicationContext.getBean (IResponseService.class);
    _adaptiveService = SpringApplicationContext.getBean (IAdaptiveService.class);
  }

  private PageList         _pageList     = null;

  public void LoadResponses (boolean validate) throws ReturnStatusException
  {
    _pageList = _responseService.getOpportunityItems (_testOpp.getOppInstance (), validate);
  }

  public PageList GetAllPages ()
  {
    return _pageList;
  }

  public PageList GetVisiblePages (final int startPage)
  {
    if (_pageList == null)
      return null;
    Predicate predicate = new Predicate ()
    {
      @Override
      public boolean evaluate (Object object) {
        ItemResponse ir = (ItemResponse) object;
        return ir.getPage () > startPage && ir.isVisible ();
      }
    };

    // CollectionUtils.select (this._pageList,predicate , outputList);
    return _pageList.filter (predicate);
  }

  public PageList GetVisiblePages ()
  {
    return GetVisiblePages (0);
  }

  public int getLastPage ()
  {
    if (_pageList == null)
      return -1;
    PageGroup lastPage = _pageList.getLastOrDefault ();
    return (lastPage != null) ? lastPage.getNumber () : 0;

  }

  public int getLastPosition ()
  {
    if (_pageList == null) {
      return -1;
    }
    ItemResponse lastResponse = null;
    if (_pageList != null && _pageList.getResponses () != null && _pageList.getResponses ().size ()  > 0) {
       lastResponse = _pageList.getResponses ().get (_pageList.getResponses ().size () - 1);
    }

    return (lastResponse != null) ? lastResponse.getPosition () : 0;
  }

  // / <summary>
  // / Has the # of questions that have been generated met the # of questions
  // for this test.
  // / </summary>
  // / <remarks>
  // / You should not generate anymore adaptive questions if this has been met.
  // / </remarks>
  public boolean IsTestLengthMet ()
  {
    return _isTestComplete;
  }

  private boolean _isTestComplete = false;

  // / <summary>
  // / Has the # of questions that have been generated met the # of questions
  // for this test.
  // / </summary>
  // / <remarks>
  // / You should not generate anymore adaptive questions if this has been met.
  // / </remarks>
  public boolean CheckIfTestComplete () throws ReturnStatusException
  {
    _isTestComplete = _responseService.isTestComplete (_testOpp.getOppInstance ().getKey ());
    return _isTestComplete;
  }

  // / <summary>
  // / If this is true then prefetch is available to be requested.
  // / </summary>
  // / <param name="itemsUnansweredMax">
  // / Determines the max # of prefetch that
  // / is allowed regardless if they are available.
  // / </param>
  public boolean CheckPrefetchAvailability (int prefetch)
  {
    if (_pageList == null)
      return false;

    // check if test is completed
    if (_isTestComplete)
      return false;

    // how many prefetched items do we need to have waiting
    int itemsNeeded = prefetch;

    // items that have no response and are required
    int itemsLeftRequired = 0;

    for (PageGroup group : _pageList)
    {
      itemsLeftRequired += group.getItemsLeftRequired ();
    }

    // if the # of items needed to be prefetched is greater than or equal
    // to the # of items left that are required, then perform prefetch
    return (itemsNeeded >= itemsLeftRequired);
  }

  // / <summary>
  // / This will call the adaptive algorithm to create a new response group.
  // When the response are
  // / generated they will be added to the internal response collection and
  // saved to the database.
  // / </summary>
  public NextItemGroupResult CreateNextItemGroup () throws Exception
  {
    if (_pageList == null)
    {
      throw new Exception ("Cannot generate next item group until responses are loaded.");
    }

    // if (IsTestLengthMet) return null;

    // get next item group from adaptive algorithm
    PageGroup pageGroup = _adaptiveService.createNextItemGroup (_testOpp.getOppInstance (), getLastPage (), getLastPosition ());

    // check if we got any items back from our request
    if (pageGroup == null || pageGroup.size () == 0)
    {
      throw new Exception ("A call to GetNextItemgroup was made but there were no items were returned.");
    }

    _pageList.add (pageGroup);

    // create generated group info to return
    return new NextItemGroupResult (pageGroup);
  }
}
