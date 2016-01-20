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
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import tds.itemrenderer.data.AccLookup;
import tds.itemrenderer.data.IITSDocument;
import tds.itemscoringengine.ItemScore;
import tds.itemscoringengine.ScoreRationale;
import tds.itemscoringengine.ScoringStatus;
import tds.student.configuration.ItemScoringSettings;
import tds.student.services.abstractions.IContentService;
import tds.student.services.abstractions.IItemScoringService;
import tds.student.services.abstractions.ITestScoringService;
import tds.student.services.abstractions.ITimerScoringService;
import tds.student.sql.abstractions.IScoringRepository;
import tds.student.sql.data.IItemResponseScorable;
import tds.student.tdslogger.TDSLogger;
import AIR.Common.TDSLogger.ITDSLogger;
import TDS.Shared.Configuration.ITDSSettingsSource;
import TDS.Shared.Configuration.TDSSettings;
import TDS.Shared.Exceptions.ReturnStatusException;

/**
 * @author temp_rreddy
 * 
 */
@Component
@Scope ("prototype")
public class TimerScoringService implements ITimerScoringService
{
  private final IScoringRepository  _scoringRepository;
  private final IItemScoringService _itemScoringService;
  private final ITestScoringService _testScoringService;
  private final IContentService     _contentService;
  @Autowired
  private ItemScoringSettings       _itemScoringSettings;
  
  @Autowired
  private ITDSSettingsSource               _tdsSettings;
  
  @Autowired
  private ITDSLogger          		_tdsLogger;

  private static final Logger       _logger = LoggerFactory.getLogger (TimerScoringService.class);

  public TimerScoringService (IScoringRepository scoringRepository, IItemScoringService itemScoringService, ITestScoringService testScoringService, IContentService contentService) {
    _scoringRepository = scoringRepository;
    _itemScoringService = itemScoringService;
    _testScoringService = testScoringService;
    _contentService = contentService;
  }

  // / <summary>
  // / Call this function to process any unscored items.
  // / </summary>
  // / <remarks>
  // / It is very imporant you make sure every possible exception is cought
  // here.
  // / If this fails and is uncought on a timer then an application exception
  // gets
  // / thrown and could shutdown the application.
  // / </remarks>
  public void processUnscoredItems () throws ReturnStatusException {
    try {
      int sessionType = _tdsSettings.getSessionType ();

      // get the last 100 items that haven't yet been scored which have been
      // sitting around for 15 minutes
      List<Map.Entry<UUID, IItemResponseScorable>> scorableResponses;

      // get the last items that have exceeded our max attempts
      List<Map.Entry<UUID, IItemResponseScorable>> expiredResponses;
      // TODO
      int pendingMins = _itemScoringSettings.getTimerPendingMins ();
      int maxAttempts = _itemScoringSettings.getTimerMaxAttempts ();

      try {
        // these items will attempt to be rescored through the item scoring
        // engine
        scorableResponses = _scoringRepository.getScoreItems (pendingMins, 0, maxAttempts, sessionType);

        // these items will be marked as scoring errors
        expiredResponses = _scoringRepository.getScoreItems (pendingMins, maxAttempts + 1, 9999, sessionType);
      } catch (Exception ex) {
    	  _tdsLogger.applicationError("ProcessUnscoredItems: Exception while trying to get items", "processExpiredItem", null, ex);
        return;
      }

      // process scorable items
      for (Map.Entry<UUID, IItemResponseScorable> scorableResponse : scorableResponses) {
        // ProcessScoreableItem (scorableResponse.Item1,
        // scorableResponse.Item2);
        processScoreableItem (scorableResponse.getKey (), scorableResponse.getValue ());
      }

      // process expired items
      for (Map.Entry<UUID, IItemResponseScorable> expiredResponse : expiredResponses) {
        // ProcessExpiredItem (expiredResponse.Item1, expiredResponse.Item2);
        processExpiredItem (expiredResponse.getKey (), expiredResponse.getValue ());
      }
    } catch (ReturnStatusException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
  }

  private void processExpiredItem (UUID oppKey, IItemResponseScorable responseScorable) throws ReturnStatusException {
    try {
      // create error score
      ItemScore score = new ItemScore (-1, -1, ScoringStatus.ScoringError, null, new ScoreRationale ()
      {
        {
          setMsg ("Max item scoring attempts met.");
        }
      }, null, null);
      // Save score to DB
      boolean scoreUpdated;
      try {
        scoreUpdated = _itemScoringService.updateItemScore (oppKey, responseScorable, score);
      } catch (Exception ex) {
        String errorMessage = String.format ("ProcesssExpiredItem: Exception while trying to update item score %s", responseScorable.getItemID ());
        _tdsLogger.applicationError(errorMessage, "processExpiredItem", null, ex);
        return;
      }
      // try and score the test now that we are not going to score this item
      if (scoreUpdated) {
        _testScoringService.scoreTest (oppKey, responseScorable.getTestKey ());
      }
    } catch (ReturnStatusException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
  }

  private void processScoreableItem (UUID oppKey, IItemResponseScorable responseScorable) throws ReturnStatusException {
    // submit score
    ItemScore score = null;
    try {
      // get its doc
      IITSDocument itsDoc = _contentService.getContent (responseScorable.getFilePath (), AccLookup.getNone ());
      // check for any score errors
      score = _itemScoringService.checkScoreability (responseScorable, itsDoc);
      // score item if no errors
      if (score == null) {
        score = _itemScoringService.scoreItem (oppKey, responseScorable, itsDoc);
      }
    } catch (Exception ex) {
      String errorMessage = String.format ("ProcessUnscoredItems: Exception while trying to score item %s", responseScorable.getItemID ());
      _tdsLogger.applicationError(errorMessage, "processScoreableItem", null, ex);
       return;
    }
    // if the scoring was synchronous we will get a proper score back, right now
    // we are not dealing with this scenario
    if (score != null && score.getScoreInfo ().getStatus () != ScoringStatus.WaitingForMachineScore) {
      boolean scoreUpdated;
      try {
        scoreUpdated = _itemScoringService.updateItemScore (oppKey, responseScorable, score);
      } catch (Exception ex) {
        String errorMessage = String.format ("ProcessUnscoredItems: Exception while trying to update item score %s", responseScorable.getItemID ());
        _tdsLogger.applicationError(errorMessage, "processScoreableItem", null, ex);
        return;
      }
      // check for returned error
      if (scoreUpdated) {
        // try and score the test now since this may have been the last item
        // holding up the test
        _testScoringService.scoreTest (oppKey, responseScorable.getTestKey ());
      } else {
        String errorMessage = String.format ("ProcessUnscoredItems: Return status error while trying to update item score %1$s", responseScorable.getItemID ());
        _tdsLogger.applicationError(errorMessage, "processScoreableItem", null, null);
      }
    }
  }
}
