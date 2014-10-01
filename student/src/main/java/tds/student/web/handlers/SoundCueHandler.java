/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.web.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tds.itemrenderer.handler.SoundCueHandlerBase;
import tds.student.sql.abstractions.IItemBankRepository;
import AIR.Common.Utilities.SpringApplicationContext;
import TDS.Shared.Exceptions.ReturnStatusException;

/**
 * @author mpatel
 *
 */
public class SoundCueHandler extends SoundCueHandlerBase
{

  private static final Logger _logger = LoggerFactory.getLogger (SoundCueHandler.class);
  IItemBankRepository ibRepository;
  @Override
  protected String GetItemPath (long bankKey, long itemKey) {
    IItemBankRepository ibRepository = SpringApplicationContext.getBean (IItemBankRepository.class);
    try {
      return ibRepository.getItemPath(bankKey, itemKey);
    } catch (ReturnStatusException e) {
      _logger.error (e.getMessage (),e);
      return "";
    }
  }
  
}
