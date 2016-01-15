package tds.student.performance.services.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tds.student.performance.dao.ConfigurationDao;
import tds.student.performance.domain.*;
import tds.student.performance.services.ConfigurationService;

import java.security.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
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
        List<TestFormWindow> testFormWindows = getTesteeTestForms(testOpportunity, testSession, formList);
    }

    /**
     *
     * <p>
     *     This method emulates the functionality of {@code StudentDLL._SelectTestForm_EqDist_SP}.
     * </p>
     *
     * @param testOpportunity
     * @param formList
     */
    private void selectTestFormEqDist(TestOpportunity testOpportunity, String formList) {
        // TODO:  call legacy version;
    }

    /**
     * <p>
     *     This method emulates the functionality of {@code StudentDLL._GetTesteeTestForms_SP}.
     *     {@code StudentDLL._GetTesteeTestForms_SP} is called from:
     *     * (@code StudentDLL._GetTesteeTestWindows_SP}
     *     * {@code StudentDLL._SelectTestForm_Predetermined_SP}
     * </p>
     *
     * @param testOpportunity
     * @param testSession
     * @param formList
     */
    private List<TestFormWindow> getTesteeTestForms(TestOpportunity testOpportunity, TestSession testSession, String formList) {
        List<TestFormWindow> tblName = configurationDao.getTestFormWindows(testOpportunity, testSession);

        // If 'guest' testees are allowed into the system, then they qualify for all forms by default since there is no
        // RTS data for them.
        if (testOpportunity.getTestee() < 0) {
            return tblName;
        }

        TideTesteeTestWindowDto tideTesteeTestWindowDto = configurationDao.getTideTesteeTestWindowDto(
                testOpportunity,
                testSession);

        if (formList != null) {
            if (formList.indexOf(':') > -1) {
                tideTesteeTestWindowDto.setRequireFormWindow(true);
            } else {
                tideTesteeTestWindowDto.setRequireForm(true);
                tideTesteeTestWindowDto.setRequireFormWindow(false);
            }
        } else if (tideTesteeTestWindowDto.getTideId() != null && tideTesteeTestWindowDto.getFormField() != null) {
            // TODO:  call this:  StudentDLL._rtsDll._GetRTSAttribute_SP (connection, clientname, testee, formField, formListRef);
        } // TODO:  There is no "else"; what should we do if none of the conditions above are satisfied?

        if (formList != null && tideTesteeTestWindowDto.getTideId() != null) {
            // key = form, value = WID
            Map<String, String> formsTbl = new HashMap<>();
            final String[] rows = StringUtils.split(formList, ';');
            for (String row : rows) {
                // Before colon delimiter = WID value.  After colon delimiter = form value.
                final String[] splitRow = StringUtils.split(row, ':');
                if (splitRow.length == 2) {
                    String form = splitRow[1];
                    String wid = splitRow[0];
                    formsTbl.put(form, wid);
                } else {
                    // There was no split on the colon delimiter, so stuff the whole record in the key.
                    formsTbl.put(splitRow[0], null);
                }
            }
        }

        return null;
    }
}
