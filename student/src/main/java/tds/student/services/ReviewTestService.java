package tds.student.services;

import AIR.Common.data.ResponseData;
import TDS.Shared.Exceptions.ReturnStatusException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import tds.student.services.data.TestOpportunity;

/**
 * A service for
 */
public interface ReviewTestService {
    /**
     * Review an
     * @param testOpportunity The {@link tds.student.services.data.TestOpportunity}
     * @param request The {@link javax.servlet.http.HttpServletRequest} representing the HTTP request from the caller
     * @return A {@link AIR.Common.data.ResponseData} indicating success or failure
     * @throws ReturnStatusException In the event of a failure
     * @throws IOException In the event the {@link javax.servlet.http.HttpServletRequest} throws
     */
    ResponseData<String> reviewTest(final TestOpportunity testOpportunity,
                                    final HttpServletRequest request) throws ReturnStatusException, IOException;
}
