package tds.student.performance.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tds.student.performance.IntegrationTest;
import tds.student.performance.domain.SetOfAdminSubject;
import tds.student.sql.data.TestGrade;

import java.util.List;

/**
 * Created by jjohnson on 12/25/15.
 */
public class ItemBankDaoTest extends IntegrationTest {
    @Autowired
    ItemBankDao itemBankDao;

    /**
     * Record used to conduct testing:
     # maxItems, startAbility
     '8', '-1.23998'
     */
    @Test
    public void should_Get_a_SetOfAdminSubject_For_SBAC_IRP_CAT_ELA_3_Summer_2015_2016() {
        String adminSubject = "(SBAC_PT)SBAC-IRP-CAT-ELA-3-Summer-2015-2016";

        SetOfAdminSubject result = itemBankDao.get(adminSubject);

        Assert.assertNotNull(result);
        Assert.assertEquals(adminSubject, result.getKey());
        Assert.assertEquals((Integer)8, result.getMaxItems());
        Assert.assertEquals(Double.valueOf(-1.23998), result.getStartAbility());
    }

    @Test
    public void should_Get_Empty_List_For_Incorrect_Data() {
        List<TestGrade> grades = itemBankDao.getTestGrades("FAKE_CLIENT", null, 0);

        Assert.assertEquals(0, grades.size());
    }

    @Test
    public void should_Get_List_Of_All_Grades_For_SBAC_PT_And_No_TestKey() {
        List<TestGrade> grades = itemBankDao.getTestGrades("SBAC_PT", null, 0);

        Assert.assertEquals(10, grades.size());
        Assert.assertTrue(grades.contains(new TestGrade("3")));
        Assert.assertTrue(grades.contains(new TestGrade("4")));
        Assert.assertTrue(grades.contains(new TestGrade("5")));
        Assert.assertTrue(grades.contains(new TestGrade("6")));
        Assert.assertTrue(grades.contains(new TestGrade("7")));
        Assert.assertTrue(grades.contains(new TestGrade("8")));
        Assert.assertTrue(grades.contains(new TestGrade("9")));
        Assert.assertTrue(grades.contains(new TestGrade("10")));
        Assert.assertTrue(grades.contains(new TestGrade("11")));
        Assert.assertTrue(grades.contains(new TestGrade("12")));
    }
}
