package tds.student.sbacossmerge.data;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginInfoAccommocation
{
  @JsonProperty ("type")
  private String       _type;

  @JsonProperty ("codes")
  private List<String> _codes;

  public String getType () {
    return _type;
  }

  public void setType (String _type) {
    this._type = _type;
  }
}