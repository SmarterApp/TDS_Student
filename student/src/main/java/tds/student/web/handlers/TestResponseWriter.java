/*******************************************************************************
 * Educational Online Test Delivery System Copyright (c) 2014 American
 * Institutes for Research
 * 
 * Distributed under the AIR Open Source License, Version 1.0 See accompanying
 * file AIR-License-1_0.txt or at http://www.smarterapp.org/documents/
 * American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.web.handlers;

import AIR.Common.Web.Session.HttpContext;
import AIR.Common.xml.TdsXmlOutputFactory;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

import tds.student.services.data.ItemResponse;
import tds.student.services.data.PageGroup;
import tds.student.services.data.PageList;
import tds.student.services.data.TestOpportunity;
import tds.student.sql.data.ItemResponseUpdateStatus;
import tds.student.web.TestManager;

// / <summary>
// / Used to write out the results of a update response request (or possibly
// other types of requests, this was left to be generic)
// / </summary>
public class TestResponseWriter // : IDisposable
{
  private XMLStreamWriter _writer;

  public TestResponseWriter (OutputStream stream) throws XMLStreamException, FactoryConfigurationError {
    _writer = TdsXmlOutputFactory.newInstance ().createXMLStreamWriter (stream);
  }

  public void writeStart (int eventID) throws XMLStreamException {
    _writer.writeStartElement ("results");

    // TODO Shajib: In .net code server's machinename's hashcode is used
    int machineHash = HttpContext.getCurrentContext ().getServer ().hashCode ();
    String machineID = Integer.toString (machineHash);
    _writer.writeAttribute ("machineID", machineID);
    _writer.writeAttribute ("eventID", Integer.toString (eventID));

  }

  public void writeSummary (TestManager tm, TestOpportunity testOpp, boolean prefetched) throws XMLStreamException {
    _writer.writeStartElement ("testsummary");

    // write out test info (NOTE: if you modify any of xml notify load test tool
    // developer)
    PageList pageList = tm.GetVisiblePages ();
    // SB:Merge Start
    // _writer.writeAttribute ("testLength", Integer.toString
    // (testOpp.getTestConfig ().getTestLength ()));
    _writer.writeAttribute ("lengthMet", Boolean.toString (tm.IsTestLengthMet ()));
    _writer.writeAttribute ("finished", Boolean.toString (tm.IsTestLengthMet () && pageList.isAllCompleted ()));
    _writer.writeAttribute ("prefetched", Boolean.toString (prefetched));
    _writer.writeAttribute ("allAnswered", Boolean.toString(pageList.isAllAnswered()));
    // SB:Merge End
    _writer.writeEndElement ();

  }

  public void writeTimestamps (long timeClientSent, long timeServerReceived, long timeServerCompleted) throws XMLStreamException {
    _writer.writeStartElement ("timestamps");

    // timestamp of when the message was sent from the client
    _writer.writeAttribute ("sent", Long.toString (timeClientSent));

    // timestamp of when the message was received from the server
    _writer.writeAttribute ("received", Long.toString (timeServerReceived));

    // timestamp of when the server was completed with the request
    _writer.writeAttribute ("completed", Long.toString (timeServerCompleted));

    _writer.writeEndElement ();
  }

  public void writeResponseUpdates (List<ItemResponseUpdateStatus> responseUpdates) throws XMLStreamException {
    _writer.writeStartElement ("updates");

    for (ItemResponseUpdateStatus responseStatus : responseUpdates) {
      _writer.writeStartElement ("response");
      _writer.writeAttribute ("position", Integer.toString (responseStatus.getPosition ()));
      _writer.writeAttribute ("status", responseStatus.getStatus ());
      _writer.writeAttribute ("reason", responseStatus.getReason () != null ? responseStatus.getReason () : "");
      _writer.writeEndElement ();
    }

    _writer.writeEndElement ();
  }

  public void writePages (PageList pages) throws XMLStreamException {
    if (pages.size () == 0)
      return;

    _writer.writeStartElement ("pages");

    for (PageGroup page : pages) {
      writePage (page);
    }

    _writer.writeEndElement ();
  }

  private void writePage (PageGroup page) throws XMLStreamException {
    // add group
    _writer.writeStartElement ("page");
    _writer.writeAttribute ("type", "contentpage");
    _writer.writeAttribute ("number", "" + page.getNumber ());
    _writer.writeAttribute ("prefetched", ""+page.getPrefetched ());

    writeSegment (page);
    writeGroup (page);
    writeItems (page);

    _writer.writeEndElement ();
  }

  private void writeItems (PageGroup page) throws XMLStreamException {
    _writer.writeStartElement ("items");

    // add responses
    for (ItemResponse item : page) {
      writeItem (item);
    }

    _writer.writeEndElement ();
  }

  private void writeItem (ItemResponse item)  throws XMLStreamException {
    _writer.writeStartElement ("item");

    // response properties
    _writer.writeAttribute ("id", item.getItemID ());
    _writer.writeAttribute ("bankKey", "" + item.getBankKey ());
    _writer.writeAttribute ("itemKey", "" + item.getItemKey ());
    _writer.writeAttribute ("position", "" + item.getPosition ());
    _writer.writeAttribute ("sequence", "" + item.getSequence ());
    // TODO SB:Merge. We have a util somewhere to generate JSON string for
    // booleans.
    // use that below.
    _writer.writeAttribute ("marked", "" + item.isMarkForReview ());
    // TODO SB:Merge. We do not support SIRVE but this may 
    _writer.writeAttribute ("readOnly", "false");
    _writer.writeAttribute ("selected", "" + item.getIsSelected ());
    _writer.writeAttribute ("required", "" + item.isRequired ());
    _writer.writeAttribute ("valid", "" + item.getIsValid ());
    _writer.writeAttribute ("prefetched", "" + item.isPrefetched ());

    _writer.writeEndElement ();
  }

  private void writeGroup (PageGroup page) throws XMLStreamException {
    _writer.writeStartElement ("group");
    _writer.writeAttribute ("id", page.getId ());
    // TODO Shiva: SB:Merge UUID random
    _writer.writeAttribute ("key", UUID.randomUUID ().toString ());
    // TODO Shiva: SB:Merge datecreated. see writeResponse below.
    _writer.writeAttribute ("created", "");
    _writer.writeAttribute ("required", "" + page.getItemsRequired ());
    _writer.writeEndElement ();
  }

  private void writeSegment (PageGroup page) throws XMLStreamException {
    _writer.writeStartElement ("segment");
    ItemResponse firstResponse = page.getFirst ();
    _writer.writeAttribute ("position", "" + firstResponse.getSegment ());
    _writer.writeAttribute ("id", firstResponse.getSegmentID ());
    _writer.writeEndElement ();
  }

  private void writeResponse (ItemResponse response) throws XMLStreamException {
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

  public void writeEnd () throws XMLStreamException {
    _writer.writeEndElement ();
    _writer.close ();
  }

  public void dispose () throws XMLStreamException {
    if (_writer != null) {
      _writer.close ();
    }
  }

}
