package tds.student.performance.caching;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import tds.student.performance.IntegrationTest;
import tds.student.performance.caching.impl.CachingServiceImpl;

//import java.time.Clock;
import java.util.Date;
import java.util.concurrent.Callable;


public class CachingServiceTest extends IntegrationTest {
    @Autowired
    CachingServiceImpl cachingService;

    Integer shortTermExpireMilliseconds = 3500;
    Integer mediumTermExpireMilliseconds = 5500;
    Integer longTermExpireMilliseconds = 10500;

    @Test
    public void should_Get_Null_When_Key_Is_Missing() {
        Object result = cachingService.getValue("test");
        Assert.assertNull(result);
    }

    @Test
    public void should_Get_Null_When_Key_Is_Missing_For_Specific_Cache() {
        Object result = cachingService.getValue(CacheType.MediumTerm, "test");
        Assert.assertNull(result);
    }

    @Test
    public void should_Get_Value_After_Set() {
        String cacheKey = "shortTermValue1";
        Object result = cachingService.getValue(CacheType.ShortTerm, cacheKey);
        Assert.assertNull(result);

        Integer cacheValue = 11;
        cachingService.setValue(CacheType.ShortTerm, cacheKey, cacheValue);

        result = cachingService.getValue(CacheType.ShortTerm, cacheKey);

        Assert.assertEquals(cacheValue, result);
    }

    @Test
    public void should_Get_Null_After_Cache_Expires() {
        String cacheKey = "shortTermValue2";
        Object result = cachingService.getValue(CacheType.ShortTerm, cacheKey);
        Assert.assertNull(result);

        Integer cacheValue = 11;
        cachingService.setValue(CacheType.ShortTerm, cacheKey, cacheValue);

        sleep(shortTermExpireMilliseconds);

        result = cachingService.getValue(CacheType.ShortTerm, cacheKey);

        Assert.assertNull(result);
    }

    @Test
    public void should_Get_Null_After_Calling_Remove() {
        String cacheKey = "longTermValue1";
        String cacheValue = "some value";
        cachingService.setValue(CacheType.LongTerm, cacheKey, cacheValue);

        Object result = cachingService.getValue(CacheType.LongTerm, cacheKey);
        Assert.assertEquals(cacheValue, result);

        cachingService.remove(CacheType.LongTerm, cacheKey);
        result = cachingService.getValue(CacheType.LongTerm, cacheKey);
        Assert.assertNull(result);
    }

    @Test(expected = NullPointerException.class)
    public void should_Throw_Exception_When_Cache_Name_Does_Not_Exist() {
        cachingService.getValue("RandomCacheNameThatDoesNotExist", "key");
    }

    @Test
    public void should_Get_Null_After_Calling_RemoveAll() {
        String cacheKey1 = "mediumTermValue1";
        String cacheKey2 = "mediumTermValue2";

        cachingService.setValue(CacheType.MediumTerm, cacheKey1, "value1");
        cachingService.setValue(CacheType.MediumTerm, cacheKey2, "value2");

        cachingService.removeAll(CacheType.MediumTerm);

        Object result1 = cachingService.getValue(CacheType.MediumTerm, cacheKey1);
        Object result2 = cachingService.getValue(CacheType.MediumTerm, cacheKey2);
        Assert.assertNull(result1);
        Assert.assertNull(result2);
    }

    @Test
    public void should_Default_To_ShortTerm_Cache() {
        String cacheKey = "defaultValue";
        Integer cacheValue = 12;

        cachingService.setValue(cacheKey, cacheValue);
        Object result = cachingService.getValue(cacheKey);
        Assert.assertEquals(cacheValue, result);

        sleep(shortTermExpireMilliseconds);

        // if this was the short term cache (as the default should be) then it will be expired by now
        result = cachingService.getValue(cacheKey);
        Assert.assertNull(result);
    }

    @Test
    public void should_Cache_Callback_Method_Value_When_Does_Not_Exist() throws Exception {
        final String now = new Date().toString();
        String cacheKey = "callback1";

        Callable<String> callable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                return echoBack(now);
            }
        };

        String result = cachingService.cacheableCall(callable, cacheKey);
        Assert.assertEquals(now, result);

        sleep(100);

        final String now2 = new Date().toString();
        callable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                return echoBack(now2);
            }
        };

        result = cachingService.cacheableCall(callable, cacheKey);
        Assert.assertEquals(now, result);

        sleep(shortTermExpireMilliseconds);

        result = cachingService.cacheableCall(callable, cacheKey);
        Assert.assertEquals(now2, result);
    }

    @Test
    public void should_Get_Null_When_Cache_Disabled() {
        cachingService.setEnabled(false);

        String cacheKey = "disabledKey";
        cachingService.setValue(cacheKey, 1);
        Object result = cachingService.getValue(cacheKey);

        Assert.assertNull(result);

        Assert.assertEquals(false, cachingService.isEnabled());

        cachingService.setEnabled(true);
    }


    private String echoBack(String message) {
        return message;
    }

    private void sleep(Integer milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
