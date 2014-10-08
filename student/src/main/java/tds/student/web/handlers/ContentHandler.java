/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.web.handlers;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import tds.itemrenderer.data.AccLookup;
import tds.itemrenderer.data.AccProperties;
import tds.itemrenderer.data.IITSDocument;
import tds.itemrenderer.data.IItemRender;
import tds.itemrenderer.data.ITSTypes.ITSEntityType;
import tds.itemrenderer.data.InvalidDataException;
import tds.itemrenderer.data.ItemRender;
import tds.itemrenderer.data.ItemRenderGroup;
import tds.itemrenderer.web.ITSDocumentXmlSerializable;
import tds.itemrenderer.web.XmlWriter;
import tds.itemrenderer.webcontrols.ErrorCategories;
import tds.itemrenderer.webcontrols.PageLayout;
import tds.itemrenderer.webcontrols.rendererservlet.ContentRenderingException;
import tds.itemrenderer.webcontrols.rendererservlet.RendererServlet;
import tds.student.services.PrintService;
import tds.student.services.abstractions.IContentService;
import tds.student.services.abstractions.IResponseService;
import tds.student.services.data.ItemResponse;
import tds.student.services.data.PageGroup;
import tds.student.services.data.TestOpportunity;
import tds.student.web.StudentContext;
import tds.student.web.StudentContextException;
import tds.student.web.StudentSettings;
import AIR.Common.Web.ContentType;
import TDS.Shared.Exceptions.ReturnStatusException;

@Scope ("prototype")
@Controller
public class ContentHandler extends TDSHandler
{

  private static final Logger _logger = LoggerFactory.getLogger (ContentHandler.class);
  
  @Autowired
  private IResponseService _responseService;

  @Autowired
  private StudentSettings  _studentSettings;

  @Autowired
  private IContentService  _contentService;

  @Autowired
  PrintService             _printService;

  /**
   * Load an item group from session DB and render html
   * 
   * @param response
   * @param page
   * @param groupId
   * @param dateCreated
   * @param prefetched
   * @param attempt
   * @throws StudentContextException
   * @throws ReturnStatusException
   * @throws ITSDocumentException
   * @throws IOException
   */
  @RequestMapping (value = "Content.axd/loadGroup", produces = "application/xml")
  @ResponseBody
  public void loadGroup (HttpServletResponse response, @RequestParam (value = "page", required = false) int page, @RequestParam (value = "groupID", required = false) String groupId,
      @RequestParam (value = "dateCreated", required = false) String dateCreated, @RequestParam (value = "new", required = false) boolean prefetched,
      @RequestParam (value = "attempt", required = false) int attempt) throws StudentContextException, ReturnStatusException, IOException {
    response.setContentType ("text/xml");
    
    // get test opp and accommodations
    TestOpportunity testOpp = getTestOpportunity ();
    
    AccLookup accLookup = getAccLookup ();

    // validate context
    if (testOpp == null) {
      StudentContext.throwMissingException ();
    }
    _logger.info("loadTest: ContentHandler.loadGroup  Opportunity ID: " + testOpp.getOppInstance ().getKey ()  +  "  group id: " + groupId);

    PageGroup pageGroup = _responseService.getItemGroup (testOpp.getOppInstance (), page, groupId, dateCreated, !_studentSettings.isReadOnly ());

    // get content for group
    _contentService.loadPageGroupDocuments (pageGroup, accLookup);

    // check for missing group
    if (pageGroup == null || pageGroup.size () == 0) {
      throw new ReturnStatusException (String.format ("An error occured getting the groupID '{%s}' on the test frame.", groupId));
    }

    // check for missing passage
    if (pageGroup.size () > 1 && pageGroup.getFilePath () == null) {
      throw new ReturnStatusException (String.format ("An error occured getting the groupID '{%s}' passage file path.", groupId));
    }

    if (prefetched && attempt == 1) {
      autoEmbossing (testOpp, accLookup, pageGroup);
    }

    // create item render group used for display to the student
    ItemRenderGroup itemRenderGroup = createItemRenderGroup (pageGroup, accLookup);

    renderGroup (itemRenderGroup, accLookup, response);

  }

  /**
   * A helper function for rending the html for an item render group.
   * 
   * @param itemRenderGroup
   * @param accLookup
   * @param response
   * @throws ContentRenderingException
   */     
  private void renderGroup (ItemRenderGroup itemRenderGroup, AccLookup accLookup, HttpServletResponse response) throws ContentRenderingException {
    try {
      // create HTML renderer
      PageLayout pageLayout = getBean ("pageLayout", PageLayout.class);
      
      // // add unique ID to page wrapper
      pageLayout.getSettings ().setUseUniquePageId (true);
      pageLayout.getSettings ().setIncludeJson(false);
      
      pageLayout.setItemRenderGroup (itemRenderGroup);
      
      // // get accs props
      AccProperties accProps = new AccProperties(accLookup);
     
      // // in braille mode force WAI layout
      if (accProps.isBrailleEnabled () || (accProps.getTestShell() != null && accProps.getTestShell().equals("TDS_TS_Accessibility"))) {
        pageLayout.setLayout("WAI");
      }
      // use layout that is in the xml
      else {
        pageLayout.setLayout();
      }
      
      // get it rendered.
      RendererServlet.getRenderedOutput (pageLayout);
      // write the rendered string to the socket.
     
     ITSDocumentXmlSerializable contentSerializer = new ITSDocumentXmlSerializable(pageLayout);

      // // render xml to stream);
       this.setMIMEType(ContentType.Xml);
  
       // // write xml
       XmlWriter writer = new XmlWriter (response.getOutputStream ());
       writer.writeStartElement("contents");
       contentSerializer.writeXml(writer);
       writer.writeEndElement();
       writer.close();
       
       // check if rendering errors
       if (pageLayout.getErrorCategory () != ErrorCategories.None) {
          throw new Exception(pageLayout.getErrorDescription ());
       }

    } catch (Exception exp) {
      exp.printStackTrace ();
      throw new ContentRenderingException (exp);
    }

  }

  /**
   * Call this function to check if the page group can be auto embossed
   * (printed).
   * 
   * Note: If you call this function it is assumed this page has just been
   * prefetched and that we can auto emboss content for the first time.
   * 
   * @param testOpp
   * @param accLookup
   * @param pageGroup
   * @throws ReturnStatusException
   */
  private void autoEmbossing (TestOpportunity testOpp, AccLookup accLookup, PageGroup pageGroup) throws ReturnStatusException  {
    AccProperties accProps = new AccProperties (accLookup);

    // make sure this is a braille test
    if (accProps.isBrailleEnabled () == false)
      return;

    // check if passage allows auto embossing
    if (pageGroup.getIitsDocument () != null &&
        allowsAutoEmbossing (accProps, pageGroup.getIitsDocument ())) {

      _printService.printPassageBraille (testOpp, pageGroup, accLookup);
    }

    for (ItemResponse item : pageGroup) {
      // make sure the test and the item allow for embossing
      if (allowsAutoEmbossing (accProps, item.getDocument ())) {
        // try and submit BRF/PRN for printing
        _printService.printItemBraille (testOpp, item, accLookup);
      }
    }
  }

  /**
   * Check if an ITS document allows auto embossing.
   * 
   * @param accProps
   * @param document
   * @return
   */
  private boolean allowsAutoEmbossing (AccProperties accProps, IITSDocument document)  {
    if (document == null) {
      return false;
    }

    // check if embossing is allowed for this document
    if (document.getType () == ITSEntityType.Unknown) {
      return false;
    }
    if (document.getType () == ITSEntityType.Item && !accProps.isEmbossItemEnabled ()) {
      return false;
    }
    if (document.getType () == ITSEntityType.Passage && !accProps.isEmbossStimulusEnabled ()) {
      return false;
    }
    // check if auto embossing is enabled for all documents
    if (accProps.hasEmbossAutoRequest ())
      return true;

    // check if document says it requests auto embossing
    return document.isAutoEmboss ();
  }

  /**
   * Gets the content and data required to render in the layout control
   * 
   * @param pageGroup
   * @param accLookup
   * @return ItemRenderGroup
   * @throws ITSDocumentException
   */
  private ItemRenderGroup createItemRenderGroup (PageGroup pageGroup, AccLookup accLookup)  {

    AccProperties accProperties = new AccProperties (accLookup);
    String language = accProperties.getLanguage ();

    // create an item group for the renderer
    ItemRenderGroup itemRenderGroup = new ItemRenderGroup (pageGroup.getId (), pageGroup.getSegmentID (), language);
    itemRenderGroup.setPrinted (pageGroup.isPrinted ());

    // create item renders
    for (ItemResponse item : pageGroup) {
      // if this item is hidden then skip it
      if (!item.isVisible ()) {
        continue;
      }
      validateContentLanguage (item.getDocument (), language);

      // create item render object, this represents a renderable item
      IItemRender itemRender = new ItemRender (item.getDocument (), item.getPosition ());
      itemRender.setResponse (item.getValue ());
      itemRender.setDisabled (false);
      itemRender.setMark (item.isMarkForReview());
      itemRender.setSelected (item.getIsSelected ());
      itemRender.setPrintable (item.isPrintable ());
      itemRender.setPrinted (item.isPrinted ());
      itemRenderGroup.add (itemRender);
    }

    // add passage to group if available
    validateContentLanguage (pageGroup.getDocument (), language);
    itemRenderGroup.setPassage (pageGroup.getDocument ());

    return itemRenderGroup;
  }

  /**
   * Checks if ITS document has content for a language and if it doesn't throws
   * exception.
   * 
   * @param doc
   * @param language
   * @throws InvalidDataException
   */
  private void validateContentLanguage (IITSDocument doc, String language) throws InvalidDataException {
    // check if <Content> exists for language
    if (doc != null && doc.getContent (language) == null) {
      // the <Content> element for this language is missing..
      String error = String.format ("The item {%s} does not have {%s} content.", doc.getItemKey (), language);

      throw new InvalidDataException (error);
    }
  }

  /**
   * Gets TestOpportunity
   * 
   * @return TestOpportunity
   */
  protected TestOpportunity getTestOpportunity () {
    return StudentContext.getTestOpportunity ();
  }

  /**
   * Gets AccLookup
   * 
   * @return AccLookup
   */
  protected AccLookup getAccLookup () {
    return StudentContext.getAccLookup ();
  }

}
