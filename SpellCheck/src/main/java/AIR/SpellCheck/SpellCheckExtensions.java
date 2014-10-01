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
import java.util.HashSet;
import java.util.List;

/**
 * @author mpatel
 * Some common helper functions for working with a spell check engine.
 */
public class SpellCheckExtensions
{
  public static List<String> GetMisspelledWords(ISpellEngine spellEngine, List<String> words)
  {
      HashSet<String> spellingErrors = new HashSet<String>();

      // check each word
        for (String word :words)
        {
            // if the word has not already been spell checked and it has an error then add to spelling errors
            if (!spellingErrors.contains(word) && !spellEngine.CheckWord(word))
            {
                spellingErrors.add(word);
            }
        }

      return new ArrayList<String> (spellingErrors);
  }

  public static List<SpellingError> GetSpellingErrors( ISpellEngine spellEngine, List<String> words)
  {
      // get all mispelled words
      List<String> misspelledWords = GetMisspelledWords(spellEngine,words);

      // create spelling errors collection
      List<SpellingError> spellingErrors = new ArrayList <SpellingError>();

      for (String misspelledWord : misspelledWords)
      {
          SpellingError spellingError = new SpellingError(misspelledWord);
          spellingError.setSuggestions (spellEngine.GetSuggestions(misspelledWord));
          spellingErrors.add(spellingError);
      }

      return spellingErrors;
  }
}
