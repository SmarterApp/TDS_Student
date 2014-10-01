/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import tds.itemrenderer.data.AccLookup;
import tds.student.services.data.TestOpportunity;
import tds.student.sql.data.AccommodationType;
import tds.student.sql.data.AccommodationValue;
import tds.student.sql.data.Accommodations;
import tds.student.sql.data.OpportunityInfo;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.data.TestConfig;
import tds.student.sql.data.TestSession;
import tds.student.sql.data.Testee;
import AIR.Common.TDSLogger.ITDSLogger;
import AIR.Common.Utilities.UrlEncoderDecoderUtils;
import AIR.Common.Web.Session.HttpContext;
import AIR.Common.Web.Session.MultiValueCookie;
import TDS.Shared.Security.TDSIdentity;

/**
 * @author Milan Patel
 * 
 */
public class StudentContext
{
	@Autowired
  private static ITDSLogger _tdsLogger;

  private final static String CACHE_PREFIX = "TDS_";
  private static final Logger _logger      = LoggerFactory.getLogger (StudentContext.class);

  public static void throwMissingException () throws StudentContextException {
    throw new StudentContextException ("Missing context info");
  }

  // TODO mpatel - implement this Method later based on Security framework
  public static boolean isAuthenticated () {
    return TDSIdentity.getCurrentTDSIdentity ().isAuthenticated ();
  }

  // / <summary>
  // / Check if a language is specified.
  // / </summary>
  public static boolean hasLanguage () {
    AccLookup accLookup = getAccLookup ();
    return (accLookup != null && accLookup.hasType ("Language"));
  }

  // / <summary>
  // / gets the language used for page internationalization (i18n)
  // / </summary>
  // / <returns>The selected language or ENU if nothing found.</returns>
  public static String getLanguage () {
    String language = null;

    // check if there are any test accommodations
    AccLookup accLookup = getAccLookup ();

    // if there are test accommodations then they override what the student
    // selected on the page
    if (accLookup != null) {
      language = accLookup.getCode ("Language");
      if (!StringUtils.isEmpty (language))
        return language;
    }

    return language != null ? language : "ENU";
  }

  public static void saveSession (TestSession session) {
    StudentCookie.setCookieData ("S_KEY", session.getKey ().toString ());
    StudentCookie.setCookieData ("S_ID", session.getId ());
    StudentCookie.setCookieData ("S_NAME", session.getName ());
    _logger.info("loadTest: StudentContext.saveSession sesskey: " + session.getKey ().toString ());
  }

  public static TestSession getSession () {
    if (!isAuthenticated ())
      return null;

    // check if session exists
    TestSession testSession = new TestSession ();
    testSession.setKey (UUID.fromString (StudentCookie.getCookieData ("S_KEY")));
    testSession.setId (StudentCookie.getCookieData ("S_ID"));
    testSession.setName (StudentCookie.getCookieData ("S_NAME"));

    // check if session exists
    // if (testSession.getKey () == Guid.Empty) return null;
    if (testSession.getKey () == null)
      return null;

    return testSession;
  }

  public static void saveTestee (Testee testee) {
    StudentCookie.setCookieData ("T_ID", testee.getId ());
    StudentCookie.setCookieData ("T_GR", testee.getGrade () != null ? testee.getGrade () : "");
    StudentCookie.setCookieData ("T_FN", testee.getFirstName () != null ? testee.getFirstName () : "");
    StudentCookie.setCookieData ("T_LN", testee.getLastName () != null ? testee.getLastName () : "");

    Map<String, String> secureValues = new HashMap<String, String> ();
    secureValues.put ("T_KEY", String.valueOf (testee.getKey ()));
    TDSIdentity identity = TDSIdentity.createNew (testee.getId (), secureValues);
    identity.setAuthenticated (true);
    identity.saveAuthCookie ();
  }

  public static Testee getTestee () {
    if (!isAuthenticated ())
      return null;

    Testee testee = new Testee ();

    String tKey = TDSIdentity.getCurrentTDSIdentity ().get ("T_KEY");

    if (tKey != null && !tKey.isEmpty ()) {
      testee.setKey (Long.parseLong (tKey));
    }

    testee.setId (StudentCookie.getCookieData ("T_ID"));
    testee.setGrade (StudentCookie.getCookieData ("T_GR"));
    testee.setFirstName (StudentCookie.getCookieData ("T_FN"));
    testee.setLastName (StudentCookie.getCookieData ("T_LN"));

    // check if testee exists
    return (testee.getKey () != 0) ? testee : null;
  }

  public static void saveOppInfo (String testKey, String testID, OpportunityInfo oppInfo) {
    // TODO mpatel
    
    _logger.info("loadTest: StudentContext.saveOppInfo testkey: " + testKey  +  " O_TID: " + testID + " O_KEY: "  +  oppInfo.getOppKey ().toString ());
    TDSIdentity.getCurrentTDSIdentity ().setAuthCookieValue ("O_TKEY", testKey);
    TDSIdentity.getCurrentTDSIdentity ().setAuthCookieValue ("O_TID", testID);
    TDSIdentity.getCurrentTDSIdentity ().setAuthCookieValue ("O_KEY", oppInfo.getOppKey ().toString ());
    TDSIdentity.getCurrentTDSIdentity ().saveAuthCookie ();

    StudentCookie.setCookieData ("O_BKEY", oppInfo.getBrowserKey ().toString ());
  }

  // / <summary>
  // / save the opened test opportunity subject.
  // / </summary>
  public static void saveSubject (String subject) {
    StudentCookie.setCookieData ("TI_S", subject);
  }

  // / <summary>
  // / get the opened test opportunity subject.
  // / </summary>
  public static String getSubject () {
    return StudentCookie.getCookieData ("TI_S");
  }

  // / <summary>
  // / save the opened test opportunity grade.
  // / </summary>
  public static void saveGrade (String grade) {
    StudentCookie.setCookieData ("TI_G", grade);
  }

  // / <summary>
  // / save the opened test opportunity grade.
  // / </summary>
  public static String getGrade () {
    return StudentCookie.getCookieData ("TI_G");
  }

  public static OpportunityInstance getOppInstance () {
    if (!isAuthenticated ())
      return null;
    UUID oppKey = UUID.fromString (TDSIdentity.getCurrentTDSIdentity ().get ("O_KEY"));
    UUID sessionKey = UUID.fromString (StudentCookie.getCookieData ("S_KEY"));
    UUID browserKey = UUID.fromString (StudentCookie.getCookieData ("O_BKEY"));

    // check if opp instance exists
    // if (oppKey == Guid.Empty || sessionKey == Guid.Empty || browserKey ==
    // Guid.Empty) return null;
    if (oppKey == null || sessionKey == null || browserKey == null)
      return null;

    _logger.info("loadTest: StudentContext.getOppInstance oppKey: " + oppKey.toString () +  "  sessionKey: "  + sessionKey.toString ());
    
    OpportunityInstance oppInstance = new OpportunityInstance (oppKey, sessionKey, browserKey);
    return oppInstance;
  }

  /**
   * Get the currently opened opportunity test key.
   * @return
   */
  public static String getTestKey () {
    return TDSIdentity.getCurrentTDSIdentity ().get ("O_TKEY");
  }
  
  /**
   *  Get the currently opened opportunity test id.
   * @return
   */
  public static String getTestID()
  {
      return TDSIdentity.getCurrentTDSIdentity ().get ("O_TID");
  }
  

  public static void saveTestConfig (TestConfig testConfig) {
    StudentCookie.setCookieData ("TC_R", testConfig.getRestart ());
    StudentCookie.setCookieData ("TC_L", testConfig.getTestLength ());
    StudentCookie.setCookieData ("TC_IT", testConfig.getInterfaceTimeout ());
    StudentCookie.setCookieData ("TC_CLT", testConfig.getContentLoadTimeout ());
    StudentCookie.setCookieData ("TC_RIT", testConfig.getRequestInterfaceTimeout ());
    StudentCookie.setCookieData ("TC_ORM", testConfig.getOppRestartMins ());
    StudentCookie.setCookieData ("TC_SP", testConfig.getStartPosition ());

    // new for 2012:
    StudentCookie.setCookieData ("TC_P", testConfig.getPrefetch ());
    StudentCookie.setCookieData ("TC_SBT", testConfig.isScoreByTDS ());
    StudentCookie.setCookieData ("TC_VC", testConfig.isValidateCompleteness ());
  }

  public static TestConfig getTestConfig () {
    TestConfig testConfig = new TestConfig ();
    testConfig.setTestLength (StudentCookie.getCookieDataInt ("TC_L"));
    testConfig.setInterfaceTimeout (StudentCookie.getCookieDataInt ("TC_IT"));
    testConfig.setContentLoadTimeout (StudentCookie.getCookieDataInt ("TC_CLT"));
    testConfig.setRequestInterfaceTimeout (StudentCookie.getCookieDataInt ("TC_RIT"));
    testConfig.setOppRestartMins (StudentCookie.getCookieDataInt ("TC_ORM"));
    testConfig.setStartPosition (StudentCookie.getCookieDataInt ("TC_SP"));
    testConfig.setPrefetch (StudentCookie.getCookieDataInt ("TC_P"));
    testConfig.setScoreByTDS (StudentCookie.getCookieDataBoolean ("TC_SBT"));
    testConfig.setValidateCompleteness (StudentCookie.getCookieDataBoolean ("TC_VC"));

    // check if test config exists
    return (testConfig.getTestLength () != 0) ? testConfig : null;
  }

  // / <summary>
  // / get the test opportunity information.
  // / </summary>
  // / <remarks>
  // / This is only available once you have started your test.
  // / </remarks>
  public static TestOpportunity getTestOpportunity () {
    if (!isAuthenticated ())
      return null;

    // get test opp from cache
    TestOpportunity testOpp = (TestOpportunity) getCacheData ("TestOpportunity");
    _logger.info("loadTest: StudentContext.getTestOpportunity TestOpportunity " + testOpp );
    if (testOpp != null) {
      return testOpp;
    }
    
    // get opportunity data
    OpportunityInstance oppInstance = getOppInstance ();
    if (oppInstance == null)
      return null;

    // get test key
    String testKey = getTestKey ();
    String testID = getTestID();
    
    // check if test key exists
    if (testKey == null || testKey.isEmpty ())
      return null;

    // get test config
    TestConfig testConfig = getTestConfig ();
    if (testConfig == null)
      return null;

    // create testopp
    testOpp = new TestOpportunity (oppInstance, testKey,testID, "ENU", testConfig);

    // TODO Remove Later Test Data
    /*
     * oppInstance = new OpportunityInstance
     * (UUID.fromString("c778d1dd-3d2f-4e27-b093-a8f469b54b5b"),
     * UUID.fromString("0e0cdd71-df1f-497c-b895-7ac88be58a8c"),
     * UUID.fromString("fdd7ea11-f15c-4eec-b4cd-f0f17c773cb8")); testKey =
     * "(Hawaii)ASISample-Reading-8-Spring-2013-2013"; testOpp = new
     * TestOpportunity (oppInstance, testKey, "ENU", testConfig);
     */

    // save test opp to cache
    setCacheData ("TestOpportunity", testOpp);

    return testOpp;
  }

  private static final String ACC_COOKIENAME = "TDS-Student-Accs";

  public static void saveSegmentsAccLookupAccommodations (List<AccLookup> segmentsAccommodations) {
    // clear any cached accommodations
    clearCacheData ("Accommodations");

    // set accommodations to cookie as JSON
    setToCookie (ACC_COOKIENAME, segmentsAccommodations, true, true);
  }

  public static void saveSegmentsAccommodations (List<Accommodations> segmentsAccommodations) {
    // saveSegmentsAccLookupAccommodations(segmentsAccommodations.Select(A =>
    // A.GetCollection()));
    List<AccLookup> accLookUpList = new ArrayList<AccLookup> ();
    CollectionUtils.collect (segmentsAccommodations, new Transformer ()
    {
      @Override
      public Object transform (Object input) {
        Accommodations accommodations = (Accommodations) input;
        AccLookup lookup = new AccLookup (accommodations.getPosition (), accommodations.getId ());

        for (AccommodationType accType : accommodations.getTypes ()) {
          for (AccommodationValue accValue : accType.getValues ()) {
            lookup.add (accType.getName (), accValue.getCode ());
          }
        }

        return lookup;
      }
    }, accLookUpList);
    saveSegmentsAccLookupAccommodations (accLookUpList);
  }

  // / <summary>
  // / get the current accommodations for global or if available a list for the
  // test/segments.
  // / </summary>
  @SuppressWarnings ("unchecked")
  public static List<AccLookup> getAccommodationsList () {
    // get accs from cache
    List<AccLookup> lookupList = (List<AccLookup>) getCacheData ("Accommodations");

    if (lookupList != null)
      return lookupList;

    // get accs from cookie
    //TODO EF: we cannot find why generic type does not work in getFromCookie
    // result we get back is list on LinkedHashMap.
    // As a way around we inlined code here and
    // used TypeReference<List<AccLookup>>
    //lookupList = (List<AccLookup>) getFromCookie (ACC_COOKIENAME, true, true);

    MultiValueCookie cookie = StudentCookie.getCookieByName (ACC_COOKIENAME);
    if (cookie == null || (cookie.getValue () != null && cookie.getValue ().isEmpty ())) {
      return lookupList;
    }

    // decode String
    String data = cookie.getValue ();
    data = UrlEncoderDecoderUtils.decode (data);

    // deserialize into object
    try {
      ObjectMapper mapper = new ObjectMapper ();
      //result = (T) mapper.readValue (data, Object.class);
      lookupList = mapper.readValue (data, new TypeReference<List<AccLookup>>() { });     
    } catch (Exception ex) {
      _logger.error (ex.getMessage (), ex);
    }
      
    // check if there are any segment accs
    if (lookupList != null && lookupList.size () > 1) {
      for (int i = 1; i < lookupList.size (); i++) {
        AccLookup segmentAccs = lookupList.get (i);

        // for each segment accs we need to start with the test accs as the base
        AccLookup testAccs = lookupList.get (0).clone (segmentAccs.getPosition (), segmentAccs.getId ());

        // then we will replace any of the test accs types with the segment accs
        testAccs.replaceWith (segmentAccs);

        // now swap in the new accs
        lookupList.set (i, testAccs);
      }
    }

    // save accommodations to cache
    if (lookupList != null) {
      setCacheData ("Accommodations", lookupList);
    }

    return lookupList;
  }

  // / <summary>
  // / get the current primary accommodations (global or test).
  // / </summary>
  public static AccLookup getAccLookup () {
    List<AccLookup> accommodationsList = getAccommodationsList ();

    if (accommodationsList != null && accommodationsList.size () > 0) {
      return accommodationsList.get (0);
    }

    return null;
  }

  private static void clearCacheData (String key) {
    String cacheKey = CACHE_PREFIX + key;
    if (HttpContext.getCurrentContext ().containsItem (cacheKey)) {
      HttpContext.getCurrentContext ().removeItem (cacheKey);
    }
  }

  private static <T> void setCacheData (String key, T value) {
    if (HttpContext.getCurrentContext () == null)
      return;

    String newKey = CACHE_PREFIX + key;
    HttpContext.getCurrentContext ().setItem (newKey, value);
  }

  @SuppressWarnings ("unchecked")
  private static <T> T getCacheData (String key) // where T : class
  {
    if (HttpContext.getCurrentContext () == null)
      return null;
    return (T) HttpContext.getCurrentContext ().getItem (CACHE_PREFIX + key);
  }

  /***
   * Retrieve JSON object from a cookie.
   * 
   * @param cookieName
   *          - name of cookie
   * @param urlDecode
   *          - true to enable url decoding of the String
   * @param fromJson
   *          - true if the String in cookie is Json stringified
   * @return
   */
  @SuppressWarnings ("unchecked")
  private static <T> T getFromCookie (String cookieName, boolean urlDecode, boolean fromJson) {
    T result = null;
     
    // read from cookie
    MultiValueCookie cookie = StudentCookie.getCookieByName (cookieName);
    if (cookie == null || (cookie.getValue () != null && cookie.getValue ().isEmpty ())) {
      return result;
    }

    // decode String
    String data = cookie.getValue ();
    if (urlDecode) {
      // data = UrlEncoderDecoderUtils.decode (data);
    }

    // deserialize into object
    if (fromJson) {
      try {
        ObjectMapper mapper = new ObjectMapper ();
        //result = (T) mapper.readValue (data, Object.class);
        result = mapper.readValue (data, new TypeReference<T>() { });		  
      } catch (Exception ex) {
    	_tdsLogger.applicationError(ex.getMessage (), "getFromCookie", null, ex); 
      }
    } else {
      result = (T) data;
    }

    return result;
  }

  private static <T> void setToCookie (String cookieName, T value, boolean urlDecode, boolean fromJson) {
    String data = "";
 
    // serialize into String
    if (fromJson) {
      try {
        ObjectMapper mapper = new ObjectMapper ();
        data = mapper.writeValueAsString (value);
      } catch (Exception e) {
        _logger.error (e.getMessage (), e);
      }
    } else {
      data = value.toString ();
    }

    // TODO Sajib: make sure you tell shiva to remove this hack once
    // AccommodationService has been fixed up.
    if (StringUtils.equalsIgnoreCase (ACC_COOKIENAME, cookieName))
    {
      if (StringUtils.isEmpty (data))
        data = "[]";
    }

    // encode String
    if (urlDecode) {
     //  data = UrlEncoderDecoderUtils.encode (data);
    }

    // save to cookie
    MultiValueCookie cookie = new MultiValueCookie (cookieName, data);
    HttpContext.getCurrentContext ().getCookies ().add (cookie);
  }
}
