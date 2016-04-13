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
package tds.student.performance.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tds.dll.common.performance.domain.SetOfAdminSubject;
import tds.student.performance.IntegrationTest;
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
        Assert.assertEquals(Float.valueOf(-1.23998F), result.getStartAbility());
        Assert.assertEquals("SBAC-IRP-CAT-ELA-3", result.getTestId());
        Assert.assertFalse(result.getSegmented());
        Assert.assertEquals("adaptive2", result.getSelectionAlgorithm());
    }

    @Test
    public void should_Get_Null_SetOfAdminSubject_For_Fake_Admin_Subject() {
        Assert.assertNull(itemBankDao.get("FAKE_ADMIN_SUBJECT"));
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

    @Test
    public void should_Get_a_TestSubject_For_SBAC_IRP_CAT_ELA_3_Summer_2015_2016() {
        String adminSubject = "(SBAC_PT)SBAC-IRP-CAT-ELA-3-Summer-2015-2016";

        String testSubject = itemBankDao.getTestSubject(adminSubject);

        Assert.assertEquals("ELA", testSubject);
    }

    @Test
    public void should_Get_Null_TestSubject_For_Fake_Admin_Subject() {
        Assert.assertNull(itemBankDao.getTestSubject("FAKE_ADMIN_SUBJECT"));
    }

    @Test
    public void should_Get_Null_Cohort_For_Fake_Data() {
        Assert.assertNull(itemBankDao.getTestFormCohort("FAKE_TEST_KEY", "FAKE_FORM_KEY"));
    }

    @Test
    public void should_Get_Cohort_Value_For_Math3_PracticeTest() {
        Assert.assertEquals("Default", itemBankDao.getTestFormCohort("(SBAC_PT)SBAC-MATH-3-Spring-2013-2015", "187-507"));
    }
}
