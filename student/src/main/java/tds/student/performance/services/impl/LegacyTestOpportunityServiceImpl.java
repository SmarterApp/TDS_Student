package tds.student.performance.services.impl;

import TDS.Shared.Exceptions.ReturnStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tds.dll.api.ICommonDLL;
import tds.student.performance.domain.TestOpportunity;
import tds.student.performance.services.LegacyTestOpportunityService;
import tds.student.performance.utils.LegacySqlConnection;

import java.sql.SQLException;

@Service
@Transactional
public class LegacyTestOpportunityServiceImpl implements LegacyTestOpportunityService {
    @Autowired
    ICommonDLL commonDll;

    @Autowired
    LegacySqlConnection legacySqlConnection;

    public void setOpportunityStatus(TestOpportunity opportunity, String status) throws SQLException, ReturnStatusException {
        commonDll.SetOpportunityStatus_SP(legacySqlConnection.get(), opportunity.getKey(), status, true, opportunity.getSessionKey().toString());
    }
}
