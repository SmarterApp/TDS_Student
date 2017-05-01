/*******************************************************************************
 * Educational Online Test Delivery System Copyright (c) 2014 American
 * Institutes for Research
 * 
 * Distributed under the AIR Open Source License, Version 1.0 See accompanying
 * file AIR-License-1_0.txt or at http://www.smarterapp.org/documents/
 * American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/

package tds.student.web.handlers;

import AIR.Common.Json.JsonHelper;
import AIR.Common.Web.EncryptionHelper;
import AIR.Common.Web.WebValueCollectionCorrect;
import AIR.Common.xml.XmlReader;
import TDS.Shared.Exceptions.FailedReturnStatusException;
import TDS.Shared.Exceptions.ReturnStatusException;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import tds.blackbox.web.handlers.TDSHandler;
import tds.itemscoringengine.ItemScoreResponse;
import tds.student.services.abstractions.IItemScoringService;
import tds.student.sql.data.ItemResponseScorable;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

/**
 * Handles requests for the application home page.
 */
@Scope ("prototype")
@Controller
public class ItemScoringCallbackHandler extends TDSHandler
{
  private static final Logger _logger = LoggerFactory.getLogger (ItemScoringCallbackHandler.class);

  @Autowired
  @Qualifier("legacyItemScoringService")
  private IItemScoringService _itemScoringService;

  /**
   * Simply selects the home view to render by returning its name.
   * 
   * @throws ReturnStatusException
   * 
   * @throws IOException
   */
  @RequestMapping (value = "/ItemScoringCallback.axd", method = RequestMethod.POST)
  public void ProcessRequest (HttpServletRequest request, ServletResponse response) throws FailedReturnStatusException, ReturnStatusException {

    if (request.getContentLength () == 0)
      return;

    ItemScoreResponse scoreResponse = null;
    ItemResponseScorable responseScorable = null;

    try {
      // Parse the score Request
      InputStream inputstream = null;
      XmlReader xmlReader = null;
      try {
        inputstream = request.getInputStream ();

        // SB:Start Hack! In the incoming request from .NET sites there is some
        // extraneous character that shows up in hexpad as "3F".
        BufferedReader bfr = new BufferedReader (new InputStreamReader (inputstream));
        StringBuffer stringBuffer = new StringBuffer ();
        String line = null;
        while ((line = bfr.readLine ()) != null)
          stringBuffer.append (line);
        // I am going to strip anything before the first "<".
        String originalRequest = stringBuffer.toString ();
        scoreResponse = ItemScoreResponse.getInstanceFromXml (originalRequest);
      } catch (IOException | JAXBException e) {
        _logger.error ("Unreadable XML in request", e);
        e.printStackTrace (System.out);
        throw new FailedReturnStatusException ("400 Invalid request");
      }

      String encryptedToken = scoreResponse.getScore ().getContextToken ().toString ();
      String decryptedToken;

      WebValueCollectionCorrect tokenData = null;
      try {
        decryptedToken = EncryptionHelper.DecryptFromBase64 (encryptedToken);
        tokenData = WebValueCollectionCorrect.getInstanceFromString (decryptedToken, false);
      } catch (Exception ex) {
        throw ex;
      }

      // parse test data
      UUID oppKey = UUID.fromString (tokenData.get ("oppKey").toString ());
      String testKey = tokenData.get ("testKey").toString ();
      String testID = tokenData.get ("testID").toString ();
      String language = tokenData.get ("language").toString ();
      int position = Integer.parseInt (tokenData.get ("position").toString ());
      long itsBank = Long.parseLong (tokenData.get ("itsBank").toString ());
      long itsItem = Long.parseLong (tokenData.get ("itsItem").toString ());
      String segmentID = tokenData.get ("segmentID").toString ();
      int sequence = Integer.parseInt (tokenData.get ("sequence").toString ());
      UUID scoreMark = UUID.fromString (tokenData.get ("scoremark").toString ());

      // Read the score context
      responseScorable = new ItemResponseScorable (testKey, testID, language, position, sequence, itsBank, itsItem, segmentID, null, scoreMark, null);

      _itemScoringService.updateItemScore (oppKey, responseScorable, scoreResponse.getScore ());
    } catch (Exception exp) {
      _logger.error ("Error updating score.", exp);
      try {
        _logger.error ("Details : " + getMessage (responseScorable, scoreResponse));
      } catch (Exception e) {
        _logger.error("Error while printing log message.", e);
        //Do nothing else.
      }
    }
  }

  private String getMessage (ItemResponseScorable scorable, ItemScoreResponse score) throws JsonGenerationException, JsonMappingException, IOException {
    String scorableString = "N/A";
    if (scorable != null)
      scorableString = JsonHelper.serialize (scorable);

    String scoreString = "N/A";
    if (score != null)
      scoreString = JsonHelper.serialize (score);

    return String.format ("ItemResponseScorable : %s ; Score : %s", scorableString, scoreString);
  }
}
