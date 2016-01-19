package tds.student.performance.services;

import AIR.Common.DB.SQLConnection;
import AIR.Common.DB.results.MultiDataResultSet;
import TDS.Shared.Exceptions.ReturnStatusException;

import java.util.Map;

public interface StudentLoginService {

    MultiDataResultSet login(SQLConnection connection, String clientname, Map<String, String> keyValues, String sessionId)
            throws ReturnStatusException;

}
