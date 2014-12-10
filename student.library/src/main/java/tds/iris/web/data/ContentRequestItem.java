package tds.iris.web.data;

import tds.itemrenderer.data.ITSTypes.ITSEntityType;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ContentRequestItem extends ContentRequestEntity
{
  private String _response;

  public ContentRequestItem () {
    super ();
    setItsType (ITSEntityType.Item);
  }

  @JsonProperty ("response")
  public String getResponse () {
    return _response;
  }

  public void setResponse (String value) {
    _response = value;
  }
}
