package tds.student.sql.repository.remote;

import TDS.Shared.Exceptions.ReturnStatusException;

import java.util.UUID;

import tds.exam.item.PageGroupRequest;
import tds.student.services.data.PageGroup;
import tds.student.sql.data.OpportunityInstance;

/**
 * Repository for interacting with item and response data in Exam Service
 */
public interface ExamItemResponseRepository {

    /**
     * Verifies access and marks an item for review
     *
     * @param opportunityInstance the {@link tds.student.sql.data.OpportunityInstance} containing exam validation data
     * @param position            the item position of the item to mark
     * @param mark                a flag indicating whether the item should be marked or unmarked
     */
    void markItemForReview(OpportunityInstance opportunityInstance, int position, boolean mark) throws ReturnStatusException;

    PageGroup getNextItemGroup(UUID id, PageGroupRequest pageGroupRequest) throws ReturnStatusException;
}
