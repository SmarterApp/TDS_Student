/*************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2014 American Institutes for Research
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at 
 * https://bitbucket.org/sbacoss/eotds/wiki/AIR_Open_Source_License
 *************************************************************************/

package tds.student.web.handlers;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import tds.student.sql.abstractions.ITisRepository;
import tds.student.web.data.TisState;
import TDS.Shared.Exceptions.ReturnStatusException;

/**
 * @author efurman
 *
 */
@Controller
@Scope ("prototype")
public class TisHandler // extends TDSHandler
{
  private static final Logger    _logger = LoggerFactory.getLogger (TisHandler.class);

  @Autowired
  private ITisRepository        _tisRepository;
  
  @RequestMapping (value = "tisReply",method=RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public void tisReply(@RequestBody (required=true) final TisState tisState,HttpServletResponse response) throws IOException {
    
    UUID testopp = null;
    try {
      testopp = UUID.fromString (tisState.getOppKey ());
    } catch (IllegalArgumentException ie) {
      String msg = String.format ("Illegal testopp format: %s", tisState.getOppKey ());
      _logger.error(msg);
      response.setStatus  (HttpStatus.BAD_REQUEST_400);
      return;
    }
    try {
      _tisRepository.tisReply (testopp, tisState.getSuccess (), tisState.getError ());
    } catch (ReturnStatusException re) {
      _logger.error (String.format ("Failed processing tisReply for %s, sucess= %s; Exception: %s ", testopp, tisState.getSuccess (), re.getMessage ()));
    }
    response.setStatus (HttpStatus.OK_200);
  }
  
  
  
}
