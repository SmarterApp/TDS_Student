package tds.student.performance.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tds.student.performance.dao.ConfigurationDao;
import tds.student.performance.domain.ClientSystemFlag;
import tds.student.performance.services.ConfigurationService;

import java.util.List;

@Service
@Transactional
public class ConfigurationServiceImpl implements ConfigurationService {
    @Autowired
    ConfigurationDao configurationDao;

    @Override
    public Boolean isFlagOn(String clientName, String auditObject) {
        List<ClientSystemFlag> flags = configurationDao.getSystemFlags(clientName);

        return clientSystemFlagIsOn(flags, auditObject, clientName);
    }

    /**
     * Determine if a {@code ClientSystemFlag} is enabled for the specified audit object (i.e. the name of the flag in
     * question) and client name combination.
     * <p>
     *     !!!! CODE SMELL !!!!
     *     This should be moved into some sort of helper and/or service for dealing with configuration settings.  It can
     *     live here for now, but this should definitely be refactored.
     * </p>
     * @param systemFlags The collection of {@code ClientSystemFlag} records to inspect.
     * @param auditObject The name of the audit object to look for.
     * @param clientName The name of the client.
     * @return {@code True} if the specified audit object is set to "On" for the client; otherwise {@code False}.
     */
    private Boolean clientSystemFlagIsOn(List<ClientSystemFlag> systemFlags, String auditObject, String clientName) {
        if (systemFlags == null || systemFlags.size() == 0) {
            return false;
            // TODO:  throw exception instead.
        }

        ClientSystemFlag flagToFind = new ClientSystemFlag(auditObject, clientName);

        return systemFlags.contains(flagToFind)
                ? systemFlags.get(systemFlags.indexOf(flagToFind)).getIsOn()
                : false;
    }
}