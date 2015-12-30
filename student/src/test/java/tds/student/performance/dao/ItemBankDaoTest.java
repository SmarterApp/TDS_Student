package tds.student.performance.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tds.student.performance.IntegrationTest;
import tds.student.performance.domain.SetOfAdminSubject;

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
}
