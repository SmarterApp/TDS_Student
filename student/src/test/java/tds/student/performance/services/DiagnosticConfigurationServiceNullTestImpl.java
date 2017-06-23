/***************************************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2017 Regents of the University of California
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 *
 * SmarterApp Open Source Assessment Software Project: http://smarterapp.org
 * Developed by Fairway Technologies, Inc. (http://fairwaytech.com)
 * for the Smarter Balanced Assessment Consortium (http://smarterbalanced.org)
 **************************************************************************************************/

package tds.student.performance.services;


import tds.dll.common.diagnostic.domain.Configuration;
import tds.dll.common.diagnostic.services.DiagnosticConfigurationService;

import java.util.List;

/**
 * Used to auto-wire into the tests
 */
public class DiagnosticConfigurationServiceNullTestImpl implements DiagnosticConfigurationService {
    @Override
    public Configuration getConfiguration(List<String> list) {
        return null;
    }
}
