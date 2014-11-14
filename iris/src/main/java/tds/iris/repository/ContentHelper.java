package tds.iris.repository;

import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import AIR.Common.Utilities.SpringApplicationContext;
import tds.iris.abstractions.repository.ContentException;
import tds.iris.abstractions.repository.IContentBuilder;
import tds.iris.abstractions.repository.IContentHelper;
import tds.iris.web.data.ContentRequest;
import tds.iris.web.data.ContentRequestItem;
import tds.itemrenderer.data.IITSDocument;
import tds.itemrenderer.data.IItemRender;
import tds.itemrenderer.data.ItemRender;
import tds.itemrenderer.data.ItemRenderGroup;

@Component
@Scope ("prototype")
public class ContentHelper implements IContentHelper
{
  private static final Logger _logger = LoggerFactory.getLogger (ContentHelper.class);

  private IContentBuilder     _contentBuilder;

  @PostConstruct
  public void init () throws ContentException {
    _contentBuilder = SpringApplicationContext.getBean ("iContentBuilder", IContentBuilder.class);
  }

  @Override
  public ItemRenderGroup loadRenderGroup (ContentRequest contentRequest) {
    String id = "Page-" + UUID.randomUUID ().toString ();
    ItemRenderGroup itemRenderGroup = new ItemRenderGroup (id, "default", "ENU");

    // load passage
    if (contentRequest.getPassage () != null) {
      String requestedPassageId = contentRequest.getPassage ().getId ();
      itemRenderGroup.setPassage (_contentBuilder.getITSDocument (requestedPassageId));
    }

    if (contentRequest.getItems () != null) {
      for (ContentRequestItem item : contentRequest.getItems ()) {
        IITSDocument document = _contentBuilder.getITSDocument (item.getId ());
        if (document != null) {
          IItemRender itemRender = new ItemRender (document, (int) document.getItemKey ());
          itemRender.setResponse (item.getResponse ());
          itemRenderGroup.add (itemRender);
        }
      }
    }

    return itemRenderGroup;
  }
}
