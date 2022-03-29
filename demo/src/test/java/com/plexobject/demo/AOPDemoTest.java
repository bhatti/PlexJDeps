package com.plexobject.demo;

import com.plexobject.aop.DynamicLoad;
import com.plexobject.aop.Trace;
import com.plexobject.aop.TraceCollector;
import com.plexobject.demo.dao.DDBMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class AOPDemoTest {
    @BeforeEach
    void setup() throws Exception {
        DynamicLoad.checkAdviceClassLoaded();
        DynamicLoad.checkAspectJAgentLoaded();
        DynamicLoad.checkAdviceClassLoaded();
    }

    @AfterEach
    void teardown() throws Exception {
        for (List<Trace> traces : TraceCollector.getInstance().getAll().values()) {
            for (Trace trace : traces) {
                System.out.println(trace);
            }
        }
    }

    @Test
    void testAop() throws Exception {
        new AOPDemo().runAopDemo();
    }

    @Test
    void testDDB() throws Exception {
        new DDBMapper().saveCatalog();
        new DDBMapper().saveProduct();
    }
}
