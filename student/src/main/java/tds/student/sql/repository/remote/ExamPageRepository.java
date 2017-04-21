package tds.student.sql.repository.remote;

import TDS.Shared.Exceptions.ReturnStatusException;

import java.util.List;
import java.util.UUID;

import tds.exam.ExamPage;
import tds.student.sql.data.OpportunityInstance;

/**
 * Repository for interacting with exam pages in Exam Service
 */
public interface ExamPageRepository {
    List<ExamPage> findAllPagesWithItems(final OpportunityInstance opportunityInstance) throws ReturnStatusException;
}
