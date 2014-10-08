/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.web.handlers;

import java.io.OutputStream;
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang3.StringUtils;

import tds.student.services.data.ItemResponse;
import tds.student.services.data.PageGroup;
import tds.student.services.data.PageList;
import tds.student.services.data.TestOpportunity;
import tds.student.sql.data.ItemResponseUpdateStatus;
import tds.student.web.TestManager;
import AIR.Common.Web.Session.HttpContext;
import AIR.Common.xml.TdsXmlOutputFactory;

// / <summary>
// / Used to write out the results of a update response request (or possibly
// other types of requests, this was left to be generic)
// / </summary>
public class TestResponseWriter // : IDisposable
{
  private XMLStreamWriter _writer;

  public TestResponseWriter (OutputStream stream) throws XMLStreamException, FactoryConfigurationError
  {
    _writer = TdsXmlOutputFactory.newInstance ().createXMLStreamWriter (stream);
  }

  public void writeStart (int eventID) throws XMLStreamException
  {
    _writer.writeStartElement ("results");

    // TODO Shajib: In .net code server's machinename's hashcode is used
    int machineHash = HttpContext.getCurrentContext ().getServer ().hashCode ();
    String machineID = Integer.toString (machineHash);
    _writer.writeAttribute ("machineID", machineID);
    _writer.writeAttribute ("eventID", Integer.toString (eventID));

  }

  public void writeSummary (TestManager tm, TestOpportunity testOpp, boolean prefetched) throws XMLStreamException
  {
    _writer.writeStartElement ("summary");

    // write out test info (NOTE: if you modify any of xml notify load test tool
    // developer)
    PageList pageList = tm.GetVisiblePages ();
    _writer.writeAttribute ("testLength", Integer.toString (testOpp.getTestConfig ().getTestLength ()));
    _writer.writeAttribute ("testLengthMet", Boolean.toString (tm.IsTestLengthMet ()));
    _writer.writeAttribute ("testFinished", Boolean.toString (tm.IsTestLengthMet () && pageList.isAllCompleted ()));
    _writer.writeAttribute ("prefetched", Boolean.toString (prefetched));

    _writer.writeEndElement ();

  }

  public void writeTimestamps (long timeClientSent, long timeServerReceived, long timeServerCompleted) throws XMLStreamException
  {
    _writer.writeStartElement ("timestamps");

    // timestamp of when the message was sent from the client
    _writer.writeAttribute ("sent", Long.toString (timeClientSent));

    // timestamp of when the message was received from the server
    _writer.writeAttribute ("received", Long.toString (timeServerReceived));

    // timestamp of when the server was completed with the request
    _writer.writeAttribute ("completed", Long.toString (timeServerCompleted));

    _writer.writeEndElement ();
  }

  public void writeResponseUpdates (List<ItemResponseUpdateStatus> responseUpdates) throws XMLStreamException
  {
    _writer.writeStartElement ("updates");

    for (ItemResponseUpdateStatus responseStatus : responseUpdates)
    {
      _writer.writeStartElement ("response");
      _writer.writeAttribute ("position", Integer.toString (responseStatus.getPosition ()));
      _writer.writeAttribute ("status", responseStatus.getStatus ());
      _writer.writeAttribute ("reason", responseStatus.getReason ()!=null?responseStatus.getReason ():"");
      _writer.writeEndElement ();
    }

    _writer.writeEndElement ();
  }

  public void writeGroups (PageList groups) throws XMLStreamException
  {
    if (groups.size () == 0)
      return;

    _writer.writeStartElement ("groups");

    for (PageGroup group : groups)
    {
      // add group
      _writer.writeStartElement ("group");

      // add group properties
      if (!StringUtils.equals (group.getId (), null))
        _writer.writeAttribute ("id", group.getId ());

      _writer.writeAttribute ("page", Integer.toString (group.getNumber ()));
      _writer.writeAttribute ("numRequired", Integer.toString (group.getItemsRequired ()));

      // add segment properties
      ItemResponse firstResponse = group.getFirst ();
      _writer.writeAttribute ("segment", Integer.toString (firstResponse.getSegment ()));
      _writer.writeAttribute ("segmentID", firstResponse.getSegmentID ());

      // add responses
      for (ItemResponse response : group)
      {
        writeResponse (response);
      }

      _writer.writeEndElement ();
    }

    _writer.writeEndElement ();
  }

  private void writeResponse (ItemResponse response) throws XMLStreamException
  {
    _writer.writeStartElement ("response");

    // response properties
    _writer.writeAttribute ("id", response.getItemID ());
    _writer.writeAttribute ("bank", Long.toString (response.getBankKey ()));
    _writer.writeAttribute ("item", Long.toString (response.getItemKey ()));
    _writer.writeAttribute ("page", Integer.toString (response.getPage ()));
    _writer.writeAttribute ("position", Integer.toString (response.getPosition ()));
    _writer.writeAttribute ("sequence", Integer.toString (response.getSequence ()));
    _writer.writeAttribute ("created", response.getDateCreated ());
    _writer.writeAttribute ("mark", Boolean.toString (response.isMarkForReview ()));
    _writer.writeAttribute ("isSelected", Boolean.toString (response.getIsSelected ()));
    _writer.writeAttribute ("isRequired", Boolean.toString (response.isRequired ()));
    _writer.writeAttribute ("isValid", Boolean.toString (response.getIsValid ()));
    _writer.writeAttribute ("prefetched", Boolean.toString (response.isPrefetched ()));

    _writer.writeEndElement ();
  }

  public void writeEnd () throws XMLStreamException
  {
    _writer.writeEndElement ();
    _writer.close ();
  }

  public void dispose () throws XMLStreamException
  {
    if (_writer != null)
    {
      _writer.close ();
    }
  }

}


