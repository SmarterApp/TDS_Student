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
public interface IItemResponseScorable
{
  @JsonProperty ("TestKey")
  public String getTestKey ();
  
  @JsonProperty ("TestID")
  public String getTestID ();

  @JsonProperty ("Language")
  public String getLanguage ();

  @JsonProperty ("BankKey")
  public long getBankKey ();

  @JsonProperty ("ItemKey")
  public long getItemKey ();

  @JsonProperty ("Position")
  public int getPosition ();

  @JsonProperty ("Sequence")
  public int getSequence ();

  @JsonProperty ("ScoreMark")
  public UUID getScoreMark ();

  @JsonProperty ("Value")
  public String getValue ();

  @JsonProperty ("ItemID")
  public String getItemID ();

  @JsonProperty ("SegmentID")
  public String getSegmentID ();

  @JsonProperty ("FilePath")
  public String getFilePath ();
}
