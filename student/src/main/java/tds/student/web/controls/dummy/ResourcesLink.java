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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;

import org.apache.commons.lang.StringUtils;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tds.student.web.DebugSettings;
import tds.student.web.controls.ScriptLink;
import tds.student.web.dummy.ResourcesSingleton;
import AIR.Common.Utilities.Path;
import AIR.Common.Web.UrlHelper;
import AIR.Common.Web.Session.Server;
import AIR.Common.Web.taglib.ClientScript;
import AIR.ResourceBundler.Xml.FileSet;
import AIR.ResourceBundler.Xml.FileSetInput;
import AIR.ResourceBundler.Xml.Resources;
import AIR.ResourceBundler.Xml.ResourcesException;

@FacesComponent (value = "ResourcesLink")
public class ResourcesLink extends UIComponentBase
{
  private static final Logger _logger = LoggerFactory.getLogger (ResourcesLink.class);
  private String              _file   = null;
  private String              _name   = null;

  public String getFile () {
    return _file;
  }

  public void setFile (String value) {
    _file = value;
  }

  public String getName () {
    return _name;
  }

  public void setName (String value) {
    _name = value;
  }

  @Override
  public String getFamily () {
    return "ResourcesLink";
  }

  @Override
  public void encodeAll (FacesContext context) throws IOException {
    Resources resources = ResourcesSingleton.load (this.getFile ());

    if (resources == null)
      return;
    
    //TODO Shiva/Sajib: This parse seems out of place. We could not figure out 
    //where in the .NET code is the parse called.
    // parse the resource file
    try {
      resources.parse ();
    } catch (JDOMException e) {
      // TODO Auto-generated catch block
      e.printStackTrace ();
      _logger.error ("Error encoding all", e);
    } catch (ResourcesException e) {
      // TODO Auto-generated catch block
      e.printStackTrace ();
      _logger.error ("Error encoding all", e);
    }
    // get base url for manifest
    // ~/Scripts/scripts_student.xml --> ~/scripts/
    String scriptLinkUrl = getFile ().replace (Path.getFileName (this.getFile ()), "");

    FileSet fileSet = resources.getFileSet (this.getName ());

    // check if ASP.NET debugging is enabled
    if (DebugSettings.isDebuggingEnabled ())
    {
      // if we are debugging then add each resource file separately
      for (Iterator<FileSetInput> iterator = fileSet.getFileInputs (); iterator.hasNext ();) {
        FileSetInput fileInput = iterator.next ();
        // ScriptLink used in .NET code
        ScriptLink scriptLink = new ScriptLink ();
        scriptLink.setSource ( UrlHelper.buildUrl (scriptLinkUrl, fileInput.getPath ()));
        scriptLink.encodeAll (context);
      }
    }

    else { // since we are in release mode then add the resource combined file
           // only
      if (fileSet != null && !StringUtils.isEmpty (fileSet.getOutput ())) {
        ScriptLink scriptLink = new ScriptLink ();
        scriptLink.setSource (UrlHelper.buildUrl (scriptLinkUrl, fileSet.getOutput ()));
        scriptLink.encodeAll (context);
      }
    }

    encodeChildren (context);
  }
}
