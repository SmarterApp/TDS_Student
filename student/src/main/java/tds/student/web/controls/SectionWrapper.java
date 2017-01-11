/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2016 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.web.controls;

import java.io.IOException;

import javax.faces.component.FacesComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.apache.commons.lang.StringUtils;

/// <summary>
/// A JSF control for wrapping a sections HTML.
/// </summary>
@FacesComponent (value = "SectionWrapper")
public final class SectionWrapper extends TDSGenericControl
{
  private boolean _aria;
  private boolean _shadow;
  private String  _headerID;
  private String  _describedBy;
  private String  _headerKey;
  private String  _headerText;
  private boolean _show;

	// SB-1505: Added following fields to sync with Proprietary Release 9.0
	// version
	private String _headerName;
	private String _topInstructionsKey;
	private String _topInstructionsText;

	public String getTopInstructionsKey() {
		return "Sections.TopInstructions." + _headerName;
	}

	public void setTopInstructionsKey(String topInstructionsKey) {
		this._topInstructionsKey = topInstructionsKey;
	}

	public String getTopInstructionsText() {
		return _topInstructionsText;
	}

	public void setTopInstructionsText(String topInstructionsText) {
		this._topInstructionsText = topInstructionsText;
	}

  public String getDescribedBy () {
    return _describedBy;
  }

  public void setDescribedBy (String value) {
    this._describedBy = value;
  }

  public boolean getAria () {
    return _aria;
  }

  public void setAria (boolean value) {
    this._aria = value;
  }

  public boolean getShadow () {
    return _shadow;
  }

  public void setShadow (boolean value) {
    this._shadow = value;
  }

  public String getHeaderKey () {
		return "Sections.TopHeader." + _headerName;
  }

  public void setHeaderKey (String value) {
    this._headerKey = value;
	}

	public String getHeaderName() {
		return _headerName;
	}

	public void setHeaderName(String headerName) {
		this._headerName = headerName;
  }

  public String getHeaderText () {
    return _headerText;
  }

  public void setHeaderText (String value) {
    this._headerText = value;
  }

  public boolean getShow () {
    return _show;
  }

  public void setShow (boolean value) {
    this._show = value;
  }

  public String getHeaderID () {
    return getId () + "Header";
  }

  @Override
  public String getFamily () {
    return "SectionWrapper";
  }

  // TODO Shajib/Shiva: tag parameter obstructs SectionWrapper working as it is
  // not passed from custom tag declaration
	public SectionWrapper() {
    // In .NET HtmlGenericControl class's constructor called from
    // TDSGenericControl with tag as parameter. UIComponentBase has no such
    // constructor
    setAria (false);
    setShadow (true);
    /* setTag (tag); */
  }

  @Override
  public void encodeBegin (FacesContext context) throws IOException {

    ResponseWriter writer = context.getResponseWriter ();
    // tds-ot-section
    writer.startElement ("div", null);
    writer.writeAttribute ("id", this.getId (), null);
    writer.writeAttribute ("class", "tds-ot-section", null);
    writer.writeAttribute ("style", this.getShow () ? "display:block;" : "display:none;", null);
    writer.writeAttribute ("aria-hidden", this.getShow () ? "false" : "true", null);

    // ot-shadowBox
    renderBeginShadow (writer);

    // header h2
    renderHeader (writer);

		// SB-1505: Added following method call to sync with Proprietary Release
		// 9.0
		// version
		renderInstructionsTop(writer);

  }

  @Override
  public void encodeEnd (FacesContext context) throws IOException {
    this.renderEndTag (context.getResponseWriter ());
  }

  public void renderBeginShadow (ResponseWriter writer) throws IOException {
    if (this.getShadow ()) {
      writer.startElement ("div", null);
      writer.writeAttribute ("class", "ot-shadowBox", null);
      // writer.endElement ("div");

      // ot-innerShadow
      writer.startElement ("div", null);
      writer.writeAttribute ("class", "ot-innerShadow", null);
      // writer.endElement ("div");
    }
  }

  public void renderHeader (ResponseWriter writer) throws IOException {

		if (StringUtils.isEmpty(this.getHeaderName()) && StringUtils.isEmpty(this.getHeaderText()))
			return;

    if (StringUtils.isEmpty (this.getHeaderText ())) {
      this.setHeaderText (getHeaderKey ());
    }

    // h2
    writer.startElement ("h1", null);
    writer.writeAttribute ("id", this.getHeaderID (), null);

    if (!StringUtils.isEmpty (this.getHeaderKey ())) {
      writer.writeAttribute ("i18n-content", this.getHeaderKey (), null);
    }
    writer.write (this.getHeaderText ());
    writer.endElement ("h1");
  }

  public void renderEndTag (ResponseWriter writer) throws IOException {
    renderEndShadow (writer);
    writer.endElement ("div"); // tds-ot-section
  }

  private void renderEndShadow (ResponseWriter writer) throws IOException {
    if (this.getShadow ()) {
      writer.endElement ("div"); // ot-innerShadow
      writer.endElement ("div"); // ot-shadowBox
    }
  }

	// SB-1505: Added following method to sync with Proprietary Release 9.0
	// version
	private void renderInstructionsTop(ResponseWriter writer) throws IOException {
		if (StringUtils.isEmpty(_headerName) && StringUtils.isEmpty(_topInstructionsText))
			return;

		if (StringUtils.isEmpty(_topInstructionsText)) {
			_topInstructionsText = getTopInstructionsKey();
		}

		writer.startElement("div", null);
		writer.writeAttribute("class", "instructions-top", null);

		if (!StringUtils.isEmpty(getTopInstructionsKey())) {
			writer.writeAttribute("i18n-content", getTopInstructionsKey(), null);
		}

		writer.write(_topInstructionsText);
		writer.endElement("div");
	}
}
