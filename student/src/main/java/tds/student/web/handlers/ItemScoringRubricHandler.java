/*******************************************************************************
 * Educational Online Test Delivery System Copyright (c) 2014 American
 * Institutes for Research
 * 
 * Distributed under the AIR Open Source License, Version 1.0 See accompanying
 * file AIR-License-1_0.txt or at http://www.smarterapp.org/documents/
 * American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/

package tds.student.web.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import tds.itemrenderer.data.IITSDocument;
import tds.student.services.abstractions.IContentService;
import AIR.Common.Web.ContentType;
import AIR.Common.Web.EncryptionHelper;
import AIR.Common.Web.TDSReplyCode;
import AIR.Common.Web.WebHelper;
import AIR.Common.data.ResponseData;
import TDS.Shared.Exceptions.ReturnStatusException;

/**
 * Handles requests for the application home page.
 */
@Scope ("prototype")
@Controller
public class ItemScoringRubricHandler extends TDSHandler
{
  @Autowired
  private IContentService     _contentService;

  private static final Logger _logger = LoggerFactory.getLogger (ItemScoringRubricHandler.class);

  @ResponseStatus (value = org.springframework.http.HttpStatus.NOT_FOUND)
  @ExceptionHandler (FileNotFoundException.class)
  public @ResponseBody ResponseData<Object> handleFileNotFoundException (FileNotFoundException e, HttpServletResponse response) {
    _logger.error (e.getMessage (), e);
    return new ResponseData<Object> (TDSReplyCode.Error.getCode (), e.getMessage (), null);
  }

  /**
   * Simply selects the home view to render by returning its name.
   * 
   * @throws IOException
   */
  @RequestMapping (value = "/ItemScoringRubric.axd", method = RequestMethod.GET, produces = "application/xml")
  @ResponseBody
  public void processRequest (@RequestParam (value = "itemid", required = true) String itemId, @RequestParam (value = "itembank", required = false) String bankId,
      @RequestParam (value = "language", required = false) String language, HttpServletRequest request, HttpServletResponse response) throws IOException, FileNotFoundException, ReturnStatusException {
    final String GENERIC_MESSAGE = "itemid not specified. one of these is a must: <itemid, bankid> or encoded itemid or itemid in the format I-<bankid>-<itsid>.";
    if (StringUtils.isEmpty (itemId))
      throw new IOException (GENERIC_MESSAGE);
    // Shiva: hack! I looked into .NET ItemScoringRubricHandler. There they have
    // assumed that the only parameters are itemid and language. in that case
    // the itemid is an encrypted file path.
    // on the otherhand i am aware that when we deploy essay scorer project we
    // have two parameters: itemid and bankid. the
    // itemid in that case is the ITS ID. To get around this problem I am going
    // to try three things:
    // 1) try decoding itemid. if decodable and there is a file by that name
    // then we assume it was a file.
    // 2) if itemid is I-<bankid>-<itemid> then we will try to use a regex to
    // get the bankid and itemid.
    // 3) else we will try to find the item xml file by bank id and item id.
    IITSDocument itsDocument = null;
    try {
      // TODO Shiva: should we be doing decryption instead? .NET is usign
      // decode.
      String filePath = EncryptionHelper.DecodeFromBase64 (itemId);
      File f = new File (filePath);
      if (!f.isFile ()) {
        String message = String.format ("File %s does not exist.", filePath);
        _logger.debug (message);
      } else {
        itsDocument = _contentService.getContent (filePath, null);
      }
    } catch (Exception exp) {
      _logger.debug (String.format ("Error decoding item %s. Message: %s. Next step is to attempt to parse into I-<bankid>-<itemid>.", itemId, exp.getMessage ()));
    }

    if (itsDocument == null) {
      if (StringUtils.isEmpty (bankId)) {
        // From above Step 2:
        Pair<String, String> tuple = getBankIdItemIdTuple (itemId);
        if (tuple != null) {
          bankId = tuple.getLeft ();
          itemId = tuple.getRight ();
        }
      }

      if (StringUtils.isEmpty (bankId))
        throw new IOException (GENERIC_MESSAGE);

      // so now we realize that we have a <itemid, bankid> combo. find the item
      // file. the rubric file will be in that.
      // for the timebeing lets hard code.
      itsDocument = _contentService.getItemContent (Long.parseLong (bankId), Long.parseLong (itemId), null);
    }

    if (itsDocument == null || itsDocument.getMachineRubric () == null || StringUtils.isEmpty (itsDocument.getMachineRubric ().getData ()))
      throw new FileNotFoundException (String.format ("No machine rubric specified in <%s, %s>.", bankId, itemId));

    // stream this file.
    File machineRubricFile = null;
    try {
      machineRubricFile = new File (new URI (itsDocument.getMachineRubric ().getData ()));
      if (!machineRubricFile.isFile ()) {
        String message = String.format ("File %s does not exist.", machineRubricFile.getAbsolutePath ());
        _logger.debug (message);
        throw new FileNotFoundException (message);
      }
    } catch (URISyntaxException exp) {
      throw new IOException (exp.getMessage ());
    }

    // stream it.
    // TODO Shiva: hack! there is some character in i-200-54115 at the begining.
    // i cannot reproduce this on my laptop
    // and i do not have time to investigate right now. so i wills stop anything
    // before the first "<" on the first line.
    WebHelper.setContentType (ContentType.Xml);

    int lineCounter = 0;
    try (BufferedReader bfr = new BufferedReader (new FileReader (machineRubricFile))) {
      String line = null;
      while ((line = bfr.readLine ()) != null) {
        if (lineCounter == 0) {
          int indexOf = line.indexOf ("<");
          if (indexOf > 0)
            line = line.substring (indexOf);
        }
        ++lineCounter;
        WebHelper.writeString (line);
      }
    }
  }

  // TODO Shiva: This logic probably exists else where too.
  final static Pattern COMPOSITE_ITEMID_PATTERN = Pattern.compile ("I-(?<bankid>[^/]*)-(?<itemid>[^/]*)");

  private Pair<String, String> getBankIdItemIdTuple (String compositeId) {
    Matcher m = COMPOSITE_ITEMID_PATTERN.matcher (compositeId);
    if (m.matches ()) {
      final String bankId = m.group ("bankid");
      final String itemId = m.group ("itemid");
      return new Pair<String, String> ()
      {
        private static final long serialVersionUID = 1L;

        @Override
        public String setValue (String value) {
          // TODO Auto-generated method stub
          return null;
        }

        @Override
        public String getLeft () {
          return bankId;
        }

        @Override
        public String getRight () {
          return itemId;
        }
      };
    }
    return null;
  }
}
