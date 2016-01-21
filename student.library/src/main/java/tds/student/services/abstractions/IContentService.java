/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.services.abstractions;

import tds.itemrenderer.data.AccLookup;
import tds.itemrenderer.data.IITSDocument;
import tds.itemrenderer.data.ITSMachineRubric;
import tds.itemscoringengine.RubricContentSource;
//import tds.itemscoringengine.Score.RubricContentSource;
import tds.student.services.data.PageGroup;
import TDS.Shared.Exceptions.ReturnStatusException;

/**
 * @author temp_rreddy
 * 
 */
public interface IContentService
{
  // / <summary>
  // / Load ITS document.
  // / </summary>
  // not required
  IITSDocument getContent (String xmlFilePath, AccLookup accommodations) throws ReturnStatusException;

  // / <summary>
  // / Load ITS document.
  // / </summary>
  IITSDocument getItemContent (long bankKey, long itemKey, AccLookup
      accommodations) throws ReturnStatusException;

  // / <summary>
  // / Load ITS content.
  // / </summary>
  IITSDocument getStimulusContent (long bankKey, long stimulusKey, AccLookup
      accommodations) throws ReturnStatusException;

  // / <summary>
  // / Given a ITS document get the machine rubric.
  // / </summary>
  // / <param name="itsDocument"></param>
  // / <param name="language"></param>
  // / <param name="rubricContentSource"></param>
  // / <returns></returns>
  ITSMachineRubric parseMachineRubric (IITSDocument itsDocument, String language, RubricContentSource rubricContentSource) throws ReturnStatusException;

  void loadPageGroupDocuments (PageGroup pageGroup, AccLookup accLookup) throws ReturnStatusException;
}
