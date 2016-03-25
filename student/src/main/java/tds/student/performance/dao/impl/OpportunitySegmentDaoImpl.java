/*******************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2016 Regents of the University of California
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 *
 * SmarterApp Open Source Assessment Software Project: http://smarterapp.org
 * Developed by Fairway Technologies, Inc. (http://fairwaytech.com)
 * for the Smarter Balanced Assessment Consortium (http://smarterbalanced.org)
 ******************************************************************************/
package tds.student.performance.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Repository;
import tds.dll.common.performance.caching.CacheType;
import tds.dll.common.performance.utils.LegacyDbNameUtility;
import tds.dll.common.performance.utils.UuidAdapter;
import tds.student.performance.dao.OpportunitySegmentDao;
import tds.student.performance.domain.InsertTesteeResponse;
import tds.student.performance.domain.ItemForTesteeResponse;
import tds.student.performance.domain.OpportunitySegment;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.*;

/**
 * Data Access Object to query testopportunity and  testopportunitysegment
 */
@Repository
public class OpportunitySegmentDaoImpl implements OpportunitySegmentDao {
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private static final Logger logger = LoggerFactory.getLogger(OpportunitySegmentDaoImpl.class);

    @Autowired
    private LegacyDbNameUtility dbNameUtility;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public OpportunitySegment getOpportunitySegmentAccommodation(UUID oppKey, Integer segment) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("oppKey", UuidAdapter.getBytesFromUUID(oppKey));
        parameters.put("segment", segment);


        final String SQL = "SELECT \n" +
                "    o.clientname AS clientName,        \n" +
                "    o._efk_adminsubject AS testKey,    \n" +
                "    o.restart AS restart,              \n" +
                "    o.status,                          \n" +
                "    o.environment,                     \n" +
                "    o._efk_testee AS testee,\n" +
                "    s._efk_segment AS segmentKey,\n" +
                "    s.formkey AS formKey,              \n" +
                "    s.algorithm,                       \n" +
                "    a.acccode AS language        \n" +
                "FROM\n" +
                "    ${sessiondb}.testopportunity o\n" +
                "LEFT OUTER JOIN\n" +
                "    ${sessiondb}.testopportunitysegment s ON (s._fk_testopportunity = o._key AND s.segmentposition = :segment)\n" +
                "LEFT OUTER JOIN\n" +
                "    ${sessiondb}.testeeaccommodations a ON (a._fk_testopportunity = o._key AND a.acctype = 'Language')\n" +
                "WHERE\n" +
                "    _key = :oppKey";

        try {
            return namedParameterJdbcTemplate.queryForObject(
                    dbNameUtility.setDatabaseNames(SQL),
                    parameters,
                    new BeanPropertyRowMapper<>(OpportunitySegment.class));
        } catch (DataAccessException exception) {
            logger.error(String.format("%s SELECT threw exception", SQL), exception);
            return null;
        }
    }

    // Replaces original T_InsertItems_SP SQL_INSERT2
    @Override
    @Cacheable(CacheType.MediumTerm)
    public List<ItemForTesteeResponse> getItemForTesteeResponse(String adminSubject, String testForm, String groupId, String languagePropertyValue ) {

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("adminSubject", adminSubject);
        parameters.put("testForm", testForm);
        parameters.put("groupId", groupId);
        parameters.put("languagePropertyValue", languagePropertyValue);


        final String SQL = "SELECT \n" +
                "    A._fk_Item AS bankItemKey,\n" +
                "    A.itemposition AS itemPosition,\n" +
                "    I._efk_itembank AS bankKey,\n" +
                "    I._efk_item AS itemKey,\n" +
                "    A.irt_b AS irtb,\n" +
                "    I.scorepoint AS scorePoint,\n" +
                "    I.itemtype AS itemType,\n" +
                "    A.isfieldtest AS isFieldTest,\n" +
                "    A.isrequired AS isRequired,\n" +
                "    A._fk_strand AS contentLevel,\n" +
                "    (SELECT \n" +
                "            F.formposition\n" +
                "        FROM\n" +
                "            ${itembankdb}.testformitem F\n" +
                "        WHERE\n" +
                "            F._fk_Item = A._fk_Item\n" +
                "                AND _fk_TestForm = :testForm\n" +
                "                AND F._fk_AdminSubject = :adminSubject) AS formPosition,\n" +
                "    I.answer AS answerKey\n" +
                "FROM\n" +
                "    ${itembankdb}.tblsetofadminitems A\n" +
                "        LEFT JOIN\n" +
                "    ${itembankdb}.tblitem I ON (A._fk_ITem = I._Key)\n" +
                "        LEFT JOIN\n" +
                "    ${itembankdb}.tblitemprops P ON (P._fk_Item = A._fk_Item)\n" +
                "WHERE\n" +
                "    A._fk_adminsubject = :adminSubject\n" +
                "        AND A.groupid = :groupId\n" +
                "        AND P._fk_adminsubject = :adminSubject\n" +
                "        AND P.propname = 'Language'\n" +
                "        AND P.propvalue = :languagePropertyValue\n" +
                "ORDER BY itemposition";


        return namedParameterJdbcTemplate.query(
                dbNameUtility.setDatabaseNames(SQL),
                parameters,
                new BeanPropertyRowMapper<>(ItemForTesteeResponse.class));

    }

    @Override
    public Boolean existsTesteeResponsesByBankKeyAndOpportunity(UUID oppKey, List<String> itemKeys) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("itemKeys", itemKeys);
        parameters.addValue("oppKey", UuidAdapter.getBytesFromUUID(oppKey));

        final String SQL = "SELECT \n" +
                "    COUNT(*)\n" +
                "FROM\n" +
                "    ${sessiondb}.testeeresponse R\n" +
                "WHERE\n" +
                "    R._fk_testopportunity = :oppKey\n" +
                "        AND _efk_itemkey IN (:itemKeys);";

        int count = namedParameterJdbcTemplate.queryForObject(
                dbNameUtility.setDatabaseNames(SQL),
                parameters,
                Integer.class);

        return count > 0;
    }


    @Override
    public String loadInsertTableForTesteeResponses(Connection connection,  List<InsertTesteeResponse> itemList) {

        // todo: table name
        // todo: create/replace

        SingleConnectionDataSource singleDataSource = new SingleConnectionDataSource(connection, false);
        JdbcTemplate singleTemplate = new JdbcTemplate(singleDataSource);
        NamedParameterJdbcTemplate singleNamedTemplate = new NamedParameterJdbcTemplate(singleDataSource);

        String tempTableName = String.format ("%s%s", "inserts", UUID.randomUUID().toString ().replace ('-', 'z'));


        final String SQL = "CREATE TEMPORARY TABLE " + tempTableName + " (\n" +
                "  Format varchar(50),\n" +
                "  IsRequired bit,\n" +
                "  relativePosition int,\n" +
                "  b float,\n" +
                "  answer varchar(10),\n" +
                "  IsFieldTest bit,\n" +
                "  _efk_ITSItem bigint,\n" +
                "  Position int,\n" +
                "  bankkey bigint,\n" +
                "  Scorepoint int,\n" +
                "  bankitemkey varchar(50),\n" +
                "  formPosition int,\n" +
                "  contentLevel varchar(200)\n" +
                ") ENGINE = MEMORY\n";

        singleTemplate.execute(SQL);

        Integer nullItemCount = 0;
        List<SqlParameterSource> parameters = new ArrayList<>();
        for (InsertTesteeResponse item : itemList) {
            parameters.add(new MapSqlParameterSource()
                    .addValue("Format", item.getItemType())
                    .addValue("IsRequired", item.getIsRequired())
                    .addValue("relativePosition", item.getItemPosition())
                    .addValue("b", item.getIrtb())
                    .addValue("answer", item.getAnswerKey())
                    .addValue("IsFieldTest", item.getIsFieldTest())
                    .addValue("efk_ITSItem", item.getItemKey())
                    .addValue("Position", item.getPosition())
                    .addValue("bankkey", item.getBankKey())
                    .addValue("Scorepoint", item.getScorePoint())
                    .addValue("bankitemkey", item.getBankItemKey())
                    .addValue("formPosition", item.getFormPosition())
                    .addValue("contentLevel", item.getContentLevel()));
            if (item.getAnswerKey() == null) {
                nullItemCount++;
            }
        }

        // todo: spring 3.2.6 and 4.0 fixes the harmless exception thrown and caught causes minor performance issue.
        // todo: answer is the only field expected to be null so can be fixed by minor logic.
        String sqlInsert;
        if (nullItemCount < itemList.size()) {
            sqlInsert =
                    "INSERT INTO " + tempTableName + " \n" +
                            "(Format, IsRequired, relativePosition, b, answer, IsFieldTest, _efk_ITSItem, Position, bankkey, Scorepoint, bankitemkey, formPosition, contentLevel)\n" +
                            "VALUES(:Format, :IsRequired, :relativePosition, :b, :answer, :IsFieldTest, :efk_ITSItem, :Position, :bankkey, :Scorepoint, :bankitemkey, :formPosition, :contentLevel)";
        } else {
            sqlInsert =
                    "INSERT INTO " + tempTableName + " \n" +
                            "(Format, IsRequired, relativePosition, b, IsFieldTest, _efk_ITSItem, Position, bankkey, Scorepoint, bankitemkey, formPosition, contentLevel)\n" +
                            "VALUES(:Format, :IsRequired, :relativePosition, :b, :IsFieldTest, :efk_ITSItem, :Position, :bankkey, :Scorepoint, :bankitemkey, :formPosition, :contentLevel)";
        }

        singleNamedTemplate.batchUpdate(sqlInsert, parameters.toArray(new SqlParameterSource[itemList.size()]));

        // for debug
        //String sqlQuery = "select * from " + tempTableName;
        //List<Map<String, Object>> mapList =  singleTemplate.queryForList(sqlQuery);

        return tempTableName;
    }

    @Override
    public void dropTempTable(Connection connection, String tableName) {

        SingleConnectionDataSource singleDataSource = new SingleConnectionDataSource(connection, false);
        JdbcTemplate singleTemplate = new JdbcTemplate(singleDataSource);

        final String SQL = "DROP TEMPORARY TABLE " + tableName;

        singleTemplate.execute(SQL);
        logger.debug("Dropped temp table {}", tableName);
    }


}
