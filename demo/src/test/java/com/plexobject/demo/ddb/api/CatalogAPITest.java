package com.plexobject.demo.ddb.api;

import com.plexobject.aop.Trace;
import com.plexobject.aop.TraceCollector;
import com.plexobject.demo.ddb.model.CatalogItem;
import com.plexobject.demo.ddb.service.CatalogService;
import com.plexobject.deps.ShowDepend;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class CatalogAPITest {
    CatalogAPI api = new CatalogAPI();

    @AfterEach
    void teardown() {
        System.out.println("*** Traces ****");
        TraceCollector.getInstance().dump();
    }

    @Test
    void testSaveCatalogTrace() {
        api.saveCatalog();
        for (List<Trace> traces : TraceCollector.getInstance().getAll().values()) {
            for (Trace trace : traces) {
                System.out.println(trace.buildSequenceConfig());
            }
        }
    }

    @Test
    void testSaveCatalogDeps() {
        ShowDepend si = new ShowDepend(false, new String[]{"com.plexobject.demo"}, false);
        si.addClassDepend(CatalogAPI.class.getName());
        si.printDotSyntax(System.out, "");
    }
}
