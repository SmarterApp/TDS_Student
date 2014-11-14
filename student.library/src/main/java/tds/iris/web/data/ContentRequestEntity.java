package tds.iris.web.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ContentRequestEntity
{
  private String _id;

  @JsonProperty ("id")
  public String getId () {
    return _id;
  }

  public void setId (String value) {
    _id = value;
  }

  @Override
  public String toString () {
    return getId ();
  }
}
