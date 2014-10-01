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
import java.util.List;

/*************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2014 American Institutes for Research
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at 
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 *************************************************************************/

/**
 * @author mpatel
 * This represents a spelling error and suggested alternatives.
 */
public class SpellingError
{
      public String word;

      public List<String> suggestions;

      public SpellingError()
      {
      }

      public SpellingError(String word)
      {
          this.word = word;
          this.suggestions = new ArrayList <String>();
      }

      public String getWord () {
        return word;
      }

      public void setWord (String word) {
        this.word = word;
      }

      public List<String> getSuggestions () {
        return suggestions;
      }

      public void setSuggestions (List<String> suggestions) {
        this.suggestions = suggestions;
      }
      
      
}
