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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import tds.student.sql.abstractions.ITisRepository;

import AIR.Common.Web.TDSReplyCode;
import AIR.Common.data.ResponseData;
import TDS.Shared.Exceptions.ReturnStatusException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
  
  @RequestMapping (value = "tisReply")
  @ResponseBody
  public void tisReply (@RequestParam (value = "key", required = true) String oppKeyStr, @RequestParam (value = "success", required = true) boolean success,
      @RequestParam (value = "error", required = false) String error,  HttpServletRequest request, HttpServletResponse response) throws ReturnStatusException, IOException {
 
    UUID testopp = null;
 //   Boolean success = null;
    try {
      testopp = UUID.fromString (oppKeyStr);
    } catch (IllegalArgumentException ie) {
      String msg = String.format ("Illegal testopp format: %s", oppKeyStr);
      _logger.error(msg);
      response.sendError (HttpStatus.BAD_REQUEST_400, msg);
      return;
    }
//    if ("true".equalsIgnoreCase (successStr) || "1".equalsIgnoreCase (successStr))
//      success = true;
//    else if ("false".equalsIgnoreCase (successStr) || "0".equalsIgnoreCase (successStr))
//      success = false;
//    else {
//      String msg = String.format ("Illegal success param format: %s", successStr);
//      _logger.error(msg);
//      response.sendError (HttpStatus.BAD_REQUEST_400, msg);
//      return;     
//    }
    try {
    _tisRepository.tisReply (testopp, success, error);
    } catch (ReturnStatusException re) {
      _logger.error (String.format ("Failed processing tisReply for %s, sucess= %s; Exception: %s ", testopp, success, re.getMessage ()));
    }
    response.setStatus (HttpStatus.OK_200);
  }
  
  @RequestMapping (value = "tisReply1")
  @ResponseBody
  public ResponseData<String> tisReply(HttpServletRequest request) throws ReturnStatusException {
    TisState tisState;
    
    try {
      ObjectMapper mapper = new ObjectMapper ();
      tisState = mapper.readValue (request.getInputStream (), TisState.class);
    } catch (IOException e) {
      _logger.error ("Failed to parse tisReply");
      return new ResponseData<String>(TDSReplyCode.Error.getCode (), "Failed to parse tisReply", "");
    }
    UUID testopp = null;
    try {
      testopp = UUID.fromString (tisState.oppKeyStr);
    } catch (IllegalArgumentException ie) {
      String msg = String.format ("Illegal testopp format: %s", tisState.oppKeyStr);
      _logger.error(msg);
      return new ResponseData<String>(TDSReplyCode.Error.getCode (), msg, "");
    }
    _tisRepository.tisReply (testopp, tisState.success, tisState.error);
    return new ResponseData<String> (TDSReplyCode.OK.getCode (), "OK", "");
  }
  
  class TisState {
    String   oppKeyStr;
    Boolean  success;
    String   error;
  }
}
