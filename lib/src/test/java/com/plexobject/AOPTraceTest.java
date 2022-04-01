package com.plexobject;

import com.plexobject.aop.DynamicLoad;
import com.plexobject.aop.Tracer;
import com.plexobject.db.DatabaseStore;
import com.plexobject.deps.ShowDepend;
import com.plexobject.deps.ShowDepends;
import org.junit.jupiter.api.Test;

public class AOPTraceTest {
    static class Demo {
        @Tracer
        public void runAopDemo() {
            System.out.println("inside runDemo");
        }
    }

    @Test
    void testShowDepends() throws Exception {
        ShowDepends si = new ShowDepends(true, new String[]{"com.demo"}, true);
        si.addJaxClasses();
        si.search(DatabaseStore.class.getName());
    }

    @Test
    void testShowDepend() throws Exception {
        ShowDepend si = new ShowDepend(true, new String[]{"com.demo"}, true);
        si.addClassDepend(DatabaseStore.class.getName());
        si.printDotSyntax(System.out);
    }

    @Test
    void testAop() throws Exception {
        DynamicLoad.checkAdviceClassLoaded();
        DynamicLoad.checkAspectJAgentLoaded();
        DynamicLoad.checkAdviceClassLoaded();
        new Demo().runAopDemo();
    }
}
