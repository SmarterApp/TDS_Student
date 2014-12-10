package tds.iris.web.data;

import tds.itemrenderer.data.ITSTypes.ITSEntityType;
import tds.itemrenderer.data.ItsItemIdUtil;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class ContentRequestEntity
{
  private String        _id;
  private long          _bankKey = 0;
  private long          _itemKey = 0;

  @JsonIgnore
  // Shiva: do i need it on this one as well ?
  private ITSEntityType _itsDocType;

  @JsonProperty ("id")
  public String getId () {

    if (getBankKey () > 0 && getItemKey () > 0)
      return ItsItemIdUtil.getItsDocumentId (getBankKey (), getItemKey (), getItsType ());

    return _id;
  }

  public void setId (String value) {
    _id = value;
  }

  @JsonProperty ("bankKey")
  public long getBankKey () {
    return _bankKey;
  }

  public void setBankKey (long value) {
    _bankKey = value;
  }

  @JsonProperty ("itemKey")
  public long getItemKey () {
    return _itemKey;
  }

  public void setItemKey (long value) {
    _itemKey = value;
  }

  @JsonIgnore
  public ITSEntityType getItsType () {
    return _itsDocType;
  }

  protected void setItsType (ITSEntityType value) {
    _itsDocType = value;
  }

  @Override
  public String toString () {
    return getId ();
  }

}
