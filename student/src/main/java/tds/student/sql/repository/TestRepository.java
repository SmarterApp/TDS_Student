/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.repository;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import TDS.Shared.Exceptions.ReturnStatusException;
import tds.dll.api.IRtsDLL;
import tds.student.sql.abstractions.ITestRepository;
import AIR.Common.DB.AbstractDAO;
import AIR.Common.DB.SQLConnection;

/**
 * @author jmambo
 * 
 */
@Component
@Scope ("prototype")
public class TestRepository extends AbstractDAO implements ITestRepository
{
  private static final Logger    _logger                = LoggerFactory.getLogger (TestRepository.class);

  @Autowired
  private IRtsDLL                _rtsDll                = null;
  
  public TestRepository () {
    super ();
  }

  @Override
  public String getTrTestId (String testeeId, String testKey) throws ReturnStatusException {
    try (SQLConnection connection = getSQLConnection ()) {
        return _rtsDll.getTrTestId(connection, testeeId, testKey);
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
  }

}
