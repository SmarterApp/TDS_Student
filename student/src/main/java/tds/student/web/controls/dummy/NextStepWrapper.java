/*******************************************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2016 American Institutes for Research
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at 
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 *******************************************************************************************************/

package tds.student.web.controls.dummy;

import java.io.IOException;
import javax.faces.component.FacesComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import org.apache.commons.lang3.StringUtils;
import tds.student.web.controls.TDSGenericControl;

/// <summary>
/// An JSF control for wrapping an action buttons.
/// </summary>
/// <remarks>
/// This contents of this tag should contain a collection of <tds:Button> tags.
/// </remarks>
@FacesComponent(value = "NextStepWrapper")
public class NextStepWrapper extends TDSGenericControl {
	/// <summary>
	/// A JSF control for wrapping an action buttons.
	/// </summary>
	/// <remarks>
	/// This contents of this tag should contain a collection of <tds:Button>
	/// tags.
	/// </remarks>
	private String headerName;

	private String headerText;

	private String bottomInstructionsText;

	private String headerID;

	private String headerKey;

	private String bottomInstructionsKey;

	public String getHeaderName() {
		return headerName;
	}

	public void setHeaderName(String headerName) {
		this.headerName = headerName;
	}

	public String getHeaderText() {
		return headerText;
	}

	public void setHeaderText(String headerText) {
		this.headerText = headerText;
	}

	public String getBottomInstructionsText() {
		return bottomInstructionsText;
	}

	public void setBottomInstructionsText(String bottomInstructionsText) {
		this.bottomInstructionsText = bottomInstructionsText;
	}

	public String getHeaderID() {
		return headerID;
	}

	public void setHeaderID(String headerID) {
		this.headerID = headerID;
	}

	// "Next Step:"
	public String getHeaderKey() {
		return "Sections.BottomHeader." + getHeaderName();
	}

	public void setHeaderKey(String headerKey) {
		this.headerKey = headerKey;
	}

	// i18n content key of Next Step
	public String getBottomInstructionsKey() {
		return "Sections.BottomInstructions." + getHeaderName();
	}

	public void setBottomInstructionsKey(String bottomInstructionsKey) {
		this.bottomInstructionsKey = bottomInstructionsKey;
	}

	/// <summary>
	/// Render the start of the <tds:NextStepWrapper> tag.
	/// </summary>
	@Override
	public void encodeBegin(FacesContext context) throws IOException {
		ResponseWriter writer = context.getResponseWriter();
		// action (start)
		writer.startElement("div", null); // <div
		writer.writeAttribute("class", "actions", null); // class="actions"
		// writer.Write(HtmlTextWriter.TagRightChar); // >

		// header h3 (start and end)
		renderHeader(writer);

		// instruction-bottom (start and end)
		renderInstructionsBottom(writer);

		// buttons-container (start)
		writer.startElement("div", null); // <div
		writer.writeAttribute("class", "buttons-container", null); // class="buttons-container"
		// writer.Write(HtmlTextWriter.TagRightChar); // >
	}

	/// <summary>
	/// Render the end of the <tds:NextStepWrapper> tag.
	/// </summary>
	@Override
	public void encodeEnd(FacesContext context) throws IOException {
		ResponseWriter writer = context.getResponseWriter();
		writer.endElement("div"); // buttons-container (end)
		writer.endElement("div"); // actions (end)
	}

	private void renderHeader(ResponseWriter writer) throws IOException {
		if (StringUtils.isEmpty(getHeaderName()) && StringUtils.isEmpty(getHeaderText()))
			return;

		if (StringUtils.isEmpty(getHeaderText())) {
			setHeaderText(getHeaderKey());
		}

		writer.startElement("h3", null); // <h3
		writer.writeAttribute("id", getHeaderID(), null); // id=""

		if (!StringUtils.isEmpty(getHeaderKey())) {
			writer.writeAttribute("i18n-content", getHeaderKey(), null); // i18n-content=""
		}

		// writer.Write(HtmlTextWriter.TagRightChar); // >
		writer.write("Next Step:"/* getHeaderText() */);
		writer.endElement("h3"); // </h3>
	}

	private void renderInstructionsBottom(ResponseWriter writer) throws IOException {
		if (StringUtils.isEmpty(getHeaderName()) && StringUtils.isEmpty(getBottomInstructionsText()))
			return;

		if (StringUtils.isEmpty(getBottomInstructionsText())) {
			setBottomInstructionsText(getBottomInstructionsKey());
		}

		writer.startElement("div", null); // <div
		writer.writeAttribute("class", "instructions-bottom", null); // class="instructions-bottom"

		if (!StringUtils.isEmpty(getBottomInstructionsKey())) {
			writer.writeAttribute("i18n-content", getBottomInstructionsKey(), null); // class="i18n-content"
		}

		// writer.write(HtmlTextWriter.TagRightChar); // >
		/*
		 * writer.write(
		 * "If the information is correct, choose <strong>Yes</strong>. If not, choose <strong>No</strong>."
		 * getBottomInstructionsText ( ) );
		 */
		writer.write(getBottomInstructionsText());
		writer.endElement("div"); // </div>
	}

}
