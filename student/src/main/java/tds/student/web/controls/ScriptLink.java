/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.web.controls;

import java.io.IOException;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.apache.commons.lang.StringUtils;

import tds.student.web.dummy.ManifestSingleton;
import tds.student.web.dummy.ResourcesSingleton;
import AIR.Common.Web.Session.Server;

@FacesComponent (value = "ScriptLink")
public class ScriptLink extends UIComponentBase
{
  private String _source;

  public String getSource () {
    return _source;
  }

  public void setSource (String value) {
    this._source = value;
  }

  @Override
  public String getFamily () {
    // TODO Auto-generated method stub
    return "ScriptLink";
  }

  @Override
  public void encodeAll (FacesContext context) throws IOException
  {
    ResponseWriter output = context.getResponseWriter ();

    String url = Server.resolveUrl (_source);

    // cache ID, this is used to change URL's signature
    if (!StringUtils.isEmpty (ResourcesSingleton.getCacheId ().getName ()))
    {
      url += StringUtils.contains (url, '?') ? "&" : "?";
      // TODO Shajib/Shiva : ResourcesSingleton.getCacheId () returns null,
      // remove "1" when it returns valid value
      url += "cid=" + "1"/* ResourcesSingleton.getCacheId () */;
    }

    if (ManifestSingleton.getFileHash (this.getSource ()) != null)
    {
      url += StringUtils.contains (url, '?') ? "&" : "?";
      url += "chksum=" + ManifestSingleton.getFileHash (this.getSource ());
    }

    output.write ("<script ");
    // check if there is a type defined
    if (!this.getAttributes ().containsKey ("type"))
    {
      output.write (" type=\"text/javascript\" ");
    }
    // output.WriteAttribute("charset", "utf-8");
    output.write (" src=\"" + url + "\" ");
    for (String key : this.getAttributes ().keySet ())
    {
      output.write (key + "=\"" + this.getAttributes ().get (key) + "\" ");
    }
    output.write ("></script>\r\n");
  }
}
