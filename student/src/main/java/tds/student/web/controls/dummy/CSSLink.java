/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.web.controls.dummy;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import TDS.Shared.Exceptions.ReturnStatusException;
import tds.student.web.StudentSettings;
import tds.student.web.dummy.ManifestSingleton;
import tds.student.web.dummy.ResourcesSingleton;
import tds.student.web.handlers.MasterShellHandler;
import AIR.Common.Configuration.AppSetting;
import AIR.Common.Web.FacesContextHelper;
import AIR.Common.Web.Session.Server;

// TODO Shajib: Not sure whether following two lines should stay or not, so
// commented for now.
// [DefaultProperty("Text"),
// ToolboxData("<{0}:CSSLink runat=\"server\" href=\"\" media=\"screen\" type=\"text/css\" rel=\"stylesheet\" />")]

@FacesComponent (value = "CSSLink")
public class CSSLink extends UIComponentBase
{
  private static final Logger _logger = LoggerFactory.getLogger (CSSLink.class);

  private String              _href;

  // TODO Shiva
  /*
   * 
   * http://stackoverflow.com/questions/7167170/injecting-resources-into-uicomponent
   * -aka-does-cdi-work-here. cannot seem to inject StudentSettings as a
   * ManagedProperty. See the link above. check if it is possible to use
   * OmniFaces.
   */

  public CSSLink ()
  {
    init ();
  }

  private StudentSettings _studentSettings;

  @Override
  public void encodeAll (FacesContext context) throws IOException
  {
    ResponseWriter output = context.getResponseWriter ();
    // resolve url
    String url = null;
    String manifestKey = this.getHref (); // This is how the file name would be
                                          // listed in our MD5 manifest

    if (this._href.contains ("%s")) {
      // This is some client specific CSS. Get client path from externs
      String clientStylePath = null;
      // TODO Shajib/Shiva : following line incurs NullPointerException
      clientStylePath = _studentSettings.getClientStylePath ();
      url = Server.resolveUrl (String.format (this._href, clientStylePath));
      manifestKey = String.format (this._href, clientStylePath);
    } else {
      url = Server.resolveUrl (this._href);
    }

    // cache ID, this is used to change URL's signature
    if (!StringUtils.isEmpty (ResourcesSingleton.getCacheId ().getName ()))
    {
      url += url.contains ("?") ? "&" : "?";
      // TODO Shajib/Shiva : ResourcesSingleton.getCacheId () returns null,
      // remove "1" when it returns valid value
      url += "cid=" + "1"/* ResourcesSingleton.getCacheId () */;
    }

    if (ManifestSingleton.getFileHash (manifestKey) != null) {
      url += url.contains ("?") ? "&" : "?";
      url += "chksum=" + ManifestSingleton.getFileHash (manifestKey);
    }

    output.startElement ("link", null);// output.WriteBeginTag("link");

    output.writeAttribute ("href", url, null);

    // check if there is a media defined
    if ((this.getAttributes ().get ("media")) == null) {
      output.writeAttribute ("media", "screen", null);
    }

    if ((this.getAttributes ().get ("type")) == null) {
      output.writeAttribute ("type", "text/css", null);
    }

    if ((this.getAttributes ().get ("rel")) == null) {
      output.writeAttribute ("rel", "stylesheet", null);
    }

    for (String key : this.getAttributes ().keySet ()) {
      output.writeAttribute (key, this.getAttributes ().get (key), null);
    }

    // self closing tag
    output.endElement ("link");
    output.write ("\r\n");
  }

  @Override
  public String getFamily () {
    return "CSSLink";
  }

  public String getHref () {
    return _href;
  }

  public void setHref (String value) {
    this._href = value;
  }

  public void setStudentSettings (StudentSettings value)
  {
    _studentSettings = value;
  }

  public StudentSettings getStudentSettings ()
  {
    return _studentSettings;
  }

  // TODO Shiva @PostConstruct does not work either.
  // this used to be a public method. making it private as PostContruct did not
  // work either.
  private void init ()
  {
    _studentSettings = FacesContextHelper.getBean ("studentSettings", StudentSettings.class);
  }
}
