package tds.student.performance.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tds.student.performance.caching.CacheType;
import tds.student.performance.dao.OpportunitySegmentDao;
import tds.student.performance.domain.ItemForTesteeResponse;
import tds.student.performance.domain.OpportunitySegment;
import tds.student.performance.domain.StudentLoginField;
import tds.student.performance.utils.UuidAdapter;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Data Access Object to query testopportunity and  testopportunitysegment
 */
@Repository
public class OpportunitySegmentDaoImpl implements OpportunitySegmentDao {
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    //private static final Logger logger = LoggerFactory.getLogger(OpportunitySegmentDaoImpl.class);

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    @Transactional
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
                "    testopportunity o\n" +
                "        LEFT OUTER JOIN\n" +
                "    testopportunitysegment s ON (s._fk_testopportunity = o._key AND s.segmentposition = :segment)\n" +
                "        LEFT OUTER JOIN\n" +
                "    testeeaccommodations a ON (a._fk_testopportunity = o._key AND a.acctype = 'Language')\n" +
                "WHERE\n" +
                "    _key = :oppKey";

        return namedParameterJdbcTemplate.queryForObject(
                SQL,
                parameters,
                new BeanPropertyRowMapper<>(OpportunitySegment.class));
    }

    // Replaces original T_InsertItems_SP SQL_INSERT2
    @Override
    @Transactional
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
                "            itembank.testformitem F\n" +
                "        WHERE\n" +
                "            F._fk_Item = A._fk_Item\n" +
                "                AND _fk_TestForm = :testForm\n" +
                "                AND F._fk_AdminSubject = :adminSubject) AS formPosition,\n" +
                "    I.answer AS answerKey\n" +
                "FROM\n" +
                "    itembank.tblsetofadminitems A\n" +
                "        LEFT JOIN\n" +
                "    itembank.tblitem I ON (A._fk_ITem = I._Key)\n" +
                "        LEFT JOIN\n" +
                "    itembank.tblitemprops P ON (P._fk_Item = A._fk_Item)\n" +
                "WHERE\n" +
                "    A._fk_adminsubject = :adminSubject\n" +
                "        AND A.groupid = :groupId\n" +
                "        AND P._fk_adminsubject = :adminSubject\n" +
                "        AND P.propname = 'Language'\n" +
                "        AND P.propvalue = :languagePropertyValue\n" +
                "ORDER BY itemposition";


        return namedParameterJdbcTemplate.query(
                SQL,
                parameters,
                new BeanPropertyRowMapper<>(ItemForTesteeResponse.class));

    }

}
