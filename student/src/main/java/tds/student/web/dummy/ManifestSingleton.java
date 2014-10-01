/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.web.dummy;

public class ManifestSingleton
{

  // / <summary>
  // / Looks up the cached data to see if we know the MD5 sum for that resource.
  // Returns null if not found
  // / </summary>
  // / <returns></returns>
  // TODO Shiva/Sajib.
  public static String getFileHash (String path)
  {
    return "manifest_is_na";
  }

  /** The current mode that the manifest is in.
  */
  public enum ManifestMode
  {
      /** Always turned off.
      */
      Disabled,

      /** Always turned on.
      */
      Enabled,

      /** Only enabled if the secure browser is used (or any other logic).
      */
      Auto
  }
  
}
