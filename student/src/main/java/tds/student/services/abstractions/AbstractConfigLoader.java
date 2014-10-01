/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.services.abstractions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tds.student.sql.data.AccList;
import tds.student.sql.data.AppExterns;
import tds.student.sql.data.ForbiddenApps;
import tds.student.sql.data.ItemScoringConfig;
import tds.student.sql.data.TesteeAttributeMetadata;
import tds.student.sql.data.NetworkDiagnostic;
import tds.student.sql.data.PTSetup;
import tds.student.sql.data.TTSVoicePack;
import AIR.Common.DB.AbstractDAO;
import TDS.Shared.Browser.BrowserRule;
import TDS.Shared.Exceptions.ReturnStatusException;

public abstract class AbstractConfigLoader extends AbstractDAO
{
  protected AppExterns                    _externs;
  protected PTSetup                       _ptSetup;
  protected List<TesteeAttributeMetadata> _testeeAttributeMetadata;
  protected ForbiddenApps                 _forbiddenApps;
  protected List<TTSVoicePack>            _voicePacks;
  protected AccList                       _globalAccList;
  protected List<BrowserRule>             _browserGlobalRules;
  protected Map<String, List<BrowserRule>> _browserTestRulesLookup;
  protected List<NetworkDiagnostic>       _networkDiagnostics;
  protected Map<String, Object>           _appSettings;
  protected List<String>                  _comments;

  protected String                        _clientName;

  public AppExterns getExterns () {
    return _externs;
  }

  public PTSetup getPTSetup () {
    return _ptSetup;
  }

  public List<TesteeAttributeMetadata> getTesteeAttributeMetadata () {
    return _testeeAttributeMetadata;
  }

  public ForbiddenApps getForbiddenApps () {
    return _forbiddenApps;
  }

  public List<TTSVoicePack> getVoicePacks () {
    return _voicePacks;
  }

  public AccList get_globalAccList () {
    return _globalAccList;
  }

  public List<BrowserRule> getBrowserRules () {
    return _browserGlobalRules;
  }
  
  public List<BrowserRule> getBrowserTestRules(String testID) {
      List<BrowserRule> rules = new ArrayList<BrowserRule>();
      
      // check for test
      if (_browserTestRulesLookup.containsKey(testID)) {
          rules.addAll(_browserTestRulesLookup.get(testID));
      }

      // check for wildcard
      if (_browserTestRulesLookup.containsKey("*")) {
          rules.addAll(_browserTestRulesLookup.get("*"));
      }

      return rules;
  }

  public List<NetworkDiagnostic> getNetworkDiagnostics () {
    return _networkDiagnostics;
  }

  public List<String> getComments () {
    return _comments;
  }

  public AccList getGlobalAccommodations () {
    return _globalAccList;
  }

  public Object getAppSetting (String name) {
    Object value;
    value = _appSettings.get (name);
    return value;
  }

  public abstract List<String> getClients () throws ReturnStatusException;

  public abstract void load () throws ReturnStatusException;

  protected List<ItemScoringConfig> _itemScoringConfigs;

  public List<ItemScoringConfig> getItemScoringConfigs ()
  {
    return _itemScoringConfigs;
  }
}
