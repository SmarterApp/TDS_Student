/*
 * using System; using System.Collections.Generic; using System.IO; using
 * System.Xml.Linq;
 */
package AIR.ResourceBundler.Xml;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.apache.commons.lang3.StringUtils;

import AIR.Common.Utilities.Path;

public class Resources
{
  private String               _sourceDir;
  private String               _configFile;
  private Map<String, FileSet> _fileSets = new HashMap<String, FileSet> ();

  // / <summary>
  // /
  // / </summary>
  // / <param name="configurationFile">The configuration File.</param>
  // / <param name="sourceDirectory">The source directory, if not supplied,
  // all relative paths are assumed
  // / to be relative to location of configuration file.
  // / </param>
  public Resources (String configurationFile, String sourceDirectory) {
    _configFile = configurationFile;

    // save root path
    if (sourceDirectory != null) {
      _sourceDir = sourceDirectory;
    } else {
      _sourceDir = configurationFile.replace (Path.getFileName (configurationFile), "");
      this._sourceDir = this._sourceDir.substring (0, this._sourceDir.length () - 1);
    }
  }

  public Resources (String configurationFile) {
    _configFile = configurationFile;
    _sourceDir = configurationFile.replace (Path.getFileName (configurationFile), "");
    this._sourceDir = this._sourceDir.substring (0, this._sourceDir.length () - 1);
  }

  public void parse () throws JDOMException, IOException, ResourcesException {
    SAXBuilder builder = new SAXBuilder ();
    // todo Shiva/Sajib: the config file may be a class path resource.
    File xmlFile = new File (_configFile);
    Document document = (Document) builder.build (xmlFile);
    Element rootElement = document.getRootElement ();

    for (Element childEl : rootElement.getChildren ()) {
      String childName = childEl.getName ();
      if ("fileSet".equalsIgnoreCase (childName)) {
        parseFileSet (childEl);
      } else if ("import".equalsIgnoreCase (childName)) {
        parseImport (childEl);
      } else if ("remove".equalsIgnoreCase (childName))
      {
        parseRemove (childEl);
      }

    }
  }

  private void parseFileSet (Element fileSetEl) throws ResourcesException {
    FileSet fileSet = new FileSet (this);
    fileSet.parse (fileSetEl);
    _fileSets.put (fileSet.getName (), fileSet);
  }

  private void parseImport (Element importEl) throws JDOMException, IOException, ResourcesException
  {
    String importPath = resolveFile (importEl.getValue ());

    // parse external config file
    Resources resources = new Resources (importPath, _sourceDir);
    resources.parse ();

    for (Iterator<FileSet> iterator = resources.getFileSets (); iterator.hasNext ();)
    {
      FileSet resource = iterator.next ();
      _fileSets.put (resource.getName (), resource);
    }
  }

  private void parseRemove (Element excludeEl)
  {
    Attribute setAttrib = excludeEl.getAttribute ("set");
    String setName = (setAttrib != null) ? setAttrib.getValue () : null;

    // remove fileset globally
    if (!StringUtils.isEmpty (setName))
    {
      _fileSets.remove (setName);
    }
  }

  public String resolveFile (String file) {
    return Path.combine (_sourceDir, file);
  }

  public FileSet getFileSet (String id) {
    // if (String.isNullOrEmpty(id))
    if (StringUtils.equals (id, null) || StringUtils.equals (id, StringUtils.EMPTY))
      return null;
    return _fileSets.containsKey (id) ? _fileSets.get (id) : null;
  }

  public Iterator<FileSet> getFileSets () {
    return _fileSets.values ().iterator ();
  }

  public Iterator<FileSetInput> getFileInputs (String name)
      throws ResourcesException {
    FileSet fileSet = getFileSet (name);

    if (fileSet == null) {
      throw new ResourcesException (String.format ("The fileset name \"{0}\" could not be found.", name));
    }

    return fileSet.getFileInputs ();
  }

}