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

import org.opentestsystem.shared.progman.client.ProgManClient;
import org.opentestsystem.shared.progman.client.domain.*;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * ProgManClient implementation for tests with no implementation
 */
public class ProgManClientNullTestImpl implements ProgManClient {
    @Override
    public ClientPropertyConfiguration getConfigurationById(String configId) {
        return null;
    }

    @Override
    public ClientPropertyConfiguration getConfiguration(String componentName, String envName, String overlay) {
        return null;
    }

    @Override
    public TenantChain getTenantChain(Map<TenantType, String> userEntityAttributes) {
        return null;
    }

    @Override
    public Map<String, Object> getCustomAssets(String componentName, Map<TenantType, String> userEntityAttributes) {
        return null;
    }

    @Override
    public List<Tenant> findTenantsByComponentAndSearchVal(String componentName, Boolean inGoodStanding, String searchVal, TenantType tenantType, Pageable pageable) {
        return null;
    }

    @Override
    public Tenant getTenantById(String tenantId) {
        return null;
    }

    @Override
    public List<TenantType> getTenantTypes() {
        return null;
    }

    @Override
    public Component getComponentByName(String componentName) {
        return null;
    }
}
