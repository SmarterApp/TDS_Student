/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.services.abstractions;

import java.util.UUID;

import TDS.Shared.Exceptions.ReturnStatusException;

/**
 * @author efurman
 *
 */
public interface IGlobalService
{
   void recordError(String application, String msg, String stackTrace, 
       UUID applicationContextId,
       String clientIp, long testee, String test, UUID oppKey, String proc) ;
}
