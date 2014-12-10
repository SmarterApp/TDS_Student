package tds.iris.web.handlers;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import AIR.Common.Web.TDSReplyCode;
import AIR.Common.data.ResponseData;
import TDS.Shared.Exceptions.TDSSecurityException;
import tds.iris.abstractions.repository.ContentException;
import tds.iris.abstractions.repository.IContentHelper;
import tds.iris.web.data.ContentRequest;
import tds.itemrenderer.data.AccLookup;
import tds.itemrenderer.data.ItemRenderGroup;
import tds.itemrenderer.webcontrols.PageSettings.UniqueIdType;
import tds.blackbox.ContentRequestException;
import tds.student.web.handlers.BaseContentRendererController;

@Scope ("prototype")
@Controller
public class IrisWebHandler extends BaseContentRendererController
{
  private static final Logger _logger = LoggerFactory.getLogger (IrisWebHandler.class);

  @Autowired
  private IContentHelper      _contentHelper;

  @PostConstruct
  public void init () {
    setPageSettingsUniqieIdType (UniqueIdType.GroupId);
  }

  // Controller starts here
  @RequestMapping (value = "content/load", produces = "application/xml")
  @ResponseBody
  public void loadContentRequest (HttpServletRequest request, HttpServletResponse response) throws ContentRequestException, IOException {
    ContentRequest contentRequest = ContentRequest.getContentRequest (request.getInputStream ());
    ItemRenderGroup itemRenderGroup = _contentHelper.loadRenderGroup (contentRequest);

    // Shiva: This is where our implementation differs from .NET.
    // In .NET the IRIS method of populating PageLayout is different than the
    // student way - the controllers are different.
    // In our case we intend to keep one single point of entry. Only IRiS allows
    // overriding of layout.
    // we will implement it by overriding the layout in the first item.
    if (!StringUtils.isEmpty (contentRequest.getLayout ()))
      itemRenderGroup.setLayout (contentRequest.getLayout ());

    renderGroup (itemRenderGroup, new AccLookup (), response);
  }

  // Controller starts here
  @RequestMapping (value = "content/reload")
  @ResponseBody
  public ResponseData<String> reloadContent (HttpServletRequest request, HttpServletResponse response) throws ContentRequestException, IOException {
    _contentHelper.reloadContent ();
    return new ResponseData<String> (TDSReplyCode.OK.ordinal (), "Reload succeeded.", "");
  }

  @ExceptionHandler ({ ContentException.class })
  @ResponseBody
  public ResponseData<String> handleContentException (ContentException excp, HttpServletResponse response) {
    _logger.error (excp.getMessage (), excp);
    response.setStatus (HttpStatus.INTERNAL_SERVER_ERROR_500);
    return new ResponseData<String> (TDSReplyCode.Error.getCode (), excp.getMessage (), "");
  }
}
