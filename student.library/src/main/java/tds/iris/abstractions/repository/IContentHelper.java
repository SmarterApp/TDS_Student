package tds.iris.abstractions.repository;

import tds.iris.web.data.ContentRequest;
import tds.itemrenderer.data.ItemRenderGroup;

public interface IContentHelper
{
  public ItemRenderGroup loadRenderGroup (ContentRequest contentRequest);
}
