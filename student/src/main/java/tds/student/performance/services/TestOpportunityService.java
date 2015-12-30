package tds.student.performance.services;

import AIR.Common.Helpers._Ref;
import TDS.Shared.Exceptions.ReturnStatusException;
import tds.student.performance.domain.ClientTestProperty;
import tds.student.performance.domain.TestOpportunity;
import tds.student.sql.data.OpportunityInstance;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Created by jjohnson on 12/26/15.
 */
public interface TestOpportunityService {
    void startTestOpportunity(OpportunityInstance opportunityInstance, String testKey, String formKeyList);

    Float getInitialAbility(TestOpportunity opportunity, ClientTestProperty property);
}
