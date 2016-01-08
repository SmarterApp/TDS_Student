/*************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2014 American Institutes for Research
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at 
 * https://bitbucket.org/sbacoss/eotds/wiki/AIR_Open_Source_License
 *************************************************************************/

package tds.student.sql.repository;

import java.sql.SQLException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import tds.dll.api.IStudentDLL;

import TDS.Shared.Exceptions.ReturnStatusException;

import tds.student.sql.abstractions.ITisRepository;
import AIR.Common.DB.AbstractDAO;
import AIR.Common.DB.SQLConnection;

/**
 * @author efurman
 *
 */
@Component
@Scope ("prototype")
public class TisRepository extends AbstractDAO implements ITisRepository
{
  private static final Logger _logger     = LoggerFactory.getLogger (TisRepository.class);

  @Autowired
  private IStudentDLL  _studentDll = null;
  
  public TisRepository () {
    super ();
  }
  
  public void setiStudentDLL (IStudentDLL _dll) {
    _studentDll = _dll;
  }
  
  public void tisReply (UUID oppkey, Boolean success, String errorMessage) throws ReturnStatusException {
    
    try (SQLConnection connection = getSQLConnection ()) {
      _studentDll.handleTISReply (connection, oppkey, success, errorMessage);
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
  }
}
