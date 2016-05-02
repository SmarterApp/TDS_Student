/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.web.handlers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import tds.student.services.data.TestOpportunity;
import tds.student.sql.data.ItemResponseUpdate;
import AIR.Common.Web.EncryptionHelper;
import AIR.Common.xml.TdsXmlInputFactory;

// / <summary>
// / Represents a response(s) update request coming from the browser client.
// / </summary>

public class TestResponseReader
{

  private String                    _action;
  // / <summary>
  // / A unique ID from the clients current session.
  // / </summary>
  private int                       _eventID;

  // / <summary>
  // / The time the data was sent from the client.
  // / </summary>
  private long                      _timestamp;

  // / <summary>
  // / The duration that a student spent on this page in the current browser session and opportunity
  // / </summary>
  private float                      _pageDuration;

  // / <summary>
  // / A list of responses that were updated.
  // / </summary>
  private List<ItemResponseUpdate> _responses;

  public TestResponseReader ()
  {
    _responses = new ArrayList<ItemResponseUpdate> ();
  }

  // / <summary>
  // / Parses the XML from the client when a response updates are made.
  // / </summary>
  public static TestResponseReader parse (InputStream stream, TestOpportunity testOpp) throws  XMLStreamException
  {
    TestResponseReader responseReader = null;
    ItemResponseUpdate responseUpdate = null;
    String testKey = testOpp.getTestKey ();
    String language = testOpp.getLanguage ();
    int attrCnt = 0;

    XMLInputFactory factory = TdsXmlInputFactory.newInstance();
    
    XMLStreamReader reader = factory.createXMLStreamReader (stream);
    
    String tagContent = null;
    while (reader.hasNext ()) {
      int event = reader.next ();
      switch (event) {
      case XMLStreamConstants.START_ELEMENT:
        switch(reader.getLocalName()){
        case "filePath":
        case "value":
          tagContent = null;
          break;
        case "request":
          responseReader = new TestResponseReader ();
          attrCnt = reader.getAttributeCount ();
          for (int i = 0; i < attrCnt; i++) {
            String attrName = reader.getAttributeLocalName (i);
            String attrValue = reader.getAttributeValue (i);
            switch (attrName) {
            case "action" :
              responseReader.setAction (attrValue);              
              break;
            case "eventID":
              responseReader.setEventID (Integer.parseInt (attrValue));             
              break;
            case "timestamp":              
              responseReader.setTimestamp (Long.parseLong (attrValue));
              break;
            case "pageDuration":
              responseReader.setPageDuration(Float.parseFloat(attrValue));
              break;
            }
          }
          break;
          
        case "responseUpdate":
          responseUpdate = new ItemResponseUpdate ();
          responseUpdate.setTestKey (testOpp.getTestKey ());
          responseUpdate.setLanguage (testOpp.getLanguage ());
          responseUpdate.setTestID (testOpp.getTestID ());
 
          attrCnt = reader.getAttributeCount ();
          for (int i = 0; i < attrCnt; i++) {
            String attrName = reader.getAttributeLocalName (i);
            String attrValue = reader.getAttributeValue (i);

            switch (attrName) {
            case "itsBank" :
              responseUpdate.setBankKey (Integer.parseInt (attrValue));              
              break;
            case "itsItem" :
              responseUpdate.setItemKey (Integer.parseInt (attrValue));              
              break;
            case "segmentID":
              responseUpdate.setSegmentID (attrValue);
              break;
            case "page":              
              responseUpdate.setPage (Integer.parseInt (attrValue));   
              break;
            case "position":              
              responseUpdate.setPosition (Integer.parseInt (attrValue));   
              break;
            case "dateCreated":
              responseUpdate.setDateCreated (attrValue);
              break; 
            case "sequence" :
              responseUpdate.setSequence (Integer.parseInt (attrValue));              
              break;  
            case "isSelected" :
              if ("true".equalsIgnoreCase (attrValue))
                responseUpdate.setIsSelected (true);
              else
                responseUpdate.setIsSelected (false);
              break;
            case "isValid":
              if ("true".equalsIgnoreCase (attrValue))
                responseUpdate.setIsValid (true);
              else
                responseUpdate.setIsValid (false);
              break;             
            }
          }
          break;
        }
        break;
        
      case XMLStreamConstants.CHARACTERS:
        tagContent = reader.getText().trim();
        break;  
      
      case XMLStreamConstants.CDATA:
        tagContent = reader.getText ().trim ();
        break;
        
      case XMLStreamConstants.END_ELEMENT:
        switch(reader.getLocalName()){
        case "responseUpdate":
          responseReader._responses.add (responseUpdate);
          break;
        
        case "filePath":
          responseUpdate.setFilePath (tagContent);
          responseUpdate.setFilePath (EncryptionHelper.DecryptFromBase64 (responseUpdate.getFilePath ()));
          break;
          
        case "value":
          responseUpdate.setValue (tagContent);
          break;
        }
        break;
       
      case XMLStreamConstants.START_DOCUMENT:
        responseReader = new TestResponseReader ();
        break;
      }
    }
    return responseReader;
//    TestResponseReader responseReader = new TestResponseReader ();
//
//    // read the HTTP POST as a xmlreader stream
//
//    {
//      XmlReader xmlReader = new XmlReader (stream);
//      if (xmlReader.readToFollowing ("request"))
//      {
//        responseReader.setAction (xmlReader.getAttribute ("action"));
//        responseReader.setEventID ((int) xmlReader.getAttributeAsLong ("eventID"));
//        responseReader.setTimestamp (xmlReader.getAttributeAsLong ("timestamp"));
//      }
//
//      // add test info
//      String testKey = testOpp.getTestKey ();
//      String language = testOpp.getLanguage ();
//
//      while (xmlReader.readToFollowing ("responseUpdate"))
//      {
//        ItemResponseUpdate responseUpdate = new ItemResponseUpdate ();
//
//        // set test properties
//        responseUpdate.setTestKey (testKey);
//        responseUpdate.setLanguage (language);
//
//        // set content properties
//        responseUpdate.setBankKey ((int) xmlReader.getAttributeAsLong ("itsBank"));
//        responseUpdate.setItemKey ((int) xmlReader.getAttributeAsLong ("itsItem"));
//        responseUpdate.setSegmentID (xmlReader.getAttribute ("segmentID"));
//        responseUpdate.setPage ((int) xmlReader.getAttributeAsLong ("page"));
//        responseUpdate.setPosition ((int) xmlReader.getAttributeAsLong ("position"));
//        responseUpdate.setDateCreated (xmlReader.getAttribute ("dateCreated"));
//
//        // set response properties
//        responseUpdate.setSequence ((int) xmlReader.getAttributeAsLong ("sequence"));
//        responseUpdate.setIsSelected (xmlReader.getAttributeAsBoolean ("isSelected"));
//        responseUpdate.setIsValid (xmlReader.getAttributeAsBoolean ("isValid"));
//
//        xmlReader.moveToElement ();
//
//        // get file path
//        if (xmlReader.readToDescendant ("filePath"))
//        {
//          responseUpdate.setFilePath (xmlReader.readString ());
//          responseUpdate.setFilePath (EncryptionHelper.DecryptFromBase64 (responseUpdate.getFilePath ()));
//        }
//
//        // get response if provided
//        if (xmlReader.readToNextSibling ("value") && !xmlReader.isEmptyElement ())
//        {
//          responseUpdate.setValue (xmlReader.readString ());
//        }
//
//        responseReader._responses.add (responseUpdate);
//      }
//
//    }
//
//    return responseReader;
  }

  public String getAction () {
    return _action;
  }

  public void setAction (String value) {
    this._action = value;
  }

  public int getEventID () {
    return _eventID;
  }

  public void setEventID (int value) {
    this._eventID = value;
  }

  public long getTimestamp () {
    return _timestamp;
  }

  public void setTimestamp (long value) {
    this._timestamp = value;
  }

  public void setPageDuration(float pageDuration) {
    this._pageDuration = pageDuration;
  }

  public float getPageDuration() {
    return this._pageDuration;
  }
  public List<ItemResponseUpdate> getResponses () {
    return _responses;
  }

  public void setResponses (List<ItemResponseUpdate> value) {
    this._responses = value;
  }

  public static void main(String[] args) throws XMLStreamException {
    String data="<request action=\"update\" eventID=\"1\" timestamp=\"1395425537492\" >" +
    		"<responseUpdates>" +
    		"<responseUpdate itsBank=\"95\" itsItem=\"10001\" segmentID=\"1\" page=\"2\" position=\"3\" dateCreated=\"2014-03-21\" sequence=\"4\" isSelected=\"true\" isValid=\"false\">" +
    		"  <filePath>response file path</filePath> " +
    		"  <value>response value</value>" +
    		"</responseUpdate>" +
    		"</responseUpdates>" +
    		"</request>";
    
    String str = "<request action=\"update\" eventID=\"2\" timestamp=\"1395682065590\"> " +
    "<responseUpdates> " +
      "<responseUpdate id=\"159-33\" itsBank=\"159\" itsItem=\"33\" segmentID=\"GRAD-Mathematics-11\" page=\"1\" position=\"1\" sequence=\"1\" dateCreated=\"2014-03-24 13:27:23.620\" isSelected=\"true\" isValid=\"true\" > " +
      "<filePath>ZblyUsNyoNpbf5ZL1KoW4YUCIlxW74zxKgQ_XYiGGFzEpF1X6sZ5YraNlO1pjFx12DBPs826QrdGiuCbxFkJaKD61VY31qWsQbO0hm8JBksdF8gPz9OtD1L5VP-tllthci-HPyCc8r1TRasFUioWCUeZmjswHzgvB6gw-ExZS7GiwaTav0xaiZ8avbDoCOfLfqVyDRAIu649Q74tfid5-aa09LbtOYOrIMxfawJqXneDup8f-LF42vOyNIKkZ81rCqv_ZevYt8v04TqeWJ7Nsw2</filePath> " +
      "<value><![CDATA[A]]></value>" +
    "</responseUpdate> " +
    "</responseUpdates> " +
    "</request>";
    
    String str1 = "<request action=\"update\" eventID=\"2\" timestamp=\"1395682420795\"> " +
    "<responseUpdates> " +
       " <responseUpdate id=\"159-446\" itsBank=\"159\" itsItem=\"446\" segmentID=\"MCA-Science-HS\" page=\"1\" position=\"1\" sequence=\"1\" dateCreated=\"2014-03-24 13:32:46.880\" isSelected=\"true\" isValid=\"true\" > " +
       " <filePath>ZblyUsNyoNpbf5ZL1KoW4YUCIlxW74zxKgQ_XYiGGFzEpF1X6sZ5YraNlO1pjFx12DBPs826QrdGiuCbxFkJaKD61VY31qWsQbO0hm8JBksdF8gPz9OtD1L5VP-tllthci-HPyCc8r1TRasFUioWCUeZmjswHzgvB6gw-ExZS7GiwaTav0xaiZ8avbDoCOfLF7lPliIDE6hOSHqkLgi-sQrgFz2uLxzi2dzyYJ_JTRcX0M_dZbPb4TtDp2048hEy4Q-VeQe1mnoUvbSaOkQOLQ2</filePath>" +
       " <value><![CDATA[<?xml version=\"1.0\" encoding=\"UTF-8\"?> <!-- MACHINE GENERATED 3/24/14 13:33 PM. DO NOT EDIT --> <!DOCTYPE AnswerSet [ <!ELEMENT AnswerSet (Question+)> <!ELEMENT AtomicObject (#PCDATA)> <!ELEMENT EdgeVector (#PCDATA)> <!ELEMENT GridImageTestPoints (TestPoint*)> <!ELEMENT LabelList (#PCDATA)> <!ELEMENT Object (PointVector,EdgeVector,LabelList,ValueList)> <!ELEMENT ObjectSet (Object,AtomicObject+)> <!ELEMENT PointVector (#PCDATA)> <!ELEMENT Question (QuestionPart)> <!ATTLIST Question id NMTOKEN #REQUIRED> <!ELEMENT QuestionPart (LabelList,GridImageTestPoints,ObjectSet)> <!ATTLIST QuestionPart id NMTOKEN #REQUIRED> <!ELEMENT TestPoint (#PCDATA)> <!ELEMENT ValueList (#PCDATA)> ]> <AnswerSet><Question id=\"\"><QuestionPart id=\"1\"><ObjectSet><RegionGroupObject name=\"All\" numselected=\"1\"><RegionObject name=\"Nucleus2\" isselected=\"true\"/><RegionObject name=\"Cell Wall2\" isselected=\"false\"/><RegionObject name=\"Ribosomes2\" isselected=\"false\"/><RegionObject name=\"Chloroplast2\" isselected=\"false\"/><RegionObject name=\"Cytoplasm2\" isselected=\"false\"/></RegionGroupObject></ObjectSet><SnapPoint></SnapPoint></QuestionPart></Question></AnswerSet>]]></value> " +
       " </responseUpdate> " +
       "</responseUpdates>" +
       "</request>";
    
    TestResponseReader responseReader = null;
    ItemResponseUpdate responseUpdate = null;
    int attrCnt = 0;
    InputStream is = new ByteArrayInputStream(str1.getBytes());

    XMLInputFactory factory = TdsXmlInputFactory.newInstance();
    
    XMLStreamReader reader = factory.createXMLStreamReader (is);
    
    String tagContent = null;
    while (reader.hasNext ()) {
      int event = reader.next ();
      switch (event) {
      case XMLStreamConstants.START_ELEMENT:
        switch(reader.getLocalName()){
        case "request":
          responseReader = new TestResponseReader ();
          attrCnt = reader.getAttributeCount ();
          for (int i = 0; i < attrCnt; i++) {
            String attrName = reader.getAttributeLocalName (i);
            String attrValue = reader.getAttributeValue (i);
            switch (attrName) {
            case "action" :
              responseReader.setAction (attrValue);              
              break;
            case "eventID":
              responseReader.setEventID (Integer.parseInt (attrValue));             
              break;
            case "timestamp":              
              responseReader.setTimestamp (Long.parseLong (attrValue));
              break;
            }
          }
          break;
          
        case "responseUpdate":
          responseUpdate = new ItemResponseUpdate ();
          responseUpdate.setTestKey ("thetestkey");
          responseUpdate.setLanguage ("thelanguage");
          attrCnt = reader.getAttributeCount ();
          for (int i = 0; i < attrCnt; i++) {
            String attrName = reader.getAttributeLocalName (i);
            String attrValue = reader.getAttributeValue (i);

            switch (attrName) {
            case "itsBank" :
              responseUpdate.setBankKey (Integer.parseInt (attrValue));              
              break;
            case "itsItem" :
              responseUpdate.setItemKey (Integer.parseInt (attrValue));              
              break;
            case "segmentID":
              responseUpdate.setSegmentID (attrValue);
              break;
            case "page":              
              responseUpdate.setPage (Integer.parseInt (attrValue));   
              break;
            case "position":              
              responseUpdate.setPosition (Integer.parseInt (attrValue));   
              break;
            case "dateCreated":
              responseUpdate.setDateCreated (attrValue);
              break; 
            case "sequence" :
              responseUpdate.setSequence (Integer.parseInt (attrValue));              
              break;  
            case "isSelected" :
              if ("true".equalsIgnoreCase (attrValue))
                responseUpdate.setIsSelected (true);
              else
                responseUpdate.setIsSelected (false);
              break;
            case "isValid":
              if ("true".equalsIgnoreCase (attrValue))
                responseUpdate.setIsValid (true);
              else
                responseUpdate.setIsValid (false);
              break;             
            }
          }
          break;
        }
        break;
        
      case XMLStreamConstants.CHARACTERS:
        tagContent = reader.getText().trim();
        break;  
      
      case XMLStreamConstants.CDATA:
        tagContent = reader.getText ().trim ();
        break;
        
      case XMLStreamConstants.END_ELEMENT:
        switch(reader.getLocalName()){
        case "responseUpdate":
          responseReader._responses.add (responseUpdate);
          break;
        
        case "filePath":
          responseUpdate.setFilePath (tagContent);
          //responseUpdate.setFilePath (EncryptionHelper.DecryptFromBase64 (responseUpdate.getFilePath ()));
          break;
          
        case "value":
          responseUpdate.setValue (tagContent);
          break;
        }
        break;
       
      case XMLStreamConstants.START_DOCUMENT:
        responseReader = new TestResponseReader ();
        break;
      }
    }
    System.out.println ("done!");
  }
}
