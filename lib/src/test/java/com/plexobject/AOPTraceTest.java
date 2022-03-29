package com.plexobject;

import com.plexobject.aop.DynamicLoad;
import com.plexobject.aop.Tracer;
import org.junit.jupiter.api.Test;

public class AOPTraceTest {
    static class Demo {
        @Tracer
        public void runAopDemo() {
            System.out.println("inside runDemo");
        }
    }
    @Test
    void testAop() throws Exception {
        DynamicLoad.checkAdviceClassLoaded();
        DynamicLoad.checkAspectJAgentLoaded();
        DynamicLoad.checkAdviceClassLoaded();
        new Demo().runAopDemo();
    }
}
