/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package AIR.SpellCheck.Engines;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import AIR.Common.Web.WebHelper;
import AIR.SpellCheck.ISpellEngine;
import AIR.SpellCheck.SpellEngineFactory;

/**
 * @author mpatel
 *
 */
@Component
public class HunspellEngine implements ISpellEngine
{
  private final static Logger LOG = LoggerFactory.getLogger(HunspellEngine.class);

  private final SpellEngineFactory spellEngineFactory;

  @Autowired
  public HunspellEngine(final SpellEngineFactory spellEngineFactory) {
    this.spellEngineFactory = spellEngineFactory;
  }

  @Override
  public boolean CheckWord(final String word) {
    final String languageCode = getLanguage();
    boolean isCorrect = true;
    try {
      if (spellEngineFactory.getDictionary(languageCode).misspelled(word)) {
        isCorrect = false;
      }
    } catch (final Exception e) {
      LOG.error("Unable to spell check word {} in language {}", word, languageCode, e);
    }
    return isCorrect;
  }
  
  
  @Override
  public List<String> GetSuggestions(final String word)  {
    final String languageCode = getLanguage();
    final List<String> suggestions = new ArrayList<> ();
    try {
      if (spellEngineFactory.getDictionary(languageCode).misspelled(word)) {
        suggestions.addAll(spellEngineFactory.getDictionary(languageCode).suggest(word));
      }
    } catch (final Exception e) {
      LOG.error("Unable to get spelling suggestions for word {} in language {}", word, languageCode, e);
    }
    
    return suggestions;
  }

  /**
   * Retrieve the language parameter from the request.
   *
   * @return The language code for the request.
   */
  String getLanguage() {
    return WebHelper.getQueryString("lang");
  }

}
