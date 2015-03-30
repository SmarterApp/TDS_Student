/*******************************************************************************
 * Educational Online Test Delivery System Copyright (c) 2014 American
 * Institutes for Research
 * 
 * Distributed under the AIR Open Source License, Version 1.0 See accompanying
 * file AIR-License-1_0.txt or at http://www.smarterapp.org/documents/
 * American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sbacossmerge.data;

import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Stack;

import javax.xml.parsers.*;

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import AIR.Common.Web.EncryptionHelper;
import tds.itemrenderer.data.AccLookup;
import tds.student.services.data.TestOpportunity;
import tds.student.sql.data.ItemResponseUpdate;
import tds.student.web.handlers.TestResponseReader;

public class TestResponseReaderSax extends TestResponseReader
{
  private Stack<ContentHandler> _parseHandlers = new Stack<ContentHandler> ();
  private XMLReader             _xmlReader;

  private int                   _lastPage;
  private int                   _prefetch;
  private AccLookup             _accLookup;
  private TestOpportunity       _testOpp;

  public AccLookup getAccLookup () {
    return _accLookup;
  }

  public void setAccLookup (AccLookup value) {
    _accLookup = value;
  }

  public int getLastPage () {
    return _lastPage;
  }

  public void setLastPage (int value) {
    _lastPage = value;
  }

  public int getPrefetch () {
    return _prefetch;
  }

  public void setPrefetch (int value) {
    _prefetch = value;
  }

  public static TestResponseReader parseSax (InputStream xml, TestOpportunity testOpp) throws Exception {
    TestResponseReaderSax saxReader = new TestResponseReaderSax ();
    saxReader._testOpp = testOpp;

    SAXParserFactory spf = SAXParserFactory.newInstance ();
    spf.setNamespaceAware (true);
    SAXParser saxParser = spf.newSAXParser ();
    XMLReader xmlReader = saxParser.getXMLReader ();

    saxReader._xmlReader = xmlReader;
    ContentHandler defaultHandler = saxReader._parseHandlers.peek ();
    xmlReader.setContentHandler (defaultHandler);

    xmlReader.parse (new InputSource (xml));

    return saxReader;
  }

  private ContentHandler popHandler () {
    _parseHandlers.pop ();
    ContentHandler previousContentHandler = _parseHandlers.peek ();
    _xmlReader.setContentHandler (previousContentHandler);
    return previousContentHandler;
  }

  private ContentHandler pushHandler (ContentHandler newHandler) {
    _parseHandlers.push (newHandler);
    _xmlReader.setContentHandler (newHandler);
    return newHandler;
  }

  private class ResponseHandler extends DefaultHandler
  {
    ItemResponseUpdate    _itemResponseUpdate;

    private StringBuilder _filePathStringBuilder = null;
    private StringBuilder _responseStringBuilder = null;

    @Override
    public void startElement (String uri, String localName, String qName, Attributes attributes) throws SAXException {
      if ("response".equalsIgnoreCase (qName)) {
        _itemResponseUpdate = new ItemResponseUpdate ();
        handleAttributesForRequest (attributes);
      } else if ("filePath".equalsIgnoreCase (qName)) {
        _filePathStringBuilder = new StringBuilder ();
      } else if ("value".equalsIgnoreCase (qName)) {
        _responseStringBuilder = new StringBuilder ();
      }
    }

    @Override
    public void endElement (String uri, String localName, String qName) throws SAXException {
      if ("response".equalsIgnoreCase (qName)) {

        _itemResponseUpdate.setTestKey (_testOpp.getTestKey ());
        _itemResponseUpdate.setLanguage (_testOpp.getLanguage ());
        _itemResponseUpdate.setTestID (_testOpp.getTestID ());

        getResponses ().add (_itemResponseUpdate);
        popHandler ();
      } else if ("filePath".equalsIgnoreCase (qName)) {
        _itemResponseUpdate.setFilePath (EncryptionHelper.DecryptFromBase64 (_filePathStringBuilder.toString ()));

        // this step is required as we decide which buffer to put the characters
        // in based on this check.
        _filePathStringBuilder = null;
      } else if ("value".equalsIgnoreCase (qName)) {
        _itemResponseUpdate.setValue (_responseStringBuilder.toString ());
        _responseStringBuilder = null;
      }

    }

    @Override
    public void characters (char[] ch, int start, int length) throws SAXException {
      if (_filePathStringBuilder != null) {
        _filePathStringBuilder.append (new String (ch, start, length));
      } else if (_responseStringBuilder != null) {
        _responseStringBuilder.append (new String (ch, start, length));
      }
    }

    private void handleAttributesForRequest (Attributes attributes) {
      for (int counter1 = 0; counter1 < attributes.getLength (); ++counter1) {
        String value = attributes.getValue (counter1);
        String name = attributes.getLocalName (counter1);
        if ("id".equalsIgnoreCase (name)) {
          // TODO Sajib: Inherit from ItemResponseUpdate and move the new
          // properties to that one and then complete this.
          // _itemResponseUpdate.setId(value);
        } else if ("bankKey".equalsIgnoreCase (name)) {
          _itemResponseUpdate.setBankKey (Long.parseLong (value));
        } else if ("itemKey".equalsIgnoreCase (name)) {
          _itemResponseUpdate.setItemKey (Long.parseLong (value));
        } else if ("segmentID".equalsIgnoreCase (name)) {
          _itemResponseUpdate.setSegmentID (value);
        } else if ("pageKey".equalsIgnoreCase (name)) {
          _itemResponseUpdate.setPageKey (value);
        } else if ("dateCreated".equalsIgnoreCase (name)) {
          // TODO sajib
        } else if ("page".equalsIgnoreCase (name)) {
          _itemResponseUpdate.setPage (Integer.parseInt (value));
        } else if ("position".equalsIgnoreCase (name)) {
          _itemResponseUpdate.setPosition (Integer.parseInt (value));
        } else if ("sequence".equalsIgnoreCase (name)) {
          _itemResponseUpdate.setSequence (Integer.parseInt (value));
        } else if ("selected".equalsIgnoreCase (name)) {
          _itemResponseUpdate.setIsSelected (Boolean.parseBoolean (value));
        } else if ("valid".equalsIgnoreCase (name)) {
          _itemResponseUpdate.setIsValid (Boolean.parseBoolean (value));
        }

      }
    }
  }

  private class AccsHandler extends DefaultHandler
  {
    private StringBuilder _value = new StringBuilder ();

    @Override
    public void startElement (String uri, String localName, String qName, Attributes attributes) throws SAXException {

    }

    @Override
    public void endElement (String uri, String localName, String qName) throws SAXException {
      _accLookup = new AccLookup ();
      _accLookup.deserializeSemiColonSeparated (_value.toString ());
      popHandler ();
    }

    @Override
    public void characters (char[] ch, int start, int length) throws SAXException {
      _value.append (new String (ch, start, length));
    }
  }

  private class RequestHandler extends DefaultHandler
  {
    public RequestHandler () {
    }

    @Override
    public void startElement (String uri, String localName, String qName, Attributes attributes) throws SAXException {
      if ("request".equalsIgnoreCase (qName)) {
        handleAttributesForRequest (attributes);
      } else if ("accs".equalsIgnoreCase (qName)) {
        pushHandler (new AccsHandler ()).startElement (uri, localName, qName, attributes);
      } else if ("responses".equalsIgnoreCase (qName)) {
        setResponses (new ArrayList<ItemResponseUpdate> ());
      } else if ("response".equalsIgnoreCase (qName)) {
        pushHandler (new ResponseHandler ()).startElement (uri, localName, qName, attributes);
      }
    }

    @Override
    public void endElement (String uri, String localName, String qName) throws SAXException {

    }

    @Override
    public void characters (char[] ch, int start, int length) throws SAXException {

    }

    private void handleAttributesForRequest (Attributes attributes) {
      for (int counter1 = 0; counter1 < attributes.getLength (); ++counter1) {
        String value = attributes.getValue (counter1);
        String name = attributes.getLocalName (counter1);
        if ("action".equalsIgnoreCase (name)) {
          setAction (value);
        } else if ("eventID".equalsIgnoreCase (name)) {
          setEventID (Integer.parseInt (value));
        } else if ("timestamp".equalsIgnoreCase (name)) {
          if (!StringUtils.isEmpty (value))
            setTimestamp (Long.parseLong (value));
        } else if ("lastPage".equalsIgnoreCase (name)) {
          if (!StringUtils.isEmpty (value))
            setLastPage (Integer.parseInt (value));
        } else if ("prefetch".equalsIgnoreCase (name)) {
          if (!StringUtils.isEmpty (value))
            setPrefetch (Integer.parseInt (value));
        }
      }
    }
  }

  private TestResponseReaderSax () {
    super ();
    ContentHandler saxContentHandler = new RequestHandler ();
    _parseHandlers.push (saxContentHandler);
  }

  public static void main (String[] argv) {
    try {
      String str = "<request action=\"update\" eventID=\"2\" timestamp=\"1413264936958\" lastPage=\"1\" prefetch=\"0\"><accs>Audio Playback Controls:TDS_APC_PSP;Dictionary:TDS_Dict0;Dictionary Options:TDS_DO_None;Item Font Size:TDS_IF_S14;Language:ENU;Permissive Mode:TDS_PM0;System Volume Control:TDS_SVC0;Test Progress Indicator:TDS_TPI_ResponsesFix;Thesaurus:TDS_TH0;Thesaurus Options:TDS_TO_None</accs><responses><response id=\"187-2517\" bankKey=\"187\" itemKey=\"2517\" segmentID=\"SBAC-ELA-6\" pageKey=\"83fdc03c-53d4-4afa-bad8-8969db11359e\" dateCreated=\"\" page=\"1\" position=\"1\" sequence=\"1\" selected=\"true\" valid=\"true\"><filePath>PfXxb%2BsL4u4SB1fRMfuBN%2B4S23YvhjIU9ZAPAQJjbdrwr1m9QRHVa%2Bcfk0m6ND%2FHTN%2FPbfA4ip7QDd8Ob7oHLLJIATKPCMxXcZJfa4kXU0U%3D</filePath><value>A</value></response></responses></request>";
      // TestResponseReader reader = TestResponseReaderSax.parse (new );
    } catch (Exception exp) {
      exp.printStackTrace ();
    }
  }

}
