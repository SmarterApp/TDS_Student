package tds.student.performance.dao;

import tds.student.performance.domain.SetOfAdminSubject;

/**
 * Created by jjohnson on 12/25/15.
 */
public interface ItemBankDao {
    SetOfAdminSubject get(String adminSubject);
}
