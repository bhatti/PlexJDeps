package com.plexobject;

import com.plexobject.aop.DynamicLoad;
import com.plexobject.aop.Trace;
import com.plexobject.aop.TraceCollector;
import com.plexobject.aop.Tracer;
import com.plexobject.db.DatabaseStore;
import com.plexobject.deps.ShowDepend;
import com.plexobject.deps.ShowDepends;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class AOPTraceTest {
    static void alice() {
        bob("hello");
    }

    static String bob(String s) {
        bib("hello");
        return "success";
    }

    @Tracer
    static String bib(String s) {
        return "done";
    }

    static class Demo {
        @Tracer
        public void runAopDemo() {
            System.out.println("inside runDemo");
        }
    }

    @AfterEach
    void teardown() {
        System.out.println("*** Traces ****");
        TraceCollector.getInstance().dump();
    }

    @Test
    void testShowDepends() throws Exception {
        ShowDepends si = new ShowDepends(true, new String[]{"com.demo"}, true);
        si.addJaxClasses();
        si.search(DatabaseStore.class.getName());
    }

    @Test
    void testShowDepend() {
        ShowDepend si = new ShowDepend(true, new String[]{"com.demo"}, true);
        si.addClassDepend(DatabaseStore.class.getName());
        si.printDotSyntax(System.out, "");
    }

    @Test
    void testAliceBob() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        alice();
        for (List<Trace> traces : TraceCollector.getInstance().getAll().values()) {
            for (Trace trace : traces) {
                System.out.println(trace.buildSequenceConfig());
            }
        }
    }
    @Test
    void testShowSequence() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        DynamicLoad.checkAdviceClassLoaded();
        DynamicLoad.checkAspectJAgentLoaded();
        DynamicLoad.checkAdviceClassLoaded();
        new Demo().runAopDemo();
        for (List<Trace> traces : TraceCollector.getInstance().getAll().values()) {
            for (Trace trace : traces) {
                System.out.println(trace.buildSequenceConfig());
            }
        }
    }
}
