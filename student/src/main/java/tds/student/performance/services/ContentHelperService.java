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
package tds.student.performance.services;

import TDS.Shared.Exceptions.ReturnStatusException;
import tds.itemrenderer.data.IITSDocument;
import tds.student.performance.domain.AccLookupWrapper;

/**
 * This service is called from ContentService and is here in order to add caching to one of the calls that relies on AccLookup.
 * ContentService will now wrap AccLookup in a AccLookupWrapper and call this helper method which will be cacheable;
 */
public interface ContentHelperService {
    IITSDocument getContent(String xmlFilePath, AccLookupWrapper accommodations) throws ReturnStatusException;
}
