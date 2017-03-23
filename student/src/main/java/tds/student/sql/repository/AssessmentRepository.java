package tds.student.sql.repository;

import TDS.Shared.Exceptions.ReturnStatusException;

import java.util.List;

import tds.accommodation.Accommodation;
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

  /**
   * @param clientName    the current envrionment's client name
   * @param assessmentKey the key of the {@link tds.assessment.Assessment}
   * @return the list of all assessment {@link tds.accommodation.Accommodation}
   * @throws ReturnStatusException
   */
  List<Accommodation> findAccommodations(final String clientName, final String assessmentKey) throws ReturnStatusException;
}
