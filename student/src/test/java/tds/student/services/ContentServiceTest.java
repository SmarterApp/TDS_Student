/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.services;

import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import tds.itemrenderer.data.AccLookup;
import tds.itemrenderer.data.IITSDocument;
import tds.itemrenderer.data.ITSMachineRubric;
import tds.itemscoringengine.RubricContentSource;
import tds.student.services.data.PageGroup;
import AIR.test.framework.AbstractTest;
import TDS.Shared.Exceptions.ReturnStatusException;

/**
 * @author temp_rreddy
 * 
 */
public class ContentServiceTest extends AbstractTest
{
  private static final Logger _logger            = LoggerFactory.getLogger (ContentServiceTest.class);

  // TODO: Use Spring configuration
  @Autowired
  ContentService     contentService;

  // Suceess Test Case
  @Test
  @Ignore("Autowire issues")
  public void testLogin () throws SQLException, ReturnStatusException {
    String xmlFilePath = null;
    AccLookup accommodations = null;
    IITSDocument iITSDocument = contentService.getContent (xmlFilePath, accommodations);
    Assert.assertTrue (iITSDocument != null);
    if (iITSDocument != null) {
      _logger.info ("Base Uri::" + iITSDocument.getBaseUri ());
      _logger.info ("Bank Key::" + iITSDocument.getBankKey ());
      _logger.info ("Answer Key::" + iITSDocument.getAnswerKey ());
      _logger.info ("Copy right Key::" + iITSDocument.getCopyright ());
      _logger.info ("Credit Key::" + iITSDocument.getCredit ());
      _logger.info ("Folder Key::" + iITSDocument.getFolderName ());
      _logger.info ("Format Key::" + iITSDocument.getFormat ());
      _logger.info ("Grade Key::" + iITSDocument.getGrade ());
      _logger.info ("Grid Answer Key::" + iITSDocument.getGridAnswerSpace ());
      _logger.info ("Group Id Key::" + iITSDocument.getGroupID ());
      _logger.info ("ID Key::" + iITSDocument.getID ());
      _logger.info ("Item Key::" + iITSDocument.getItemKey ());
      _logger.info ("Parent Folder name Key::" + iITSDocument.getParentFolderName ());
      _logger.info ("Render Spec Key::" + iITSDocument.getRendererSpec ());
      _logger.info ("Stimulus Key::" + iITSDocument.getStimulusKey ());
      _logger.info ("Dri Dir Segments Key::" + iITSDocument.getBaseUriDirSegments ());
    }
  }

  // Failure Test Case
  @Test
  @Ignore("Autowire issues")
  public void testLoginFailure () throws SQLException, ReturnStatusException {
    String xmlFilePath = null;
    AccLookup accommodations = null;
    IITSDocument iITSDocument = contentService.getContent (xmlFilePath, accommodations);
    Assert.assertTrue (iITSDocument == null);
  }

  // Success Test Case
  @Test
  @Ignore("Autowire issues")
  public void testGetItemContent () throws SQLException, ReturnStatusException {
    long bankKey = 0;
    long itemKey = 0;
    AccLookup accommodations = null;
    IITSDocument iITSDocument = contentService.getItemContent (bankKey, itemKey, accommodations);
    Assert.assertTrue (iITSDocument != null);
    if (iITSDocument != null) {
      _logger.info ("Base Uri::" + iITSDocument.getBaseUri ());
      _logger.info ("Bank Key::" + iITSDocument.getBankKey ());
      _logger.info ("Answer Key::" + iITSDocument.getAnswerKey ());
      _logger.info ("Copy right Key::" + iITSDocument.getCopyright ());
      _logger.info ("Credit Key::" + iITSDocument.getCredit ());
      _logger.info ("Folder Key::" + iITSDocument.getFolderName ());
      _logger.info ("Format Key::" + iITSDocument.getFormat ());
      _logger.info ("Grade Key::" + iITSDocument.getGrade ());
      _logger.info ("Grid Answer Key::" + iITSDocument.getGridAnswerSpace ());
      _logger.info ("Group Id Key::" + iITSDocument.getGroupID ());
      _logger.info ("ID Key::" + iITSDocument.getID ());
      _logger.info ("Item Key::" + iITSDocument.getItemKey ());
      _logger.info ("Parent Folder name Key::" + iITSDocument.getParentFolderName ());
      _logger.info ("Render Spec Key::" + iITSDocument.getRendererSpec ());
      _logger.info ("Stimulus Key::" + iITSDocument.getStimulusKey ());
      _logger.info ("Dri Dir Segments Key::" + iITSDocument.getBaseUriDirSegments ());
    }
  }

  // Failure Test Case
  @Test
  @Ignore("Autowire issues")
  public void testGetItemContentFailure () throws SQLException, ReturnStatusException {
    long bankKey = 0;
    long itemKey = 0;
    AccLookup accommodations = null;
    IITSDocument iITSDocument = contentService.getItemContent (bankKey, itemKey, accommodations);
    Assert.assertTrue (iITSDocument == null);
  }

  // Success Test Case
  @Test
  @Ignore("Autowire issues")
  public void testgetStimulusContent () throws SQLException, ReturnStatusException {
    long bankKey = 0;
    long itemKey = 0;
    AccLookup accommodations = null;
    IITSDocument iITSDocument = contentService.getStimulusContent (bankKey, itemKey, accommodations);
    Assert.assertTrue (iITSDocument != null);
    if (iITSDocument != null) {
      _logger.info ("Base Uri::" + iITSDocument.getBaseUri ());
      _logger.info ("Bank Key::" + iITSDocument.getBankKey ());
      _logger.info ("Answer Key::" + iITSDocument.getAnswerKey ());
      _logger.info ("Copy right Key::" + iITSDocument.getCopyright ());
      _logger.info ("Credit Key::" + iITSDocument.getCredit ());
      _logger.info ("Folder Key::" + iITSDocument.getFolderName ());
      _logger.info ("Format Key::" + iITSDocument.getFormat ());
      _logger.info ("Grade Key::" + iITSDocument.getGrade ());
      _logger.info ("Grid Answer Key::" + iITSDocument.getGridAnswerSpace ());
      _logger.info ("Group Id Key::" + iITSDocument.getGroupID ());
      _logger.info ("ID Key::" + iITSDocument.getID ());
      _logger.info ("Item Key::" + iITSDocument.getItemKey ());
      _logger.info ("Parent Folder name Key::" + iITSDocument.getParentFolderName ());
      _logger.info ("Render Spec Key::" + iITSDocument.getRendererSpec ());
      _logger.info ("Stimulus Key::" + iITSDocument.getStimulusKey ());
      _logger.info ("Dri Dir Segments Key::" + iITSDocument.getBaseUriDirSegments ());
    }
  }

  // Failure Test Case
  @Test
  @Ignore("Autowire issues")
  public void testgetStimulusContentFailure () throws SQLException, ReturnStatusException {
    long bankKey = 0;
    long itemKey = 0;
    AccLookup accommodations = null;
    IITSDocument iITSDocument = contentService.getStimulusContent (bankKey, itemKey, accommodations);
    Assert.assertTrue (iITSDocument == null);
  }

  // Failure Test Case
  @Test
  @Ignore("Autowire issues")
  public void testLoadPageGroupDocuments () throws SQLException, ReturnStatusException {
    PageGroup pageGroup = null;
    AccLookup accLookup = null;
    contentService.loadPageGroupDocuments (pageGroup, accLookup);
  }

  // Success Test Case
  @Test
  @Ignore("Autowire issues")
  public void testParseMachineRubric () throws SQLException, ReturnStatusException {
    IITSDocument itsDocument = null;
    String language = null;
    RubricContentSource rubricContentSource = null;
    ITSMachineRubric iTSMachineRubric = contentService.parseMachineRubric (itsDocument, language, rubricContentSource);
    Assert.assertTrue (iTSMachineRubric != null);
    if (iTSMachineRubric != null) {
      _logger.info ("Data::" + iTSMachineRubric.getData ());
      _logger.info ("Is Valid::" + iTSMachineRubric.getIsValid ());
      _logger.info ("Type Value::" + iTSMachineRubric.getType ());
    }
  }

  // Failure Test Case
  @Test
  @Ignore("Autowire issues")
  public void testParseMachineRubricFailue () throws SQLException, ReturnStatusException {
      IITSDocument itsDocument = null;
      String language = null;
      RubricContentSource rubricContentSource = null;
      ITSMachineRubric iTSMachineRubric = contentService.parseMachineRubric (itsDocument, language, rubricContentSource);
      Assert.assertTrue (iTSMachineRubric == null);
  }

}
