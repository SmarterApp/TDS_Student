/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.singletons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import tds.student.tdslogger.TDSLogger;
import AIR.Common.TDSLogger.ITDSLogger;
import TDS.Shared.Exceptions.ReturnStatusException;
import TDS.Shared.Exceptions.RuntimeReturnStatusException;

/**
 * @author temp_rreddy
 * 
 */
@Component
// just being explicit
@Scope ("singleton")
public class ClientManager implements ApplicationContextAware
{
  @Autowired
  private ITDSLogger          					 _tdsLogger;

  private ApplicationContext                     _beansContext = null;
  private static final Logger                    _logger       = LoggerFactory.getLogger (ClientManager.class);
  // TODO mpatel/ Shiva - Find replacement for Action class in Java
  // public static Action<ClientSingleton> OnLoaded;
  private final Object                           _syncRoot     = new Object ();                                // for
  // locking
  private boolean                                _inLock       = false;

  private final HashMap<String, ClientSingleton> _clientLookup = new HashMap<String, ClientSingleton> ();

  public List<ClientSingleton> List () {
    List<ClientSingleton> list = new ArrayList<ClientSingleton> (_clientLookup.values ());
    // return _clientLookup.Values.ToList();
    return list;
  }

  @Override
  public void setApplicationContext (ApplicationContext applicationContext) throws BeansException {
    // TODO Auto-generated method stub
    _beansContext = applicationContext;
  }

  public ClientSingleton getClient (String clientName) throws RuntimeReturnStatusException {
    // check if valid client name
    if (StringUtils.isEmpty (clientName))
      return null;

    // if true then we loaded the clients data for this request
    boolean isLoaded = false;
    // check if client has already been loaded before
    ClientSingleton client = tryGetClient (clientName);
    // if the client is null then this is the first time client is being
    // requested
    if (client == null) {
      synchronized (_syncRoot) {
        try {
          // check if the client loading process is trying to
          // recursively load
          // client (only way you could enter this lock)
          if (_inLock)
            throw new RuntimeReturnStatusException ("The client loading procedure tried to recursively load client.");
          _inLock = true;

          // check again if client exists in case they were loaded
          // when locking
          // (double lock check)
          client = tryGetClient (clientName);

          // begin client loading process
          if (client == null) {
            // call SP's...
            client = load (clientName);

            // check if client was loaded successfully
            if (client != null) {
              // add client to lookup
              Set (client);
              isLoaded = true;
            }
          }
        } catch (Exception ex) {
          String error = String.format ("The client %s failed to load.", clientName);          
          _tdsLogger.configFatal(error, "getClient", ex);
          throw new RuntimeReturnStatusException (new ReturnStatusException (error));
        } finally {
          _inLock = false;
        }

        // if this was the first time the client was loaded then fire
        // event
        if (isLoaded)
          notifyLoaded (client);
      }
    }

    // if the client is still null here then it does not exist in the
    // session DB
    if (client == null) {
      String error = String.format ("The client %1$s could not be found.", clientName);
      throw new RuntimeReturnStatusException (new ReturnStatusException (error));
    }

    return client;
  }

  private ClientSingleton load (String clientName) throws Exception {
    // load client
    ClientSingleton client = _beansContext.getBean ("clientSingleton", ClientSingleton.class);
    // the clientName passed in here may not match what is in TDSSettings.
    client.setName (clientName);
    client.load ();

    return (client.getIsLoaded ()) ? client : null;
  }

  // TODO mpatel/ Shiva - Find replacement for Action class in Java
  private void notifyLoaded (ClientSingleton client) {
	  // According Shiva "noting to do in C#"
    /*
     * if (OnLoaded == null) return;
     * 
     * try { 
     * 	OnLoaded(client); 
     * } catch (Exception ex) {
     * //_logger.error(ex.getMessage(), ex); 
     * // SB-507 // TDSLogger.Config.Error(ex);
     * 	_tdsLogger.configError(ex.getMessage(), "getClient", ex));
     * }
     */
  }

  private void Set (ClientSingleton clientSingleton) {
    _clientLookup.put (clientSingleton.getName (), clientSingleton);
  }

  // / <summary>
  // / Do we have the client loaded
  // / </summary>
  public boolean contains (String clientName) {
    synchronized (_syncRoot) {
      return _clientLookup.containsKey (clientName);
    }
  }

  // / <summary>
  // / Gets loaded client.
  // / </summary>
  private ClientSingleton tryGetClient (String clientName) {
    // validate name
    if (StringUtils.isEmpty (clientName))
      return null;

    // check if we loaded client already
    ClientSingleton client;
    client = _clientLookup.get (clientName);
    return client;
  }

}
