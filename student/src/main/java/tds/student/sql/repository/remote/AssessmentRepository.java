package tds.student.sql.repository.remote;

import TDS.Shared.Exceptions.ReturnStatusException;

import tds.assessment.Assessment;

/**
 * Repository to interact with Assessment data
 */
public interface AssessmentRepository {
  /**
   * Retrieves an {@link tds.assessment.Assessment} from the assessment service by the assessment key
   *
   * @param clientName the current envrionment's client name
   * @param key        the key of the {@link tds.assessment.Assessment}
   * @return the fully populated {@link tds.assessment.Assessment}
   * @throws ReturnStatusException
   */
  Assessment findAssessment(final String clientName, final String key) throws ReturnStatusException;
}
