/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.repository;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

import tds.dll.api.ICommonDLL;
import tds.dll.api.IStudentDLL;
import tds.student.services.abstractions.AbstractConfigLoader;
import tds.student.sql.data.AccList;
import tds.student.sql.data.AccListParseData;
import tds.student.sql.data.AppExterns;
import tds.student.sql.data.Data;
import tds.student.sql.data.ForbiddenApps;
import tds.student.sql.data.ItemScoringConfig;
import tds.student.sql.data.TesteeAttributeMetadata;
import tds.student.sql.data.NetworkDiagnostic;
import tds.student.sql.data.PTSetup;
import tds.student.sql.data.TTSVoicePack;
import AIR.Common.Configuration.AppSettingsHelper;
import AIR.Common.DB.SQLConnection;
import AIR.Common.DB.SqlParametersMaps;
import AIR.Common.DB.results.DbResultRecord;
import AIR.Common.DB.results.MultiDataResultSet;
import AIR.Common.DB.results.SingleDataResultSet;
import AIR.Common.Web.BrowserOS;
import TDS.Shared.Browser.BrowserAction;
import TDS.Shared.Browser.BrowserRule;
import TDS.Shared.Exceptions.ReturnStatusException;

/**
 * @author temp_rreddy
 * 
 */
@Component
@Scope ("prototype")
public class ConfigLoader extends AbstractConfigLoader implements ServletContextAware
{
  private static final Logger _logger     = LoggerFactory.getLogger (ItemBankRepository.class);
  
  private String _projectsPath = "";
  
  @Autowired
  ICommonDLL                  _commonDll  = null;
  @Autowired
  private IStudentDLL         _studentDll = null;

  public ConfigLoader () {
    super ();
  }
 
  public void setiCommonDLL (ICommonDLL _dll) {
    _commonDll = _dll;
  }

  public void setiStudentDLL (IStudentDLL _dll) {
    _studentDll = _dll;
  }

  @Override
  public void load () throws ReturnStatusException {

    try {
      loadExterns ();
      loadAppSettings ();
      loadPTSetup ();
      loadLoginRequirements ();
      loadSecureBrowserRequired();
      loadForbiddenApps ();
      loadVoicePacks ();
      loadGlobalAccommodations ();
      // not using it
      // loadCLS ();
      loadNetworkDiagnostics ();
      loadBrowserRules ();
      try {
        loadComments ();
      } catch (IOException e) {
        _logger.error (e.getMessage ());
        throw new ReturnStatusException (e);
      }
      loadItemScoringConfigs();
    } catch (ReturnStatusException e1) {
      _logger.error (e1.getMessage ());
      throw new ReturnStatusException (e1);
    }
  }

  @Override
  public List<String> getClients () throws ReturnStatusException {

    final String SQL_QUERY_CLIENT_NAMES = "SELECT clientname FROM _externs";
    List<String> clientNames = new ArrayList<String> ();

    try (SQLConnection connection = getSQLConnection ()) {
      SingleDataResultSet results = executeStatement (connection, SQL_QUERY_CLIENT_NAMES, null, true).getResultSets ().next ();
      ReturnStatusException.getInstanceIfAvailable (results);
      Iterator<DbResultRecord> records = results.getRecords ();
      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        clientNames.add (record.<String> get ("clientname"));
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }

    return clientNames;

  }


  protected void loadExterns () throws ReturnStatusException {
	AppExterns externs = new AppExterns ();

    final String CMD_GET_CURRENT_SESSIONS = "SELECT * FROM externs WHERE ClientName = ${clientName} ";
    SqlParametersMaps parametersQuery = new SqlParametersMaps ();
    addClientParameter (parametersQuery);

    try (SQLConnection connection = getSQLConnection ()) {

      SingleDataResultSet results = executeStatement (connection, CMD_GET_CURRENT_SESSIONS, parametersQuery, false).getResultSets ().next ();
      ReturnStatusException.getInstanceIfAvailable (results);
      // reader.setFixNulls (true);
      Iterator<DbResultRecord> records = results.getRecords ();
      if (records.hasNext ()) {
        DbResultRecord record = records.next ();
        externs.setClientName (record.<String> get ("ClientName"));
        externs.setEnvironment (record.<String> get ("Environment"));
        externs.setClientStylePath (record.<String> get ("ClientStylePath"));
        externs.setTimeZoneOffset (record.<Integer> get ("TimeZoneOffset"));
        if (record.hasColumn ("TestDB")) {
          externs.setItemBankDB (record.<String> get ("TestDB"));
        }
        if (record.hasColumn ("testeeCheckin")) {
          externs.setTesteeCheckin (record.<String> get ("testeeCheckin"));
        }
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    // set app name
    externs.setAppName (getTdsSettings().getAppName ());
    // override environment
    String configEnvironment = AppSettingsHelper.get ("Environment");
    if (!StringUtils.isEmpty (configEnvironment)) {
      externs.setEnvironment (configEnvironment);
    }
    // override client style path
    String configClientStylePath = AppSettingsHelper.get ("ClientStylePath");
    if (!StringUtils.isEmpty (configClientStylePath)) {
      externs.setClientStylePath (configClientStylePath);
    }
    synchronized (this) {
      _externs = externs;
    }
  }
  
  protected void loadPTSetup () throws ReturnStatusException {
    PTSetup ptSetup = null;
    
    try (SQLConnection connection = getSQLConnection ()) {
      SingleDataResultSet results = _studentDll.T_GetPTSetup_SP(connection, getTdsSettings().getClientName());
      Iterator<DbResultRecord> records = results.getRecords ();
      if (records.hasNext ()) {
        DbResultRecord record = records.next ();
        if (record.hasColumn ("loginAs")) {
          // record.setFixNulls (true);
          ptSetup = new PTSetup ();
          ptSetup.setLoginAs (record.<String> get ("loginAs"));
          ptSetup.setFirstName (record.<String> get ("firstname"));
          ptSetup.setSessionKey (record.<UUID> get ("SessionKey"));
          ptSetup.setSessionID (record.<String> get ("SessionID"));
        }
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    synchronized (this) {
      _ptSetup = ptSetup;
    }
  }

  protected void loadLoginRequirements () throws ReturnStatusException {

    List<TesteeAttributeMetadata> testeeAttributeMetadata = new ArrayList<TesteeAttributeMetadata> ();

    try (SQLConnection connection = getSQLConnection ()) {
      //SingleDataResultSet results = executeStatement (connection, CMD_GET_LOGIN_REQUIREMENTS, parametersQuery, false).getResultSets ().next ();
    	SingleDataResultSet results = _studentDll.T_TesteeAttributeMetadata_SP(connection, getTdsSettings().getClientName());
      ReturnStatusException.getInstanceIfAvailable (results);
      Iterator<DbResultRecord> records = results.getRecords ();
      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        TesteeAttributeMetadata metadataRecord = new TesteeAttributeMetadata ();
        metadataRecord.setId (record.<String> get ("TDS_ID"));
        metadataRecord.setRtsName (record.<String> get ("RTSName"));
        metadataRecord.setType ( record.<String> get ("Type"));
        metadataRecord.setLabel (record.<String> get ("Label"));
        metadataRecord.setSortOrder (record.<Integer> get ("SortOrder"));
        metadataRecord.setAtLogin (record.<String> get ("atLogin"));
        metadataRecord.setLatencySite (record.<Boolean> get ("isLatencySite"));
        testeeAttributeMetadata.add (metadataRecord);
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    Collections.sort (testeeAttributeMetadata, new Comparator<TesteeAttributeMetadata> ()
    {
      /*
       * (non-Javadoc)
       * 
       * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
       */
      @Override
      public int compare (TesteeAttributeMetadata o1, TesteeAttributeMetadata o2) {
        return o1.getSortOrder () - o2.getSortOrder ();
      }
    });
    // loginRequirements.Sort((R1, R2) => R1.SortOrder.CompareTo(R2.SortOrder));

    // Interlocked.Exchange(ref _loginRequirements, loginRequirements);
    synchronized (this) {
      _testeeAttributeMetadata = testeeAttributeMetadata;
    }
  }

  // just call this once

  protected void loadSecureBrowserRequired() throws ReturnStatusException {
    boolean isSecureBrowserRequired = true;

    try (SQLConnection connection = getSQLConnection ()) {

      isSecureBrowserRequired = _studentDll._SecureBrowserRequired_FN(connection, getTdsSettings().getClientName());

    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    // Interlocked.Exchange(ref _forbiddenApps, forbiddenApps);
    synchronized (this) {
      _isSecureBrowserRequired = isSecureBrowserRequired;
    }
  }

  protected void loadForbiddenApps () throws ReturnStatusException {

    ForbiddenApps forbiddenApps = new ForbiddenApps ();

    try (SQLConnection connection = getSQLConnection ()) {

      //MultiDataResultSet resultSets = executeStatement (connection, CMD_GET_FORBIDDEN_APPS, parametersQuery, false);
      MultiDataResultSet resultSets = _studentDll.GetForbiddenApps_SP(connection, getTdsSettings().getClientName());

      Iterator<SingleDataResultSet> results = resultSets.getResultSets ();
      // first expected result set
      if (results.hasNext ()) {

        SingleDataResultSet firstResultSet = results.next ();
        ReturnStatusException.getInstanceIfAvailable (firstResultSet);
        Iterator<DbResultRecord> firstRecords = firstResultSet.getRecords ();
        while (firstRecords.hasNext ()) {
            DbResultRecord record = firstRecords.next ();
            forbiddenApps.addAgentOS (record.<String> get ("AgentOS"), record.<String> get ("OS_ID"));
        }
        
        if (results.hasNext ()) {
          SingleDataResultSet secondResultSet = results.next ();
          Iterator<DbResultRecord> records = secondResultSet.getRecords ();
          while (records.hasNext ()) {
        	DbResultRecord record = records.next ();
            ForbiddenApps.Process process = forbiddenApps.new Process ();
            process.setOs (record.<String> get ("OS_ID"));
            process.setName (record.<String> get ("ProcessName").toLowerCase ());
            process.setDescription (record.<String> get ("ProcessDescription"));
            forbiddenApps.addApplication (process.getOs (), process);
          }
        }
        if (results.hasNext ()) {
          SingleDataResultSet thirdResultSet = results.next ();
          Iterator<DbResultRecord> records = thirdResultSet.getRecords ();
          while (records.hasNext ()) {
        	DbResultRecord record = records.next ();
            ForbiddenApps.ExcludedSchool excludedSchool = forbiddenApps.new ExcludedSchool ();
            excludedSchool.setDistrictName (record.<String> get ("DistrictName"));
            excludedSchool.setDistrictID (record.<String> get ("DistrictID"));
            excludedSchool.setSchoolName (record.<String> get ("SchoolName"));
            excludedSchool.setSchoolID (record.<String> get ("SchoolID"));
            forbiddenApps.addExcludedSchool (excludedSchool);
          }
        }
      }

    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    // Interlocked.Exchange(ref _forbiddenApps, forbiddenApps);
    synchronized (this) {
      _forbiddenApps = forbiddenApps;
    }
  }

  protected void loadVoicePacks () throws ReturnStatusException {

    List<TTSVoicePack> voicePacks = new ArrayList<TTSVoicePack> ();
    
    try (SQLConnection connection = getSQLConnection ()) {
      //SingleDataResultSet results = executeStatement (connection, CMD_GET_VOICE_PACKS, parametersQuery, false).getResultSets ().next ();
      SingleDataResultSet results = _studentDll.T_GetVoicePacks_SP(connection, getTdsSettings().getClientName());
      ReturnStatusException.getInstanceIfAvailable (results);
      Iterator<DbResultRecord> records = results.getRecords ();
      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        TTSVoicePack voicePack = new TTSVoicePack ();
        voicePack.setOs (record.<String> get ("OS_ID"));
        voicePack.setName (record.<String> get ("VoicePackName"));
        voicePack.setPriority (record.<Integer> get ("Priority"));
        voicePack.setLanguageCode (record.<String> get ("LanguageCode"));
        voicePacks.add (voicePack);
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    synchronized (this) {
      _voicePacks = voicePacks;
    }
    // Interlocked.Exchange(ref _voicePacks, voicePacks);
  }

  protected void loadGlobalAccommodations () throws ReturnStatusException {

    AccList globalAccList = new AccList ();

    try (SQLConnection connection = getSQLConnection ()) {
      //SingleDataResultSet results = executeStatement (connection, CMD_GET_GLOBAL_ACC, parametersQuery, false).getResultSets ().next ();
      SingleDataResultSet results = _studentDll.IB_GlobalAccommodations_SP(connection, getTdsSettings().getClientName(), "StudentGlobal").getResultSets ().next ();
      ReturnStatusException.getInstanceIfAvailable (results);
      Iterator<DbResultRecord> records = results.getRecords ();

      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        Data accData = AccListParseData.parseData (record);
        // HACK: Skip loading non-functional accommodations
        if (!accData.isFunctional ())
          continue;
        globalAccList.add (accData);
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    synchronized (this) {
      _globalAccList = globalAccList;
    }
    // Interlocked.Exchange(ref _globalAccList, globalAccList);
  }

  /*
   * not using it protected void loadCLS ()
   * 
   * {
   * 
   * String appName = TDSSettings.getAppName ();
   * 
   * String environment = _externs.getEnvironment ();
   * 
   * String clientStylePath = _externs.getClientStylePath ();
   * 
   * // check if CLS environment is overridden
   * 
   * String overrideCLSEnvironment = AppSettingsHelper.get ("CLS.Environment");
   * 
   * if (!StringUtils.isEmpty (overrideCLSEnvironment))
   * 
   * {
   * 
   * environment = overrideCLSEnvironment;
   * 
   * }
   * 
   * CLSConfig clsConfig = new CLSConfig (ClientName, environment, appName,
   * clientStylePath, true);
   * 
   * // check if we need to override the cls config file name. by default //
   * Hoai-Anh's library looks into the
   * 
   * // "Projects/<ClientStylePath>/config" path. overrideCLSConfigFileName //
   * should be a parametrized path
   * 
   * // - the parameter will be replaced with the client name. String
   * overrideCLSConfigFile = AppSettingsHelper.get ("CLS.ConfigFile");
   * 
   * if (StringUtils.isEmpty (overrideCLSConfigFile))
   * 
   * {
   * 
   * clsConfig.LoadCLSConfig ();
   * 
   * }
   * 
   * else
   * 
   * {
   * 
   * // TODO: instead of Externs.ClientStylePath use Externs.ClientName
   * 
   * String alternateConfigFileName = String.format (overrideCLSConfigFile,
   * clientStylePath);
   * 
   * alternateConfigFileName = UrlHelper.MapPath (alternateConfigFileName);
   * 
   * clsConfig.LoadCLSConfig (alternateConfigFileName);
   * 
   * }
   * 
   * if (clsConfig.IsLoadedXML)
   * 
   * { synchronized (clsConfig) { _clsConfig = clsConfig; } //
   * Interlocked.Exchange(ref _clsConfig, clsConfig);
   * 
   * }
   * 
   * }
   */
  protected void loadBrowserRules () throws ReturnStatusException {
    String appName = getTdsSettings().getAppName ();
    List<BrowserRule> globalRules = new ArrayList<BrowserRule> ();
    Map<String, List<BrowserRule>> testRulesLookup = new HashMap<String, List<BrowserRule>>();

    try (SQLConnection connection = getSQLConnection ()) {

      //SingleDataResultSet results = executeStatement (connection, CMD_GET_BROWSER_WHITE_LIST, parametersQuery, false).getResultSets ().next ();
      SingleDataResultSet results = _commonDll.T_GetBrowserWhiteList_SP(connection, getTdsSettings().getClientName(), appName);

      ReturnStatusException.getInstanceIfAvailable (results);
      Iterator<DbResultRecord> records = results.getRecords ();

      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        
        // create browser rule
        BrowserRule browserRule = new BrowserRule ();
        browserRule.setPriority (record.<Integer> get ("Priority"));
        browserRule.setOsName (BrowserOS.getBrowserOsFromDbString (record.<String> get ("OSName")));
        browserRule.setOsMinVersion (record.<Float> get ("OSMinVersion").doubleValue ());
        browserRule.setOsMaxVersion (record.<Float> get ("OSMaxVersion").doubleValue ());
        browserRule.setArchitecture (record.<String> get ("HW_Arch"));
        browserRule.setName (record.<String> get ("BrowserName"));
        browserRule.setMinVersion (record.<Float> get ("BrowserMinVersion").doubleValue ());
        browserRule.setMaxVersion (record.<Float> get ("BrowserMaxVersion").doubleValue ());
        browserRule.setAction (BrowserAction.getBrowserActionFromStringCaseInsensitive (record.<String> get ("Action")));

        if (record.hasColumn ("MessageKey")) {
          browserRule.setMessageKey (record.<String> get ("MessageKey"));
        }

        // check the browser rule context
        String contextType = "GLOBAL";
        String context = "*";

        if (record.hasColumn("ContextType"))
        {
            contextType = record.<String> get ("ContextType"); // GLOBAL, TEST, SEGMENT
            context = record.<String> get ("Context"); // * or id
        }

        // put browser rule in the right bucket
        if (contextType.equals("GLOBAL"))
        {
            globalRules.add(browserRule);
        }
        else if (contextType.equals("TEST"))
        {
            if (!testRulesLookup.containsKey(context))
            {
                testRulesLookup.put(context, new ArrayList<BrowserRule>());
            }

            testRulesLookup.get(context).add(browserRule);
        }
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    synchronized (this) {
      _browserGlobalRules = globalRules;
      _browserTestRulesLookup = testRulesLookup;
    }
  }

  protected void loadNetworkDiagnostics () throws ReturnStatusException {
    String appName = getTdsSettings().getAppName ();
    List<NetworkDiagnostic> networkDiagnostics = new ArrayList<NetworkDiagnostic> ();
    try (SQLConnection connection = getSQLConnection ()) {

      //SingleDataResultSet results = executeStatement (connection, CMD_GET_NEGWORK_DOAGNOSTICS, parametersQuery, false).getResultSets ().next ();
      SingleDataResultSet results = _studentDll.T_GetNetworkDiagnostics_SP(connection, getTdsSettings().getClientName(), appName);

      ReturnStatusException.getInstanceIfAvailable (results);
      Iterator<DbResultRecord> records = results.getRecords ();

      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        NetworkDiagnostic netDiag = new NetworkDiagnostic ();
        netDiag.setTestLabel (record.<String> get ("TestLabel"));
        netDiag.setMinDataRateRequired (record.<Integer> get ("MinDataRateReqd"));
        netDiag.setAverageItemSize (record.<Integer> get ("AveItemSize"));
        netDiag.setResponseTime (record.<Integer> get ("ResponseTime"));
        networkDiagnostics.add (netDiag);
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    synchronized (this) {
      _networkDiagnostics = networkDiagnostics;
    }
  }

  protected void loadAppSettings () throws ReturnStatusException {
    String appName = getTdsSettings().getAppName ();
    Map<String, Object> appSettings = new HashMap<String, Object> ();
    
    try (SQLConnection connection = getSQLConnection ()) {
      //SingleDataResultSet results = executeStatement (connection, CMD_GET_APP_SETTINGS, parametersQuery, false).getResultSets ().next ();
      SingleDataResultSet results = _studentDll.T_GetApplicationSettings_SP(connection, getTdsSettings().getClientName(), appName);
      
      ReturnStatusException.getInstanceIfAvailable (results);
      Iterator<DbResultRecord> records = results.getRecords ();

      while (records.hasNext ()) {

        DbResultRecord record = records.next ();
        String name = record.<String> get ("Property");
        String type = record.<String> get ("Type");
        String value = record.<String> get ("Value");

        // get value
        Object obj;

        switch (type.toLowerCase ()) {
        case "boolean":
          obj = new Boolean (value).booleanValue ();
          break;
        case "integer":
          obj = Integer.parseInt (value);
          break;
        default:
          obj = value;
          break; // default also covers "string"
        }

        appSettings.put (name, obj);
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    synchronized (this) {
      _appSettings = appSettings;
    }
  }

  protected void loadComments () throws IOException {
     String clientPath = getClientPath (); 
     if (clientPath == null) return; 
     String path = clientPath + File.separator + "Config" + File.separator  + "comments.txt";

     List<String> comments = new ArrayList<String> (); 
     File file = new File (path);
     if (file.exists()) {
    	 BufferedReader commentsReader = new BufferedReader (new FileReader (file));
         String str; 
         while ((str = commentsReader.readLine ()) != null) { 
        	 comments.add (str); 
         } 
         commentsReader.close();
     }
     
     synchronized (this) { 
    	 _comments = comments; 
     }
  }
  
  /**
   * Get the fully qualified path to the client directory in projects.
   */
  public String getClientPath()
  {
      StringBuilder builder = new StringBuilder(_projectsPath).append(File.separator).append(_externs.getClientStylePath());
      return builder.toString();
  }

  @Override
  public void setServletContext(ServletContext servletContext) {
	  _projectsPath = servletContext.getRealPath("/Projects");
  }

  private void addClientParameter (SqlParametersMaps parametersQuery) {
    parametersQuery.put ("clientname", getTdsSettings().getClientName ());
  }
  
  private void loadItemScoringConfigs() throws ReturnStatusException {
      List<ItemScoringConfig> configs = new ArrayList<ItemScoringConfig>();
      
      try (SQLConnection connection = getSQLConnection ()) {
    	  SingleDataResultSet firstResultSet = _studentDll.T_GetItemScoringConfigs_SP(connection, getTdsSettings().getClientName());

    	  ReturnStatusException.getInstanceIfAvailable (firstResultSet);
    	  Iterator<DbResultRecord> records = firstResultSet.getRecords ();
    	  while (records.hasNext ()) {    		  
    		  DbResultRecord record = records.next ();
    		  ItemScoringConfig config = new ItemScoringConfig();
    		  config.setContext(record.<String> get ("Context"));
    		  config.setItemType(record.<String> get ("ItemType"));
    		  config.setEnabled(record.<Boolean> get ("Item_in"));

    		  Object priority = record.<Object> get ("Priority");
    		  if (priority instanceof Boolean) {
    			  if (((Boolean) priority)) config.setPriority (1);            		  
    			  else config.setPriority (0);
    		  }
    		  else if (priority instanceof Integer) {
    			  config.setPriority (((Integer) priority).intValue());
    		  }

    		  if (record.<String> get ("ServerUrl") != null) {
    			  config.setServerUrl(record.<String> get ("ServerUrl"));
    		  }
    		  configs.add (config);
    	  }
      } catch (SQLException e) {
    	  _logger.error (e.getMessage ());
    	  throw new ReturnStatusException (e);
      }
      synchronized (this) {
    	  _itemScoringConfigs = configs;
      }
  }
}
