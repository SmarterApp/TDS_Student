/*******************************************************************************
 * Educational Online Test Delivery System Copyright (c) 2014 American
 * Institutes for Research
 * 
 * Distributed under the AIR Open Source License, Version 1.0 See accompanying
 * file AIR-License-1_0.txt or at http://www.smarterapp.org/documents/
 * American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.web.handlers;

import AIR.Common.Web.BrowserParser;
import AIR.Common.Web.Session.Server;
import AIR.Common.Web.WebHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import tds.blackbox.web.handlers.TDSHandler;
import tds.itemrenderer.data.AccLookup;
import tds.itemrenderer.data.IITSDocument;
import tds.itemrenderer.data.ItemRender;
import tds.itemrenderer.data.ItemRenderGroup;
import tds.itemrenderer.webcontrols.PageLayout;
import tds.itemrenderer.webcontrols.rendererservlet.RendererServlet;
import tds.student.services.abstractions.IContentService;
import tds.student.sql.abstractions.IItemBankRepository;
import tds.student.web.StudentContext;

/**
 * @author mpatel
 * 
 */
@Scope ("prototype")
@Controller
public class DialogFrameHandler extends TDSHandler
{
  private static Logger       _logger       = LoggerFactory.getLogger (DialogFrameHandler.class);
  private final IItemBankRepository itemBankRepository;
  private final PageLayout pageLayout;
  private final IContentService contentService;

  @Autowired
  public DialogFrameHandler(final IItemBankRepository itemBankRepository,
                            final PageLayout pageLayout,
                            final IContentService contentService) {
    this.itemBankRepository = itemBankRepository;
    this.pageLayout = pageLayout;
    this.contentService = contentService;
  }

  @RequestMapping (value = "DialogFrame.axd/getContent", produces = "application/xml")
  @ResponseBody
  public String getDialogFrameContent () {
    // get query String params
    long bankKey = WebHelper.getQueryValueLong ("bankKey");
    long itemKey = WebHelper.getQueryValueLong ("itemKey");

    //SB-483
    String contentLanguage = WebHelper.getQueryString ("language");
    if (StringUtils.isEmpty (contentLanguage))
      contentLanguage = StudentContext.getLanguage ();

    String studentHelpFile;

    try
    {
      studentHelpFile = itemBankRepository.getItemPath (bankKey, itemKey);
    } catch (Exception ex)
    {
      // NOTE: We would get an error here if the tutorials config is not loaded.
      // Larry/Selina said to ignore these and just log.
      _logger.error (ex.getMessage (), ex);
      throw new IllegalArgumentException (ex.getMessage ());
    }

    // check if student help was found
    if (StringUtils.isEmpty (studentHelpFile))
    {
      // TODO: Send the reference item key that wanted this tutorial for logging
      // purposes
      _logger.error (String.format ("Could not find the student help for the item %s-%s.", bankKey, itemKey));
      throw new IllegalArgumentException (String.format ("Could not find the student help for the item %s-%s.", bankKey, itemKey));
    }

    IITSDocument itsDocument;

    try
    {
      final AccLookup languageAccommodation = new AccLookup();
      languageAccommodation.add ("Language", contentLanguage);
      itsDocument = contentService.getContent(studentHelpFile, languageAccommodation);
    } catch (Exception ex)
    {
      // Jeremy approved ignoring this and just logging.
      _logger.error (ex.getMessage (), ex);
      throw new IllegalArgumentException (ex.getMessage ());
    }

    // render content (use the right language here)
    String groupId = String.format ("I-%s-%s", bankKey, itemKey);
    ItemRenderGroup itemRenderGroup = new ItemRenderGroup (groupId, null, contentLanguage);
    itemRenderGroup.add (new ItemRender (itsDocument, (int) itemKey));

    pageLayout.setItemRenderGroup (itemRenderGroup);

    RendererServlet.getRenderedOutput (pageLayout);
    System.out.println (pageLayout.getRenderToString ());
    return pageLayout.getRenderToString ();
  }
}
