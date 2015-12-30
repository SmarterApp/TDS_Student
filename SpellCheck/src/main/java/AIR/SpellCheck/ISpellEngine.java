/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package AIR.SpellCheck;

import java.util.List;

/**
 * @author mpatel
 *  Spell check engine interface.
 */
public interface ISpellEngine
{
  /**
   * Check if a word is mispelled.
   * @param word - The word to check.
   * @return boolean - If this returns false then the word is misspelled.
   */
  boolean CheckWord(String word);
  
  /**
   * Get suggestions for a word.
   * @param word - The word to check.
   * @return List- Returns a list of other spelling suggestions.
   */
  List<String> GetSuggestions(String word);
  
}
