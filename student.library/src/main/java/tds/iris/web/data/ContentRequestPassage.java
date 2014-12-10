package tds.iris.web.data;

import tds.itemrenderer.data.ITSTypes.ITSEntityType;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ContentRequestPassage extends ContentRequestEntity
{
  private boolean _autoLoad = true;

  public ContentRequestPassage () {
    super ();
    setItsType (ITSEntityType.Passage);
  }

  @JsonProperty ("autoLoad")
  public boolean getAutoLoad () {
    return _autoLoad;
  }

  public void setAutoLoad (boolean value) {
    _autoLoad = value;
  }
}
