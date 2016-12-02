/*************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2016 American Institutes for Research
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at 
 * https://bitbucket.org/sbacoss/eotds/wiki/AIR_Open_Source_License
 *************************************************************************/

package tds.student.web.controls.dummy;

import java.io.IOException;

import javax.faces.component.FacesComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.apache.commons.lang3.StringUtils;

import tds.student.web.controls.TDSGenericControl;

/**
 * @author Vkesharia
 *
 */
/// <summary>
/// A JSF control for wrapping a sound check section HTML.
/// </summary>
@FacesComponent(value = "SoundCheckWrapper")
public class SoundCheckWrapper extends TDSGenericControl {

	private String id;
	private String headerId;
	private String headerKey;
	private String headerName;
	private String headerText;
	private String topInstructionsText;
	private String topInstructionsKey;
	private boolean show;

	/**
	 * @return the headerId
	 */
	public String getHeaderId() {
		return "section" + id + "Header";
	}

	/**
	 * @param headerId
	 *            the headerId to set
	 */
	public void setHeaderId(String headerId) {
		this.headerId = headerId;
	}

	/**
	 * @return the headerKey
	 */
	public String getHeaderKey() {
		return "Sections.TopHeader." + headerName;
	}

	/**
	 * @param headerKey
	 *            the headerKey to set
	 */
	public void setHeaderKey(String headerKey) {
		this.headerKey = headerKey;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the headerName
	 */
	public String getHeaderName() {
		return headerName;
	}

	/**
	 * @param headerName
	 *            the headerName to set
	 */
	public void setHeaderName(String headerName) {
		this.headerName = headerName;
	}

	/**
	 * @return the headerText
	 */
	public String getHeaderText() {
		return headerText;
	}

	/**
	 * @param headerText
	 *            the headerText to set
	 */
	public void setHeaderText(String headerText) {
		this.headerText = headerText;
	}

	/**
	 * @return the topInstructionsText
	 */
	public String getTopInstructionsText() {
		return topInstructionsText;
	}

	/**
	 * @param topInstructionsText
	 *            the topInstructionsText to set
	 */
	public void setTopInstructionsText(String topInstructionsText) {
		this.topInstructionsText = topInstructionsText;
	}

	/**
	 * @return the topInstructionsKey
	 */
	public String getTopInstructionsKey() {
		return "Sections.TopInstructions." + headerName;
	}

	/**
	 * @param topInstructionsKey
	 *            the topInstructionsKey to set
	 */
	public void setTopInstructionsKey(String topInstructionsKey) {
		this.topInstructionsKey = topInstructionsKey;
	}

	/**
	 * @return the show
	 */
	public Boolean getShow() {
		return show;
	}

	/**
	 * @param show
	 *            the show to set
	 */
	public void setShow(Boolean show) {
		this.show = show;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.faces.component.UIComponentBase#encodeBegin(javax.faces.context.
	 * FacesContext)
	 */
	@Override
	public void encodeBegin(FacesContext context) throws IOException {

		ResponseWriter writer = context.getResponseWriter();
		// tds-ot-section
		writer.startElement("div", null);
		writer.writeAttribute("id", this.id, null);
		writer.writeAttribute("class", "shadowBox small soundcheck", null);
		writer.writeAttribute("style", show ? "display:block;" : "display:none;", null);

		/* writer.Write(HtmlTextWriter.TagRightChar); */

		// header h1
		renderHeader(writer);

		// div.instructions-top
		renderInstructionsTop(writer);

	}

	@Override
	public void encodeEnd(FacesContext context) throws IOException {
		ResponseWriter writer = context.getResponseWriter();
		writer.endElement("div"); // tds-ot-section
	}

	private void renderHeader(ResponseWriter writer) throws IOException {

		if (StringUtils.isEmpty(getHeaderName()) && StringUtils.isEmpty(getHeaderText()))
			return;

		if (StringUtils.isEmpty(getHeaderText())) {
			setHeaderText(getHeaderKey());
		}

		// h1 (for 2014)
		writer.startElement("h1", null);
		writer.writeAttribute("id", getHeaderId(), null);

		if (!StringUtils.isEmpty(getHeaderKey())) {
			writer.writeAttribute("i18n-content", getHeaderKey(), null); // i18n-content=""
		}

		// writer.Write(HtmlTextWriter.TagRightChar);
		writer.write(getHeaderText());
		writer.endElement("h1");
	}

	private void renderInstructionsTop(ResponseWriter writer) throws IOException {
		if (StringUtils.isEmpty(getHeaderName()) && StringUtils.isEmpty(getTopInstructionsText()))
			return;

		if (StringUtils.isEmpty(getTopInstructionsText())) {
			setTopInstructionsText(getTopInstructionsKey());
		}

		writer.startElement("div", null);
		writer.writeAttribute("class", "instructions-top", null);

		if (!StringUtils.isEmpty(getTopInstructionsKey())) {
			writer.writeAttribute("i18n-content", getTopInstructionsKey(), null);
		}

		writer.write(getTopInstructionsText());
		writer.endElement("div");
	}

}
