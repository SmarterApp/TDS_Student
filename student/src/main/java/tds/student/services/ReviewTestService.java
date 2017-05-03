package tds.student.services;

import AIR.Common.data.ResponseData;
import TDS.Shared.Exceptions.ReturnStatusException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import tds.student.services.data.TestOpportunity;
import tds.student.web.TestManager;

/**
 * A service for
 */
public interface ReviewTestService {
    /**
     * Move an {@link tds.exam.Exam} to the "review" status, meaning a student has answered all the questions and is
     * ready to review the questions before submitting the exam for scoring.
     *
     * @param testOpportunity The {@link tds.student.services.data.TestOpportunity}
     * @param testManager     The {@link tds.student.web.TestManager} handling the
     *                        {@link tds.student.services.data.TestOpportunity}
     * @return A {@link AIR.Common.data.ResponseData} indicating success or failure
     * @throws ReturnStatusException In the event of a failure
     * @throws IOException           In the event the {@link javax.servlet.http.HttpServletRequest} throws
     */
    ResponseData<String> reviewTest(final TestOpportunity testOpportunity,
                                    final TestManager testManager) throws ReturnStatusException;
}
