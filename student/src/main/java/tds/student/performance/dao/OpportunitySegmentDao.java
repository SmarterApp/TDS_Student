package tds.student.performance.dao;

import tds.student.performance.domain.InsertTesteeResponse;
import tds.student.performance.domain.ItemForTesteeResponse;
import tds.student.performance.domain.OpportunitySegment;

import java.sql.Connection;
import java.util.List;
import java.util.UUID;

public interface OpportunitySegmentDao {

    OpportunitySegment getOpportunitySegmentAccommodation(UUID oppKey, Integer segment);

    List<ItemForTesteeResponse> getItemForTesteeResponse(String adminSubject, String testForm, String groupId, String languagePropertyValue);

    Boolean existsTesteeResponsesByBankKeyAndOpportunity(UUID oppKey, List<String> itemKeys);

    String loadInsertTableForTesteeResponses(Connection connection,  List<InsertTesteeResponse> itemList);

    void dropTempTable(Connection connection, String tableName);


    }
