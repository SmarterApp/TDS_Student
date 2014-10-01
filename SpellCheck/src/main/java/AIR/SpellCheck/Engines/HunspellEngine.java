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
  
  @Autowired
  SpellEngineFactory _spellEngine;
  
  @Override
  public boolean CheckWord (String word) {
    boolean isCorrect = true;
    try {
      String languageCode = WebHelper.getQueryString ("lang");
      if (_spellEngine.getDictionary (languageCode).misspelled(word)) {
        isCorrect = false;
      }
      if (_spellEngine.getDictionary (languageCode).misspelled(word)) {
        isCorrect = false;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return isCorrect;
  }
  
  
  @Override
  public List<String> GetSuggestions (String word)  {
    List<String> suggestions = new ArrayList<String> ();
    try {
      String languageCode = WebHelper.getQueryString ("lang");
      if (_spellEngine.getDictionary (languageCode).misspelled(word)) {
        suggestions.addAll (_spellEngine.getDictionary (languageCode).suggest(word));
      }
      if (_spellEngine.getDictionary (languageCode).misspelled(word)) {
        suggestions.addAll (_spellEngine.getDictionary (languageCode).suggest(word));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    return suggestions;
  }

}
