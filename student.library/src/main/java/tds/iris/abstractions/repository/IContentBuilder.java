package tds.iris.abstractions.repository;

import tds.itemrenderer.data.IITSDocument;

public interface IContentBuilder
{
  public IITSDocument getITSDocument (String id);
}
