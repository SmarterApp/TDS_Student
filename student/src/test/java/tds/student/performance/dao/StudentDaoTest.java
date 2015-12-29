package tds.student.performance.dao;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

import java.util.List;

import static org.junit.Assert.assertNotNull;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:performance-integration-context.xml")
@TransactionConfiguration
public class StudentDaoTest {

    @Autowired
    StudentDao studentDao;

    @Test
    public void testFindUserByUsername() {

        List<String> studentIds = studentDao.findStudentId(2);
        System.out.println(studentIds);
        assertNotNull(studentIds);
    }


}
