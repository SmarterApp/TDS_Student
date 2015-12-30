/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.data;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author temp_rreddy
 * 
 */
public interface IItemResponseUpdate
{
  @JsonProperty ("ItemID")
  public String getItemID ();

  @JsonProperty ("DateCreated")
  public String getDateCreated ();

  @JsonProperty ("Sequence")
  public int getSequence ();

  @JsonProperty ("Page")
  public int getPage ();

  @JsonProperty ("Position")
  public int getPosition ();

  @JsonProperty ("Value")
  public String getValue ();

  @JsonProperty ("IsSelected")
  public boolean getIsSelected ();

  @JsonProperty ("IsValid")
  public boolean getIsValid ();

  @JsonProperty ("ScoreMark")
  public void setScoreMark (UUID _scoreMark);

  @JsonProperty ("FilePath")
  public String getFilePath ();
}
