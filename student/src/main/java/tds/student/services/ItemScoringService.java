/*******************************************************************************
 * Educational Online Test Delivery System Copyright (c) 2014 American
 * Institutes for Research
 * 
 * Distributed under the AIR Open Source License, Version 1.0 See accompanying
 * file AIR-License-1_0.txt or at http://www.smarterapp.org/documents/
 * American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import AIR.Common.TDSLogger.ITDSLogger;
import AIR.Common.Web.EncryptionHelper;
import AIR.Common.Web.WebValueCollection;
import AIR.Common.Web.WebValueCollectionCorrect;
import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.ReturnStatusException;
import tds.itemrenderer.data.AccLookup;
import tds.itemrenderer.data.IITSDocument;
import tds.itemrenderer.data.ITSMachineRubric;
import tds.itemrenderer.data.ITSMachineRubric.ITSMachineRubricType;
import tds.itemrenderer.processing.ITSDocumentHelper;
import tds.itemscoringengine.ItemScoreInfo;
import tds.itemscoringengine.IItemScorerManager;
import tds.itemscoringengine.ItemScore;
import tds.itemscoringengine.ResponseInfo;
import tds.itemscoringengine.RubricContentSource;
import tds.itemscoringengine.RubricContentType;
import tds.itemscoringengine.ScoreRationale;
import tds.itemscoringengine.ScorerInfo;
import tds.itemscoringengine.ScoringStatus;
import tds.itemscoringengine.WebProxyItemScorerCallback;
import tds.student.configuration.ItemScoringSettings;
import tds.student.services.abstractions.IContentService;
import tds.student.services.abstractions.IItemScoringService;
import tds.student.sql.abstractions.IConfigRepository;
import tds.student.sql.abstractions.IResponseRepository;
import tds.student.sql.abstractions.IScoringRepository;
import tds.student.sql.data.IItemResponseScorable;
import tds.student.sql.data.IItemResponseUpdate;
import tds.student.sql.data.ItemResponseUpdate;
import tds.student.sql.data.ItemResponseUpdateStatus;
import tds.student.sql.data.ItemScoringConfig;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.singletons.ClientSingleton;
import tds.student.tdslogger.TDSLogger;

@Component
@Scope ("prototype")
public class ItemScoringService implements IItemScoringService
{

  private static final Logger _logger = LoggerFactory.getLogger (ItemScoringService.class);

  @Autowired
  private IResponseRepository _responseRepository;
  @Autowired
  private IConfigRepository   _configRepository;
  @Autowired
  private IScoringRepository  _scoringRepository;
  @Autowired
  private IContentService     _contentService;
  @Autowired
  private ItemScoringSettings _itemScoringSettings;
  @Autowired
  private IItemScorerManager  _itemScorer;
  @Autowired
  private ITDSLogger          _tdsLogger;

  private String getItemID (IItemResponseScorable responseScorable) {
    return String.format ("I-%s-%s", responseScorable.getBankKey (), responseScorable.getItemKey ());
  }

  /**
   * Get the item scoring config.
   */
  private ItemScoringConfig getItemScoringConfig (String format, String testID) throws ReturnStatusException {
    Iterable<ItemScoringConfig> itemScoringConfigs = _configRepository.getItemScoringConfigs ();
    Iterator<ItemScoringConfig> configsIterator = itemScoringConfigs.iterator ();
    ItemScoringConfig selected = null;

    while (configsIterator.hasNext ()) {
      ItemScoringConfig c = configsIterator.next ();
      // match format and context
      if ((c.getItemType ().equalsIgnoreCase (format) || c.getItemType ().equals ("*")) && (c.getContext ().equalsIgnoreCase (testID) || c.getContext ().equals ("*"))) {
        // now find the highest priority config
        if (selected == null || c.getPriority () > selected.getPriority ())
          selected = c;
      }
    }
    return selected;
  }

  // function for logging item scorer errors
  private ItemScore createEmptyScore (ScoringStatus scoreStatus, final String message) {
    return new ItemScore (-1, -1, scoreStatus, null, new ScoreRationale ()
    {
      {
        setMsg (message);
      }
    }, null, null);
  }

  /**
   * Check a response to see if it is scoreable. If it is not scoreable then you
   * will get back a Score object with the reason why. Otherwise if you get back
   * NULL then there are no problems and you can proceed with scoring the
   * response.
   */
  @Override
  public ItemScore checkScoreability (IItemResponseScorable responseScorable, IITSDocument itsDoc) throws ReturnStatusException {
    // check if item scoring is disabled
    if (!_itemScoringSettings.getEnabled ()) {
      return createEmptyScore (ScoringStatus.NotScored, "Item scoring setting is disabled.");
    }

    if (responseScorable == null) {
      return createEmptyScore (ScoringStatus.NotScored, "Could not find scoreable response.");
    }

    if (responseScorable.getValue () == null) {
      return createEmptyScore (ScoringStatus.NotScored, "There was nothing to score.");
    }

    // check if doc exists
    if (itsDoc == null) {
      return createEmptyScore (ScoringStatus.ScoringError, "ITS document was not found.");
    }

    String itemFormat = itsDoc.getFormat ();

    // get the scorer information for this item type
    ScorerInfo scorerInfo = _itemScorer.GetScorerInfo (itemFormat);

    // if there is no scorer info then this item type is not registered to a
    // scorer and cannot be scored
    if (scorerInfo == null) {
      return createEmptyScore (ScoringStatus.NotScored, "TDS does not score " + itemFormat);
    }

    // check if we should parse the xml for the rubric
    if (scorerInfo.getRubricContentSource () != RubricContentSource.None) {
      ITSMachineRubric machineRubric = _contentService.parseMachineRubric (itsDoc, responseScorable.getLanguage (), scorerInfo.getRubricContentSource ());

      // make sure this item has a machine rubric
      if (machineRubric == null) {
        return createEmptyScore (ScoringStatus.ScoringError, "Machine rubric was not found.");
      }

      // check if rubric has any data
      if (!machineRubric.getIsValid ()) {
        return createEmptyScore (ScoringStatus.ScoringError, "Machine rubric was empty.");
      }
    }

    // check if item scoring config is enabled for this format
    ItemScoringConfig itemScoringConfig = getItemScoringConfig (itemFormat, responseScorable.getTestID ());

    // check if item scoring config exists (there should always be a default
    // configured)
    if (itemScoringConfig == null) {
      return createEmptyScore (ScoringStatus.ScoringError, "Item scoring config was not found.");
    }

    // check if the item format is disabled
    if (!itemScoringConfig.isEnabled ()) {
      return createEmptyScore (ScoringStatus.NotScored, "Item scoring config for this item format is disabled.");
    }

    return null;
  }

  private String getDimensionsXmlForSP (ItemScore score) throws ReturnStatusException {
    // TODO Shiva: Why are the rationales being set to null ?
    ItemScoreInfo scoreInfo = score.getScoreInfo ();
    scoreInfo.setRationale (null);

    if (scoreInfo.getSubScores () != null) {
      for (ItemScoreInfo subScore : scoreInfo.getSubScores ()) {
        subScore.setRationale (null);
      }
    }

    String xml;
    try {
      xml = scoreInfo.toXmlString ();
      // TODO Shiva: hack!!! I am not sure at this late stage how the AA will
      // react to the xml string. so i am going to take it out.
      xml = StringUtils.replace (xml, "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>", "");
    } catch (JAXBException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException ("Could not parse scoreinfo xml");
    }
    return xml;
  }

  @Override
  public boolean updateItemScore (UUID oppKey, IItemResponseScorable response, ItemScore score) throws ReturnStatusException {
    ScoreRationale scoreRationale = score.getScoreInfo ().getRationale (); // get
                                                                           // this
                                                                           // here
    // before it is
    // removed
    String scoreDimensions = getDimensionsXmlForSP (score);

    ReturnStatus updateStatus = _scoringRepository.updateItemScore (oppKey, response, score.getScoreInfo ().getPoints (), score.getScoreInfo ().getStatus ().toString (), scoreRationale.getMsg (),
        scoreDimensions);

    return (updateStatus.getStatus ().equals ("updated"));

  }

  /**
   * Update a response for an instance of a test opportunity.
   * 
   * @throws ReturnStatusException
   */
  protected ItemResponseUpdateStatus updateResponse (OpportunityInstance oppInstance, IItemResponseUpdate responseUpdated, ItemScore score) throws ReturnStatusException {
    StopWatch dbTimer = new StopWatch ();
    dbTimer.start ();

    ItemScoreInfo scoreInfoObj = score.getScoreInfo ();
    ScoreRationale scoreRationaleObj = score.getScoreInfo ().getRationale ();
    ReturnStatus updateStatus = _responseRepository.updateScoredResponse (oppInstance, responseUpdated, scoreInfoObj.getPoints (), scoreInfoObj.getStatus ().toString (), scoreRationaleObj.getMsg (),
        score.getScoreLatency ());

    dbTimer.stop ();

    if (!updateStatus.getStatus ().equals ("updated") && !updateStatus.getStatus ().equals ("warning")) {
      throw new ReturnStatusException (updateStatus);
    }

    return (new ItemResponseUpdateStatus (responseUpdated.getPosition (), updateStatus.getStatus (), updateStatus.getReason (), dbTimer.getTime ()));
  }

  @Override
  public List<ItemResponseUpdateStatus> updateResponses (OpportunityInstance oppInstance, List<ItemResponseUpdate> responsesUpdated) throws ReturnStatusException {
    List<ItemResponseUpdateStatus> responseResults = new ArrayList<ItemResponseUpdateStatus> ();

    for (ItemResponseUpdate responseUpdate : responsesUpdated) {
      ItemResponseUpdateStatus updateStatus;

      // get its doc
      IITSDocument itsDoc = _contentService.getContent (responseUpdate.getFilePath (), AccLookup.getNone ());

      // check if loaded document
      if (itsDoc == null) {
        throw new ReturnStatusException (String.format ("When updating item id '%s' could not load the file '%s'.", responseUpdate.getItemID (), responseUpdate.getFilePath ()));
      }

      // check for any score errors
      ItemScore score = checkScoreability (responseUpdate, itsDoc);

      // if there was a score returned from score errors check then there was a
      // problem
      if (score != null) {
        // save response with error
        updateStatus = updateResponse (oppInstance, responseUpdate, score);
      } else {
        // for asynchronous we need to save the score first indicating it
        // is machine scorable and then submit to the scoring web site
        if (isScoringAsynchronous (itsDoc)) {
          score = new ItemScore (-1, -1, ScoringStatus.WaitingForMachineScore, null, new ScoreRationale ()
          {
            {
              setMsg ("Waiting for machine score.");
            }
          }, new ArrayList<ItemScoreInfo> (), null);
          updateStatus = updateResponse (oppInstance, responseUpdate, score);

          // TODO: if score returned here ends up being
          // ScoringStatus.ScoringError should we save this?
          score = scoreResponse (oppInstance.getKey (), responseUpdate, itsDoc);
        }
        // for synchronous we need to score first and then save
        else {
          score = scoreResponse (oppInstance.getKey (), responseUpdate, itsDoc);
          updateStatus = updateResponse (oppInstance, responseUpdate, score);
        }
      }

      responseResults.add (updateStatus);
    }

    return responseResults;
  }

  /**
   * Score a response for an instance of a test opportunity.
   * 
   * @throws ReturnStatusException
   */
  private ItemScore scoreResponse (UUID oppKey, IItemResponseScorable responseScorable, IITSDocument itsDoc) throws ReturnStatusException {
    StopWatch stopwatch = new StopWatch ();
    stopwatch.start ();

    ItemScore score = scoreItem (oppKey, responseScorable, itsDoc);

    // if there is no score then create a not scored
    if (score == null) {
      score = new ItemScore (-1, -1, ScoringStatus.NotScored, null, null, null, null);
    }

    stopwatch.stop ();
    score.setScoreLatency (stopwatch.getTime ());

    return score;
  }

  /**
   * Get the server url for proxy server.
   * 
   * @throws ReturnStatusException
   */
  private URL getServerUri (String format, String testID) throws ReturnStatusException {
    String serverUrl = null;

    // TODO shiva: this does not make sense? should we not look for in the
    // itemscoring config table first for
    // a more specific url before looking in a generic url? but that is how .NEt
    // has been coded up.
    // i am instead going to code it up differently.
    // if there is no url from settings then try and get from configs
    // get server url from item scoring config SP
    ItemScoringConfig itemScoringConfig = getItemScoringConfig (format, testID);

    if (itemScoringConfig != null && itemScoringConfig.getServerUrl () != null) {
      // use config url
      serverUrl = itemScoringConfig.getServerUrl ();
    }

    // check if multiple server urls was provided
    if (!StringUtils.isEmpty (serverUrl) && serverUrl.indexOf ("|") > 0) {
      String[] servers = serverUrl.split ("|");
      int serverIndex = (new Random ()).nextInt (servers.length);
      serverUrl = servers[serverIndex];
    }

    // use the generic server.
    if (serverUrl == null)
      serverUrl = _itemScoringSettings.getServerUrl ();

    URL uri;
    try {
      uri = new URL (serverUrl);
    } catch (MalformedURLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException ("Malformed scoring server url.");
    }
    return uri;
  }

  private URL getCallbackUri () throws ReturnStatusException {
    String callbackUrl = _itemScoringSettings.getCallbackUrl ();
    URL uri;
    try {
      uri = new URL (callbackUrl);
    } catch (MalformedURLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException ("Malformed callback url.");
    }
    return uri;
  }

  // function for logging item scorer errors and use TDSLogger
  private void log (IItemResponseScorable responseScorable, String message, String methodName, Exception ex) {
    String error = String.format ("ITEM SCORER (%s): %s", responseScorable.getItemID (), message);
    _tdsLogger.applicationError (error, methodName, null, ex);

  };

  @Override
  public ItemScore scoreItem (UUID oppKey, IItemResponseScorable responseScorable, IITSDocument itsDoc) throws ReturnStatusException {
    final String itemID = getItemID (responseScorable);
    String itemFormat = itsDoc.getFormat ();

    // get the scorer information for this item type
    ScorerInfo scorerInfo = _itemScorer.GetScorerInfo (itemFormat);

    ITSMachineRubric machineRubric = new ITSMachineRubric (ITSMachineRubricType.Text, null);
    RubricContentType rubricContentType = RubricContentType.ContentString;

    if (scorerInfo.getRubricContentSource () != RubricContentSource.None) {
      // get items rubric
      machineRubric = _contentService.parseMachineRubric (itsDoc, responseScorable.getLanguage (), scorerInfo.getRubricContentSource ());
      if (machineRubric == null)
        return null;

      // create response info to pass to item scorer
      if (machineRubric.getType ().equals (ITSMachineRubricType.Uri))
        rubricContentType = RubricContentType.Uri;
      else
        rubricContentType = RubricContentType.ContentString;

      // if this is true then load the rubric manually for the scoring engine
      if (rubricContentType == RubricContentType.Uri && _itemScoringSettings.getAlwaysLoadRubric ()) {
        rubricContentType = RubricContentType.ContentString;

        // read text from stream
        try {
          InputStream stream = ITSDocumentHelper.getStream (machineRubric.getData ());
          BufferedReader streamReader = new BufferedReader (new InputStreamReader (stream));
          try {
            StringBuilder sb = new StringBuilder ();
            String line;

            line = streamReader.readLine ();

            while (line != null) {
              sb.append (line);
              sb.append (System.lineSeparator ());
              line = streamReader.readLine ();
            }
            machineRubric.setData (sb.toString ());
          } finally {
            try {
              streamReader.close ();
            } catch (Exception e) {
            }
          }
        } catch (IOException e) {
          _logger.error (e.getMessage (), e);
          throw new ReturnStatusException ("Failed to read rubric.");
        }
      }
    }

    // create rubric object
    Object rubricContent;
    if (rubricContentType == RubricContentType.Uri) {
      rubricContent = machineRubric.createUri ();
    } else {
      rubricContent = machineRubric.getData (); // xml
    }

    ResponseInfo responseInfo = new ResponseInfo (itemFormat, itemID, responseScorable.getValue (), rubricContent, rubricContentType, null, true);

    // perform sync item scoring
    if (!isScoringAsynchronous (itsDoc)) {
      try {
        return _itemScorer.ScoreItem (responseInfo, null);
      } catch (final Exception ex) {
        return new ItemScore (-1, -1, ScoringStatus.ScoringError, null, new ScoreRationale ()
        {
          {
            setMsg ("Exception scoring item " + itemID + ": " + ex);
          }
        }, null, null);
      }
    }

    // create callback token if there is a callback url
    // TODO: Add warning at app startup if ItemScoringCallbackUrl is missing
    if (!StringUtils.isEmpty (_itemScoringSettings.getCallbackUrl ())) {
      // Create the token
      WebValueCollectionCorrect tokenData = new WebValueCollectionCorrect ();
      tokenData.put ("oppKey", oppKey);
      tokenData.put ("testKey", responseScorable.getTestKey ());
      tokenData.put ("testID", responseScorable.getTestID ());
      tokenData.put ("language", responseScorable.getLanguage ());
      tokenData.put ("position", responseScorable.getPosition ());
      tokenData.put ("itsBank", responseScorable.getBankKey ());
      tokenData.put ("itsItem", responseScorable.getItemKey ());
      tokenData.put ("segmentID", responseScorable.getSegmentID ());
      tokenData.put ("sequence", responseScorable.getSequence ());
      tokenData.put ("scoremark", responseScorable.getScoreMark ());

      // encrypt token (do not url encode)
      String encryptedToken = EncryptionHelper.EncryptToBase64 (tokenData.toString (false));

      // save token
      responseInfo.setContextToken (encryptedToken);
    }

    ItemScore scoreItem = null;

    // perform scoring
    String message = null;
    try {
      WebProxyItemScorerCallback webProxyCallback = null;

      // check if there is a URL which will make this call asynchronous
      URL serverUri = getServerUri (itemFormat, responseScorable.getTestID ());
      URL callbackUri = getCallbackUri ();

      if (serverUri != null && callbackUri != null) {
        webProxyCallback = new WebProxyItemScorerCallback (serverUri.toString (), callbackUri.toString ());
      }

      // call web proxy scorer
      scoreItem = _itemScorer.ScoreItem (responseInfo, webProxyCallback);

      // validate results
      if (scoreItem == null) {
        log (responseScorable, "Web proxy returned NULL score.", "scoreItem", null);
      } else if (scoreItem.getScoreInfo ().getStatus () == ScoringStatus.ScoringError) {
        message = String.format ("Web proxy returned a scoring error status: '%s'.", (scoreItem.getScoreInfo ().getRationale () != null ? scoreItem.getScoreInfo ().getRationale () : ""));
        log (responseScorable, message, "scoreItem", null);
      } else if (webProxyCallback != null && scoreItem.getScoreInfo ().getStatus () != ScoringStatus.WaitingForMachineScore) {
        message = String.format ("Web proxy is in asynchronous mode and returned a score status of %s. It should return %s.", scoreItem.getScoreInfo ().getStatus ().toString (),
            ScoringStatus.WaitingForMachineScore.toString ());
        log (responseScorable, message, "scoreItem", null);
      } else if (webProxyCallback == null && scoreItem.getScoreInfo ().getStatus () == ScoringStatus.WaitingForMachineScore) {
        message = String.format ("Web proxy is in synchronous mode but returned incorrect status of %s.", scoreItem.getScoreInfo ().getStatus ().toString ());
        log (responseScorable, message, "scoreItem", null);
      }
    } catch (Exception ex) {
      message = String.format ("EXCEPTION = '%s'.", ex.getMessage ());
      log (responseScorable, message, "scoreItem", ex);
    }

    return scoreItem;
  }

  /**
   * Is the scoring for this response asynchronous?
   */
  private boolean isScoringAsynchronous (IITSDocument itsDoc) {
    if (itsDoc != null && !StringUtils.isEmpty (itsDoc.getFormat ())) {
      ScorerInfo scorerInfo = _itemScorer.GetScorerInfo (itsDoc.getFormat ());

      // check if scorer exists - not all item types are scored in TDS
      if (scorerInfo != null) {
        return scorerInfo.isSupportsAsyncMode ();
      }
    }

    return false;
  }

}
