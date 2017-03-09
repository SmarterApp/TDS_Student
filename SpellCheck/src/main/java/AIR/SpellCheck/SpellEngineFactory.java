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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mpatel
 *
 */
public class SpellEngineFactory
{
  private final static Logger LOG = LoggerFactory.getLogger(SpellEngineFactory.class);

  private final Map<String, Hunspell.Dictionary> _dictionaryMap = new HashMap<String, Hunspell.Dictionary>();
  
  private final List<String> _availableDictionaryLanguages;
  
  /**
   * Constructor
   * @param dictLanguageMap A map of language code to dictionary resource directory
   */
  public SpellEngineFactory (final Map<String, String> dictLanguageMap) {
    _availableDictionaryLanguages = new ArrayList<> (dictLanguageMap.keySet ());
    final Hunspell hunspell = Hunspell.getInstance();

    try {
      for(final String languageCode:_availableDictionaryLanguages) {
        final String dictLocation = getClass().getClassLoader()
            .getResource("dictionary//" +
                dictLanguageMap.get(languageCode) + "//" +
                dictLanguageMap.get(languageCode) + ".dic")
            .getPath();
        final Hunspell.Dictionary dictionary = hunspell.getDictionary(URLDecoder.decode(dictLocation.replace(".dic", ""),"UTF-8"));
        _dictionaryMap.put(languageCode.toUpperCase(), dictionary);
      }
    } catch (final Exception e) {
      LOG.error("Unable to initialize SpellEngineFactory", e);
    }
  }

  /**
   * Returns Hunspell Dictionary based on the languageCode 
   * Ex: ENU - for en_US; ESN - for es_MX
   *  
   * @param languageCode The language code
   * @return The Hunspell Dictionary for the given language
   * @throws IllegalArgumentException if the languageCode is blank or the dictionary does not exist
   */
  public Hunspell.Dictionary getDictionary(final String languageCode) throws IllegalArgumentException{
    if(StringUtils.isBlank(languageCode)) {
      throw new IllegalArgumentException("language parameter must be specified to get dictionary.");
    }
    if(!_dictionaryMap.containsKey(languageCode.toUpperCase())) {
      throw new IllegalArgumentException("SpellCheck dictionary is not available for labguage " +
          languageCode +
          ". Allowed Languages :" +
          _availableDictionaryLanguages);
    }
    return _dictionaryMap.get(languageCode.toUpperCase());
  }
  /**
   * Always returns US English Dictionary
   * @return The Hunspell dictionary for the english language
   * @throws IllegalArgumentException If the english dictionary does not exist
   */
  public Hunspell.Dictionary getDictionary() throws IllegalArgumentException {
    return _dictionaryMap.get("ENU");
  }
  
}
