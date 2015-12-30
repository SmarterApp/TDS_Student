package tds.student.performance.services;

import TDS.Shared.Exceptions.ReturnStatusException;
import tds.student.performance.exceptions.ReturnErrorException;

import java.sql.SQLException;
import java.util.UUID;

public interface LegacyErrorHandlerService {
    void logDbError(String procName, String message, Long testee, String test, Integer opportunity, UUID key);
    void throwReturnErrorException(String client, String procName, String appkey, String argstring, UUID oppkey, String context, String status) throws ReturnErrorException, SQLException, ReturnStatusException;
}
