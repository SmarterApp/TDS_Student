/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.repositorysp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import tds.student.services.abstractions.AbstractConfigLoader;
import tds.student.sql.abstractions.IConfigRepository;
import tds.student.sql.data.AccList;
import tds.student.sql.data.AppExterns;
import tds.student.sql.data.ForbiddenApps;
import tds.student.sql.data.ItemScoringConfig;
import tds.student.sql.data.TesteeAttributeMetadata;
import tds.student.sql.data.NetworkDiagnostic;
import tds.student.sql.data.PTSetup;
import tds.student.sql.data.TTSVoicePack;
import tds.student.sql.singletons.ClientSingleton;
import tds.student.web.dummy.TDSApplication;
import TDS.Shared.Browser.BrowserRule;
import TDS.Shared.Exceptions.ReturnStatusException;

/**
 * 
 * @author temp_rreddy
 * 
 * 
 */
@Component
@Scope ("prototype")
public class ConfigRepository implements IConfigRepository, ApplicationContextAware

{
  private static final Logger  _logger = LoggerFactory.getLogger (ConfigRepository.class);

  private AbstractConfigLoader _loader;

  public ConfigRepository () {
    super ();
  }

  public List<String> getClients () throws ReturnStatusException {
    try {
      return this._loader.getClients ();
    } catch (ReturnStatusException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
  }

  public AppExterns getExterns () throws ReturnStatusException {
    return this._loader.getExterns ();
  }

  public PTSetup getPTSetup () {
    return this._loader.getPTSetup ();
  }

  public Iterable<TesteeAttributeMetadata> getLoginRequirements() {
    return getTesteeAttributeMetadata (null, null, new String[]{"VERIFY", "REQUIRE"} ).values ();
  }

  public ForbiddenApps getForbiddenApps () {
    return this._loader.getForbiddenApps ();
  }

  public Iterable<TTSVoicePack> getVoicePacks () {
    return this._loader.getVoicePacks ();
  }

  public AccList getGlobalAccommodations () {
    return this._loader.getGlobalAccommodations ();
  }

  // Not using
  /*
   * public CLSConfig getCLSConfig () {
   * 
   * return _loader.getCLSConfig ();
   * 
   * }
   */

  public Object getAppSetting (String name) {
    return _loader.getAppSetting (name);
  }

  public Iterable<BrowserRule> getBrowserRules () {
    return _loader.getBrowserRules ();
  }

  public Iterable<NetworkDiagnostic> getNetworkDiagnostics () {
    return _loader.getNetworkDiagnostics ();
  }

  public Iterable<String> getComments () {
    return _loader.getComments ();
  }

  @Override
  public Iterable<ItemScoringConfig> getItemScoringConfigs() throws ReturnStatusException {
	  return _loader.getItemScoringConfigs();
  }
  
  @Override
  public void setApplicationContext (ApplicationContext applicationContext) throws BeansException {
    ClientSingleton client = TDSApplication.getClient (applicationContext);
    _loader = client.getConfig ();
  }

  @Override
  public Map<String, TesteeAttributeMetadata> getTesteeAttributeMetadata (
      String[] ids,
      String[] types,
      String[] atLogins) {
    final Map<String,TesteeAttributeMetadata> answer = new HashMap<String,TesteeAttributeMetadata>();
    for ( TesteeAttributeMetadata record : _loader.getTesteeAttributeMetadata ()) {
      if ( ids != null ) {
        boolean match_id = false;
        final String id_i = record.getId ();
        for ( final String id_j : ids ) {
          if (id_j.equalsIgnoreCase (id_i)) {
            match_id = true;
            break;
          }
        }
        if ( ! match_id ) {
          continue;
        }
      }
      if ( types != null ) {
        boolean match_type = false;
        final String type_i = record.getType ();
        for ( final String type_j : ids ) {
          if (type_j.equalsIgnoreCase (type_i)) {
            match_type = true;
            break;
          }
        }
        if ( ! match_type ) {
          continue;
        }
      }
      if ( atLogins != null ) {
        boolean match_atLogin = false;
        final String atLogin_i = record.getAtLogin ();
        for ( String atLogin_j : ids ) {
          if (atLogin_j.equalsIgnoreCase (atLogin_i)) {
            match_atLogin = true;
            break;
          }
        }
        if ( ! match_atLogin ) {
          continue;
        }
      }
    }
    return answer;
  }

  @Override
  public Iterable<BrowserRule> getBrowserTestRules(String testKey)
		  throws ReturnStatusException {
	  return _loader.getBrowserTestRules(testKey);
  }

@Override
public Map<String, Object> getClientAppSettings() throws ReturnStatusException {
	// TODO Auto-generated method stub
	return null;
}
}
