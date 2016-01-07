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
import java.util.ArrayList;
import java.util.List;

import javax.faces.component.FacesComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.apache.commons.lang3.StringUtils;

@FacesComponent (value = "Button")
public class Button extends TDSGenericControl
{
  public enum ButtonType {
    Link, Button, Submit;

    public static ButtonType getButtonTypeCaseInsensitive (String value) {
      for (ButtonType t : ButtonType.values ()) {
        if (StringUtils.equalsIgnoreCase (t.name (), value))
          return t;
      }
      return Submit;
    }
  }

  private ButtonType _type;
  private String     _styleClass;
  private String     _tabIndex;
  private String     _text;
  private String     _key;

  public String getInputType () {
    return _type.name ();
  }

  public void setInputType (String value) {
    _type = ButtonType.getButtonTypeCaseInsensitive (value);
  }

  public String getStyleClass () {
    return _styleClass;
  }

  public void setStyleClass (String value) {
    this._styleClass = value;
  }

  public String getTabIndex () {
    return _tabIndex;
  }

  public void setTabIndex (String value) {
    this._tabIndex = value;
  }

  public String getText () {
    return _text;
  }

  public void setText (String value) {
    this._text = value;
  }

  public String getKey () {
    return _key;
  }

  public void setKey (String value) {
    this._key = value;
  }

  public Button () {
    // super(tag);
    _type = ButtonType.Button;
  }

  @Override
  public void encodeBegin (FacesContext context) throws IOException {
    ResponseWriter writer = context.getResponseWriter ();
    if (this._type == ButtonType.Link) {
      writer.startElement ("a", null);
      writer.writeAttribute ("href", "#", null);
      writer.writeAttribute ("tabindex", "0", null);
      if (_key != null)
        writer.writeAttribute ("i18n-button", _key, null);

      writeAttributes (writer);
      // writer.write(HtmlTextWriter.TagRightChar);

      writer.startElement ("span", null);
      // writer.write(HtmlTextWriter.TagRightChar);
      writeLabel (writer);
    } else if (this._type == ButtonType.Button || this._type == ButtonType.Submit) {
      writer.startElement ("span", null);
      writeAttributes (writer);
      // writer.Write(HtmlTextWriter.TagRightChar);

      writer.startElement ("span", null);
      // writer.Write(HtmlTextWriter.TagRightChar);

      writer.startElement ("button", null);

      if (this._type == ButtonType.Button) {
        writer.writeAttribute ("type", "button", null);
      } else if (this._type == ButtonType.Submit) {
        writer.writeAttribute ("type", "submit", null);
      }

      writer.writeAttribute ("tabindex", "0", null);
      if (_key != null)
        writer.writeAttribute ("i18n-button", _key, null);

      // writer.Write(HtmlTextWriter.TagRightChar);

      writeLabel (writer);
    }
  }

  @Override
  public void encodeEnd (FacesContext context) throws IOException {
    ResponseWriter writer = context.getResponseWriter ();
    if (this._type == ButtonType.Link) {
      writer.endElement ("span");
      writer.endElement ("a");
    } else if (this._type == ButtonType.Button || this._type == ButtonType.Submit) {
      writer.endElement ("button");
      writer.endElement ("span");
      writer.endElement ("span");
    }
  }

  @Override
  public String getFamily () {
    return "Button";
  }

  /*
   * This method was named as GetClass() in the .NET code.
   */
  private String getStyleClassInternal () {
    List<String> classes = new ArrayList<String> ();

    classes.add ("tds-button");

    if (!StringUtils.isEmpty (_styleClass)) {
      for (String className : _styleClass.split (" ")) {
        if (!StringUtils.isEmpty (className)) {
          classes.add (className);
        }
      }
    }

    return StringUtils.join (classes.toArray (), " ");
  }

  // / <summary>
  // / Write out generic attributes for all button types.
  // / </summary>
  private void writeAttributes (ResponseWriter writer) throws IOException {
    writer.writeAttribute ("id", this.getId (), null);
    writer.writeAttribute ("class", getStyleClassInternal (), null);
  }

  // / <summary>
  // / Write out label text.
  // / </summary>
  private void writeLabel (ResponseWriter writer) throws IOException {
    String label = null;

    if (!StringUtils.isEmpty (_text)) {
      label = _text;
    } else if (!StringUtils.isEmpty (_key)) {
      label = _key;
    }

    if (label != null) {
      writer.write (label);
    }
  }

}
