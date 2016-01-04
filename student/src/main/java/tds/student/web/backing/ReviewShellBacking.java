/*******************************************************************************
 * Educational Online Test Delivery System Copyright (c) 2014 American
 * Institutes for Research
 * 
 * Distributed under the AIR Open Source License, Version 1.0 See accompanying
 * file AIR-License-1_0.txt or at http://www.smarterapp.org/documents/
 * American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.web.backing;

import java.io.IOException;
import java.util.Iterator;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import TDS.Shared.Exceptions.ReturnStatusException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

import AIR.Common.Json.JsonHelper;
import AIR.Common.Utilities.TDSStringUtils;
import AIR.Common.Web.taglib.ClientScript;
import tds.student.performance.services.ItemBankService;
import tds.student.web.backing.dummy.StudentPage;
import tds.student.services.abstractions.ITestScoringService;
import tds.student.services.data.ItemResponse;
import tds.student.services.data.PageGroup;
import tds.student.services.data.TestOpportunity;
import tds.student.web.StudentContext;
import tds.student.sql.abstractions.IItemBankRepository;
import tds.student.sql.data.TestConfig;
import tds.student.sql.data.TestProperties;
import tds.student.sql.data.TestSegment;
import tds.student.web.StudentContextException;
import tds.student.web.TestManager;
import tds.student.web.StudentSettings;
import tds.student.services.data.PageList;

public/* partial */class ReviewShellBacking extends StudentPage
{
  private static final Logger _logger = LoggerFactory.getLogger (ReviewShellBacking.class);

  ItemBankService             itemBankService;
  StudentSettings             _studentSettings;
  ITestScoringService         _scoringService;

  @Override
  public ClientScript getClientScript () {
    return super.getClientScript ();
  }

  @PostConstruct
  public void onInit () {
    itemBankService = getBean ("itemBankService", ItemBankService.class);
    _studentSettings = getBean ("studentSettings", StudentSettings.class);
    _scoringService = getBean ("testScoringService", ITestScoringService.class);

    TestOpportunity testOpp = StudentContext.getTestOpportunity ();
    if (testOpp == null)
      try {
        StudentContext.throwMissingException ();
      } catch (StudentContextException e) {
        _logger.error (e.getMessage ());
        return;
      }

    TestProperties testProps = null;
    try {
      testProps = itemBankService.getTestProperties (testOpp.getTestKey ());
    } catch (Exception e) {
      _logger.error (e.getMessage ());
    }

    // load responses
    TestManager testManager = new TestManager (testOpp);

    // load responses and if we aren't in read only mode then validate opp
    try {
      testManager.LoadResponses (!_studentSettings.isReadOnly ());
    } catch (Exception e) {
      _logger.error (e.getMessage ());
    }

    // get visible groups
    PageList pages = testManager.GetVisiblePages ();

    // remove all the pages that are part of a impermeable segment
    /*
     * pages.RemoveAll(delegate(PageGroup group) { for (TestSegment testSegment
     * : testProps.Segments) { if (testSegment.IsPermeable == 0 &&
     * testSegment.ID == group.SegmentID) return true; }
     * 
     * return false; });
     */

    if (pages != null) {
      Iterator<PageGroup> iter = pages.iterator ();
      while (iter.hasNext ()) {
        PageGroup group = iter.next ();
        for (TestSegment testSegment : testProps.getSegments ()) {
          if (testSegment.getIsPermeable () == 0 && StringUtils.equals (testSegment.getId (), group.getSegmentID ())) {
            iter.remove ();
          }
        }
      }
    }

    setReviewablePages (pages);

    // check if can complete test
    boolean canCompleteTest = true;

    TestConfig testConfig = StudentContext.getTestConfig ();

    if (testConfig.isValidateCompleteness ()) {
      try {
        canCompleteTest = _scoringService.canCompleteTest (testOpp.getOppInstance ().getKey (), testOpp.getTestKey ());
      } catch (ReturnStatusException e) {
        _logger.error (e.getMessage ());
      }
    }

    // build javascript
    StringBuilder javascript = new StringBuilder ();
    javascript.append (TDSStringUtils.format ("var canCompleteTest = {0};", Boolean.toString (canCompleteTest).toLowerCase ()));
    try {
      javascript.append (TDSStringUtils.format ("var tdsTestee = {0};", JsonHelper.serialize (StudentContext.getTestee ())));
    } catch (JsonGenerationException e) {
      _logger.error (e.getMessage ());
    } catch (JsonMappingException e) {
      _logger.error (e.getMessage ());
    } catch (IOException e) {
      _logger.error (e.getMessage ());
    }

    try {
      javascript.append (TDSStringUtils.format ("var tdsTestProps = {0};", JsonHelper.serialize (testProps)));
    } catch (JsonGenerationException e) {
      _logger.error (e.getMessage ());
    } catch (JsonMappingException e) {
      _logger.error (e.getMessage ());
    } catch (IOException e) {
      _logger.error (e.getMessage ());
    }

    // write javascript
    this.getClientScript ().addToJsCode (javascript.toString ());
  }

  // / <summary>
  // / Add all the pages we can review.
  // / </summary>
  // / <param name="pages"></param>
  private void setReviewablePagesOld (PageList pages) {
    StringBuilder jsonGroups = new StringBuilder ();
    jsonGroups.append ("var groups = [];");

    boolean anyGroupMarked = false;

    for (PageGroup page : pages) {
      boolean marked = false;

      // check for marked
      for (ItemResponse response : page) {
        if (response.isMarkForReview ()) {
          anyGroupMarked = true;
          marked = true;
        }
      }

      ItemResponse firstResponse = page.getFirst ();
      ItemResponse lastResponse = page.getLast ();

      // write group info
      jsonGroups.append ("groups.push({");
      jsonGroups.append (TDSStringUtils.format ("id: '{0}', ", page.getId ()));
      jsonGroups.append (TDSStringUtils.format ("page: {0}, ", page.getNumber ()));
      jsonGroups.append (TDSStringUtils.format ("firstPos: {0}, ", firstResponse.getPosition ()));
      jsonGroups.append (TDSStringUtils.format ("lastPos: {0}, ", lastResponse.getPosition ()));
      jsonGroups.append (TDSStringUtils.format ("marked: {0}", Boolean.toString (marked).toLowerCase ()));
      jsonGroups.append ("});");
    }

    // add javascript to page
    this.getClientScript ().addToJsCode (jsonGroups.toString ());
  }

  // / <summary>
  // / Add all the pages we can review.
  // / </summary>
  // / <param name="pages"></param>
  private void setReviewablePages (PageList pages) {
    StringBuilder jsonGroups = new StringBuilder ();
    jsonGroups.append ("var groups = [];");

    boolean anyGroupMarked = false;

    for (PageGroup page : pages) {
      boolean marked = false;

      // check for marked
      for (ItemResponse response : page) {
        if (response.isMarkForReview ()) {
          anyGroupMarked = true;
          marked = true;
        }
      }

      ItemResponse firstResponse = page.getFirst ();
      ItemResponse lastResponse = page.getLast ();

      // write group info
      jsonGroups.append ("groups.push({");
      jsonGroups.append (TDSStringUtils.format ("id: '{0}', ", page.getId ()));
      jsonGroups.append (TDSStringUtils.format ("page: {0}, ", page.getNumber ()));
      jsonGroups.append (TDSStringUtils.format ("firstPos: {0}, ", firstResponse.getPosition ()));
      jsonGroups.append (TDSStringUtils.format ("lastPos: {0}, ", lastResponse.getPosition ()));
      jsonGroups.append (TDSStringUtils.format ("marked: {0}, ", Boolean.toString (marked).toLowerCase ()));

      StringBuilder itemsStringBuilder = new StringBuilder ();
      for (int counter1 = 0; counter1 < page.size (); ++counter1) {
        ItemResponse item = page.get (counter1);
        itemsStringBuilder.append ("{");
        itemsStringBuilder.append (TDSStringUtils.format ("position: {0}, ", item.getPosition ()));
        itemsStringBuilder.append (TDSStringUtils.format ("marked: {0}, ", Boolean.toString (item.isMarkForReview ()).toLowerCase ()));
        itemsStringBuilder.append (TDSStringUtils.format ("answered: {0}", Boolean.toString (item.getAnswered ()).toLowerCase ()));
        itemsStringBuilder.append ("}");
        if (counter1 < page.size () - 1)
          itemsStringBuilder.append (", ");
      }

      jsonGroups.append (TDSStringUtils.format ("items:[{0}]", itemsStringBuilder.toString ()));

      jsonGroups.append ("});");
    }

    // add javascript to page
    this.getClientScript ().addToJsCode (jsonGroups.toString ());
  }
  // TODO Shajib: is page_load needed?
  /*
   * protected void Page_Load(object sender, EventArgs e) { }
   */
}
