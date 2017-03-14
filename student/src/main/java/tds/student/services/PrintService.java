/*******************************************************************************
 * Educational Online Test Delivery System Copyright (c) 2014 American
 * Institutes for Research
 * 
 * Distributed under the AIR Open Source License, Version 1.0 See accompanying
 * file AIR-License-1_0.txt or at http://www.smarterapp.org/documents/
 * American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.services;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import tds.itemrenderer.data.AccLookup;
import tds.itemrenderer.data.ITSAttachment;
import tds.itemrenderer.data.ITSContent;
import tds.student.services.abstractions.IContentService;
import tds.student.services.data.ItemResponse;
import tds.student.services.data.PageGroup;
import tds.student.services.data.TestOpportunity;
import tds.student.sql.abstractions.IOpportunityRepository;
import tds.student.sql.data.OpportunityInstance;
import AIR.Common.TDSLogger.ITDSLogger;
import TDS.Shared.Exceptions.ReturnStatusException;

import java.io.IOException;
import java.util.List;

/**
 * @author temp_rreddy
 * 
 */
@Component
@Scope ("prototype")
public class PrintService
{
  private static final Logger    _logger = LoggerFactory
                                             .getLogger (PrintService.class);

  @Autowired
  IContentService                _contentService;

  @Autowired
  private IOpportunityRepository _oppRepository;

  @Autowired
  private ITDSLogger             _tdsLogger;

  private String getItemLabel (ItemResponse response, boolean isTranscript) {
    return String.format ("%1$s %2$d",
            isTranscript ? "Item and Transcript" : "Item",
            response.getPosition ()
    );
  }

  private String getPassageLabel (PageGroup group, boolean isTranscript) throws ReturnStatusException {
    StringBuilder label = new StringBuilder ("Passage");

    if (isTranscript) {
      label.append(" and Transcript");
    }

    if (group.size () > 0) {
      label.append (" for ");

      ItemResponse firstResponse = group.getFirst ();
      ItemResponse lastResponse = group.getLast ();

      if (group.size () == 1) {
        // TODO
        // label.appendFormat("Item {0}", firstResponse.getPosition ());
        String postion = String.format ("Item %1$d",
            firstResponse.getPosition ());
        label.append (postion);

      } else {
        // TODO
        // label.appendFormat("Items {0}-{1}", firstResponse.getPosition
        // (),
        // lastResponse.getPosition ());
        String postion = String
            .format ("Items %1$d-%2$d", firstResponse.getPosition (),
                lastResponse.getPosition ());
        label.append (postion);

      }
    }
    return label.toString ();

  }

  /**
   * Determines if the list of attachments means that this is a Braille Transcript embossing request
   *
   * @param brailleAttachments List of braille attachments for this request
   * @return
     */
  private static boolean isTranscriptRequest(List<ITSAttachment> brailleAttachments) {
    return brailleAttachments.size() == 2 && brailleAttachments.get(1).getSubType().endsWith("_transcript");
  }

  public boolean printPassage (OpportunityInstance oppInstance, PageGroup pageGroupToPrint, String requestParameters) throws ReturnStatusException {
    return printPassage ("PRINTPASSAGE", oppInstance, pageGroupToPrint, requestParameters);
  }

  public boolean printPage (OpportunityInstance oppInstance, PageGroup pageGroupToPrint, String requestParameters) throws ReturnStatusException {
    return printPassage ("PRINTPAGE", oppInstance, pageGroupToPrint, requestParameters);
  }

  // / <summary>
  // / Sends a request to the proctor to print a passage.
  // / </summary>
  public boolean printPassage (String requestType, OpportunityInstance oppInstance, PageGroup pageGroupToPrint, String requestParameters) throws ReturnStatusException {
    if (pageGroupToPrint == null || pageGroupToPrint.size () == 0)
      return false;
    try {
      // get xml file to print
      String requestValue = pageGroupToPrint.getFilePath ();
      if (StringUtils.isEmpty (requestValue))
        return false;

      // send request to session DB
      String requestDesc = getPassageLabel (pageGroupToPrint, false);
      _oppRepository.submitRequest (oppInstance,
          pageGroupToPrint.getNumber (), 0, requestType,
          requestDesc, requestValue.replace ("\\", "\\\\"), requestParameters);
    } catch (ReturnStatusException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return true;
  }

  // / <summary>
  // / Sends a request to the proctor to print a item.
  // / </summary>
  public boolean printItem (OpportunityInstance oppInstance, ItemResponse responseToPrint, String requestParameters) throws ReturnStatusException {
    if (responseToPrint == null)
      return false;
    try {
      // get xml file to print
      String requestValue = responseToPrint.getFilePath ();
      if (StringUtils.isEmpty (requestValue))
        return false;
      // send request to session DB
      String requestDesc = getItemLabel (responseToPrint, false);
      _oppRepository.submitRequest (oppInstance,
          responseToPrint.getPage (), responseToPrint.getPosition (),
          "PRINTITEM", requestDesc, requestValue.replace ("\\", "\\\\"), requestParameters);
    } catch (ReturnStatusException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }

    return true;
  }

  public boolean printPassageBraille (TestOpportunity testOpp, PageGroup pageGroupToPrint, AccLookup accLookup) throws ReturnStatusException {
    return printPassageBraille ("EMBOSSPASSAGE", testOpp, pageGroupToPrint, accLookup);
  }

  public boolean printPageBraille (TestOpportunity testOpp, PageGroup pageGroupToPrint, AccLookup accLookup) throws ReturnStatusException {
    return printPassageBraille ("EMBOSSPAGE", testOpp, pageGroupToPrint, accLookup);
  }

  // / <summary>
  // / Sends a request to the proctor to print passage BRF/PRN.
  // / </summary>
  public boolean printPassageBraille (String requestType, TestOpportunity testOpp, PageGroup pageGroupToPrint, AccLookup accLookup) throws ReturnStatusException {
    if (pageGroupToPrint == null || pageGroupToPrint.size () == 0)
      return false;
    try {
      // check for file path
      String xmlPath = pageGroupToPrint.getFilePath ();
      if (StringUtils.isEmpty (xmlPath)) {
        throw new ReturnStatusException (
            new Exception (
                String.format (
                    "PrintPassageBraille: Invalid xml file path for group %1$s.",
                    pageGroupToPrint.getId ())));
      }

      // try and load document if it isn't already loaded
      if (pageGroupToPrint.getDocument () == null) {
        pageGroupToPrint.setDocument (_contentService.getContent (
            xmlPath, accLookup));
      }

      // check if document is loaded
      if (pageGroupToPrint.getDocument () == null)
        return false;

      // get content
      ITSContent content = pageGroupToPrint.getDocument ().getContent (
          testOpp.getLanguage ());
      if (content == null)
        return false;

      // get attachments for the accommodations braille type
      List<ITSAttachment> brailleAttachments = content.GetBrailleTypeAttachment (accLookup);

      // check if any attachments
      if (brailleAttachments == null || brailleAttachments.isEmpty()) {
        // log attachments are missing
        String testKey = testOpp.getTestKey ();
        String groupID = pageGroupToPrint.getId ();
        String error = String.format ("PrintPassageBraille: Cannot find a matching braille attachment for the test %s and passage %s.",
            testKey, groupID);
        _tdsLogger.rendererWarn (error, "printPassageBraille");
        return false;
      }

      ITSAttachment mainBrailleAttachment = brailleAttachments.get(0);

      // final String requestType = "EMBOSSPASSAGE";
      String requestValue = mainBrailleAttachment.getFile ();
      String requestParameters = "FileFormat:" + mainBrailleAttachment.getType ().toUpperCase (); // name:value;name:value

      boolean isTranscript = isTranscriptRequest(brailleAttachments);

      String requestDesc = String.format ("%1$s (%2$s)",
              getPassageLabel (pageGroupToPrint, isTranscript),
              mainBrailleAttachment.getType ()
      );

      if (isTranscript) {
        requestValue += ";" + brailleAttachments.get(1).getFile();
      }

      _oppRepository.submitRequest (testOpp.getOppInstance (),
              pageGroupToPrint.getNumber (), 0, requestType, requestDesc,
              requestValue.replace ("\\", "\\\\"), requestParameters);

    } catch (ReturnStatusException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    pageGroupToPrint.setPrinted (true);

    return true;
  }

  // / <summary>
  // / Sends a request to the proctor to print item BRF/PRN.
  // / </summary>
  public boolean printItemBraille (TestOpportunity testOpp, ItemResponse responseToPrint, AccLookup accLookup) throws ReturnStatusException {
    try {
      // load content
      String xmlPath = responseToPrint.getFilePath ();
      if (StringUtils.isEmpty (xmlPath)) {
        throw new ReturnStatusException (
            new Exception (
                String.format (
                    "PrintItemBraille: Invalid xml file path for item %1$s",
                    responseToPrint.getItemID ())));
      }
      // try and load document if it isn't already loaded
      if (responseToPrint.getDocument () == null) {
        responseToPrint.setDocument (_contentService.getContent (xmlPath,
            accLookup));
      }
      // check if document is loaded
      if (responseToPrint.getDocument () == null)
        return false;

      // get content
      ITSContent content = responseToPrint.getDocument ().getContent (
          testOpp.getLanguage ());
      if (content == null)
        return false;

      // get attachemnt for the accommodations braille type
      List<ITSAttachment> brailleAttachments = content.GetBrailleTypeAttachment (accLookup);

      // check if any attachments
      if (brailleAttachments == null || brailleAttachments.isEmpty()) {
        // log attachments are missing
        String testKey = testOpp.getTestKey ();
        String itemID = responseToPrint.getItemID ();
        String error = String.format ("PrintItemBraille: Cannot find a matching braille attachment for the test %s and item %s.",
            testKey, itemID);
        _tdsLogger.rendererWarn (error, "printItemBraille");
        return false;
      }

      ITSAttachment mainBrailleAttachment = brailleAttachments.get(0);

      boolean isTranscript = isTranscriptRequest(brailleAttachments);

      final String requestType = "EMBOSSITEM";
      String requestValue = mainBrailleAttachment.getFile();
      String requestParameters = "FileFormat:" + mainBrailleAttachment.getType().toUpperCase();
      String requestDesc = String.format("%1$s (%2$s)",
              getItemLabel(responseToPrint, isTranscript),
              mainBrailleAttachment.getType()
      );

      if (isTranscript) {
        requestValue += ";" + brailleAttachments.get(1).getFile();
      }

      _oppRepository.submitRequest(testOpp.getOppInstance(),
              responseToPrint.getPage(), responseToPrint.getPosition(),
              requestType, requestDesc, requestValue.replace("\\", "\\\\"), requestParameters);

      responseToPrint.setPrinted (true);
    } catch (ReturnStatusException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return true;
  }
}
