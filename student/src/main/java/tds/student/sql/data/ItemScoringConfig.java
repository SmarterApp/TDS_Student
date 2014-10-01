/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ItemScoringConfig {
	
	private String _context;
    private String _itemType;
    private boolean _enabled; // Item_in
    private int _priority;
    private String _serverUrl;

	@JsonProperty ("Context")
    public String getContext () { return _context; }
	
	public void setContext (String context) { _context = context; }
	
	@JsonProperty ("ItemType")
    public String getItemType () { return _itemType; }
	
	public void setItemType (String itemType) { _itemType = itemType; }
	
	@JsonProperty ("Enabled")
    public boolean isEnabled () { return _enabled; } 
	
	public void setEnabled (boolean enabled) { _enabled = enabled; }
	
	@JsonProperty ("Priority")
    public int getPriority () { return _priority; }
	
	public void setPriority (int priority) { _priority = priority; }
	
	@JsonProperty ("ServerUrl")
    public String getServerUrl () { return _serverUrl; }
    
	public void setServerUrl (String serverUrl) { _serverUrl = serverUrl; }
}
