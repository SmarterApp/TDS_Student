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

public class ItemResponseUpdateStatus {
	public int _position;
    public String _status;
    public String _reason;
    public long _dbLatency;

    public ItemResponseUpdateStatus() {}

    public ItemResponseUpdateStatus(int position, String status, String reason, long latency) {
    	_position = position;
    	_status = status;
    	_reason = reason;
    	_dbLatency = latency;
    }

    @JsonProperty("Position")
    public int getPosition() {
    	return _position;
    }
    public void setPosition(int _position) {
    	this._position = _position;
    }
	@JsonProperty("Status")
	public String getStatus() {
		return _status;
	}
	public void setStatus(String _status) {
		this._status = _status;
	}
	@JsonProperty("Reason")
	public String getReason() {
		return _reason;
	}
	public void setReason(String _reason) {
		this._reason = _reason;
	}
	@JsonProperty("DbLatency")
	public long getDbLatency() {
		return _dbLatency;
	}
	public void setDbLatency(long _dbLatency) {
		this._dbLatency = _dbLatency;
	}
}
