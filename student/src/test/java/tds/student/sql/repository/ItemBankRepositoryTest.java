/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.repository;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.ComponentScan;

import tds.student.sql.abstractions.IItemBankRepository;
import tds.student.sql.data.AccList;
import tds.student.sql.data.TestForm;
import tds.student.sql.data.TestProperties;
import tds.student.sql.data.TestSegment;
import AIR.test.framework.AbstractTest;

@ComponentScan
public class ItemBankRepositoryTest extends AbstractTest
{
	private static final Logger _logger  = LoggerFactory.getLogger(ItemBankRepositoryTest.class);	
	
	@Autowired
	@Qualifier("iItemBankRepository")
	IItemBankRepository _itemRepository = null;
  
  @Test
  public void testListTests () throws Exception{
    //((ItemBankRepository) _itemRepository)._commonDll._CanChangeOppStatus_FN (null, "abc", "123");
    try {
      List<String> list = _itemRepository.listTests ();
      Assert.assertTrue (list.size () > 0);
      _logger.info ("Test List SIZE::" + list.size ());
    } catch (Exception exp) {
      exp.printStackTrace ();
      _logger.error (exp.getMessage ());
      throw exp;
    }
  }
  
  //@Test
  public void testGetTestProperties () throws Exception{
    try {
      TestProperties testProperties = _itemRepository.getTestProperties ("(Oregon)ELPA_6-8-Winter-2012-2013");
      Assert.assertTrue (testProperties != null);
      if (testProperties != null) {
        _logger.info ("TestProperties Test Id::" + testProperties.getTestID ());
        _logger.info ("TestProperties Display Name::" + testProperties.getDisplayName ());
        _logger.info ("TestProperties is selectable::" + testProperties.isSelectable ());
        _logger.info ("TestProperties Validate Completeness::" + testProperties.isValidateCompleteness ());
        _logger.info ("TestProperties Acc Family::" + testProperties.getAccFamily ());
        _logger.info ("TestProperties Sort Order::" + testProperties.getSortOrder ());
        _logger.info ("TestProperties Grade::" + testProperties.getGrade ());
        _logger.info ("TestProperties Subject::" + testProperties.getSubject ());
        _logger.info ("TestProperties Score By TDS::" + testProperties.isScoreByTDS ());
        _logger.info ("TestProperties Max Opportunities::" + testProperties.getMaxOpportunities ());
        _logger.info ("TestProperties Min Items::" + testProperties.getMinItems ());
        _logger.info ("TestProperties Max Items::" + testProperties.getMaxItems ());
        _logger.info ("TestProperties Prefetch::" + testProperties.getPrefetch ());

        _logger.info ("TestProperties Segments List Size::" + testProperties.getSegments ().size ());

        if (testProperties.getSegments () != null) {
          List<TestSegment> testSegmentList = testProperties.getSegments ();
          if (testSegmentList != null && testSegmentList.size () > 0) {
            for (int i = 0; i < testSegmentList.size (); i++) {
              TestSegment testSegment = testSegmentList.get (i);
              _logger.info ("TestProperties Segments Id::" + testSegment.getId ());
              _logger.info ("TestProperties Segments Position::" + testSegment.getPosition ());
              _logger.info ("TestProperties Segments Lable::" + testSegment.getLabel ());
              _logger.info ("TestProperties Segments Permeable::" + testSegment.getIsPermeable ());
              _logger.info ("TestProperties Segments Entry Approval::" + testSegment.getEntryApproval ());
              _logger.info ("TestProperties Segments Exit Approval::" + testSegment.getExitApproval ());
              _logger.info ("TestProperties Segments Item Review::" + testSegment.isItemReview ());
            }

          }
        }
      }
    } catch (Exception exp) {
      exp.printStackTrace ();
      _logger.error (exp.getMessage ());
      throw exp;
    }
  }
  
  //@Test
  public void testGetTestAccommodations () throws Exception{
    try {
      AccList accList = _itemRepository.getTestAccommodations ("(Oregon)Oregon-Student Help-NA-Winter-2011-2012");
      Assert.assertTrue (accList.size () > 0);
      if (accList != null)
        _logger.info ("SIZE::" + accList.getDependencies ().size ());
    } catch (Exception exp) {
      exp.printStackTrace ();
      _logger.error (exp.getMessage ());
      throw exp;
    }
  }
  
  //@Test
  public void testGetGrades () throws Exception{
    try {
      List<String> gradesList = _itemRepository.getGrades ();
      Assert.assertTrue (gradesList.size () > 0);
      if (gradesList != null)
        _logger.info ("Grades List SIZE::" + gradesList.size ());
    } catch (Exception exp) {
      exp.printStackTrace ();
      _logger.error (exp.getMessage ());
      throw exp;
    }
  }
  
  //@Test
  public void testGetTestForms () throws Exception{
    try {
      List<TestForm> testFormsList = _itemRepository.getTestForms ("OAKS-Writing-HS");
      Assert.assertTrue (testFormsList.size () > 0);
      if (testFormsList != null)
        _logger.info ("Test Forms SIZE::" + testFormsList.size ());
    } catch (Exception exp) {
      exp.printStackTrace ();
      _logger.error (exp.getMessage ());
      throw exp;
    }
  }
  
  //TODO need valid bankkey and itemkey for 'Oregon'
  //@Test
  public void testgetItemPath () throws Exception{
    try {
      long bankkey = 0;
      long itemkey = 0;
      String itempath = _itemRepository.getItemPath (bankkey, itemkey);
      Assert.assertTrue (itempath != null);
      if (itempath != null)
        _logger.info ("itempath value::" + itempath);
    } catch (Exception exp) {
      exp.printStackTrace ();
      _logger.error (exp.getMessage ());
      throw exp;
    }
  }
  
  //TODO need test case for Oregon - bankkey and stimuluskey
  //@Test
  public void testGetStimulusPath () throws Exception {
    try {
      long bankkey = 0;
      long stimuluskey = 0;
      String stimulusPath = _itemRepository.getStimulusPath (bankkey, stimuluskey);
      Assert.assertTrue (stimulusPath != null);
      if (stimulusPath != null)
        _logger.info ("stimulusPath value::" + stimulusPath);
    } catch (Exception exp) {
      exp.printStackTrace ();
      _logger.error (exp.getMessage ());
      throw exp;
    }
  }


}
