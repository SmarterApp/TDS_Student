/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.singletons;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import TDS.Shared.Exceptions.RuntimeReturnStatusException;

import scoringengine.Scorer;
import scoringengine.TestCollection;
import tds.student.services.abstractions.AbstractConfigLoader;
import AIR.Common.Configuration.ConfigurationManager;

/**
 * @author temp_rreddy
 * 
 */
@Component
@Scope ("prototype")
public class ClientSingleton
{
  private String                       _clientName;
  private AbstractConfigLoader         _configLoader           = null;

  private boolean                      _loading                = false;
  private boolean                      _loaded                 = false;
  private final Object                 _testScorerLock         = new Object (); // for
                                                                                // locking
  // test scorer
  // loading
  private String                       _testScorerLoading      = null;         // test
                                                                                // that
                                                                                // //
                                                                                // is
  // currently
  // loading (null
  // means none)
  private boolean                      _conversionTablesLoaded = false;

  private boolean                      _loadConversionTablesWithClient;
  // A collection of tests used for scoring.
  private TestCollection               _testScorerCollection;

  public String getName () {
    return _clientName;
  }

  @Autowired
  public ClientSingleton (AbstractConfigLoader loader) {
    _configLoader = loader;
  }

  public void load () throws Exception {
    if (_loading)
      throw new Exception ("Client data is currently loading");
    if (_loaded)
      throw new Exception ("Client data is already loaded");

    _loading = true;

    loadInternal ();

    _loading = false;
    _loaded = true;
  }

  private void loadInternal () throws Exception {
    // load configs
    _configLoader.load ();

    // initialize test scoring
    InitScoring ();
  }

  public boolean getIsLoaded () {
    return _loaded;
  }

  public AbstractConfigLoader getConfig () {
    return _configLoader;
  }

  public String getItemBankConnectionString () throws Exception {
    if (ConfigurationManager.getConnectionStrings ("ITEMBANK_DB") != null) {
      return ConfigurationManager.getConnectionStrings ("ITEMBANK_DB");
    }

    if (ConfigurationManager.getConnectionStrings ("SESSION_DB") == null) {
      throw new Exception ("There is no valid item bank or session DB connection string in the web.config.");
    }

    // TODO mpatel - replace SqlConnectionStringBuilder in Java
    /*
     * SqlConnectionStringBuilder connectionBuilder = new
     * SqlConnectionStringBuilder
     * (ConfigurationManager.getConnectionStrings("SESSION_DB"));
     * 
     * AppExterns appExterns = _configLoader.getExterns ();
     * 
     * if (appExterns == null) { throw new Exception (
     * "Could not get the connection string from the session DB because the externs table is empty."
     * ); }
     * 
     * // modify session DB connection strings table to what is specificed in //
     * externs connectionBuilder.InitialCatalog = appExterns.getItemBankDB ();
     * return connectionBuilder.ConnectionString;
     */
    return "";
  }

  public Scorer CreateTestScorer () {
    return new Scorer (_testScorerCollection);
  }
  // / <summary>
  // / Initialize the scoring service with data.
  // / </summary>
  private void InitScoring () throws Exception {
    // Create test scoring collection
    String ibConnectionString = getItemBankConnectionString ();
    _testScorerCollection = new TestCollection (ibConnectionString, "TDS", false);
  }

  /**
   * @return the _loadConversionTablesWithClient
   */
  public boolean isLoadConversionTablesWithClient () {
    return _loadConversionTablesWithClient;
  }

  /**
   * @param _loadConversionTablesWithClient
   *          the _loadConversionTablesWithClient to set
   */
  public void setLoadConversionTablesWithClient (boolean _loadConversionTablesWithClient) {
    this._loadConversionTablesWithClient = _loadConversionTablesWithClient;
  }

  // / <summary>
  // / Check if the test is not currently loading and exists in the scorer
  // collection.
  // / </summary>
  // / <param name="testKey"></param>
  // / <returns></returns>
  public boolean hasTestForScoring (String testKey) {
    // BUG: HasTest can report true but the test can still be in process of
    // loading
    return (_testScorerLoading != testKey) && _testScorerCollection.hasTest (testKey);
  }

  public void loadTestForScoring (String testKey) {
    // check if test is loading/loaded
    if (hasTestForScoring (testKey))
      return;

    // lock any other test for this client from loading
    synchronized (_testScorerLock) {
      // double check if test is loading/loaded
      if (hasTestForScoring (testKey))
        return;

      // check if conversion tables have been loaded
      if (!_conversionTablesLoaded) {
        // load the conversion tables
        if (_loadConversionTablesWithClient) {
          _testScorerCollection.loadConversionTables (_clientName);
        } else {
          _testScorerCollection.loadConversionTables ();
        }
        _conversionTablesLoaded = true;
      }

      // mark this test as loading
      _testScorerLoading = testKey;

      try {
        // load test scoring data for this test
        _testScorerCollection.loadTest (testKey);
      } catch (Exception ex) {
        // if the test failed to load then make sure to remove it
        if (_testScorerCollection.hasTest (testKey)) {
          _testScorerCollection.deleteTest (testKey);
        }

        throw ex;
      } finally {
        // unmark this test as loading
        _testScorerLoading = null;
      }
    }
  }

  synchronized void setName (String value) {
    if (_loaded)
      throw new RuntimeReturnStatusException ("Reseting client name on the configuration singleton not allowed after load");
    _clientName = value;
    _configLoader.getTdsSettings ().setClientName (value);
    
  }

}
