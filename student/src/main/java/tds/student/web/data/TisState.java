/*************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2014 American Institutes for Research
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at 
 * https://bitbucket.org/sbacoss/eotds/wiki/AIR_Open_Source_License
 *************************************************************************/

package tds.student.web.data;

/**
 * @author mpatel
 *
 */
public class TisState {
  String   oppKey;
  Boolean  success;
  String   error;
  
  
  public String getOppKey () {
    return oppKey;
  }
  public void setOppKey (String oppKey) {
    this.oppKey = oppKey;
  }
  public Boolean getSuccess () {
    return success;
  }
  public String getError () {
    return error;
  }
  public void setSuccess (Boolean success) {
    this.success = success;
  }
  public void setError (String error) {
    this.error = error;
  }
  
  @Override
  public String toString () {
    return "TisState [oppKey=" + oppKey + ", success=" + success + ", error=" + error + "]";
  }
  
  
  
}
