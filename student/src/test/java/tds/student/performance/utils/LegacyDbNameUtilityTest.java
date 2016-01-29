package tds.student.performance.utils;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import tds.student.performance.IntegrationTest;

/**
 * Created by jjohnson on 1/28/16.
 */
public class LegacyDbNameUtilityTest extends IntegrationTest {
    @Autowired
    LegacyDbNameUtility dbNameUtility;

    @Test
    public void should_Replace_ConfigDb_Placeholder_With_Value_From_Settings_File() {
        final String test = "SELECT 'dbNameUtility' from ${configdb}.table_name";

        String result = dbNameUtility.setDatabaseNames(test);

        Assert.assertNotNull(result);
        Assert.assertEquals("SELECT 'dbNameUtility' from configs.table_name", result);
    }

    @Test
    public void should_Replace_Multiple_Occurrences_of_ConfigDb_Placeholder_With_Value_From_Settings_File() {
        final String test = "SELECT 'dbNameUtility' from ${configdb}.table_name, ${configdb}.other_table_name";

        String result = dbNameUtility.setDatabaseNames(test);

        Assert.assertNotNull(result);
        Assert.assertEquals("SELECT 'dbNameUtility' from configs.table_name, configs.other_table_name", result);
    }

    @Test
    public void should_Ignore_Case_When_Replacing_ConfigDb_Placeholder_Text_With_Value_From_Settings_File() {
        final String test = "SELECT 'dbNameUtility' from ${CONFIGDB}.table_name";

        String result = dbNameUtility.setDatabaseNames(test);

        Assert.assertNotNull(result);
        Assert.assertEquals("SELECT 'dbNameUtility' from configs.table_name", result);
    }

    @Test
    public void should_Ignore_Case_When_Replacing_Multiple_Occurrences_of_ConfigDb_Placeholder_Text_With_Value_From_Settings_File() {
        final String test = "SELECT 'dbNameUtility' from ${CONFIGDB}.table_name, ${ConfigDb}.other_table_name";

        String result = dbNameUtility.setDatabaseNames(test);

        Assert.assertNotNull(result);
        Assert.assertEquals("SELECT 'dbNameUtility' from configs.table_name, configs.other_table_name", result);
    }

    @Test
    public void should_Replace_Various_Mixed_Case_Placeholders_With_Value_From_Settings_File() {
        final String test = "SELECT 'dbNameUtility' from ${CONFIGDB}.table_name, ${ItemBankDb}.table_name, ${sessionDb}.table_name, ${ARCHIVEdb}.table_name";

        String result = dbNameUtility.setDatabaseNames(test);

        Assert.assertNotNull(result);
        Assert.assertEquals("SELECT 'dbNameUtility' from configs.table_name, itembank.table_name, session.table_name, archive.table_name", result);
    }

    @Test
    public void should_Get_Configured_Database_Name_For_Archive_Database() {
        String result = dbNameUtility.getDatabaseName(LegacyDbNameUtility.Databases.Archive);

        Assert.assertNotNull(result);
        Assert.assertEquals("archive", result);
    }

    @Test
    public void should_Get_Configured_Database_Name_For_Config_Database() {
        String result = dbNameUtility.getDatabaseName(LegacyDbNameUtility.Databases.Config);

        Assert.assertNotNull(result);
        Assert.assertEquals("configs", result);
    }

    @Test
    public void should_Get_Configured_Database_Name_For_Itembank_Database() {
        String result = dbNameUtility.getDatabaseName(LegacyDbNameUtility.Databases.Itembank);

        Assert.assertNotNull(result);
        Assert.assertEquals("itembank", result);
    }

    @Test
    public void should_Get_Configured_Database_Name_For_Session_Database() {
        String result = dbNameUtility.getDatabaseName(LegacyDbNameUtility.Databases.Session);

        Assert.assertNotNull(result);
        Assert.assertEquals("session", result);
    }
}

