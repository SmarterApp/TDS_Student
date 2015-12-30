package tds.student.performance.utils;

import AIR.Common.DB.SQLConnection;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

import java.sql.SQLException;

/**
 * Created by jjohnson on 12/29/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:performance-integration-context.xml")
@TransactionConfiguration
public class LegacySqlConnectionTest {

    @Autowired
    private LegacySqlConnection sqlConnectionHelper;

    @Test
    public void should_Get_an_AIR_SqlConnection() {
        try {
            SQLConnection result = sqlConnectionHelper.get();

            Assert.assertNotNull(result);
        } catch (SQLException e) {
            Assert.fail();
        }
    }
}
