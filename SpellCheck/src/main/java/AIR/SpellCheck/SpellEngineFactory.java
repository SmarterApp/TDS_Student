/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package AIR.SpellCheck;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.URLDecoder;

import dk.dren.hunspell.Hunspell;

/**
 * @author mpatel
 *
 */
public class SpellEngineFactory
{

  private Hunspell _hunSpell;
  
  private Map<String, Hunspell.Dictionary> _dictionaryMap = new HashMap<String, Hunspell.Dictionary>();
  
  private List<String> _availableDictionaryLanguages;
  
  /**
   * 
   */
  public SpellEngineFactory (Map<String, String> dictLanguageMap) {
    _availableDictionaryLanguages = new ArrayList<> (dictLanguageMap.keySet ());
    _hunSpell = Hunspell.getInstance ();
    try {
      for(String languageCode:_availableDictionaryLanguages) {
        String dictLocation = getClass ().getClassLoader ().getResource  ("dictionary//"+dictLanguageMap.get (languageCode)+"//"+dictLanguageMap.get (languageCode)+".dic").getPath ();
        _dictionaryMap.put (languageCode.toUpperCase (),_hunSpell.getDictionary (URLDecoder.decode(dictLocation.replace (".dic", ""), "UTF-8")));
      }
    } catch (Exception e) {
       e.printStackTrace ();
    }
  }
  
  public Hunspell getHunSpell () {
    return _hunSpell;
  }

  /**
   * Returns Hunspell Dictionary based on the languageCode 
   * Ex: ENU - for en_US; ESN - for es_MX
   *  
   * @param language
   * @param country
   * @return
   * @throws IllegalArgumentException
   */
  public Hunspell.Dictionary getDictionary(String languageCode) throws IllegalArgumentException{
    if(languageCode == null || languageCode.isEmpty ()) {
      throw new IllegalArgumentException("language parameter must be specified to get dictionary.");
    }
    if(!_dictionaryMap.containsKey (languageCode.toUpperCase ())) {
      throw new IllegalArgumentException("SpellCheck dictionary is not available for labguage "+languageCode +". Allowed Languages :"+_availableDictionaryLanguages);
    }
    return _dictionaryMap.get (languageCode.toUpperCase ());
  }
  /**
   * Always returns US English Dictionary
   * @return
   * @throws IllegalArgumentException
   */
  public Hunspell.Dictionary getDictionary() throws IllegalArgumentException{
    return _dictionaryMap.get ("ENU");
  }
  
}
