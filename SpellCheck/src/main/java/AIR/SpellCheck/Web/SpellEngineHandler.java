/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package AIR.SpellCheck.Web;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import AIR.Common.Web.ContentType;
import AIR.Common.Web.HttpHandlerBase;
import AIR.SpellCheck.ISpellEngine;
import AIR.SpellCheck.SpellCheckExtensions;
import AIR.SpellCheck.SpellingError;

/**
 * @author mpatel
 *
 */
@Controller
@Scope ("request")
public class SpellEngineHandler extends HttpHandlerBase
{
  @Autowired
  private ISpellEngine _spellEngine;


  /**
   *  Gets the words from the form post
   * @return
   */
  private String[] GetWords(String text)
  {
      String[] words = text.split(" ");
      return words;
  }

  /**
   * http://localhost/spellcheck/SpellEngine.ashx/Word?lang=en&word=test 
   * @return
   */
  @RequestMapping (value = "checkWord")
  @ResponseBody
  private String CheckWord(@RequestParam (value = "word", required = true) String word, 
      HttpServletResponse response)
  {
      boolean correct = _spellEngine.CheckWord(word);

      // set HTTP status
      int status = correct ? HttpStatus.FOUND.value () : HttpStatus.NOT_FOUND.value ();
      response.setStatus (status);
      // write message
      SetMIMEType(ContentType.Text);
      String result = correct ? "SPELLINGSUCCESS" : "SPELLINGERROR";
      return result;
  }

  @RequestMapping (value = "checkText",method= RequestMethod.POST)
  @ResponseBody
  private List<String> CheckText(@RequestParam (value = "text", required = true) String text)
  {
      List<String> misspelledWords = SpellCheckExtensions.GetMisspelledWords(_spellEngine,Arrays.asList (GetWords(text)));

      // write out spelling errors
      SetMIMEType(ContentType.Json);
      return misspelledWords;
  }

  // http://localhost/spellcheck/SpellEngine.ashx/Suggest?lang=en&word=test
  @RequestMapping (value = "getSuggestions")
  @ResponseBody
  private List<String> GetSuggestions (@RequestParam (value = "word", required = true) String word)
  {
      List<String> suggestions = _spellEngine.GetSuggestions(word);

      // write out suggestions
      SetMIMEType(ContentType.Json);
      return suggestions;
  }

  // http://localhost/spellcheck/SpellEngine.ashx/Text?lang=en
  @RequestMapping (value = "getErrors",method= RequestMethod.POST, produces = "application/json")
  @ResponseBody
  private List<SpellingError> GetErrors(@RequestParam (value = "text", required = true) String text)
  {
      // get spelling errors
      String[] words = GetWords(text);
      List<SpellingError> spellingErrors = SpellCheckExtensions.GetSpellingErrors(_spellEngine,Arrays.asList (words));

   // write out spelling errors
      //SetMIMEType(ContentType.Json);
      return spellingErrors;
  }
  
  @Override
  protected void onBeanFactoryInitialized () {
    // TODO Auto-generated method stub
    
  }
}
