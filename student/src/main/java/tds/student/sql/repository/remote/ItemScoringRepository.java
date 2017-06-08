package tds.student.sql.repository.remote;

import TDS.Shared.Exceptions.ReturnStatusException;
import tds.student.sql.data.ItemResponseUpdate;
import tds.student.sql.data.ItemResponseUpdateStatus;

import java.util.List;
import java.util.UUID;

/**
 * Implementations of this interface are responsible for updating exam item scores.
 */
public interface ItemScoringRepository {

    /**
     * Update and score the given item responses.
     *
     * @param examId            The exam id
     * @param sessionId         The session id
     * @param browserId         The browser id
     * @param clientName        The exam client name
     * @param pageDuration      The page duration
     * @param responseUpdates   The student responses
     * @return The response update status values for the responses
     * @throws ReturnStatusException On an unexpected response
     */
    List<ItemResponseUpdateStatus> updateResponses(final UUID examId,
                                                   final UUID sessionId,
                                                   final UUID browserId,
                                                   final String clientName,
                                                   final Float pageDuration,
                                                   final List<ItemResponseUpdate> responseUpdates) throws ReturnStatusException;
}
