package tds.student.performance.dao;

import tds.student.performance.domain.SetOfAdminSubject;
import tds.student.sql.data.TestGrade;

import java.util.List;

public interface ItemBankDao {
    SetOfAdminSubject get(String adminSubject);
    List<TestGrade> getTestGrades(String clientName, String testKey, Integer sessionType);
    String getTestSubject(String testKey);
}
