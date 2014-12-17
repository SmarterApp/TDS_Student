/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *       
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.iris.logger;

import javax.servlet.http.HttpServletRequest;

import AIR.Common.TDSLogger.ITDSLogger;

public class IrisLogger implements ITDSLogger
{

  @Override
  public void applicationError (String msg, String methodName, HttpServletRequest request, Exception ex) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void applicationWarn (String msg, String methodName, HttpServletRequest request, Exception ex) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void applicationInfo (String msg, String methodName, HttpServletRequest request) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void applicationFatal (String msg, String methodName, Exception ex) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void configFatal (String msg, String methodName, Exception ex) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void configError (String msg, String methodName, Exception ex) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void sqlWarn (String msg, String methodName) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void javascriptError (String msg, String details, String methodName, HttpServletRequest request) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void javascriptCritical (String msg, String details, String methodName, HttpServletRequest request) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void javascriptInfo (String msg, String details, String methodName, HttpServletRequest request) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void rendererWarn (String msg, String methodName) {
    // TODO Auto-generated method stub
    
  }

}
