/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.abstractions;

import java.util.List;
import java.util.Map;

import TDS.Shared.Browser.BrowserRule;
import TDS.Shared.Exceptions.ReturnStatusException;
import tds.student.sql.data.AccList;
import tds.student.sql.data.AppExterns;
import tds.student.sql.data.ForbiddenApps;
import tds.student.sql.data.ItemScoringConfig;
import tds.student.sql.data.TesteeAttributeMetadata;
import tds.student.sql.data.NetworkDiagnostic;
import tds.student.sql.data.PTSetup;
import tds.student.sql.data.TTSVoicePack;

/**
 * 
 * @author temp_rreddy
 * 
 * 
 */

public interface IConfigRepository

{

  List<String> getClients () throws ReturnStatusException;

  AppExterns getExterns () throws ReturnStatusException;

  PTSetup getPTSetup () throws ReturnStatusException;

  Iterable<TesteeAttributeMetadata> getLoginRequirements () throws ReturnStatusException;

  /**
   * Returns the records of the client_testeeattribute table
   * @param ids Array of strings designating which attribute keys (tds_id) to match. Returns all if null.
   * @param types Array of strings designating types to match. Returns all types if null
   * @param atLogins Array of strings designating "atLogin" values to match. Returns all if null.
   * @return A map whose values are the TesteeAttributeMetadata instances, and whose keys are the ID values
   * @throws ReturnStatusException
   */
  Map<String,TesteeAttributeMetadata> getTesteeAttributeMetadata ( String[] ids, String[] types, String[] atLogins ) throws ReturnStatusException;

  ForbiddenApps getForbiddenApps () throws ReturnStatusException;

  Iterable<TTSVoicePack> getVoicePacks () throws ReturnStatusException;

  AccList getGlobalAccommodations () throws ReturnStatusException;

  // not using
  // CLSConfig getCLSConfig ();

  Object getAppSetting (String name) throws ReturnStatusException;

  Iterable<BrowserRule> getBrowserRules () throws ReturnStatusException;
  
  Iterable<BrowserRule> getBrowserTestRules(String testKey) throws ReturnStatusException;

  Iterable<NetworkDiagnostic> getNetworkDiagnostics () throws ReturnStatusException;

  Iterable<String> getComments () throws ReturnStatusException;

  Iterable<ItemScoringConfig> getItemScoringConfigs() throws ReturnStatusException;

}
