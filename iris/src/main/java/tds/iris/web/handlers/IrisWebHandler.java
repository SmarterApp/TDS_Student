package tds.iris.web.handlers;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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
  @Autowired
  private IContentHelper _contentHelper;

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
    renderGroup (itemRenderGroup, new AccLookup (), response);
  }
}
