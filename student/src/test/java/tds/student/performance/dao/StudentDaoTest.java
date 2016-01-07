package tds.student.performance.dao;


import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tds.student.performance.IntegrationTest;

import java.util.List;

import static org.junit.Assert.assertNotNull;


public class StudentDaoTest extends IntegrationTest {

    @Autowired
    StudentDao studentDao;

    @Test
    public void testFindUserByUsername() {

        List<String> studentIds = studentDao.findStudentId(2);
        System.out.println(studentIds);
        assertNotNull(studentIds);
    }


}
