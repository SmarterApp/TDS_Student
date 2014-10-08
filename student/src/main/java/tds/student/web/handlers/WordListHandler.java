/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.web.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import tds.itemrenderer.handler.WordListHandlerBase;
import tds.student.sql.abstractions.IItemBankRepository;
import TDS.Shared.Exceptions.ReturnStatusException;

/**
 * @author jmambo
 *
 */
@Controller
@Scope ("prototype")
public class WordListHandler extends WordListHandlerBase
{
  
  @Autowired
  private IItemBankRepository _ibRepository;


  /* (non-Javadoc)
   * @see tds.itemrenderer.handler.WordListHandlerBase#getItemPath(long, long)
   */
  @Override
  protected String getItemPath (long bankKey, long itemKey) throws ReturnStatusException {
      return _ibRepository.getItemPath(bankKey, itemKey);
  }

}
