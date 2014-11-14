package tds.iris.web.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ContentRequestItem extends ContentRequestEntity
{
  private String _response;

  @JsonProperty ("response")
  public String getResponse () {
    return _response;
  }

  public void setResponse (String value) {
    _response = value;
  }
}
