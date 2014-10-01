/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.web.dummy;

import java.util.HashMap;
import java.util.Map;

import AIR.Common.Configuration.AppSetting;
import AIR.Common.Configuration.AppSettings;
import AIR.Common.Web.Session.Server;
import AIR.ResourceBundler.Xml.Resources;

public class ResourcesSingleton
{
  private static Object                       _syncRoot        = new Object ();

  private static final Map<String, Resources> _resourcesLookup = new HashMap<String, Resources> ();

  private static Resources get (String file)
  {
    Resources resources;
    resources = _resourcesLookup.get (file);
    return resources;
  }

  public static Resources load (String virtualPath)
  {
    Resources manifest = get (virtualPath);

    if (manifest == null)
    {
      synchronized (_syncRoot)
      {
        manifest = get (virtualPath);

        if (manifest == null)
        {
          /*
           * C# code filePath = HttpContext.Current.Server.MapPath(virtualPath);
           */

          // get the physical path corresponding to virtual path on the web
          // server
          String filePath = Server.mapPath (virtualPath);
          manifest = new Resources (filePath);
        }
      }
    }

    return manifest;
  }

  public static AppSetting<String> getCacheId () {
    return AppSettings.getString ("http.cache.id");
  }

  public static AppSetting<Boolean> getCacheValidate () {
    return AppSettings.getBoolean ("http.cache.validate");
  }

}
