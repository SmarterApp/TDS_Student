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
