/*
 * using System; using System.Collections.Generic; using System.Linq; using
 * System.Text;
 */
package AIR.ResourceBundler.Xml;

public class ResourcesException extends Exception
{
  public ResourcesException () {
  }

  public ResourcesException (String message) {
    super (message);
  }

  public ResourcesException (String message, Exception innerException) {
    super (message, innerException);
  }
}