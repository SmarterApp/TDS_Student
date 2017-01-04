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
