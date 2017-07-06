/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.services;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import tds.itemrenderer.ITSDocumentFactory;
import tds.itemrenderer.data.AccLookup;
import tds.itemrenderer.data.IITSDocument;
import tds.itemrenderer.data.ITSContent;
import tds.itemrenderer.data.ITSMachineRubric;
import tds.itemscoringengine.RubricContentSource;
/*
 * import tds.itemrenderer.ITSDocumentFactory; import
 * tds.itemrenderer.data.AccLookup; import tds.itemrenderer.data.IITSDocument;
 * import tds.itemrenderer.data.ITSContent; import
 * tds.itemrenderer.data.ITSMachineRubric;
 */
import tds.student.performance.domain.AccLookupWrapper;
import tds.student.performance.services.ContentHelperService;
import tds.student.performance.services.ItemBankService;
import tds.student.services.abstractions.IContentService;
import tds.student.services.data.ItemResponse;
import tds.student.services.data.PageGroup;
import tds.student.sql.abstractions.IItemBankRepository;
import tds.student.sql.repository.remote.ContentRepository;

import TDS.Shared.Exceptions.ReturnStatusException;

/**
 * @author temp_rreddy
 * 
 */
@Component
@Scope ("prototype")
public class ContentService implements IContentService
{
  private final ItemBankService itemBankService;
  private final ContentRepository contentRepository;

  @Autowired
  public ContentService(final ItemBankService itemBankService,
                        final ContentRepository contentRepository) {
    this.itemBankService = itemBankService;
    this.contentRepository = contentRepository;
  }

  private static final Logger       _logger = LoggerFactory.getLogger (ContentService.class);

  public IITSDocument getContent (final String xmlFilePath, final AccLookup accommodations) throws ReturnStatusException {
    return contentRepository.findItemDocument(xmlFilePath, accommodations);
  }

  public IITSDocument getItemContent (long bankKey, long itemKey, AccLookup accommodations) throws ReturnStatusException {
    try {
      String itemPath = itemBankService.getItemPath (bankKey, itemKey);
      if (StringUtils.isEmpty (itemPath))
        return null;

      return getContent (itemPath, accommodations);
    } catch (ReturnStatusException e) {
      _logger.error (e.getMessage ());
      throw e;
    }
  }

  public IITSDocument getStimulusContent (long bankKey, long stimulusKey, AccLookup accommodations) throws ReturnStatusException {
    String stimulusPath = itemBankService.getStimulusPath (bankKey, stimulusKey);
    if (StringUtils.isEmpty (stimulusPath))
      return null;

    return getContent (stimulusPath, accommodations);
  }

  // / <summary>
  // / Load all the documents for a page group.
  // / </summary>
  public void loadPageGroupDocuments (PageGroup pageGroup, AccLookup accLookup) throws ReturnStatusException {
    try {
      pageGroup.setDocument (getContent (pageGroup.getFilePath (), accLookup));
      for (ItemResponse itemResponse : pageGroup) {
        itemResponse.setDocument (getContent (itemResponse.getFilePath (), accLookup));
      }
    } catch (ReturnStatusException e) {
      _logger.error (e.getMessage ());
      throw e;
    }
  }

  // / <summary>
  // / This parses the machine rubric from an ITS document.
  // / </summary>
  // / <returns>Returns either the data or a path for the rubric depending on
  // the source.</returns>
  public ITSMachineRubric parseMachineRubric (IITSDocument itsDocument, String language, RubricContentSource rubricContentSource) throws ReturnStatusException {
    ITSMachineRubric machineRubric = null;
    // if the source is item bank then parse the answer key attribute
    // NOTE: we use to get this from the response table
    if (rubricContentSource == RubricContentSource.AnswerKey) {
      machineRubric = new ITSMachineRubric (ITSMachineRubric.ITSMachineRubricType.Text, itsDocument.getAnswerKey ()+"|"+itsDocument.getMaxScore());
    }
    // if the source is item xml then get the machine rubric element
    else if (rubricContentSource == RubricContentSource.ItemXML) {
      // get top level machine rubric
      machineRubric = itsDocument.getMachineRubric ();
      // if empty try and get content elements machine rubric
      if (machineRubric == null) {
        // get its content for the current tests language
        ITSContent itsContent = itsDocument.getContent (language);
        // make sure this item has a machine rubric
        if (itsContent != null) {
          machineRubric = itsContent.getMachineRubric ();
        }
      }
    }
    return machineRubric;
  }
}
