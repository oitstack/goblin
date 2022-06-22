package io.github.oitstack.goblin.spi;

import io.github.oitstack.goblin.spi.testrule.TestRuleManager;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author yangguang
 * @date 2022/5/27
 */
@RunWith(MockitoJUnitRunner.class)
public class TestRuleManagerTest {

    @Test
    public void pluginsLoadTest() {
        Assert.assertTrue(TestRuleManager.getInstance().getTestRules().size() == 2);
    }
}
