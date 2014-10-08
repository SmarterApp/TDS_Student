/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package AIR.SpellCheck.Engines;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/*import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.RuleMatch;*/

import AIR.SpellCheck.ISpellEngine;

/**
 * @author mpatel
 *
 */
public class LanguageToolEngine  implements ISpellEngine
{


  /* (non-Javadoc)
   * @see AIR.SpellCheck.ISpellEngine#CheckWord(java.lang.String)
   */
  @Override
  public boolean CheckWord (String word) {
    boolean isCorrect = true;
/*    try {
      System.out.println (Locale.getDefault ());
      System.out.println (Language.getLanguageForLocale (Locale.getDefault ()));
      JLanguageTool langTool = new JLanguageTool(Language.getLanguageForLocale (Locale.getDefault ()));
      langTool.activateDefaultPatternRules();
      List<RuleMatch> matches = langTool.check(word);
       
      for (RuleMatch match : matches) {
        System.out.println("Potential error at line " +
            match.getLine() + ", column " +
            match.getColumn() + ": " + match.getMessage());
        System.out.println("Suggested correction: " +
            match.getSuggestedReplacements());
        if(match.getRule ().isDictionaryBasedSpellingRule ()) {
          isCorrect = false;
          System.out.println("Potential error at line " +
              match.getLine() + ", column " +
              match.getColumn() + ": " + match.getMessage());
          System.out.println("Suggested correction: " +
              match.getSuggestedReplacements());
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }*/
    return isCorrect;
  }
  
  public static void main (String[] args) {
    System.out.println (new LanguageToolEngine ().GetSuggestions   ("wrod"));
  }
  
  /* (non-Javadoc)
   * @see AIR.SpellCheck.ISpellEngine#GetSuggestions(java.lang.String)
   */
  @Override
  public List<String> GetSuggestions (String word) {
//    List<String> suggestions = new ArrayList<String> ();
   /* try {
      JLanguageTool langTool = new JLanguageTool(Language.getLanguageForLocale (Locale.getDefault ()));
      langTool.activateDefaultPatternRules();
      List<RuleMatch> matches = langTool.check(word);
       
      for (RuleMatch match : matches) {
        if(match.getRule ().isDictionaryBasedSpellingRule ()) {
          System.out.println("Potential error at line " +
              match.getLine() + ", column " +
              match.getColumn() + ": " + match.getMessage());
          System.out.println("Suggested correction: " +
              match.getSuggestedReplacements());
          return match.getSuggestedReplacements();
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }*/
    return null;
  }

}
