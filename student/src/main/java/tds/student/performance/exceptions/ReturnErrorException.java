/*******************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2016 Regents of the University of California
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 *
 * SmarterApp Open Source Assessment Software Project: http://smarterapp.org
 * Developed by Fairway Technologies, Inc. (http://fairwaytech.com)
 * for the Smarter Balanced Assessment Consortium (http://smarterbalanced.org)
 ******************************************************************************/
package tds.student.performance.exceptions;

public class ReturnErrorException extends Exception {


    private String status;
    private String reason;
    private String context;
    private String appKey;

    public ReturnErrorException(String message) {
        super(message);
    }

    public ReturnErrorException(String status, String reason, String context, String appKey) {
        super(String.format("Error status: %s, reason: %s, context: %s, appKey: %s", status, reason, context, appKey));

        this.status = status;
        this.reason = reason;
        this.context = context;
        this.appKey = appKey;
    }

    public String getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public String getContext() {
        return context;
    }

    public String getAppKey() {
        return appKey;
    }
}
