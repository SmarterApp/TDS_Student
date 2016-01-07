package tds.student.performance.services.impl;

import org.aspectj.weaver.ast.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tds.student.performance.dao.ConfigurationDao;
import tds.student.performance.domain.*;
import tds.student.performance.services.ConfigurationService;

import java.security.Timestamp;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ConfigurationServiceImpl implements ConfigurationService {
    // Represents the DataBaseTable assignedTbl = getDataBaseTable ("stfpAssigned")
    private class StfpAssigned {
        private String testKey;
        private String window;
        private Integer windowMax;
        private String mode;
        private Timestamp startDate;
        private Timestamp endDate;
        private String frmKey;
        private Integer count;
    }

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
     *
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

    @Override
    public String selectTestFormDriver(TestOpportunity testOpportunity, TestSession testSession, String formList) {
        // formRts is an Integer in legacy code, but can be a boolean.  Number doesn't matter; all that matters is if
        // it's greater than 0.
        Boolean formRts = false;
        String formCohort = null;

        ClientTestMode clientTestMode = configurationDao.getClientTestMode(testOpportunity);
        if (clientTestMode.getRequireRtsForm() || clientTestMode.getRequireRtsFormWindow()) {
            formRts = true;
        }

        // NOTE:  When the legacy _InitializeStudentOpportunity calls _SelectTestForm_Driver_SP (the legacy version of
        // this method), the formCohort that is passed in will always be null.  The formCohort that is passed in isn't
        // populated/dealt with until after the call to _SelectTestForm_Driver_SP.  As a matter of fact, when formCohort
        // is declared, it is explicitly set to null and then passed into this method.
        if (/*formCohort == null
                && */ (formRts || formList != null || clientTestMode.getRequireRtsFormIfExists())) {
            selectTestFormPredetermined(testOpportunity, testSession, formList);
        } else {
            selectTestFormEqDist(testOpportunity, formList);
        }

        return "not yet implemented";
    }

    /**
     *
     * <p>
     *     This method emulates the functionality of {@code StudentDLL._SelectTestForm_Predetermined_SP}.
     * </p>
     *
     * @param testOpportunity
     * @param formList
     */
    private void selectTestFormPredetermined(TestOpportunity testOpportunity, TestSession testSession, String formList) {

    }

    private void selectTestFormEqDist(TestOpportunity testOpportunity, String formList) {

    }

    /**
     * <p>
     *     This method emulates the functionality of {@code StudentDLL._GetTesteeTestForms_SP}.
     * </p>
     *
     * @param testOpportunity
     * @param testSession
     * @param formList
     */
    private List<TestFormWindow> getTesteeTestForms(TestOpportunity testOpportunity, TestSession testSession, String formList) {
        List<TestFormWindow> testFormWindows = configurationDao.getTestFormWindows(testOpportunity, testSession);

        // If 'guest' testees are allowed into the system, then they qualify for all forms by default since there is no
        // RTS data for them.
        if (testOpportunity.getTestee() < 0) {
            return testFormWindows;
        }

        return null;
    }
}
